package com.cyberattack;

import java.io.IOException;

import com.cyberattack.data.CyberAttackBranch;
import com.cyberattack.data.CyberAttackGen;
import com.cyberattack.data.CyberAttackLoad;
import com.cyberattack.data.CyberAttackMonitor;
import com.sced.auxData.FormPTDFXL;

/**
 * Class where all data can be accessed.
 * 
 * @author Xingpeng.Li (xplipower@gmail.com)
 *
 */
public class CyberAttackDataCenter {
	
	double _baseMVA = 100;
	int _slack = 0;
	double _ptdfTolValue = 0.01;
	double[][] _ptdf;

	CyberAttackLoad _loads;
	CyberAttackGen _gens;
	CyberAttackBranch _branches;
	CyberAttackMonitor _monitorSet;
	
	public CyberAttackDataCenter() throws IOException {init();}
	
	private void init() throws IOException
	{
		_branches = new CyberAttackBranch(this);
		_gens = new CyberAttackGen(this);
		_monitorSet = new CyberAttackMonitor(this);
		_loads = new CyberAttackLoad(this);
	}
	
	public CyberAttackLoad getCyberAttackLoad() {return _loads;}
	public CyberAttackGen getCyberAttackGen() {return _gens;}
	public CyberAttackBranch getCyberAttackBranch() {return _branches;}
	public CyberAttackMonitor getCyberAttackMonitorSet() {return _monitorSet;}

	public double getMVAbase() {return _baseMVA;}
	public int getSlackBusIdx() {return _slack;}
	public double getPTDFTolValue() {return _ptdfTolValue;}
	public double[][] getPTDF() {
		if (_ptdf == null) calcPTDF();
		return _ptdf;
	}
	public double[] getDensePTDF(int idxBrc) {
		if (_ptdf == null) calcPTDF();
		return _ptdf[idxBrc];
	}

	private void calcPTDF() {
		FormPTDFXL ptdfCalc = new FormPTDFXL(_loads.sizeBuses(), _slack, _branches.getFrmBuses(), _branches.getToBuses(), _branches.getX());
		_ptdf = ptdfCalc.getPTDF();
		//ptdfCalc.dump();
	}
	

	
}
