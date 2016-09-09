package com.hl7soft.sevenedit.model.structure.dparser.message;

import com.hl7soft.sevenedit.db.defs.FieldDefinition;
import com.hl7soft.sevenedit.db.defs.FieldEntry;
import com.hl7soft.sevenedit.db.defs.IDefinitionFactory;
import com.hl7soft.sevenedit.db.defs.ISegmentEntry;
import com.hl7soft.sevenedit.db.defs.ISegmentEntryContainer;
import com.hl7soft.sevenedit.db.defs.MessageDefinition;
import com.hl7soft.sevenedit.db.defs.SegmentDefinition;
import com.hl7soft.sevenedit.model.data.DataRange;
import com.hl7soft.sevenedit.model.data.IData;
import com.hl7soft.sevenedit.model.structure.dparser.segment2.ISSxField;
import com.hl7soft.sevenedit.model.structure.dparser.segment2.ISSxSegment;
import com.hl7soft.sevenedit.model.structure.dparser.segment2.ISSxStructure;
import com.hl7soft.sevenedit.model.structure.dparser.segment2.SSxMessageHeaderSegment;
import com.hl7soft.sevenedit.model.structure.dparser.segment2.SSxSegment;
import com.hl7soft.sevenedit.model.structure.dparser.segment2.SSxStructure;
import com.hl7soft.sevenedit.model.structure.parser.Delimiters;
import java.util.ArrayList;
import java.util.List;

public class MSxStructure implements IMSxStructure {
    IData data;
    SSxStructure segmentStructure;
    MSxMessage message;
    IDefinitionFactory definitionFactory;
    DataRange range;
    boolean includeEmpty = true;
    String forcedVersion;
    String defaultVersion;

    public MSxStructure(IData data) {
	this(data, null);
    }

    public MSxStructure(IData data, IDefinitionFactory definitionFactory) {
	this.data = data;
	this.definitionFactory = definitionFactory;
	this.range = new DataRange(0, data.getLength() - 1);
    }

    public IData getData() {
	return this.data;
    }

    public IDefinitionFactory getDefinitionFactory() {
	return this.definitionFactory;
    }

    public ISSxStructure getSegmentStructure() {
	return this.segmentStructure;
    }

    public int getEndOffset() {
	return this.segmentStructure.getEndOffset();
    }

    public int getLength() {
	return this.segmentStructure.getLength();
    }

    public int getStartOffset() {
	return this.segmentStructure.getStartOffset();
    }

    public void parse() {
	this.segmentStructure = new SSxStructure(this.data, this.definitionFactory);
	this.segmentStructure.setIncludeEmpty(this.includeEmpty);
	this.segmentStructure.setForcedVersion(this.forcedVersion);
	this.segmentStructure.setDefaultVersion(this.defaultVersion);
	this.segmentStructure.parse();

	this.message = createMessage();

	parseFully();
    }

    private void parseFully() {
	if (this.message == null) {
	    return;
	}

	parseMessage(this.message);
	parseChildrenFully(this.message);
    }

    private void parseChildrenFully(IMSxSegmentContainer container) {
	int i = 0;
	for (int n = container.getSegmentsCount(); i < n; i++) {
	    MSxSegment segment = (MSxSegment) container.getSegment(i);
	    parseSegment(segment);
	    if (segment.getSegmentsCount() > 0)
		parseChildrenFully(((IMSxSegmentContainer) (segment)));
	    else
		parseChildrenFully(((IMSxFieldContainer) (segment)));
	}

    }

    private void parseChildrenFully(IMSxFieldContainer container) {
	int i = 0;
	for (int n = container.getFieldsCount(); i < n; i++) {
	    MSxField field = (MSxField) container.getField(i);
	    parseField(field);
	    parseChildrenFully(((IMSxFieldContainer) (field)));
	}

    }

    private void parseMessage(MSxMessage message) {
	MSxStructure structure = message.getStructure();
	ISSxStructure segmentStructure = structure.getSegmentStructure();

	SSxSegment msh = findMSH();
	int sgmIdx0 = 0;
	if (msh != null) {
	    sgmIdx0 = segmentStructure.getSegmentIndex(msh);
	}

	if (message.getDefinition() == null) {
	    ArrayList segments = new ArrayList();
	    int i = sgmIdx0;
	    for (int n = segmentStructure.getSegmentsCount(); i < n; i++) {
		segments.add(segmentStructure.getSegment(i));
	    }

	    List remainingSegments = prepareSegments(segments);
	    i = 0;
	    for (int n = remainingSegments.size(); i < n; i++) {
		message.addSegment((MSxSegment) remainingSegments.get(i));
	    }

	    return;
	}

	SegmentIterator segmentIterator = new SegmentIterator(segmentStructure, sgmIdx0, segmentStructure.getSegmentsCount());
	List segments = collectSegments(message, segmentIterator, message.getDefinition(), false);

	if (segments != null) {
	    int i = 0;
	    for (int n = segments.size(); i < n; i++) {
		MSxSegment s = (MSxSegment) segments.get(i);

		if ((s.isReal()) || (structure.isIncludeEmpty())) {
		    message.addSegment(s);
		}

	    }

	}

	List remainingSegments = prepareSegments(convertToList(segmentIterator));
	int i = 0;
	for (int n = remainingSegments.size(); i < n; i++)
	    message.addSegment((MSxSegment) remainingSegments.get(i));
    }

    private List<ISSxSegment> convertToList(SegmentIterator iterator) {
	ArrayList res = new ArrayList();
	while (iterator.hasNext()) {
	    res.add(iterator.getSegment());
	    iterator.next();
	}
	return res;
    }

    private List<MSxSegment> prepareSegments(List<ISSxSegment> list) {
	List segments = new ArrayList(list.size());

	int i = 0;
	for (int n = list.size(); i < n; i++) {
	    ISSxSegment segment = (ISSxSegment) list.get(i);
	    if ((segment != null) && (!isEmptySegment(segment))) {
		segments.add(new MSxSegment(getStructure(), this.message, segment.getName(), null, getSegmentDefinition(segment.getName(), this.message.getVersion()), segment));
	    }

	}

	segments = groupToArrays(segments);

	return segments;
    }

    private List<MSxSegment> groupToArrays(List<MSxSegment> segments) {
	List res = new ArrayList();

	int i = 0;
	for (int n = segments.size(); i < n; i++) {
	    MSxSegment segment = (MSxSegment) segments.get(i);

	    if (i + 1 < n) {
		if (compareNames(((MSxSegment) segments.get(i + 1)).getName(), segment.getName())) {
		    int j = i + 1;
		    while ((j < n) && (compareNames(((MSxSegment) segments.get(j)).getName(), segment.getName()))) {
			j++;
		    }

		    MSxSegment segmentArray = createSegmentArray(this.message, null, segment.getName());
		    for (int k = i; k < j; k++) {
			segmentArray.addSegment((MSxSegment) segments.get(k));
		    }
		    res.add(segmentArray);
		    i = j - 1;
		    continue;
		}
	    }

	    res.add(segment);
	}

	return res;
    }

    private boolean compareNames(String n1, String n2) {
	if (n1 == null) {
	    return false;
	}

	return n1.equals(n2);
    }

    private boolean isEmptySegment(ISSxSegment s) {
	return (s.getDataRange() == null) || (s.getDataRange().getLength() == 0);
    }

    private List<MSxSegment> collectSegments(MSxMessage message, SegmentIterator segmentIterator, ISegmentEntryContainer segmentEntryContainer, boolean insideGroup) {
	List segments = new ArrayList(0);

	int i = 0;
	for (int n = segmentEntryContainer.getSegmentEntriesCount(); i < n; i++) {
	    ISegmentEntry segmentEntry = segmentEntryContainer.getSegmentEntry(i);
	    MSxSegment segment = null;

	    if (segmentEntry.getType() == 1) {
		segment = collectSingleSegment(message, segmentIterator, segmentEntry, insideGroup);
	    } else if (segmentEntry.getType() == 2) {
		segment = collectGroupSegment(message, segmentIterator, segmentEntry, insideGroup);

		if (segment == null) {
		    segment = createEmptySegmentGroup(message, segmentEntry);
		}
	    } else if (segmentEntry.getType() == 3) {
		segment = collectListedSegment(message, segmentIterator, segmentEntry);
	    }

	    if (segment == null) {
		segment = new MSxSegment(this, message, segmentEntry.getName(), segmentEntry, getSegmentDefinition(segmentEntry.getName(), message.getVersion()), null);
	    }

	    segments.add(segment);
	}

	return segments;
    }

    private MSxSegment createEmptySegmentGroup(MSxMessage message, ISegmentEntry segmentGroupEntry) {
	MSxSegment segment = createSegmentGroup(message, segmentGroupEntry);

	int i = 0;
	for (int n = segmentGroupEntry.getSegmentEntriesCount(); i < n; i++) {
	    ISegmentEntry segmentEntry = segmentGroupEntry.getSegmentEntry(i);

	    if (segmentEntry.getType() == 2)
		segment.addSegment(createEmptySegmentGroup(message, segmentEntry));
	    else {
		segment.addSegment(new MSxSegment(this, message, segmentEntry.getName(), segmentEntry, getSegmentDefinition(segmentEntry.getName(), message.getVersion()), null));
	    }
	}

	return segment;
    }

    private MSxSegment collectGroupSegment(MSxMessage message, SegmentIterator segmentIterator, ISegmentEntry segmentGroupEntry, boolean insideGroup) {
	ArrayList groups = new ArrayList(2);
	boolean end = false;
	while (!end) {
	    List groupSegments = collectSegments(message, segmentIterator, segmentGroupEntry, true);

	    if (!containsNonEmptySegment(groupSegments)) {
		break;
	    }
	    MSxSegment segmentGroup = createSegmentGroup(message, segmentGroupEntry);
	    int i = 0;
	    for (int n = groupSegments.size(); i < n; i++) {
		segmentGroup.addSegment((MSxSegment) groupSegments.get(i));
	    }
	    groups.add(segmentGroup);
	}

	if (groups.size() > 1) {
	    MSxSegment segmentGroupArray = createSegmentGroupArray(message, segmentGroupEntry);
	    for (int j = 0; j < groups.size(); j++) {
		segmentGroupArray.addSegment((MSxSegment) groups.get(j));
	    }
	    return segmentGroupArray;
	}
	if (groups.size() == 1) {
	    return (MSxSegment) groups.get(0);
	}

	return null;
    }

    private MSxSegment createSegmentGroup(MSxMessage message, ISegmentEntry segmentGroupEntry) {
	MSxSegment segmentGroup = new MSxSegment(this, message, segmentGroupEntry.getName(), segmentGroupEntry, null, null);
	segmentGroup.setGroup(true);
	return segmentGroup;
    }

    private MSxSegment createSegmentGroupArray(MSxMessage message, ISegmentEntry segmentGroupEntry) {
	MSxSegment segmentGroupArray = new MSxSegment(this, message, segmentGroupEntry.getName(), segmentGroupEntry, null, null);
	segmentGroupArray.setArray(true);
	return segmentGroupArray;
    }

    private MSxSegment collectListedSegment(MSxMessage message, SegmentIterator segmentIterator, ISegmentEntry segmentListEntry) {
	ISSxSegment segment = segmentIterator.getSegment();

	if (segment == null) {
	    return null;
	}

	int i = 0;
	for (int n = segmentListEntry.getSegmentEntriesCount(); i < n; i++) {
	    ISegmentEntry se = segmentListEntry.getSegmentEntry(i);
	    if ((segment.getName() != null) && (segment.getName().equals(se.getName()))) {
		segmentIterator.next();
		MSxSegment sgm = createSegment(message, segmentListEntry, segment);
		return sgm;
	    }
	}

	return null;
    }

    private MSxSegment createSegmentArray(MSxMessage message, ISegmentEntry segmentEntry, String segmentName) {
	MSxSegment segmentArray = new MSxSegment(this, message, segmentName, segmentEntry, getSegmentDefinition(segmentName, message.getVersion()), null);
	segmentArray.setArray(true);
	return segmentArray;
    }

    private MSxSegment createSegment(MSxMessage message, ISegmentEntry segmentEntry, ISSxSegment segment) {
	return new MSxSegment(this, message, segment.getName(), segmentEntry, getSegmentDefinition(segment.getName(), message.getVersion()), segment);
    }

    private boolean isExpectedSegment(String expectedSegmentName, ISSxSegment segment) {
	return (segment.getName() != null) && (segment.getName().equals(expectedSegmentName));
    }

    private boolean containsNonEmptySegment(List<MSxSegment> elements) {
	if (elements == null) {
	    return false;
	}

	int n = elements.size();
	for (int i = 0; i < n; i++) {
	    IMSxSegment segment = (IMSxSegment) elements.get(i);
	    if (segment.getDataRange() != null) {
		return true;
	    }
	}
	return false;
    }

    private MSxSegment collectSingleSegment(MSxMessage message, SegmentIterator segmentIterator, ISegmentEntry segmentEntry, boolean insideGroup) {
	ArrayList collectedSegments = collectSegmentsToList(message, segmentIterator, segmentEntry, insideGroup);

	if (collectedSegments.size() == 1) {
	    return (MSxSegment) collectedSegments.get(0);
	}
	if (collectedSegments.size() > 1) {
	    MSxSegment segmentArray = createSegmentArray(message, segmentEntry, segmentEntry.getName());
	    addSegmentsToArray(collectedSegments, segmentArray);
	    return segmentArray;
	}

	return null;
    }

    private void addSegmentsToArray(ArrayList<MSxSegment> segments, MSxSegment segmentArray) {
	int i = 0;
	for (int n = segments.size(); i < n; i++)
	    segmentArray.addSegment((MSxSegment) segments.get(i));
    }

    private ArrayList<MSxSegment> collectSegmentsToList(MSxMessage message, SegmentIterator segmentIterator, ISegmentEntry segmentEntry, boolean insideGroup) {
	String expectedSegmentName = segmentEntry.getName();
	ArrayList collectedSegments = new ArrayList(2);

	boolean end = false;
	while (!end) {
	    ISSxSegment segment = segmentIterator.getSegment();

	    if (segment == null) {
		break;
	    }
	    if (isEmptySegment(segment)) {
		segmentIterator.next();
	    } else {
		if (!isExpectedSegment(expectedSegmentName, segment))
		    break;
		collectedSegments.add(createSegment(message, segmentEntry, segment));

		if ((insideGroup) && (segmentEntry.getParentContainer().getSegmentEntryIndex(segmentEntry) == 0)) {
		    segmentIterator.next();
		    break;
		}

		segmentIterator.next();
	    }
	}
	return collectedSegments;
    }

    private void parseSegment(MSxSegment segment) {
	if ((segment.isArray()) || (segment.isGroup())) {
	    return;
	}

	ISSxSegment sourceSegment = segment.sourceSegment;

	if (sourceSegment != null) {
	    int i = 0;
	    for (int n = sourceSegment.getFieldsCount(); i < n; i++) {
		ISSxField f = sourceSegment.getField(i);
		MSxField field = new MSxField(this, segment, segment, f.getEntry(), f.getDefinition(), f.getLevel(), f);
		segment.addField(field);
	    }

	} else {
	    IDefinitionFactory factory = getDefinitionFactory();
	    SegmentDefinition definition = segment.getDefinition();
	    if (definition != null) {
		int i = 0;
		for (int n = definition.getFieldEntriesCount(); i < n; i++) {
		    FieldEntry fieldEntry = definition.getFieldEntry(i);
		    FieldDefinition fieldDefinition = factory != null ? factory.getFieldDefinition(segment.getVersion(), fieldEntry.getName()) : null;
		    MSxField field = new MSxField(this, segment, segment, fieldEntry, fieldDefinition, 0, null);
		    segment.addField(field);
		}
	    }
	}
    }

    public void parseField(MSxField field) {
	ISSxField sourceField = field.sourceField;
	if (sourceField != null) {
	    int i = 0;
	    for (int n = sourceField.getFieldsCount(); i < n; i++) {
		ISSxField f = sourceField.getField(i);
		field.addField(new MSxField(this, field.getParentSegment(), field, f.getEntry(), f.getDefinition(), f.getLevel(), f));
	    }

	} else {
	    IDefinitionFactory factory = getDefinitionFactory();
	    FieldDefinition definition = field.getDefinition();
	    if (definition != null) {
		int i = 0;
		for (int n = definition.getFieldEntriesCount(); i < n; i++) {
		    FieldEntry fieldEntry = definition.getFieldEntry(i);
		    FieldDefinition fieldDefinition = factory != null ? factory.getFieldDefinition(field.getParentSegment().getVersion(), fieldEntry.getName()) : null;
		    field.addField(new MSxField(getStructure(), field.getParentSegment(), field, fieldEntry, fieldDefinition, field.level + 1, null));
		}
	    }
	}
    }

    private SegmentDefinition getSegmentDefinition(String name, String version) {
	IDefinitionFactory factory = getDefinitionFactory();
	return factory != null ? factory.getSegmentDefinition(version, name) : null;
    }

    private MSxMessage createMessage() {
	String messageName = null;
	String messageVersion = null;
	Delimiters delimiters = null;
	MessageDefinition messageDefinition = null;

	int p0 = 0;
	SSxMessageHeaderSegment msh = findMSH();
	if (msh != null) {
	    messageName = msh.getMessageName();
	    messageVersion = msh.getVersion();
	    delimiters = msh.getDelimiters();
	    messageDefinition = getMessageDefinition(msh);
	    p0 = msh.getDataRange().getP0();
	}

	if (messageVersion == null) {
	    messageVersion = this.defaultVersion;
	}
	if (this.forcedVersion != null) {
	    messageVersion = this.forcedVersion;
	}

	return new MSxMessage(this, messageName, messageVersion, delimiters, messageDefinition, new DataRange(p0, getData().getLength() - 1));
    }

    private SSxMessageHeaderSegment findMSH() {
	if (this.segmentStructure.getSegmentsCount() == 0) {
	    return null;
	}

	int i = 0;
	for (int n = this.segmentStructure.getSegmentsCount(); i < n; i++) {
	    SSxSegment segment = this.segmentStructure.getSegment(i);
	    if ((segment instanceof SSxMessageHeaderSegment)) {
		return (SSxMessageHeaderSegment) segment;
	    }
	}

	return null;
    }

    private MessageDefinition getMessageDefinition(SSxMessageHeaderSegment msh) {
	if (getDefinitionFactory() == null) {
	    return null;
	}

	return getDefinitionFactory().getMessageDefinition(msh.getVersion(), msh.getMessageName());
    }

    public IMSxElement getChildAt(int idx) {
	return this.message;
    }

    public int getChildrenCount() {
	return 1;
    }

    public int getChildIndex(IMSxElement node) {
	return 0;
    }

    public int getChildIndexByOffset(int offs) {
	return 0;
    }

    public IMSxElement getParentElement() {
	return null;
    }

    public IMSxStructure getStructure() {
	return null;
    }

    public void addChildren(int idx, IMSxElement[] children) {
    }

    public IMSxElement[] getChildren(int idx, int len) {
	return null;
    }

    public void removeChildren(int idx, int len) {
    }

    public DataRange getDataRange() {
	return this.range;
    }

    public boolean isReal() {
	return true;
    }

    public int countChildren() {
	return 0;
    }

    public String toString() {
	return "MessageStructure";
    }

    public boolean isIncludeEmpty() {
	return this.includeEmpty;
    }

    public void setIncludeEmpty(boolean includeEmpty) {
	this.includeEmpty = includeEmpty;
    }

    public String getForcedVersion() {
	return this.forcedVersion;
    }

    public void setForcedVersion(String forcedVersion) {
	this.forcedVersion = forcedVersion;
    }

    public String getDefaultVersion() {
	return this.defaultVersion;
    }

    public void setDefaultVersion(String defaultVersion) {
	this.defaultVersion = defaultVersion;
    }

    public void setDefinitionFactory(IDefinitionFactory definitionFactory) {
	this.definitionFactory = definitionFactory;
    }

    public MSxMessage getMessage() {
	return this.message;
    }

    private class SegmentIterator {
	ISSxStructure source;
	int startSegmentIndex;
	int endSegmentIndex;
	int idx = 0;

	public SegmentIterator(ISSxStructure source, int startSegmentIndex, int endSegmentIndex) {
	    this.source = source;
	    this.startSegmentIndex = startSegmentIndex;
	    this.endSegmentIndex = endSegmentIndex;
	    reset();
	}

	public void reset() {
	    this.idx = this.startSegmentIndex;
	}

	public void next() {
	    if (this.idx < this.endSegmentIndex)
		this.idx += 1;
	}

	public boolean hasNext() {
	    return this.idx < this.endSegmentIndex;
	}

	public ISSxSegment getSegment() {
	    if (this.idx < this.endSegmentIndex) {
		return this.source.getSegment(this.idx);
	    }
	    return null;
	}
    }
}