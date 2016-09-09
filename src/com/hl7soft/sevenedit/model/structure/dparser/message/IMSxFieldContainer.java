package com.hl7soft.sevenedit.model.structure.dparser.message;

public abstract interface IMSxFieldContainer {
	public abstract int getFieldsCount();

	public abstract IMSxField getField(int paramInt);

	public abstract int getFieldIndex(IMSxField paramIMSxField);
}