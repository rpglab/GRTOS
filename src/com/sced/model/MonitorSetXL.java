package com.sced.model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import com.sced.model.param.ParamInput;
import com.utilxl.log.LogTypeXL;

/**
 * Info for critical element to be monitored for base-case 
 * 
 * @author Xingpeng Li, xplipower@gmail.com 
 *
 */
public class MonitorSetXL {

	SystemModelXL _model;
	
	public MonitorSetXL(SystemModelXL model) {_model = model;}
	
	boolean _monitorAllBrc = ParamInput.getMonitorAllBrcBaseCase();  // monitor all lines flow if true; otherwise, monitor a subset of lines defined by _monitorBrcSet.
	int _size;
	int[] _monitorBrcSet;      // index of branches that need to be monitored
	double[] _pkInit;         // pk_init
	double[] _pkLimit;    // Line thermal limit in MW, sqrt(RateA^2 - max(Qfrm, Qto)^2)
	boolean[] _isConstActive;   // if false, then, the associated constraint is not modeled.

	
	public void setIsMonitorAllBrc(boolean monitorAllBrc) {_monitorAllBrc = monitorAllBrc;}
	public void setMonitorBrcSet(int[] monitorSet) {_monitorBrcSet = monitorSet; setSize();}
	private void setSize() {
		if (_monitorBrcSet == null) {_size = 0; return;}
		_size = _monitorBrcSet.length;
	}
	public void setPkInit(double[] pkInit) {_pkInit = pkInit;}
	public void setPkLimit(double[] pkLimit) {_pkLimit = pkLimit;}
	public void setIsConstActive(boolean[] isConstActive) {_isConstActive = isConstActive;}

	public int size() {return _size;}
	public boolean getIsMonitorAllBrc() {return _monitorAllBrc;}
	public int[] getMonitorBrcSet() {return _monitorBrcSet;}
	public double[] getPkInit() {return _pkInit;}
	public double[] getPkLimit() {return _pkLimit;}
	public boolean[] getIsConstActive() {return _isConstActive;}

	public void dataCheck() {
		_model.getDiary().hotLine(LogTypeXL.CheckPoint, "# of branches in monitorSet for base-case is: "+_size);
	}
	
	public void dump()
	{
		String fileName = "MonitorSetXL.csv";
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
		_model.getDiary().hotLine("Base case branch monitor/constraint data for SCED is dumpped to the file "+fileName);
	}
	
	public void dump(PrintWriter pw)
	{
		double baseMVA = _model.getMVAbase();
		if (_monitorAllBrc == true)
		{
			pw.println("Parameter _monitorAllBrc is true; thus all lines are monitored for base case");
			return;
		}
		pw.println("idx,brcIdx,pkLimit,pkInit");
		for (int i=0; i<size(); i++)
		{
			pw.format("%d,%d,%f,%f\n",
					(i+1),
					_monitorBrcSet[i],
					_pkLimit[i]*baseMVA,
					_pkInit[i]*baseMVA);
		}
	}

}
