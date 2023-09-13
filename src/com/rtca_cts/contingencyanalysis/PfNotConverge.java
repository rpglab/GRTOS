package com.rtca_cts.contingencyanalysis;

import java.util.Arrays;

/**
 * 
 * Initialized in Feb. 2014.
 * 
 * @author Xingpeng.Li (xplipower@gmail.com)
 * 
 */
public class PfNotConverge
{
	
	int[] _ndx;  // store the contingency branch index, which the power flow program can not converge under that contingency 
	int _numNotConv;  // the number of contingencies that the power flow can not converge

	
	public PfNotConverge(int num)
	{
		_ndx = new int[num];
		Arrays.fill(_ndx,-1);
	}
		
	public void updateData(int iContin)
	{
		_ndx[_numNotConv++] = iContin;
	}
	
	
	public int getNumNotConv() {return _numNotConv;}
	public int[] getAllContinNotConv() 
	{
		int[] ndx = new int[_numNotConv];
		System.arraycopy(_ndx, 0, ndx, 0, _numNotConv);
		return ndx;
	}
	
	
}
