package com.hl7soft.sevenedit.db.tables;

public class TableItem {
	String value;
	String description;

	public TableItem(String value, String description) {
		this.value = value;
		this.description = description;
	}

	public String getValue() {
		return this.value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public TableItem copy() {
		return new TableItem(this.value, this.description);
	}
}