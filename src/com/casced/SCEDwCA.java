package com.casced;

import java.io.File;
import java.io.IOException;

import com.powerdata.openpa.psse.PsseModel;
import com.powerdata.openpa.psse.PsseModelException;
import com.powerdata.openpa.psse.powerflow.FastDecoupledPowerFlow;
import com.powerdata.openpa.psse.powerflow.VoltageSource;
import com.rtca_cts.contingencyanalysis.ContingencyAnalysis;
import com.rtca_cts.contingencyanalysis.ContingencyAnalysisSeq;
import com.rtca_cts.contingencyanalysis.SelectKeyContingencies;
import com.rtca_cts.param.ParamFDPFlow;
import com.rtca_cts.param.ParamIO;
import com.sced.gurobi.GRB_SCEDXL;
import com.sced.model.SCEDXL;
import com.sced.model.SystemModelXL;
import com.sced.model.param.ParamInput;
import com.sced.model.param.ReadParams;
import com.utilxl.iofiles.AuxFileXL;
import com.utilxl.log.DiaryXL;
import com.utilxl.log.LogTypeXL;

import gurobi.GRBException;

/**
 * 
 * Initialized in January 2017.
 * 
 * @author Xingpeng Li (xplipower@gmail.com)
 *
 */
public class SCEDwCA {

	DiaryXL _diary;
	PsseModel _modelPA;
	SystemModelXL _scedModel;

	FastDecoupledPowerFlow _pfBaseCase;
	float[] _vmBasePf;
	float[] _vaBasePf;
	
	Results4ReDispatch _origVioResults;
	DataFormatConverter _dfConvt;
	
	String _logSolver;
	SCEDXL _sced;
	GRB_SCEDXL _grbSolver;
	Results4ReDispatch _resultsPostSCED;
	
	/* Setting for the loop between SCED and CA/PF */
	double _tolInvidiVioPct = 0.01;  // All individual flow violation need to be less than _tolInvidiVioPct (e.g., 1%) of its limit (MVA).
	double _tolTotalVio = 5;   // The total flow violation (in MVA) need to be less than _tolTotalVio (e.g., 2 MVA or 5 MVA).
	int _maxIter = 1;     // limit of iterations
	int _countIter;    // # of iteration applied before reaching the stop criterion
	boolean _convged;      // true if converged
	ConstraintUpdateAlgorithm _ctgcyUpdateMethodIter = ConstraintUpdateAlgorithm.Replace; // it matters only when _maxIter >=2 and the 1st iter does not converge.
	
	public SCEDwCA(DiaryXL diary, PsseModel modelPA)
	{
		_diary = diary;
		_modelPA = modelPA;
		init();
	}
	private void init()	{_scedModel = new SystemModelXL(_diary);}
	public void setMaxIter(int maxIter) {_maxIter = maxIter;}
	

	public FastDecoupledPowerFlow runPf(VoltageSource vstart) throws PsseModelException, IOException
	{
		return runPf(vstart, null, null);
	}

	public FastDecoupledPowerFlow runPf(VoltageSource vstart,float[] vm, float[] va) throws PsseModelException, IOException
	{
		FastDecoupledPowerFlow pf = new FastDecoupledPowerFlow(_modelPA);
    	if (pf.getNumofIslands() != 1) System.err.println("The network is not fully connected. It has "+pf.getNumofIslands()+" independent islands...");
		pf.setShowRunInfo(false);
		if (vm != null) {
			pf.setMarkUsingVgSched(false);
			vstart = VoltageSource.LastSolved;
			pf.setVM(vm); pf.setVA(va);
		}
		
    	boolean MarkGenVarLimit = ParamFDPFlow.getGenVarLimit();
		pf.runPowerFlow(vstart, MarkGenVarLimit, 1, 10);
		
		if (pf.isPfConv()) System.out.println("\nThe power flow for base case converged.");
		else System.out.println("\nThe power flow for base case did not converge.");
		
		_diary.hotLine(LogTypeXL.MileStone, "Base-case Power flow simulation is done");
		return pf;
	}
	
	public Results4ReDispatch runCA(FastDecoupledPowerFlow pfBaseCase, boolean isInitCA) throws PsseModelException, IOException
	{
		AuxFileXL.createFolder(ParamIO.getCAPath());
		if (isInitCA == true) ParamIO.setCAFolder(ParamIO.getCAFolder()+_modelPA.getFileName()+"/");
		else ParamIO.setCAFolder(ParamIO.getCAFolder()+"/PostSCED/");
		AuxFileXL.createFolder(ParamIO.getCAPath());
		_diary.hotLine(LogTypeXL.MileStone, "RTCA output folder: "+ParamIO.getCAPath());
		
		if (pfBaseCase.isPfConv() == false) {
			_diary.hotLine(LogTypeXL.Error, "Base case power flow does not converge, CA is thus skipped");
			return null;
		}
		
	    int nBrcCtgcy = _modelPA.getContingencyListBrcData().size();
	    Results4ReDispatch resultsPostSCED = new Results4ReDispatch(_diary, _modelPA, nBrcCtgcy);
	    resultsPostSCED.retrieveBaseCaseInfo4SCED(pfBaseCase.isPfConv());

		ContingencyAnalysis ca = new ContingencyAnalysisSeq(_modelPA);
		ca.setShowRunInfo(false);
		ca.setGeneInfoPath(ParamIO.getOutPath() + ParamIO.getCAFolder());
		ca.setURI(_modelPA.getURI());
		ca.setOrigPfResults(pfBaseCase);
		ca.setResultsCollector4SCED(resultsPostSCED);
		
	    ca.runTransmContingencyAnalysis();
		ca.outputVioInfoTransmCont();
		SelectKeyContingencies selectKeyConti = new SelectKeyContingencies(ca);
		selectKeyConti.launch();
		selectKeyConti.dumpKeyBrcContiList();
	    //ca.runGenContingencyAnalysis();
		//ca.outputVioInfoGenCont();
		//selectKeyConti.dumpKeyGenContiList();

	    resultsPostSCED.cleanup();
		_diary.hotLine(LogTypeXL.MileStone, "RTCA simulation is done");
		return resultsPostSCED;
	}
	
	/** Load additional generator data */
	public GenEcoData loadExtraGenData() throws PsseModelException, IOException
	{
		int ngen = _modelPA.getGenerators().size();
		GenEcoData genEcoData = new GenEcoData(_diary, ngen);
		genEcoData.readGensCost();
		genEcoData.readGensRamp();
		return genEcoData;
	}
	
	/** Convert data format for SCED application */ 
	public void convtDataFormat(GenEcoData genEcoData) throws PsseModelException, IOException
	{
		convtSysData();
		convtGenEcoData();
		convtNetworkConstraintsData();
		
		_scedModel.checkData();
		_diary.hotLine(LogTypeXL.MileStone, "Data for SCED is completely loaded");
	}
	
	public void convtSysData() throws PsseModelException
	{
		_dfConvt = new DataFormatConverter(_modelPA);
		_dfConvt.fillData(_scedModel);
		
		boolean[] isBrcRadial = _modelPA.getRadialBrcData().getIsBrcRadial();
		_scedModel.getBranches().setIsBrcRadial(isBrcRadial);
	}
	
	public void convtGenEcoData() throws PsseModelException, IOException
	{
		GenEcoData genEcoData = loadExtraGenData();
		_dfConvt.fillGenEcoData(_scedModel, genEcoData);
	}
	
	public void convtNetworkConstraintsData() throws PsseModelException
	{
		_dfConvt.fillCtgcyData(_scedModel, _origVioResults);
	}

	/** Set SCED parameters */ 
	public void setScedModelParam() throws PsseModelException
	{
		_scedModel.dump();
		_logSolver = ParamInput.getGurobiLogFileName();
		File gLog = new File(_logSolver);
		if (gLog.exists()) {
			boolean flag = gLog.delete();
			if (flag == true) _diary.hotLine("Old gurobi log file "+gLog+" has been deleted");
			else _diary.hotLine("Old gurobi log file "+gLog+" is NOT deleted");
		}
	}
	
	public void runSCED()
	{
		_sced = new SCEDXL(_diary, _scedModel);
		try {
			GRB_SCEDXL grbSolver = new GRB_SCEDXL(_scedModel, _logSolver, _diary);
			grbSolver.setPgLBSV(false);
			grbSolver.setPreventiveCtrl(true);
			
			grbSolver.declareVar();
			grbSolver.varUpdate();
			grbSolver.defineConstraints();
			grbSolver.addObj();
			grbSolver.solve();
			
			_sced.saveResults(grbSolver);
			if (_ctgcyUpdateMethodIter == ConstraintUpdateAlgorithm.Add) _grbSolver = grbSolver;
		} catch (GRBException e) {
			String errInfo = "Error occured when calling Gurobi solver...";
			System.err.println(errInfo);
			_diary.hotLine(LogTypeXL.Error, errInfo);
			e.printStackTrace();
		}
	}
	
	/** Reset Pg in OpenPA model */
	public void resetPg2EMS() throws PsseModelException {_dfConvt.resetPg(_sced.getPg());}
	
	/** Dump SCED results */
	public void dumpSCEDResults() throws PsseModelException
	{
		_sced.dumpSCEDResults(_modelPA);
	}
	
	public void runInitPfCA(VoltageSource vstart) throws PsseModelException, IOException
	{
		/** Solve the current base case power flow */
		_pfBaseCase = runPf(vstart);
		_vmBasePf = _pfBaseCase.getVM();
		_vaBasePf = _pfBaseCase.getVA();
		_diary.hotLine(LogTypeXL.MileStone, "Original Base-case power flow simulation is done");
		
		/** Solve contingency analysis */
		_origVioResults = runCA(_pfBaseCase, true);
		logVioStatInfo(_origVioResults, false);
		_origVioResults.dump(false);  // isNm1Check = false;
		_resultsPostSCED = null;
	}
	
	public void prepare4SCED() throws PsseModelException, IOException
	{
		GenEcoData genEcoData = loadExtraGenData();
		convtDataFormat(genEcoData);
		setScedModelParam();
	}
	
	public enum ConstraintUpdateAlgorithm {
		Unknown, Replace, Add;
		public static ConstraintUpdateAlgorithm fromConfig(String cfg)
		{
			switch(cfg.toLowerCase())
			{
				case "replace": return Replace;			
				case "add": return Add;			
				default: return Unknown;
			}
		}
	}
	
	public void replaceNetworkConstraints() throws PsseModelException {
		_dfConvt.updateCtgcyData(_scedModel, _resultsPostSCED);
	}
	
	/** Run SCED and CA until the solution converges or maximum number of iterations is reached  */
	public void runSCEDwCA() throws PsseModelException, IOException
	{
		_diary.hotLine(LogTypeXL.CheckPoint, "The maximum iteration for SCED is "+_maxIter);
		
		_countIter = 0;
		while (_countIter < _maxIter)
		{
			if (_countIter != 0) {
				if (_ctgcyUpdateMethodIter == ConstraintUpdateAlgorithm.Replace) replaceNetworkConstraints();
				else if (_ctgcyUpdateMethodIter == ConstraintUpdateAlgorithm.Add) {
					UpdateSCEDConstraintsGRB updateConstEngine = new UpdateSCEDConstraintsGRB(_grbSolver);
					updateConstEngine.launchUpdate(_origVioResults, _resultsPostSCED);
				}
			}
			_countIter++;
			String message = "\nSCED iteration "+_countIter+" starts ...";
			_diary.hotLine(LogTypeXL.MileStone, message); System.out.println(message);
			
			runSCED();
			if (_countIter == _maxIter) dumpSCEDResults();
			resetPg2EMS();
			FastDecoupledPowerFlow pf = runPf(VoltageSource.LastSolved, _vmBasePf, _vaBasePf);
			_resultsPostSCED = runCA(pf, false);
			logVioStatInfo(_resultsPostSCED, true);
			if (isCnvged(_resultsPostSCED) == true) {_convged = true; break;}
		}
		
		if (_convged == false) {
			String message = "The loop for SCED with CA failed to converge after "+_countIter+" iterations";
			_diary.hotLine(LogTypeXL.CheckPoint, message); System.out.println(message);
		} else {
			String message = "The loop for SCED with CA converged in "+_countIter+" iterations";
			_diary.hotLine(LogTypeXL.CheckPoint, message); System.out.println(message);
		}
		_resultsPostSCED.dump(true);
	}
	
	/** check if the violations for current solution within tolerance */
	public boolean isCnvged(Results4ReDispatch results)
	{
		if (results.getMaxIndivdlPctVio4BaseCase() > _tolInvidiVioPct) return false;
		if (results.getSumAmtVio4BaseCase() > _tolTotalVio) return false;
		if (results.getMaxIndivdlPctVio4CtgcyCase() > _tolInvidiVioPct) return false;
		if (results.getSumAmtVio4CtgcyCase() > _tolTotalVio) return false;
		return true;
	}
	
	private void logVioStatInfo(Results4ReDispatch results, boolean isResultAfterSCED)
	{
		String mess = "Statistics before SCED";
		if (isResultAfterSCED == true) mess = "Statistics after SCED";
		_diary.hotLine(LogTypeXL.CheckPoint, mess+": maximum individual violation in PERCENT for base case is "+results.getMaxIndivdlPctVio4BaseCase());
		_diary.hotLine(LogTypeXL.CheckPoint, mess+": total violation in MVA for base case is "+results.getSumAmtVio4BaseCase());
		_diary.hotLine(LogTypeXL.CheckPoint, mess+": maximum individual violation in percent for contingency case is "+results.getMaxIndivdlPctVio4CtgcyCase());
		_diary.hotLine(LogTypeXL.CheckPoint, mess+": total violation in MVA for contingency case is "+results.getSumAmtVio4CtgcyCase());
	}
	
	public PsseModel getPsseModel() {return _modelPA;}
	public SystemModelXL getSysModel() {return _scedModel;}
	public DataFormatConverter getDataFormatConverter() {return _dfConvt;}
	public SCEDXL getSCEDResults() {return _sced;}

	public static void main(String[] args) throws Exception
	{
		/** Log file creation */
		DiaryXL diary = new DiaryXL();
		diary.initial();
		
		/** Load parameters in configure file */
		ReadParams loadParam = new ReadParams();
		loadParam.launch();
		diary.hotLine("Configure file '"+loadParam.getConfigureFileName()+"' has been loaded");
		
		/** Parse program arguments */
		String uri = null;
		String svstart = "realtime";
		float minxmag = 0.0001f;
		for(int i=0; i < args.length;)
		{
			String s = args[i++].toLowerCase();
			int ssx = 1;
			if (s.startsWith("--")) ++ssx;
			switch(s.substring(ssx))
			{
				case "uri":
					uri = args[i++];
					break;
				case "voltage":
					svstart = args[i++];
					break;
				case "minxmag":
					minxmag = Float.parseFloat(args[i++]);
					break;
			}
		}
		
		VoltageSource vstart = VoltageSource.fromConfig(svstart);
		if (uri == null)
		{
			System.err.format("Usage: -uri model_uri [-voltage flat|realtime] [ -minxmag smallest reactance magnitude (default to 0.001) ] [ -debug dir ] [ -results path_to_results]");
			System.exit(1);
		}

 	    PsseModel model = PsseModel.Open(uri);
 	    model.setMinXMag(minxmag);
		model.setDiary(diary);
		diary.hotLine(LogTypeXL.MileStone, "OpenPA model raw data is loaded");
		
		//TODO temporary code
//		ACBranchList branches = model.getBranches();
//		for (int i=0; i<branches.size(); i++) {
//			float tmp = (float) (branches.getRateA(i)*1.15);
//			branches.setRateC(i, tmp);
//		}
		

		/** SCED with CA simulation */
		SCEDwCA scedCA = new SCEDwCA(diary, model);
		scedCA.runInitPfCA(vstart);
		scedCA.prepare4SCED();
		scedCA.runSCEDwCA();
		//scedCA._scedModel.getSenstvtFactor().getFormPTDF().dump();

		/** Program ends here */
		diary.done();
		System.out.println("Program ends here.");
	}
	
}
