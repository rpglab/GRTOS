package com.sced.model.data;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import com.sced.model.SystemModelXL;
import com.sced.model.data.base.OneTermDevList;
import com.sced.model.data.elem.GenXL;
import com.utilxl.log.DiaryXL;
import com.utilxl.log.LogTypeXL;

/**
 * 
 * @author Xingpeng Li, xplipower@gmail.com 
 *
 */
public class GenListXL extends OneTermDevList {

	int _sizeGenCost;  // # of gens that have cost curve.
	int _sizeSegments; // # of total cost segments.

	double[] _pgInit;
	double[] _pgmax;
	double[] _pgmin;
	
	double[] _minup;
	double[] _mindown;
	double[] _costSU;
	double[] _costNL;

	double[] _energyRamp;   // in p.u./min
	double[] _spinRamp;     // in p.u./min
	boolean[] _costCurveFlag;   // 1 indicates that a cost curve is available, 0 denotes that no cost data for the associated gen
	int[] _mapToCostCurve;  // index of cost curve array; -1 means no cost curve available;
	double[][] _segmentBreadth;  // the number of 1-dimension array equals to the number of generators that have cost curve available.
	double[][] _segmentPrice; 
	
	public GenListXL(SystemModelXL model) {super(model);}
	
	public int sizeGenCost() {return _sizeGenCost;}
	public int sizeCostSegment() {return _sizeSegments;}

	
	public void setSize(int size) {_size = size; initSize();}
	public void setSizeGenCost(int size) {_sizeGenCost = size; initSizeGenCost();}
	public void setSizeCostSegment(int size) {_sizeSegments = size;}
	
	public void setSvcSt(int idx, boolean st) {_st[idx] = st;}
	
	public void setPgInit(int idx, double pg) {_pgInit[idx] = pg;}
	public void setPgmax(int idx, double pgmax) {_pgmax[idx] = pgmax;}
	public void setPgmin(int idx, double pgmin) {_pgmin[idx] = pgmin;}
	
	public void setMinup(int idx, double minup) {_minup[idx] = minup;}
	public void setMindown(int idx, double mindown) {_mindown[idx] = mindown;}
	public void setCostSU(int idx, double costSU) {_costSU[idx] = costSU;}
	public void setCostNL(int idx, double costNL) {_costNL[idx] = costNL;}
//	public void setCostOp(int idx, double costOp) {_costOp[idx] = costOp;}

	public void setEnergyRamp(int idx, double ramp) {_energyRamp[idx] = ramp;}
	public void setSpinRamp(int idx, double ramp) {_spinRamp[idx] = ramp;}
	public void setCostCurveFlag(int idx, boolean costCurveFlag) {_costCurveFlag[idx] = costCurveFlag;}
	public void setMapToCostCurve(int idx, int mapToCostCurve) {_mapToCostCurve[idx] = mapToCostCurve;}
	public void setSegmentBreadth(int idx, double[] genSegmentBreadth) {_segmentBreadth[idx] = genSegmentBreadth;}
	public void setSegmentPrice(int idx, double[] genSegmentPrice) {_segmentPrice[idx] = genSegmentPrice;}

	private void initSize() {
		_busIdx = new int[_size];
		_st = new boolean[_size];
		_pgInit = new double[_size];
		_pgmax = new double[_size];
		_pgmin = new double[_size];
		
		_minup = new double[_size];
		_mindown = new double[_size];
		_costSU = new double[_size];
		_costNL = new double[_size];
		
		_energyRamp = new double[_size];
		_spinRamp = new double[_size];
		_costCurveFlag = new boolean[_size];
		_mapToCostCurve = new int[_size];
	}
	
	private void initSizeGenCost() {
		_segmentBreadth =  new double[_sizeGenCost][];
		_segmentPrice =  new double[_sizeGenCost][];
	}
	
	public GenXL getGen(int i) {return new GenXL(this, i);}

	public double[] getPgInit() {return _pgInit;}
	public double[] getPgmax() {return _pgmax;}
	public double[] getPgmin() {return _pgmin;}
	public double getPgInit(int g) {return _pgInit[g];}
	public double getPgmax(int g) {return _pgmax[g];}
	public double getPgmin(int g) {return _pgmin[g];}

	/** Get total active power generation */
	public double getTotalPgInit() {
		double totalPg = 0;
		for (int i=0; i<_size; i++) {
			if (_st[i] == false) continue;
			totalPg += _pgInit[i];
		}
		return totalPg;
	}
	
	public double getEnergyRamp(int i) {return _energyRamp[i];}
	public double getSpinRamp(int i) {return _spinRamp[i];}
	
	public boolean hasCostCurveFlag(int i) {return _costCurveFlag[i];}
	public int getIdxMapToCostCurve(int i) {return _mapToCostCurve[i];}
	
	public double[][] getSegmentBreadth() {return _segmentBreadth;}
	public double[][] getSegmentPrice() {return _segmentPrice;}
	
	
	/** Check input model data and fix them if needed */
	public void dataCheck() {
		DiaryXL diary = _model.getDiary();
		diary.hotLine(LogTypeXL.CheckPoint, "# of generators is: "+_size);
		for (int i=0; i<_size; i++) {
			if (_st[i] == false) continue;
			if (_pgInit[i] > _pgmax[i]) {
				diary.hotLine(LogTypeXL.Warning, "At bus "+_model.getBuses().getBusNumber(_busIdx[i]) + ", Gen "+i+", pg > pgmax, pgmax("+_pgmax[i]+") is set to pg(" + _pgInit[i]+")");
				_pgmax[i] = _pgInit[i];
			}
			if (_pgInit[i] < _pgmin[i]) {
				diary.hotLine(LogTypeXL.Warning, "At bus "+_model.getBuses().getBusNumber(_busIdx[i]) + ", Gen "+i+", pg < pgmin, pgmin("+_pgmin[i]+") is set to pg(" + _pgInit[i]+")");
				_pgmin[i] = _pgInit[i];
			}
			if (_pgInit[i] < 0) {
				diary.hotLine(LogTypeXL.Warning, "At bus "+_model.getBuses().getBusNumber(_busIdx[i]) + ", Gen "+i+", pg < 0");
			}
		}
		diary.hotLine(LogTypeXL.CheckPoint, sizeInSvc()+" generators out of "+size()+" are in service");
		
		double totalPg = 0;
		double totalPgmax = 0;
		double totalPgmaxAll = 0;
		for (int i=0; i<_size; i++)
		{
			totalPg += _pgInit[i];
			if (_st[i] == true) totalPgmax += _pgmax[i];
			totalPgmaxAll += _pgmax[i];
		}
		diary.hotLine(LogTypeXL.CheckPoint, "Total online generation capacity is "+ totalPgmax+" p.u.");
		diary.hotLine(LogTypeXL.CheckPoint, "Total generation capacity is "+ totalPgmaxAll+" p.u.");
		diary.hotLine(LogTypeXL.CheckPoint, "Total initial generation is "+ totalPg+" p.u.");
	}
	
	/** Only count online generators */
	public double getPgMaxSum(int[] genIdx)
	{
		if (genIdx == null) return 0;
		double pgMaxSum = 0;
		for (int g=0; g<genIdx.length; g++)
			if (_st[genIdx[g]] == true) pgMaxSum += _pgmax[genIdx[g]];
		return pgMaxSum;
	}

	/** Only count online generators */
	public double getPgInitSum(int[] genIdx)
	{
		if (genIdx == null) return 0;
		double pgInitSum = 0;
		for (int g=0; g<genIdx.length; g++)
			if (_st[genIdx[g]] == true) pgInitSum += _pgInit[genIdx[g]];
		return pgInitSum;
	}
	
	
	/** Report the system total operating (variable) cost */
	public double getInitSysTotalVarCost() {return getSysTotalVarCost(_pgInit);}

	/** Report the system total operating (variable) cost */
	public double getSysTotalVarCost(double[][] pgi) {
		double totalVarCost = 0;
		for (int g=0; g<pgi.length; g++)
		{
			for (int i=0; i<pgi[g].length; i++)
				totalVarCost += pgi[g][i] * _segmentPrice[g][i];
		}
		return totalVarCost;
	}
	
	/** Report the system total operating (variable) cost */
	public double getSysTotalVarCost(double[] pg) {
		double totalVarCost = 0;
		for (int g=0; g<_size; g++)
		{
			if (_costCurveFlag[g] == false) continue;
			if (pg[g] <= 0) continue;
			int idxMap = _mapToCostCurve[g];
			double[] breadths = _segmentBreadth[idxMap];
			double[] prices = _segmentPrice[idxMap];
			double[] pgi = getPgi(g, pg[g], breadths);
			for (int i=0; i<breadths.length; i++)
				totalVarCost += pgi[i] * prices[i];
		}
		return totalVarCost;
	}
	private double[] getPgi(int g, double pg, double[] breadths)
	{
		double[] pgi = new double[breadths.length];
		for (int i=0; i<breadths.length; i++)
		{
			if (pg <= breadths[i]) {pgi[i] = pg; pg = 0; break;}
			else {pgi[i] = breadths[i]; pg -= breadths[i];}
		}
		if (pg > 0.001) {
			String errInfo = "Error: pg exceeds sum of pgi for generator "+(g+1);
			_model.getDiary().hotLine(LogTypeXL.Error, errInfo); System.err.println(errInfo);
		}
		return pgi;
	}
	private double[][] getPgi(double[] pg)
	{
		double[][] pgi = new double[_sizeGenCost][];
		for (int g=0; g<_size; g++)
		{
			if (_costCurveFlag[g] == false) continue;
			int idxMap = _mapToCostCurve[g];
			pgi[idxMap] = new double[_segmentBreadth[idxMap].length];
			if (pg[g] <= 0) continue;
			pgi[idxMap] = getPgi(g, pg[g], _segmentBreadth[idxMap]);
		}
		return pgi;
	}

	
	public void dump()
	{
		String fileName = "GenListXL.csv";
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
		_model.getDiary().hotLine("Generator data for SCED is dumpped to a file "+fileName);
	}
	
	public void dump(PrintWriter pw)
	{
		double baseMVA = _model.getMVAbase();
		pw.println("genIdx,genBusIdx,genBusID,status,pgInit,pgmax,pgmin,energyRamp,spinRamp,costCurveFlag");
		for (int i=0; i<_size; i++)
		{
			int st = 0;
			if (_st[i] == true) st = 1;
			int hasCostCurve = 1;
			if (_costCurveFlag[i] == false) hasCostCurve = 0;
			pw.format("%d,%d,%s,%d,%f,%f,%f,%f,%f,%d\n",
					(i+1),
					(_busIdx[i]+1),
					_model.getBuses().getBusID(_busIdx[i]),
					st,
					_pgInit[i]*baseMVA,
					_pgmax[i]*baseMVA,
					_pgmin[i]*baseMVA,
					_energyRamp[i]*baseMVA,
					_spinRamp[i]*baseMVA,
					hasCostCurve);
		}
	}

	
	public void dumpCostCurve()
	{
		String fileName = "GenListXLCostCurve.csv";
		File pout = new File(fileName);
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(new BufferedWriter(new FileWriter(pout)));
		} catch (IOException e) {
			e.printStackTrace();
		}
		dumpCostCurve(pw);
		pw.flush();
		pw.close();
		_model.getDiary().hotLine("Generator cost curve data for SCED is dumpped to the file "+fileName);
	}
	
	public void dumpCostCurve(PrintWriter pw)
	{
		double baseMVA = _model.getMVAbase();
		double[][] pgi = getPgi(_pgInit);
		pw.println("idx,genIdx,segmentIdx,segmentBreadth,pgiInit,segLeftCap,segmentPrice");
		int count = 0;
		for (int i=0; i<_size; i++)
		{
			if (_costCurveFlag[i] == false) continue;
			int idxMap = _mapToCostCurve[i];
			for (int j=0; j<_segmentBreadth[idxMap].length; j++)
			{
				count++;
				pw.format("%d,%d,%d,%f,%f,%f,%f\n",
						count,
						(i+1),
						(j+1),
						_segmentBreadth[idxMap][j]*baseMVA,
						pgi[idxMap][j]*baseMVA,
						(_segmentBreadth[idxMap][j] - pgi[idxMap][j])*baseMVA,
						_segmentPrice[idxMap][j]/baseMVA);
			}
		}
	}
	
	
}
