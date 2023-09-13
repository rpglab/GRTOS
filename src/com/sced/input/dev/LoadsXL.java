package com.sced.input.dev;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import com.sced.input.ReadModelDataXL;
import com.sced.util.AuxMethodXL;

/**
 * 
 * @author Xingpeng Li, xplipower@gmail.com 
 *
 */
public class LoadsXL {

	ReadModelDataXL _model;
	BusesXL _buses;
	int _size;
	
	// base value from input file
	int[] _busIdx;    // bus index
//	int[] _busNum;    // bus number before reindex
	String[] _id;
	int[] _stat;
	
	double[] _pload;   // active load in MW
	double[] _qload;   // reactive load in MVAR

	public int size() {return _size;}
	public int getBusIdx(int idx) {return _busIdx[idx];}
	public String getID(int idx) {return _id[idx];}
	public int getStat(int idx) {return _stat[idx];}
	public double getPload(int idx) {return _pload[idx];}
	public double getQload(int idx) {return _qload[idx];}
	
	
	public LoadsXL(ReadModelDataXL model) {_model = model; _buses = _model.getBuses();}

	public void readData(File file) throws IOException
	{
		ArrayList<Integer> busIdx = new ArrayList<Integer>();  
//		ArrayList<Integer> busNum = new ArrayList<Integer>();    
		ArrayList<String> id = new ArrayList<String>();    
		ArrayList<Integer> stat = new ArrayList<Integer>();   

		ArrayList<Double> pload = new ArrayList<Double>();   
//		ArrayList<Double> qload = new ArrayList<Double>();    

		_size = 0;
		System.out.println("Reading load data ...");
		_model.getDiary().hotLine("Start reading load data ...");
		Scanner reader = new Scanner(file);
		if (_model.hasHeading() == true) reader.nextLine();
		while (reader.hasNextLine() && reader.hasNext()) {
			_size++;
			
			int busNumber = reader.nextInt();
			busIdx.add(_buses.getIdx(busNumber));
//			busNum.add(busNumber);
			
			id.add(reader.next());
			stat.add(reader.nextInt());

			pload.add(reader.nextDouble());
//			qload.add(reader.nextDouble());
		}
		reader.close();
		
		/* save data into member variables */
		_busIdx = AuxMethodXL.convtArrayListToInt(busIdx);
//		_busNum = AuxMethod.convtArrayListToInt(busNum);
		_id = AuxMethodXL.convtArrayListToStr(id);
		_stat = AuxMethodXL.convtArrayListToInt(stat);
		
		_pload = AuxMethodXL.convtArrayListToDouble(pload);
//		_qload = AuxMethod.convtArrayListToDouble(qload);

		System.out.println("   Finish reading load data");
		_model.getDiary().hotLine("Finish reading load data ...");
	}

	
	
}
