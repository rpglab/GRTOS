package com.rtca_cts.transmissionswitching;

import static org.junit.Assert.assertTrue;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;

import org.junit.Test;

import com.powerdata.openpa.psse.ACBranchList;
import com.powerdata.openpa.psse.GenList;
import com.powerdata.openpa.psse.Load;
import com.powerdata.openpa.psse.LoadList;
import com.powerdata.openpa.psse.PsseModel;
import com.powerdata.openpa.psse.PsseModelException;
import com.powerdata.openpa.psse.powerflow.VoltageSource;
import com.powerdata.openpa.psse.util.ImpedanceFilter;
import com.rtca_cts.ausXP.InitPsseModel;
import com.rtca_cts.contingencyanalysis.ContingencyAnalysisSeq;
import com.rtca_cts.contingencyanalysis.GensPFactors;
import com.rtca_cts.param.ParamFDPFlow;
import com.rtca_cts.param.ParamIO;
import com.utilxl.array.AuxArrayXL;
import com.utilxl.iofiles.AuxFileXL;


/**
 * Implement transmission switching;
 * 
 * Initialized in Jun. 2014.
 * 
 * @author Xingpeng.Li, xplipower@gmail.com 
 */
public class CorrectiveTransmSwit {
	
	PsseModel _model;
	ImpedanceFilter _zfilt;
	ACBranchList _branches;
	int _nbr;
    GenList _gens;
    int _nGen;
	VoltageSource _vstart = VoltageSource.Flat;
	float[] _vmBasePf = null;
	float[] _vaBasePf = null;

	// for threads info
	int _nproc = 1;
	int _rank = 0;

	long _t_Start;      // Cpu time to launch this object.
	long _startTimeOneTS;  // used to record the TS solution time for each contingency.
	long _timeExcluded = 0;   // solution time excluded spent by non-necessary code.
	
	// data output issue
	String _title = null;    // Contains the raw file name.
	String _path;   // for summation violation information per raw file.
	
	String _pathBestTS;   // for details violation information per raw file.
	String _fileNameBestTSTransm = "results_BestTS_PerBrcConti.txt";
	String _fileNameBestTSGen = "results_BestTS_PerGenConti.txt";
	
	String _pathAllTS;   // for details summation violation information per TS.
	boolean _outputAllTS;
	String _fileNameAllTS = "results_SumVio_PerTS.txt";
	
	String _pathAllTS_AllVio;   // for details individual violation information per TS.
	boolean _outputAllTS_AllVio;  // if this is true, when it launchs CheckTS routine, _outputAllTS will also be automatically set to true.
	String _fileNameAllTS_AllVio = "results_ElemsVio_PerTS.txt";

	
	// for Gen Var Limit
	boolean _MarkGenVarLimit = ParamFDPFlow.getGenVarLimit();  // true by default, Change PV bus to PQ bus once the Qg is beyond its capacity.
	int _GenVarLimitOption = 1;       // 1 by default, options that how deal with Gen Var Limit Constraint.
	int _MaxIterPfVarLimit = 10;      // 10 by default, maximum iteration for option 1.
//	boolean _MarkUsingVgSched = true;    // if true, then, use generator scheduled set point to be the Vm of PV Buses.
		
	// Transm Swit for transmission contingenies
	int _numContiCheckRTTransm = 1;  // The program is checking the _numContiCheckRTTransm-th Transm contingency.
	int[] _numTSCheckedTransm;
	
	int _numContToBeCheckedTransm;
	int[] _idxContToBeCheckedTransm;  // index of Transm contingencies of which violation may be reduced by transmission switching.
	int _numContAlreadyBeenCheckedTransm = 0;
	
	int _numContVioWithinTolTransm;    // number of Transm Contingencies that its violation is within tolerance. 
	int _numContVioWithinVmTolTransm;    // number of Transm Contingencies that its voltage violation is within tolerance. 
	int _numContVioWithinBrcTolTransm;    // number of Transm Contingencies that its flow violation is within tolerance. 
	
	int _numContVioBadTransm;    // number of Transm Contingencies that its violation can not be eliminated or reduced. 
	int[] _idxContVioBadTransm;
	float[] _sumVmVioOrigContVioBadTransm;      // summation of voltage violation of the original contingency, index conform to _idxContVioBadTransm.
	float[] _sumBrcVioOrigContVioBadTransm;     // summation of branch violation of the original contingency, index conform to _idxContVioBadTransm.

	int _numContVioToNoVioTransm;    // number of Transm Contingencies that its violation can be eliminated.
	int[] _numGoodTStoNoVioTransm;
	int _numContVioToRedVioTransm;    // number of Transm Contingencies that its violation can be reduced.
	int[] _numGoodTStoRedVioTransm;
	
	int _numContVioImpTransm;
	int[] _idxContWithImpTransm;
	float[] _sumVmVioOrigContWithImpTransm;      // summation of voltage violation of the original contingency, index conform to _idxContWithImpTransm.
	float[] _sumBrcVioOrigContWithImpTransm;     // summation of branch violation of the original contingency, index conform to _idxContWithImpTransm.
	
	float[] _maxBrcVioImpTransm;       // max Brc violation improvement (in percentage) per contingency.
	float[] _VmVioImpAssoToMaxBrcVioImpTransm;     // Voltage violation improvement (in percentage) associated to the Max Brc violation improvement per contingency.
	int[] _idxTSMaxBrcVioImpTransm;    // best switching action corresponding to max Brc violation improvement. 

	float[] _maxVmVioImpTransm;       // max Voltage violation improvement (in percentage) per contingency.
	float[] _BrcVioImpAssoToMaxVmVioImpTransm;   // Brc violation improvement (in percentage) associated to the Max Brc violation improvement per contingency.
	int[] _idxTSMaxVmVioImpTransm;

	float[] _maxSumVioImpTransm;     // max violation improvement (in percentage) per contingency.
	float[] _BrcVioImpAssoToMaxSumVioImpTransm;  // Voltage violation improvement (in percentage) associated to the Max (Brc violation improvement + Voltage violation improvement) per contingency.
	float[] _VmVioImpAssoToMaxSumVioImpTransm;   // Brc violation improvement (in percentage) associated to the Max (Brc violation improvement + Voltage violation improvement) per contingency.
	int[] _idxTSMaxSumVioImpTransm;

	// Transm Swit for generator contingenies
	int _numContiCheckRTGen = 1;  // The program is checking the _numContiCheckRTGen-th Gen contingency.
	int[] _numTSCheckedGen;

	int _numContToBeCheckedGen;
	int[] _idxContToBeCheckedGen;  // index of Gen contingencies of which violation may be reduced by transmission switching.
	int _numContAlreadyBeenCheckedGen = 0;
	
	int _numContVioWithinTolGen;    // number of Gen Contingencies that its violation is within tolerance. 
	int _numContVioWithinVmTolGen;    // number of Gen Contingencies that its voltage violation is within tolerance. 
	int _numContVioWithinBrcTolGen;    // number of Gen Contingencies that its flow violation is within tolerance. 

	int _numContVioBadGen;    // number of Gen Contingencies that its violation can not be eliminated or reduced. 
	int[] _idxContVioBadGen;
	float[] _sumVmVioOrigContVioBadGen;      // summation of voltage violation of the original contingency, index conform to _idxContVioBadGen.
	float[] _sumBrcVioOrigContVioBadGen;     // summation of branch violation of the original contingency, index conform to _idxContVioBadGen.
	
	int _numContVioToNoVioGen;    // number of Gen Contingencies that its violation can be eliminated.
	int[] _numGoodTStoNoVioGen;
	int _numContVioToRedVioGen;    // number of Gen Contingencies that its violation can be reduced.
	int[] _numGoodTStoRedVioGen;	
		
	int _numContVioImpGen;
	int[] _idxContWithImpGen;
	float[] _sumVmVioOrigContWithImpGen;      // summation of voltage violation of the original contingency, index conform to _idxContWithImpGen.
	float[] _sumBrcVioOrigContWithImpGen;     // summation of branch violation of the original contingency, index conform to _idxContWithImpGen.

	float[] _maxBrcVioImpGen;       // max Brc violation improvement (in percentage) per contingency.
	float[] _VmVioImpAssoToMaxBrcVioImpGen;     // Voltage violation improvement (in percentage) associated to the Max Brc violation improvement per contingency.
	int[] _idxTSMaxBrcVioImpGen;    // best switching action corresponding to max Brc violation improvement. 

	float[] _maxVmVioImpGen;       // max Voltage violation improvement (in percentage) per contingency.
	float[] _BrcVioImpAssoToMaxVmVioImpGen;   // Brc violation improvement (in percentage) associated to the Max Brc violation improvement per contingency.
	int[] _idxTSMaxVmVioImpGen;

	float[] _maxSumVioImpGen;
	float[] _BrcVioImpAssoToMaxSumVioImpGen;  // Voltage violation improvement (in percentage) associated to the Max (Brc violation improvement + Voltage violation improvement) per contingency.
	float[] _VmVioImpAssoToMaxSumVioImpGen;   // Brc violation improvement (in percentage) associated to the Max (Brc violation improvement + Voltage violation improvement) per contingency.
	int[] _idxTSMaxSumVioImpGen;


	public CorrectiveTransmSwit(PsseModel model) throws PsseModelException, IOException
	{
		_model = model;
		_branches = _model.getBranches();
		initPath();
	}

	void initPath()
	{
//		_path = ParamIO.getOutPath();
		if(_rank == 0) AuxFileXL.createFolder(ParamIO.getOutPath());
		_path = ParamIO.getTSPath();
		if(_rank == 0) AuxFileXL.createFolder(_path);
		_pathBestTS = _path;
		_pathAllTS = _path;
		_pathAllTS_AllVio = _path;
	}

	/** All path related methods must be called before calling initial(). */
	public void initial(long t_Start, String title, VoltageSource vstart) throws PsseModelException, IOException
	{
		_title = title;
		initOutputBestTS(title);
		initial(t_Start, vstart);
	}
	public void initial(long t_Start, String title, VoltageSource vstart, float[] vmBasePf, float[] vaBasePf) throws PsseModelException, IOException
	{
		_title = title;
		initOutputBestTS(title);
		initial(t_Start, vstart, vmBasePf, vaBasePf);
	}
	public void initial(long t_Start, VoltageSource vstart) throws PsseModelException, IOException
	{
		_vstart = vstart;
		initial(t_Start);
	}
	public void initial(long t_Start, VoltageSource vstart, float[] vmBasePf, float[] vaBasePf) throws PsseModelException, IOException
	{
		_vstart = vstart;
		initial(t_Start, vmBasePf, vaBasePf);
	}
	public void initial(long t_Start, String title) throws PsseModelException, IOException
	{
		_title = title;
		initOutputBestTS(title);
		initial(t_Start);
	}
	public void initial(long t_Start, float[] vmBasePf, float[] vaBasePf) throws PsseModelException, IOException
	{
		_vmBasePf = vmBasePf;
		_vaBasePf = vaBasePf;
		initial(t_Start);
	}
	public void initial(long t_Start) throws PsseModelException, IOException
	{
		if (_outputAllTS == true) initOutputAllTS();
		if (_outputAllTS_AllVio == true) initOutputAllTS_AllVio();
		
		_t_Start = t_Start;
		_nbr = _branches.size();
		_gens = _model.getGenerators();
		_nGen = _gens.size();
		
		_numGoodTStoNoVioTransm = new int[_nbr];
		_numGoodTStoRedVioTransm = new int[_nbr];	
		
		_idxContVioBadTransm = new int[_nbr];
		_sumVmVioOrigContVioBadTransm = new float[_nbr];
		_sumBrcVioOrigContVioBadTransm = new float[_nbr];

		_idxContWithImpTransm = new int[_nbr];		
		_sumVmVioOrigContWithImpTransm = new float[_nbr];
		_sumBrcVioOrigContWithImpTransm = new float[_nbr];

		_maxBrcVioImpTransm = new float[_nbr];
		_VmVioImpAssoToMaxBrcVioImpTransm = new float[_nbr];
		_idxTSMaxBrcVioImpTransm = new int[_nbr];
		
		_maxVmVioImpTransm = new float[_nbr];
		_BrcVioImpAssoToMaxVmVioImpTransm = new float[_nbr];
		_idxTSMaxVmVioImpTransm = new int[_nbr];
		
		_maxSumVioImpTransm = new float[_nbr];
		_BrcVioImpAssoToMaxSumVioImpTransm = new float[_nbr];
		_VmVioImpAssoToMaxSumVioImpTransm = new float[_nbr];
		_idxTSMaxSumVioImpTransm = new int[_nbr];
		
		_numTSCheckedTransm = new int[_nbr];

		// Gen
		_numGoodTStoNoVioGen = new int[_nGen];
		_numGoodTStoRedVioGen = new int[_nGen];		
		
		_idxContVioBadGen = new int[_nGen];
		_sumVmVioOrigContVioBadGen = new float[_nGen];
		_sumBrcVioOrigContVioBadGen = new float[_nGen];

		_idxContWithImpGen = new int[_nGen];			
		_sumVmVioOrigContWithImpGen = new float[_nGen];
		_sumBrcVioOrigContWithImpGen= new float[_nGen];

		_maxBrcVioImpGen = new float[_nGen];
		_VmVioImpAssoToMaxBrcVioImpGen = new float[_nGen];
		_idxTSMaxBrcVioImpGen = new int[_nGen];
		
		_maxVmVioImpGen = new float[_nGen];
		_BrcVioImpAssoToMaxVmVioImpGen = new float[_nGen];
		_idxTSMaxVmVioImpGen = new int[_nGen];
		
		_maxSumVioImpGen = new float[_nGen];
		_BrcVioImpAssoToMaxSumVioImpGen = new float[_nGen];
		_VmVioImpAssoToMaxSumVioImpGen = new float[_nGen];
		_idxTSMaxSumVioImpGen = new int[_nGen];
		
		_numTSCheckedGen = new int[_nGen];
	}
	
	/** threads info. */ 
	public void setThreadInfo(int nproc, int rank) {_nproc = nproc; _rank = rank;}

	public String getPath() { return _path;}
	public void setPath(String path) { _path = path; if(_rank == 0) AuxFileXL.createFolder(path);}
	
	public String getPathBestTS() { return _pathBestTS;}
	public void setPathBestTS(String pathBestTS) { _pathBestTS = pathBestTS; if(_rank == 0) AuxFileXL.createFolder(pathBestTS);}

	public String getPathAllTS() { return _pathAllTS;}
	public void setPathAllTS(String pathAllTS) { _pathAllTS = pathAllTS; if(_rank == 0) AuxFileXL.createFolder(pathAllTS);}
	public void setOutputAllTS(boolean mark) { _outputAllTS = mark;}
	public void setOutputAllTS(boolean mark, String pathAllTS)
	{
		_outputAllTS = mark; 
		setPathAllTS(pathAllTS);
	}
	public String getPathAllTS_AllVio() { return _pathAllTS_AllVio;}
	public void setPathAllTS_AllVio(String pathAllVio) { _pathAllTS_AllVio = pathAllVio; if(_rank == 0) AuxFileXL.createFolder(pathAllVio);}
	public void setOutputAllTS_AllVio(boolean mark) { _outputAllTS_AllVio = mark;}
	public void setOutputAllTS_AllVio(boolean mark, String pathAllVio)
	{
		_outputAllTS_AllVio = mark; 
		setPathAllTS_AllVio(pathAllVio);
	}
	
	
//	public void setMarkUsingVgSched(boolean aa) {_MarkUsingVgSched = aa;}
	
	public void clearNumCheckedTransm() { _numContToBeCheckedTransm = 0;}
	public void setContToCheckTransm(int[] idxContToBeChecked)
	{
		_idxContToBeCheckedTransm = idxContToBeChecked;
		_numContToBeCheckedTransm = idxContToBeChecked.length;
		_numContAlreadyBeenCheckedTransm += idxContToBeChecked.length;
	}

	public void clearNumCheckedGen() {_numContToBeCheckedGen = 0;}
	public void setContToCheckGen(int[] idxContToBeChecked)
	{
		_idxContToBeCheckedGen = idxContToBeChecked;
		_numContToBeCheckedGen = _idxContToBeCheckedGen.length;
		_numContAlreadyBeenCheckedGen += idxContToBeChecked.length;
	}
	
	public long getTimeExcluded() {return _timeExcluded;}
	public void clearTimeExclded() {_timeExcluded = 0;}

	public void runTS() throws PsseModelException, IOException
	{
		runTSGen();
		runTSTransm();
	}
	public void runTSGen() throws PsseModelException, IOException
	{
		for(int i=0; i<_numContToBeCheckedGen; i++)
		{
			if (_numContiCheckRTGen == 1) initPreTS_Setting();
			runTransmSwitGen(i);
			if (_rank == 0) System.out.println("    The " + (_numContiCheckRTGen++) + "-th Gen Contingency is done with TS process.");
			if (_rank == 0) System.out.println("    Total time until now: " + (System.nanoTime() - _t_Start)/1e9f);
		}
//		analyzeDataGen();
	}
	
	public void runTSTransm() throws PsseModelException, IOException
	{
		for(int i=0; i<_numContToBeCheckedTransm; i++)
		{
			initPreTS_Setting();
			runTransmSwitTransm(i);
			if (_rank == 0) System.out.println("    The " + (_numContiCheckRTTransm++) + "-th Brc Contingency is done with TS process.");
			if (_rank == 0) System.out.println("    Total time until now: " + (System.nanoTime() - _t_Start)/1e9f);
		}
//		analyzeDataTransm();
	}
	
	public void initPreTS_Setting() throws PsseModelException
	{
		_model.getNearbyElemsData().clearRadialData();
		_model.getTransmSwitListData().clearTSListDataMining();
		_model.getTransmSwitListData().clearNdxRadialBrc();
	}
	
	public void runTransmSwitGen(int iGenConti) throws PsseModelException, IOException
	{
		_startTimeOneTS = System.nanoTime();
//		_model.resetTP();  //Not necessary any more to keep this one for parallel computing.
		int idxGenCont = _idxContToBeCheckedGen[iGenConti];
		boolean inSvc = _gens.isInSvc(idxGenCont);
		assertTrue (inSvc == true);
		_gens.setInSvc(idxGenCont, false);
		if (_rank == 0) System.out.println("\nCheck TS on Gen Contingency: "+idxGenCont+ " (index from 0)");
		
		_model.getBusTypeManagerData().usePrePFBusType();
		GensPFactors genPFactors = new GensPFactors(_model,idxGenCont);
		genPFactors.createPfactorsRes();
		genPFactors.launch();
		_model.getBusTypeManagerData().setPreTSBusType();
		
		int idxGenBus = _gens.get(idxGenCont).getBus().getI();
		int[] contiInfo = {idxGenCont+1, idxGenBus, -1};
		TransmSwit checkTS = callCheckTransmSwit(contiInfo);
		if (_rank == 0)
		{
			_numTSCheckedGen[_numContiCheckRTGen-1] = checkTS.getNumTSChecked();
			if (checkTS.getWithinTol() == false)
			{
				int numTStoNoVio = checkTS.getNumTStoNoVio();
				if (numTStoNoVio > 0)
				{
					_numGoodTStoNoVioGen[_numContVioToNoVioGen++] = numTStoNoVio;
					retrieveImpInfoGen(checkTS, idxGenCont, true);
				}
				else
				{
					int numTStoRedVio = checkTS.getNumTStoRedVio();
					if (numTStoRedVio > 0)
					{
						_numGoodTStoRedVioGen[_numContVioToRedVioGen++] = numTStoRedVio;
						retrieveImpInfoGen(checkTS, idxGenCont, true);
					}
					else retrieveImpInfoGen(checkTS, idxGenCont, false);
				}
			}
			else _numContVioWithinTolGen++;
			if (checkTS.getSumContiVmVio() < checkTS.getTolSumVm()) _numContVioWithinVmTolGen++;
			if (checkTS.getSumContiBrcVio() < checkTS.getTolSumMVA()) _numContVioWithinBrcTolGen++;
		}
		
		genPFactors.refresh();
		_gens.setInSvc(idxGenCont, true);
	}
	
	public void runTransmSwitTransm(int iBrcConti) throws PsseModelException, IOException
	{
		_startTimeOneTS = System.nanoTime();
		int idxCont = _idxContToBeCheckedTransm[iBrcConti];
		boolean inSvc = _branches.isInSvc(idxCont);
		assertTrue (inSvc == true);
		_branches.setInSvc(idxCont, false); 
		
        /* Below: temporary code for SCEDwCTS */
        //_model.getDiary().hotLine(LogTypeXL.CheckPoint, "Testing ctgcy: " + (idxCont+1));;
        /* Above: temporary code for SCEDwCTS */

		if (_rank == 0) System.out.println("\nCheck TS on Brc Contingency: "+idxCont + " (index from 0)");
		_model.getBusTypeManagerData().usePrePFBusType();
		_model.getBusTypeManagerData().setPreTSBusType();

		int numFrmBus = _branches.get(idxCont).getFromBus().getI();
		int numToBus = _branches.get(idxCont).getToBus().getI();
		int[] contiInfo = {idxCont+1, numFrmBus, numToBus};
		TransmSwit checkTS = callCheckTransmSwit(contiInfo);
		if (_rank == 0)
		{
			_numTSCheckedTransm[_numContiCheckRTTransm-1] = checkTS.getNumTSChecked();
			if (checkTS.getWithinTol() == false)
			{
				int numTStoNoVio = checkTS.getNumTStoNoVio();
				if (numTStoNoVio > 0)
				{
					_numGoodTStoNoVioTransm[_numContVioToNoVioTransm++] = numTStoNoVio;
					retrieveImpInfoTransm(checkTS, idxCont, true);
				}
				else
				{
					int numTStoRedVio = checkTS.getNumTStoRedVio();
					if (numTStoRedVio > 0)
					{
						_numGoodTStoRedVioTransm[_numContVioToRedVioTransm++] = numTStoRedVio;
						retrieveImpInfoTransm(checkTS, idxCont, true);
					}
					else retrieveImpInfoTransm(checkTS, idxCont, false);
				}
			}
			else _numContVioWithinTolTransm++;
			if (checkTS.getSumContiVmVio() < checkTS.getTolSumVm()) _numContVioWithinVmTolTransm++;
			if (checkTS.getSumContiBrcVio() < checkTS.getTolSumMVA()) _numContVioWithinBrcTolTransm++;
		}

		_branches.setInSvc(idxCont, true);
		//return checkTS.getSumBrcVioTS();
	}
	
	TransmSwit callCheckTransmSwit(int[] contiInfo) throws PsseModelException, IOException
	{
		TransmSwit checkTS = null;
		if (_nproc == 1) checkTS = new TransmSwitSeq(_model, _vstart, _vmBasePf, _vaBasePf);
		else checkTS = new TransmSwitParallel(_model, _vstart, _vmBasePf, _vaBasePf);
		
//		if(_MarkUsingVgSched == false) checkTS.setMarkUsingVgSched(false);
		if (_MarkGenVarLimit == true) checkTS.enableGenVarLimit();
		else checkTS.clearGenVarLimit();
		if (_outputAllTS_AllVio == true) {
			_outputAllTS = true;
			checkTS.setOutputAllTS_AllVio(_outputAllTS_AllVio, _pathAllTS_AllVio + _fileNameAllTS_AllVio);
		}
		checkTS.setOutputAllTS(_outputAllTS, _pathAllTS + _fileNameAllTS, contiInfo);
		checkTS.setAllTSTitle(_title);
		checkTS.setThreadInfo(_nproc, _rank);
		checkTS.launchTS();
		
		_startTimeOneTS += checkTS.getTimeExcluded();
		_timeExcluded += checkTS.getTimeExcluded();
		return checkTS;
	}
	
	public void analyzeData()
	{
		analyzeDataGen();
		analyzeDataTransm();
	}
	
	public void analyzeDataGen()
	{
		if (_rank == 0)
		{
			if (_numContAlreadyBeenCheckedGen != 0)
			{
				_numGoodTStoNoVioGen = shrinkArraySize(_numGoodTStoNoVioGen,_numContVioToNoVioGen);
				_numGoodTStoRedVioGen = shrinkArraySize(_numGoodTStoRedVioGen,_numContVioToRedVioGen);
			}
			if (_numContVioImpGen != 0)
			{
				_idxContVioBadGen = shrinkArraySize(_idxContVioBadGen, _numContVioBadGen);
				_sumBrcVioOrigContVioBadGen = shrinkArraySize(_sumBrcVioOrigContVioBadGen, _numContVioBadGen);
				_sumVmVioOrigContVioBadGen = shrinkArraySize(_sumVmVioOrigContVioBadGen, _numContVioBadGen);			
				
				_idxContWithImpGen = shrinkArraySize(_idxContWithImpGen, _numContVioImpGen);			
				_sumBrcVioOrigContWithImpGen = shrinkArraySize(_sumBrcVioOrigContWithImpGen, _numContVioImpGen);
				_sumVmVioOrigContWithImpGen = shrinkArraySize(_sumVmVioOrigContWithImpGen, _numContVioImpGen);
				
				_maxBrcVioImpGen = shrinkArraySize(_maxBrcVioImpGen, _numContVioImpGen);
				_VmVioImpAssoToMaxBrcVioImpGen = shrinkArraySize(_VmVioImpAssoToMaxBrcVioImpGen, _numContVioImpGen);
				_idxTSMaxBrcVioImpGen = shrinkArraySize(_idxTSMaxBrcVioImpGen, _numContVioImpGen);
						
				_maxVmVioImpGen = shrinkArraySize(_maxVmVioImpGen, _numContVioImpGen);
				_BrcVioImpAssoToMaxVmVioImpGen = shrinkArraySize(_BrcVioImpAssoToMaxVmVioImpGen, _numContVioImpGen);
				_idxTSMaxVmVioImpGen = shrinkArraySize(_idxTSMaxVmVioImpGen, _numContVioImpGen);			
				
				_maxSumVioImpGen = shrinkArraySize(_maxSumVioImpGen, _numContVioImpGen);
				_BrcVioImpAssoToMaxSumVioImpGen = shrinkArraySize(_BrcVioImpAssoToMaxSumVioImpGen, _numContVioImpGen);
				_VmVioImpAssoToMaxSumVioImpGen = shrinkArraySize(_VmVioImpAssoToMaxSumVioImpGen, _numContVioImpGen);
				_idxTSMaxSumVioImpGen = shrinkArraySize(_idxTSMaxSumVioImpGen, _numContVioImpGen);			
			}
			else
			{
				_idxContVioBadGen = null;
				_sumBrcVioOrigContVioBadGen = null;
				_sumVmVioOrigContVioBadGen = null;			
				_idxContWithImpGen = null;			
				_sumBrcVioOrigContWithImpGen = null;
				_sumVmVioOrigContWithImpGen = null;
				_maxBrcVioImpGen = null;
				_VmVioImpAssoToMaxBrcVioImpGen = null; 
				_idxTSMaxBrcVioImpGen = null;	
				_maxVmVioImpGen = null;
				_BrcVioImpAssoToMaxVmVioImpGen = null; 
				_idxTSMaxVmVioImpGen = null;	
				_maxSumVioImpGen = null;
				_BrcVioImpAssoToMaxSumVioImpGen = null; 
				_VmVioImpAssoToMaxSumVioImpGen = null;
				_idxTSMaxSumVioImpGen = null;
			}
		}
	}
	
	
	public void analyzeDataTransm()
	{
		if (_rank == 0)
		{
			if (_numContAlreadyBeenCheckedTransm != 0)
			{
				_numGoodTStoNoVioTransm = shrinkArraySize(_numGoodTStoNoVioTransm,_numContVioToNoVioTransm);
				_numGoodTStoRedVioTransm = shrinkArraySize(_numGoodTStoRedVioTransm,_numContVioToRedVioTransm);
			}
			if (_numContVioImpTransm != 0)
			{
				_idxContVioBadTransm = shrinkArraySize(_idxContVioBadTransm, _numContVioBadTransm);		
				_sumBrcVioOrigContVioBadTransm = shrinkArraySize(_sumBrcVioOrigContVioBadTransm, _numContVioBadTransm);		
				_sumVmVioOrigContVioBadTransm = shrinkArraySize(_sumVmVioOrigContVioBadTransm, _numContVioBadTransm);		
				
				_idxContWithImpTransm = shrinkArraySize(_idxContWithImpTransm, _numContVioImpTransm);		
				_sumBrcVioOrigContWithImpTransm = shrinkArraySize(_sumBrcVioOrigContWithImpTransm, _numContVioImpTransm);
				_sumVmVioOrigContWithImpTransm = shrinkArraySize(_sumVmVioOrigContWithImpTransm, _numContVioImpTransm);

				_maxBrcVioImpTransm = shrinkArraySize(_maxBrcVioImpTransm, _numContVioImpTransm);
				_VmVioImpAssoToMaxBrcVioImpTransm = shrinkArraySize(_VmVioImpAssoToMaxBrcVioImpTransm, _numContVioImpTransm);
				_idxTSMaxBrcVioImpTransm = shrinkArraySize(_idxTSMaxBrcVioImpTransm, _numContVioImpTransm);
				
				_maxVmVioImpTransm = shrinkArraySize(_maxVmVioImpTransm, _numContVioImpTransm);
				_BrcVioImpAssoToMaxVmVioImpTransm = shrinkArraySize(_BrcVioImpAssoToMaxVmVioImpTransm, _numContVioImpTransm);
				_idxTSMaxVmVioImpTransm = shrinkArraySize(_idxTSMaxVmVioImpTransm, _numContVioImpTransm);
				
				_maxSumVioImpTransm = shrinkArraySize(_maxSumVioImpTransm, _numContVioImpTransm);
				_BrcVioImpAssoToMaxSumVioImpTransm = shrinkArraySize(_BrcVioImpAssoToMaxSumVioImpTransm, _numContVioImpTransm);
				_VmVioImpAssoToMaxSumVioImpTransm = shrinkArraySize(_VmVioImpAssoToMaxSumVioImpTransm, _numContVioImpTransm);
				_idxTSMaxSumVioImpTransm = shrinkArraySize(_idxTSMaxSumVioImpTransm, _numContVioImpTransm);			
			}
			else
			{
				_idxContVioBadTransm = null;		
				_sumBrcVioOrigContVioBadTransm = null;		
				_sumVmVioOrigContVioBadTransm = null;
				_idxContWithImpTransm = null;			
				_sumBrcVioOrigContWithImpTransm = null;
				_sumVmVioOrigContWithImpTransm = null;
				_maxBrcVioImpTransm = null;
				_VmVioImpAssoToMaxBrcVioImpTransm = null; 
				_idxTSMaxBrcVioImpTransm = null;	
				_maxVmVioImpTransm = null;
				_BrcVioImpAssoToMaxVmVioImpTransm = null; 
				_idxTSMaxVmVioImpTransm = null;	
				_maxSumVioImpTransm = null;
				_BrcVioImpAssoToMaxSumVioImpTransm = null; 
				_VmVioImpAssoToMaxSumVioImpTransm = null;
				_idxTSMaxSumVioImpTransm = null;
			}
		}
	}

	void retrieveImpInfoGen(TransmSwit checkTS, int idxGenCont, boolean isTSWork) throws PsseModelException
	{
		if (isTSWork == true)
		{
			_idxContWithImpGen[_numContVioImpGen] = idxGenCont;
			_sumBrcVioOrigContWithImpGen[_numContVioImpGen] = checkTS.getSumContiBrcVio();
			_sumVmVioOrigContWithImpGen[_numContVioImpGen] = checkTS.getSumContiVmVio();
			
			_maxBrcVioImpGen[_numContVioImpGen] = checkTS.getMaxBrcVioImp();
			_VmVioImpAssoToMaxBrcVioImpGen[_numContVioImpGen] = checkTS.getVmVioImpAssoToMaxBrcVioImp();
			_idxTSMaxBrcVioImpGen[_numContVioImpGen] = checkTS.getIdxTSMaxBrcVioImp();
			
			_maxVmVioImpGen[_numContVioImpGen] = checkTS.getMaxVmVioImp();
			_BrcVioImpAssoToMaxVmVioImpGen[_numContVioImpGen] = checkTS.getBrcVioImpAssoToMaxVmVioImp();
			_idxTSMaxVmVioImpGen[_numContVioImpGen] = checkTS.getIdxTSMaxVmVioImp();

			_maxSumVioImpGen[_numContVioImpGen] = checkTS.getMaxSumVioImp();
			_BrcVioImpAssoToMaxSumVioImpGen[_numContVioImpGen] = checkTS.getBrcVioImpAssoToMaxSumVioImp();
			_VmVioImpAssoToMaxSumVioImpGen[_numContVioImpGen] = checkTS.getVmVioImpAssoToMaxSumVioImp();
			_idxTSMaxSumVioImpGen[_numContVioImpGen] = checkTS.getIdxTSMaxSumVioImp();
			outputBestTSGen(isTSWork);  // output methods must be invoked before _numContVioImpGen++;
			_numContVioImpGen++;  
		}
		else
		{
			_idxContVioBadGen[_numContVioBadGen] = idxGenCont;
			_sumBrcVioOrigContVioBadGen[_numContVioBadGen] = checkTS.getSumContiBrcVio();
			_sumVmVioOrigContVioBadGen[_numContVioBadGen] = checkTS.getSumContiVmVio();
			outputBestTSGen(isTSWork);   // output methods must be invoked before _numContVioBadGen++;
			_numContVioBadGen++;
		}
	}
	
	void retrieveImpInfoTransm(TransmSwit checkTS, int idxCont, boolean isTSWork) throws PsseModelException
	{
		if (isTSWork == true)
		{
			_idxContWithImpTransm[_numContVioImpTransm] = idxCont;
			_sumBrcVioOrigContWithImpTransm[_numContVioImpTransm] = checkTS.getSumContiBrcVio();
			_sumVmVioOrigContWithImpTransm[_numContVioImpTransm] = checkTS.getSumContiVmVio();

			_maxBrcVioImpTransm[_numContVioImpTransm] = checkTS.getMaxBrcVioImp();
			_VmVioImpAssoToMaxBrcVioImpTransm[_numContVioImpTransm] = checkTS.getVmVioImpAssoToMaxBrcVioImp();
			_idxTSMaxBrcVioImpTransm[_numContVioImpTransm] = checkTS.getIdxTSMaxBrcVioImp();
			
			_maxVmVioImpTransm[_numContVioImpTransm] = checkTS.getMaxVmVioImp();
			_BrcVioImpAssoToMaxVmVioImpTransm[_numContVioImpTransm] = checkTS.getBrcVioImpAssoToMaxVmVioImp();
			_idxTSMaxVmVioImpTransm[_numContVioImpTransm] = checkTS.getIdxTSMaxVmVioImp();
			
			_maxSumVioImpTransm[_numContVioImpTransm] = checkTS.getMaxSumVioImp();
			_BrcVioImpAssoToMaxSumVioImpTransm[_numContVioImpTransm] = checkTS.getBrcVioImpAssoToMaxSumVioImp();
			_VmVioImpAssoToMaxSumVioImpTransm[_numContVioImpTransm] = checkTS.getVmVioImpAssoToMaxSumVioImp();
			_idxTSMaxSumVioImpTransm[_numContVioImpTransm] = checkTS.getIdxTSMaxSumVioImp();
			outputBestTSTransm(isTSWork);  // output methods must be invoked before _numContVioImpTransm++;
			_numContVioImpTransm++;
		}
		else
		{
			_idxContVioBadTransm[_numContVioBadTransm] = idxCont;
			_sumBrcVioOrigContVioBadTransm[_numContVioBadTransm] = checkTS.getSumContiBrcVio();
			_sumVmVioOrigContVioBadTransm[_numContVioBadTransm] = checkTS.getSumContiVmVio();
			outputBestTSTransm(isTSWork);  // output methods must be invoked before _numContVioImpTransm++;
			_numContVioBadTransm++; 
		}
	}
	
	int[] shrinkArraySize(int[] Array, int size)
	{
		if (Array.length != size)
		{
			Array = Arrays.copyOf(Array, size);
		}
		return Array;
	}
	
	float[] shrinkArraySize(float[] Array, int size)
	{
		if (Array.length != size)
		{
			Array = Arrays.copyOf(Array, size);
		}
		return Array;
	}
	
	void checkResults()
	{
		int tmp = _numContVioToNoVioTransm + _numContVioToRedVioTransm;
		assertTrue(_numContVioImpTransm == tmp);
		int tmp2 = _numContVioToNoVioGen + _numContVioToRedVioGen;
		assertTrue(_numContVioImpGen == tmp2);
		assertTrue(_numContVioWithinVmTolGen >= _numContVioWithinTolGen);
		assertTrue(_numContVioWithinBrcTolGen >= _numContVioWithinTolGen);
		assertTrue(_numContVioWithinVmTolTransm >= _numContVioWithinTolTransm);
		assertTrue(_numContVioWithinBrcTolTransm >= _numContVioWithinTolTransm);
		
		if (_numContVioImpGen != 0)
		{
			assertTrue(_idxContVioBadGen.length == _numContVioBadGen);
			assertTrue(_sumBrcVioOrigContVioBadGen.length == _numContVioBadGen);
			assertTrue(_sumVmVioOrigContVioBadGen.length == _numContVioBadGen);
			assertTrue(_idxContWithImpGen.length == _numContVioImpGen);
			assertTrue(_sumVmVioOrigContWithImpGen.length == _numContVioImpGen);
			assertTrue(_sumBrcVioOrigContWithImpGen.length == _numContVioImpGen);		
			assertTrue(_maxBrcVioImpGen.length == _numContVioImpGen);
			assertTrue(_VmVioImpAssoToMaxBrcVioImpGen.length == _numContVioImpGen);
			assertTrue(_idxTSMaxBrcVioImpGen.length == _numContVioImpGen);
			assertTrue(_maxVmVioImpGen.length == _numContVioImpGen);
			assertTrue(_BrcVioImpAssoToMaxVmVioImpGen.length == _numContVioImpGen);
			assertTrue(_idxTSMaxVmVioImpGen.length == _numContVioImpGen);
			assertTrue(_maxSumVioImpGen.length == _numContVioImpGen);
			assertTrue(_BrcVioImpAssoToMaxSumVioImpGen.length == _numContVioImpGen);
			assertTrue(_VmVioImpAssoToMaxSumVioImpGen.length == _numContVioImpGen);
			assertTrue(_idxTSMaxSumVioImpGen.length == _numContVioImpGen);
		}
		
		if (_numContVioImpTransm != 0)
		{
			assertTrue(_idxContVioBadTransm.length == _numContVioBadTransm);
			assertTrue(_sumBrcVioOrigContVioBadTransm.length == _numContVioBadTransm);
			assertTrue(_sumVmVioOrigContVioBadTransm.length == _numContVioBadTransm);
			assertTrue(_idxContWithImpTransm.length == _numContVioImpTransm);
			assertTrue(_sumVmVioOrigContWithImpTransm.length == _numContVioImpTransm);
			assertTrue(_sumBrcVioOrigContWithImpTransm.length == _numContVioImpTransm);		
			assertTrue(_maxBrcVioImpTransm.length == _numContVioImpTransm);
			assertTrue(_VmVioImpAssoToMaxBrcVioImpTransm.length == _numContVioImpTransm);
			assertTrue(_idxTSMaxBrcVioImpTransm.length == _numContVioImpTransm);
			assertTrue(_maxVmVioImpTransm.length == _numContVioImpTransm);
			assertTrue(_BrcVioImpAssoToMaxVmVioImpTransm.length == _numContVioImpTransm);
			assertTrue(_idxTSMaxVmVioImpTransm.length == _numContVioImpTransm);
			assertTrue(_maxSumVioImpTransm.length == _numContVioImpTransm);
			assertTrue(_BrcVioImpAssoToMaxSumVioImpTransm.length == _numContVioImpTransm);
			assertTrue(_VmVioImpAssoToMaxSumVioImpTransm.length == _numContVioImpTransm);
			assertTrue(_idxTSMaxSumVioImpTransm.length == _numContVioImpTransm);
		}
	}

	/** Create a file for saving all the summation-based TS results. */
	public void initOutputAllTS()
	{
		String pathToFile = _pathAllTS + _fileNameAllTS;	
		if(_rank == 0) AuxFileXL.initFileWithTitle(pathToFile, _title, true);
	}
	/** Create a file for saving all the individual-based TS results. */
	public void initOutputAllTS_AllVio()
	{
		String pathToFile = _pathAllTS_AllVio + _fileNameAllTS_AllVio;	
		if(_rank == 0) AuxFileXL.initFileWithTitle(pathToFile, _title, true);
	}
	
	/** Note: it would use default path if setPath() method is not called or called after this method. */
	public void initOutputBestTS(String uri)
	{
		String rootDr = _pathBestTS + _fileNameBestTSTransm;	
		String rootDr2 = _pathBestTS + _fileNameBestTSGen;	
		if(_rank == 0) AuxFileXL.initFileWithTitle(rootDr, uri, true);
		if(_rank == 0) AuxFileXL.initFileWithTitle(rootDr2, uri, true);
	}
	/** Note: it would use default path if setPath() method is not called or called after this method. */
	public void outputBestTSGen(boolean isTSWork) throws PsseModelException
	{
		String rootDr = _pathBestTS;
		try
		{
  			//File BestTS_results = new File(rootDr+_fileNameBestTS);
   		    OutputStream resultFile = new FileOutputStream(rootDr + _fileNameBestTSGen, true);
   		    PrintStream outFile = new PrintStream (resultFile, true);
   		    
   		    String typeCont = "gen";
   		    outFile.print("  "+typeCont); 		    
   		    if (isTSWork == true)
   		    {
   	   			int idxCont = _idxContWithImpGen[_numContVioImpGen];
   	   			int frmBusNum = _gens.get(idxCont).getBus().getI();
   	   			int toBusNum = -1;
   	   		    outFile.print("  "+(idxCont + 1));
   	   		    outFile.print("  "+frmBusNum);
   	   		    outFile.print("  "+toBusNum);

   	   		    float sumBrcVio = _sumBrcVioOrigContWithImpGen[_numContVioImpGen];
   	   		    float sumVmVio = _sumVmVioOrigContWithImpGen[_numContVioImpGen];
   	   		    outFile.print("  "+sumBrcVio);
   	   		    outFile.print("  "+sumVmVio);

   	   			int idxBrcBestTS1 = _idxTSMaxBrcVioImpGen[_numContVioImpGen];
   	   			outputBrcInfo(outFile, idxBrcBestTS1);
   	   			float tmp1 = _maxBrcVioImpGen[_numContVioImpGen];
   	   			float tmp2 = _VmVioImpAssoToMaxBrcVioImpGen[_numContVioImpGen];
   	   		    float[] op2 = {tmp1, tmp2};
   	   		    outputElemFrmArrayPerct(outFile, op2);
   		
   	   			int idxBrcBestTS2 = _idxTSMaxVmVioImpGen[_numContVioImpGen];
   	   			outputBrcInfo(outFile, idxBrcBestTS2);
   	  			float tmp3 = _maxVmVioImpGen[_numContVioImpGen];
   	   			float tmp4 = _BrcVioImpAssoToMaxVmVioImpGen[_numContVioImpGen];
   	   		    float[] op3 = {tmp3, tmp4};
   	   		    outputElemFrmArrayPerct(outFile, op3);

   	   			int idxBrcBestTS3 = _idxTSMaxSumVioImpGen[_numContVioImpGen];
   	   			outputBrcInfo(outFile, idxBrcBestTS3);
  	   			float tmp56 = _maxSumVioImpGen[_numContVioImpGen];
   	   			float tmp5 = _BrcVioImpAssoToMaxSumVioImpGen[_numContVioImpGen];
   	   			float tmp6 = _VmVioImpAssoToMaxSumVioImpGen[_numContVioImpGen];
   	   		    float[] op4 = {tmp56, tmp5, tmp6};
   	   		    outputElemFrmArrayPerct(outFile, op4);
   	 		    System.out.println("    Output Best TS Data successfully");
   		    }
   		    else
   		    {
   	   			int idxCont = _idxContVioBadGen[_numContVioBadGen];
   	   			int frmBusNum = _gens.get(idxCont).getBus().getI();
   	   			int toBusNum = -1;
   	   		    outFile.print("  "+(idxCont + 1));
   	   		    outFile.print("  "+frmBusNum);
   	   		    outFile.print("  "+toBusNum);

   	   		    float sumBrcVio = _sumBrcVioOrigContVioBadGen[_numContVioBadGen];
   	   		    float sumVmVio = _sumVmVioOrigContVioBadGen[_numContVioBadGen];
   	   		    outFile.print("  "+sumBrcVio);
   	   		    outFile.print("  "+sumVmVio);

   		    	int[] tmp = {-1, -1, -1, 0, 0, -1, -1, -1, 0, 0, -1, -1, -1, 0, 0, 0};
   		    	outputElemFrmArrayPerct(outFile, tmp);
   	 		    System.out.println("    No benificial TS for this Gen contingency");
   		    }
   		    outFile.print("  "+_nproc);
   		    outFile.print("  "+_numTSCheckedGen[_numContiCheckRTGen-1]);
	   		outFile.print("  "+((System.nanoTime() - _startTimeOneTS)/1e9f));
   		    outFile.println();   		    
      		outFile.close();
       		resultFile.close();
		}
		catch (IOException e) {
	    	System.out.println("\nCannot write Best TS Data to file" + e);
	    	e.printStackTrace();
		}
	}
	
	public void outputBestTSTransm(boolean isTSWork) throws PsseModelException
	{
		String rootDr = _pathBestTS;
		try
		{
   		    OutputStream resultFile = new FileOutputStream(rootDr + _fileNameBestTSTransm, true);
   		    PrintStream outFile = new PrintStream (resultFile, true);
   		    
   		    String typeCont = "branch";
   		    outFile.print("  "+typeCont);
   		    if (isTSWork == true)
   		    {
   	   			int idxCont = _idxContWithImpTransm[_numContVioImpTransm];
   	   			outputBrcInfo(outFile, idxCont);

   	   		    float sumBrcVio = _sumBrcVioOrigContWithImpTransm[_numContVioImpTransm];
   	   		    float sumVmVio = _sumVmVioOrigContWithImpTransm[_numContVioImpTransm];
   	   		    outFile.print("  "+sumBrcVio);
   	   		    outFile.print("  "+sumVmVio);
   	   		    
   	   			int idxBrcBestTS1 = _idxTSMaxBrcVioImpTransm[_numContVioImpTransm];
   	   			outputBrcInfo(outFile, idxBrcBestTS1);
   	   			float tmp1 = _maxBrcVioImpTransm[_numContVioImpTransm];
   	   			float tmp2 = _VmVioImpAssoToMaxBrcVioImpTransm[_numContVioImpTransm];
   	   		    float[] op2 = {tmp1, tmp2};
   	   		    outputElemFrmArrayPerct(outFile, op2);
   		
   	   			int idxBrcBestTS2 = _idxTSMaxVmVioImpTransm[_numContVioImpTransm];
   	   			outputBrcInfo(outFile, idxBrcBestTS2);
   	  			float tmp3 = _maxVmVioImpTransm[_numContVioImpTransm];
   	   			float tmp4 = _BrcVioImpAssoToMaxVmVioImpTransm[_numContVioImpTransm];
   	   		    float[] op3 = {tmp3, tmp4};
   	   		    outputElemFrmArrayPerct(outFile, op3);
   	  			
   	   			int idxBrcBestTS3 = _idxTSMaxSumVioImpTransm[_numContVioImpTransm];
   	   			outputBrcInfo(outFile, idxBrcBestTS3);
   	   			float tmp56 = _maxSumVioImpTransm[_numContVioImpTransm];
   	   			float tmp5 = _BrcVioImpAssoToMaxSumVioImpTransm[_numContVioImpTransm];
   	   			float tmp6 = _VmVioImpAssoToMaxSumVioImpTransm[_numContVioImpTransm];
   	   		    float[] op4 = {tmp56, tmp5, tmp6};
   	   		    outputElemFrmArrayPerct(outFile, op4);
   	 		    System.out.println("    Output Best TS Data successfully");
   		    }
   		    else
   		    {
   	   			int idxCont = _idxContVioBadTransm[_numContVioBadTransm];
   	   			outputBrcInfo(outFile, idxCont);

   	   		    float sumBrcVio = _sumBrcVioOrigContVioBadTransm[_numContVioBadTransm];
   	   		    float sumVmVio = _sumVmVioOrigContVioBadTransm[_numContVioBadTransm];
   	   		    outFile.print("  "+sumBrcVio);
   	   		    outFile.print("  "+sumVmVio);
  	 	
   		    	int[] tmp = {-1, -1, -1, 0, 0, -1, -1, -1, 0, 0, -1, -1, -1, 0, 0, 0};
   		    	outputElemFrmArrayPerct(outFile, tmp);
   	 		    System.out.println("    No benificial TS for this Brc contingency");
   		    }
   		    outFile.print("  "+_nproc);
   		    outFile.print("  "+_numTSCheckedTransm[_numContiCheckRTTransm-1]);
	   		outFile.print("  "+((System.nanoTime() - _startTimeOneTS)/1e9f));
   		    outFile.println();   		    
      		outFile.close();
       		resultFile.close();
		}
		catch (IOException e) {
	    	System.out.println("\nCannot write Best TS Data to file" + e);
	    	e.printStackTrace();
		}
	}
	
	private void outputBrcInfo(PrintStream outFile, int idxBrc) throws PsseModelException
	{
		int frmBusNum = _branches.get(idxBrc).getFromBus().getI();
		int toBusNum = _branches.get(idxBrc).getToBus().getI();
	    outFile.print("  "+(idxBrc + 1));
	    outFile.print("  "+frmBusNum);
	    outFile.print("  "+toBusNum);
	}
	
	public void outputResults() throws PsseModelException
	{
		float timeSol = -1;
		outputResults(timeSol);
	}
	public void outputResults(float timeSol) throws PsseModelException
	{
		if (_rank == 0)
		{
			checkResults();
	    	System.out.println();
			String rootDr = _path;
			
			if (_title != null)
			{
				try
				{
		   		    OutputStream resultFile = new FileOutputStream(rootDr+"FilesChecked.txt", true);
		   		    PrintStream outFile = new PrintStream (resultFile, true);
		   		    outFile.println(_title);
		   		    
		   			resultFile.close();
		   			outFile.close();
		   			System.out.println(); System.out.println("Output filesChecked.txt to a file successfully.");
		   		} catch (IOException e) {
		   			System.out.println("\nFail to output CaGeneral Info to a file" + e);
		   			e.printStackTrace();
		   		}
			}

			try
			{
	  			//File TS_results = new File(rootDr+"TS_results.txt");
	   		    OutputStream resultFile = new FileOutputStream(rootDr+"results_TS.txt", true);
	   		    PrintStream outFile = new PrintStream (resultFile, true);
	   		    outFile.print("  "+timeSol); 
	   		    
	   		    float PloadTotal = 0;
	   		    float QloadTotal = 0;
	   		    boolean[] isBusInArea = null;
	   		    boolean checkArea = _model.getElemMnt().getCheckArea();   //consistent with violation monitor setting.
		    	if (checkArea == true) isBusInArea = _model.getElemMnt().getIsBusInArea();
	   		    LoadList loads = _model.getLoads();
	   		    for (Load load : loads)
	   		    {
	   		    	if (load.isInSvc())
	   		    	{
	   		    		boolean mark = true;
	   		    		if (checkArea == true)
	   		    		{
	   	   		    		int idxLoadBus = load.getBus().getIndex();
	   	   		    	    mark = isBusInArea[idxLoadBus];
	   		    		}
	   		    		if (mark == true)
	   		    		{
	   		    			PloadTotal += load.getPpu();
	   		    			QloadTotal += load.getQpu();
	   		    		}
	   		    	}
	   		    }
	   		    outFile.print("  "+PloadTotal); 
	   		    outFile.print("  "+QloadTotal); 
	   		    
	   		    outFile.print("  "+(_numContAlreadyBeenCheckedTransm + _numContAlreadyBeenCheckedGen));
	   		    outFile.print("  "+(_numContVioWithinTolTransm + _numContVioWithinTolGen));
	   		    outFile.print("  "+(_numContVioWithinBrcTolTransm + _numContVioWithinBrcTolGen));
	   		    outFile.print("  "+(_numContVioWithinVmTolTransm + _numContVioWithinVmTolGen));	       		    
	   		    outFile.print("  "+(_numContVioBadTransm + _numContVioBadGen));
	   		    outFile.print("  "+(_numContVioToNoVioTransm + _numContVioToNoVioGen));

	   		    float aa = 0;
	   		    for(int i=0; i<_numContVioToNoVioTransm; i++)
	   		    {
	   		    	aa += _numGoodTStoNoVioTransm[i];
	   		    }
	   		    for(int i=0; i<_numContVioToNoVioGen; i++)
	   		    {
	   		    	aa += _numGoodTStoNoVioGen[i];
	   		    }
	   		    aa = aa / (_numContVioToNoVioTransm + _numContVioToNoVioGen);
	   		    if ((_numContVioToNoVioTransm + _numContVioToNoVioGen) == 0) aa = 0;
	   		    	
	   		    float bb = 0;
	   		    for(int i=0; i<_numContVioToRedVioTransm; i++)
	   		    {
	   		    	bb += _numGoodTStoRedVioTransm[i];
	   		    }
	   		    for(int i=0; i<_numContVioToRedVioGen; i++)
	   		    {
	   		    	bb += _numGoodTStoRedVioGen[i];
	   		    }
	   		    bb = bb / (_numContVioToRedVioTransm + _numContVioToRedVioGen);
	   		    if ((_numContVioToRedVioTransm + _numContVioToRedVioGen) == 0) bb = 0;
	   		    
	   		    outFile.print("  "+aa);
	   		    outFile.print("  "+(_numContVioToRedVioTransm + _numContVioToRedVioGen));
	   		    outFile.print("  "+bb);
	   		    
	   		    float[] qw1 = AuxArrayXL.connectTwoArray(_maxBrcVioImpTransm, _maxBrcVioImpGen);
	   		    float[] qw2 = AuxArrayXL.connectTwoArray(_VmVioImpAssoToMaxBrcVioImpTransm, _VmVioImpAssoToMaxBrcVioImpGen);
	   		    float[] qw3 = AuxArrayXL.connectTwoArray(_maxVmVioImpTransm, _maxVmVioImpGen);
	   		    float[] qw4 = AuxArrayXL.connectTwoArray(_BrcVioImpAssoToMaxVmVioImpTransm, _BrcVioImpAssoToMaxVmVioImpGen);
	   		    float[] qw56 = AuxArrayXL.connectTwoArray(_maxSumVioImpTransm, _maxSumVioImpGen);	 
	   		    float[] qw5 = AuxArrayXL.connectTwoArray(_BrcVioImpAssoToMaxSumVioImpTransm, _BrcVioImpAssoToMaxSumVioImpGen);
	   		    float[] qw6 = AuxArrayXL.connectTwoArray(_VmVioImpAssoToMaxSumVioImpTransm, _VmVioImpAssoToMaxSumVioImpGen);
	   		    
	   		    int[] idx1 = AuxArrayXL.getIdxMinMax(qw1);
	   		    int[] idx3 = AuxArrayXL.getIdxMinMax(qw3);
	   		    int[] idx56 = AuxArrayXL.getIdxMinMax(qw56);
	   		    
	   		    float avg1 = AuxArrayXL.calculateAverageOfArray(qw1);
	   		    float avg2 = AuxArrayXL.calculateAverageOfArray(qw2);
	   		    float avg3 = AuxArrayXL.calculateAverageOfArray(qw3);
	   		    float avg4 = AuxArrayXL.calculateAverageOfArray(qw4);
	   		    float avg56 = AuxArrayXL.calculateAverageOfArray(qw56);
	   		    float avg5 = AuxArrayXL.calculateAverageOfArray(qw5);
	   		    float avg6 = AuxArrayXL.calculateAverageOfArray(qw6);
	   		    
	   		    float[] tmp = new float[21];
	   		    if ((_numContVioImpGen + _numContVioImpTransm) != 0)
	   		    {
	   		    	float[] tmp2 = {
	   	   		    		qw1[idx1[0]], qw2[idx1[0]], qw3[idx3[0]], qw4[idx3[0]], qw56[idx56[0]], qw5[idx56[0]], qw6[idx56[0]], 
	   	   		    		qw1[idx1[1]], qw2[idx1[1]], qw3[idx3[1]], qw4[idx3[1]], qw56[idx56[1]], qw5[idx56[1]], qw6[idx56[1]], 
	   	   		    		avg1, avg2, avg3, avg4, avg56, avg5, avg6 };
	   		    	tmp = tmp2;
	   		    }
	   		    outputElemFrmArrayPerct(outFile, tmp);
	   		    
	   		    // excludes all zero elements
	   		    {
	   	   		    int[] idx11 = AuxArrayXL.getIdxMinMaxIgnoreZero(qw1);
	   	   		    int[] idx33 = AuxArrayXL.getIdxMinMaxIgnoreZero(qw3);
	   	   		    int[] idx5656 = AuxArrayXL.getIdxMinMaxIgnoreZero(qw56);
	   	   		    
	   	   		    float gh1 = 0;
	   	   		    float gh2 = 0;
	   	   		    float gh3 = 0;
	   	   		    float gh4 = 0;
	   	   		    float gh56 = 0;
	   	   		    float gh5 = 0;
	   	   		    float gh6 = 0;
	   	   		    
	   	   		    float rt1 = 0;
	   	   		    float rt2 = 0;
	   	   		    float rt3 = 0;
	   	   		    float rt4 = 0;
	   	   		    float rt56 = 0;
	   	   		    float rt5 = 0;
	   	   		    float rt6 = 0;
	   	   		    
	   	   		    if (idx11[0] >= 0)
	   	   		    {
	   	   		    	assertTrue(idx11[1] >= 0);
	   	   		    	gh1 = qw1[idx11[0]];
	   	   		    	gh2 = qw2[idx11[0]];
	   	   		    	rt1 = qw1[idx11[1]];
	   	   		    	rt2 = qw2[idx11[1]];
	   	   		    }
	   	   		    if (idx33[0] >= 0)
	   	   		    {
	   	   		    	assertTrue(idx33[1] >= 0);
	   	   		    	gh3 = qw3[idx33[0]];
	   	   		    	gh4 = qw4[idx33[0]];
	   	   		    	rt3 = qw3[idx33[1]];
	   	   		    	rt4 = qw4[idx33[1]];
	   	   		    }
	   	   		    if (idx5656[0] >= 0)
	   	   		    {
	   	   		    	assertTrue(idx5656[1] >= 0);
	   	   		    	gh56 = qw56[idx5656[0]];
	   	   		    	gh5 = qw5[idx5656[0]];
	   	   		    	gh6 = qw6[idx5656[0]];
	   	   		    	rt56 = qw56[idx5656[1]];
	   	   		    	rt5 = qw5[idx5656[1]];
	   	   		    	rt6 = qw6[idx5656[1]];
	   	   		    }
	   	   		    
	   	   		    float avg11 = AuxArrayXL.calculateAverageOfArrayIgnoreZero(qw1);
	   	   		    float avg22 = AuxArrayXL.calculateAverageOfArrayIgnoreZero(qw2);
	   	   		    float avg33 = AuxArrayXL.calculateAverageOfArrayIgnoreZero(qw3);
	   	   		    float avg44 = AuxArrayXL.calculateAverageOfArrayIgnoreZero(qw4);
	   	   		    float avg5656 = AuxArrayXL.calculateAverageOfArrayIgnoreZero(qw56);
	   	   		    float avg55 = AuxArrayXL.calculateAverageOfArrayIgnoreZero(qw5);
	   	   		    float avg66 = AuxArrayXL.calculateAverageOfArrayIgnoreZero(qw6);
	   	   		    
	   	   		    float[] tmp2 = {
	   	   		    		gh1, gh2, gh3, gh4, gh56, gh5, gh6,
	   	   		    		rt1, rt2, rt3, rt4, rt56, rt5, rt6,
	   	   		    		avg11, avg22, avg33, avg44, avg5656, avg55, avg66 };   	   		    
	   	   		    outputElemFrmArrayPerct(outFile, tmp2);
	   		    }  		    
	   		    outFile.println();	    
	      		outFile.close();
	       		resultFile.close();
	 		    System.out.println("Output Transm Swit Data successfully");
			}
			catch (IOException e) {
		    	System.out.println("\nCannot write Transm Swit Data to file" + e);
		    	e.printStackTrace();
			}
			
			try
			{
	  			//File TS_results_Gen = new File(rootDr+"TS_results_Gen.txt");
	   		    OutputStream resultFile = new FileOutputStream(rootDr+"results_TS_Gen.txt", true);
	   		    PrintStream outFile = new PrintStream (resultFile, true);
	   		    
	   		    outFile.print("  "+_numContAlreadyBeenCheckedGen);
	   		    outFile.print("  "+_numContVioWithinTolGen);
	   		    outFile.print("  "+(_numContVioWithinBrcTolGen));
	   		    outFile.print("  "+(_numContVioWithinVmTolGen));
	   		    outFile.print("  "+_numContVioBadGen);
	   		    outFile.print("  "+_numContVioToNoVioGen);

	   		    float aa = 0;
	   		    for(int i=0; i<_numContVioToNoVioGen; i++)
	   		    {
	   		    	aa += _numGoodTStoNoVioGen[i];
	   		    }
	   		    aa = aa / (_numContVioToNoVioGen);
	   		    if (_numContVioToNoVioGen == 0) aa = 0;
	   		    
	   		    float bb = 0;
	   		    for(int i=0; i<_numContVioToRedVioGen; i++)
	   		    {
	   		    	bb += _numGoodTStoRedVioGen[i];
	   		    }
	   		    bb = bb / (_numContVioToRedVioGen);
	   		    if (_numContVioToRedVioGen == 0) bb = 0;
	   		    
	   		    outFile.print("  "+aa);
	   		    outFile.print("  "+_numContVioToRedVioGen);
	   		    outFile.print("  "+bb);
	   		    
	   		    float[] maxBrcVioImpGen = { 0f };
	   		    float[] VmVioImpAssoToMaxBrcVioImpGen = { 0f };
	   		    float[] maxVmVioImpGen = { 0f };
	   		    float[] BrcVioImpAssoToMaxVmVioImpGen = { 0f };
	   		    float[] maxSumVioImpGen = { 0f };
	   		    float[] BrcVioImpAssoToMaxSumVioImpGen = { 0f };
	   		    float[] VmVioImpAssoToMaxSumVioImpGen = { 0f };
	   		    if(_maxBrcVioImpGen != null) {maxBrcVioImpGen = _maxBrcVioImpGen;}
	   		    if(_VmVioImpAssoToMaxBrcVioImpGen != null) {VmVioImpAssoToMaxBrcVioImpGen = _VmVioImpAssoToMaxBrcVioImpGen;}
	   		    if(_maxVmVioImpGen != null) {maxVmVioImpGen = _maxVmVioImpGen;}
	   		    if(_BrcVioImpAssoToMaxVmVioImpGen != null) {BrcVioImpAssoToMaxVmVioImpGen = _BrcVioImpAssoToMaxVmVioImpGen;}
	   		    if(_maxSumVioImpGen != null) {maxSumVioImpGen = _maxSumVioImpGen;}
	   		    if(_BrcVioImpAssoToMaxSumVioImpGen != null) {BrcVioImpAssoToMaxSumVioImpGen = _BrcVioImpAssoToMaxSumVioImpGen;}
	   		    if(_VmVioImpAssoToMaxSumVioImpGen != null) {VmVioImpAssoToMaxSumVioImpGen = _VmVioImpAssoToMaxSumVioImpGen;}
	   		    
	   		    int[] idx1 = AuxArrayXL.getIdxMinMax(maxBrcVioImpGen);
	   		    int[] idx3 = AuxArrayXL.getIdxMinMax(maxVmVioImpGen);
	   		    int[] idx56 = AuxArrayXL.getIdxMinMax(maxSumVioImpGen);
	   		    
	   		    float avg1 = AuxArrayXL.calculateAverageOfArray(maxBrcVioImpGen);
	   		    float avg2 = AuxArrayXL.calculateAverageOfArray(VmVioImpAssoToMaxBrcVioImpGen);
	   		    float avg3 = AuxArrayXL.calculateAverageOfArray(maxVmVioImpGen);
	   		    float avg4 = AuxArrayXL.calculateAverageOfArray(BrcVioImpAssoToMaxVmVioImpGen);
	   		    float avg56 = AuxArrayXL.calculateAverageOfArray(maxSumVioImpGen);
	   		    float avg5 = AuxArrayXL.calculateAverageOfArray(BrcVioImpAssoToMaxSumVioImpGen);
	   		    float avg6 = AuxArrayXL.calculateAverageOfArray(VmVioImpAssoToMaxSumVioImpGen);
	   		    
	   		    float[] tmp = {
	   		    		maxBrcVioImpGen[idx1[0]], VmVioImpAssoToMaxBrcVioImpGen[idx1[0]], 
	   		    		maxVmVioImpGen[idx3[0]],  BrcVioImpAssoToMaxVmVioImpGen[idx3[0]], 
	   		    		maxSumVioImpGen[idx56[0]], BrcVioImpAssoToMaxSumVioImpGen[idx56[0]], VmVioImpAssoToMaxSumVioImpGen[idx56[0]],   		    		
	   		    		maxBrcVioImpGen[idx1[1]], VmVioImpAssoToMaxBrcVioImpGen[idx1[1]], 
	   		    		maxVmVioImpGen[idx3[1]], BrcVioImpAssoToMaxVmVioImpGen[idx3[1]], 
	   		    		maxSumVioImpGen[idx56[1]],	BrcVioImpAssoToMaxSumVioImpGen[idx56[1]], VmVioImpAssoToMaxSumVioImpGen[idx56[1]],   		    		
	   		    		avg1, avg2, avg3, avg4, avg56, avg5, avg6 };  		    
	   		    outputElemFrmArrayPerct(outFile, tmp);

	   		    // output data excludes all zeros elements.
	   		    {
	   	   		    float[] qw1 = maxBrcVioImpGen;
	   	   		    float[] qw2 = VmVioImpAssoToMaxBrcVioImpGen;
	   	   		    float[] qw3 = maxVmVioImpGen;
	   	   		    float[] qw4 = BrcVioImpAssoToMaxVmVioImpGen;
	   	   		    float[] qw56 = maxSumVioImpGen;
	   	   		    float[] qw5 = BrcVioImpAssoToMaxSumVioImpGen;
	   	   		    float[] qw6 = VmVioImpAssoToMaxSumVioImpGen;

	   	   		    int[] idx11 = AuxArrayXL.getIdxMinMaxIgnoreZero(qw1);
	   	   		    int[] idx33 = AuxArrayXL.getIdxMinMaxIgnoreZero(qw3);
	   	   		    int[] idx5656 = AuxArrayXL.getIdxMinMaxIgnoreZero(qw56);

	   	   		    float gh1 = 0;
	   	   		    float gh2 = 0;
	   	   		    float gh3 = 0;
	   	   		    float gh4 = 0;
	   	   		    float gh56 = 0;
	   	   		    float gh5 = 0;
	   	   		    float gh6 = 0;
	   	   		    
	   	   		    float rt1 = 0;
	   	   		    float rt2 = 0;
	   	   		    float rt3 = 0;
	   	   		    float rt4 = 0;
	   	   		    float rt56 = 0;
	   	   		    float rt5 = 0;
	   	   		    float rt6 = 0;
	   	   		    
	   	   		    if (idx11[0] >= 0)
	   	   		    {
	   	   		    	assertTrue(idx11[1] >= 0);
	   	   		    	gh1 = qw1[idx11[0]];
	   	   		    	gh2 = qw2[idx11[0]];
	   	   		    	rt1 = qw1[idx11[1]];
	   	   		    	rt2 = qw2[idx11[1]];
	   	   		    }
	   	   		    if (idx33[0] >= 0)
	   	   		    {
	   	   		    	assertTrue(idx33[1] >= 0);
	   	   		    	gh3 = qw3[idx33[0]];
	   	   		    	gh4 = qw4[idx33[0]];
	   	   		    	rt3 = qw3[idx33[1]];
	   	   		    	rt4 = qw4[idx33[1]];
	   	   		    }
	   	   		    if (idx5656[0] >= 0)
	   	   		    {
	   	   		    	assertTrue(idx5656[1] >= 0);
	   	   		    	gh56 = qw56[idx5656[0]];
	   	   		    	gh5 = qw5[idx5656[0]];
	   	   		    	gh6 = qw6[idx5656[0]];
	   	   		    	rt56 = qw56[idx5656[1]];
	   	   		    	rt5 = qw5[idx5656[1]];
	   	   		    	rt6 = qw6[idx5656[1]];
	   	   		    }
	   	   		    
	   	   		    float avg11 = AuxArrayXL.calculateAverageOfArrayIgnoreZero(qw1);
	   	   		    float avg22 = AuxArrayXL.calculateAverageOfArrayIgnoreZero(qw2);
	   	   		    float avg33 = AuxArrayXL.calculateAverageOfArrayIgnoreZero(qw3);
	   	   		    float avg44 = AuxArrayXL.calculateAverageOfArrayIgnoreZero(qw4);
	   	   		    float avg5656 = AuxArrayXL.calculateAverageOfArrayIgnoreZero(qw56);
	   	   		    float avg55 = AuxArrayXL.calculateAverageOfArrayIgnoreZero(qw5);
	   	   		    float avg66 = AuxArrayXL.calculateAverageOfArrayIgnoreZero(qw6);
	   	   		    
	   	   		    float[] tmp2 = {
	   	   		    		gh1, gh2, gh3, gh4, gh56, gh5, gh6,
	   	   		    		rt1, rt2, rt3, rt4, rt56, rt5, rt6,
	   	   		    		avg11, avg22, avg33, avg44, avg5656, avg55, avg66 };   	   		    
	   	   		    outputElemFrmArrayPerct(outFile, tmp2);
	   		    }
	   		    outFile.println();   		    
	      		outFile.close();
	       		resultFile.close();
	 		    System.out.println("Output Transm Swit Data for Gen Cont successfully");
			}
			catch (IOException e) {
		    	System.out.println("\nCannot write Transm Swit Data for Gen Cont to file" + e);
		    	e.printStackTrace();
			}

			try
			{		    
	  			//File TS_results_Transm = new File(rootDr+"TS_results_Transm.txt");
	   		    OutputStream resultFile = new FileOutputStream(rootDr+"results_TS_Transm.txt", true);
	   		    PrintStream outFile = new PrintStream (resultFile, true);
	   		    
	   		    outFile.print("  "+_numContAlreadyBeenCheckedTransm);
	   		    outFile.print("  "+_numContVioWithinTolTransm);
	   		    outFile.print("  "+(_numContVioWithinBrcTolTransm));
	   		    outFile.print("  "+(_numContVioWithinVmTolTransm)); 		    
	   		    outFile.print("  "+_numContVioBadTransm );
	   		    outFile.print("  "+_numContVioToNoVioTransm);

	   		    float aa = 0;
	   		    for(int i=0; i<_numContVioToNoVioTransm; i++)
	   		    {
	   		    	aa += _numGoodTStoNoVioTransm[i];
	   		    }
	   		    aa = aa / (_numContVioToNoVioTransm);
	   		    if (_numContVioToNoVioTransm == 0) aa = 0;
	   		    
	   		    float bb = 0;
	   		    for(int i=0; i<_numContVioToRedVioTransm; i++)
	   		    {
	   		    	bb += _numGoodTStoRedVioTransm[i];
	   		    }
	   		    bb = bb / (_numContVioToRedVioTransm );
	   		    if (_numContVioToRedVioTransm == 0) bb = 0;
	   		    
	   		    outFile.print("  "+aa);
	   		    outFile.print("  "+_numContVioToRedVioTransm);
	   		    outFile.print("  "+bb);
	   		    
	   		    float[] maxBrcVioImpTransm = { 0f };
	   		    float[] VmVioImpAssoToMaxBrcVioImpTransm = { 0f };
	   		    float[] maxVmVioImpTransm = { 0f };
	   		    float[] BrcVioImpAssoToMaxVmVioImpTransm = { 0f };
	   		    float[] maxSumVioImpTransm = { 0f };
	   		    float[] BrcVioImpAssoToMaxSumVioImpTransm = { 0f };
	   		    float[] VmVioImpAssoToMaxSumVioImpTransm = { 0f };
	   		    if(_maxBrcVioImpTransm != null) {maxBrcVioImpTransm = _maxBrcVioImpTransm;}
	   		    if(_VmVioImpAssoToMaxBrcVioImpTransm != null) {VmVioImpAssoToMaxBrcVioImpTransm = _VmVioImpAssoToMaxBrcVioImpTransm;}
	   		    if(_maxVmVioImpTransm != null) {maxVmVioImpTransm = _maxVmVioImpTransm;}
	   		    if(_BrcVioImpAssoToMaxVmVioImpTransm != null) {BrcVioImpAssoToMaxVmVioImpTransm = _BrcVioImpAssoToMaxVmVioImpTransm;}
	   		    if(_maxSumVioImpTransm != null) {maxSumVioImpTransm = _maxSumVioImpTransm;}
	   		    if(_BrcVioImpAssoToMaxSumVioImpTransm != null) {BrcVioImpAssoToMaxSumVioImpTransm = _BrcVioImpAssoToMaxSumVioImpTransm;}
	   		    if(_VmVioImpAssoToMaxSumVioImpTransm != null) {VmVioImpAssoToMaxSumVioImpTransm = _VmVioImpAssoToMaxSumVioImpTransm;}
	   		       		    
	   		    int[] idx1 = AuxArrayXL.getIdxMinMax(maxBrcVioImpTransm);
	   		    int[] idx3 = AuxArrayXL.getIdxMinMax(maxVmVioImpTransm);
	   		    int[] idx56 = AuxArrayXL.getIdxMinMax(maxSumVioImpTransm);
		    
	   		    float avg1 = AuxArrayXL.calculateAverageOfArray(maxBrcVioImpTransm);
	   		    float avg2 = AuxArrayXL.calculateAverageOfArray(VmVioImpAssoToMaxBrcVioImpTransm);
	   		    float avg3 = AuxArrayXL.calculateAverageOfArray(maxVmVioImpTransm);
	   		    float avg4 = AuxArrayXL.calculateAverageOfArray(BrcVioImpAssoToMaxVmVioImpTransm);
	   		    float avg56 = AuxArrayXL.calculateAverageOfArray(maxSumVioImpTransm);
	   		    float avg5 = AuxArrayXL.calculateAverageOfArray(BrcVioImpAssoToMaxSumVioImpTransm);
	   		    float avg6 = AuxArrayXL.calculateAverageOfArray(VmVioImpAssoToMaxSumVioImpTransm);
	   		    
	   		    float[] tmp = {
	   		    		maxBrcVioImpTransm[idx1[0]], VmVioImpAssoToMaxBrcVioImpTransm[idx1[0]], 
	   		    		maxVmVioImpTransm[idx3[0]], BrcVioImpAssoToMaxVmVioImpTransm[idx3[0]], 
	   		    		maxSumVioImpTransm[idx56[0]], BrcVioImpAssoToMaxSumVioImpTransm[idx56[0]], VmVioImpAssoToMaxSumVioImpTransm[idx56[0]],   		    		
	   		    		maxBrcVioImpTransm[idx1[1]], VmVioImpAssoToMaxBrcVioImpTransm[idx1[1]], 
	   		    		maxVmVioImpTransm[idx3[1]], BrcVioImpAssoToMaxVmVioImpTransm[idx3[1]],
	   		    		maxSumVioImpTransm[idx56[1]], BrcVioImpAssoToMaxSumVioImpTransm[idx56[1]], VmVioImpAssoToMaxSumVioImpTransm[idx56[1]],   		    		
	   		    		avg1, avg2, avg3, avg4, avg56, avg5, avg6 }; 		    
	   		    outputElemFrmArrayPerct(outFile, tmp);

	   		    // output data excludes all zeros elements.
	   		    {
	   	   		    float[] qw1 = maxBrcVioImpTransm;
	   	   		    float[] qw2 = VmVioImpAssoToMaxBrcVioImpTransm;
	   	   		    float[] qw3 = maxVmVioImpTransm;
	   	   		    float[] qw4 = BrcVioImpAssoToMaxVmVioImpTransm;
	   	   		    float[] qw56 = maxSumVioImpTransm;
	   	   		    float[] qw5 = BrcVioImpAssoToMaxSumVioImpTransm;
	   	   		    float[] qw6 = VmVioImpAssoToMaxSumVioImpTransm;

	   	   		    int[] idx11 = AuxArrayXL.getIdxMinMaxIgnoreZero(qw1);
	   	   		    int[] idx33 = AuxArrayXL.getIdxMinMaxIgnoreZero(qw3);
	   	   		    int[] idx5656 = AuxArrayXL.getIdxMinMaxIgnoreZero(qw56);

	   	   		    float gh1 = 0;
	   	   		    float gh2 = 0;
	   	   		    float gh3 = 0;
	   	   		    float gh4 = 0;
	   	   		    float gh56 = 0;
	   	   		    float gh5 = 0;
	   	   		    float gh6 = 0;
	   	   		    
	   	   		    float rt1 = 0;
	   	   		    float rt2 = 0;
	   	   		    float rt3 = 0;
	   	   		    float rt4 = 0;
	   	   		    float rt56 = 0;
	   	   		    float rt5 = 0;
	   	   		    float rt6 = 0;
	   	   		    
	   	   		    if (idx11[0] >= 0)
	   	   		    {
	   	   		    	assertTrue(idx11[1] >= 0);
	   	   		    	gh1 = qw1[idx11[0]];
	   	   		    	gh2 = qw2[idx11[0]];
	   	   		    	rt1 = qw1[idx11[1]];
	   	   		    	rt2 = qw2[idx11[1]];
	   	   		    }
	   	   		    if (idx33[0] > 0)
	   	   		    {
	   	   		    	assertTrue(idx33[1] >= 0);
	   	   		    	gh3 = qw3[idx33[0]];
	   	   		    	gh4 = qw4[idx33[0]];
	   	   		    	rt3 = qw3[idx33[1]];
	   	   		    	rt4 = qw4[idx33[1]];
	   	   		    }
	   	   		    if (idx5656[0] >= 0)
	   	   		    {
	   	   		    	assertTrue(idx5656[1] >= 0);
	   	   		    	gh56 = qw56[idx5656[0]];
	   	   		    	gh5 = qw5[idx5656[0]];
	   	   		    	gh6 = qw6[idx5656[0]];
	   	   		    	rt56 = qw56[idx5656[1]];
	   	   		    	rt5 = qw5[idx5656[1]];
	   	   		    	rt6 = qw6[idx5656[1]];
	   	   		    }
	   	   		    
	   	   		    float avg11 = AuxArrayXL.calculateAverageOfArrayIgnoreZero(qw1);
	   	   		    float avg22 = AuxArrayXL.calculateAverageOfArrayIgnoreZero(qw2);
	   	   		    float avg33 = AuxArrayXL.calculateAverageOfArrayIgnoreZero(qw3);
	   	   		    float avg44 = AuxArrayXL.calculateAverageOfArrayIgnoreZero(qw4);
	   	   		    float avg5656 = AuxArrayXL.calculateAverageOfArrayIgnoreZero(qw56);
	   	   		    float avg55 = AuxArrayXL.calculateAverageOfArrayIgnoreZero(qw5);
	   	   		    float avg66 = AuxArrayXL.calculateAverageOfArrayIgnoreZero(qw6);
	   	   		    
	   	   		    float[] tmp2 = {
	   	   		    		gh1, gh2, gh3, gh4, gh56, gh5, gh6,
	   	   		    		rt1, rt2, rt3, rt4, rt56, rt5, rt6,
	   	   		    		avg11, avg22, avg33, avg44, avg5656, avg55, avg66 };  	   		    
	   	   		    outputElemFrmArrayPerct(outFile, tmp2);
	   		    }
	   		    outFile.println(); 		    
	      		outFile.close();
	       		resultFile.close();
	 		    System.out.println("Output Transm Swit Data for Transm Cont successfully");
			}
			catch (IOException e) {
		    	System.out.println("\nCannot write Transm Swit Data for Transm Cont to file" + e);
		    	e.printStackTrace();
			}
	    	System.out.println();
		}
	}
	
	void outputElemFrmArrayPerct(PrintStream outFile, float[] a)
	{
		for (int i=0; i<a.length; i++)
			outFile.print("  "+(a[i]*100)+"%");
	}

	void outputElemFrmArrayPerct(PrintStream outFile, int[] a)
	{
		for (int i=0; i<a.length; i++)
			outFile.print("  "+(a[i]*100)+"%");
	}

		
    @Test
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
		
		InitPsseModel initModel = new InitPsseModel(model);
		initModel.initSetting();
		initModel.initNBestTS(uri);
		model.getNBestTSReport_NoTitle().setFileNameTransm("NoTitle_BestTS_Transm");
		model.getNBestTSReport_NoTitle().setFileNameGen("NoTitle_BestTS_Gen");
		model.getNBestTSReport_NoTitle().initPrint();

		float[] vmBasePf;
		float[] vaBasePf;
//		int[] idxBrcContToBeChecked;
//		int[] idxGenContToBeChecked;
		int[] idxBrcContToBeChecked = new int[] {158, 159, 169, 2019, 2270, 2388, 2810};
		int[] idxGenContToBeChecked = new int[] {30, 204, 244};
		{
			ContingencyAnalysisSeq ca = new ContingencyAnalysisSeq(model, vstart);
			ca.setMarkUsingVgSched(true); //ca.setRealTimeStartForGenBus(false);
			ca.setShowRunInfo(false);
			ca.convOR();
	/*		
			long t_StartCA = System.nanoTime();
			System.out.println("\nRunning contingency analysis...");
			System.out.println("    Running Generator contingency analysis...");
		    ca.runGenContingencyAnalysis();
			System.out.println("    Generator contingency analysis is finished.");
			System.out.println("      Gen CA time : " + (System.nanoTime() - t_StartCA)/1e9f);
			
			long t_StartTmp = System.nanoTime();
			System.out.println("    Running Transmission contingency analysis...");
		    ca.runTransmContingencyAnalysis();
			System.out.println("    Transmission contingency analysis is finished.");
			System.out.println("      Brc CA time : " + (System.nanoTime() - t_StartTmp)/1e9f);
			System.out.println("    Total CA time : " + (System.nanoTime() - t_StartCA)/1e9f);
			ca.outputVioInfoGenCont();
			ca.outputVioInfoTransmCont();
			idxBrcContToBeChecked = ca.getIdxContVioVmAndBrcAllTransm();
			idxGenContToBeChecked = ca.getIdxContVioVmAndBrcAllGen(); 
	*/		
			vmBasePf = ca.getVmBasePf();
			vaBasePf = ca.getVaBasePf();
		} 
		//idxBrcContToBeChecked = new int[] {};
		//idxGenContToBeChecked = new int[] {};

		System.out.println("\nNumber of Generator    Contingency to be checked with TS is: "+idxGenContToBeChecked.length);
		System.out.println("Number of Transmission Contingency to be checked with TS is: "+idxBrcContToBeChecked.length);
		
		CorrectiveTransmSwit engineCTS = new CorrectiveTransmSwit(model);
		engineCTS.setOutputAllTS_AllVio(true, engineCTS.getPathAllTS_AllVio() + model.getFileName() + "/"); 
		engineCTS.setOutputAllTS(true, engineCTS.getPathAllTS() + model.getFileName() + "/");     
		
		engineCTS.setContToCheckTransm(idxBrcContToBeChecked);
		engineCTS.setContToCheckGen(idxGenContToBeChecked);
		//engineCTS.setMarkUsingVgSched(true);
		engineCTS.initial(t_Start, uri, VoltageSource.LastSolved, vmBasePf, vaBasePf);
	
		engineCTS.runTS();
		float timeSol = (System.nanoTime() - t_Start)/1e9f;
		engineCTS.analyzeData();
		engineCTS.outputResults(timeSol);
		
		System.out.println("    Total time : " + (System.nanoTime() - t_Start)/1e9f);		
	}

}
