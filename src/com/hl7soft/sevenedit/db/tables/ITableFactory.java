package com.hl7soft.sevenedit.db.tables;

import java.util.List;

public abstract interface ITableFactory {
	public abstract ITable getTable(Integer paramInteger);

	public abstract List<Integer> getTableNumbers();

	public abstract String getTableName(Integer paramInteger);
}