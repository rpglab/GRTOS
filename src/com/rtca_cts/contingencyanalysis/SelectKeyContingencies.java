package com.rtca_cts.contingencyanalysis;

import java.util.Arrays;

import com.powerdata.openpa.psse.PsseModelException;
import com.rtca_cts.param.ParamVio;


/**
 * Select critical contingencies (beyond tolerances)
 * 
 * Initialized in Jan. 2015.
 * 
 * @author Xingpeng.Li (xplipower@gmail.com)
 *
 */
public class SelectKeyContingencies {
	
	ContingencyAnalysis _ca;
	
	// a contingency would be considered as critical, if either thermal violation or voltage violation is critical.
	boolean _checkedBrcConti;
	int[] _keyBrcConti = null;  // index of critical branch contingencies. 
	
	boolean _checkedGenConti;
	int[] _keyGenConti = null;  // index of critical generator contingencies. 
	
	// thermal violations would be considered as critical if either sumVio > sumTol or any elemVio > elemTol are satisfied.
	boolean _checkBrcVio = true;
	float _sumBrcVioTol = ParamVio.getTolSumMVA();
	float _elemBrcVioTol = ParamVio.getTolElemMVA(); // in per unit
	
	// voltage violations would be considered as critical if either sumVio > sumTol or any elemVio > elemTol are satisfied.
	boolean _checkVmVio = true;
	float _sumVmVioTol = ParamVio.getTolSumVm();
	float _elemVmVioTol = ParamVio.getTolElemVm();
	
	public SelectKeyContingencies(ContingencyAnalysis ca) {_ca = ca;}

	public void resetCheckMark() {_checkedBrcConti = false; _checkedGenConti = false;}
	public void setCheckVio(boolean markBrcVio, boolean markVmVio) {setCheckBrcVio(markBrcVio); setCheckVmVio(markVmVio);}
	
	public void setCheckBrcVio(boolean mark) {_checkBrcVio = mark;}
	public void setBrcVioTol(float sumTol, float elemTol) {setSumBrcVioTol(sumTol); setElemBrcVioTol(elemTol);}
	public void setSumBrcVioTol(float tol) {_sumBrcVioTol = tol;}
	public void setElemBrcVioTol(float tol) {_elemBrcVioTol = tol;}

	public void setCheckVmVio(boolean mark) {_checkVmVio = mark;}
	public void setVmVioTol(float sumTol, float elemTol) {setSumVmVioTol(sumTol); setElemVmVioTol(elemTol);}
	public void setSumVmVioTol(float tol) {_sumVmVioTol = tol;}
	public void setElemVmVioTol(float tol) {_elemVmVioTol = tol;}
	
	
	public void launch()
	{
		searchBrcConti();
		searchGenConti();
	}
	
	public int[] searchBrcConti()
	{
		if (_checkedBrcConti == true) return _keyBrcConti;
		boolean[] isKeyConti = new boolean[_ca.getNumContiVioVmAndBrcAllTransm()];
		if (_checkBrcVio == true)
		{
	        int[] mapIdxToBrcVio = _ca.getIdxMapContToBrcVioContTransm();
			float[][] allBrcDiff = _ca.getAllBrcDiffTransm();
			float[] sumBrcVio = _ca.getSumBrcVioExtPerContTransm();
			isKeyConti = checkBrcVio(isKeyConti, mapIdxToBrcVio, allBrcDiff, sumBrcVio);
		}
		
		if (_checkVmVio == true)
		{
	        int[] mapIdxToVmVio = _ca.getIdxMapContToVmVioContTransm();
			float[][] allVmDiff = _ca.getVmVioDiffTransm();
			float[] sumVmVio = _ca.getSumVmVioExtPerContTransm();
			isKeyConti = checkVmVio(isKeyConti, mapIdxToVmVio, allVmDiff, sumVmVio);
		}
		
		_keyBrcConti = extractIdxKeyConti(isKeyConti, _ca.getIdxContVioVmAndBrcAllTransm());
		_checkedBrcConti = true;
		return _keyBrcConti;
	}
	
	public int[] searchGenConti()
	{
		if (_checkedGenConti == true) return _keyGenConti;
		boolean[] isKeyConti = new boolean[_ca.getNumContiVioVmAndBrcAllGen()];
		if (_checkBrcVio == true)
		{
	        int[] mapIdxToBrcVio = _ca.getIdxMapContToBrcVioContGen();
			float[][] allBrcDiff = _ca.getAllBrcDiffGen();
			float[] sumBrcVio = _ca.getSumBrcVioExtPerContGen();
			isKeyConti = checkBrcVio(isKeyConti, mapIdxToBrcVio, allBrcDiff, sumBrcVio);
		}
		
		if (_checkVmVio == true)
		{
	        int[] mapIdxToVmVio = _ca.getIdxMapContToVmVioContGen();
			float[][] allVmDiff = _ca.getVmVioDiffGen();
			float[] sumVmVio = _ca.getSumVmVioExtPerContGen();
			isKeyConti = checkVmVio(isKeyConti, mapIdxToVmVio, allVmDiff, sumVmVio);
		}
		
		_keyGenConti = extractIdxKeyConti(isKeyConti, _ca.getIdxContVioVmAndBrcAllGen());
		_checkedGenConti = true;
		return null;
	}
	
	private boolean[] checkBrcVio(boolean[] isKeyConti, int[] mapIdxToBrcVio, float[][] allBrcDiff, float[] sumBrcVio)
	{
		if (mapIdxToBrcVio == null) return isKeyConti;
		for(int i=0; i<mapIdxToBrcVio.length; i++)
		{
			if (isKeyConti[i] == true) continue;
			if (mapIdxToBrcVio[i] > -1)
			{
				int idx = mapIdxToBrcVio[i];
				isKeyConti[i] = isBrcVioKey(allBrcDiff[idx]);
				if (isKeyConti[i] == false)
					if (sumBrcVio[i] > _sumBrcVioTol) {isKeyConti[i] = true;}
			}
		}
		return isKeyConti;
	}
	
	private boolean[] checkVmVio(boolean[] isKeyConti, int[] mapIdxToVmVio, float[][] allVmDiff, float[] sumVmVio)
	{
		if (mapIdxToVmVio == null) return isKeyConti;
		for (int i=0; i<mapIdxToVmVio.length; i++)
		{
			if (isKeyConti[i] == true) continue;
			if (mapIdxToVmVio[i] > -1)
			{
				int idx = mapIdxToVmVio[i];
				isKeyConti[i] = isVmVioKey(allVmDiff[idx]);
				if (isKeyConti[i] == false)
					if (sumVmVio[i] > _sumVmVioTol) {isKeyConti[i] = true;}
			}
		}
		return isKeyConti;
	}
	
	private int[] extractIdxKeyConti(boolean[] isKeyConti, int[] idxConti)
	{
		int[] keyBrcConti = new int[isKeyConti.length];
		int ndx = 0;
		for(int i=0; i<isKeyConti.length; i++)
			if (isKeyConti[i] == true) keyBrcConti[ndx++] = idxConti[i];
		return Arrays.copyOf(keyBrcConti, ndx);
	}

	boolean isBrcVioKey(float[] brcDiff)
	{
		boolean isKey = false;
		for(int i=0; i<brcDiff.length; i++)
			if (brcDiff[i] > _elemBrcVioTol) {isKey = true; break;}
		return isKey;
	}
	
	boolean isVmVioKey(float[] vmDiff)
	{
		boolean isKey = false;
		for(int i=0; i<vmDiff.length; i++)
			if (Math.abs(vmDiff[i]) > _elemVmVioTol) {isKey = true; break;}
		return isKey;
	}
	
	
	public int[] getKeyBrcConti()
	{
		if (_keyBrcConti == null) searchBrcConti();
		return _keyBrcConti;
	}
	
	public int[] getKeyGenConti()
	{
		if (_keyGenConti == null) searchGenConti();
		return _keyGenConti;
	}
	
	
	
	/** Dump key gen contingency list as one column. */
	public void dumpKeyGenContiListOneColumn() throws PsseModelException {dumpKeyGenContiListOneColumn("oneColKeyGenContList.txt");}
	public void dumpKeyGenContiListOneColumn(String fileName) throws PsseModelException
	{
		WriteContiList.dumpOneColumnContiList(_keyGenConti, fileName);
	}
	/** Dump key gen contingency list with detail info. */
	public void dumpKeyGenContiList() throws PsseModelException {dumpKeyGenContiList("keyGenContList.txt");}
	public void dumpKeyGenContiList(String fileName) throws PsseModelException
	{
		WriteContiList.dumpGenContiList(_ca.getGenList(), _keyGenConti, fileName);
	}


	/** Dump key brc contingency list as one column. */
	public void dumpKeyBrcContiListOneColumn() throws PsseModelException {dumpKeyBrcContiListOneColumn("oneColKeyBrcContList.txt");}
	public void dumpKeyBrcContiListOneColumn(String fileName) throws PsseModelException
	{
		WriteContiList.dumpOneColumnContiList(_keyBrcConti, fileName);
	}
	/** Dump key brc contingency list with detail info. */
	public void dumpKeyBrcContiList() throws PsseModelException {dumpKeyBrcContiList("keyBrcContList.txt");}
	public void dumpKeyBrcContiList(String fileName) throws PsseModelException
	{
		WriteContiList.dumpBrcContiList(_ca.getBranches(), _keyBrcConti, fileName);
	}


	

}
