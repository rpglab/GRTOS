package com.casced;

import com.powerdata.openpa.psse.GenList;
import com.powerdata.openpa.psse.PsseModel;
import com.powerdata.openpa.psse.powerflow.FastDecoupledPowerFlow;
import com.powerdata.openpa.psse.powerflow.VoltageSource;
import com.rtca_cts.param.ParamFDPFlow;
import com.rtca_cts.param.ParamIO;
import com.rtca_cts.param.ParamTS;
import com.rtca_cts.transmissionswitching.CorrectiveTransmSwitNaive;
import com.sced.model.param.ReadParams;
import com.utilxl.iofiles.AuxFileXL;
import com.utilxl.log.DiaryXL;
import com.utilxl.log.LogTypeXL;

/**
 * Input raw file should be of post-SCED Pg solution.
 * 
 * @author xingpeng
 *
 */
public class CTSsimulatorPostSCED {

		public static void main(String[] args) throws Exception
		{
			/** Log file creation */
			DiaryXL diary = new DiaryXL();
			diary.initial();
			
			/** Load parameters in configure file */
			ReadParams loadParam = new ReadParams();
			loadParam.launch();
			diary.hotLine("Configure file '"+loadParam.getConfigureFileName()+"' has been loaded");
			
			/** Parse program arguments */
			String uri = null;
			String svstart = "Flat";
			float minxmag = 0.0001f;
			for(int i=0; i < args.length;)
			{
				String s = args[i++].toLowerCase();
				int ssx = 1;
				if (s.startsWith("--")) ++ssx;
				switch(s.substring(ssx))
				{
					case "uri":
						uri = args[i++];
						break;
					case "voltage":
						svstart = args[i++];
						break;
					case "minxmag":
						minxmag = Float.parseFloat(args[i++]);
						break;
				}
			}
			
			VoltageSource vstart = VoltageSource.fromConfig(svstart);
			if (uri == null)
			{
				System.err.format("Usage: -uri model_uri [-voltage flat|realtime] [ -minxmag smallest reactance magnitude (default to 0.001) ] [ -debug dir ] [ -results path_to_results]");
				System.exit(1);
			}
	
			ParamIO.setCAFolder("CAResults_PostSCED");
			AuxFileXL.createFolder(ParamIO.getCAPath());
			String caFolder = ParamIO.getCAFolder();
			ParamIO.setCAFolder(caFolder);
			long t_StartOneFile = System.nanoTime();	
			System.out.println("\n\n******************** New raw file is loaded ***************************");
			
			/* Read PSSE raw file */
	 	    PsseModel model = PsseModel.Open(uri);
	 	    model.setMinXMag(minxmag);
			model.setDiary(diary);
			diary.hotLine(LogTypeXL.MileStone, "OpenPA model raw data is loaded");
	
			/* Run original PF */
			//model.getBusTypeManagerData();
			FastDecoupledPowerFlow pf = new FastDecoupledPowerFlow(model);
			pf.setShowRunInfo(false);

			boolean MarkVarLimit = ParamFDPFlow.getGenVarLimit();
			pf.setMarkUsingVgSched(true);
			pf.runPowerFlow(vstart, MarkVarLimit, 1);	
			if (pf.isPfConv() == false) {
				String mess = "Original pre-SCED case does NOT converge for power flow simulation";
				System.out.println(mess); diary.hotLine(LogTypeXL.Error, mess);
				System.exit(2);
			}
			pf.updateModelVoltage();

			// set new Pg obtained from SCED
			float[] newPg = AuxFileXL.readOneCollumFloatData("dataToRead/newPgSCED.txt", false);
			GenList gensPAData = model.getGenerators();
			boolean useMainIsland = false;
			if (useMainIsland == true) {
				MainIsland misland = new MainIsland(model);
				gensPAData = model.getIslands().get(misland.getIdxMainIsland()).getGenerators();
			}
			int nGens =  gensPAData.size();
			for(int g=0; g<nGens; g++)
			{
				gensPAData.setPS(g, newPg[g]);
				gensPAData.setP(g, newPg[g]);
			}
			diary.hotLine("Generators' Pg has been reset based on the result from SCED.");		

			/* CTS simulator starts */ 
			ParamTS.setTSOption(6);
			ParamIO.setTSFolder("TSResultPostSCED/");
			CorrectiveTransmSwitNaive ctsApp = new CorrectiveTransmSwitNaive(model);
			ctsApp.setRunCA(false);
			ctsApp.setBrcContListFileName("_EDM_v2_Brc.txt"); // actual file name: rawFileName + "".
			ctsApp.setTstart(t_StartOneFile);
			//ctsApp.setAbsPath(absPath);
			
			ctsApp.runPF(vstart); /* ctsApp.runPF(VoltageSource.RealTime); */
			ctsApp.runCA();
			ctsApp.runCTS();
	
			/** Program ends here */
			diary.done();
			System.out.println("Program ends here.");
		}
	
}
