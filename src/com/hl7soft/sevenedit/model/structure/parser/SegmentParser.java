package com.hl7soft.sevenedit.model.structure.parser;

import com.hl7soft.sevenedit.db.defs.FieldDefinition;
import com.hl7soft.sevenedit.db.defs.FieldEntry;
import com.hl7soft.sevenedit.db.defs.IDefinitionFactory;
import com.hl7soft.sevenedit.db.defs.SegmentDefinition;

public class SegmentParser {
    boolean includeEmpty = true;
    IDefinitionFactory definitionFactory;

    public Segment parseSegment(String data, Delimiters delimiters, String version) {
	return parseSegment(data, 0, data.length(), delimiters, version);
    }

    public Segment parseSegment(String data, int p0, int p1, Delimiters delimiters, String version) {
	if ((data == null) || (delimiters == null)) {
	    return null;
	}

	int sgmLen = p1 - p0;
	if (sgmLen < 3) {
	    return null;
	}

	String segmentName = data.subSequence(p0, p0 + 3).toString();
	SegmentDefinition segmentDefinition = this.definitionFactory != null ? this.definitionFactory.getSegmentDefinition(version, segmentName) : null;

	if ((sgmLen > 3) && (data.charAt(p0 + 3) != delimiters.getFieldDelimiter())) {
	    return null;
	}

	Segment segment = new Segment(segmentName);
	segment.setDefinition(segmentDefinition);

	boolean containsDelimiters = isDelimiterContainerSegment(segmentName);
	int[] idxs = Util.explode(data, delimiters.getFieldDelimiter(), p0, p1);
	idxs = removeSegmentName(idxs);
	int fieldEntriesCount = segmentDefinition != null ? segmentDefinition.getFieldEntriesCount() : 0;
	fieldEntriesCount = containsDelimiters ? fieldEntriesCount - 1 : fieldEntriesCount;

	int n = Math.max(fieldEntriesCount, idxs.length / 2);

	int idx0 = 0;
	int idx1 = 0;
	for (int i = 0; i < n; i++) {
	    FieldDefinition fieldDefinition = null;
	    FieldEntry fieldEntry = null;
	    if (segmentDefinition != null) {
		if (containsDelimiters)
		    fieldEntry = getFieldEntry(segmentDefinition, i + 1);
		else {
		    fieldEntry = getFieldEntry(segmentDefinition, i);
		}
		if (fieldEntry != null) {
		    fieldDefinition = segmentDefinition.getFactory().getFieldDefinition(segmentDefinition.getVersion(), fieldEntry.getName());
		}
	    }

	    Field fieldNode = null;

	    if (i < idxs.length / 2) {
		idx0 = idxs[(2 * i)];
		idx1 = idxs[(2 * i + 1)];

		if ((containsDelimiters) && (i == 0)) {
		    Field msh1 = new Field();
		    msh1.setValue("" + delimiters.getFieldDelimiter());
		    segment.addField(msh1);
		}

		int repIdx = Util.indexOf(data, delimiters.getRepeatDelimiter(), idx0);
		if ((repIdx != -1) && (repIdx <= idx1))
		    if (!(containsDelimiters & i == 0)) {
			fieldNode = new Field();
			fieldNode.setArray(true);
			fieldNode.setDefinition(fieldDefinition);
			fieldNode.setEntry(fieldEntry);

			int[] idxsRep = Util.explode(data, delimiters.getRepeatDelimiter(), idx0, idx1);
			for (int j = 0; j < idxsRep.length / 2; j++) {
			    Field arrayNode = parseField(data, idxsRep[(2 * j)], idxsRep[(2 * j + 1)], delimiters, fieldDefinition);
			    arrayNode.setDefinition(fieldDefinition);
			    arrayNode.setEntry(fieldEntry);
			    fieldNode.addField(arrayNode);
			}

			break;
		    }

		if ((containsDelimiters & ((i == 1) || (i == 0)))) {
		    fieldNode = new Field();
		    fieldNode.setValue(getData(data, idx0, idx1));
		} else {
		    fieldNode = parseField(data, idx0, idx1, delimiters, fieldDefinition);
		}
		fieldNode.setDefinition(fieldDefinition);
		fieldNode.setEntry(fieldEntry);
	    } else {
		label583: if (!this.includeEmpty) {
		    continue;
		}

		fieldNode = buildEmptyFieldNode(fieldDefinition);
		fieldNode.setDefinition(fieldDefinition);
		fieldNode.setEntry(fieldEntry);
	    }

	    segment.addField(fieldNode);
	}

	return segment;
    }

    private FieldEntry getFieldEntry(SegmentDefinition segmentDefinition, int i) {
	return i < segmentDefinition.getFieldEntriesCount() ? segmentDefinition.getFieldEntry(i) : null;
    }

    private int[] removeSegmentName(int[] idxs) {
	int[] tmp = new int[idxs.length - 2];
	System.arraycopy(idxs, 2, tmp, 0, tmp.length);
	return tmp;
    }

    private Field buildEmptyFieldNode(FieldDefinition fieldDefinition) {
	Field fieldNode = new Field();

	if (fieldDefinition != null) {
	    int i = 0;
	    for (int n = fieldDefinition.getFieldEntriesCount(); i < n; i++) {
		FieldEntry subFieldEntry = fieldDefinition.getFieldEntry(i);
		FieldDefinition subFieldDefinition = fieldDefinition.getFactory().getFieldDefinition(fieldDefinition.getVersion(), subFieldEntry.getName());
		Field subFieldNode = buildEmptyFieldNode(subFieldDefinition);
		subFieldNode.setEntry(subFieldEntry);
		subFieldNode.setDefinition(subFieldDefinition);

		fieldNode.addField(subFieldNode);
	    }
	}

	return fieldNode;
    }

    private Field parseField(String data, int p0, int p1, Delimiters delimiters, FieldDefinition fieldDefinition) {
	Field field = new Field();

	int[] idxs = Util.explode(data, delimiters.getComponentDelimiter(), p0, p1);

	if ((fieldDefinition == null) && (idxs.length / 2 == 1)) {
	    field.setValue(getData(data, p0, p1));
	    return field;
	}

	int entriesCount = fieldDefinition != null ? fieldDefinition.getFieldEntriesCount() : 0;
	int n = Math.max(entriesCount, idxs.length / 2);
	for (int k = 0; k < n; k++) {
	    Field subFieldNode = null;
	    FieldEntry subFieldEntry = null;
	    FieldDefinition subFieldDefinition = null;

	    if (k < entriesCount) {
		subFieldEntry = fieldDefinition != null ? fieldDefinition.getFieldEntry(k) : null;
		subFieldDefinition = fieldDefinition != null ? fieldDefinition.getFactory().getFieldDefinition(fieldDefinition.getVersion(), subFieldEntry.getName()) : null;
	    }

	    if (k < idxs.length / 2) {
		subFieldNode = parseComponent(data, idxs[(2 * k)], idxs[(2 * k + 1)], delimiters, subFieldDefinition);
	    } else {
		if (!this.includeEmpty) {
		    continue;
		}
		subFieldNode = buildEmptyFieldNode(subFieldDefinition);
	    }

	    subFieldNode.setDefinition(subFieldDefinition);
	    subFieldNode.setEntry(subFieldEntry);
	    field.addField(subFieldNode);
	}

	return field;
    }

    private Field parseComponent(String data, int p0, int p1, Delimiters delimiters, FieldDefinition fieldDefinition) {
	Field field = new Field();

	int[] idxs = Util.explode(data, delimiters.getSubcomponentDelimiter(), p0, p1);

	if ((fieldDefinition == null) && (idxs.length / 2 == 1)) {
	    field.setValue(getData(data, p0, p1));
	    return field;
	}

	int entriesCount = fieldDefinition != null ? fieldDefinition.getFieldEntriesCount() : 0;
	int n = Math.max(entriesCount, idxs.length / 2);
	for (int k = 0; k < n; k++) {
	    Field subFieldNode = null;
	    FieldEntry subFieldEntry = null;
	    FieldDefinition subFieldDefinition = null;

	    if (k < entriesCount) {
		subFieldEntry = fieldDefinition != null ? fieldDefinition.getFieldEntry(k) : null;
		subFieldDefinition = fieldDefinition != null ? fieldDefinition.getFactory().getFieldDefinition(fieldDefinition.getVersion(), subFieldEntry.getName()) : null;
	    }

	    if (k < idxs.length / 2) {
		subFieldNode = parseSubcomponent(data, idxs[(2 * k)], idxs[(2 * k + 1)], delimiters, subFieldDefinition);
	    } else {
		if (!this.includeEmpty) {
		    continue;
		}
		subFieldNode = buildEmptyFieldNode(subFieldDefinition);
	    }

	    subFieldNode.setDefinition(subFieldDefinition);
	    subFieldNode.setEntry(subFieldEntry);

	    field.addField(subFieldNode);
	}

	return field;
    }

    private Field parseSubcomponent(String data, int p0, int p1, Delimiters delimiters, FieldDefinition fieldDefinition) {
	Field field = new Field();
	field.setValue(getData(data, p0, p1));
	return field;
    }

    private String getData(String data, int p0, int p1) {
	return data.substring(p0, p1);
    }

    public boolean isIncludeEmpty() {
	return this.includeEmpty;
    }

    public void setIncludeEmpty(boolean includeEmpty) {
	this.includeEmpty = includeEmpty;
    }

    public IDefinitionFactory getDefinitionFactory() {
	return this.definitionFactory;
    }

    public void setDefinitionFactory(IDefinitionFactory definitionFactory) {
	this.definitionFactory = definitionFactory;
    }

    public boolean isDelimiterContainerSegment(String segmentName) {
	return ("MSH".equals(segmentName)) || ("BHS".equals(segmentName)) || ("FHS".equals(segmentName));
    }
}