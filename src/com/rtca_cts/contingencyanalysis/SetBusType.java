package com.rtca_cts.contingencyanalysis;

import java.util.Arrays;

import com.powerdata.openpa.psse.BusTypeCode;
import com.powerdata.openpa.psse.PsseModel;
import com.powerdata.openpa.psse.PsseModelException;

/**
 * 
 * Initialized in Jun, 2014.
 * 
 * @author Xingpeng.Li (xplipower@gmail.com)
 * 
 */
public class SetBusType {

	PsseModel _model;
	
	SetBusType(PsseModel model)
	{
		_model = model;
	}
	
	
	/** Just set a bus as PQ bus.
	 * Not considering any other possible changes of other bus type.
	 * @param ndxBus - index of new PQ buses */
	public void addPQBus(int ndxBus) throws PsseModelException
	{
		int[] oldPQBuses = _model.getBusNdxForType(BusTypeCode.Load);
		int[] newPQBuses = addElemInArray(oldPQBuses, ndxBus, true);
        setPQBuses(newPQBuses);
	}
	
	/** Just set a bus as PV bus.
	 * Not considering any other possible changes of other bus type.
	 * @param ndxBus - index of new PV buses */
	public void addPVBus(int ndxBus) throws PsseModelException
	{
		int[] oldPVBuses = _model.getBusNdxForType(BusTypeCode.Gen);
		int[] newPVBuses = addElemInArray(oldPVBuses, ndxBus, true);
        setPVBuses(newPVBuses);
	}
	
	/** Just set a bus as Slack bus.
	 * Not considering any other possible changes of other bus type.
	 * @param ndxBus - index of new Slack buses */
	public void addSlackBus(int ndxBus) throws PsseModelException
	{
		int[] oldSlackBuses = _model.getBusNdxForType(BusTypeCode.Slack);
		int[] newSlackBuses = addElemInArray(oldSlackBuses, ndxBus, true);
        setSlackBuses(newSlackBuses);
	}
	
	/** Just remove a bus from current PQ buses.
	 * Not considering any other possible changes of other bus type.
	 * @param ndxBus - index of the PQ bus to be removed */
	public void remPQBus(int ndxBus) throws PsseModelException
	{
		int[] oldPQBuses = _model.getBusNdxForType(BusTypeCode.Load);
		int[] newPQBuses = remElemInArray(oldPQBuses, ndxBus);
        setPQBuses(newPQBuses);
	}
	
	/** Just remove a bus from current PV buses.
	 * Not considering any other possible changes of other bus type.
	 * @param ndxBus - index of the PV bus to be removed */
	public void remPVBus(int ndxBus) throws PsseModelException
	{
		int[] oldPVBuses = _model.getBusNdxForType(BusTypeCode.Gen);
		int[] newPVBuses = remElemInArray(oldPVBuses, ndxBus);
        setPVBuses(newPVBuses);
	}

	/** Just remove a bus from current Slack buses.
	 * Not considering any other possible changes of other bus type.
	 * @param ndxBus - index of the Slack bus to be removed */
	public void remSlackBus(int ndxBus) throws PsseModelException
	{
		int[] oldSlackBuses = _model.getBusNdxForType(BusTypeCode.Slack);
		int[] newSlackBuses = remElemInArray(oldSlackBuses, ndxBus);
        setSlackBuses(newSlackBuses);
	}
	
	
	/** Just set new PQ buses, not considering the corresponding new PV/Slack buses automatically. */
	public void setPQBuses(int[] newPQ) throws PsseModelException
	{
		_model.setBusNdxForTypeLoad(newPQ);
	}
	
	/** Just set new PV buses, not considering the corresponding new PQ/Slack buses automatically. */
	public void setPVBuses(int[] newPV) throws PsseModelException
	{
		_model.setBusNdxForTypeGen(newPV);
	}
	
	/** Just set new slack buses, not considering the corresponding new PQ/PV buses automatically. */
	public void setSlackBuses(int[] newSlack) throws PsseModelException
	{
		_model.setBusNdxForTypeSlack(newSlack);
	}
	
	
	/** add one element in one array and sort it from small number to big number.
	 * @param oldArray - must be sorted from small number to big numbers.
	 * @param elem - element to be added.
	 * @param notSameElem - if true, then, return oldArray if element to be added is already there. 
	 *    */
	public int[] addElemInArray(int[] oldArray, int elem, boolean notSameElem) throws PsseModelException
	{
		int newPQlength = oldArray.length + 1;
		int[] newPQ = new int[newPQlength];
		Arrays.fill(newPQ, -1);
		int ndxBusesAdded = newPQlength - 1;
		for (int i=0; i<oldArray.length; i++)
		{
			if (elem < oldArray[i])
			{
				ndxBusesAdded = i;
				if (notSameElem == true)
				{
					if (i != (oldArray.length-1)) 
					{
						if (elem == oldArray[i+1]) return oldArray;
					}
				}
				break;
			}
		}
		System.arraycopy(oldArray, 0, newPQ, 0, ndxBusesAdded);
		newPQ[ndxBusesAdded] = elem;
		int tmp = newPQlength - ndxBusesAdded - 1;
		int desPos = ndxBusesAdded + 1;
		System.arraycopy(oldArray, ndxBusesAdded, newPQ, desPos, tmp);
		return newPQ;
	}
	
	/** Remove one element in one array */
	public int[] remElemInArray(int[] oldPV, int elem) throws PsseModelException
	{
		int idx = -1;
		for (int i=0; i<oldPV.length; i++)
		{
			if (oldPV[i] == elem)
			{
				idx = i;
				break;
			}
		}
		if (idx == -1)
		{
			System.out.println("Something wrong when calculate the new PV buses in SetBusType.java ....");
		}
		int newPVlength = oldPV.length-1;
		int[] newPV = new int[newPVlength];
		System.arraycopy(oldPV, 0, newPV, 0, idx);
		int tmp = newPVlength - idx;
		int srcPos = idx + 1;
		System.arraycopy(oldPV, srcPos, newPV, idx, tmp);
		return newPV;
	}
		
}
