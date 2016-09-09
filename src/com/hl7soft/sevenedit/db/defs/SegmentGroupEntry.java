package com.hl7soft.sevenedit.db.defs;

import java.util.List;

public class SegmentGroupEntry extends SegmentEntry {
	public SegmentGroupEntry(String name) {
		this(name, null);
	}

	public SegmentGroupEntry(String name, List<SegmentEntry> entries) {
		super(name);

		setType(2);

		if (entries != null) {
			int i = 0;
			for (int n = entries.size(); i < n; i++)
				addSegmentEntry((SegmentEntry) entries.get(i));
		}
	}
}