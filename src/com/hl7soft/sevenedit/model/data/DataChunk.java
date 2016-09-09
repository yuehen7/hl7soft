package com.hl7soft.sevenedit.model.data;

public class DataChunk implements CharSequence {
    public char[] array;
    public int offset;
    public int count;

    public DataChunk() {
	this(null, 0, 0);
    }

    public DataChunk(char[] array, int offset, int count) {
	this.array = array;
	this.offset = offset;
	this.count = count;
    }

    public int getStartOffset() {
	return this.offset;
    }

    public char charAt(int index) {
	if ((index < 0) || (index >= this.count)) {
	    throw new StringIndexOutOfBoundsException(index);
	}
	return this.array[(this.offset + index)];
    }

    public int length() {
	return this.count;
    }

    public DataChunk subSequence(int start, int end) {
	if (start < 0)
	    throw new StringIndexOutOfBoundsException(start);
	if (end > count)
	    throw new StringIndexOutOfBoundsException(end);
	if (start > end) {
	    throw new StringIndexOutOfBoundsException(end - start);
	} else {
	    DataChunk chunk = new DataChunk();
	    chunk.array = array;
	    chunk.offset = offset + start;
	    chunk.count = end - start;
	    return chunk;
	}
    }

    public String toString() {
	if (this.array != null) {
	    return new String(this.array, this.offset, this.count);
	}
	return new String();
    }
}