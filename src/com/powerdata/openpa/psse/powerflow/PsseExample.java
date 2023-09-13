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

import java.io.File;
import java.io.PrintWriter;

import com.powerdata.openpa.psse.BusList;
import com.powerdata.openpa.psse.Line;
import com.powerdata.openpa.psse.LineList;
import com.powerdata.openpa.psse.PsseModel;
import com.powerdata.openpa.psse.PsseModelException;
import com.powerdata.openpa.psse.util.MinZMagFilter;
import com.powerdata.openpa.tools.PAMath;

/**
 * This example shows how to load in a PSS/e file, and calculate branch flows for NontransformerBranch(Line)
 * 
 * This application takes a single parameter, a path to a psse raw file
 * 
 * @author chris@powerdata.com
 * 
 */
public class PsseExample
{
	PsseModel _model;
	float[] _va, _vm;
	
	public PsseExample(PsseModel model) throws PsseModelException
	{
		_model = model;
		
		/*
		 * the PowerCalculator class methods prefer arrays for bus voltage angle and magnitude
		 */
		BusList buses = _model.getBuses();
		int nbus = buses.size();
		_va = new float[nbus];
		_vm = new float[nbus];
		for(int i=0; i < nbus; ++i)
		{
			/* get angle in radians */
			_va[i] = buses.getVArad(i);
			/* get magnitude per-unit on bus KV */
			_vm[i] = buses.getVMpu(i);
		}
	}
	
	public void calculateLineFlows(PrintWriter out) throws PsseModelException
	{
		/*
		 * PowerCalculator contains utilities to calculate line flows
		 */
		PowerCalculator pcalc = new PowerCalculator(_model);
		
		/*
		 * calculate flows for just nontransformer branches, any list of objects
		 * maintaining the ACBranch interface will work.  
		 */
		LineList lines = _model.getLines();
		float[][] results = pcalc.calcACBranchFlows(lines, _va, _vm);
		
		/*
		 * Another example of the same thing, but filtering and manipulating small X values
		 */
		float[][] results2 = pcalc.calcACBranchFlows(lines, _va, _vm, new MinZMagFilter(lines, 0.001f));

		/*
		 * Results come back as an array of arrays, from-side active power,
		 * from-side reactive power, to-side active power, and to-side reactive
		 * power
		 */
		float[] pfrom = results[0];
		float[] qfrom = results[1];
		float[] pto = results[2];
		float[] qto = results[3];
		
		/*
		 * report results
		 */
		out.println("Line                          FromMW  FromMVAr  ToMW  ToMVAr");
		for (Line l : lines)
		{
			int i = l.getIndex();
			out.format("%-30s %6.2f %6.2f %6.2f %6.2f\n", 
					l.getObjectName(), PAMath.pu2mw(pfrom[i]),
					PAMath.pu2mvar(qfrom[i]),
					PAMath.pu2mw(pto[i]),
					PAMath.pu2mvar(qto[i]));
		}
		out.flush();
	}

	public static void main(String[] args) throws Exception
	{
		String fname = args[0];
		File praw = new File(fname);
		if (!praw.exists())
		{
			System.err.format("No raw file found at: %s", fname);
			System.exit(1);
		}

		/*
		 * Open an instance of PsseModel that reads a PSS/e raw file. For a more
		 * advanced example that allows other sources of data besides just raw
		 * files, see FastDecoupledPowerFlow.main()
		 */
		PsseModel model = PsseModel.Open(String.format("psseraw:file=%s", fname));

		PsseExample example = new PsseExample(model);
		PrintWriter out = new PrintWriter(System.out);
		
		/*
		 * Calculate and display branch flows on Nontransformer Branches
		 */
		example.calculateLineFlows(out);
	}
}

