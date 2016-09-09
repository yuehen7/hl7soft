package com.hl7soft.sevenedit.db.tables;

import java.util.ArrayList;
import java.util.List;

public class Table implements ITable {
	int number;
	int type = 1;
	String name;
	List<TableItem> items = new ArrayList(2);

	public Table(int number, String name, int type) {
		this.number = number;
		this.name = name;
		this.type = type;
	}

	public TableItem getItem(int idx) {
		return (TableItem) this.items.get(idx);
	}

	public void addItem(TableItem item) {
		addItem(getItemsCount(), item);
	}

	public void addItem(int idx, TableItem item) {
		this.items.add(idx, item);
	}

	public void removeItem(TableItem item) {
		this.items.remove(item);
	}

	public int getIndex(TableItem item) {
		return this.items.indexOf(item);
	}

	public int getItemsCount() {
		return this.items.size();
	}

	public String getName() {
		return this.name;
	}

	public int getNumber() {
		return this.number;
	}

	public int getType() {
		return this.type;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public void setType(int type) {
		this.type = type;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getValues() {
		ArrayList values = new ArrayList();
		for (int i = 0; i < this.items.size(); i++) {
			values.add(((TableItem) this.items.get(i)).getValue());
		}
		return values;
	}

	public Table copy() {
		Table copy = new Table(this.number, this.name, this.type);
		for (int i = 0; i < this.items.size(); i++) {
			copy.addItem(((TableItem) this.items.get(i)).copy());
		}
		return copy;
	}
}