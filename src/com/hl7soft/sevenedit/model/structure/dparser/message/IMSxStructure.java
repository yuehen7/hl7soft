package com.hl7soft.sevenedit.model.structure.dparser.message;

import com.hl7soft.sevenedit.db.defs.IDefinitionFactory;
import com.hl7soft.sevenedit.model.data.IData;

public abstract interface IMSxStructure extends IMSxElement {
	public abstract IData getData();

	public abstract IDefinitionFactory getDefinitionFactory();

	public abstract IMSxMessage getMessage();

	public abstract void parse();
}