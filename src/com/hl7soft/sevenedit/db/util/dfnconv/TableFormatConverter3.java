package com.hl7soft.sevenedit.db.util.dfnconv;

import com.hl7soft.sevenedit.db.tables.ITable;
import com.hl7soft.sevenedit.db.tables.Table;
import com.hl7soft.sevenedit.db.tables.TableFactory;
import com.hl7soft.sevenedit.db.tables.TableItem;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.util.List;
import java.util.Vector;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import net.n3.nanoxml.IXMLElement;
import net.n3.nanoxml.IXMLParser;
import net.n3.nanoxml.IXMLReader;
import net.n3.nanoxml.StdXMLReader;
import net.n3.nanoxml.XMLParserFactory;

public class TableFormatConverter3 {
	static File tablesFolder = new File("../hl7-database/db/271/tables");

	static File outFile = new File("../hl7-db/tables/tables.xml");

	public void run() {
		try {
			System.out.println("Reading tables...");
			TableFactory factory = readTables();
			writeTables(factory);
			System.out.println("Total tables: " + factory.getTableNumbers().size());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private TableFactory readTables() throws Exception {
		TableFactory factory = new TableFactory();

		File[] files = tablesFolder.listFiles();
		for (int i = 0; i < files.length; i++) {
			File file = files[i];

			FileInputStream is = new FileInputStream(file);
			Table table = readTableDefinition(is);
			is.close();

			factory.addTable(table);
		}

		return factory;
	}

	private void writeTables(TableFactory factory) throws Exception {
		FileOutputStream os = new FileOutputStream(outFile);

		OutputStreamWriter writer = new OutputStreamWriter(os, "UTF-8");
		XMLOutputFactory xmlFactory = XMLOutputFactory.newInstance();
		XMLStreamWriter xwriter = xmlFactory.createXMLStreamWriter(writer);

		xwriter.writeStartDocument("UTF-8", "1.0");
		xwriter.writeStartElement("tables");

		List<Integer> tableNums = factory.getTableNumbers();

		if (tableNums != null) {
			for (Integer num : tableNums) {
				ITable table = factory.getTable(num);

				xwriter.writeStartElement("table");
				xwriter.writeAttribute("num", "" + num);
				xwriter.writeAttribute("name", table.getName());
				xwriter.writeAttribute("type", table.getType() == 2 ? "HL7" : "User");

				int i = 0;
				for (int n = table.getItemsCount(); i < n; i++) {
					TableItem item = table.getItem(i);
					xwriter.writeStartElement("item");
					xwriter.writeAttribute("value", item.getValue());
					xwriter.writeAttribute("description", item.getDescription());
					xwriter.writeEndElement();
				}

				xwriter.writeEndElement();
			}
		}

		xwriter.writeEndElement();
		xwriter.writeEndDocument();
		xwriter.flush();

		os.close();
	}

	public static Table readTableDefinition(InputStream is) {
		try {
			IXMLElement root = readXml(is);
			if (!"table".equalsIgnoreCase(root.getName())) {
				throw new RuntimeException("Missing 'table' tag.");
			}

			String name = root.getAttribute("name", null);

			String descr = root.getAttribute("description", null);
			String typeStr = root.getAttribute("type", null);
			int type = typeStr.equalsIgnoreCase("hl7") ? 2 : 1;

			Integer num = Integer.valueOf(Integer.parseInt(stripZeroes(name)));

			Table dfn = new Table(num.intValue(), descr, type);

			Vector v = root.getChildrenNamed("item");
			boolean composite = (v != null) && (v.size() > 0);

			if (composite) {
				for (int i = 0; i < v.size(); i++) {
					IXMLElement node = (IXMLElement) v.get(i);

					String value = node.getAttribute("value", null);
					String description = node.getAttribute("description", null);

					dfn.addItem(new TableItem(value, description));
				}
			}

			return dfn;
		} catch (Exception e) {
			throw new RuntimeException("Error reading table.", e);
		}
	}

	private static IXMLElement readXml(InputStream is) throws Exception {
		BufferedReader bufReader = new BufferedReader(new InputStreamReader(is));
		IXMLParser parser = XMLParserFactory.createDefaultXMLParser();
		IXMLReader xmlReader = new StdXMLReader(bufReader);
		parser.setReader(xmlReader);
		IXMLElement root = (IXMLElement) parser.parse();
		bufReader.close();

		return root;
	}

	private static String stripZeroes(String str) {
		int cnt = 0;
		for (int i = 0; (i < str.length()) && (str.charAt(i) == '0'); i++) {
			cnt++;
		}

		return str.substring(cnt);
	}

	public static void main(String[] args) {
		new TableFormatConverter3().run();
	}
}