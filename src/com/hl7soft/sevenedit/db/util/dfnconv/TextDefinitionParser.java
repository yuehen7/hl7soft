package com.hl7soft.sevenedit.db.util.dfnconv;

import com.hl7soft.sevenedit.db.defs.ISegmentEntryContainer;
import com.hl7soft.sevenedit.db.defs.MessageDefinition;
import com.hl7soft.sevenedit.db.defs.SegmentEntry;
import java.io.BufferedReader;
import java.io.StringReader;

public class TextDefinitionParser {
    public MessageDefinition parseMessageDefinition(String data, String name, String version) {
	int lineCnt = 0;
	try {
	    MessageDefinition messageDefinition = new MessageDefinition(name, version);

	    ISegmentEntryContainer segmentEntryContainer = messageDefinition;
	    String line = null;
	    BufferedReader reader = new BufferedReader(new StringReader(data));
	    while ((line = reader.readLine()) != null) {
		lineCnt++;
		line = line.trim();

		if (line.length() != 0) {
		    ChunkInfo chunkInfo = parseChunk(line);

		    if (chunkInfo.chunkType == 2) {
			SegmentEntry segmentEntry = new SegmentEntry(chunkInfo.name, 2);
			segmentEntry.setDescription(chunkInfo.description);
			segmentEntry.setMinCount(chunkInfo.minCount);
			segmentEntry.setMaxCount(chunkInfo.maxCount);
			segmentEntryContainer.addSegmentEntry(segmentEntry);
			segmentEntry.setParentContainer(segmentEntryContainer);
			segmentEntryContainer = segmentEntry;
		    } else if (chunkInfo.chunkType == 3) {
			if (!(segmentEntryContainer instanceof SegmentEntry)) {
			    throw new RuntimeException("Unexpected end of group: " + chunkInfo.name);
			}

			SegmentEntry parentSegmentEntryContainer = (SegmentEntry) segmentEntryContainer;
			if (!parentSegmentEntryContainer.getName().equals(chunkInfo.name)) {
			    throw new RuntimeException("Group wasn't closed: " + parentSegmentEntryContainer.getName());
			}

			segmentEntryContainer = parentSegmentEntryContainer.getParentContainer();
		    } else if (chunkInfo.chunkType == 1) {
			SegmentEntry segmentEntry = new SegmentEntry(chunkInfo.name);
			segmentEntry.setDescription(chunkInfo.description);
			segmentEntry.setMinCount(chunkInfo.minCount);
			segmentEntry.setMaxCount(chunkInfo.maxCount);
			segmentEntryContainer.addSegmentEntry(segmentEntry);
			segmentEntry.setParentContainer(segmentEntryContainer);
		    }
		}

	    }

	    return messageDefinition;
	} catch (Exception e) {
	    throw new RuntimeException("Error parsing messages definition at line: " + lineCnt, e);
	}
    }

    private ChunkInfo parseChunk(String line) {
	ChunkInfo chunkInfo = new ChunkInfo();

	if (line.indexOf("---") != -1) {
	    if (line.indexOf("begin") != -1)
		chunkInfo.chunkType = 2;
	    else if (line.indexOf("end") != -1)
		chunkInfo.chunkType = 3;
	    else {
		throw new RuntimeException("Invalid group format: 'begin' or 'end' expected.");
	    }

	    String name = line.substring(line.indexOf("---") + 3);

	    if (chunkInfo.chunkType == 2)
		name = name.substring(0, name.indexOf("begin"));
	    else if (chunkInfo.chunkType == 3) {
		name = name.substring(0, name.indexOf("end"));
	    }
	    name = name.trim();
	    name = name.toUpperCase();

	    if (!isValidGroupName(name)) {
		throw new RuntimeException("Invalid group name: " + name);
	    }

	    chunkInfo.name = name.toUpperCase();
	    chunkInfo.description = groupNameToDescription(name);

	    String str = line.substring(0, line.indexOf("---"));
	    str = str.trim();
	    str = removeSpaces(str);

	    if (str.length() == 0) {
		chunkInfo.minCount = 1;
		chunkInfo.maxCount = 1;
	    } else if ((str.equals("{")) || (str.equals("}"))) {
		chunkInfo.minCount = 1;
		chunkInfo.maxCount = 0;
	    } else if ((str.equals("[{")) || (str.equals("{[")) || (str.equals("}]")) || (str.equals("]}"))) {
		chunkInfo.minCount = 0;
		chunkInfo.maxCount = 0;
	    } else if ((str.equals("[")) || (str.equals("]"))) {
		chunkInfo.minCount = 0;
		chunkInfo.maxCount = 1;
	    } else {
		throw new RuntimeException("Unknown min/max count format.");
	    }
	} else {
	    chunkInfo.chunkType = 1;

	    String body = removeTrailingNumber(line);
	    body = body.trim();

	    String sgmNameBlock = getSegmentNameBlock(body);
	    String description = body.substring(body.indexOf(sgmNameBlock) + sgmNameBlock.length());
	    description = description.trim();

	    if (description.indexOf('\t') != -1) {
		throw new RuntimeException("Tab character detected in description.");
	    }

	    chunkInfo.description = description;

	    sgmNameBlock = sgmNameBlock.trim();
	    sgmNameBlock = removeSpaces(sgmNameBlock);
	    String name = getSegmentName(sgmNameBlock);
	    chunkInfo.name = name;

	    int[] count = getCount(sgmNameBlock);
	    chunkInfo.minCount = count[0];
	    chunkInfo.maxCount = count[1];
	}

	if (chunkInfo.name == null) {
	    throw new RuntimeException("Name is null.");
	}

	if (chunkInfo.description == null) {
	    throw new RuntimeException("Description is null.");
	}

	return chunkInfo;
    }

    private boolean isValidGroupName(String str) {
	if (str.indexOf(' ') != -1) {
	    return false;
	}

	int i = 0;
	for (int n = str.length(); i < n; i++) {
	    char c = str.charAt(i);
	    if ((!Character.isLetter(c)) && (!Character.isDigit(c)) && (c != '_')) {
		return false;
	    }
	}

	return true;
    }

    private String removeSpaces(String str) {
	StringBuffer sb = new StringBuffer();
	int i = 0;
	for (int n = str.length(); i < n; i++) {
	    char c = str.charAt(i);
	    if (!Character.isWhitespace(c)) {
		sb.append(c);
	    }
	}
	return sb.toString();
    }

    private String removeTrailingNumber(String str) {
	for (int i = str.length() - 1; i >= 0; i--) {
	    char c = str.charAt(i);
	    if ((!Character.isDigit(c)) && (c != '.')) {
		return str.substring(0, i + 1);
	    }
	}

	return null;
    }

    private String getSegmentNameBlock(String str) {
	int sgmNameStart = -1;
	int i = 0;
	for (int n = str.length() - 3; i < n; i++) {
	    String tmp = str.substring(i, i + 3);
	    if (isSegmentName(tmp)) {
		sgmNameStart = i;
		break;
	    }
	}

	if (sgmNameStart == -1) {
	    throw new RuntimeException("Segment name not found.");
	}

	i = sgmNameStart + 3;
	for (int n = str.length(); i < n; i++) {
	    char c = str.charAt(i);
	    if (Character.isLetter(c)) {
		return str.substring(0, i);
	    }
	}

	throw new RuntimeException("Segment name block not found.");
    }

    private boolean isSegmentName(String str) {
	if (str.length() != 3) {
	    return false;
	}

	for (int i = 0; i < 3; i++) {
	    char c = str.charAt(i);
	    if (((!Character.isDigit(c)) && (!Character.isLetter(c))) || ((Character.isLetter(c)) && (!Character.isUpperCase(c)))) {
		return false;
	    }
	}

	return true;
    }

    private String getSegmentName(String str) {
	if (str.length() == 3) {
	    if (isSegmentName(str)) {
		return str;
	    }
	    return null;
	}

	int i = 0;
	for (int n = str.length() - 3; i < n; i++) {
	    String tmp = str.substring(i, i + 3);
	    if (isSegmentName(tmp)) {
		return tmp;
	    }
	}

	return null;
    }

    private int[] getCount(String str) {
	int[] count = new int[2];
	String sgmName = getSegmentName(str);

	if (sgmName == null) {
	    throw new RuntimeException("Segment name not found.");
	}

	String tmp = str.substring(0, str.indexOf(sgmName));
	tmp = tmp.trim();

	if (tmp.length() == 0) {
	    count[0] = 1;
	    count[1] = 1;
	} else if (tmp.equals("{")) {
	    count[0] = 1;
	    count[1] = 0;
	} else if ((tmp.equals("[{")) || (tmp.equals("{["))) {
	    count[0] = 0;
	    count[1] = 0;
	} else if (tmp.equals("[")) {
	    count[0] = 0;
	    count[1] = 1;
	} else {
	    throw new RuntimeException("Unknown min/max count: " + tmp);
	}

	return count;
    }

    private String groupNameToDescription(String str) {
	StringBuffer sb = new StringBuffer();
	int i = 0;
	for (int n = str.length(); i < n; i++) {
	    char c = str.charAt(i);
	    if (c == '_')
		sb.append(' ');
	    else {
		sb.append(c);
	    }
	}

	str = sb.toString();
	str = str.toLowerCase();
	str = str.substring(0, 1).toUpperCase() + str.substring(1);

	return str;
    }

    public class ChunkInfo {
	public static final int CHUNK_TYPE_SEGMENT = 1;
	public static final int CHUNK_TYPE_SEGMENT_GROUP_START = 2;
	public static final int CHUNK_TYPE_SEGMENT_GROUP_END = 3;
	String name;
	String description;
	int chunkType;
	int minCount;
	int maxCount;

	public ChunkInfo() {
	}
    }
}