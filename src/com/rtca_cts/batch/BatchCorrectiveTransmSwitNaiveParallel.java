package com.rtca_cts.batch;

import mpi.MPI;

import com.powerdata.openpa.psse.PsseModel;
import com.powerdata.openpa.psse.powerflow.VoltageSource;
import com.rtca_cts.ausXP.PsseRaw2CSV;
import com.rtca_cts.param.ParamIO;
import com.rtca_cts.transmissionswitching.CorrectiveTransmSwitNaive;
import com.utilxl.iofiles.AuxFileXL;
import com.utilxl.iofiles.OutputArrays;
import com.utilxl.iofiles.ReadFilesInAFolder;
import com.utilxl.string.AuxStringXL;

/**
 * TS program for multiple input .raw files;
 * Parallel version.
 * 
 * Initialized in Sep. 2014.
 * 
 * @author Xingpeng.Li, xplipower@gmail.com 
 */
public class BatchCorrectiveTransmSwitNaiveParallel {
		
		public static void main(String[] args) throws Exception
		{
		    MPI.Init(args);
		    if (MPI.Initialized() == false)
		    	System.out.println("  Error Happens when starting MPI program.");
			long t_Start = System.nanoTime();
		    int nproc = MPI.COMM_WORLD.Size();
	        int rank = MPI.COMM_WORLD.Rank();
	        if (rank == 0) System.out.println(" \nNumber of threads invoked : "+nproc + "\n ");
//	        String outDir = "/p/lscratchd/pranavab/outputs/"; // this folder should exit before program starts.
//	        ParamIO.setOutPath(outDir);
//	        if (rank == 0) AuxFileXL.createFolder(outDir);
	        if (rank == 0) AuxFileXL.createFolder(ParamIO.getOutPath());
	        ParamIO.setOutPath(ParamIO.getOutPath() + "_nproc_"+nproc+"/");
	        if (rank == 0) AuxFileXL.createFolder(ParamIO.getOutPath());

			String uri = null;
			float minxmag = 0.0001f;
			String svstart = "Flat";
			VoltageSource vstart = VoltageSource.fromConfig(svstart);
			
			String inputPath = "dataToRead";
			boolean isAbsPath = false;
			boolean isInputCSVFiles = false;
			
			for (int i=0; i < args.length;)
			{
				String s = args[i++].toLowerCase();
				int ssx = 1;
				if (s.startsWith("--")) ++ssx;
				switch(s.substring(ssx))
				{
					case "csv":
						inputPath = args[i++];
						isInputCSVFiles = true;
						break;
					case "abspath":
						String markAbsPath = args[i++].toLowerCase();
						if (markAbsPath.equals("true")) isAbsPath = true;
						else if (markAbsPath.equals("false")) isAbsPath = false;
						else if (rank == 0) System.err.println("Parameter associated with absPath is off...");
						break;
				}
			}
			if (rank == 0)
			{
				System.out.println("inputPath: " + inputPath);
				System.out.println("isAbsPath: " + isAbsPath);
			}
			
//			ReadFilesInAFolder readFiles = new ReadFilesInAFolder("D:/pjm", true);
			ReadFilesInAFolder readFiles = new ReadFilesInAFolder(inputPath, isAbsPath);
			readFiles.readFileNames();
			String[] fileNames = null;
			if (isInputCSVFiles == false) fileNames = readFiles.getAllRawFileNames();
			else fileNames = readFiles.getFolderNames();
			String absPath = readFiles.getPath();
			if (rank == 0) OutputArrays.outputArray(fileNames, ParamIO.getOutPath() + "fileToBeChecked.txt", true, true, true);

			int numContiCheckedTS = 0;
			if (rank == 0) AuxFileXL.createFolder(ParamIO.getCAPath());
			String caFolder = ParamIO.getCAFolder();
			for (int idx=0; idx<fileNames.length; idx++)
			{
				ParamIO.setCAFolder(caFolder);
				String outputCSVPath = null;
				if (isInputCSVFiles == false)
				{
					String inputRawFile = readFiles.getPath()+"/"+fileNames[idx];
					outputCSVPath = System.getProperty("user.dir") + "/casesCSVFormat/";
					if (rank == 0) AuxFileXL.createFolder(outputCSVPath);
					outputCSVPath = outputCSVPath+fileNames[idx];
					if (rank == 0)
					{
						AuxFileXL.createFolder(outputCSVPath);
						PsseRaw2CSV.launch(inputRawFile, outputCSVPath);
					}
					MPI.COMM_WORLD.Barrier();    // other threads should and have to wait until all the CSV files are created. 
				} else outputCSVPath = absPath + fileNames[idx];
				
				long t_StartOneFile = System.nanoTime();
				uri = AuxStringXL.getUriForOpenPA(outputCSVPath, true);
				PsseModel model = PsseModel.Open(uri, rank);
				model.setMinXMag(minxmag);
				if (rank == 0) System.out.println("\n\n**************** New raw file is loaded ****************");

				/* Main application */
				CorrectiveTransmSwitNaive ctsApp = new CorrectiveTransmSwitNaive(model, rank, nproc);
				ctsApp.setRunCA(true);
				ctsApp.setTstart(t_StartOneFile);
				ctsApp.setAbsPath(absPath);
				
				ctsApp.runPF(vstart); /* ctsApp.runPF(VoltageSource.RealTime); */
				ctsApp.runCA();
				ctsApp.runCTS();
				numContiCheckedTS += ctsApp.getNumContiCheckedTS();

				MPI.COMM_WORLD.Barrier();    // other threads should to wait before continue.
			}
			if (rank == 0) System.out.println("\n# of .raw files checked : " + fileNames.length);
			if (rank == 0) System.out.println("# of contingencies overall all .raw files checked : " + numContiCheckedTS);
			if (rank == 0) System.out.println("Total simulation time : " + (System.nanoTime() - t_Start)/1e9f);
			//System.out.println("\n         --- Simulation is finished for rank : " + rank + " --- ");

		    MPI.Finalize();
		}
		
}
