package com.hl7soft.sevenedit.db.tables;

public abstract interface ITable {
	public static final int TYPE_USER = 1;
	public static final int TYPE_HL7 = 2;

	public abstract TableItem getItem(int paramInt);

	public abstract int getItemsCount();

	public abstract String getName();

	public abstract int getNumber();

	public abstract int getType();
}