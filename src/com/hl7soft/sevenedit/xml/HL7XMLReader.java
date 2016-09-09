package com.hl7soft.sevenedit.xml;

import com.hl7soft.sevenedit.model.structure.parser.Delimiters;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PushbackInputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.n3.nanoxml.IXMLElement;
import net.n3.nanoxml.IXMLParser;
import net.n3.nanoxml.IXMLReader;
import net.n3.nanoxml.StdXMLReader;
import net.n3.nanoxml.XMLParserFactory;

public class HL7XMLReader {
	List<String> data;
	IXMLElement root;
	private static final char SGM_DELIMITER = '\r';
	String currentMessageName;
	Delimiters currentMessageDelimiters;

	public void convert(InputStream is) {
		try {
			initXmlRoot(is);
			process();
		} catch (Exception e) {
			throw new RuntimeException("Error reading HL7-XML.", e);
		}
	}

	private void initXmlRoot(InputStream is) {
		try {
			InputStreamReader reader = createReader(is);
			IXMLParser parser = XMLParserFactory.createDefaultXMLParser();
			IXMLReader xmlReader = new StdXMLReader(new BufferedReader(reader));
			parser.setReader(xmlReader);
			this.root = ((IXMLElement) parser.parse());
		} catch (Exception e) {
			throw new RuntimeException("Invalid XML format.", e);
		}
	}

	private InputStreamReader createReader(InputStream is) throws IOException, UnsupportedEncodingException {
		PushbackInputStream pbis = new PushbackInputStream(is, 1000);
		byte[] data = new byte[1000];
		pbis.read(data);
		pbis.unread(data);
		String encoding = XMLEncodingDetector.detect(data, Charset.defaultCharset().name());

		return new InputStreamReader(pbis, encoding);
	}

	public void process() throws Exception {
		if (this.root == null) {
			return;
		}

		if (isMessageRoot(this.root))
			processMessage(this.root);
		else if (isBatchRoot(this.root))
			processMultipleMessages(this.root);
	}

	private boolean isBatchRoot(IXMLElement node) {
		if (node.getChildrenCount() == 0) {
			return false;
		}

		IXMLElement messageRoot = node.getChildAtIndex(0);
		if (!isMessageRoot(messageRoot)) {
			return false;
		}

		return true;
	}

	private boolean isMessageRoot(IXMLElement node) {
		if (node.getChildrenCount() == 0) {
			return false;
		}

		IXMLElement msh = node.getChildAtIndex(0);
		if (!"MSH".equalsIgnoreCase(msh.getName())) {
			return false;
		}

		return true;
	}

	private void processMultipleMessages(IXMLElement batchRoot) {
		int i = 0;
		for (int n = batchRoot.getChildrenCount(); i < n; i++) {
			IXMLElement messageRoot = batchRoot.getChildAtIndex(i);
			processMessage(messageRoot);
		}
	}

	private void processMessage(IXMLElement messageRoot) {
		this.currentMessageName = messageRoot.getName();

		this.currentMessageDelimiters = getMessageDelimiters(messageRoot);

		StringBuffer sb = new StringBuffer();

		int i = 0;
		for (int n = messageRoot.getChildrenCount(); i < n; i++) {
			IXMLElement segmentRoot = messageRoot.getChildAtIndex(i);
			sb.append(getSegmentData(segmentRoot));
			if (i != n - 1) {
				sb.append('\r');
			}
		}

		if (this.data == null) {
			this.data = new ArrayList();
		}
		this.data.add(sb.toString());
	}

	private String getSegmentData(IXMLElement segmentRoot) {
		String segmentName = segmentRoot.getName();
		boolean msh = "MSH".equalsIgnoreCase(segmentName);

		boolean isLst = segmentName.contains("LST");
		if (isLst) {
			return getSegmentListData(segmentRoot);
		}

		boolean isGroup = segmentName.startsWith(this.currentMessageName);
		if (isGroup) {
			return getSegmentGroupData(segmentRoot);
		}

		if ((segmentName == null) || (segmentName.length() != 3)) {
			throw new RuntimeException("Invalid segment name: " + getPathToNode(segmentRoot));
		}

		StringBuffer sb = new StringBuffer();

		sb.append(segmentName.toUpperCase());
		sb.append(this.currentMessageDelimiters.getFieldDelimiter());

		if (msh) {
			sb.append(this.currentMessageDelimiters.getComponentDelimiter());
			sb.append(this.currentMessageDelimiters.getRepeatDelimiter());
			sb.append(this.currentMessageDelimiters.getEscapeDelimiter());
			sb.append(this.currentMessageDelimiters.getSubcomponentDelimiter());
		}

		int currentFieldPosition = 1;
		String previousTagName = null;
		int i = 0;
		for (int n = segmentRoot.getChildrenCount(); i < n; i++) {
			IXMLElement fldNode = segmentRoot.getChildAtIndex(i);
			String fieldTagName = fldNode.getName();

			if ((!"MSH.1".equalsIgnoreCase(fieldTagName)) && (!"MSH.2".equalsIgnoreCase(fieldTagName))) {
				if (fieldTagName.equalsIgnoreCase(previousTagName)) {
					sb.append(this.currentMessageDelimiters.getRepeatDelimiter());
					sb.append(getFieldData(fldNode, 0));
				} else {
					int fieldPos = parseFieldPosition(fldNode);

					fieldPos = msh ? fieldPos - 1 : fieldPos;

					for (int j = currentFieldPosition; j < fieldPos; j++) {
						sb.append(this.currentMessageDelimiters.getFieldDelimiter());
					}
					currentFieldPosition = fieldPos;

					isLst = fieldTagName.contains("LST");
					if (isLst) {
						sb.append(getFieldListData(fldNode, 0));
					} else {
						sb.append(getFieldData(fldNode, 0));

						previousTagName = fieldTagName;
					}
				}
			}
		}
		return sb.toString();
	}

	private int parseFieldPosition(IXMLElement fldNode) {
		if (fldNode == null) {
			return -1;
		}

		String fieldTagName = fldNode.getName();

		int fieldPos = -1;

		if (fieldTagName.endsWith("LST")) {
			fieldTagName = fieldTagName.substring(0, fieldTagName.length() - 4);
		}

		int tmpIdx = fieldTagName.indexOf('.');
		if (tmpIdx == -1)
			throw new RuntimeException("Invalid field tag: " + getPathToNode(fldNode) + ". Field position required.");
		try {
			fieldPos = Integer.parseInt(fieldTagName.substring(tmpIdx + 1));
		} catch (Exception e) {
			throw new RuntimeException(
					"Invalid field tag: " + getPathToNode(fldNode) + ". Field position is not an integer.");
		}

		if ((fieldPos < 1) || (fieldPos > 65535)) {
			throw new RuntimeException(
					"Invalid field tag: " + getPathToNode(fldNode) + ". Field position is out of range [1..65535].");
		}

		return fieldPos;
	}

	private String getSegmentListData(IXMLElement segmentListRoot) {
		StringBuffer sb = new StringBuffer();

		int i = 0;
		for (int n = segmentListRoot.getChildrenCount(); i < n; i++) {
			IXMLElement segmentNode = segmentListRoot.getChildAtIndex(i);
			sb.append(getSegmentData(segmentNode));
			if (i != n - 1) {
				sb.append('\r');
			}
		}

		return sb.toString();
	}

	private String getSegmentGroupData(IXMLElement segmentGroupRoot) {
		StringBuffer sb = new StringBuffer();

		int i = 0;
		for (int n = segmentGroupRoot.getChildrenCount(); i < n; i++) {
			IXMLElement segmentNode = segmentGroupRoot.getChildAtIndex(i);
			sb.append(getSegmentData(segmentNode));
			if (i != n - 1) {
				sb.append('\r');
			}
		}

		return sb.toString();
	}

	private String getFieldData(IXMLElement fieldRoot, int level) {
		if (fieldRoot.getChildrenCount() == 0) {
			String content = fieldRoot.getContent();
			return (content != null) && (content.length() > 0) ? content : "";
		}

		boolean isLst = fieldRoot.getName().contains("LST");
		if (isLst) {
			return getFieldListData(fieldRoot, level);
		}

		StringBuffer sb = new StringBuffer();

		int currentFieldPosition = 1;
		String previousTagName = null;
		int i = 0;
		for (int n = fieldRoot.getChildrenCount(); i < n; i++) {
			IXMLElement fldNode = fieldRoot.getChildAtIndex(i);
			String fieldTagName = fldNode.getName();

			isLst = fieldTagName.contains("LST");
			if (isLst) {
				sb.append(getFieldListData(fldNode, level));
			} else if (fieldTagName.equals(previousTagName)) {
				sb.append(this.currentMessageDelimiters.getRepeatDelimiter());
				sb.append(getFieldData(fldNode, level));
			} else {
				int tmpIdx = fieldTagName.indexOf('.');
				if (tmpIdx == -1)
					throw new RuntimeException(
							"Invalid field tag: " + getPathToNode(fldNode) + ". Field position required.");
				int fieldPos;
				try {
					fieldPos = Integer.parseInt(fieldTagName.substring(tmpIdx + 1));
				} catch (Exception e) {
					System.out.println(fieldTagName.substring(tmpIdx + 1));
					throw new RuntimeException(
							"Invalid field tag: " + getPathToNode(fldNode) + ". Field position is not an integer.");
				}

				if ((fieldPos < 1) || (fieldPos > 65535)) {
					throw new RuntimeException("Invalid field tag: " + getPathToNode(fldNode)
							+ ". Field position is out of range [1..65535].");
				}

				while (currentFieldPosition < fieldPos) {
					if (level == 0)
						sb.append(this.currentMessageDelimiters.getComponentDelimiter());
					else if (level == 1)
						sb.append(this.currentMessageDelimiters.getSubcomponentDelimiter());
					else {
						throw new RuntimeException("Field nesting level is too deep: " + getPathToNode(fldNode));
					}

					currentFieldPosition++;
				}

				sb.append(getFieldData(fldNode, level + 1));

				previousTagName = fieldTagName;
			}
		}
		return sb.toString();
	}

	private String getFieldListData(IXMLElement fieldRoot, int level) {
		StringBuffer sb = new StringBuffer();

		int i = 0;
		for (int n = fieldRoot.getChildrenCount(); i < n; i++) {
			IXMLElement fldNode = fieldRoot.getChildAtIndex(i);

			sb.append(getFieldData(fldNode, level));

			if (i != n - 1) {
				sb.append(this.currentMessageDelimiters.getRepeatDelimiter());
			}
		}

		return sb.toString();
	}

	private String getPathToNode(IXMLElement node) {
		List path = new ArrayList();
		path.add(node);

		IXMLElement childNode = node;
		IXMLElement parentNode = null;
		while ((parentNode = childNode.getParent()) != null) {
			path.add(parentNode);
			childNode = parentNode;
		}

		Collections.reverse(path);
		StringBuffer sb = new StringBuffer();
		int i = 0;
		for (int n = path.size(); i < n; i++) {
			sb.append(((IXMLElement) path.get(i)).getName());
			if (i != n - 1) {
				sb.append(" -> ");
			}
		}

		return sb.toString();
	}

	private Delimiters getMessageDelimiters(IXMLElement messageRoot) {
		Delimiters delimiters = new Delimiters();

		IXMLElement msh = messageRoot.getChildAtIndex(0);
		if (!"MSH".equalsIgnoreCase(msh.getName())) {
			throw new RuntimeException("MSH tag not found.");
		}

		if (msh.getChildrenCount() > 0) {
			IXMLElement msh1 = msh.getChildAtIndex(0);

			if ("MSH.1".equalsIgnoreCase(msh1.getName())) {
				String str = msh1.getContent();

				if (str.length() == 1) {
					char fieldDelimiter = str.charAt(0);
					delimiters.setFieldDelimiter(fieldDelimiter);
				}
			}

		}

		if (msh.getChildrenCount() > 1) {
			IXMLElement msh2 = msh.getChildAtIndex(1);

			if ("MSH.2".equalsIgnoreCase(msh2.getName())) {
				String str = msh2.getContent();

				if (str.length() == 4) {
					char componentDelimiter = str.charAt(0);
					char repeatDelimiter = str.charAt(1);
					char escapeDelimiter = str.charAt(2);
					char subcomponentDelimiter = str.charAt(3);
					delimiters.setComponentDelimiter(componentDelimiter);
					delimiters.setRepeatDelimiter(repeatDelimiter);
					delimiters.setEscapeDelimiter(escapeDelimiter);
					delimiters.setSubcomponentDelimiter(subcomponentDelimiter);
				}
			}
		}

		return delimiters;
	}

	public List<String> getData() {
		return this.data;
	}
}