package com.rtca_cts.contingencyanalysis;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import com.powerdata.openpa.psse.ACBranchList;
import com.powerdata.openpa.psse.GenList;
import com.powerdata.openpa.psse.PsseModelException;
import com.rtca_cts.param.ParamIO;
import com.utilxl.array.AuxArrayXL;
import com.utilxl.iofiles.OutputArrays;

/**
 * 
 * @author Xingpeng.Li, xplipower@gmail.com 
 *
 */
public class WriteContiList {
	
	public static void dumpOneColumnContiList(int[] contiList, String fileName)
	{
		fileName = ParamIO.getCAPath() + fileName;
		AuxArrayXL.allElemsPlusANum(contiList, 1);  // index then starts from 1.
		OutputArrays.outputArray(contiList, fileName, true, false, true);
	}

	public static void dumpBrcContiList(ACBranchList branches, int[] brcList, String fileName) throws PsseModelException
	{
		fileName = ParamIO.getCAPath() + fileName;
		try
		{
		    OutputStream resultFile = new FileOutputStream(fileName, false);
		    PrintStream outFile = new PrintStream (resultFile);
			outFile.println(" Branch index starts from 1.");
			outFile.println(" No., idxContiBrc, frmBusNum, toBusNum, brcID");
			if (brcList != null)
			{
				for(int i=0; i<brcList.length; i++)
				{
					outFile.print(" "+(i+1));
					int idxContiBrc = brcList[i];
					outFile.print(" "+(idxContiBrc+1));
					int frmBusNum = branches.getFromBus(idxContiBrc).getI();
					int toBusNum = branches.getToBus(idxContiBrc).getI();
					outFile.print(" "+frmBusNum);
					outFile.print(" "+toBusNum);
					outFile.println(" "+branches.getCKT(idxContiBrc));
				}
			}
		    outFile.close();
//		    System.out.println("Write Brc Conti List successfully");
	    } catch (FileNotFoundException e) {
	    	System.out.println();
	    	System.out.println("Cannot write Brc Conti List to file" + e);
	    	e.printStackTrace();
	    }
	}

	public static void dumpGenContiList(GenList gens, int[] genList, String fileName) throws PsseModelException
	{
		fileName = ParamIO.getCAPath() + fileName;
		try
		{
		    OutputStream resultFile = new FileOutputStream(fileName, false);
		    PrintStream outFile = new PrintStream (resultFile);
			outFile.println(" Gen index starts from 1.");
			outFile.println(" No., idxContiGen, genBusNum, genID");
			if (genList != null)
			{
				for(int i=0; i<genList.length; i++)
				{
					outFile.print(" "+(i+1));
					int idxContiGen = genList[i];
					outFile.print(" "+(idxContiGen+1));
					int genBusNum = gens.getBus(idxContiGen).getI();
					gens.getI(idxContiGen);
					outFile.print(" "+genBusNum);
					String genID = gens.getID(idxContiGen);
					outFile.println(" "+genID);
				}
			}
		    outFile.close();
//		    System.out.println("Write Gen Conti List successfully");
	    } catch (FileNotFoundException e) {
	    	System.out.println();
	    	System.out.println("Cannot write Gen Conti List to file" + e);
	    	e.printStackTrace();
	    }
	}

	
}
