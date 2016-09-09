package com.hl7soft.sevenedit.db.tables.io.bin;

public abstract interface TableReaderListener {
	public abstract void onEvent(TableReaderEvent paramTableReaderEvent);
}