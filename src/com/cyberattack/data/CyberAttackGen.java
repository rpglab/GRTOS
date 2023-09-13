package com.cyberattack.data;

import java.io.IOException;

import com.cyberattack.CyberAttackDataCenter;
import com.powerdata.openpa.tools.SimpleCSV;

public class CyberAttackGen {
	
	int _size;
	int[] _genBus;
	double[] _pgmax;
	double[] _pgmin;
	double[] _pginit;
	double[] _pgSCED;
	double[] _pgCost;
	
	CyberAttackDataCenter _cyberModel;
	
	public CyberAttackGen(CyberAttackDataCenter cyberModel) {_cyberModel = cyberModel;}

	public void readGenData(String path2GenFile) throws IOException {
		SimpleCSV loadsCSV = new SimpleCSV(path2GenFile);

		_genBus = loadsCSV.getInts("genBusIdx");
		_size = _genBus.length;
		for (int g=0; g<_size; g++)
			_genBus[g] = _genBus[g] - 1;

		_pgmax = loadsCSV.getDoubles("Pgmax");
		_pgmin = loadsCSV.getDoubles("Pgmin");
		_pginit = loadsCSV.getDoubles("PgInit");
		_pgSCED = loadsCSV.getDoubles("PgSCED");
		_pgCost = loadsCSV.getDoubles("cost");
	}
	
	public int size() {return _size;}
	public int[] getGenBus() {return _genBus;}
	public double[] getPgMax() {return _pgmax;}
	public double[] getPgMin() {return _pgmin;}
	public double[] getPgInit() {return _pginit;}
	public double[] getPgSCED() {return _pgSCED;}
	public double[] getPgCost() {return _pgCost;}
	
	public int getGenBusIdx(int genIdx) {return _genBus[genIdx];}
	

}
