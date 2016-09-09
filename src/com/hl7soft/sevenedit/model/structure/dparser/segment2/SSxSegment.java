package com.hl7soft.sevenedit.model.structure.dparser.segment2;

import com.hl7soft.sevenedit.db.defs.SegmentDefinition;
import com.hl7soft.sevenedit.model.data.DataChunk;
import com.hl7soft.sevenedit.model.data.DataRange;
import com.hl7soft.sevenedit.model.data.IData;
import com.hl7soft.sevenedit.model.structure.parser.Delimiters;
import com.hl7soft.sevenedit.model.util.DataHelper;
import com.hl7soft.sevenedit.model.util.StringHelper;
import java.util.ArrayList;
import java.util.List;

public class SSxSegment implements ISSxSegment {
    String name;
    String version;
    DataRange dataRange;
    Delimiters delimiters;
    SegmentDefinition definition;
    List<SSxField> fields;
    boolean parsed;
    SSxStructure structure;
    boolean isValid;
    int countChildren;

    public SSxSegment(SSxStructure structure, String name, String version, DataRange dataRange, Delimiters delimiters, SegmentDefinition definition) {
	this.structure = structure;
	this.name = name;
	this.version = version;
	this.dataRange = dataRange;
	this.delimiters = delimiters;
	this.definition = definition;
	this.isValid = checkValid();
	this.countChildren = doCountChildren();
    }

    public SegmentDefinition getDefinition() {
	return this.definition;
    }

    public void addField(int idx, SSxField field) {
	if (this.fields == null) {
	    this.fields = new ArrayList(1);
	}
	this.fields.add(idx, field);
    }

    public void addField(SSxField field) {
	if (this.fields == null) {
	    this.fields = new ArrayList(1);
	}
	this.fields.add(field);
    }

    public void removeField(ISSxField field) {
	if ((this.fields == null) || (field == null)) {
	    return;
	}
	this.fields.remove(field);
    }

    public void removeField(int idx) {
	removeField(getField(idx));
    }

    public ISSxField getField(int idx) {
	if ((idx < 0) || (idx >= getFieldsCount())) {
	    return null;
	}

	return this.fields != null ? (SSxField) this.fields.get(idx) : null;
    }

    public int countChildren() {
	return this.countChildren;
    }

    private int doCountChildren() {
	try {
	    if ((getName() == null) || (!isValidSegmentFormat())) {
		return 0;
	    }

	    IData idata = getParentStructure().getData();
	    DataChunk data = idata.getDataChunk(0, idata.getLength(), null);
	    Delimiters delimiters = getDelimiters();
	    SegmentDefinition segmentDefinition = getDefinition();

	    if (delimiters == null) {
		return 0;
	    }

	    boolean containsDelimiters = DataHelper.isDelimiterContainerSegment(getName());
	    int[] idxs = StringHelper.explode(data, delimiters.getFieldDelimiter(), getDataRange().getStartOffset(), getDataRange().getEndOffset());
	    int fieldEntriesCount = segmentDefinition != null ? segmentDefinition.getFieldEntriesCount() : 0;
	    return Math.max(fieldEntriesCount, idxs.length / 2 - (containsDelimiters ? 0 : 1));
	} catch (Exception e) {
	}
	return 0;
    }

    public int getFieldsCount() {
	return this.fields != null ? this.fields.size() : 0;
    }

    public ISSxStructureElement getChildAt(int idx) {
	return getField(idx);
    }

    public int getChildrenCount() {
	return getFieldsCount();
    }

    public int getChildIndex(ISSxStructureElement node) {
	if ((this.fields == null) || (node == null)) {
	    return -1;
	}

	return this.fields.indexOf(node);
    }

    public int getFieldIndex(ISSxField field) {
	if ((this.fields == null) || (field == null)) {
	    return -1;
	}

	return this.fields.indexOf(field);
    }

    public void removeAllFields() {
	this.fields = null;
    }

    public String getName() {
	return this.name;
    }

    public void setName(String name) {
	this.name = name;
    }

    public String toString() {
	if (this.definition != null) {
	    return this.definition.getDescription() + " [" + this.definition.getName() + "]" + " (" + getDataRange().getStartOffset() + ":" + getDataRange().getEndOffset() + ")";
	}
	return this.name + " Segment" + " (" + getDataRange().getStartOffset() + ":" + getDataRange().getEndOffset() + ")";
    }

    public ISSxStructureElement getParentElement() {
	return getParentStructure();
    }

    public SSxStructure getParentStructure() {
	return this.structure;
    }

    public Delimiters getDelimiters() {
	return this.delimiters;
    }

    public String getVersion() {
	if (this.structure == null) {
	    return null;
	}

	int idx = this.structure.getSegmentIndex(this);
	for (int i = idx - 1; i >= 0; i--) {
	    ISSxSegment sgm = this.structure.getSegment(i);
	    if ((sgm instanceof SSxMessageHeaderSegment)) {
		SSxMessageHeaderSegment msh = (SSxMessageHeaderSegment) sgm;
		return msh.getVersion();
	    }
	}

	return null;
    }

    public int getFieldIndexByOffset(int offs) {
	if (this.fields == null) {
	    return -1;
	}

	int i = 0;
	for (int n = this.fields.size(); i < n; i++) {
	    ISSxField field = (ISSxField) this.fields.get(i);

	    if ((field.getDataRange().getStartOffset() <= offs) && (offs <= field.getDataRange().getEndOffset())) {
		return i;
	    }
	}

	return -1;
    }

    public int getChildIndexByOffset(int offs) {
	return getFieldIndexByOffset(offs);
    }

    public boolean isValidSegmentFormat() {
	return this.isValid;
    }

    private boolean checkValid() {
	try {
	    if (getName() == null) {
		return false;
	    }

	    Delimiters delimiters = getDelimiters();

	    if (delimiters == null) {
		return false;
	    }

	    if (getDataRange().getLength() > 3) {
		IData data = getParentStructure().getData();
		DataChunk chunk = data.getDataChunk(getDataRange().getStartOffset(), getDataRange().getLength(), null);
		if (chunk.charAt(3) != delimiters.getFieldDelimiter()) {
		    return false;
		}

	    }

	    int i = 0;
	    for (int n = this.name.length(); i < n; i++) {
		int ch = this.name.charAt(i);
		if (((!Character.isLetter(ch)) || (!Character.isUpperCase(ch))) && (!Character.isDigit(ch))) {
		    return false;
		}
	    }

	    return true;
	} catch (Exception e) {
	}
	return false;
    }

    public boolean isReal() {
	return getDataRange() != null;
    }

    public IData getData() {
	return getParentStructure().getData();
    }

    public DataRange getDataRange() {
	return this.dataRange;
    }
}