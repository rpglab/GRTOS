package com.utilxl.iofiles;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Scanner;

import com.utilxl.string.AuxStringXL;

/**
 * 
 * @author Xingpeng.Li (xplipower@gmail.com)
 *
 */
public class ReadMultiCollumFloatData {

	String _fileName;  // path to file and data file name 
	int _nCol;
	int _nRow;
	float[][] _dataByRow;      // _data[0] contains all the data in the 1st row.
	float[][] _dataByCol;  // _dataByCol[0] contains all the data in the 1st column.
	
	
	public ReadMultiCollumFloatData(int nRow, int nCol, String fileName)
	{
		initial(nRow, nCol, fileName, true);
	}
	
	public ReadMultiCollumFloatData(int nRow, int nCol, String fileName, boolean isAbsPath)
	{
		initial(nRow, nCol, fileName, isAbsPath);
	}

	void initial(int nRow, int nCol, String fileName, boolean isAbsPath)
	{
		_nRow =nRow;
		_nCol= nCol;
		_dataByRow = new float[_nRow][_nCol];
		_dataByCol = new float[_nCol][_nRow];
		if (isAbsPath == false) fileName = AuxStringXL.filePathRelativeToAbs(fileName);
		_fileName = fileName;
		loadFileData();
	}
	
	public void loadFileData()
	{
		try {
			InputStream myFile= new FileInputStream(_fileName);
			Scanner loadData = new Scanner(myFile);
			for (int i=0; i<_nRow; i++)
				for (int j=0; j<_nCol; j++)
				{
					float data = loadData.nextFloat();
					_dataByRow[i][j] = data;
				    _dataByCol[j][i] = data;
				}
			loadData.close();
			myFile.close();
		} catch (Exception Err) {
			System.out.println("\nError Loading Float Data File in ReadMultiCollumFloatData.java ! \n " + Err);
			_dataByRow = null;
			_dataByCol = null;
			System.exit(0); // added on Mar.9th 2016.
		}
	}
	
	public float[][] getArrayByRow() { return _dataByRow;}
	public float[][] getArrayByCol() { return _dataByCol;}

}
