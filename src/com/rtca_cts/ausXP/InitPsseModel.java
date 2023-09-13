package com.rtca_cts.ausXP;

import java.io.FileNotFoundException;

import com.powerdata.openpa.psse.PsseModel;
import com.powerdata.openpa.psse.PsseModelException;
import com.rtca_cts.ausData.CheckModelData;
import com.rtca_cts.param.ParamManager;

public class InitPsseModel {
	
	PsseModel _model;
	
	public InitPsseModel(PsseModel model) {_model = model;}
	
	public void initSetting() throws PsseModelException, FileNotFoundException
	{
		CheckModelData checkData = new CheckModelData(_model);
		checkData.checkPgMaxData();
		_model.getBusTypeManagerData();
		
		ParamManager manager = new ParamManager(_model);
		manager.launchGeneInfoSetup();
		
		_model.getRadialBrcData();
		_model.getNearbyElemsData().setCheckRadialBrc(true);
	}
	public void initNBestTS(String uri) throws FileNotFoundException, PsseModelException
	{
		_model.getNBestTSReport().initPrint();
		_model.getNBestTSReport().initTitle(uri);
	}


}
