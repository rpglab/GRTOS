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
 * Support Complex numbers in cartesian form.
 * 
 * @author chris@powerdata.com
 *
 */
public class ComplexList extends ComplexListBase<Complex>
{
	public float[] re() {return _v1;}
	public float[] im() {return _v2;}
	
	public float re(int ndx) {return _v1[ndx];}
	public float im(int ndx) {return _v2[ndx];}

	public ComplexList() {super();}
	
	public ComplexList(int descap, boolean setsize) {super(descap, setsize);}
	
	public ComplexList(Collection<Complex> collection)
	{
		int csize = collection.size();
		_v1 = new float[csize];
		_v2 = new float[csize];
		_size = csize;
		int i=0;
		for(Complex c : collection)
		{
			_v1[i] = c.re();
			_v2[i++] = c.im();
		}
	}

	public ComplexList(List<Float> re, List<Float> im)
	{
		int n = re.size();
		_v1 = new float[n];
		_v2 = new float[n];
		_size = n;
		for(int i=0; i < n; ++i)
		{
			_v1[i] = re.get(i);
			_v2[i] = im.get(i);
		}
	}
	
	public ComplexList(float[] re, float[] im)
	{
		_v1 = re.clone();
		_v2 = im.clone();
		_size = re.length;
	}

	@Override
	public void add(int index, Complex element)
	{
		_add(index, element.re(), element.im());
	}
	
	@Override
	public Complex remove(int index)
	{
		Complex rv = new Complex(_v1[index], _v2[index]);
		_remove(index);
		return rv;
	}

	@Override
	public Complex set(int index, Complex element)
	{
		Complex rv = new Complex(_v1[index], _v2[index]);
		_v1[index] = element.re();
		_v2[index] = element.im();
		return rv;
	}
	
	@Override
	public Complex get(int index)
	{
		return new Complex(_v1[index], _v2[index]);
	}
	
	/* Added by Xingpeng Li on 12/8/2018 */
	public void setIM(int ndx, float x) {_v2[ndx] = x;}
	
}
