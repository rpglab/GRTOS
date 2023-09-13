package com.rtca_cts.contingencyanalysis;

import java.util.Arrays;

import com.powerdata.openpa.psse.PsseModel;
import com.powerdata.openpa.psse.PsseModelException;
import com.rtca_cts.ausData.AreaData;
import com.rtca_cts.ausData.VoltageLevelData;
import com.rtca_cts.param.ParamVio;


/**
 * 
 * Determine which buses to be monitored for violations, and
 * which branches to be monitored for violations;
 * 
 * Initialized in Jan. 2015.
 * 
 * @author Xingpeng.Li, xplipower@gmail.com 
 *
 */
public class ElemsMonitorVio {
	
	PsseModel _model;
	VoltageLevelData _VmMntData = null;
	AreaData _areaMntData = null;
	
	boolean[] _isBusMnt = null;
	boolean[] _isBrcMnt = null;
	
	// exclude some specific buses and branches monitor setting
	boolean _excludePartBus;     // if true, all radial branches are excluded from the contingency list.
	int[] _idxExcuBus = null;
	boolean _excludePartBrc;     // if true, all radial branches are excluded from the contingency list.
	int[] _idxExcuBrc = null;
	
	// Consider area info.
	boolean _checkArea = ParamVio.getCheckArea();
	static int[] _areaMntList = ParamVio.getAreaMntList();        // areas of interest in terms of recording violations.
	boolean _isTwoEndBusesInArea = ParamVio.getTwoEndBusesInArea();
	
	// Consider about Voltage Level
	boolean _checkBusVmLevel = ParamVio.getCheckBusVmLevel();
	float _BusHighVmLevel = ParamVio.getBusHighVmLevel();    // Only buses whose voltage level is greater than this value will be recorded if _checkBusHighVoltage is true;
	
	boolean _checkBrcVmLevel = ParamVio.getCheckBrcVmLevel();
	boolean _useBrcVmAbsHigh = ParamVio.getIsBrcVmAbsHigh();     // if false, then, branches whose either end bus is > 70.0 KV would be considered as high-voltage.
	float _BrcHighVmLevel = ParamVio.getBrcHighVmLevel();    // Only branches whose voltage level (either Frm bus or To bus) is greater than this value will be recorded if _checkBrcHighVoltage is true;	boolean _useAbsHighVoltBrcForMonitor = true;     // if false, then, branches whose either end bus is > 70.0 KV would be considered as high-voltage.

	
	public ElemsMonitorVio(PsseModel model)
	{
		_model = model;
	}
	
	public void setAreaData(AreaData areaData) {_areaMntData = areaData;}
	public void setVmData(VoltageLevelData vmData) {_VmMntData = vmData;}
	
	public AreaData getAreaData() {return _areaMntData;}
	public VoltageLevelData getVmData() {return _VmMntData;}
	
	public void setExcuBus(boolean excludePartBus, int[] idxExcuBus) {_excludePartBus = excludePartBus; _idxExcuBus = idxExcuBus;}
	public void setExcuBrc(boolean excludePartBrc, int[] idxExcuBrc) {_excludePartBrc = excludePartBrc; _idxExcuBrc = idxExcuBrc;}
	
	public boolean[] getIsBusMnt() throws PsseModelException
	{
		if (_isBusMnt == null) calcIsBusMnt();
		return _isBusMnt;
	}
	
	public boolean[] getIsBrcMnt() throws PsseModelException
	{
		if (_isBrcMnt == null) calcIsBrcMnt();
		return _isBrcMnt;
	}

	public boolean[] getIsBrcInArea() throws PsseModelException
	{
		return (_isTwoEndBusesInArea == true) ? getAreaMntData().getIsBrcTwoEndsInArea() : getAreaMntData().getIsBranchInArea();
	}
	
	public boolean[] getIsBusInArea() throws PsseModelException
	{
		return getAreaMntData().getIsBusInArea();
	}

	private VoltageLevelData getVmMntData()
	{
		if (_VmMntData == null) _VmMntData = new VoltageLevelData(_model, _BusHighVmLevel, _BrcHighVmLevel);
		return _VmMntData;
	}
	
	private AreaData getAreaMntData()
	{
		if (_areaMntData == null) _areaMntData = new AreaData(_model, _areaMntList);
		return _areaMntData;
	}
	
	private boolean[] calcIsBusMnt() throws PsseModelException
	{
		int nbus = _model.getBuses().size();
		_isBusMnt = new boolean[nbus];
		Arrays.fill(_isBusMnt, true);
		if (_checkBusVmLevel == true)
		{
			boolean[] isBusHighVm = getVmMntData().getIsBusOfHighVol();
			for (int i=0; i<nbus; i++)
				if (isBusHighVm[i] == false) _isBusMnt[i] = false;
		}
		if (_checkArea == true)
		{
			boolean[] isBusInArea = getAreaMntData().getIsBusInArea();
			for (int i=0; i<nbus; i++)
				if (isBusInArea[i] == false) _isBusMnt[i] = false;
		}
		if (_excludePartBus == true)
		{
			for (int i=0; i<_idxExcuBus.length; i++)
				_isBusMnt[_idxExcuBus[i]] = false;
		}
		
		return _isBusMnt;
	}
	
	private boolean[] calcIsBrcMnt() throws PsseModelException
	{
		int nbr = _model.getBranches().size();
		_isBrcMnt = new boolean[nbr];
		Arrays.fill(_isBrcMnt, true);
		if (_checkBrcVmLevel == true)
		{
			boolean[] isBrcHighVm;
			if (_useBrcVmAbsHigh == true) isBrcHighVm = getVmMntData().getIsBrcOfAbsHighVol();
			else isBrcHighVm = getVmMntData().getIsBrcOfHighVol();
			for (int i=0; i<nbr; i++)
				if (isBrcHighVm[i] == false) _isBrcMnt[i] = false;
		}
		if (_checkArea == true)
		{
			boolean[] isBrcInArea = (_isTwoEndBusesInArea == true) ? getAreaMntData().getIsBrcTwoEndsInArea() : getAreaMntData().getIsBranchInArea();
			for (int i=0; i<nbr; i++)
				if (isBrcInArea[i] == false) _isBrcMnt[i] = false;
		}
		if (_excludePartBrc == true)
		{
			for (int i=0; i<_idxExcuBrc.length; i++)
				_isBrcMnt[_idxExcuBrc[i]] = false;
		}

		return _isBrcMnt;
	}
	
	public boolean getCheckArea() {return _checkArea;}
	

}
