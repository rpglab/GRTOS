package com.rtca_cts.transmissionswitching;


import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import com.powerdata.openpa.psse.PsseModel;
import com.powerdata.openpa.psse.PsseModelException;
import com.powerdata.openpa.psse.powerflow.VoltageSource;
import com.rtca_cts.contingencyanalysis.VioResult;
import com.rtca_cts.param.ParamFACTS;

/**
 * Transmission switching;
 * 
 * Initialized in Jun. 2014.
 * 
 * @author Xingpeng.Li, xplipower@gmail.com 
 */
public class TransmSwitSeq extends TransmSwit
{
	
	TransmSwitSeq(PsseModel model) throws PsseModelException
	{
		super(model);
	}
	TransmSwitSeq(PsseModel model, VoltageSource vstart) throws PsseModelException
	{
		super(model, vstart);
	}
	TransmSwitSeq(PsseModel model, VoltageSource vstart, float[] vmBasePf, float[] vaBasePf) throws PsseModelException
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
		
		if (conv == true) {
			System.out.println("        The summation of flow    violation: "+_sumContiBrcVio);
			System.out.println("        The summation of voltage violation: "+_sumContiVmVio);
			if (_sumContiVmVio < _tolSumVm && _sumContiBrcVio < _tolSumMVA)
			{
				_withinTol = true;
				System.out.println("    The original contingency violation is within tolerance");
				System.out.println("    Transm Swit won't be implemented...");
				if (_outputAllTS == true) dumpContiNotCritical(_outFile, 13);
				if (_outputAllTS_AllVio == true) {_outFile_AllVio.print(" "+_markConti);dumpContiNotCritical(_outFile_AllVio, _numTcol-6);}
			} else {
				runTransmSwit(); writeNBestTS();
			    ResultReportForStabilityCheck report = new ResultReportForStabilityCheck(this);
			    report.launchReport();
			}
		} else {
			if (_outputAllTS == true) dumpContiNotConv(_outFile, 15);
			if (_outputAllTS_AllVio == true) {_outFile_AllVio.print(" "+_markConti);dumpContiNotConv(_outFile_AllVio, _numTcol-4);}
		}
		closePrintStream();
	}
	
	@Test
	public void runTransmSwit() throws PsseModelException, IOException
	{
		if (_outputAllTS_AllVio == true) dumpContiAllVio();
		System.out.println("    Running Transm Swit...");
		long t_Start = System.nanoTime();
        _RateC = getRateC();
        
		_numTSChecked = 0;		
		int iStart = 0;
		int iEnd = determineTSList();
		
		if (ParamFACTS.getStatusFACTS() == false) {
			/* Start transmission switching loop */
			for (int iNdx=iStart; iNdx<iEnd; iNdx++)
			{
				int i = _idxTS[iNdx];
				boolean inSvc = _branches.isInSvc(i);
				if (inSvc == true) runSingleTS(i);
				else dumpTSNotInSvcPackage(i);
				_numTSChecked++;
			}
		} else {
			/* Start FACTS loop */
			for (int iNdx=iStart; iNdx<iEnd; iNdx++)
			{
				int i = _idxTS[iNdx];
				boolean inSvc = _branches.isInSvc(i);
				if (inSvc == true) runSingleFACTS(i);
				else dumpTSNotInSvcPackage(i);
				_numTSChecked++;
			}
		}
		
		setNumTSForRoughlyStat();
		assertTrue(_numTSChecked == iEnd); // need to be uncommented.
		System.out.println("      TS process is finished.");
		System.out.println("      # of switching actions checked : " + _numTSChecked);
		if (_outputAllTS == true) dumpAllTSResults(_outFile);
		System.out.println("      Solution time of TS process: " + (System.nanoTime() - t_Start)/1e9f);
	}
	
	@Override
	void setIndicatorIndiElemVioCheck()
	{
		if (_outputAllTS == true)
		{
			if (_checkNewVioOccured == true)
			{
				if (_newBrcVioOccured == true) _indicatorIndiElemVioCheck[_numTSChecked][0] = 1;
				else _indicatorIndiElemVioCheck[_numTSChecked][0] = 0;
				if (_newVmVioOccured == true) _indicatorIndiElemVioCheck[_numTSChecked][1] = 1;
				else _indicatorIndiElemVioCheck[_numTSChecked][1] = 0;
			}
			else 
			{
				_indicatorIndiElemVioCheck[_numTSChecked][0] = -1;
				_indicatorIndiElemVioCheck[_numTSChecked][1] = -1;
			}
			
			if (_checkContiVioWorse == true)
			{
				if (_contiBrcVioWorse == true) _indicatorIndiElemVioCheck[_numTSChecked][2] = 1;
				else _indicatorIndiElemVioCheck[_numTSChecked][2] = 0;
				if (_contiVmVioWorse == true) _indicatorIndiElemVioCheck[_numTSChecked][3] = 1;
				else _indicatorIndiElemVioCheck[_numTSChecked][3] = 0;
			}
			else
			{
				_indicatorIndiElemVioCheck[_numTSChecked][2] = -1;
				_indicatorIndiElemVioCheck[_numTSChecked][3] = -1;
			}
		}
	}
	
    @Override
	void dumpTSNotInSvcPackage(int i) throws PsseModelException
	{
		if (_outputAllTS == true)
		{
			_sumVioTS[_numTSChecked] = getArrayWithSameElems(2, -5f);
			_sumVioTSImp[_numTSChecked] = getArrayWithSameElems(2, -5f);
			_numVioTS[_numTSChecked] = getArrayWithSameElems(2, -5);
			_indicatorIndiElemVioCheck[_numTSChecked] = getArrayWithSameElems(4, -5);
		}
		if (_outputAllTS_AllVio == true) dumpTSNotInSer(i);					
	}

    @Override
	void dumpTSNotConvPackage(int i) throws PsseModelException
	{
		if (_outputAllTS == true)
		{
	    	_sumVioTS[_numTSChecked] = getArrayWithSameElems(2, -2f);
	    	_sumVioTSImp[_numTSChecked] = getArrayWithSameElems(2, -2f);
	    	_numVioTS[_numTSChecked] = getArrayWithSameElems(2, -2);
			_indicatorIndiElemVioCheck[_numTSChecked] = getArrayWithSameElems(4, -2);
		}
		if (_outputAllTS_AllVio == true) dumpTSNotConv(i);
	}

    @Override
	void dumpTSConvPackage(int i, float sumBrcVio, float sumVmVio, VioResult testC) throws PsseModelException
	{
		if (_outputAllTS == true)
		{
			_sumVioTS[_numTSChecked][0] = sumBrcVio;
			_sumVioTS[_numTSChecked][1] = sumVmVio;                   		
			_sumVioTSImp[_numTSChecked][0] = getPerctImp(_sumContiBrcVio, sumBrcVio);
			_sumVioTSImp[_numTSChecked][1] = getPerctImp(_sumContiVmVio, sumVmVio);              
			_numVioTS[_numTSChecked][0] = testC.sizeBrc();
			_numVioTS[_numTSChecked][1] = testC.sizeV();                    		
		}
		if (_outputAllTS_AllVio == true) dumpTSAllVioInfo(i, testC);
	}


	
}
