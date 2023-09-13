package com.rtca_cts.transmissionswitching;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import com.powerdata.openpa.psse.PsseModel;
import com.powerdata.openpa.psse.PsseModelException;
import com.powerdata.openpa.psse.powerflow.VoltageSource;
import com.rtca_cts.contingencyanalysis.VioResult;
import com.utilxl.array.AuxArrayXL;
import com.utilxl.tools.para.MPJExpressXL;

/**
 * Transmission switching - parallel version;
 * Parallel the ranking list. 
 * 
 * Initialized in Sep. 2014.
 * 
 * @author Xingpeng.Li, xplipower@gmail.com 
 */
public class TransmSwitParallel extends TransmSwit
{

	float[][] _sumVioTSPara;      // sum of violation for each TS checked, two elems: _sumBrcVio, _sumVmVio.
	float[][] _sumVioTSImpPara;      // sum of violation for each TS checked, two elems: _sumBrcVio, _sumVmVio.
	int[][] _numVioTSPara;           // number of violation for each TS checked, two elems: _numBrcVio, _numVmVio.
	int[][] _indicatorIndiElemVioCheckPara;

	TransmSwitParallel(PsseModel model) throws PsseModelException 
	{
		super(model);
	}

	TransmSwitParallel(PsseModel model, VoltageSource vstart) throws PsseModelException
	{
		super(model, vstart);
	}
	
	TransmSwitParallel(PsseModel model, VoltageSource vstart, float[] vmBasePf, float[] vaBasePf) throws PsseModelException
	{
		super(model, vstart, vmBasePf, vaBasePf);
	}
	
	@Override
	public void setThreadInfo(int nproc, int rank) {_nproc = nproc; _rank = rank;}

	/** Launch Transmission switching */
	@Override
	public void launchTS() throws PsseModelException, IOException
	{
		buildPrintStream();
		_withinTol = false;
		boolean conv;
		if (_checked == true) conv = _convPf;
		else conv = runContiPf();

		if (conv == true)
		{
//			System.out.println("pre_LaunchTS() -  _rank: "+_rank);
			if (_rank == 0) System.out.println("        The summation of flow    violation: "+_sumContiBrcVio);
			if (_rank == 0) System.out.println("        The summation of voltage violation: "+_sumContiVmVio);
			if (_sumContiVmVio < _tolSumVm && _sumContiBrcVio < _tolSumMVA)
			{
				_withinTol = true;
				if (_rank == 0) System.out.println("    The original contingency violation is within tolerance");
				if (_rank == 0) System.out.println("    Transm Swit won't be implemented...");
				if (_rank == 0 && _outputAllTS == true) dumpContiNotCritical(_outFile, 13);
				if (_outputAllTS_AllVio == true) {_outFile_AllVio.print(" "+_markConti);dumpContiNotCritical(_outFile_AllVio, _numTcol-6);}
			}
			else {runTransmSwit(); if(_rank == 0) writeNBestTS();
			    if(_rank == 0) {
		            ResultReportForStabilityCheck report = new ResultReportForStabilityCheck(this);
		            report.launchReport();
			    }
			}
		}
		else
		{
//			System.out.println("quit_LaunchTS() -  _rank: "+_rank);
			if (_rank == 0 && _outputAllTS == true) dumpContiNotConv(_outFile, 15);
			if (_outputAllTS_AllVio == true) {_outFile_AllVio.print(" "+_markConti);dumpContiNotConv(_outFile_AllVio, _numTcol-4);}
		}
		closePrintStream();
	}

	public void runTransmSwit() throws PsseModelException, IOException
	{
		if (_outputAllTS_AllVio == true) dumpContiAllVio();
		if (_rank == 0) System.out.println("    Running Transm Swit...");
		long t_Start = System.nanoTime();
        _RateC = getRateC();
        
		int numTS = determineTSList();
		MPJExpressXL paraMPJ = new MPJExpressXL(_nproc, _rank);
		int[] tmp = paraMPJ.assignWorkPara(numTS);
		int iStart = tmp[0];
		int iEnd = tmp[1] + 1;
		int nIter = tmp[2];
//		System.out.println(" _rank: "+_rank + " iStart: "+iStart+" iEnd: "+iEnd+" nIter: "+ nIter);
		
		_sumVioTSPara = new float[nIter][2];
    	_sumVioTSImpPara = new float[nIter][2];
    	_numVioTSPara = new int[nIter][2];
    	_indicatorIndiElemVioCheckPara = new int[nIter][4];

    	//TODO: to be updated for FACTS loop
		// Start transmission switching loop
		_numTSChecked = 0;
		for (int iNdx=iStart; iNdx<iEnd; iNdx++)
		{
			int i = _idxTS[iNdx];
			boolean inSvc = _branches.isInSvc(i);
			if (inSvc == true) runSingleTS(i);
			else dumpTSNotInSvcPackage(i);
			_numTSChecked++;
		}
		setNumTSForRoughlyStat();
		assertTrue(_numTSChecked == nIter);
//		System.out.println(" test before Comm _rank: "+_rank);
		ThreadsComm(paraMPJ);
//		System.out.println(" test after Comm _rank: "+_rank);

		if (_rank == 0) System.out.println("      TS process is finished.");
		if (_rank == 0) System.out.println("      # of switching actions checked : " + _numTSChecked);		
		if (_rank == 0 && _outputAllTS == true) dumpAllTSResults(_outFile);
		if (_rank == 0) System.out.println("      Solution time of TS process: " + (System.nanoTime() - t_Start)/1e9f);
	}
	
	/** Save all the results to thread 0. */
	void ThreadsComm(MPJExpressXL paraMPJ)
	{
		_numTSChecked = paraMPJ.ThreadCommSumOneNumber(_numTSChecked);
		
		_numBrcTStoNoVio = paraMPJ.ThreadCommSumOneNumber(_numBrcTStoNoVio);
		_numBrcTStoRedVio = paraMPJ.ThreadCommSumOneNumber(_numBrcTStoRedVio);
		_numBrcTSVio = paraMPJ.ThreadCommSumOneNumber(_numBrcTSVio);
		_numBrcTSNotConv = paraMPJ.ThreadCommSumOneNumber(_numBrcTSNotConv);
		
		int[] idxTSMaxBrcVioImpAll = paraMPJ.ThreadCommOneNumber(_idxTSMaxBrcVioImp);
		float[] maxBrcVioImpAll = paraMPJ.ThreadCommOneNumber(_maxBrcVioImp);
		float[] VmVioImpAssoToMaxBrcVioImpAll = paraMPJ.ThreadCommOneNumber(_VmVioImpAssoToMaxBrcVioImp);
		if (_rank == 0)
		{
			int idxBest = AuxArrayXL.getIdxMaxNum(maxBrcVioImpAll, VmVioImpAssoToMaxBrcVioImpAll);
			_idxTSMaxBrcVioImp = idxTSMaxBrcVioImpAll[idxBest];
			_maxBrcVioImp = maxBrcVioImpAll[idxBest];
			_VmVioImpAssoToMaxBrcVioImp = VmVioImpAssoToMaxBrcVioImpAll[idxBest];
		}
		
		int[] idxTSMaxVmVioImpAll = paraMPJ.ThreadCommOneNumber(_idxTSMaxVmVioImp);
		float[] maxVmVioImpAll = paraMPJ.ThreadCommOneNumber(_maxVmVioImp);
		float[] BrcVioImpAssoToMaxVmVioImpAll = paraMPJ.ThreadCommOneNumber(_BrcVioImpAssoToMaxVmVioImp);
		if (_rank == 0)
		{
			int idxBest = AuxArrayXL.getIdxMaxNum(maxVmVioImpAll, BrcVioImpAssoToMaxVmVioImpAll);
			_idxTSMaxVmVioImp = idxTSMaxVmVioImpAll[idxBest];
			_maxVmVioImp = maxVmVioImpAll[idxBest];
			_BrcVioImpAssoToMaxVmVioImp = BrcVioImpAssoToMaxVmVioImpAll[idxBest];
		}
		
		int[] idxTSMaxSumVioImpAll = paraMPJ.ThreadCommOneNumber(_idxTSMaxSumVioImp);
		float[] maxSumVioImpAll = paraMPJ.ThreadCommOneNumber(_maxSumVioImp);
		float[] BrcVioImpAssoToMaxSumVioImpAll = paraMPJ.ThreadCommOneNumber(_BrcVioImpAssoToMaxSumVioImp);
		float[] VmVioImpAssoToMaxSumVioImpAll = paraMPJ.ThreadCommOneNumber(_VmVioImpAssoToMaxSumVioImp);
		if (_rank == 0)
		{
			int idxBest = AuxArrayXL.getIdxMaxNum(maxSumVioImpAll);
			_idxTSMaxSumVioImp = idxTSMaxSumVioImpAll[idxBest];
			_maxSumVioImp = maxSumVioImpAll[idxBest];
			_BrcVioImpAssoToMaxSumVioImp = BrcVioImpAssoToMaxSumVioImpAll[idxBest];
			_VmVioImpAssoToMaxSumVioImp = VmVioImpAssoToMaxSumVioImpAll[idxBest];
		}
		
		_sumVioTS = paraMPJ.ThreadCommTwoDimArray(_sumVioTSPara);
		_sumVioTSImp = paraMPJ.ThreadCommTwoDimArray(_sumVioTSImpPara);
		_numVioTS = paraMPJ.ThreadCommTwoDimArray(_numVioTSPara);
		_indicatorIndiElemVioCheck = paraMPJ.ThreadCommTwoDimArray(_indicatorIndiElemVioCheckPara);
	}

	@Override
	void setIndicatorIndiElemVioCheck()
	{
		if (_outputAllTS == true)
		{
			if (_checkNewVioOccured == true)
			{
				if (_newBrcVioOccured == true) _indicatorIndiElemVioCheckPara[_numTSChecked][0] = 1;
				else _indicatorIndiElemVioCheckPara[_numTSChecked][0] = 0;
				if (_newVmVioOccured == true) _indicatorIndiElemVioCheckPara[_numTSChecked][1] = 1;
				else _indicatorIndiElemVioCheckPara[_numTSChecked][1] = 0;
			}
			else 
			{
				_indicatorIndiElemVioCheckPara[_numTSChecked][0] = -1;
				_indicatorIndiElemVioCheckPara[_numTSChecked][1] = -1;
			}
			
			if (_checkContiVioWorse == true)
			{
				if (_contiBrcVioWorse == true) _indicatorIndiElemVioCheckPara[_numTSChecked][2] = 1;
				else _indicatorIndiElemVioCheckPara[_numTSChecked][2] = 0;
				if (_contiVmVioWorse == true) _indicatorIndiElemVioCheckPara[_numTSChecked][3] = 1;
				else _indicatorIndiElemVioCheckPara[_numTSChecked][3] = 0;
			}
			else
			{
				_indicatorIndiElemVioCheckPara[_numTSChecked][2] = -1;
				_indicatorIndiElemVioCheckPara[_numTSChecked][3] = -1;
			}
		}
	}
	
    @Override
	void dumpTSNotInSvcPackage(int i) throws PsseModelException
	{
		if (_outputAllTS == true)
		{
			_sumVioTSPara[_numTSChecked] = getArrayWithSameElems(2, -5f);
			_sumVioTSImpPara[_numTSChecked] = getArrayWithSameElems(2, -5f);
			_numVioTSPara[_numTSChecked] = getArrayWithSameElems(2, -5);
			_indicatorIndiElemVioCheckPara[_numTSChecked] = getArrayWithSameElems(4, -5);
		}
		if (_outputAllTS_AllVio == true) dumpTSNotInSer(i);					
	}

    @Override
	void dumpTSNotConvPackage(int i) throws PsseModelException
	{
		if (_outputAllTS == true)
		{
	    	_sumVioTSPara[_numTSChecked] = getArrayWithSameElems(2, -2f);
	    	_sumVioTSImpPara[_numTSChecked] = getArrayWithSameElems(2, -2f);
	    	_numVioTSPara[_numTSChecked] = getArrayWithSameElems(2, -2);
			_indicatorIndiElemVioCheckPara[_numTSChecked] = getArrayWithSameElems(4, -2);
		}
		if (_outputAllTS_AllVio == true) dumpTSNotConv(i);
	}

    @Override
	void dumpTSConvPackage(int i, float sumBrcVio, float sumVmVio, VioResult testC) throws PsseModelException
	{
		if (_outputAllTS == true)
		{
			_sumVioTSPara[_numTSChecked][0] = sumBrcVio;
			_sumVioTSPara[_numTSChecked][1] = sumVmVio;                   		
			_sumVioTSImpPara[_numTSChecked][0] = getPerctImp(_sumContiBrcVio, sumBrcVio);
			_sumVioTSImpPara[_numTSChecked][1] = getPerctImp(_sumContiVmVio, sumVmVio);              
			_numVioTSPara[_numTSChecked][0] = testC.sizeBrc();
			_numVioTSPara[_numTSChecked][1] = testC.sizeV();                    		
		}
		if (_outputAllTS_AllVio == true) dumpTSAllVioInfo(i, testC);
	}


	

}

