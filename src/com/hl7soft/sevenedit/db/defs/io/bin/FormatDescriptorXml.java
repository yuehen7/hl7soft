package com.hl7soft.sevenedit.db.defs.io.bin;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public class FormatDescriptorXml {
	String formaName;
	String formatVersion;
	String defsLocation;
	Set<String> versions;
	Map<String, VersionRecord> versionRecords;

	public FormatDescriptorXml() {
		this.formaName = "hl7-defs";

		this.formatVersion = "3.1";

		this.defsLocation = "data/defs";
	}

	public List<String> getVersions() {
		return this.versions != null ? new ArrayList(this.versions) : null;
	}

	public void addVersion(String version) {
		if (version == null) {
			return;
		}

		if (this.versions == null) {
			this.versions = new TreeSet();
		}
		this.versions.add(version);
	}

	public void addMessageRecord(String version, String name, String description, String ref) {
		if ((version == null) || (name == null)) {
			return;
		}

		if (this.versionRecords == null) {
			this.versionRecords = new HashMap();
		}

		VersionRecord record = (VersionRecord) this.versionRecords.get(version);
		if (record == null) {
			record = new VersionRecord(version);
			this.versionRecords.put(version, record);
		}

		record.addMessageRecord(name, description, ref);
	}

	public List<String> getMessages(String version) {
		if (version == null) {
			return null;
		}

		return this.versionRecords != null ? ((VersionRecord) this.versionRecords.get(version)).getMessages() : null;
	}

	public String getMessageDescription(String version, String name) {
		if ((version == null) || (name == null)) {
			return null;
		}

		VersionRecord record = (VersionRecord) this.versionRecords.get(version);
		if (record == null) {
			return null;
		}

		MessageRecord messageRecord = record.getMessageRecord(name);
		return messageRecord != null ? messageRecord.getDescription() : null;
	}

	public String getMessageRef(String version, String name) {
		if ((version == null) || (name == null)) {
			return null;
		}

		VersionRecord record = (VersionRecord) this.versionRecords.get(version);
		if (record == null) {
			return null;
		}

		MessageRecord messageRecord = record.getMessageRecord(name);
		return messageRecord != null ? messageRecord.getRef() : null;
	}

	public void addSegmentRecord(String version, String name, String description, String ref) {
		if ((version == null) || (name == null)) {
			return;
		}

		if (this.versionRecords == null) {
			this.versionRecords = new HashMap();
		}

		VersionRecord record = (VersionRecord) this.versionRecords.get(version);
		if (record == null) {
			record = new VersionRecord(version);
			this.versionRecords.put(version, record);
		}

		record.addSegmentRecord(name, description, ref);
	}

	public List<String> getSegments(String version) {
		if (version == null) {
			return null;
		}

		return this.versionRecords != null ? ((VersionRecord) this.versionRecords.get(version)).getSegments() : null;
	}

	public String getSegmentDescription(String version, String name) {
		if ((version == null) || (name == null)) {
			return null;
		}

		VersionRecord record = (VersionRecord) this.versionRecords.get(version);
		if (record == null) {
			return null;
		}

		SegmentRecord segmentRecord = record.getSegmentRecord(name);
		return segmentRecord != null ? segmentRecord.getDescription() : null;
	}

	public String getSegmentRef(String version, String name) {
		if ((version == null) || (name == null)) {
			return null;
		}

		VersionRecord record = (VersionRecord) this.versionRecords.get(version);
		if (record == null) {
			return null;
		}

		SegmentRecord segmentRecord = record.getSegmentRecord(name);
		return segmentRecord != null ? segmentRecord.getRef() : null;
	}

	public void addFieldRecord(String version, String name, String description, String ref) {
		if ((version == null) || (name == null)) {
			return;
		}

		if (this.versionRecords == null) {
			this.versionRecords = new HashMap();
		}

		VersionRecord record = (VersionRecord) this.versionRecords.get(version);
		if (record == null) {
			record = new VersionRecord(version);
			this.versionRecords.put(version, record);
		}

		record.addFieldRecord(name, description, ref);
	}

	public List<String> getFields(String version) {
		if (version == null) {
			return null;
		}

		return this.versionRecords != null ? ((VersionRecord) this.versionRecords.get(version)).getFields() : null;
	}

	public String getFieldDescription(String version, String name) {
		if ((version == null) || (name == null)) {
			return null;
		}

		VersionRecord record = (VersionRecord) this.versionRecords.get(version);
		if (record == null) {
			return null;
		}

		FieldRecord fieldRecord = record.getFieldRecord(name);
		return fieldRecord != null ? fieldRecord.getDescription() : null;
	}

	public String getFieldRef(String version, String name) {
		if (version == null) {
			return null;
		}

		VersionRecord record = (VersionRecord) this.versionRecords.get(version);
		if (record == null) {
			return null;
		}

		FieldRecord fieldRecord = record.getFieldRecord(name);
		return fieldRecord != null ? fieldRecord.getRef() : null;
	}

	public void write(OutputStream os) {
		try {
			OutputStreamWriter writer = new OutputStreamWriter(os, "UTF-8");
			XMLOutputFactory xmlFactory = XMLOutputFactory.newInstance();
			XMLStreamWriter xwriter = xmlFactory.createXMLStreamWriter(writer);

			xwriter.writeStartDocument("UTF-8", "1.0");
			xwriter.writeStartElement("format");
			xwriter.writeAttribute("name", "hl7-defs");
			xwriter.writeAttribute("version", this.formatVersion);

			List<String> versions = getVersions();

			if (versions != null) {
				xwriter.writeStartElement("defs");
				xwriter.writeAttribute("loc", this.defsLocation);

				for (String version : versions) {
					xwriter.writeStartElement("version");
					xwriter.writeAttribute("name", version);

					List messages = getMessages(version);
					if (messages != null) {
						xwriter.writeStartElement("messages");

						int j = 0;
						for (int n2 = messages.size(); j < n2; j++) {
							String name = ((String) messages.get(j)).toUpperCase();
							String descr = getMessageDescription(version, name);
							String ref = getMessageRef(version, name);
							xwriter.writeStartElement("message");
							xwriter.writeAttribute("name", name);
							if (!isEmpty(descr)) {
								xwriter.writeAttribute("descr", descr);
							}
							xwriter.writeAttribute("ref", ref);
							xwriter.writeEndElement();
						}

						xwriter.writeEndElement();
					}

					List segments = getSegments(version);
					if (segments != null) {
						xwriter.writeStartElement("segments");

						int j = 0;
						for (int n2 = segments.size(); j < n2; j++) {
							String name = ((String) segments.get(j)).toUpperCase();
							String descr = getSegmentDescription(version, name);
							String ref = getSegmentRef(version, name);
							xwriter.writeStartElement("segment");
							xwriter.writeAttribute("name", name);
							if (!isEmpty(descr)) {
								xwriter.writeAttribute("descr", descr);
							}
							xwriter.writeAttribute("ref", ref);
							xwriter.writeEndElement();
						}

						xwriter.writeEndElement();
					}

					List fields = getFields(version);
					if (fields != null) {
						xwriter.writeStartElement("fields");

						int j = 0;
						for (int n2 = fields.size(); j < n2; j++) {
							String name = ((String) fields.get(j)).toUpperCase();
							String descr = getFieldDescription(version, name);
							String ref = getFieldRef(version, name);
							xwriter.writeStartElement("field");
							xwriter.writeAttribute("name", name);
							if (!isEmpty(descr)) {
								xwriter.writeAttribute("descr", descr);
							}
							xwriter.writeAttribute("ref", ref);
							xwriter.writeEndElement();
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
		} catch (Exception e) {
			throw new RuntimeException("Error writing descriptor.", e);
		}
	}

	private boolean isEmpty(String str) {
		return (str == null) || (str.length() == 0);
	}

	public static FormatDescriptorXml read(InputStream is) throws UnknownDefinitionFileFormat {
		try {
			XMLEventReader reader = XMLInputFactory.newInstance().createXMLEventReader(is, "UTF-8");

			FormatDescriptorXml d = null;

			String currentVersion = null;

			while (reader.hasNext()) {
				XMLEvent event = reader.nextEvent();

				if (event.isStartElement()) {
					StartElement element = (StartElement) event;
					String tagName = element.getName().getLocalPart();

					if ("format".equals(tagName)) {
						d = new FormatDescriptorXml();

						String formatName = getAttributeValue(element, "name");
						String formatVersion = getAttributeValue(element, "version");

						d.setFormatVersion(formatVersion);

						if (!"hl7-defs".equals(formatName)) {
							throw new UnknownDefinitionFileFormat("Unknown format: " + formatName);
						}

					} else if ("defs".equals(tagName)) {
						d.setDefsLocation(getAttributeValue(element, "loc"));
					} else if ("version".equals(tagName)) {
						String name = getAttributeValue(element, "name");

						if (name == null) {
							throw new RuntimeException("Missing version name.");
						}

						currentVersion = name;
						d.addVersion(currentVersion);
					} else if ("message".equals(tagName)) {
						String name = getAttributeValue(element, "name");
						String description = getAttributeValue(element, "descr");
						String ref = getAttributeValue(element, "ref");

						d.addMessageRecord(currentVersion, name, description, ref);
					} else if ("segment".equals(tagName)) {
						String name = getAttributeValue(element, "name");
						String description = getAttributeValue(element, "descr");
						String ref = getAttributeValue(element, "ref");

						d.addSegmentRecord(currentVersion, name, description, ref);
					} else if ("field".equals(tagName)) {
						String name = getAttributeValue(element, "name");
						String description = getAttributeValue(element, "descr");
						String ref = getAttributeValue(element, "ref");

						d.addFieldRecord(currentVersion, name, description, ref);
					}
				} else if (event.isEndElement()) {
					EndElement element = (EndElement) event;
					String tagName = element.getName().getLocalPart();

					if ("version".equals(tagName)) {
						currentVersion = null;
					}
				}
			}

			reader.close();

			return d;
		} catch (UnknownDefinitionFileFormat e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException("Error reading descriptor.", e);
		}
	}

	private static String getAttributeValue(StartElement element, String name) {
		Attribute attribute = element.getAttributeByName(new QName(name));
		return attribute != null ? attribute.getValue() : null;
	}

	public String getFormatVersion() {
		return this.formatVersion;
	}

	public void setFormatVersion(String formatVersion) {
		this.formatVersion = formatVersion;
	}

	public String getDefsLocation() {
		return this.defsLocation;
	}

	public void setDefsLocation(String defsLocation) {
		this.defsLocation = defsLocation;
	}

	class FieldRecord {
		String name;
		String description;
		String ref;

		public FieldRecord(String name, String description, String ref) {
			this.name = name;
			this.description = description;
			this.ref = ref;
		}

		public String getName() {
			return this.name;
		}

		public String getDescription() {
			return this.description;
		}

		public String getRef() {
			return this.ref;
		}
	}

	class SegmentRecord {
		String name;
		String description;
		String ref;

		public SegmentRecord(String name, String description, String ref) {
			this.name = name;
			this.description = description;
			this.ref = ref;
		}

		public String getName() {
			return this.name;
		}

		public String getDescription() {
			return this.description;
		}

		public String getRef() {
			return this.ref;
		}
	}

	class MessageRecord {
		String name;
		String description;
		String ref;

		public MessageRecord(String name, String description, String ref) {
			this.name = name;
			this.description = description;
			this.ref = ref;
		}

		public String getName() {
			return this.name;
		}

		public String getDescription() {
			return this.description;
		}

		public String getRef() {
			return this.ref;
		}
	}

	class VersionRecord {
		String name;
		Map<String, FormatDescriptorXml.MessageRecord> messageRecords;
		Map<String, FormatDescriptorXml.SegmentRecord> segmentRecords;
		Map<String, FormatDescriptorXml.FieldRecord> fieldRecords;

		public VersionRecord(String name) {
			this.name = name;
		}

		public String getName() {
			return this.name;
		}

		public void addMessageRecord(String name, String description, String ref) {
			if (name == null)
				return;
			if (messageRecords == null)
				messageRecords = new TreeMap();
			MessageRecord record = new MessageRecord(name, description, ref);
			messageRecords.put(name, record);
		}

		public FormatDescriptorXml.MessageRecord getMessageRecord(String name) {
			if (name == null) {
				return null;
			}

			return this.messageRecords != null ? (FormatDescriptorXml.MessageRecord) this.messageRecords.get(name)
					: null;
		}

		public List<String> getMessages() {
			return this.messageRecords != null ? new ArrayList(this.messageRecords.keySet()) : null;
		}

		public void addSegmentRecord(String name, String description, String ref) {
			if (name == null)
				return;
			if (segmentRecords == null)
				segmentRecords = new TreeMap();
			SegmentRecord record = new SegmentRecord(name, description, ref);
			segmentRecords.put(name, record);
		}

		public FormatDescriptorXml.SegmentRecord getSegmentRecord(String name) {
			if (name == null) {
				return null;
			}

			return this.segmentRecords != null ? (FormatDescriptorXml.SegmentRecord) this.segmentRecords.get(name)
					: null;
		}

		public List<String> getSegments() {
			return this.segmentRecords != null ? new ArrayList(this.segmentRecords.keySet()) : null;
		}

		public void addFieldRecord(String name, String description, String ref) {
			if (name == null)
				return;
			if (fieldRecords == null)
				fieldRecords = new TreeMap();
			FieldRecord record = new FieldRecord(name, description, ref);
			fieldRecords.put(name, record);
		}

		public FormatDescriptorXml.FieldRecord getFieldRecord(String name) {
			if (name == null) {
				return null;
			}

			return this.fieldRecords != null ? (FormatDescriptorXml.FieldRecord) this.fieldRecords.get(name) : null;
		}

		public List<String> getFields() {
			return this.fieldRecords != null ? new ArrayList(this.fieldRecords.keySet()) : null;
		}
	}
}