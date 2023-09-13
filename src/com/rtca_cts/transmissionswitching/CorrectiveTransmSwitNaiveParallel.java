package com.rtca_cts.transmissionswitching;

import mpi.MPI;

import com.powerdata.openpa.psse.PsseModel;
import com.powerdata.openpa.psse.powerflow.FastDecoupledPowerFlow;
import com.powerdata.openpa.psse.powerflow.VoltageSource;
import com.rtca_cts.ausData.RadialBranches;
import com.rtca_cts.ausXP.InitPsseModel;
import com.rtca_cts.contingencyanalysis.ContingencyAnalysisSeq;
import com.rtca_cts.param.ParamFDPFlow;
import com.utilxl.array.AuxArrayXL;
import com.utilxl.iofiles.AuxFileXL;

/**
 * Transmission switching;
 * Parallel version.
 * 
 * Initialized in Sep. 2014.
 * 
 * @author Xingpeng.Li, xplipower@gmail.com 
 */
public class CorrectiveTransmSwitNaiveParallel {

	
	public static void main(String[] args) throws Exception
	{
		MPI.Init(args);
		long t_Start = System.nanoTime();

	    if (MPI.Initialized() == false)
	    	System.out.println("  Error Happens when starting MPI program.");
	    int nproc = MPI.COMM_WORLD.Size();
	    int rank = MPI.COMM_WORLD.Rank();
	    if (rank == 0) System.out.println("# of threads invoked: "+nproc+" \n ");
	            
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

//		String outputCSVPath = System.getProperty("user.dir") + "/casesCSVFormat/";
//		if (rank == 0) AuxFileXL.createFolder(outputCSVPath);
//		String inputRawFile = "dataToRead"+"/"+"2.raw";
//		if (rank == 0)
//		{
//			AuxFileXL.createFolder(outputCSVPath);
//			PsseRaw2CSV.launch(inputRawFile, outputCSVPath);
//		}
//		MPI.COMM_WORLD.Barrier();    // other threads should and have to wait until all the CSV files are created. 
//		uri = AuxStringXL.getUriForOpenPA(outputCSVPath, true);
		PsseModel model = PsseModel.Open(uri, rank);
		model.setMinXMag(minxmag);
		
		InitPsseModel initModel = new InitPsseModel(model);
		initModel.initSetting();
		if (rank == 0) initModel.initNBestTS(uri);
		if (rank == 0)
		{
			model.getNBestTSReport_NoTitle().setFileNameTransm("NoTitle_BestTS_Transm");
			model.getNBestTSReport_NoTitle().setFileNameGen("NoTitle_BestTS_Gen");
			model.getNBestTSReport_NoTitle().initPrint();
		}

		boolean runCA = true;
		
		FastDecoupledPowerFlow pf = new FastDecoupledPowerFlow(model);
		pf.setShowRunInfo(false);
		boolean MarkGenVarLimit = ParamFDPFlow.getGenVarLimit();
		pf.runPowerFlow(vstart, MarkGenVarLimit, 1, 10);
		if (pf.isPfConv()) {if (rank == 0) System.out.println("\nThe power flow for base case converged.");}
		else {if (rank == 0) System.out.println("\nThe power flow for base case did not converge.");}
		
		float[] vmBasePf = pf.getVM();
		float[] vaBasePf = pf.getVA();
		int[] idxBrcVioBasePf = null;
		if (pf.getVioRateC().getViol() == true)
		{
			idxBrcVioBasePf = pf.getVioRateC().getIdxBrc();
			int[] idxV = pf.getVioRateC().getIdxV();
			if (idxV != null)
			{
				int sizeVoltVio = idxV.length;
				if (sizeVoltVio != 0) System.err.println("\n&---------- Warning: there are "+sizeVoltVio+" bus voltage violation in the base case. ----------&");				
			}
		}
		
		int[] idxBrcVio = idxBrcVioBasePf;
		if (idxBrcVio != null)
		{
			for(int idx=0; idx<idxBrcVio.length; idx++)
			{
				int idxBrc = idxBrcVio[idx];
				if (rank == 0) System.out.println("    Increase the branch "+ (idxBrc+1) + " rating to infinity due to flow vio on this branch in base case.");
				model.getBranches().setRateA(idxBrc, 99999f);
				model.getBranches().setRateB(idxBrc, 99999f);
				model.getBranches().setRateC(idxBrc, 99999f);
			}
			model.clearACBrcCap();
		}
		
		int[] idxBrcContToBeChecked = null;
		int[] idxGenContToBeChecked = null;
		if (runCA == true)
		{
			ContingencyAnalysisSeq ca = new ContingencyAnalysisSeq(model, VoltageSource.Flat);
			ca.setMarkUsingVgSched(true); /*ca.setRealTimeStartForGenBus(true);*/
			ca.setShowRunInfo(false);
			
			System.out.println("\nRunning contingency analysis...");
			long timeCA = System.nanoTime();
			System.out.println("    Running Generator contingency analysis...");
		    ca.runGenContingencyAnalysis();
			System.out.println("    Generator contingency analysis is finished.");
			System.out.println("      Gen CA time : " + (System.nanoTime() - timeCA)/1e9f);
			
			System.out.println("    Running Transmission contingency analysis...");
		    ca.runTransmContingencyAnalysis();
			System.out.println("    Transmission contingency analysis is finished.");
			System.out.println("      (Gen + Transm) CA time : " + (System.nanoTime() - timeCA)/1e9f);
			
			idxBrcContToBeChecked = ca.getIdxContVioVmAndBrcAllTransm();
			idxGenContToBeChecked = ca.getIdxContVioVmAndBrcAllGen();
		} else {
			idxBrcContToBeChecked = AuxFileXL.readOneCollumIntData("dataToRead/brcContList.txt", false);
			AuxArrayXL.allElemsPlusANum(idxBrcContToBeChecked, -1);
			idxGenContToBeChecked = AuxFileXL.readOneCollumIntData("dataToRead/genContList.txt", false);
			AuxArrayXL.allElemsPlusANum(idxGenContToBeChecked, -1);
		}
		
		if (rank == 0) 
		{
			int numBrcConti;  int numGenConti;
			if (idxBrcContToBeChecked == null) numBrcConti = 0; else numBrcConti = idxBrcContToBeChecked.length;
			System.out.println(" \nNumber of Transmission Contingency to be checked with TS is: "+numBrcConti);
			if (idxGenContToBeChecked == null) numGenConti = 0; else  numGenConti = idxGenContToBeChecked.length;
			System.out.println("Number of Generator    Contingency to be checked with TS is: "+numGenConti+"\n ");
		}
		
		CorrectiveTransmSwit engineCTS = new CorrectiveTransmSwit(model);
		engineCTS.setThreadInfo(nproc, rank);
//		engineCTS.setOutputAllTS_AllVio(true, engineCTS.getPathAllTS_AllVio()+fileNames[i]+"/");
//		engineCTS.setOutputAllTS(true, engineCTS.getPathAllTS() + fileNames[i] + "/");     // to be modified.
		engineCTS.setOutputAllTS(true);     // to be modified.
		//engineCTS.setMarkUsingVgSched(true);
		engineCTS.initial(t_Start, uri, VoltageSource.LastSolved, vmBasePf, vaBasePf);
				
		MPI.COMM_WORLD.Barrier();    // other threads should and have to wait until all the CSV files are created. 
		// Exclude solution time starting from here.
		RadialBranches radialCheck = new RadialBranches(model);
		boolean[] isRadial = radialCheck.getIsBrcRadial();
		// Exclude solution time ending to here.

		int numContiCheckedTS = 0;
		int[][] checkList = {idxBrcContToBeChecked, idxGenContToBeChecked};
		for (int iRow=0; iRow<2; iRow++)
		{
			if (checkList[iRow] == null) continue;
			for (int j=0; j<checkList[iRow].length; j++)
			{
				int idxConti = checkList[iRow][j];
				
				// Exclude solution time starting from here.
				if (iRow == 0 && isRadial[idxConti] == true)
				{
					System.err.println("\nSuper warning: Brc contingency "+idxConti+" is a radial branch. \n ");
					String title = "Brc contingency "+idxConti+" checked is a radial branch";
					AuxFileXL.initFileWithTitle(engineCTS.getPath()+"ContiBrcIsRadial.txt", title, true);
					continue;
				}
				// Exclude solution time ending to here.
				
				int[] idxContiArray = {idxConti};
				if (iRow == 0)
				{
					engineCTS.setContToCheckTransm(idxContiArray); 
					engineCTS.runTSTransm();
				} else if (iRow == 1) {
					engineCTS.setContToCheckGen(idxContiArray); 
					engineCTS.runTSGen();
				}
				numContiCheckedTS++;
			}
		}
		float timeSol = (System.nanoTime() - t_Start)/1e9f;
		engineCTS.analyzeData();
		engineCTS.outputResults(timeSol);
		
		if (rank == 0) System.out.println("\n    Total simulation time until now : " + (System.nanoTime() - t_Start)/1e9f);		
		if (rank == 0) System.out.println("# of contingencies overall all .raw files checked : " + numContiCheckedTS);
		if (rank == 0) System.out.println("    Total time : " + (System.nanoTime() - t_Start)/1e9f);		
	}
	

}
