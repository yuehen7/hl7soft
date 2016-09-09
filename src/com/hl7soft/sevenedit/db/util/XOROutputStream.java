package com.hl7soft.sevenedit.db.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class XOROutputStream extends OutputStream {
	OutputStream os;
	private byte[] key = null;

	private int count = 0;

	private byte previous = 27;

	public XOROutputStream(OutputStream os, String privateKey) {
		this.os = os;
		this.key = privateKey.getBytes();
	}

	public void write(int i) throws IOException {
		byte b = (byte) i;
		b = (byte) (b ^ (this.key[(this.count++ % this.key.length)] ^ this.previous));
		this.os.write(b & 0xFF);
		this.previous = ((byte) i);
	}

	public static void main(String[] args) {
		try {
			String key = "ABCDEF";

			String phrase = "This is test sequence.";

			ByteArrayOutputStream bos = new ByteArrayOutputStream();

			XOROutputStream os = new XOROutputStream(bos, key);
			os.write(phrase.getBytes());
			os.close();
			byte[] encoded = bos.toByteArray();

			System.out.println(new String(encoded));

			XORInputStream is = new XORInputStream(new ByteArrayInputStream(encoded), key);
			byte[] decoded = new byte[encoded.length];
			is.read(decoded);

			System.out.println(new String(decoded));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}