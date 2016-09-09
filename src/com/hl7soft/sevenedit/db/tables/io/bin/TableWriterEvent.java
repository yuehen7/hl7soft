package com.hl7soft.sevenedit.db.tables.io.bin;

public class TableWriterEvent {
	public static final int STARTED = 1;
	public static final int FINISHED = 2;
	public static final int PROGRESS = 3;
	int id;
	int step;
	int totalSteps;
	TableFormatWriter source;

	public TableWriterEvent(TableFormatWriter source, int id, int step, int totalSteps) {
		this.source = source;
		this.id = id;
		this.step = step;
		this.totalSteps = totalSteps;
	}

	public int getStep() {
		return this.step;
	}

	public int getTotalSteps() {
		return this.totalSteps;
	}

	public TableFormatWriter getSource() {
		return this.source;
	}

	public int getId() {
		return this.id;
	}
}