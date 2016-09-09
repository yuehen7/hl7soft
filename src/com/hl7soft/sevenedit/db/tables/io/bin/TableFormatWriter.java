package com.hl7soft.sevenedit.db.tables.io.bin;

import com.hl7soft.sevenedit.db.tables.ITable;
import com.hl7soft.sevenedit.db.tables.ITableFactory;
import com.hl7soft.sevenedit.db.tables.TableItem;
import com.hl7soft.sevenedit.db.util.XOROutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class TableFormatWriter {
	public static final int VERSION_3_0 = 1;
	public static final int VERSION_3_1 = 2;
	int formatVersion = 2;
	TableWriterListener listener;
	int stepCnt;
	int totalSteps;

	public void write(ITableFactory factory, OutputStream os) throws IOException {
		if ((factory == null) || (os == null)) {
			return;
		}

		try {
			this.stepCnt = 0;
			this.totalSteps = calculateTotalSteps(factory);

			if (this.listener != null) {
				this.listener.onEvent(new TableWriterEvent(this, 1, -1, -1));
			}

			ZipOutputStream out = new ZipOutputStream(os);
			writeFormatDescriptor(factory, out);
			writeTables(factory, out);

			out.finish();
		} catch (Exception e) {
			throw new IOException("Error writing.", e);
		} finally {
			if (this.listener != null)
				this.listener.onEvent(new TableWriterEvent(this, 2, -1, -1));
		}
	}

	private int calculateTotalSteps(ITableFactory factory) {
		List nums = factory.getTableNumbers();
		return nums != null ? nums.size() : 0;
	}

	private void writeTables(ITableFactory factory, ZipOutputStream out) throws IOException {
		if ((factory == null) || (out == null)) {
			return;
		}

		List<Integer> tableNumbers = factory.getTableNumbers();
		for (Integer num : tableNumbers) {
			ITable table = factory.getTable(num);

			ByteArrayOutputStream os = new ByteArrayOutputStream();
			writeTable(table, encryptionFilter(os));
			os.close();

			String ref = "tables/t-" + num + ".td";
			if (this.formatVersion == 2) {
				ref = "data/tables/t" + num;
			}

			writeZipFileEntry(out, ref, os.toByteArray());

			if (this.listener != null)
				this.listener.onEvent(new TableWriterEvent(this, 3, this.stepCnt++, this.totalSteps));
		}
	}

	private void writeFormatDescriptor(ITableFactory factory, ZipOutputStream out) throws Exception {
		if (this.formatVersion == 1)
			writeLegacyFormatDescriptor(factory, out);
		else
			writeXmlFormatDescriptor(factory, out);
	}

	private void writeLegacyFormatDescriptor(ITableFactory factory, ZipOutputStream out) throws IOException {
		FormatDescriptor descriptor = new FormatDescriptor();
		descriptor.setFormatName("hl7-tables-bin");
		descriptor.setFormatVersion("3.0");

		List tableNumbers = factory.getTableNumbers();
		descriptor.setTableNumbers(tableNumbers);

		ByteArrayOutputStream os = new ByteArrayOutputStream();
		descriptor.write(os);
		os.close();

		writeZipFileEntry(out, "descriptor", os.toByteArray());
	}

	private void writeXmlFormatDescriptor(ITableFactory factory, ZipOutputStream out) throws IOException {
		FormatDescriptorXml d = new FormatDescriptorXml();

		List<Integer> tableNumbers = factory.getTableNumbers();
		for (Integer num : tableNumbers) {
			ITable t = factory.getTable(num);
			if (t != null) {
				d.addTableRecord(num, t.getName(), "t" + num);
			}
		}

		ByteArrayOutputStream os = new ByteArrayOutputStream();
		d.write(os);
		os.close();

		writeZipFileEntry(out, "format.xml", os.toByteArray());
	}

	private static void writeZipFileEntry(ZipOutputStream zos, String zipEntryName, byte[] byteArray)
			throws IOException {
		int byteArraySize = byteArray.length;

		CRC32 crc = new CRC32();
		crc.update(byteArray, 0, byteArraySize);

		ZipEntry entry = new ZipEntry(zipEntryName);
		entry.setMethod(8);
		entry.setSize(byteArraySize);
		entry.setCrc(crc.getValue());

		zos.putNextEntry(entry);
		zos.write(byteArray, 0, byteArraySize);
		zos.closeEntry();
	}

	public void writeTable(ITable table, OutputStream os) throws IOException {
		writeInt(table.getNumber(), os);
		writeString(table.getName(), os);
		writeByte(table.getType(), os);

		writeInt(table.getItemsCount(), os);
		int i = 0;
		for (int n = table.getItemsCount(); i < n; i++)
			writeTableItem(table.getItem(i), os);
	}

	private void writeTableItem(TableItem tableItem, OutputStream out) throws IOException {
		writeString(tableItem.getValue(), out);
		writeString(tableItem.getDescription(), out);
	}

	public void writeString(String str, OutputStream out) throws IOException {
		if (this.formatVersion == 1) {
			writeStringDefaultCharset(str, out);
			return;
		}

		writeStringUTF(str, out);
	}

	public void writeStringDefaultCharset(String str, OutputStream out) throws IOException {
		if (str != null) {
			int strLen = str.length();
			writeInt(strLen, out);
			out.write(str.getBytes());
		} else {
			writeInt(-1, out);
		}
	}

	public void writeStringUTF(String str, OutputStream out) throws IOException {
		if (str == null) {
			writeInt(-1, out);
			return;
		}

		byte[] bytes = str.getBytes("UTF-8");
		writeInt(bytes.length, out);
		out.write(bytes);
	}

	public void writeInt(int i, OutputStream out) throws IOException {
		out.write(i & 0xFF);
		out.write(i >> 8 & 0xFF);
		out.write(i >> 16 & 0xFF);
		out.write(i >> 24 & 0xFF);
	}

	public void writeByte(int i, OutputStream out) throws IOException {
		out.write(i & 0xFF);
	}

	public void writeBoolean(boolean b, OutputStream out) throws IOException {
		out.write(b ? 1 : 0);
	}

	private OutputStream encryptionFilter(OutputStream os) {
		if (os == null) {
			return null;
		}

		if (this.formatVersion == 1) {
			return new XOROutputStream(os, "@22#1~#");
		}

		return os;
	}

	public void addListener(TableWriterListener listener) {
		this.listener = listener;
	}

	public void removeListener(TableWriterListener listener) {
		this.listener = null;
	}

	public int getFormatVersion() {
		return this.formatVersion;
	}

	public void setFormatVersion(int formatVersion) {
		this.formatVersion = formatVersion;
	}
}