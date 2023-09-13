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

import com.powerdata.openpa.psse.PsseModelException;
import com.powerdata.openpa.tools.AbstractBaseObject;

public class TwoTermDCLineResult extends AbstractBaseObject
{
	TwoTermDCLineResultList _rlist;
	
	public TwoTermDCLineResult(TwoTermDCLineResultList list, int ndx)
	{
		super(list, ndx);
		_rlist = list;
	}
	
	public float getTapR() throws PsseModelException {return _rlist.getTapR(_ndx);}
	public float getTapI() throws PsseModelException {return _rlist.getTapI(_ndx);}
	public float getAlpha() throws PsseModelException {return _rlist.getAlpha(_ndx);}
	public float getGamma() throws PsseModelException {return _rlist.getGamma(_ndx);}
	public float getMWR() throws PsseModelException {return _rlist.getMWR(_ndx);}
	public float getMWI() throws PsseModelException {return _rlist.getMWI(_ndx);}
	public float getMVArR() throws PsseModelException {return _rlist.getMVArR(_ndx);}
	public float getMVArI() throws PsseModelException {return _rlist.getMVArI(_ndx);}

	@Override
	public String toString()
	{
		String v = "<err>";
		try
		{
			v =  String.format("%s: alpha=%f, rtap=%f, rmw=%f, rmvar=%f, gamma=%f, itap=%f, imw=%f, imvar=%f\n",
					getObjectName(), getAlpha(), getTapR(), getMWR(), getMVArR(),
					getGamma(), getTapI(), getMWI(), getMVArI());
		} catch (PsseModelException e)
		{
			e.printStackTrace();
		}
		return v;
	}
	
	
}
