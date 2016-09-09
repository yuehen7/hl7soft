package com.hl7soft.sevenedit.db.defs;

public abstract interface ISegmentEntry extends ISegmentEntryContainer {
    public static final int TYPE_SINGLE_SEGMENT = 1;
    public static final int TYPE_SEGMENT_GROUP = 2;
    public static final int TYPE_SEGMENT_CHOICE = 3;

    public abstract String getName();

    public abstract int getType();

    public abstract String getDescription();

    public abstract String getNotes();

    public abstract ISegmentEntryContainer getParentContainer();

    public abstract int getMinCount();

    public abstract int getMaxCount();
}