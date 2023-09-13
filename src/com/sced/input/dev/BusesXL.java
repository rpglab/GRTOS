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
public class BusesXL {

	ReadModelDataXL _model;
	int _size;
	
	/** Match bus number (_i) with re-index numbers (0 .. _size).
	Key is bus number, value is bus index. */
	HashMap<Integer, Integer> _numToIdx;

	// base value from file
	int[] _i;      // bus number
	int[] _ide;   // bus type
	double[] _basekv;
	
	double[] _gl;   // bus GL shunt, G value in MW at unit bus voltage
	double[] _bl;   // bus BL shunt, B value in MVar at unit bus voltage
	
	double _vm[];    // in per unit
	double _va[];    // in radian

	int _area[];
	int _zone[];
	
	public BusesXL(ReadModelDataXL model) {_model = model;}
	
	public void readData(File file) throws IOException
	{
		HashMap<Integer, Integer> numToIdx = new HashMap<Integer, Integer>();
		ArrayList<Integer> i = new ArrayList<Integer>();
//		ArrayList<Integer> ide = new ArrayList<Integer>();
		ArrayList<Double> basekv = new ArrayList<Double>();
		
//		ArrayList<Double> gl = new ArrayList<Double>();
//		ArrayList<Double> bl = new ArrayList<Double>();
		
//		ArrayList<Double> vm = new ArrayList<Double>();
		ArrayList<Double> va = new ArrayList<Double>();
		
		ArrayList<Integer> area = new ArrayList<Integer>();
//		ArrayList<Integer> zone = new ArrayList<Integer>();
		
		_size = 0;
		System.out.println("Reading bus data ...");
		_model.getDiary().hotLine("Start reading bus data ...");
		Scanner reader = new Scanner(file);
		if (_model.hasHeading() == true) reader.nextLine();
		while (reader.hasNextLine() && reader.hasNext()) {
			int busNum = reader.nextInt();
			numToIdx.put(busNum, _size++);
			
			i.add(busNum);
//			ide.add(reader.nextInt());
			
			basekv.add(reader.nextDouble());
//			gl.add(reader.nextDouble());
//			bl.add(reader.nextDouble());
//			vm.add(reader.nextDouble());
			va.add(reader.nextDouble());
			
			area.add(reader.nextInt());
//			zone.add(reader.nextInt());
		}
		reader.close();
		
		/* save data into member variables */
		_numToIdx = numToIdx;
		_i = AuxMethodXL.convtArrayListToInt(i);
//		_ide = AuxMethod.convtArrayListToInt(ide);
		
		_basekv = AuxMethodXL.convtArrayListToDouble(basekv);
//		_gl = AuxMethod.convtArrayListToDouble(gl);
//		_bl = AuxMethod.convtArrayListToDouble(bl);
//		_vm = AuxMethod.convtArrayListToDouble(vm);
		_va = AuxMethodXL.convtArrayListToDouble(va);

		_area = AuxMethodXL.convtArrayListToInt(area);
//		_zone = AuxMethod.convtArrayListToInt(zone);
		
		System.out.println("   Finish reading bus data");
		_model.getDiary().hotLine("Finish reading bus data ...");
	}

	/** mapping for bus number and bus index */
	public HashMap<Integer, Integer> getMapMatrix() {return _numToIdx;}
	/** Given a bus number, get the bus index */
	public int getIdx(int busNumber) {return _numToIdx.get(busNumber);}
    /** number of buses */
	public int size() {return _size;}
	/** Get bus number */
	public int getBusNum(int i) {return _i[i];}
	/** Get bus voltage level */
	public double getBaseKV(int i) {return _basekv[i];}
	/** Get bus G shunt */
	public double getGL(int i) {return _gl[i];}
	/** Get bus B shunt */
	public double getBL(int i) {return _bl[i];}
	/** Get bus voltage magnitude */
	public double getVm(int i) {return _vm[i];}
	/** Get bus voltage angle */
	public double getVa(int i) {return _va[i];}
	/** Get area */
	public int getArea(int i) {return _area[i];}
	/** Get zone */
	public int getZone(int i) {return _zone[i];}
	
	
	
	
}
