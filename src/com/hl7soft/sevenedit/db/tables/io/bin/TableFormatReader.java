package com.hl7soft.sevenedit.db.tables.io.bin;

import com.hl7soft.sevenedit.db.tables.ITable;
import com.hl7soft.sevenedit.db.tables.Table;
import com.hl7soft.sevenedit.db.tables.TableFactory;
import com.hl7soft.sevenedit.db.tables.TableItem;
import com.hl7soft.sevenedit.db.util.XORInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class TableFormatReader {
	public static final int VERSION_3_0 = 1;
	public static final int VERSION_3_1 = 2;
	int formatVersion = 2;
	ZipFile zipFile;
	File file;
	TableFactory tableFactory;
	TableReaderListener listener;

	public TableFormatReader(File file) {
		this.file = file;
	}

	public void read() {
		try {
			if (this.listener != null) {
				this.listener.onEvent(new TableReaderEvent(this, 1, -1, -1));
			}

			this.zipFile = new ZipFile(this.file, 1);

			List<Integer> tableNumbers = null;

			if (this.zipFile.getEntry("format.xml") != null) {
				this.formatVersion = 2;
				FormatDescriptorXml d = FormatDescriptorXml
						.read(this.zipFile.getInputStream(this.zipFile.getEntry("format.xml")));
				tableNumbers = d.getTableNumbers();
			} else if (this.zipFile.getEntry("descriptor") != null) {
				this.formatVersion = 1;
				FormatDescriptor d = FormatDescriptor
						.read(this.zipFile.getInputStream(this.zipFile.getEntry("descriptor")));
				tableNumbers = d.getTableNumbers();
			} else {
				throw new RuntimeException("Unknown format.");
			}

			int stepCnt = 0;
			int totalSteps = tableNumbers.size();

			TableFactory tableFactory = new TableFactory();

			for (Integer num : tableNumbers) {
				String ref = "tables/t-" + num + ".td";
				if (this.formatVersion == 2) {
					ref = "data/tables/t" + num;
				}

				ZipEntry entry = this.zipFile.getEntry(ref);
				if (entry != null) {
					ITable table = readTable(decryptionFilter(this.zipFile.getInputStream(entry)));
					tableFactory.addTable(table);

					if (this.listener != null) {
						this.listener.onEvent(new TableReaderEvent(this, 3, stepCnt++, totalSteps));
					}
				}
			}

			this.tableFactory = tableFactory;
		} catch (Exception e) {
			throw new RuntimeException("Error reading tables file.", e);
		} finally {
			if (this.listener != null)
				this.listener.onEvent(new TableReaderEvent(this, 2, -1, -1));
		}
	}

	private InputStream decryptionFilter(InputStream is) {
		if (is == null) {
			return null;
		}

		if (this.formatVersion == 1) {
			return new XORInputStream(is, "@22#1~#");
		}

		return is;
	}

	public TableFactory getTableFactory() {
		return this.tableFactory;
	}

	public void addListener(TableReaderListener listener) {
		this.listener = listener;
	}

	public void removeListener(TableReaderListener listener) {
		this.listener = null;
	}

	public ITable readTable(InputStream is) throws IOException {
		Integer number = Integer.valueOf(readInt(is));
		String name = readString(is);
		Integer type = Integer.valueOf(readByte(is));

		Table table = new Table(number.intValue(), name, type.intValue());
		Integer itemsCount = Integer.valueOf(readInt(is));
		for (int i = 0; i < itemsCount.intValue(); i++) {
			table.addItem(readTableItem(is));
		}

		return table;
	}

	private TableItem readTableItem(InputStream is) throws IOException {
		String value = readString(is);
		String description = readString(is);

		return new TableItem(value, description);
	}

	public boolean readBoolean(InputStream is) throws IOException {
		return is.read() == 1;
	}

	public int readByte(InputStream is) throws IOException {
		return is.read();
	}

	public int readInt(InputStream is) throws IOException {
		int i = 0;
		i = is.read();
		i |= is.read() << 8;
		i |= is.read() << 16;
		i |= is.read() << 24;
		return i;
	}

	public String readString(InputStream is) throws IOException {
		if (this.formatVersion == 1) {
			return readStringDefaultCharset(is);
		}

		return readStringUTF(is);
	}

	public String readStringDefaultCharset(InputStream is) throws IOException {
		int strLen = readInt(is);
		if (strLen == -1) {
			return null;
		}
		byte[] buf = new byte[strLen];
		is.read(buf);
		String str = new String(buf);
		return str;
	}

	public String readStringUTF(InputStream is) throws IOException {
		int len = readInt(is);
		if (len == -1) {
			return null;
		}
		byte[] buf = new byte[len];
		is.read(buf);
		return new String(buf, Charset.forName("UTF-8"));
	}
}