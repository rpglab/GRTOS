package com.cyberattack.data;

import java.io.IOException;

import com.cyberattack.CyberAttackDataCenter;
import com.powerdata.openpa.tools.SimpleCSV;

public class CyberAttackBranch {

	int _size;
	int[] _frmBus;
	int[] _toBus;
	double[] _x;
	double[] _rateA;
	double[] _pkPreRT;   // actual line flow at t=-5,
	double[] _pkRT_ISO;   // line flow that ISO measures
	double[] _pkSCED;    // expected line flow from ISO's SCED

	CyberAttackDataCenter _cyberModel;
	
	public CyberAttackBranch(CyberAttackDataCenter cyberModel) {_cyberModel = cyberModel;}

	public void readBranchData(String path2BrcFile) throws IOException {
		SimpleCSV loadsCSV = new SimpleCSV(path2BrcFile);

		_frmBus = loadsCSV.getInts("frmBusIdx");
		_toBus = loadsCSV.getInts("toBusIdx");
		_size = _frmBus.length;
		for (int k=0; k<_size; k++)
		{
			_frmBus[k] = _frmBus[k] - 1;
			_toBus[k] = _toBus[k] - 1;
		}

		_x = loadsCSV.getDoubles("x");
		_rateA = loadsCSV.getDoubles("rateA");
		if (loadsCSV.hasCol("pkPreRT") == true) _pkPreRT = loadsCSV.getDoubles("pkPreRT");
		if (loadsCSV.hasCol("pkRT_ISO") == true) _pkRT_ISO = loadsCSV.getDoubles("pkRT_ISO");
		if (loadsCSV.hasCol("pkPostRTSCED") == true) _pkSCED = loadsCSV.getDoubles("pkPostRTSCED");
	}
	
	public int size() {return _size;}
	public int[] getFrmBuses() {return _frmBus;}
	public int[] getToBuses() {return _toBus;}
	public double[] getX() {return _x;}
	public double[] getRateA() {return _rateA;}
	public double[] getPkPreRT() {return _pkPreRT;}
	public double[] getPkRT_ISO() {return _pkRT_ISO;}
	public double[] getPkSCED() {return _pkSCED;}
	
	
}
