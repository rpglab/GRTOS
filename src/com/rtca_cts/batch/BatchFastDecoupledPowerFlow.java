package com.rtca_cts.batch;

import java.io.File;

import com.powerdata.openpa.psse.BusList;
import com.powerdata.openpa.psse.Island;
import com.powerdata.openpa.psse.IslandList;
import com.powerdata.openpa.psse.PsseModel;
import com.powerdata.openpa.psse.powerflow.FastDecoupledPowerFlow;
import com.powerdata.openpa.psse.powerflow.PowerFlowConvergence;
import com.powerdata.openpa.psse.powerflow.PowerFlowConvergenceList;
import com.powerdata.openpa.psse.powerflow.VoltageSource;
import com.powerdata.openpa.tools.PAMath;
import com.rtca_cts.param.ParamFDPFlow;
import com.utilxl.iofiles.ReadFilesInAFolder;

/**
 * Basic fast decoupled power flow program for multiple input .raw files;
 * 
 * Initialized in Aug. 2014.
 * 
 * @author Xingpeng.Li, xplipower@gmail.com 
 */
public class BatchFastDecoupledPowerFlow {
	
	public static void main(String[] args) throws Exception
	{
		String uri = null;
		String svstart = "Flat";
		File dbgdir = null;
		float minxmag = 0.0001f;
		
		VoltageSource vstart = VoltageSource.fromConfig(svstart);
		
//		ReadFilesInAFolder readFiles = new ReadFilesInAFolder("M://MPJWorkspace//debug_20140214//dataToRead", true);
		ReadFilesInAFolder readFiles = new ReadFilesInAFolder("D:\\raw", true);
		readFiles.readFileNames();
		String[] fileNames = readFiles.getAllRawFileNames();
		int numRawFiles = fileNames.length;
		System.out.println("# of .raw files : "+numRawFiles+" \n ");
		
		long t_Start = System.nanoTime();
		int numConv = 0;
		for (int idx=0; idx<numRawFiles; idx++)
		{
			System.out.println("We are running simulation for the idx "+(idx+1)+"-th raw file (Total # : "+numRawFiles+" ). ");
			uri = "psseraw:file="+readFiles.getPath()+fileNames[idx]+"&lowx=adjust";
			PsseModel model = PsseModel.Open(uri);
			model.setMinXMag(minxmag);
			FastDecoupledPowerFlow pf = new FastDecoupledPowerFlow(model);
			if (dbgdir != null) pf.setDebugDir(dbgdir);
			pf.setShowRunInfo(false);
			
			pf.setMarkUsingVgSched(false);
			pf.setRealTimeStartForGenBus(true);
			vstart = VoltageSource.RealTime;
					
			PowerFlowConvergenceList pslist = pf.runPowerFlow(vstart, ParamFDPFlow.getGenVarLimit(), 1);

			System.out.println("Island Converged Iterations WorstPBus   Pmm   WorstQBus   Qmm");
			IslandList islands = model.getIslands();
			BusList buses = model.getBuses();
			for(PowerFlowConvergence psol : pslist)
			{
				Island i = islands.get(psol.getIslandNdx());
				System.out.format("  %s     %5s       %2d     %9s %7.2f %9s %7.2f\n",
					i.getObjectName(),
					String.valueOf(psol.getConverged()),
					psol.getIterationCount(),
					buses.get(psol.getWorstPbus()).getObjectName(),
					PAMath.pu2mw(psol.getWorstPmm()),
					buses.get(psol.getWorstQbus()).getObjectName(),
					PAMath.pu2mvar(psol.getWorstQmm()));
			}
//			String path = pf.getPath()+"/"+fileNames[idx]+"/";
//			pf.setPath(path);
			if (pf.isPfConv() == true) {numConv++; pf.outputVioInfo();}
			System.out.println();
		}
		System.out.println("   # of cases which converge : "+numConv+" out of "+numRawFiles+" cases");
		System.out.println("   Total time : " + (System.nanoTime() - t_Start)/1e9f);		
	}


}
