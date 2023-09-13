package com.sced.input.dev;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import com.sced.input.ReadModelDataXL;
import com.sced.util.AuxMethodXL;

/**
 * 
 * @author Xingpeng Li, xplipower@gmail.com 
 *
 */
public class BranchContingenciesXL {

	ReadModelDataXL _model;
	
	int _size;
	int[] _ctgcyIndex; //
	int[] _isEnabled;
	
	int[][] _ctgcyBranchIdx;
	
	public BranchContingenciesXL(ReadModelDataXL model) {_model = model;}
	
	public void readData(File file) throws IOException
	{
		ArrayList<Integer> ctgcyIndex = new ArrayList<Integer>();   
		ArrayList<Integer> isEnabled = new ArrayList<Integer>();    

		_size = 0;
		System.out.println("Reading contingency data ...");
		_model.getDiary().hotLine("Start reading contingency data ...");
		Scanner reader = new Scanner(file);
		if (_model.hasHeading() == true) reader.nextLine();
		while (reader.hasNextLine() && reader.hasNext()) {
			_size++;
			ctgcyIndex.add(reader.nextInt());
			isEnabled.add(reader.nextInt());
		}
		reader.close();
		
		_ctgcyIndex = AuxMethodXL.convtArrayListToInt(ctgcyIndex);
		_isEnabled = AuxMethodXL.convtArrayListToInt(isEnabled);
		
		System.out.println("   Finish reading contingency data");
		_model.getDiary().hotLine("Finish reading contingency data ...");
	}
	
	public void readDataCtgcyElem(File file) throws IOException
	{
		HashMap<Integer, ArrayList<Integer>> ctgcyElem = new HashMap<Integer, ArrayList<Integer>>();

		System.out.println("Reading contingency elements data ...");
		_model.getDiary().hotLine("Start reading contingency elements data ...");
		Scanner reader = new Scanner(file);
		if (_model.hasHeading() == true) reader.nextLine();
		while (reader.hasNextLine() && reader.hasNext()) {
			int ctgcyIdx = reader.nextInt();
			int brcIdx = reader.nextInt() - 1;  // branch index in the data file is supposed to start by 1, while starts by 0 in JAVA.  
			if (ctgcyElem.containsKey(ctgcyIdx)) ctgcyElem.get(ctgcyIdx).add(brcIdx);
			else {
				ArrayList<Integer> intArrayTmp = new ArrayList<Integer>();
				intArrayTmp.add(brcIdx);
				ctgcyElem.put(ctgcyIdx, intArrayTmp);
			}
		}
		reader.close();
		
		/* save data into member variables */
		_ctgcyBranchIdx = new int[_size][];
		for (int i=0; i<_size; i++) {
			int[] elems;
			int key = _ctgcyIndex[i];
			if (ctgcyElem.containsKey(key)) elems = AuxMethodXL.convtArrayListToInt(ctgcyElem.get(key));
			else elems = new int[0];
			_ctgcyBranchIdx[i] = elems;
		}
		
		System.out.println("   Finish reading contingency elements data");
		_model.getDiary().hotLine("Finish reading contingency elements data ...");
	}
	
	public int size() {return _size;}
	public boolean isActive(int i) {return (_isEnabled[i]==1) ? true : false;} 
	public int[] getCtgcyIndex() {return _ctgcyIndex;}
	public int[] getCtgcyBrcIdx(int i) {return _ctgcyBranchIdx[i];}
	
	
}
