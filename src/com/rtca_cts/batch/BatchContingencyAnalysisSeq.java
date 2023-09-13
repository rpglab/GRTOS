package com.rtca_cts.batch;

import com.powerdata.openpa.psse.PsseModel;
import com.powerdata.openpa.psse.powerflow.VoltageSource;
import com.rtca_cts.contingencyanalysis.ContingencyAnalysisSeq;
import com.utilxl.iofiles.ReadFilesInAFolder;

/**
 * CA program for multiple input .raw files;
 * 
 * Initialized in June 2014.
 * 
 * @author Xingpeng.Li, xplipower@gmail.com 
 */
public class BatchContingencyAnalysisSeq {

	
	public static void main(String[] args) throws Exception
	{
		long t_Start = System.nanoTime();
		String uri = null;
		float minxmag = 0.00001f;
		
		String svstart = "Flat";
		VoltageSource vstart = VoltageSource.fromConfig(svstart);
		
		//ReadFilesInAFolder readFiles = new ReadFilesInAFolder("M://MPJWorkspace//debug_20140214//dataToRead", true);
		//ReadFilesInAFolder readFiles = new ReadFilesInAFolder("M:/MPJWorkspace3/CA_003/dataToRead", true);
		ReadFilesInAFolder readFiles = new ReadFilesInAFolder("dataToRead", false);
		readFiles.readFileNames();
		String[] fileNames = readFiles.getAllRawFileNames();
		
		for(int idx=0; idx<fileNames.length; idx++)
		{
			System.out.println("We are running simulation for the idx "+(idx+1)+"-th raw file (Total # : "+fileNames.length+" ). ");
			long t_Start_dummy = System.nanoTime();
			uri = "psseraw:file="+readFiles.getPath()+fileNames[idx]+"&lowx=adjust";
			PsseModel model = PsseModel.Open(uri);
			model.setMinXMag(minxmag);
			
			ContingencyAnalysisSeq ca = new ContingencyAnalysisSeq(model, vstart);
			ca.setMarkUsingVgSched(false);
			ca.setPath(ca.getPath()+fileNames[idx]+"/");
			ca.setURI(uri);
			ca.setShowRunInfo(false);
			
//			boolean conv = ca.convOR();
//			if (conv) System.out.println("Power flow for Base case can converge..");
//			else System.err.println("Power flow for Base case cannot converge..");
//			if (conv)
			{
				System.out.println("     Start running CA for raw file: "+fileNames[idx]);
			    ca.runGenContingencyAnalysis();
			    System.out.println("     CA simulation for Generator Contingency is done.");
			    ca.runTransmContingencyAnalysis();
				System.out.println("     CA simulation is done. Solution time is:"+(System.nanoTime() - t_Start_dummy)/1e9f);
			}
			long t_EndCA = System.nanoTime();
			
//			ca.outputVioInfoBaseCase();
			ca.outputVioInfoTransmCont();
			ca.outputVioInfoGenCont();

			long t_EndOutputCA = System.nanoTime();
			
			float[] times = new float[2];
			times[0] = (t_EndCA - t_Start_dummy)/1e9f;
			times[1] = (t_EndOutputCA - t_Start_dummy)/1e9f;
			
			ca.outputGeneralInfoTmp(times);

			System.out.println("     Total solution time for now : " + (System.nanoTime() - t_Start)/1e9f);	
			System.out.println();
		}
		System.out.println();
		System.out.println("Total solution time : " + (System.nanoTime() - t_Start)/1e9f);	
	}
}
