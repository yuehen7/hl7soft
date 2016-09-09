package com.hl7soft.sevenedit.db.defs;

public class FieldEntry {
	public static final int OPT_OPTIONAL = 1;
	public static final int OPT_REQUIRED = 2;
	public static final int OPT_CONDITIONAL = 3;
	public static final int OPT_BACK_COMPAT = 4;
	public static final int OPT_NOT_USED = 5;
	String name;
	String defaultValue;
	String notes;
	String description;
	int maxLength;
	int tableNumber;
	int optionality = 1;

	int repeatCount = 1;
	IFieldEntryContainer parentContainer;

	public FieldEntry(String name) {
		this.name = name;
	}

	public FieldEntry(String name, String description) {
		this.name = name;
		this.description = description;
	}

	public FieldEntry(FieldEntry entry) {
		this.name = entry.getName();
		this.defaultValue = entry.getDefaultValue();
		this.notes = entry.getNotes();
		this.description = entry.getDescription();
		this.maxLength = entry.getMaxLength();
		this.tableNumber = entry.getTableNumber();
		this.optionality = entry.getOptionality();
		this.repeatCount = entry.getRepeatCount();
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDefaultValue() {
		return this.defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public String getNotes() {
		return this.notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getMaxLength() {
		return this.maxLength;
	}

	public void setMaxLength(int maxLength) {
		this.maxLength = maxLength;
	}

	public int getTableNumber() {
		return this.tableNumber;
	}

	public void setTableNumber(int tableNumber) {
		this.tableNumber = tableNumber;
	}

	public int getRepeatCount() {
		return this.repeatCount;
	}

	public void setRepeatCount(int repeatCount) {
		this.repeatCount = repeatCount;
	}

	public IFieldEntryContainer getParentContainer() {
		return this.parentContainer;
	}

	public void setParentContainer(IFieldEntryContainer parentContainer) {
		this.parentContainer = parentContainer;
	}

	public int getOptionality() {
		return this.optionality;
	}

	public void setOptionality(int optionality) {
		this.optionality = optionality;
	}

	public static FieldEntry create(FieldEntry fieldEntry) {
		FieldEntry f = new FieldEntry(fieldEntry.getName());
		f.setDefaultValue(fieldEntry.getDefaultValue());
		f.setDescription(fieldEntry.getDescription());
		f.setMaxLength(fieldEntry.getMaxLength());
		f.setNotes(fieldEntry.getNotes());
		f.setOptionality(fieldEntry.getOptionality());
		f.setParentContainer(fieldEntry.getParentContainer());
		f.setRepeatCount(fieldEntry.getRepeatCount());
		f.setTableNumber(fieldEntry.getTableNumber());

		return f;
	}
}