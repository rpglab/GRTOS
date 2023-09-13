package com.utilxl.tools.writeData;

import static org.junit.Assert.assertTrue;

import java.io.FileOutputStream;
import java.io.PrintStream;

import com.powerdata.openpa.psse.ACBranch;
import com.powerdata.openpa.psse.ACBranchList;
import com.powerdata.openpa.psse.Bus;
import com.powerdata.openpa.psse.BusTypeCode;
import com.powerdata.openpa.psse.PsseModel;
import com.powerdata.openpa.psse.powerflow.FastDecoupledPowerFlow;
import com.powerdata.openpa.psse.powerflow.VoltageSource;
import com.powerdata.openpa.psse.util.ImpedanceFilter;
import com.utilxl.iofiles.AuxFileXL;
import com.utilxl.iofiles.ReadFilesInAFolder;

/**
 * 
 * @author Xingpeng.Li (xplipower@gmail.com)
 *
 */
public class writeToTXT {

	
	public static void main(String[] args) throws Exception
	{
		long time_Start = System.nanoTime();
		
		ReadFilesInAFolder readFiles = new ReadFilesInAFolder("C:\\Users\\xingpeng\\data\\TVASystem\\raw", true);
//		ReadFilesInAFolder readFiles = new ReadFilesInAFolder("C:\\pjm", true);
//		ReadFilesInAFolder readFiles = new ReadFilesInAFolder("dataToRead", false);
		readFiles.readFileNames();
		String[] fileNames = readFiles.getAllRawFileNames();
		String absPath = readFiles.getPath();
		String absPathOutput = System.getProperty("user.dir")+"/filesOfOutput/";
		
		String svstart = "Flat";
		float minxmag = 0.00001f;
		boolean MarkVarLimit = true;
		for (int iFile=0; iFile<fileNames.length; iFile++)
		{
			String uri = "psseraw:file="+absPath+fileNames[iFile]+"&lowx=adjust";
			PsseModel model = PsseModel.Open(uri);
			model.setMinXMag(minxmag);
			String fileName = fileNames[iFile].substring(0, fileNames[iFile].length()-4);
			
			ImpedanceFilter zfilt = model.getXFilter();
			FastDecoupledPowerFlow pf = new FastDecoupledPowerFlow(model);
			pf.setMarkUsingVgSched(true);
			pf.setRealTimeStartForGenBus(true);
			VoltageSource vstart = VoltageSource.fromConfig(svstart);
			pf.setShowRunInfo(false);
			pf.runPowerFlow(vstart, MarkVarLimit, 1);
			if (pf.isPfConv() == true)
			{
				float[] vm = pf.getVM();
				float[] va = pf.getVA();
				float[] pfrm = pf.getPfrom();
				float[] pto = pf.getPto();
				float[] qfrm = pf.getQfrom();
				float[] qto = pf.getQto();
				
				ACBranchList branches = model.getBranches(); 
				int numLines = model.getLines().size();
				int numTransf = model.getTransformers().size();
				boolean writeIdx = false;
				
				/* Bus */
				String outputFileName = fileName + "_bus.txt";
		 	    PrintStream outFile = new PrintStream (new FileOutputStream((absPathOutput+outputFileName), true));
		 	    if (writeIdx == true) outFile.print(" index");
				outFile.println(" busNum busType busKV GL BL Vm Va Area Zone");
				int nbuses = model.getBuses().size();
				for (int idx=0; idx<nbuses; idx++) {
					if (writeIdx == true) outFile.print(" "+(idx+1));
					outFile.print(" "+model.getBuses().getI(idx));
					int busType = 1;
					if (model.getBuses().getBusType(idx) == BusTypeCode.Gen) busType = 2;
					else if (model.getBuses().getBusType(idx) == BusTypeCode.Slack) busType = 3;
					outFile.print(" "+busType);
					outFile.print(" "+model.getBuses().getBASKV(idx));
					outFile.print(" "+model.getBuses().getGL(idx));
					outFile.print(" "+model.getBuses().getBL(idx));
					outFile.print(" "+vm[idx]);
					outFile.print(" "+va[idx]);
					outFile.print(" "+model.getBuses().getAREA(idx));
					outFile.println(" "+model.getBuses().getZONE(idx));
				}
				outFile.close();

				/* Branch */
				outputFileName = fileName + "_branch.txt";
		 	    outFile = new PrintStream (new FileOutputStream((absPathOutput+outputFileName), true));
		 	    if (writeIdx == true) outFile.print(" index");
				outFile.print(" frmBusNum toBusNum ckt lineType stat");
				outFile.print(" rij xij Bpi tapij angleij");
				outFile.println(" rateA rateB rateC pfrm pto qfrm qto");
				for (int idx=0; idx<branches.size(); idx++)
				{
					if (writeIdx == true) outFile.print(" "+(idx+1));
					
					ACBranch branch = branches.get(idx);
					Bus frmBus = branch.getFromBus();
					Bus toBus = branch.getToBus();
					
					int frmBusNum = frmBus.getI();
					int toBusNum = toBus.getI();
					String ckt = branch.getCKT();
					outFile.print(" "+frmBusNum);
					outFile.print(" "+toBusNum);
					outFile.print(" "+ckt);
					
					int brcType = 1;
					if (idx >= numLines)
					{
						if ((idx - numLines) < numTransf) brcType = 2;
						else brcType = 3;
					}
					outFile.print(" "+brcType);

					int status = 1;
					if (branch.isInSvc() == false) status = 0;
					outFile.print(" "+status);
					
					float rij = zfilt.getR(idx);
					float xij = zfilt.getX(idx);
					outFile.print(" "+rij);
					outFile.print(" "+xij);

					assertTrue(branch.getFromBchg() == branch.getToBchg()); 
					float Bpi = branch.getFromBchg() + branch.getBmag();
					outFile.print(" "+Bpi);

					outFile.print(" " + branch.getFromTap()/branch.getToTap());
					outFile.print(" " + branch.getPhaseShift());

					outFile.print(" " + branches.getRateA(idx));
					outFile.print(" " + branches.getRateB(idx));
					outFile.print(" " + branches.getRateC(idx));
					
					outFile.print(" " + -pfrm[idx]*100);
					outFile.print(" " + -pto[idx]*100);
					outFile.print(" " + -qfrm[idx]*100);
					outFile.print(" " + -qto[idx]*100);
					outFile.println();
				}
				outFile.close();
				
				/* Load */
				outputFileName = fileName + "_load.txt";
		 	    outFile = new PrintStream (new FileOutputStream((absPathOutput+outputFileName), true));
		 	    if (writeIdx == true) outFile.print(" index");
				outFile.println(" busNum id stat Pload Qload");
				int nloads = model.getLoads().size();
				for (int idx=0; idx<nloads; idx++) {
					if (writeIdx == true) outFile.print(" "+(idx+1));
					outFile.print(" "+model.getLoads().getBus(idx).getI());
					outFile.print(" "+model.getLoads().getID(idx));
					outFile.print(" "+model.getLoads().getSTATUS(idx));
					outFile.print(" "+model.getLoads().getP(idx));
					outFile.println(" "+model.getLoads().getQ(idx));
				}
				outFile.close();
				
				/* Gen */
				outputFileName = fileName + "_gen.txt";
		 	    outFile = new PrintStream (new FileOutputStream((absPathOutput+outputFileName), true));
		 	    if (writeIdx == true) outFile.print(" index");
				outFile.println(" busNum id stat Pg Qg Pgmax Pgmin Qgmax Qgmin Vref");
				int nGens = model.getGenerators().size();
				for (int idx=0; idx<nGens; idx++) {
					if (writeIdx == true) outFile.print(" "+(idx+1));
					outFile.print(" "+model.getGenerators().getBus(idx).getI());
					outFile.print(" "+model.getGenerators().getID(idx));
					outFile.print(" "+model.getGenerators().getSTAT(idx));
					outFile.print(" "+model.getGenerators().getP(idx));
					outFile.print(" "+model.getGenerators().getQ(idx));
					outFile.print(" "+model.getGenerators().getPT(idx));
					outFile.print(" "+model.getGenerators().getPB(idx));
					outFile.print(" "+model.getGenerators().getQT(idx));
					outFile.print(" "+model.getGenerators().getQB(idx));
					outFile.println(" "+model.getGenerators().getVS(idx));
				}
				outFile.close();
				
				/* Shunt */
				outputFileName = fileName + "_shunt.txt";
		 	    outFile = new PrintStream (new FileOutputStream((absPathOutput+outputFileName), true));
		 	    if (writeIdx == true) outFile.print(" index");
				outFile.println(" busNum stat GL BL");
				int nShunts = model.getShunts().size();
				for (int idx=0; idx<nShunts; idx++) {
					if (writeIdx == true) outFile.print(" "+(idx+1));
					outFile.print(" "+model.getShunts().getBus(idx).getI());
					int inSvc = 1;
					if (model.getShunts().isInSvc(idx) == false) inSvc = 0;
					outFile.print(" " + inSvc);
					outFile.print(" " + model.getShunts().getG(idx));
					outFile.println(" " + model.getShunts().getB(idx));
				}
				outFile.close();
			}
			else
			{
				String title = "Power flow for "+fileNames[iFile]+" does not converge. ";
				AuxFileXL.initFileWithTitle(absPathOutput+"PFnotConverge.txt", title, true);
			}
		}
		
		System.out.println("Total simulation time is: " + (System.nanoTime() - time_Start)/1e9);
		System.out.println("Simulation is done here!");
	}


	
}
