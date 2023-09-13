package com.rtca_cts.ausData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.powerdata.openpa.psse.ACBranch;
import com.powerdata.openpa.psse.ACBranchList;
import com.powerdata.openpa.psse.OneTermDev;
import com.powerdata.openpa.psse.PsseModel;
import com.powerdata.openpa.psse.PsseModelException;
import com.utilxl.array.AuxArrayListXL;

/**
 * 
 * @author Xingpeng.Li (xplipower@gmail.com)
 *
 */
public class BusGroupElems {
	
	PsseModel _model;
	boolean _checkOnLineElemsOnly = false; // if true, then only check elements which are online.
	
	boolean[] _isGenAtBus = null;
	boolean[] _isLoadAtBus = null;
	boolean[] _isShuntAtBus = null;
	boolean[] _isSVCAtBus = null;
//	boolean[] _isBrcAtBus;          // Not needed since each bus would has at least one branch connected to it.
	
	int[] _idxMapBusToGens;        // map bus index to the array _gens.
	int[] _idxMapBusToLoads;
	int[] _idxMapBusToShunts;
	int[] _idxMapBusToSVCs;
//	int[] _idxMapBusToBrcs;        // Not needed since each bus would has at least one branch connected to it.
	
	int[][] _gens = null;            // e.g. _gen[1] denotes all the generators' indices which connects to the bus with index _idxMapBusToGens[1].
	int[][] _loads = null;
	int[][] _shunts = null;
	int[][] _svcs = null;
	
	int _maxNumBrcToOneBus = 200;    // Warning: This method would be wrong if there exists a bus which connects _maxNumBrcToOneBus lines.
	int[][] _brcs = null;            // including From-bus items and To-bus items. 
	int[][] _busToBuses = null;          // buses connect to bus i.
	
	public BusGroupElems(PsseModel model) throws PsseModelException
	{
		_model = model;
		int nBus = _model.getBuses().size();
		
		_isGenAtBus = new boolean[nBus];
		_isLoadAtBus = new boolean[nBus];
		_isShuntAtBus = new boolean[nBus];
		_isSVCAtBus = new boolean[nBus];
//		_isBrcAtBus = new boolean[nBus];
		
		_idxMapBusToGens = new int[nBus];
		_idxMapBusToLoads = new int[nBus];
		_idxMapBusToShunts = new int[nBus];
		_idxMapBusToSVCs = new int[nBus];
//		_idxMapBusToBrcs = new int[nBus];
				
		Arrays.fill(_idxMapBusToGens, -1);		
		Arrays.fill(_idxMapBusToLoads, -1);
		Arrays.fill(_idxMapBusToShunts, -1);
		Arrays.fill(_idxMapBusToSVCs, -1);
//		Arrays.fill(_idxMapBusToBrcs, -1);
	}
	
	public void setOnlyCheckOnLineElems() {_checkOnLineElemsOnly = true;}
	public void setCheckAllElems() {_checkOnLineElemsOnly = false;}
	
	public void launch() throws PsseModelException
	{
		setBusGroupGen();
		setBusGroupLoad();
		setBusGroupShunt();
		setBusGroupSVC();
		setBusGroupACBrc();
	}
	
	void setBusGroupGen() throws PsseModelException {_gens = setBusGroupElems(_model.getGenerators(), _isGenAtBus, _idxMapBusToGens);}
	void setBusGroupLoad() throws PsseModelException {_loads = setBusGroupElems(_model.getLoads(), _isLoadAtBus, _idxMapBusToLoads);}
	void setBusGroupShunt() throws PsseModelException {_shunts = setBusGroupElems(_model.getShunts(), _isShuntAtBus, _idxMapBusToShunts);}
	void setBusGroupSVC() throws PsseModelException {_svcs = setBusGroupElems(_model.getSvcs(), _isSVCAtBus, _idxMapBusToSVCs);}
	
	public int[][] setBusGroupElems(List<? extends OneTermDev> list, boolean[] isElemAtBus, int[] idxMapBusToElems) throws PsseModelException
	{
		int nElem = list.size();
		int nBus = _model.getBuses().size();
		int[] num = new int[nBus];  // # of elements at each bus.
		int[][] idxElemAtBusTmp = new int[nBus][nElem];
		
		int ndx = 0;
		for (int i=0; i<nElem; i++)
		{
			OneTermDev elem = list.get(i);
			boolean check = true;
			if (_checkOnLineElemsOnly == true) check = elem.isInSvc();
			if (check == true)
			{
	     		int idx	= elem.getBus().getIndex();
	     		if (isElemAtBus[idx] == false)
	     			{isElemAtBus[idx] = true; idxMapBusToElems[idx] = ndx++;}
	     		idxElemAtBusTmp[idx][num[idx]] = elem.getIndex();
	     		num[idx]++;
			}
		}
		
		int[][] elems = new int[ndx][];
		for (int i=0; i<nBus; i++)
		{
			if (isElemAtBus[i] == true)
			{
				int tmp = idxMapBusToElems[i];
				elems[tmp] = new int[num[i]];
				System.arraycopy(idxElemAtBusTmp[i], 0, elems[tmp], 0, num[i]);
			}
		}
		return elems;
	}
	
	/** Warning: This method would be wrong if there exists a bus which connects _maxNumBrcToOneBus lines. */
	public void setBusGroupACBrc() throws PsseModelException
	{
		int nBus = _model.getBuses().size();
		ACBranchList branches = _model.getBranches();
		int nbr = branches.size();
		if (nbr > _maxNumBrcToOneBus) _brcs = new int[nBus][_maxNumBrcToOneBus];
		else _brcs = new int[nBus][nbr];
		
		int[] numBrcForBus = new int[nBus];  // # of branches at each bus.
		for (int i=0; i<nbr; i++)
		{
			ACBranch branch = branches.get(i);
			boolean check = true;
			if (_checkOnLineElemsOnly == true) check = branch.isInSvc();
			if (check == true)
			{
	     		int idxFrmBus = branch.getFromBus().getIndex();
	     		_brcs[idxFrmBus][numBrcForBus[idxFrmBus]] = i;
	     		numBrcForBus[idxFrmBus]++;
				
	     		int idxToBus = branch.getToBus().getIndex();
	     		_brcs[idxToBus][numBrcForBus[idxToBus]] = i;
	     		numBrcForBus[idxToBus]++;
			}
		}
		for (int i=0; i<nBus; i++)
			_brcs[i] = Arrays.copyOf(_brcs[i], numBrcForBus[i]);
	}
	
	/** Warning: This method would be wrong if there exists a bus which connects _maxNumBrcToOneBus lines. */
	public void setBusGroupBus() throws PsseModelException
	{
		if (_brcs == null) setBusGroupACBrc();
		ACBranchList branches = _model.getBranches();
		
		int nBus = _model.getBuses().size();
		_busToBuses = new int[nBus][];

		for (int i=0; i<nBus; i++)
		{
			ArrayList<Integer> idxNewBuses = new ArrayList<Integer>();
			int[] idxBrcs = _brcs[i];
			for (int j=0; j<idxBrcs.length; j++)
			{
				int idxBrc = idxBrcs[j];
				ACBranch branch = branches.get(idxBrc);
	     		int idxBus = branch.getFromBus().getIndex();
	     		if (i == idxBus) idxBus = branch.getToBus().getIndex();
				if (idxNewBuses.contains(idxBus) == false) idxNewBuses.add(idxBus);
			}
			_busToBuses[i] = AuxArrayListXL.toIntArray(idxNewBuses);
		}
	}

	public int[][] getBusToBuses() throws PsseModelException
	{
		if (_busToBuses == null) setBusGroupBus();
		return _busToBuses;
	}
	

	/** Get the indices of all the generators at bus i; 
	 *  return null if there is no generator at bus i.
	 * @param i : index of bus.
	 * @throws PsseModelException 
	 * */ 
	public int[] getGenIndex(int i) throws PsseModelException
	{
		if (_gens == null) setBusGroupGen();
		return (_isGenAtBus[i] == true) ? _gens[_idxMapBusToGens[i]] : null;
	}
	
	/** Get the indices of all the loads at bus i; 
	 *  return null if there is no load at bus i.
	 * @param i : index of bus.
	 * @throws PsseModelException 
	 * */ 
	public int[] getLoadIndex(int i) throws PsseModelException
	{
		if (_loads == null) setBusGroupLoad();
		return (_isLoadAtBus[i] == true) ? _loads[_idxMapBusToLoads[i]] : null;
	}
	
	/** Get the indices of all the shunts at bus i; 
	 *  return null if there is no shunt at bus i.
	 * @param i : index of bus.
	 * @throws PsseModelException 
	 * */ 
	public int[] getShuntIndex(int i) throws PsseModelException
	{
		if (_shunts == null) setBusGroupShunt();
		return (_isShuntAtBus[i] == true) ? _shunts[_idxMapBusToShunts[i]] : null;
	}
	
	/** Get the indices of all the svcs at bus i; 
	 *  return null if there is no svc at bus i.
	 * @param i : index of bus.
	 * @throws PsseModelException 
	 * */ 
	public int[] getSVCIndex(int i) throws PsseModelException
	{
		if (_svcs == null) setBusGroupSVC();
		return (_isSVCAtBus[i] == true) ? _svcs[_idxMapBusToSVCs[i]] : null;
	}
	
	/** Get the indices of all the branches connecting bus i; 
	 * @param i : index of bus.
	 * @throws PsseModelException 
	 * */ 
	public int[] getBrcIndex(int i) throws PsseModelException
	{
		if (_brcs == null) setBusGroupACBrc();
		return _brcs[i];
	}

	public boolean[] getIsGenAtBus() throws PsseModelException 
	{
		if (_isGenAtBus == null) setBusGroupGen();
		return _isGenAtBus;
	}
	public boolean[] getIsLoadAtBus() throws PsseModelException
	{
		if (_isLoadAtBus == null) setBusGroupLoad();
		return _isLoadAtBus;
	}
	public boolean[] getIsShuntAtBus() throws PsseModelException
	{
		if (_shunts == null) setBusGroupShunt();
		return _isShuntAtBus;
	}
	public boolean[] getIsSVCAtBus() throws PsseModelException
	{
		if (_svcs == null) setBusGroupSVC();
		return _isSVCAtBus;
	}
	
	public float getTotalPLoad(int i) throws PsseModelException {return getTotalLoad(i, true);}
	public float getTotalQLoad(int i) throws PsseModelException {return getTotalLoad(i, false);}
	public float getTotalLoad(int i, boolean isPload) throws PsseModelException
	{
		int[] idxLoad = getLoadIndex(i);
		if (idxLoad == null) return 0f;
		float totalLoad = 0f;
		for (int idx=0; idx<idxLoad.length; idx++) {
			if (_model.getLoads().isInSvc(idxLoad[idx]) == false) continue; 
			if (isPload == true) totalLoad += _model.getLoads().getPpu(idxLoad[idx]);
			else totalLoad += _model.getLoads().getQpu(idxLoad[idx]);
		}
		return totalLoad;
	}
	
	public float getTotalBShunt(int i) throws PsseModelException {return getTotalShunt(i, true);}
	public float getTotalGShunt(int i) throws PsseModelException {return getTotalShunt(i, false);}
	public float getTotalShunt(int i, boolean isBValue) throws PsseModelException
	{
		int[] idxShunt = getShuntIndex(i);
		if (idxShunt == null) return 0f;
		float totalShunt = 0f;
		for (int idx=0; idx<idxShunt.length; idx++) {
			if (_model.getShunts().isInSvc(idxShunt[idx]) == false) continue; 
			if (isBValue == true) totalShunt += _model.getShunts().getBpu(idxShunt[idx]);
			else totalShunt += _model.getShunts().getGpu(idxShunt[idx]);
		}
		return totalShunt;
	}
	

	
	
}
