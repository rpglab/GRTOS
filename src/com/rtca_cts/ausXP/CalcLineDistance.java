package com.rtca_cts.ausXP;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import com.powerdata.openpa.psse.ACBranchList;
import com.powerdata.openpa.psse.PsseModel;
import com.powerdata.openpa.psse.PsseModelException;
import com.rtca_cts.ausData.BusGroupElems;
import com.rtca_cts.param.ParamIO;
import com.utilxl.array.AuxArrayXL;
import com.utilxl.iofiles.AuxFileXL;
import com.utilxl.iofiles.OutputArrays;
import com.utilxl.iofiles.ReadFilesInAFolder;
import com.utilxl.iofiles.ReadMultiCollumIntData;

/**
 * Initialized in Feb. 2016.
 * 
 * @author Xingpeng.Li (xplipower@gmail.com)
 *
 */
public class CalcLineDistance {

	PsseModel _model;
	ACBranchList _branches;
	BusGroupElems _busGroupElems = null;
	
	boolean _checkBrcOnline = true;

	public CalcLineDistance(PsseModel model) throws PsseModelException
	{
		_model = model;
		_branches = _model.getBranches();
		_busGroupElems = _model.getBusGroupElems();
	}
	
	public int calcLineDistance_GenCtgcy(int idxSrcBrc, int idxDestGen) throws PsseModelException
	{
		return calcLineDistance(idxSrcBrc, idxDestGen, true);
	}

	public int calcLineDistance_BrcCtgcy(int idxSrcBrc, int idxDestBrc) throws PsseModelException
	{
		return calcLineDistance(idxSrcBrc, idxDestBrc, false);
	}

	private int calcLineDistance(int idxSrcBrc, int idxDestElem, boolean isDestGenCtgcy) throws PsseModelException
	{
		int frmBusIdxSrc = _branches.getFromBus(idxSrcBrc).getIndex();
		int toBusIdxSrc = _branches.getToBus(idxSrcBrc).getIndex();
		if (isDestGenCtgcy == false) {
			int frmBusIdxDest = _branches.getFromBus(idxDestElem).getIndex();
			int toBusIdxDest = _branches.getToBus(idxDestElem).getIndex();
			return calcLineDistance(new int[] {frmBusIdxSrc, toBusIdxSrc}, new int[] {frmBusIdxDest, toBusIdxDest});
		} else {
			int genBusIdxDest = _model.getGenerators().getBus(idxDestElem).getIndex();
			return calcLineDistance(new int[] {frmBusIdxSrc, toBusIdxSrc}, new int[] {genBusIdxDest});
		}
	}

	public int calcLineDistance(int[] idxSrcBuses, int[] idxDestBuses) throws PsseModelException
	{
		ArrayList<Integer> idxSrcBusesList = new ArrayList<Integer>();
		for (int i=0; i<idxSrcBuses.length; i++)
			idxSrcBusesList.add(idxSrcBuses[i]);
		return calcLineDistance(idxSrcBusesList, idxDestBuses);
	}
	
	/** return line-distance of the shortest path from @idxSrcBuses to @idxDstBuses
	 * @throws PsseModelException */
	public int calcLineDistance(ArrayList<Integer> idxSrcBuses, int[] idxDestBuses) throws PsseModelException
	{
		int lineDistance = 0;
		ArrayList<Integer> idxBrcNearby = new ArrayList<Integer>();
		for (int i=0; i<idxDestBuses.length; i++) {
			if (idxSrcBuses.contains(idxDestBuses[i])) return lineDistance;
		}
		
		long t_start = System.nanoTime();
		do {
			lineDistance++;
		    if ((System.nanoTime() - t_start)/1e9f > 2)
		    	{System.out.println(" Time's up for determining the bus distance."); break;}

			ArrayList<Integer> idxNewBuses = new ArrayList<Integer>();
			for (int i=0; i<idxSrcBuses.size(); i++)
			{
				int idxBus = idxSrcBuses.get(i);
				int[] idxBrcs = _busGroupElems.getBrcIndex(idxBus);
				int numBrcSatisfied = 0;
				for (int j=0; j<idxBrcs.length; j++)
				{
					int idxBrc = idxBrcs[j];
					if (idxBrcNearby.contains(idxBrc) == false)
					{
						boolean markBrc = true;
						if (_checkBrcOnline == true) markBrc = _branches.isInSvc(idxBrc);  // must before areaCheck and radialBrcCheck.
//						if (_checkArea == true && markBrc == true) markBrc = _isBranchInArea[idxBrc];
//						if (_checkRadial == true && markBrc == true) markBrc = !_radialCheck.isBrcRadialBrc(idxBrc);
						if (markBrc == true)
							{ idxBrcNearby.add(idxBrc); numBrcSatisfied++;}

						int otherEndBusIdx = findOtherBrcEndBusIdx(idxBrc, idxBus);
//						boolean markAgain = true;
//						if (_checkArea == true) markAgain = _isBusInArea[otherEndBusIdx];
//						if (markAgain == true)
//						{
							if (idxNewBuses.contains(otherEndBusIdx) == false) idxNewBuses.add(otherEndBusIdx);
//						}
					}
				}
//				if (idxBrcNearby.size() >= num) break;
				if (numBrcSatisfied == 0) idxNewBuses.remove((Integer) idxBus);
			}
			
			for (int i=0; i<idxDestBuses.length; i++) {
				int idxDestBus = idxDestBuses[i];
//				if (idxNewBuses.indexOf((Integer) idxDestBus) == -1) continue;
				if (idxNewBuses.contains(idxDestBus) == false) continue;
				else return lineDistance;
			}
			idxSrcBuses = idxNewBuses;
		} while(true);
		return -1;
	}

	
	/** Given branch index and one end-bus index, the other end-bus index will be returned.
	 * @throws PsseModelException       */
	public int findOtherBrcEndBusIdx(int idxBrc, int oneEndBusIdx) throws PsseModelException
	{
		int idxBus = _branches.getFromBus(idxBrc).getIndex();
		if (idxBus == oneEndBusIdx) idxBus = _branches.getToBus(idxBrc).getIndex();
		return idxBus;
	}

	
	public static void main(String[] args) throws PsseModelException, IOException
	{
		long t_Start = System.nanoTime();
		String uri = null;
		
		ReadMultiCollumIntData BrcTSinfo = new ReadMultiCollumIntData(7895, 17, "dataToRead\\rawDataForDistance_Transm.txt", false);
		ReadMultiCollumIntData GenTSinfo = new ReadMultiCollumIntData(169, 17, "dataToRead\\rawDataForDistance_Gen.txt", false);
		int[][] Tm1Info = BrcTSinfo.getArrayByRow();
		int[][] Gm1Info = GenTSinfo.getArrayByRow();

		ReadFilesInAFolder readFiles = new ReadFilesInAFolder("C:/Users/xingpeng/data/PJMSystem/raw", true);
//		ReadFilesInAFolder readFiles = new ReadFilesInAFolder("dataToRead", false);
		readFiles.readFileNames();
		String[] fileNames = readFiles.getAllRawFileNames();
		AuxFileXL.createFolder(ParamIO.getOutPath());
		OutputArrays.outputArray(fileNames, ParamIO.getOutPath() + "fileToBeChecked.txt", true, true, true);
				
		String outFileName_Transm = "distance_Transm.txt";
	    OutputStream resultFile_Transm = new FileOutputStream(outFileName_Transm, false);
	    PrintStream outFile_Transm = new PrintStream (resultFile_Transm);
	    
		String outFileName_Gen = "distance_Gen.txt";
	    OutputStream resultFile_Gen = new FileOutputStream(outFileName_Gen, false);
	    PrintStream outFile_Gen = new PrintStream (resultFile_Gen);

		for (int i=0; i<fileNames.length; i++)
		{
			String hourStr = fileNames[i].substring(0, fileNames[i].length()-4);
			int hourNum = Integer.parseInt(hourStr);
			if (hourNum > 167 || hourNum < 144) continue;
			
			System.out.println("\n\n******************** New raw file is loaded ***************************");
			uri = "psseraw:file="+readFiles.getPath()+fileNames[i]+"&lowx=adjust";
			PsseModel model = PsseModel.Open(uri);
			CalcLineDistance calcBrcDis = new CalcLineDistance(model);
			
			int[] idxTm1DataInterested = AuxArrayXL.getIdxOfRowWithSameMark(Tm1Info, hourNum);
			int[] idxGm1DataInterested = AuxArrayXL.getIdxOfRowWithSameMark(Gm1Info, hourNum);

   	   	 	for (int iT=0; iT<idxTm1DataInterested.length; iT++)
			{
				int idxRow = idxTm1DataInterested[iT];
				if (hourNum != Tm1Info[idxRow][0]) System.exit(0);
				int idxCtgcy = Tm1Info[idxRow][1];
				outFile_Transm.print(" " + hourNum + " " + idxCtgcy);
				for (int jj=2; jj<17; jj++) {
					int idxTS = Tm1Info[idxRow][jj];
					if (idxTS < 0) {
				    	outFile_Transm.print(" -1" + " -1");
				    	continue;
					}
					int distance = calcBrcDis.calcLineDistance_BrcCtgcy(idxTS-1, idxCtgcy-1);
			    	outFile_Transm.print(" " + idxTS + " " + distance);
				}
			    outFile_Transm.println();
			}

   	   	 	for (int iG=0; iG<idxGm1DataInterested.length; iG++)
			{
				int idxRow = idxGm1DataInterested[iG];
				if (hourNum != Gm1Info[idxRow][0]) System.exit(0);
				int idxCtgcy = Gm1Info[idxRow][1];
				outFile_Gen.print(" " + hourNum + " " + idxCtgcy);
				for (int jj=2; jj<17; jj++) {
					int idxTS = Gm1Info[idxRow][jj];
					if (idxTS < 0) {
						outFile_Gen.print(" -1" + " -1");
				    	continue;
					}
					int distance = calcBrcDis.calcLineDistance_GenCtgcy(idxTS-1, idxCtgcy-1);
					outFile_Gen.print(" " + idxTS + " " + distance);
				}
				outFile_Gen.println();
			}
			
		}
	    outFile_Transm.close();
	    resultFile_Transm.close();
	    
	    outFile_Gen.close();
	    resultFile_Gen.close();

		System.out.println("\nTotal simulation time is: " + (System.nanoTime() - t_Start)/1e9);
		System.out.println("Simulation is done here!");

	}
	
}
