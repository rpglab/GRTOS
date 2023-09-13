package com.rtca_cts.transmissionswitching;


import java.io.FileNotFoundException;
import java.io.IOException;

import com.powerdata.openpa.psse.PsseModel;
import com.powerdata.openpa.psse.PsseModelException;
import com.powerdata.openpa.psse.powerflow.FastDecoupledPowerFlow;
import com.powerdata.openpa.psse.powerflow.VoltageSource;
import com.rtca_cts.ausData.RadialBranches;
import com.rtca_cts.ausXP.InitPsseModel;
import com.rtca_cts.ausXP.EDM.FixedCTSlength;
import com.rtca_cts.ausXP.EDM.PreDataProcessing;
import com.rtca_cts.contingencyanalysis.ContingencyAnalysisSeq;
import com.rtca_cts.contingencyanalysis.SelectKeyContingencies;
import com.rtca_cts.param.ParamFDPFlow;
import com.rtca_cts.param.ParamIO;
import com.rtca_cts.param.ParamTS;
import com.utilxl.array.AuxArrayXL;
import com.utilxl.iofiles.AuxFileXL;
import com.utilxl.string.AuxStringXL;
import com.utilxl.tools.para.MPJExpressXL;

/**
 * Transmission switching;
 * 
 * Initialized in Sep.12th, 2014.
 * Last updated in Mar.20th, 2017.
 * 
 * @author Xingpeng.Li, xplipower@gmail.com 
 */
public class CorrectiveTransmSwitNaive {
	
	int _rank = 0;      // thread ranking
	int _nproc = 1;     // number of threads.
	long _t_Start;
	String _absPath;
	String _uri;
	PsseModel _model;
	
	FastDecoupledPowerFlow _pfBaseCase;
	float[] _vmBasePf;
	float[] _vaBasePf;
	
	boolean _runCA = true;
	String _brcContListFileName = "_brcContList.txt";  // (caseFileName + contListFileName) is the true name. It matters only when _runCA == false;
	String _genContListFileName = "_genContList.txt";  // matters only when _runCA == false;
	int[] _idxBrcContToBeChecked;
	int[] _idxGenContToBeChecked;
	int _numContiCheckedTS;
	
	String _TSList4DMFileName = "TSList.txt";

	public CorrectiveTransmSwitNaive(PsseModel model) throws FileNotFoundException, PsseModelException
	{
		_model = model;
		init();
	}
	
	/** Construction method if parallel computing is used */
	public CorrectiveTransmSwitNaive(PsseModel model, int rank, int nproc) throws FileNotFoundException, PsseModelException
	{
		_rank = rank;
		_nproc = nproc;
		_model = model;
		init();
	}
	
	private void init() throws FileNotFoundException, PsseModelException
	{
		_uri = _model.getURI();
		InitPsseModel initModel = new InitPsseModel(_model);
		initModel.initSetting();
		if (_rank == 0) {
			initModel.initNBestTS(_uri);
			_model.getNBestTSReport_NoTitle().setFileNameTransm("NoTitle_BestTS_Transm");
			_model.getNBestTSReport_NoTitle().setFileNameGen("NoTitle_BestTS_Gen");
			_model.getNBestTSReport_NoTitle().initPrint();
		}
		
		_absPath = AuxStringXL.relPathToAbsPath("dataToRead");
		if (ParamTS.getTSOption() == 4) {
			ParamTS.setCheckBrcVmLevel(false);
			_model.getTransmSwitListData().setCheckBrcVmLevel(false);
//			ParamTS.setCheckArea(false);
//			model.getTransmSwitListData().setCheckArea(false);
		}
	}
	
	public void setTstart(long t_Start) {_t_Start = t_Start;}
	public void setAbsPath(String absPath) {_absPath = absPath;}
	public void setRunCA(boolean runCA) {_runCA = runCA;}
	public void setBrcContListFileName(String brcContListFileName) {_brcContListFileName = brcContListFileName;}
	public void setGenContListFileName(String genContListFileName) {_genContListFileName = genContListFileName;}
	public void setTSList4DMFileName(String TSList4DMFileName) {_TSList4DMFileName = TSList4DMFileName;}
	public int getNumContiCheckedTS() {return _numContiCheckedTS;}
	
	public void runPF(VoltageSource vstart) throws PsseModelException, IOException
	{
		// Exclude solution time starting from here.
		long tExclude = System.nanoTime();
		FastDecoupledPowerFlow pf = new FastDecoupledPowerFlow(_model);
		pf.setShowRunInfo(false);
//		pf.setMarkUsingVgSched(false);
//		pf.setRealTimeStartForGenBus(true);
		boolean MarkGenVarLimit = ParamFDPFlow.getGenVarLimit();
		pf.runPowerFlow(vstart, MarkGenVarLimit, 1, 10);
		if (pf.isPfConv()) {if (_rank == 0) System.out.println("\nThe power flow for base case converged.");}
		else if (_rank == 0) {System.out.println("\nThe power flow for base case did not converge.");}
		
		_vmBasePf = pf.getVM();
		_vaBasePf = pf.getVA();
		int[] idxBrcVioBasePf = null;
		if (pf.getVioRateC().getViol() == true) {
			idxBrcVioBasePf = pf.getVioRateC().getIdxBrc();
			if (_rank == 0) {
				int[] idxV = pf.getVioRateC().getIdxV();
				if (idxV != null) {
					int sizeVoltVio = idxV.length;
					if (sizeVoltVio != 0) System.err.println("Warning: there are "+sizeVoltVio+" bus voltage violation in the base case.");				
				}
			}
		}
		
		int[] idxBrcVio = idxBrcVioBasePf;
		if (idxBrcVio != null)
		{
			for(int idx=0; idx<idxBrcVio.length; idx++)
			{
				int idxBrc = idxBrcVio[idx];
				if (_rank == 0) {
					String infoOutput = "    Increase the branch "+ (idxBrc+1) + " rating to infinity due to flow vio on this branch in base case.";
					System.out.println(infoOutput);
					AuxFileXL.initFileWithTitle(System.getProperty("user.dir")+"/filesOfOutput/"+"BrcRatesIncrease.txt", new String[] {_model.getFileName(), infoOutput}, true);
				}
				_model.getBranches().setRateA(idxBrc, 99999f);
				_model.getBranches().setRateB(idxBrc, 99999f);
				_model.getBranches().setRateC(idxBrc, 99999f);
			}
			_model.clearACBrcCap();
		}
		pf.clearVioRateC();
		_pfBaseCase = pf;
		//if (_rank == 0) _pfBaseCase.outputResults();
		
		if (_rank == 0) System.out.println("\nSolution time excluded : " + (System.nanoTime() - tExclude)/1e9f);	
		if (_rank == 0) System.out.println("    Solution time before excluded: " + (System.nanoTime() - _t_Start)/1e9f);		
		_t_Start += (System.nanoTime() - tExclude);
		if (_rank == 0) System.out.println("    Solution time after excluded : " + (System.nanoTime() - _t_Start)/1e9f);		
		// Exclude solution time ending to here.
	}
	
	public void runCA() throws PsseModelException, IOException
	{
		if (_runCA == true)
		{
			long t_Start_dummy = System.nanoTime();	
			AuxFileXL.createFolder(ParamIO.getCAPath());
			ParamIO.setCAFolder(ParamIO.getCAFolder()+_model.getFileName()+"/");
			AuxFileXL.createFolder(ParamIO.getCAPath());
			ContingencyAnalysisSeq ca = new ContingencyAnalysisSeq(_model, VoltageSource.Flat);
			ca.setGeneInfoPath(ParamIO.getOutPath() + ParamIO.getCAFolder());
			ca.setOrigPfResults(_pfBaseCase);
			//ca.setMarkUsingVgSched(true); /*ca.setRealTimeStartForGenBus(true);*/
			ca.setShowRunInfo(false);
			ca.setURI(_uri);
			
			if (_rank == 0) _model.getContingencyListGenData().dumpContiList();
			if (_rank == 0) _model.getContingencyListBrcData().dumpContiList();

			if (_rank == 0) System.out.println("\nRunning contingency analysis...");
			long timeCA = System.nanoTime();
			
			if (_rank == 0) System.out.println("    Running Generator contingency analysis...");
		    ca.runGenContingencyAnalysis();
		    if (_rank == 0) System.out.println("    Generator contingency analysis is finished.");
		    if (_rank == 0) System.out.println("      Gen CA time : " + (System.nanoTime() - timeCA)/1e9f);
			
		    if (_rank == 0) System.out.println("    Running Transmission contingency analysis...");
		    ca.runTransmContingencyAnalysis();
		    if (_rank == 0) System.out.println("    Transmission contingency analysis is finished.");
		    if (_rank == 0) System.out.println("      (Gen + Transm) CA time : " + (System.nanoTime() - timeCA)/1e9f);

			_idxBrcContToBeChecked = ca.getIdxContVioVmAndBrcAllTransm();
			_idxGenContToBeChecked = ca.getIdxContVioVmAndBrcAllGen();

		    if (_rank == 0) {
				long t_EndCA = System.nanoTime();
				ca.outputVioInfoGenCont();
				ca.outputVioInfoTransmCont();
				
				long t_EndOutputCA = System.nanoTime();
				float[] times = new float[2];
				times[0] = (t_EndCA - t_Start_dummy)/1e9f;
				times[1] = (t_EndOutputCA - t_Start_dummy)/1e9f;
				ca.outputGeneralInfoTmp(times);
				
				SelectKeyContingencies selectKeyConti = new SelectKeyContingencies(ca);
				selectKeyConti.launch();
				_idxBrcContToBeChecked = selectKeyConti.getKeyBrcConti();
				_idxGenContToBeChecked = selectKeyConti.getKeyGenConti();
				
				selectKeyConti.dumpKeyBrcContiList();
				selectKeyConti.dumpKeyGenContiList();
		    }
		    
		    if (_nproc > 1) {
				MPJExpressXL tmpMPJ = new MPJExpressXL(_nproc, _rank);
				_idxBrcContToBeChecked = tmpMPJ.spreadArray(_idxBrcContToBeChecked);
				_idxGenContToBeChecked = tmpMPJ.spreadArray(_idxGenContToBeChecked);
		    }
		} else {
			_idxBrcContToBeChecked = AuxFileXL.readOneCollumIntData(_absPath+ _model.getFileName()+ _brcContListFileName, true);
			AuxArrayXL.allElemsPlusANum(_idxBrcContToBeChecked, -1);
			_idxGenContToBeChecked = AuxFileXL.readOneCollumIntData(_absPath+ _model.getFileName()+ _genContListFileName, true);
			AuxArrayXL.allElemsPlusANum(_idxGenContToBeChecked, -1);
		}

		int numBrcConti = 0, numGenConti = 0;
		if (_idxBrcContToBeChecked != null) numBrcConti = _idxBrcContToBeChecked.length;
		if (_idxGenContToBeChecked != null) numGenConti = _idxGenContToBeChecked.length;
		if (_rank == 0) System.out.println("\nNumber of Transmission Contingency to be checked with TS is: "+numBrcConti);
		if (_rank == 0) System.out.println("Number of Generator    Contingency to be checked with TS is: "+numGenConti+"\n");
	}
	
	public void runCTS() throws PsseModelException, IOException
	{
		CorrectiveTransmSwit engineCTS = new CorrectiveTransmSwit(_model);
		engineCTS.setThreadInfo(_nproc, _rank);
		//engineCTS.setOutputAllTS_AllVio(true, engineCTS.getPathAllTS_AllVio()+_model.getFileName()+"/");
		engineCTS.setOutputAllTS(true, engineCTS.getPathAllTS() + _model.getFileName() + "/");     // to be modified.
		engineCTS.initial(_t_Start, _uri, VoltageSource.LastSolved, _vmBasePf, _vaBasePf);
		
		// Exclude solution time starting from here.
		long tExclude = System.nanoTime();
		RadialBranches radialCheck = _model.getRadialBrcData();
		boolean[] isRadial = radialCheck.getIsBrcRadial();
		_t_Start += (System.nanoTime() - tExclude);
		// Exclude solution time ending to here.
		
		if (ParamTS.getTSOption() == 3) _model.getTransmSwitListData().readTSList(_absPath + _TSList4DMFileName);
		else if (ParamTS.getTSOption() == 5) {
			//TODO: Hard coded here
			float tocTS = 0.1f;
			PreDataProcessing procBrc = new PreDataProcessing();
			procBrc.server(883, 6, "dataToRead\\EDM_Brc.txt", false, tocTS);
			_model.getTransmSwitListData().setMapBrcCtgcyToTSList(procBrc.getMap());

			PreDataProcessing procGen = new PreDataProcessing();
			procGen.server(1983, 6, "dataToRead\\EDM_Gen.txt", false, tocTS);
			_model.getTransmSwitListData().setMapGenCtgcyToTSList(procGen.getMap());
		} else if (ParamTS.getTSOption() == 6) {
			//TODO: Hard coded here
			int numRow = 5; int numCol = 6;
			FixedCTSlength proc = new FixedCTSlength(numRow, numCol, "dataToRead\\EDM_v2_Brc_CTSList.txt", false);
			_model.getTransmSwitListData().setMapBrcCtgcyToTSList(proc.getMap());
		}
		
		int numContiCheckedTS = 0;
		int[][] checkList = {_idxBrcContToBeChecked, _idxGenContToBeChecked};
		for (int iRow=0; iRow<2; iRow++)
		{
			if (checkList[iRow] == null) continue;
			for (int j=0; j<checkList[iRow].length; j++)
			{
				int idxContiBrc = checkList[iRow][j];
				
				// Exclude solution time starting from here.
				if (iRow == 0 && isRadial[idxContiBrc] == true)
				{
					if (_rank == 0) {
						tExclude = System.nanoTime();
						String title = "Brc contingency "+(idxContiBrc+1)+" is a radial branch and thus skipped";
						System.err.println("\nSuper warning: " + title + "\n");
						AuxFileXL.initFileWithTitle(engineCTS.getPath()+"ContiBrcIsRadial.txt", _model.getFileName(), true);
						AuxFileXL.initFileWithTitle(engineCTS.getPath()+"ContiBrcIsRadial.txt", title, true);
						_t_Start += (System.nanoTime() - tExclude);
					}
					continue;
				}
				// Exclude solution time ending to here.
				
				int[] idxConti = {idxContiBrc};
				if (iRow == 0) {
					engineCTS.setContToCheckTransm(idxConti);
					engineCTS.runTSTransm();
				} else if (iRow == 1) {
					engineCTS.setContToCheckGen(idxConti);
					engineCTS.runTSGen();
				}
				numContiCheckedTS++;
			}
		}
		float timeSol = (System.nanoTime() - _t_Start)/1e9f;
		engineCTS.analyzeData();
		engineCTS.outputResults(timeSol);
		if (_rank == 0) _model.getNBestTSReport().closeTSReport();
		
		_numContiCheckedTS = numContiCheckedTS;
		if (_rank == 0) System.out.println("# of contingencies checked for this .raw files: " + numContiCheckedTS);
		if (_rank == 0) System.out.println("    Total simulation time until now : " + (System.nanoTime() - _t_Start)/1e9f);		
	}
	
	
	public static void main(String[] args) throws Exception
	{
		long t_Start = System.nanoTime();
		String uri = null;
		String svstart = "Flat";
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
		
		CorrectiveTransmSwitNaive ctsApp = new CorrectiveTransmSwitNaive(model);
		ctsApp.setRunCA(true);
		ctsApp.setTstart(t_Start);
		
		ctsApp.runPF(vstart);
		ctsApp.runCA();
		ctsApp.runCTS();
		
		System.out.println("    Total time : " + (System.nanoTime() - t_Start)/1e9f);		
	}

}
