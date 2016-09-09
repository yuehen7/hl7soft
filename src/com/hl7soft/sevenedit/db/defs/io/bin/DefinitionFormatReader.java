package com.hl7soft.sevenedit.db.defs.io.bin;

import com.hl7soft.sevenedit.db.defs.DefinitionFactory;
import com.hl7soft.sevenedit.db.defs.FieldDefinition;
import com.hl7soft.sevenedit.db.defs.FieldEntry;
import com.hl7soft.sevenedit.db.defs.MessageDefinition;
import com.hl7soft.sevenedit.db.defs.SegmentDefinition;
import com.hl7soft.sevenedit.db.defs.SegmentEntry;
import com.hl7soft.sevenedit.db.util.XORInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class DefinitionFormatReader {
	public static final int VERSION_3_0 = 1;
	public static final int VERSION_3_1 = 2;
	int formatVersion = 2;
	File file;
	DefinitionFactory definitionFactory;
	ZipFile zipFile;
	DefinitionReaderListener listener;
	IFormatDescriptor descriptor;

	public DefinitionFormatReader(File file) {
		this.file = file;
	}

	public void read() throws UnknownDefinitionFileFormat {
		try {
			if (this.listener != null) {
				this.listener.onEvent(new DefinitionReaderEvent(this, 1));
			}
			try {
				this.zipFile = new ZipFile(this.file, 1);
			} catch (Exception e) {
				throw new UnknownDefinitionFileFormat("Unknown format.");
			}

			readDescriptor();

			if (this.descriptor == null) {
				throw new UnknownDefinitionFileFormat("Unknown format.");
			}

			if ("3.0".equals(this.descriptor.getFormatVersion()))
				this.formatVersion = 1;
			else if ("3.1".equals(this.descriptor.getFormatVersion()))
				this.formatVersion = 2;
			else {
				throw new UnknownDefinitionFileFormat("Unknown format version.");
			}

			int stepCnt = 0;
			int totalSteps = calculateTotalSteps(this.descriptor);

			List versions = this.descriptor.getVersions();

			DefinitionFactory definitionFactory = new DefinitionFactory();

			if (versions != null) {
				int i = 0;
				for (int n = versions.size(); i < n; i++) {
					String version = (String) versions.get(i);

					List messageNames = this.descriptor.getMessages(version);
					if (messageNames != null) {
						int j = 0;
						for (int n2 = messageNames.size(); j < n2; j++) {
							String name = (String) messageNames.get(j);
							String ref = this.descriptor.getMessageRef(version, name);
							ZipEntry entry = this.zipFile.getEntry(ref);

							if (entry != null) {
								MessageDefinition dfn = readMessageDefinition(
										decryptionFilter(this.zipFile.getInputStream(entry)));
								definitionFactory.addMessageDefinition(dfn);
							}

							if (this.listener != null) {
								this.listener.onEvent(new DefinitionReaderEvent(this, 3, stepCnt++, totalSteps));
							}
						}

					}

					List segmentNames = this.descriptor.getSegments(version);
					if (segmentNames != null) {
						int j = 0;
						for (int n2 = segmentNames.size(); j < n2; j++) {
							String name = (String) segmentNames.get(j);
							String ref = this.descriptor.getSegmentRef(version, name);
							ZipEntry entry = this.zipFile.getEntry(ref);

							if (entry != null) {
								SegmentDefinition dfn = readSegmentDefinition(
										decryptionFilter(this.zipFile.getInputStream(entry)));
								definitionFactory.addSegmentDefinition(dfn);
							}

							if (this.listener != null) {
								this.listener.onEvent(new DefinitionReaderEvent(this, 3, stepCnt++, totalSteps));
							}
						}

					}

					List fieldNames = this.descriptor.getFields(version);
					if (fieldNames != null) {
						int j = 0;
						for (int n2 = fieldNames.size(); j < n2; j++) {
							String name = (String) fieldNames.get(j);
							String ref = this.descriptor.getFieldRef(version, name);
							ZipEntry entry = this.zipFile.getEntry(ref);

							if (entry != null) {
								FieldDefinition fieldDefinition = readFieldDefinition(
										decryptionFilter(this.zipFile.getInputStream(entry)));
								definitionFactory.addFieldDefinition(fieldDefinition);
							}

							if (this.listener != null) {
								this.listener.onEvent(new DefinitionReaderEvent(this, 3, stepCnt++, totalSteps));
							}
						}
					}
				}
			}

			this.definitionFactory = definitionFactory;
		} catch (UnknownDefinitionFileFormat e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException("Error reading definition file.", e);
		} finally {
			if (this.listener != null)
				this.listener.onEvent(new DefinitionReaderEvent(this, 2));
		}
	}

	private void readDescriptor() throws UnknownDefinitionFileFormat, IOException {
		if (this.zipFile.getEntry("format.xml") != null)
			this.descriptor = new XmlDescriptorWrapper(
					FormatDescriptorXml.read(this.zipFile.getInputStream(this.zipFile.getEntry("format.xml"))));
		else if (this.zipFile.getEntry("descriptor") != null)
			this.descriptor = new LegacyFormatDescriptorWrapper(
					FormatDescriptor.read(this.zipFile.getInputStream(this.zipFile.getEntry("descriptor"))));
	}

	private int calculateTotalSteps(IFormatDescriptor descriptor) {
		int total = 0;

		List versions = descriptor.getVersions();
		if (versions != null) {
			int i = 0;
			for (int n = versions.size(); i < n; i++) {
				String version = (String) versions.get(i);

				List messageNames = descriptor.getMessages(version);
				total += (messageNames != null ? messageNames.size() : 0);

				List segmentNames = descriptor.getSegments(version);
				total += (segmentNames != null ? segmentNames.size() : 0);

				List fieldNames = descriptor.getFields(version);
				total += (fieldNames != null ? fieldNames.size() : 0);
			}
		}

		return total;
	}

	private InputStream decryptionFilter(InputStream is) {
		if (is == null) {
			return null;
		}

		if (this.formatVersion == 2) {
			return is;
		}

		return new XORInputStream(is, "@22#1~#");
	}

	public DefinitionFactory getDefinitionFactory() {
		return this.definitionFactory;
	}

	public File getFile() {
		return this.file;
	}

	public DefinitionReaderListener getListener() {
		return this.listener;
	}

	public void addListener(DefinitionReaderListener listener) {
		this.listener = listener;
	}

	public void close() {
		try {
			if (this.zipFile != null)
				this.zipFile.close();
		} catch (Exception e) {
			throw new RuntimeException("Error closing reader.", e);
		}
	}

	public MessageDefinition readMessageDefinition(InputStream is) throws IOException {
		if (is.available() == 0) {
			return null;
		}

		String name = readString(is);
		String version = readString(is);
		String description = readString(is);
		String notes = readString(is);

		MessageDefinition messageDefinition = new MessageDefinition(name, version);
		messageDefinition.setDescription(description);
		messageDefinition.setNotes(notes);

		List sgmEntries = readSegmentEntries(is);
		if (sgmEntries != null) {
			int i = 0;
			for (int n = sgmEntries.size(); i < n; i++) {
				messageDefinition.addSegmentEntry((SegmentEntry) sgmEntries.get(i));
			}
		}

		return messageDefinition;
	}

	private List<SegmentEntry> readSegmentEntries(InputStream is) throws IOException {
		if (is.available() == 0) {
			return null;
		}

		int n = readInt(is);

		List list = null;

		for (int i = 0; i < n; i++) {
			int type = convertSegmentEntryType(readByte(is));
			String name = readString(is);
			String description = readString(is);
			String notes = readString(is);
			int minCount = readInt(is);
			int maxCount = readInt(is);

			SegmentEntry segmentEntry = new SegmentEntry(name);
			segmentEntry.setDescription(description);
			segmentEntry.setType(type);
			segmentEntry.setNotes(notes);
			segmentEntry.setMinCount(minCount);
			segmentEntry.setMaxCount(maxCount);

			List sgmEntries = readSegmentEntries(is);
			if (sgmEntries != null) {
				int j = 0;
				for (int n2 = sgmEntries.size(); j < n2; j++) {
					segmentEntry.addSegmentEntry((SegmentEntry) sgmEntries.get(j));
				}
			}

			if (list == null) {
				list = new ArrayList(1);
			}
			list.add(segmentEntry);
		}

		return list;
	}

	private int convertSegmentEntryType(int type) {
		switch (type) {
		case 1:
			return 1;
		case 2:
			return 2;
		case 3:
			return 3;
		}

		throw new RuntimeException("Unknown segment entry type: " + type);
	}

	public SegmentDefinition readSegmentDefinition(InputStream is) throws IOException {
		if (is.available() == 0) {
			return null;
		}

		String name = readString(is);
		String version = readString(is);
		String description = readString(is);
		String notes = readString(is);

		SegmentDefinition segmentDefinition = new SegmentDefinition(name, version);
		segmentDefinition.setDescription(description);
		segmentDefinition.setNotes(notes);

		List entries = readFieldEntries(is);
		if (entries != null) {
			int i = 0;
			for (int n = entries.size(); i < n; i++) {
				segmentDefinition.addFieldEntry((FieldEntry) entries.get(i));
			}
		}

		return segmentDefinition;
	}

	public FieldDefinition readFieldDefinition(InputStream is) throws IOException {
		if (is.available() == 0) {
			return null;
		}

		String name = readString(is);
		String version = readString(is);
		String description = readString(is);
		String notes = readString(is);
		boolean primitive = readBoolean(is);

		FieldDefinition fieldDefinition = new FieldDefinition(name, version);
		fieldDefinition.setDescription(description);
		fieldDefinition.setNotes(notes);

		List entries = readFieldEntries(is);
		if (entries != null) {
			int i = 0;
			for (int n = entries.size(); i < n; i++) {
				fieldDefinition.addFieldEntry((FieldEntry) entries.get(i));
			}
		}

		return fieldDefinition;
	}

	private List<FieldEntry> readFieldEntries(InputStream is) throws IOException {
		if (is.available() == 0) {
			return null;
		}

		List list = null;

		int n = readInt(is);

		for (int i = 0; i < n; i++) {
			String name = readString(is);
			String description = readString(is);
			String notes = readString(is);
			int maxLength = readInt(is);
			int opt = readByte(is);
			int tableNumber = readInt(is);
			int repeatCount = readInt(is);
			String defaultValue = readString(is);

			FieldEntry fieldEntry = new FieldEntry(name);
			fieldEntry.setDescription(description);
			fieldEntry.setNotes(notes);
			fieldEntry.setMaxLength(maxLength);
			fieldEntry.setOptionality(opt);
			fieldEntry.setTableNumber(tableNumber);
			fieldEntry.setRepeatCount(repeatCount);
			fieldEntry.setDefaultValue(defaultValue);

			if (list == null) {
				list = new ArrayList(1);
			}
			list.add(fieldEntry);
		}

		return list;
	}

	public boolean readBoolean(InputStream is) throws IOException {
		return is.read() == 1;
	}

	public int readByte(InputStream is) throws IOException {
		return is.read();
	}

	public int readInt(InputStream is) throws IOException {
		int i = 0;
		i = is.read();
		i |= is.read() << 8;
		i |= is.read() << 16;
		i |= is.read() << 24;
		return i;
	}

	public String readString(InputStream is) throws IOException {
		if (this.formatVersion == 1) {
			return readStringDefaultCharset(is);
		}

		return readStringUTF(is);
	}

	public String readStringDefaultCharset(InputStream is) throws IOException {
		int strLen = readInt(is);
		if (strLen == -1) {
			return null;
		}
		byte[] buf = new byte[strLen];
		is.read(buf);
		String str = new String(buf);
		return str;
	}

	public String readStringUTF(InputStream is) throws IOException {
		int len = readInt(is);
		if (len == -1) {
			return null;
		}
		byte[] buf = new byte[len];
		is.read(buf);
		return new String(buf, "UTF-8");
	}

	class XmlDescriptorWrapper implements DefinitionFormatReader.IFormatDescriptor {
		FormatDescriptorXml descriptor;

		public XmlDescriptorWrapper(FormatDescriptorXml descriptor) {
			this.descriptor = descriptor;
		}

		public String getMessageDescription(String version, String name) {
			return null;
		}

		public String getMessageRef(String version, String name) {
			return this.descriptor.getDefsLocation() + "/" + this.descriptor.getMessageRef(version, name);
		}

		public List<String> getMessages(String version) {
			return this.descriptor.getMessages(version);
		}

		public String getSegmentDescription(String version, String name) {
			return null;
		}

		public String getSegmentRef(String version, String name) {
			return this.descriptor.getDefsLocation() + "/" + this.descriptor.getSegmentRef(version, name);
		}

		public List<String> getSegments(String version) {
			return this.descriptor.getSegments(version);
		}

		public List<String> getVersions() {
			return this.descriptor.getVersions();
		}

		public String getFieldDescription(String version, String name) {
			return null;
		}

		public String getFieldRef(String version, String name) {
			return this.descriptor.getDefsLocation() + "/" + this.descriptor.getFieldRef(version, name);
		}

		public List<String> getFields(String version) {
			return this.descriptor.getFields(version);
		}

		public String getFormatVersion() {
			return this.descriptor.getFormatVersion();
		}
	}

	class LegacyFormatDescriptorWrapper implements DefinitionFormatReader.IFormatDescriptor {
		FormatDescriptor descriptor;

		public LegacyFormatDescriptorWrapper(FormatDescriptor descriptor) {
			this.descriptor = descriptor;
		}

		public String getMessageRef(String version, String name) {
			return "versions/" + version + "/messages/" + name + ".md";
		}

		public List<String> getMessages(String version) {
			return this.descriptor.getMessageNames(version);
		}

		public String getSegmentRef(String version, String name) {
			return "versions/" + version + "/segments/" + name + ".sd";
		}

		public List<String> getSegments(String version) {
			return this.descriptor.getSegmentNames(version);
		}

		public List<String> getVersions() {
			return this.descriptor.getVersions();
		}

		public String getFieldRef(String version, String name) {
			return "versions/" + version + "/fields/" + name + ".fd";
		}

		public List<String> getFields(String version) {
			return this.descriptor.getFieldNames(version);
		}

		public String getFormatVersion() {
			return this.descriptor.getFormatVersion();
		}
	}

	static abstract interface IFormatDescriptor {
		public abstract String getFormatVersion();

		public abstract List<String> getVersions();

		public abstract List<String> getMessages(String paramString);

		public abstract String getMessageRef(String paramString1, String paramString2);

		public abstract List<String> getSegments(String paramString);

		public abstract String getSegmentRef(String paramString1, String paramString2);

		public abstract List<String> getFields(String paramString);

		public abstract String getFieldRef(String paramString1, String paramString2);
	}
}