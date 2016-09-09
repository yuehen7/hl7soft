package com.hl7soft.sevenedit.model.structure.dparser.message;

import com.hl7soft.sevenedit.db.defs.MessageDefinition;
import com.hl7soft.sevenedit.model.structure.parser.Delimiters;

public abstract interface IMSxMessage extends IMSxSegmentContainer, IMSxElement {
	public abstract String getName();

	public abstract String getVersion();

	public abstract Delimiters getDelimiters();

	public abstract MessageDefinition getDefinition();

	public abstract IMSxStructure getStructure();
}