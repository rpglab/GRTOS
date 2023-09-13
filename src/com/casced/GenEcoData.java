package com.casced;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

import com.sced.model.param.ParamInput;
import com.sced.util.AuxMethodXL;
import com.utilxl.log.DiaryXL;

public class GenEcoData {

	DiaryXL _diary;
	
	int _size;         // # of generators
	int _sizeGenCost;  // # of gens that have cost curve.
	int _sizeSegments;
	
	double[] _energyRamp;   // in MW/min
	double[] _spinRamp;     // in MW/min
	
	boolean[] _costCurveFlag;   // true indicates that a cost curve is available, false denotes that no cost data for the associated gen
	int[] _mapToCostCurve;  // index of cost curve array; -1 means no cost curve available;
	double[][] _segmentBreadth;  // the number of 1-dimension array equals to the number of generators that have cost curve available.
	double[][] _segmentPrice; 
	
	public GenEcoData(DiaryXL diary, int size) {_diary = diary;_size = size; init();}

	String _genCostFilePath;
	String _genRampFilePath;
	public void init() {
		_genCostFilePath = ParamInput.getOpenPAGenCostFilePath();
		_genRampFilePath = ParamInput.getOpenPAGenRampFilePath();
	}
	
	public int size() {return _size;}
	public int sizeGenCost() {return _sizeGenCost;}
	public int sizeSegment() {return _sizeSegments;}
	
	public double getEnergyRamp(int i) {return _energyRamp[i];}
	public double getSpinRamp(int i) {return _spinRamp[i];}
	public boolean hasCostCurveData(int i) {return _costCurveFlag[i];}

	public int getMapToCostCurve(int i) {return _mapToCostCurve[i];}
	public double[] getSegmentBreadth(int i) {return _segmentBreadth[i];}
	public double[] getSegmentPrice(int i) {return _segmentPrice[i];}

	public void readGensCost() throws IOException {readGensCost(new File(_genCostFilePath));}
	public void readGensCost(File file) throws IOException
	{
		HashMap<Integer, ArrayList<Double>> segmentBreadth = new HashMap<Integer, ArrayList<Double>>();
		HashMap<Integer, ArrayList<Double>> segmentPrice = new HashMap<Integer, ArrayList<Double>>();
		
		_sizeSegments = 0;
		_sizeGenCost = 0;
		//System.out.println("Reading generator cost data ...");
		_diary.hotLine("Start reading generator cost data");
		Scanner reader = new Scanner(file);
		reader.nextLine(); // skip heading line
		while (reader.hasNextLine() && reader.hasNext()) {
			_sizeSegments++;
			int genIdx = reader.nextInt() - 1;  //Generator index in the data file is supposed to start by 1, while starts by 0 in JAVA.
			reader.nextInt();   // skip segment index
			if (segmentBreadth.containsKey(genIdx)) {
				segmentBreadth.get(genIdx).add(reader.nextDouble());
				segmentPrice.get(genIdx).add(reader.nextDouble());
			}
			else {
				_sizeGenCost++;
				ArrayList<Double> tmpArray = new ArrayList<Double>();
				tmpArray.add(reader.nextDouble());
				segmentBreadth.put(genIdx, tmpArray);
				
				ArrayList<Double> tmp2Array = new ArrayList<Double>();
				tmp2Array.add(reader.nextDouble());
				segmentPrice.put(genIdx, tmp2Array);
			}
		}
		reader.close();

		/* save data into member variables */
		_costCurveFlag = new boolean[_size];
		Arrays.fill(_costCurveFlag, true);
		_mapToCostCurve = new int[_size];
		_segmentBreadth = new double[_size][];
		_segmentPrice = new double[_size][];
		int idxMapping = 0;
		for (int i=0; i<_size; i++) {
			if (segmentBreadth.containsKey(i)) {
				_mapToCostCurve[i] = idxMapping;
				double[] breadths = AuxMethodXL.convtArrayListToDouble(segmentBreadth.get(i));
				_segmentBreadth[idxMapping] = breadths;
				double[] prices = AuxMethodXL.convtArrayListToDouble(segmentPrice.get(i));
				_segmentPrice[idxMapping] = prices;
				idxMapping++;
			} else {_costCurveFlag[i] = false; _mapToCostCurve[i] = -1;}
		}
		
		//System.out.println("   Finish reading generator cost data");
		_diary.hotLine("Finish reading generator cost data");
	}

	public void readGensRamp() throws IOException {readGensRamp(new File(_genRampFilePath));}
	public void readGensRamp(File file) throws IOException
	{
		_energyRamp = new double[_size];
		_spinRamp = new double[_size];
		
		_diary.hotLine("Start reading generator ramp data");
		Scanner reader = new Scanner(file);
		reader.nextLine(); // skip heading line
		while (reader.hasNextLine() && reader.hasNext()) {
			int genIdx = reader.nextInt() - 1;  //Generator index in the data file is supposed to start by 1, while starts by 0 in JAVA.
			double ramp = reader.nextDouble();
			_energyRamp[genIdx] = ramp;
			_spinRamp[genIdx] = ramp;
		}
		reader.close();
		_diary.hotLine("Finish reading generator ramp data");
	}

	
	
}
