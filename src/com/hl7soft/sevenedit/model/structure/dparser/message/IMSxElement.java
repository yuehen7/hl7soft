package com.hl7soft.sevenedit.model.structure.dparser.message;

import com.hl7soft.sevenedit.model.data.DataRange;
import com.hl7soft.sevenedit.model.data.IData;

public abstract interface IMSxElement {
    public abstract IData getData();

    public abstract DataRange getDataRange();

    public abstract IMSxStructure getStructure();

    public abstract IMSxElement getParentElement();

    public abstract IMSxElement getChildAt(int paramInt);

    public abstract int getChildrenCount();

    public abstract int getChildIndex(IMSxElement paramIMSxElement);

    public abstract int getChildIndexByOffset(int paramInt);

    public abstract boolean isReal();
}