package com.sced.model.data;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

import com.sced.model.SystemModelXL;
import com.sced.model.data.base.OneTermDevList;
import com.sced.model.data.elem.LoadXL;
import com.utilxl.log.LogTypeXL;

/**
 * 
 * @author Xingpeng Li, xplipower@gmail.com 
 *
 */
public class LoadListXL extends OneTermDevList {

	public enum LoadType {
		/** Actual load */   													Real, 
		/** Virtual load for modeling loss, or import/export tie-line flow */   Virtual, 
	}
	
	int _sizeRealLoad;       // number of real loads, the loads with index from 0 to (_sizeRealLoad-1) should be real while other loads should be virtual.
	
	double[] _PloadInit;  // in p.u., initial P load, right before redispatch per RT-SCED
	double[] _Pload;    // in p.u., length = sizeLoad. Forecasing/target load, right after redispatch per SCED
	LoadType[] _loadType;
	
	double[] _busPLoad; // in p.u., load at each bus, length = sizeBus;
	double[] _busPLoadInit; // in p.u., load at each bus, length = sizeBus;
	
	public LoadListXL(SystemModelXL model) {super(model);}
	
	public void setSizeRealLoad(int sizeRealLoad) {_sizeRealLoad = sizeRealLoad;}
	public void setSize(int size) {_size = size; initSize();}
	public void setSvcSt(int idx, boolean st) {_st[idx] = st;}
	public void setPloadInit(int idx, double ploadInit) {_PloadInit[idx] = ploadInit;}
	public void setPload(int idx, double pload) {_Pload[idx] = pload;}
	public void setLoadType(int idx, LoadType type) {_loadType[idx] = type;}
	/** Set _PloadInit the same with _Pload */
	public void setPloadPerPloadInit() {
		for (int i=0; i<_size; i++)
			_Pload[i] = _PloadInit[i];
	}
	
	private void initSize() {
		_busIdx = new int[_size];
		_st = new boolean[_size];
		_Pload = new double[_size];
		_PloadInit = new double[_size];
		
		_loadType = new LoadType[_size];
		Arrays.fill(_loadType, LoadType.Real);
	}

	public int sizeRealLoad() {return _sizeRealLoad;}
	public LoadXL getLoad(int i) {return new LoadXL(this, i);}
	public double getPLoadInit(int i) {return _PloadInit[i];}
	public double getPLoad(int i) {return _Pload[i];}
	public double[] getPLoad() {return _Pload;}
	public LoadType getLoadType(int i) {return _loadType[i];}
	/** Return the difference (Pload - PloadInit) */
	public double getPloadDiff(int i) {return (_Pload[i] - _PloadInit[i]);}
	
	/** Get active power load */
	public double getBusLoad(int idxBus) {
		if (_busPLoad == null) calcBusPLoad();
		return _busPLoad[idxBus];
	}

	/** Get active power loads */ 
	public double[] getBusLoad() {
		if (_busPLoad == null) calcBusPLoad();
		return _busPLoad;
	}
	private void calcBusPLoad() {
		_busPLoad = new double[_model.getBuses().size()];
		for (int i=0; i<_size; i++) {
			if (_st[i] == false) continue;
			int idxBus = _busIdx[i];
			_busPLoad[idxBus] += _Pload[i];
		}
	}
	public void clearBusLoad() {_busPLoad = null;}

	/** Get initial active power load */
	public double getBusLoadInit(int idxBus) {
		if (_busPLoadInit == null) calcBusPLoadInit();
		return _busPLoadInit[idxBus];
	}

	/** Get initial active power loads */ 
	public double[] getBusLoadInit() {
		if (_busPLoadInit == null) calcBusPLoadInit();
		return _busPLoadInit;
	}
	private void calcBusPLoadInit() {
		_busPLoadInit = new double[_model.getBuses().size()];
		for (int i=0; i<_size; i++) {
			if (_st[i] == false) continue;
			int idxBus = _busIdx[i];
			_busPLoadInit[idxBus] += _PloadInit[i];
		}
	}
	public void clearBusLoadInit() {_busPLoadInit = null;}

	/** total in-service load */
	public double getTotalPLoadInit() {return calcTotalPLoad(_PloadInit);}
	public double getTotalPLoad() {return calcTotalPLoad(_Pload);}
	private double calcTotalPLoad(double[] pload) {
		double totalPload = 0;
		for (int i=0; i<_size; i++)
		{
			if (_st[i] == false) continue;
			totalPload += pload[i];
		}
		return totalPload;
	}
	
	/** total in-service real load */
	public double getTotalPRealLoadInit() {return calcTotalPRealLoad(_PloadInit);}
	public double getTotalPRealLoad() {return calcTotalPRealLoad(_Pload);}
	private double calcTotalPRealLoad(double[] pload) {
		double totalPload = 0;
		for (int i=0; i<_size; i++)
		{
			if (_st[i] == false) continue;
			if (_loadType[i] == LoadType.Virtual) continue; 
			totalPload += pload[i];
		}
		return totalPload;
	}
	
	public void dump()
	{
		String fileName = "LoadListXL.csv";
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
		_model.getDiary().hotLine("Load data for SCED is dumpped to the file "+fileName);
	}
	
	public void dump(PrintWriter pw)
	{
		double baseMVA = _model.getMVAbase();
		pw.println("loadIdx,loadBusIdx,status,pLoadInit,pLoad,loadType");
		for (int i=0; i<_size; i++)
		{
			int st = 1;
			if (_st[i] == false) st = 0;
			pw.format("%d,%d,%d,%f,%f,%s\n",
					(i+1),
					(_busIdx[i]+1),
					st,
					_PloadInit[i]*baseMVA,
					_Pload[i]*baseMVA,
					_loadType[i].toString());
		}
	}

	public void dataCheck() {
		_model.getDiary().hotLine(LogTypeXL.CheckPoint, "# of loads is: "+_size);
		_model.getDiary().hotLine(LogTypeXL.CheckPoint, "# of real loads is: "+_sizeRealLoad);
		_model.getDiary().hotLine(LogTypeXL.CheckPoint, "# of virtual loads is: "+(_size-_sizeRealLoad));
		_model.getDiary().hotLine(LogTypeXL.CheckPoint, "Total in-service initial load is "+getTotalPLoadInit()+" p.u.");
		_model.getDiary().hotLine(LogTypeXL.CheckPoint, "Total in-service initial real load is "+getTotalPRealLoadInit()+" p.u.");
		_model.getDiary().hotLine(LogTypeXL.CheckPoint, "Total in-service forecasting load is "+getTotalPLoad()+" p.u.");
		_model.getDiary().hotLine(LogTypeXL.CheckPoint, "Total in-service forecasting real load is "+getTotalPRealLoad()+" p.u.");
		_model.getDiary().hotLine(LogTypeXL.CheckPoint, sizeInSvc()+" loads out of "+size()+" are in service");
		
		int count1 = 0;
		int countNeg = 0;
		double[] busPLoad = getBusLoad();
		for (int i=0; i<busPLoad.length; i++) 
		{
			if (busPLoad[i] != 0) {
				count1++;
				if (busPLoad[i] < 0) countNeg++;
			}
		}
		_model.getDiary().hotLine(LogTypeXL.CheckPoint, count1+" buses out of "+busPLoad.length+" have loads");
		_model.getDiary().hotLine(LogTypeXL.CheckPoint, countNeg+" buses out of "+busPLoad.length+" have negative loads");
	}
	
}
