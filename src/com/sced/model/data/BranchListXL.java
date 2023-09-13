package com.sced.model.data;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import com.sced.model.SystemModelXL;
import com.sced.model.data.base.TwoTermDevListXL;
import com.sced.model.data.elem.BranchXL;
import com.utilxl.log.DiaryXL;
import com.utilxl.log.LogTypeXL;

/**
 * 
 * @author Xingpeng Li, xplipower@gmail.com 
 *
 */
public class BranchListXL extends TwoTermDevListXL {

	double[] _r;
	double[] _x;
	double[] _RateA;     // in p.u., MVA Rating
	double[] _RateB;
	double[] _RateC;
	double[] _pkInit;   // initial line active power
	double[] _pkLimitA;    // branch flow limit (in p.u., MW rating), min(sqrt(RateA^2-Qfrm^2), sqrt(RateA^2-Qto^2))
	double[] _pkLimitC;    // Approximate value, min(sqrt(RateC^2-Qfrm^2), sqrt(RateC^2-Qto^2)), Qfrm and Qto are from base-case power flow study.
	
	//double[] _tap;    // from-side versus to-side
	double[] _angle;    // from-side minus to-side, in radian
	
	boolean[] _isBrcRadial;

	public BranchListXL(SystemModelXL model) {super(model);}

	public void setSize(int size) {_size = size; initSize();}
	public void setFrmBusIdx(int idx, int frmBusIdx) {_frmBusIdx[idx] = frmBusIdx;}
	public void setToBusIdx(int idx, int toBusIdx) {_toBusIdx[idx] = toBusIdx;}
	public void setID(int idx, String id) {_id[idx] = id;}

	public void setSvcSt(int idx, boolean st) {_st[idx] = st;}
	public void setR(int idx, double r) {_r[idx] = r;}
	public void setX(int idx, double x) {_x[idx] = x;}
	public void setRateA(int idx, double rateA) {_RateA[idx] = rateA;}
	public void setRateB(int idx, double rateB) {_RateB[idx] = rateB;}
	public void setRateC(int idx, double rateC) {_RateC[idx] = rateC;}
	public void setPkInit(int idx, double pkInit) {_pkInit[idx] = pkInit;}
	public void setPkLimitA(int idx, double pkLimit) {_pkLimitA[idx] = pkLimit;}
	public void setPkLimitC(int idx, double pkLimit) {_pkLimitC[idx] = pkLimit;}
	
	public void setIsBrcRadial(boolean[] isBrcRadial) {_isBrcRadial = isBrcRadial;}
	public boolean[] getIsBrcRadial() {return _isBrcRadial;}
	public boolean getIsBrcRadial(int k) {return _isBrcRadial[k];}

	//public void setTap(int idx, double tap) {_tap[idx] = tap;}
	public void setAngle(int idx, double angle) {_angle[idx] = angle;}
	//public double[] getTap() {return _tap;}
	public double[] getAngle() {return _angle;}

	protected void initSize() {
		_frmBusIdx = new int[_size];
		_toBusIdx = new int[_size];
		_id = new String[_size];
		_st = new boolean[_size];
		_r = new double[_size];
		_x = new double[_size];
		
		_RateA = new double[_size];
		_RateB = new double[_size];
		_RateC = new double[_size];
		
		_pkInit = new double[_size];
//		_tap = new double[_size];
		_angle = new double[_size];
	}
	
	public void initPkLimitA() {_pkLimitA = new double[_size];}
	public void initPkLimitC() {_pkLimitC = new double[_size];}

	public BranchXL getLine(int i) {return new BranchXL(this, i);}
	
	public double[] getRateA() {return _RateA;}
	public double[] getRateC() {return _RateC;}
	public double[] getX() {return _x;}
	public double[] getPkInit() {return _pkInit;}
	public double getPkInit(int k) {return _pkInit[k];}
	public double[] getPkLimitA() {return _pkLimitA;}
	public double[] getPkLimitC() {return _pkLimitC;}
	
	/** Get branch rating under NORMAL operation */
	public double[] getBrcNormalRating()
	{
		double[] ratings = getPkLimitA();
		if (ratings == null) ratings = getRateA();
		return ratings;
	}
	
	/** Get branch rating under CONTINGENCY operation */
	public double[] getBrcCtgcyRating()
	{
		double[] ratings = getPkLimitC();
		if (ratings == null) ratings = getRateC();
		return ratings;
	}
	
	public void checkData() {
		DiaryXL diary = _model.getDiary();
		diary.hotLine(LogTypeXL.CheckPoint, "# of branches is: "+_size);
		for (int i=0; i<_size; i++) {
			if (_RateA[i] > _RateB[i]) {
				_RateB[i] = _RateA[i];
				diary.hotLine(LogTypeXL.Warning, "Brc "+i+" connecting frmBus "+_frmBusIdx[i] +" and toBus "
						+_toBusIdx[i] +" : RateB < RateA, RateB is set to RateA");
			}
			if (_RateB[i] > _RateC[i]) {
				_RateC[i] = _RateB[i];
				diary.hotLine(LogTypeXL.Warning, "Brc "+i+" connecting frmBus "+_frmBusIdx[i] +" and toBus "
						+_toBusIdx[i] +" : RateC < RateB, RateC is set to RateB");
			}
		}
		diary.hotLine(LogTypeXL.CheckPoint, sizeInSvc()+" lines out of "+size()+" are in service.");
		
		int count = 0;
		for (int i=0; i<_size; i++)
			if(_angle[i] != 0) {count++; diary.hotLine("Branch "+i+" has a non-zero phase shifter angle setting");}
		diary.hotLine(LogTypeXL.CheckPoint, "# of branches that have non-zero angle: "+count);
		
		if (_isBrcRadial != null) {
			int num = 0;
			for (int i=0; i<_size; i++)
				if (_isBrcRadial[i] == true) num++;
			diary.hotLine(LogTypeXL.CheckPoint, num +" lines out of "+size()+" are radial lines.");
		}
	}
	
	public int sizeLinesInSvc() {
		int num = 0;
		for (int i=0; i<_size; i++) {
			if (_st[i] == true) num++;
		}
		return num;
	}
	
	public void dump()
	{
		String fileName = "BranchListXL.csv";
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
		_model.getDiary().hotLine("Branch data for SCED is dumpped to the file "+fileName);
	}
	
	public void dump(PrintWriter pw)
	{
		double baseMVA = _model.getMVAbase();
		String isBrcRadial = "Unknown";
		pw.println("brcIdx,fromBusIdx,toBusIdx,branchID,status,isBrcRadial,r,x,rateA,rateB,rateC,pkInit,pkLimitA,pkLimitC,angle,angleInDegree");
		for (int i=0; i<_size; i++)
		{
			int st = 0;
			if (_st[i] == true) st = 1;
			if (_isBrcRadial != null) {
				if (_isBrcRadial[i] == true) isBrcRadial = "Yes";
				else isBrcRadial = "No";
			}
			
			pw.format("%d,%d,%d,%s,%d,%s,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f\n",
					(i+1),
					(_frmBusIdx[i]+1),
					(_toBusIdx[i]+1),
					_id[i],
					st,
					isBrcRadial,
					_r[i],
					_x[i],
					_RateA[i]*baseMVA,
					_RateB[i]*baseMVA,
					_RateC[i]*baseMVA,
					_pkInit[i]*baseMVA,
					_pkLimitA[i]*baseMVA,
					_pkLimitC[i]*baseMVA,
					_angle[i],
					_angle[i]*180/Math.PI);
		}
	}
	
}
