package com.hl7soft.sevenedit.model.structure.dparser.message;

import com.hl7soft.sevenedit.db.defs.MessageDefinition;
import com.hl7soft.sevenedit.model.data.DataRange;
import com.hl7soft.sevenedit.model.data.IData;
import com.hl7soft.sevenedit.model.structure.parser.Delimiters;
import java.util.ArrayList;
import java.util.List;

public class MSxMessage implements IMSxMessage {
	String name;
	String version;
	MessageDefinition definition;
	List<MSxSegment> segments;
	MSxStructure structure;
	DataRange dataRange;
	Delimiters delimiters;

	public MSxMessage(MSxStructure structure, String name, String version, Delimiters delimiters,
			MessageDefinition definition, DataRange dataRange) {
		this.structure = structure;
		this.name = name;
		this.version = version;
		this.definition = definition;
		this.delimiters = delimiters;
		this.dataRange = dataRange;
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

	public int getSegmentsCount() {
		return this.segments != null ? this.segments.size() : 0;
	}

	public MessageDefinition getDefinition() {
		return this.definition;
	}

	public String getName() {
		return this.name;
	}

	public String getVersion() {
		return this.version;
	}

	public int getEndOffset() {
		return (getDataRange() != null ? Integer.valueOf(getDataRange().getEndOffset()) : null).intValue();
	}

	public int getLength() {
		return getEndOffset() - getStartOffset();
	}

	public int getStartOffset() {
		return (getDataRange() != null ? Integer.valueOf(getDataRange().getStartOffset()) : null).intValue();
	}

	public MSxStructure getStructure() {
		return this.structure;
	}

	public void setParentStructure(MSxStructure parentStructure) {
		this.structure = parentStructure;
	}

	public void setDefinition(MessageDefinition definition) {
		this.definition = definition;
	}

	public IMSxElement getChildAt(int idx) {
		return getSegment(idx);
	}

	public int getChildrenCount() {
		return getSegmentsCount();
	}

	public int getChildIndex(IMSxElement node) {
		return getSegmentIndex((IMSxSegment) node);
	}

	public int getChildIndexByOffset(int offs) {
		return getSegmentIndexByOffset(offs);
	}

	public IMSxElement getParentElement() {
		return getStructure();
	}

	public void addChildren(int idx, IMSxElement[] children) {
		int i = idx;
		for (int n = idx + children.length; i < n; i++)
			addSegment(idx, (MSxSegment) children[(i - idx)]);
	}

	public IMSxElement[] getChildren(int idx, int len) {
		IMSxElement[] children = new IMSxElement[len];
		int i = idx;
		for (int n = idx + len; i < n; i++) {
			children[(i - idx)] = getSegment(i);
		}

		return children;
	}

	public void removeChildren(int idx, int len) {
		for (int i = 0; i < len; i++)
			removeSegment(idx);
	}

	public IData getData() {
		return getStructure().getData();
	}

	public void setDataRange(DataRange dataRange) {
		this.dataRange = dataRange;
	}

	public DataRange getDataRange() {
		return this.dataRange;
	}

	public String toString() {
		if (this.definition != null) {
			return this.definition.getDescription() + " [" + this.definition.getName() + "]";
		}
		return "Message";
	}

	public boolean isReal() {
		return this.dataRange != null;
	}

	public Delimiters getDelimiters() {
		return this.delimiters;
	}
}