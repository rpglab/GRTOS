package com.cyberattack;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import com.cyberattack.CyberAttackFDIDSim.DetectionType;
import com.sced.util.ReadFilesInAFolderXL;

public class BatchCyberAttackFDIDSim {
	
	
	public static void main(String[] args) throws IOException
	{
		//String path2Folder = "input/FDI_model/case118_20171024_w_noise/Ln_118";
		//String path2Folder = "input/FDI_model/case118_20170911_only_w_noise";
		//String path2Folder = "input/FDI_model/case118_20171024_const/Ln_111";
		//String path2Folder = "input/FDI_model/polish_v1_wNoise/N-10_L-0.20";
		//String path2Folder = "input/FDI_model/polish_v1_wNoiseONLY";
		//String path2Folder = "input/FDI_model/case73_wAttack/Ln_99";
		//String path2Folder = "input/FDI_model/IEEE_118-bus/outage_141/FDI/wFDI/Ln_118";
		//String path2Folder = "input/FDI_model/IEEE_118-bus/outage_141/FDI/wFDI_wNoise/Ln_118";
		String path2Folder = "input/FDI_model/IEEE_118-bus/outage_141/FDI/wNoise_ONLY";
		ReadFilesInAFolderXL streamer = new ReadFilesInAFolderXL(path2Folder, false);
		streamer.readFileNames();
		int nfolder = streamer.getFolders().length;

		double[][] SMLDI = new double[nfolder][];
		int numCAIElem = 3;
		int[][] idxCAI = new int[nfolder][numCAIElem];
		double[][] valueCAI = new double[nfolder][numCAIElem];
		int[][] idxALC = new int[nfolder][];
		
		for (int i=0; i<nfolder; i++) {
			String path2Case = path2Folder + "/" + streamer.getFolderName(i);
			System.out.println("Processing case: " + path2Case);
			LoadFDIData loading = new LoadFDIData(path2Case);
			loading.loadData();
			
			CyberAttackDataCenter fdiModel = loading.getFDIDataModel();
			fdiModel.getCyberAttackMonitorSet().enableMonitorAllBrc();
			
			CyberAttackFDIDSim fdidSim = new CyberAttackFDIDSim(fdiModel);
			fdidSim.detectBaseCase();
			fdidSim.dump(path2Case);
			
			SMLDI[i] = fdidSim.calcAvgTopRatio();
			idxCAI[i] = fdidSim.getIdxMaxElemInCAI(numCAIElem);
			double[] CAI = fdidSim.getCAI();
			for (int j=0; j<numCAIElem; j++) {
				valueCAI[i][j] = CAI[idxCAI[i][j]];
			}
			idxALC[i] = fdidSim.getIdxALC(DetectionType.Danger);
		}
		
		File pout = new File("FDIDResults_Statistics.csv");
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(new BufferedWriter(new FileWriter(pout)));
		} catch (IOException e) {
			e.printStackTrace();
		}
		pw.println(path2Folder);
		pw.println("case,AvgTop5,AvgTop8,AvgTop10,AvgTop12,AvgTop15,AvgTop20, idxCAI1, idxCAI2, idxCAI3, valueCAI1, valueCAI2, valueCAI3, numALC-Danger");
		for (int i=0; i<nfolder; i++) {
			pw.format("%s,%f,%f,%f,%f,%f,%f,%d,%d,%d,%f,%f,%f",
					streamer.getFolderName(i), SMLDI[i][0], SMLDI[i][1], SMLDI[i][2], 
					SMLDI[i][3], SMLDI[i][4], SMLDI[i][5],
					(idxCAI[i][0]+1), (idxCAI[i][1]+1), (idxCAI[i][2]+1),
					valueCAI[i][0], valueCAI[i][1], valueCAI[i][2]);
			if (idxALC[i] == null) {pw.format(",0\n"); continue;}
			if (idxALC[i].length == 0) {pw.format(",0\n"); continue;}
			pw.format(",%d", idxALC[i].length);
			for (int j=0; j<idxALC[i].length; j++) {
				pw.format(",%d", (idxALC[i][j]+1));
			}
			pw.format("\n");
		}
		pw.flush();
		pw.close();
		
		System.out.println("Simulation is done here");
	}

}
