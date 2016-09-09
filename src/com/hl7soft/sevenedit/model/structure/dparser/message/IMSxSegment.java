package com.hl7soft.sevenedit.model.structure.dparser.message;

import com.hl7soft.sevenedit.db.defs.ISegmentEntry;
import com.hl7soft.sevenedit.db.defs.SegmentDefinition;
import com.hl7soft.sevenedit.model.structure.parser.Delimiters;

public abstract interface IMSxSegment extends IMSxSegmentContainer, IMSxFieldContainer, IMSxElement {
	public abstract String getName();

	public abstract String getVersion();

	public abstract Delimiters getDelimiters();

	public abstract boolean isGroup();

	public abstract boolean isArray();

	public abstract SegmentDefinition getDefinition();

	public abstract ISegmentEntry getEntry();

	public abstract IMSxSegmentContainer getParentContainer();

	public abstract IMSxMessage getParentMessage();

	public abstract IMSxStructure getStructure();
}