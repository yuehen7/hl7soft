package com.hl7soft.sevenedit.db.defs.io.xml;

import com.hl7soft.sevenedit.db.defs.FieldDefinition;
import com.hl7soft.sevenedit.db.defs.FieldEntry;
import com.hl7soft.sevenedit.db.defs.MessageDefinition;
import com.hl7soft.sevenedit.db.defs.SegmentDefinition;
import com.hl7soft.sevenedit.db.defs.SegmentEntry;
import com.hl7soft.sevenedit.db.defs.SegmentListEntry;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import net.n3.nanoxml.IXMLElement;
import net.n3.nanoxml.IXMLParser;
import net.n3.nanoxml.IXMLReader;
import net.n3.nanoxml.StdXMLReader;
import net.n3.nanoxml.XMLParserFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XmlDefinitionReader {
    public static MessageDefinition readMessageDefinition(InputStream is) {
	try {
	    IXMLElement root = readXml(is);
	    if (!"message".equalsIgnoreCase(root.getName())) {
		throw new RuntimeException("Missing 'message' tag.");
	    }

	    String name = root.getAttribute("name", null);
	    String version = root.getAttribute("version", null);
	    String descr = root.getAttribute("descr", null);
	    String notes = root.getAttribute("notes", null);

	    MessageDefinition messageDefinition = new MessageDefinition(name, version);
	    messageDefinition.setDescription(descr);
	    messageDefinition.setNotes(notes);

	    List entries = getSegmentEntries(root);
	    if (entries != null) {
		int j = 0;
		for (int n2 = entries.size(); j < n2; j++) {
		    messageDefinition.addSegmentEntry((SegmentEntry) entries.get(j));
		}
	    }

	    return messageDefinition;
	} catch (Exception e) {
	    throw new RuntimeException("Can't read message model!", e);
	}
    }

    private static List<SegmentEntry> getSegmentEntries(IXMLElement node) {
	List list = null;

	int i = 0;
	for (int n = node.getChildrenCount(); i < n; i++) {
	    IXMLElement childElement = node.getChildAtIndex(i);
	    String tagName = childElement.getName();

	    String name = childElement.getAttribute("name", null);
	    String descr = childElement.getAttribute("descr", null);
	    String notes = childElement.getAttribute("notes", null);
	    String min = childElement.getAttribute("min", null);
	    String max = childElement.getAttribute("max", null);

	    if ((!"segment".equals(childElement.getName())) && (!"segment-group".equals(childElement.getName())) && (!"segment-choice".equals(childElement.getName()))) {
		throw new RuntimeException("Unexpected tag: " + childElement.getName());
	    }

	    SegmentEntry segmentEntry = new SegmentEntry(name);
	    segmentEntry.setDescription(descr);
	    segmentEntry.setNotes(notes);
	    segmentEntry.setMinCount(min != null ? Integer.parseInt(min) : 0);
	    segmentEntry.setMaxCount(max != null ? Integer.parseInt(max) : 0);

	    if ("segment-group".equals(tagName))
		segmentEntry.setType(2);
	    else if ("segment-choice".equals(tagName))
		segmentEntry.setType(3);
	    else {
		segmentEntry.setType(1);
	    }

	    if (("segment-group".equals(tagName)) || ("segment-choice".equals(tagName))) {
		List entries = getSegmentEntries(childElement);
		if (entries != null) {
		    int j = 0;
		    for (int n2 = entries.size(); j < n2; j++) {
			segmentEntry.addSegmentEntry((SegmentEntry) entries.get(j));
		    }
		}
	    }

	    if (list == null) {
		list = new ArrayList(2);
	    }
	    list.add(segmentEntry);
	}

	return list;
    }

    private static SegmentEntry processSegmentGroup(Node node) {
	Map attrs = mapAttributes(node.getAttributes());
	String name = (String) attrs.get("name");
	String descr = (String) attrs.get("descr");
	String notes = (String) attrs.get("notes");
	String minStr = (String) attrs.get("min");
	String maxStr = (String) attrs.get("max");

	int min = 0;
	int max = 0;

	if (minStr != null) {
	    min = Integer.parseInt(minStr);
	}

	if (maxStr != null) {
	    max = Integer.parseInt(maxStr);
	}

	SegmentEntry segmentGroup = new SegmentEntry(name);
	segmentGroup.setType(2);
	segmentGroup.setDescription(descr);
	segmentGroup.setNotes(notes);
	segmentGroup.setMinCount(min);
	segmentGroup.setMaxCount(max);

	NodeList childNodes = node.getChildNodes();
	int len = childNodes.getLength();
	int segCnt = 0;
	for (int i = 0; i < len; i++) {
	    Node childNode = childNodes.item(i);
	    if (childNode.getNodeName().equals("segment")) {
		SegmentEntry segmentEntry = processSegmentEntry(childNode);
		segmentGroup.addSegmentEntry(segCnt, segmentEntry);
		segCnt++;
	    } else if (childNode.getNodeName().equals("segment-group")) {
		SegmentEntry segmentEntry = processSegmentGroup(childNode);
		segmentGroup.addSegmentEntry(segCnt, segmentEntry);
		segCnt++;
	    } else if (childNode.getNodeName().equals("segment-choice")) {
		SegmentEntry segmentEntry = processSegmentList(childNode);
		segmentGroup.addSegmentEntry(segmentEntry);
		segCnt++;
	    }
	}

	return segmentGroup;
    }

    private static SegmentListEntry processSegmentList(Node node) {
	Map attrs = mapAttributes(node.getAttributes());
	String descr = (String) attrs.get("descr");
	String notes = (String) attrs.get("notes");

	SegmentListEntry segmentList = new SegmentListEntry();
	segmentList.setType(3);
	segmentList.setDescription(descr);
	segmentList.setNotes(notes);

	NodeList childNodes = node.getChildNodes();
	int len = childNodes.getLength();
	int segCnt = 0;
	for (int i = 0; i < len; i++) {
	    Node childNode = childNodes.item(i);
	    if (childNode.getNodeName().equals("segment")) {
		SegmentEntry segmentEntry = processSegmentEntry(childNode);
		segmentList.addSegmentEntry(segmentEntry);
		segCnt++;
	    } else if (childNode.getNodeName().equals("segment-group")) {
		SegmentEntry segmentEntry = processSegmentGroup(childNode);
		segmentList.addSegmentEntry(segmentEntry);
		segCnt++;
	    } else if (childNode.getNodeName().equals("segment-choice")) {
		SegmentEntry segmentEntry = processSegmentList(childNode);
		segmentList.addSegmentEntry(segmentEntry);
		segCnt++;
	    }
	}

	return segmentList;
    }

    private static SegmentEntry processSegmentEntry(Node node) {
	Map attrs = mapAttributes(node.getAttributes());
	String name = (String) attrs.get("name");
	String descr = (String) attrs.get("descr");
	String notes = (String) attrs.get("notes");
	String minStr = (String) attrs.get("min");
	String maxStr = (String) attrs.get("max");

	int min = 0;
	int max = 0;

	if (minStr != null) {
	    min = Integer.parseInt(minStr);
	}

	if (maxStr != null) {
	    max = Integer.parseInt(maxStr);
	}

	SegmentEntry segmentEntry = new SegmentEntry(name);
	segmentEntry.setDescription(descr);
	segmentEntry.setNotes(notes);
	segmentEntry.setMinCount(min);
	segmentEntry.setMaxCount(max);
	return segmentEntry;
    }

    public static SegmentDefinition readSegmentDefinition(InputStream is) {
	String name = null;
	try {
	    IXMLElement root = readXml(is);
	    if (!"segment".equalsIgnoreCase(root.getName())) {
		throw new RuntimeException("Missing 'segment' tag.");
	    }

	    name = root.getAttribute("name", null);
	    String version = root.getAttribute("version", null);
	    String descr = root.getAttribute("description", null);
	    String notes = root.getAttribute("notes", null);

	    SegmentDefinition segmentDefinition = new SegmentDefinition(name, version);
	    segmentDefinition.setDescription(descr);
	    segmentDefinition.setNotes(notes);

	    Vector v = root.getChildrenNamed("field");
	    if ((v == null) || (v.size() == 0)) {
		throw new RuntimeException("Missing 'field' tags.");
	    }

	    for (int i = 0; i < v.size(); i++) {
		IXMLElement node = (IXMLElement) v.get(i);

		String fieldName = node.getAttribute("name", null);
		String fieldDescr = node.getAttribute("descr", null);
		String fieldNotes = node.getAttribute("notes", null);
		String fieldLen = node.getAttribute("len", null);
		String fieldOpt = node.getAttribute("opt", null);
		String fieldRepeat = node.getAttribute("repeat", null);
		String fieldTable = node.getAttribute("table", null);

		FieldEntry fieldEntry = new FieldEntry(fieldName);

		if (fieldLen != null) {
		    int len = Integer.parseInt(fieldLen);
		    fieldEntry.setMaxLength(len);
		}

		int optionality = 1;
		if (fieldOpt != null) {
		    if (fieldOpt.equals("R"))
			optionality = 2;
		    else if (fieldOpt.equals("O"))
			optionality = 1;
		    else if (fieldOpt.equals("C"))
			optionality = 3;
		    else if (fieldOpt.equals("X"))
			optionality = 5;
		    else if (fieldOpt.equals("B")) {
			optionality = 4;
		    }
		}
		fieldEntry.setOptionality(optionality);

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
		fieldEntry.setRepeatCount(rep);

		if (fieldTable != null) {
		    fieldEntry.setTableNumber(Integer.parseInt(fieldTable));
		}

		if (fieldDescr != null) {
		    fieldEntry.setDescription(fieldDescr);
		}

		if (fieldNotes != null) {
		    fieldEntry.setNotes(fieldNotes);
		}

		segmentDefinition.addFieldEntry(fieldEntry);
	    }

	    return segmentDefinition;
	} catch (Exception e) {
	    throw new RuntimeException("Can't read segment model: " + name, e);
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

    public static FieldDefinition readFieldDefinition(InputStream is) {
	try {
	    IXMLElement root = readXml(is);
	    if (!"datatype".equalsIgnoreCase(root.getName())) {
		throw new RuntimeException("Missing 'datatype' tag.");
	    }

	    String name = root.getAttribute("name", null);
	    String version = root.getAttribute("version", null);
	    String descr = root.getAttribute("descr", null);
	    String notes = root.getAttribute("notes", null);

	    FieldDefinition datatypeModel = new FieldDefinition(name, version);
	    datatypeModel.setDescription(descr);
	    datatypeModel.setNotes(notes);

	    Vector v = root.getChildrenNamed("field");
	    if ((v != null) || (v.size() > 0)) {
		for (int i = 0; i < v.size(); i++) {
		    IXMLElement node = (IXMLElement) v.get(i);

		    String fieldName = node.getAttribute("name", null);
		    String fieldDescr = node.getAttribute("descr", null);
		    String fieldNotes = node.getAttribute("notes", null);
		    String fieldTable = node.getAttribute("table", null);
		    String fieldLen = node.getAttribute("len", null);
		    String fieldOpt = node.getAttribute("opt", null);

		    FieldEntry fieldEntry = new FieldEntry(fieldName);

		    if (fieldTable != null) {
			int table = Integer.parseInt(fieldTable);
			fieldEntry.setTableNumber(table);
		    }

		    if (fieldDescr != null) {
			fieldEntry.setDescription(fieldDescr);
		    }

		    if (fieldLen != null) {
			int len = Integer.parseInt(fieldLen);
			fieldEntry.setMaxLength(len);
		    }

		    int optionality = 1;
		    if (fieldOpt != null) {
			if (fieldOpt.equals("R"))
			    optionality = 2;
			else if (fieldOpt.equals("O"))
			    optionality = 1;
			else if (fieldOpt.equals("C"))
			    optionality = 3;
			else if (fieldOpt.equals("X"))
			    optionality = 5;
			else if (fieldOpt.equals("B")) {
			    optionality = 4;
			}
		    }
		    fieldEntry.setOptionality(optionality);

		    if (fieldNotes != null) {
			fieldEntry.setNotes(fieldNotes);
		    }

		    datatypeModel.addFieldEntry(fieldEntry);
		}
	    }

	    return datatypeModel;
	} catch (Exception e) {
	    throw new RuntimeException("Can't read datatype model!", e);
	}
    }

    private static Map mapAttributes(NamedNodeMap attrs) {
	HashMap map = new HashMap();

	if (attrs == null) {
	    return map;
	}

	int numAttrs = attrs.getLength();

	for (int i = 0; i < numAttrs; i++) {
	    Attr attr = (Attr) attrs.item(i);

	    String attrName = attr.getNodeName();
	    String attrValue = attr.getNodeValue();

	    map.put(attrName, attrValue);
	}

	return map;
    }
}