package com.rtca_cts.transmissionswitching;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import org.junit.Test;

import com.powerdata.openpa.psse.GenList;
import com.powerdata.openpa.psse.PsseModel;
import com.powerdata.openpa.psse.PsseModelException;
import com.rtca_cts.ausData.AreaData;
import com.rtca_cts.param.ParamGenReDisptch;
import com.rtca_cts.param.ParamIO;
import com.utilxl.iofiles.AuxFileXL;

public class ResultReportForStabilityCheck {
	
	TransmSwit _checkTS;
	PsseModel _model;
	int[] _contiInfo;   // It has three integer numbers. Note this array saves the contingency data by number instead of index.
                        // If brc conti: brcIdx+1, brcFrmBusNumber, brcToBusNumber; if gen conti: genIdx+1, genBusNumber, -1. 
	
	ResultReportForStabilityCheck(TransmSwit checkTS)
	{
		_checkTS = checkTS;
		_model = _checkTS.getPsseModel();
		_contiInfo = _checkTS.getContiInfo();
	}
	
	void launchReport() throws FileNotFoundException, PsseModelException
	{
		if (isTSWork() == false) return;
		if (_contiInfo[2] == -1) {outputGenContiInfo(); outputReDispatch();}
		else outputTransmContiInfo();
	}
	
	void outputGenContiInfo() throws FileNotFoundException, PsseModelException
	{
		String path = getPath("gen");

		String fileName = "ContiTSInfo_Gen.txt";
		PrintStream outPUT = new PrintStream (new FileOutputStream(path + fileName, true), true);
		
		int genIdx = _contiInfo[0] - 1;
		int genBusIdx = _model.getGenerators().getBus(genIdx).getI();
		String genID = _model.getGenerators().getID(genIdx);
		
		outPUT.print("  "+genBusIdx);
		outPUT.print("  "+genID);
		outputBestTS(outPUT);
		
		outPUT.println();
		outPUT.close();
	}
	
	boolean isTSWork()
	{
		if (_checkTS.getNumTStoNoVio() > 0) return true;
		if (_checkTS.getNumTStoRedVio() > 0) return true;
		return false;
	}
	
	
	@Test
	void outputReDispatch() throws FileNotFoundException, PsseModelException
	{
		String path = getPath("gen");
		
		int genIdx = _contiInfo[0] - 1;
		int contGenBusIdx = _model.getGenerators().getBus(genIdx).getI();
		assertTrue(contGenBusIdx == _contiInfo[1]);
		String fileName = "ContiTSInfo_Gen_NewPG_Conti_" + (genIdx+1) + ".txt";
		PrintStream outPUT = new PrintStream (new FileOutputStream(path + fileName, true), true);
		
		GenList gens = _model.getGenerators();
		boolean[] isGenInArea = null;
		boolean checkArea = ParamGenReDisptch.getCheckArea();
		if (checkArea == true)
		{
			AreaData areas = _model.getElemMnt().getAreaData();
			isGenInArea = areas.getIsGenInArea();
		}
		
		for (int i=0; i<gens.size(); i++)
		{
			if (checkArea == true && isGenInArea[i] == false) continue;
			if (gens.isInSvc(i) == false) continue;
			int genBusIdx = _model.getGenerators().getBus(i).getI();
			String genID = _model.getGenerators().getID(i);
			float newPg = _model.getGenerators().getPpu(i);
			
			outPUT.print("  "+genBusIdx);
			outPUT.print("  "+genID);
			outPUT.print("  "+newPg);
			outPUT.println();
		}
		outPUT.close();
	}
	
	
	void outputTransmContiInfo() throws FileNotFoundException, PsseModelException
	{
		String path = getPath("brc");

		String fileName = "ContiTSInfo_Transm.txt";
		PrintStream outPUT = new PrintStream (new FileOutputStream(path + fileName, true), true);
		
		int brcFrmBusNum = _contiInfo[1];
		int brcToBusNum = _contiInfo[2];
		
		outPUT.print("  "+brcFrmBusNum);
		outPUT.print("  "+brcToBusNum);
		outPUT.print("  "+_model.getBranches().getCKT(_contiInfo[0] - 1));
		outputBestTS(outPUT);
		
		outPUT.println();
		outPUT.close();
	}
	
	void outputBestTS(PrintStream outPUT) throws PsseModelException
	{
		int TSBrcIdxBrcVio = _checkTS.getIdxTSMaxBrcVioImp();
		int TSBrcIdxVmVio = _checkTS.getIdxTSMaxVmVioImp();
		int TSBrcIdxAllVio = _checkTS.getIdxTSMaxSumVioImp();
		outputBrcInfo(outPUT, TSBrcIdxBrcVio);
		outputBrcInfo(outPUT, TSBrcIdxVmVio);
		outputBrcInfo(outPUT, TSBrcIdxAllVio);
	}

	void outputBrcInfo(PrintStream outPUT, int brcIdx) throws PsseModelException
	{
		int frmBus = _model.getBranches().getFromBus(brcIdx).getI();
		int toBus = _model.getBranches().getToBus(brcIdx).getI();
		String brcID = _model.getBranches().getCKT(brcIdx);
		outPUT.print("  "+frmBus);
		outPUT.print("  "+toBus);
		outPUT.print("  "+brcID);
	}

	String getPath(String contiType)
	{
		String path = _checkTS.getPathToFileAllTS_AllVio();
		if (path != null)
		{
			int pos = path.lastIndexOf("/");
			path = path.substring(0, pos);
		} else {
			path = ParamIO.getTSPath();
			AuxFileXL.createFolder(path);
			path = path + _model.getFileName();
			AuxFileXL.createFolder(path);
		}
		path = path + "/" + contiType + "ContiInfo/";
		AuxFileXL.createFolder(path);
		return path;
	}
	
	
}
