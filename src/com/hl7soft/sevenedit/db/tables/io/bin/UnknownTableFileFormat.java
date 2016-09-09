package com.hl7soft.sevenedit.db.tables.io.bin;

public class UnknownTableFileFormat extends Exception {
	public UnknownTableFileFormat() {
	}

	public UnknownTableFileFormat(String message, Throwable cause) {
		super(message, cause);
	}

	public UnknownTableFileFormat(String message) {
		super(message);
	}

	public UnknownTableFileFormat(Throwable cause) {
		super(cause);
	}
}