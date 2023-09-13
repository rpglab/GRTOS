package com.rtca_cts.batch;

import mpi.MPI;

import com.powerdata.openpa.psse.PsseModel;
import com.powerdata.openpa.psse.powerflow.VoltageSource;
import com.rtca_cts.ausXP.PsseRaw2CSV;
import com.rtca_cts.contingencyanalysis.ContingencyAnalysis;
import com.rtca_cts.contingencyanalysis.ContingencyAnalysisParallel;
import com.utilxl.iofiles.AuxFileXL;
import com.utilxl.iofiles.ReadFilesInAFolder;
import com.utilxl.string.AuxStringXL;

/**
 * CA program for multiple input .raw files;
 * Parallel version.
 * 
 * Initialized in June 2014.
 * 
 * @author Xingpeng.Li, xplipower@gmail.com 
 */
public class BatchContingencyAnalysisParallel {

	
	public static void main(String[] args) throws Exception
	{		
	    MPI.Init(args);
	    if (MPI.Initialized() == false)
	    	System.out.println("  Error Happens when starting MPI program.");
		long t_Start = System.nanoTime();
	    int nproc = MPI.COMM_WORLD.Size();
        int rank = MPI.COMM_WORLD.Rank();
        if (rank == 0) System.out.println(" \nNumber of threads invoked : "+nproc + "\n ");
        
		String uri = null;
		
		float minxmag = 0.0001f;
		String svstart = "Flat";
		VoltageSource vstart = VoltageSource.fromConfig(svstart);

		ReadFilesInAFolder readFiles = new ReadFilesInAFolder("dataToRead", false);
		readFiles.readFileNames();
		String[] fileNames = readFiles.getAllRawFileNames();
		
		for(int idx=0; idx<fileNames.length; idx++)
		{
			if (rank == 0) System.out.println("We are running simulation for the idx "+(idx+1)+"-th raw file (Total # : "+fileNames.length+" ). ");
			long t_Start_dummy = System.nanoTime();
			String inputRawFile = readFiles.getPath()+"/"+fileNames[idx];
			String outputCSVPath = System.getProperty("user.dir") + "/casesCSVFormat/";
			if (rank == 0) AuxFileXL.createFolder(outputCSVPath);
			outputCSVPath = outputCSVPath+fileNames[idx];
			if (rank == 0)
			{
				AuxFileXL.createFolder(outputCSVPath);
				PsseRaw2CSV.launch(inputRawFile, outputCSVPath);
			}
			MPI.COMM_WORLD.Barrier();    // other threads should and have to wait until all the CSV files are created. 
			uri = AuxStringXL.getUriForOpenPA(outputCSVPath, true);
			PsseModel model = PsseModel.Open(uri, rank);
			model.setMinXMag(minxmag);
			
			ContingencyAnalysis ca = new ContingencyAnalysisParallel(model, vstart, nproc, rank);
			ca.setMarkUsingVgSched(true);
			
			ca.setPath(ca.getPath()+fileNames[idx]+"/");
			ca.setURI(uri);
			ca.setShowRunInfo(false);
			
			//boolean conv = ca.convOR();		
			//if(conv)
			{
				if(rank == 0) System.out.println("     Rank_0 Start running CA for : "+fileNames[idx] + "\n     ... May have to wait for a long time ...");
			    ca.runGenContingencyAnalysis();
			    ca.runTransmContingencyAnalysis();
			    if(rank == 0) System.out.println("     CA simulation is done. Solution time is:"+(System.nanoTime() - t_Start_dummy)/1e9f);
			}
			
			if (rank == 0)
			{
				long t_EndCA = System.nanoTime();
//				ca.outputVioInfoBaseCase();
				ca.outputVioInfoTransmCont();
				ca.outputVioInfoGenCont();
				
				long t_EndOutputCA = System.nanoTime();
				float[] times = new float[2];
				times[0] = (t_EndCA - t_Start_dummy)/1e9f;
				times[1] = (t_EndOutputCA - t_Start_dummy)/1e9f;
				ca.outputGeneralInfoTmp(times);		
				System.out.println("     Writing data to files is done");
				System.out.println("     Total time for now is : " + (System.nanoTime() - t_Start)/1e9f + "\n ");
			}
		}
        if (rank == 0) System.out.println("Number of threads invoked : "+nproc);
		if (rank == 0) System.out.println("Total solution time for "+fileNames.length+" raw files is : " + (System.nanoTime() - t_Start)/1e9f);
	    MPI.Finalize();
	}

}
