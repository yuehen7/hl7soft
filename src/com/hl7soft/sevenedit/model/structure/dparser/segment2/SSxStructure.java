package com.hl7soft.sevenedit.model.structure.dparser.segment2;

import com.hl7soft.sevenedit.db.defs.FieldDefinition;
import com.hl7soft.sevenedit.db.defs.FieldEntry;
import com.hl7soft.sevenedit.db.defs.IDefinitionFactory;
import com.hl7soft.sevenedit.db.defs.SegmentDefinition;
import com.hl7soft.sevenedit.model.data.DataChunk;
import com.hl7soft.sevenedit.model.data.DataRange;
import com.hl7soft.sevenedit.model.data.IData;
import com.hl7soft.sevenedit.model.structure.parser.Delimiters;
import com.hl7soft.sevenedit.model.util.DataHelper;
import com.hl7soft.sevenedit.model.util.StringHelper;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SSxStructure implements ISSxStructure {
	IData data;
	List<ISSxSegment> segments;
	boolean parsed;
	IDefinitionFactory definitionFactory;
	boolean includeEmpty = true;
	DataRange range;
	int countChildren;
	String highestVersion;
	Delimiters defaultDelimiters = new Delimiters();
	String forcedVersion;
	String defaultVersion;

	public SSxStructure(IData data) {
		this(data, null);
	}

	public SSxStructure(IData data, IDefinitionFactory definitionFactory) {
		this.data = data;
		this.definitionFactory = definitionFactory;
		this.range = new DataRange(0, data.getLength() - 1);
	}

	public IData getData() {
		return this.data;
	}

	public void addSegment(int idx, SSxSegment segment) {
		if (this.segments == null) {
			this.segments = new ArrayList(1);
		}
		this.segments.add(idx, segment);
	}

	public void setSegment(int idx, SSxSegment segment) {
		if (this.segments == null) {
			this.segments = new ArrayList(1);
		}
		this.segments.set(idx, segment);
	}

	public void addSegment(SSxSegment segment) {
		if (this.segments == null) {
			this.segments = new ArrayList(1);
		}
		this.segments.add(segment);
	}

	public void removeSegment(SSxSegment segment) {
		if ((this.segments == null) || (segment == null)) {
			return;
		}
		this.segments.remove(segment);
	}

	public void removeSegment(int idx) {
		removeSegment(getSegment(idx));
	}

	public SSxSegment getSegment(int idx) {
		parseIfNecessary();

		if ((idx < 0) || (idx >= getSegmentsCount())) {
			return null;
		}

		return (SSxSegment) this.segments.get(idx);
	}

	private void parseIfNecessary() {
		if (!isParsed())
			;
	}

	public SSxSegment getSegmentByOffset(int offs) {
		int idx = getSegmentIndexByOffset(offs);
		return idx >= 0 ? getSegment(idx) : null;
	}

	public int getSegmentIndexByOffset(int offs) {
		if (this.segments == null) {
			return -1;
		}

		int i = 0;
		for (int n = this.segments.size(); i < n; i++) {
			ISSxSegment segment = (ISSxSegment) this.segments.get(i);

			if ((segment.getDataRange().getStartOffset() <= offs) && (offs <= segment.getDataRange().getEndOffset())) {
				return i;
			}
		}

		return -1;
	}

	public int getNextSegmentIndexByOffset(int offs) {
		if (this.segments == null) {
			return -1;
		}

		int res = -1;

		int prevOffs = 0;
		int i = 0;
		for (int n = this.segments.size(); i < n; i++) {
			ISSxSegment segment = (ISSxSegment) this.segments.get(i);
			if ((prevOffs < offs) && (offs <= segment.getDataRange().getStartOffset())) {
				break;
			}
			res = i;
			prevOffs = segment.getDataRange().getEndOffset();
		}

		return res + 1;
	}

	public int getSegmentIndex(ISSxSegment segment) {
		parseIfNecessary();

		if (this.segments == null) {
			return -1;
		}
		return this.segments.indexOf(segment);
	}

	public int getSegmentsCount() {
		parseIfNecessary();

		return this.segments != null ? this.segments.size() : 0;
	}

	public ISSxStructureElement getChildAt(int idx) {
		return getSegment(idx);
	}

	public int getChildrenCount() {
		return getSegmentsCount();
	}

	public int getChildIndex(ISSxStructureElement node) {
		if (this.segments == null) {
			return -1;
		}
		return this.segments.indexOf(node);
	}

	public ISSxStructureElement getParentElement() {
		return null;
	}

	public SSxStructure getParentStructure() {
		return null;
	}

	public boolean isParsed() {
		return this.parsed;
	}

	public void parse() {
		if (isParsed()) {
			return;
		}

		doParse();
		parseFully();
		this.parsed = true;
	}

	private void doParse() {
		this.highestVersion = getHighestVersion();
		List segmentBounds = SSxUtil.scanSegmentBounds(this.data.getDataChunk(0, this.data.getLength(), null));
		Delimiters currentDelimiters = this.defaultDelimiters;
		String currentVersion = this.highestVersion;
		int i = 0;
		for (int n = segmentBounds.size(); i < n; i++) {
			DataRange r = (DataRange) segmentBounds.get(i);

			int p0 = r.getStartOffset();
			int p1 = r.getEndOffset();
			DataChunk chunk = this.data.getDataChunk(0, this.data.getLength(), null);
			String segmentName = parseSegmentName(chunk, p0, p1);
			SSxSegment segment = null;
			if ("MSH".equals(segmentName)) {
				DataChunk sgmChunk = this.data.getDataChunk(p0, p1 - p0, null);
				currentDelimiters = parseDelimiters(sgmChunk);
				MsgInfo msgInfo = parseMessageName(sgmChunk, currentDelimiters);
				currentVersion = parseMessageVersion(sgmChunk, currentDelimiters);

				if ((isUnknownVersion(currentVersion)) && (this.defaultVersion != null)) {
					currentVersion = this.defaultVersion;
				}
				if (this.forcedVersion != null) {
					currentVersion = this.forcedVersion;
				}

				segment = new SSxMessageHeaderSegment(this, msgInfo != null ? msgInfo.messageName : null,
						currentVersion, new DataRange(p0, p1), currentDelimiters,
						getSegmentDefinition(currentVersion, segmentName));
			} else {
				segment = new SSxSegment(this, segmentName, currentVersion, new DataRange(p0, p1), currentDelimiters,
						getSegmentDefinition(currentVersion, segmentName));
				if (!segment.isValid) {
					segment.setName(null);
				}
			}
			addSegment(segment);
		}
	}

	private void parseFully() {
		if (this.segments == null) {
			return;
		}

		int i = 0;
		for (int n = this.segments.size(); i < n; i++) {
			SSxSegment segment = (SSxSegment) this.segments.get(i);
			parseSegment(segment);
			parseChildrenFully(segment);
		}
	}

	private void parseChildrenFully(ISSxFieldContainer container) {
		int i = 0;
		for (int n = container.getFieldsCount(); i < n; i++) {
			SSxField field = (SSxField) container.getField(i);
			parseField(field);
			parseChildrenFully(field);
		}
	}

	private void parseSegment(SSxSegment segment) {
		DataChunk data = getData().getDataChunk(0, segment.getData().getLength(), null);
		Delimiters delimiters = segment.getDelimiters();
		SegmentDefinition segmentDefinition = segment.getDefinition();
		String segmentName = segment.getName();

		if ((segmentName == null) || (!segment.isValidSegmentFormat()) || (data == null) || (delimiters == null)
				|| (segment.getDataRange().getLength() < 3)) {
			return;
		}

		boolean containsDelimiters = DataHelper.isDelimiterContainerSegment(segmentName);
		int[] idxs = StringHelper.explode(data, delimiters.getFieldDelimiter(), segment.getDataRange().getStartOffset(),
				segment.getDataRange().getEndOffset());
		int dataFieldsCount = idxs.length / 2 - (containsDelimiters ? 0 : 1);
		int modelFieldsCount = segmentDefinition != null ? segmentDefinition.getFieldEntriesCount() : 0;
		int childrenCount = Math.max(modelFieldsCount, dataFieldsCount);

		int idx0 = 0;
		int idx1 = 0;
		for (int k = 0; k < childrenCount; k++) {
			FieldDefinition fieldDefinition = null;
			FieldEntry fieldEntry = null;
			if (k < modelFieldsCount) {
				if ((k == 4) && ("OBX".equals(segment.getName())) && (idxs.length / 2 > 2)) {
					DataRange rangeObx2 = new DataRange(idxs[4], idxs[5]);
					DataChunk chunkObx2 = getData().getDataChunk(rangeObx2.getP0(), rangeObx2.getLength(), null);
					String obx5Type = chunkObx2.toString();

					if ((obx5Type == null) || (obx5Type.isEmpty())) {
						obx5Type = "ST";
					}

					if (segmentDefinition != null) {
						fieldEntry = segmentDefinition.getFieldEntry(k);
						if (fieldEntry != null) {
							fieldEntry.setName(obx5Type);
						}
					}
				}

				fieldEntry = segmentDefinition != null ? segmentDefinition.getFieldEntry(k) : null;
				fieldDefinition = this.definitionFactory != null ? this.definitionFactory
						.getFieldDefinition(segmentDefinition.getVersion(), fieldEntry.getName()) : null;
			}

			SSxField fieldNode = null;

			if (k < dataFieldsCount) {
				if (containsDelimiters) {
					if (k == 0) {
						idx0 = idxs[(2 * k + 1)];
						idx1 = idxs[(2 * k + 1)] + 1;
					} else {
						idx0 = idxs[(2 * k)];
						idx1 = idxs[(2 * k + 1)];
					}
				} else {
					idx0 = idxs[(2 * (k + 1))];
					idx1 = idxs[(2 * (k + 1) + 1)];
				}

				fieldNode = new SSxField(this, segment, k, 0, fieldEntry, fieldDefinition, new DataRange(idx0, idx1));
			} else if (isIncludeEmpty()) {
				fieldNode = new SSxField(this, segment, k, 0, fieldEntry, fieldDefinition);
			}

			if (fieldNode != null)
				segment.addField(k, fieldNode);
		}
	}

	private void parseField(SSxField field) {
		DataChunk data = getData().getDataChunk(0, getData().getLength(), null);
		ISSxSegment parentSegment = field.getParentSegment();
		FieldDefinition fieldDefinition = field.getDefinition();
		Delimiters delimiters = parentSegment.getDelimiters();

		if (delimiters == null) {
			return;
		}

		if (field.isArray()) {
			int[] idxsRep = StringHelper.explode(data, delimiters.getRepeatDelimiter(),
					field.getDataRange().getStartOffset(), field.getDataRange().getEndOffset());
			for (int k = 0; k < idxsRep.length / 2; k++) {
				SSxField arrayNode = new SSxField(this, field, k, field.getLevel(), field.getEntry(),
						field.getDefinition(), new DataRange(idxsRep[(2 * k)], idxsRep[(2 * k + 1)]));
				field.addField(k, arrayNode);
			}
		} else if (!field.isPrimitive()) {
			char delimiter = delimiters.getComponentDelimiter();
			if (field.getLevel() == 0)
				delimiter = delimiters.getComponentDelimiter();
			else if (field.getLevel() == 1)
				delimiter = delimiters.getSubcomponentDelimiter();
			else {
				return;
			}

			int[] idxs = field.getDataRange() != null ? StringHelper.explode(data, delimiter,
					field.getDataRange().getStartOffset(), field.getDataRange().getEndOffset()) : new int[0];

			if ((fieldDefinition == null) && (idxs.length / 2 == 1)) {
				return;
			}

			int dataFieldsCount = idxs.length / 2;
			int modelFieldsCount = fieldDefinition != null ? fieldDefinition.getFieldEntriesCount() : 0;
			int fieldsCount = Math.max(modelFieldsCount, dataFieldsCount);

			for (int k = 0; k < fieldsCount; k++) {
				SSxField subFieldNode = null;
				FieldEntry subFieldEntry = null;
				FieldDefinition subFieldDefinition = null;

				if (k < modelFieldsCount) {
					subFieldEntry = fieldDefinition != null ? fieldDefinition.getFieldEntry(k) : null;
					subFieldDefinition = fieldDefinition != null ? fieldDefinition.getFactory()
							.getFieldDefinition(fieldDefinition.getVersion(), subFieldEntry.getName()) : null;
				}

				if (k < dataFieldsCount) {
					subFieldNode = new SSxField(this, field, k, field.getLevel() + 1, subFieldEntry, subFieldDefinition,
							new DataRange(idxs[(2 * k)], idxs[(2 * k + 1)]));
				} else if (isIncludeEmpty()) {
					subFieldNode = new SSxField(this, field, k, field.getLevel() + 1, subFieldEntry,
							subFieldDefinition);
				}

				if (subFieldNode != null)
					field.addField(k, subFieldNode);
			}
		}
	}

	private boolean isUnknownVersion(String version) {
		List versions = this.definitionFactory != null ? this.definitionFactory.getVersions() : null;
		return (versions != null) && (!versions.contains(version));
	}

	private SegmentDefinition getSegmentDefinition(String version, String segmentName) {
		if (this.definitionFactory == null) {
			return null;
		}

		return this.definitionFactory.getSegmentDefinition(version, segmentName);
	}

	private String getHighestVersion() {
		if (this.definitionFactory == null) {
			return null;
		}

		List versions = this.definitionFactory.getVersions();
		if ((versions == null) || (versions.size() == 0)) {
			return null;
		}

		versions = new ArrayList(versions);
		Collections.sort(versions);

		return (String) versions.get(versions.size() - 1);
	}

	public void unparse() {
		ISSxStructureElement[] removedElems = new ISSxStructureElement[getSegmentsCount()];
		for (int i = 0; i < removedElems.length; i++) {
			removedElems[i] = getSegment(i);
		}

		this.segments = null;
		this.parsed = false;
	}

	private String parseSegmentName(DataChunk chunk, int p0, int p1) {
		if (p1 - p0 < 3) {
			return null;
		}

		if (p0 + 3 < chunk.length()) {
			String temp = chunk.toString();
			// return temp.substring(p0, p0 + 3);
			return chunk.subSequence(p0, p0 + 3).toString();
		}

		return null;
	}

	private Delimiters parseDelimiters(DataChunk chunk) {
		Delimiters d = new Delimiters();
		if (chunk.length() < 3) {
			return d;
		}

		if (chunk.length() >= 4) {
			d.setFieldDelimiter(chunk.charAt(3));
		}
		if (chunk.length() >= 5) {
			d.setComponentDelimiter(chunk.charAt(4));
		}
		if (chunk.length() >= 6) {
			d.setRepeatDelimiter(chunk.charAt(5));
		}
		if (chunk.length() >= 7) {
			d.setEscapeDelimiter(chunk.charAt(6));
		}
		if (chunk.length() >= 8) {
			d.setSubcomponentDelimiter(chunk.charAt(7));
		}

		return d;
	}

	private String parseMessageVersion(DataChunk chunk, Delimiters delimiters) {
		String msh11 = getFieldValue(chunk.toString(), delimiters, 11);
		return msh11 != null ? getComponentValue(msh11, delimiters, 0) : null;
	}

	private String getFieldValue(String str, Delimiters delimiters, int i) {
		String[] tkz = StringHelper.explode(str, delimiters.getFieldDelimiter());

		if ((tkz == null) || (tkz.length <= i)) {
			return null;
		}

		return tkz[i];
	}

	private String getComponentValue(String value, Delimiters delimiters, int i) {
		if (value == null) {
			return null;
		}

		String[] tokens = StringHelper.explode(value, delimiters.getComponentDelimiter());

		if ((tokens == null) || (tokens.length <= i)) {
			return null;
		}

		return tokens[i];
	}

	private MsgInfo parseMessageName(DataChunk chunk, Delimiters delimiters) {
		String[] tkz = StringHelper.explode(chunk.toString(), delimiters.getFieldDelimiter());

		if ((tkz == null) || (tkz.length < 9) || (!"MSH".equals(tkz[0]))) {
			return null;
		}

		String tmp = tkz[8];

		if (tmp.length() == 0) {
			return null;
		}

		String[] tkz2 = StringHelper.explode(tmp, delimiters.getComponentDelimiter());

		String messageName = null;
		String messageStructureId = null;

		if (tkz2.length == 1) {
			messageName = DataHelper.getMessageName(tkz2[0], null);
		} else if (tkz2.length >= 2) {
			messageName = DataHelper.getMessageName(tkz2[0], tkz2[1]);

			if (tkz2.length >= 3) {
				messageStructureId = tkz2[2];
			}
		}

		MsgInfo msgInfo = new MsgInfo();
		msgInfo.messageName = messageName;
		msgInfo.messageStructureId = messageStructureId;

		return msgInfo;
	}

	public int getEndOffset() {
		if ((this.segments == null) || (this.segments.size() == 0)) {
			return -1;
		}

		ISSxSegment segment = getSegment(getSegmentsCount() - 1);
		return segment.getDataRange().getEndOffset();
	}

	public int getLength() {
		return getEndOffset() - getStartOffset();
	}

	public int getStartOffset() {
		if ((this.segments == null) || (this.segments.size() == 0)) {
			return -1;
		}

		ISSxSegment segment = getSegment(0);
		return segment.getDataRange().getStartOffset();
	}

	public void setParentElement(ISSxStructureElement parent) {
	}

	public void setParentStructure(ISSxStructure structure) {
	}

	public IDefinitionFactory getDefinitionFactory() {
		return this.definitionFactory;
	}

	public void dump() {
		System.out.println("DUMP STRUCTURE (len: " + this.data.getLength() + ")");
		int i = 0;
		for (int n = getSegmentsCount(); i < n; i++) {
			ISSxSegment segment = getSegment(i);
			System.out.println(segment.toString() + " (parsed:true)");
			dump(segment);
		}
	}

	private void dump(ISSxFieldContainer container) {
		int i = 0;
		for (int n = container.getFieldsCount(); i < n; i++) {
			ISSxField field = container.getField(i);
			System.out.println(
					getIndent(4 * (field.getLevel() + 1)) + " " + i + ". " + field.toString() + " (parsed:true)");
			dump(field);
		}
	}

	private String getIndent(int n) {
		StringBuffer sb = new StringBuffer(n);
		for (int i = 0; i < n; i++) {
			sb.append(' ');
		}
		return sb.toString();
	}

	public void setEndOffset(int endOffset) {
	}

	public void setStartOffset(int startOffset) {
	}

	public void shiftOffsets(int n) {
	}

	public int countChildren() {
		return 0;
	}

	public boolean isIncludeEmpty() {
		return this.includeEmpty;
	}

	public void setIncludeEmpty(boolean includeEmpty) {
		this.includeEmpty = includeEmpty;
	}

	public int getChildIndexByOffset(int offs) {
		return getSegmentIndexByOffset(offs);
	}

	public boolean isReal() {
		return true;
	}

	public DataRange getDataRange() {
		return this.range;
	}

	public Delimiters getDefaultDelimiters() {
		return this.defaultDelimiters;
	}

	public String getDefaultVersion() {
		return this.defaultVersion;
	}

	public String getForcedVersion() {
		return this.forcedVersion;
	}

	public void setForcedVersion(String forcedVersion) {
		this.forcedVersion = forcedVersion;
	}

	public void setDefinitionFactory(IDefinitionFactory definitionFactory) {
		this.definitionFactory = definitionFactory;
	}

	public void setDefaultDelimiters(Delimiters defaultDelimiters) {
		this.defaultDelimiters = defaultDelimiters;
	}

	public void setDefaultVersion(String defaultVersion) {
		this.defaultVersion = defaultVersion;
	}

	class MsgInfo {
		String messageName;
		String messageStructureId;

		MsgInfo() {
		}
	}
}