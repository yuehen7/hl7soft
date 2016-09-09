package com.hl7soft.sevenedit.model.util;

import com.hl7soft.sevenedit.db.defs.FieldDefinition;
import com.hl7soft.sevenedit.model.structure.dparser.segment2.ISSxField;
import com.hl7soft.sevenedit.model.structure.dparser.segment2.ISSxFieldContainer;
import com.hl7soft.sevenedit.model.structure.dparser.segment2.ISSxSegment;
import java.util.Date;

public class DataHelper {
	private static final String[] MONTH_NAME = { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct",
			"Nov", "Dec" };

	public static boolean isDelimiterContainerSegment(String segmentName) {
		return ("MSH".equals(segmentName)) || ("BHS".equals(segmentName)) || ("FHS".equals(segmentName));
	}

	public static String dateToTs(Date date) {
		if (date == null) {
			return null;
		}

		String dateStr = StringHelper.padString(new StringBuilder().append("").append(date.getYear() + 1900).toString(),
				4, '0', false)
				+ StringHelper.padString(new StringBuilder().append("").append(date.getMonth() + 1).toString(), 2, '0',
						false)
				+ StringHelper.padString(new StringBuilder().append("").append(date.getDate()).toString(), 2, '0',
						false);
		String timeStr = StringHelper.padString(new StringBuilder().append("").append(date.getHours()).toString(), 2,
				'0', false)
				+ StringHelper.padString(new StringBuilder().append("").append(date.getMinutes()).toString(), 2, '0',
						false)
				+ StringHelper.padString(new StringBuilder().append("").append(date.getSeconds()).toString(), 2, '0',
						false);
		String res = dateStr + timeStr;
		return res;
	}

	public static String dateToDt(Date date) {
		if (date == null) {
			return null;
		}

		String dateStr = StringHelper.padString(new StringBuilder().append("").append(date.getYear() + 1900).toString(),
				4, '0', false)
				+ StringHelper.padString(new StringBuilder().append("").append(date.getMonth() + 1).toString(), 2, '0',
						false)
				+ StringHelper.padString(new StringBuilder().append("").append(date.getDate()).toString(), 2, '0',
						false);
		return dateStr;
	}

	public static String getMessageName(String type, String event) {
		String messageName = type;

		if ((event != null) && (event.trim().length() != 0)) {
			messageName = messageName + "_" + event;
		}
		return messageName;
	}

	public static String getMessageType(String msgName) {
		if (msgName == null) {
			return null;
		}

		String[] ary = StringHelper.explode(msgName, '_');
		return ary[0];
	}

	public static String getMessageEvent(String msgName) {
		if (msgName == null) {
			return null;
		}

		String[] ary = StringHelper.explode(msgName, '_');
		if (ary.length > 1) {
			return ary[1];
		}
		return null;
	}

	public static Date tsToDate(String value) {
		try {
			if ((value == null) || (value.length() == 0)) {
				return null;
			}

			String year = value.substring(0, 4);
			String month = null;
			if (value.length() > 4) {
				month = value.substring(4, 6);
			}
			String day = null;
			if (value.length() > 6) {
				day = value.substring(6, 8);
			}
			String hh = null;
			if (value.length() > 8) {
				hh = value.substring(8, 10);
			}
			String mm = null;
			if (value.length() > 10) {
				mm = value.substring(10, 12);
			}
			String ss = null;
			if (value.length() > 12) {
				ss = value.substring(12, 14);
			}

			int iyear = 0;
			int imonth = 0;
			int iday = 0;
			int ihr = 0;
			int imin = 0;
			int isec = 0;

			iyear = Integer.parseInt(year);
			if (month != null) {
				imonth = Integer.parseInt(month) - 1;
			}
			if (day != null) {
				iday = Integer.parseInt(day);
			}
			if (hh != null) {
				ihr = Integer.parseInt(hh);
			}
			if (mm != null) {
				imin = Integer.parseInt(mm);
			}
			if (ss != null) {
				isec = Integer.parseInt(ss);
			}

			return new Date(iyear - 1900, imonth, iday, ihr, imin, isec);
		} catch (Exception e) {
		}
		return null;
	}

	public static Date dtToDate(String value) {
		try {
			if ((value == null) || (value.length() == 0)) {
				return null;
			}

			String year = value.substring(0, 4);
			String month = null;
			if (value.length() > 4) {
				month = value.substring(4, 6);
			}
			String day = null;
			if (value.length() > 6) {
				day = value.substring(6, 8);
			}

			int iyear = 0;
			int imonth = 0;
			int iday = 0;

			iyear = Integer.parseInt(year);
			if (month != null) {
				imonth = Integer.parseInt(month) - 1;
			}
			if (day != null) {
				iday = Integer.parseInt(day);
			}

			return new Date(iyear - 1900, imonth, iday);
		} catch (Exception e) {
		}
		return null;
	}

	public static String convertDtToString(String value) {
		if ((value == null) || (value.length() == 0)) {
			return "";
		}

		try {
			String year = value.substring(0, 4);
			String month = null;
			if (value.length() > 4) {
				month = value.substring(4, 6);
			}
			String day = null;
			if (value.length() > 6) {
				day = value.substring(6, 8);
			}

			String res = "";
			if (day != null) {
				res = res + day;
			}
			if (month != null) {
				int monthIdx = Integer.parseInt(month) - 1;
				res = res + " " + MONTH_NAME[monthIdx];
			}
			if (res.length() > 0) {
				res = res + " ";
			}
			return res + year;
		} catch (Exception e) {
		}
		return null;
	}

	public static String convertTsToString(String value) {
		if ((value == null) || (value.length() == 0)) {
			return "";
		}

		try {
			String year = value.substring(0, 4);
			String month = null;
			if (value.length() > 4) {
				month = value.substring(4, 6);
			}
			String day = null;
			if (value.length() > 6) {
				day = value.substring(6, 8);
			}
			String hh = null;
			if (value.length() > 8) {
				hh = value.substring(8, 10);
			}
			String mm = null;
			if (value.length() > 10) {
				mm = value.substring(10, 12);
			}
			String ss = null;
			if (value.length() > 12) {
				ss = value.substring(12, 14);
			}

			String res = "";
			if (day != null) {
				res = res + day;
			}
			if (month != null) {
				int monthIdx = Integer.parseInt(month) - 1;
				res = res + " " + MONTH_NAME[monthIdx];
			}
			if (res.length() > 0) {
				res = res + " ";
			}
			res = res + year;

			if (hh != null) {
				res = res + " " + hh;
			}
			if (mm != null) {
				res = res + ":" + mm;
			}
			if (ss != null)
				;
			return res + ":" + ss;
		} catch (Exception e) {
		}

		return null;
	}

	public static boolean isDateField(ISSxField field) {
		return checkFieldNameEquals("DT", field);
	}

	public static boolean isDelimitersField(ISSxField field) {
		try {
			ISSxSegment segment = field.getParentSegment();

			if (!"MSH".equals(segment.getName())) {
				return false;
			}

			int fieldIndex = segment.getFieldIndex(field);
			return (fieldIndex == 0) || (fieldIndex == 1);
		} catch (Exception e) {
		}
		return false;
	}

	public static boolean isTimestampField(ISSxField field) {
		try {
			if (field.getDefinition() == null) {
				return false;
			}

			if ((checkFieldNameEquals("TS", field)) || (checkFieldNameEquals("DTM", field))) {
				return true;
			}

			ISSxFieldContainer parent = (ISSxFieldContainer) field.getParentElement();
			if (((parent instanceof ISSxField)) && (checkFieldNameEquals("TS", (ISSxField) parent))) {
				return true;
			}
		} catch (Exception e) {
		}
		return false;
	}

	public static boolean isFormattedDataField(ISSxField field) {
		return checkFieldNameEquals("FT", field);
	}

	public static boolean checkFieldNameEquals(String name, ISSxField field) {
		if ((name == null) || (field.getDefinition() == null)) {
			return false;
		}

		return name.equals(field.getDefinition().getName());
	}

	public static boolean isValidSegmentName(String name) {
		if ((name == null) || (name.length() != 3)) {
			return false;
		}

		int i = 0;
		for (int n = name.length(); i < n; i++) {
			int ch = name.charAt(i);
			if (((!Character.isLetter(ch)) || (!Character.isUpperCase(ch))) && (!Character.isDigit(ch))) {
				return false;
			}
		}

		return true;
	}

	public static boolean isValidSegmentGroupName(String name) {
		if (name == null) {
			return false;
		}

		int i = 0;
		for (int n = name.length(); i < n; i++) {
			int ch = name.charAt(i);
			if (((!Character.isLetter(ch)) || (!Character.isUpperCase(ch))) && (!Character.isDigit(ch)) && (ch != 95)) {
				return false;
			}
		}

		return true;
	}
}