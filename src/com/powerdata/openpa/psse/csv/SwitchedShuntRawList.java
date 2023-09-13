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

import java.io.File;
import java.io.IOException;

import com.powerdata.openpa.psse.PsseModelException;
import com.powerdata.openpa.tools.LoadArray;
import com.powerdata.openpa.tools.SimpleCSV;

public class SwitchedShuntRawList extends com.powerdata.openpa.psse.SwitchedShuntList
{
	/** parent container */
	PsseRawModel _eq;
	/** number of items in the DB */
	int _size;
	/** object IDs (really just the bus number) */
	String _ids[];
	
	// Base values from the CSV file
	String[] _i, _swrem, _rmidnt;
	int[] _modsw, _stat;
	float[] _vswhi, _vswlo, _rmpct, _binit;

	int[][] _n;
	float[][] _b;

	public SwitchedShuntRawList(PsseRawModel eq) throws PsseModelException
	{
		super(eq);
		_eq = eq;
		File dbfile = new File(_eq.getDir(), "SwitchedShunt.csv");
		try
		{
			SimpleCSV swsh = new SimpleCSV(dbfile);
			_size = swsh.getRowCount();
			_i = swsh.get("I");
			_modsw = LoadArray.Int(swsh,"MODSW", this, "getDeftMODSW");
			_vswhi = LoadArray.Float(swsh, "VSWHI", this, "getDeftVSWHI");
			_vswlo = LoadArray.Float(swsh, "VSWLO", this, "getDeftVSWLO");
			_swrem = LoadArray.String(swsh, "SWREM", this, "getDeftSWREM");
			_rmpct = LoadArray.Float(swsh, "RMPCT", this, "getDeftRMPCT");
			_rmidnt = LoadArray.String(swsh, "RMIDNT", this, "getDeftRMIDNT");
			_binit = LoadArray.Float(swsh, "BINIT", this, "getDeftBINIT");
			_stat = LoadArray.Int(swsh, "STAT", this, "getDeftSTAT");
			
			_n = new int[_size][8];
			_b = new float[_size][8];
			
			for (int i=0, j=1; i < 8; ++i, ++j)
			{
				int[] tn = LoadArray.Int(swsh, "N"+j, this, "getDeftN");
				float[] tb = LoadArray.Float(swsh, "B"+j, this, "getDeftB");
				for(int k=0; k < _size; ++k)
				{
					_n[k][i] = tn[k];
					_b[k][i] = tb[k];
				}
			}

		} catch (IOException | ReflectiveOperationException | RuntimeException e)
		{
			throw new PsseModelException(e);
		}
	}

	@Override
	public String getI(int ndx) throws PsseModelException {return _i[ndx];}
	@Override
	public String getObjectID(int ndx) throws PsseModelException {return _i[ndx];}
	@Override
	public int size() {return _size;}
	@Override
	public int getMODSW(int ndx) throws PsseModelException {return _modsw[ndx];}
	@Override
	public float getVSWHI(int ndx) throws PsseModelException {return _vswhi[ndx];}
	@Override
	public float getVSWLO(int ndx) throws PsseModelException {return _vswlo[ndx];}
	@Override
	public String getSWREM(int ndx) throws PsseModelException {return _swrem[ndx];}
	@Override
	public float getRMPCT(int ndx) throws PsseModelException {return _rmpct[ndx];}
	@Override
	public String getRMIDNT(int ndx) throws PsseModelException {return _rmidnt[ndx];}
	@Override
	public float getBINIT(int ndx) throws PsseModelException {return _binit[ndx];}

	
	public int getDeftMODSW(int ndx) throws PsseModelException {return super.getMODSW(ndx);}
	public float getDeftVSWHI(int ndx) throws PsseModelException {return super.getVSWHI(ndx);}
	public float getDeftVSWLO(int ndx) throws PsseModelException {return super.getVSWLO(ndx);}
	public String getDeftSWREM(int ndx) throws PsseModelException {return super.getSWREM(ndx);}
	public float getDeftRMPCT(int ndx) throws PsseModelException {return super.getRMPCT(ndx);}
	public String getDeftRMIDNT(int ndx) throws PsseModelException {return super.getRMIDNT(ndx);}
	public float getDeftBINIT(int ndx) throws PsseModelException {return super.getBINIT(ndx);}
	public int getDeftSTAT(int ndx) throws PsseModelException {return super.isInSvc(ndx)?1:0;}
	
	public int getDeftN(int ndx) throws PsseModelException {return 0;}
	public float getDeftB(int ndx) throws PsseModelException {return 0f;}
	
	public int[] getN(int ndx) {return _n[ndx];}
	public float[] getB(int ndx) {return _b[ndx];}

	@Override
	public boolean isInSvc(int ndx) throws PsseModelException {return _stat[ndx] == 1;}
	@Override
	public void setInSvc(int ndx, boolean state) throws PsseModelException
	{
		_stat[ndx] = state ? 1 : 0;
	}
	
	
}
