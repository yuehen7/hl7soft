package com.hl7soft.sevenedit.model.structure.dparser.segment2;

import com.hl7soft.sevenedit.db.defs.IDefinitionFactory;
import com.hl7soft.sevenedit.model.data.IData;

public abstract interface ISSxStructure extends ISSxStructureElement {
    public abstract IData getData();

    public abstract ISSxSegment getSegment(int paramInt);

    public abstract ISSxSegment getSegmentByOffset(int paramInt);

    public abstract int getSegmentIndexByOffset(int paramInt);

    public abstract int getSegmentIndex(ISSxSegment paramISSxSegment);

    public abstract int getSegmentsCount();

    public abstract boolean isIncludeEmpty();

    public abstract IDefinitionFactory getDefinitionFactory();
}