package com.hl7soft.sevenedit.model.structure.dparser.message;

import com.hl7soft.sevenedit.db.defs.FieldDefinition;
import com.hl7soft.sevenedit.db.defs.FieldEntry;
import com.hl7soft.sevenedit.model.data.DataRange;
import com.hl7soft.sevenedit.model.data.IData;
import com.hl7soft.sevenedit.model.structure.dparser.segment2.ISSxField;
import java.util.ArrayList;
import java.util.List;

public class MSxField implements IMSxField, IMSxFieldContainer {
    IMSxStructure parentStructure;
    List<MSxField> fields;
    ISSxField sourceField;
    IMSxFieldContainer container;
    IMSxSegment parentSegment;
    FieldEntry entry;
    FieldDefinition definition;
    DataRange dataRange;
    int level;

    public MSxField(IMSxStructure parentStructure, IMSxSegment parentSegment, IMSxFieldContainer container, FieldEntry entry, FieldDefinition definition, int level,
	    ISSxField sourceField) {
	this.parentSegment = parentSegment;
	this.parentStructure = parentStructure;
	this.sourceField = sourceField;
	this.container = container;
	this.entry = entry;
	this.definition = definition;
	this.level = level;

	if ((sourceField != null) && (sourceField.getDataRange() != null))
	    this.dataRange = new DataRange(sourceField.getDataRange().getStartOffset(), sourceField.getDataRange().getEndOffset());
    }

    public IMSxElement getChildAt(int idx) {
	return getField(idx);
    }

    public int getChildrenCount() {
	return getFieldsCount();
    }

    public int getChildIndex(IMSxElement node) {
	return getFieldIndex((IMSxField) node);
    }

    public int getChildIndexByOffset(int offs) {
	return getFieldIndexByOffset(offs);
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

    public IMSxElement getParentElement() {
	return (IMSxElement) this.container;
    }

    public IMSxStructure getStructure() {
	return this.parentStructure;
    }

    public boolean isParsed() {
	return true;
    }

    public void parse() {
    }

    public void unparse() {
    }

    public FieldDefinition getDefinition() {
	if (this.sourceField != null) {
	    return this.sourceField.getDefinition();
	}

	return this.definition;
    }

    public FieldEntry getEntry() {
	if (this.sourceField != null) {
	    return this.sourceField.getEntry();
	}

	return this.entry;
    }

    public int getLevel() {
	return this.level;
    }

    public IMSxSegment getParentSegment() {
	return this.parentSegment;
    }

    public boolean isArray() {
	return this.sourceField != null ? this.sourceField.isArray() : false;
    }

    public boolean isPrimitive() {
	if (this.sourceField != null) {
	    return this.sourceField.isPrimitive();
	}

	if (getDefinition() != null) {
	    return getDefinition().isPrimitive();
	}

	return false;
    }

    public IData getData() {
	return this.parentStructure.getData();
    }

    public DataRange getDataRange() {
	return this.dataRange;
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

    public boolean isReal() {
	return this.dataRange != null;
    }

    public IMSxFieldContainer getParentContainer() {
	return this.container;
    }

    public String getName() {
	return this.entry != null ? this.entry.getName() : null;
    }

    public int getTableNumber() {
	return this.entry != null ? this.entry.getTableNumber() : -1;
    }

    public String toString() {
	if (this.entry != null) {
	    return new StringBuilder().append(isArray() ? "Array - " : "").append(this.entry.getDescription()).append(" [").append(this.entry.getName()).append("] (")
		    .append(getDataRange()).append(", lev: ").append(getLevel()).append(")").toString();
	}
	return new StringBuilder().append("Field (").append(getDataRange()).append(", lev: ").append(getLevel()).append(")").toString();
    }
}