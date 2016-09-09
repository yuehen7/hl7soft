package com.hl7soft.sevenedit.db.defs;

import java.util.List;

public class SegmentListEntry extends SegmentEntry {
	public SegmentListEntry() {
		this(null);
	}

	public SegmentListEntry(List<SegmentEntry> entries) {
		super((String) null);

		setType(3);

		if (entries != null) {
			int i = 0;
			for (int n = entries.size(); i < n; i++)
				addSegmentEntry((SegmentEntry) entries.get(i));
		}
	}
}