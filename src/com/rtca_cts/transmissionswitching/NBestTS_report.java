package com.rtca_cts.transmissionswitching;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import com.powerdata.openpa.psse.ACBranchList;
import com.powerdata.openpa.psse.PsseModel;
import com.powerdata.openpa.psse.PsseModelException;
import com.rtca_cts.param.ParamIO;
import com.rtca_cts.param.ParamTS;
import com.utilxl.array.AuxArrayXL;
import com.utilxl.iofiles.AuxFileXL;

public class NBestTS_report
{
	
	PsseModel _model;
	ACBranchList _branches;
	
//	int[] _contiInfo;   // It has three integer numbers. Note this array saves the contingency data by number instead of index.
//                        // If brc conti: brcIdx+1, brcFrmBusNumber, brcToBusNumber; if gen conti: genIdx+1, genBusNumber, -1. 
//	
//	float[][] _sumVioTS;      // sum of violation for each TS checked, two elems: _sumBrcVio, _sumVmVio.
//	float[][] _sumVioTSImp;      //  It equals to (SumContiVio - SumTSVio) / SumContiVio; it is 0 if SumContiVio == 0.
//                                 // two elems: _sumBrcVioImp, _sumVmVioImp.

	int _reportBestNumTS = ParamTS.getReportBestNumTS();  // report the best N switching actions for each contingency.

	String _fileNameTransm = "BestTS_Transm_";
	String _fileNameGen = "BestTS_Gen_";
	
	PrintStream[] _outFileTransm;
	PrintStream[] _outFileTransm_Pareto;
	
	PrintStream[] _outFileGen;
	PrintStream[] _outFileGen_Pareto;

	public NBestTS_report(PsseModel model) throws PsseModelException {_model = model; _branches =  _model.getBranches();}
	
	public void initPrint() throws FileNotFoundException
	{
		_outFileTransm = new PrintStream[_reportBestNumTS];
		_outFileTransm_Pareto = new PrintStream[_reportBestNumTS];

		_outFileGen = new PrintStream[_reportBestNumTS];
		_outFileGen_Pareto = new PrintStream[_reportBestNumTS];

		String path = ParamIO.getTSPath();
		AuxFileXL.createFolder(path);
		for (int i=0; i<_reportBestNumTS; i++)
		{
			String fileNameTransm = _fileNameTransm + (i+1) + ".txt";
			_outFileTransm[i] = new PrintStream (new FileOutputStream(path + fileNameTransm, true), true);
			String fileNameTransm_P = _fileNameTransm + (i+1) + "_Pareto.txt";
			_outFileTransm_Pareto[i] = new PrintStream (new FileOutputStream(path + fileNameTransm_P, true), true);
			
			String fileNameGen = _fileNameGen + (i+1) + ".txt";
			_outFileGen[i] = new PrintStream (new FileOutputStream(path + fileNameGen, true), true);
			String fileNameGen_P = _fileNameGen + (i+1) + "_Pareto.txt";
			_outFileGen_Pareto[i] = new PrintStream (new FileOutputStream(path + fileNameGen_P, true), true);
		}
	}
	
	public void initTitle(String uri)
	{
		for (int i=0; i<_reportBestNumTS; i++)
		{
			_outFileTransm[i].println("  "+uri);
			_outFileTransm_Pareto[i].println("  "+uri);
			_outFileGen[i].println("  "+uri);
			_outFileGen_Pareto[i].println("  "+uri);
		}
	}

	void writeNBestTS(int[] TSList, int[][] indicatorIndiElemVioCheck, float[][] sumVioTSImp, int[] contiInfo, float sumContiBrcVio, float sumContiVmVio) throws PsseModelException
	{
		int[] flagPareto = chechParetoFlag(indicatorIndiElemVioCheck);
		float[][] sumVioTSImpT = AuxArrayXL.transposeArray(sumVioTSImp);
		writeNBestTS(TSList, flagPareto, sumVioTSImpT, contiInfo, sumContiBrcVio, sumContiVmVio, false);
		for (int i=0; i<flagPareto.length; i++)
			if (flagPareto[i] == 1) {sumVioTSImpT[0][i] = -8; sumVioTSImpT[1][i] = -8;}
		writeNBestTS(TSList, flagPareto, sumVioTSImpT, contiInfo, sumContiBrcVio, sumContiVmVio, true);
	}

	void writeNBestTS(int[] TSList, int[] flagPareto, float[][] sumVioTSImpT, int[] contiInfo, float sumContiBrcVio, float sumContiVmVio, boolean isPareto) throws PsseModelException
	{
		int numTS = _reportBestNumTS;
		if (TSList == null) numTS = 0;
		else if (numTS > TSList.length) numTS = TSList.length;
		
		String typeCont = "branch";
		PrintStream[] outFile = _outFileTransm;
		if (isPareto == true) outFile = _outFileTransm_Pareto;
		if (contiInfo[2] == -1)
			{typeCont = "gen"; outFile = (isPareto == true) ? _outFileGen_Pareto :_outFileGen;}
		int[] tmp1 = {-1, -1, -1, 0, 0, -6, -1};
		int[] tmp2 = {-1, -1, -1, 0, 0, -6, -1};
		int[] tmp3 = {-1, -1, -1, 0, 0, 0, -6, -1};

		if (numTS != 0) {
			int[] idxBestTS_BrcVio = AuxArrayXL.getIdxOfMaxElems(sumVioTSImpT[0], sumVioTSImpT[1], numTS);
			int[] idxBestTS_VmVio = AuxArrayXL.getIdxOfMaxElems(sumVioTSImpT[1], sumVioTSImpT[0], numTS);

			int size = sumVioTSImpT[0].length;
			float[] sumVioImp = new float[size];
			for (int i=0; i<size; i++)
				sumVioImp[i] = sumVioTSImpT[0][i] + sumVioTSImpT[1][i];
			int[] idxBestTS_BothVio = AuxArrayXL.getIdxOfMaxElems(sumVioImp, numTS);
			
			for (int i=0; i<numTS; i++)
			{
				outFile[i].print("  "+typeCont);
				outFile[i].print("  "+contiInfo[0]);
				outFile[i].print("  "+contiInfo[1]);
				outFile[i].print("  "+contiInfo[2]);
				outFile[i].print("  "+sumContiBrcVio);
				outFile[i].print("  "+sumContiVmVio);

				int idxBrcVio = idxBestTS_BrcVio[i];
				boolean mark = false;
				if (sumVioTSImpT[0][idxBrcVio] > 0) mark = true;
				else if (sumVioTSImpT[0][idxBrcVio] == 0 && sumVioTSImpT[1][idxBrcVio] > 0) mark = true; 
				if (mark == true)
				{
					int idxTSBrcVio = TSList[idxBrcVio];
					outputBrcInfo(outFile[i], idxTSBrcVio);
				    outputElemFrmArrayPerct(outFile[i], new float[] {sumVioTSImpT[0][idxBrcVio], sumVioTSImpT[1][idxBrcVio]});
					outFile[i].print(" "+flagPareto[idxBrcVio]); outFile[i].print(" "+(idxBrcVio+1));
				}
				else outputElemFrmArrayPerct(outFile[i], tmp1);
				
				int idxVmVio = idxBestTS_VmVio[i];
				mark = false;
				if (sumVioTSImpT[1][idxVmVio] > 0) mark = true;
				else if (sumVioTSImpT[1][idxVmVio] == 0 && sumVioTSImpT[0][idxVmVio] > 0) mark = true; 
				if (mark == true)
				{
					int idxTSVmVio = TSList[idxVmVio];
					outputBrcInfo(outFile[i], idxTSVmVio);
				    outputElemFrmArrayPerct(outFile[i], new float[] {sumVioTSImpT[1][idxVmVio], sumVioTSImpT[0][idxVmVio]});
					outFile[i].print(" "+flagPareto[idxVmVio]); outFile[i].print(" "+(idxVmVio+1));
				}
				else outputElemFrmArrayPerct(outFile[i], tmp2);

				int idxBothVio = idxBestTS_BothVio[i];
				if (sumVioImp[idxBothVio] > 0)
				{
					int idxTSBothVio = TSList[idxBothVio];
					outputBrcInfo(outFile[i], idxTSBothVio);
				    outputElemFrmArrayPerct(outFile[i], new float[] {sumVioImp[idxBothVio], sumVioTSImpT[0][idxBothVio], sumVioTSImpT[1][idxBothVio]});
					outFile[i].print(" "+flagPareto[idxBothVio]); outFile[i].print(" "+(idxBothVio+1));
				}
				else outputElemFrmArrayPerct(outFile[i], tmp3);
				outFile[i].println();
			}
		}
		for (int i=numTS; i<_reportBestNumTS; i++)
		{
			outFile[i].print("  "+typeCont);
			outFile[i].print("  "+contiInfo[0]);
			outFile[i].print("  "+contiInfo[1]);
			outFile[i].print("  "+contiInfo[2]);
			outFile[i].print("  "+sumContiBrcVio);
			outFile[i].print("  "+sumContiVmVio);
			outputElemFrmArrayPerct(outFile[i], tmp1);
			outputElemFrmArrayPerct(outFile[i], tmp2);
			outputElemFrmArrayPerct(outFile[i], tmp3);
			outFile[i].println();
		}
	}
	
	private int[] chechParetoFlag(int[][] indicatorIndiElemVioCheck)
	{
		int size = indicatorIndiElemVioCheck.length;
		int[] flagPareto = new int[size];
		for (int i=0; i<size; i++)
		{
			if (indicatorIndiElemVioCheck[i][0] == -2) {flagPareto[i] = -2; continue;}
			if (indicatorIndiElemVioCheck[i][0] == -3) {flagPareto[i] = -3; continue;}
			if (indicatorIndiElemVioCheck[i][0] == -4) {flagPareto[i] = -4; continue;}
			if (indicatorIndiElemVioCheck[i][0] == -5) {flagPareto[i] = -5; continue;}
			if (isArrayOneSameElem(indicatorIndiElemVioCheck[i], -1) == true) {flagPareto[i] = -1; continue;}
			for (int j=0; j<indicatorIndiElemVioCheck[i].length; j++)
				if (indicatorIndiElemVioCheck[i][j] == 1) {flagPareto[i] = 1; break;}
		}
		return flagPareto;
	}
	
	boolean isArrayOneSameElem(int[] array, int elem)
	{
		for (int i=0; i<array.length; i++)
			if (array[i] != elem) return false;
		return true;
	}
	
	private void outputBrcInfo(PrintStream outFile, int idxBrc) throws PsseModelException
	{
		int frmBusNum = _branches.get(idxBrc).getFromBus().getI();
		int toBusNum = _branches.get(idxBrc).getToBus().getI();
	    outFile.print("  "+(idxBrc + 1));
	    outFile.print("  "+frmBusNum);
	    outFile.print("  "+toBusNum);
	}
	
	void outputElemFrmArrayPerct(PrintStream outFile, float[] a)
	{
		for (int i=0; i<a.length; i++)
			outFile.print("  "+(a[i]*100)+"%");
	}

	void outputElemFrmArrayPerct(PrintStream outFile, int[] a)
	{
		for (int i=0; i<a.length; i++)
			outFile.print("  "+(a[i]*100)+"%");
	}
	
	/** Turn off PrintStream. */
	public void closeTSReport()
	{
		for (int i=0; i<_reportBestNumTS; i++)
		{
			_outFileTransm[i].close();
			_outFileTransm_Pareto[i].close();
			_outFileGen[i].close();
			_outFileGen_Pareto[i].close();
		}
	}
	
	public void setFileNameTransm(String name) {_fileNameTransm = name;}
	public void setFileNameGen(String name) {_fileNameGen = name;}

}
