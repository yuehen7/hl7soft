package com.hl7soft.sevenedit.db.tables;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class TableFactory implements ITableFactory {
	TreeMap<Integer, ITable> tables = new TreeMap();

	public void addTable(ITable table) {
		this.tables.put(Integer.valueOf(table.getNumber()), table);
	}

	public void removeTable(Integer tableNumber) {
		this.tables.remove(tableNumber);
	}

	public ITable getTable(Integer tableNumber) {
		return (ITable) this.tables.get(tableNumber);
	}

	public List<Integer> getTableNumbers() {
		return new ArrayList(this.tables.keySet());
	}

	public void clear() {
		this.tables.clear();
	}

	public String getTableName(Integer num) {
		ITable t = getTable(num);
		return t != null ? t.getName() : null;
	}
}