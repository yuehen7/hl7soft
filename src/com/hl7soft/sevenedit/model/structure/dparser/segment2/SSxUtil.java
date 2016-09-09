package com.hl7soft.sevenedit.model.structure.dparser.segment2;

import com.hl7soft.sevenedit.model.data.DataRange;
import com.hl7soft.sevenedit.model.structure.path.IndexPath;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SSxUtil {
	public static List<Integer> getFieldPosition(ISSxField field) {
		if (field == null) {
			return null;
		}

		List res = null;
		ISSxFieldContainer containter = null;

		ISSxField currentField = field;

		boolean end = false;
		while (!end) {
			containter = currentField.getParentContainer();

			if (!(containter instanceof ISSxField)) {
				if (res == null) {
					res = new ArrayList(2);
				}
				res.add(Integer.valueOf(containter.getFieldIndex(currentField)));

				break;
			}

			ISSxField containerField = (ISSxField) containter;

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

	public static String getFieldPositionString(ISSxField field) {
		ISSxSegment segment = field.getParentSegment();

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

	public static List<ISSxStructureElement> getParents(ISSxStructureElement element) {
		if (element == null) {
			return null;
		}

		List res = null;

		ISSxStructureElement currentElement = element;
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

	public static ISSxStructureElement findElementForDataRange(ISSxStructureElement root, DataRange range) {
		return findElementForDataRange(root, range, true);
	}

	public static ISSxStructureElement findElementForDataRange(ISSxStructureElement root, DataRange range,
			boolean parse) {
		if ((root.getDataRange() == null) || (!root.getDataRange().contains(range))) {
			return null;
		}

		int i = 0;
		for (int n = root.getChildrenCount(); i < n; i++) {
			ISSxStructureElement el = root.getChildAt(i);

			ISSxStructureElement el2 = findElementForDataRange(el, range, parse);

			if (el2 != null) {
				return el2;
			}
		}

		if ((root.getDataRange() != null) && (root.getDataRange().matches(range))) {
			return root;
		}

		return null;
	}

	public static List<DataRange> scanSegmentBounds(CharSequence data) {
		return scanSegmentBounds(data, 0, data.length());
	}

	public static List<DataRange> scanSegmentBounds(CharSequence data, int p0, int p1) {
		List segmentBounds = new ArrayList(1);

		int sgmStart = p0;

		for (int i = p0; i < p1; i++) {
			char c = data.charAt(i);
			if (c == '\r') {
				segmentBounds.add(new DataRange(sgmStart, i));
				sgmStart = i + 1;
			}
		}

		if (sgmStart < p1) {
			segmentBounds.add(new DataRange(sgmStart, p1));
		}

		return segmentBounds;
	}

	public static ISSxStructureElement findDeepestElement(ISSxStructureElement root, int p0, int p1,
			boolean allowParsing) {
		int i = 0;
		for (int n = root.getChildrenCount(); i < n; i++) {
			ISSxStructureElement el = root.getChildAt(i);
			if ((el.getDataRange().getStartOffset() <= p0) && (p1 <= el.getDataRange().getEndOffset())) {
				ISSxStructureElement tmpEl = findDeepestElement(el, p0, p1, allowParsing);
				if (tmpEl != null) {
					return tmpEl;
				}
				return el;
			}
		}

		return null;
	}

	public static int getLineNumberByOffset(ISSxStructure structure, int offs) {
		try {
			if ((structure == null) || (structure.getSegmentsCount() == 0)) {
				return -1;
			}

			return structure.getSegmentIndex(structure.getSegmentByOffset(offs));
		} catch (Exception e) {
		}
		return -1;
	}

	public static int getColumnNumberByOffset(ISSxStructure structure, int offs) {
		try {
			ISSxSegment segment = structure.getSegmentByOffset(offs);
			return segment != null ? offs - segment.getDataRange().getStartOffset() : -1;
		} catch (Exception e) {
		}
		return -1;
	}

	public static List<ISSxStructureElement> getParentsList(ISSxStructureElement element) {
		if (element == null) {
			return null;
		}

		List res = null;

		ISSxStructureElement currentElement = element;
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

	public static IndexPath convertToIndexPath(ISSxStructureElement element) {
		if (element == null) {
			return null;
		}

		List parents = getParents(element);
		if (parents == null) {
			return null;
		}

		parents.add(element);

		IndexPath indexPath = new IndexPath();
		ISSxStructureElement currentElement = (ISSxStructureElement) parents.get(0);
		int i = 1;
		for (int n = parents.size(); i < n; i++) {
			ISSxStructureElement tmpElement = (ISSxStructureElement) parents.get(i);
			indexPath.addValue(currentElement.getChildIndex(tmpElement));
			currentElement = tmpElement;
		}

		return indexPath;
	}

	public static List<IndexPath> convertToIndexPaths(List<ISSxStructureElement> paths) {
		if (paths == null) {
			return null;
		}

		List res = new ArrayList(paths.size());
		int i = 0;
		for (int n = paths.size(); i < n; i++) {
			IndexPath p = convertToIndexPath((ISSxStructureElement) paths.get(i));
			if (p != null) {
				res.add(p);
			}
		}
		return res;
	}

	public static ISSxStructureElement convertToElement(ISSxStructure structure, IndexPath path) {
		if ((path == null) || (path.getSize() == 0)) {
			return null;
		}

		ISSxStructureElement currentElement = structure;
		int i = 0;
		for (int n = path.getSize(); i < n; i++) {
			int idx = path.getValue(i);
			if ((idx < 0) || (idx >= currentElement.getChildrenCount())) {
				break;
			}
			currentElement = currentElement.getChildAt(idx);
		}

		return currentElement;
	}

	public static List<ISSxStructureElement> convertToElements(ISSxStructure structure, List<IndexPath> paths) {
		if (paths == null) {
			return null;
		}

		List res = new ArrayList(paths.size());
		int i = 0;
		for (int n = paths.size(); i < n; i++) {
			ISSxStructureElement el = convertToElement(structure, (IndexPath) paths.get(i));
			if (el != null) {
				res.add(el);
			}
		}
		return res;
	}

	public static List<Integer> getFieldIndexPath(ISSxField field) {
		if (field == null) {
			return null;
		}

		List parents = getParents(field);
		if (parents == null) {
			return null;
		}

		parents.remove(field.getParentSegment().getParentStructure());

		parents.add(field);

		List res = null;

		ISSxStructureElement curEl = (ISSxStructureElement) parents.get(0);
		int i = 1;
		for (int n = parents.size(); i < n; i++) {
			ISSxStructureElement el = (ISSxStructureElement) parents.get(i);
			if (res == null) {
				res = new ArrayList();
			}
			res.add(Integer.valueOf(curEl.getChildIndex(el)));
			curEl = el;
		}

		return res;
	}

	public static ISSxField getFieldByIndexPath(ISSxSegment segment, List<Integer> path) {
		if ((path == null) || (segment == null) || (path.size() == 0)) {
			return null;
		}

		if (path.size() == 1) {
			return segment.getField(((Integer) path.get(0)).intValue());
		}

		ISSxFieldContainer container = segment;

		int i = 0;
		for (int n = path.size(); i < n; i++) {
			int idx = ((Integer) path.get(i)).intValue();
			if (container.getFieldsCount() <= idx) {
				return null;
			}

			ISSxField f = container.getField(idx);
			container = f;
		}

		return (ISSxField) container;
	}

	public static int getTableNumber(ISSxField field) {
		String cName = getComponentName(field);

		if (("HD.1".equals(cName)) || ("CWE.1".equals(cName)) || ("CWE.4".equals(cName)) || ("CWE.10".equals(cName))
				|| ("CE.1".equals(cName)) || ("CE.4".equals(cName))) {
			SSxField parentField = (SSxField) field.getParentElement();
			return parentField.getTableNumber();
		}

		return field.getTableNumber();
	}

	public static String getComponentName(ISSxField field) {
		if ((field.getParentElement() instanceof ISSxSegment)) {
			ISSxSegment parentSegment = (ISSxSegment) field.getParentElement();
			return String.format("%s.%s",
					new Object[] { parentSegment.getName(), Integer.valueOf(parentSegment.getFieldIndex(field) + 1) });
		}
		ISSxField parentField = (ISSxField) field.getParentElement();
		return String.format("%s.%s",
				new Object[] { parentField.getName(), Integer.valueOf(parentField.getFieldIndex(field) + 1) });
	}
}