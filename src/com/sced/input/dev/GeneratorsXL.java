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
public class GeneratorsXL {

	ReadModelDataXL _model;
	BusesXL _buses;
	int _size;
	int _sizeGenCost;  // # of gens that have cost curve.
	int _sizeSegments;
	
	// base value from input file
	int[] _busIdx;    // bus index
//	int[] _busNum;    // bus number before reindex
	String[] _id;
	int[] _stat;
	
	double[] _pgInit;     // in MW
	double[] _qgInit;     // in MVAR
	double[] _pgmax;  // in MW
	double[] _pgmin;  // in MW
	double[] _qgmax;  // in MVAR
	double[] _qgmin;  // in MVAR
	double[] _vref;

	double[] _minup;   // minimum up time
	double[] _mindown; // minimum down time
	
	double[] _costSU;    // start up cost
	double[] _costNL;    // no load cost
//	double[] _costOp;    // operation cost
	
	double[] _energyRamp;   // in MW/min
	double[] _spinRamp;     // in MW/min
	int[] _costCurveFlag;   // 1 indicates that a cost curve is available, 0 denotes that no cost data for the associated gen
	int[] _mapToCostCurve;  // index of cost curve array; -1 means no cost curve available;
	double[][] _segmentBreadth;  // the number of 1-dimension array equals to the number of generators that have cost curve available.
	double[][] _segmentPrice; 
	
	public GeneratorsXL(ReadModelDataXL model) {_model = model; _buses = _model.getBuses();}
	
	public int size() {return _size;}
	public int sizeGenCost() {return _sizeGenCost;}
	public int sizeSegment() {return _sizeSegments;}
	public int getBusIdx(int i) {return _busIdx[i];}
	public String getId(int i) {return _id[i];}
	public int getStat(int i) {return _stat[i];}
	
	public double getPgInit(int i) {return _pgInit[i];}
	public double getQgInit(int i) {return _qgInit[i];}
	public double getPgmax(int i) {return _pgmax[i];}
	public double getPgmin(int i) {return _pgmin[i];}
	public double getQgmax(int i) {return _qgmax[i];}
	public double getQgmin(int i) {return _qgmin[i];}
	public double getVref(int i) {return _vref[i];}

	/** Get minimum up time */
	public double getMinUP(int i) {return _minup[i];}
	/** Get minimum down time */
	public double getMinDW(int i) {return _mindown[i];}
	
	/** Get start up cost */
	public double getCostSU(int i) {return _costSU[i];}
	/** Get no load cost */
	public double getCostNL(int i) {return _costNL[i];}
//	/** Get operation cost */
//	public double getCostOp(int i) {return _costOp[i];}
	
	public double getEnergyRamp(int i) {return _energyRamp[i];}
	public double getSpinRamp(int i) {return _spinRamp[i];}
	public boolean hasCostCurveData(int i) {return (_costCurveFlag[i] == 1) ? true : false;}

	public int getMapToCostCurve(int i) {return _mapToCostCurve[i];}
	public double[] getSegmentBreadth(int i) {return _segmentBreadth[i];}
	public double[] getSegmentPrice(int i) {return _segmentPrice[i];}
	
	
	public void readData(File file) throws IOException
	{
		ArrayList<Integer> busIdx = new ArrayList<Integer>();  
//		ArrayList<Integer> busNum = new ArrayList<Integer>();    
		ArrayList<String> id = new ArrayList<String>();    
		ArrayList<Integer> stat = new ArrayList<Integer>();   

		ArrayList<Double> pg = new ArrayList<Double>();   
//		ArrayList<Double> qg = new ArrayList<Double>();    
		ArrayList<Double> pgmax = new ArrayList<Double>();  
		ArrayList<Double> pgmin = new ArrayList<Double>();  
//		ArrayList<Double> qgmax = new ArrayList<Double>();    
//		ArrayList<Double> qgmin = new ArrayList<Double>();   
//		ArrayList<Double> vref = new ArrayList<Double>();

		ArrayList<Double> energyRamp = new ArrayList<Double>();   
		ArrayList<Double> spinRamp = new ArrayList<Double>();
		ArrayList<Integer> costCurveFlag = new ArrayList<Integer>();

		_size = 0;
		System.out.println("Reading generator data ...");
		_model.getDiary().hotLine("Start reading generator data ...");
		Scanner reader = new Scanner(file);
		if (_model.hasHeading() == true) reader.nextLine();
		while (reader.hasNextLine() && reader.hasNext()) {
			_size++;
					
			int busNumber = reader.nextInt();
			busIdx.add(_buses.getIdx(busNumber));
//			busNum.add(busNumber);
			
			id.add(reader.next());
			stat.add(reader.nextInt());

			pg.add(reader.nextDouble());
//			qg.add(reader.nextDouble());
			pgmax.add(reader.nextDouble());
			pgmin.add(reader.nextDouble());
//			qgmax.add(reader.nextDouble());
//			qgmin.add(reader.nextDouble());
//			vref.add(reader.nextDouble());		

			energyRamp.add(reader.nextDouble());
			spinRamp.add(reader.nextDouble());
			costCurveFlag.add(reader.nextInt());
		}
		reader.close();
		
		/* save data into member variables */
		_busIdx = AuxMethodXL.convtArrayListToInt(busIdx);
//		_busNum = AuxMethod.convtArrayListToInt(busNum);
		_id = AuxMethodXL.convtArrayListToStr(id);
		_stat = AuxMethodXL.convtArrayListToInt(stat);
		
		_pgInit = AuxMethodXL.convtArrayListToDouble(pg);
//		_qg = AuxMethod.convtArrayListToDouble(qg);
		_pgmax = AuxMethodXL.convtArrayListToDouble(pgmax);
		_pgmin = AuxMethodXL.convtArrayListToDouble(pgmin);
//		_qgmax = AuxMethod.convtArrayListToDouble(qgmax);
//		_qgmin = AuxMethod.convtArrayListToDouble(qgmin);
//		_vref = AuxMethod.convtArrayListToDouble(vref);
		
		_energyRamp = AuxMethodXL.convtArrayListToDouble(energyRamp);
		_spinRamp = AuxMethodXL.convtArrayListToDouble(spinRamp);
		_costCurveFlag = AuxMethodXL.convtArrayListToInt(costCurveFlag);

		System.out.println("   Finish reading generator data");
		_model.getDiary().hotLine("Finish reading generator data ...");
	}
	
	public void readDataGensCost(File file) throws IOException
	{
		HashMap<Integer, ArrayList<Double>> segmentBreadth = new HashMap<Integer, ArrayList<Double>>();
		HashMap<Integer, ArrayList<Double>> segmentPrice = new HashMap<Integer, ArrayList<Double>>();
		
		_sizeSegments = 0;
		_sizeGenCost = 0;
		System.out.println("Reading generator cost data ...");
		_model.getDiary().hotLine("Start reading generator cost data ...");
		Scanner reader = new Scanner(file);
		if (_model.hasHeading() == true) reader.nextLine();
		while (reader.hasNextLine() && reader.hasNext()) {
			_sizeSegments++;
			int genIdx = reader.nextInt() - 1;  //Generator index in the data file is supposed to start by 1, while starts by 0 in JAVA.
			reader.nextInt();
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
			} else _mapToCostCurve[i] = -1;
		}
		
		System.out.println("   Finish reading generator cost data");
		_model.getDiary().hotLine("Finish reading generator cost data ...");
	}
	
}


