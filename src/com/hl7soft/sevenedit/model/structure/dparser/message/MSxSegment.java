package com.hl7soft.sevenedit.model.structure.dparser.message;

import com.hl7soft.sevenedit.db.defs.ISegmentEntry;
import com.hl7soft.sevenedit.db.defs.SegmentDefinition;
import com.hl7soft.sevenedit.model.data.DataRange;
import com.hl7soft.sevenedit.model.data.IData;
import com.hl7soft.sevenedit.model.structure.dparser.segment2.ISSxSegment;
import com.hl7soft.sevenedit.model.structure.parser.Delimiters;
import java.util.ArrayList;
import java.util.List;

public class MSxSegment implements IMSxSegment {
	IMSxStructure parentStructure;
	IMSxMessage parentMessage;
	String name;
	ISSxSegment sourceSegment;
	ISegmentEntry segmentEntry;
	SegmentDefinition definition;
	IMSxSegmentContainer parentContainer;
	boolean group;
	boolean array;
	List<MSxSegment> segments;
	DataRange dataRange;
	List<MSxField> fields;

	public MSxSegment(IMSxStructure parentStructure, IMSxMessage parentMessage, String name, ISegmentEntry segmentEntry,
			SegmentDefinition definition, ISSxSegment sourceSegment) {
		this.parentStructure = parentStructure;
		this.segmentEntry = segmentEntry;
		this.parentMessage = parentMessage;
		this.sourceSegment = sourceSegment;
		this.definition = definition;
		this.name = name;

		if ((sourceSegment != null) && (sourceSegment.getDataRange().getStartOffset() != -1))
			this.dataRange = new DataRange(sourceSegment.getDataRange().getStartOffset(),
					sourceSegment.getDataRange().getEndOffset());
	}

	private MSxSegment getFirstRealSegment() {
		int i = 0;
		for (int n = getSegmentsCount(); i < n; i++) {
			if (getSegment(i).isReal()) {
				return getSegment(i);
			}
		}

		return null;
	}

	private MSxSegment getLastRealSegment() {
		for (int i = getSegmentsCount() - 1; i >= 0; i--) {
			if (getSegment(i).isReal()) {
				return getSegment(i);
			}
		}

		return null;
	}

	public Delimiters getDelimiters() {
		return getParentMessage().getDelimiters();
	}

	public SegmentDefinition getDefinition() {
		return this.definition;
	}

	public ISegmentEntry getEntry() {
		return this.segmentEntry;
	}

	public String getName() {
		return this.name;
	}

	public IMSxSegmentContainer getParentContainer() {
		return this.parentContainer;
	}

	public IMSxMessage getParentMessage() {
		return this.parentMessage;
	}

	public IMSxStructure getStructure() {
		return this.parentStructure;
	}

	public String getVersion() {
		return this.parentMessage.getVersion();
	}

	public boolean isArray() {
		return this.array;
	}

	public boolean isGroup() {
		return this.group;
	}

	public int getEndOffset() {
		if ((this.segments != null) && (this.segments.size() > 0)) {
			for (int i = this.segments.size() - 1; i >= 0; i--) {
				MSxSegment segment = (MSxSegment) this.segments.get(i);
				if (segment.isReal()) {
					return segment.getEndOffset();
				}
			}
		} else if (this.sourceSegment != null) {
			return this.sourceSegment.getDataRange().getEndOffset();
		}

		return -1;
	}

	public int getLength() {
		return getEndOffset() - getStartOffset();
	}

	public DataRange getDataRange() {
		if ((!isArray()) && (!isGroup())) {
			return this.dataRange;
		}

		MSxSegment sgm1 = getFirstRealSegment();
		MSxSegment sgm2 = getLastRealSegment();

		if ((sgm1 != null) && (sgm2 != null)) {
			return new DataRange(sgm1.getDataRange().getP0(), sgm2.getDataRange().getP1());
		}

		return null;
	}

	public IData getData() {
		return getStructure().getData();
	}

	public int getStartOffset() {
		if ((this.segments != null) && (this.segments.size() > 0)) {
			int i = 0;
			for (int n = this.segments.size(); i < n; i++) {
				MSxSegment segment = (MSxSegment) this.segments.get(i);
				if (segment.isReal()) {
					return segment.getStartOffset();
				}
			}
		} else if (this.sourceSegment != null) {
			return this.sourceSegment.getDataRange().getStartOffset();
		}

		return -1;
	}

	public void addSegment(int idx, MSxSegment segment) {
		if (this.segments == null) {
			this.segments = new ArrayList(1);
		}

		this.segments.add(idx, segment);
		registerParent(segment);
	}

	public void addSegment(MSxSegment segment) {
		addSegment(getSegmentsCount(), segment);
	}

	public void removeSegment(MSxSegment segment) {
		removeSegment(getSegmentIndex(segment));
	}

	public void removeSegment(int idx) {
		if ((idx < 0) || (idx >= getSegmentsCount())) {
			return;
		}

		MSxSegment segment = getSegment(idx);
		this.segments.remove(idx);
		unregisterParent(segment);
	}

	private void registerParent(MSxSegment segment) {
		if (segment != null)
			segment.setParentContainer(this);
	}

	private void unregisterParent(MSxSegment segment) {
		if (segment != null)
			segment.setParentContainer(null);
	}

	public MSxSegment getSegment(int idx) {
		if ((this.segments == null) || (idx < 0) || (idx >= this.segments.size())) {
			return null;
		}

		return (MSxSegment) this.segments.get(idx);
	}

	public int getSegmentIndex(IMSxSegment segment) {
		return this.segments != null ? this.segments.indexOf(segment) : -1;
	}

	public int getSegmentIndexByOffset(int offs) {
		if ((this.segments == null) || (offs < 0)) {
			return -1;
		}

		int i = 0;
		for (int n = this.segments.size(); i < n; i++) {
			MSxSegment segment = getSegment(i);
			if ((segment.getStartOffset() <= offs) && (offs <= segment.getEndOffset())) {
				return i;
			}
		}

		return -1;
	}

	public int getFieldIndexByOffset(int offs) {
		if (this.fields == null) {
			return -1;
		}

		int i = 0;
		for (int n = this.fields.size(); i < n; i++) {
			IMSxField field = (IMSxField) this.fields.get(i);

			if ((field.getDataRange() != null) && (field.getDataRange().contains(offs))) {
				return i;
			}
		}

		return -1;
	}

	public int getSegmentsCount() {
		return this.segments != null ? this.segments.size() : 0;
	}

	public void setGroup(boolean group) {
		this.group = group;
	}

	public void setArray(boolean array) {
		this.array = array;
	}

	public ISegmentEntry getSegmentEntry() {
		return this.segmentEntry;
	}

	public void setSegmentEntry(ISegmentEntry segmentEntry) {
		this.segmentEntry = segmentEntry;
	}

	public void setParentContainer(IMSxSegmentContainer parentContainer) {
		this.parentContainer = parentContainer;
	}

	public IMSxElement getChildAt(int idx) {
		if ((this.segments != null) && (this.segments.size() > 0))
			return getSegment(idx);
		if ((this.fields != null) && (this.fields.size() > 0)) {
			return getField(idx);
		}

		return null;
	}

	public int getChildrenCount() {
		if ((this.segments != null) && (this.segments.size() > 0))
			return getSegmentsCount();
		if ((this.fields != null) && (this.fields.size() > 0)) {
			return getFieldsCount();
		}

		return 0;
	}

	public int getChildIndex(IMSxElement node) {
		if ((this.segments != null) && (this.segments.size() > 0))
			return getSegmentIndex((IMSxSegment) node);
		if ((this.fields != null) && (this.fields.size() > 0)) {
			return getFieldIndex((IMSxField) node);
		}

		return -1;
	}

	public int getChildIndexByOffset(int offs) {
		if ((this.segments != null) && (this.segments.size() > 0))
			return getSegmentIndexByOffset(offs);
		if ((this.fields != null) && (this.fields.size() > 0)) {
			return getFieldIndexByOffset(offs);
		}

		return -1;
	}

	public IMSxElement getParentElement() {
		return (IMSxElement) this.parentContainer;
	}

	public boolean isReal() {
		return getDataRange() != null;
	}

	public void addField(int idx, MSxField field) {
		if (this.fields == null) {
			this.fields = new ArrayList(1);
		}
		this.fields.add(idx, field);
	}

	public void setField(int idx, MSxField field) {
		if (this.fields == null) {
			this.fields = new ArrayList(1);
		}
		this.fields.set(idx, field);
	}

	public void addField(MSxField field) {
		if (this.fields == null) {
			this.fields = new ArrayList(1);
		}
		this.fields.add(field);
	}

	public void removeField(IMSxFieldContainer field) {
		if ((this.fields == null) || (field == null)) {
			return;
		}
		this.fields.remove(field);
	}

	public void removeField(int idx) {
		removeField(getField(idx));
	}

	public IMSxField getField(int idx) {
		if ((idx < 0) || (idx >= getFieldsCount())) {
			return null;
		}

		return this.fields != null ? (MSxField) this.fields.get(idx) : null;
	}

	public int getFieldIndex(IMSxField field) {
		if ((this.fields == null) || (field == null)) {
			return -1;
		}

		return this.fields.indexOf(field);
	}

	public int getFieldsCount() {
		return this.fields != null ? this.fields.size() : 0;
	}

	public String toString() {
		if (this.segmentEntry != null) {
			if (this.segmentEntry.getType() == 3) {
				StringBuffer sb = new StringBuffer();
				sb.append("Segment List (");
				int i = 0;
				for (int n = this.segmentEntry.getSegmentEntriesCount(); i < n; i++) {
					sb.append(this.segmentEntry.getSegmentEntry(i).getName());
					if (i < n - 1) {
						sb.append(',');
					}
				}
				sb.append(")");

				if (this.sourceSegment != null) {
					sb.append(' ');
					sb.append('[');
					sb.append(this.sourceSegment.getName());
					sb.append(']');
				}

				sb.append(new StringBuilder().append(" (").append(getStartOffset()).append(":").append(getEndOffset())
						.append(")").toString());

				return sb.toString();
			}

			return new StringBuilder().append(this.segmentEntry.getDescription()).append(" [")
					.append(this.segmentEntry.getName()).append("]").append(this.group ? " [GROUP]" : "")
					.append(this.array ? " [ARRAY]" : "").append(" (").append(getStartOffset()).append(":")
					.append(getEndOffset()).append(")").toString();
		}

		return new StringBuilder().append(getName()).append(" Segment").append(" (").append(getStartOffset())
				.append(":").append(getEndOffset()).append(")").toString();
	}
}