package com.hl7soft.sevenedit.model.structure.path;

import java.util.ArrayList;
import java.util.List;

public class IndexPath {
	List<Integer> indexes;

	public void addValue(int value) {
		if (this.indexes == null) {
			this.indexes = new ArrayList(2);
		}
		this.indexes.add(Integer.valueOf(value));
	}

	public int getValue(int idx) {
		if ((this.indexes == null) || (idx < 0) || (idx >= this.indexes.size())) {
			return -1;
		}

		return ((Integer) this.indexes.get(idx)).intValue();
	}

	public void setValue(int idx, int value) {
		this.indexes.set(idx, Integer.valueOf(value));
	}

	public void removeValue(int idx) {
		this.indexes.remove(idx);
	}

	public int getSize() {
		return this.indexes != null ? this.indexes.size() : 0;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Path: (");
		if (this.indexes != null) {
			int i = 0;
			for (int n = this.indexes.size(); i < n; i++) {
				sb.append(this.indexes.get(i));
				if (i < n - 1) {
					sb.append(',');
				}
			}
		}
		sb.append(")");
		return sb.toString();
	}
}