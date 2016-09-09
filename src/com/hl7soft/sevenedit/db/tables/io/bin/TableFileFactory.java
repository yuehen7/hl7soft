package com.hl7soft.sevenedit.db.tables.io.bin;

import com.hl7soft.sevenedit.db.tables.ITable;
import com.hl7soft.sevenedit.db.tables.ITableFactory;
import com.hl7soft.sevenedit.db.tables.Table;
import com.hl7soft.sevenedit.db.tables.TableItem;
import com.hl7soft.sevenedit.db.util.XORInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class TableFileFactory implements ITableFactory {
	public static final int VERSION_3_0 = 1;
	public static final int VERSION_3_1 = 2;
	int formatVersion = 2;
	TreeMap<Integer, ITable> cache;
	File file;
	ZipFile zipFile;
	IFormatDescriptor descriptor;

	public TableFileFactory(File file) throws FileNotFoundException, UnknownTableFileFormat {
		this.file = file;
		initialize();
	}

	private void initialize() throws FileNotFoundException, UnknownTableFileFormat {
		try {
			if (!this.file.exists()) {
				throw new FileNotFoundException("File not found: " + this.file.getAbsolutePath());
			}
			try {
				this.zipFile = new ZipFile(this.file, 1);
			} catch (Exception e) {
				throw new UnknownTableFileFormat("Unknown format");
			}

			if (this.zipFile.getEntry("format.xml") != null) {
				this.formatVersion = 2;
				FormatDescriptorXml d = FormatDescriptorXml
						.read(this.zipFile.getInputStream(this.zipFile.getEntry("format.xml")));
				this.descriptor = new XmlFormatDescriptor(d);
			} else if (this.zipFile.getEntry("descriptor") != null) {
				this.formatVersion = 1;
				FormatDescriptor d = FormatDescriptor
						.read(this.zipFile.getInputStream(this.zipFile.getEntry("descriptor")));
				this.descriptor = new LegacyFormatDescriptor(d);
			} else {
				throw new RuntimeException("Unknown format.");
			}
		} catch (FileNotFoundException e) {
			throw e;
		} catch (UnknownTableFileFormat e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException("Unexpected initialization exception.", e);
		}
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

	public boolean isTableCached(int idx) {
		if (this.cache == null) {
			return false;
		}

		return this.cache.get(Integer.valueOf(idx)) != null;
	}

	public File getFile() {
		return this.file;
	}

	public void flushResources() {
		this.cache = null;
	}

	public ITable getTable(Integer num) {
		if (!isTableCached(num.intValue())) {
			if (this.cache == null) {
				this.cache = new TreeMap();
			}

			String ref = this.descriptor.getTableRef(num);
			ZipEntry tableEntry = this.zipFile.getEntry(ref);
			if (tableEntry != null) {
				try {
					ITable table = readTable(decryptionFilter(this.zipFile.getInputStream(tableEntry)));
					this.cache.put(num, table);
				} catch (Exception e) {
					throw new RuntimeException("Error loading table.", e);
				}
			}
		}

		return (ITable) this.cache.get(num);
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

	public List<Integer> getTableNumbers() {
		return this.descriptor != null ? this.descriptor.getTableNumbers() : null;
	}

	public String getTableName(Integer num) {
		if (this.formatVersion == 1) {
			ITable t = getTable(num);
			return t != null ? t.getName() : null;
		}

		return this.descriptor.getTableName(num);
	}

	class XmlFormatDescriptor implements TableFileFactory.IFormatDescriptor {
		FormatDescriptorXml descriptor;

		public XmlFormatDescriptor(FormatDescriptorXml descriptor) {
			this.descriptor = descriptor;
		}

		public String getTableName(Integer num) {
			return this.descriptor.getTableName(num);
		}

		public List<Integer> getTableNumbers() {
			return this.descriptor.getTableNumbers();
		}

		public String getTableRef(Integer num) {
			return this.descriptor.getTablesLocation() + "/" + this.descriptor.getTableRef(num);
		}
	}

	class LegacyFormatDescriptor implements TableFileFactory.IFormatDescriptor {
		FormatDescriptor descriptor;

		public LegacyFormatDescriptor(FormatDescriptor descriptor) {
			this.descriptor = descriptor;
		}

		public String getTableName(Integer num) {
			return null;
		}

		public List<Integer> getTableNumbers() {
			return this.descriptor.getTableNumbers();
		}

		public String getTableRef(Integer num) {
			return "tables/t-" + num + ".td";
		}
	}

	static abstract interface IFormatDescriptor {
		public abstract List<Integer> getTableNumbers();

		public abstract String getTableName(Integer paramInteger);

		public abstract String getTableRef(Integer paramInteger);
	}
}