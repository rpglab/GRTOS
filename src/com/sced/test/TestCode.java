package com.sced.test;

import com.sced.auxData.FormLODFXL;
import com.sced.auxData.FormOTDFXL;
import com.sced.auxData.FormPTDFXL;
import com.sced.input.ReadModelDataXL;
import com.sced.model.SystemModelXL;
import com.sced.model.data.BusGrpXL;
import com.sced.model.data.LoadListXL;
import com.sced.util.ReadMultiCollumDoubleDataXL;
import com.utilxl.log.DiaryXL;

public class TestCode {
	
	public static void main(String[] args) {
		DiaryXL diary = new DiaryXL();
		diary.setFileName(diary.getFileName() + "_XL");
		diary.initial();

		int idxReferenceBus = 0; // index starting from 0
//		ReadMultiCollumDoubleData dataApp = new ReadMultiCollumDoubleData(6, 3, "input/ptdf/case5.txt", false); //idxReferenceBus = 3
//		ReadMultiCollumDoubleData dataApp = new ReadMultiCollumDoubleData(5, 3, "input/ptdf/case5_ctgcy3.txt", false); //idxReferenceBus = 3
		ReadMultiCollumDoubleDataXL dataApp = new ReadMultiCollumDoubleDataXL(20, 4, "input/ptdf/case14XL.txt", false); //idxReferenceBus = 0
//		ReadMultiCollumDoubleData dataApp = new ReadMultiCollumDoubleData(41, 3, "input/ptdf/case30.txt", false); //idxReferenceBus = 0
//		ReadMultiCollumDoubleData dataApp = new ReadMultiCollumDoubleData(40, 3, "input/ptdf/case30_ctgcy8.txt", false); //idxReferenceBus = 0
//		ReadMultiCollumDoubleData dataApp = new ReadMultiCollumDoubleData(186, 3, "input/ptdf/case118.txt", false); //idxReferenceBus = 68
//		ReadMultiCollumDoubleData dataApp = new ReadMultiCollumDoubleData(20486, 3, "input/ptdf/pjm_1.txt", false); //idxReferenceBus = 13842
		double[][] rawData = dataApp.getArrayByCol();
		int nLine = rawData[0].length;
		
		int[] frmBuses = new int[nLine];
		int[] toBuses = new int[nLine];
		int nBus = -1;
		for (int i=0; i<nLine; i++) {
			int frmBus = (int)rawData[0][i] - 1;
			int toBus = (int)rawData[1][i] - 1;
			frmBuses[i] = frmBus;
			toBuses[i] = toBus;
			if (nBus < frmBus) nBus = frmBus;
			if (nBus < toBus) nBus = toBus;
		}
		nBus += 1;
		double[] x = rawData[2];
		boolean[] st = new boolean[20];
		for (int i=0; i<20; i++){
			st[i] = (int)rawData[3][i] == 1 ? true : false;
		}

		st[16] = false;
		FormPTDFXL calcPTDF = new FormPTDFXL(nBus, idxReferenceBus, frmBuses, toBuses, x, st);
		double[][] ptdf = calcPTDF.getPTDF();
		
		
		ReadModelDataXL read = new ReadModelDataXL("input/sced_ver01/small_case_sced_data/w_title", diary);
//		ReadModelData read = new ReadModelData("input/sced_ver01/pjm_sced_data/w_title", diary);
		read.setIsHasHeading(true);
		read.readData();
		
		SystemModelXL model = new SystemModelXL(diary);
		model.fillData(read);
		model.getGens().setPgInit(0, 0.6107);
		model.getGens().setPgInit(1, 0.0);
		model.getGens().setPgInit(2, 0.14);
		model.getGens().setPgInit(3, 0.0);
		model.getGens().setPgInit(4, 0.40);
		model.getGens().setPgInit(5, 0.45);
		model.getGens().setPgInit(6, 0.3641);
		

		double[] pk = new double[nLine];
		BusGrpXL busGrp = model.getBusGrp();
		LoadListXL loads = model.getLoads();
		for (int k=0; k<nLine; k++) {
			for (int n=0; n<nBus; n++) {
				pk[k] += -loads.getBusLoad(n)*ptdf[k][n];
				/* gen impact on line flow */
				int[] idxGen = busGrp.getGenIndex(n);  
				if (idxGen != null) {
					for (int g=0; g<idxGen.length; g++) {
						double pgInit = model.getGens().getPgInit(idxGen[g]);
						pk[k] += pgInit*ptdf[k][n];
					}
				}
			}
		}

		
		//OutputArrays.outputArray(ptdf, true, "output/ptdfArray.txt", false, false);
		
		/* Test LODF calculation */
		diary.hotLine("Start calculating LODF");
		FormLODFXL calcLODF = new FormLODFXL(calcPTDF);
//		double[][] lodfMatrix = calcLODF.getLODFMatrix(frmBuses, toBuses);
		double[][] lodfMatrix = calcLODF.getLODFMatrix();
//		for (int idxLine=0; idxLine<frmBuses.length; idxLine++) {
//		for (int idxLine=0; idxLine<1000; idxLine++) {
//			//int idxLine = i;
//			double[] lodfVector = calcLODF.getLODF(idxLine, frmBuses[idxLine], toBuses[idxLine]);
//		}
		
		/* Test OTDF calculation */
		FormOTDFXL calcOTDF = new FormOTDFXL(calcPTDF, calcLODF);
		double[] otdf = calcOTDF.getOTDFInjBus(13, 8);
		double[] otdf2 = calcOTDF.getOTDFMonBrc(0, 16);
		
		
		/* Program ends here */
		System.out.println("Program pseudo ends");
		diary.done();
		System.out.println("Program ends");
		
	}

}
