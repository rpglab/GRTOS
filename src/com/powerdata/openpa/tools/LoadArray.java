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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class LoadArray
{
	public static int[] Int(SimpleCSV csv, String prop, Object def, String fn)
			throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
	{
		int size = csv.getRowCount();
		int array[] = new int[size];

		Method getDef = def.getClass().getMethod(fn, int.class);
		String vals[] = csv.get(prop);
		// if no vals were provided use all defaults
		if (vals == null)
		{
			for(int i=0; i<size; i++)
			{
				array[i] = (int)getDef.invoke(def, i);
			}
		}
		else
		{
			for(int i=0; i<size; i++)
			{
				String v = vals[i];
				array[i] = (v != null && SimpleCSV.IsNumber(v))?
					Integer.parseInt(v):(int)getDef.invoke(def, i);
			}
		}
		return array;
	}
	public static float[] Float(SimpleCSV csv, String prop, Object def, String fn)
			throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
	{
		int size = csv.getRowCount();
		float array[] = new float[size];

		Method getDef = def.getClass().getMethod(fn, int.class);
		String vals[] = csv.get(prop);

		// if no vals were provided use all defaults
		if (vals == null)
		{
			for(int i=0; i<size; i++)
			{
				array[i] = (float)getDef.invoke(def, i);
			}
		}
		else
		{
			for(int i=0; i<size; i++)
			{
				String v = vals[i];
				array[i] = (v != null && SimpleCSV.IsNumber(v))?
					Float.parseFloat(v):(float)getDef.invoke(def, i);
			}
		}
		return array;
	}
	public static String[] String(SimpleCSV csv, String prop, Object def, String fn)
		throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
	{
		int size = csv.getRowCount();
		String array[] = new String[size];


		
		Method getDef = def.getClass().getMethod(fn, int.class);
		String vals[] = csv.get(prop);
		
		// if no vals were provided use all defaults
		if (vals == null)
		{
			for(int i=0; i<size; i++) array[i] = (String)getDef.invoke(def, i);
		}
		else
		{
			for(int i=0; i<size; i++)
			{
				String v = vals[i];
				array[i] = (v != null && v.length() > 0)?v:(String)getDef.invoke(def, i);
			}
		}
		return array;
	}
}
