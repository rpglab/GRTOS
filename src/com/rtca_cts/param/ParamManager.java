package com.rtca_cts.param;

import com.powerdata.openpa.psse.PsseModel;
import com.powerdata.openpa.psse.PsseModelException;
import com.rtca_cts.ausData.AreaData;
import com.rtca_cts.ausData.VoltageLevelData;

/**
 * This class should be used at the very beginning of any application program, right after reading the data.
 * 
 * Used to set up Static variables in other Param.java class.
 * 
 * @author Xingpeng.Li (xplipower@gmail.com)
 *
 */
public class ParamManager {
	
	PsseModel _model;
	
	boolean _checkArea = false;
	boolean _isTwoEndBusesInArea = true;       // if true, only branch whose both end buses are within _areaList belongs to _areaList.
//	int[] _areaList = new int[] {1,2,3};        // areas of interest.
	int[] _areaList = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30};


	boolean _CheckBusVmLevel = false;
	float _BusHighVmLevel = 70.001f;      // for VmVio, bus voltage level below this value will not be recorded if _CheckBusVmLevel is true
	                                        // Note that this value won't matter if array _isHighVoltBus[] in VioResult.java is not null;
	
	boolean _CheckBrcVmLevel = false;     // If true, then, do not consider low-voltage lines for VioMonitor, CAList, and TSList.
	float _BrcHighVmLevel = 70.001f;      // for BrcVio, only branches whose voltage level (either Frm bus or To bus) is greater than this value 
	                                        // will be recorded if checkBrcVmLevel is true.
	boolean _isBrcVmAbsHigh = false; // If false, then, branches whose either end bus is greater than
 	                                             //  _BrcHighVmLevel would be considered as high-voltage.
	                                           // If true, then, only branches whose both end buses are greater than
                                                 //  _BrcHighVmLevelForVioMonitor would be considered as high-voltage.

	public ParamManager() { }
	public ParamManager(PsseModel model) {_model = model;}

	// Set methods
	public void setPsseModel(PsseModel model) {_model = model;}

	public void setCheckArea(boolean mark) {_checkArea = mark;}
	public void setAreaMntList(int[] area) {_areaList = area;}
	public void setTwoEndBusesInArea(boolean mark) {_isTwoEndBusesInArea = mark;}

	public void setCheckBusVmLevel(boolean mark) {_CheckBusVmLevel = mark;}
	public void setBusHighVmLevel(float a) {_BusHighVmLevel = a;}
	
	public void setCheckBrcVmLevel(boolean mark) {_CheckBrcVmLevel = mark;}
	public void setBrcHighVmLevel(float a) {_BrcHighVmLevel = a;}
	public void setIsBrcVmAbsHigh(boolean mark) {_isBrcVmAbsHigh = mark;}

	// Get methods
	public boolean getCheckArea() {return _checkArea;}
	public int[] getAreaMntList() {return _areaList;}
	public boolean getTwoEndBusesInArea() {return _isTwoEndBusesInArea;}

	public boolean getCheckBusVmLevel() {return _CheckBusVmLevel;}
	public float getBusHighVmLevel() {return _BusHighVmLevel;} 
	
	public boolean getCheckBrcVmLevel() {return _CheckBrcVmLevel;}
	public float getBrcHighVmLevel() {return _BrcHighVmLevel;} 
	public boolean getIsBrcVmAbsHigh() {return _isBrcVmAbsHigh;}

	
	/** TS, CA, Vio would then share the same parameters. */
	public void launchGeneInfoSetup() throws PsseModelException
	{
		setGeneParamInfo();
		if (_checkArea == true) setAreaDataInfo();
		setVoltageLevelDataInfo();
		setInitInstances();
	}

	public void setGeneParamInfo() throws PsseModelException
	{
		setGeneAreaParamInfo();
		setGeneVmParamInfo();
	}

	
	/** TS, CA, Vio would then share the same areas parameters. */
	public void setGeneAreaInfo() throws PsseModelException
	{
		setGeneAreaParamInfo();
		if (_checkArea == true) setAreaDataInfo();
	}
	
	/** TS, CA, Vio would then share the same areas parameters. */
	public void setGeneAreaParamInfo() throws PsseModelException
	{
		setAreaInfoForParamVio();
		setAreaInfoForParamCA();
		setAreaInfoForParamTS();
		setAreaInfoForParamGenReDisptch();
	}

	
	public void setInitInstances() throws PsseModelException
	{
		_model.getElemMnt();
		_model.getTransmSwitListData();
		_model.getNearbyElemsData();
		_model.getContingencyListBrcData();
		_model.getContingencyListGenData();
	}
	
	public void setAreaDataInfo() throws PsseModelException
	{
		AreaData areaData = new AreaData(_model, _areaList);
		_model.setAreaDataGenReDisptch(areaData);
		_model.getElemMnt().setAreaData(areaData);
		_model.getTransmSwitListData().setAreaData(areaData);
		_model.getNearbyElemsData().setCheckArea(areaData);
		_model.getContingencyListBrcData().setAreaData(areaData);
		_model.getContingencyListGenData().setAreaData(areaData);
	}
	
	/** TS, CA, Vio would then share the same voltage-related parameters. */
	public void setGeneVmInfo() throws PsseModelException
	{
		setGeneVmParamInfo();
		setVoltageLevelDataInfo();
	}
	
	/** TS, CA, Vio would then share the same voltage-related parameters. */
	public void setGeneVmParamInfo() throws PsseModelException
	{
		setVmInfoForParamVio();
		setVmInfoForParamCA();
		setVmInfoForParamTS();
	}

	public void setVoltageLevelDataInfo() throws PsseModelException
	{
		VoltageLevelData vmData = new VoltageLevelData(_model, _BusHighVmLevel, _BrcHighVmLevel);
		_model.getElemMnt().setVmData(vmData);
		_model.getTransmSwitListData().setVmData(vmData);
		_model.getContingencyListBrcData().setVoltageLevelData(vmData);
	}
	
	// Area info
	public void setAreaInfoForParamVio()
	{
		ParamVio.setCheckArea(_checkArea);
		ParamVio.setAreaMntList(_areaList);
		ParamVio.setTwoEndBusesInArea(_isTwoEndBusesInArea);
	}
	
	public void setAreaInfoForParamCA()
	{
		ParamCA.setCheckArea(_checkArea);
		ParamCA.setAreaForContiList(_areaList);
		ParamCA.setTwoEndBusesInArea(_isTwoEndBusesInArea);
	}
	
	public void setAreaInfoForParamTS()
	{
		ParamTS.setCheckArea(_checkArea);
		ParamTS.setAreaForContiList(_areaList);
		ParamTS.setTwoEndBusesInArea(_isTwoEndBusesInArea);
	}
	
	public void setAreaInfoForParamGenReDisptch()
	{
		ParamGenReDisptch.setCheckArea(_checkArea);
		ParamGenReDisptch.setAreaList(_areaList);
	}
	
	// Vm info
	public void setVmInfoForParamVio()
	{
		ParamVio.setCheckBusVmLevel(_CheckBusVmLevel);
		ParamVio.setBusHighVmLevel(_BusHighVmLevel);
		
		ParamVio.setCheckBrcVmLevel(_CheckBrcVmLevel);
		ParamVio.setBrcHighVmLevel(_BrcHighVmLevel);
		ParamVio.setIsBrcVmAbsHigh(_isBrcVmAbsHigh);
	}
	
	public void setVmInfoForParamCA()
	{
		ParamCA.setCheckBrcVmLevel(_CheckBrcVmLevel);
		ParamCA.setBrcHighVmLevel(_BrcHighVmLevel);
		ParamCA.setIsBrcVmAbsHigh(_isBrcVmAbsHigh);
	}

	public void setVmInfoForParamTS()
	{
		ParamTS.setCheckBrcVmLevel(_CheckBrcVmLevel);
		ParamTS.setBrcHighVmLevel(_BrcHighVmLevel);
		ParamTS.setIsBrcVmAbsHigh(_isBrcVmAbsHigh);
	}
	
	
}
