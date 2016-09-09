package com.hl7soft.sevenedit.db.defs;

import java.util.ArrayList;
import java.util.List;

public class MessageDefinition implements ISegmentEntryContainer {
	String name;
	String version;
	String description;
	String notes;
	List<SegmentEntry> entries;
	IDefinitionFactory factory;

	public MessageDefinition(String name, String version) {
		this.name = name;
		this.version = version;
	}

	public MessageDefinition(String name, String version, String description) {
		this.name = name;
		this.version = version;
		this.description = description;
	}

	public MessageDefinition(MessageDefinition definition) {
		this.name = definition.getName();
		this.version = definition.getVersion();
		this.description = definition.getDescription();
		this.notes = definition.getNotes();

		if (definition.getSegmentEntriesCount() > 0) {
			this.entries = new ArrayList(definition.getSegmentEntriesCount());

			int i = 0;
			for (int n = definition.getSegmentEntriesCount(); i < n; i++) {
				SegmentEntry tmpEntry = new SegmentEntry(definition.getSegmentEntry(i));
				tmpEntry.setParentContainer(this);
				this.entries.add(tmpEntry);
			}
		}
	}

	public String getDescription() {
		return this.description;
	}

	public String getName() {
		return this.name;
	}

	public String getNotes() {
		return this.notes;
	}

	public String getVersion() {
		return this.version;
	}

	public int getSegmentEntriesCount() {
		return this.entries != null ? this.entries.size() : 0;
	}

	public IDefinitionFactory getFactory() {
		return this.factory;
	}

	public void setFactory(IDefinitionFactory factory) {
		this.factory = factory;
	}

	public void addSegmentEntry(SegmentEntry segmentEntry) {
		addSegmentEntry(getSegmentEntriesCount(), segmentEntry);
	}

	public void addSegmentEntry(int idx, SegmentEntry segmentEntry) {
		if (segmentEntry == null) {
			return;
		}

		if (this.entries == null) {
			this.entries = new ArrayList(1);
		}

		this.entries.add(idx, segmentEntry);
		segmentEntry.setParentContainer(this);
	}

	public void removeSegmentEntry(int idx) {
		if (this.entries == null) {
			return;
		}

		if ((idx < 0) || (idx >= getSegmentEntriesCount())) {
			return;
		}

		SegmentEntry segmentEntry = getSegmentEntry(idx);
		this.entries.remove(idx);
		segmentEntry.setParentContainer(null);
	}

	public SegmentEntry getSegmentEntry(int idx) {
		return this.entries != null ? (SegmentEntry) this.entries.get(idx) : null;
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

	public void setEntries(List<SegmentEntry> entries) {
		this.entries = entries;
	}

	public static MessageDefinition duplicate(MessageDefinition messageDefinition) {
		MessageDefinition m = new MessageDefinition(messageDefinition.getName(), messageDefinition.getVersion());

		m.setDescription(messageDefinition.getDescription());
		m.setNotes(messageDefinition.getNotes());
		m.setFactory(messageDefinition.getFactory());

		int i = 0;
		for (int n = messageDefinition.getSegmentEntriesCount(); i < n; i++) {
			m.addSegmentEntry(SegmentEntry.create(messageDefinition.getSegmentEntry(i)));
		}

		return m;
	}

	public int getSegmentEntryIndex(ISegmentEntry entry) {
		return (this.entries != null ? Integer.valueOf(this.entries.indexOf(entry)) : null).intValue();
	}
}