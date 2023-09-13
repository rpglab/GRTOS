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

import java.util.AbstractList;
import java.util.Arrays;

/**
 * shared implementation between both forms of complex numbers 
 * 
 * @author chris@powerdata.com
 *
 * @param <T>
 */
public abstract class ComplexListBase<T> extends AbstractList<T>
{
	protected static final int DeftCap = 16; 
	
	protected int _size;
	protected float[] _v1;
	protected float[] _v2;

	public ComplexListBase()
	{
		_v1 = new float[DeftCap];
		_v2 = new float[DeftCap];
		_size = 0;
	}
	
	public ComplexListBase(int descap, boolean setsize)
	{
		_v1 = new float[descap];
		_v2 = new float[descap];
		_size = setsize ? descap : 0;
	}
	
	public void ensureCapacity(int descap)
	{
		int ccap = _v1.length;
		if (ccap < descap)
		{
			_v1 = Arrays.copyOf(_v1, descap);
			_v2 = Arrays.copyOf(_v2, descap);
		}
	}
	
	protected void _add(int index, float v1, float v2)
	{
		ensureCapacity(_size+1);
		if (index < _size)
		{
			int cnt = _size-index;
			System.arraycopy(_v1, index, _v1, index+1, cnt);
			System.arraycopy(_v2, index, _v2, index+1, cnt);
		}
		_v1[index] = v1;
		_v2[index] = v2;
		++_size;
	}

	
	protected void _remove(int index)
	{
		int cnt = _size-index-1;
		System.arraycopy(_v1, index+1, _v1, index, cnt);
		System.arraycopy(_v2, index+1, _v2, index, cnt);
		--_size;
	}
	
	@Override
	public int size() {return _size;}

	@Override
	public String toString()
	{
		int n = (_v1 == null) ? 0 : _v1.length;
		String rv = null;
		if (n > 0)
		{
			StringBuilder rvb = new StringBuilder(10*n);
			rvb.append('[');
			for(int i=0; i < n; ++i)
			{
				if (i > 0) rvb.append(", ");
				rvb.append('(');
				rvb.append(_v1[i]);
				rvb.append(',');
				rvb.append(_v2[i]);
				rvb.append(')');
			}
			rvb.append(']');
			rv = rvb.toString();
		}
		return rv;
	}

	public void setSize(int newsize)
	{
		_size = newsize;
	}
	
	
}
