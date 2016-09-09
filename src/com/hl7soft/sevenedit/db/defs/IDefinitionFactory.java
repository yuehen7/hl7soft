package com.hl7soft.sevenedit.db.defs;

import java.util.List;

public abstract interface IDefinitionFactory {
	public abstract List<String> getVersions();

	public abstract List<String> getMessages(String paramString);

	public abstract List<String> getSegments(String paramString);

	public abstract List<String> getFields(String paramString);

	public abstract String getMessageDescription(String paramString1, String paramString2);

	public abstract String getSegmentDescription(String paramString1, String paramString2);

	public abstract String getFieldDescription(String paramString1, String paramString2);

	public abstract MessageDefinition getMessageDefinition(String paramString1, String paramString2);

	public abstract SegmentDefinition getSegmentDefinition(String paramString1, String paramString2);

	public abstract FieldDefinition getFieldDefinition(String paramString1, String paramString2);
}