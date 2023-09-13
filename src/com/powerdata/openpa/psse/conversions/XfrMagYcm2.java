package com.powerdata.openpa.psse.conversions;

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

import com.powerdata.openpa.psse.PhaseShifter;
import com.powerdata.openpa.psse.PsseModelException;
import com.powerdata.openpa.psse.Transformer;
import com.powerdata.openpa.tools.Complex;

public class XfrMagYcm2 extends XfrMagYTool
{

	@Override
	protected Complex _getYMag(Transformer xf) throws PsseModelException
	{
		return cvt(xf.getFromBus().getBASKV(), xf.getNOMV1(), xf.getMAG1(), xf.getMAG2(), xf.getSBASE1_2());
	}
	@Override
	protected Complex _getYMag(PhaseShifter xf) throws PsseModelException
	{
		return cvt(xf.getFromBus().getBASKV(), xf.getNOMV1(), xf.getMAG1(), xf.getMAG2(), xf.getSBASE1_2());
	}
	
	protected Complex cvt(float baskv, float nomv1, float mag1, float mag2, float sbase)
	{
		float vratio = baskv / nomv1; 
		float vrsq = vratio * vratio;
		float ghe = mag1 / 1e+08F * vrsq;
		float ymabs = mag2 * (sbase / 100F) * vrsq;
		return new Complex(ghe, (float) Math.sqrt(ymabs * ymabs - ghe * ghe));
	}
}
