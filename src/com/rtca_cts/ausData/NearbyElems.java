package com.rtca_cts.ausData;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Test;

import com.powerdata.openpa.psse.ACBranchList;
import com.powerdata.openpa.psse.PsseModel;
import com.powerdata.openpa.psse.PsseModelException;
import com.powerdata.openpa.psse.powerflow.VoltageSource;
import com.utilxl.array.AuxArrayListXL;
import com.utilxl.array.AuxArrayXL;
import com.utilxl.iofiles.OutputArrays;


/**
 * 
 * Initialized in Sep. 2014.
 * 
 * @author Xingpeng.Li (xplipower@gmail.com)
 *
 */
public class NearbyElems {

	PsseModel _model;
	ACBranchList _branches;
	
	BusGroupElems _busGroupElems = null;
	AreaData _areaData;
	
	boolean _markTimeLimit = true;
	float _timeLimitBrc = 2f;  // Time limit (in seconds) for searching nearby branches.
	float _timeLimitBus = 2f;  // Time limit (in seconds) for searching nearby buses.
	
	boolean _checkBrcOnline = true; // if false, then, do not check the status of branches. 
	
	boolean _checkArea; // if true, elements which locates outside given areas will be excluded. 
	int[] _areas = null;
	boolean[] _isBusInArea = null;
	boolean[] _isBranchInArea = null;
	
	boolean _checkRadial; // if true, branch which is a radial line will be excluded.
	RadialBranches _radialCheck = null;
	
	public NearbyElems(PsseModel model) throws PsseModelException
	{
		_model = model;
		_branches = _model.getBranches();
	}
	
	public void setTimeLimit(boolean mark) {_markTimeLimit = mark;}
	public void setTimeLimit(boolean mark, float timeBrc, float timeBus)
	{
		setTimeLimit(mark);
		_timeLimitBrc = timeBrc;
		_timeLimitBus = timeBus;
	}
	
	
	public void setCheckBrcOnline(boolean mark) {_checkBrcOnline = mark;}
	
	public void setCheckRadialBrc(boolean checkRadial) {_checkRadial = checkRadial;}
	public void setCheckRadialBrc(boolean checkRadial, RadialBranches radialCheck)
	{
		_checkRadial = checkRadial; 
		_radialCheck = radialCheck;
	}
	
	
	public void setCheckArea(AreaData areaData) {_areaData = areaData; _areas = _areaData.getAreaList();}
	public void setCheckArea(boolean checkArea, AreaData areaData) {_checkArea = checkArea; setCheckArea(areaData);}
	public void setCheckArea(boolean checkArea, int[] areas) {_checkArea = checkArea; _areas = areas;}
	public void setCheckArea(boolean checkArea, boolean[] isBusInArea, boolean[] isBranchInArea)
	{
		_checkArea = checkArea;
		_isBusInArea = isBusInArea;
		_isBranchInArea = isBranchInArea;
	}

	private AreaData getAreaData()
	{
		assertTrue(_areas != null);
		if (_areaData == null) _areaData = new AreaData(_model, _areas);
		return _areaData;
	}
	
	void getAreaInfo() throws PsseModelException
	{
		if (_isBusInArea == null) _isBusInArea = getAreaData().getIsBusInArea();
		if (_isBranchInArea == null) _isBranchInArea = getAreaData().getIsBrcTwoEndsInArea();
	}
	
	/** Area check, radial line check, et al. */
	int appCheck(int num) throws PsseModelException
	{
		if (_busGroupElems == null) _busGroupElems = _model.getBusGroupElems();
		if (_checkRadial == true) getRadialData();
		if (_checkArea == true) getAreaInfo();
		if (num >= _branches.size()) num = _branches.size();
		return num;
	}
	
	public RadialBranches getRadialData() throws PsseModelException
	{
		if (_radialCheck == null) _radialCheck = new RadialBranches(_model);
		return _radialCheck;
	}
	
	/** Return buses index for the branches given. No duplicate bus index. */
	ArrayList<Integer> getBusIndexForBrc(int[] idxBrcs) throws PsseModelException
	{
		ArrayList<Integer> idxBuses = new ArrayList<Integer>();
		if (idxBrcs == null) return idxBuses;
		for (int i=0; i<idxBrcs.length; i++)
		{
			int idxBrc = idxBrcs[i];
			int idxToBus = _branches.getToBus(idxBrc).getIndex();
			int idxFrmBus = _branches.getFromBus(idxBrc).getIndex();
			if(idxBuses.contains(idxToBus) == false) idxBuses.add(idxToBus);
			if(idxBuses.contains(idxFrmBus) == false) idxBuses.add(idxFrmBus);
		}
		return idxBuses;
	}
	
	/** Return buses index for the branches given. No duplicate bus index. */
	ArrayList<Integer> getBusIndex(int[] idxBrcs, int[] idxBuses) throws PsseModelException
	{
		ArrayList<Integer> idxAllBuses = getBusIndexForBrc(idxBrcs);
		if (idxBuses == null) return idxAllBuses;
		for (int i=0; i<idxBuses.length; i++)
			if (idxAllBuses.contains(idxBuses[i]) == false) idxAllBuses.add(idxBuses[i]);
		return idxAllBuses;
	}
	
	public int[] getNearbyBrcsForBrc(int idxBrcTarget, int num, boolean includeItself) throws PsseModelException
	{
		return getNearbyBrcsForBrcs(new int[] {idxBrcTarget}, num, includeItself);
	}
	
	/** return index of @num branches that are closest to a branch with index @idxBrcTarget 
	 *  @includeItself : 
	 *   the branch with index @idxBrcTarget will be included in the nearby branches if true;
	 *   the branch with index @idxBrcTarget will be excluded in the nearby branches if false.
	 *   
	 * @throws PsseModelException */
	@Test
	public int[] getNearbyBrcsForBrcs(int[] idxBrcsTarget, int num, boolean includeItself) throws PsseModelException
	{
		return getNearbyBrcs(null, idxBrcsTarget, num, includeItself);
	}
	
	public int[] getNearbyBrcsForOneGen(int idxGenTarget, int num) throws PsseModelException
	{
		int genBusIdx = _model.getGenerators().getBus(idxGenTarget).getIndex();
		return getNearbyBrcsForOneBus(genBusIdx, num);
	}

	/** return index of @num branches that are closest to a bus with index @idxBusTarget
	 * @throws PsseModelException */
	public int[] getNearbyBrcsForOneBus(int idxBusTarget, int num) throws PsseModelException
	{
		return getNearbyBrcsForBuses(new int[] {idxBusTarget}, num);
	}
	
	/** return index of @num branches that are closest to buses with index @idxBusTarget
	 * @throws PsseModelException */
	public int[] getNearbyBrcsForBuses(int[] idxBusTarget, int num) throws PsseModelException
	{
		return getNearbyBrcs(idxBusTarget, null, num, false);
	}
	
	/** return index of @num branches that are closest to buses with index @idxBusTarget
	 * @throws PsseModelException */
	public int[] getNearbyBrcs(int[] idxBusTarget, int[] idxBrcsTarget, int num, boolean includeBrcself) throws PsseModelException
	{
//		if (num >= _branches.size()) return AuxArrayXL.getSeqIndex(_branches.size());
		num = appCheck(num);

		int[] tmpIdxBuses = idxBusTarget;
		if (_checkArea == true && idxBusTarget != null)
		{
			int tmpNdx = 0;
        	for(int i=0; i<idxBusTarget.length; i++)
        	{
        		if (_isBusInArea[idxBusTarget[i]] == true)
        			tmpIdxBuses[tmpNdx++] = idxBusTarget[i];
        	}
        	Arrays.copyOf(tmpIdxBuses, tmpNdx);
		}
		idxBusTarget = tmpIdxBuses;
		
		int[] tmpIdxBrcs = idxBrcsTarget;
		if (_checkArea == true && idxBrcsTarget != null)
		{
			int tmpNdx = 0;
        	for(int i=0; i<idxBrcsTarget.length; i++)
        	{
        		if (_isBranchInArea[idxBrcsTarget[i]] == true)
        			tmpIdxBrcs[tmpNdx++] = idxBrcsTarget[i];
        	}
        	Arrays.copyOf(tmpIdxBrcs, tmpNdx);
		}
		idxBrcsTarget = tmpIdxBrcs;
		
		ArrayList<Integer> idxBuses = getBusIndex(idxBrcsTarget, idxBusTarget);
		ArrayList<Integer> idxBrcNearby = null;
        if (includeBrcself == false && idxBrcsTarget != null)
        {
        	idxBrcNearby = getNearbyBrcForBuses(idxBuses, num+idxBrcsTarget.length);
        	for(int i=0; i<idxBrcsTarget.length; i++)
        		idxBrcNearby.remove((Integer) idxBrcsTarget[i]);
        }
        else {idxBrcNearby = getNearbyBrcForBuses(idxBuses, num);}
		AuxArrayListXL.ShrinkSize(idxBrcNearby, num);
		
		return AuxArrayListXL.toIntArray(idxBrcNearby);
	}

	
	/** return index of @num branches that are closest to the buses given with index @idxBuses 
	 * @throws PsseModelException */  
	ArrayList<Integer> getNearbyBrcForBuses(ArrayList<Integer> idxBuses, int num) throws PsseModelException
	{
		ArrayList<Integer> idxBrcNearby = new ArrayList<Integer>();
		long t_start = System.nanoTime();
		do {
			if (idxBrcNearby.size() >= num) break;
			if (_markTimeLimit == true) 
			{
			    if ((System.nanoTime() - t_start)/1e9f > _timeLimitBrc)
			    	{System.out.println(" Time's up for searching nearby brc."); break;}
			}

			ArrayList<Integer> idxNewBuses = new ArrayList<Integer>();
			for (int i=0; i<idxBuses.size(); i++)
			{
				int idxBus = idxBuses.get(i);
				int[] idxBrcs = _busGroupElems.getBrcIndex(idxBus);
				int numBrcSatisfied = 0;
				for (int j=0; j<idxBrcs.length; j++)
				{
					int idxBrc = idxBrcs[j];
					if (idxBrcNearby.contains(idxBrc) == false)
					{
						boolean markBrc = true;
						if (_checkBrcOnline == true) markBrc = _branches.isInSvc(idxBrc);  // must before areaCheck and radialBrcCheck.
						if (_checkArea == true && markBrc == true) markBrc = _isBranchInArea[idxBrc];
						if (_checkRadial == true && markBrc == true) markBrc = !_radialCheck.isBrcRadialBrc(idxBrc);
						if (markBrc == true)
							{ idxBrcNearby.add(idxBrc); numBrcSatisfied++;}

						int otherEndBusIdx = findOtherBrcEndBusIdx(idxBrc, idxBus);
						boolean markAgain = true;
						if (_checkArea == true) markAgain = _isBusInArea[otherEndBusIdx];
						if (markAgain == true)
						{
							if (idxNewBuses.contains(otherEndBusIdx) == false) idxNewBuses.add(otherEndBusIdx);
						}
					}
				}
				if (idxBrcNearby.size() >= num) break;
				if (numBrcSatisfied == 0) idxNewBuses.remove((Integer) idxBus);
			}
			idxBuses = idxNewBuses;
		} while(true);
		return idxBrcNearby;
	}
	
	/** Return index of branches that are directly connected to branch with index @idxBrcTarget  
	 * Could contains duplicate branches.
	 * Status-check, area-check and radial-branch-check are not implemented.
	 * @throws PsseModelException */
	public int[] findDirectConnectedBrcIdx(int idxBrcTarget) throws PsseModelException
	{
		if (_busGroupElems == null) _busGroupElems = _model.getBusGroupElems();
		int[] idxBrcFrmBus = _busGroupElems.getBrcIndex(_branches.getFromBus(idxBrcTarget).getIndex());
		int[] idxBrcToBus = _busGroupElems.getBrcIndex(_branches.getToBus(idxBrcTarget).getIndex());
		return AuxArrayXL.connectTwoArray(idxBrcFrmBus, idxBrcToBus);
	}
	
	/** Return index of buses that are directly connected to bus with index @idxBusTarget  
	 * This method can handle multiple branches between two buses. as well as the branch status.
	 * @throws PsseModelException */
	public int[] findDirectConnectedBusIdx(int idxBusTarget) throws PsseModelException
	{
		if (_busGroupElems == null) _busGroupElems = _model.getBusGroupElems();
		int[] idxBrcs = _busGroupElems.getBrcIndex(idxBusTarget);
		int[] idxBuses = new int[idxBrcs.length];
		Arrays.fill(idxBuses, -1);
		int num = 0;
		for (int i=0; i<idxBrcs.length; i++)
		{
			int idxBrc = idxBrcs[i];
			boolean markBrc = true;
			if (_checkBrcOnline == true) markBrc = _branches.isInSvc(idxBrc);
			if (markBrc == true)
			{
				int idxBus = findOtherBrcEndBusIdx(idxBrc, idxBusTarget);
				if (AuxArrayXL.isElemInArray(idxBuses, idxBus) == false) idxBuses[num++] = idxBus;
			}
		}
		idxBuses = Arrays.copyOf(idxBuses, num);
		return idxBuses;
	}
	
	/** Given branch index and one end-bus index, the other end-bus index will be returned.
	 * @throws PsseModelException       */
	public int findOtherBrcEndBusIdx(int idxBrc, int oneEndBusIdx) throws PsseModelException
	{
		int idxBus = _branches.getFromBus(idxBrc).getIndex();
		if (idxBus == oneEndBusIdx) idxBus = _branches.getToBus(idxBrc).getIndex();
		return idxBus;
	}
	
	/** When topology changes, this method must called before any get() methods are called. */
	public void clearRadialData() {_radialCheck = null;}
	
	
	/** added on Jun.17, 2019 */
	public static void main(String[] args) throws Exception {
		String uri = null;
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
			}
		}
		if (uri == null)
		{
			System.err.format("Usage: -uri model_uri [-voltage flat|realtime] [ -minxmag smallest reactance magnitude (default to 0.001) ] [ -debug dir ] [ -results path_to_results]");
			System.exit(1);
		}
		
		PsseModel model = PsseModel.Open(uri);
		ACBranchList branches = model.getBranches();
		NearbyElems elem = new NearbyElems(model);
		elem.setCheckRadialBrc(true);
		
		int nbrc = model.getBranches().size();
		int numSwitBrcs = 100;
		int[][] outputArray = new int[numSwitBrcs+3][nbrc];
		boolean[] isRadialLine = model.getRadialBrcData().getIsBrcRadial();
		
		for (int i=0; i<nbrc; i++) {
			outputArray[0][i] = model.getBranches().getFromBus(i).getI();
			outputArray[1][i] = model.getBranches().getToBus(i).getI();
			outputArray[2][i] = 1;
			if (isRadialLine[i] == true) continue;
			outputArray[2][i] = 0;

			elem.clearRadialData();
			int idxCont = i;
			boolean inSvc = branches.isInSvc(idxCont);
			assertTrue (inSvc == true);
			branches.setInSvc(idxCont, false); 
			int[] candidList = elem.getNearbyBrcsForBrc(idxCont, numSwitBrcs, false);
			System.out.println("i: "+i+" numTS: "+candidList.length);

			int actualNum = candidList.length;
			for (int j=0; j<actualNum; j++) outputArray[j+3][i] = candidList[j]+1;
			branches.setInSvc(idxCont, true);
		}
		OutputArrays.outputArray(outputArray);
		System.out.println("Program ends here ...");
	}

	
	
}
