package com.utilxl.log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Initialized on Jul.18th, 2014.
 * 
 * @author Xingpeng.Li, xplipower@gmail.com 
 */
public class DiaryXL {
	
	PrintStream _outFile;
	
	boolean _useDynamicUniqueName = true;
	String _folderName = "log";
	String _fileName = "diary";       
	String _fileExt = ".txt";        // by default it is .txt extension
	String _path = System.getProperty("user.dir");
	String _fullPath;
	
	String _initStr;
	
	boolean _enableLog = true;
	boolean _deleteOrigLogFile;
	
	String _timeStart;                      // in the format of "yyyy-MM-dd HH:mm:ss"
	long _t_Start;                          // CPU time to launch this object

	int _numLogRow = 1;                     // row index starting from 1
	
	public DiaryXL()
	{
		_timeStart = getCurrentTimeStamp();
		_t_Start = System.nanoTime();
	}
	
	public DiaryXL(String initStr)
	{
		this();
		_initStr = initStr;
	}
	
	public void enableLog() {_enableLog = true;}
	public void disableLog() {_enableLog = false;}

	public void initial()
	{
		if (_enableLog == false) return;
		String path = _path + System.getProperty("file.separator") + _folderName;
		File diaryFolder = new File(path);
		if (diaryFolder.isDirectory() == false)
		{
		    try {
		    	diaryFolder.mkdir();
		    	System.out.println("New Folder <log> is created");
		     } catch(SecurityException se) {
			    System.err.println("Folder <log> does not exist, and fail to be created..");
		     }
		}
		String fullPath = path + System.getProperty("file.separator");
		String fileName = _fileName;
		if (_useDynamicUniqueName) fileName += "_" + getCurrentTimeStamp().replaceAll(":", "-").replaceAll(" ", "_");
		fileName += _fileExt;
		initial(fullPath+fileName);
	}
	
	public void initial(String fullPath)
	{
		if (_enableLog == false) return;
		_fullPath = fullPath;
		File diaryFile = new File(_fullPath);
		if (diaryFile.exists() && !diaryFile.isDirectory() && _deleteOrigLogFile == true)
		{
			if (diaryFile.delete()) System.out.println("Original log file "+diaryFile.getName() + " is deleted");
		    else System.err.println("Fail to delete original log file");
		}
	    try {
			_outFile = new PrintStream (new FileOutputStream(_fullPath, true), true);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	    String initStr = "diary record starts here ...";
	    if (_initStr != null) initStr = _initStr + ", " + initStr;
	    hotLine(LogTypeXL.Start, initStr);
	    System.out.println("Log file is created: " + _fullPath);
	}
	
	public void hotLine(String message) {hotLine(LogTypeXL.Log, message);}
	public void hotLine(LogTypeXL type, String message)
	{
		if (_enableLog == false) return;
		String timelog = getCurrentTimeStamp() + " No." + (_numLogRow++) + " "; 
		timelog += "t=" + shrinkDecimalDigits(getSolutionTime(), 2) + " seconds ";
		timelog += "[ " + TypeToString(type) + ", " + message + " ]";
		_outFile.println(timelog);
	}
	
	/** Call this method when log record is over */
	public void done() 
	{
		if (_enableLog == false) return;
		hotLine(LogTypeXL.End, "diary record ends here ...");
		_outFile.close();
	    System.out.println("Total time for this log file: " + getSolutionTime());
	    System.out.println("Log file is closed: " + _fullPath);
	}
	
	/** shrink a double number to only @numDD digits after dot if it is more than @numDD 
	 * and return a String object */
	public String shrinkDecimalDigits(double number, int numDD)
	{
		String str = Double.toString(number);
		int idxDot = str.indexOf(".");
		if (idxDot == -1) return str;
		if ((idxDot + numDD + 1) < str.length()) str = str.substring(0, idxDot + numDD + 1);
		return str;
	}
	
	public String getCurrentTimeStamp() {
	    SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    Date now = new Date();
	    return sdfDate.format(now);
	}
	
	/** Get solution time in seconds for the moment of calling of this method. */
	public double getSolutionTime() {return (System.nanoTime() - _t_Start)/1e9f;}
	public String getFileName() {return _fileName;}

	public void setFolderName(String name) {_folderName = name;}
	/** file extension is not needed, it is .txt by default. */
	public void setFileName(String name) {_fileName = name;}
	public void setPath(String path) {_path = path;}
	
	public long getT_Start() {return _t_Start;}

	public String TypeToString(LogTypeXL type)
	{
        switch(type)
        {
			case Start:         return "Start";			
			case End:           return "End";
        	case Debug:         return "Debug";
        	case Log:           return "Log";
        	case Comment:       return "Comment";
        	case CheckPoint:    return "CheckPoint";
        	case MileStone:     return "MileStone";
        	case Warning:       return "Warning";
        	case Error:         return "Error";
        	case Other:         return "Other";
        	default:            return "WrongLogType";
        }
	}
	
	public static void main(String[] arg)
	{
		/* Create diary, track the potential warnings. */
		DiaryXL diary = new DiaryXL();
		diary.setFileName(diary.getFileName() + "_XL");
		diary.initial();

		/* Wwrite some information to log file */
		diary.hotLine(LogTypeXL.CheckPoint, "this is a check point");
		diary.hotLine(LogTypeXL.Warning, "this is a warning record");
		diary.hotLine(LogTypeXL.Log, "this is a normal log information");
		diary.hotLine(LogTypeXL.Error, "this is a error log information");
		
		/* Program ends here */
		System.out.println("Program pseudo ends");
		diary.done();
		System.out.println("Program ends");
	}
	
}
