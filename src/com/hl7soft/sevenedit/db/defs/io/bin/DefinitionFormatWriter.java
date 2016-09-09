package com.hl7soft.sevenedit.db.defs.io.bin;

import com.hl7soft.sevenedit.db.defs.FieldDefinition;
import com.hl7soft.sevenedit.db.defs.FieldEntry;
import com.hl7soft.sevenedit.db.defs.IDefinitionFactory;
import com.hl7soft.sevenedit.db.defs.IFieldEntryContainer;
import com.hl7soft.sevenedit.db.defs.ISegmentEntry;
import com.hl7soft.sevenedit.db.defs.ISegmentEntryContainer;
import com.hl7soft.sevenedit.db.defs.MessageDefinition;
import com.hl7soft.sevenedit.db.defs.SegmentDefinition;
import com.hl7soft.sevenedit.db.util.XOROutputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

public class DefinitionFormatWriter {
	public static final int VERSION_3_0 = 1;
	public static final int VERSION_3_1 = 2;
	int formatVersion = 2;
	DefinitionWriterListener listener;
	int stepCnt;
	int totalSteps;
	boolean legacyFormat;
	Map<Object, Integer> definitionSequenceNumber;

	public void write(IDefinitionFactory factory, OutputStream os) {
		if ((factory == null) || (os == null)) {
			return;
		}

		try {
			if ((this.formatVersion != 1) && (this.formatVersion != 2)) {
				throw new RuntimeException("Not supported version: " + this.formatVersion);
			}

			if (this.listener != null) {
				this.listener.onEvent(new DefinitionWriterEvent(this, 1, -1, -1));
			}

			this.stepCnt = 0;
			this.totalSteps = enumerateDefinitions(factory);
			ZipOutputStream out = new ZipOutputStream(os);

			writeFormat30(factory, out);

			out.finish();
			out.flush();
			out.close();
		} catch (Exception e) {
			throw new RuntimeException("Error writing definition format.", e);
		} finally {
			if (this.listener != null)
				this.listener.onEvent(new DefinitionWriterEvent(this, 2, -1, -1));
		}
	}

	private int enumerateDefinitions(IDefinitionFactory factory) {
		int total = 0;

		this.definitionSequenceNumber = new HashMap();

		List versions = factory.getVersions();

		if (versions != null) {
			int i = 0;
			for (int n = versions.size(); i < n; i++) {
				String version = (String) versions.get(i);

				List messageNames = factory.getMessages(version);
				if (messageNames != null) {
					int j = 0;
					for (int n2 = messageNames.size(); j < n2; j++) {
						String name = ((String) messageNames.get(j)).toUpperCase();
						MessageDefinition msgDef = factory.getMessageDefinition(version, name);
						if (msgDef != null) {
							this.definitionSequenceNumber.put(msgDef, new Integer(total++));
						}
					}
				}

				List segmentNames = factory.getSegments(version);
				if (segmentNames != null) {
					int j = 0;
					for (int n2 = segmentNames.size(); j < n2; j++) {
						String name = ((String) segmentNames.get(j)).toUpperCase();
						SegmentDefinition sgmDef = factory.getSegmentDefinition(version, name);
						if (sgmDef != null) {
							this.definitionSequenceNumber.put(sgmDef, new Integer(total++));
						}
					}
				}

				List fieldNames = factory.getFields(version);
				if (fieldNames != null) {
					int j = 0;
					for (int n2 = fieldNames.size(); j < n2; j++) {
						String name = ((String) fieldNames.get(j)).toUpperCase();
						FieldDefinition fldDef = factory.getFieldDefinition(version, name);
						if (fldDef != null) {
							this.definitionSequenceNumber.put(fldDef, new Integer(total++));
						}
					}
				}
			}
		}

		return total;
	}

	private void writeFormat30(IDefinitionFactory factory, ZipOutputStream out) throws Exception {
		writeDescriptor(factory, out);
		writeMessageDefinitions(factory, out);
		writeSegmentDefinitions(factory, out);
		writeFieldDefinitions(factory, out);
	}

	private void writeDescriptor(IDefinitionFactory factory, ZipOutputStream out) throws Exception {
		if (this.formatVersion == 1)
			writeDescriptor30(factory, out);
		else if (this.formatVersion == 2)
			writeDescriptor31(factory, out);
	}

	private OutputStream encryptionFilter(OutputStream os) {
		if (os == null) {
			return null;
		}

		if (this.formatVersion == 1) {
			return new XOROutputStream(os, "@22#1~#");
		}

		return os;
	}

	void writeMessageDefinitions(IDefinitionFactory factory, ZipOutputStream out) throws Exception {
		List versions = factory.getVersions();

		if (versions == null) {
			return;
		}

		int i = 0;
		for (int n = versions.size(); i < n; i++) {
			String version = (String) versions.get(i);
			List messageNames = factory.getMessages(version);

			if (messageNames != null) {
				int j = 0;
				for (int n2 = messageNames.size(); j < n2; j++) {
					String messageName = (String) messageNames.get(j);
					messageName = messageName.toUpperCase();
					MessageDefinition msgDfn = factory.getMessageDefinition(version, messageName);

					if (msgDfn != null) {
						if (this.formatVersion == 1) {
							writeZipFileEntry(out, "versions/" + version + "/messages/" + messageName + ".md",
									writeMessageDefinition(msgDfn));
						} else if (this.formatVersion == 2) {
							writeZipFileEntry(out, "data/defs/d" + this.definitionSequenceNumber.get(msgDfn),
									writeMessageDefinition(msgDfn));
						}

						if (this.listener != null)
							this.listener.onEvent(new DefinitionWriterEvent(this, 3, this.stepCnt++, this.totalSteps));
					}
				}
			}
		}
	}

	private byte[] writeMessageDefinition(MessageDefinition messageDefinition) throws Exception {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		writeMessageDefinition(messageDefinition, encryptionFilter(os));
		os.close();
		return os.toByteArray();
	}

	void writeSegmentDefinitions(IDefinitionFactory factory, ZipOutputStream out) throws Exception {
		List versions = factory.getVersions();

		if (versions == null) {
			return;
		}

		int i = 0;
		for (int n = versions.size(); i < n; i++) {
			String version = (String) versions.get(i);
			List segmentNames = factory.getSegments(version);

			if (segmentNames != null) {
				int j = 0;
				for (int n2 = segmentNames.size(); j < n2; j++) {
					String segmentName = (String) segmentNames.get(j);
					segmentName = segmentName.toUpperCase();
					SegmentDefinition sgmDfn = factory.getSegmentDefinition(version, segmentName);

					if (sgmDfn != null) {
						if (this.formatVersion == 1) {
							writeZipFileEntry(out, "versions/" + version + "/segments/" + segmentName + ".sd",
									writeSegmentDefinition(sgmDfn));
						} else if (this.formatVersion == 2) {
							writeZipFileEntry(out, "data/defs/d" + this.definitionSequenceNumber.get(sgmDfn),
									writeSegmentDefinition(sgmDfn));
						}

						if (this.listener != null)
							this.listener.onEvent(new DefinitionWriterEvent(this, 3, this.stepCnt++, this.totalSteps));
					}
				}
			}
		}
	}

	private byte[] writeSegmentDefinition(SegmentDefinition segmentDefinition) throws Exception {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		writeSegmentDefinition(segmentDefinition, encryptionFilter(os));
		os.close();
		return os.toByteArray();
	}

	private void writeFieldDefinitions(IDefinitionFactory factory, ZipOutputStream out) throws Exception {
		List versions = factory.getVersions();

		if (versions == null) {
			return;
		}

		int i = 0;
		for (int n = versions.size(); i < n; i++) {
			String version = (String) versions.get(i);
			List fieldNames = factory.getFields(version);

			if (fieldNames != null) {
				int j = 0;
				for (int n2 = fieldNames.size(); j < n2; j++) {
					String fieldName = (String) fieldNames.get(j);
					fieldName = fieldName.toUpperCase();
					FieldDefinition fieldDefinition = factory.getFieldDefinition(version, fieldName);

					if (fieldDefinition != null) {
						if (this.formatVersion == 1) {
							writeZipFileEntry(out, "versions/" + version + "/fields/" + fieldName + ".fd",
									writeFieldDefinition(fieldDefinition));
						} else if (this.formatVersion == 2) {
							writeZipFileEntry(out, "data/defs/d" + this.definitionSequenceNumber.get(fieldDefinition),
									writeFieldDefinition(fieldDefinition));
						}

						if (this.listener != null)
							this.listener.onEvent(new DefinitionWriterEvent(this, 3, this.stepCnt++, this.totalSteps));
					}
				}
			}
		}
	}

	private byte[] writeFieldDefinition(FieldDefinition dfn) throws Exception {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		writeFieldDefinition(dfn, encryptionFilter(os));
		os.close();
		return os.toByteArray();
	}

	private void writeDescriptor30(IDefinitionFactory factory, ZipOutputStream out) throws Exception {
		FormatDescriptor descriptor = new FormatDescriptor();
		descriptor.setFormatName("hl7-defs-bin");
		descriptor.setFormatVersion("3.0");

		List versions = factory.getVersions();

		if (versions != null) {
			int i = 0;
			for (int n = versions.size(); i < n; i++) {
				String version = (String) versions.get(i);

				List messageNames = factory.getMessages(version);
				if (messageNames != null) {
					int j = 0;
					for (int n2 = messageNames.size(); j < n2; j++) {
						String name = ((String) messageNames.get(j)).toUpperCase();
						if (factory.getMessageDefinition(version, name) != null) {
							descriptor.addMessageName(version, name);
						}
					}
				}

				List segmentNames = factory.getSegments(version);
				if (segmentNames != null) {
					int j = 0;
					for (int n2 = segmentNames.size(); j < n2; j++) {
						String name = ((String) segmentNames.get(j)).toUpperCase();
						if (factory.getSegmentDefinition(version, name) != null) {
							descriptor.addSegmentName(version, name);
						}
					}
				}

				List fieldNames = factory.getFields(version);
				if (fieldNames != null) {
					int j = 0;
					for (int n2 = fieldNames.size(); j < n2; j++) {
						String name = ((String) fieldNames.get(j)).toUpperCase();
						if (factory.getFieldDefinition(version, name) != null) {
							descriptor.addFieldName(version, name);
						}
					}
				}
			}
		}

		ByteArrayOutputStream os = new ByteArrayOutputStream();
		descriptor.write(os);
		os.close();

		writeZipFileEntry(out, "descriptor", os.toByteArray());
	}

	private void writeDescriptor31(IDefinitionFactory factory, ZipOutputStream out) throws Exception {
		ByteArrayOutputStream os = new ByteArrayOutputStream();

		OutputStreamWriter writer = new OutputStreamWriter(os, "UTF-8");
		XMLOutputFactory xmlFactory = XMLOutputFactory.newInstance();
		XMLStreamWriter xwriter = xmlFactory.createXMLStreamWriter(writer);

		xwriter.writeStartDocument("UTF-8", "1.0");
		xwriter.writeStartElement("format");
		xwriter.writeAttribute("name", "hl7-defs");
		xwriter.writeAttribute("version", "3.1");

		List<String> versions = factory.getVersions();

		if (versions != null) {
			xwriter.writeStartElement("defs");
			xwriter.writeAttribute("loc", "data/defs");

			for (String version : versions) {
				xwriter.writeStartElement("version");
				xwriter.writeAttribute("name", version);

				List messages = factory.getMessages(version);
				if (messages != null) {
					xwriter.writeStartElement("messages");

					int j = 0;
					for (int n2 = messages.size(); j < n2; j++) {
						String name = ((String) messages.get(j)).toUpperCase();
						String descr = factory.getMessageDescription(version, name);
						MessageDefinition msgDef = factory.getMessageDefinition(version, name);
						if (msgDef != null) {
							xwriter.writeStartElement("message");
							xwriter.writeAttribute("name", name);
							if (!isEmpty(descr)) {
								xwriter.writeAttribute("descr", descr);
							}
							xwriter.writeAttribute("ref", "d" + this.definitionSequenceNumber.get(msgDef));
							xwriter.writeEndElement();
						}
					}

					xwriter.writeEndElement();
				}

				List segments = factory.getSegments(version);
				if (segments != null) {
					xwriter.writeStartElement("segments");

					int j = 0;
					for (int n2 = segments.size(); j < n2; j++) {
						String name = ((String) segments.get(j)).toUpperCase();
						String descr = factory.getSegmentDescription(version, name);
						SegmentDefinition sgmDef = factory.getSegmentDefinition(version, name);
						if (sgmDef != null) {
							xwriter.writeStartElement("segment");
							xwriter.writeAttribute("name", name);
							if (!isEmpty(descr)) {
								xwriter.writeAttribute("descr", descr);
							}
							xwriter.writeAttribute("ref", "d" + this.definitionSequenceNumber.get(sgmDef));
							xwriter.writeEndElement();
						}
					}

					xwriter.writeEndElement();
				}

				List fields = factory.getFields(version);
				if (fields != null) {
					xwriter.writeStartElement("fields");

					int j = 0;
					for (int n2 = fields.size(); j < n2; j++) {
						String name = ((String) fields.get(j)).toUpperCase();
						String descr = factory.getFieldDescription(version, name);
						FieldDefinition fldDef = factory.getFieldDefinition(version, name);
						if (fldDef != null) {
							xwriter.writeStartElement("field");
							xwriter.writeAttribute("name", name);
							if (!isEmpty(descr)) {
								xwriter.writeAttribute("descr", descr);
							}
							xwriter.writeAttribute("ref", "d" + this.definitionSequenceNumber.get(fldDef));
							xwriter.writeEndElement();
						}
					}

					xwriter.writeEndElement();
				}

				xwriter.writeEndElement();
			}

			xwriter.writeEndElement();
		}

		xwriter.writeEndElement();
		xwriter.writeEndDocument();
		xwriter.flush();
		os.close();

		writeZipFileEntry(out, "format.xml", os.toByteArray());
	}

	private boolean isEmpty(String str) {
		return (str == null) || (str.length() == 0);
	}

	public void removeListener(DefinitionWriterListener listener) {
		this.listener = null;
	}

	public void addListener(DefinitionWriterListener listener) {
		this.listener = listener;
	}

	private static void writeZipFileEntry(ZipOutputStream zos, String zipEntryName, byte[] byteArray)
			throws IOException {
		int byteArraySize = byteArray.length;

		CRC32 crc = new CRC32();
		crc.update(byteArray, 0, byteArraySize);

		ZipEntry entry = new ZipEntry(zipEntryName);
		entry.setMethod(8);
		entry.setSize(byteArraySize);
		entry.setCrc(crc.getValue());

		zos.putNextEntry(entry);
		zos.write(byteArray, 0, byteArraySize);
		zos.closeEntry();
	}

	public int getFormatVersion() {
		return this.formatVersion;
	}

	public void setFormatVersion(int formatVersion) {
		this.formatVersion = formatVersion;
	}

	public void writeMessageDefinition(MessageDefinition messageDefinition, OutputStream os) throws IOException {
		if (messageDefinition == null) {
			return;
		}

		writeString(messageDefinition.getName(), os);
		writeString(messageDefinition.getVersion(), os);
		writeString(messageDefinition.getDescription(), os);
		writeString(messageDefinition.getNotes(), os);

		writeSegmentEntries(messageDefinition, os);
	}

	private void writeSegmentEntries(ISegmentEntryContainer container, OutputStream os) throws IOException {
		writeInt(container.getSegmentEntriesCount(), os);

		int i = 0;
		for (int n = container.getSegmentEntriesCount(); i < n; i++) {
			ISegmentEntry segmentEntry = container.getSegmentEntry(i);

			writeByte(convertSegmentEntryType(segmentEntry.getType()), os);
			writeString(segmentEntry.getName(), os);
			writeString(segmentEntry.getDescription(), os);
			writeString(segmentEntry.getNotes(), os);
			writeInt(segmentEntry.getMinCount(), os);
			writeInt(segmentEntry.getMaxCount(), os);

			writeSegmentEntries(segmentEntry, os);
		}
	}

	public void writeSegmentDefinition(SegmentDefinition segmentDefinition, OutputStream os) throws IOException {
		if (segmentDefinition == null) {
			return;
		}

		writeString(segmentDefinition.getName(), os);
		writeString(segmentDefinition.getVersion(), os);
		writeString(segmentDefinition.getDescription(), os);
		writeString(segmentDefinition.getNotes(), os);

		writeFieldEntries(segmentDefinition, os);
	}

	public void writeFieldDefinition(FieldDefinition fieldDefinition, OutputStream os) throws IOException {
		if (fieldDefinition == null) {
			return;
		}

		writeString(fieldDefinition.getName(), os);
		writeString(fieldDefinition.getVersion(), os);
		writeString(fieldDefinition.getDescription(), os);
		writeString(fieldDefinition.getNotes(), os);
		writeBoolean(fieldDefinition.isPrimitive(), os);

		writeFieldEntries(fieldDefinition, os);
	}

	public void writeFieldEntries(IFieldEntryContainer container, OutputStream os) throws IOException {
		writeInt(container.getFieldEntriesCount(), os);
		int i = 0;
		for (int n = container.getFieldEntriesCount(); i < n; i++) {
			FieldEntry fieldEntry = container.getFieldEntry(i);

			writeString(fieldEntry.getName(), os);
			writeString(fieldEntry.getDescription(), os);
			writeString(fieldEntry.getNotes(), os);
			writeInt(fieldEntry.getMaxLength(), os);
			writeByte(fieldEntry.getOptionality(), os);
			writeInt(fieldEntry.getTableNumber(), os);
			writeInt(fieldEntry.getRepeatCount(), os);
			writeString(fieldEntry.getDefaultValue(), os);
		}
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

	public void writeString(String str, OutputStream out) throws IOException {
		if (this.formatVersion == 1) {
			writeStringDefaultCharset(str, out);
			return;
		}

		writeStringUTF(str, out);
	}

	public void writeStringDefaultCharset(String str, OutputStream out) throws IOException {
		if (str != null) {
			int strLen = str.length();
			writeInt(strLen, out);
			out.write(str.getBytes());
		} else {
			writeInt(-1, out);
		}
	}

	public void writeStringUTF(String str, OutputStream out) throws IOException {
		if (str == null) {
			writeInt(-1, out);
			return;
		}

		byte[] bytes = str.getBytes("UTF-8");
		writeInt(bytes.length, out);
		out.write(bytes);
	}

	public void writeInt(int i, OutputStream out) throws IOException {
		out.write(i & 0xFF);
		out.write(i >> 8 & 0xFF);
		out.write(i >> 16 & 0xFF);
		out.write(i >> 24 & 0xFF);
	}

	public void writeByte(int i, OutputStream out) throws IOException {
		out.write(i & 0xFF);
	}

	public void writeBoolean(boolean b, OutputStream out) throws IOException {
		out.write(b ? 1 : 0);
	}
}