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



/**
 * 
 * @author chris@powerdata.com
 *
 */
public class PowerFlowConvergence
{
	int _ndx;
	PowerFlowConvergenceList _list;
	
	public PowerFlowConvergence(PowerFlowConvergenceList list, int ndx)
	{
		_ndx = ndx;
		_list = list;
	}

	public void setWorstPbus(int worstp) {_list.setWorstPbus(_ndx, worstp);}
	public void setWorstQbus(int worstq) {_list.setWorstQbus(_ndx, worstq);}
	public void setWorstPmm(float mm) {_list.setWorstPmm(_ndx, mm);}
	public void setWorstQmm(float mm) {_list.setWorstQmm(_ndx, mm);}
	public void setConverged(boolean conv) {_list.setConverged(_ndx, conv);}
	public void setIterationCount(int itercnt) {_list.setIterationCount(_ndx, itercnt);}
	public int getIslandNdx() {return _ndx;}
	public boolean getConverged() {return _list.getConverged(_ndx);}

	public int getWorstPbus() {return _list.getWorstPbus(_ndx);}
	public float getWorstPmm() {return _list.getWorstPmm(_ndx);}
	public int getWorstQbus() {return _list.getWorstQbus(_ndx);}
	public float getWorstQmm() {return _list.getWorstQmm(_ndx);}
	public int getIterationCount() {return _list.getIterationCount(_ndx);}
}
