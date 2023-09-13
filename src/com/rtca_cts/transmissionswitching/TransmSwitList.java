package com.rtca_cts.transmissionswitching;

import java.util.Arrays;
import java.util.HashMap;

import com.powerdata.openpa.psse.ACBranchList;
import com.powerdata.openpa.psse.BusTypeCode;
import com.powerdata.openpa.psse.PsseModel;
import com.powerdata.openpa.psse.PsseModelException;
import com.rtca_cts.ausData.AreaData;
import com.rtca_cts.ausData.NearbyElems;
import com.rtca_cts.ausData.RadialBranches;
import com.rtca_cts.ausData.VoltageLevelData;
import com.rtca_cts.contingencyanalysis.VioResult;
import com.rtca_cts.param.ParamTS;
import com.sced.auxData.FormLODFXL;
import com.sced.auxData.FormPTDFXL;
import com.sced.auxData.FormTSDFXL;
import com.utilxl.array.AuxArrayXL;
import com.utilxl.iofiles.AuxFileXL;

/**
 * Generate switching list.
 * 
 * @author Xingpeng.Li (xplipower@gmail.com)
 *
 */
public class TransmSwitList {
	
	PsseModel _model;
	ACBranchList _branches;
	
	VoltageLevelData _VmData = null;
	AreaData _areaData = null;

	int[] _idxTS = null; // index of branches to be switched.
	int[] _idxTSDataMining = null;  // exclude radial branches and out-of-service branches.
	HashMap<Integer, int[]> _mapBrcCtgcyToTSList; // for Enhanced Data Method
	HashMap<Integer, int[]> _mapGenCtgcyToTSList; // for Enhanced Data Method

	boolean _MarkTSNotCheckLinesDesignated = false;
	int[] _LinesNotCheckTS = null;  // transmission switching check does not apply to these lines.

	boolean _excludeRadialBranch = ParamTS.getExcludeRadialBranch();     // if true, all radial branches are excluded from the contingency list.
	int[] _ndxRadialBrc = null;
	
	boolean _checkArea = ParamTS.getCheckArea();      // Note: areas check function will be used for both TS check and Violation record!
	int[] _areaList = ParamTS.getAreaList();               // Areas which are concerned.
	boolean _isTwoEndBusesInArea = ParamTS.getTwoEndBusesInArea();

	boolean _CheckBrcVmLevel = ParamTS.getCheckBrcVmLevel(); // if true, branch contingency list exclude low-voltage level branches. 
	float _BrcHighVmLevel = ParamTS.getBrcHighVmLevel();
	boolean _isBrcVmAbsHigh = ParamTS.getIsBrcVmAbsHigh();
	
	public TransmSwitList(PsseModel model) throws PsseModelException {_model = model; _branches = _model.getBranches();}

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
	
	public void setAreaData(AreaData areaData) {_areaData = areaData;}
	public void setVmData(VoltageLevelData VmData) {_VmData = VmData;}

	public void setCheckArea(boolean mark) {_checkArea = mark;}
	public void setCheckBrcVmLevel(boolean mark) {_CheckBrcVmLevel = mark;}
	
	public boolean[] getIsBusInArea() throws PsseModelException
	{
		return getAreaData().getIsBusInArea();
	}
	
	public boolean[] getIsBrcInArea() throws PsseModelException
	{
		if (_isTwoEndBusesInArea == true) return getAreaData().getIsBrcTwoEndsInArea();
		else return getAreaData().getIsBranchInArea();
	}

	private boolean[] getIsBrcHighVmLevel() throws PsseModelException
	{
		if (_isBrcVmAbsHigh == true) return getVmData().getIsBrcOfAbsHighVol();
		else return getVmData().getIsBrcOfHighVol();
	}

	public void clearTSList() {_idxTS = null;}
	public void clearTSListDataMining() {_idxTSDataMining = null;}

	public int[] getTSList() {return _idxTS;}
	public int[] getTSListDataMining() throws PsseModelException
	{
		if (_idxTSDataMining != null) return _idxTSDataMining;
		_idxTSDataMining = new int[_idxTS.length];
		RadialBranches radialBrc = getRadialData();
		int num = 0;
		for (int i=0; i<_idxTS.length; i++)
			if (_branches.isInSvc(_idxTS[i]) == true && radialBrc.isBrcRadialBrc(_idxTS[i]) == false)
				_idxTSDataMining[num++] = _idxTS[i];
		_idxTSDataMining = Arrays.copyOf(_idxTSDataMining, num);
		return _idxTSDataMining;
	}
	
	/** Get TS List for Enhanced Data Mining Method */
	public int[] getTSListEDM(boolean isBrcCtgcy, int idxCtgcy) throws PsseModelException
	{
		int[] list = null;
		if (isBrcCtgcy == true) list = _mapBrcCtgcyToTSList.get(idxCtgcy);
		else list = _mapGenCtgcyToTSList.get(idxCtgcy);
		if (list == null) return list;
		
		/* The following may not be needed if system topology does not change. */ 
		int[] advList = new int[list.length];
		RadialBranches radialBrc = getRadialData();
		int num = 0;
		for (int i=0; i<list.length; i++)
			if (_branches.isInSvc(list[i]) == true && radialBrc.isBrcRadialBrc(list[i]) == false)
				advList[num++] = list[i];
		advList = Arrays.copyOf(advList, num);
		return advList;
	}
	
	public void setMapBrcCtgcyToTSList(HashMap<Integer, int[]> map) {_mapBrcCtgcyToTSList = map;}
	public void setMapGenCtgcyToTSList(HashMap<Integer, int[]> map) {_mapGenCtgcyToTSList = map;}
	
	/** Read constant switching list. The brc index to read is assumed starting from 1 rather than 0. */
	public int[] readTSList(String file) {return readTSList(file, true);}
	
	/** Read constant switching list. @file : path/to/file/fileName.suffix */
	public int[] readTSList(String file, boolean isBrcIndexStartOne)
	{
		int[] candidList = AuxFileXL.readOneCollumIntData(file);
		if (isBrcIndexStartOne == true) AuxArrayXL.allElemsPlusANum(candidList, -1);
		_idxTS = candidList;
		return _idxTS;
	}
	
	/** calculate an enumeration TS List. */
	public int[] calcEnumTSList() throws PsseModelException
	{
		int nbr = _model.getBranches().size();
		_idxTS = new int[nbr];
		int numTS = 0;
		boolean[] checkBrcSwit = isBrcInEnumTSList();
		for (int i=0; i<nbr; i++)
			if (checkBrcSwit[i] == true) _idxTS[numTS++] = i;
		_idxTS = Arrays.copyOf(_idxTS, numTS);
		return _idxTS;
	}

	/** calculate an enumeration TS List. */
	private boolean[] isBrcInEnumTSList() throws PsseModelException
	{
		int nbr = _branches.size();
        boolean[] checkBrcSwit = new boolean[nbr];
    	Arrays.fill(checkBrcSwit, true);
    	
    	if (_checkArea == true)
    	{
    		boolean[] isBrcInArea = getIsBrcInArea();
			for (int i=0; i<nbr; i++)
				if (isBrcInArea[i] == false) checkBrcSwit[i] = false;
    	}
    	if (_CheckBrcVmLevel == true)
    	{
    		boolean[] isBrcHighVmLevel = getIsBrcHighVmLevel();
			for (int i=0; i<nbr; i++)
				if (isBrcHighVmLevel[i] == false) checkBrcSwit[i] = false;
    	}
    	if (_MarkTSNotCheckLinesDesignated == true)
    	{
			for(int i=0; i<_LinesNotCheckTS.length; i++)
				checkBrcSwit[_LinesNotCheckTS[i]] = false;
    	}
    	if (_excludeRadialBranch == true)
    	{
    		getNdxRadialBrc();
    		for (int i=0; i<_ndxRadialBrc.length; i++)
    			checkBrcSwit[_ndxRadialBrc[i]] = false;
    	}
    	
    	//make sure that the line to be switched is on-line. 
		for (int i=0; i<nbr; i++)
			if (_branches.isInSvc(i) == false) checkBrcSwit[i] = false;
		return checkBrcSwit;
	}

	// return index of radial branches.
	int[] getNdxRadialBrc() throws PsseModelException 
	{
		return (_ndxRadialBrc == null) ? calcNdxRadialBrc() : _ndxRadialBrc;
	}
	public void setNdxRadialBrc(int[] ndxRadialBrc) {_ndxRadialBrc = ndxRadialBrc;}
	public void clearNdxRadialBrc() {_ndxRadialBrc = null;}
	public int[] calcNdxRadialBrc() throws PsseModelException 
	{
		_ndxRadialBrc = getRadialData().getNdxRadialBrc();
		return _ndxRadialBrc;
	}
	
	public RadialBranches getRadialData() throws PsseModelException
	{
		return _model.getNearbyElemsData().getRadialData();
	}

	// nearby branches
	public int[] getNearbyBrcsForOneGen(int idxGenTarget, int num) throws PsseModelException
	{
		int genBusIdx = _model.getGenerators().getBus(idxGenTarget).getIndex();
		return getNearbyBrcsForOneBus(genBusIdx, num);
	}
	
	public int[] getNearbyBrcsForOneBus(int idxBusTarget, int num) throws PsseModelException
	{
		return getNearbyBrcsForBuses(new int[] {idxBusTarget}, num);
	}
	
	public int[] getNearbyBrcsForBuses(int[] idxBusTarget, int num) throws PsseModelException
	{
		return getNearbyBrcs(idxBusTarget, null, num, false);
	}

	
	public int[] getNearbyBrcs(int idxBrcsTarget, int num, boolean includeBrcself) throws PsseModelException
	{
		return getNearbyBrcs(new int[] {idxBrcsTarget}, num, includeBrcself);
	}

	public int[] getNearbyBrcs(int[] idxBrcsTarget, int num, boolean includeBrcself) throws PsseModelException
	{
		return getNearbyBrcs(null, idxBrcsTarget, num, includeBrcself);
	}

	public int[] getNearbyBrcs(int[] idxBusTarget, int[] idxBrcsTarget, int num, boolean includeBrcself) throws PsseModelException
	{
		NearbyElems nearbyElems = _model.getNearbyElemsData();
		nearbyElems.setCheckRadialBrc(_excludeRadialBranch);
		nearbyElems.setCheckArea(_checkArea, getIsBusInArea(), getIsBrcInArea());
		return nearbyElems.getNearbyBrcs(idxBusTarget, idxBrcsTarget, num, includeBrcself);
	}

	// @path = path/to/file.
	public void writeTSListWarning(String path, String title) {writeTSListWarning(path, new String[] {title});}
	public void writeTSListWarning(String path, String[] title)
	{
		AuxFileXL.initFileWithTitle(path+"ContiTSListWrong.txt", title, true);
	}
	
	// Added on 10.29.2018
	public int[] getTSListLODF(VioResult testC, float[] pfrmContiPf, int TSOption) throws PsseModelException {
		int nbr = _model.getBranches().size(); 
		boolean[] stat = new boolean[nbr];
		for (int i=0; i<nbr; i++)
			stat[i] = _model.getBranches().isInSvc(i);
		FormPTDFXL postCtgcyPTDF = new FormPTDFXL(_model.getBuses().size(), 
				_model.getBusNdxForType(BusTypeCode.Slack)[0], _model.getACBrcCapData().getFrmBusesIdx(),
				_model.getACBrcCapData().getToBusesIdx(), _model.getACBrcCapData().getBrcX(), stat);
		postCtgcyPTDF.getPTDF();
		//TODO
		int[] idxBrcVio = new int[2];
		idxBrcVio[0] = testC.getMaxVioBrcInd();
		idxBrcVio[1] = testC.getMaxVioBrcPerctInd();

		boolean[] brcDirctn = new boolean[2];
		brcDirctn[0] = testC.getMaxVioBrc_FlowDirctn();
		brcDirctn[1] = testC.getMaxVioBrcPerct_FlowDirctn();

		if (idxBrcVio[0] == idxBrcVio[1]) {
			//System.out.println("same");
			idxBrcVio = new int[] {testC.getMaxVioBrcInd()};
		    brcDirctn = new boolean[] {testC.getMaxVioBrc_FlowDirctn()};
		}
			
		FormTSDFXL tsdf = new FormTSDFXL(postCtgcyPTDF);
//		FormLODFXL lodf = new FormLODFXL(postCtgcyPTDF);
//		double[] aaa = lodf.getLODF(1066);
//		double[] aassa = lodf.getLODF(456);
//		double[][] lodfMatrix = lodf.getLODFMatrix();
//		double[][] tsdfMatrix = AuxArrayXL.transposeArray(tsdf.getTSDF());
//		boolean aa = AuxArrayXL.isTwoArraySame(tsdfMatrix, lodfMatrix);
		
		/* Generate candidate TS list */
		int numTS_Target = ParamTS.getNumTS();
		int numTS = (int) Math.max(numTS_Target+5, numTS_Target*1.3);
		double[] factors = null;
		int[][] TSLists = new int[idxBrcVio.length][];
		for (int i=0; i<idxBrcVio.length; i++) {
			factors = tsdf.getTSDF(idxBrcVio[i]);
			for (int j=0; j<factors.length; j++) {
				if (Double.isFinite(factors[j]) == false) factors[j] = 0;
				//TODO: for option 7, the flow direction of switching line should be considered, *1 or *-1
				else if (pfrmContiPf[j] < 0) factors[j] = -factors[j];
				if (TSOption == 8) factors[j] = factors[j] * pfrmContiPf[j];
			}
			if (brcDirctn[i] == true) TSLists[i] = AuxArrayXL.getIdxOfMinElems(factors, numTS);
			else TSLists[i] = AuxArrayXL.getIdxOfMaxElems(factors, numTS);
		}
		int[] TSList = AuxArrayXL.removeDupElems(AuxArrayXL.mergeToOneDim(TSLists));
//		TSLists = new int[2][];
//		TSLists[0] = new int[] {5,6,8,9,44};
//		TSLists[1] = new int[] {632,96,58,555,44};
//		AuxArrayXL.removeDupElems(AuxArrayXL.mergeToOneDim(TSLists));
		
		/* Remove radial lines from candidate TS list */
		int[] TSListNonRadial = new int[TSList.length];
		RadialBranches radialBrc = getRadialData();
		int num = 0;
		for (int i=0; i<TSList.length; i++) {
			if (_branches.isInSvc(TSList[i]) == true && radialBrc.isBrcRadialBrc(TSList[i]) == false)
				TSListNonRadial[num++] = TSList[i];
			else {
				System.out.println("Radial line: " + TSList[i] + " removed from candidate TS list");
			}
			if (num == numTS_Target) break;
		}
		TSListNonRadial = Arrays.copyOf(TSListNonRadial, num);
		return TSListNonRadial;
	}
	
//	if (completeEnumTS == false && (candidList == null || candidList.length != numSwitBrcs))
//	{
//		int numTStmp = 0;
//		if (candidList != null) numTStmp = candidList.length;
//		String title;						
//		if (iRow == 0) title = "# of TS for Brc contingency "+(idxContiBrc+1)+" is "+numTStmp+ ", not equal to " + numSwitBrcs+" ...";
//		else title = "# of TS for Gen contingency "+(idxContiBrc+1)+" is " +numTStmp+", not equal to " + numSwitBrcs+" ...";
//	}

	

}
