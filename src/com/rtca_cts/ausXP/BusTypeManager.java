package com.rtca_cts.ausXP;

import com.powerdata.openpa.psse.BusTypeCode;
import com.powerdata.openpa.psse.PsseModel;
import com.powerdata.openpa.psse.PsseModelException;

public class BusTypeManager {
	
	PsseModel _model;
	
	// Before power flow 
	int[] _slackPrePF;
	int[] _pvPrePF;
	int[] _pqPrePF;

	// After power flow, before contingency analysis
	int[] _slackPreCA;
	int[] _pvPreCA;
	int[] _pqPreCA;
	
	// After contingency analysis, before transmission switching. 
	int[] _slackPreTS;
	int[] _pvPreTS;
	int[] _pqPreTS;

	public BusTypeManager(PsseModel model) throws PsseModelException {_model = model; init();}
	
	void init() throws PsseModelException
	{
		_slackPrePF = _model.getBusNdxForType(BusTypeCode.Slack);
		_pvPrePF = _model.getBusNdxForType(BusTypeCode.Gen);
		_pqPrePF = _model.getBusNdxForType(BusTypeCode.Load);
	}

	public void usePrePFBusType() throws PsseModelException {setBusType(_slackPrePF, _pvPrePF, _pqPrePF);}
	public void usePreCABusType() throws PsseModelException {setBusType(_slackPreCA, _pvPreCA, _pqPreCA);}
	public void usePreTSBusType() throws PsseModelException {setBusType(_slackPreTS, _pvPreTS, _pqPreTS);}
	
	public void setBusType(int[] slack, int[] pv, int[] pq) throws PsseModelException
	{
		_model.setBusNdxForTypeSlack(slack);
		_model.setBusNdxForTypeGen(pv);
		_model.setBusNdxForTypeLoad(pq);
	}
	
	public void setPrePFBusType(int[] slack, int[] pv, int[] pq) throws PsseModelException
	{
		_slackPrePF = slack;
		_pvPrePF = pv;
		_pqPrePF = pq;
	}
	
	public void setPreCABusType(int[] slack, int[] pv, int[] pq) throws PsseModelException 
	{
		_slackPreCA = slack;
		_pvPreCA = pv;
		_pqPreCA = pq;
	}
	
	public void setPreTSBusType(int[] slack, int[] pv, int[] pq) throws PsseModelException 
	{
		_slackPreTS = slack;
		_pvPreTS = pv;
		_pqPreTS = pq;
	}

	/** Set current bus type as PreTS bus type setting. */
	public void setPreTSBusType() throws PsseModelException
	{
		setPreTSBusType(getCurrentSlackBuses(), getCurrentPVBuses(), getCurrentPQBuses());
	}

	
	/** return[0] - slack buses, return[1] - pv buses, return[1] - pq buses. */
	public int[][] getCurrentBusTypes() throws PsseModelException
	{
		int[] slack = getCurrentSlackBuses();
		int[] pv = getCurrentPVBuses();
		int[] pq = getCurrentPQBuses();
		return new int[][] {slack, pv, pq};
	}

	public int[] getCurrentSlackBuses() throws PsseModelException
	{
		return _model.getBusNdxForType(BusTypeCode.Slack);
	}

	public int[] getCurrentPVBuses() throws PsseModelException
	{
		return _model.getBusNdxForType(BusTypeCode.Gen);
	}

	public int[] getCurrentPQBuses() throws PsseModelException
	{
		return _model.getBusNdxForType(BusTypeCode.Load);
	}


	
}
