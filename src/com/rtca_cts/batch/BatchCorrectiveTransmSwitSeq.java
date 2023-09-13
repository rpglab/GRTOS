package com.rtca_cts.batch;

import com.powerdata.openpa.psse.PsseModel;
import com.powerdata.openpa.psse.powerflow.VoltageSource;
import com.rtca_cts.contingencyanalysis.ContingencyAnalysisSeq;
import com.rtca_cts.transmissionswitching.CorrectiveTransmSwit;
import com.utilxl.iofiles.ReadFilesInAFolder;
import com.utilxl.log.DiaryXL;

/**
 * TS program for multiple input .raw files;
 * 
 * Initialized in Sep, 2014.
 * 
 * @author Xingpeng.Li, xplipower@gmail.com 
 */
public class BatchCorrectiveTransmSwitSeq {
	
	
	
	public static void main(String[] args) throws Exception
	{
		/** Log file creation */
		DiaryXL diary = new DiaryXL();
		diary.initial();
		
		long t_Start = System.nanoTime();
		String uri = null;		
		
		ReadFilesInAFolder readFiles = new ReadFilesInAFolder("dataToRead", false);
		readFiles.readFileNames();
		String[] fileNames = readFiles.getAllRawFileNames();

		for (int i=0; i<fileNames.length; i++)
		{
			long t_StartOneFile = System.nanoTime();			
			uri = "psseraw:file="+readFiles.getPath()+fileNames[i]+"&lowx=adjust";
			PsseModel model = PsseModel.Open(uri);
			model.setMinXMag(0.00001f);
			model.setDiary(diary);
			float[] vmBase = null;
			float[] vaBase = null;
			int[] idxBrcContToBeChecked = null;
			int[] idxGenContToBeChecked = null;
			{
				ContingencyAnalysisSeq ca = new ContingencyAnalysisSeq(model, VoltageSource.RealTime);
				ca.setMarkUsingVgSched(false); ca.setRealTimeStartForGenBus(true);
				ca.setShowRunInfo(false);
				
				System.out.println("    Running Generator contingency analysis...");
			    ca.runGenContingencyAnalysis();
				System.out.println("    Generator contingency analysis is finished.");
				System.out.println("      Gen CA time : " + (System.nanoTime() - t_StartOneFile)/1e9f);
				
				System.out.println("    Running Transmission contingency analysis...");
			    ca.runTransmContingencyAnalysis();
				System.out.println("    Transmission contingency analysis is finished.");
				System.out.println("      (Gen + Transm) CA time : " + (System.nanoTime() - t_StartOneFile)/1e9f);
				
				vmBase = ca.getVmBasePf();
				vaBase = ca.getVaBasePf();
				idxBrcContToBeChecked = ca.getIdxContVioVmAndBrcAllTransm();
				idxGenContToBeChecked = ca.getIdxContVioVmAndBrcAllGen();
				if (idxBrcContToBeChecked == null) idxBrcContToBeChecked = new int[0];
				if (idxGenContToBeChecked == null) idxGenContToBeChecked = new int[0];
			}
			System.out.println();
			System.out.println("Number of Generator    Contingency to be checked with TS is: "+idxGenContToBeChecked.length);
			System.out.println("Number of Transmission Contingency to be checked with TS is: "+idxBrcContToBeChecked.length);
			
			CorrectiveTransmSwit engineCTS = new CorrectiveTransmSwit(model);
			engineCTS.setContToCheckTransm(idxBrcContToBeChecked);
			engineCTS.setContToCheckGen(idxGenContToBeChecked);
			//engineCTS.setMarkUsingVgSched(true);
			//engineCTS.setBrcNotCheckTS(idxRadBrc);
			engineCTS.initial(t_Start, uri, VoltageSource.LastSolved, vmBase, vaBase);
			engineCTS.runTS();
			float timeSol = (System.nanoTime() - t_StartOneFile)/1e9f;
			engineCTS.outputResults(timeSol);
			
			System.out.println("    Solution time corresponding to this raw file: " + (System.nanoTime() - t_StartOneFile)/1e9f);
			System.out.println("    Total time until now: " + (System.nanoTime() - t_Start)/1e9f + "\n\n ");
		}
		System.out.println("    Total time : " + (System.nanoTime() - t_Start)/1e9f);	
		
		/** Program ends here */
		diary.done();
		System.out.println("Program ends here.");
	}


}
