package com.hl7soft.sevenedit.db.defs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

public class DefinitionFactory implements IDefinitionFactory {
    TreeMap<String, TreeMap<String, FieldDefinition>> fieldDefinitions;
    TreeMap<String, TreeMap<String, SegmentDefinition>> segmentDefinitions;
    TreeMap<String, TreeMap<String, MessageDefinition>> messageDefinitions;

    public void addFieldDefinition(FieldDefinition fieldDefinition) {
	if ((fieldDefinition == null) || (fieldDefinition.getName() == null) || (fieldDefinition.getVersion() == null)) {
	    return;
	}

	if (this.fieldDefinitions == null) {
	    this.fieldDefinitions = new TreeMap();
	}

	TreeMap map = (TreeMap) this.fieldDefinitions.get(fieldDefinition.getVersion());

	if (map == null) {
	    map = new TreeMap();
	    this.fieldDefinitions.put(fieldDefinition.getVersion(), map);
	}

	map.put(fieldDefinition.getName(), fieldDefinition);

	if ((fieldDefinition instanceof FieldDefinition))
	    fieldDefinition.setFactory(this);
    }

    public void removeFieldDefinition(String version, String name) {
	if (this.fieldDefinitions == null) {
	    return;
	}

	TreeMap map = (TreeMap) this.fieldDefinitions.get(version);

	if (map == null) {
	    return;
	}

	map.remove(name);
    }

    public FieldDefinition getFieldDefinition(String version, String name) {
	if ((this.fieldDefinitions == null) || (version == null) || (name == null)) {
	    return null;
	}

	name = name.toUpperCase();

	TreeMap map = (TreeMap) this.fieldDefinitions.get(version);

	if (map == null) {
	    return null;
	}

	return (FieldDefinition) map.get(name);
    }

    public void addSegmentDefinition(SegmentDefinition segmentDefinition) {
	if ((segmentDefinition == null) || (segmentDefinition.getName() == null) || (segmentDefinition.getVersion() == null)) {
	    return;
	}

	if (this.segmentDefinitions == null) {
	    this.segmentDefinitions = new TreeMap();
	}

	TreeMap map = (TreeMap) this.segmentDefinitions.get(segmentDefinition.getVersion());

	if (map == null) {
	    map = new TreeMap();
	    this.segmentDefinitions.put(segmentDefinition.getVersion(), map);
	}

	map.put(segmentDefinition.getName(), segmentDefinition);
	if ((segmentDefinition instanceof SegmentDefinition))
	    segmentDefinition.setFactory(this);
    }

    public void removeSegmentDefinition(String version, String name) {
	if (this.segmentDefinitions == null) {
	    return;
	}

	TreeMap map = (TreeMap) this.segmentDefinitions.get(version);

	if (map == null) {
	    return;
	}

	map.remove(name);
    }

    public SegmentDefinition getSegmentDefinition(String version, String name) {
	if ((this.segmentDefinitions == null) || (version == null) || (name == null)) {
	    return null;
	}

	name = name.toUpperCase();

	TreeMap map = (TreeMap) this.segmentDefinitions.get(version);

	if (map == null) {
	    return null;
	}

	return (SegmentDefinition) map.get(name);
    }

    public void addMessageDefinition(MessageDefinition messageDefinition) {
	if ((messageDefinition == null) || (messageDefinition.getName() == null) || (messageDefinition.getVersion() == null)) {
	    return;
	}

	if (this.messageDefinitions == null) {
	    this.messageDefinitions = new TreeMap();
	}

	TreeMap map = (TreeMap) this.messageDefinitions.get(messageDefinition.getVersion());

	if (map == null) {
	    map = new TreeMap();
	    this.messageDefinitions.put(messageDefinition.getVersion(), map);
	}

	map.put(messageDefinition.getName(), messageDefinition);
	if ((messageDefinition instanceof MessageDefinition))
	    messageDefinition.setFactory(this);
    }

    public void removeMessageDefinition(String version, String name) {
	if (this.messageDefinitions == null) {
	    return;
	}

	TreeMap map = (TreeMap) this.messageDefinitions.get(version);

	if (map == null) {
	    return;
	}

	map.remove(name);
    }

    public MessageDefinition getMessageDefinition(String version, String name) {
	if ((this.messageDefinitions == null) || (version == null) || (name == null)) {
	    return null;
	}

	name = name.toUpperCase();

	TreeMap map = (TreeMap) this.messageDefinitions.get(version);

	if (map == null) {
	    return null;
	}

	return (MessageDefinition) map.get(name);
    }

    public List<String> getVersions() {
	TreeSet versions = null;

	if (this.fieldDefinitions != null) {
	    if (versions == null) {
		versions = new TreeSet();
	    }

	    versions.addAll(this.fieldDefinitions.keySet());
	}

	if (this.segmentDefinitions != null) {
	    if (versions == null) {
		versions = new TreeSet();
	    }

	    versions.addAll(this.segmentDefinitions.keySet());
	}

	if (this.messageDefinitions != null) {
	    if (versions == null) {
		versions = new TreeSet();
	    }

	    versions.addAll(this.messageDefinitions.keySet());
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

    public List<String> getFields(String version) {
	if (this.fieldDefinitions == null) {
	    return null;
	}

	TreeMap map = (TreeMap) this.fieldDefinitions.get(version);

	if (map == null) {
	    return null;
	}

	return new ArrayList(map.keySet());
    }

    public List<String> getMessages(String version) {
	if (this.messageDefinitions == null) {
	    return null;
	}

	TreeMap map = (TreeMap) this.messageDefinitions.get(version);

	if (map == null) {
	    return null;
	}

	return new ArrayList(map.keySet());
    }

    public List<String> getSegments(String version) {
	if (this.segmentDefinitions == null) {
	    return null;
	}

	TreeMap map = (TreeMap) this.segmentDefinitions.get(version);

	if (map == null) {
	    return null;
	}

	return new ArrayList(map.keySet());
    }

    public void clear() {
	this.fieldDefinitions = null;
	this.segmentDefinitions = null;
	this.messageDefinitions = null;
    }

    public String getFieldDescription(String version, String name) {
	FieldDefinition def = getFieldDefinition(version, name);
	return def != null ? def.getDescription() : null;
    }

    public String getMessageDescription(String version, String name) {
	MessageDefinition def = getMessageDefinition(version, name);
	return def != null ? def.getDescription() : null;
    }

    public String getSegmentDescription(String version, String name) {
	SegmentDefinition def = getSegmentDefinition(version, name);
	return def != null ? def.getDescription() : null;
    }
}