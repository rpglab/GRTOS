package com.sced.util;


/**
 * Auxiliary methods : for array
 * 
 * Initialized in Jun. 2014
 * 
 * @author Xingpeng.Li (xplipower@gmail.com).
 *
 */
public class AuxStringXL {
	
	
	/** change relative path to absolute path */
	public static String relPathToAbsPath(String relativePath)
	{
		String absPath = System.getProperty("user.dir");
		absPath = replaceElem(absPath, '\\', '/');
		absPath = endWith(absPath, "/");
		if (relativePath == null) return absPath;
		relativePath = replaceElem(relativePath, '\\', '/');
		if (relativePath.startsWith("/") == true) relativePath = relativePath.substring(1, relativePath.length());
		absPath = absPath + relativePath;
	    absPath = endWith(absPath, "/");
		return absPath;
	}
	
	/** change relative pathToFile to absolute pathToFile */
	public static String filePathRelativeToAbs(String fileName)
	{
		String root = replaceElem(System.getProperty("user.dir"), '\\', '/');
		fileName = replaceElem(fileName, '\\', '/');
		if (fileName.startsWith("/") == false) fileName = "/" + fileName;
		return (root + fileName);
	}
	
	/** Replace all the oldChar with newChar in String 'str' if any. */
	public static String replaceElem(String str, char oldChar, char newChar)
	{
	    for (int i=0; i<str.length(); i++)
	    {
	    	if (str.charAt(i) == oldChar)
	    		str = str.substring(0,i)+newChar+str.substring(i+1);
	    }
	    return str;
	}
	
	/** 
	 * @param str
	 * @param end
	 * @return str if String "str" ends with "end";
	 *  "str"+"end" if String "str" does not ends with "end";
	 */
	public static String endWith(String str, String end)
	{
		String newStr = str;
		if (newStr.endsWith(end) == false) newStr = newStr + end;		
		return newStr;
	}
	
	/** 
	 * @param str
	 * @param start
	 * @return str if String "str" starts with "start";
	 *  "start"+"str" if String "str" does not starts with "start";
	 */
	public static String startWith(String str, String start)
	{
		String newStr = str;
		if (newStr.startsWith(start) == false) newStr = start + newStr;		
		return newStr;
	}
	
	/** 
	 * @param str
	 * @param start
	 * @return str if String "str" does not start with "start";
	 *  "str"-"start" if String "str" starts with "start";
	 */
	public static String notStartWith(String str, String start)
	{
		String newStr = str;
		if (newStr.startsWith(start) == true) newStr = start.substring(start.length());		
		return newStr;
	}
	
	/** Return input String uri for PsseModel.open() method. 
	 * @Param_path should be the path to csv files if (isPath == true);
	 * @Param_path should be the path to raw files + raw file name if (isPath == false).
	 * 
	 * */
	public static String getUriForOpenPA(String path, boolean isPath)
	{
		String uri;
		if (isPath == true) uri = "psseraw:path="+path+"&lowx=adjust";
		else uri = "psseraw:file="+path+"&lowx=adjust";
		return replaceElem(uri, '\\', '/');
	}	
	
}
