package com.hl7soft.sevenedit.db.tables.io.xml;

import com.hl7soft.sevenedit.db.tables.ITable;
import com.hl7soft.sevenedit.db.tables.ITableFactory;
import com.hl7soft.sevenedit.db.tables.Table;
import com.hl7soft.sevenedit.db.tables.TableItem;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import net.n3.nanoxml.IXMLElement;
import net.n3.nanoxml.IXMLParser;
import net.n3.nanoxml.IXMLReader;
import net.n3.nanoxml.StdXMLReader;
import net.n3.nanoxml.XMLParserFactory;

public class XMLTableFactory implements ITableFactory {
    TreeMap<Integer, ITable> tables;
    InputStream is;
    IXMLElement root;

    public XMLTableFactory(InputStream is) {
	read(is);
    }

    private void read(InputStream is) {
	this.is = is;
	this.tables = null;
	try {
	    readDom();
	    processDom();
	} catch (Exception e) {
	    throw new RuntimeException("Can't read tables.", e);
	}
    }

    private void readDom() {
	try {
	    BufferedReader bufReader = new BufferedReader(new InputStreamReader(this.is));
	    IXMLParser parser = XMLParserFactory.createDefaultXMLParser();
	    IXMLReader xmlReader = new StdXMLReader(bufReader);
	    parser.setReader(xmlReader);
	    this.root = ((IXMLElement) parser.parse());
	    bufReader.close();
	} catch (Exception e) {
	    throw new RuntimeException("Can't parse XML", e);
	}
    }

    private void processDom() {
	if (!"tables".equals(this.root.getName())) {
	    throw new RuntimeException("Bad format.");
	}

	int n = this.root.getChildrenCount();
	for (int i = 0; i < n; i++) {
	    IXMLElement tableNode = this.root.getChildAtIndex(i);
	    processTableNode(tableNode);
	}
    }

    private void processTableNode(IXMLElement tableNode) {
	if (!"table".equals(tableNode.getName())) {
	    return;
	}

	String tableNum = tableNode.getAttribute("num", null);
	String tableName = tableNode.getAttribute("name", null);
	String tableType = tableNode.getAttribute("type", null);

	int num = Integer.parseInt(tableNum);
	int type = 1;
	if ("HL7".equalsIgnoreCase(tableType)) {
	    type = 2;
	}

	Table table = new Table(num, tableName, type);
	if (this.tables == null) {
	    this.tables = new TreeMap();
	}
	this.tables.put(Integer.valueOf(num), table);

	int n = tableNode.getChildrenCount();
	for (int i = 0; i < n; i++) {
	    IXMLElement valueNode = tableNode.getChildAtIndex(i);
	    String value = valueNode.getAttribute("value", null);
	    String description = valueNode.getAttribute("description", null);

	    if (value != null)
		table.addItem(new TableItem(value, description));
	}
    }

    public ITable getTable(Integer tableNumber) {
	return this.tables != null ? (ITable) this.tables.get(tableNumber) : null;
    }

    public List<Integer> getTableNumbers() {
	return this.tables != null ? new ArrayList(this.tables.keySet()) : null;
    }

    public String getTableName(Integer num) {
	ITable t = getTable(num);
	return t != null ? t.getName() : null;
    }
}