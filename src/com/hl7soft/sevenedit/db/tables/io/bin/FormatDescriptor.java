package com.hl7soft.sevenedit.db.tables.io.bin;

import com.hl7soft.sevenedit.model.util.StringHelper;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class FormatDescriptor {
	String formatName;
	String formatVersion;
	List<Integer> tableNumbers;

	public String getFormatVersion() {
		return this.formatVersion;
	}

	public void setFormatVersion(String formatVersion) {
		this.formatVersion = formatVersion;
	}

	public String getFormatName() {
		return this.formatName;
	}

	public void setFormatName(String formatName) {
		this.formatName = formatName;
	}

	public List<Integer> getTableNumbers() {
		return this.tableNumbers;
	}

	public void setTableNumbers(List<Integer> tableNumbers) {
		this.tableNumbers = tableNumbers;
	}

	public void write(OutputStream os) throws IOException {
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os));

		if (this.formatName != null) {
			writer.write("format: " + this.formatName + "\n");
		}
		if (this.formatVersion != null) {
			writer.write("version: " + this.formatVersion + "\n");
		}

		if (this.tableNumbers == null) {
			return;
		}

		List tableNumbersStrList = new ArrayList(this.tableNumbers.size());
		int i = 0;
		for (int n = this.tableNumbers.size(); i < n; i++) {
			tableNumbersStrList.add(((Integer) this.tableNumbers.get(i)).toString());
		}

		String versionsStr = StringHelper.implode(tableNumbersStrList, ",");
		writer.write("hl7-tables: " + versionsStr + "\n");

		writer.flush();
	}

	public static FormatDescriptor read(InputStream is) throws IOException {
		Properties props = new Properties();
		props.load(is);

		FormatDescriptor descriptor = new FormatDescriptor();

		descriptor.setFormatName(props.getProperty("format"));
		descriptor.setFormatVersion(props.getProperty("version"));

		String tableNumbersStr = props.getProperty("hl7-tables");
		if (tableNumbersStr == null) {
			return descriptor;
		}

		String[] tableNumsAry = StringHelper.explode(tableNumbersStr, ',');
		List tableNumbers = new ArrayList(tableNumsAry.length);
		for (int i = 0; i < tableNumsAry.length; i++) {
			String tableNum = tableNumsAry[i].trim();
			tableNumbers.add(Integer.valueOf(Integer.parseInt(tableNum)));
		}

		descriptor.setTableNumbers(tableNumbers);

		return descriptor;
	}
}