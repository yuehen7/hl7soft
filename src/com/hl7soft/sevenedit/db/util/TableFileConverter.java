package com.hl7soft.sevenedit.db.util;

import com.hl7soft.sevenedit.model.util.StringHelper;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

public class TableFileConverter {
	public static void main(String[] args) {
		TableFileConverter app = new TableFileConverter();
		app.run();
	}

	public void run() {
		try {
			FileInputStream is = new FileInputStream("tables.txt");
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			String line = null;
			int lineCnt = 0;
			List tables = new ArrayList();
			Table table = null;

			while ((line = reader.readLine()) != null) {
				lineCnt++;

				line = line.trim();

				if (line.length() != 0) {
					String[] tkz = StringHelper.explode(line, '\t');

					if ((line.startsWith("User")) || (line.startsWith("HL7"))) {
						table = new Table();
						table.name = tkz[2];
						table.type = (line.startsWith("User") ? 1 : 0);
						tables.add(table);
					} else {
						if (tkz.length != 4) {
							throw new RuntimeException("Bad format at line : " + lineCnt);
						}

						if (table.num == -1) {
							table.num = Integer.parseInt(tkz[0], 10);
						}

						String tn = tkz[0];
						String value = tkz[2];
						String valueDescription = tkz[3];

						if (table.num != Integer.parseInt(tn, 10)) {
							throw new RuntimeException("Bad format at line : " + lineCnt);
						}

						if ((table.num != 211) && (table.num != 256) && (table.num != 356)
								&& ((value.contains("...")) || (value.length() == 0) || (value.indexOf(32) != -1))) {
							throw new RuntimeException("Bad format at line : " + lineCnt);
						}

						if (value.equals("<null>")) {
							throw new RuntimeException("Null value at line : " + lineCnt);
						}

						TableItem item = new TableItem(value, valueDescription);
						table.add(item);
					}
				}
			}

			ByteArrayOutputStream os = new ByteArrayOutputStream();
			OutputStreamWriter writer = new OutputStreamWriter(os, Charset.forName("UTF-8"));
			XMLOutputFactory xmlFactory = XMLOutputFactory.newInstance();
			XMLStreamWriter xwriter = xmlFactory.createXMLStreamWriter(writer);

			xwriter.writeStartDocument("UTF-8", "1.0");
			xwriter.writeStartElement("tables");

			int i = 0;
			for (int n = tables.size(); i < n; i++) {
				Table t = (Table) tables.get(i);

				xwriter.writeStartElement("table");
				xwriter.writeAttribute("num", "" + t.num);
				xwriter.writeAttribute("name", t.name);
				xwriter.writeAttribute("type", t.type == 1 ? "User" : "HL7");

				int j = 0;
				for (int n2 = t.items.size(); j < n2; j++) {
					TableItem item = (TableItem) t.items.get(j);
					xwriter.writeStartElement("item");
					xwriter.writeAttribute("value", "" + item.value);
					xwriter.writeAttribute("description", "" + item.descr);
					xwriter.writeEndElement();
				}

				xwriter.writeEndElement();
			}

			xwriter.writeEndElement();
			xwriter.writeEndDocument();
			xwriter.flush();
			xwriter.close();

			System.out.println(new String(os.toByteArray()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static class TableItem {
		String value;
		String descr;

		public TableItem(String value, String descr) {
			this.value = value;
			this.descr = descr;
		}
	}

	static class Table {
		int num = -1;
		int type;
		String name;
		List<TableFileConverter.TableItem> items;

		public void add(TableFileConverter.TableItem item) {
			if (this.items == null) {
				this.items = new ArrayList();
			}
			this.items.add(item);
		}
	}
}