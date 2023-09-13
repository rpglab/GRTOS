package com.rtca_cts.contingencyanalysis;

import java.util.Arrays;

import com.powerdata.openpa.psse.PsseModel;
import com.powerdata.openpa.psse.PsseModelException;
import com.powerdata.openpa.psse.GenList;
import com.rtca_cts.ausData.AreaData;
import com.rtca_cts.param.ParamCA;

/**
 * 
 * @author Xingpeng.Li, xplipower@gmail.com 
 *
 */
public class ContingencyListGen {
	
	PsseModel _model;
	GenList _gens;
	int _nGen;
	
	boolean[] _isGenInContiList = null;
	int[] _genContiList = null;

    boolean _MarkCANotCheckGensDesignated = false;
	int[] _GensNotCheckNm1;  // N-1 contingency check does not apply to these gens.

    boolean _MarkCACheckGensDesignated = false;
	int[] _GensCheckNm1;  // N-1 contingency check will apply to these gens.
	
	boolean _checkArea = ParamCA.getCheckArea();
	boolean[] _isGenInArea = null;
	AreaData _areaData;
	int[] _areaList = ParamCA.getAreaForContiList();
	
	public ContingencyListGen() {}
	public ContingencyListGen(PsseModel model) throws PsseModelException
	{
		_model = model;
		_gens = model.getGenerators();
		_nGen = _gens.size();
	}
	
	public void setAreaData(AreaData areaData) {_areaData = areaData;}

	/** return index of generators in contingency list. */
	public int[] getGenContiList() throws PsseModelException
	{
		if (_genContiList == null) _genContiList = calcGenContiList();
		return _genContiList;
	}
	
	/** return flags for all generators. */
	public boolean[] getIsGenInContiList() throws PsseModelException
	{
		if (_isGenInContiList == null) _isGenInContiList = isGenInContiList();
		return _isGenInContiList;
	}
	public void setIsGenInContiList(boolean[] checkList) throws PsseModelException {_isGenInContiList = checkList;}

	private AreaData getAreaData()
	{
		if (_areaData == null) _areaData = new AreaData(_model, _areaList);
		return _areaData;
	}
	
	/** Dump gen contingency list as one column. */
	public void dumpContiListOneColumn() throws PsseModelException {dumpContiListOneColumn("oneColGenContList.txt");}
	public void dumpContiListOneColumn(String fileName) throws PsseModelException
	{
		WriteContiList.dumpOneColumnContiList(getGenContiList(), fileName);
	}
	/** Dump gen contingency list with detail info. */
	public void dumpContiList() throws PsseModelException {dumpContiList("genContList.txt");}
	public void dumpContiList(String fileName) throws PsseModelException
	{
		WriteContiList.dumpGenContiList(_gens, getGenContiList(), fileName);
	}
	
	private int[] calcGenContiList() throws PsseModelException
	{
		getIsGenInContiList();
		int[] contingencyList = new int[_isGenInContiList.length]; 
		int num = 0;
		for(int i=0; i<_isGenInContiList.length; i++)
			if (_isGenInContiList[i] == true) contingencyList[num++] = i;
		contingencyList = Arrays.copyOf(contingencyList, num);
		return contingencyList;
	}
	
	private boolean[] isGenInContiList() throws PsseModelException
	{
		boolean[] checkGenConti = new boolean[_nGen];
    	Arrays.fill(checkGenConti, true);
    	if (_checkArea == true)
    	{
    		if (_isGenInArea == null) _isGenInArea = getAreaData().getIsGenInArea();
			for (int i=0; i<_nGen; i++)
				if (_isGenInArea[i] == false) checkGenConti[i] = false;
    	}
    	if (_MarkCANotCheckGensDesignated == true)
    	{
			for(int i=0; i<_GensNotCheckNm1.length; i++)
				checkGenConti[_GensNotCheckNm1[i]] = false;
    	}
    	if (_MarkCACheckGensDesignated == true)
    	{
    		for (int i=0; i<_GensCheckNm1.length; i++)
    			checkGenConti[_GensCheckNm1[i]] = true;
    	}
    	
    	for (int i=0; i<_nGen; i++)
    		if (_gens.isInSvc(i) == false) checkGenConti[i] = false;
    	return checkGenConti;
	}
	
	public void setGensNotCheckNm1(boolean mark, int[] GensNotCheckNm1)
	{
		_MarkCANotCheckGensDesignated = mark;	
		_GensNotCheckNm1 = GensNotCheckNm1;
	}
	
	public void setGensCheckNm1(boolean mark, int[] GensCheckNm1)
	{
		_MarkCACheckGensDesignated = mark;	
		_GensCheckNm1 = GensCheckNm1;
	}
	
	
	
}
