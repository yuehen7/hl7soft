package com.hl7soft.sevenedit.db.defs;

import java.util.ArrayList;
import java.util.List;

public class SegmentEntry implements ISegmentEntry, ISegmentEntryContainer {
	String name;
	String description;
	String notes;
	int minCount;
	int maxCount;
	int type = 1;
	List<SegmentEntry> entries;
	ISegmentEntryContainer parentContainer;

	public SegmentEntry(String name) {
		this.name = name;
	}

	public SegmentEntry(String name, int type) {
		this.name = name;
		this.type = type;
	}

	public SegmentEntry(int type, String name, String description, int minCount, int maxCount) {
		this.type = type;
		this.name = name;
		this.description = description;
		this.minCount = minCount;
		this.maxCount = maxCount;
	}

	public SegmentEntry(ISegmentEntry entry) {
		this.name = entry.getName();
		this.description = entry.getDescription();
		this.notes = entry.getNotes();
		this.minCount = entry.getMinCount();
		this.maxCount = entry.getMaxCount();
		this.type = entry.getType();

		if (entry.getSegmentEntriesCount() > 0) {
			this.entries = new ArrayList(entry.getSegmentEntriesCount());
			int i = 0;
			for (int n = entry.getSegmentEntriesCount(); i < n; i++) {
				SegmentEntry tmpEntry = new SegmentEntry(entry.getSegmentEntry(i));
				tmpEntry.setParentContainer(this);
				this.entries.add(tmpEntry);
			}
		}
	}

	public static SegmentEntry create(ISegmentEntry segmentEntry) {
		SegmentEntry s = new SegmentEntry(segmentEntry.getName());

		s.setDescription(segmentEntry.getDescription());
		s.setNotes(segmentEntry.getNotes());
		s.setMinCount(segmentEntry.getMinCount());
		s.setMaxCount(segmentEntry.getMaxCount());
		s.setType(segmentEntry.getType());
		s.setParentContainer(segmentEntry.getParentContainer());

		int i = 0;
		for (int n = segmentEntry.getSegmentEntriesCount(); i < n; i++) {
			s.addSegmentEntry(create(segmentEntry.getSegmentEntry(i)));
		}

		return s;
	}

	public String getDescription() {
		return this.description;
	}

	public int getMaxCount() {
		return this.maxCount;
	}

	public int getMinCount() {
		return this.minCount;
	}

	public String getName() {
		return this.name;
	}

	public String getNotes() {
		return this.notes;
	}

	public ISegmentEntryContainer getParentContainer() {
		return this.parentContainer;
	}

	public int getType() {
		return this.type;
	}

	public int getSegmentEntriesCount() {
		return this.entries != null ? this.entries.size() : 0;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public void setMinCount(int minCount) {
		this.minCount = minCount;
	}

	public void setMaxCount(int maxCount) {
		this.maxCount = maxCount;
	}

	public void setType(int type) {
		this.type = type;
	}

	public void setParentContainer(ISegmentEntryContainer parentContainer) {
		this.parentContainer = parentContainer;
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

	public int getSegmentEntryIndex(ISegmentEntry entry) {
		return (this.entries != null ? Integer.valueOf(this.entries.indexOf(entry)) : null).intValue();
	}

	public List<SegmentEntry> getEntries() {
		return this.entries;
	}

	public void setEntries(List<SegmentEntry> entries) {
		this.entries = entries;
	}
}