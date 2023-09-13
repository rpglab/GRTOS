package com.powerdata.openpa.psse.powerflow;

/**
 * 

This class/code is from OpenPA version 1; the associated copyright is provided below:

Copyright (c) 2016, PowerData Corpration, Incremental Systems Corporation All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following 
conditions are met:

Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.

Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following 
disclaimer in the documentation and/or other materials provided with the distribution.

Neither the name of cmtest1 nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, 
BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO 
EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR 
TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
POSSIBILITY OF SUCH DAMAGE.

 *
 */

import java.util.AbstractList;
import java.util.Arrays;
/**
 * 
 * @author chris@powerdata.com
 *
 */
public class PowerFlowConvergenceList extends AbstractList<PowerFlowConvergence>
{
	int[] _worstpbus, _worstqbus, _niter;
	float[] _worstpmm, _worstqmm;
	boolean[] _conv;
	int _size;

	public PowerFlowConvergenceList(int size)
	{
		/* setup results arrays */
		_worstpbus = new int[size];
		_worstqbus = new int[size];
		_niter = new int[size];
		_worstpmm = new float[size];
		_worstqmm = new float[size];
		_conv = new boolean[size];
		_size = size;
		
		Arrays.fill(_worstpbus, -1);
		Arrays.fill(_worstqbus, -1);
	}
	
	@Override
	public PowerFlowConvergence get(int index)
	{
		return new PowerFlowConvergence(this, index);
	}

	@Override
	public int size()
	{
		return _size;
	}

	public void setWorstPbus(int ndx, int worstp) {_worstpbus[ndx] = worstp;}
	public void setWorstQbus(int ndx, int worstq) {_worstqbus[ndx] = worstq;}
	public void setWorstPmm(int ndx, float mm) {_worstpmm[ndx] = mm;}
	public void setWorstQmm(int ndx, float mm) {_worstqmm[ndx] = mm;}
	public void setConverged(int ndx, boolean conv) {_conv[ndx] = conv;}
	public boolean getConverged(int ndx) {return _conv[ndx];}
	public int getWorstPbus(int ndx) {return _worstpbus[ndx];} 
	public float getWorstPmm(int ndx) {return _worstpmm[ndx];}
	public int getWorstQbus(int ndx) {return _worstqbus[ndx];}
	public float getWorstQmm(int ndx) {return _worstqmm[ndx];}
	public int getIterationCount(int ndx) {return _niter[ndx];}
	public void setIterationCount(int ndx, int itercnt) {_niter[ndx] = itercnt;}
}