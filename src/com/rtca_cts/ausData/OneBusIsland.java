package com.rtca_cts.ausData;

import java.util.Arrays;

import com.powerdata.openpa.psse.BusList;
import com.powerdata.openpa.psse.PsseModel;
import com.powerdata.openpa.psse.PsseModelException;
import com.rtca_cts.ausData.BusGroupElems;


/**
 * OneBusIsland: given a bus "aa", only one bus directly connects to it; 
 *               this bus "aa" is a OneBusIsland Bus.
 * 
 * @author Xingpeng.Li (xplipower@gmail.com)
 * 
 */
public class OneBusIsland {

	PsseModel _model;
	BusGroupElems _busGroupElems;
	
	int[] _idxBus; // index of bus which is an one bus island.
	int[][] _idxBrcMapByBus; // index of branch that connects to the onebusisland.
	
	int _numBrcs;    // length of the following array.
	int[] _idxBrcs;  // index of all branches that connects to the onebusisland.

	public OneBusIsland(PsseModel model) throws PsseModelException
	{
		_model = model;
		_busGroupElems = _model.getBusGroupElems();
	}
	
	public int[] getIdxSingleBusIsland() throws PsseModelException
	{
		if (_idxBus == null) calcIdxSingleBusIsland();
		return _idxBus;
	}
	
	public int[][] getIdxBrcMapByBus() throws PsseModelException
	{
		if (_idxBrcMapByBus == null) calcIdxBrcMapByBus();
		return _idxBrcMapByBus;
	}
	
	public int[] getIdxBrcs() throws PsseModelException
	{
		if (_idxBrcs == null) calcIdxBrcs();
		return _idxBrcs;
	}
	
	private void calcIdxSingleBusIsland() throws PsseModelException
	{
		BusList buses = _model.getBuses();
		int numBus = buses.size();
		_idxBus = new int[numBus];
		
		int[][] busToBuses = _busGroupElems.getBusToBuses();
		int num = 0;
		for (int i=0; i<numBus; i++)
			if (busToBuses[i].length == 1) _idxBus[num++] = i;
		_idxBus = Arrays.copyOf(_idxBus, num);
	}
	
	private void calcIdxBrcMapByBus() throws PsseModelException
	{
		if (_idxBus == null) calcIdxSingleBusIsland();
		_idxBrcMapByBus = new int[_idxBus.length][];
		for (int i=0; i<_idxBus.length; i++)
			_idxBrcMapByBus[i] = _busGroupElems.getBrcIndex(_idxBus[i]);
	}
	
	private void calcIdxBrcs() throws PsseModelException
	{
		if (_idxBrcMapByBus == null) calcIdxBrcMapByBus();
		int nbrc = _model.getBranches().size();
		_numBrcs = 0;
		_idxBrcs = new int[nbrc];
		for (int i=0; i<_idxBus.length; i++)
			for (int j=0; j<_idxBrcMapByBus[i].length; j++)
				_idxBrcs[_numBrcs++] = _idxBrcMapByBus[i][j];
		_idxBrcs = Arrays.copyOf(_idxBrcs, _numBrcs);
	}

}
