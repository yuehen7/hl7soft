package com.hl7soft.sevenedit.model.structure.dparser.message;

import com.hl7soft.sevenedit.db.defs.FieldDefinition;
import com.hl7soft.sevenedit.db.defs.FieldEntry;

public abstract interface IMSxField extends IMSxElement, IMSxFieldContainer {
    public abstract FieldDefinition getDefinition();

    public abstract FieldEntry getEntry();

    public abstract IMSxSegment getParentSegment();

    public abstract int getLevel();

    public abstract boolean isArray();

    public abstract boolean isPrimitive();

    public abstract IMSxFieldContainer getParentContainer();

    public abstract int getTableNumber();

    public abstract String getName();
}