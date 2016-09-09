package com.hl7soft.sevenedit.model.structure.dparser.segment2;

import com.hl7soft.sevenedit.db.defs.FieldDefinition;
import com.hl7soft.sevenedit.db.defs.FieldEntry;
import com.hl7soft.sevenedit.model.data.DataChunk;
import com.hl7soft.sevenedit.model.data.DataRange;
import com.hl7soft.sevenedit.model.data.IData;
import com.hl7soft.sevenedit.model.structure.parser.Delimiters;
import com.hl7soft.sevenedit.model.util.DataHelper;
import com.hl7soft.sevenedit.model.util.StringHelper;
import java.util.ArrayList;
import java.util.List;

public class SSxField implements ISSxField {
    FieldDefinition definition;
    FieldEntry entry;
    DataRange dataRange;
    List<SSxField> fields;
    ISSxStructure structure;
    ISSxFieldContainer parentContainer;
    boolean parsed;
    int level;
    boolean array;
    int fieldIndex;
    int countChildren;

    public SSxField(ISSxStructure structure, ISSxFieldContainer parent, int fieldIndex, int level, FieldEntry entry, FieldDefinition definition) {
	this(structure, parent, fieldIndex, level, entry, definition, null);
    }

    public SSxField(ISSxStructure structure, ISSxFieldContainer parent, int fieldIndex, int level, FieldEntry entry, FieldDefinition definition, DataRange dataRange) {
	this.structure = structure;
	this.parentContainer = parent;
	this.dataRange = dataRange;
	this.fieldIndex = fieldIndex;
	this.level = level;
	this.entry = entry;
	this.definition = definition;
	this.array = checkArray();
	this.countChildren = doCountChildren();
    }

    public boolean isArray() {
	return this.array;
    }

    public boolean checkArray() {
	try {
	    if (getParentStructure() == null) {
		return false;
	    }

	    IData idata = getParentStructure().getData();
	    DataChunk data = idata.getDataChunk(0, idata.getLength(), null);
	    ISSxSegment parentSegment = getParentSegment();

	    if (parentSegment == null) {
		return false;
	    }

	    Delimiters delimiters = getParentSegment().getDelimiters();
	    if ((delimiters == null) || (!isReal())) {
		return false;
	    }

	    if (this.dataRange == null) {
		return false;
	    }

	    int repCharIdx = StringHelper.indexOf(data, delimiters.getRepeatDelimiter(), getDataRange().getStartOffset(), getDataRange().getEndOffset());
	    return (repCharIdx != -1) && (getLevel() == 0) && (!isDelimitersContaner());
	} catch (Exception e) {
	}
	return false;
    }

    private boolean isDelimitersContaner() {
	if (!(getParent() instanceof ISSxSegment)) {
	    return false;
	}

	ISSxSegment parentSegment = getParentSegment();
	return (DataHelper.isDelimiterContainerSegment(parentSegment.getName())) && (this.fieldIndex < 2);
    }

    public boolean isPrimitive() {
	if (isArray()) {
	    return false;
	}

	if (this.definition != null) {
	    return this.definition.isPrimitive();
	}

	if (getParentSegment() != null) {
	    if ((DataHelper.isDelimiterContainerSegment(getParentSegment().getName()) & this.fieldIndex < 2)) {
		return true;
	    }
	}

	return countChildren() == 0;
    }

    public FieldDefinition getDefinition() {
	return this.definition;
    }

    public FieldEntry getEntry() {
	return this.entry;
    }

    public ISSxStructureElement getChildAt(int idx) {
	return getField(idx);
    }

    public int getChildrenCount() {
	return getFieldsCount();
    }

    public void addField(int idx, SSxField field) {
	if (this.fields == null) {
	    this.fields = new ArrayList(1);
	}
	this.fields.add(idx, field);
    }

    public void setField(int idx, SSxField field) {
	if (this.fields == null) {
	    this.fields = new ArrayList(1);
	}
	this.fields.set(idx, field);
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
	return this.fields != null ? (SSxField) this.fields.get(idx) : null;
    }

    public int getFieldsCount() {
	return this.fields != null ? this.fields.size() : 0;
    }

    public ISSxFieldContainer getParent() {
	return this.parentContainer;
    }

    public int getFieldIndex(ISSxField field) {
	if ((this.fields == null) || (field == null)) {
	    return -1;
	}

	return this.fields.indexOf(field);
    }

    public int getChildIndex(ISSxStructureElement node) {
	return getFieldIndex((ISSxField) node);
    }

    public boolean isAbsent() {
	return !isReal();
    }

    public ISSxSegment getParentSegment() {
	if ((getParentElement() instanceof ISSxSegment)) {
	    return (ISSxSegment) getParentElement();
	}

	return ((ISSxField) getParentElement()).getParentSegment();
    }

    public int getLevel() {
	return this.level;
    }

    public ISSxStructureElement getParentElement() {
	return (ISSxStructureElement) this.parentContainer;
    }

    public ISSxStructure getParentStructure() {
	return this.structure;
    }

    public int countChildren() {
	return this.countChildren;
    }

    private int doCountChildren() {
	IData idata = getParentStructure().getData();
	DataChunk data = idata.getDataChunk(0, idata.getLength(), null);
	FieldDefinition fieldDefinition = getDefinition();
	ISSxSegment parentSegment = getParentSegment();
	Delimiters delimiters = parentSegment.getDelimiters();

	if (isArray()) {
	    int[] idxsRep = StringHelper.explode(data, delimiters.getRepeatDelimiter(), getDataRange().getStartOffset(), getDataRange().getEndOffset());
	    return idxsRep.length / 2;
	}

	if ((fieldDefinition != null) && (fieldDefinition.isPrimitive()))
	    return 0;
	char delim;
	if (getLevel() == 0) {
	    delim = delimiters.getComponentDelimiter();
	} else {
	    if (getLevel() == 1)
		delim = delimiters.getSubcomponentDelimiter();
	    else
		return 0;
	}
	int[] idxs = getDataRange() != null ? StringHelper.explode(data, delim, getDataRange().getStartOffset(), getDataRange().getEndOffset()) : new int[0];

	if ((fieldDefinition == null) && (idxs.length / 2 == 1)) {
	    return 0;
	}

	int entriesCount = fieldDefinition != null ? fieldDefinition.getFieldEntriesCount() : 0;
	int n = Math.max(entriesCount, idxs.length / 2);
	return n;
    }

    public int getFieldIndexByOffset(int offs) {
	if (this.fields == null) {
	    return -1;
	}

	int i = 0;
	for (int n = this.fields.size(); i < n; i++) {
	    ISSxField field = (ISSxField) this.fields.get(i);

	    if ((field.getDataRange() != null) && (field.getDataRange().getStartOffset() <= offs) && (offs <= field.getDataRange().getEndOffset())) {
		return i;
	    }
	}

	return -1;
    }

    public int getChildIndexByOffset(int offs) {
	return getFieldIndexByOffset(offs);
    }

    public boolean isReal() {
	return this.dataRange != null;
    }

    public IData getData() {
	return getParentStructure().getData();
    }

    public DataRange getDataRange() {
	return this.dataRange;
    }

    public void setDataRange(DataRange dataRange) {
	this.dataRange = dataRange;
    }

    public ISSxFieldContainer getParentContainer() {
	return this.parentContainer;
    }

    public String getName() {
	return this.entry != null ? this.entry.getName() : null;
    }

    public int getTableNumber() {
	return this.entry != null ? this.entry.getTableNumber() : 0;
    }

    public String toString() {
	if (this.entry != null) {
	    return new StringBuilder().append(isArray() ? "Array - " : "").append(this.entry.getDescription()).append(" [").append(this.entry.getName()).append("] (")
		    .append(getDataRange()).append(", lev: ").append(this.level).append(")").toString();
	}
	return new StringBuilder().append("Field (").append(getDataRange().getStartOffset()).append(":").append(getDataRange().getEndOffset()).append(", lev: ").append(this.level)
		.append(")").toString();
    }
}