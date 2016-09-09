package com.hl7soft.sevenedit.model.util;

import java.awt.Color;
import java.util.List;

public class StringHelper {
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

	public static String padString(String str, int padLen, char padChar, boolean leftJustified) {
		if (str == null) {
			str = "";
		}
		int strLen = str.length();
		if (strLen == padLen)
			return str;
		if (strLen > padLen) {
			return str.substring(0, padLen);
		}
		StringBuffer sb = new StringBuffer();
		if (!leftJustified) {
			for (int i = 0; i < padLen - strLen; i++) {
				sb.append(padChar);
			}
			for (int i = 0; i < strLen; i++)
				sb.append(str.charAt(i));
		} else {
			for (int i = 0; i < strLen; i++) {
				sb.append(str.charAt(i));
			}
			for (int i = 0; i < padLen - strLen; i++) {
				sb.append(padChar);
			}
		}

		return sb.toString();
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

	public static int[] explode(CharSequence charSequence, char character, int fromIdx, int toIdx) {
		int size = 0;
		for (int i = fromIdx; i < toIdx; i++) {
			char c = charSequence.charAt(i);
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
			char c = charSequence.charAt(i);
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

	public static String[] explode(String str, char sep) {
		if (str == null) {
			return null;
		}

		int lineLen = str.length();
		if (lineLen == 0) {
			return new String[] { str };
		}

		int cnt = 1;
		for (int i = 0; i < lineLen; i++) {
			char c = str.charAt(i);
			if (c == sep) {
				cnt++;
			}

		}

		if (cnt == 1) {
			return new String[] { str };
		}

		StringBuffer sb = new StringBuffer();
		String[] res = new String[cnt];
		cnt = 0;
		for (int i = 0; i < lineLen; i++) {
			char c = str.charAt(i);
			if (c == sep) {
				res[(cnt++)] = sb.toString();
				sb = new StringBuffer();
			} else {
				sb.append(c);
			}
		}
		res[cnt] = sb.toString();
		return res;
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

	public static String[] explodeCsv(String line) {
		if ((line == null) || (line.trim().length() == 0)) {
			return null;
		}

		char sep = ';';
		int lineLen = line.length();
		if (lineLen == 0) {
			return new String[] { line };
		}

		boolean skipSep = false;
		int cnt = 1;
		for (int i = 0; i < lineLen; i++) {
			char c = line.charAt(i);
			if (c == '"') {
				skipSep = !skipSep;
			}
			if ((c == sep) && (!skipSep)) {
				cnt++;
			}

		}

		if (cnt == 1) {
			return new String[] { line };
		}

		StringBuffer sb = new StringBuffer();
		String[] res = new String[cnt];
		cnt = 0;
		skipSep = false;
		for (int i = 0; i < lineLen; i++) {
			char c = line.charAt(i);
			if (c == '"') {
				skipSep = !skipSep;
			}
			if ((c == sep) && (!skipSep)) {
				res[(cnt++)] = sb.toString();
				sb = new StringBuffer();
			} else {
				sb.append(c);
			}
		}
		res[cnt] = sb.toString();

		String token = null;
		for (int i = 0; i < res.length; i++) {
			token = res[i];
			if ((token.length() > 0) && (token.charAt(0) == '"')) {
				token = token.substring(1, token.length() - 1);
			}
			res[i] = token;
		}

		return res;
	}

	public static String implodeCsv(String[] tokens) {
		StringBuffer sb = new StringBuffer(tokens.length);

		for (int i = 0; i < tokens.length; i++) {
			if ((tokens[i] != null) && (tokens[i].indexOf(";") != -1)) {
				sb.append('"');
				sb.append(tokens[i]);
				sb.append('"');
			} else {
				sb.append(tokens[i]);
			}
			if (i != tokens.length - 1) {
				sb.append(';');
			}
		}
		return sb.toString();
	}

	public static String implode(List<String> tokens, String sep) {
		if (tokens.size() == 0) {
			return "";
		}

		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < tokens.size(); i++) {
			sb.append(nullToEmpty((String) tokens.get(i)));
			if (i != tokens.size() - 1) {
				sb.append(sep);
			}
		}
		return sb.toString();
	}

	public static String implode(String[] tokens, String sep) {
		if (tokens == null) {
			return null;
		}

		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < tokens.length; i++) {
			sb.append(nullToEmpty(tokens[i]));
			if (i != tokens.length - 1) {
				sb.append(sep);
			}
		}
		return sb.toString();
	}

	public static String toHexString(int value) {
		return "<0x" + Integer.toHexString(value).toUpperCase() + ">";
	}

	public static String trim(String str) {
		if (str == null) {
			return null;
		}

		return str.trim();
	}

	public static String nullToEmpty(String str) {
		if (str == null) {
			return "";
		}
		return str;
	}

	public static String getSequence(char c, int n) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < n; i++) {
			sb.append(c);
		}
		return sb.toString();
	}

	public static int getLevenshteinDistance(String s, String t) {
		if ((s == null) || (t == null)) {
			throw new IllegalArgumentException("Strings must not be null");
		}

		int n = s.length();
		int m = t.length();

		if (n == 0)
			return m;
		if (m == 0) {
			return n;
		}

		int[] p = new int[n + 1];
		int[] d = new int[n + 1];

		for (int i = 0; i <= n; i++) {
			p[i] = i;
		}

		for (int j = 1; j <= m; j++) {
			char t_j = t.charAt(j - 1);
			d[0] = j;

			for (int i = 1; i <= n; i++) {
				int cost = s.charAt(i - 1) == t_j ? 0 : 1;

				d[i] = Math.min(Math.min(d[(i - 1)] + 1, p[i] + 1), p[(i - 1)] + cost);
			}

			int[] _d = p;
			p = d;
			d = _d;
		}

		return p[n];
	}

	public static String replaceAll(String str, String from, String to) {
		return replaceAll(str, from, to, true);
	}

	public static String replaceAll(String str, String from, String to, boolean caseSensitive) {
		if (from.equals("")) {
			throw new IllegalArgumentException("Old pattern must have content.");
		}

		StringBuffer result = new StringBuffer();

		String tmpStr = str;
		if (!caseSensitive) {
			tmpStr = str.toLowerCase();
			from = from.toLowerCase();
		}

		int startIdx = 0;
		int idxOld = 0;
		while ((idxOld = tmpStr.indexOf(from, startIdx)) >= 0) {
			result.append(str.substring(startIdx, idxOld));
			result.append(to);
			startIdx = idxOld + from.length();
		}

		result.append(str.substring(startIdx));
		return result.toString();
	}

	public static String replaceAll(String str, char from, char to) {
		StringBuffer sb = new StringBuffer();

		int i = 0;
		for (int n = str.length(); i < n; i++) {
			char ch = str.charAt(i);
			if (ch == from)
				sb.append(to);
			else {
				sb.append(ch);
			}
		}

		return sb.toString();
	}

	public static boolean isEmpty(String str) {
		return (str == null) || (str.length() == 0);
	}

	public static String escapeHTML(String string) {
		if (string == null) {
			return null;
		}

		StringBuffer sb = new StringBuffer(string.length());

		boolean lastWasBlankChar = false;
		int len = string.length();

		for (int i = 0; i < len; i++) {
			char c = string.charAt(i);
			if (c == ' ') {
				if (lastWasBlankChar) {
					lastWasBlankChar = false;

					sb.append(' ');
				} else {
					lastWasBlankChar = true;
					sb.append(' ');
				}
			} else {
				lastWasBlankChar = false;

				if (c == '"') {
					sb.append("&quot;");
				} else if (c == '&') {
					sb.append("&amp;");
				} else if (c == '<') {
					sb.append("&lt;");
				} else if (c == '>') {
					sb.append("&gt;");
				} else if (c == '\n') {
					sb.append("<br>");
				} else {
					int ci = 0xFFFF & c;
					if (ci < 160) {
						sb.append(c);
					} else {
						sb.append("&#");
						sb.append(new Integer(ci).toString());
						sb.append(';');
					}
				}
			}
		}
		return sb.toString();
	}

	public static String null2empty(String str) {
		if (str == null) {
			return "";
		}

		return str;
	}

	public static String empty2str(String str, String str2) {
		if ((str == null) || (str.isEmpty())) {
			return str2;
		}

		return str;
	}

	public static boolean compare(String s1, String s2, boolean caseSensitive) {
		if ((s1 == null) && (s2 == null)) {
			return true;
		}

		if (s1 == null) {
			return false;
		}

		return caseSensitive ? s1.equals(s2) : s1.equalsIgnoreCase(s2);
	}

	public static String errorToString(Throwable ex) {
		return errorToString(ex, "\n");
	}

	public static String errorToString(Throwable ex, String delim) {
		if (ex == null) {
			return null;
		}

		StringBuffer sb = new StringBuffer();
		sb.append(ex.getMessage());

		Throwable cause = ex;

		boolean end = false;
		while (!end) {
			Throwable tc = cause.getCause();

			if ((tc == null) || (tc == cause)) {
				break;
			}
			sb.append(delim).append(tc.getMessage());
			cause = tc;
		}

		return sb.toString();
	}

	public static String escapeStyled(String str) {
		if (str == null) {
			return null;
		}

		StringBuffer sb = new StringBuffer();

		int i = 0;
		for (int n = str.length(); i < n; i++) {
			char ch = str.charAt(i);
			if ((ch == '{') || (ch == '}') || (ch == '(') || (ch == ')') || (ch == '#') || (ch == ':') || (ch == ',')
					|| (ch == '\\')) {
				sb.append('\\');
			}
			sb.append(ch);
		}

		return sb.toString();
	}

	public static String toHex(int value) {
		String s = Integer.toHexString(value);
		if (s.length() == 1) {
			s = "0" + s;
		}
		return s;
	}

	public static String colorToHex(Color color) {
		return toHex(color.getRed()) + toHex(color.getGreen()) + toHex(color.getBlue());
	}
}