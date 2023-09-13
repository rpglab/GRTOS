package com.rtca_cts.contingencyanalysis;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import org.junit.Test;

import com.casced.Results4ReDispatch;
import com.powerdata.openpa.psse.ACBranch;
import com.powerdata.openpa.psse.ACBranchList;
import com.powerdata.openpa.psse.Bus;
import com.powerdata.openpa.psse.BusList;
import com.powerdata.openpa.psse.GenList;
import com.powerdata.openpa.psse.PsseModel;
import com.powerdata.openpa.psse.PsseModelException;
import com.powerdata.openpa.psse.powerflow.FastDecoupledPowerFlow;
import com.powerdata.openpa.psse.powerflow.VoltageSource;
import com.rtca_cts.param.ParamCA;
import com.rtca_cts.param.ParamFDPFlow;
import com.rtca_cts.param.ParamVio;
import com.utilxl.iofiles.AuxFileXL;
import com.utilxl.string.AuxStringXL;


/**
 * Implement contingency analysis;
 * 
 * Initialized in Dec. 2014.
 * 
 * @author Xingpeng.Li, xplipower@gmail.com 
 */
public abstract class ContingencyAnalysis {
	
	// for threads info
	int _nproc = 1;
	int _rank = 0;
	
	//
	PsseModel _model;
	VoltageSource _vstart;   // for power flow in base base. Default: Flat. 
	VoltageSource _vstartCA = VoltageSource.LastSolved;   // For contingency analysis; Default: LastSolved. 
	ACBranchList _branches;
	GenList _gens;
	BusList _buses;
	float[] _BaseKV;   // Voltage level, unit: KV.

	String _path;   // for details violation information output files.
	String _pathForGeneInfo;   // for general violation information output files.
	String _uri = null;
	boolean _showRunInfo = true;   // output some CA results if true.

	int _nbr;
	int _nGen;
	int _nBus;
	
	boolean _markTolVio = ParamVio.getMarkTolVio();    // if true: then, tolerance is applied, and record the contingencies
	                                                   // that the corresponding flow violation or voltage violation is over _tolMVA or _tolVm.
    float _tolSumMVA = ParamVio.getTolSumMVA();    // tolerance, summation of flow violations (in per unit).
    float _tolSumVm = ParamVio.getTolSumVm();    // tolerance, summation of voltage violations (in per unit).

	float _Vmax = ParamVio.getVmMax();
	float _Vmin = ParamVio.getVmMin();
	
	boolean _isAllBusMnt = ParamVio.getIsAllBusMnt();   // monitor voltage violations on all buses. 
	boolean _isAllBrcMnt = ParamVio.getIsAllBrcMnt();  // monitor flow violations on all branches. 
	boolean[] _isBusMnt = null;
	boolean[] _isBrcMnt = null;
	
	// for Gen Var Limit
	boolean _MarkGenVarLimit = ParamFDPFlow.getGenVarLimit();  // true by default, Change PV bus to PQ bus once the Qg is beyond its capacity.
	int _GenVarLimitOption = 1;       // 1 by default, options that how deal with Gen Var Limit Constraint.
	int _MaxIterPfVarLimit = 10;      // 10 by default, maximum iteration for option 1.
	boolean _MarkUsingVgSched = true;    // if true, then, use generator scheduled set point to be the Vm of PV Buses, for the original/basic power flow.
	boolean _isRealTimeStartForGenBus = false;   // if _MarkUsingVgSched == true, then, this option doesn't matter, for the original/basic power flow. 
	
	// for base case
	FastDecoupledPowerFlow _PfBase;
	boolean _IsBasePfChecked;
	boolean _convPfBase;
	boolean _MarkvioBase;       // true if there is violation for the base case
	boolean _MarkvioVmBase;     // true if there is voltage violation for the base case
	boolean _MarkvioBrcBase;    // true if there is branch thermal violation for the base case

	float[] _vaBasePf = null;       // Va solution from base case power flow. As a starting point for CA.
	float[] _vmBasePf = null;       // Vm solution from base case power flow. As a starting point for CA.

    int _sizeVmVioBase;
    int[] _getIdxVmVioBase;
    float[] _VmVioBase;
    float[] _VmVioDiffBase;
    
    int _sizeBrcVioBase;
    int[] _getIdxBrcVioBase;
    float[] _sfrmVioBase;
    float[] _stoVioBase;
    float[] _rateUsedBase;
    float[] _brcDiffVioBase;

	// for transmission contingency
	float[] _sumVioBrcPerContTransm;        // sum of thermal violation per contingency.
	float _sumVioBrcWorstContTransm;        // sum of thermal violation of the worst contingency situation.
	int _idxVioBrcWorstContTransm;        // 

	int _numTestedNm1Transm;   // number of branches that were performed Nm1.

	int _nContVioVmAndBrcAllTransm;   // total # of contingencies with any/either type of violation.
	int[] _IdxContVioVmAndBrcAllTransm;      // index of contingencies with any/either type of violation.
    int[] _idxMapContToVmVioContTransm;   // the element corresponds to the index of _IdxContVioVmAllTransm.
    int[] _idxMapContToBrcVioContTransm;
	float[] _sumBrcVioExtPerContTransm;          // summation of flow violations per contingency (includes contingency causing VmVio and contingency causing BrcVio)
	float[] _sumVmVioExtPerContTransm;          // summation of voltage violations per contingency (includes contingency causing VmVio and contingency causing BrcVio)

	
	int _numContPVtoPQTransm;      // number of contingencies that have PQ buses that are switched from original PV bus. 
	int[] _idxContPVtoPQTransm;    // index of contingency that have PQ buses that are switched from original PV bus. 
	int[] _numPVtoPQEachContTransm;        // how many PV buses are switched to PQ buses for each contingency,
	int[] _numOrigPVEachContTransm;      // number of original PV buses.

	int[] _sizeVAllTransm;    // sizeV[i] denotes how many buses have voltage violation issue in the i-th contingency that recorded
	int[] _IdxContVioVmAllTransm;   // index of branch that cause voltage violations when this branch is gone.
	int[] _idxVioVmMapContExtTransm; // the element corresponds to the index of _IdxContVioVmAndBrcAllTransm.

	float[][] _AllVmDiffTransm;    // the voltage difference (the extent of violation)  by checking N-1, can be either positive or negative
	float[][] _AllVmTransm;    // the voltage magnitude. 
	float[][] _AllVaTransm;    // the voltage angle.
	int[][] _IdxVAllTransm;        // index of bus whose voltage is violated by checking N-1 
	int _totalVmVioNumAllTransm;   // the number of total voltage violations of all contingencies.	
	int _nVioVmAllTransm;         // the number of contingency that cause voltage violation, it is also the length of sizeV, IdxContVioVm;
                                  //  it is also the number of rows of IdxV and VmVioDiff
	int _nVioVmLowAllTransm;       // the number of contingency that cause low voltage violation.
	int _nVioVmHighAllTransm;	   // the number of contingency that cause high voltage violation.
	
	float _sumAllVmVioTransm;
	float _sumAllVmLowVioTransm;
	float _sumAllVmHighVioTransm;

	int[] _numVmVioTransm;     // # of voltage violations Per Contingency, exactly the same with _sizeVAllTransm.
	int[] _numVmLowVioTransm;     // # of low voltage violations Per Contingency
	int[] _numVmHighVioTransm;     // # of high voltage violations Per Contingency

	float[] _maxVmdiffTransm;     // max voltage violations Per Contingency
	float[] _maxVmdiffLowTransm;     // max low voltage violations Per Contingency
	float[] _maxVmdiffHighTransm;     // max high voltage violations Per Contingency
	
	int _numVmVioAllTransm;     // # of voltage violations for all contingencies, exactly the same with _sizeVAllTransm
	int _numVmLowVioAllTransm;     // # of low voltage violations for all contingencies
	int _numVmHighVioAllTransm;     // # of high voltage violations for all contingencies

	float _maxVmdiffAllTransm;       // max voltage violations overall every Contingency
	float _maxVmLowdiffAllTransm;       // max low voltage violations overall every Contingency
	float _maxVmHighdiffAllTransm;       // max high voltage violations overall every Contingency

	int[] _sizeBrcAllTransm;         // # of Branch violation per contingency
	int[] _IdxContVioBrcAllTransm;    // index of branch contingency that cause thermal violations
	
	float[][] _AllBrcDiffTransm;
	int[][] _IdxBrcAllTransm;
	int _totalBrcVioNumAllTransm;
	int _nVioBrcAllTransm;
	float[][] _BrcVioPfrmAllTransm;    // Pfrm information, only for lines that have thermal violation. 
	float[][] _BrcVioPtoAllTransm;
	float[][] _BrcVioQfrmAllTransm;
	float[][] _BrcVioQtoAllTransm;
	
	float[] _maxBrcSdiffPerContTransm;    // max branch thermal violations per Contingency
	int[] _IdxMaxBrcSdiffPerContTransm;    // index of the branch that have maximum thermal violations per Contingency
	float _maxBrcSdiffAllTransm;          // max branch thermal violations overall every Contingency
	int _idxMaxBrcSdiffAllTransm;          // index of branch corresponding to _maxBrcSdiffAllTransm
	float _sumAllSdiffTransm;
	
	float[] _maxBrcVioPerctPerContTransm;    // max branch thermal violations per Contingency  in percentage
	float _maxBrcVioPerctAllTransm;          // max branch thermal violations overall every Contingency in percentage
	
	VioResultAll _testAllTransm;
	PfNotConverge _PfNotCTransm;
	int _numNotConvTransm;        // the number of branches contingencies that the power flow can not converge
	int[] _IdxNotConvTransm;       // store the index of contingency branches, which the power flow program can not converge under those contingencies 


	// for Generator contingency
	float[] _sumVioBrcPerContGen;        // sum of thermal violation per contingency.
	float _sumVioBrcWorstContGen;        // sum of thermal violation of the worst contingency situation.
	int _idxVioBrcWorstContGen;        // 

	int _optionPfactor = ParamCA.getPfactorForGenConti();
	int _numTestedNm1Gen;   // number of Gen that were performed Nm1.
	
	int _nContVioVmAndBrcAllGen;      // total # of contingencies with any/either type of violation.
	int[] _IdxContVioVmAndBrcAllGen;   // index of contingencies with any/either type of violation.
    int[] _idxMapContToVmVioContGen;   // the element corresponds to the index of _IdxContVioVmAllGen.
    int[] _idxMapContToBrcVioContGen;
	float[] _sumBrcVioExtPerContGen;          // summation of flow violations per contingency (includes contingency causing VmVio and contingency causing BrcVio)
	float[] _sumVmVioExtPerContGen;          // summation of voltage violations per contingency (includes contingency causing VmVio and contingency causing BrcVio)

	int _numContPVtoPQGen;      // number of contingencies that have PQ buses that are switched from original PV bus. 
	int[] _idxContPVtoPQGen;    // index of contingency that have PQ buses that are switched from original PV bus. 
	int[] _numPVtoPQEachContGen;        // how many PV buses are switched to PQ buses for each contingency,
	int[] _numOrigPVEachContGen;      // number of original PV buses.
	
	int[] _sizeVAllGen;    // sizeV[i] denotes how many buses have voltage violation issue in the i-th contingency that recorded
	int[] _IdxContVioVmAllGen;   // index of generators that cause voltage violations when this generators is lost
	int[] _idxVioVmMapContExtGen; // the element corresponds to the index of _IdxContVioVmAndBrcAllGen.

	float[][] _AllVmDiffGen;    // the voltage difference (the extent of violation)  by checking N-1 
	float[][] _AllVmGen;    // the voltage magnitude. 
	float[][] _AllVaGen;    // the voltage angle.
	int[][] _IdxVAllGen;        // index of bus whose voltage is violated by checking N-1 
	int _totalVmVioNumAllGen;   // the number of total voltage violations of all contingencies.
	int _nVioVmAllGen;         // the number of contingency that cause voltage violation, it is also the length of sizeV, IdxContVioVm;
                                //  it is also the number of row of IdxV and VmVioDiff.
	int _nVioVmLowAllGen;
	int _nVioVmHighAllGen;

	float _sumAllVmLowVioGen;
	float _sumAllVmHighVioGen;
	float _sumAllVmVioGen;

	
	int[] _numVmVioGen;     // # of voltage violations Per Contingency, exactly the same with _sizeVAllGen.
	int[] _numVmLowVioGen;     // # of low voltage violations Per Contingency
	int[] _numVmHighVioGen;     // # of high voltage violations Per Contingency

	float[] _maxVmdiffGen;     // max voltage violations Per Contingency
	float[] _maxVmdiffLowGen;     // max low voltage violations Per Contingency
	float[] _maxVmdiffHighGen;     // max high voltage violations Per Contingency
	
	int _numVmVioAllGen;     // # of voltage violations for all contingencies, exactly the same with _sizeVAllGen.
	int _numVmLowVioAllGen;     // # of low voltage violations for all contingencies
	int _numVmHighVioAllGen;     // # of high voltage violations for all contingencies

	float _maxVmdiffAllGen;       // max voltage violations overall every Contingency
	float _maxVmLowdiffAllGen;       // max low voltage violations overall every Contingency
	float _maxVmHighdiffAllGen;       // max high voltage violations overall every Contingency
	
	int[] _sizeBrcAllGen;    
	int[] _IdxContVioBrcAllGen;  
	float[][] _AllBrcDiffGen;
	int[][] _IdxBrcAllGen;       
	int _totalBrcVioNumAllGen; 
	int _nVioBrcAllGen;         
	float[][] _BrcVioPfrmAllGen;   // Pfrm information, only for lines that have thermal violation. 
	float[][] _BrcVioPtoAllGen;
	float[][] _BrcVioQfrmAllGen;
	float[][] _BrcVioQtoAllGen;
	
	float[] _maxBrcSdiffPerContGen;    // max branch thermal violations per Contingency
	int[] _IdxMaxBrcSdiffPerContGen;    // index of the branch that have maximum thermal violations per Contingency
	float _maxBrcSdiffAllGen;          // max branch thermal violations overall every Contingency
	int _idxMaxBrcSdiffAllGen;          // index of branch corresponding to _maxBrcSdiffAllGen
	float _sumAllSdiffGen;

	float[] _maxBrcVioPerctPerContGen;    // max branch thermal violations per Contingency  in percentage
	float _maxBrcVioPerctAllGen;          // max branch thermal violations overall every Contingency in percentage

	VioResultAll _testAllGen;
	PfNotConverge _PfNotCGen;
	int _numNotConvGen;      // the number of Gens contingencies that the power flow can not converge
	int[] _IdxNotConvGen;    // store the index of contingency Gens, which the power flow program can not converge under those contingencies 
	GensDyrData _genDyr;        // Generator Param H (inertia).
	
	
	public ContingencyAnalysis(PsseModel model)
			throws PsseModelException, IOException
	{
		initial(model);
		String svstart = "Flat";
		_vstart = VoltageSource.fromConfig(svstart);
	}

	public ContingencyAnalysis(PsseModel model, VoltageSource vstart)
			throws PsseModelException, IOException
	{
		initial(model);
		_vstart = vstart;
	}

	void initial(PsseModel model) throws PsseModelException
	{
		_model = model;
		_branches = _model.getBranches();
	    _nbr = _branches.size();
		_gens = _model.getGenerators();
		_nGen = _gens.size();
		_buses = _model.getBuses();
		_BaseKV = _buses.getBASKV();
		_nBus = _buses.size();
	}
	
	public void setURI(String uri) {_uri = uri;}
	public void setShowRunInfo(boolean mark) { _showRunInfo = mark;}

	public String getPath() {return _path;}
	public void setPath(String path) throws IOException 
	{
		_path = path;
		AuxFileXL.createFolder(path);
	}
	public String getGeneInfoPath() {return _pathForGeneInfo;}
	public void setGeneInfoPath(String pathForGeneInfo) throws IOException
	{
		_pathForGeneInfo = pathForGeneInfo;
		AuxFileXL.createFolder(pathForGeneInfo);
	}

	//For base case power flow
	public void setVstart(VoltageSource vstart)	{_vstart = vstart;}
	public void setMarkUsingVgSched(boolean aa) {_MarkUsingVgSched = aa;}
	public void setRealTimeStartForGenBus(boolean mark) {_isRealTimeStartForGenBus = mark;}
	
	//for all power flow involved 
	public void setMaxIterPfVarLimit(int num) {_MaxIterPfVarLimit = num;}

	//for violation
	public float getVmax() {return _Vmax;}
	public void setVmax(int Vmax) {_Vmax = Vmax;}
	public float getVmin() {return _Vmin;}
	public void setVmin(int Vmin) {_Vmin = Vmin;}

	// Gen Var limit setting
	public void clearGenVarLimit() {_MarkGenVarLimit = false;}
	public void enableGenVarLimit() 
	{
		_MarkGenVarLimit = true;
		_GenVarLimitOption = 1;
	}
	public void enableGenVarLimit(int option)
	{
		_MarkGenVarLimit = true;
		_GenVarLimitOption = option;
	}

	public void clearMarkTolVio() {_markTolVio = false;} 
	public void setMarkTolVio() {_markTolVio = true;} 

	
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

	@Test
	public void initGenDyr() throws PsseModelException
	{
		if (_genDyr == null)
		{
			_genDyr = new GensDyrData();
			float[] mbase = _model.getGenerators().getMBASE();
			float[] MH = _genDyr.getGenMH();
		    assertTrue(MH.length <= mbase.length);
			_genDyr.setGenMachineMVA(mbase);
		}
	}
	
	// check base-case power flow
	public boolean checkOrigPf() throws PsseModelException, IOException
	{
		if (_IsBasePfChecked == false) convOR();
		if (_convPfBase == false)
		{
			System.err.println("Power flow for base case does not converge. So simulation for contingency analysis stops..");
			System.exit(1);
		}
		return _convPfBase;
	}
	
	// check the convergence of the original power flow problem  (Base case)
	public boolean convOR() throws PsseModelException, IOException
	{
		_model.getBusTypeManagerData().usePrePFBusType();
		FastDecoupledPowerFlow pf = new FastDecoupledPowerFlow(_model);
		pf.setShowRunInfo(false);
		if (_showRunInfo == true) System.out.println();
    	if (pf.getNumofIslands() != 1) System.err.println("The network is not fully connected. It has "+pf.getNumofIslands()+" independent islands...");

		if (_MarkUsingVgSched == false)
		{
			pf.setMarkUsingVgSched(false); 
			if (_isRealTimeStartForGenBus == true) pf.setRealTimeStartForGenBus(true);
		}
		pf.runPowerFlow(_vstart, _MarkGenVarLimit, _GenVarLimitOption, _MaxIterPfVarLimit);
		setOrigPfResults(pf);
		return pf.isPfConv();
	}
	
	public void setOrigPfResults(FastDecoupledPowerFlow pf) throws PsseModelException
	{
		boolean conv = pf.isPfConv();
		_convPfBase = conv;
		_vaBasePf = pf.getVA();
		_vmBasePf = pf.getVM();

		if (_showRunInfo == true && _rank == 0)
		{
			if (conv) {System.out.println("The original power flow converged.");}
		    else {System.err.println("The power flow of base case did not converge.");}
		}
		if (conv) analyzeOrigPfResult(pf);
		_PfBase = pf;
		_IsBasePfChecked = true;
	}

	public void analyzeOrigPfResult(FastDecoupledPowerFlow pf) throws PsseModelException
	{
        VioResult test = new VioResult(_model, _nbr, true);
        analyzeVioResult(test, pf, getRateA());
        
        _MarkvioBase = test.getViol();        
     	_MarkvioVmBase = test.getVioVoltage();
    	_MarkvioBrcBase = test.getVioBrc();

	    _sizeVmVioBase = test.sizeV();
	    _getIdxVmVioBase = test.getIdxV();
	    _VmVioBase = test.getVm();
	    _VmVioDiffBase = test.getVmDiff();
	    
	    _sizeBrcVioBase = test.sizeBrc();
	    _getIdxBrcVioBase = test.getIdxBrc();
	    _sfrmVioBase = test.getSfrm();
	    _stoVioBase = test.getSto();
	    _rateUsedBase = test.getRateUsed();
	    _brcDiffVioBase = test.getBrcDiff();
	}

	void analyzeVioResult(VioResult test, FastDecoupledPowerFlow pf, float[] rate) throws PsseModelException
	{
        if (_isAllBusMnt == false) test.setIsBusMnt(getIsBusMonitor());
        if (_isAllBrcMnt == false) test.setIsBrcMnt(getIsBrcMonitor());
        test.setVmMax(_Vmax); test.setVmMin(_Vmin);
        test.launch(pf.getVM(), pf.getVA(), 
        		pf.getPfrom(), pf.getPto(), pf.getQfrom(), pf.getQto(), pf.getSfrom(), pf.getSto(), rate);
	}
	
	// @param i is the index of the Contingency Branch
	protected void runSingleTransmConti(int i) throws PsseModelException, IOException
	{
		if (_showRunInfo == true) System.out.println("Brc Contingency: "+i+", _rank: "+_rank);
		_branches.setInSvc(i, false);
		_numTestedNm1Transm++;
		_model.getBusTypeManagerData().usePrePFBusType();
		
		FastDecoupledPowerFlow pfnm1 = runContiPowerFlow();
		if (_results != null) _results.setCtgcyCaseInfo4SCED(i, pfnm1.isPfConv());
        recordContiVios(pfnm1, i, _testAllTransm, _PfNotCTransm);
        
       int numIsland = pfnm1.getNumofIslands();
        if (numIsland != 1) System.err.println("Warning: Brc Contingency "+i+" causes : "+numIsland+" islands.");
        _branches.setInSvc(i, true);
	}

	protected void runSingleGenConti(int i, float[] MH) throws PsseModelException, IOException
	{
		if (_showRunInfo == true) System.out.println("Gen Contingency: "+i+", _rank: "+_rank);
		_gens.setInSvc(i, false);
		_numTestedNm1Gen++;
		_model.getBusTypeManagerData().usePrePFBusType();
		
		GensPFactors genPFactors = new GensPFactors(_model,i);
		if (_optionPfactor == 1) genPFactors.createPfactorsCapa();
		else if (_optionPfactor == 2) genPFactors.createPfactorsRes();
		else if (_optionPfactor == 3) genPFactors.createPfactorsH(MH);
		else if (_optionPfactor != 0) System.err.println("option_Participation factor is not chosen properly..");
		genPFactors.launch();						
		
		FastDecoupledPowerFlow pfnm1 = runContiPowerFlow();
        recordContiVios(pfnm1, i, _testAllGen, _PfNotCGen);
        
		genPFactors.refresh();
        _gens.setInSvc(i, true);
	}
	
	FastDecoupledPowerFlow runContiPowerFlow() throws PsseModelException, IOException
	{
        FastDecoupledPowerFlow pfnm1 = new FastDecoupledPowerFlow(_model);
        pfnm1.setShowRunInfo(false);
		pfnm1.setMarkUsingVgSched(false);
		pfnm1.setVA(_vaBasePf.clone()); pfnm1.setVM(_vmBasePf.clone());
        pfnm1.runPowerFlow(_vstartCA, _MarkGenVarLimit, _GenVarLimitOption, _MaxIterPfVarLimit);
        return pfnm1;
	}

	public void recordContiVios(FastDecoupledPowerFlow pfnm1, int i, VioResultAll testAll, PfNotConverge PfNotC) throws PsseModelException
	{
        boolean converged = pfnm1.isPfConv();
        if (converged)
        {
            VioResult test = new VioResult(_model, _nbr, converged);
            analyzeVioResult(test, pfnm1, getRateC());
            if (test.getViol())
            {
            	boolean record = true;
            	float sumVmVio = test.getSumVmDiff();
            	float sumBrcVio = test.getSumBrcDiff();
            	if (_markTolVio == true)
            	{
            		if ( (sumVmVio < _tolSumVm) && (sumBrcVio < _tolSumMVA))  
            			record = false;
            	}
            	if (record == true)
            	{
            		testAll.updateVioData(i, test.sizeV(), test.sizeBrc(), sumBrcVio, sumVmVio);
                    if(test.getVioVoltage())
                    {
                    	testAll.updateVmVioData(i, test.getIdxV(), test.getVmDiff(), test.sizeV(), test.getMaxVolDiff(), test.getMaxVioBusInd(),
                        		test.getVm(),test.getVa());
                    }
                    if (test.getVioBrc())
                    {
                    	testAll.updateBrcVioInPerctData(test.getMaxBrcVioDiffPerct(), test.getMaxVioBrcPerctInd());
                    	testAll.updateBrcVioData(i, test.getIdxBrc(), test.getBrcDiff(), test.sizeBrc(), test.getMaxBrcVioDiff(), test.getMaxVioBrcInd(), 
                        		test.getPfrm(), test.getPto(), test.getQfrm(), test.getQto());
                    }
            	}
            }
            if (_MarkGenVarLimit)
            	testAll.updateInfoPVtoPQ(i, pfnm1.getNumOrigPVtoPQBuses(), pfnm1.getNumOrigPVBuses());
        }
        else {PfNotC.updateData(i);}
        
	}
	
	public float[] getRateA() throws PsseModelException {return _model.getACBrcCapData().getRateA();}
	public float[] getRateB() throws PsseModelException {return _model.getACBrcCapData().getRateB();}
	public float[] getRateC() throws PsseModelException {return _model.getACBrcCapData().getRateC();}
	
	public PsseModel getPsseModel()  {return _model;}
	public ACBranchList getBranches()  {return _branches;}
	public GenList getGenList()  {return _gens;}
	public float[] getVaBasePf() {return _vaBasePf;}
	public float[] getVmBasePf() {return _vmBasePf;}
	
	
	public int getNumContiVioVmAndBrcAllTransm() {return _nContVioVmAndBrcAllTransm;}
	public int[] getIdxContVioVmAndBrcAllTransm() {return _IdxContVioVmAndBrcAllTransm;}
	public int[] getIdxMapContToVmVioContTransm() {return _idxMapContToVmVioContTransm;}
	public int[] getIdxMapContToBrcVioContTransm() {return _idxMapContToBrcVioContTransm;}
	public float[] getSumVmVioExtPerContTransm() {return _sumVmVioExtPerContTransm;}
	public float[] getSumBrcVioExtPerContTransm() {return _sumBrcVioExtPerContTransm;}

	public int getNumContiVioVmAndBrcAllGen() {return _nContVioVmAndBrcAllGen;}
	public int[] getIdxContVioVmAndBrcAllGen() {return _IdxContVioVmAndBrcAllGen;}
	public int[] getIdxMapContToVmVioContGen() {return _idxMapContToVmVioContGen;}
	public int[] getIdxMapContToBrcVioContGen() {return _idxMapContToBrcVioContGen;}
	public float[] getSumVmVioExtPerContGen() {return _sumVmVioExtPerContGen;}
	public float[] getSumBrcVioExtPerContGen() {return _sumBrcVioExtPerContGen;}

	//
	public int[] getIdxContVioBrcAllTransm() {return _IdxContVioBrcAllTransm;}
	public float[][] getAllBrcDiffTransm() {return _AllBrcDiffTransm;}
	public int[] getIdxContVioVmAllTransm() {return _IdxContVioVmAllTransm;}
	public float[][] getVmVioDiffTransm() {return _AllVmDiffTransm;}
	
	public int[] getIdxContVioBrcAllGen() {return _IdxContVioBrcAllGen;}
	public float[][] getAllBrcDiffGen() {return _AllBrcDiffGen;}
	public int[] getIdxContVioVmAllTGen() {return _IdxContVioVmAllGen;}
	public float[][] getVmVioDiffGen() {return _AllVmDiffGen;}

	void analyzeVioInfoTransm()
	{
		_sumAllVmLowVioTransm = 0;
		_sumAllVmHighVioTransm = 0;
		_sumAllVmVioTransm = 0;
		_numVmLowVioTransm = new int[_nVioVmAllTransm];
		_numVmHighVioTransm = new int[_nVioVmAllTransm];
		_numVmVioTransm = new int[_nVioVmAllTransm];
		_maxVmdiffLowTransm = new float[_nVioVmAllTransm];
		_maxVmdiffHighTransm = new float[_nVioVmAllTransm];
		_maxVmdiffTransm = new float[_nVioVmAllTransm];	
		_nVioVmLowAllTransm = 0;
		_nVioVmHighAllTransm = 0;

		for (int i=0; i<_nVioVmAllTransm; i++)
		{
			_numVmLowVioTransm[i] = 0;
			_numVmHighVioTransm[i] = 0;
			_numVmVioTransm[i] = 0;
			_maxVmdiffLowTransm[i] = 0;
			_maxVmdiffHighTransm[i] = 0;
			_maxVmdiffTransm[i] = 0;
			for (int j=0; j<_sizeVAllTransm[i]; j++)
			{
				float tmp = _AllVmDiffTransm[i][j];
				if (tmp > 0)
				{
					_numVmHighVioTransm[i]++;
					_numVmVioTransm[i]++;
					_sumAllVmHighVioTransm += tmp;
					if(tmp > _maxVmdiffHighTransm[i])
					{
						_maxVmdiffHighTransm[i] = tmp;
						if(tmp > Math.abs(_maxVmdiffTransm[i]))
							_maxVmdiffTransm[i] = tmp;
					}
				}
				else if (tmp < 0)
				{
					_numVmLowVioTransm[i]++;
					_numVmVioTransm[i]++;
					_sumAllVmLowVioTransm += tmp;
					if(tmp < _maxVmdiffLowTransm[i])
					{
						_maxVmdiffLowTransm[i] = tmp;
						if(-tmp > Math.abs(_maxVmdiffTransm[i]))
							_maxVmdiffTransm[i] = tmp;
					}
			    }
				else System.err.println("Something wrong here when calculate the summation of Vm violations.");
			}
			if (_numVmLowVioTransm[i] > 0)  _nVioVmLowAllTransm++;
			if (_numVmHighVioTransm[i] > 0)  _nVioVmHighAllTransm++;			
		}
		_sumAllVmVioTransm = _sumAllVmHighVioTransm - _sumAllVmLowVioTransm;
		assertTrue(_nVioVmLowAllTransm <= _nVioVmAllTransm);
		assertTrue(_nVioVmHighAllTransm <= _nVioVmAllTransm);
		
		_maxVmdiffAllTransm = 0;
		_maxVmLowdiffAllTransm = 0;
		_maxVmHighdiffAllTransm = 0;
		_numVmVioAllTransm = 0;
		_numVmLowVioAllTransm = 0;
		_numVmHighVioAllTransm = 0;
		for(int i=0; i<_nVioVmAllTransm; i++)
		{
			float tmp = _maxVmdiffLowTransm[i];
			if (tmp < _maxVmLowdiffAllTransm)
				_maxVmLowdiffAllTransm = tmp;
			
			float tmp2 = _maxVmdiffHighTransm[i];
			if (tmp2 > _maxVmHighdiffAllTransm)
				_maxVmHighdiffAllTransm = tmp2;
			_numVmLowVioAllTransm += _numVmLowVioTransm[i];
			_numVmHighVioAllTransm += _numVmHighVioTransm[i];
		}
		if (_maxVmHighdiffAllTransm >= Math.abs(_maxVmLowdiffAllTransm))
			_maxVmdiffAllTransm = _maxVmHighdiffAllTransm;
		else
			_maxVmdiffAllTransm = _maxVmLowdiffAllTransm;
		_numVmVioAllTransm = _numVmLowVioAllTransm + _numVmHighVioAllTransm;
		assertTrue(_numVmVioAllTransm == _totalVmVioNumAllTransm);		

		_maxBrcSdiffAllTransm = 0;
		for (int i=0; i<_maxBrcSdiffPerContTransm.length; i++)
		{
			if (_maxBrcSdiffPerContTransm[i] > _maxBrcSdiffAllTransm)
			{
				_maxBrcSdiffAllTransm = _maxBrcSdiffPerContTransm[i];
				_idxMaxBrcSdiffAllTransm = _IdxMaxBrcSdiffPerContTransm[i];
			}
		}
		
		_maxBrcVioPerctAllTransm = 0;
		for (int i=0; i<_maxBrcVioPerctPerContTransm.length; i++)
		{
			if (_maxBrcVioPerctPerContTransm[i] > _maxBrcVioPerctAllTransm)
				_maxBrcVioPerctAllTransm = _maxBrcVioPerctPerContTransm[i];
		}
		
		assertTrue(_nVioBrcAllTransm == _maxBrcSdiffPerContTransm.length);
		_sumAllSdiffTransm = 0;
		_sumVioBrcPerContTransm = new float[_nVioBrcAllTransm];
		_sumVioBrcWorstContTransm = 0;
		for (int i=0; i<_nVioBrcAllTransm; i++)
		{
			_sumVioBrcPerContTransm[i] = 0;
			for (int j=0; j<_sizeBrcAllTransm[i]; j++)
			{
				_sumAllSdiffTransm += _AllBrcDiffTransm[i][j];
				_sumVioBrcPerContTransm[i] += _AllBrcDiffTransm[i][j];
			}
			if (_sumVioBrcPerContTransm[i] > _sumVioBrcWorstContTransm)
			{
				_sumVioBrcWorstContTransm = _sumVioBrcPerContTransm[i];
				_idxVioBrcWorstContTransm = _IdxContVioBrcAllTransm[i];
			}
		}


	}
	
	void analyzeVioInfoGen()
	{
		_sumAllVmLowVioGen = 0;
		_sumAllVmHighVioGen = 0;
		_sumAllVmVioGen = 0;
		_numVmLowVioGen = new int[_nVioVmAllGen];
		_numVmHighVioGen = new int[_nVioVmAllGen];
		_numVmVioGen = new int[_nVioVmAllGen];
		_maxVmdiffLowGen = new float[_nVioVmAllGen];
		_maxVmdiffHighGen = new float[_nVioVmAllGen];
		_maxVmdiffGen = new float[_nVioVmAllGen];
		_nVioVmLowAllGen = 0;
		_nVioVmHighAllGen = 0;
		for (int i=0; i<_nVioVmAllGen; i++)
		{
			_numVmLowVioGen[i] = 0;
			_numVmHighVioGen[i] = 0;
			_numVmVioGen[i] = 0;
			_maxVmdiffLowGen[i] = 0;
			_maxVmdiffHighGen[i] = 0;
			_maxVmdiffGen[i] = 0;
			for (int j=0; j<_sizeVAllGen[i]; j++)
			{
				float tmp = _AllVmDiffGen[i][j];
				if (tmp > 0)
				{
					_numVmHighVioGen[i]++;
					_numVmVioGen[i]++;
					_sumAllVmHighVioGen += tmp;
					if(tmp > _maxVmdiffHighGen[i])
					{
						_maxVmdiffHighGen[i] = tmp;
						if(tmp > Math.abs(_maxVmdiffGen[i]))
							_maxVmdiffGen[i] = tmp;
					}
				}
				else if (tmp < 0)
				{
					_numVmLowVioGen[i]++;
					_numVmVioGen[i]++;
					_sumAllVmLowVioGen += tmp;
					if (tmp < _maxVmdiffLowGen[i])
					{
						_maxVmdiffLowGen[i] = tmp;
						if(-tmp > Math.abs(_maxVmdiffGen[i]))
							_maxVmdiffGen[i] = tmp;
					}
			    }
				else System.err.println("Something wrong here when calculate the summation of Vm violations.");
			}
			if (_numVmLowVioGen[i] > 0)  _nVioVmLowAllGen++;
			if (_numVmHighVioGen[i] > 0)  _nVioVmHighAllGen++;			
		}
		_sumAllVmVioGen = _sumAllVmHighVioGen - _sumAllVmLowVioGen;	
		assertTrue(_nVioVmLowAllGen <= _nVioVmAllGen);
		assertTrue(_nVioVmHighAllGen <= _nVioVmAllGen);

		_maxVmdiffAllGen = 0;
		_maxVmLowdiffAllGen = 0;
		_maxVmHighdiffAllGen = 0;
		_numVmVioAllGen = 0;
		_numVmLowVioAllGen = 0;
		_numVmHighVioAllGen = 0;
		for(int i=0; i<_nVioVmAllGen; i++)
		{
			float tmp = _maxVmdiffLowGen[i];
			if (tmp < _maxVmLowdiffAllGen)
				_maxVmLowdiffAllGen = tmp;
			
			float tmp2 = _maxVmdiffHighGen[i];
			if (tmp2 > _maxVmHighdiffAllGen)
				_maxVmHighdiffAllGen = tmp2;
			_numVmLowVioAllGen += _numVmLowVioGen[i];
			_numVmHighVioAllGen += _numVmHighVioGen[i];
		}
		if (_maxVmHighdiffAllGen >= Math.abs(_maxVmLowdiffAllGen))
			_maxVmdiffAllGen = _maxVmHighdiffAllGen;
		else
			_maxVmdiffAllGen = _maxVmLowdiffAllGen;
		_numVmVioAllGen = _numVmLowVioAllGen + _numVmHighVioAllGen;
		assertTrue(_numVmVioAllGen == _totalVmVioNumAllGen);
		
		_maxBrcSdiffAllGen = 0;
		for (int i=0; i<_maxBrcSdiffPerContGen.length; i++)
		{
			if (_maxBrcSdiffPerContGen[i] > _maxBrcSdiffAllGen)
			{
				_maxBrcSdiffAllGen = _maxBrcSdiffPerContGen[i];
			    _idxMaxBrcSdiffAllGen = _IdxMaxBrcSdiffPerContGen[i];				
			}
		}
		
		_maxBrcVioPerctAllGen = 0;
		for (int i=0; i<_maxBrcVioPerctPerContGen.length; i++)
		{
			if (_maxBrcVioPerctPerContGen[i] > _maxBrcVioPerctAllGen)
				_maxBrcVioPerctAllGen = _maxBrcVioPerctPerContGen[i];
		}
		
		assertTrue(_nVioBrcAllGen == _maxBrcSdiffPerContGen.length);
		_sumAllSdiffGen = 0;
		_sumVioBrcPerContGen = new float[_nVioBrcAllGen];
		_sumVioBrcWorstContGen = 0;
		for (int i=0; i<_nVioBrcAllGen; i++)
		{
			_sumVioBrcPerContGen[i] = 0;
			for (int j=0; j<_sizeBrcAllGen[i]; j++)
			{
				_sumAllSdiffGen += _AllBrcDiffGen[i][j];
				_sumVioBrcPerContGen[i] += _AllBrcDiffGen[i][j];
			}
			if (_sumVioBrcPerContGen[i] > _sumVioBrcWorstContGen)
			{
				_sumVioBrcWorstContGen = _sumVioBrcPerContGen[i];
				_idxVioBrcWorstContGen = _IdxContVioBrcAllGen[i];
			}
		}
	}
	
	void outputSumInfoTransm(PrintStream outFile)
	{
  	    outFile.println(" ******************************************************************************");
  	    outFile.println(" All_values_are_based_on_per_unit_here............");
  	    outFile.println(" RateC_is_used_for_analyzing_Brc_Violation");
		outFile.println(" Number_of_branch_that_were_performed_Nm1:"+_numTestedNm1Transm);
		outFile.println(" Number_of_Contingencies_that_did_not_converge:" + _numNotConvTransm);
		outFile.println(" Total_Number_of_Voltage_Violations:" + _numVmVioAllTransm);
		outFile.println(" Total_Number_of_Low_Voltage_Violations:" + _numVmLowVioAllTransm);
		outFile.println(" Total_Number_of_High_Voltage_Violations:" + _numVmHighVioAllTransm);
		outFile.println(" Total_Number_of_Branch_Thermal_Violations:" + _totalBrcVioNumAllTransm);
		outFile.println(" Maximum_Branch_Thermal_Violations_for_all_contingencies:" + _maxBrcSdiffAllTransm);
		float num = 0;
		if (_maxBrcVioPerctAllTransm > 0) num = (_maxBrcVioPerctAllTransm - 1)*100;
		outFile.println(" Maximum_Branch_Thermal_Violations_for_all_contingencies(In_Percentage):" + num + "%");
		outFile.println(" Maximum_Voltage_Violation_for_all_contingencies:" + _maxVmdiffAllTransm);
		outFile.println(" Maximum_Low_Voltage_Violation_for_all_contingencies:" + _maxVmLowdiffAllTransm);
		outFile.println(" Maximum_High_Voltage_Violation_for_all_contingencies:" + _maxVmHighdiffAllTransm);
		outFile.println(" Number_of_Contingencies_that_Cause_Vm_Violation:"+_nVioVmAllTransm);
		outFile.println(" Number_of_Contingencies_that_Cause_Low_Vm_Violation:"+_nVioVmLowAllTransm);
		outFile.println(" Number_of_Contingencies_that_Cause_High_Vm_Violation:"+_nVioVmHighAllTransm);
		outFile.println(" Number_of_Contingencies_that_Cause_Brc_Violation:"+_nVioBrcAllTransm);
		outFile.println(" Number_of_Contingencies_that_have_PQ_switched_from_original_PV:" + _numContPVtoPQTransm);
		outFile.println(" Total_violation_of_branch_thermal_limit_is:"+_sumAllSdiffTransm);
		outFile.println(" Total_violation_of_Voltage_is:"+_sumAllVmVioTransm);
		outFile.println(" Total_violation_of_High-Voltage_is:"+_sumAllVmHighVioTransm);
		outFile.println(" Total_violation_of_Low-Voltage_is:"+_sumAllVmLowVioTransm);   			
  	    outFile.println(" ******************************************************************************");
		outFile.println();
	}

	void outputSumInfoGen(PrintStream outFile)
	{
  	    outFile.println(" ******************************************************************************");
  	    outFile.println(" All_values_are_based_on_per_unit_here............");
  	    outFile.println(" RateC_is_used_for_analyzing_Brc_Violation");
		outFile.println(" Number_of_generators_that_were_performed_Nm1:"+_numTestedNm1Gen);
		outFile.println(" Number_of_Contingencies_that_did_not_converge:" + _numNotConvGen);
		outFile.println(" Total_Number_of_Voltage_Violations:" + _numVmVioAllGen);
		outFile.println(" Total_Number_of_Low_Voltage_Violations:" + _numVmLowVioAllGen);
		outFile.println(" Total_Number_of_High_Voltage_Violations:" + _numVmHighVioAllGen);
		outFile.println(" Total_Number_of_Branch_Thermal_Violations:" + _totalBrcVioNumAllGen);
		outFile.println(" Maximum_Branch_Thermal_Violations_for_all_contingencies:" + _maxBrcSdiffAllGen);
		float num = 0;
		if (_maxBrcVioPerctAllGen > 0) num = (_maxBrcVioPerctAllGen - 1)*100;
		outFile.println(" Maximum_Branch_Thermal_Violations_for_all_contingencies(In_Percentage):" + num + "%");
		outFile.println(" Maximum_Voltage_Violation_for_all_contingencies:" + _maxVmdiffAllGen);
		outFile.println(" Maximum_Low_Voltage_Violation_for_all_contingencies:" + _maxVmLowdiffAllGen);
		outFile.println(" Maximum_High_Voltage_Violation_for_all_contingencies:" + _maxVmHighdiffAllGen);
		outFile.println(" Number_of_Contingencies_that_Cause_Vm_Violation:"+_nVioVmAllGen);
		outFile.println(" Number_of_Contingencies_that_Cause_Low_Vm_Violation:"+_nVioVmLowAllGen);
		outFile.println(" Number_of_Contingencies_that_Cause_High_Vm_Violation:"+_nVioVmHighAllGen);
		outFile.println(" Number_of_Contingencies_that_Cause_Brc_Violation:"+_nVioBrcAllGen);
		outFile.println(" Number_of_Contingencies_that_have_PQ_switched_from_original_PV:" + _numContPVtoPQGen);
		outFile.println(" Total_violation_of_branch_thermal_limit_is:"+_sumAllSdiffGen);
		outFile.println(" Total_violation_of_Voltage_is:"+_sumAllVmVioGen);
		outFile.println(" Total_violation_of_High-Voltage_is:"+_sumAllVmHighVioGen);
		outFile.println(" Total_violation_of_Low-Voltage_is:"+_sumAllVmLowVioGen);
  	    outFile.println(" ******************************************************************************");
		outFile.println();
	}

	/** Output violation information for transmission contingencies */
	public void outputVioInfoTransmCont() throws PsseModelException
	{
		String rootDr=getPath();	
		//output summary info
		try {
			String fileName = "summaryInfoTransm.txt";
			File summaryInfoTransm = new File(rootDr+fileName);
			deleteFile(summaryInfoTransm);
   		    OutputStream resultFile = new FileOutputStream(rootDr+fileName, true);
   		    PrintStream outFile = new PrintStream (resultFile, true);   
   		    outputSumInfoTransm(outFile);
			resultFile.close();
			outFile.close();
			ShowDataToFileSucceed(summaryInfoTransm);
		} catch (IOException e) {
			System.out.println();
			System.out.println("Fail to output summaryInfoTransm info to a file" + e);
			e.printStackTrace();
		}
		
		// output sumVioPerCont
		try
		{
			String fileName = "sumVioPerContTransm.txt";
  			File sumVioPerContTransm = new File(rootDr+fileName);
  			deleteFile(sumVioPerContTransm);
   		    OutputStream resultFile = new FileOutputStream(rootDr+fileName, true);
   		    PrintStream outFile = new PrintStream (resultFile, true);   		    
   			
   			outFile.println(" No., ContingencyIndex, fbusCont, tbusCont, sumBrcVio, sumVmVio");
   			int ndxVmVio = 0;
   			if (_IdxContVioVmAndBrcAllTransm != null) assertTrue(_IdxContVioVmAndBrcAllTransm.length == _sumBrcVioExtPerContTransm.length);
   			if (_IdxContVioVmAndBrcAllTransm != null) assertTrue(_IdxContVioVmAndBrcAllTransm.length == _sumVmVioExtPerContTransm.length);
   			int size = 0;
   			if (_IdxContVioVmAndBrcAllTransm != null) size = _IdxContVioVmAndBrcAllTransm.length;
   			for (int i=0; i<size; i++)
   			{
   				int idxCont = _IdxContVioVmAndBrcAllTransm[i];
				ACBranch br = _branches.get(idxCont);
				assertTrue(br.isInSvc() == true);
				int fbusCont = br.getFromBus().getI();
				int tbusCont = br.getToBus().getI();
				idxCont++;
				ndxVmVio++;
				outFile.print(" "+ndxVmVio);
				outFile.print(" "+idxCont);
				outFile.print(" "+fbusCont);
				outFile.print(" "+tbusCont);
				outFile.print(" "+_sumBrcVioExtPerContTransm[i]);
				outFile.print(" "+_sumVmVioExtPerContTransm[i]);
				outFile.println();				
   			}
   			resultFile.close();
   			outFile.close();
   			ShowDataToFileSucceed(sumVioPerContTransm);
   		} catch (IOException e) {
   			System.out.println();
   			System.out.println("Fail to output sumVioPerCont info to a file" + e);
   			e.printStackTrace();
   		}
				
		// output the voltage violation information.
		try
		{
			String fileName = "VioVmTransm.txt";
  			File VioVmTransm = new File(rootDr+fileName);
  			deleteFile(VioVmTransm);
   		    OutputStream resultFile = new FileOutputStream(rootDr+fileName, true);
   		    PrintStream outFile = new PrintStream (resultFile, true);   		    
   			
   			outFile.println(" No., BrcContingencyIndex, fbusCont, tbusCont, VmVioBusIndex, Vm, Vm_Violations");
   			int ndxVmVio = 0;
   			for (int i=0; i<_nVioVmAllTransm; i++)
   			{
   				int idxCont = _IdxContVioVmAllTransm[i];
				ACBranch br = _branches.get(idxCont);
				assertTrue(br.isInSvc() == true);
				int fbusCont = br.getFromBus().getI();
				int tbusCont = br.getToBus().getI();
				idxCont++;
   				for (int j=0; j<_sizeVAllTransm[i]; j++)
   				{
   					ndxVmVio++;
   					outFile.print(" "+ndxVmVio);
   					outFile.print(" "+idxCont);
   					outFile.print(" "+fbusCont);
   					outFile.print(" "+tbusCont);
   					outFile.print(" "+(_IdxVAllTransm[i][j]+1));
   					outFile.print(" "+_AllVmTransm[i][j]);
   					outFile.print(" "+_AllVmDiffTransm[i][j]);
   					outFile.println();
   				}
   			}
   			resultFile.close();
   			outFile.close();
   			ShowDataToFileSucceed(VioVmTransm);
   		} catch (IOException e) {
   			System.out.println();
   			System.out.println("Fail to output VioVmTransm info to a file" + e);
   			e.printStackTrace();
   		}

		// output the branch thermal violation information.
		try
		{
  			File VioBrcTransm = new File(rootDr+"VioBrcTransm.txt");
  			deleteFile(VioBrcTransm);
   		    OutputStream resultFile = new FileOutputStream(rootDr+"VioBrcTransm.txt", true);
   		    PrintStream outFile = new PrintStream (resultFile, true);  		    

   		    float[] rateA = getRateA();
   		    float[] rateB = getRateB();
   		    float[] rateC = getRateC();

   			outFile.println(" No., BrcContingencyIndex, fbusCont, tbusCont, Brc_Vio, fbusVio, tbusVio, Sfrm, Sto, Sdiff, RateA, RateB, RateC");
   			int ndxBrcVio = 0;
   			for (int i=0; i<_nVioBrcAllTransm; i++)
   			{
   				int idxCont = _IdxContVioBrcAllTransm[i];
				ACBranch br = _branches.get(idxCont);
				assertTrue(br.isInSvc() == true);
				int fbusCont = br.getFromBus().getI();
				int tbusCont = br.getToBus().getI();
   				idxCont++;
   				for (int j=0; j<_sizeBrcAllTransm[i]; j++)
   				{
   					ndxBrcVio++;
   					outFile.print(" "+ndxBrcVio);
   					outFile.print(" "+idxCont);
   					outFile.print(" "+fbusCont);
   					outFile.print(" "+tbusCont);
   					int idxtmp = _IdxBrcAllTransm[i][j];
   					outFile.print(" "+(idxtmp+1));
   					ACBranch br2 = _branches.get(idxtmp);
   					assertTrue(br2.isInSvc() == true);
   					int fbusVio = br2.getFromBus().getI();
   					int tbusVio = br2.getToBus().getI();
   					outFile.print(" "+fbusVio);
   					outFile.print(" "+tbusVio);
   					float sfrm = (float) Math.sqrt(Math.pow(_BrcVioPfrmAllTransm[i][j], 2) + Math.pow(_BrcVioQfrmAllTransm[i][j], 2));
   					float sto = (float) Math.sqrt(Math.pow(_BrcVioPtoAllTransm[i][j], 2) + Math.pow(_BrcVioQtoAllTransm[i][j], 2));
   					outFile.print(" "+sfrm*100);
   					outFile.print(" "+sto*100);
   					outFile.print(" "+_AllBrcDiffTransm[i][j]*100);
   					outFile.print(" "+rateA[idxtmp]);
   					outFile.print(" "+rateB[idxtmp]);
   					outFile.print(" "+rateC[idxtmp]);
   					outFile.println();
   				}
   			}
   			resultFile.close();
   			outFile.close();
   			ShowDataToFileSucceed(VioBrcTransm);
   		} catch (IOException e) {
   			System.out.println();
   			System.out.println("Fail to output VioBrcTransm info to a file" + e);
   			e.printStackTrace();
   		}
		
		// output the contingencies that can not converge information.
		try
		{
  			File NotConvTransm = new File(rootDr+"NotConvTransm.txt");
  			deleteFile(NotConvTransm);
   		    OutputStream resultFile = new FileOutputStream(rootDr+"NotConvTransm.txt", true);
   		    PrintStream outFile = new PrintStream (resultFile, true);

   			outFile.println(" No., BrcContingencyIndex, fbusCont, tbusCont");
   			int ndxBrcVio = 0;
   			for (int i=0; i<_numNotConvTransm; i++)
   			{
   				int idxCont = _IdxNotConvTransm[i];
				ACBranch br = _branches.get(idxCont);
				assertTrue(br.isInSvc() == true);
				int fbusCont = br.getFromBus().getI();
				int tbusCont = br.getToBus().getI();
				idxCont++;
				ndxBrcVio++;
				outFile.print(" "+ndxBrcVio);
				outFile.print(" "+idxCont);
				outFile.print(" "+fbusCont);
				outFile.print(" "+tbusCont);
				outFile.println();
   			}
   			resultFile.close();
   			outFile.close();
   			ShowDataToFileSucceed(NotConvTransm);
   		} catch (IOException e) {
   			System.out.println();
   			System.out.println("Fail to output NotConvTransm info to a file" + e);
   			e.printStackTrace();
   		}
		
		// output some general contingency information.
		try
		{
  			File VmVioPerCont = new File(rootDr+"PerCont_VmVio_Transm.txt");
  			deleteFile(VmVioPerCont);
   		    OutputStream resultFile = new FileOutputStream(rootDr+"PerCont_VmVio_Transm.txt", true);
   		    PrintStream outFile = new PrintStream (resultFile, true);
   			
   			outFile.println(" No., BrcContingencyIndex, fbusCont, tbusCont, #ofVolVio, #ofLowVolVio, #ofHighVolVio, MaxVolVio, MaxLowVolVio, MaxHighVolVio");
   			int ndxBrcVio = 0;
   			for (int i=0; i<_nVioVmAllTransm; i++)
   			{
   				int idxCont = _IdxContVioVmAllTransm[i];
				ACBranch br = _branches.get(idxCont);
				assertTrue(br.isInSvc() == true);
				int fbusCont = br.getFromBus().getI();
				int tbusCont = br.getToBus().getI();
				idxCont++;
				ndxBrcVio++;
				outFile.print(" "+ndxBrcVio);
				outFile.print(" "+idxCont);
				outFile.print(" "+fbusCont);
				outFile.print(" "+tbusCont);		
				outFile.print(" "+_numVmVioTransm[i]);
				outFile.print(" "+_numVmLowVioTransm[i]);
				outFile.print(" "+_numVmHighVioTransm[i]);
				outFile.print(" "+_maxVmdiffTransm[i]);
				outFile.print(" "+_maxVmdiffLowTransm[i]);
				outFile.print(" "+_maxVmdiffHighTransm[i]);			
				outFile.println();
   			}
   			resultFile.close();
   			outFile.close();
   			ShowDataToFileSucceed(VmVioPerCont);
   		} catch (IOException e) {
   			System.out.println();
   			System.out.println("Fail to output PerCont_VmVio_Transm info to a file" + e);
   			e.printStackTrace();
   		}
		try
		{
  			File BrcVio = new File(rootDr+"PerCont_BrcVio_Transm.txt");
  			deleteFile(BrcVio);
   		    OutputStream resultFile = new FileOutputStream(rootDr+"PerCont_BrcVio_Transm.txt", true);
   		    PrintStream outFile = new PrintStream (resultFile, true);
   			
   			outFile.println(" No., BrcContingencyIndex, fbusCont, tbusCont, #ofBrcVio, MaxBrcVio");
   			int ndxBrcVio = 0;
   			for (int i=0; i<_nVioBrcAllTransm; i++)
   			{
   				int idxCont = _IdxContVioBrcAllTransm[i];
				ACBranch br = _branches.get(idxCont);
				assertTrue(br.isInSvc() == true);
				int fbusCont = br.getFromBus().getI();
				int tbusCont = br.getToBus().getI();
				idxCont++;
				ndxBrcVio++;
				outFile.print(" "+ndxBrcVio);
				outFile.print(" "+idxCont);
				outFile.print(" "+fbusCont);
				outFile.print(" "+tbusCont);
				outFile.print(" "+_sizeBrcAllTransm[i]);
				outFile.print(" "+_maxBrcSdiffPerContTransm[i]);				
				outFile.println();				
   			}
   			resultFile.close();
   			outFile.close();
   			ShowDataToFileSucceed(BrcVio);
   		} catch (IOException e) {
   			System.out.println();
   			System.out.println("Fail to output PerCont_BrcVio_Transm info to a file" + e);
   			e.printStackTrace();
   		}
		try
		{
  			File PVtoPQ = new File(rootDr+"PerCont_PVtoPQ_Transm.txt");
  			deleteFile(PVtoPQ);
   		    OutputStream resultFile = new FileOutputStream(rootDr+"PerCont_PVtoPQ_Transm.txt", true);
   		    PrintStream outFile = new PrintStream (resultFile, true);   		    
   			
   			outFile.println(" No., BrcContingencyIndex, fbusCont, tbusCont, #ofPVtoPQ");
   			int ndxBrcVio = 0;
   			for (int i=0; i<_numContPVtoPQTransm; i++)
   			{
   				int idxCont = _idxContPVtoPQTransm[i];
				ACBranch br = _branches.get(idxCont);
				assertTrue(br.isInSvc() == true);
				int fbusCont = br.getFromBus().getI();
				int tbusCont = br.getToBus().getI();
				idxCont++;
				ndxBrcVio++;
				outFile.print(" "+ndxBrcVio);
				outFile.print(" "+idxCont);
				outFile.print(" "+fbusCont);
				outFile.print(" "+tbusCont);
				outFile.print(" "+_numPVtoPQEachContTransm[i]);
				outFile.println();
   			}
   			resultFile.close();
   			outFile.close();
   			ShowDataToFileSucceed(PVtoPQ);
   		} catch (IOException e) {
   			System.out.println();
   			System.out.println("Fail to output PVtoPQ info to a file" + e);
   			e.printStackTrace();
   		}
	}

	
	/** Output violation information for generator contingencies */
	public void outputVioInfoGenCont() throws PsseModelException
	{
		String rootDr=getPath();
		//output summary info
		try {
			String fileName = "summaryInfoGen.txt";
			File summaryInfoGen = new File(rootDr+fileName);
			deleteFile(summaryInfoGen);
   		    OutputStream resultFile = new FileOutputStream(rootDr+fileName, true);
   		    PrintStream outFile = new PrintStream (resultFile, true);   
   		    outputSumInfoGen(outFile);
			resultFile.close();
			outFile.close();
			ShowDataToFileSucceed(summaryInfoGen);
		} catch (IOException e) {
			System.out.println();
			System.out.println("Fail to output summaryInfoGen info to a file" + e);
			e.printStackTrace();
		}

		// output sumVioPerCont
		try
		{
			String fileName = "sumVioPerContGen.txt";
  			File sumVioPerContGen = new File(rootDr+fileName);
  			deleteFile(sumVioPerContGen);
  			OutputStream resultFile = new FileOutputStream(rootDr+fileName, true);
   		    PrintStream outFile = new PrintStream (resultFile, true);   		    
   			
   			outFile.println(" No., GenContingencyIndex, ContGenBusNumber, sumBrcVio, sumVmVio");
   			int ndxVmVio = 0;
   			if (_IdxContVioVmAndBrcAllGen != null)
   			{
   	   			assertTrue(_IdxContVioVmAndBrcAllGen.length == _sumBrcVioExtPerContGen.length);
   	   			assertTrue(_IdxContVioVmAndBrcAllGen.length == _sumVmVioExtPerContGen.length);
   	   			for (int i=0; i<_IdxContVioVmAndBrcAllGen.length; i++)
   	   			{
   	   				int idxCont = _IdxContVioVmAndBrcAllGen[i];
   	   				int genBusNumber = _gens.get(idxCont).getBus().getI();
   					assertTrue(_gens.get(idxCont).isInSvc() == true);
   					idxCont++;
   					ndxVmVio++;
   					outFile.print(" "+ndxVmVio);
   					outFile.print(" "+idxCont);
   					outFile.print(" "+genBusNumber);
   					outFile.print(" "+_sumBrcVioExtPerContGen[i]);
   					outFile.print(" "+_sumVmVioExtPerContGen[i]);
   					outFile.println();				
   	   			}
   			}
   			resultFile.close();
   			outFile.close();
   			ShowDataToFileSucceed(sumVioPerContGen);
   		} catch (IOException e) {
   			System.out.println();
   			System.out.println("Fail to output sumVioPerCont info to a file" + e);
   			e.printStackTrace();
   		}

		// output the voltage violation information.
		try
		{		    
  			File VioVmGen = new File(rootDr+"VioVmGen.txt");
  			deleteFile(VioVmGen);
   		    OutputStream resultFile = new FileOutputStream(rootDr+"VioVmGen.txt", true);
   		    PrintStream outFile = new PrintStream (resultFile, true);

   			outFile.println(" No., GenContingencyIndex, GenBusNumber, VmVioBusIndex, Vm, Vm_Violations");
   			int ndxVmVio = 0;
   			for (int i=0; i<_nVioVmAllGen; i++)
   			{
   				int idxCont = _IdxContVioVmAllGen[i];
   				int genBusNumber = _gens.get(idxCont).getBus().getI();
   				idxCont++;
   				for (int j=0; j<_sizeVAllGen[i]; j++)
   				{
   					ndxVmVio++;
   					outFile.print(" "+ndxVmVio);
   					outFile.print(" "+idxCont);
   					outFile.print(" "+genBusNumber);
   					outFile.print(" "+(_IdxVAllGen[i][j]+1));
   					outFile.print(" "+_AllVmGen[i][j]);
   					outFile.print(" "+_AllVmDiffGen[i][j]);
   					outFile.println();
   				}
   			}
   			resultFile.close();
   			outFile.close();
   			ShowDataToFileSucceed(VioVmGen);
   		} catch (IOException e) {
   			System.out.println();
   			System.out.println("Fail to output VioVmGen info to a file" + e);
   			e.printStackTrace();
   		}

		// output the branch thermal violation information.
		try
		{
  			File VioBrcGen = new File(rootDr+"VioBrcGen.txt");
  			deleteFile(VioBrcGen);
   		    OutputStream resultFile = new FileOutputStream(rootDr+"VioBrcGen.txt", true);
   		    PrintStream outFile = new PrintStream (resultFile, true);  		    

   	        float[] rateA = getRateA();
   	        float[] rateB = getRateB();
   	        float[] rateC = getRateC();

   			outFile.println(" No., GenContingencyIndex, GenBusNumber, idxBrc_Vio, fbusVio, tbusVio, Sfrm, Sto, Sdiff, RateA, RateB, RateC");
   			int ndxBrcVio = 0;
   			for (int i=0; i<_nVioBrcAllGen; i++)
   			{
   				int idxCont = _IdxContVioBrcAllGen[i];
   				int genBusNumber = _gens.get(idxCont).getBus().getI();
   				idxCont++;
   				for (int j=0; j<_sizeBrcAllGen[i]; j++)
   				{
   					ndxBrcVio++;
   					outFile.print(" "+ndxBrcVio);
   					outFile.print(" "+idxCont);
   					outFile.print(" "+genBusNumber);
   					int idxtmp = _IdxBrcAllGen[i][j];
   					outFile.print(" "+(idxtmp+1));
   					ACBranch br2 = _branches.get(idxtmp);
   					assertTrue(br2.isInSvc() == true);
   					int fbusVio = br2.getFromBus().getI();
   					int tbusVio = br2.getToBus().getI();
   					outFile.print(" "+fbusVio);
   					outFile.print(" "+tbusVio);
   					float sfrm = (float) Math.sqrt(Math.pow(_BrcVioPfrmAllGen[i][j], 2) + Math.pow(_BrcVioQfrmAllGen[i][j], 2));
   					float sto = (float) Math.sqrt(Math.pow(_BrcVioPtoAllGen[i][j], 2) + Math.pow(_BrcVioQtoAllGen[i][j], 2));
   					outFile.print(" "+sfrm*100);
   					outFile.print(" "+sto*100);
   					outFile.print(" "+_AllBrcDiffGen[i][j]*100);
   					outFile.print(" "+rateA[idxtmp]);
   					outFile.print(" "+rateB[idxtmp]);
   					outFile.print(" "+rateC[idxtmp]);
   					outFile.println();
   				}
   			}
   			resultFile.close();
   			outFile.close();
   			ShowDataToFileSucceed(VioBrcGen);
   		} catch (IOException e) {
   			System.out.println();
   			System.out.println("Fail to output VioBrcGen info to a file" + e);
   			e.printStackTrace();
   		}
		
		// output the contingencies that can not converge information.
		try
		{
  			File NotConvGen = new File(rootDr+"NotConvGen.txt");
  			deleteFile(NotConvGen);
   		    OutputStream resultFile = new FileOutputStream(rootDr+"NotConvGen.txt", true);
   		    PrintStream outFile = new PrintStream (resultFile, true);

   			outFile.println(" No., GenContingencyIndex, GenBusNumber");
   			int ndxBrcVio = 0;
   			for (int i=0; i<_numNotConvGen; i++)
   			{
   				int idxCont = _IdxNotConvGen[i];
   				int genBusNumber = _gens.get(idxCont).getBus().getI();
				ndxBrcVio++;
				outFile.print(" "+ndxBrcVio);
				outFile.print(" "+(idxCont+1));
				outFile.print(" "+genBusNumber);
				outFile.println();
   			}
   			resultFile.close();
   			outFile.close();
   			ShowDataToFileSucceed(NotConvGen);
   		} catch (IOException e) {
   			System.out.println();
   			System.out.println("Fail to output NotConvGen info to a file" + e);
   			e.printStackTrace();
   		}
		
		// output some general contingency information.
		try
		{
  			File VmVioPerCont = new File(rootDr+"PerCont_VmVio_Gen.txt");
  			deleteFile(VmVioPerCont);
   		    OutputStream resultFile = new FileOutputStream(rootDr+"PerCont_VmVio_Gen.txt", true);
   		    PrintStream outFile = new PrintStream (resultFile, true);
   			
   			outFile.println(" No., GenContingencyIndex, GenBusIndex, #ofVolVio, #ofLowVolVio, #ofHighVolVio, MaxVolVio, MaxLowVolVio, MaxHighVolVio");
   			int ndxBrcVio = 0;
   			for (int i=0; i<_nVioVmAllGen; i++)
   			{   				
   				int idxCont = _IdxContVioVmAllGen[i];
   				int genBusNumber = _gens.get(idxCont).getBus().getI();
				ndxBrcVio++;
				outFile.print(" "+ndxBrcVio);
				outFile.print(" "+(idxCont+1));
				outFile.print(" "+genBusNumber);
				outFile.print(" "+_numVmVioGen[i]);
				outFile.print(" "+_numVmLowVioGen[i]);
				outFile.print(" "+_numVmHighVioGen[i]);
				outFile.print(" "+_maxVmdiffGen[i]);
				outFile.print(" "+_maxVmdiffLowGen[i]);
				outFile.print(" "+_maxVmdiffHighGen[i]);			
				outFile.println();
   			}
   			resultFile.close();
   			outFile.close();
   			ShowDataToFileSucceed(VmVioPerCont);
   		} catch (IOException e) {
   			System.out.println();
   			System.out.println("Fail to output PerCont_VmVio_Gen info to a file" + e);
   			e.printStackTrace();
   		}
		try
		{
  			File BrcVio = new File(rootDr+"PerCont_BrcVio_Gen.txt");
  			deleteFile(BrcVio);
   		    OutputStream resultFile = new FileOutputStream(rootDr+"PerCont_BrcVio_Gen.txt", true);
   		    PrintStream outFile = new PrintStream (resultFile, true);    			
   			
   			outFile.println(" No., GenContingencyIndex, GenBusNumber, #ofBrcVio, MaxBrcVio");
   			int ndxBrcVio = 0;
   			for (int i=0; i<_nVioBrcAllGen; i++)
   			{
   				int idxCont = _IdxContVioBrcAllGen[i];
   				int genBusNumber = _gens.get(idxCont).getBus().getI();
				ndxBrcVio++;
				outFile.print(" "+ndxBrcVio);
				outFile.print(" "+(idxCont+1));
				outFile.print(" "+genBusNumber);
				outFile.print(" "+_sizeBrcAllGen[i]);
				outFile.print(" "+_maxBrcSdiffPerContGen[i]);				
				outFile.println();
   			}
   			resultFile.close();
   			outFile.close();
   			ShowDataToFileSucceed(BrcVio);
   		} catch (IOException e) {
   			System.out.println();
   			System.out.println("Fail to output PerCont_BrcVio_Gen info to a file" + e);
   			e.printStackTrace();
   		}
		try
		{
  			File PVtoPQ = new File(rootDr+"PerCont_PVtoPQ_Gen.txt");
  			deleteFile(PVtoPQ);
   		    OutputStream resultFile = new FileOutputStream(rootDr+"PerCont_PVtoPQ_Gen.txt", true);
   		    PrintStream outFile = new PrintStream (resultFile, true);
   			
   			outFile.println(" No., GenContingencyIndex, GenBusNumber, #ofPVtoPQ");
   			int ndxBrcVio = 0;
   			for (int i=0; i<_numContPVtoPQGen; i++)
   			{
   				int idxCont = _idxContPVtoPQGen[i];
   				int genBusNumber = _gens.get(idxCont).getBus().getI();
				ndxBrcVio++;
				outFile.print(" "+ndxBrcVio);
				outFile.print(" "+(idxCont+1));
				outFile.print(" "+genBusNumber);
				outFile.print(" "+_numPVtoPQEachContGen[i]);
				outFile.println();
   			}
   			resultFile.close();
   			outFile.close();
   			ShowDataToFileSucceed(PVtoPQ);
   		} catch (IOException e) {
   			System.out.println();
   			System.out.println("Fail to output PVtoPQ_GenCont info to a file" + e);
   			e.printStackTrace();
   		}
	}

	
	/** Print some results info to screen. */
	public void ShowDataToFileSucceed(File file)
	{
		if (_showRunInfo == true)
  			System.out.println("Output "+file.getName()+" info to a file successfully.");   				
	}
	
	/** Check file existed or not; if yes, then, delete it. */
	public void deleteFile(File file)
	{
		if (file.exists() && !file.isDirectory())
		{
			boolean deleted = file.delete();
			if (_showRunInfo == true)
			{
				System.out.println();
				if (deleted == true) System.out.println("Original "+file.getName() + " is deleted.");
			    else System.out.println("Delete operation is failed.");
			}
		}
	}

	void outputElemFrmArray(PrintStream outFile, int[] a)
	{
		for (int i=0; i<a.length; i++)
			outFile.print("  "+a[i]);
	}
	void outputElemFrmArray(PrintStream outFile, float[] a)
	{
		for (int i=0; i<a.length; i++)
			outFile.print("  "+a[i]);
	}

	public void outputVioInfoBaseCase() throws PsseModelException, IOException {}
	public void runGenContingencyAnalysis() throws PsseModelException, IOException {}
	public void runTransmContingencyAnalysis() throws PsseModelException, IOException {}
	
	public void outputGeneralInfoTmp(float[] times) throws PsseModelException {}

	
	public void outputBrcInfo(PrintStream outFile, int idxBrc) throws PsseModelException
	{
        boolean isSvr = _model.getBranches().isInSvc(idxBrc);
 		assertTrue(isSvr == true);

 		outFile.print(" "+(idxBrc+1));
		outFile.print(" "+_model.getBranches().getCKT(idxBrc));
		
		Bus frmBus = _model.getBranches().getFromBus(idxBrc);
		outputBusInfo(outFile, frmBus);
		
		Bus toBus = _model.getBranches().getToBus(idxBrc);
		outputBusInfo(outFile, toBus);
	}

	public void outputBusInfo(PrintStream outFile, Bus busElem) throws PsseModelException
	{
		outFile.print(" "+busElem.getI());
		String name = busElem.getNAME();
		name = AuxStringXL.replaceElem(name, ' ', '_');
		outFile.print(" "+name);
		outFile.print(" "+busElem.getBASKV());
	}
	
	public void outputDummyBusInfo(PrintStream outFile) throws PsseModelException
	{
		outFile.print(" -1 -1 -1");
	}
	
	public void writeBrcVioDetailInfo() throws PsseModelException
	{
		String rootDr=getGeneInfoPath();

		try {
   		    OutputStream resultFile = new FileOutputStream(rootDr+"BrcVioInfoDetail.txt", true);
   		    PrintStream outFile = new PrintStream (resultFile);
   		    
   		    String rawFileName = _model.getFileName();
   		    rawFileName = rawFileName.substring(0, rawFileName.length()-4);

   		    float[] rateA = getRateA();
   		    float[] rateB = getRateB();
   		    float[] rateC = getRateC();

   			for (int i=0; i<_nVioBrcAllTransm; i++)
   			{
   				int idxCont = _IdxContVioBrcAllTransm[i];
   				for (int j=0; j<_sizeBrcAllTransm[i]; j++)
   				{
   		   			outFile.print(" "+rawFileName + " Branch");
   					outputBrcInfo(outFile, idxCont);

   					int idxtmp = _IdxBrcAllTransm[i][j];
   					outputBrcInfo(outFile, idxtmp);
   					
   					outFile.print(" "+_AllBrcDiffTransm[i][j]*100);
   					outFile.print(" "+rateA[idxtmp]);
   					outFile.print(" "+rateB[idxtmp]);
   					outFile.print(" "+rateC[idxtmp]);
   					outFile.println();
   				}
   			}
   			
   			for (int i=0; i<_nVioBrcAllGen; i++)
   			{
   				int idxCont = _IdxContVioBrcAllGen[i];
   	 	        Bus genBus = _model.getGenerators().getBus(idxCont);
   	 	        String genID = _model.getGenerators().getID(idxCont);

   				for (int j=0; j<_sizeBrcAllGen[i]; j++)
   				{
   		   			outFile.print(" "+rawFileName + " Gen");
   		   			outFile.print(" "+(idxCont+1)+" "+genID);
   	   	 	        outputBusInfo(outFile, genBus);
   	   	 	        outputDummyBusInfo(outFile);

   					int idxtmp = _IdxBrcAllGen[i][j];
   					outputBrcInfo(outFile, idxtmp);
   					
   					outFile.print(" "+_AllBrcDiffGen[i][j]*100);
   					outFile.print(" "+rateA[idxtmp]);
   					outFile.print(" "+rateB[idxtmp]);
   					outFile.print(" "+rateC[idxtmp]);
   					outFile.println();
   				}
   			}

   			resultFile.close();
   			outFile.close();
   			if (_showRunInfo == true) {System.out.println(); System.out.println("Output VioDetails.txt to a file successfully.");}
   		} catch (IOException e) {
   			System.out.println("Fail to output FilesChecked Info to a file" + e);
   			e.printStackTrace();
   		}

		
		
	}
	
	public void writeVmVioDetailInfo() throws PsseModelException
	{
		String rootDr=getGeneInfoPath();

		try {
   		    OutputStream resultFile = new FileOutputStream(rootDr+"VmVioInfoDetail.txt", true);
   		    PrintStream outFile = new PrintStream (resultFile);
   		    
   		    String rawFileName = _model.getFileName();
   		    rawFileName = rawFileName.substring(0, rawFileName.length()-4);

   			for (int i=0; i<_nVioVmAllTransm; i++)
   			{
   				int idxCont = _IdxContVioVmAllTransm[i];
   				for (int j=0; j<_sizeVAllTransm[i]; j++)
   				{
   		   			outFile.print(" "+rawFileName + " Branch");
   					outputBrcInfo(outFile, idxCont);

   					int idxBus = _IdxVAllTransm[i][j];
   					Bus busElem = _model.getBuses().get(idxBus);
   					outputBusInfo(outFile, busElem);
   					outFile.print(" "+_AllVmDiffTransm[i][j]);
   					outFile.println();
   				}
   			}
   			
   			for (int i=0; i<_nVioVmAllGen; i++)
   			{
   				int idxCont = _IdxContVioVmAllGen[i];
   	 	        Bus genBus = _model.getGenerators().getBus(idxCont);
   	 	        String genID = _model.getGenerators().getID(idxCont);
   	 	        
   				for (int j=0; j<_sizeVAllGen[i]; j++)
   				{
   		   			outFile.print(" "+ rawFileName + " Gen");
   		   			outFile.print(" "+(idxCont+1)+" "+genID);
   	   	 	        outputBusInfo(outFile, genBus);
   	   	 	        outputDummyBusInfo(outFile);

   					int idxBus = _IdxVAllGen[i][j];
   					Bus busElem = _model.getBuses().get(idxBus);
   					outputBusInfo(outFile, busElem);
   					outFile.print(" "+_AllVmDiffGen[i][j]);
   					outFile.println();
   				}
   			}
   		    
   			resultFile.close();
   			outFile.close();
   			if (_showRunInfo == true) {System.out.println(); System.out.println("Output VioDetails.txt to a file successfully.");}
   		} catch (IOException e) {
   			System.out.println("Fail to output FilesChecked Info to a file" + e);
   			e.printStackTrace();
   		}
	}

	
	
	Results4ReDispatch _results;
	public void setResultsCollector4SCED(Results4ReDispatch results) {
		_results = results;
	}
	

}
