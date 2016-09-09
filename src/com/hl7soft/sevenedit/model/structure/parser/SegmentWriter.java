package com.hl7soft.sevenedit.model.structure.parser;

import java.io.StringWriter;
import java.io.Writer;

public class SegmentWriter {
	public static String writeToString(Segment segment, Delimiters delimiters) {
		StringWriter writer = new StringWriter();
		write(segment, delimiters, writer);
		return writer.getBuffer().toString();
	}

	public static void write(Segment segment, Delimiters delimiters, Writer writer) {
		if (segment == null) {
			return;
		}

		try {
			boolean msh = ("MSH".equals(segment.getName())) || ("FHS".equals(segment.getName()))
					|| ("BHS".equals(segment.getName()));
			writer.write(segment.getName());
			if (containsRealField(segment)) {
				writer.write(delimiters.getFieldDelimiter());
				writeFields(writer, segment, delimiters, 0, false, msh);
			}
			writer.flush();
		} catch (Exception e) {
			throw new RuntimeException("Error writing segment.", e);
		}
	}

	private static void writeFields(Writer writer, IFieldContainer container, Delimiters delimiters, int lev,
			boolean array, boolean msh) {
		try {
			int n = getLastRealFieldIndex(container);

			for (int i = 0; i <= n; i++) {
				Field field = container.getField(i);

				if ((!msh) || (i != 0)) {
					if (field.isArray()) {
						writeFields(writer, field, delimiters, lev, true, false);
					} else if (field.getFieldsCount() > 0) {
						writeFields(writer, field, delimiters, lev + 1, false, false);
					} else {
						String value = field.getValue();
						if (value != null) {
							writer.write(value);
						}

					}

					if (i != n)
						if (array)
							writer.write(delimiters.getRepeatDelimiter());
						else
							writer.write(getDelimiter(delimiters, lev));
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("Error writing fields.", e);
		}
	}

	private static char getDelimiter(Delimiters delimiters, int lev) throws Exception {
		switch (lev) {
		case 0:
			return delimiters.getFieldDelimiter();
		case 1:
			return delimiters.getComponentDelimiter();
		case 2:
			return delimiters.getSubcomponentDelimiter();
		}
		throw new RuntimeException("Delimiter not found, lev: " + lev);
	}

	private static int getLastRealFieldIndex(IFieldContainer container) {
		for (int i = container.getFieldsCount() - 1; i >= 0; i--) {
			if (isReal(container.getField(i))) {
				return i;
			}
		}

		return -1;
	}

	private static boolean containsRealField(IFieldContainer container) {
		int i = 0;
		for (int n = container.getFieldsCount(); i < n; i++) {
			if (isReal(container.getField(i))) {
				return true;
			}
		}

		return false;
	}

	private static boolean isReal(Field field) {
		if (field.getFieldsCount() > 0) {
			return containsRealField(field);
		}

		return field.getValue() != null;
	}
}