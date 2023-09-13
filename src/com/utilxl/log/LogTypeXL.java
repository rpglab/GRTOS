package com.utilxl.log;

/**
 * 
 * @author Xingpeng.Li (xplipower@gmail.com)
 *
 */
public enum LogTypeXL
{
	Start, End, Debug, Log, Comment, CheckPoint, MileStone, Warning, Error, Other;
	
	public static LogTypeXL fromConfig(String cfg)
	{
		switch(cfg.toLowerCase())
		{
			case "start": return Start;			
			case "end": return End;			
			case "debug": return Debug;
			case "log": return Log;
			case "comment": return Comment;
			case "checkpoint": return CheckPoint;
			case "milestone": return MileStone;
			case "warning": return Warning;
			case "error": return Error;
			case "other": return Other;		
			default: return Debug;
		}
	}
}
