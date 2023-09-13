package com.rtca_cts.contingencyanalysis;

import java.util.Arrays;

import com.powerdata.openpa.psse.ACBranchList;
import com.powerdata.openpa.psse.PsseModel;
import com.powerdata.openpa.psse.PsseModelException;
import com.rtca_cts.ausData.AreaData;
import com.rtca_cts.ausData.RadialBranches;
import com.rtca_cts.ausData.VoltageLevelData;
import com.rtca_cts.param.ParamCA;

/**
 * 
 * @author Xingpeng.Li, xplipower@gmail.com 
 *
 */
public class ContingencyListBrc {
	
	PsseModel _model;
	ACBranchList _branches;
	
	VoltageLevelData _VmData = null;
	AreaData _areaData = null;

	boolean[] _isBrcInContiList = null;
	int[] _brcContiList = null;
	
	boolean _excludeRadialBranch = ParamCA.getExcludeRadialBranch();     // if true, all radial branches are excluded from the contingency list.
	int[] _ndxRadialBrc = null;

    boolean _MarkCANotCheckLinesDesignated = false;
	int[] _LinesNotCheckNm1;  // N-1 contingency check does not apply to these lines.

	boolean _MarkCACheckLinesDesignated = false;
	int[] _LinesCheckNm1;  // N-1 contingency check will apply to these lines.

	boolean _exculdeOneBusIslandBrc = ParamCA.getExculdeOneBusIslandBrc();
	
	boolean _checkArea = ParamCA.getCheckArea();
	int[] _areaList = ParamCA.getAreaForContiList();
	boolean _isTwoEndBusesInArea = ParamCA.getTwoEndBusesInArea();
	
	boolean _CheckBrcVmLevel = ParamCA.getCheckBrcVmLevel(); // if true, branch contingency list exclude low-voltage level branches. 
	float _BrcHighVmLevel = ParamCA.getBrcHighVmLevel();
	boolean _isBrcVmAbsHigh = ParamCA.getIsBrcVmAbsHigh();

	public ContingencyListBrc(PsseModel model) throws PsseModelException
	{
		_model = model;
		_branches = _model.getBranches();
	}
	
	public void setAreaData(AreaData areaData) {_areaData = areaData;}
	public void setVoltageLevelData(VoltageLevelData vmData) {_VmData = vmData;}

	private boolean[] getIsBrcInArea() throws PsseModelException
	{
		if (_isTwoEndBusesInArea == true) return getAreaData().getIsBrcTwoEndsInArea();
		else return getAreaData().getIsBranchInArea();
	}
	
	private boolean[] getIsBrcHighVmLevel() throws PsseModelException
	{
		if (_isBrcVmAbsHigh == true) return getVmData().getIsBrcOfAbsHighVol();
		else return getVmData().getIsBrcOfHighVol();
	}
	
	private AreaData getAreaData()
	{
		if (_areaData == null) _areaData = new AreaData(_model, _areaList);
		return _areaData;
	}
	
	private VoltageLevelData getVmData()
	{
		if (_VmData == null) _VmData = new VoltageLevelData(_model, -1f, _BrcHighVmLevel);
		return _VmData;
	}
	
	public int size() throws PsseModelException {return getBrcContiList().length;}
	
	/** return branch index. */
	public int[] getBrcContiList() throws PsseModelException
	{
		if (_brcContiList == null) _brcContiList = calcBrcContiList();
		return _brcContiList;
	}
	
	/** return flags for all branches */
	public boolean[] getIsBrcInContiList() throws PsseModelException
	{
		if (_isBrcInContiList == null) _isBrcInContiList = isBrcInContiList();
		return _isBrcInContiList;
	}
	public void setIsBrcInContiList(boolean[] checkList) throws PsseModelException {_isBrcInContiList = checkList;}
	
	/** Dump brc contingency list as one column. */
	public void dumpContiListOneColumn() throws PsseModelException {dumpContiListOneColumn("oneColBrcContList.txt");}
	public void dumpContiListOneColumn(String fileName) throws PsseModelException
	{
		WriteContiList.dumpOneColumnContiList(getBrcContiList(), fileName);
	}
	/** Dump brc contingency list with detail info. */
	public void dumpContiList() throws PsseModelException {dumpContiList("brcContList.txt");}
	public void dumpContiList(String fileName) throws PsseModelException
	{
		WriteContiList.dumpBrcContiList(_branches, getBrcContiList(), fileName);
	}
	

	private int[] calcBrcContiList() throws PsseModelException
	{
		getIsBrcInContiList();
		int[] contingencyList = new int[_isBrcInContiList.length];
		int num = 0;
		for(int i=0; i<_isBrcInContiList.length; i++)
			if (_isBrcInContiList[i] == true) contingencyList[num++] = i;
		contingencyList = Arrays.copyOf(contingencyList, num);
		return contingencyList;
	}
	
	private boolean[] isBrcInContiList() throws PsseModelException
	{
		int nbr = _branches.size();
	    boolean[] checkBrcConti = new boolean[nbr];        
    	Arrays.fill(checkBrcConti, true);
    	
    	if (_checkArea == true)
    	{
    		boolean[] isBrcInArea = getIsBrcInArea();
			for (int i=0; i<nbr; i++)
				if (isBrcInArea[i] == false) checkBrcConti[i] = false;
    	}
    	if (_CheckBrcVmLevel == true)
    	{
    		boolean[] isBrcHighVmLevel = getIsBrcHighVmLevel();
			for (int i=0; i<nbr; i++)
				if (isBrcHighVmLevel[i] == false) checkBrcConti[i] = false;
    	}
    	if (_MarkCANotCheckLinesDesignated == true)
    	{
			for(int i=0; i<_LinesNotCheckNm1.length; i++)
				checkBrcConti[_LinesNotCheckNm1[i]] = false;
    	}
    	if (_exculdeOneBusIslandBrc == true)
    	{
    		int[] idxBrc = _model.getOneBusIslandData().getIdxBrcs();
    		for (int i=0; i<idxBrc.length; i++)
				checkBrcConti[idxBrc[i]] = false;
    	}
//		int[] idxBrc = _model.getOneBusIslandData().getIdxBrcs();
    	if (_MarkCACheckLinesDesignated == true)
    	{
    		for (int i=0; i<_LinesCheckNm1.length; i++)
				checkBrcConti[_LinesNotCheckNm1[i]] = true;
    	}
    	if (_excludeRadialBranch == true)
    	{
    		getNdxRadialBrc();
    		for (int i=0; i<_ndxRadialBrc.length; i++)
    			checkBrcConti[_ndxRadialBrc[i]] = false;
    	}
		for (int i=0; i<nbr; i++)
			if (_branches.isInSvc(i) == false) checkBrcConti[i] = false;  
	    return checkBrcConti;
	}
	
	
	public void setLinesNotCheckNm1(boolean mark, int[] LinesNotCheckNm1)
	{
		_MarkCANotCheckLinesDesignated = mark;	
		_LinesNotCheckNm1 = LinesNotCheckNm1;
	}
	
	public void setLinesCheckNm1(boolean mark, int[] LinesCheckNm1)
	{
		_MarkCACheckLinesDesignated = mark;	
		_LinesCheckNm1 = LinesCheckNm1;
	}
	
	public void enableExcluRadialBrc() {_excludeRadialBranch = true;}
	public void clearExcluRadialBrc() {_excludeRadialBranch = false;}

	// return index of radial branches.
	int[] getNdxRadialBrc() throws PsseModelException 
	{
		return (_ndxRadialBrc == null) ? calcNdxRadialBrc() : _ndxRadialBrc;
	}
	public void setNdxRadialBrc(int[] ndxRadialBrc) {_ndxRadialBrc = ndxRadialBrc;}
	public int[] calcNdxRadialBrc() throws PsseModelException
	{
		//TODO: note that this is just for N-1; it is not correct for N-1-1.
		RadialBranches radialBrc = _model.getRadialBrcData();
		_ndxRadialBrc = radialBrc.getNdxRadialBrc();
		return _ndxRadialBrc;
	}
	
	


}
