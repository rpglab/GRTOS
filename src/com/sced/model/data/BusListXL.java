package com.sced.model.data;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

import com.sced.model.SystemModelXL;
import com.sced.model.data.elem.BusXL;
import com.utilxl.log.LogTypeXL;

/**
 * 
 * @author Xingpeng Li, xplipower@gmail.com 
 *
 */
public class BusListXL {

	SystemModelXL _model;
	
	/** Match bus number (_i) with re-index numbers (0 .. _size-1). 
	Key is bus number, value is bus index. */
	HashMap<Integer, Integer> _numToIdx;
	int[] _busNumber;
	
	String[] _busID;     // bus name
	
	int _size;
	double _basekv[];
	int _area[];
	double _vm[];    // in per unit
	double _va[];    // in radian
	int _slackBusIdx = 0; // default slack bus
	
	public BusListXL (SystemModelXL model) {_model = model;}


	public void setSize(int size) {_size = size; initSize();}
	public void setSlackBusIdx(int idx) {_slackBusIdx = idx;}
	public void setBusNumMapIdx(HashMap<Integer, Integer> numToIdx) {_numToIdx = numToIdx;}
	public void setBusNumber(int idx, int num) { _busNumber[idx] = num;}
	public void setBaseKV(int idx, double dd) { _basekv[idx] = dd;}
	public void setArea(int idx, int dd) { _area[idx] = dd;}
	public void setVm(int idx, double dd) { _vm[idx] = dd;}
	public void setVa(int idx, double dd) { _va[idx] = dd;}

	public void setBusID(int idx, String id) { _busID[idx] = id;}

	/** Re-index bus number from 0 to numBus-1,
	 * this involves each data item that has a bus number. */
/*	public void reindex() {
		_numToIdx = new HashMap<Integer, Integer>();
		for (int i=0; i<_size; i++)
			_numToIdx.put(_busNumber[i], i);
	} */
	
	/** Given a bus number, get the bus index */
	public int getIdx(int busNumber) {return _numToIdx.get(busNumber);}
	public int getBusNumber(int i) {return _busNumber[i];}
	public String getBusID(int i) {return _busID[i];}
	public BusXL getBus(int i) {return new BusXL(this, i);}
	public int size() {return _size;}
	public int getSlackBusIdx() {return _slackBusIdx;}
	
	private void initSize() {
		_busNumber = new int[_size];
		_basekv = new double[_size];
		_area = new int[_size];
		_va = new double[_size];
		_vm = new double[_size];
		_busID = new String[_size];
	}
	
	public void calcBusNumberMap()
	{
		_numToIdx = new HashMap<Integer, Integer>();
		for (int i=0; i<_size; i++)
			_numToIdx.put(_busNumber[i], i);
	}
	
	public void dataCheck() {
		_model.getDiary().hotLine(LogTypeXL.CheckPoint, "# of buses is: "+_size);
	}
	
	public void dump()
	{
		String fileName = "BusListXL.csv";
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
		_model.getDiary().hotLine("Bus data for SCED is dumpped to the file "+fileName);
	}
	
	public void dump(PrintWriter pw)
	{
		double baseMVA = _model.getMVAbase();
		pw.println("busIdx,busNumber,busID,baseKV,area,vm,va,vaInDegree,pload");
		for (int i=0; i<_size; i++)
		{
			pw.format("%d,%d,%s,%f,%d,%f,%f,%f,%f\n",
					(i+1),
					_busNumber[i],
					_busID[i],
					_basekv[i],
					_area[i],
					_vm[i],
					_va[i],
					_va[i]*180/Math.PI,
					_model.getLoads().getBusLoad(i)*baseMVA);
		}
	}

}
