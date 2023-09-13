package com.powerdata.openpa.tools;

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

import java.util.Collection;
import java.util.List;

/**
 * 
 * List of complex numbers in polar form
 * 
 * @author chris@powerdata.com
 *
 */
public class PComplexList extends ComplexListBase<PComplex>
{
	public float[] r() {return _v1;}
	public float[] theta() {return _v2;}
	public float r(int ndx) {return _v1[ndx];}
	public float theta(int ndx) {return _v2[ndx];}


	public PComplexList() {super();}

	public PComplexList(int descap, boolean setsize) {super(descap, setsize);}

	
	public PComplexList(Collection<PComplex> collection)
	{
		int csize = collection.size();
		_v1 = new float[csize];
		_v2 = new float[csize];
		_size = csize;
		int i=0;
		for(PComplex c : collection)
		{
			_v1[i] = c.r();
			_v2[i++] = c.theta();
		}
	}

	public PComplexList(List<Float> r, List<Float> theta)
	{
		int n = r.size();
		_v1 = new float[n];
		_v2 = new float[n];
		_size = n;
		for(int i=0; i < n; ++i)
		{
			_v1[i] = r.get(i);
			_v2[i] = theta.get(i);
		}
	}
	public PComplexList(float[] r, float[] theta)
	{
		_v1 = r.clone();
		_v2 = theta.clone();
		_size = r.length;
	}
	@Override
	public void add(int index, PComplex element)
	{
		_add(index, element.r(), element.theta());
	}
	
	@Override
	public PComplex remove(int index)
	{
		PComplex rv = new PComplex(_v1[index], _v2[index]);
		_remove(index);
		return rv;
	}

	@Override
	public PComplex set(int index, PComplex element)
	{
		PComplex rv = new PComplex(_v1[index], _v2[index]);
		_v1[index] = element.r();
		_v2[index] = element.theta();
		return rv;
	}
	
	@Override
	public PComplex get(int index)
	{
		return new PComplex(_v1[index], _v2[index]);
	}

}

