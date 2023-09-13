package com.rtca_cts.ausXP;

import static org.junit.Assert.assertTrue;

import java.io.FileOutputStream;
import java.io.PrintStream;

import org.junit.Test;

import com.powerdata.openpa.psse.ACBranch;
import com.powerdata.openpa.psse.ACBranchList;
import com.powerdata.openpa.psse.Bus;
import com.powerdata.openpa.psse.BusList;
import com.powerdata.openpa.psse.BusTypeCode;
import com.powerdata.openpa.psse.GenList;
import com.powerdata.openpa.psse.PsseModel;
import com.powerdata.openpa.psse.powerflow.FastDecoupledPowerFlow;
import com.powerdata.openpa.psse.powerflow.VoltageSource;
import com.powerdata.openpa.psse.util.ImpedanceFilter;
import com.rtca_cts.ausData.BusGroupElems;
import com.utilxl.iofiles.AuxFileXL;
import com.utilxl.iofiles.ReadFilesInAFolder;

/** 
 * Output all network data and
 * power flow solutions to a file.
 * 
 * @author xingpeng
 *
 */
public class OutputPFData {

//  Output data's format (by column)
//	No.	
//	system_title : raw file name (".raw" is excluded).
//	branch_type	: 1 - line; 2 - transformer; 3 - phase shifter
//	branch_No	
//	from Bus Number	 
//	to Bus Number	
//	Circuit_ID	
//	Status	: 1 - online; 0 - offline.
//  Bpi : (B/2) + Bmag
//	Rij	: z.re()
//	Xij	: z.im()
//	Gij : (1/z).re()	
//	Bij	: (1/z).im()
//	B	: in pu	
//	B/2	: in pu
//	Tap_i	
//	Tap_j	
//	PSAngle : in rad (phase_shifter_angle_ij)
//	Bmag	: in pu	
//	V_i	: in pu
//	V_j	: in pu
//	theta_i	: in rad
//	theta_j	: in rad	
//  thetaK : theta_i - PSAngle - theta_j
//	Pij	: in pu	_ flow out of frm bus
//	Pji	: in pu	_ flow out of to bus
//	Qij	: in pu	
//	Qji	: in pu	
//	Sij	: in pu	
//	Sji	: in pu
//	rateA : in pu
//	rateB : in pu
//	rateC : in pu
//	Vi_level : in kv.
//	Vj_level : in kv.

	
//  2-Output data's format (by column)
//	No.	
//	system_title : raw file name (".raw" is excluded).
//	bus_number	: 1 - line; 2 - transformer; 3 - phase shifter
//	bus_type	
//	bus_Pd	 (shunts and svc are considered)
//	bus_Qd	 (shunts and svc are considered)
//	bus_vm	
//	bus_va.

//  3-Output data's format (by column)
//	No.
//	system_title : raw file name (".raw" is excluded).
//	gen_number	: 1 - line; 2 - transformer; 3 - phase shifter
//	gen_bus
//	gen_Status	
//	gen_Pg
//	gen_Qg
//	gen_Pmax
//	gen_pmin
//	gen_Qmax
//	gen_Qmin
	
	PsseModel _model;
	FastDecoupledPowerFlow _pf;
	
	OutputPFData(PsseModel model, FastDecoupledPowerFlow pf)
	{
		_model = model;
		_pf = pf;
	}
	
	//TODO if needed.
	void outputLaunch(PrintStream outFile)
	{
		
	}
	
	
	@Test
	public static void main(String[] args) throws Exception
	{
		long time_Start = System.nanoTime();
		
		ReadFilesInAFolder readFiles = new ReadFilesInAFolder("C:\\Users\\xingpeng\\data\\PJMSystem\\raw", true);
		//ReadFilesInAFolder readFiles = new ReadFilesInAFolder("C:\\Users\\xingpeng\\data\\test cases - small scale", true);
//		ReadFilesInAFolder readFiles = new ReadFilesInAFolder("D:\\pjm", true);
//		ReadFilesInAFolder readFiles = new ReadFilesInAFolder("dataToRead", false);
		readFiles.readFileNames();
		String[] fileNames = readFiles.getAllRawFileNames();
		String absPath = readFiles.getPath();
		String absPathOutput = System.getProperty("user.dir")+"/filesOfOutput/";
		
		boolean outputInOneSingleFile = false;
		String svstart = "Flat";
		boolean MarkVarLimit = false; // NOTE: if Gen Var Limit is considered, Qg output should be modified !!! 
		int numCount = 1;
		int numCount2 = 1;
		int numCount3 = 1;
		for (int iFile=0; iFile<fileNames.length; iFile++)
		{
			String uri = "psseraw:file="+absPath+fileNames[iFile]+"&lowx=adjust";
			PsseModel model = PsseModel.Open(uri);
			String hourStr = fileNames[iFile].substring(0, fileNames[iFile].length()-4);
			
			String outputFileName = hourStr + " - NetWorkData.txt";
			String outputFileName2 = hourStr + " - BusData.txt";
			String outputFileName3 = hourStr + " - GenData.txt";
			if (outputInOneSingleFile == true) 
			{
				outputFileName = "NetWorkData.txt";
				outputFileName2 = "BusData.txt";
				outputFileName3 = "GenData.txt";
			}
	 	    PrintStream outFile = new PrintStream (new FileOutputStream((absPathOutput+outputFileName), true));
	 	    PrintStream outFile2 = new PrintStream (new FileOutputStream((absPathOutput+outputFileName2), true));
	 	    PrintStream outFile3 = new PrintStream (new FileOutputStream((absPathOutput+outputFileName3), true));

			float minxmag = 0.00001f;
			model.setMinXMag(minxmag);
			ImpedanceFilter zfilt = model.getXFilter();
			FastDecoupledPowerFlow pf = new FastDecoupledPowerFlow(model);
//			if (MarkVarLimit)
//			{
//				ElemGroupAtGenBuses elemGroupGenBuses = new ElemGroupAtGenBuses(model);
//				pf.setElemGroupAtGenBuses(elemGroupGenBuses);
//			}
			pf.setMarkUsingVgSched(true);
			pf.setRealTimeStartForGenBus(true);
			VoltageSource vstart = VoltageSource.fromConfig(svstart);
			pf.setShowRunInfo(false);
			pf.runPowerFlow(vstart, MarkVarLimit, 1);
			if (pf.isPfConv() == true)
			{
				float[] vm = pf.getVM();
				float[] va = pf.getVA();
				BusList buses = model.getBuses();
				
				float[][] loads = pf.getBusLoad();
				float[] Pload = loads[0];
				float[] Qload = loads[1];
				float[][] shunts = pf.getBusShunt();
				float[] Pshunt = shunts[0];
				float[] Qshunt = shunts[1];
				
				for (int ib=0; ib<vm.length; ib++)
				{
					outFile2.print(" "+numCount2++);
					outFile2.print(" "+hourStr);
					outFile2.print(" "+buses.getI(ib));
					
					int bustype = 1;
					if (buses.getBusType(ib) == BusTypeCode.Gen) bustype = 2;
					else if (buses.getBusType(ib) == BusTypeCode.Slack) bustype = 3;
					outFile2.print(" "+bustype);

					float pd = (Pload[ib] - Pshunt[ib]*vm[ib]*vm[ib])/100;
					float qd = (Qload[ib] - Qshunt[ib]*vm[ib]*vm[ib])/100;
					
					BusGroupElems busGrpElems = model.getBusGroupElems();
					int[] idxSvcs = busGrpElems.getSVCIndex(ib);
					if (idxSvcs != null)
					{
						float busSvcB = 0f;
						for(int j=0; j<idxSvcs.length; j++)
						{
							int idxSvc = idxSvcs[j];
							if(model.getSvcs().isInSvc(idxSvc))
							{
								busSvcB += model.getSvcs().get(idxSvc).getBINIT()/100f;
							}
						}
						qd -= busSvcB*vm[ib]*vm[ib];									
					}
					outFile2.print(" "+pd);
					outFile2.print(" "+qd);
					outFile2.print(" "+vm[ib]);
					outFile2.print(" "+va[ib]);
					outFile2.println();
				}
				
				GenList gens = model.getGenerators();
				for (int ig=0; ig<gens.size(); ig++)
				{
					outFile3.print(" "+numCount3++);
					outFile3.print(" "+hourStr);
					outFile3.print(" "+(ig+1));
					outFile3.print(" "+gens.getBus(ig).getI());
					
					int status = 1;
					if (gens.isInSvc(ig) == false) status = 0;
					outFile3.print(" "+status);
					
					outFile3.print(" "+gens.getPpu(ig));
					outFile3.print(" "+gens.getQpu(ig));
					outFile3.print(" "+gens.getPT(ig)/100);
					outFile3.print(" "+gens.getPB(ig)/100);
					outFile3.print(" "+gens.getQT(ig)/100);
					outFile3.print(" "+gens.getQB(ig)/100);
					outFile3.println();
				}
				
				float[] pfrm = pf.getPfrom();
				float[] pto = pf.getPto();
				float[] qfrm = pf.getQfrom();
				float[] qto = pf.getQto();
				float[] sfrm = pf.getSfrom();
				float[] sto = pf.getSto();
				
				ACBranchList branches = model.getBranches(); 
				int numLines = model.getLines().size();
				int numTransf = model.getTransformers().size();
				for (int idx=0; idx<branches.size(); idx++)
				{
					outFile.print(" "+numCount++);
					outFile.print(" "+hourStr);
					
					int brcType = 1;
					if (idx >= numLines)
					{
						if ((idx - numLines) < numTransf) brcType = 2;
						else brcType = 3;
					}
					outFile.print(" "+brcType);
					
					ACBranch branch = branches.get(idx);
					Bus frmBus = branch.getFromBus();
					Bus toBus = branch.getToBus();
					int frmBusNum = frmBus.getI();
					int toBusNum = toBus.getI();
					String ckt = branch.getCKT();
					int status = 1;
					if (branch.isInSvc() == false) status = 0;
					
					outFile.print(" "+(idx+1));
					outFile.print(" "+frmBusNum);
					outFile.print(" "+toBusNum);
					outFile.print(" "+ckt);
					outFile.print(" "+status);
					
					float bpi = branch.getBmag()+branch.getFromBchg();
					outFile.print(" "+bpi);
					
					float rij = zfilt.getR(idx);
					float xij = zfilt.getX(idx);
					float gij = zfilt.getY(idx).re();
					float bij = zfilt.getY(idx).im();
					outFile.print(" "+rij);
					outFile.print(" "+xij);
					outFile.print(" "+gij);
					outFile.print(" "+bij);
					
					assertTrue(branch.getFromBchg() == branch.getToBchg()); 
					outFile.print(" " + (branch.getFromBchg()*2));
					outFile.print(" " + branch.getFromBchg());
					
					outFile.print(" " + branch.getFromTap());
					outFile.print(" " + branch.getToTap());
					outFile.print(" " + branch.getPhaseShift());
					outFile.print(" " + branch.getBmag());

					int frmBusIdx = frmBus.getIndex();
					int toBusIdx = toBus.getIndex();
					outFile.print(" " + vm[frmBusIdx]);
					outFile.print(" " + vm[toBusIdx]);
					outFile.print(" " + va[frmBusIdx]);
					outFile.print(" " + va[toBusIdx]);
					
					float thetaK = va[frmBusIdx] - va[toBusIdx] - branch.getPhaseShift();
					outFile.print(" " + thetaK);
					
					outFile.print(" " + (-pfrm[idx]));
					outFile.print(" " + (-pto[idx]));
					outFile.print(" " + (-qfrm[idx]));
					outFile.print(" " + (-qto[idx]));
					outFile.print(" " + sfrm[idx]);
					outFile.print(" " + sto[idx]);
					
					outFile.print(" " + branches.getRateA(idx)/100);
					outFile.print(" " + branches.getRateB(idx)/100);
					outFile.print(" " + branches.getRateC(idx)/100);
					outFile.print(" " + frmBus.getBASKV());
					outFile.print(" " + toBus.getBASKV());
					outFile.println();
				}
			}
			else
			{
				String title = "Power flow for "+fileNames[iFile]+" does not converge. ";
				AuxFileXL.initFileWithTitle(absPathOutput+"PFnotConverge.txt", title, true);
			}
			outFile.close();
			outFile2.close();
			outFile3.close();
		}
		
		System.out.println("Total simulation time is: " + (System.nanoTime() - time_Start)/1e9);
		System.out.println("Simulation is done here!");
	}
	
}
