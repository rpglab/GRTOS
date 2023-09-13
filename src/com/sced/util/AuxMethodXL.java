package com.sced.util;

import java.util.ArrayList;

/**
 * 
 * @author Xingpeng.Li (xplipower@gmail.com)
 *
 */
public class AuxMethodXL {
	
	/** Single quote pairs are of higher priority.  */
	public static String[] getTokensByComma(String str) {return getTokens(str, ',');}
	/** Single quote pairs are of higher priority.  */
	public static String[] getTokens(String str, char delim)
	{
		ArrayList<String> strArray = new ArrayList<String>();
		int idx;
		
		// remove comments
		while ((idx = str.indexOf("/*")) != -1)
		{
			int idx2 = str.indexOf("*/");
			str = str.substring(0, idx) + str.substring(idx2+2);
		}
		idx = str.indexOf("//");
		if (idx != -1) str = str.substring(0, idx);
		
		// analyze the string with single quote and comma.
		String strTmp = "";
		int i = 0;
		for (; i<str.length(); i++)
		{
			char tmp = str.charAt(i);
			if (tmp == '\'')
			{
				// nothing except for blank is between a comma and the first single quote. 
				if (isNoInfo(strTmp) == false) {System.err.println("only blank may exist between a comma and the first single quote of a pair."); System.exit(1);}
				else strTmp = "";
				strTmp += tmp;
				int ndxSingleQuote = str.indexOf("'");
				str = str.substring(ndxSingleQuote+1);
				ndxSingleQuote = str.indexOf("'");
				strTmp += str.substring(0, ndxSingleQuote+1);
				strArray.add(strTmp);
				str = str.substring(ndxSingleQuote+1);
				int ndxComma = str.indexOf(",");
				if (ndxComma != -1) {
					if (isNoInfo(str.substring(0, ndxComma)) == false) {System.err.println("only blank may exist between the second single quote of a pair and  a comma."); System.exit(1);}
					str = str.substring(ndxComma+1);
				}
				strTmp = ""; i=-1; continue;
			}
			if (tmp != delim) strTmp += tmp;
			else {strArray.add(strTmp); str = str.substring(i+1); strTmp = ""; i=-1; continue;}
		}
		if (i == str.length() && i != 0) {strArray.add(strTmp); str = null;}

		return convtArrayListToStr(strArray);
	}
		
	
	public static String[] convtArrayListToStr(ArrayList<String> arrayList)
	{
		int size = arrayList.size();
		if (size == 0) return null;
		String[] strArray = new String[size];
		for (int i=0; i<size; i++)
			strArray[i] = arrayList.get(i);
		return strArray;
	}

	public static int[] convtArrayListToInt(ArrayList<Integer> arrayList)
	{
		int size = arrayList.size();
		if (size == 0) return null;
		int[] strArray = new int[size];
		for (int i=0; i<size; i++)
			strArray[i] = arrayList.get(i);
		return strArray;
	}

	public static double[] convtArrayListToDouble(ArrayList<Double> arrayList)
	{
		int size = arrayList.size();
		if (size == 0) return null;
		double[] strArray = new double[size];
		for (int i=0; i<size; i++)
			strArray[i] = arrayList.get(i);
		return strArray;
	}

	/** return true if @str = "   ". */
	public static boolean isNoInfo(String str)
	{
		return (str == null || str.trim().isEmpty()) ? true : false;
	}
	
	public static boolean isExceedStrLength(int idx, int length)
	{
		if (idx < length) return false;
		else return true;
	}
	
	/** for PSS/E raw file */
	public static boolean isOneTypeEntryEnded(String line)
	{
		line = line.trim();
		boolean end = line.startsWith("0");
		/* The following two lines may not be needed. */
		if (end == true) end = line.contains("/");
		if (end == true) end = line.toLowerCase().contains("end");
		return end;
	}
	
	public static void stringsTrim(String[] lines)
	{
		if (lines == null) return;
		for (int i=0; i<lines.length; i++)
			lines[i] = lines[i].trim();
	}

	/** Remove quotes if the first character is a single quote or the last character is a single quote. */
	public static void removeBoundarySingleQuotes(ArrayList<String> str)
	{
		for (int i=0; i<str.size(); i++)
			str.set(i, removeBoundarySingleQuotes(str.get(i)));
	}

	/** Remove quotes if the first character is a single quote or the last character is a single quote. */
	public static void removeBoundarySingleQuotes(String[] lines)
	{
		if (lines == null) return;
		for (int i=0; i<lines.length; i++)
			lines[i] = removeBoundarySingleQuotes(lines[i]);
	}

	/** Remove quotes if the first character is a single quote or the last character is a single quote. */
	public static String removeBoundarySingleQuotes(String str)
	{
		if (str.startsWith("'")) str = str.substring(1);
		if (str.endsWith("'")) str = str.substring(0, str.length()-1);
		return str;
	}

	/** Remove quotes if the first character is a single quote or the last character is a single quote. */
	public static void removeBoundaryDoubleQuotes(ArrayList<String> str)
	{
		for (int i=0; i<str.size(); i++)
			str.set(i, removeBoundaryDoubleQuotes(str.get(i)));
	}

	/** Remove quotes if the first character is a double quote or the last character is a double quote. */
	public static void removeBoundaryDoubleQuotes(String[] lines)
	{
		if (lines == null) return;
		for (int i=0; i<lines.length; i++)
			lines[i] = removeBoundaryDoubleQuotes(lines[i]);
	}

	/** Remove quotes if the first character is a double quote or the last character is a double quote. */
	public static String removeBoundaryDoubleQuotes(String str)
	{
		String elem = "\"";
		if (str.startsWith(elem)) str = str.substring(elem.length());
		if (str.endsWith(elem)) str = str.substring(0, str.length()-elem.length());
		return str;
	}

	public static boolean isFirstCharQ(String str) {return isFirstCharMatch(str, 'Q');}
	public static boolean isFirstCharMatch(String str, char e) {return (str.charAt(0) == e) ? true : false;}

}

