package com.rtca_cts.ausData;

import java.util.Arrays;

import com.powerdata.openpa.psse.ACBranchList;
import com.powerdata.openpa.psse.BusList;
import com.powerdata.openpa.psse.PsseModel;
import com.powerdata.openpa.psse.PsseModelException;

/**
 * 
 * 
 * @author Xingpeng.Li (xplipower@gmail.com)
 *
 */
public class VoltageLevelData {

	PsseModel _model;
	float _VrefBus, _VrefBrc;
	
	boolean[] _isHighVolBus = null;     // bus whose voltage is greater than _Vref is treated as high voltage bus.
	boolean[] _isHighVolBrc = null;     // a brc (e.g. a transformer) is considered as high-voltage brc if there exists one bus that is of high voltage level. 
	boolean[] _isAbsHighVolBrc = null;   // a brc (e.g. a transformer) is considered as high-voltage if both end buses are of high voltage levels.
	
	public VoltageLevelData(PsseModel model, float VrefBus, float VrefBrc)
	{
		_model = model;
		_VrefBus = VrefBus;
		_VrefBrc = VrefBrc;
	}
	
	public void setVref(float VrefBus, float VrefBrc)
	{
		setVrefBus(VrefBus);
		setVrefBrc(VrefBrc);
	}
	
	public void setVrefBus(float VrefBus)
	{
		if (VrefBus != _VrefBus)
		{
			_VrefBus = VrefBus;
			_isHighVolBus = null;
		}
	}

	public void setVrefBrc(float VrefBrc)
	{
		if (_VrefBrc != VrefBrc)
		{
			_VrefBrc = VrefBrc;
			_isHighVolBrc = null;
			_isAbsHighVolBrc = null;
		}
	}
	
	public float getVrefBus() {return _VrefBus;}
	public float getVrefBrc() {return _VrefBrc;}
	
	public boolean[] getIsBusOfHighVol() throws PsseModelException
	{
		if (_isHighVolBus == null) checkBusVoltage();
		return _isHighVolBus;
	}

	public boolean[] getIsBrcOfHighVol() throws PsseModelException
	{
		if (_isHighVolBrc == null) checkBrcVoltage();
		return _isHighVolBrc;
	}

	public boolean[] getIsBrcOfAbsHighVol() throws PsseModelException
	{
		if (_isAbsHighVolBrc == null) checkBrcAbsVoltage();
		return _isAbsHighVolBrc;
	}

	
	void checkBusVoltage() throws PsseModelException
	{
		if (_VrefBus == 0) System.err.println("_Vref is not chosen properly...");
		BusList buses = _model.getBuses();
		_isHighVolBus = new boolean[buses.size()];
		for (int i=0; i<buses.size(); i++)
		{
			float baseKV = buses.getBASKV(i);
			if (baseKV > _VrefBus) _isHighVolBus[i] = true;
		}
	}

	void checkBrcVoltage() throws PsseModelException
	{
		if (_VrefBrc == 0) System.err.println("_Vref is not chosen properly...");
		ACBranchList branches = _model.getBranches();
		_isHighVolBrc = new boolean[branches.size()];
		for (int i=0; i<branches.size(); i++)
		{
			float baseKVOfFrmBus = branches.getFromBus(i).getBASKV();
			if (baseKVOfFrmBus > _VrefBrc) {_isHighVolBrc[i] = true; continue;}
			float baseKVOfToBus = branches.getToBus(i).getBASKV();
			if (baseKVOfToBus > _VrefBrc) {_isHighVolBrc[i] = true;}
		}
	}
	
	void checkBrcAbsVoltage() throws PsseModelException
	{
		if (_VrefBrc == 0) System.err.println("_Vref is not chosen properly...");
		ACBranchList branches = _model.getBranches();
		_isAbsHighVolBrc = new boolean[branches.size()];
		Arrays.fill(_isAbsHighVolBrc, true);

		for (int i=0; i<branches.size(); i++)
		{
			float baseKVOfFrmBus = branches.getFromBus(i).getBASKV();
			if (baseKVOfFrmBus <= _VrefBrc) {_isAbsHighVolBrc[i] = false; continue;}
			float baseKVOfToBus = branches.getToBus(i).getBASKV();
			if (baseKVOfToBus <= _VrefBrc) _isAbsHighVolBrc[i] = false;
		}
	}
	
}
