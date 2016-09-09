package com.hl7soft.sevenedit.model.structure.dparser.segment2;

import com.hl7soft.sevenedit.model.data.DataRange;
import com.hl7soft.sevenedit.model.data.IData;

public abstract interface ISSxStructureElement {
    public abstract IData getData();

    public abstract DataRange getDataRange();

    public abstract ISSxStructure getParentStructure();

    public abstract ISSxStructureElement getParentElement();

    public abstract ISSxStructureElement getChildAt(int paramInt);

    public abstract int getChildrenCount();

    public abstract int getChildIndex(ISSxStructureElement paramISSxStructureElement);

    public abstract int getChildIndexByOffset(int paramInt);

    public abstract boolean isReal();
}