package com.rtca_cts.contingencyanalysis;

import com.rtca_cts.param.ParamVio;
import com.utilxl.array.AuxArrayXL;
import com.utilxl.iofiles.AuxFileXL;

/**
 * 
 * @author Xingpeng.Li (xplipower@gmail.com)
 *
 */
public class AnalyzeVioResult {
	
	VioResult _vioRecord;
	
//	// flag : violation is critical or not.
//	boolean _keyVio;
//	boolean _keyBrcVio;
//	boolean _keyVmVio;
//
	
	// thermal violations would be considered as critical if either sumVio > sumTol or any elemVio > elemTol are satisfied.
	float _sumBrcVioTol = ParamVio.getTolSumMVA();  
	float _elemBrcVioTol = ParamVio.getTolElemMVA(); // in per unit
	
	// voltage violations would be considered as critical if either sumVio > sumTol or any elemVio > elemTol are satisfied.
	float _sumVmVioTol = ParamVio.getTolSumVm();
	float _elemVmVioTol = ParamVio.getTolElemVm();
	
	public AnalyzeVioResult(VioResult vioRecord) {_vioRecord = vioRecord;}
	
	
	public boolean isVioKey()
	{
		if (isBrcVioKey() == true) return true;
		if (isVmVioKey() == true) return true;
		return false;
	}
	
	public boolean isBrcVioKey()
	{
		if (_vioRecord.getVioBrc() == false) return false;
		if (_vioRecord.getSumBrcDiff() > _sumBrcVioTol) return true;
		float[] brcDiff = _vioRecord.getBrcDiff();
		for (int i=0; i<brcDiff.length; i++)
			if (brcDiff[i] > _elemBrcVioTol) return true;
		return false;
	}

	public boolean isVmVioKey()
	{
		if (_vioRecord.getVioVoltage() == false) return false;
		if (_vioRecord.getSumVmDiff() > _sumVmVioTol) return true;
		float[] vmDiff = _vioRecord.getVmDiffAbs();
		for (int i=0; i<vmDiff.length; i++)
			if (vmDiff[i] > _elemVmVioTol) return true;
		return false;
	}

	
	public void outputSummaryVioInfo()
	{
		System.out.println("# of violation : "+ _vioRecord.size());
		System.out.println("# of flow violation : "+ _vioRecord.sizeBrc());
		System.out.println("# of voltage violation : "+ _vioRecord.sizeV() + " \n");
	}
	
	public void checkAbnormalVio(String outputPathToFile, String uri, float sumBrcVioAbnormal, float sumVmVioAbnormal)
	{
		float sumBrcVio = _vioRecord.getSumBrcDiff();
		float sumVmVio = _vioRecord.getSumVmDiff();
		if (sumBrcVio > sumBrcVioAbnormal || sumVmVio > sumVmVioAbnormal)
		{
			String title = "Super warning: Abnormal large violations monitored.";
			title += "  SumBrcVio (in per unit) : "+ sumBrcVio + ",  " + "SumVmVio (in per unit) : "+ sumVmVio;
			
			String[] titles = new String[] {uri, title};
			AuxFileXL.initFileWithTitle(outputPathToFile, titles, true);
		}
	}
	
	/** report @numOfBrcVio branches with most serious flow violations (in MW) on them. */
	public int[] findSeriousBrcWithVio(int numOfBrcVio)
	{
		return findSeriousBrcWithVio(numOfBrcVio, true);
	}

	/** report @numOfBrcVio branches with most serious flow violations on them. */
	public int[] findSeriousBrcWithVio(int numOfBrcVio, boolean concernAbsVio)
	{
		if (numOfBrcVio == 0) return null;
		int[] idxVioBrc = _vioRecord.getIdxBrc();
		if (idxVioBrc == null) return null;
		int concernNumOfVioTmp = idxVioBrc.length;
		if (numOfBrcVio > concernNumOfVioTmp) {numOfBrcVio = concernNumOfVioTmp;}

		float[] flowVioBrc = _vioRecord.getBrcDiff();
		if (concernAbsVio == false)
		{
			float[] flowRateUsed = _vioRecord.getRateUsedForVioBrc();
			flowVioBrc = AuxArrayXL.arrayIndiviDividen(flowVioBrc, flowRateUsed);
		}
		int[] idxTmp = AuxArrayXL.getIdxOfMaxElems(flowVioBrc, numOfBrcVio);
		idxVioBrc = AuxArrayXL.getElemsGivenIndex(idxVioBrc, idxTmp);
		return idxVioBrc;
	}
	
	/** report @numOfBusVio buses with most serious voltage violations on them. */
	public int[] findSeriousBusWithVio(int numOfBusVio)
	{
		if (numOfBusVio == 0) return null;
		int[] idxVioBus = _vioRecord.getIdxV();
		if (idxVioBus == null) return null;
		int concernNumOfVioTmp = idxVioBus.length;
		if (numOfBusVio > concernNumOfVioTmp) {numOfBusVio = concernNumOfVioTmp;}

		float[] vmVioBus = _vioRecord.getVmDiffAbs();
		int[] idxTmp = AuxArrayXL.getIdxOfMaxElems(vmVioBus, numOfBusVio);
		idxVioBus = AuxArrayXL.getElemsGivenIndex(idxVioBus, idxTmp);
		return idxVioBus;
	}
	
	
}
