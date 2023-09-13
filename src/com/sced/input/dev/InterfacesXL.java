package com.sced.input.dev;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

import com.sced.input.ReadModelDataXL;
import com.sced.util.AuxMethodXL;

/**
 * 
 * @author Xingpeng Li, xplipower@gmail.com 
 *
 */
public class InterfacesXL {

	ReadModelDataXL _model;
	
	int _size;
	int[] _interfaceIndex;
	int[] _isEnabled;
	double[] _interfaceLimit;
	
	int[][] _interfaceLineIdx;
	boolean[][] _interfaceLinesDirection; // if true, from-bus to to-bus; if false, to-bus to from-bus.
	
	public InterfacesXL(ReadModelDataXL model) {_model = model;}
	
	public void readData(File file) throws IOException
	{
		ArrayList<Integer> interfaceIndex = new ArrayList<Integer>();   
		ArrayList<Integer> isEnabled = new ArrayList<Integer>();    
		ArrayList<Double> interfaceLimit = new ArrayList<Double>();  

		_size = 0;
		System.out.println("Reading interface data ...");
		_model.getDiary().hotLine("Start reading interface data ...");
		Scanner reader = new Scanner(file);
		if (_model.hasHeading() == true) reader.nextLine();
		while (reader.hasNextLine() && reader.hasNext()) {
			_size++;
			interfaceIndex.add(reader.nextInt());
			isEnabled.add(reader.nextInt());
			interfaceLimit.add(reader.nextDouble());
		}
		reader.close();
		
		_interfaceIndex = AuxMethodXL.convtArrayListToInt(interfaceIndex);
		_isEnabled = AuxMethodXL.convtArrayListToInt(isEnabled);
		_interfaceLimit = AuxMethodXL.convtArrayListToDouble(interfaceLimit);
		
		System.out.println("   Finish reading interface data");
		_model.getDiary().hotLine("Finish reading interface data ...");
	}
	
	public void readDataInterfaceElem(File file) throws IOException
	{
		HashMap<Integer, ArrayList<Integer>> interfaceElem = new HashMap<Integer, ArrayList<Integer>>();

		System.out.println("Reading interface elements data ...");
		_model.getDiary().hotLine("Start reading interface elements data ...");
		Scanner reader = new Scanner(file);
		if (_model.hasHeading() == true) reader.nextLine();
		while (reader.hasNextLine() && reader.hasNext()) {
			int interfaceIdx = reader.nextInt();
			int brcIdx = reader.nextInt() - 1;  // branch index in the data file is supposed to start by 1, while starts by 0 in JAVA.  
			if (interfaceElem.containsKey(interfaceIdx)) interfaceElem.get(interfaceIdx).add(brcIdx);
			else {
				ArrayList<Integer> intArrayTmp = new ArrayList<Integer>();
				intArrayTmp.add(brcIdx);
				interfaceElem.put(interfaceIdx, intArrayTmp);
			}
		}
		reader.close();
		
		/* save data into member variables */
		_interfaceLineIdx = new int[_size][];
		for (int i=0; i<_size; i++) {
			int[] elems;
			int key = _interfaceIndex[i];
			if (interfaceElem.containsKey(key)) elems = AuxMethodXL.convtArrayListToInt(interfaceElem.get(key));
			else elems = new int[0];
			_interfaceLineIdx[i] = elems;
		}
		fillInterfaceLinesDirection();
		
		System.out.println("   Finish reading interface elements data");
		_model.getDiary().hotLine("Finish reading interface elements data ...");
	}
	
	private void fillInterfaceLinesDirection() {
		int row = _interfaceLineIdx.length;
		_interfaceLinesDirection = new boolean[row][];
		for (int i=0; i<row; i++)
		{
			int num = _interfaceLineIdx[i].length;
			_interfaceLinesDirection[i] = new boolean[num];
			Arrays.fill(_interfaceLinesDirection[i], true);
		}
	}

	public int size() {return _size;}
	public int getIsEnabled(int i) {return _isEnabled[i];}
	public double getInterfaceLimit(int i) {return _interfaceLimit[i];}
	public int[] getInterfaceLineIdx(int i) {return _interfaceLineIdx[i];}
	public boolean[] getInterfaceLineDirection(int i) {return _interfaceLinesDirection[i];}
	

}
