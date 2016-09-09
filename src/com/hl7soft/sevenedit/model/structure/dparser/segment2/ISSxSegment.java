package com.hl7soft.sevenedit.model.structure.dparser.segment2;

import com.hl7soft.sevenedit.db.defs.SegmentDefinition;
import com.hl7soft.sevenedit.model.structure.parser.Delimiters;

public abstract interface ISSxSegment extends ISSxFieldContainer, ISSxStructureElement {
	public abstract String getName();

	public abstract String getVersion();

	public abstract SegmentDefinition getDefinition();

	public abstract Delimiters getDelimiters();

	public abstract boolean isValidSegmentFormat();
}