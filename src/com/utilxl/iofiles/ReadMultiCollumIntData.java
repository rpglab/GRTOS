package com.utilxl.iofiles;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Scanner;

import com.utilxl.string.AuxStringXL;

/**
 * This routine is used to read an integer data file, 
 * the array size has to be given.
 * 
 * @author Xingpeng.Li (xplipower@gmail.com)
 *
 */
public class ReadMultiCollumIntData {
	
	String _fileName;  // path to file and data file name 
	int _nCol;
	int _nRow;
	int[][] _dataByRow;      // _data[0] contains all the data in the 1st row.
	int[][] _dataByCol;  // _dataByCol[0] contains all the data in the 1st column.
	
	
	public ReadMultiCollumIntData(int nRow, int nCol, String fileName)
	{
		initial(nRow, nCol, fileName, true);
	}
	
	public ReadMultiCollumIntData(int nRow, int nCol, String fileName, boolean isAbsPath)
	{
		initial(nRow, nCol, fileName, isAbsPath);
	}

	void initial(int nRow, int nCol, String fileName, boolean isAbsPath)
	{
		_nRow =nRow;
		_nCol= nCol;
		_dataByRow = new int[_nRow][_nCol];
		_dataByCol = new int[_nCol][_nRow];
		if (isAbsPath == false) fileName = AuxStringXL.filePathRelativeToAbs(fileName);
		_fileName = fileName;
		loadFileData();
	}
	
	public void loadFileData()
	{
		try{
			InputStream myFile= new FileInputStream(_fileName);
			Scanner loadData = new Scanner(myFile);
			for (int i=0; i<_nRow; i++)
				for (int j=0; j<_nCol; j++)
				{
					int data = loadData.nextInt();
					_dataByRow[i][j] = data;
				    _dataByCol[j][i] = data;
				}
			loadData.close();
			myFile.close();
		} catch (Exception Err) {
			System.out.println("\nError Loading Integer Data File in ReadMultiCollumIntData.java ! \n " + Err);
			_dataByRow = null;
			_dataByCol = null;
		}
	}
	
	public int[][] getArrayByRow() { return _dataByRow;}
	public int[][] getArrayByCol() { return _dataByCol;}

}
