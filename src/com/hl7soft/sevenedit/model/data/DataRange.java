package com.hl7soft.sevenedit.model.data;

public class DataRange implements IDataRange {
    int p0;
    int p1;

    public DataRange(int p0, int p1) {
	this.p0 = p0;
	this.p1 = p1;
    }

    public boolean contains(DataRange r) {
	if (r == null) {
	    return false;
	}

	return (getStartOffset() <= r.getStartOffset()) && (r.getEndOffset() <= getEndOffset());
    }

    public boolean contains(int p0, int p1) {
	return (getStartOffset() <= p0) && (p1 <= getEndOffset());
    }

    public boolean contains(int pos) {
	return (getStartOffset() <= pos) && (pos <= getEndOffset());
    }

    public boolean matches(DataRange r) {
	return (getStartOffset() == r.getStartOffset()) && (r.getEndOffset() == getEndOffset());
    }

    public boolean insersects(DataRange r) {
	if (r == null) {
	    return false;
	}

	return ((getStartOffset() <= r.getStartOffset()) && (r.getStartOffset() <= getEndOffset()))
		|| ((getStartOffset() <= r.getEndOffset()) && (r.getEndOffset() <= getEndOffset()));
    }

    public int getStartOffset() {
	return this.p0;
    }

    public int getEndOffset() {
	return this.p1;
    }

    public int getLength() {
	return getEndOffset() - getStartOffset();
    }

    public String toString() {
	return "DataRange (" + getStartOffset() + "," + getEndOffset() + ")";
    }

    public DataRange clone() {
	return new DataRange(this.p0, this.p1);
    }

    public int getP0() {
	return this.p0;
    }

    public int getP1() {
	return this.p1;
    }

    public void setStartOffset(int p0) {
	this.p0 = p0;
    }

    public void setEndOffset(int p1) {
	this.p1 = p1;
    }
}