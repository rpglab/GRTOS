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

import java.util.ArrayList;

import com.powerdata.openpa.psse.util.TransformerRaw;
import com.powerdata.openpa.tools.Complex;
import com.powerdata.openpa.tools.ComplexList;

public class TransformerPrep
{
	ArrayList<Integer>	xf	= new ArrayList<>(), wndx = new ArrayList<>();
	ArrayList<String>	bus1	= new ArrayList<>(), bus2 = new ArrayList<>();

	ArrayList<Float> zr = new ArrayList<>(), zx = new ArrayList<>();
			
	public void prep(TransformerRaw xf, int wndx, String bus1, String bus2, Complex z)
	{
		this.xf.add(xf.getIndex());
		this.wndx.add(wndx);
		this.bus1.add(bus1);
		this.bus2.add(bus2);
		zr.add(z.re());
		zx.add(z.im());
	}
	
	public int size() {return xf.size();}
	public int[] getXfRaw() {return makeIntArray(xf);}
	public int[] getWndx() {return makeIntArray(wndx);}
	public String[] getBusI() {return makeStringArray(bus1);}
	public String[] getBusJ() {return makeStringArray(bus2);}
	public ComplexList getZ() {return new ComplexList(zr, zx);}
	
	int[] makeIntArray(ArrayList<Integer> list)
	{
		int n = list.size();
		int[] rv = new int[n];
		for(int i=0; i < n; ++i)
			rv[i] = list.get(i);
		return rv;
	}
	String[] makeStringArray(ArrayList<String> list)
	{
		int n = list.size();
		String[] rv = new String[n];
		for(int i=0; i < n; ++i)
			rv[i] = list.get(i);
		return rv;
	}

}
