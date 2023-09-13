package com.rtca_cts.transmissionswitching;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Calendar;

import com.powerdata.openpa.psse.PsseModel;
import com.powerdata.openpa.psse.powerflow.FastDecoupledPowerFlow;
import com.powerdata.openpa.psse.powerflow.VoltageSource;
import com.rtca_cts.ausData.NearbyElems;
import com.rtca_cts.contingencyanalysis.AnalyzeVioResult;
import com.rtca_cts.contingencyanalysis.VioResult;
import com.rtca_cts.param.ParamFDPFlow;
import com.rtca_cts.param.ParamIO;
import com.utilxl.iofiles.AuxFileXL;

/**
 * 
 * The input raw file should be post-contingency network.
 * 
 * Initialized in Oct. 2014.
 * 
 * Current version is for ISO-NE system.
 * 
 * @author Xingpeng.Li
 */
public class CorrectiveTSforPostContiNetwork {
	
	
	public static void main(String[] args) throws Exception
	{
	    Calendar c = Calendar.getInstance();
	    System.out.format("Program starts at %tl:%tM %tp, %tD%n%n", c, c, c, c, c, c);

		long t_Start = System.nanoTime();
		String uri = null;
		String svstart = "Flat";
		float minxmag = 0.00001f;
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

		boolean useRealTimeVmForGenVmSetting = true;
		if (useRealTimeVmForGenVmSetting == true) vstart = VoltageSource.RealTime;
		
		float VmLevel = 70.0f;   // ignore branch voltage violation below this value (KV).
		boolean useAbsHighVoltBrcForMonitor = false;  // whehter to use high voltage side as transformer branch voltage level.
		System.out.format("Violations on branches/buses whose voltage level is less than %6.2f KV are ignored.\n", VmLevel);
		if (useAbsHighVoltBrcForMonitor == true) System.out.println("Use high-voltage side as transformers' voltage level.");
		else System.out.println("Use low-voltage side as transformers' voltage level.");
		
		boolean concernAbsVioReduction = true;   // For flow vio: if true, consider absolute over-loading values; if false, consider percentage over-loading values.
		int concernNumOfBrcVio = 2;    // only consider the first concernNumOfBrcVio worst violations when selecting switching actions for flow vio.
		                               // concernNumOfBrcVio and concernNumOfVmVio should be a small number.
//		int concernNumOfVmVio = 0;    // only consider the first concernNumOfVmVio worst violations when selecting switching actions for Vm vio.
		
		boolean completeEnum = false;  // if true, then @param numSwitBrcs wouldn't matter.
		int numSwitBrcs = 100;   // # of candidate switching actions; it should be greater than 10*(concernNumOfBrcVio + concernNumOfVmVio).
		int numTSReported = 5;             // # of beneficail switching actions repored.
		
		boolean ingoreVmVio = true;  // if true, then, do not consider voltage violations when selecting candidate switching actions.
		if (ingoreVmVio == true) System.out.println("Do not consider voltage violations when selecting candidate switching actions.\n");
		else System.out.println("TS actions for voltage violations has not been implemented, so ignored.");

		float brcVioElemTol = 0.02f; // relates to outputs of flag of newVioExist or contiVioWorse after TS.
		float vmVioElemTol = 0.002f; // relates to outputs of flag of newVioExist or contiVioWorse after TS.

		float sumBrcVioAbnormal = 30f;
		float sumVmVioAbnormal = 30f;
		
		String outputPath = ParamIO.getOutPath();
		AuxFileXL.createFolder(outputPath);
		outputPath += "Resutls_PostContiNetwork/";
		AuxFileXL.createFolder(outputPath);
		
		boolean outputAllTS_AllVio = true;
		boolean outputAllTS = true;
		if (outputAllTS_AllVio == true) outputAllTS = true;
		
		PsseModel model = PsseModel.Open(uri);
		model.setMinXMag(minxmag);
		
        FastDecoupledPowerFlow pfnm1 = new FastDecoupledPowerFlow(model);
        pfnm1.setShowRunInfo(false);
        
		boolean MarkGenVarLimit = ParamFDPFlow.getGenVarLimit();
		if (useRealTimeVmForGenVmSetting == true) pfnm1.setMarkUsingVgSched(false); 
		pfnm1.setRealTimeStartForGenBus(useRealTimeVmForGenVmSetting);
        pfnm1.runPowerFlow(vstart, MarkGenVarLimit, 1, 10);
		model.getBusTypeManagerData().setPreTSBusType(pfnm1.getSlackBuses(), pfnm1.getPvBuses(), pfnm1.getPqBuses());
        
        if (pfnm1.isPfConv() == true)
        {    		
    		VioResult vioRecord = pfnm1.getVioRateC();
    		float sumBrcVio = vioRecord.getSumBrcDiff();
    		float sumVmVio = vioRecord.getSumVmDiff();

    		AnalyzeVioResult analyzePfnm1Vio = new AnalyzeVioResult(vioRecord); 
    		analyzePfnm1Vio.outputSummaryVioInfo();
    		analyzePfnm1Vio.checkAbnormalVio(outputPath+"AbnormalVioPostContiNetwork.txt", uri, sumBrcVioAbnormal, sumVmVioAbnormal);

    		int[] idxVioBrc = vioRecord.getIdxBrc();
    		boolean needToCheckTS = false;
    		if (idxVioBrc != null) needToCheckTS= true;
    		if (ingoreVmVio == false && vioRecord.getIdxV() != null) needToCheckTS = true;
    		
    		if (needToCheckTS == true)
    		{
    			idxVioBrc = analyzePfnm1Vio.findSeriousBrcWithVio(concernNumOfBrcVio, concernAbsVioReduction);
    			
    			// Start transmission switching
    			TransmSwit checkTS = new TransmSwitSeq(model, VoltageSource.LastSolved);
    			checkTS.setContiPFInfo(pfnm1);
    			checkTS.setAbnormalVioTol(sumBrcVioAbnormal, sumVmVioAbnormal);
    			//if (useRealTimeVmForGenVmSetting == true) checkTS.setMarkUsingVgSched(true);
    			checkTS.setRealTimeStartForGenBus(useRealTimeVmForGenVmSetting);
    			
    			if (MarkGenVarLimit == true) checkTS.enableGenVarLimit();
    			else checkTS.clearGenVarLimit();
    			
    			checkTS.setElemVioCheckTS(true, true, brcVioElemTol, vmVioElemTol);
    			
    			if (completeEnum == false)
    			{
        			NearbyElems nearbyElems = new NearbyElems(model);
        			nearbyElems.setCheckRadialBrc(true);
        			int[] candidList = nearbyElems.getNearbyBrcsForBrcs(idxVioBrc, numSwitBrcs, true);					
        			if (candidList == null || candidList.length != numSwitBrcs)
        			{
        				String title = "# of TS for this post-contingency network is: "+candidList.length+" not equal to " + numSwitBrcs+" ...";
        				AuxFileXL.initFileWithTitle(outputPath+"ContiTSListWrong.txt", title, true);
        			}
//        			checkTS.setCheckSpecifiedSwitBrc(true, candidList);
    			}
    			
    			String fileNameAllTS = "results_SumVio_PerTS.txt";
    			String fileNameAllTS_AllVio = "results_All_Individual_Vio.txt";
    			AuxFileXL.initFileWithTitle(outputPath+fileNameAllTS, uri, true);
    			AuxFileXL.initFileWithTitle(outputPath+fileNameAllTS_AllVio, uri, true);
    			int[] contiInfo = new int[] {-7, -7, -7};
    			checkTS.setOutputAllTS_AllVio(outputAllTS_AllVio, outputPath + fileNameAllTS_AllVio);
    			checkTS.setOutputAllTS(outputAllTS, outputPath + fileNameAllTS, contiInfo);
    			checkTS.setAllTSTitle(uri);
    			
    			checkTS.launchTS();
    			
    			//code to choose the best Switching action.
    			DetermineBestSwitActions bestTS = new DetermineBestSwitActions(checkTS);
    			int[] beneTS = bestTS.getBestTSforBrcVio(numTSReported);
    			float[] sumBrcVioBeneTS = bestTS.getSumBrcVioOfBestTSForBrcVio();
    			float[] sumVmVioBeneTS = bestTS.getSumVmVioOfBestTSForBrcVio();
    			
    			String outputTS = outputPath + "SwitBrcForBrcVioReport.txt";
       		    PrintStream outFile = new PrintStream (new FileOutputStream(outputTS, true), true);
       		    
       		    outFile.println(uri);
       		    outFile.println("### Summation of flow violation before switching: "+sumBrcVio + ". ###");
       		    outFile.println("### Summation of voltage violation before switching: "+sumVmVio + ". ###\n");
       		    outFile.println(" idxSwitBrc, frmBusOfTS, toBusOfTS, sumBrcVio, sumVm");
       		    for (int i=0; i<beneTS.length; i++)
       		    {
       		    	int idxSwitBrc = beneTS[i];
       		    	int frmBusIdx = model.getBranches().getFromBus(idxSwitBrc).getI();
       		    	int toBusIdx = model.getBranches().getToBus(idxSwitBrc).getI();
       	   		    outFile.print(" "+(idxSwitBrc+1)+" "+frmBusIdx+" "+toBusIdx);
       	   		    outFile.println(" " + sumBrcVioBeneTS[i] + " " + sumVmVioBeneTS[i]);
       		    }
       		    outFile.close();
    		}
            else System.out.println("No violation for post-contingency case. TS is ignored.");
        }
        else System.out.println("The power flow of post-contingency does not converge.");
		
		System.out.println("\nTotal simulation time : " + (System.nanoTime() - t_Start)/1e9f);		
	    c = Calendar.getInstance();
	    System.out.format("Program ends at %tl:%tM %tp, %tD%n%n", c, c, c, c, c, c);
//	    System.out.format("    %tB %te, %tY%n", c, c, c, c, c, c); // -->  "May 29, 2006"
//	    System.out.format("    %tl:%tM %tp%n", c, c, c);  // -->  "2:34 am"
//	    System.out.format("    %tD%n%n", c);    // -->  "05/29/06"

	}

}
