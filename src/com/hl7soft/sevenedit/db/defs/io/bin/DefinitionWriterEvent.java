package com.hl7soft.sevenedit.db.defs.io.bin;

public class DefinitionWriterEvent {
	public static final int STARTED = 1;
	public static final int FINISHED = 2;
	public static final int PROGRESS = 3;
	int id;
	int step;
	int totalSteps;
	DefinitionFormatWriter source;

	public DefinitionWriterEvent(DefinitionFormatWriter source, int id, int step, int totalSteps) {
		this.id = id;
		this.source = source;
		this.step = step;
		this.totalSteps = totalSteps;
	}

	public int getStep() {
		return this.step;
	}

	public int getTotalSteps() {
		return this.totalSteps;
	}

	public DefinitionFormatWriter getSource() {
		return this.source;
	}

	public int getId() {
		return this.id;
	}
}