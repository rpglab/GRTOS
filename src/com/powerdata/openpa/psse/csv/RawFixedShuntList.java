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
import com.powerdata.openpa.psse.ShuntList;
import com.powerdata.openpa.tools.LoadArray;
import com.powerdata.openpa.tools.SimpleCSV;

public class RawFixedShuntList extends ShuntList
{
	String[] _i, _id;
	float[] _g, _b;
	int _size;
	int[] _stat;
	
	public RawFixedShuntList(PsseRawModel model) throws PsseModelException
	{
		super(model);
		File dbfile = new File(model.getDir(), "FixedShunt.csv");
		try
		{
			SimpleCSV shunts = new SimpleCSV(dbfile);
			_size = shunts.getRowCount();
			_i = shunts.get("I");
			_id = LoadArray.String(shunts, "ID", this, "getDeftID");
			_g = LoadArray.Float(shunts, "G", this, "getDeftG");
			_b = LoadArray.Float(shunts, "B", this, "getDeftB");
			_stat = LoadArray.Int(shunts, "STAT", this, "getDeftSTAT");

		} catch (IOException | ReflectiveOperationException e)
		{
			throw new PsseModelException(e);
		}
	}
	
	public String getDeftID(int ndx) throws PsseModelException {return super.getID(ndx);}
	public float getDeftG(int ndx) throws PsseModelException {return super.getG(ndx);}
	public float getDeftB(int ndx) throws PsseModelException {return super.getB(ndx);}
	public float getDeftSTAT(int ndx) throws PsseModelException {return super.isInSvc(ndx)?1:0;}
	
	@Override
	public String getI(int ndx) throws PsseModelException {return _i[ndx];}
	@Override
	public String getObjectID(int ndx) throws PsseModelException
	{
		String rv = String.format("%s-FXDSH", getBus(ndx).getObjectID());
		String id = getID(ndx);
		if (!id.isEmpty())
			rv = String.format("%s-%s", rv, id);
		return rv;
	}

	
	@Override
	public String getObjectName(int ndx) throws PsseModelException
	{
		String rv = getBus(ndx).getObjectName();
		String id = getID(ndx);
		if (!id.isEmpty())
			rv = String.format("%s-%s", rv, id);
		return rv;
	}

	@Override
	public int size() {return _size;}

	@Override
	public float getB(int ndx) throws PsseModelException {return _b[ndx];}
	@Override
	public float getG(int ndx) throws PsseModelException {return _g[ndx];}
	@Override
	public String getID(int ndx) throws PsseModelException {return _id[ndx];}

	@Override
	public boolean isInSvc(int ndx) throws PsseModelException {return _stat[ndx] == 1;}
	@Override
	public void setInSvc(int ndx, boolean state) throws PsseModelException {_stat[ndx] = state ? 1 : 0;}
}
