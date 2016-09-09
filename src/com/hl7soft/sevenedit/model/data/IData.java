package com.hl7soft.sevenedit.model.data;

public abstract interface IData {
	public abstract DataChunk getDataChunk(int paramInt1, int paramInt2, DataChunk paramDataChunk);

	public abstract String getString(int paramInt1, int paramInt2);

	public abstract int getLength();

	public abstract void insert(int paramInt, String paramString);

	public abstract void remove(int paramInt1, int paramInt2);

	public abstract char charAt(int paramInt);
}