package com.hl7soft.sevenedit.model.structure.dparser.segment2;

import com.hl7soft.sevenedit.db.defs.FieldDefinition;
import com.hl7soft.sevenedit.db.defs.FieldEntry;

public abstract interface ISSxField extends ISSxFieldContainer, ISSxStructureElement {
    public abstract FieldDefinition getDefinition();

    public abstract FieldEntry getEntry();

    public abstract ISSxSegment getParentSegment();

    public abstract int getLevel();

    public abstract boolean isArray();

    public abstract boolean isPrimitive();

    public abstract boolean isReal();

    public abstract ISSxFieldContainer getParentContainer();

    public abstract String getName();

    public abstract int getTableNumber();
}