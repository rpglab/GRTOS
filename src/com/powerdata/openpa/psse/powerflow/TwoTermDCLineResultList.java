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
import com.powerdata.openpa.psse.TwoTermDCLineList;
import com.powerdata.openpa.tools.BaseList;

public class TwoTermDCLineResultList extends BaseList<TwoTermDCLineResult>
{
	protected TwoTermDCLineList _lines;
	protected float[] _rtap, _itap, _alpha, _gamma, _mwr, _mvarr, _mwi, _mvari;
	
	public TwoTermDCLineResultList(TwoTermDCLineList lines, float[] rtap,
			float[] itap, float[] alpha, float[] gamma, float[] mwr,
			float[] mvarr, float[] mwi, float[] mvari)
	{
		_lines = lines;
		_rtap = rtap;
		_itap = itap;
		_alpha = alpha;
		_gamma = gamma;
		_mwr = mwr;
		_mvarr = mvarr;
		_mwi = mwi;
		_mvari = mvari;
	}

	@Override
	public String getObjectID(int ndx) throws PsseModelException
	{
		return _lines.getObjectID(ndx);
	}

	@Override
	public TwoTermDCLineResult get(int index)
	{
		return new TwoTermDCLineResult(this, index);
	}
	

	@Override
	public int size()
	{
		return _lines.size();
	}

	public float getTapR(int ndx) throws PsseModelException {return _rtap[ndx];}
	public float getTapI(int ndx) throws PsseModelException {return _itap[ndx];}
	public float getAlpha(int ndx) throws PsseModelException {return _alpha[ndx];}
	public float getGamma(int ndx) throws PsseModelException {return _gamma[ndx];}

	public float getMWR(int ndx) throws PsseModelException {return _mwr[ndx];}
	public float getMWI(int ndx) throws PsseModelException {return _mwi[ndx];}
	public float getMVArR(int ndx) throws PsseModelException {return _mvarr[ndx];}
	public float getMVArI(int ndx) throws PsseModelException {return _mvari[ndx];}
}
