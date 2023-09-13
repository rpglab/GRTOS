package com.rtca_cts.param;


/**
 * 
 *  All static variables should be modified if needed at the very beginning of
 *  any application program! Otherwise, chaos may occur!!!
 * 
 * @author Xingpeng.Li (xplipower@gmail.com)
 *
 */
public class ParamIO {

	static String _UserDir = System.getProperty("user.dir");
	
	static String _PathPsseRaw = null;
	static String _PathCSV = null;
	static String _BasicOutPath = null;

	static String _IOFolder = "/filesOfOutput/";
	static String _IOFolderName = "filesOfOutput";
	
	// All the following folders are sub-folders in _IOFolder.
	static String _ACPFFolder = "ACPFResult/";
	static String _ACPFFolderName = "ACPFResult";
	
	static String _CAFolder = "CAResult/";
	static String _CAFolderName = "CAResult";
	
	static String _TSFolder = "TSResult/";
	static String _TSFolderName = "TSResult";
	
	public static void setPathPsseRaw(String path) { _PathPsseRaw = path;}
	public static void setPathCSV(String path) { _PathCSV = path;}
	public static void setOutPath(String path) { _BasicOutPath = path;}
	
	public static String getCAFolder() {return _CAFolder;}
	public static void setCAFolder(String caFolder) {_CAFolder = caFolder;}
	
	public static String getTSFolder() {return _TSFolder;}
	public static void setTSFolder(String tsFolder) {_TSFolder = tsFolder;}
	
	public static String getPathPsseRaw()
	{
		if (_PathPsseRaw != null) return _PathPsseRaw;
		return _UserDir + "/dataToRead/";
	}

	public static String getPathCSV()
	{
		if (_PathCSV != null) return _PathCSV;
		return _UserDir + "/casesCSVFormat/";
	}

	public static String getOutPath()
	{
		if (_BasicOutPath != null) return _BasicOutPath;
		return _UserDir + _IOFolder;
	}
	
	public static String getACPFPath() {return getOutPath()+_ACPFFolder;}
	public static String getCAPath() {return getOutPath()+_CAFolder;}
//	public static String getCAGeneInfoPath() {return getCAPath()+ "_GeneralInfo/";}
	public static String getTSPath() {return getOutPath()+_TSFolder;}
	
	
/*	// For parallel computing
	static String _CAParaFolder = "CAResultPara/";
	static String _CAParaFolderName = "CAResultPara";
	public static String getCAParaPath() {return getOutPath()+_CAParaFolder;}
*/
	

}
