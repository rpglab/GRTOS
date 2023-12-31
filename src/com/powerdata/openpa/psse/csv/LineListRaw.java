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

import com.powerdata.openpa.psse.Bus;
import com.powerdata.openpa.psse.BusList;
import com.powerdata.openpa.psse.LineList;
import com.powerdata.openpa.psse.LineMeterEnd;
import com.powerdata.openpa.psse.PsseModelException;
import com.powerdata.openpa.tools.LoadArray;
import com.powerdata.openpa.tools.SimpleCSV;

public class LineListRaw extends LineList
{
	PsseRawModel _eq;
	BusList _buses;
	protected String _i[],_j[],_ckt[];
	protected float _r[],_x[],_b[],_ratea[],_rateb[],_ratec[],_gi[],_bi[],_gj[],_bj[];
	protected int _st[];
	protected float _len[];
	protected int _size;
	
	public LineListRaw(PsseRawModel model) throws PsseModelException
	{
		super(model);
		_eq = model;
		try
		{
			File dbfile = new File(model.getDir(), "NontransformerBranch.csv");
			_buses = model.getBuses();
			SimpleCSV branches = new SimpleCSV(dbfile);
			_size	= branches.getRowCount();
			_i		= branches.get("I");
			_j		= branches.get("J");
			_ckt	= LoadArray.String(branches,"CKT",this,"getDeftCKT");
			_r		= LoadArray.Float(branches,"R",this,"getDeftR");
			_x		= branches.getFloats("X");
			_b		= LoadArray.Float(branches,"B",this,"getDeftB");
			_ratea	= LoadArray.Float(branches,"RATEA",this,"getDeftRATEA");
			_rateb	= LoadArray.Float(branches,"RATEB",this,"getDeftRATEB");
			_ratec	= LoadArray.Float(branches,"RATEC",this,"getDeftRATEC");
			_gi		= LoadArray.Float(branches,"GI",this,"getDeftGI");
			_bi		= LoadArray.Float(branches,"BI",this,"getDeftBI");
			_gj		= LoadArray.Float(branches,"GJ",this,"getDeftGJ");
			_bj		= LoadArray.Float(branches,"BJ",this,"getDeftBJ");
			_st		= LoadArray.Int(branches,"ST",this,"getDeftST");
			_len	= LoadArray.Float(branches,"LEN",this,"getDeftLEN");
			reindex();
			
		}
		catch(Exception e)
		{
			throw new PsseModelException(getClass().getName()+": "+e, e);
		}
	}
	
	@Override
	public int size() { return _size; }

	
	@Override
	public Bus getFromBus(int ndx) throws PsseModelException
	{
		return _buses.get(getI(ndx));
	}

	@Override
	public Bus getToBus(int ndx) throws PsseModelException
	{
		String j = getJ(ndx);
		if (j.charAt(0)=='-') j = j.substring(1);
		return _buses.get(j);
	}

	@Override
	public String getI(int ndx) { return _i[ndx]; }
	@Override
	public String getJ(int ndx)
	{
		String j = _j[ndx];
		if (j.charAt(0) == '-') j = j.substring(1);
		return j;
	}
	
	@Override
	public LineMeterEnd getMeteredEnd(int ndx) throws PsseModelException
	{
		return (_j[ndx].charAt(0)=='-')?LineMeterEnd.To : LineMeterEnd.From;
	}

	@Override
	public String getCKT(int ndx) { return _ckt[ndx]; }
	@Override
	public float getR(int ndx) { return _r[ndx]; }
	@Override
	public float getX(int ndx) { return _x[ndx]; }
	@Override
	public float getB(int ndx) { return _b[ndx]; }
	@Override
	public float getRATEA(int ndx) { return _ratea[ndx]; }
	@Override
	public float getRATEB(int ndx) { return _rateb[ndx]; }
	@Override
	public float getRATEC(int ndx) { return _ratec[ndx]; }
	@Override
	public float getGI(int ndx) { return _gi[ndx]; }
	@Override
	public float getBI(int ndx) { return _bi[ndx]; }
	@Override
	public float getGJ(int ndx) { return _gj[ndx]; }
	@Override
	public float getBJ(int ndx) { return _bj[ndx]; }
	@Override
	public int getST(int ndx) { return _st[ndx]; }
	@Override
	public float getLEN(int ndx) { return _len[ndx]; }
	@Override
	public String getObjectID(int ndx)
	{
		String j = _j[ndx];
		if (j.startsWith("-")) j = j.substring(1);
		return "LN-" + _i[ndx] + ":" + j + ":" + _ckt[ndx];
	}

	@Override
	public void setInSvc(int ndx, boolean state) throws PsseModelException
	{
		_st[ndx] = state ? 1 : 0;
		_eq.resetTP();
	}

	@Override
	public boolean isInSvc(int ndx) throws PsseModelException
	{
		return _st[ndx] == 1;
	}

	public String getDeftCKT(int ndx) throws PsseModelException {return super.getCKT(ndx);}
	public float getDeftR(int ndx) throws PsseModelException {return super.getR(ndx);}
	public float getDeftB(int ndx) throws PsseModelException {return super.getB(ndx);}
	public float getDeftRATEA(int ndx) throws PsseModelException {return super.getRATEA(ndx);}
	public float getDeftRATEB(int ndx) throws PsseModelException {return super.getRATEB(ndx);}
	public float getDeftRATEC(int ndx) throws PsseModelException {return super.getRATEC(ndx);}
	public float getDeftGI(int ndx) throws PsseModelException {return super.getGI(ndx);}
	public float getDeftBI(int ndx) throws PsseModelException {return super.getBI(ndx);}
	public float getDeftGJ(int ndx) throws PsseModelException {return super.getGJ(ndx);}
	public float getDeftBJ(int ndx) throws PsseModelException {return super.getBJ(ndx);}
	public int getDeftST(int ndx) throws PsseModelException {return super.getST(ndx);}
	public float getDeftLEN(int ndx) throws PsseModelException {return super.getLEN(ndx);}

	@Override
	public float[] getRATEA() { return _ratea; }
	@Override
	public float[] getRATEB() {	return _rateb; }
	@Override
	public float[] getRATEC() {	return _ratec; }
	
	@Override
	public void setRATEA(int ndx, float rateA) throws PsseModelException {_ratea[ndx] = rateA;}
	@Override
	public void setRATEB(int ndx, float rateB) throws PsseModelException {_rateb[ndx] = rateB;}
	@Override
	public void setRATEC(int ndx, float rateC) throws PsseModelException {_ratec[ndx] = rateC;}

	@Override
	public void setX(int ndx, float x) throws PsseModelException {_x[ndx] = x;}

	
}
