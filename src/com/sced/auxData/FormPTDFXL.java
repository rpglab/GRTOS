package com.sced.auxData;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

import com.sced.auxData.ptdf.SparseMatrixInverseXL;
import com.sced.model.SystemModelXL;

/**
 * Power Transfer Distribution Factor (PTDF)
 * large initial stack space is needed
 * When use eclipse, put "-Xmx10240m" in the VM arguments section for 10k+ bus system.
 * 
 * Initialized in September 2016.
 * 
 * @author Xingpeng.Li (xplipower@gmail.com)
 *
 */
public class FormPTDFXL {
	
	SystemModelXL _sysModel;
	
	int _nbus;
	int _slack;
	int[] _frmBuses;
	int[] _toBuses;
	double[] _x;
	boolean[] _stat;
	
	double[][] _ptdf;

	public FormPTDFXL(SystemModelXL model) {
		_sysModel = model;
		init(_sysModel.getBuses().size(), _sysModel.getBuses().getSlackBusIdx(), _sysModel.getBranches().getFrmBusIdx(), _sysModel.getBranches().getToBusIdx(), _sysModel.getBranches().getX());
		_stat = _sysModel.getBranches().getIsInSvc();
	}
	
	public FormPTDFXL(int nbus, int slack, int[] frmBuses, int[] toBuses, double[] x) {
		init(nbus, slack, frmBuses, toBuses, x);
		_stat = new boolean[frmBuses.length];
		Arrays.fill(_stat, true);
	}
	
	public FormPTDFXL(int nbus, int slack, int[] frmBuses, int[] toBuses, double[] x, boolean[] stat) {
		init(nbus, slack, frmBuses, toBuses, x);
		_stat = stat;
	}
	
	private void init(int nbus, int slack, int[] frmBuses, int[] toBuses, double[] x) {
		_nbus = nbus;
		_slack = slack;
		_frmBuses = frmBuses;
		_toBuses = toBuses;
		_x = x;
		if (_sysModel != null) _sysModel.getDiary().hotLine("FormPTDF class is initialized for creating PTDF matrix");
	}
	
	/* Free-up memory  */
	public void clearPTDF() {_ptdf = null;}
	
	public double[][] getPTDF() {
		if (_ptdf == null) calcPTDF();
		return _ptdf; 
	}

	public double getPTDF(int idxBrc, int idxBus) {
		if (_ptdf == null) calcPTDF();
		return _ptdf[idxBrc][idxBus]; 
	}

	/** inverse the stored sparse matrix */
	private void calcPTDF() {
		System.out.println("PTDF matrix calculation starts");
		if (_sysModel != null) _sysModel.getDiary().hotLine("PTDF matrix calculation starts");
		SparseMatrixInverseXL bdc = new SparseMatrixInverseXL(_nbus, _slack, _frmBuses, _toBuses, _x, _stat);
		bdc.buildMatrices();

		double[][] ZMatrix = new double[_nbus][_nbus];
        double[] rhs = new double[_nbus];
		for (int i=0; i<rhs.length; i++) {
			if (i == _slack) continue;
			rhs[i] = 1;
			ZMatrix[i] = bdc.solve(rhs);
			rhs[i] = 0;
		}
		
		double[][] ptdf = new double[_frmBuses.length][_nbus];
		for (int i=0; i<_frmBuses.length; i++) {
			if (_stat[i] == false) continue;
			for (int j=0; j<_nbus; j++) {
				int busIdx = j;
				if (busIdx == _slack) ptdf[i][j] = 0;
				else {
					int frmBus = _frmBuses[i];
					int toBus = _toBuses[i];
					double zmi = ZMatrix[frmBus][busIdx];
					double zni = ZMatrix[toBus][busIdx];
//					if (_slack == j) {
//						System.out.println("slack stop rest and then continue...");
//					}
					ptdf[i][j] = (zmi - zni)/_x[i];
				}
			}
		}
		_ptdf = ptdf;
		System.out.println("PTDF calculation process is done here");
		if (_sysModel != null) {
			_sysModel.getDiary().hotLine("PTDF matrix calculation is done");
			_sysModel.getDiary().hotLine("The size of PTDF matrix is: "+_frmBuses.length+"*"+_nbus);
			_sysModel.getDiary().hotLine("The reference bus for PTDF matrix is: "+(_slack+1));
		}
	}
	
	public SystemModelXL getSystemModel() {return _sysModel;}
	
	public int getSlackBusIdx() {return _slack;}
	public int sizeBrc() {return _frmBuses.length;}
	public int sizeBus() {return _nbus;}
	
	public int[] getFrmBuses() {return _frmBuses;}
	public int[] getToBuses() {return _toBuses;}

	public int getFrmBuses(int k) {return _frmBuses[k];}
	public int getToBuses(int k) {return _toBuses[k];}

	
	public void dump()
	{		
		String fileName = "PTDF_Matrix.csv";
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
		if (_sysModel != null) _sysModel.getDiary().hotLine("PTDF matrix is dumpped to the file "+fileName);
	}
	
	private void dump(PrintWriter pw)
	{
		double[][] ptdf = getPTDF();
		int nbrc = ptdf.length;
		
		pw.println("The reference bus is: bus_" + (_slack+1));
		pw.println("PTDF matrix: nbrc * nbus");
		pw.format("%d*%d,", nbrc, _nbus);
		for (int n=0; n<_nbus; n++)
			pw.format("bus_%d,", (n+1));
		pw.println();
		
		for (int i=0; i<nbrc; i++) {
			pw.format("branch_%d,", (i+1));
			for (int j=0; j<_nbus; j++) {
				pw.format("%f,", ptdf[i][j]);
			}
			pw.format("\n");
		}
	}

	
}
