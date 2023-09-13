package com.sced.util;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * 
 * Initialized in Jul. 2014.
 * 
 * @author Xingpeng.Li (xplipower@gmail.com)
 *
 */
public class OutputArraysXL {

	public OutputArraysXL() {}

	static String _path=System.getProperty("user.dir")+"/output/";
//	static String _path=ParamIO.getOutPath();
	static String _fileName = "tmpArrayFile.txt";
	
	public static void outputArray(int[] array)
	{
		AuxFileXL.createFolder(_path);
		outputArray(array, _path+_fileName, true);
	}
	
	/** fileName : /path/to/file. */
	public static void outputArray(int[] array, String fileName, boolean isAbsPath)
	{
		outputArray(array, fileName, isAbsPath, false);
	}

	/** fileName : /path/to/file. */
	public static void outputArray(int[] array, String fileName, boolean isAbsPath, boolean appendData)
	{
		outputArray(array, fileName, isAbsPath, appendData, false);
	}

	/** fileName : /path/to/file. */
	public static void outputArray(int[] array, String fileName, boolean isAbsPath, boolean appendData, boolean justElems)
	{
		if (isAbsPath == false) fileName = AuxStringXL.filePathRelativeToAbs(fileName);
		try
		{
		    OutputStream resultFile = new FileOutputStream(fileName, appendData);
		    PrintStream outFile = new PrintStream (resultFile);
		    if (array == null) {System.err.println("Warning: array to write is null... ");}
		    else
		    {
			    if (justElems == false)
			    {
				    outFile.println(" i, array[i]");
					for(int i=0; i<array.length; i++)
					{
						outFile.print(" "+i);
						outFile.println(" "+array[i]);
					}
			    }
			    else
			    {
					for(int i=0; i<array.length; i++)
						outFile.println(array[i]);
			    }
		    }
		    outFile.close();
		    System.out.println("Output tmp array data successfully");
	    } catch (FileNotFoundException e) {
	    	System.out.println();
	    	System.err.println("Cannot write tmp array data to file" + e);
	    	e.printStackTrace();
	    }
	}
	
	public static void outputArray(float[] array)
	{
		AuxFileXL.createFolder(_path);
		outputArray(array, _path+_fileName, true);
	}

	/** fileName : /path/to/file. */
	public static void outputArray(float[] array, String fileName, boolean isAbsPath)
	{
		outputArray(array, fileName, isAbsPath, true);
	}

	/** @param fileName : /path/to/file. */
	public static void outputArray(float[] array, String fileName, boolean isAbsPath, boolean appendData)
	{
		outputArray(array, fileName, isAbsPath, appendData, false);
	}

	/** fileName : /path/to/file. */
	public static void outputArray(float[] array, String fileName, boolean isAbsPath, boolean appendData, boolean justElems)
	{
		if (isAbsPath == false) fileName = AuxStringXL.filePathRelativeToAbs(fileName);
		try
		{		    
		    OutputStream resultFile = new FileOutputStream(fileName, appendData);
		    PrintStream outFile = new PrintStream (resultFile);
		    if (array == null) {System.err.println("Warning: array to write is null... ");}
		    else
		    {
			    if (justElems == false)
			    {
				    outFile.println(" i, array[i]");
					for(int i=0; i<array.length; i++)
					{
						outFile.print(" "+i);
						outFile.println(" "+array[i]);
					}
			    }
			    else
			    {
					for(int i=0; i<array.length; i++)
						outFile.println(array[i]);
			    }
		    }
		    outFile.close();
		    System.out.println("Output tmp array data successfully");
	    } catch (FileNotFoundException e) {
	    	System.out.println();
	    	System.out.println("Cannot write tmp array data to file" + e);
	    	e.printStackTrace();
	    }
	}

	public static void outputArray(int[][] array)
	{
		outputArray(array, false);
	}

	/** 
	 * e.g. array[0] = [ 2, 3, 4]
	 *      array[1] = [ 5, 6, 7]
	 * if byRow == true, then, output:
	 *         2 3 4
	 *         5 6 7
	 * if byRow == false, then, output:
	 *         2 5
	 *         3 6
	 *         4 7
	 * 
	 * @param array
	 * @param byRow
	 */
	public static void outputArray(int[][] array, boolean byRow)
	{
		AuxFileXL.createFolder(_path);
		outputArray(array, byRow, _path+_fileName, true);
	}
	
	public static void outputArray(int[][] array, boolean byRow, String fileName, boolean isAbsPath)
	{
		outputArray(array, byRow, fileName, isAbsPath, true);
	}

	public static void outputArray(int[][] arrays, boolean byRow, String fileName, boolean isAbsPath, boolean appendData)
	{
		if (isAbsPath == false) fileName = AuxStringXL.filePathRelativeToAbs(fileName);
		try
		{
//			File genH = new File(rootDr+fileName);
//			if (genH.exists() && !genH.isDirectory())
//			{
//				System.out.println(); 
//				if (genH.delete()) System.out.println("Original "+genH.getName() + " is deleted.");
//			    else System.out.println("Delete operation is failed.");
//			}
		    OutputStream resultFile = new FileOutputStream(fileName, appendData);
		    PrintStream outFile = new PrintStream (resultFile);
		    
	    	int maxLength = AuxArrayXL.getMaxLength(arrays);
		    if (byRow == true)
		    {
		    	outFile.print(" i");
		    	for (int i=0; i<maxLength; i++)
		    		outFile.print(" array[i]["+(i+1)+"]");
			    outFile.println();
				for(int i=0; i<arrays.length; i++)
				{
					outFile.print(" "+(i+1));
					for (int j=0; j<arrays[i].length; j++)
						outFile.print(" "+arrays[i][j]);
					outFile.println();
				}
		    }
		    else
		    {
		    	int[] arraysLength = AuxArrayXL.getArraysLength(arrays);
		    	int size = arrays.length;
		    	outFile.print(" depth");
		    	for (int i=0; i<size; i++)
		    		outFile.print(" array["+(i+1)+"]");
			    outFile.println();
		    	for (int i=0; i<maxLength; i++)
		    	{
					outFile.print(" "+(i+1));
					for(int j=0; j<size; j++)
					{
						if(i < arraysLength[j]) outFile.print(" "+arrays[j][i]);
						else outFile.print(" NULL");
					}
					outFile.println();
		    	}
		    }
		    
		    outFile.close();
		    System.out.println("Output tmp array data successfully");
	    } catch (FileNotFoundException e) {
	    	System.out.println();
	    	System.out.println("Cannot write tmp array data to file" + e);
	    	e.printStackTrace();
	    }
	}

	public static void outputArray(float[][] array)
	{
		outputArray(array, false);
	}

	public static void outputArray(float[][] array, boolean byRow)
	{
		AuxFileXL.createFolder(_path);
		outputArray(array, byRow, _path+_fileName, true);
	}
	
	public static void outputArray(float[][] array, boolean byRow, String fileName, boolean isAbsPath)
	{
		outputArray(array, byRow, fileName, isAbsPath, true);
	}

	public static void outputArray(float[][] arrays, boolean byRow, String fileName, boolean isAbsPath, boolean appendData)
	{
		if (isAbsPath == false) fileName = AuxStringXL.filePathRelativeToAbs(fileName);
		try
		{
		    OutputStream resultFile = new FileOutputStream(fileName, appendData);
		    PrintStream outFile = new PrintStream (resultFile);
		    
	    	int maxLength = AuxArrayXL.getMaxLength(arrays);
		    if (byRow == true)
		    {
		    	outFile.print(" i");
		    	for (int i=0; i<maxLength; i++)
		    		outFile.print(" array[i]["+(i+1)+"]");
			    outFile.println();
				for(int i=0; i<arrays.length; i++)
				{
					outFile.print(" "+(i+1));
					for (int j=0; j<arrays[i].length; j++)
						outFile.print(" "+arrays[i][j]);
					outFile.println();
				}
		    }
		    else
		    {
		    	int[] arraysLength = AuxArrayXL.getArraysLength(arrays);
		    	int size = arrays.length;
		    	outFile.print(" depth");
		    	for (int i=0; i<size; i++)
		    		outFile.print(" array["+(i+1)+"]");
			    outFile.println();
		    	for (int i=0; i<maxLength; i++)
		    	{
					outFile.print(" "+(i+1));
					for(int j=0; j<size; j++)
					{
						if(i < arraysLength[j]) outFile.print(" "+arrays[j][i]);
						else outFile.print(" NULL");
					}
					outFile.println();
		    	}
		    }
		    
		    outFile.close();
		    System.out.println("Output tmp array data successfully");
	    } catch (FileNotFoundException e) {
	    	System.out.println();
	    	System.out.println("Cannot write tmp array data to file" + e);
	    	e.printStackTrace();
	    }
	}
	
	public static void outputArray(double[][] array)
	{
		outputArray(array, false);
	}

	public static void outputArray(double[][] array, boolean byRow)
	{
		AuxFileXL.createFolder(_path);
		outputArray(array, byRow, _path+_fileName, true);
	}
	
	public static void outputArray(double[][] array, boolean byRow, String fileName, boolean isAbsPath)
	{
		outputArray(array, byRow, fileName, isAbsPath, true);
	}

	public static void outputArray(double[][] arrays, boolean byRow, String fileName, boolean isAbsPath, boolean appendData)
	{
		if (isAbsPath == false) fileName = AuxStringXL.filePathRelativeToAbs(fileName);
		try
		{
		    OutputStream resultFile = new FileOutputStream(fileName, appendData);
		    PrintStream outFile = new PrintStream (resultFile);
		    
	    	int maxLength = AuxArrayXL.getMaxLength(arrays);
		    if (byRow == true)
		    {
		    	outFile.print(" i");
		    	for (int i=0; i<maxLength; i++)
		    		outFile.print(" array[i]["+(i+1)+"]");
			    outFile.println();
				for(int i=0; i<arrays.length; i++)
				{
					outFile.print(" "+(i+1));
					for (int j=0; j<arrays[i].length; j++)
						outFile.print(" "+arrays[i][j]);
					outFile.println();
				}
		    }
		    else
		    {
		    	int[] arraysLength = AuxArrayXL.getArraysLength(arrays);
		    	int size = arrays.length;
		    	outFile.print(" depth");
		    	for (int i=0; i<size; i++)
		    		outFile.print(" array["+(i+1)+"]");
			    outFile.println();
		    	for (int i=0; i<maxLength; i++)
		    	{
					outFile.print(" "+(i+1));
					for(int j=0; j<size; j++)
					{
						if(i < arraysLength[j]) outFile.print(" "+arrays[j][i]);
						else outFile.print(" NULL");
					}
					outFile.println();
		    	}
		    }
		    
		    outFile.close();
		    System.out.println("Output tmp array data successfully");
	    } catch (FileNotFoundException e) {
	    	System.out.println();
	    	System.out.println("Cannot write tmp array data to file" + e);
	    	e.printStackTrace();
	    }
	}
	
	public static void outputArray(String[] array)
	{
		AuxFileXL.createFolder(_path);
		outputArray(array, _path+_fileName, true);
	}
	
	/** fileName : /path/to/file. */
	public static void outputArray(String[] array, String fileName, boolean isAbsPath)
	{
		outputArray(array, fileName, isAbsPath, true);
	}

	/** fileName : /path/to/file. */
	public static void outputArray(String[] array, String fileName, boolean isAbsPath, boolean appendData)
	{
		outputArray(array, fileName, isAbsPath, appendData, false);
	}

	/** fileName : /path/to/file. */
	public static void outputArray(String[] array, String fileName, boolean isAbsPath, boolean appendData, boolean justElems)
	{
		if (isAbsPath == false) fileName = AuxStringXL.filePathRelativeToAbs(fileName);
		try
		{
		    OutputStream resultFile = new FileOutputStream(fileName, appendData);
		    PrintStream outFile = new PrintStream (resultFile);
		    if (array == null) {System.err.println("Warning: array to write is null... ");}
		    else
		    {
			    if (justElems == false)
			    {
				    outFile.println(" i, array[i]");
					for(int i=0; i<array.length; i++)
					{
						outFile.print(" "+i);
						outFile.println(" "+array[i]);
					}
			    }
			    else
			    {
					for(int i=0; i<array.length; i++)
						outFile.println(array[i]);
			    }
		    }
		    outFile.close();
		    System.out.println("Output file: "+ fileName+ " successfully.");
	    } catch (FileNotFoundException e) {
	    	System.out.println();
	    	System.err.println("Fail to Output file: "+ fileName+ "." + e);
	    	e.printStackTrace();
	    }
	}

	
	
	
	
}
