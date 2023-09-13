package com.sced.auxData.ptdf;

import java.util.Arrays;

/**
 * Perform sparse matrix inversion 
 * using some classes from 1st version OpenPA
 * 
 * @author Xingpeng.Li (xplipower@gmail.com)
 *
 */
public class SparseMatrixInverseXL {

	int _nbus;
	int[] _slack;
	int[] _frmBuses;
	int[] _toBuses;
	double[] _x;
	boolean[] _stat;
	
	double[] _bself;
	double[] _bbranch;
	LinkNetXL _net;
	
	FactorizedBMatrixXL _bp;
	
	public SparseMatrixInverseXL(int nbus, int slack, int[] frmBuses, int[] toBuses, double[] x) {
		init(nbus, new int[] {slack}, frmBuses, toBuses, x);
		_stat = new boolean[frmBuses.length];
		Arrays.fill(_stat, true);
	}
	
	public SparseMatrixInverseXL(int nbus, int[] slack, int[] frmBuses, int[] toBuses, double[] x) {
		init(nbus, slack, frmBuses, toBuses, x);
		_stat = new boolean[frmBuses.length];
		Arrays.fill(_stat, true);
	}
	
	public SparseMatrixInverseXL(int nbus, int slack, int[] frmBuses, int[] toBuses, double[] x, boolean[] stat) {
		init(nbus, new int[] {slack}, frmBuses, toBuses, x);
		_stat = stat;
	}
	
	public SparseMatrixInverseXL(int nbus, int[] slack, int[] frmBuses, int[] toBuses, double[] x, boolean[] stat) {
		init(nbus, slack, frmBuses, toBuses, x);
		_stat = stat;
	}
	
	private void init(int nbus, int[] slack, int[] frmBuses, int[] toBuses, double[] x) {
		_nbus = nbus;
		_slack = slack;
		_frmBuses = frmBuses;
		_toBuses = toBuses;
		_x = x;
	}

	public void buildMatrices() {
		formNet();
		SparseBMatrixXL prepb = new SparseBMatrixXL(_net, _slack, _bbranch, _bself);
		_bp = prepb.factorize();
	}

	private void formNet() {
		_bself = new double[_nbus];
		_bbranch = new double[_frmBuses.length];
		LinkNetXL net = new LinkNetXL();
		net.ensureCapacity(_nbus-1, _frmBuses.length);
		for (int i=0; i<_frmBuses.length; i++) {
			if (_stat[i] == true) {
				int fbus = _frmBuses[i];
				int tbus = _toBuses[i];
				int brx = net.findBranch(fbus, tbus);
				if (brx == -1)
				{
					brx = net.addBranch(fbus, tbus);
				}
				
				double b = 1.0/_x[i];
				_bself[fbus] += b;
				_bself[tbus] += b;
				_bbranch[brx] -= b;
			}
		}
		_net = net;
	}

	public double[] solve(double[] rhs) {
		return _bp.solve(rhs);
	}
	
	
}
