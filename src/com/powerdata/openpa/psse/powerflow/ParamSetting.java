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

public class ParamSetting {

	/** active power convergence tolerance */
	static float _ptol = 0.01f;
	/** reactive power convergence tolerance */
	static float _qtol = 0.01f;
	/** maximum iterations before giving up */
	static int _itermax = 20;

	float _qtoltap = .05f;

	
	float minxmag = 0.00001f;
	String svstart = "Flat";

	public VoltageSource getInitType() {return VoltageSource.fromConfig(svstart);}
	
	public static void setParam(FastDecoupledPowerFlow pf)
	{
		pf.setPtol(_ptol);
		pf.setQtol(_qtol);
		pf.setMaxIter(_itermax);
	}
	
	public void setPtol(float ptol) {_ptol = ptol;}
	public void setQtol(float qtol) {_qtol = qtol;}
	public void setIterMax(int itermax) {_itermax = itermax;}
	
	public int getIterMax() {return _itermax;}
	
	public float getMinxmag() {return minxmag;}
		
}
