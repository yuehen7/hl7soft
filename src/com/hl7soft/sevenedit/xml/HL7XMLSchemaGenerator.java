package com.hl7soft.sevenedit.xml;

import com.hl7soft.sevenedit.db.defs.FieldDefinition;
import com.hl7soft.sevenedit.db.defs.FieldEntry;
import com.hl7soft.sevenedit.db.defs.IDefinitionFactory;
import com.hl7soft.sevenedit.db.defs.IFieldEntryContainer;
import com.hl7soft.sevenedit.db.defs.ISegmentEntry;
import com.hl7soft.sevenedit.db.defs.ISegmentEntryContainer;
import com.hl7soft.sevenedit.db.defs.MessageDefinition;
import com.hl7soft.sevenedit.db.defs.SegmentDefinition;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import net.n3.nanoxml.XMLElement;
import net.n3.nanoxml.XMLWriter;

public class HL7XMLSchemaGenerator {
	public static final int GROUP_TAG_NAME = 1;
	public static final int GROUP_TAG_NUMBER = 2;
	boolean includeTableValuesCheck;
	boolean includeTimestampPatternCheck;
	boolean linkFieldsAsPublicTypes = true;
	MessageDefinition messageDefinition;
	OutputStream os;
	Map<String, XSDType> publicSegmentTypes;
	Map<String, XSDType> publicFieldTypes;
	int groupTagStyle = 1;
	boolean useLstTag;
	boolean deterministic;

	public void write(MessageDefinition messageDefinition, OutputStream os) {
		try {
			this.messageDefinition = messageDefinition;
			this.os = os;
			this.publicSegmentTypes = new TreeMap();
			this.publicFieldTypes = new TreeMap();

			XMLElement root = createRoot();

			XSDElement messageElement = new XSDElement(messageDefinition.getName().toUpperCase(), 1, 1);
			messageElement.innerType = new XSDType();
			messageElement.innerType.sequenceElements = collectSegmentElements(messageDefinition, new int[] { 1 },
					new int[] { 1 });
			appendElement(root, messageElement);

			appendPublicSegmentTypes(root);
			appendPublicFieldTypes(root);

			XMLWriter xmlWriter = new XMLWriter(os);
			xmlWriter.write(root, true);
		} catch (Exception e) {
			throw new RuntimeException("Error generating XML schema.", e);
		}
	}

	public String generate(MessageDefinition messageModel) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		write(messageModel, os);
		return new String(os.toByteArray());
	}

	private XMLElement createRoot() {
		XMLElement root = new XMLElement("xsd:schema");
		root.setAttribute("xmlns:xsd", "http://www.w3.org/2001/XMLSchema");
		return root;
	}

	private void appendElement(XMLElement root, XSDSequenceElement seqElement) {
		if (seqElement == null) {
			return;
		}

		XMLElement node = null;

		if ((seqElement instanceof XSDElement)) {
			XSDElement element = (XSDElement) seqElement;

			node = new XMLElement("xsd:element");
			if (element.name != null) {
				node.setAttribute("name", element.name);
			}
			if (element.publicType != null) {
				node.setAttribute("type", element.publicType);
			}
			if (element.minOccurs != 1) {
				node.setAttribute("minOccurs", "" + element.minOccurs);
			}
			if (element.maxOccurs != 1) {
				if (element.maxOccurs == -1)
					node.setAttribute("maxOccurs", "unbounded");
				else {
					node.setAttribute("maxOccurs", "" + element.maxOccurs);
				}
			}
			if (element.innerType != null) {
				appendType(node, element.innerType);
			}
		} else if ((seqElement instanceof XSDChoice)) {
			XSDChoice choice = (XSDChoice) seqElement;
			node = new XMLElement("xsd:choice");

			if (choice.minOccurs != 1) {
				node.setAttribute("minOccurs", "" + choice.minOccurs);
			}
			if (choice.maxOccurs != 1) {
				if (choice.maxOccurs == -1)
					node.setAttribute("maxOccurs", "unbounded");
				else {
					node.setAttribute("maxOccurs", "" + choice.maxOccurs);
				}
			}

			if (choice.choiceElements != null) {
				int i = 0;
				for (int n = choice.choiceElements.size(); i < n; i++) {
					XSDElement element = (XSDElement) choice.choiceElements.get(i);

					XMLElement elementNode = new XMLElement("xsd:element");
					if (element.name != null) {
						elementNode.setAttribute("name", element.name);
					}
					if (element.publicType != null) {
						elementNode.setAttribute("type", element.publicType);
					}

					node.addChild(elementNode);
				}
			}
		}

		if (node != null)
			root.addChild(node);
	}

	private void appendType(XMLElement root, XSDType type) {
		XMLElement typeNode = null;

		if (type.sequenceElements != null) {
			typeNode = new XMLElement("xsd:complexType");
		} else {
			typeNode = new XMLElement("xsd:simpleType");

			XMLElement restrictionNode = new XMLElement("xsd:restriction");
			restrictionNode.setAttribute("base", "xsd:string");
			typeNode.addChild(restrictionNode);
		}

		if (type.name != null) {
			typeNode.setAttribute("name", type.name);
		}
		appendElements(typeNode, type.sequenceElements);
		root.addChild(typeNode);
	}

	private void appendElements(XMLElement root, List<XSDSequenceElement> elements) {
		if (elements == null) {
			return;
		}

		XMLElement sequenceNode = new XMLElement("xsd:sequence");
		root.addChild(sequenceNode);

		int i = 0;
		for (int n = elements.size(); i < n; i++) {
			XSDSequenceElement xsdElement = (XSDSequenceElement) elements.get(i);

			appendElement(sequenceNode, xsdElement);
		}
	}

	private void appendPublicSegmentTypes(XMLElement root) {
		if (this.publicSegmentTypes == null) {
			return;
		}

		Set keys = this.publicSegmentTypes.keySet();
		for (Iterator iter = keys.iterator(); iter.hasNext();) {
			String key = (String) iter.next();
			XSDType type = (XSDType) this.publicSegmentTypes.get(key);

			appendType(root, type);
		}
	}

	private void appendPublicFieldTypes(XMLElement root) {
		if (this.publicFieldTypes == null) {
			return;
		}

		Set keys = this.publicFieldTypes.keySet();
		for (Iterator iter = keys.iterator(); iter.hasNext();) {
			String key = (String) iter.next();
			XSDType type = (XSDType) this.publicFieldTypes.get(key);

			appendType(root, type);
		}
	}

	private List<XSDSequenceElement> collectSegmentElements(ISegmentEntryContainer container, int[] lstCnt,
			int[] grpCnt) {
		List res = new ArrayList();

		HashMap tagNameCnt = null;
		if (this.deterministic) {
			tagNameCnt = new HashMap(2);
		}

		int i = 0;
		for (int n = container.getSegmentEntriesCount(); i < n; i++) {
			ISegmentEntry segmentEntry = container.getSegmentEntry(i);

			XSDSequenceElement seqElement = null;

			if (segmentEntry.getType() == 1) {
				String sgmName = segmentEntry.getName();

				if (this.deterministic) {
					if (tagNameCnt.containsKey(sgmName)) {
						Integer cnt = (Integer) tagNameCnt.get(sgmName);
						Integer localInteger1 = cnt;
						Integer localInteger2 = cnt = Integer.valueOf(cnt.intValue() + 1);
						tagNameCnt.put(sgmName, cnt);
						sgmName = sgmName + "" + cnt;
					} else {
						tagNameCnt.put(sgmName, Integer.valueOf(1));
					}
				}

				XSDElement element = new XSDElement(sgmName);
				element.minOccurs = convMinOccurs(segmentEntry.getMinCount());
				element.maxOccurs = convMaxOccurs(segmentEntry.getMaxCount());
				element.publicType = segmentEntry.getName();
				seqElement = element;

				addToPublicSegmentTypes(segmentEntry.getName());
			} else if (segmentEntry.getType() == 2) {
				String name = null;

				if (this.groupTagStyle == 2)
					name = this.messageDefinition.getName() + ".GRP." + grpCnt[0];
				else {
					name = this.messageDefinition.getName() + "." + segmentEntry.getName();
				}

				XSDElement element = new XSDElement(name);
				element.minOccurs = convMinOccurs(segmentEntry.getMinCount());
				element.maxOccurs = convMaxOccurs(segmentEntry.getMaxCount());
				element.innerType = new XSDType();
				element.innerType.sequenceElements = collectSegmentElements(segmentEntry, lstCnt, grpCnt);
				seqElement = element;

				grpCnt[0] += 1;
			} else if (segmentEntry.getType() == 3) {
				XSDChoice choice = new XSDChoice();
				choice.minOccurs = convMinOccurs(segmentEntry.getMinCount());
				choice.maxOccurs = convMaxOccurs(segmentEntry.getMaxCount());

				Object choiceElements = new ArrayList(2);
				int j = 0;
				for (int n2 = segmentEntry.getSegmentEntriesCount(); j < n2; j++) {
					ISegmentEntry choiceEntry = segmentEntry.getSegmentEntry(j);

					XSDElement element = new XSDElement(choiceEntry.getName());
					element.publicType = choiceEntry.getName();
					((List) choiceElements).add(element);

					addToPublicSegmentTypes(choiceEntry.getName());
				}

				choice.choiceElements = ((List) choiceElements);
				seqElement = choice;
			}

			if (seqElement != null) {
				if ((this.useLstTag) && ((segmentEntry.getMaxCount() == 0) || (segmentEntry.getMaxCount() > 1))) {
					String name = this.messageDefinition.getName() + ".LST." + lstCnt[0];
					XSDElement lstElement = new XSDElement(name);
					lstElement.maxOccurs = 1;
					lstElement.innerType = new XSDType();
					lstElement.innerType.add(seqElement);
					seqElement = lstElement;
					lstCnt[0] += 1;
				}

				if (res == null) {
					res = new ArrayList();
				}
				res.add(seqElement);
			}
		}
		return res;
	}

	private void addToPublicSegmentTypes(String sgmName) {
		if (this.publicSegmentTypes.get(sgmName) == null) {
			SegmentDefinition segmentDefinition = this.messageDefinition.getFactory()
					.getSegmentDefinition(this.messageDefinition.getVersion(), sgmName);

			if (segmentDefinition == null) {
				throw new RuntimeException("Segment definition not found: " + sgmName + " version: "
						+ this.messageDefinition.getVersion());
			}

			XSDType segmentType = createSingleSegmentType(segmentDefinition);
			this.publicSegmentTypes.put(segmentType.name, segmentType);
		}
	}

	private List<XSDSequenceElement> collectFieldElements(String parentName, IFieldEntryContainer container) {
		if ((container == null) || (container.getFieldEntriesCount() == 0)) {
			return null;
		}

		List res = null;

		int i = 0;
		for (int n = container.getFieldEntriesCount(); i < n; i++) {
			FieldEntry fieldEntry = container.getFieldEntry(i);

			if (parentName == null) {
				parentName = "UNKN";
			}

			String name = parentName + "." + (i + 1);
			XSDElement element = new XSDElement(name);

			element.minOccurs = (fieldEntry.getOptionality() == 2 ? 1 : 0);
			element.maxOccurs = (fieldEntry.getRepeatCount() != 0 ? fieldEntry.getRepeatCount() : -1);

			if (this.linkFieldsAsPublicTypes) {
				element.publicType = fieldEntry.getName();

				if (this.publicFieldTypes.get(fieldEntry.getName()) == null) {
					FieldDefinition fieldDefinition = this.messageDefinition.getFactory()
							.getFieldDefinition(this.messageDefinition.getVersion(), fieldEntry.getName());

					if (fieldDefinition == null) {
						throw new RuntimeException("Field definition not found: " + fieldEntry.getName() + " version: "
								+ this.messageDefinition.getVersion());
					}

					XSDType fieldType = createFieldType(fieldDefinition);
					this.publicFieldTypes.put(fieldType.name, fieldType);
				}
			} else {
				FieldDefinition fieldDefinition = this.messageDefinition.getFactory()
						.getFieldDefinition(this.messageDefinition.getVersion(), fieldEntry.getName());

				if (fieldDefinition == null) {
					throw new RuntimeException("Field definition not found: " + fieldEntry.getName() + " version: "
							+ this.messageDefinition.getVersion());
				}

				XSDType fieldType = createFieldType(fieldDefinition);
				element.innerType = fieldType;
			}

			if ((this.useLstTag & ((fieldEntry.getRepeatCount() == 0) || (fieldEntry.getRepeatCount() > 1)))) {
				String lstNodeName = parentName + "." + (i + 1) + ".LST";
				XSDElement lstElement = new XSDElement(lstNodeName);
				lstElement.maxOccurs = 1;
				lstElement.innerType = new XSDType();
				lstElement.innerType.add(element);
				element = lstElement;
			}

			if (res == null) {
				res = new ArrayList();
			}
			res.add(element);
		}

		return res;
	}

	private int convMinOccurs(int minOccurs) {
		if (minOccurs == 0) {
			return 0;
		}

		return minOccurs;
	}

	private int convMaxOccurs(int maxOccurs) {
		if (maxOccurs == 0) {
			return -1;
		}

		return maxOccurs;
	}

	private XSDType createFieldType(FieldDefinition fieldDefinition) {
		XSDType type = new XSDType(fieldDefinition.getName());
		type.sequenceElements = collectFieldElements(fieldDefinition.getName(), fieldDefinition);

		return type;
	}

	private XSDType createSingleSegmentType(SegmentDefinition segmentDefinition) {
		XSDType type = new XSDType(segmentDefinition.getName());
		type.sequenceElements = collectFieldElements(segmentDefinition.getName(), segmentDefinition);

		return type;
	}

	public int getGroupTagStyle() {
		return this.groupTagStyle;
	}

	public void setGroupTagStyle(int groupTagStyle) {
		this.groupTagStyle = groupTagStyle;
	}

	public boolean isUseLstTag() {
		return this.useLstTag;
	}

	public void setUseLstTag(boolean useLstTag) {
		this.useLstTag = useLstTag;
	}

	public boolean isDeterministic() {
		return this.deterministic;
	}

	public void setDeterministic(boolean deterministic) {
		this.deterministic = deterministic;
	}

	class XSDChoice implements HL7XMLSchemaGenerator.XSDSequenceElement {
		int minOccurs = 0;

		int maxOccurs = -1;
		List<HL7XMLSchemaGenerator.XSDElement> choiceElements;

		XSDChoice() {
		}
	}

	class XSDElement implements HL7XMLSchemaGenerator.XSDSequenceElement {
		int minOccurs = 0;

		int maxOccurs = -1;
		String name;
		String publicType;
		HL7XMLSchemaGenerator.XSDType innerType;

		public XSDElement(String name, int minOccurs, int maxOccurs) {
			this.name = name;
			this.minOccurs = minOccurs;
			this.maxOccurs = maxOccurs;
		}

		public XSDElement(String name) {
			this(name, 0, -1);
		}
	}

	class XSDType {
		String name;
		List<HL7XMLSchemaGenerator.XSDSequenceElement> sequenceElements;

		public XSDType() {
		}

		public XSDType(String name) {
			this.name = name;
		}

		public void add(HL7XMLSchemaGenerator.XSDSequenceElement element) {
			if (this.sequenceElements == null) {
				this.sequenceElements = new ArrayList();
			}

			this.sequenceElements.add(element);
		}
	}

	static abstract interface XSDSequenceElement {
	}
}