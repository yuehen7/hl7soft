package com.hl7soft.sevenedit.xml;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class IndentingXMLStreamWriter implements XMLStreamWriter {
	XMLStreamWriter out;
	private int depth = 0;

	private int[] stack = { 0, 0, 0, 0 };
	private static final int WROTE_MARKUP = 1;
	private static final int WROTE_DATA = 2;
	private String indent = "  ";

	private String newLine = "\n";

	private char[] linePrefix = null;
	public static final String DEFAULT_INDENT = "  ";
	public static final String NORMAL_END_OF_LINE = "\n";

	public IndentingXMLStreamWriter(XMLStreamWriter out) {
		this.out = out;
	}

	public void setIndent(String indent) {
		if (!indent.equals(this.indent)) {
			this.indent = indent;
			this.linePrefix = null;
		}
	}

	public String getIndent() {
		return this.indent;
	}

	public void setNewLine(String newLine) {
		if (!newLine.equals(this.newLine)) {
			this.newLine = newLine;
			this.linePrefix = null;
		}
	}

	public static String getLineSeparator() {
		try {
			return System.getProperty("line.separator");
		} catch (SecurityException ignored) {
		}
		return "\n";
	}

	public String getNewLine() {
		return this.newLine;
	}

	public void writeStartDocument() throws XMLStreamException {
		beforeMarkup();
		this.out.writeStartDocument();
		afterMarkup();
	}

	public void writeStartDocument(String version) throws XMLStreamException {
		beforeMarkup();
		this.out.writeStartDocument(version);
		afterMarkup();
	}

	public void writeStartDocument(String encoding, String version) throws XMLStreamException {
		beforeMarkup();
		this.out.writeStartDocument(encoding, version);
		afterMarkup();
	}

	public void writeDTD(String dtd) throws XMLStreamException {
		beforeMarkup();
		this.out.writeDTD(dtd);
		afterMarkup();
	}

	public void writeProcessingInstruction(String target) throws XMLStreamException {
		beforeMarkup();
		this.out.writeProcessingInstruction(target);
		afterMarkup();
	}

	public void writeProcessingInstruction(String target, String data) throws XMLStreamException {
		beforeMarkup();
		this.out.writeProcessingInstruction(target, data);
		afterMarkup();
	}

	public void writeComment(String data) throws XMLStreamException {
		beforeMarkup();
		this.out.writeComment(data);
		afterMarkup();
	}

	public void writeEmptyElement(String localName) throws XMLStreamException {
		beforeMarkup();
		this.out.writeEmptyElement(localName);
		afterMarkup();
	}

	public void writeEmptyElement(String namespaceURI, String localName) throws XMLStreamException {
		beforeMarkup();
		this.out.writeEmptyElement(namespaceURI, localName);
		afterMarkup();
	}

	public void writeEmptyElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
		beforeMarkup();
		this.out.writeEmptyElement(prefix, localName, namespaceURI);
		afterMarkup();
	}

	public void writeStartElement(String localName) throws XMLStreamException {
		beforeStartElement();
		this.out.writeStartElement(localName);
		afterStartElement();
	}

	public void writeStartElement(String namespaceURI, String localName) throws XMLStreamException {
		beforeStartElement();
		this.out.writeStartElement(namespaceURI, localName);
		afterStartElement();
	}

	public void writeStartElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
		beforeStartElement();
		this.out.writeStartElement(prefix, localName, namespaceURI);
		afterStartElement();
	}

	public void writeCharacters(String text) throws XMLStreamException {
		this.out.writeCharacters(text);
		afterData();
	}

	public void writeCharacters(char[] text, int start, int len) throws XMLStreamException {
		this.out.writeCharacters(text, start, len);
		afterData();
	}

	public void writeCData(String data) throws XMLStreamException {
		this.out.writeCData(data);
		afterData();
	}

	public void writeEntityRef(String name) throws XMLStreamException {
		this.out.writeEntityRef(name);
		afterData();
	}

	public void writeEndElement() throws XMLStreamException {
		beforeEndElement();
		this.out.writeEndElement();
		afterEndElement();
	}

	public void writeEndDocument() throws XMLStreamException {
		try {
			while (this.depth > 0)
				writeEndElement();
		} catch (Exception ignored) {
		}
		this.out.writeEndDocument();
		afterEndDocument();
	}

	protected void beforeMarkup() {
		int soFar = this.stack[this.depth];
		if (((soFar & 0x2) == 0) && ((this.depth > 0) || (soFar != 0))) {
			try {
				writeNewLine(this.depth);
				if ((this.depth > 0) && (getIndent().length() > 0))
					afterMarkup();
			} catch (Exception e) {
			}
		}
	}

	protected void afterMarkup() {
		this.stack[this.depth] |= 1;
	}

	protected void afterData() {
		this.stack[this.depth] |= 2;
	}

	protected void beforeStartElement() {
		beforeMarkup();
		if (this.stack.length <= this.depth + 1) {
			int[] newStack = new int[this.stack.length * 2];
			System.arraycopy(this.stack, 0, newStack, 0, this.stack.length);
			this.stack = newStack;
		}
		this.stack[(this.depth + 1)] = 0;
	}

	protected void afterStartElement() {
		afterMarkup();
		this.depth += 1;
	}

	protected void beforeEndElement() {
		if ((this.depth > 0) && (this.stack[this.depth] == 1))
			try {
				writeNewLine(this.depth - 1);
			} catch (Exception ignored) {
			}
	}

	protected void afterEndElement() {
		if (this.depth > 0)
			this.depth -= 1;
	}

	protected void afterEndDocument() {
		if (this.stack[(this.depth = 0)] == 1)
			try {
				writeNewLine(0);
			} catch (Exception ignored) {
			}
		this.stack[this.depth] = 0;
	}

	protected void writeNewLine(int indentation) throws XMLStreamException {
		int newLineLength = getNewLine().length();
		int prefixLength = newLineLength + getIndent().length() * indentation;

		if (prefixLength > 0) {
			if (this.linePrefix == null) {
				this.linePrefix = (getNewLine() + getIndent()).toCharArray();
			}
			while (prefixLength > this.linePrefix.length) {
				char[] newPrefix = new char[newLineLength + (this.linePrefix.length - newLineLength) * 2];

				System.arraycopy(this.linePrefix, 0, newPrefix, 0, this.linePrefix.length);

				System.arraycopy(this.linePrefix, newLineLength, newPrefix, this.linePrefix.length,
						this.linePrefix.length - newLineLength);

				this.linePrefix = newPrefix;
			}
			this.out.writeCharacters(this.linePrefix, 0, prefixLength);
		}
	}

	public Object getProperty(String name) throws IllegalArgumentException {
		return this.out.getProperty(name);
	}

	public NamespaceContext getNamespaceContext() {
		return this.out.getNamespaceContext();
	}

	public void setDefaultNamespace(String uri) throws XMLStreamException {
		this.out.setDefaultNamespace(uri);
	}

	public String getPrefix(String uri) throws XMLStreamException {
		return this.out.getPrefix(uri);
	}

	public void setNamespaceContext(NamespaceContext context) throws XMLStreamException {
		this.out.setNamespaceContext(context);
	}

	public void setPrefix(String prefix, String uri) throws XMLStreamException {
		this.out.setPrefix(prefix, uri);
	}

	public void writeAttribute(String prefix, String namespaceURI, String localName, String value)
			throws XMLStreamException {
		this.out.writeAttribute(prefix, namespaceURI, localName, value);
	}

	public void writeAttribute(String namespaceURI, String localName, String value) throws XMLStreamException {
		this.out.writeAttribute(namespaceURI, localName, value);
	}

	public void writeAttribute(String localName, String value) throws XMLStreamException {
		this.out.writeAttribute(localName, value);
	}

	public void writeDefaultNamespace(String namespaceURI) throws XMLStreamException {
		this.out.writeDefaultNamespace(namespaceURI);
	}

	public void writeNamespace(String prefix, String namespaceURI) throws XMLStreamException {
		this.out.writeNamespace(prefix, namespaceURI);
	}

	public void close() throws XMLStreamException {
		this.out.close();
	}

	public void flush() throws XMLStreamException {
		this.out.flush();
	}
}