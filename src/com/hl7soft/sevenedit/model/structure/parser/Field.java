package com.hl7soft.sevenedit.model.structure.parser;

import com.hl7soft.sevenedit.db.defs.FieldDefinition;
import com.hl7soft.sevenedit.db.defs.FieldEntry;
import java.util.ArrayList;
import java.util.List;

public class Field implements IFieldContainer {
    String value;
    boolean array;
    FieldDefinition definition;
    FieldEntry entry;
    List<Field> fields;
    IFieldContainer parent;

    public boolean isArray() {
	return this.array;
    }

    public void setArray(boolean array) {
	this.array = array;
    }

    public FieldDefinition getDefinition() {
	return this.definition;
    }

    public void setDefinition(FieldDefinition definition) {
	this.definition = definition;
    }

    public FieldEntry getEntry() {
	return this.entry;
    }

    public void setEntry(FieldEntry entry) {
	this.entry = entry;
    }

    public void addField(int idx, Field field) {
	if (this.fields == null) {
	    this.fields = new ArrayList(1);
	}
	this.fields.add(idx, field);
	registerParent(field);
    }

    public void addField(Field field) {
	if (this.fields == null) {
	    this.fields = new ArrayList(1);
	}
	this.fields.add(field);
	registerParent(field);
    }

    public void removeField(Field field) {
	if ((this.fields == null) || (field == null)) {
	    return;
	}
	this.fields.remove(field);
	unregisterParent(field);
    }

    public void removeField(int idx) {
	removeField(getField(idx));
    }

    public Field getField(int idx) {
	return this.fields != null ? (Field) this.fields.get(idx) : null;
    }

    public int getFieldsCount() {
	return this.fields != null ? this.fields.size() : 0;
    }

    public IFieldContainer getParent() {
	return this.parent;
    }

    public void setParent(IFieldContainer parent) {
	this.parent = parent;
    }

    private void registerParent(Field field) {
	if (field != null)
	    field.setParent(this);
    }

    private void unregisterParent(Field field) {
	if (field != null)
	    field.setParent(null);
    }

    public int getFieldIndex(Field field) {
	if ((this.fields == null) || (field == null)) {
	    return -1;
	}

	return this.fields.indexOf(field);
    }

    public Segment getParentSegment() {
	return getParentSegment(this);
    }

    private Segment getParentSegment(Field field) {
	if (field == null) {
	    return null;
	}

	if ((field.getParent() instanceof Segment)) {
	    return (Segment) field.getParent();
	}

	return getParentSegment((Field) field.getParent());
    }

    public String getValue() {
	return this.value;
    }

    public void setValue(String value) {
	this.value = value;
    }
}