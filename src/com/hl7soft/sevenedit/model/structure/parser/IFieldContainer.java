package com.hl7soft.sevenedit.model.structure.parser;

public abstract interface IFieldContainer {
	public abstract int getFieldsCount();

	public abstract Field getField(int paramInt);

	public abstract int getFieldIndex(Field paramField);

	public abstract void addField(int paramInt, Field paramField);

	public abstract void addField(Field paramField);

	public abstract void removeField(Field paramField);

	public abstract void removeField(int paramInt);
}