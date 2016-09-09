package com.hl7soft.sevenedit.db.defs.io.bin;

import com.hl7soft.sevenedit.model.util.StringHelper;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.TreeMap;
import java.util.TreeSet;

public class FormatDescriptor {
	String formatName;
	String formatVersion;
	TreeMap<String, TreeSet<String>> messageNames;
	TreeMap<String, TreeSet<String>> segmentNames;
	TreeMap<String, TreeSet<String>> fieldNames;

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

	public List<String> getVersions() {
		TreeSet versions = null;

		if (this.fieldNames != null) {
			if (versions == null) {
				versions = new TreeSet();
			}

			versions.addAll(this.fieldNames.keySet());
		}

		if (this.segmentNames != null) {
			if (versions == null) {
				versions = new TreeSet();
			}

			versions.addAll(this.segmentNames.keySet());
		}

		if (this.messageNames != null) {
			if (versions == null) {
				versions = new TreeSet();
			}

			versions.addAll(this.messageNames.keySet());
		}

		List list = null;
		Iterator iterator;
		if (versions != null) {
			list = new ArrayList(versions.size());
			for (iterator = versions.iterator(); iterator.hasNext();) {
				list.add(iterator.next());
			}
		}

		return list;
	}

	public void addMessageName(String version, String name) {
		if ((version == null) || (name == null)) {
			return;
		}

		if (this.messageNames == null) {
			this.messageNames = new TreeMap();
		}

		TreeSet set = (TreeSet) this.messageNames.get(version);
		if (set == null) {
			set = new TreeSet();
			this.messageNames.put(version, set);
		}

		set.add(name);
	}

	public List<String> getMessageNames(String version) {
		if ((this.messageNames == null) || (version == null)) {
			return null;
		}

		TreeSet set = (TreeSet) this.messageNames.get(version);
		if (set == null) {
			return null;
		}

		return new ArrayList(set);
	}

	public void addSegmentName(String version, String name) {
		if ((version == null) || (name == null)) {
			return;
		}

		if (this.segmentNames == null) {
			this.segmentNames = new TreeMap();
		}

		TreeSet set = (TreeSet) this.segmentNames.get(version);
		if (set == null) {
			set = new TreeSet();
			this.segmentNames.put(version, set);
		}

		set.add(name);
	}

	public List<String> getSegmentNames(String version) {
		if ((this.segmentNames == null) || (version == null)) {
			return null;
		}

		TreeSet set = (TreeSet) this.segmentNames.get(version);
		if (set == null) {
			return null;
		}

		return new ArrayList(set);
	}

	public void addFieldName(String version, String name) {
		if ((version == null) || (name == null)) {
			return;
		}

		if (this.fieldNames == null) {
			this.fieldNames = new TreeMap();
		}

		TreeSet set = (TreeSet) this.fieldNames.get(version);
		if (set == null) {
			set = new TreeSet();
			this.fieldNames.put(version, set);
		}

		set.add(name);
	}

	public List<String> getFieldNames(String version) {
		if ((this.fieldNames == null) || (version == null)) {
			return null;
		}

		TreeSet set = (TreeSet) this.fieldNames.get(version);
		if (set == null) {
			return null;
		}

		return new ArrayList(set);
	}

	public void write(OutputStream os) throws IOException {
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os));

		if (this.formatName != null) {
			writer.write("format: " + this.formatName + "\n");
		}
		if (this.formatVersion != null) {
			writer.write("version: " + this.formatVersion + "\n");
		}

		List versions = getVersions();
		if (versions == null) {
			return;
		}
		String versionsStr = StringHelper.implode(versions, ",");
		writer.write("hl7-versions: " + versionsStr + "\n");

		int i = 0;
		for (int n = versions.size(); i < n; i++) {
			String version = (String) versions.get(i);

			List messageNames = getMessageNames(version);
			List segmentNames = getSegmentNames(version);
			List fieldNames = getFieldNames(version);

			String msgNamesStr = StringHelper.implode(messageNames, ",");
			if (msgNamesStr != null) {
				writer.write("hl7-" + version + "-messages: " + msgNamesStr + "\n");
			}

			String sgmNamesStr = StringHelper.implode(segmentNames, ",");
			if (sgmNamesStr != null) {
				writer.write("hl7-" + version + "-segments: " + sgmNamesStr + "\n");
			}

			String fldNamesStr = StringHelper.implode(fieldNames, ",");
			if (fldNamesStr != null) {
				writer.write("hl7-" + version + "-fields: " + fldNamesStr + "\n");
			}
		}

		writer.flush();
	}

	public static FormatDescriptor read(InputStream is) throws IOException {
		Properties props = new Properties();
		props.load(is);

		FormatDescriptor descriptor = new FormatDescriptor();

		descriptor.setFormatName(props.getProperty("format"));
		descriptor.setFormatVersion(props.getProperty("version"));

		String versions = props.getProperty("hl7-versions");
		if (versions == null) {
			return descriptor;
		}

		String[] versionsAry = StringHelper.explode(versions, ',');
		for (int i = 0; i < versionsAry.length; i++) {
			String version = versionsAry[i].trim();

			String messageNamesStr = props.getProperty("hl7-" + version + "-messages");
			String[] messageNames = StringHelper.explode(messageNamesStr, ',');

			if (messageNames != null) {
				for (int j = 0; j < messageNames.length; j++) {
					descriptor.addMessageName(version, messageNames[j].trim());
				}
			}

			String segmentNamesStr = props.getProperty("hl7-" + version + "-segments");
			String[] segmentNames = StringHelper.explode(segmentNamesStr, ',');

			if (segmentNames != null) {
				for (int j = 0; j < segmentNames.length; j++) {
					descriptor.addSegmentName(version, segmentNames[j].trim());
				}
			}

			String fieldNamesStr = props.getProperty("hl7-" + version + "-fields");
			String[] fieldNames = StringHelper.explode(fieldNamesStr, ',');

			if (fieldNames != null) {
				for (int j = 0; j < fieldNames.length; j++) {
					descriptor.addFieldName(version, fieldNames[j].trim());
				}
			}
		}

		return descriptor;
	}
}