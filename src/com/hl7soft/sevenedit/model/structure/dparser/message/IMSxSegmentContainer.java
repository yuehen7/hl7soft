package com.hl7soft.sevenedit.model.structure.dparser.message;

public abstract interface IMSxSegmentContainer {
	public abstract void addSegment(MSxSegment paramMSxSegment);

	public abstract void addSegment(int paramInt, MSxSegment paramMSxSegment);

	public abstract void removeSegment(MSxSegment paramMSxSegment);

	public abstract void removeSegment(int paramInt);

	public abstract int getSegmentsCount();

	public abstract IMSxSegment getSegment(int paramInt);

	public abstract int getSegmentIndex(IMSxSegment paramIMSxSegment);

	public abstract int getSegmentIndexByOffset(int paramInt);
}