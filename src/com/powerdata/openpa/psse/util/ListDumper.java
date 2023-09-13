package com.powerdata.openpa.psse.util;

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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Iterator;

import com.powerdata.openpa.psse.PsseModel;
import com.powerdata.openpa.tools.BaseList;

public class ListDumper
{
	static final char Dlm = ',';
	public void dump(PsseModel model, File outdir) throws IOException,
			ReflectiveOperationException, RuntimeException
	{
		if (!outdir.exists()) outdir.mkdirs();
		Method[] methods = model.getClass().getMethods();
		for (Method m : methods)
		{
			Class<?> rtype = m.getReturnType();
			if (rtype.getPackage() != null && rtype.getPackage().getName()
					.equals("com.powerdata.openpa.psse"))
			{
				while (rtype != null && rtype != Object.class
						&& rtype != void.class)
				{
					if (rtype == BaseList.class
							&& m.getParameterTypes().length == 0)
					{
						String nm = m.getName();
						String title = nm.substring(3);
						File nfile = new File(outdir, title + ".csv");
						BaseList<?> list = (BaseList<?>) m.invoke(model,
								new Object[] {});
						dumpList(nfile, list);
					}
					rtype = rtype.getSuperclass();
				}
			}
		}
	}

	void dumpList(File nfile, BaseList<?> list) throws IOException,
			ReflectiveOperationException, IllegalArgumentException
	{
		Method[] methods = list.getClass().getMethods();
		ArrayList<Method> ometh = new ArrayList<>();
		ArrayList<String> mname = new ArrayList<>();
		for (Method m : methods)
		{
			Class<?> mclass = m.getReturnType();
			boolean isiterator = false;
			for(Class<?> i : mclass.getInterfaces()) {if (i == Iterator.class) {isiterator=true;break;}}
			Class<?> mcsuper = mclass.getSuperclass();
			while (mcsuper != null && mcsuper != Object.class)
			{
				mclass = mcsuper;
				mcsuper = mclass.getSuperclass();
			}
			if (mclass != AbstractCollection.class && !mclass.isArray()
					&& mclass != void.class && mclass != Object.class && !isiterator)
			{
				Class<?>[] ptype = m.getParameterTypes();
				if (ptype.length == 1 && ptype[0] == int.class)
				{

					String nm = m.getName();
					boolean yget = nm.startsWith("get");
					boolean yis = nm.startsWith("is");
					ometh.add(m);
					mname.add(nm.equals("get") ? "toString()" : nm
							.substring(yget ? 3 : (yis ? 2 : 0)));
				}
			}
		}
		int n = list.size();
		if (!ometh.isEmpty() && n > 0)
		{
			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(
					nfile)));
			pw.print(mname.get(0));
			for (int i = 1; i < mname.size(); ++i)
			{
				pw.print(Dlm);
				pw.print(mname.get(i));
			}
			pw.println();

			/* output data for each row */
			for (int i = 0; i < n; ++i)
			{
				for (int j = 0; j < ometh.size(); ++j)
				{
					/* output cell */
					if (j>0) pw.print(Dlm);
					Object v = ometh.get(j).invoke(list, i);
					boolean isstr = !Number.class.isInstance(v);
					if (isstr) pw.print('\'');
					String vs = v == null ? null : v.toString();
					pw.print((vs==null)?"<null>":vs);
					if (isstr) pw.print('\'');
				}
				pw.println();
			}
			pw.close();
		}
	}
	
	public static void main(String[] args) throws Exception
	{
		String uri = null;
		File outdir = new File(System.getProperty("user.dir"));
		for(int i=0; i < args.length;)
		{
			String s = args[i++].toLowerCase();
			int ssx = 1;
			if (s.startsWith("--")) ++ssx;
			switch(s.substring(ssx))
			{
				case "uri":
					uri = args[i++];
					break;
				case "outdir":
					outdir = new File(args[i++]);
					break;
			}
		}
		if (uri == null)
		{
			System.err.format("Usage: -uri model_uri "
					+ "[ --outdir output_directory (deft to $CWD ]\n");
			System.exit(1);
		}
		
		new ListDumper().dump(PsseModel.Open(uri), outdir);
	}
}
