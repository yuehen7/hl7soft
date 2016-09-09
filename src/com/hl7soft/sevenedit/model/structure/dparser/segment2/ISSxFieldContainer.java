package com.hl7soft.sevenedit.model.structure.dparser.segment2;

public abstract interface ISSxFieldContainer {
	public abstract int getFieldsCount();

	public abstract ISSxField getField(int paramInt);

	public abstract int getFieldIndex(ISSxField paramISSxField);
}