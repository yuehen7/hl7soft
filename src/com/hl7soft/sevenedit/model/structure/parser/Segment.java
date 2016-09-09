package com.hl7soft.sevenedit.model.structure.parser;

import com.hl7soft.sevenedit.db.defs.SegmentDefinition;
import java.util.ArrayList;
import java.util.List;

public class Segment implements IFieldContainer {
    String name;
    SegmentDefinition definition;
    List<Field> fields;

    public Segment(String name) {
	this.name = name;
    }

    public SegmentDefinition getDefinition() {
	return this.definition;
    }

    public void setDefinition(SegmentDefinition definition) {
	this.definition = definition;
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

    private void registerParent(Field field) {
	if (field != null)
	    field.setParent(this);
    }

    private void unregisterParent(Field field) {
	if (field != null)
	    field.setParent(null);
    }

    public Field getField(int idx) {
	if ((idx < 0) || (idx >= getFieldsCount())) {
	    return null;
	}

	return this.fields != null ? (Field) this.fields.get(idx) : null;
    }

    public int getFieldsCount() {
	return this.fields != null ? this.fields.size() : 0;
    }

    public int getFieldIndex(Field field) {
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
}