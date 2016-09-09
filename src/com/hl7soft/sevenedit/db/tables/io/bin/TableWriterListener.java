package com.hl7soft.sevenedit.db.tables.io.bin;

public abstract interface TableWriterListener {
	public abstract void onEvent(TableWriterEvent paramTableWriterEvent);
}