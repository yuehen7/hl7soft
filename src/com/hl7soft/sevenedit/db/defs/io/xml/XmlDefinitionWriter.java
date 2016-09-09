package com.hl7soft.sevenedit.db.defs.io.xml;

import com.hl7soft.sevenedit.db.defs.FieldDefinition;
import com.hl7soft.sevenedit.db.defs.FieldEntry;
import com.hl7soft.sevenedit.db.defs.ISegmentEntryContainer;
import com.hl7soft.sevenedit.db.defs.MessageDefinition;
import com.hl7soft.sevenedit.db.defs.SegmentDefinition;
import com.hl7soft.sevenedit.db.defs.SegmentEntry;
import java.io.IOException;
import java.io.OutputStream;
import net.n3.nanoxml.XMLElement;
import net.n3.nanoxml.XMLWriter;

public class XmlDefinitionWriter {
    public static void write(MessageDefinition messageDefinition, OutputStream os) {
	try {
	    XMLElement root = new XMLElement("message");

	    root.setAttribute("name", messageDefinition.getName());

	    if (messageDefinition.getVersion() != null) {
		root.setAttribute("version", messageDefinition.getVersion());
	    }

	    if (messageDefinition.getDescription() != null) {
		root.setAttribute("descr", messageDefinition.getDescription());
	    }
	    if (messageDefinition.getNotes() != null) {
		root.setAttribute("notes", messageDefinition.getNotes());
	    }
	    appendSegmentEntries(root, messageDefinition);

	    XMLWriter writer = new XMLWriter(os);
	    writer.write(root, true);
	} catch (Exception e) {
	    throw new RuntimeException("Can't write message definition.", e);
	}
    }

    private static void appendSegmentEntries(XMLElement root, ISegmentEntryContainer container) throws IOException {
	int i = 0;
	for (int n = container.getSegmentEntriesCount(); i < n; i++) {
	    SegmentEntry segmentEntry = (SegmentEntry) container.getSegmentEntry(i);

	    if (segmentEntry.getType() == 2) {
		XMLElement segmentGroupNode = new XMLElement("segment-group");
		segmentGroupNode.setAttribute("name", segmentEntry.getName());
		if (segmentEntry.getDescription() != null) {
		    segmentGroupNode.setAttribute("descr", segmentEntry.getDescription());
		}
		if (segmentEntry.getNotes() != null) {
		    segmentGroupNode.setAttribute("notes", segmentEntry.getNotes());
		}
		if (segmentEntry.getMinCount() != 0) {
		    segmentGroupNode.setAttribute("min", "" + segmentEntry.getMinCount());
		}
		if (segmentEntry.getMaxCount() != 0) {
		    segmentGroupNode.setAttribute("max", "" + segmentEntry.getMaxCount());
		}

		appendSegmentEntries(segmentGroupNode, segmentEntry);

		root.addChild(segmentGroupNode);
	    } else if (segmentEntry.getType() == 1) {
		XMLElement segmentNode = new XMLElement("segment");
		segmentNode.setAttribute("name", segmentEntry.getName());
		if (segmentEntry.getDescription() != null) {
		    segmentNode.setAttribute("descr", segmentEntry.getDescription());
		}
		if (segmentEntry.getNotes() != null) {
		    segmentNode.setAttribute("notes", segmentEntry.getNotes());
		}
		if (segmentEntry.getMinCount() != 0) {
		    segmentNode.setAttribute("min", "" + segmentEntry.getMinCount());
		}
		if (segmentEntry.getMaxCount() != 0) {
		    segmentNode.setAttribute("max", "" + segmentEntry.getMaxCount());
		}

		root.addChild(segmentNode);
	    } else if (segmentEntry.getType() == 3) {
		XMLElement segmentListNode = new XMLElement("segment-choice");
		if (segmentEntry.getDescription() != null) {
		    segmentListNode.setAttribute("name", segmentEntry.getName());
		}
		if (segmentEntry.getDescription() != null) {
		    segmentListNode.setAttribute("descr", segmentEntry.getDescription());
		}
		if (segmentEntry.getNotes() != null) {
		    segmentListNode.setAttribute("notes", segmentEntry.getNotes());
		}

		appendSegmentEntries(segmentListNode, segmentEntry);

		root.addChild(segmentListNode);
	    } else {
		throw new RuntimeException("Unknown type.");
	    }
	}
    }

    public static void write(SegmentDefinition segmentDefinition, OutputStream os) {
	try {
	    XMLElement root = new XMLElement("segment");

	    root.setAttribute("name", segmentDefinition.getName());

	    if (segmentDefinition.getVersion() != null) {
		root.setAttribute("version", segmentDefinition.getVersion());
	    }

	    if (segmentDefinition.getDescription() != null) {
		root.setAttribute("description", segmentDefinition.getDescription());
	    }
	    if (segmentDefinition.getNotes() != null) {
		root.setAttribute("notes", segmentDefinition.getNotes());
	    }

	    int i = 0;
	    for (int n = segmentDefinition.getFieldEntriesCount(); i < n; i++) {
		FieldEntry field = segmentDefinition.getFieldEntry(i);

		XMLElement fieldNode = new XMLElement("field");
		root.addChild(fieldNode);

		fieldNode.setAttribute("name", field.getName());

		if (field.getMaxLength() > 0) {
		    fieldNode.setAttribute("len", "" + field.getMaxLength());
		}

		String optStr = "O";
		switch (field.getOptionality()) {
		case 1:
		    optStr = "0";
		    break;
		case 2:
		    optStr = "R";
		    break;
		case 3:
		    optStr = "C";
		    break;
		case 5:
		    optStr = "X";
		    break;
		case 4:
		    optStr = "B";
		    break;
		}

		if (field.getOptionality() != 1) {
		    fieldNode.setAttribute("opt", optStr);
		}

		String repStr = "1";
		if (field.getRepeatCount() == 0)
		    repStr = "Y";
		else {
		    repStr = "" + field.getRepeatCount();
		}
		if (field.getRepeatCount() != 1) {
		    fieldNode.setAttribute("repeat", repStr);
		}

		if (field.getTableNumber() > 0) {
		    fieldNode.setAttribute("table", "" + field.getTableNumber());
		}

		if (field.getDescription() != null) {
		    fieldNode.setAttribute("descr", field.getDescription());
		}

		if (field.getNotes() != null) {
		    fieldNode.setAttribute("notes", field.getNotes());
		}
	    }

	    XMLWriter writer = new XMLWriter(os);
	    writer.write(root, true);
	} catch (Exception e) {
	    throw new RuntimeException("Error writing segment definition.", e);
	}
    }

    public static void write(FieldDefinition fieldDefinition, OutputStream os) {
	try {
	    XMLElement root = new XMLElement("datatype");
	    root.setAttribute("name", fieldDefinition.getName());
	    if (fieldDefinition.getVersion() != null) {
		root.setAttribute("version", fieldDefinition.getVersion());
	    }
	    if (fieldDefinition.getDescription() != null) {
		root.setAttribute("descr", fieldDefinition.getDescription());
	    }
	    if (fieldDefinition.getNotes() != null) {
		root.setAttribute("notes", fieldDefinition.getNotes());
	    }

	    if (!fieldDefinition.isPrimitive()) {
		int i = 0;
		for (int n = fieldDefinition.getFieldEntriesCount(); i < n; i++) {
		    FieldEntry field = fieldDefinition.getFieldEntry(i);

		    XMLElement fieldNode = new XMLElement("field");
		    root.addChild(fieldNode);

		    fieldNode.setAttribute("name", field.getName());

		    if (field.getNotes() != null) {
			fieldNode.setAttribute("notes", field.getNotes());
		    }

		    if (field.getTableNumber() > 0) {
			fieldNode.setAttribute("table", "" + field.getTableNumber());
		    }

		    if (field.getMaxLength() > 0) {
			fieldNode.setAttribute("len", "" + field.getMaxLength());
		    }

		    String optStr = "O";
		    switch (field.getOptionality()) {
		    case 1:
			optStr = "0";
			break;
		    case 2:
			optStr = "R";
			break;
		    case 3:
			optStr = "C";
			break;
		    case 5:
			optStr = "X";
			break;
		    case 4:
			optStr = "B";
			break;
		    }

		    if (field.getOptionality() != 1) {
			fieldNode.setAttribute("opt", optStr);
		    }

		    if (field.getDescription() != null) {
			fieldNode.setAttribute("descr", field.getDescription());
		    }
		}
	    }

	    XMLWriter writer = new XMLWriter(os);
	    writer.write(root, true);
	} catch (Exception e) {
	    throw new RuntimeException("Can't write segment model.", e);
	}
    }
}