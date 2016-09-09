package com.hl7soft.sevenedit.db.tables.io.bin;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public class FormatDescriptorXml {
	String formatVersion;
	String tablesLocation;
	Map<Integer, TableRecord> tableRecords;

	public FormatDescriptorXml() {
		this.formatVersion = "3.1";

		this.tablesLocation = "data/tables";
	}

	public void addTableRecord(Integer num, String name, String ref) {
		if (this.tableRecords == null) {
			this.tableRecords = new HashMap();
		}

		TableRecord tableRecord = new TableRecord(num, name, ref);
		this.tableRecords.put(num, tableRecord);
	}

	public List<Integer> getTableNumbers() {
		return this.tableRecords != null ? new ArrayList(this.tableRecords.keySet()) : null;
	}

	public String getTableName(Integer num) {
		TableRecord tableRecord = (TableRecord) this.tableRecords.get(num);
		return tableRecord != null ? tableRecord.getName() : null;
	}

	public String getTableRef(Integer num) {
		TableRecord tableRecord = (TableRecord) this.tableRecords.get(num);
		return tableRecord != null ? tableRecord.getRef() : null;
	}

	public void write(OutputStream os) {
		try {
			OutputStreamWriter writer = new OutputStreamWriter(os, "UTF-8");
			XMLOutputFactory xmlFactory = XMLOutputFactory.newInstance();
			XMLStreamWriter xwriter = xmlFactory.createXMLStreamWriter(writer);

			xwriter.writeStartDocument("UTF-8", "1.0");
			xwriter.writeStartElement("format");
			xwriter.writeAttribute("name", "hl7-tables");
			xwriter.writeAttribute("version", this.formatVersion);

			List<Integer> tableNums = getTableNumbers();

			if (tableNums != null) {
				xwriter.writeStartElement("tables");
				xwriter.writeAttribute("loc", this.tablesLocation);

				for (Integer num : tableNums) {
					String name = getTableName(num);
					String ref = getTableRef(num);
					xwriter.writeStartElement("table");
					xwriter.writeAttribute("num", "" + num);
					if (!isEmpty(name)) {
						xwriter.writeAttribute("name", name);
					}
					xwriter.writeAttribute("ref", ref);
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

	public static FormatDescriptorXml read(InputStream is) {
		try {
			XMLEventReader reader = XMLInputFactory.newInstance().createXMLEventReader(is, "UTF-8");

			FormatDescriptorXml d = null;

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

						if (!"hl7-tables".equals(formatName)) {
							throw new RuntimeException("Unknown format: " + formatName);
						}

					} else if ("tables".equals(tagName)) {
						d.setTablesLocation(getAttributeValue(element, "loc"));
					} else if ("table".equals(tagName)) {
						String numStr = getAttributeValue(element, "num");
						String name = getAttributeValue(element, "name");
						String ref = getAttributeValue(element, "ref");

						d.addTableRecord(new Integer(numStr), name, ref);
					}
				}
			}

			reader.close();

			return d;
		} catch (Exception e) {
			throw new RuntimeException("Error reading format descriptor.", e);
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

	public String getTablesLocation() {
		return this.tablesLocation;
	}

	public void setTablesLocation(String defsLocation) {
		this.tablesLocation = defsLocation;
	}

	class TableRecord {
		Integer num;
		String name;
		String ref;

		public TableRecord(Integer num, String name, String ref) {
			this.num = num;
			this.name = name;
			this.ref = ref;
		}

		public Integer getNum() {
			return this.num;
		}

		public String getName() {
			return this.name;
		}

		public String getRef() {
			return this.ref;
		}
	}
}