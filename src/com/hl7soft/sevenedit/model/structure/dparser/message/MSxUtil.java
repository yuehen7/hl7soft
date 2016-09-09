package com.hl7soft.sevenedit.model.structure.dparser.message;

import com.hl7soft.sevenedit.db.defs.FieldDefinition;
import com.hl7soft.sevenedit.db.defs.FieldEntry;
import com.hl7soft.sevenedit.db.defs.ISegmentEntry;
import com.hl7soft.sevenedit.db.defs.MessageDefinition;
import com.hl7soft.sevenedit.db.defs.SegmentDefinition;
import com.hl7soft.sevenedit.model.data.DataRange;
import com.hl7soft.sevenedit.model.util.StringHelper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MSxUtil {
	public static List<Integer> getFieldPosition(IMSxField field) {
		if (field == null) {
			return null;
		}

		List res = null;
		IMSxFieldContainer containter = null;

		IMSxField currentField = field;

		boolean end = false;
		while (!end) {
			containter = currentField.getParentContainer();

			if (!(containter instanceof IMSxField)) {
				if (res == null) {
					res = new ArrayList(2);
				}
				res.add(Integer.valueOf(containter.getFieldIndex(currentField)));

				break;
			}

			IMSxField containerField = (IMSxField) containter;

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

	public static String getFieldPositionString(IMSxField field) {
		IMSxSegment segment = field.getParentSegment();

		StringBuffer sb = new StringBuffer();
		sb.append(segment.getName()).append("-");
		List list = getFieldPosition(field);
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

	public static List<IMSxElement> getParents(IMSxElement element) {
		if (element == null) {
			return null;
		}

		List res = null;

		IMSxElement currentElement = element;
		while ((currentElement = currentElement.getParentElement()) != null) {
			if (res == null) {
				res = new ArrayList(2);
			}
			res.add(currentElement);
		}

		if (res != null) {
			Collections.reverse(res);
		}

		return res;
	}

	public static IMSxElement findElementForDataRange(IMSxElement root, DataRange range) {
		if ((root.getDataRange() == null) || (!root.getDataRange().contains(range))) {
			return null;
		}

		int i = 0;
		for (int n = root.getChildrenCount(); i < n; i++) {
			IMSxElement el = root.getChildAt(i);

			IMSxElement el2 = findElementForDataRange(el, range);

			if (el2 != null) {
				return el2;
			}
		}

		if ((root.getDataRange() != null) && (root.getDataRange().matches(range))) {
			return root;
		}

		return null;
	}

	public static IMSxElement getElementContainingDataRange(IMSxElement root, DataRange range) {
		if ((root.getDataRange() == null) || (!root.getDataRange().contains(range))) {
			return null;
		}

		int i = 0;
		for (int n = root.getChildrenCount(); i < n; i++) {
			IMSxElement el = root.getChildAt(i);

			IMSxElement el2 = getElementContainingDataRange(el, range);

			if (el2 != null) {
				return el2;
			}
		}

		if ((root.getDataRange() != null) && (root.getDataRange().contains(range))) {
			return root;
		}

		return null;
	}

	public static IMSxElement getDeepestChild(IMSxElement root) {
		if (root.getChildrenCount() == 0) {
			return root;
		}

		return getDeepestChild(root.getChildAt(0));
	}

	public static List<IMSxSegment> appendSegments(IMSxSegmentContainer container, List<IMSxSegment> list) {
		if (container == null) {
			return list;
		}

		int i = 0;
		for (int n = container.getSegmentsCount(); i < n; i++) {
			IMSxSegment s = container.getSegment(i);

			if ((s.isGroup()) || (s.isArray())) {
				list = appendSegments(s, list);
			} else {
				if (list == null) {
					list = new ArrayList(2);
				}

				list.add(s);
			}
		}

		return list;
	}

	public static boolean isDateField(IMSxField field) {
		return checkFieldNameEquals("DT", field);
	}

	public static boolean isDelimitersField(IMSxField field) {
		try {
			IMSxSegment segment = field.getParentSegment();

			if (!"MSH".equals(segment.getName())) {
				return false;
			}

			int fieldIndex = segment.getFieldIndex(field);
			return (fieldIndex == 0) || (fieldIndex == 1);
		} catch (Exception e) {
		}
		return false;
	}

	public static boolean isTimestampField(IMSxField field) {
		try {
			if (field.getDefinition() == null) {
				return false;
			}

			if ((checkFieldNameEquals("TS", field)) || (checkFieldNameEquals("DTM", field))) {
				return true;
			}

			IMSxFieldContainer parent = (IMSxFieldContainer) field.getParentElement();
			if (((parent instanceof IMSxField)) && (checkFieldNameEquals("TS", (IMSxField) parent))) {
				return true;
			}
		} catch (Exception e) {
		}
		return false;
	}

	public static boolean isFormattedDataField(IMSxField field) {
		return checkFieldNameEquals("FT", field);
	}

	public static boolean checkFieldNameEquals(String name, IMSxField field) {
		if ((name == null) || (field.getDefinition() == null)) {
			return false;
		}

		return name.equals(field.getDefinition().getName());
	}

	public static List<Integer> getSegmentPathToMessage(IMSxSegment segment) {
		try {
			List res = null;
			IMSxSegmentContainer containter = null;
			IMSxSegment tmpSegment = segment;
			boolean end = false;

			while (!end) {
				containter = tmpSegment.getParentContainer();

				if (containter == null) {
					break;
				}

				if (((containter instanceof IMSxSegment)) && (((IMSxSegment) containter).isArray())) {
					tmpSegment = (IMSxSegment) containter;
				} else {
					if (res == null) {
						res = new ArrayList(2);
					}
					res.add(new Integer(containter.getSegmentIndex(tmpSegment)));

					if (!(containter instanceof IMSxSegment)) {
						break;
					}
					tmpSegment = (IMSxSegment) containter;
				}
			}
			Collections.reverse(res);

			return res;
		} catch (Exception e) {
			throw new RuntimeException("Error calculating segment path.", e);
		}
	}

	public static String getElementPath(IMSxElement element) {
		if (element == null) {
			return null;
		}

		if ((element instanceof IMSxMessage)) {
			return "";
		}

		if ((element instanceof IMSxSegment)) {
			IMSxSegment segment = (IMSxSegment) element;
			IMSxSegmentContainer segmentContainer = segment.getParentContainer();

			String segmentName = segment.getName();

			if (segmentName == null) {
				segmentName = "???";
			}

			if ((segment.getEntry() != null) && (segment.getEntry().getType() == 3)) {
				segmentName = "---";
			}

			if (((segmentContainer instanceof IMSxSegment)) && (((IMSxSegment) segmentContainer).isArray())) {
				IMSxSegment parentSegment = (IMSxSegment) segmentContainer;
				if (parentSegment.isArray()) {
					int idx = segmentContainer.getSegmentIndex(segment);
					return getElementPath(parentSegment) + "/" + segmentName + "[" + (idx + 1) + "]";
				}
			} else {
				int cnt = 0;
				int i = 0;
				for (int n = segmentContainer.getSegmentIndex(segment); i < n; i++) {
					if (segmentName.equals(segmentContainer.getSegment(i).getName())) {
						cnt++;
					}
				}

				StringBuffer sb = new StringBuffer();
				sb.append(getElementPath((IMSxElement) segmentContainer));
				sb.append("/");
				sb.append(segmentName);
				if (cnt > 0) {
					sb.append("[");
					sb.append(cnt + 1);
					sb.append("]");
				}

				if (segment.isArray()) {
					sb.append("*");
				}

				return sb.toString();
			}

			return getElementPath((IMSxElement) segmentContainer) + segmentName;
		}

		if ((element instanceof IMSxField)) {
			IMSxField field = (IMSxField) element;
			IMSxFieldContainer fieldContainer = field.getParentContainer();
			int idx = fieldContainer.getFieldIndex(field);

			if (field.isArray()) {
				StringBuffer sb = new StringBuffer();
				sb.append(getElementPath((IMSxElement) fieldContainer));
				sb.append("/");
				sb.append(idx + 1);
				sb.append("*");

				return sb.toString();
			}

			return getElementPath((IMSxElement) fieldContainer) + "/" + (idx + 1);
		}

		return null;
	}

	public static IMSxElement findElementByPath(IMSxMessage message, String path) {
		try {
			if ((message == null) || (path == null)) {
				return null;
			}

			if (path.length() == 0) {
				return message;
			}

			if (path.charAt(0) != '/') {
				return null;
			}

			path = path.substring(1);

			String[] tokens = StringHelper.explode(path, "/");
			if ((tokens == null) || (tokens.length == 0)) {
				return null;
			}

			IMSxElement curElement = message;
			for (int i = 0; i < tokens.length; i++) {
				String token = tokens[i];

				if (isSegmentPath(token)) {
					if (!(curElement instanceof IMSxSegmentContainer)) {
						return null;
					}

					boolean array = token.indexOf('*') != -1;
					if (array) {
						token = token.substring(0, token.length() - 1);
					}

					int idx = 0;
					String segmentName = token;

					if (containsIndex(token)) {
						idx = parseIndex(token) - 1;
						segmentName = removeIndex(token);
					}

					IMSxSegment segment = findSegment((IMSxSegmentContainer) curElement, segmentName, idx);
					curElement = segment;

					if (curElement == null) {
						return null;
					}

					if ((segment.isArray()) && (!array)) {
						curElement = segment.getSegment(0);
					} else if ((!segment.isArray()) && (array)) {
						if (i + 1 < tokens.length) {
							if (!tokens[(i + 1)].equals(segmentName + "[1]")) {
								return null;
							}

							i++;
						}
					}
				} else {
					if (!(curElement instanceof IMSxFieldContainer)) {
						return null;
					}

					boolean array = token.indexOf('*') != -1;
					if (array) {
						token = token.substring(0, token.length() - 1);
					}
					int fieldIdx = Integer.parseInt(token) - 1;
					curElement = findField((IMSxFieldContainer) curElement, fieldIdx);

					if (curElement == null) {
						return null;
					}

					IMSxField field = (IMSxField) curElement;
					if ((field.isArray()) && (!array)) {
						curElement = field.getField(0);
					} else if ((!field.isArray()) && (array)) {
						if (i + 1 < tokens.length) {
							if (!tokens[(i + 1)].equals("1")) {
								return null;
							}

							i++;
						}
					}
				}
			}

			return curElement;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private static IMSxSegment findSegment(IMSxSegmentContainer container, String name, int idx) {
		int cnt = 0;

		int j = 0;
		for (int n2 = container.getSegmentsCount(); j < n2; j++) {
			IMSxSegment segment = container.getSegment(j);
			String segmentName = segment.getName();

			if (segmentName == null) {
				segmentName = "???";
			}

			if ((segment.getEntry() != null) && (segment.getEntry().getType() == 3)) {
				segmentName = "---";
			}

			if (name.equals(segmentName)) {
				if (idx == cnt) {
					return segment;
				}
				cnt++;
			}
		}

		return null;
	}

	private static IMSxField findField(IMSxFieldContainer container, int idx) {
		if ((idx < 0) || (idx > container.getFieldsCount() - 1)) {
			return null;
		}

		return container.getField(idx);
	}

	private static boolean containsIndex(String name) {
		return name.indexOf("[") != -1;
	}

	private static boolean isSegmentPath(String name) {
		return !Character.isDigit(name.charAt(0));
	}

	private static int parseIndex(String name) {
		int idx = name.indexOf("[");
		int idx2 = name.indexOf("]");
		String idxStr = name.substring(idx + 1, idx2);
		return Integer.parseInt(idxStr);
	}

	private static String removeIndex(String name) {
		int idx = name.indexOf("[");
		return name.substring(0, idx);
	}

	public static int getTableNumber(IMSxField field) {
		if (field == null) {
			return 0;
		}

		String parentFieldName = getParentFieldName(field);

		if ("HD".equals(parentFieldName)) {
			MSxField parentField = (MSxField) field.getParentElement();

			if (parentField.getFieldsCount() != 3) {
				return -1;
			}

			if ((parentField.getFieldIndex(field) == 0) && (field.isReal()) && (!parentField.getField(1).isReal())
					&& (!parentField.getField(2).isReal())) {
				return parentField.getTableNumber();
			}
			return field.getTableNumber();
		}

		if ("CWE".equals(parentFieldName)) {
			MSxField parentField = (MSxField) field.getParentElement();
			if ((parentField.getFieldIndex(field) == 0) || (parentField.getFieldIndex(field) == 3)
					|| (parentField.getFieldIndex(field) == 9)) {
				return parentField.getTableNumber();
			}
		} else if ("CE".equals(parentFieldName)) {
			MSxField parentField = (MSxField) field.getParentElement();
			if ((parentField.getFieldIndex(field) == 0) || (parentField.getFieldIndex(field) == 3)) {
				return parentField.getTableNumber();
			}
		}

		return field.getTableNumber();
	}

	public static String getParentFieldName(IMSxField field) {
		if (!(field.getParentElement() instanceof MSxField)) {
			return null;
		}

		MSxField parentField = (MSxField) field.getParentElement();
		if (parentField.getEntry() == null) {
			return null;
		}

		return parentField.getName();
	}

	public static String getComponentName(IMSxField field) {
		if ((field.getParentElement() instanceof IMSxSegment)) {
			IMSxSegment parentSegment = (IMSxSegment) field.getParentElement();
			return String.format("%s-%s",
					new Object[] { parentSegment.getName(), Integer.valueOf(parentSegment.getFieldIndex(field) + 1) });
		}
		IMSxField parentField = (IMSxField) field.getParentElement();
		return String.format("%s-%s",
				new Object[] { parentField.getName(), Integer.valueOf(parentField.getFieldIndex(field) + 1) });
	}

	public static String getMessageDescription(IMSxMessage message) {
		MessageDefinition definition = message.getDefinition();
		return definition != null ? definition.getDescription() : "Undefined Message";
	}

	public static String getSegmentDescription(IMSxSegment segment) {
		ISegmentEntry segmentEntry = segment.getEntry();
		if ((segmentEntry != null) && (segmentEntry.getDescription() != null)) {
			return segmentEntry.getDescription();
		}

		SegmentDefinition definition = segment.getDefinition();
		if ((definition != null) && (definition.getDescription() != null)) {
			return definition.getDescription();
		}

		return "Undefined Segment";
	}

	public static String getFieldDescription(IMSxField field) {
		FieldEntry fieldEntry = field.getEntry();
		if ((fieldEntry != null) && (fieldEntry.getDescription() != null)) {
			return fieldEntry.getDescription();
		}

		FieldDefinition definition = field.getDefinition();
		if ((definition != null) && (definition.getDescription() != null)) {
			return definition.getDescription();
		}

		return "Undefined Field";
	}
}