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

import com.powerdata.openpa.psse.BusList;
import com.powerdata.openpa.psse.PsseModel;
import com.powerdata.openpa.psse.PsseModelException;
import com.powerdata.openpa.tools.LoadArray;
import com.powerdata.openpa.tools.SimpleCSV;

public class GenList extends com.powerdata.openpa.psse.GenList
{
	PsseModel _eq;
	BusList _buses;
	int _size;
	
	String _i[];
	String _id[];
	float _pg[],_qg[],_qt[],_qb[],_vs[];
	String _ireg[];
	float _mbase[],_zr[],_zx[],_rt[],_xt[],_gtap[];
	int _stat[];
	float _rmpct[],_pt[],_pb[];

	public GenList(PsseModel eq, File dir) throws PsseModelException
	{
		super(eq);
		try
		{
			_eq = eq;
			_buses = _eq.getBuses();
			File dbfile = new File(dir, "Generator.csv");
			SimpleCSV gens = new SimpleCSV(dbfile);
			_size	= gens.getRowCount();
			_i		= gens.get("I");
			_id		= LoadArray.String(gens,"ID",this,"getID");
			_pg		= LoadArray.Float(gens,"PG",this,"getP");
			_qg		= LoadArray.Float(gens,"QG",this,"getQ");
			_qt		= LoadArray.Float(gens,"QT",this,"getQT");
			_qb		= LoadArray.Float(gens,"QB",this,"getQB");
			_vs		= LoadArray.Float(gens,"VS",this,"getVS");
			_ireg	= LoadArray.String(gens,"IREG",this,"getIREG");
			_mbase	= LoadArray.Float(gens,"MBASE",this,"getMBASE");
			_zr		= LoadArray.Float(gens,"ZR",this,"getZR");
			_zx		= LoadArray.Float(gens,"ZX",this,"getZX");
			_rt		= LoadArray.Float(gens,"RT",this,"getRT");
			_xt		= LoadArray.Float(gens,"XT",this,"getXT");
			_gtap	= LoadArray.Float(gens,"GTAP",this,"getGTAP");
			_stat	= LoadArray.Int(gens,"STAT",this,"getSTAT");
			_rmpct	= LoadArray.Float(gens,"RMPCT",this,"getRMPCT");
			_pt		= LoadArray.Float(gens,"PT",this,"getPT");
			_pb		= LoadArray.Float(gens,"PB",this,"getPB");
			if (_i == null)
			{
				throw new PsseModelException(getClass().getName()+" missing I in "+dbfile);
			}
			
			reindex();
		}
		catch(Exception e)
		{
			throw new PsseModelException(getClass().getName()+": "+e);
		}
	}
	@Override
	public String getI(int ndx) { return _i[ndx]; }
	@Override
	public String getID(int ndx) { return _id[ndx]; }
	@Override
	public float getP(int ndx) { return _pg[ndx]; }
	@Override
	public float getQ(int ndx) { return _qg[ndx]; }
	@Override
	public float getQT(int ndx) { return _qt[ndx]; }
	@Override
	public float getQB(int ndx) { return _qb[ndx]; }
	@Override
	public float getVS(int ndx) { return _vs[ndx]; }
	@Override
	public String getIREG(int ndx)
	{
		String i = _ireg[ndx];
		return (i.equals("0")) ? getI(ndx) : _ireg[ndx];
	}

	@Override
	public float getMBASE(int ndx) { return _mbase[ndx]; }
	@Override
	public float getZR(int ndx) { return _zr[ndx]; }
	@Override
	public float getZX(int ndx) { return _zx[ndx]; }
	@Override
	public float getRT(int ndx) { return _rt[ndx]; }
	@Override
	public float getXT(int ndx) { return _xt[ndx]; }
	@Override
	public float getGTAP(int ndx) { return _gtap[ndx]; }
	@Override
	public int getSTAT(int ndx) { return _stat[ndx]; }
	@Override
	public float getRMPCT(int ndx) { return _rmpct[ndx]; }
	@Override
	public float getPT(int ndx) { return _pt[ndx]; }
	@Override
	public float getPB(int ndx) { return _pb[ndx]; }
	@Override
	public String getObjectID(int ndx) { return "GEN-"+_i[ndx]+":"+_id[ndx]; }
	@Override
	public int size() { return _size; }
	@Override
	public void setInSvc(int ndx, boolean state) throws PsseModelException {_stat[ndx] = state?1:0;}

	public String getDeftID(int ndx) throws PsseModelException {return super.getID(ndx);}
	public float getDeftP(int ndx) throws PsseModelException {return super.getP(ndx);}
	public float getDeftQ(int ndx) throws PsseModelException {return super.getQ(ndx);}
	public float getDeftQT(int ndx) throws PsseModelException {return super.getQT(ndx);}
	public float getDeftQB(int ndx) throws PsseModelException {return super.getQB(ndx);}
	public float getDeftVS(int ndx) throws PsseModelException {return super.getVS(ndx);}
	public String getDeftIREG(int ndx) throws PsseModelException {return super.getIREG(ndx);}
	public float getDeftMBASE(int ndx) throws PsseModelException {return super.getMBASE(ndx);}
	public float getDeftZR(int ndx) throws PsseModelException {return super.getZR(ndx);}
	public float getDeftZX(int ndx) throws PsseModelException {return super.getZX(ndx);}
	public float getDeftRT(int ndx) throws PsseModelException {return super.getRT(ndx);}
	public float getDeftXT(int ndx) throws PsseModelException {return super.getXT(ndx);}
	public float getDeftGTAP(int ndx) throws PsseModelException {return super.getGTAP(ndx);}
	public int getDeftSTAT(int ndx) throws PsseModelException {return super.getSTAT(ndx);}
	public float getDeftRMPCT(int ndx) throws PsseModelException {return super.getRMPCT(ndx);}
	public float getDeftPT(int ndx) throws PsseModelException {return super.getPT(ndx);}
	public float getDeftPB(int ndx) throws PsseModelException {return super.getPB(ndx);}
	
	@Override
	public int[] getSTAT() throws PsseModelException {	return _stat;}
	
	
	@Override
	public void setP(int ndx, float p) { _pg[ndx] = p; }
	@Override
	public void setQ(int ndx, float q) { _qg[ndx] = q; }
	@Override
	public float[] getMBASE() { return _mbase; }
	
	@Override
	public void setQ(float[] qg) {_qg = qg;}
	@Override
	public float[] getQ() {	return _qg;}

	@Override
	public void setP(float[] pg) {_pg = pg;}
	@Override
	public float[] getP() {return _pg;}

	//Added by Xingpeng.Li
	@Override
	public void setPT(int ndx, float mw) throws PsseModelException {_pt[ndx] = mw;}

	//Added by Xingpeng.Li Jan.11 2015
	@Override
	public boolean[] getIsElemAtBus() throws PsseModelException {return _eq.getBusGroupElems().getIsGenAtBus();}

}
