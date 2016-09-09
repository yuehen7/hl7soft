package com.hl7soft.sevenedit.db.defs.io.bin;

public class UnknownDefinitionFileFormat extends Exception {
	public UnknownDefinitionFileFormat() {
	}

	public UnknownDefinitionFileFormat(String message, Throwable cause) {
		super(message, cause);
	}

	public UnknownDefinitionFileFormat(String message) {
		super(message);
	}

	public UnknownDefinitionFileFormat(Throwable cause) {
		super(cause);
	}
}