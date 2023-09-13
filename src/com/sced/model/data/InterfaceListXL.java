package com.sced.model.data;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import com.sced.model.SystemModelXL;
import com.utilxl.log.LogTypeXL;

/**
 * 
 * @author Xingpeng Li, xplipower@gmail.com 
 *
 */
public class InterfaceListXL {

	SystemModelXL _model;
	
	int _size;
	boolean[] _isInterfaceActive;   // is interface active for the base case
	double[] _totalLimitBaseCase;     // interface limit in the base case 
	int[][] _interfaceLines;  // line index
	double[][] _interfaceEmgcyLimits;  // interface total emergency limits if one line forming the interface is temperately under contingency.
	boolean[][] _interfaceLinesDirection; // if true, from-bus to to-bus; if false, to-bus to from-bus.

	public InterfaceListXL(SystemModelXL model) {_model = model; init();}
	
	private void init() {
		_size = 0;
//		_isCtgcyActive[1] = true;
	}
	
	public void setSize(int size) {_size = size; initSize();}

	protected void initSize() {
		_isInterfaceActive = new boolean[_size];
		_totalLimitBaseCase = new double[_size];
		_interfaceLines = new int[_size][];
		_interfaceEmgcyLimits = new double[_size][];
		_interfaceLinesDirection = new boolean[_size][];
	}
	
	public void setInterfaceActiveFlag(int i, boolean activeFlag) {_isInterfaceActive[i] = activeFlag;}
	public void setInterfaceLimit(int i, double totalLimit) {_totalLimitBaseCase[i] = totalLimit;}
	public void setInterfaceLines(int i, int[] interfaceLines) {_interfaceLines[i] = interfaceLines;}
	public void setInterfaceEmgcyLimits(int i, double[] interfaceEmgcyLimits) {_interfaceEmgcyLimits[i] = interfaceEmgcyLimits;}
	public void setInterfaceLinesDirection(int i, boolean[] direction) {_interfaceLinesDirection[i] = direction;}

	public void setInterfaceActiveFlag(boolean[] activeFlag) {_isInterfaceActive = activeFlag;}
	public void setInterfaceLimit(double[] totalLimit) {_totalLimitBaseCase = totalLimit;}
	public void setInterfaceLines(int[][] interfaceLines) {_interfaceLines = interfaceLines;}
	public void setInterfaceEmgcyLimits(double[][] interfaceEmgcyLimits) {_interfaceEmgcyLimits = interfaceEmgcyLimits;}
	public void setInterfaceLinesDirection(boolean[][] direction) {_interfaceLinesDirection = direction;}

	public int size() {return _size;}
	public boolean[] isInterfaceActive() {return _isInterfaceActive;}
	public double[] getTotalLimit() {return _totalLimitBaseCase;}
	public int[][] getInterfaceLines() {return _interfaceLines;}
	public double[][] getInterfaceEmgcyLimits() {return _interfaceEmgcyLimits;}
	public boolean[][] getInterfaceLinesDirection() {return _interfaceLinesDirection;}
	
	/** Given the contingency line index, for all interfaces, return the index of the location of
	 *  same contingency line in that interface lines array or -1 if not found in the interface lines.  */
	public int[] getItemIdxPerInterface(int idxCtgcyBrc)
	{
		int[] indices = new int[_interfaceLines.length];
		for (int i=0; i<_interfaceLines.length; i++)
			indices[i] = getPos(idxCtgcyBrc, _interfaceLines[i]);
		return indices;
	}
	
	private int getPos(int item, int[] array)
	{
		for (int i=0; i<array.length; i++)
			if (item == array[i]) return i;
		return -1;
	}
	
	public int getTotalNumInterfaceLines()
	{
		int count = 0;
		for (int i=0; i<_size; i++)
			count += _interfaceLines[i].length;
		return count;
	}
	
	public void dataCheck() {
		_model.getDiary().hotLine(LogTypeXL.CheckPoint, "# of interface is: "+_size);
		int count = 0;
		for (int i=0; i<_size; i++)
			if (_isInterfaceActive[i] == true) count++;
		_model.getDiary().hotLine(LogTypeXL.CheckPoint, "# of active interface is: "+count);
	}

	public void dump()
	{
		String fileName = "InterfaceListXL.csv";
		File pout = new File(fileName);
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(new BufferedWriter(new FileWriter(pout)));
		} catch (IOException e) {
			e.printStackTrace();
		}
		dump(pw);
		pw.flush();
		pw.close();
		_model.getDiary().hotLine("Interface data for SCED is dumpped to the file "+fileName);
	}
	
	public void dump(PrintWriter pw)
	{
		double baseMVA = _model.getMVAbase();
		pw.println("idxInterface,auxIdx,interfaceLineIdx,interfaceLineDirection,isActive4BaseCase,totalNormalLimit,totalEmergencyLimit");
		for (int i=0; i<_size; i++)
		{
			int[] interfaceLines = _interfaceLines[i];
			int st = 1;
			if (_isInterfaceActive[i] == false) st = 0;
			for (int j=0; j<interfaceLines.length; j++)
			{
				String directionFlag = "True";
				if (_interfaceLinesDirection[i][j] == false) directionFlag = "False";
				pw.format("%d,%d,%d,%s,%d,%f,%f\n",
						(i+1),
						(j+1),
						(interfaceLines[j]+1),
						directionFlag,
						st,
						_totalLimitBaseCase[i]*baseMVA,
						_interfaceEmgcyLimits[i][j]*baseMVA);
			}
		}
	}

}
