package com.hl7soft.sevenedit.db.util;

import java.io.IOException;
import java.io.InputStream;

public class XORInputStream extends InputStream {
	InputStream is;
	private byte[] key = null;

	private int count = 0;

	private byte previous = 27;

	public XORInputStream(InputStream is, String privateKey) {
		this.is = is;
		this.key = privateKey.getBytes();
	}

	public int read() throws IOException {
		int i = this.is.read();

		if (i == -1) {
			return -1;
		}

		byte b = (byte) i;
		b = (byte) (b ^ (this.previous ^ this.key[(this.count++ % this.key.length)]));
		this.previous = b;

		return b & 0xFF;
	}

	public int available() throws IOException {
		return this.is.available();
	}
}