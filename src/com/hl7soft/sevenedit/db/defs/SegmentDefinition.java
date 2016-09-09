package com.hl7soft.sevenedit.db.defs;

import java.util.ArrayList;
import java.util.List;

public class SegmentDefinition implements IFieldEntryContainer {
	String name;
	String version;
	String description;
	String notes;
	IDefinitionFactory factory;
	List<FieldEntry> entries;

	public SegmentDefinition(String name, String version) {
		this.name = name;
		this.version = version;
	}

	public SegmentDefinition(String name, String version, String description) {
		this.name = name;
		this.version = version;
		this.description = description;
	}

	public SegmentDefinition(SegmentDefinition definition) {
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

	public String getDescription() {
		return this.description;
	}

	public IDefinitionFactory getFactory() {
		return this.factory;
	}

	public String getName() {
		return this.name;
	}

	public String getNotes() {
		return this.notes;
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

	public String getVersion() {
		return this.version;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public void setFactory(IDefinitionFactory factory) {
		this.factory = factory;
	}

	public static SegmentDefinition create(SegmentDefinition segmentDefinition) {
		SegmentDefinition s = new SegmentDefinition(segmentDefinition.getName(), segmentDefinition.getVersion());

		s.setDescription(segmentDefinition.getDescription());
		s.setFactory(segmentDefinition.getFactory());
		s.setNotes(segmentDefinition.getNotes());

		for (int i = 0; i < segmentDefinition.getFieldEntriesCount(); i++) {
			s.addFieldEntry(FieldEntry.create(segmentDefinition.getFieldEntry(i)));
		}

		return s;
	}

	public int getFieldEntryIndex(FieldEntry entry) {
		return this.entries != null ? this.entries.indexOf(entry) : -1;
	}
}