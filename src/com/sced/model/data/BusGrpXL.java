package com.sced.model.data;

import java.util.ArrayList;
import java.util.Arrays;

import com.sced.model.SystemModelXL;
import com.sced.model.data.base.OneTermDevList;
import com.sced.util.AuxMethodXL;

/**
 * 
 * @author Xingpeng Li, xplipower@gmail.com 
 *
 */
public class BusGrpXL {

	SystemModelXL _model;
	boolean _checkOnLineElemsOnly = false; // if true, then only track elements which are online.
	
	boolean[] _isGenAtBus;
	boolean[] _isLoadAtBus;
	
	int[] _idxMapBusToGens;        // map bus index to the array _gens.
	int[] _idxMapBusToLoads;
	
	int[][] _gens;            // e.g. _gen[_idxMapBusToGens[0]] denotes all the generators' indices which connects to the bus 0.
	int[][] _loads;
	
	int _maxNumBrcToOneBus = 200;    // Warning: This method would be wrong if there exists a bus which connects _maxNumBrcToOneBus lines.
	int[][] _brcs = null;            // including From-bus items and To-bus items. 
	int[][] _busToBuses = null;          // buses connect to bus i.
	
	double[] _busLoad;

	public BusGrpXL (SystemModelXL model) {_model = model; init();}
	
	private void init() {
		int nBus = _model.getBuses().size();
		_isGenAtBus = new boolean[nBus];
		_isLoadAtBus = new boolean[nBus];
		_idxMapBusToGens = new int[nBus];
		_idxMapBusToLoads = new int[nBus];
		Arrays.fill(_idxMapBusToGens, -1);		
		Arrays.fill(_idxMapBusToLoads, -1);
		launch();
	}
	
	public void setOnlyCheckOnLineElems() {_checkOnLineElemsOnly = true;}
	public void setCheckAllElems() {_checkOnLineElemsOnly = false;}
	
	public void launch()
	{
		setBusGroupGen();
		setBusGroupLoad();
		setBusGroupACBrc();
	}
	
	void setBusGroupGen() {_gens = setBusGroupElems(_model.getGens(), _isGenAtBus, _idxMapBusToGens);}
	void setBusGroupLoad() {_loads = setBusGroupElems(_model.getLoads(), _isLoadAtBus, _idxMapBusToLoads);}
	
	public int[][] setBusGroupElems(OneTermDevList list, boolean[] isElemAtBus, int[] idxMapBusToElems)
	{
		int nElem = list.size();
		int nBus = _model.getBuses().size();
		int[] num = new int[nBus];  // # of elements at each bus.
		int[][] idxElemAtBusTmp = new int[nBus][nElem];
		
		int ndx = 0;
		for (int i=0; i<nElem; i++)
		{
			boolean check = true;
			if (_checkOnLineElemsOnly == true) check = list.isInSvc(i);
			if (check == true)
			{
	     		int idx	= list.getBusIdx(i);
	     		if (isElemAtBus[idx] == false)
	     			{isElemAtBus[idx] = true; idxMapBusToElems[idx] = ndx++;}
	     		idxElemAtBusTmp[idx][num[idx]] = i;
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
	public void setBusGroupACBrc()
	{
		int nBus = _model.getBuses().size();
		BranchListXL branches = _model.getBranches();
		int nbr = branches.size();
		if (nbr > _maxNumBrcToOneBus) _brcs = new int[nBus][_maxNumBrcToOneBus];
		else _brcs = new int[nBus][nbr];
		
		int[] numBrcForBus = new int[nBus];  // # of branches at each bus.
		for (int i=0; i<nbr; i++)
		{
			boolean check = true;
			if (_checkOnLineElemsOnly == true) check = branches.isInSvc(i);
			if (check == true)
			{
	     		int idxFrmBus = branches.getFrmBusIdx(i);
	     		_brcs[idxFrmBus][numBrcForBus[idxFrmBus]] = i;
	     		numBrcForBus[idxFrmBus]++;
				
	     		int idxToBus = branches.getToBusIdx(i);
	     		_brcs[idxToBus][numBrcForBus[idxToBus]] = i;
	     		numBrcForBus[idxToBus]++;
			}
		}
		for (int i=0; i<nBus; i++)
			_brcs[i] = Arrays.copyOf(_brcs[i], numBrcForBus[i]);
	}
	
	/** Warning: This method would be wrong if there exists a bus which connects _maxNumBrcToOneBus lines. */
	public void setBusGroupBus()
	{
		if (_brcs == null) setBusGroupACBrc();
		BranchListXL branches = _model.getBranches();
		
		int nBus = _model.getBuses().size();
		_busToBuses = new int[nBus][];

		for (int i=0; i<nBus; i++)
		{
			ArrayList<Integer> idxNewBuses = new ArrayList<Integer>();
			int[] idxBrcs = _brcs[i];
			for (int j=0; j<idxBrcs.length; j++)
			{
				int idxBrc = idxBrcs[j];
	     		int idxBus = branches.getFrmBusIdx(idxBrc);
	     		if (i == idxBus) idxBus = branches.getToBusIdx(idxBrc);
				if (idxNewBuses.contains(idxBus) == false) idxNewBuses.add(idxBus);
			}
			_busToBuses[i] = AuxMethodXL.convtArrayListToInt(idxNewBuses);
		}
	}

	public int[][] getBusToBuses()
	{
		if (_busToBuses == null) setBusGroupBus();
		return _busToBuses;
	}
	

	/** Get the indices of all the generators at bus i; 
	 *  return null if there is no generator at bus i.
	 * @param i : index of bus.
	 * @throws PsseModelException 
	 * */ 
	public int[] getGenIndex(int i)
	{
		if (_gens == null) setBusGroupGen();
		return (_isGenAtBus[i] == true) ? _gens[_idxMapBusToGens[i]] : null;
	}
	
	/** Get the indices of all the loads at bus i; 
	 *  return null if there is no load at bus i.
	 * @param i : index of bus.
	 * */ 
	public int[] getLoadIndex(int i)
	{
		if (_loads == null) setBusGroupLoad();
		return (_isLoadAtBus[i] == true) ? _loads[_idxMapBusToLoads[i]] : null;
	}
		
	/** Get the indices of all the branches connecting bus i; 
	 * @param i : index of bus.
	 * @throws PsseModelException 
	 * */ 
	public int[] getBrcIndex(int i)
	{
		if (_brcs == null) setBusGroupACBrc();
		return _brcs[i];
	}

	public boolean[] getIsGenAtBus()
	{
		if (_isGenAtBus == null) setBusGroupGen();
		return _isGenAtBus;
	}
	public boolean[] getIsLoadAtBus()
	{
		if (_isLoadAtBus == null) setBusGroupLoad();
		return _isLoadAtBus;
	}
	
	/** Get the total load at bus i. @param i : index of bus. */
	public double getTotalLoad(int i)
	{
		if (_busLoad != null) return _busLoad[i];
		return calcTotalLoad(i);
	}
	
	/** Calculate the total load at bus i. @param i : index of bus. */
	private double calcTotalLoad(int i)
	{
		int[] idxLoad = getLoadIndex(i);
		if (idxLoad == null) return 0f;
		double totalLoad = 0f;
		for (int idx=0; idx<idxLoad.length; idx++) {
			if (_model.getLoads().isInSvc(idxLoad[idx]) == false) continue; 
			totalLoad += _model.getLoads().getPLoad(idxLoad[idx]);
		}
		return totalLoad;
	}
	
	/** Calculate bus load */
	public void calcBusLoad() {
		if (_busLoad != null) return;
		int nBus = _model.getBuses().size();
		_busLoad = new double[nBus];
		for (int n=0; n<nBus; n++)
			_busLoad[n] = calcTotalLoad(n);
	}

	public double getTotalPgInit(int i)
	{
		int[] idxLoad = getGenIndex(i);
		if (idxLoad == null) return 0f;
		double totalGen = 0f;
		for (int idx=0; idx<idxLoad.length; idx++) {
			if (_model.getGens().isInSvc(idxLoad[idx]) == false) continue; 
			totalGen += _model.getGens().getPgInit(idxLoad[idx]);
		}
		return totalGen;
	}

}
