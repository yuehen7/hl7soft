package com.hl7soft.sevenedit.db.util.dfnconv;

import com.hl7soft.sevenedit.db.defs.FieldDefinition;
import com.hl7soft.sevenedit.db.defs.FieldEntry;
import com.hl7soft.sevenedit.db.defs.ISegmentEntryContainer;
import com.hl7soft.sevenedit.db.defs.MessageDefinition;
import com.hl7soft.sevenedit.db.defs.SegmentDefinition;
import com.hl7soft.sevenedit.db.defs.SegmentEntry;
import com.hl7soft.sevenedit.db.defs.io.xml.XmlDefinitionWriter;
import com.hl7soft.sevenedit.model.util.StringHelper;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import net.n3.nanoxml.IXMLElement;
import net.n3.nanoxml.IXMLParser;
import net.n3.nanoxml.IXMLReader;
import net.n3.nanoxml.StdXMLReader;
import net.n3.nanoxml.XMLParserFactory;

public class DefinitionFormatConverter3 {
	static String VERSION = "2.7.1";

	static File definitionsFolder = new File("../../hl7-database/db/271/");

	static File outDefinitionsFolder = new File("../..//hl7-db/defs/v" + VERSION);

	static ArrayList<String> messagesList = new ArrayList();

	public void run() throws Exception {
		outDefinitionsFolder.mkdirs();
		convertMessages();

		System.out.println("DONE");
	}

	private void convertMessages() throws Exception {
		File folder = new File(definitionsFolder, "messages");
		File outFolder = new File(outDefinitionsFolder, "messages");
		outFolder.mkdirs();

		System.out.println(folder.getAbsolutePath());

		File[] files = folder.listFiles();
		for (int i = 0; i < files.length; i++) {
			File file = files[i];

			if (messagesList.contains(file.getName())) {
				System.out.println(file.getName());

				MessageDefinition definition = readMessageDefinition(file);
				writeMessageDefinition(definition, new File(outFolder, file.getName()));
			}
		}
	}

	private void convertSegments() throws Exception {
		File folder = new File(definitionsFolder, "segments");
		File outFolder = new File(outDefinitionsFolder, "segments");
		outFolder.mkdirs();

		File[] files = folder.listFiles();
		for (int i = 0; i < files.length; i++) {
			File file = files[i];

			SegmentDefinition definition = readSegmentDefinition(file);
			writeSegmentDefinition(definition, new File(outFolder, file.getName()));
		}
	}

	private void convertFields() throws Exception {
		File folder = new File(definitionsFolder, "fields");
		File outFolder = new File(outDefinitionsFolder, "datatypes");
		outFolder.mkdirs();

		File[] files = folder.listFiles();
		for (int i = 0; i < files.length; i++) {
			File file = files[i];

			FieldDefinition definition = readFieldDefinition(file);
			writeFieldDefinition(definition, new File(outFolder, file.getName()));
		}
	}

	private void writeDescriptor() throws Exception {
		File file = new File(outDefinitionsFolder, "descriptor");
		FileOutputStream os = new FileOutputStream(file);
		OutputStreamWriter writer = new OutputStreamWriter(os);
		writer.write("version: " + VERSION);
		writer.flush();
		os.close();
	}

	private MessageDefinition readMessageDefinition(File file) throws Exception {
		FileInputStream is = new FileInputStream(file);
		MessageDefinition definition = readMessageDefinition(is);
		is.close();
		return definition;
	}

	private void writeMessageDefinition(MessageDefinition definition, File file) throws Exception {
		FileOutputStream os = new FileOutputStream(file);
		XmlDefinitionWriter.write(definition, os);
		os.close();
	}

	public static MessageDefinition readMessageDefinition(InputStream is) {
		try {
			IXMLElement root = readXml(is);
			if (!"message".equalsIgnoreCase(root.getName())) {
				throw new RuntimeException("Missing 'message' tag.");
			}

			String name = root.getAttribute("name", null);

			String descr = root.getAttribute("description", null);

			MessageDefinition dfn = new MessageDefinition(name, null, descr);

			dfn.setEntries(getMessageDefinitionItems(dfn, root));

			return dfn;
		} catch (Exception e) {
			throw new RuntimeException("Error reading message definition.", e);
		}
	}

	private static List<SegmentEntry> getMessageDefinitionItems(ISegmentEntryContainer parent, IXMLElement node) {
		List list = null;

		int i = 0;
		for (int n = node.getChildrenCount(); i < n; i++) {
			IXMLElement childElement = node.getChildAtIndex(i);
			String tagName = childElement.getName();

			SegmentEntry item = null;

			if ("segment-group".equals(tagName)) {
				String name = childElement.getAttribute("name", null);
				String type = childElement.getAttribute("type", null);
				String count = childElement.getAttribute("count", null);
				int[] countAry = parseCount(count);

				item = new SegmentEntry(2, type, name, countAry[0], countAry[1]);
				item.setParentContainer(parent);
			} else if ("segment-choice".equals(tagName)) {
				String name = childElement.getAttribute("name", null);
				String type = childElement.getAttribute("type", null);
				String count = childElement.getAttribute("count", null);
				int[] countAry = parseCount(count);

				item = new SegmentEntry(3, type, name, countAry[0], countAry[1]);
				item.setParentContainer(parent);

				String choiceOptionsStr = childElement.getAttribute("choices", null);
				String[] choiceOptions = StringHelper.explode(choiceOptionsStr, ',');
				for (int j = 0; j < choiceOptions.length; j++) {
					item.addSegmentEntry(new SegmentEntry(1, choiceOptions[j], choiceOptions[j] + " Segment", 0, 0));
				}
			} else if ("segment".equals(tagName)) {
				String name = childElement.getAttribute("name", null);
				String type = childElement.getAttribute("type", null);
				String count = childElement.getAttribute("count", null);
				int[] countAry = parseCount(count);

				item = new SegmentEntry(1, type, name, countAry[0], countAry[1]);
				item.setParentContainer(parent);
			} else {
				throw new RuntimeException("Unexpected tag: " + childElement.getName());
			}

			if ("segment-group".equals(tagName)) {
				item.setEntries(getMessageDefinitionItems(item, childElement));
			}

			if (list == null) {
				list = new ArrayList(2);
			}
			list.add(item);
		}

		return list;
	}

	private static int[] parseCount(String value) {
		if (value.contains("..")) {
			String[] ary = value.split("\\.\\.");
			int n1 = Integer.parseInt(ary[0]);
			int n2 = ary[1].equals("*") ? 0 : Integer.parseInt(ary[1]);
			return new int[] { n1, n2 };
		}
		int n1 = Integer.parseInt(value);
		return new int[] { n1, n1 };
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

	private SegmentDefinition readSegmentDefinition(File file) throws Exception {
		FileInputStream is = new FileInputStream(file);
		SegmentDefinition definition = readSegmentDefinition(is);
		is.close();
		return definition;
	}

	public static SegmentDefinition readSegmentDefinition(InputStream is) {
		String name = null;
		try {
			IXMLElement root = readXml(is);
			if (!"segment".equalsIgnoreCase(root.getName())) {
				throw new RuntimeException("Missing 'segment' tag.");
			}

			name = root.getAttribute("name", null);

			String descr = root.getAttribute("description", null);

			SegmentDefinition dfn = new SegmentDefinition(name, null, descr);

			Vector v = root.getChildrenNamed("field");
			if ((v == null) || (v.size() == 0)) {
				throw new RuntimeException("Missing 'field' tags.");
			}

			for (int i = 0; i < v.size(); i++) {
				IXMLElement node = (IXMLElement) v.get(i);

				String fieldName = node.getAttribute("type", null);
				String fieldDescription = node.getAttribute("name", null);

				String fieldLenMin = node.getAttribute("min-len", null);
				String fieldLenMax = node.getAttribute("max-len", null);
				String fieldConfLen = node.getAttribute("c-len", null);
				String fieldOpt = node.getAttribute("optionality", null);
				String fieldRepeat = node.getAttribute("repeat", null);
				String fieldTable = node.getAttribute("table", null);

				FieldEntry item = new FieldEntry(fieldName, fieldDescription);

				if (fieldLenMax != null) {
					item.setMaxLength(Integer.parseInt(fieldLenMax));
				}

				int optionality = 1;
				if ("R".equals(fieldOpt))
					optionality = 2;
				else if ("C".equals(fieldOpt))
					optionality = 3;
				else if ("W".equals(fieldOpt)) {
					optionality = 4;
				}
				item.setOptionality(optionality);

				int rep = 1;
				if ((fieldRepeat != null) && (fieldRepeat.trim().length() > 0)) {
					if (fieldRepeat.equalsIgnoreCase("Y"))
						rep = 0;
					else if (fieldRepeat.equalsIgnoreCase("N"))
						rep = 1;
					else {
						rep = Integer.parseInt(fieldRepeat);
					}
				}
				item.setRepeatCount(rep);

				if (fieldTable != null) {
					String tableNumPrepared = stripZeroes(fieldTable);
					item.setTableNumber(Integer.parseInt(tableNumPrepared));
				}

				dfn.addFieldEntry(item);
			}

			return dfn;
		} catch (Exception e) {
			throw new RuntimeException("Error reading segment definition.", e);
		}
	}

	private FieldDefinition readFieldDefinition(File file) throws Exception {
		FileInputStream is = new FileInputStream(file);
		FieldDefinition definition = readFieldDefinition(is);
		is.close();
		return definition;
	}

	public static FieldDefinition readFieldDefinition(InputStream is) {
		try {
			IXMLElement root = readXml(is);
			if (!"field".equalsIgnoreCase(root.getName())) {
				throw new RuntimeException("Missing 'field' tag.");
			}

			String name = root.getAttribute("name", null);

			String descr = root.getAttribute("description", null);

			FieldDefinition dfn = new FieldDefinition(name, null, descr);

			Vector v = root.getChildrenNamed("field");
			boolean composite = (v != null) && (v.size() > 0);

			if (composite) {
				for (int i = 0; i < v.size(); i++) {
					IXMLElement node = (IXMLElement) v.get(i);

					String fieldType = node.getAttribute("type", null);
					String fieldName = node.getAttribute("name", null);

					String fieldTable = node.getAttribute("table", null);
					String fieldLenMin = node.getAttribute("min-len", null);
					String fieldLenMax = node.getAttribute("max-len", null);
					String fieldConfLen = node.getAttribute("c-len", null);
					String fieldOpt = node.getAttribute("optionality", null);

					FieldEntry item = new FieldEntry(fieldType, fieldName);

					if (fieldTable != null) {
						String tableNumPrepared = stripZeroes(fieldTable);
						item.setTableNumber(Integer.parseInt(tableNumPrepared));
					}

					if (fieldLenMax != null) {
						item.setMaxLength(Integer.parseInt(fieldLenMax));
					}

					int optionality = 1;
					if ("R".equals(fieldOpt))
						optionality = 2;
					else if ("C".equals(fieldOpt))
						optionality = 3;
					else if ("W".equals(fieldOpt)) {
						optionality = 4;
					}
					item.setOptionality(optionality);

					dfn.addFieldEntry(item);
				}
			}

			return dfn;
		} catch (Exception e) {
			throw new RuntimeException("Error reading field definition.", e);
		}
	}

	private static String stripZeroes(String str) {
		int cnt = 0;
		for (int i = 0; (i < str.length()) && (str.charAt(i) == '0'); i++) {
			cnt++;
		}

		return str.substring(cnt);
	}

	private void writeSegmentDefinition(SegmentDefinition definition, File file) throws Exception {
		FileOutputStream os = new FileOutputStream(file);
		XmlDefinitionWriter.write(definition, os);
		os.close();
	}

	private void writeFieldDefinition(FieldDefinition definition, File file) throws Exception {
		FileOutputStream os = new FileOutputStream(file);
		XmlDefinitionWriter.write(definition, os);
		os.close();
	}

	public static void main(String[] args) {
		try {
			new DefinitionFormatConverter3().run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static {
		messagesList.add("CCI_I22.xml");
		messagesList.add("CCM_I22.xml");
		messagesList.add("CCR_I16.xml");
		messagesList.add("CCR_I17.xml");
		messagesList.add("CCU_I20.xml");
		messagesList.add("CQU_I19.xml");
		messagesList.add("EHC_E24.xml");
		messagesList.add("PGL_PC6.xml");
		messagesList.add("PGL_PC7.xml");
		messagesList.add("PGL_PC8.xml");
		messagesList.add("PPG_PCG.xml");
		messagesList.add("PPG_PCH.xml");
		messagesList.add("PPG_PCJ.xml");
		messagesList.add("PPP_PCB.xml");
		messagesList.add("PPP_PCC.xml");
		messagesList.add("PPP_PCD.xml");
		messagesList.add("PPR_PC1.xml");
		messagesList.add("PPR_PC2.xml");
		messagesList.add("PPR_PC3.xml");
		messagesList.add("PPT_PCL.xml");
		messagesList.add("PPV_PCA.xml");
		messagesList.add("PRR_PC5.xml");
		messagesList.add("PTR_PCF.xml");
		messagesList.add("SCN_S37.xml");
		messagesList.add("SDN_S36.xml");
		messagesList.add("SDR_S31.xml");
		messagesList.add("SMD_S32.xml");
	}
}