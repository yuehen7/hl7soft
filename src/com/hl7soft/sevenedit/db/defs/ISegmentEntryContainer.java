package com.hl7soft.sevenedit.db.defs;

public abstract interface ISegmentEntryContainer {
    public abstract ISegmentEntry getSegmentEntry(int paramInt);

    public abstract int getSegmentEntriesCount();

    public abstract int getSegmentEntryIndex(ISegmentEntry paramISegmentEntry);

    public abstract void addSegmentEntry(SegmentEntry paramSegmentEntry);

    public abstract void addSegmentEntry(int paramInt, SegmentEntry paramSegmentEntry);

    public abstract void removeSegmentEntry(int paramInt);
}