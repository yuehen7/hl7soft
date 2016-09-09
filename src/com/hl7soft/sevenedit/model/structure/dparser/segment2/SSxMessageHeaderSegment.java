package com.hl7soft.sevenedit.model.structure.dparser.segment2;

import com.hl7soft.sevenedit.db.defs.SegmentDefinition;
import com.hl7soft.sevenedit.model.data.DataRange;
import com.hl7soft.sevenedit.model.structure.parser.Delimiters;

public class SSxMessageHeaderSegment extends SSxSegment {
    String messageName;
    String messageVersion;

    public SSxMessageHeaderSegment(SSxStructure structure, String messageName, String version, DataRange dataRange, Delimiters delimiters, SegmentDefinition definition) {
	super(structure, "MSH", version, dataRange, delimiters, definition);
	this.messageVersion = version;
	this.messageName = messageName;
    }

    public String getMessageName() {
	return this.messageName;
    }

    public String getVersion() {
	return this.messageVersion;
    }

    public void setVersion(String messageVersion) {
	this.messageVersion = messageVersion;
    }

    public void setMessageName(String messageName) {
	this.messageName = messageName;
    }
}