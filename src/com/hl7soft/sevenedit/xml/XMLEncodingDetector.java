package com.hl7soft.sevenedit.xml;

import java.nio.charset.Charset;

public class XMLEncodingDetector {
	public static String detect(byte[] data, String defaultEncoding) {
		String enc = detect(data);
		return enc != null ? enc : defaultEncoding;
	}

	public static String detect(byte[] data) {
		if ((data == null) || (data.length == 0)) {
			return null;
		}

		if ((data[0] == -17) && (data[1] == -69) && (data[2] == -65)) {
			return "UTF-8";
		}

		if ((data[0] == 0) && (data[1] == 0) && (data[2] == -2) && (data[3] == -1)) {
			return "UTF-32";
		}
		if ((data[0] == -1) && (data[1] == -2) && (data[2] == 0) && (data[3] == 0)) {
			return "UTF-32";
		}

		if ((data[0] == -2) && (data[1] == -1)) {
			return "UTF-16";
		}
		if ((data[0] == -1) && (data[1] == -2)) {
			return "UTF-16";
		}

		String tmpData = new String(data, Charset.forName("US-ASCII"));

		String encValue = extractEncodingValue(tmpData);
		if (encValue == null) {
			return null;
		}

		if ("ISO-8859-1".equalsIgnoreCase(encValue)) {
			return "ISO-8859-1";
		}
		if ("ISO-8859-2".equalsIgnoreCase(encValue)) {
			return "ISO-8859-2";
		}
		if ("ISO-8859-3".equalsIgnoreCase(encValue)) {
			return "ISO-8859-3";
		}
		if ("ISO-8859-4".equalsIgnoreCase(encValue)) {
			return "ISO-8859-4";
		}
		if ("ISO-8859-5".equalsIgnoreCase(encValue)) {
			return "ISO-8859-5";
		}
		if ("ISO-8859-6".equalsIgnoreCase(encValue)) {
			return "ISO-8859-6";
		}
		if ("ISO-8859-7".equalsIgnoreCase(encValue)) {
			return "ISO-8859-7";
		}
		if ("ISO-8859-8".equalsIgnoreCase(encValue)) {
			return "ISO-8859-8";
		}
		if ("ISO-8859-9".equalsIgnoreCase(encValue)) {
			return "ISO-8859-9";
		}
		if ("ISO-8859-13".equalsIgnoreCase(encValue)) {
			return "ISO-8859-13";
		}
		if ("ISO-8859-15".equalsIgnoreCase(encValue)) {
			return "ISO-8859-15";
		}
		if ("US-ASCII".equalsIgnoreCase(encValue)) {
			return "US-ASCII";
		}
		if (("windows-1250".equalsIgnoreCase(encValue)) || ("win-1250".equalsIgnoreCase(encValue))) {
			return "windows-1250";
		}
		if (("windows-1251".equalsIgnoreCase(encValue)) || ("win-1251".equalsIgnoreCase(encValue))) {
			return "windows-1251";
		}
		if (("windows-1252".equalsIgnoreCase(encValue)) || ("win-1252".equalsIgnoreCase(encValue))) {
			return "windows-1252";
		}
		if (("windows-1253".equalsIgnoreCase(encValue)) || ("win-1253".equalsIgnoreCase(encValue))) {
			return "windows-1253";
		}
		if (("windows-1254".equalsIgnoreCase(encValue)) || ("win-1254".equalsIgnoreCase(encValue))) {
			return "windows-1254";
		}
		if (("windows-1255".equalsIgnoreCase(encValue)) || ("win-1255".equalsIgnoreCase(encValue))) {
			return "windows-1255";
		}
		if (("windows-1256".equalsIgnoreCase(encValue)) || ("win-1256".equalsIgnoreCase(encValue))) {
			return "windows-1256";
		}
		if (("windows-1257".equalsIgnoreCase(encValue)) || ("win-1257".equalsIgnoreCase(encValue))) {
			return "windows-1257";
		}
		if (("windows-1258".equalsIgnoreCase(encValue)) || ("win-1258".equalsIgnoreCase(encValue))) {
			return "windows-1258";
		}
		if ("Shift_JIS".equalsIgnoreCase(encValue)) {
			return "Shift_JIS";
		}
		if ("EUC-JP".equalsIgnoreCase(encValue)) {
			return "EUC-JP";
		}
		if ("ISO-2022-JP".equalsIgnoreCase(encValue)) {
			return "ISO-2022-JP";
		}
		if ("GB2312".equalsIgnoreCase(encValue)) {
			return "GB2312";
		}
		if ("EUC-KR".equalsIgnoreCase(encValue)) {
			return "EUC-KR";
		}
		if ("Big5".equalsIgnoreCase(encValue)) {
			return "Big5";
		}
		if ("UTF-8".equalsIgnoreCase(encValue)) {
			return "UTF-8";
		}
		if ("UTF-16".equalsIgnoreCase(encValue)) {
			return "UTF-16";
		}
		if ("UTF-32".equalsIgnoreCase(encValue)) {
			return "UTF-32";
		}

		return null;
	}

	private static String extractEncodingValue(String xml) {
		int idx = xml.indexOf("<?");
		if (idx == -1) {
			return null;
		}

		int idx2 = xml.indexOf("?>", idx);
		if (idx == -1) {
			return null;
		}

		String xmlHeader = xml.substring(idx, idx2 + 2);

		idx = xmlHeader.indexOf("encoding=");
		if (idx == -1) {
			return null;
		}

		if (xml.charAt(idx + 9) == '"') {
			idx2 = xml.indexOf(34, idx + 10);
			if (idx2 == -1) {
				return null;
			}

			return xml.substring(idx + 10, idx2);
		}

		idx2 = xml.indexOf(32, idx + 10);
		if (idx2 == -1) {
			idx2 = xml.indexOf(63, idx + 10);
			if (idx2 == -1) {
				return null;
			}
		}

		return xml.substring(idx + 9, idx2);
	}
}