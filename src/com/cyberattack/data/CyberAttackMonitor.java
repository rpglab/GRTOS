package com.cyberattack.data;

import com.cyberattack.CyberAttackDataCenter;

public class CyberAttackMonitor {

	/*------------------  Monitor Set  ------------------*/	
	/* Pre-Real-time monitor set */
	int[] _monitorSetPreRT;   // for base case
	//double[] _pkPreRT;
	//double[] _limitAPreRT;
	
	int[] _idxCtgcyPreRT;        // for ctgcy case
	int[][] _monitorSetCtgcyPreRT;  // for ctgcy case
	double[][] _pkcPreRT;
	double[][] _limitCPreRT;
	
	double[] _pgPreRT;
	
	/* Real-time monitor set */
	int[] _monitorSetRT;
	int[] _idxCtgcyRT;
	int[][] _monitorSetCtgcyRT;
	
	double[] _pgRT_ISO;    // Measured active power output of unit g at t=0.
	double[] _pkRT_ISO;    // Measured active flow on branch k at t=0 reported from ISO’s SE or PF.
	
	/* Post-Real-time monitor set */
	int[] _monitorSetPostRT;
	int[] _idxCtgcyPostRT;
	int[][] _monitorSetCtgcyPostRT;
	

	CyberAttackDataCenter _cyberModel;
	
	public CyberAttackMonitor(CyberAttackDataCenter cyberModel) {_cyberModel = cyberModel;}

	/* Pre-Real-time monitor set */
	public int[] getMonitorSetPreRT() {return _monitorSetPreRT;}
	//public double[] getPkPreRT() {return _pkPreRT;}
	//public double[] getLimitAPreRT() {return _limitAPreRT;}
	
	public int[] getIdxCtgcyPreRT() {return _idxCtgcyPreRT;}
	public int[][] getMonitorCtgcySetPreRT() {return _monitorSetCtgcyPreRT;}
	public double[][] getPkcPreRT() {return _pkcPreRT;}
	public double[][] getLimitCPreRT() {return _limitCPreRT;}

	public double[] getPgPreRT() {return _pgPreRT;}

	/* Real-time monitor set */
	public int[] getMonitorSetRT() {return _monitorSetRT;}
	public int[] getIdxCtgcyRT() {return _idxCtgcyRT;}
	public int[][] getMonitorCtgcySetRT() {return _monitorSetCtgcyRT;}
	
	public double[] getPgRT_ISO() {return _pgRT_ISO;}
	public double[] getPkRT_ISO() {return _pkRT_ISO;}
	
	/* Pre-Real-time monitor set */
	public int[] getMonitorSetPostRT() {return _monitorSetPostRT;}
	public int[] getIdxCtgcyPostRT() {return _idxCtgcyPostRT;}
	public int[][] getMonitorCtgcySetPostRT() {return _monitorSetCtgcyPostRT;}
	
	public void enableMonitorAllBrc() {
		int nbrc = _cyberModel.getCyberAttackBranch().size();
		_monitorSetPreRT = new int[nbrc];
		for (int i=0; i<nbrc; i++)
			_monitorSetPreRT[i] = i;
	}
	
	
}
