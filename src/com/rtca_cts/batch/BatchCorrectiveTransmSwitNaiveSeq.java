package com.rtca_cts.batch;

import com.powerdata.openpa.psse.PsseModel;
import com.powerdata.openpa.psse.powerflow.VoltageSource;
import com.rtca_cts.param.ParamIO;
import com.rtca_cts.transmissionswitching.CorrectiveTransmSwitNaive;
import com.utilxl.iofiles.AuxFileXL;
import com.utilxl.iofiles.OutputArrays;
import com.utilxl.iofiles.ReadFilesInAFolder;
import com.utilxl.log.DiaryXL;

/**
 * TS program for multiple input .raw files;
 * 
 * Initialized in Sep. 2014.
 * 
 * @author Xingpeng.Li, xplipower@gmail.com 
 */
public class BatchCorrectiveTransmSwitNaiveSeq {
	
	public static void main(String[] args) throws Exception
	{
		/** Log file creation */
		DiaryXL diary = new DiaryXL();
		diary.initial();

		long t_Start = System.nanoTime();
		String uri = null;
		String svstart = "RealTime";
		float minxmag = 0.0001f;
		for(int i=0; i < args.length;)
		{
			String s = args[i++].toLowerCase();
			int ssx = 1;
			if (s.startsWith("--")) ++ssx;
			switch(s.substring(ssx))
			{
				case "voltage":
					svstart = args[i++];
					break;
			}
		}
		VoltageSource vstart = VoltageSource.fromConfig(svstart);
		
//		ReadFilesInAFolder readFiles = new ReadFilesInAFolder("D:/rawT", true);
		ReadFilesInAFolder readFiles = new ReadFilesInAFolder("dataToRead", false);
		readFiles.readFileNames();
		
		String[] fileNames = readFiles.getAllRawFileNames();
		AuxFileXL.createFolder(ParamIO.getOutPath());
		OutputArrays.outputArray(fileNames, ParamIO.getOutPath() + "fileToBeChecked.txt", true, true, true);
		String absPath = readFiles.getPath();
		
		int numContiCheckedTS = 0;
		AuxFileXL.createFolder(ParamIO.getCAPath());
		String caFolder = ParamIO.getCAFolder();
		for (int i=0; i<fileNames.length; i++)
		{
			ParamIO.setCAFolder(caFolder);
			long t_StartOneFile = System.nanoTime();	
			System.out.println("\n\n******************** New raw file is loaded ***************************");
			uri = "psseraw:file="+readFiles.getPath()+fileNames[i]+"&lowx=adjust";
			
			PsseModel model = PsseModel.Open(uri); 
			model.setMinXMag(minxmag);
			model.setDiary(diary);
			
			CorrectiveTransmSwitNaive ctsApp = new CorrectiveTransmSwitNaive(model);
			ctsApp.setRunCA(true);
			ctsApp.setTstart(t_StartOneFile);
			ctsApp.setAbsPath(absPath);
			
			ctsApp.runPF(vstart); /* ctsApp.runPF(VoltageSource.RealTime); */
			ctsApp.runCA();
			ctsApp.runCTS();
			numContiCheckedTS += ctsApp.getNumContiCheckedTS();
		}
		
		System.out.println("\n# of .raw files checked : " + fileNames.length);
		System.out.println("# of contingencies overall all .raw files checked : " + numContiCheckedTS);
		System.out.println("Total simulation time : " + (System.nanoTime() - t_Start)/1e9f);
		//System.out.println("\nSimulation is done here.");

		/** Program ends here */
		diary.done();
		System.out.println("Program ends here.");
	}

}
