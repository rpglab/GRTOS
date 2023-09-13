package com.sced.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Scanner;


/** 
 * Auxiliary methods : for array
 * 
 * Initialized on Aug.2014
 * 
 * @author Xingpeng.Li (xplipower@gmail.com)
 *
 */
public class AuxFileXL {
	
	/** 
	 * check folder "path" exists or not
	 * return true if "path" does not originally exist and it is created successfully.
	 * return false if "path" exists.
	 * TODO: @exception it cannot create multiple folders
	 * */
	public static boolean createFolder(String path)
	{
		File directory = new File(path);
		return createFolder(directory);
	}
	/** 
	 * check folder "directory" exists or not
	 * return true if "directory" does not originally exist and it is created successfully.
	 * return false if "directory" exists.
	 * TODO: @exception it cannot create multiple folders
	 * */
	public static boolean createFolder(File directory)
	{
		boolean success = false;
		if (!directory.exists() || directory.isFile())
			success = directory.mkdir();
		return success;
	}
	
	
	/** Output an int[] array to a row in the file designated by outFile of PrintStream-type . */
	public static void outputElemFrmArray(PrintStream outFile, int[] array)
	{
		outputElemFrmArray(outFile, array, true);
	}
	
	/** Output an int[] array to a row in the file designated by outFile of PrintStream-type . */
	public static void outputElemFrmArray(PrintStream outFile, int[] array, boolean nextLine)
	{
		for (int i=0; i<array.length; i++)
			outFile.print("  "+array[i]);
		if (nextLine) outFile.println();
	}
	
	/** Output a float[] array to a row in the file designated by outFile of PrintStream-type . */
	public static void outputElemFrmArray(PrintStream outFile, float[] array)
	{
		outputElemFrmArray(outFile, array, true);
	}
	
	/** Output a float[] array to a row in the file designated by outFile of PrintStream-type . */
	public static void outputElemFrmArray(PrintStream outFile, float[] array, boolean nextLine)
	{
		for (int i=0; i<array.length; i++)
			outFile.print("  "+array[i]);
		if (nextLine) outFile.println();
	}
	
	public static void deleteFolder(String folder)
	{
		deleteFolder(new File(folder));
	}
	
	public static void deleteFolder(File folder)
	{
		deleteFolder(folder, false);
	}

	public static void deleteFolder(File folder, boolean showRunInfo)
	{
		if (folder.exists() && folder.isDirectory())
		{
			delete(folder);
//			boolean deleted = folder.delete();
//			if (showRunInfo == true)
//			{
//				System.out.println();
//				if (deleted == true) System.out.println("Original "+folder.getName() + " is deleted.");
//			    else System.out.println("Delete operation is failed.");
//			}
		}
	}

	/** Check file existed or not; if yes, then, delete it. */
	public static void deleteFile(File file)
	{
		deleteFile(file, false);
	}

	/** Check file existed or not; if yes, then, delete it. */
	public static void deleteFile(File file, boolean showRunInfo)
	{
		if (file.exists() && !file.isDirectory())
		{
			boolean deleted = file.delete();
			if (showRunInfo == true)
			{
				System.out.println();
				if (deleted == true) System.out.println("Original "+file.getName() + " is deleted.");
			    else System.out.println("Delete operation is failed.");
			}
		}
	}
	
	/** Delete file/folder with contents.  */
    public static void delete(File file) {delete(file, false);}
    public static void delete(File file, boolean showRunInfo)
    {
    	if (file.isDirectory()) 
    	{
    		//directory is empty, then delete it
    		if(file.list().length==0) {
    		   file.delete();
    		   if (showRunInfo == true) System.out.println("Directory is deleted : " + file.getAbsolutePath());
    		} else {
    		   //list all the directory contents
        	   String files[] = file.list();
        	   for (String temp : files) {
         	      //construct the file structure
         	      File fileDelete = new File(file, temp);
           	      delete(fileDelete);  //recursive delete
        	   }
 
        	   //check the directory again, if empty then delete it
        	   if(file.list().length==0) {
             	     file.delete();
             	    if (showRunInfo == true) System.out.println("Directory is deleted : " + file.getAbsolutePath());
        	   }
    		}
    	} 
    	else {
    		file.delete(); //if file, then delete it
    		if (showRunInfo == true) System.out.println("File is deleted : " + file.getAbsolutePath());
    	}
    }


	/** Initialize a file with a title.
	 * e.g., pathToFile : M:\\RATCPackage\\filesOfOutput\\a.txt  */
	public static void initFileWithTitle(String absPathToFile, String title)
	{
		initFileWithTitle(absPathToFile, title, true);
	}
	public static void initFileWithTitle(String absPathToFile, String title, boolean append)
	{
		try
		{
   		    OutputStream resultFile = new FileOutputStream(absPathToFile, append);
   		    dumpStringToFile(resultFile, title, true);
       		resultFile.close();
 		    System.out.println("Initialize file "+ absPathToFile + " successfully.");
		}
		catch (IOException e) {
	    	System.out.println("Initialization of file " + absPathToFile + " failed." + e);
	    	e.printStackTrace();
		}
	}
	
	public static void initFileWithTitle(String absPathToFile, String[] titles, boolean append)
	{
		try
		{
   		    OutputStream resultFile = new FileOutputStream(absPathToFile, append);
   		    dumpStringToFile(resultFile, titles);
       		resultFile.close();
 		    System.out.println("Initialize file "+ absPathToFile + " successfully.");
		}
		catch (IOException e) {
	    	System.out.println("Initialization of file " + absPathToFile + " failed." + e);
	    	e.printStackTrace();
		}
	}
	
	/** Initialize a file with a title. */
	public static void initFileWithTitle(File file, String title)
	{
		initFileWithTitle(file, title, true);
	}
	public static void initFileWithTitle(File file, String title, boolean append)
	{
		try
		{
   		    OutputStream resultFile = new FileOutputStream(file, append);
   		    dumpStringToFile(resultFile, title, true);
       		resultFile.close();
 		    System.out.println("Initialize file "+ file.getAbsolutePath() + " successfully.");
		}
		catch (IOException e) {
	    	System.out.println("Initialization of file " + file.getAbsolutePath() + " failed." + e);
	    	e.printStackTrace();
		}
	}
	
	public static void initFileWithTitle(File file, String[] titles)
	{
		initFileWithTitle(file, titles, true);
	}
	public static void initFileWithTitle(File file, String[] titles, boolean append)
	{
		try
		{
   		    OutputStream resultFile = new FileOutputStream(file, append);
   		    dumpStringToFile(resultFile, titles);
       		resultFile.close();
 		    System.out.println("Initialize file "+ file.getAbsolutePath() + " successfully.");
		}
		catch (IOException e) {
	    	System.out.println("Initialization of file " + file.getAbsolutePath() + " failed." + e);
	    	e.printStackTrace();
		}
	}
	

	public static void dumpStringToFile(OutputStream resultFile, String title)
	{
		dumpStringToFile(resultFile, title, true);
	}
	public static void dumpStringToFile(OutputStream resultFile, String title, boolean nextLine)
	{
	    PrintStream outFile = new PrintStream (resultFile);
	    if (nextLine == true) outFile.println("  "+title);
	    else outFile.print("  "+title);
  		outFile.close();
	}
	
	public static void dumpStringToFile(OutputStream resultFile, String[] titles)
	{
	    PrintStream outFile = new PrintStream (resultFile);
	    int size = titles.length;
	    for (int i=0; i<size; i++)
		    outFile.println("  "+titles[i]);
  		outFile.close();
	}
	
	/** Read one collum integer data from file. */
	public static int[] readOneCollumIntData(String fileName)
	{
		return readOneCollumIntData(fileName, true);
	}
	public static int[] readOneCollumIntData(String fileName, boolean isAbsPath)
	{
		if (isAbsPath == false) fileName = AuxStringXL.filePathRelativeToAbs(fileName);
		File readFile = new File(fileName);
		return readOneCollumIntData(readFile);
	}
	public static int[] readOneCollumIntData(File readFile)
	{
		ArrayList<Integer> rawData = new ArrayList<Integer>();
	    int numLineSearching = 0;
		try {
			InputStream readInput= new FileInputStream(readFile);
			Scanner readData = new Scanner(readInput);	
		    while (readData.hasNextLine())
			{
				String inputData = readData.nextLine();
				int aa = Integer.parseInt(inputData);
				rawData.add(aa);
				numLineSearching++;
			}
		    readData.close();
		    readInput.close();		    
		} catch (Exception Err1) {
			System.out.println("\nError loading file!"+ Err1 + " \n ");
		}
		int[] intData = new int[numLineSearching];
	    for (int i=0; i<numLineSearching; i++)
	    	intData[i] = rawData.get(i);
	    return intData;
	}
	
	/** Read one collum float data from file. */
	public static float[] readOneCollumFloatData(String fileName)
	{
		return readOneCollumFloatData(fileName, true);
	}
	public static float[] readOneCollumFloatData(String fileName, boolean isAbsPath)
	{
		if (isAbsPath == false) fileName = AuxStringXL.filePathRelativeToAbs(fileName);
		File readFile = new File(fileName);
		return readOneCollumFloatData(readFile);
	}
	public static float[] readOneCollumFloatData(File readFile)
	{
		ArrayList<Float> rawData = new ArrayList<Float>();
	    int numLineSearching = 0;
		try {
			InputStream readInput= new FileInputStream(readFile);
			Scanner readData = new Scanner(readInput);	
		    while (readData.hasNextLine())
			{
				String inputData = readData.nextLine();
				float aa = Float.parseFloat(inputData);
				rawData.add(aa);
				numLineSearching++;
			}
		    readData.close();
		    readInput.close();		    
		} catch (Exception Err1) {
			System.out.println("\nError loading file!"+ Err1 + " \n ");
		}
		float[] intData = new float[numLineSearching];
	    for (int i=0; i<numLineSearching; i++)
	    	intData[i] = rawData.get(i);
	    return intData;
	}
	

}
