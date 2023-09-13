package com.sced.model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

import com.sced.model.param.ParamInput;
import com.utilxl.log.LogTypeXL;

/**
 * Info for critical branch contingency list 
 * 
 * Added critcal interface list in March 2017.
 * 
 * @author Xingpeng Li, xplipower@gmail.com 
 *
 */
public class BrcCtgcyListXL {

	SystemModelXL _model;
	
	boolean _simAllPotentialCtgcy = ParamInput.getSimAllPotentialCtgcy(); // if true, overrides all other settings; if false; the very below setting doesn't matter and it will follow other setting.
	boolean _monALLBrc4SimAllPotentialCtgcy = ParamInput.getMonALLBrc4SimAllPotentialCtgcy(); // if true monitor all lines; it typically should be true
											 // if false, monitor the same set as defined by MonitorSetXL._monitorBrcSet.
	int[] _inSvcNonRadialBrc;  // an index array of all in-service non-radial branches
	boolean[] _isInSvcNonRadialBrcCtgcyActive; // all are true unless for specific requirement

	boolean _usePkInit = ParamInput.getUsePkInit();     // matters only when _usePTDFforSCED == true.
	boolean _usePkcInit = ParamInput.getUsePkcInit();     // matters only when _usePTDFforSCED == true and _usePkInit == true and _simAllPotentialCtgcy == true.
	
	/* The below variables will NOT be in effect if _simAllPotentialCtgcy == true */
	int _size;                  // # of critical contingencies
	int[] _ctgcyBranchIdx;      // branch index of critical contingencies 
	boolean[] _isCtgcyActive;   //  whether a contingency is active
	int[][] _monitorSetCtgcyCase;    // monitor set for all contingency cases
	boolean _monitorAllBrc = ParamInput.getMonitorAllBrcCtgcyCasey();  // monitor all lines flow if true; otherwise, monitor a subset of lines.
									// if false, _monitorSetCtgcyCase should be NON-NULL.
	double[][] _pkcInit;              // Pk,c,init
	double[][] _pkcLimit;         // Line thermal limit in MW, sqrt(RateC^2 - max(Qfrm, Qto)^2)
	boolean[][] _isConstActive;   // if false, then the corresponding constraint is not modeled.

	/* Interface limit */
	boolean[] _isCtgcyActive4Interface;  // given a critical contingency, whether it is also critical for interface
	int[] _mapCtgcyIdx2Interface;   // given a general ctgcy index c, convert c to either -1 or the associated index for the below arrays.
	int[] _interfaceCtgcyBrcIdx;    // critical contingencies for only interface limit
	boolean[][] _isInterfaceActiveCtgcyCase;   // is interface limit active for contingency case, rank: nCtgcy * nInterface
	int[][] _monInterfaceLines;    // Note the difference between interface and interface lines (forming interfaces)
	double[][] _pkcInitInterfaceLines;    // Note the difference between interface and interface lines (forming interfaces)
	
	
	public BrcCtgcyListXL(SystemModelXL model) {_model = model; init();}
	
	private void init() {}
	protected void initSize() {
		_ctgcyBranchIdx = new int[_size];
		_isCtgcyActive = new boolean[_size];
		Arrays.fill(_isCtgcyActive, true);
	}
	
	public void setSize(int size) {_size = size; initSize();}
	public void setIsMonitorAllBrc(boolean flag) {_monitorAllBrc = flag;}
	public void setIsCtgcyActive(int c, boolean st) {_isCtgcyActive[c] = st;}
	public void setCtgcyBrcIdx(int[] idx) {_ctgcyBranchIdx = idx;}
	public void setCtgcyBrcIdx(int c, int idx) {_ctgcyBranchIdx[c] = idx;}
	public void setCtgcyCaseMonitorSet(int[][] monitorSet) {_monitorSetCtgcyCase = monitorSet;}
	
	public void setPkcInit(double[][] pkcInit) {_pkcInit = pkcInit;}
	public void setPkcLimit(double[][] pkcLimit) {_pkcLimit = pkcLimit;}
	public void setIsConstActive(boolean[][] isConstActive) {_isConstActive = isConstActive;}

	public void setInterfaceCtgcyBrcIdx(int[] interfaceCtgcyBrcIdx) {_interfaceCtgcyBrcIdx = interfaceCtgcyBrcIdx; calcMapCtgcy2Interface();}
	public void setIsInterfaceActiveCtgcyCase(boolean[][] isInterfaceActiveCtgcyCase) {_isInterfaceActiveCtgcyCase = isInterfaceActiveCtgcyCase;}
	public void setMonInterfaceLines(int[][] monInterfaceLines) {_monInterfaceLines = monInterfaceLines;}
	public void setPkcInitInterfaceLines(double[][] pkcLimitInterfaceLines) {_pkcInitInterfaceLines = pkcLimitInterfaceLines;}
	
	public int[] getInterfaceCtgcyBrcIdx() {return _interfaceCtgcyBrcIdx;}
	public boolean isCtgcyActive4Interface(int c) {
		if (_model.getInterfaceList().size() == 0) return false;
		return (_simAllPotentialCtgcy == false) ? _isCtgcyActive4Interface[c] : getIsInSvcNonRadialBrcCtgcyActive()[c];
	}
	public boolean[] getIsInterfaceActiveCtgcyCase(int c) {
		if (_simAllPotentialCtgcy == true) return getTrueBooleanArray(_model.getInterfaceList().size());
		if (_isInterfaceActiveCtgcyCase == null) return getTrueBooleanArray(_model.getInterfaceList().size());
		if (_isInterfaceActiveCtgcyCase.length == 0) return getTrueBooleanArray(_model.getInterfaceList().size());
		return _isInterfaceActiveCtgcyCase[_mapCtgcyIdx2Interface[c]];
	}
	public int[] getMonInterfaceLines(int c) {return _monInterfaceLines[_mapCtgcyIdx2Interface[c]];}
	public double[] getPkcInitInterfaceLines(int c) {return _pkcInitInterfaceLines[_mapCtgcyIdx2Interface[c]];}
	
	private boolean[] getTrueBooleanArray(int num) {
		boolean[] tmp = new boolean[num];
		Arrays.fill(tmp, true);
		return tmp;
	}
	private void calcMapCtgcy2Interface() {
		_isCtgcyActive4Interface = new boolean[_size];
		_mapCtgcyIdx2Interface = new int[_size];
		Arrays.fill(_mapCtgcyIdx2Interface, -1);
		for (int i=0; i<_interfaceCtgcyBrcIdx.length; i++)
			for (int c=0; c<_size; c++)
				if (_ctgcyBranchIdx[c] == _interfaceCtgcyBrcIdx[i]) {
					_isCtgcyActive4Interface[c] = true;
					_mapCtgcyIdx2Interface[c] = i; 
					break;
				}
	}
	
	
	public int size() {return (_simAllPotentialCtgcy == false) ? _size : getInSvcNonRadialBrc().length;}
	public boolean isCtgcyActive(int c) {return (_simAllPotentialCtgcy == false) ? _isCtgcyActive[c] : getIsInSvcNonRadialBrcCtgcyActive()[c];}
	public int getCtgcyBrcIdx(int c) {return (_simAllPotentialCtgcy == false) ? _ctgcyBranchIdx[c] : getInSvcNonRadialBrc()[c];}
	
	
	public boolean getIsMonitorAllBrc() {return (_simAllPotentialCtgcy == false) ? _monitorAllBrc : _monALLBrc4SimAllPotentialCtgcy;}
	public int[] getCtgcyCaseMonitorSet(int c) {
		if (_simAllPotentialCtgcy == true) return _model.getMonitorSet().getMonitorBrcSet();
		if (_monitorSetCtgcyCase == null) return null;
		return _monitorSetCtgcyCase[c];
	}
	public double[] getCtgcyPkcInit(int c) {
		if (_pkcInit == null) return null;
		return _pkcInit[c];
	}
	public double[] getCtgcyPkcLimit(int c) {
		if (_simAllPotentialCtgcy == true) return _model.getMonitorSet().getPkLimit();
		if (_pkcLimit == null) return null;
		return _pkcLimit[c];
	}
	public boolean[] getIsConstActive(int c)
	{
		if (_simAllPotentialCtgcy == true) return null;
		if (_isConstActive == null) return null;
		return _isConstActive[c];
	}
	
	
	/** Returned int[] - idxMap2CtgcyList 
	 * e.g., if idxMap2CtgcyList[idxBrc] == -1, then, this branch is not monitored for contingency @c.
	 * e.g., if idxMap2CtgcyList[idxBrc] == 0, then, the initial branch flow is _pkcInit[c][0] and limit is _pkcLimit[c][0]. */
	public int[] getIdxMap2CtgcyList(int c)
	{
		int sizeBrc = _model.getBranches().size();
		int[] idxMap2CtgcyList = new int[sizeBrc];
		Arrays.fill(idxMap2CtgcyList, -1);
		int[] monitorSetCtgcyCase = _monitorSetCtgcyCase[c];
		for (int i=0; i<monitorSetCtgcyCase.length; i++)
			idxMap2CtgcyList[monitorSetCtgcyCase[i]] = i;
		return idxMap2CtgcyList;
	}
	
	/** Returned int[] - idxMap2CtgcyInterfaceLine 
	 * e.g., if idxMap2CtgcyInterfaceLine[idxBrc] == -1, then, this branch is not monitored for interface limit purpose under contingency @c.
	 * e.g., if idxMap2CtgcyInterfaceLine[idxBrc] == 0, then, the initial branch flow is _pkcLimitInterfaceLines[c][0]. */
	public int[] getIdxMap2CtgcyInterfaceLines(int c)
	{
		int sizeBrc = _model.getBranches().size();
		int[] idxMap2CtgcyInterfaceLines = new int[sizeBrc];
		Arrays.fill(idxMap2CtgcyInterfaceLines, -1);
		if (_isCtgcyActive4Interface[c] == true) {
			int[] monInterfaceLines = _monInterfaceLines[_mapCtgcyIdx2Interface[c]];
			for (int i=0; i<monInterfaceLines.length; i++)
				idxMap2CtgcyInterfaceLines[monInterfaceLines[i]] = i;
		}
		return idxMap2CtgcyInterfaceLines;
	}
	
	
	public void setUsePkInit(boolean flag) {_usePkInit = flag;}
	public void setUsePkcInit(boolean flag) {_usePkcInit = flag;}
	
	public boolean isUsePkInit() {return _usePkInit;}
	public boolean isUsePkcInit() {return _usePkcInit;}

	public void setSimAllPotentialCtgcy(boolean flag) {_simAllPotentialCtgcy = flag;}
	public void setInSvcNonRadialBrc(int[] idxBrc) {_inSvcNonRadialBrc = idxBrc;}
	public void setIsInSvcNonRadialBrcCtgcyActive(boolean[] flags) {_isInSvcNonRadialBrcCtgcyActive = flags;}

	public boolean isSimAllPotentialCtgcy() {return _simAllPotentialCtgcy;}

	public int[] getInSvcNonRadialBrc() {
		if (_inSvcNonRadialBrc == null) calcInSvcNonRadialBrc();
		return _inSvcNonRadialBrc;
	}
	private void calcInSvcNonRadialBrc()
	{
		boolean[] isBrcRadial = _model.getBranches().getIsBrcRadial();
		int count = 0;
		_inSvcNonRadialBrc = new int[isBrcRadial.length];
		for (int k=0; k<isBrcRadial.length; k++)
		{
			if (isBrcRadial[k] == true) continue;
			if (_model.getBranches().isInSvc(k) == false) continue;
			_inSvcNonRadialBrc[count++] = k;
		}
		_inSvcNonRadialBrc = Arrays.copyOf(_inSvcNonRadialBrc, count);
	}

	public boolean[] getIsInSvcNonRadialBrcCtgcyActive() {
		if (_isInSvcNonRadialBrcCtgcyActive == null) calcIsInSvcNonRadialBrcCtgcyActive();
		return _isInSvcNonRadialBrcCtgcyActive;
	}
	private void calcIsInSvcNonRadialBrcCtgcyActive()
	{
		_isInSvcNonRadialBrcCtgcyActive = new boolean[getInSvcNonRadialBrc().length];
		Arrays.fill(_isInSvcNonRadialBrcCtgcyActive, true);
	}
	
	
	public void dataCheck() {
		_model.getDiary().hotLine(LogTypeXL.CheckPoint, "# of critical contingencies is: "+_size);
		int count = 0;
		for (int i=0; i<_size; i++)
			if (_isCtgcyActive[i] == true) count++;
		_model.getDiary().hotLine(LogTypeXL.CheckPoint, "# of active critical contingencies is: "+count);

		if (_monitorSetCtgcyCase != null) {
			int numMon = 0;
			for (int i=0; i<_size; i++)
				if (_isCtgcyActive[i] == true) numMon += _monitorSetCtgcyCase[i].length;
			_model.getDiary().hotLine(LogTypeXL.CheckPoint, "# of active contingency-related constraints is: "+numMon);
		} else _model.getDiary().hotLine(LogTypeXL.Warning, "_monitorSetCtgcyCase is NULL");
	}


	public void dump()
	{
		String fileName = "BrcCtgcyListXL.csv";
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
		_model.getDiary().hotLine("Branch contingency constraint data for SCED is dumpped to the file "+fileName);
	}
	
	public void dump(PrintWriter pw)
	{
		double baseMVA = _model.getMVAbase();
		if (_monitorAllBrc == true)
		{
			pw.println("Parameter _monitorAllBrc is true; thus, all lines are monitored for contingency cases");
			return;
		}
		pw.println("idxCtgcy,ctgcyBrcIdx,isActive,monitorBrcIdx,pkclimit,pkcInit");
		for (int i=0; i<_size; i++)
		{
			int st = 1;
			if (_isCtgcyActive[i] == false) st = 0;
			int[] monitorIdx = _monitorSetCtgcyCase[i];
			double[] pkcLimit = _pkcLimit[i];
			double[] pkcInit = _pkcInit[i];
			for (int j=0; j<monitorIdx.length; j++)
			{
				pw.format("%d,%d,%d,%d,%f,%f\n",
						(i+1),
						(_ctgcyBranchIdx[i]+1),
						st,
						(monitorIdx[j]+1),
						pkcLimit[j]*baseMVA,
						pkcInit[j]*baseMVA);
			}
		}
	}
	
}
