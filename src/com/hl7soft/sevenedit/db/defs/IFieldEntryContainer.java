package com.hl7soft.sevenedit.db.defs;

public abstract interface IFieldEntryContainer {
	public abstract FieldEntry getFieldEntry(int paramInt);

	public abstract int getFieldEntriesCount();

	public abstract int getFieldEntryIndex(FieldEntry paramFieldEntry);

	public abstract void addFieldEntry(FieldEntry paramFieldEntry);

	public abstract void addFieldEntry(int paramInt, FieldEntry paramFieldEntry);

	public abstract void removeFieldEntry(int paramInt);
}