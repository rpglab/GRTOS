package com.cyberattack.data;

import java.io.IOException;
import java.util.Arrays;

import com.cyberattack.CyberAttackDataCenter;
import com.powerdata.openpa.tools.SimpleCSV;

/**
 * Input is load profiles (in MW, MVAr).
 * Each column should have data (can be 0) for all buses or loads.
 * 
 * @author Xingpeng.Li (xplipower@gmail.com)
 *
 */
public class CyberAttackLoad {

	boolean _isZeroBusLoadRemoved;
	int _nloads = -1;
	int _nbuses = -1;
	int[] _loadBusIdx;  // load bus index (non-zero load), the same size with the number of non-zero loads.
	int[] _busIdxMap2LoadIdx;  // Load index is _busIdxMap2LoadIdx[n] for bus n

	/*------------------  Load Profile  -----------------*/	
	/* Pre-Real-Time period */
	double[] _pdPreRT; // Actual active power load (Pd) at t=-TimeED (pre-Real-Time)
	double[] _qdPreRT; // Actual reactive power load (Qd) at t=-TimeED (pre-Real-Time)

	double[] _pdRT_Predict; // Forecast active power load (Pd) at t=0 (Real-Time)
	double[] _qdRT_Predict; // Forecast reactive power load (Qd) at t=0 (Real-Time)

	/* Real-time period */
	double[] _pdRT; // Actual active power load (Pd) at t=0 (Real-Time)
	double[] _qdRT; // Actual reactive power load (Qd) at t=0 (Real-Time)
	
	double[] _pdRT_ISO; // ISO's active power load (Pd) at t=0 (Real-Time)
	double[] _qdRT_ISO; // ISO's reactive power load (Qd) at t=0 (Real-Time)
	
//	int[] idxLoadAttack;    // index of loads that are under control of attacker
//	double[] _pdRT_Attack;  // Attacker's active power load (Pd) at t=0 (Real-Time)
//	double[] _qdRT_Attack;  // Attacker's reactive power load (Qd) at t=0 (Real-Time)

	double[] _pdPostRT_Predict; // Forecast active power load (Pd) at t=TimeED (post-Real-Time)
	double[] _qdPostRT_Predict; // Forecast reactive power load (Qd) at t=TimeED (post-Real-Time)

	/* Post-Real-time period */
	double[] _pdPostRT; // Actual active power load (Pd) at t=TimeED (post-Real-Time)
	double[] _qdPostRT; // Actual reactive power load (Qd) at t=TimeED (post-Real-Time)

	double[] _pdPostRT_ISO; // ISO's active power load (Pd) at t=0 (Real-Time)
	double[] _qdPostRT_ISO; // ISO's reactive power load (Qd) at t=0 (Real-Time)

	CyberAttackDataCenter _cyberModel;
	
	public CyberAttackLoad(CyberAttackDataCenter cyberModel) throws IOException 
	{
		_cyberModel = cyberModel;
	}
	
	public void readLoadData(String path2LoadFile) throws IOException {
		SimpleCSV loadsCSV = new SimpleCSV(path2LoadFile);
//		if (loadsCSV.hasCol("busIdx") == true) _isBusLoad = true;
//		else if (loadsCSV.hasCol("loadIdx") == true) _isBusLoad = false;
//		else System.exit(1);
		
		/* Pre-Real-Time period */
		if (loadsCSV.hasCol("pdPreRT") == true) _pdPreRT = loadsCSV.getDoubles("pdPreRT");
		if (loadsCSV.hasCol("qdPreRT") == true) _qdPreRT = loadsCSV.getDoubles("qdPreRT");
		if (loadsCSV.hasCol("pdRT_Predict") == true) _pdRT_Predict = loadsCSV.getDoubles("pdRT_Predict");
		if (loadsCSV.hasCol("qdRT_Predict") == true) _qdRT_Predict = loadsCSV.getDoubles("qdRT_Predict");
		
		/* Real-time period */
		if (loadsCSV.hasCol("pdRT") == true) _pdRT = loadsCSV.getDoubles("pdRT");
		if (loadsCSV.hasCol("qdRT") == true) _qdRT = loadsCSV.getDoubles("qdRT");
		if (loadsCSV.hasCol("pdRT_ISO") == true) _pdRT_ISO = loadsCSV.getDoubles("pdRT_ISO");
		if (loadsCSV.hasCol("qdRT_ISO") == true) _qdRT_ISO = loadsCSV.getDoubles("qdRT_ISO");
		if (loadsCSV.hasCol("pdPostRT_Predict") == true) _pdPostRT_Predict = loadsCSV.getDoubles("pdPostRT_Predict");
		if (loadsCSV.hasCol("qdPostRT_Predict") == true) _qdPostRT_Predict = loadsCSV.getDoubles("qdPostRT_Predict");

		/* Post-Real-time period */
		if (loadsCSV.hasCol("pdPostRT") == true) _pdPostRT = loadsCSV.getDoubles("pdPostRT");
		if (loadsCSV.hasCol("qdPostRT") == true) _qdPostRT = loadsCSV.getDoubles("qdPostRT");
		if (loadsCSV.hasCol("pdPostRT_ISO") == true) _pdPostRT_ISO = loadsCSV.getDoubles("pdPostRT_ISO");
		if (loadsCSV.hasCol("qdPostRT_ISO") == true) _qdPostRT_ISO = loadsCSV.getDoubles("qdPostRT_ISO");
		
		_nbuses = _pdRT_ISO.length;
		removeZeroBusLoad();
	}
	
	private void removeZeroBusLoad() {
		if (_isZeroBusLoadRemoved == true) return;
		
		if (_pdPreRT != null) _pdPreRT = converter(_pdPreRT);
		if (_qdPreRT != null) _qdPreRT = converter(_qdPreRT);
		if (_pdRT_Predict != null) _pdRT_Predict = converter(_pdRT_Predict);
		if (_qdRT_Predict != null) _qdRT_Predict = converter(_qdRT_Predict);

		if (_pdRT != null) _pdRT = converter(_pdRT);
		if (_qdRT != null) _qdRT = converter(_qdRT);
		if (_pdRT_ISO != null) _pdRT_ISO = converter(_pdRT_ISO);
		if (_qdRT_ISO != null) _qdRT_ISO = converter(_qdRT_ISO);
		if (_pdPostRT_Predict != null) _pdPostRT_Predict = converter(_pdPostRT_Predict);
		if (_qdPostRT_Predict != null) _qdPostRT_Predict = converter(_qdPostRT_Predict);

		if (_pdPostRT != null) _pdPostRT = converter(_pdPostRT);
		if (_qdPostRT != null) _qdPostRT = converter(_qdPostRT);
		if (_pdPostRT_ISO != null) _pdPostRT_ISO = converter(_pdPostRT_ISO);
		if (_qdPostRT_ISO != null) _qdPostRT_ISO = converter(_qdPostRT_ISO);

		_isZeroBusLoadRemoved = true;
	}
	
	private double[] converter(double[] busLoad) {
		if (_loadBusIdx == null) calcLoadBusIdx(busLoad);
		double[] nonZeroBusload = new double[_nloads];
		for (int i=0; i<busLoad.length; i++)
		{
			if (_busIdxMap2LoadIdx[i] == -1) continue;
			nonZeroBusload[_busIdxMap2LoadIdx[i]] = busLoad[i];
		}
		return nonZeroBusload;
	}
	
	private void calcLoadBusIdx(double[] busLoad) {
		_busIdxMap2LoadIdx = new int[busLoad.length];
		Arrays.fill(_busIdxMap2LoadIdx, -1);
		
		_nloads = 0;
		int[] loadBusIdx = new int[busLoad.length];
		for (int i=0; i<busLoad.length; i++)
		{
			if (busLoad[i] == 0) continue;
			_busIdxMap2LoadIdx[i] = _nloads;
			loadBusIdx[_nloads++] = i;
		}
		_loadBusIdx = new int[_nloads];
		Arrays.fill(_loadBusIdx, -1);
		System.arraycopy(loadBusIdx, 0, _loadBusIdx, 0, _nloads);
	}
	

	
//	public boolean convertBusLoad2Load(SystemModelXL model) {
//		if (_isBusLoad == false) return false;
//		
//		if (_pdPreRT != null) _pdPreRT = converter(model, _pdPreRT);
//		if (_qdPreRT != null) _qdPreRT = converter(model, _qdPreRT);
//		if (_pdRT_Predict != null) _pdRT_Predict = converter(model, _pdRT_Predict);
//		if (_qdRT_Predict != null) _qdRT_Predict = converter(model, _qdRT_Predict);
//
//		if (_pdRT != null) _pdRT = converter(model, _pdRT);
//		if (_qdRT != null) _qdRT = converter(model, _qdRT);
//		if (_pdRT_ISO != null) _pdRT_ISO = converter(model, _pdRT_ISO);
//		if (_qdRT_ISO != null) _qdRT_ISO = converter(model, _qdRT_ISO);
//		if (_pdPostRT_Predict != null) _pdPostRT_Predict = converter(model, _pdPostRT_Predict);
//		if (_qdPostRT_Predict != null) _qdPostRT_Predict = converter(model, _qdPostRT_Predict);
//
//		if (_pdPostRT != null) _pdPostRT = converter(model, _pdPostRT);
//		if (_qdPostRT != null) _qdPostRT = converter(model, _qdPostRT);
//		if (_pdPostRT_ISO != null) _pdPostRT_ISO = converter(model, _pdPostRT_ISO);
//		if (_qdPostRT_ISO != null) _qdPostRT_ISO = converter(model, _qdPostRT_ISO);
//
//		_isBusLoad = false;
//		return true;
//	}
//	
//	private double[] converter(SystemModelXL model, double[] busLoad) {
//		BusGrpXL busGrp = model.getBusGrp();
//		int nRealLoads = model.getLoads().sizeRealLoad();
//		double[] realLoads = new double[nRealLoads];
//		for (int n=0; n<busLoad.length; n++)
//		{
//			int[] idxLoads = busGrp.getLoadIndex(n);
//			if (idxLoads == null) continue;
//			double totalLoadBus = getTotalRealLoad(model, idxLoads);
//			for (int i=0; i<idxLoads.length; i++)
//			{
//				int idxLoad = idxLoads[i];
//				if (idxLoad >= nRealLoads) continue;
//				realLoads[idxLoad] = busLoad[n] * (model.getLoads().getPLoad(idxLoad)/totalLoadBus);
//			}
//		}
//		return realLoads;
//	}
//	
//	private double getTotalRealLoad(SystemModelXL model, int[] idxLoads)
//	{
//		double totalLoadBus = 0;
//		for (int i=0; i<idxLoads.length; i++) {
//			int idxLoad = idxLoads[i];
//			if (model.getLoads().getLoadType(idxLoad) == LoadType.Real)
//				totalLoadBus += model.getLoads().getPLoad(idxLoad);
//		}
//		return totalLoadBus;
//	}
	

	/* Pre-Real-Time period */
	public double[] getPdPreRT() {return (_pdPreRT == null) ? getPdRT() : _pdPreRT;}
	public double[] getQdPreRT() {return (_qdPreRT == null) ? getQdRT() : _qdPreRT;}
	
	public double[] getPdRT_Predict() {return (_pdRT_Predict == null) ? getPdPreRT() : _pdRT_Predict;}
	public double[] getQdRT_Predict() {return (_qdRT_Predict == null) ? getQdPreRT() : _qdRT_Predict;}
	
	/* Real-time period */
	public double[] getPdRT() {return _pdRT;}
	public double[] getQdRT() {return _qdRT;}
	
	public double[] getPdRT_ISO() {return _pdRT_ISO;}
	public double[] getQdRT_ISO() {return _qdRT_ISO;}
	
//	public int[] getIdxLoadAttack() {return idxLoadAttack;}
//	public double[] getPdRT_Attack() {return _pdRT_Attack;}
//	public double[] getQdRT_Attack() {return _qdRT_Attack;}

	public double[] getPdPostRT_Predict() {return (_pdPostRT_Predict == null) ? getPdRT_ISO() : _pdPostRT_Predict;}
	public double[] getQdPostRT_Predict() {return (_qdPostRT_Predict == null) ? getQdRT_ISO() : _qdPostRT_Predict;}
	
	/* Post-Real-time period */
	public double[] getPdPostRT() {return (_pdPostRT == null) ? getPdRT() : _pdPostRT;}
	public double[] getQdPostRT() {return (_qdPostRT == null) ? getQdRT() : _qdPostRT;}
	
	public double[] getPdPostRT_ISO() {return (_pdPostRT_ISO == null) ? getPdRT_ISO() : _pdPostRT_ISO;}
	public double[] getQdPostRT_ISO() {return (_qdPostRT_ISO == null) ? getQdRT_ISO() : _qdPostRT_ISO;}
	
	
	public int sizeBuses() {return _nbuses;}
	public int sizeLoads() {return _nloads;}
	public int getLoadBusIdx(int loadIdx) {return _loadBusIdx[loadIdx];}
	
}
