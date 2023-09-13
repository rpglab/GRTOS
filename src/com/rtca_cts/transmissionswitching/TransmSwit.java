package com.rtca_cts.transmissionswitching;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;

import com.powerdata.openpa.psse.ACBranchList;
import com.powerdata.openpa.psse.PsseModel;
import com.powerdata.openpa.psse.PsseModelException;
import com.powerdata.openpa.psse.powerflow.FastDecoupledPowerFlow;
import com.powerdata.openpa.psse.powerflow.VoltageSource;
import com.rtca_cts.contingencyanalysis.AnalyzeVioResult;
import com.rtca_cts.contingencyanalysis.VioResult;
import com.rtca_cts.param.ParamFACTS;
import com.rtca_cts.param.ParamFDPFlow;
import com.rtca_cts.param.ParamIO;
import com.rtca_cts.param.ParamTS;
import com.rtca_cts.param.ParamVio;
import com.utilxl.array.AuxArrayXL;
import com.utilxl.iofiles.AuxFileXL;

/**
 * Implement transmission switching;
 * 
 * Initialized in Sep. 2014.
 * 
 * @author Xingpeng.Li, xplipower@gmail.com 
 */
public abstract class TransmSwit {
	
	PsseModel _model;
	ACBranchList _branches;
	int _nbr;

	// debug for power flow code
	float _sumBrcVioAbnormal = 30f; 
	float _sumVmVioAbnormal = 30f;    // if summation of voltage violation is greater than 20 per unit, then program will report this abnormal data.
			
	// for threads info
	int _nproc = 1;
	int _rank = 0;
	
	// output data for TS
	boolean _useToolBusIndexforOutput;
	long _timeExcluded = 0;  // solution time to be excluded.  
	String _title;
	int _markConti = -1;
	int _markTS = -2;
	
	boolean _outputAllTS;
	String _pathToFileAllTS;     //   : path//to//file, e.g.  D:/testFile.txt .
	PrintStream _outFile;
	
	boolean _outputAllTS_AllVio;
	int _numTcol = 11;   // number of total collums of output data; for _outFile_AllVio.
	String _pathToFileAllTS_AllVio = null;
	PrintStream _outFile_AllVio;
	
	int _TSOption = ParamTS.getTSOption();
	int[] _idxTS;             // branch index of TS checked.
	float[][] _sumVioTS;      // sum of violation for each TS checked, two elems: _sumBrcVio, _sumVmVio.
	float[][] _sumVioTSImp;      //  It equals to (SumContiVio - SumTSVio) / SumContiVio; it is 0 if SumContiVio == 0.
	                             // two elems: _sumBrcVioImp, _sumVmVioImp.
	int[][] _numVioTS;           // number of violation for each TS checked, two elems: _numBrcVio, _numVmVio.
	int[][] _indicatorIndiElemVioCheck;
	int _numTSChecked;        // used in the TS loop. After the loop, this integer means the number of TS checked.
                              // number of branches that were performed transmission switching
	
	// violation related variables
	float[] _RateC;
	float _Vmax = ParamVio.getVmMax();
	float _Vmin = ParamVio.getVmMin();
	float _tolSumMVA = ParamVio.getTolSumMVA();    // in per unit, benefit of switching action for branch thermal violation not bigger than this value would not be considered.
	float _tolSumVm = ParamVio.getTolSumVm();    // for voltage magnitude.

	// Violation Monitor info
	boolean _isAllBusMnt = ParamVio.getIsAllBusMnt();   // monitor voltage violations on all buses. 
	boolean _isAllBrcMnt = ParamVio.getIsAllBrcMnt();  // monitor flow violations on all branches. 
	boolean[] _isBusMnt = null;
	boolean[] _isBrcMnt = null;
	
	boolean _setTolForZeroVioWithinTol;   // if true, then, check method checkZeroVioWithinTol().
	
	// for Gen Var Limit
//	ElemGroupAtGenBuses _elemGroupGenBuses;
	boolean _MarkGenVarLimit = ParamFDPFlow.getGenVarLimit();  // true by default, Change PV bus to PQ bus once the Qg is beyond its capacity.
	int _GenVarLimitOption = 1;       // 1 by default, options that how deal with Gen Var Limit Constraint.
	int _MaxIterPfVarLimit = 10;      // 10 by default, maximum iteration for option 1.
	
//	boolean _MarkUsingVgSched = true;    // if true, then, use generator scheduled set point to be the Vm of PV Buses.
	boolean _isRealTimeStartForGenBus;
	
	// For base case power flow
	float[] _vmBasePf = null;   // Vm of normal operating condition. No conti, no TS. 
	float[] _vaBasePf = null;   // Va of normal operating condition. No conti, no TS. 

	// for contingency power flow
	VoltageSource _vstartConti = VoltageSource.Flat;   // Default: Flat.
	int[] _contiInfo;   // It has three integer numbers. Note this array saves the contingency data by (index + 1) instead of index.
	                    // If brc conti: brcIdx+1, brcFrmBusNumber, brcToBusNumber; if gen conti: genIdx+1, genBusNumber, -1. 
	boolean _checked = false;
	boolean _convPf;
	float _sumContiBrcVio;
	float _sumContiVmVio;
	boolean _withinTol;    // true if violation of conti-pf is within tolerance
	float[] _vaContiPf = null;       // Va solution from base case power flow. As a starting point for TS.
	float[] _vmContiPf = null;       // Vm solution from base case power flow. As a starting point for TS.
	float[] _pfrmContiPf = null;
	VioResult _testC;
	ArrayList<Integer> _idxVmVioConti = null;
	ArrayList<Integer> _idxBrcVioConti = null;
	
	// element-based violation check for define TS benefitial or not.
	float _brcVioElemTol = ParamVio.getBrcVioElemTol();   // tolerance for flow violation on branch-based.
	float _vmVioElemTol = ParamVio.getVmVioElemTol();   // tolerance for voltage violation on bus-based.
	boolean _checkNewVioOccured = ParamVio.getCheckNewVioOccured();   // if true, then, check the existence of new violation after switching 
	boolean _newBrcVioOccured;
	boolean _newVmVioOccured;
	boolean _checkContiVioWorse = ParamVio.getCheckContiVioWorse();    // if true, then, check whether the violation from contingency get worse after switching.
	boolean _contiBrcVioWorse;
	boolean _contiVmVioWorse;
	
	// for summary result of TS. 
	ArrayList<Integer> _idxBrcTStoNoVio = new ArrayList<Integer>();    // eliminate violation, both voltage and thermal 
	ArrayList<Integer> _idxBrcTStoRedVio = new ArrayList<Integer>();   // reduce violation, but not to zero.
	ArrayList<Integer> _idxBrcTSVio = new ArrayList<Integer>();        // switching actions that do not help.
	ArrayList<Integer> _idxBrcTSNotConv = new ArrayList<Integer>();        // switching actions that do not converge.

	int _numBrcTStoNoVio;     // number of TS which can fully eliminate violation, both voltage and thermal
	int _numBrcTStoRedVio;    // number of TS which can fully reduce violation, but not to zero
	int _numBrcTSVio;         // number of TS which do not help
	int _numBrcTSNotConv;     // number of TS which do not converge

	int _idxTSMaxBrcVioImp;
	float _maxBrcVioImp = -1e6f;                  // in percentage, should belong to range [0, 1].
	float _VmVioImpAssoToMaxBrcVioImp = 0f;
	
	int _idxTSMaxVmVioImp;
	float _maxVmVioImp = -1e6f;
	float _BrcVioImpAssoToMaxVmVioImp = 0f;
	
	int _idxTSMaxSumVioImp;
	float _maxSumVioImp= -1e6f;
	float _VmVioImpAssoToMaxSumVioImp = 0f;
	float _BrcVioImpAssoToMaxSumVioImp = 0f;
	
	
	public TransmSwit(PsseModel model) throws PsseModelException
	{
		initial(model);
	}
	
	TransmSwit(PsseModel model, VoltageSource vstart) throws PsseModelException
	{
		initial(model);
		_vstartConti = vstart;
	}
	TransmSwit(PsseModel model, VoltageSource vstart, float[] vmBasePf, float[] vaBasePf) throws PsseModelException
	{
//		this(model);
		initial(model);
		_vstartConti = vstart;
		_vmBasePf = vmBasePf;
		_vaBasePf = vaBasePf;
	}
	
	/** initialization */
	void initial(PsseModel model) throws PsseModelException
	{
		_model = model;
		_branches = _model.getBranches();
		_nbr = _branches.size();
	}
	
	/** dummy threads info. */ 
	public void setThreadInfo(int nproc, int rank) {System.err.println("Program should never come to here - abstract setThreadInfo()..");}
	public void setAllTSTitle(String title) {_title = title;}
//	public void setMarkUsingVgSched(boolean mark) {_MarkUsingVgSched = mark;}
	public void setRealTimeStartForGenBus(boolean mark) {_isRealTimeStartForGenBus = mark;}

	public void clearGenVarLimit() { _MarkGenVarLimit = false;}
	public void enableGenVarLimit() {enableGenVarLimit(1);}
	public void enableGenVarLimit(int option)
	{
		_MarkGenVarLimit = true;
		_GenVarLimitOption = option;
	}
	
	public void setElemVioCheckTS (boolean checkNewVio, boolean checkContivioWrose)
	{
		_checkNewVioOccured = checkNewVio;
		_checkContiVioWorse = checkContivioWrose;
	}
	public void setElemVioCheckTS (boolean checkNewVio, boolean checkContivioWrose, float brcVioElemTol, float vmVioElemTol)
	{
		setElemVioCheckTS(checkNewVio, checkContivioWrose);
		_brcVioElemTol = brcVioElemTol;
		_vmVioElemTol = vmVioElemTol;
	}
	
	public void setOutputAllTS(boolean mark) { _outputAllTS = mark;}
	public void setOutputAllTS(boolean mark, String path) { _outputAllTS = mark; setPathToFileAllTS(path);}
	public void setOutputAllTS(boolean mark, String path, int[] contiInfo) { setOutputAllTS(mark, path); _contiInfo = contiInfo;}
	public void setPathToFileAllTS(String path) { _pathToFileAllTS = path;}
	public String getPathToFileAllTS() { return _pathToFileAllTS;}

	public void setOutputAllTS_AllVio(boolean mark) { _outputAllTS_AllVio = mark;}
	public void setOutputAllTS_AllVio(boolean mark, String path) { _outputAllTS_AllVio = mark; setPathToFileAllTS_AllVio(path);}
	public void setOutputAllTS_AllVio(boolean mark, String path, int[] contiInfo) { setOutputAllTS_AllVio(mark, path);}
	public void setPathToFileAllTS_AllVio(String path) { _pathToFileAllTS_AllVio = path;}
	public String getPathToFileAllTS_AllVio() { return _pathToFileAllTS_AllVio;}

	public PsseModel getPsseModel() {return _model;}
	public int[] getContiInfo() {return _contiInfo;}

	public void setContiPFInfo(FastDecoupledPowerFlow pfnm1) throws PsseModelException
	{
		_checked = true;
		_convPf = pfnm1.isPfConv();
		_testC = pfnm1.getVioRateC();
		_sumContiBrcVio = _testC.getSumBrcDiff();
		_sumContiVmVio = _testC.getSumVmDiff();
		_vmContiPf = pfnm1.getVM();
		_vaContiPf = pfnm1.getVA();
		if (_TSOption == 7 || _TSOption == 8) _pfrmContiPf = pfnm1.getPfrom();
//		_model.getBusTypeManagerData().setPreTSBusType(pfnm1.getSlackBuses(), pfnm1.getPvBuses(), pfnm1.getPqBuses());
	}
	
	public void setAbnormalVioTol(float sumBrcVioAbnormal, float sumVmVioAbnormal)
	{
		_sumBrcVioAbnormal = sumBrcVioAbnormal;
		_sumVmVioAbnormal = sumVmVioAbnormal;
	}

	/** Launch Transmission switching */
	void launchTS() throws PsseModelException, IOException {System.err.println("Program should never come to here - abstract lauchTS()..");}

	/** Before calling this function, busType has to be set to PreCA mode. */
	public boolean runContiPf() throws PsseModelException, IOException
	{
		long timeTmp = System.nanoTime();
		FastDecoupledPowerFlow pfnm1 = new FastDecoupledPowerFlow(_model);
		pfnm1.setShowRunInfo(false);
//		if (_MarkUsingVgSched == false) {pfnm1.setMarkUsingVgSched(false); pfnm1.setRealTimeStartForGenBus(_isRealTimeStartForGenBus);}
//		if (_vstartConti == VoltageSource.LastSolved) {pfnm1.setVA(_vaBasePf.clone()); pfnm1.setVM(_vmBasePf.clone());}
		pfnm1.setMarkUsingVgSched(false);
		pfnm1.setVA(_vaBasePf.clone()); pfnm1.setVM(_vmBasePf.clone());
		pfnm1.runPowerFlow(VoltageSource.LastSolved, _MarkGenVarLimit, _GenVarLimitOption, _MaxIterPfVarLimit);
//		pfnm1.runPowerFlow(VoltageSource.Flat, true, 1, 10);
//		_model.getBusTypeManagerData().setPreTSBusType(pfnm1.getSlackBuses(), pfnm1.getPvBuses(), pfnm1.getPqBuses());

		_convPf = pfnm1.isPfConv();
		
		if (_convPf) {if (_rank == 0) System.out.println("    The power flow under this contingency converged.");}
		else {if (_rank == 0) System.out.println("    The power flow under this contingency did not converge."); return _convPf;}
		
		_testC = new VioResult(_model, _nbr, true);
		analyzeVioResult(_testC, pfnm1);
		_sumContiBrcVio = _testC.getSumBrcDiff();
		_sumContiVmVio = _testC.getSumVmDiff();
		
		if (_rank == 0)
		{
			if (_sumContiVmVio > _sumVmVioAbnormal || _sumContiBrcVio > _sumBrcVioAbnormal)
			{
				String path = ParamIO.getOutPath();
				String title = null;
				if (_contiInfo[2] > 0)
				{
					title = "Super warning: Brc contingency "+_contiInfo[0]+" cause abnormal large violations.";
					System.err.println("\nSuper warning: Brc contingency "+_contiInfo[0]+" cause abnormal large violations. \n ");
				}
				else
				{
					title = "Super warning: Gen contingency "+_contiInfo[0]+" cause abnormal large violations.";
					System.err.println("\nSuper warning: Gen contingency "+_contiInfo[0]+" cause abnormal large violations. \n ");
				}
				title = title + "  SumBrcVio (in per unit) : "+ _sumContiBrcVio + ",  " + "SumVmVio (in per unit) : "+ _sumContiVmVio;
				
				String[] titles = new String[] {_title, title};
				AuxFileXL.initFileWithTitle(path+"AbnormalContiViolationData.txt", titles, true);
			}
		}
		
		_vaContiPf = pfnm1.getVA();
		_vmContiPf = pfnm1.getVM();
		if (_TSOption == 7 || _TSOption == 8) _pfrmContiPf = pfnm1.getPfrom();
		_timeExcluded += System.nanoTime() - timeTmp;
		return _convPf;
	}
	
	void analyzeVioResult(VioResult test, FastDecoupledPowerFlow pf) throws PsseModelException
	{
        if (_isAllBusMnt == false) test.setIsBusMnt(getIsBusMonitor());
        if (_isAllBrcMnt == false) test.setIsBrcMnt(getIsBrcMonitor());
        test.setVmMax(_Vmax); test.setVmMin(_Vmin);
        test.launch(pf.getVM(), pf.getVA(), 
        		pf.getPfrom(), pf.getPto(), pf.getQfrom(), pf.getQto(), pf.getSfrom(), pf.getSto(), getRateC());
	}

	/** Return flags Whether a bus would be monitored for voltage violation. */
	protected boolean[] getIsBusMonitor() throws PsseModelException
	{
		if (_isBusMnt == null) _isBusMnt = _model.getElemMnt().getIsBusMnt();
		return _isBusMnt;
	}

	/** Return flags Whether a branch would be monitored for flow violation. */
	protected boolean[] getIsBrcMonitor() throws PsseModelException
	{
		if (_isBrcMnt == null) _isBrcMnt = _model.getElemMnt().getIsBrcMnt();
		return _isBrcMnt;
	}
	
	protected float[] getRateC() throws PsseModelException
	{
		if (_RateC == null) _RateC = _model.getACBrcCapData().getRateC();
		return _RateC;
	}
	
	void runSingleTS(int i) throws PsseModelException, IOException
	{
		_branches.setInSvc(i, false);
        FastDecoupledPowerFlow pfTS = runTSPowerFlow();
        recordTSVios(pfTS, i);
//        /* Below: temporary code for SCEDwCTS */
//        float[] sfrm = pfTS.getSfrom();
//        float[] sto = pfTS.getSto();
//        int[] monitorBrc = new int[] {228};
//        for (int idx=0; idx<monitorBrc.length; idx++)
//        {
//        	float flow = Math.max(sfrm[monitorBrc[idx]], sto[monitorBrc[idx]]);
//            _model.getDiary().hotLine(LogTypeXL.CheckPoint, "CTS: " + (i+1) + ", monitor line: " + monitorBrc[idx] + ", flow: " + flow);;
//        }
//        /* Above: temporary code for SCEDwCTS */
        _branches.setInSvc(i, true);
	}
	
	void runSingleFACTS(int i) throws PsseModelException, IOException
	{
		//TODO: to check
		float origX = _model.getBranches().getX(i);
		float revisedX = origX * ParamFACTS.getXChangeRatio();
		_model.getACBrcCapData().setX(i, revisedX);
		_model.clearXFilter(); // this can be improved by just changing the corresponding x element
		//_model.getXFilter().set(index, element);
		
        FastDecoupledPowerFlow pfTS = runTSPowerFlow();
        recordTSVios(pfTS, i);
        
		_model.getACBrcCapData().setX(i, origX);
		_model.clearXFilter();
	}
	
	FastDecoupledPowerFlow runTSPowerFlow() throws PsseModelException, IOException
	{
		_model.getBusTypeManagerData().usePreTSBusType();
        FastDecoupledPowerFlow pfTS = new FastDecoupledPowerFlow(_model);
		pfTS.setShowRunInfo(false);
		pfTS.setMarkUsingVgSched(false);
		pfTS.setVA(_vaContiPf.clone()); pfTS.setVM(_vmContiPf.clone());
		pfTS.runPowerFlow(VoltageSource.LastSolved, _MarkGenVarLimit, _GenVarLimitOption, _MaxIterPfVarLimit);
        return pfTS;
	}

	public void recordTSVios(FastDecoupledPowerFlow pfTS, int i) throws PsseModelException
	{
        boolean converged = pfTS.isPfConv();
        if (converged)
        {
    		VioResult testC = new VioResult(_model, _nbr, true);
    		analyzeVioResult(testC, pfTS);
        	float sumBrcVio = testC.getSumBrcDiff();
        	float sumVmVio = testC.getSumVmDiff();

        	dumpTSConvPackage(i, sumBrcVio, sumVmVio, testC);
            if (testC.getViol() == false) {_idxBrcTStoNoVio.add(i); checkVioImp(i, sumBrcVio, sumVmVio);}
            else
            {
            	boolean individualVioWorse = checkIndividualVioWorse(testC);
            	if (individualVioWorse == false)
            	{
            		checkVioImp(i, sumBrcVio, sumVmVio);
            		boolean isWithinZeroVio = false;
            		if (_setTolForZeroVioWithinTol == true) isWithinZeroVio = checkZeroVioWithinTol(pfTS, sumVmVio, sumBrcVio);
                	if (isWithinZeroVio == true) { _idxBrcTStoNoVio.add(i);}
                	else
                	{
                		_idxBrcTStoRedVio.add(i);
                		
//                    	float initSumVmVio = _sumContiVmVio * 0.99f;
//                    	float initSumMVAVio = _sumContiBrcVio * 0.99f;
//                    	
//                    	// both flow violation summation and voltage violation summation have to be reduced
//                    	// (no newVio if contiVio is zero) for a switching action to be considered as beneficial. 
//                    	boolean markRedVio = true;
//                    	if (sumVmVio > _tolSumVm)
//                    	    {if (sumVmVio > initSumVmVio) markRedVio = false;}
//                    	if (sumBrcVio > _tolSumMVA)
//                        	{if (sumBrcVio > initSumMVAVio) markRedVio = false;}
//                    	if (markRedVio == true) {_idxBrcTStoRedVio.add(i);}
//                    	else {_idxBrcTSVio.add(i);}
                	}
            	} else {_idxBrcTSVio.add(i);}
            }
        } else {
        	_idxBrcTSNotConv.add(i);
        	dumpTSNotConvPackage(i);
        }
	}

	
	int determineTSList() throws PsseModelException
	{
		TransmSwitList tsListD = _model.getTransmSwitListData();
		// branches clostest to contingency element.
		int numTS = ParamTS.getNumTS();
		if (_TSOption == 1)
		{
			if (_contiInfo[2] == -1) _idxTS = tsListD.getNearbyBrcsForOneGen(_contiInfo[0] - 1, numTS);
			else _idxTS = tsListD.getNearbyBrcs(_contiInfo[0]-1, numTS, false);
		}
		else if (_TSOption == 2)
		{
    		AnalyzeVioResult analyzePfnm1Vio = new AnalyzeVioResult(_testC);
			int[] idxVioBus = analyzePfnm1Vio.findSeriousBusWithVio(ParamTS.getNumVmVioConcern());
			int[] idxVioBrc = analyzePfnm1Vio.findSeriousBrcWithVio(ParamTS.getNumBrcVioConcern(), ParamTS.getIsNumBrcVioAbsConcern());
			_idxTS = tsListD.getNearbyBrcs(idxVioBus, idxVioBrc, numTS, false);
		}
		else if (_TSOption == 3) {_idxTS = tsListD.getTSListDataMining();}
		else if (_TSOption == 4) {_idxTS = tsListD.calcEnumTSList();}
		else if (_TSOption == 5 || _TSOption == 6) {
			if (_contiInfo[2] == -1) _idxTS = tsListD.getTSListEDM(false, _contiInfo[0]-1);
			else _idxTS = tsListD.getTSListEDM(true, _contiInfo[0]-1);
		}
		else if (_TSOption == 7 || _TSOption == 8) {
			_idxTS = tsListD.getTSListLODF(_testC, _pfrmContiPf, _TSOption);
		}
		
		numTS = 0;
		if (_idxTS != null) numTS = _idxTS.length;
//        if (_outputAllTS == true)
//		{
        	_sumVioTS = new float[numTS][2];
        	_sumVioTSImp = new float[numTS][2];
        	_numVioTS = new int[numTS][2];
			_indicatorIndiElemVioCheck = new int[numTS][4];
//		}
		return numTS;
	}
	
	void buildPrintStream() throws FileNotFoundException 
	{
		if (_rank ==0 && _outputAllTS == true)
		    _outFile = new PrintStream (new FileOutputStream(_pathToFileAllTS, true), true);
		if (_outputAllTS_AllVio == true)
		    _outFile_AllVio = new PrintStream (new FileOutputStream(_pathToFileAllTS_AllVio, true), true);
	}
	
	void closePrintStream() throws FileNotFoundException 
	{
		if (_rank == 0 && _outputAllTS == true) { _outFile.flush(); _outFile.close();}
		if (_outputAllTS_AllVio == true) { _outFile_AllVio.flush(); _outFile_AllVio.close();}
	}

	void writeNBestTS() throws PsseModelException 
	{
		_model.getNBestTSReport().writeNBestTS(_idxTS, _indicatorIndiElemVioCheck, _sumVioTSImp, _contiInfo, _sumContiBrcVio, _sumContiVmVio);
		_model.getNBestTSReport_NoTitle().writeNBestTS(_idxTS, _indicatorIndiElemVioCheck, _sumVioTSImp, _contiInfo, _sumContiBrcVio, _sumContiVmVio);
	}
	
	void dumpTSConvPackage(int i, float sumBrcVio, float sumVmVio, VioResult testC) throws PsseModelException {}
	void dumpTSNotConvPackage(int i) throws PsseModelException {}
	void dumpTSNotInSvcPackage(int i) throws PsseModelException {}

	boolean checkZeroVioWithinTol(FastDecoupledPowerFlow pfnm1, float sumVmVio, float sumBrcVio) throws PsseModelException
	{
		boolean zeroVio = true;
		float tolA = 0.01f;
		float tolB = 0.02f;
		if ((sumVmVio < (1 + tolA) * _sumContiVmVio) && (sumBrcVio < (1 + tolA) * _sumContiBrcVio))
		{
			float[] vm = pfnm1.getVM();
			for (int i=0; i<vm.length; i++)
			{
				if (vm[i] > _Vmax * (1 + tolB)) {zeroVio = false; break;}
				else if(vm[i] < _Vmin * (1 - tolB)) {zeroVio = false; break;}
			}
			if (zeroVio == true)
			{
				float[] sfrm = pfnm1.getSfrom();
				float[] sto = pfnm1.getSto();
				for(int i=0; i<sfrm.length; i++)
				{
					float sMax = Math.max(sfrm[i], sto[i]);
					if (sMax > _RateC[i]*(1 + tolB)/100) {zeroVio = false; break;}
				}
			}
		}
		else {zeroVio = false;}
		return zeroVio;
	}
	
	void checkVioImp(int idxTS, float sumBrcVio, float sumVmVio)
	{
		float brcVioImp = getPerctImp(_sumContiBrcVio, sumBrcVio);
		float vmVioImp = getPerctImp(_sumContiVmVio, sumVmVio);
		
		if (brcVioImp >= _maxBrcVioImp)
		{
			boolean record = true;
			if ((brcVioImp == _maxBrcVioImp) && (vmVioImp <= _VmVioImpAssoToMaxBrcVioImp))
				record = false;
			if (record == true)
			{
				_idxTSMaxBrcVioImp = idxTS;
				_maxBrcVioImp = brcVioImp;
				_VmVioImpAssoToMaxBrcVioImp = vmVioImp;
			}
		}
		if (vmVioImp >= _maxVmVioImp)
		{
			boolean record = true;
			if ((vmVioImp == _maxVmVioImp) && (brcVioImp <= _BrcVioImpAssoToMaxVmVioImp))
				record = false;
			if (record == true)
			{
				_idxTSMaxVmVioImp = idxTS;
				_maxVmVioImp = vmVioImp;
				_BrcVioImpAssoToMaxVmVioImp = brcVioImp;
			}
		}
		float sumVioImp = brcVioImp + vmVioImp;
		if (sumVioImp > _maxSumVioImp)
		{
			_idxTSMaxSumVioImp = idxTS;
			_maxSumVioImp = sumVioImp;
			_BrcVioImpAssoToMaxSumVioImp = brcVioImp;
			_VmVioImpAssoToMaxSumVioImp = vmVioImp;
		}
	}

	
	boolean isNewVioOccured(VioResult testC)
	{
		boolean mark = false;
		_newBrcVioOccured = isNewBrcVioOccured(testC);
		_newVmVioOccured = isNewVmVioOccured(testC);
		if (_newBrcVioOccured || _newVmVioOccured) mark = true;
		return mark;
	}
	boolean isNewBrcVioOccured(VioResult testC)
	{
		if (_testC.sizeBrc() == 0)
		{
			if (testC.sizeBrc() == 0) return false;
			else
			{
				float[] brcDiff = testC.getBrcDiff();
				for (int i=0; i<brcDiff.length; i++)
					if (brcDiff[i] > _brcVioElemTol) return true;
				return false;
			}
		}
		else
		{
			if (testC.sizeBrc() == 0) return false;
			if (_idxBrcVioConti == null) _idxBrcVioConti = AuxArrayXL.toArrayList(_testC.getIdxBrc());
			int[] idxBrcVioTS = testC.getIdxBrc();
			float[] brcDiff = testC.getBrcDiff();
			for (int i=0; i<idxBrcVioTS.length; i++)
			{
				int brcVioBusIdx = idxBrcVioTS[i];
				if (_idxBrcVioConti.contains(brcVioBusIdx) == false)
				{
					if (brcDiff[i] > _brcVioElemTol) return true;
				}
			}
			return false;
		}
	}
	boolean isNewVmVioOccured(VioResult testC)
	{
		if (_testC.sizeV() == 0)
		{
			if (testC.sizeV() == 0) return false;
			else
			{
				float[] vmDiffAbs = testC.getVmDiffAbs();
				for (int i=0; i<vmDiffAbs.length; i++)
					if (vmDiffAbs[i] > _vmVioElemTol) return true;
				return false;
			}
		}
		else
		{
			if (testC.sizeV() == 0) return false;
			if (_idxVmVioConti == null) _idxVmVioConti = AuxArrayXL.toArrayList(_testC.getIdxV());
			int[] idxVmVioTS = testC.getIdxV();
			float[] vmDiffAbs = testC.getVmDiffAbs();
			for (int i=0; i<idxVmVioTS.length; i++)
			{
				int vmVioBusIdx = idxVmVioTS[i];
				if (_idxVmVioConti.contains(vmVioBusIdx) == false)
				{
					if (vmDiffAbs[i] > _vmVioElemTol) return true;
				}
			}
			return false;
		}
	}
	
	
	boolean isContiVioWorse(VioResult testC)
	{
		boolean mark = false;
		_contiBrcVioWorse = isContiBrcVioWorse(testC);
		_contiVmVioWorse = isContiVmVioWorse(testC);
		if (_contiBrcVioWorse || _contiVmVioWorse) mark = true;
		return mark;
	}
	boolean isContiBrcVioWorse(VioResult testC)
	{
		if (_testC.sizeBrc() == 0) return false;
		else
		{
			if (testC.sizeBrc() == 0) return false;
			int[] idxBrcVioConti = _testC.getIdxBrc();
			float[] brcDiffConti = _testC.getBrcDiff();
			ArrayList<Integer> idxBrcVioTS = AuxArrayXL.toArrayList(testC.getIdxBrc());
			float[] brcDiffTS = testC.getBrcDiff();

			for (int i=0; i<idxBrcVioConti.length; i++)
			{
				int idx = idxBrcVioConti[i];
				if (idxBrcVioTS.contains(idx) == true)
				{
					int idxTS = idxBrcVioTS.indexOf(idx);
					if (brcDiffTS[idxTS] > (brcDiffConti[i] + _brcVioElemTol)) return true;
				}
			}
			return false;
		}
	}
	boolean isContiVmVioWorse(VioResult testC)
	{
		if (_testC.sizeV() == 0) return false;
		else
		{
			if (testC.sizeV() == 0) return false;
			int[] idxVmVioConti = _testC.getIdxV();
			float[] vmDiffAbsConti = _testC.getVmDiffAbs();
			ArrayList<Integer> idxVmVioTS = AuxArrayXL.toArrayList(testC.getIdxV());
			float[] vmDiffAbsTS = testC.getVmDiffAbs();

			for (int i=0; i<idxVmVioConti.length; i++)
			{
				int idx = idxVmVioConti[i];
				if (idxVmVioTS.contains(idx) == true)
				{
					int idxTS = idxVmVioTS.indexOf(idx);
					if (vmDiffAbsTS[idxTS] > (vmDiffAbsConti[i] + _vmVioElemTol)) return true;
				}
			}
			return false;
		}
	}
	
	
	/** Includes sumBrcVio and sumVmVio. */
	void writeContiInfo(PrintStream outFile)
	{
		AuxFileXL.outputElemFrmArray(outFile, _contiInfo, false);
		writeContiSumVio(outFile);
	}
	
	void writeContiSumVio(PrintStream outFile) 
	{
		outFile.print(" "+_sumContiBrcVio+" "+_sumContiVmVio);	
	}
	
	void writeConstantArray(PrintStream outFile, int num, int elem, boolean nextLine)
	{
		AuxFileXL.outputElemFrmArray(outFile, getArrayWithSameElems(num, elem), nextLine);		
	}
	
	int[] getArrayWithSameElems(int num, int elem)
	{
		int[] array = new int[num];
		Arrays.fill(array, elem);
		return array;
	}
	float[] getArrayWithSameElems(int num, float elem)
	{
		float[] array = new float[num];
		Arrays.fill(array, elem);
		return array;
	}
	
	float getPerctImp(float oldSumVio, float newSumVio)
	{
		if (oldSumVio == 0) return 0f;
		float imp = (oldSumVio - newSumVio) / oldSumVio;
		return imp;
	}
	
	/** Return an int[] with three elements.
	 * Either: idxBrc+1, idxFrmBus+1, idxToBus+1;
	 * Or: idxBrc+1, From Bus Number, To Bus Number.  
	 * @throws PsseModelException */
	int[] getBrcInfoForOutput(int idxBrc) throws PsseModelException
	{
		int numFrmBus = _branches.getFromBus(idxBrc).getIndex();
		int numToBus = _branches.getToBus(idxBrc).getIndex();
		if (_useToolBusIndexforOutput == true) { numFrmBus++; numToBus++;}
		else
		{
			numFrmBus = _branches.getFromBus(idxBrc).getI();
			numToBus = _branches.getToBus(idxBrc).getI();
		}
		idxBrc++;
		return new int[] {idxBrc, numFrmBus, numToBus};
	}
	
	/** Return bus index plus one or the bus number as in the .raw file. */
	int getBusInfoForOutput(int idxBus) throws PsseModelException
	{
		idxBus++;
		if (_useToolBusIndexforOutput == false)
			idxBus = _model.getBuses().get(idxBus-1).getI();
		return idxBus;
	}
	
	
	void dumpContiNotConv(PrintStream outFile, int numCollumLeft)
	{
		long timeTmp = System.nanoTime();
		AuxFileXL.outputElemFrmArray(outFile, _contiInfo, false);
		writeConstantArray(outFile, numCollumLeft, -3, true);
		_timeExcluded += System.nanoTime() - timeTmp;
	}
	
	void dumpContiNotCritical(PrintStream outFile, int numCollumLeft)
	{
		long timeTmp = System.nanoTime();
		writeContiInfo(outFile);
		writeConstantArray(outFile, numCollumLeft, -4, true);
		_timeExcluded += System.nanoTime() - timeTmp;
	}
		
	void dumpAllTSResults(PrintStream outFile) throws PsseModelException
	{
		long timeTmp = System.nanoTime();
		for (int i=0; i<_numTSChecked; i++)
		{
			writeContiInfo(outFile);
			int idxBrc = _idxTS[i];
			int numberFrmBus = _branches.get(idxBrc).getFromBus().getI();
			int numberToBus = _branches.get(idxBrc).getToBus().getI();
			outFile.print(" "+(idxBrc+1)+" "+numberFrmBus+" "+numberToBus);
			AuxFileXL.outputElemFrmArray(outFile, _sumVioTS[i], false);
			AuxFileXL.outputElemFrmArray(outFile, _sumVioTSImp[i], false);
			AuxFileXL.outputElemFrmArray(outFile, _numVioTS[i], false);
			AuxFileXL.outputElemFrmArray(outFile, _indicatorIndiElemVioCheck[i], true);
//			outFile.flush();
			
			if (_rank == 0)
			{
				if (_sumVioTS[i][0] > _sumBrcVioAbnormal || _sumVioTS[i][1] > _sumVmVioAbnormal)
				{
					String path = ParamIO.getOutPath();
					String title = null, title2 = null;
					
					if (_contiInfo[2] > 0) title = "Super warning: Brc contingency "+_contiInfo[0];
					else title = "Super warning: Gen contingency "+_contiInfo[0];
					title = title + "  SumBrcVio (in per unit) : "+ _sumContiBrcVio + ",  " + "SumVmVio (in per unit) : "+ _sumContiVmVio;
					title2 = "TS Brc: " + (idxBrc+1) + "cause abnormal violations.";
					title2 = title2 + "SumBrcVio (in per unit) : "+ _sumVioTS[i][0] + ",  " + "SumVmVio (in per unit) : "+ _sumVioTS[i][1] + " \n ";
					
					String[] titles = new String[] {_title, title, title2};
					AuxFileXL.initFileWithTitle(path+"AbnormalTransmSwitViolationData.txt", titles, true);
				}
			}
		}
		_timeExcluded += System.nanoTime() - timeTmp;
	}
	
	
	void dumpContiAllVioPrefixData()
	{
		_outFile_AllVio.print(" "+_markConti);
		writeContiInfo(_outFile_AllVio);
	}

	void dumpContiAllVio() throws PsseModelException
	{
		long timeTmp = System.nanoTime();
		if (_testC.size() == 0) { dumpContiAllVioPrefixData(); writeConstantArray(_outFile_AllVio, 5, -6, true);}
		else dumpAllVio(_outFile_AllVio, _testC, 1, -1);	
		_timeExcluded += System.nanoTime() - timeTmp;
	}
	
	/** For contingency violation info, the 4-th param does not have any meaning. */
	void dumpAllVio(PrintStream outFile, VioResult testC, int optionPrefixData, int idxBrcTS) throws PsseModelException
	{
		if (testC.sizeBrc() != 0) dumpAllBrcVio(outFile, testC, optionPrefixData, idxBrcTS);
		if (testC.sizeV() != 0) dumpAllVmVio(outFile, testC, optionPrefixData, idxBrcTS);
	}
	
	void dumpAllBrcVio(PrintStream outFile, VioResult testC, int optionPrefixData, int idxBrcTS) throws PsseModelException
	{
		int[] idxBrcs = testC.getIdxBrc();
		float[] pfDiffs = testC.getBrcDiff();
		for (int i=0; i<idxBrcs.length; i++)
		{
			if (optionPrefixData == 1) dumpContiAllVioPrefixData();
			else if (optionPrefixData == 2) dumpTSAllVioPrefixData(idxBrcTS, testC);
			int idxBrc = idxBrcs[i];
			AuxFileXL.outputElemFrmArray(outFile, getBrcInfoForOutput(idxBrc), false);
			outFile.println(" "+ pfDiffs[i] + " "+ (_branches.getRateC(idxBrc)/100));
		}
	}
	
	void dumpAllVmVio(PrintStream outFile, VioResult testC, int optionPrefixData, int idxBrcTS) throws PsseModelException
	{
		int[] idxBusVms = testC.getIdxV();
		float[] vmDiffs = testC.getVmDiff();
		for (int i=0; i<idxBusVms.length; i++)
		{
			if (optionPrefixData == 1) dumpContiAllVioPrefixData();
			else if (optionPrefixData == 2) dumpTSAllVioPrefixData(idxBrcTS, testC);
			int idxBusVm = idxBusVms[i];
			int idxBusOutput = getBusInfoForOutput(idxBusVm);
			outFile.print(" " + idxBusOutput + " " + (-1) + " " + (-1));
			outFile.println(" "+ vmDiffs[i] + " "+ (-6));
		}
	}
	
	void dumpTSNotInSer(int idxBrcTS) throws PsseModelException
	{
		long timeTmp = System.nanoTime();		
		_outFile_AllVio.print(" "+_markTS);
		AuxFileXL.outputElemFrmArray(_outFile_AllVio, getBrcInfoForOutput(idxBrcTS), false);
		writeConstantArray(_outFile_AllVio, 7, -5, true);
		_timeExcluded += System.nanoTime() -timeTmp;
	}

	void dumpTSNotConv(int idxBrcTS) throws PsseModelException
	{
		long timeTmp = System.nanoTime();		
		_outFile_AllVio.print(" "+_markTS);
		AuxFileXL.outputElemFrmArray(_outFile_AllVio, getBrcInfoForOutput(idxBrcTS), false);
		writeConstantArray(_outFile_AllVio, 7, -2, true);
		_timeExcluded += System.nanoTime() -timeTmp;
	}
	
	void dumpTSAllVioPrefixData(int idxBrcTS, VioResult testC) throws PsseModelException
	{
		_outFile_AllVio.print(" "+_markTS);
		AuxFileXL.outputElemFrmArray(_outFile_AllVio, getBrcInfoForOutput(idxBrcTS), false);
		_outFile_AllVio.print(" "+ testC.getSumBrcDiff() + " "+ testC.getSumVmDiff());
	}

	void dumpTSAllVioInfo(int idxBrcTS, VioResult testC) throws PsseModelException
	{
		long timeTmp = System.nanoTime();		
		if (testC.size() == 0) { dumpTSAllVioPrefixData(idxBrcTS, testC); writeConstantArray(_outFile_AllVio, 5, -6, true);}
		else dumpAllVio(_outFile_AllVio, testC, 2, idxBrcTS);	
		_timeExcluded += System.nanoTime() -timeTmp;
	}

	boolean checkIndividualVioWorse(VioResult testC)
	{
    	boolean individualVioWorse = false;
    	if (_checkNewVioOccured == true) {if (isNewVioOccured(testC) == true) individualVioWorse = true;}
    	if (_checkContiVioWorse == true) {if (isContiVioWorse(testC) == true) individualVioWorse = true;}
    	setIndicatorIndiElemVioCheck();
    	return individualVioWorse;
	}
	void setIndicatorIndiElemVioCheck() { System.err.println("Program should never come to here - abstract setIndicatorIndiElemVioCheck()");}
	
	void setNumTSForRoughlyStat()
	{
		_numBrcTStoNoVio = _idxBrcTStoNoVio.size();
		_numBrcTStoRedVio = _idxBrcTStoRedVio.size();
		_numBrcTSVio = _idxBrcTSVio.size();
		_numBrcTSNotConv = _idxBrcTSNotConv.size();
	}
	
	public int getThreadNumber() {return _nproc;}
	public int getThreadRank() {return _rank;}
	
	public float getTolSumMVA() {return _tolSumMVA;}
	public float getTolSumVm() {return _tolSumVm;}
	
	public boolean getWithinTol() {return _withinTol;}
	public float getSumContiVmVio() {return _sumContiVmVio;}
	public float getSumContiBrcVio() {return _sumContiBrcVio;}
	
	public int getNumTSChecked() {return _numTSChecked;}
	public int getNumTStoNoVio()  {return _numBrcTStoNoVio;}
	public int getNumTStoRedVio() {return _numBrcTStoRedVio;}
	/** Including TS for which the power flow does not converge. */
	public int getNumTStoVio()    {return (_numBrcTSVio + _numBrcTSNotConv);}
	
	public int getIdxTSMaxBrcVioImp() {return _idxTSMaxBrcVioImp;}
	public float getMaxBrcVioImp() {return _maxBrcVioImp;}
	public float getVmVioImpAssoToMaxBrcVioImp() {return _VmVioImpAssoToMaxBrcVioImp;}
	
	public int getIdxTSMaxVmVioImp() {return _idxTSMaxVmVioImp;}
	public float getMaxVmVioImp() {return _maxVmVioImp;}
	public float getBrcVioImpAssoToMaxVmVioImp() {return _BrcVioImpAssoToMaxVmVioImp;}
	
	public int getIdxTSMaxSumVioImp() {return _idxTSMaxSumVioImp;}
	public float getMaxSumVioImp() {return _maxSumVioImp;}
	public float getBrcVioImpAssoToMaxSumVioImp() {return _BrcVioImpAssoToMaxSumVioImp;}
	public float getVmVioImpAssoToMaxSumVioImp() {return _VmVioImpAssoToMaxSumVioImp;}
	
	public long getTimeExcluded() { return _timeExcluded;}
	
	public int[] getIdxTSCheck()   {return _idxTS;}
	public float[][] getSumVioTS()   {return _sumVioTS;}
	public float[][] getSumVioTSImp()   {return _sumVioTSImp;}
	public int[][] getNumVioTS()    {return _numVioTS;}
	public int[][] getMarkIndiVio()  {return  _indicatorIndiElemVioCheck;}
	
	
	// added on 07/30/2017
	public float[][] getSumBrcVioTS()
	{
		float[][] newArray = new float[2][];
		newArray[0] = AuxArrayXL.transposeArray(_sumVioTS)[0];
		newArray[1] = AuxArrayXL.transposeArray(_sumVioTSImp)[0];
		return newArray;
	}
	
}
