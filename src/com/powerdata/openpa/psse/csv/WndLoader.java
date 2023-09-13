package com.powerdata.openpa.psse.csv;

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

import java.lang.reflect.Array;
import java.lang.reflect.Method;

import com.powerdata.openpa.psse.PsseModelException;

class WndLoader
{
	static final Class<Transformer3RawList> _class = Transformer3RawList.class;
	
	Method[] _methods;
	public WndLoader(String prop) throws PsseModelException
	{
		try
		{
			String pn = "get"+prop;
			_methods = new Method[] {null,
					_class.getMethod(pn+"1", int.class),
					_class.getMethod(pn+"2", int.class),
					_class.getMethod(pn+"3", int.class)};
		} catch (ReflectiveOperationException | SecurityException e)
		{
			throw new PsseModelException(e);
		}
	}
	
	public Object load(Transformer3RawList rlist, int[] ndx,
			int[] wnd, Class<?> type) throws PsseModelException
	{
		int n = ndx.length;
		Object rv = Array.newInstance(type, n);
		try
		{
			for (int i = 0; i < n; ++i)
			{
				Array.set(rv, i, _methods[wnd[i]].invoke(rlist, ndx[i]));

			}
		} catch (ArrayIndexOutOfBoundsException | ReflectiveOperationException e)
		{
			throw new PsseModelException(e);
		}
		return rv;
	}
}