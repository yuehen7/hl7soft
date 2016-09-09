package com.hl7soft.sevenedit.model;

import java.util.HashMap;
import java.util.Map;

public class ModelEvent {
    public static final int TITLE_CHANGED = 1;
    public static final int DOCUMENT_ADDED = 2;
    public static final int DOCUMENT_REMOVED = 3;
    public static final int ACTIVE_DOCUMENT_CHANGED = 4;
    public static final int LOCKED_STATE_CHANGED = 5;
    public static final int DEFINITION_FACTORY_CHANGED = 6;
    public static final int TABLE_FACTORY_CHANGED = 7;
    int id;
    Map<String, Object> properties;
    ModelImpl source;

    public ModelEvent(ModelImpl source, int id) {
	this.source = source;
	this.id = id;
    }

    public int getId() {
	return this.id;
    }

    public Object get(String key) {
	return getProperties().get(key);
    }

    public Object put(String key, Object value) {
	return getProperties().put(key, value);
    }

    public Object remove(Object key) {
	return getProperties().remove(key);
    }

    private Map<String, Object> getProperties() {
	if (this.properties == null) {
	    this.properties = new HashMap(1);
	}

	return this.properties;
    }

    public ModelImpl getSource() {
	return this.source;
    }
}