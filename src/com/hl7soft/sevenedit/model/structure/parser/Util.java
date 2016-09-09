package com.hl7soft.sevenedit.model.structure.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Util {
    public static List<Integer> getFieldPosition(Field field) {
	if (field == null) {
	    return null;
	}

	List res = null;
	IFieldContainer containter = null;

	Field currentField = field;

	boolean end = false;
	while (!end) {
	    containter = currentField.getParent();

	    if (!(containter instanceof Field)) {
		if (res == null) {
		    res = new ArrayList(2);
		}
		res.add(Integer.valueOf(containter.getFieldIndex(currentField)));

		break;
	    }

	    Field containerField = (Field) containter;

	    if (containerField.isArray()) {
		currentField = containerField;
	    } else {
		if (res == null) {
		    res = new ArrayList(2);
		}
		res.add(Integer.valueOf(containter.getFieldIndex(currentField)));

		currentField = containerField;
	    }
	}
	return res;
    }

    public static int getFieldLevel(Field field) {
	List res = getFieldPosition(field);
	return res != null ? res.size() : -1;
    }

    public static String getFieldPositionString(Field field) {
	List list = getFieldPosition(field);
	if ((list == null) || (list.size() == 0)) {
	    return null;
	}

	StringBuffer sb = new StringBuffer();
	Segment sgm = field.getParentSegment();
	sb.append(sgm.getName());
	sb.append("-");
	if (list != null) {
	    for (int i = list.size() - 1; i >= 0; i--) {
		sb.append(((Integer) list.get(i)).intValue() + 1);
		if (i > 0) {
		    sb.append('-');
		}
	    }
	}
	return sb.toString();
    }

    public static Segment getParentSegment(Field field) {
	if (field == null) {
	    return null;
	}

	if ((field.getParent() instanceof Segment)) {
	    return (Segment) field.getParent();
	}

	return getParentSegment((Field) field.getParent());
    }

    public static List<Object> getParents(Field field) {
	if (field == null) {
	    return null;
	}

	List res = null;

	Field curField = field;

	boolean end = false;
	while (!end) {
	    IFieldContainer container = curField.getParent();

	    if (!(container instanceof Field)) {
		break;
	    }
	    if (res == null) {
		res = new ArrayList(2);
	    }
	    res.add(container);

	    curField = (Field) container;
	}

	if (res == null) {
	    res = new ArrayList(2);
	}
	res.add(getParentSegment(field));

	if (res != null) {
	    Collections.reverse(res);
	}

	return res;
    }

    public static String getFieldPath(Field field) {
	if (field == null) {
	    return null;
	}

	List parents = getParents(field);
	if ((parents == null) || (parents.size() == 0)) {
	    return null;
	}

	List path = new ArrayList(parents);
	path.add(field);

	StringBuffer sb = new StringBuffer();

	int i = 0;
	for (int n = path.size() - 1; i < n; i++) {
	    Object el = path.get(i);

	    if ((el instanceof Segment)) {
		Segment s = (Segment) el;
		sb.append(s.getName());
		int idx = s.getFieldIndex((Field) path.get(i + 1));
		sb.append('-');
		sb.append(idx + 1);
	    } else if ((el instanceof Field)) {
		Field f = (Field) el;
		int idx = f.getFieldIndex((Field) path.get(i + 1));
		if (f.isArray()) {
		    sb.append("[").append(idx + 1).append("]");
		} else {
		    sb.append('-');
		    sb.append(idx + 1);
		}
	    }
	}

	return sb.toString();
    }

    public static Field getFieldByPosition(Segment segment, String fieldPos) {
	try {
	    if ((segment == null) || (fieldPos == null)) {
		return null;
	    }

	    if ((fieldPos == null) || (fieldPos.length() < 5) || (fieldPos.charAt(3) != '-')) {
		return null;
	    }

	    fieldPos = fieldPos.substring(4);
	    String[] tokens = explode(fieldPos, "-");

	    IFieldContainer container = segment;

	    int i = 0;
	    for (int n = tokens.length; i < n; i++) {
		String tk = tokens[i];
		Field f;
		if (tk.indexOf(91) != -1) {
		    Integer idx = Integer.valueOf(Integer.parseInt(tk.substring(0, tk.indexOf(91))) - 1);
		    Integer idx2 = Integer.valueOf(Integer.parseInt(tk.substring(tk.indexOf(91) + 1, tk.length() - 1)) - 1);

		    f = container.getField(idx.intValue());
		    if (f.isArray()) {
			f = f.getField(idx2.intValue());
		    } else if (idx2.intValue() > 0) {
			return null;
		    }

		} else {
		    Integer idx = Integer.valueOf(Integer.parseInt(tk) - 1);

		    if (idx.intValue() >= container.getFieldsCount()) {
			return null;
		    }

		    f = container.getField(idx.intValue());
		}

		container = f;
	    }

	    return (Field) container;
	} catch (Exception e) {
	}
	return null;
    }

    public static String[] explode(String str, String sep) {
	if (str == null) {
	    return null;
	}

	if ((str.length() == 0) || (sep == null) || (sep.indexOf(sep) == -1)) {
	    return new String[] { str };
	}

	int sepLen = sep.length();

	int cnt = 1;
	int idx2 = 0;
	boolean end = false;
	while (!end) {
	    idx2 = str.indexOf(sep, idx2);
	    if (idx2 == -1) {
		break;
	    }
	    idx2 += sepLen;
	    cnt++;
	}

	if (cnt == 1) {
	    return new String[] { str };
	}

	String[] res = new String[cnt];
	cnt = 0;
	idx2 = 0;
	int idx1 = 0;
	end = false;
	while (!end) {
	    idx2 = str.indexOf(sep, idx1);

	    if (idx2 != -1) {
		res[(cnt++)] = str.substring(idx1, idx2);
	    } else {
		res[(cnt++)] = str.substring(idx1);
		break;
	    }

	    idx1 = idx2 + sepLen;
	}

	return res;
    }

    public static int[] explode(String str, char character, int fromIdx, int toIdx) {
	int size = 0;
	for (int i = fromIdx; i < toIdx; i++) {
	    char c = str.charAt(i);
	    if (c == character) {
		size++;
	    }
	}
	if (size == 0) {
	    return new int[] { fromIdx, toIdx };
	}

	int[] ary = new int[2 * size + 2];
	int ptr = 0;
	for (int i = fromIdx; i < toIdx; i++) {
	    char c = str.charAt(i);
	    if (c == character) {
		if (ptr == 0) {
		    ary[(2 * ptr)] = fromIdx;
		    ary[(2 * ptr + 1)] = i;
		} else {
		    ary[(2 * ptr)] = (ary[(2 * ptr - 1)] + 1);
		    ary[(2 * ptr + 1)] = i;
		}
		ptr++;
	    }
	}
	ary[(ary.length - 2)] = (ary[(ary.length - 3)] + 1);
	ary[(ary.length - 1)] = toIdx;
	return ary;
    }

    public static int indexOf(CharSequence charSequence, char character, int from) {
	int i = from;
	for (int n = charSequence.length(); i < n; i++) {
	    if (charSequence.charAt(i) == character) {
		return i;
	    }
	}
	return -1;
    }

    public static int indexOf(CharSequence charSequence, char character, int from, int to) {
	for (int i = from; i < to; i++) {
	    if (charSequence.charAt(i) == character) {
		return i;
	    }
	}
	return -1;
    }
}