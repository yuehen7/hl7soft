package com.hl7soft.sevenedit.model.data;

public abstract interface IDataRange {
    public abstract int getStartOffset();

    public abstract int getEndOffset();

    public abstract int getLength();
}