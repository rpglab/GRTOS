package com.sced.test;

import com.sced.auxData.ptdf.SparseMatrixInverseXL;
import com.sced.util.OutputArraysXL;
import com.sced.util.ReadMultiCollumDoubleDataXL;
import com.utilxl.log.DiaryXL;

public class TestTmpCode {

	public static void main(String[] args) {
		DiaryXL diary = new DiaryXL();
		diary.setFileName(diary.getFileName() + "_XL");
		diary.initial();
		
		int inverseOption = 4;       // 1: JAMA, 2: ACmM, 3: la4j, 4: SparseMatrix
		int idxReferenceBus = 68; // index starting from 0
//		ReadMultiCollumDoubleData dataApp = new ReadMultiCollumDoubleData(6, 3, "input/ptdf/case5.txt", false); //idxReferenceBus = 3
//		ReadMultiCollumDoubleData dataApp = new ReadMultiCollumDoubleData(41, 3, "input/ptdf/case30.txt", false); //idxReferenceBus = 0
		ReadMultiCollumDoubleDataXL dataApp = new ReadMultiCollumDoubleDataXL(186, 3, "input/ptdf/case118.txt", false); //idxReferenceBus = 68
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
		double[][] ptdf = new double[nLine][nBus];
		
		
		double[] x = rawData[2];
		System.out.println("Program starts Bp matrix calculation process");
		diary.hotLine("Program starts Bp matrix calculation process");
		double[][] BpMatrix = new double[nBus-1][nBus-1];
		for (int i=0; i<nLine; i++) {
			int frmBus = frmBuses[i];
			int toBus = toBuses[i];
			boolean isFrmRefBus = false, isToRefBus = false;
			if (frmBus > idxReferenceBus) frmBus--;
			else if (frmBus == idxReferenceBus) isFrmRefBus = true;
			if (toBus > idxReferenceBus) toBus--;
			else if (toBus == idxReferenceBus) isToRefBus = true;
			
			if (isFrmRefBus == false) BpMatrix[frmBus][frmBus] += 1/x[i];
			if (isToRefBus == false) BpMatrix[toBus][toBus] += 1/x[i];
			if (isFrmRefBus == false && isToRefBus == false) {
				BpMatrix[frmBus][toBus] = -1/x[i];
				BpMatrix[toBus][frmBus] = -1/x[i];
			}
		}

		System.out.println("Program starts inverse Bp matrix calculation process");
		diary.hotLine("Program starts inverse Bp matrix calculation process");
		double[][] ZMatrix = new double[BpMatrix.length][BpMatrix.length];
		{
			//ZMatrix = inv(BpMatrix);
			
			if (inverseOption == 1) {
				
			} else if (inverseOption == 2) {

			} else if (inverseOption == 3) {
				/* Not ready yet */
				//InterfaceLA4J matx = new InterfaceLA4J(BpMatrix);
				//ZMatrix = matx.inverseGaussJordan();
				
			} else if (inverseOption == 4) {
				SparseMatrixInverseXL bp = new SparseMatrixInverseXL(nBus, new int[] {idxReferenceBus}, frmBuses, toBuses, x);
				bp.buildMatrices();
				diary.hotLine("Program finishes LU Decomposition process");
				
		        double[] rhs = new double[BpMatrix.length+1];
				for (int i=0; i<rhs.length; i++) {
					//System.out.println("i: "+i);
					//if (i == idxReferenceBus) continue;
					rhs[i] = 1;
					double[] tmpArray = bp.solve(rhs);
					int idxZMatrix = i;
					if (i > idxReferenceBus) idxZMatrix = idxZMatrix - 1;
					System.arraycopy(tmpArray, 0, ZMatrix[idxZMatrix], 0, idxReferenceBus);
					System.arraycopy(tmpArray, idxReferenceBus + 1, ZMatrix[idxZMatrix], idxReferenceBus, rhs.length - idxReferenceBus - 1);
//					if (rhs.length - idxReferenceBus - 1 > 0) {
//					}
					rhs[i] = 0;
				}
			} else if (inverseOption == 5) {
				SparseMatrixInverseXL bp = new SparseMatrixInverseXL(nBus, new int[] {idxReferenceBus}, frmBuses, toBuses, x);
				bp.buildMatrices();
				
		        double[] rhs = new double[BpMatrix.length+1];
				for (int i=0; i<rhs.length; i++) {
					System.out.println("i: "+i);
					if (i == idxReferenceBus) continue;
					//TODO:
					rhs[i] = 1;
					double[] tmpArray = bp.solve(rhs);
					int idxZMatrix = i;
					if (i > idxReferenceBus) idxZMatrix = idxZMatrix - 1;
					System.arraycopy(tmpArray, 0, ZMatrix[idxZMatrix], 0, idxReferenceBus);
					System.arraycopy(tmpArray, idxReferenceBus + 1, ZMatrix[idxZMatrix], idxReferenceBus, rhs.length - idxReferenceBus - 1);
//					if (rhs.length - idxReferenceBus - 1 > 0) {
//					}
					rhs[i] = 0;
				}
			}
		}
		OutputArraysXL.outputArray(ZMatrix, true, "output/ZMatrixArray.txt", false, false);
		
		if (inverseOption != 5) {
			String message = "Program starts ptdf calculation process";
			System.out.println(message);
			diary.hotLine(message);
			for (int i=0; i<nLine; i++)
				for (int j=0; j<nBus; j++) {
					int busIdx = j;
					if (busIdx == idxReferenceBus) ptdf[i][j] = 0;
					else {
						int frmBus = frmBuses[i];
						int toBus = toBuses[i];
						
						double zmi = 0, zni = 0;
						if (j > idxReferenceBus) busIdx = j - 1;
						
						if (frmBus < idxReferenceBus) zmi = ZMatrix[frmBus][busIdx];
						else if (frmBus > idxReferenceBus) zmi = ZMatrix[frmBus-1][busIdx];
						
						if (toBus < idxReferenceBus) zni = ZMatrix[toBus][busIdx];
						else if (toBus > idxReferenceBus) zni = ZMatrix[toBus-1][busIdx];

						ptdf[i][j] = (zmi - zni)/x[i];
//						ptdf[i][j] = (ZMatrix[frmBus][busIdx] - ZMatrix[toBus][busIdx])/x[i];
					}
					//System.out.println("i: "+i +", j: " +j +" PTDFvalue: "+ptdf[i][j]);
				}
			System.out.println("PTDF calculation process is done here");
		}
		OutputArraysXL.outputArray(ptdf, true, "output/ptdfArray.txt", false, false);
		
		/* Program ends here */
		System.out.println("Program pseudo ends");
		diary.done();
		System.out.println("Program ends");
	}
	
}
