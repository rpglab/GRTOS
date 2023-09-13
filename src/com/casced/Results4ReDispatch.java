package com.casced;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import com.powerdata.openpa.psse.ACBranchList;
import com.powerdata.openpa.psse.PsseModel;
import com.powerdata.openpa.psse.PsseModelException;
import com.sced.model.SystemModelXL;
import com.utilxl.log.DiaryXL;
import com.utilxl.log.LogTypeXL;

/**
 * 
 * Initialized in January 2017.
 * 
 * @author Xingpeng Li (xplipower@gmail.com)
 *
 */
public class Results4ReDispatch {

	DiaryXL _diary;
	PsseModel _model;
	ACBranchList _branches;
	
	PfResults4ReDispatch _pfresults;
	CAResults4ReDispatch _caresults;
	InterfaceVioResults _interfaceResults;

	public Results4ReDispatch(DiaryXL diary, PsseModel model, int nCtgcy) throws PsseModelException
	{
		_diary = diary;
		_model = model;
		_branches = _model.getBranches();

		_pfresults = new PfResults4ReDispatch(_model);
		_caresults = new CAResults4ReDispatch(_model, nCtgcy);
		
		_interfaceResults = new InterfaceVioResults(_model, this, nCtgcy);
		_pfresults.setInterfaceVioResults(_interfaceResults);
		_caresults.setInterfaceVioResults(_interfaceResults);
	}
	
	public void retrieveBaseCaseInfo4SCED(boolean conv) throws PsseModelException {
		_pfresults.retrieveBrcInfo4SCED(conv);
	}
	
	public void setCtgcyCaseInfo4SCED(int idxCtgcy, boolean conv) throws PsseModelException {
		_caresults.analyzeCAResults(idxCtgcy, conv);
	}


	/** Must be called before calling any get methods */
	public void cleanup() {
		_pfresults.cleanup();
		_caresults.cleanup();
		_interfaceResults.cleanup();
	}

	/* Get critical constraints info from the base-case power flow */
	public boolean isPfConverged() {return _pfresults.isPfConverged();}
	public int getPfConstSize() {return _pfresults.size();}
	public int[] getPfIdxBrc() {return _pfresults.getIdxBrc();}
	public BrcFlowMonitorType[] getPfBrcFlowMonitorType() {return _pfresults.getBrcFlowMonType();}
	public float[] getPfPk() {return _pfresults.getPk();}
	public float[] getPfPkLimit() {return _pfresults.getPkLimit();}
	public float[] getPfPkPercent() {return _pfresults.getPkPercent();}
	public float[] getPfRating() {return _pfresults.getRating();}
	/** Get the maximum of individual violation in percent */
	public float getMaxIndivdlPctVio4BaseCase() {return _pfresults.getMaxIndivdlPctVio();}
	/** Get the sum of MVA-flow-violation */
	public float getSumAmtVio4BaseCase() {return _pfresults.getSumAmtVio();}
	
	
	/* Get critical constraints info from the contingency-case power flow */
	public int getNonConvCtgcyPfSize() {return _caresults.sizeNconvCtgcy();}
	public int[] getNonConvCtgcyList() {return _caresults.getNonConvCtgcy();}
	public int getCaCtgcySize() {return _caresults.size();}
	public int[] getCaCriticalCtgcy() {return _caresults.getCriticalCtgcy();}
	public int[][] getCaIdxBrcCtgcy() {return _caresults.getIdxBrcCtgcy();}
	public BrcFlowMonitorType[][] getCABrcFlowMonitorType() {return _caresults.getBrcFlowMonType();}
	public float[][] getCaPkc() {return _caresults.getPkc();}
	public float[][] getCaPkcLimit() {return _caresults.getPkcLimit();}
	public float[][] getCaPkcPercent() {return _caresults.getPercent();}
	public float[][] getCaRating() {return _caresults.getRating();}
	/** Get the maximum of individual violation in percent */
	public float getMaxIndivdlPctVio4CtgcyCase() {return _caresults.getMaxIndivdlPctVio();}
	/** Get the sum of MVA flow violation*/
	public float getSumAmtVio4CtgcyCase() {return _caresults.getSumAmtVio();}
	
	
	/* Interface data */ 
	public int getSizeInterface() {return _interfaceResults.getSizeInterface();}
	public float[] getLimitBaseCase() {return _interfaceResults.getLimitBaseCase();}
	public int[][] getInterfaceLines() {return _interfaceResults.getInterfaceLines();}
	public float[][] getInterfaceEmgcyLimits() {return _interfaceResults.getInterfaceEmgcyLimits();}
	public boolean[][] getInterfaceLinesDirection() {return _interfaceResults.getInterfaceLinesDirection();}

	/* Interface base-case constraints */
	public boolean[] getIsInterfaceMonBaseCase() {return _interfaceResults.getIsInterfaceMonBaseCase();}
	public float[] getInterfaceMWBaseCase() {return _interfaceResults.getInterfaceMWBaseCase();}
	public float[] getPercentMWBaseCase() {return _interfaceResults.getPercentMWBaseCase();}
	public BrcFlowMonitorType[] getFlowMonTypeBaseCase() {return _interfaceResults.getFlowMonTypeBaseCase();}
	
	/* Interface contingency-case constraints */
	public int getNumCtgcy() {return _interfaceResults.getNumCtgcy();}
	public int[] getInterfaceCtgcyList() {return _interfaceResults.getCtgcyList();}
	public boolean[][] getIsInterfaceMonCtgcyCase() {return _interfaceResults.getIsInterfaceMonCtgcyCase();}
	public float[][][] getInterfaceLineMWCtgcyCase() {return _interfaceResults.getInterfaceLineMWCtgcyCase();}
	public float[][] getInterfaceMWCtgcyCase() {return _interfaceResults.getInterfaceMWCtgcyCase();}
	public float[][] getPercentMWCtgcyCase() {return _interfaceResults.getPercentMWCtgcyCase();}
	public BrcFlowMonitorType[][] getFlowMonTypeCtgcyCase() {return _interfaceResults.getFlowMonTypeCtgcyCase();}
	public int[][] getMonInterfaceLinesCtgcyCase() {return _interfaceResults.getMonInterfaceLinesCtgcyCase();}
	public float[][] getPkcInitInterfaceLinesCtgcyCase() {return _interfaceResults.getPkcInitInterfaceLinesCtgcyCase();}

	
	public void dump(boolean isNm1Check) throws PsseModelException
	{
		String fileName = "ConstraintsFromCA2SCED.csv";
		if (isNm1Check == true) fileName = "Nm1CheckResults.csv";
		File pout = new File(fileName);
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(new BufferedWriter(new FileWriter(pout)));
		} catch (IOException e) {
			e.printStackTrace();
		}
		dump(pw, isNm1Check);
		pw.flush();
		pw.close();
		_diary.hotLine(LogTypeXL.Log, "File "+fileName+" has been created");
		
		_interfaceResults.dump(isNm1Check);
	}
	
	public void dump(PrintWriter pw, boolean isNm1Check) throws PsseModelException
	{
		if (isNm1Check == false) {
			pw.format("//---\n This file contains branch flow constraints\n");
			pw.format("They are obtained from base-case power flow study and contingency analysis study\n");
			pw.format("These contraints will be sent to DC power flow model based SCED simulation\n\n");
		} else pw.format("//---\n Results of base-case power flow check and N-1 feasibility check\n\n");
		
		pw.format("//--- base case\n _tol4BrcFlowMonitorBaseCase = %f\n", _pfresults.getTol4BrcFlowMonitorBaseCase());
		pw.format("_tol4BrcFlowWarningBaseCase = %f\n", _pfresults.getTol4BrcFlowWarningBaseCase());
		pw.format("_tol4BrcFlowVioBaseCase = %f\n\n", _pfresults.getTol4BrcFlowVioBaseCase());
		
		pw.format("//--- ctgcy case\n _tol4BrcFlowMonitorCtgcyCase = %f\n", _caresults.getTol4BrcFlowMonitorCtgcyCase());
		pw.format("_tol4BrcFlowWarningCtgcyCase = %f\n", _caresults.getTol4BrcFlowWarningCtgcyCase());
		pw.format("_tol4BrcFlowVioCtgcyCase = %f\n\n", _caresults.getTol4BrcFlowVioCtgcyCase());
		
		pw.format("//---\n RateA is used as the limit for base case study\n"); 
		pw.format("RateC is used as the limit for contingency case study\n"); 
		
		pw.format("Note that if base-case power flow does not converge - contingency analysis run will be skipped \n");

		if (isPfConverged() == true) pw.format("Fortunately base-case power flow does converge\n\n");
		else {pw.format("Unfortunately - base-case power flow does NOT converge\n\n"); return;}
		
		if (getNonConvCtgcyPfSize() != 0) {
			String mess = "# of non-converged contingency power flow runs is: "+getNonConvCtgcyPfSize();
			pw.println(mess); _diary.hotLine(mess);
			int[] nonConvCtgcy = getNonConvCtgcyList();
			for (int c=0; c<nonConvCtgcy.length; c++)
				pw.println("Branch contingency "+(nonConvCtgcy[c]+1)+ " does not converge");
		} else {
			String mess = "All contingency power flow runs converged";
			pw.println(mess); _diary.hotLine(mess);
		}
		pw.println();
		
		pw.println("idx,caseType,ctgcyBrcIdx,ctgcyBrcID,monitorBrcIdx,monitorBrcID,monitorType,initFlowPfrmMW,limitMW,brcLoadingCondition,percent,ratingMVA,vioMVA");
		float[] percentPF = getPfPkPercent();
		for (int i=0; i<getPfConstSize(); i++)
		{
			int idxMonBrc = getPfIdxBrc()[i];
			String brcFlowSt = SystemModelXL.getBrcFlowFlag(percentPF[i]);
			
			pw.format("%d,%s,%d,%s,%d,%s,%s,%f,%f,%s,%f,%f,%f\n",
					(i+1),
					"baseCase",
					-1,
					"NA/Null",
					(idxMonBrc+1),
					_branches.get(idxMonBrc).getObjectID(),
					getPfBrcFlowMonitorType()[i].toString(),
					getPfPk()[i],
					getPfPkLimit()[i],
					brcFlowSt,
					getPfPkPercent()[i],
					getPfRating()[i],
					getPfRating()[i] * (percentPF[i] - 1) );
		}

		int count = getPfConstSize();
		for (int i=0; i<getCaCtgcySize(); i++)
		{
			int[] idxBrc = getCaIdxBrcCtgcy()[i];
			BrcFlowMonitorType[] flowMonType = getCABrcFlowMonitorType()[i];
			float[] pkc = getCaPkc()[i];
			float[] limit = getCaPkcLimit()[i];
			float[] percent = getCaPkcPercent()[i];
			float[] rating = getCaRating()[i];
			int size = idxBrc.length;
			
			for (int j=0; j<size; j++)
			{
				count++;
				int idxCtgcyBrc = getCaCriticalCtgcy()[i];
				int idxMonBrc = idxBrc[j];
				String brcFlowSt = SystemModelXL.getBrcFlowFlag(percent[j]);

				pw.format("%d,%s,%d,%s,%d,%s,%s,%f,%f,%s,%f,%f,%f\n",
						count,
						"ctgcyCase",
						(idxCtgcyBrc+1),
						_branches.get(idxCtgcyBrc).getObjectID(),
						(idxMonBrc+1),
						_branches.get(idxMonBrc).getObjectID(),
						flowMonType[j].toString(),
						pkc[j],
						limit[j],
						brcFlowSt,
						percent[j],
						rating[j],
						rating[j] * (percent[j] - 1) );
			}
		}
	}
	
}
