package com.hl7soft.sevenedit.db.defs;

import java.util.ArrayList;
import java.util.List;

public class FieldDefinition implements IFieldEntryContainer {
	String name;
	String version;
	String description;
	String notes;
	IDefinitionFactory factory;
	List<FieldEntry> entries;

	public FieldDefinition(String name, String version) {
		this.name = name;
		this.version = version;
	}

	public FieldDefinition(String name, String version, String description) {
		this.name = name;
		this.version = version;
		this.description = description;
	}

	public FieldDefinition(FieldDefinition definition) {
		this.name = definition.getName();
		this.version = definition.getVersion();
		this.description = definition.getDescription();
		this.notes = definition.getNotes();

		if (definition.getFieldEntriesCount() > 0) {
			this.entries = new ArrayList(definition.getFieldEntriesCount());
			int i = 0;
			for (int n = definition.getFieldEntriesCount(); i < n; i++) {
				FieldEntry tmpEntry = new FieldEntry(definition.getFieldEntry(i));
				tmpEntry.setParentContainer(this);
				this.entries.add(tmpEntry);
			}
		}
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getVersion() {
		return this.version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getNotes() {
		return this.notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public boolean isPrimitive() {
		return (this.entries == null) || (this.entries.size() == 0);
	}

	public IDefinitionFactory getFactory() {
		return this.factory;
	}

	public void setFactory(IDefinitionFactory factory) {
		this.factory = factory;
	}

	public void addFieldEntry(FieldEntry fieldEntry) {
		addFieldEntry(getFieldEntriesCount(), fieldEntry);
	}

	public void addFieldEntry(int idx, FieldEntry fieldEntry) {
		if (fieldEntry == null) {
			return;
		}

		if (this.entries == null) {
			this.entries = new ArrayList(1);
		}

		this.entries.add(idx, fieldEntry);
		if ((fieldEntry instanceof FieldEntry))
			fieldEntry.setParentContainer(this);
	}

	public void removeFieldEntry(int idx) {
		if (this.entries == null) {
			return;
		}

		if ((idx < 0) || (idx >= getFieldEntriesCount())) {
			return;
		}

		FieldEntry fieldEntry = getFieldEntry(idx);
		this.entries.remove(idx);

		fieldEntry.setParentContainer(null);
	}

	public int getFieldEntriesCount() {
		return this.entries != null ? this.entries.size() : 0;
	}

	public FieldEntry getFieldEntry(int idx) {
		return this.entries != null ? (FieldEntry) this.entries.get(idx) : null;
	}

	public static FieldDefinition create(FieldDefinition fieldDefinition) {
		FieldDefinition f = new FieldDefinition(fieldDefinition.getName(), fieldDefinition.getVersion());
		f.setDescription(fieldDefinition.getDescription());
		f.setFactory(fieldDefinition.getFactory());
		f.setNotes(fieldDefinition.getNotes());

		for (int i = 0; i < fieldDefinition.getFieldEntriesCount(); i++) {
			f.addFieldEntry(FieldEntry.create(fieldDefinition.getFieldEntry(i)));
		}

		return f;
	}

	public int getFieldEntryIndex(FieldEntry entry) {
		return this.entries != null ? this.entries.indexOf(entry) : -1;
	}
}