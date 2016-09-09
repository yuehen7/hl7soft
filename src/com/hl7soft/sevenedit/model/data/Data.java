package com.hl7soft.sevenedit.model.data;

public class Data implements IData {
	private static final char[] empty = new char[0];
	char[] data;
	private int count;

	public Data() {
		this(10);
	}

	public Data(String str) {
		this(str.length() + 1);
		doInsert(0, str);
	}

	public Data(int initialLength) {
		if (initialLength < 1) {
			initialLength = 0;
		}
		this.data = new char[initialLength];
		this.data[0] = '\r';
		this.count = 1;
	}

	public Data(IData data) {
		this(data.getLength());
		insert(0, data.getString(0, data.getLength() - 1));
	}

	public DataChunk getDataChunk(int offs, int len, DataChunk chunk) {
		if (offs + len > this.count) {
			throw new RuntimeException("Invalid chunk (offs:" + offs + ", len:" + len + ")");
		}

		if (chunk == null) {
			chunk = new DataChunk();
		}

		chunk.array = this.data;
		chunk.offset = offs;
		chunk.count = len;
		return chunk;
	}

	public char charAt(int offs) {
		return this.data[offs];
	}

	public int getLength() {
		return this.count;
	}

	public String getString(int offs, int len) {
		if (offs + len > this.count) {
			throw new RuntimeException("Invalid range: " + this.count);
		}

		if (len == 0) {
			return "";
		}

		return new String(this.data, offs, len);
	}

	public String getString() {
		return getString(0, this.count - 1);
	}

	public void insert(int offs, String str) {
		doInsert(offs, str);
	}

	private void doInsert(int offs, String str) {
		if ((offs > this.count) || (offs < 0)) {
			throw new RuntimeException("Invalid location: " + this.count);
		}
		char[] chars = str.toCharArray();
		replace(offs, 0, chars, 0, chars.length);
	}

	public void remove(int offs, int len) {
		doRemove(offs, len);
	}

	public String doRemove(int offs, int len) {
		if (offs + len > this.count) {
			throw new RuntimeException("Invalid range: " + this.count);
		}
		String removedString = getString(offs, len);
		replace(offs, len, empty, 0, 0);
		return removedString;
	}

	public void replace(int offs, int len, String str) {
		remove(offs, len);
		insert(offs, str);
	}

	void replace(int offset, int length, char[] replArray, int replOffset, int replLength) {
		int delta = replLength - length;
		int src = offset + length;
		int nmove = this.count - src;
		int dest = src + delta;
		if (this.count + delta >= this.data.length) {
			int newLength = Math.max(2 * this.data.length, this.count + delta);
			char[] newData = new char[newLength];
			System.arraycopy(this.data, 0, newData, 0, offset);
			System.arraycopy(replArray, replOffset, newData, offset, replLength);
			System.arraycopy(this.data, src, newData, dest, nmove);
			this.data = newData;
		} else {
			System.arraycopy(this.data, src, this.data, dest, nmove);
			System.arraycopy(replArray, replOffset, this.data, offset, replLength);
		}
		this.count += delta;
	}

	void resize(int ncount) {
		char[] ndata = new char[ncount];
		System.arraycopy(this.data, 0, ndata, 0, Math.min(ncount, this.count));
		this.data = ndata;
	}

	public String toString() {
		return getString();
	}
}