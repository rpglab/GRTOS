package com.sced.auxData;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/** 
 * Line Outage Distribution Factor (LODF)
 * 
 * Initialized in September 2016.
 * 
 * @author Xingpeng.Li (xplipower@gmail.com)
 * 
 */
public class FormLODFXL {
	
	FormPTDFXL _ptdf;

	public FormLODFXL(FormPTDFXL ptdf) {
		_ptdf = ptdf;
		if (_ptdf.getSystemModel() != null) _ptdf.getSystemModel().getDiary().hotLine("FormLODF class is initialized for calculating LODF");
	}
	
	
	/** LODF for outage of line @param idxLine */
	public double getLODF(int idxLineCtgcy, int idxLineMonitor) {
		if (idxLineMonitor == idxLineCtgcy) return -1;
		double lodf = _ptdf.getPTDF(idxLineMonitor, _ptdf.getFrmBuses(idxLineCtgcy)) - _ptdf.getPTDF(idxLineMonitor, _ptdf.getToBuses(idxLineCtgcy));
		double lodfCtgcy = _ptdf.getPTDF(idxLineCtgcy, _ptdf.getFrmBuses(idxLineCtgcy)) - _ptdf.getPTDF(idxLineCtgcy, _ptdf.getToBuses(idxLineCtgcy));
		lodf = lodf / (1.0 - lodfCtgcy);
		return lodf;
	}

	public double[] getLODF(int idxCtgcyLine) {
		return getLODF(idxCtgcyLine, _ptdf.getFrmBuses(idxCtgcyLine), _ptdf.getToBuses(idxCtgcyLine));
	}
	
	/** LODF for outage of line @param idxCtgcyLine 
	 * @frmBus is the from bus of the contingency line,
	 * @toBus is the to bus of the contingency line.
	 * */
	public double[] getLODF(int idxCtgcyLine, int frmBus, int toBus) {
		double[] lodf = new double[_ptdf.sizeBrc()];
//		double[][] ptdf = _ptdf.getPTDF();
		for (int i=0; i<_ptdf.sizeBrc(); i++) {
			lodf[i] = _ptdf.getPTDF(i, frmBus) - _ptdf.getPTDF(i, toBus);
//			lodf[i] = ptdf[i][frmBus] - ptdf[i][toBus];
		}
		double tmpNum = 1.0 - lodf[idxCtgcyLine];
		for (int i=0; i<_ptdf.sizeBrc(); i++) {
			lodf[i] = lodf[i]/tmpNum;
		}
		lodf[idxCtgcyLine] = -1;
		return lodf;
	}

	public double[][] getLODFMatrix() {
		return getLODFMatrix(_ptdf.getFrmBuses(), _ptdf.getToBuses());
	}

	public double[][] getLODFMatrix(int[] frmBuses, int[] toBuses) {
		System.out.println("LODF matrix calculation starts");
		double[][] lodfMatrix = new double[frmBuses.length][];
		for (int i=0; i<frmBuses.length; i++) {
			lodfMatrix[i] = getLODF(i, frmBuses[i], toBuses[i]);
		}
		System.out.println("LODF calculation process is done here");
		return lodfMatrix;
	}
	
	
	public void dump()
	{		
		String fileName = "LODF_Matrix.csv";
		File pout = new File(fileName);
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(new BufferedWriter(new FileWriter(pout)));
		} catch (IOException e) {
			e.printStackTrace();
		}
		dump(pw);
		pw.flush();
		pw.close();
	}
	
	private void dump(PrintWriter pw)
	{
		double[][] lodf = getLODFMatrix();
		int nbrc = _ptdf.sizeBrc();
		
		pw.println("Each row corresponds to a contingency");
		pw.println("LODF matrix: nbrc * nbrc");
		pw.format("%d*%d,", nbrc, nbrc);
		for (int n=0; n<nbrc; n++)
			pw.format("branch_%d,", (n+1));
		pw.println();
		
		for (int i=0; i<nbrc; i++) {
			pw.format("Ctgcy-branch_%d,", (i+1));
			for (int j=0; j<nbrc; j++) {
				pw.format("%f,", lodf[i][j]);
			}
			pw.format("\n");
		}
	}


	

}
