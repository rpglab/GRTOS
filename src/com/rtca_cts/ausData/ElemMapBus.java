package com.rtca_cts.ausData;

import java.util.Arrays;

import com.powerdata.openpa.psse.OneTermDev;
import com.powerdata.openpa.psse.PsseBaseList;
import com.powerdata.openpa.psse.PsseModel;
import com.powerdata.openpa.psse.PsseModelException;


/**
 * 
 * Map Elements to Buses;  
 * E.g., this routine is able to return all buses 
 * that has at least a generator connecting to it.  
 * 
 * Initialized in Jan. 2015.
 * 
 * @author Xingpeng.Li (xplipower@gmail.com)
 *
 */
public class ElemMapBus {
	
	PsseModel _model;
	BusGroupElems _busGroupElems;
	
	int[] _genBuses = null;
	int[] _loadBuses = null;
	int[] _shuntsBuses = null;
	int[] _svcBuses = null;

	public ElemMapBus(PsseModel model) throws PsseModelException
	{
		_model = model;
		_busGroupElems = model.getBusGroupElems();
	}
	
	public void launch() throws PsseModelException
	{
		setGenBus();
		setLoadBus();
		setShuntBus();
		setSVCBus();
	}
	
	void setGenBus() throws PsseModelException {_genBuses = mapElemstoBus(_model.getGenerators());}
	void setLoadBus() throws PsseModelException {_loadBuses = mapElemstoBus(_model.getLoads());}
	void setShuntBus() throws PsseModelException {_shuntsBuses = mapElemstoBus(_model.getShunts());}
	void setSVCBus() throws PsseModelException {_svcBuses = mapElemstoBus(_model.getSvcs());}
	
	/** Map one-terminal elements to bus set. */
	public int[] mapElemstoBus(PsseBaseList<? extends OneTermDev> list) throws PsseModelException
	{
		int nElem = list.size();
		int[] elemBus = new int[nElem];
		boolean[] isElemAtBus = list.getIsElemAtBus();
		
		int ndx = 0;
		int size = isElemAtBus.length;
		for (int i=0; i<size; i++)
			if (isElemAtBus[i] == true) elemBus[ndx++] = i;
		elemBus = Arrays.copyOf(elemBus, ndx);
		return elemBus;
	}
	
	public int[] getGenBuses() throws PsseModelException
	{
		if (_genBuses == null) setGenBus();
		return _genBuses;
	}
	
	public int[] getLoadBuses() throws PsseModelException
	{
		if (_loadBuses == null) setLoadBus();
		return _loadBuses;
	}
	
	public int[] getShuntBuses() throws PsseModelException
	{
		if (_shuntsBuses == null) setShuntBus();
		return _shuntsBuses;
	}
	
	public int[] getSVCBuses() throws PsseModelException
	{
		if (_svcBuses == null) setSVCBus();
		return _svcBuses;
	}
	
	
	
	
}
