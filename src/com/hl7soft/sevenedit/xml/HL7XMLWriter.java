package com.hl7soft.sevenedit.xml;

import java.io.File;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import com.hl7soft.sevenedit.db.defs.IDefinitionFactory;
import com.hl7soft.sevenedit.db.defs.ISegmentEntry;
import com.hl7soft.sevenedit.db.defs.MessageDefinition;
import com.hl7soft.sevenedit.db.defs.io.bin.DefinitionFileFactory;
import com.hl7soft.sevenedit.model.data.Data;
import com.hl7soft.sevenedit.model.structure.dparser.message.IMSxElement;
import com.hl7soft.sevenedit.model.structure.dparser.message.IMSxField;
import com.hl7soft.sevenedit.model.structure.dparser.message.IMSxFieldContainer;
import com.hl7soft.sevenedit.model.structure.dparser.message.IMSxMessage;
import com.hl7soft.sevenedit.model.structure.dparser.message.IMSxSegment;
import com.hl7soft.sevenedit.model.structure.dparser.message.IMSxSegmentContainer;
import com.hl7soft.sevenedit.model.structure.dparser.message.MSxMessage;
import com.hl7soft.sevenedit.model.structure.dparser.message.MSxStructure;

public class HL7XMLWriter {
	public static final int GROUP_TAG_NAME = 1;
	public static final int GROUP_TAG_NUMBER = 2;

	boolean formatOutput = true;
	boolean includeEmptyNodes;
	String batchTagName = "BATCH";

	int groupTagStyle = 1;
	IDefinitionFactory definitionFactory;
	Charset charset = Charset.forName("UTF-8");
	String forcedVersion;
	String defaultVersion;

	public HL7XMLWriter() {
		try {
			String path = HL7XMLWriter.class.getResource("/data/models/models.dfn").getPath();
			File file = new File(path);
			IDefinitionFactory definitionFactory = new DefinitionFileFactory(file);
			this.setDefinitionFactory(definitionFactory);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void convert(String message, StringWriter os) {
		ArrayList messages = new ArrayList();
		messages.add(message);
		convert(messages, os);
	}

	public void convert(List<String> messages, StringWriter os) {
		try {
			XMLOutputFactory xmlFactory = XMLOutputFactory.newInstance();
			XMLStreamWriter xwriter = xmlFactory.createXMLStreamWriter(os);

			if (this.formatOutput) {
				xwriter = new IndentingXMLStreamWriter(xwriter);
			}

			xwriter.writeStartDocument("UTF-8", "1.0");

			if (messages.size() > 1) {
				xwriter.writeStartElement(prepareBatchName(this.batchTagName));
			}

			int i = 0;
			for (int n = messages.size(); i < n; i++) {
				String data = (String) messages.get(i);
				if(data!=null && data.indexOf("\r\n")>-1){
					data = data.replace("\r\n", "\r");
				}else if(data!=null && data.indexOf("\n")>-1){
					data = data.replace("\n", "\r");
				}
				MSxStructure structure = new MSxStructure(new Data(data), this.definitionFactory);
				structure.setDefaultVersion(this.defaultVersion);
				structure.setForcedVersion(this.forcedVersion);
				structure.parse();

				MSxMessage message = structure.getMessage();
				writeMessage(xwriter, i, message);
				xwriter.flush();
			}

			if (messages.size() > 1) {
				xwriter.writeEndElement();
			}

			xwriter.writeEndDocument();
			xwriter.flush();
			xwriter.close();
		} catch (Exception e) {
			throw new RuntimeException("Error exporting HL7-XML format.", e);
		}
	}

	private void writeMessage(XMLStreamWriter xwriter, int idx, IMSxMessage message) throws Exception {
		try {
			MessageDefinition definition = message.getDefinition();
			String messageElementName = definition != null ? definition.getName() : message.getName();
			if ((messageElementName == null) || (messageElementName.length() == 0)) {
				throw new RuntimeException("Message name is empty.");
			}

			xwriter.writeStartElement(messageElementName);
			writeSegmentNodes(xwriter, message, new int[] { 1 });
			xwriter.writeEndElement();
		} catch (Exception e) {
			throw new RuntimeException("Error exporting message #" + (idx + 1) + ".", e);
		}
	}

	private void writeSegmentNodes(XMLStreamWriter xwriter, IMSxSegmentContainer container, int[] grpCnt)
			throws Exception {
		int i = 0;
		for (int n = container.getSegmentsCount(); i < n; i++) {
			IMSxSegment segment = container.getSegment(i);
			int nameCnt = 0;

			if (segment.isArray()) {
				if ((segment.isReal()) && ((this.includeEmptyNodes) || (!isEmpty(segment)))) {
					writeSegmentArrayNodes(xwriter, segment, nameCnt, grpCnt);
				}

			} else if (segment.isReal()) {
				boolean isEmpty = isEmpty(segment);
				if ((!isEmpty) || ((isEmpty) && (this.includeEmptyNodes)))
					writeSegmentNode(xwriter, segment, nameCnt, grpCnt);
			}
		}
	}

	private void writeSegmentArrayNodes(XMLStreamWriter xwriter, IMSxSegment sgmArray, int nameCnt, int[] grpCnt)
			throws Exception {
		ISegmentEntry segmentEntry = sgmArray.getEntry();
		IMSxMessage message = sgmArray.getParentMessage();
		String msgName = message.getName();

		if (segmentEntry != null) {
			if (segmentEntry.getType() == 2) {
				int i = 0;
				for (int n = sgmArray.getSegmentsCount(); i < n; i++) {
					IMSxSegment sg = sgmArray.getSegment(i);

					String groupElementName = null;
					if (this.groupTagStyle == 2) {
						groupElementName = msgName + ".GRP." + grpCnt[0];
					} else {
						groupElementName = msgName + "." + prepareGroupName(segmentEntry.getName());
					}

					xwriter.writeStartElement(groupElementName);
					writeSegmentNodes(xwriter, sg, grpCnt);
					xwriter.writeEndElement();
				}

				grpCnt[0] += 1;
			} else if ((segmentEntry.getType() == 1) || (segmentEntry.getType() == 3)) {
				int i = 0;
				for (int n = sgmArray.getSegmentsCount(); i < n; i++) {
					IMSxSegment s = sgmArray.getSegment(i);

					String sgmElementName = getNameWithCounter(s.getName(), nameCnt);

					if (countRealFields(s) == 0) {
						xwriter.writeEmptyElement(sgmElementName);
					} else {
						xwriter.writeStartElement(sgmElementName);
						writeFieldNodes(xwriter, s, new int[] { 1 });
						xwriter.writeEndElement();
					}

				}

			}

		} else {
			int i = 0;
			for (int n = sgmArray.getSegmentsCount(); i < n; i++) {
				IMSxSegment s = sgmArray.getSegment(i);
				xwriter.writeStartElement(s.getName());
				xwriter.writeEndElement();
			}
		}
	}

	private void writeSegmentNode(XMLStreamWriter xwriter, IMSxSegment sgm, int nameCnt, int[] grpCnt)
			throws Exception {
		ISegmentEntry segmentEntry = sgm.getEntry();
		IMSxMessage message = sgm.getParentMessage();
		String msgName = message.getName();

		if (segmentEntry != null) {
			if ((segmentEntry.getType() == 1) || (segmentEntry.getType() == 3)) {
				String sgmElementName = getNameWithCounter(sgm.getName(), nameCnt);

				if (countRealFields(sgm) == 0) {
					xwriter.writeEmptyElement(sgmElementName);
					return;
				}

				xwriter.writeStartElement(sgmElementName);
				writeFieldNodes(xwriter, sgm, new int[] { 1 });
				xwriter.writeEndElement();
			} else if (segmentEntry.getType() == 2) {
				String sgmName = null;
				if (this.groupTagStyle == 2) {
					sgmName = msgName + ".GRP." + grpCnt[0];
				} else {
					sgmName = msgName + "." + prepareGroupName(segmentEntry.getName());
				}

				xwriter.writeStartElement(sgmName);
				writeSegmentNodes(xwriter, sgm, grpCnt);
				xwriter.writeEndElement();

				grpCnt[0] += 1;
			}

		} else {
			if (countRealFields(sgm) == 0) {
				xwriter.writeEmptyElement(sgm.getName());
				return;
			}

			xwriter.writeStartElement(sgm.getName());
			writeFieldNodes(xwriter, sgm, new int[] { 1 });
			xwriter.writeEndElement();
		}
	}

	private int countRealFields(IMSxFieldContainer container) {
		int cnt = 0;

		int i = 0;
		for (int n = container.getFieldsCount(); i < n; i++) {
			IMSxField field = container.getField(i);

			if (field.isReal()) {
				if (field.isArray()) {
					if ((this.includeEmptyNodes) || (!isEmpty(field))) {
						cnt += countRealFields(field);
					}
				} else
					cnt++;
			}
		}
		return cnt;
	}

	private void writeFieldNodes(XMLStreamWriter xwriter, IMSxFieldContainer container, int[] fieldCnt)
			throws Exception {
		int i = 0;
		for (int n = container.getFieldsCount(); i < n; i++) {
			IMSxField field = container.getField(i);

			if (field.isArray()) {
				if ((field.isReal()) && ((this.includeEmptyNodes) || (!isEmpty(field)))) {
					writeFieldArrayNodes(xwriter, field, fieldCnt);
				}

			} else if (field.isReal()) {
				boolean isEmpty = isEmpty(field);
				if ((!isEmpty) || ((isEmpty) && (this.includeEmptyNodes))) {
					writeFieldNode(xwriter, field, fieldCnt);
				}

			}

			fieldCnt[0] += 1;
		}
	}

	private void writeFieldArrayNodes(XMLStreamWriter xwriter, IMSxField fieldArray, int[] fieldCnt) throws Exception {
		IMSxElement parent = fieldArray.getParentElement();

		String parentName = "UNKN";

		if ((parent instanceof IMSxSegment)) {
			parentName = ((IMSxSegment) parent).getName();
		} else if ((parent instanceof IMSxField)) {
			IMSxField parentField = (IMSxField) parent;
			parentName = parentField.getEntry() != null ? parentField.getEntry().getName() : parentName;
		}

		int i = 0;
		for (int n = fieldArray.getChildrenCount(); i < n; i++) {
			IMSxField field = fieldArray.getField(i);
			xwriter.writeStartElement(parentName + "." + fieldCnt[0]);

			if (!field.isPrimitive()) {
				writeFieldNodes(xwriter, field, new int[] { 1 });
			} else {
				xwriter.writeCharacters(getFieldValue(field));
			}

			xwriter.writeEndElement();
		}
	}

	private void writeFieldNode(XMLStreamWriter xwriter, IMSxField field, int[] fieldCnt) throws Exception {
		IMSxElement parent = field.getParentElement();
		String parentName = "UNKN";
		String fieldName = field.getEntry() != null ? field.getEntry().getName() : null;

		if ((parent instanceof IMSxSegment)) {
			parentName = ((IMSxSegment) parent).getName();
		} else if ((parent instanceof IMSxField)) {
			IMSxField parentField = (IMSxField) parent;
			parentName = parentField.getEntry() != null ? parentField.getEntry().getName() : parentName;
		}

		if ((parentName == null) || (parentName.equalsIgnoreCase("N/A"))) {
			parentName = "UNKN";
		}

		String fieldElementName = parentName + "." + fieldCnt[0];

		if (!field.isPrimitive()) {
			xwriter.writeStartElement(fieldElementName);
			writeFieldNodes(xwriter, field, new int[] { 1 });
			xwriter.writeEndElement();
		} else {
			String fieldValue = getFieldValue(field);
			if ((fieldValue == null) || (fieldValue.length() == 0)) {
				xwriter.writeEmptyElement(fieldElementName);
				return;
			}

			xwriter.writeStartElement(fieldElementName);
			xwriter.writeCharacters(fieldValue);
			xwriter.writeEndElement();
		}
	}

	private String getFieldValue(IMSxField field) {
		return field.getData().getString(field.getDataRange().getStartOffset(), field.getDataRange().getLength());
	}

	private String getNameWithCounter(String name, int cnt) {
		if ((name == null) || (cnt < 2)) {
			return name;
		}

		return name + "" + cnt;
	}

	private boolean isEmpty(IMSxField field) {
		if (field.isPrimitive()) {
			return (!field.isReal()) || (field.getDataRange().getLength() == 0);
		}

		int i = 0;
		for (int n = field.getFieldsCount(); i < n; i++) {
			IMSxField subField = field.getField(i);
			if (!isEmpty(subField)) {
				return false;
			}

		}

		return true;
	}

	private boolean isEmpty(IMSxSegment segment) {
		return !segment.isReal();
	}

	private String prepareBatchName(String str) {
		if (str == null) {
			return "BATCH";
		}

		StringBuffer sb = new StringBuffer();

		int i = 0;
		for (int n = str.length(); i < n; i++) {
			char c = str.charAt(i);

			if (c != '_') {
				if (c == ' ')
					c = '_';
				else {
					if (!Character.isLetterOrDigit(c))
						continue;
				}
			}
			sb.append(c);
		}

		str = sb.toString();
		str = str.toUpperCase();

		return str;
	}

	private String prepareGroupName(String str) {
		StringBuffer sb = new StringBuffer();

		int i = 0;
		for (int n = str.length(); i < n; i++) {
			char c = str.charAt(i);

			if (c != '_') {
				if (c == ' ')
					c = '_';
				else {
					if (!Character.isLetterOrDigit(c))
						continue;
				}
			}
			sb.append(c);
		}

		str = sb.toString();
		str = str.toUpperCase();

		return str;
	}

	public boolean isIncludeEmptyNodes() {
		return this.includeEmptyNodes;
	}

	public void setIncludeEmptyNodes(boolean includeEmptyNodes) {
		this.includeEmptyNodes = includeEmptyNodes;
	}

	public String getBatchTagName() {
		return this.batchTagName;
	}

	public void setBatchTagName(String batchTagName) {
		this.batchTagName = batchTagName;
	}

	public int getGroupTagStyle() {
		return this.groupTagStyle;
	}

	public void setGroupTagStyle(int groupTagStyle) {
		this.groupTagStyle = groupTagStyle;
	}

	public IDefinitionFactory getDefinitionFactory() {
		return this.definitionFactory;
	}

	public void setDefinitionFactory(IDefinitionFactory definitionFactory) {
		this.definitionFactory = definitionFactory;
	}

	public boolean isFormatOutput() {
		return this.formatOutput;
	}

	public void setFormatOutput(boolean formatOutput) {
		this.formatOutput = formatOutput;
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
}
