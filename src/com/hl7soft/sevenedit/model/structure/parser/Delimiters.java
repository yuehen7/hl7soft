package com.hl7soft.sevenedit.model.structure.parser;

public class Delimiters {
	char fieldDelimiter = '|';

	char compDelimiter = '^';

	char subcompDelimiter = '&';

	char repeatDelimiter = '~';

	char escapeDelimiter = '\\';

	public Delimiters() {
	}

	public Delimiters(char fieldDelimiter, char compDelimiter, char repeatDelimiter, char escapeDelimiter,
			char subcompDelimiter) {
		this.fieldDelimiter = fieldDelimiter;
		this.compDelimiter = compDelimiter;
		this.repeatDelimiter = repeatDelimiter;
		this.escapeDelimiter = escapeDelimiter;
		this.subcompDelimiter = subcompDelimiter;
	}

	public char getComponentDelimiter() {
		return this.compDelimiter;
	}

	public void setComponentDelimiter(char compSep) {
		this.compDelimiter = compSep;
	}

	public char getEscapeDelimiter() {
		return this.escapeDelimiter;
	}

	public void setEscapeDelimiter(char escapeChar) {
		this.escapeDelimiter = escapeChar;
	}

	public char getFieldDelimiter() {
		return this.fieldDelimiter;
	}

	public void setFieldDelimiter(char fieldSep) {
		this.fieldDelimiter = fieldSep;
	}

	public char getRepeatDelimiter() {
		return this.repeatDelimiter;
	}

	public void setRepeatDelimiter(char repeatSep) {
		this.repeatDelimiter = repeatSep;
	}

	public char getSubcomponentDelimiter() {
		return this.subcompDelimiter;
	}

	public void setSubcomponentDelimiter(char subcompSep) {
		this.subcompDelimiter = subcompSep;
	}

	public byte[] toBytes() {
		byte[] ary = new byte[5];
		ary[0] = ((byte) this.fieldDelimiter);
		ary[1] = ((byte) this.compDelimiter);
		ary[2] = ((byte) this.repeatDelimiter);
		ary[3] = ((byte) this.escapeDelimiter);
		ary[4] = ((byte) this.subcompDelimiter);
		return ary;
	}

	public char[] toChars() {
		char[] ary = new char[5];
		ary[0] = this.fieldDelimiter;
		ary[1] = this.compDelimiter;
		ary[2] = this.repeatDelimiter;
		ary[3] = this.escapeDelimiter;
		ary[4] = this.subcompDelimiter;
		return ary;
	}

	public Delimiters duplicate() {
		return new Delimiters(this.fieldDelimiter, this.compDelimiter, this.repeatDelimiter, this.escapeDelimiter,
				this.subcompDelimiter);
	}

	public String toString() {
		return "" + this.fieldDelimiter + this.compDelimiter + this.repeatDelimiter + this.escapeDelimiter
				+ this.subcompDelimiter;
	}
}