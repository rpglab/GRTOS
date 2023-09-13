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

import com.powerdata.openpa.psse.Area;
import com.powerdata.openpa.psse.Bus;
import com.powerdata.openpa.psse.Load;
import com.powerdata.openpa.psse.LoadList;
import com.powerdata.openpa.psse.Owner;
import com.powerdata.openpa.psse.PsseModelException;
import com.powerdata.openpa.psse.Zone;

public class LoadSubList extends LoadList
{
	LoadList _base;
	int[] _ndxs;
	boolean _indexed = false;
		
	public LoadSubList(LoadList base, int[] ndxs) throws PsseModelException
	{
		super(base.getPsseModel());
		_base = base;
		_ndxs = ndxs;
	}
	
	protected int map(int ndx) {return _ndxs[ndx];}

	@Override
	public Load get(String id)
	{
		if (!_indexed)
		{
			_indexed = true;
			try
			{
				reindex();
			} catch (PsseModelException e)
			{
				e.printStackTrace();
			}
		}
		return super.get(id);
	}

	@Override
	public Bus getBus(int ndx) throws PsseModelException {return _base.getBus(map(ndx));}
	@Override
	public boolean isInSvc(int ndx) throws PsseModelException {return _base.isInSvc(map(ndx));}
	@Override
	public Area getAreaObj(int ndx) throws PsseModelException {return _base.getAreaObj(map(ndx));}
	@Override
	public Zone getZoneObj(int ndx) throws PsseModelException {return _base.getZoneObj(map(ndx));}
	@Override
	public Owner getOwnerObj(int ndx) throws PsseModelException {return _base.getOwnerObj(map(ndx));}
	@Override
	public String getObjectName(int ndx) throws PsseModelException {return _base.getObjectName(map(ndx));}
	@Override
	public String getI(int ndx) throws PsseModelException {return _base.getI(map(ndx));}
	@Override
	public String getID(int ndx) throws PsseModelException {return _base.getID(map(ndx));}
	@Override
	public int getSTATUS(int ndx) throws PsseModelException {return _base.getSTATUS(map(ndx));}
	@Override
	public int getAREA(int ndx) throws PsseModelException {return _base.getAREA(map(ndx));}
	@Override
	public int getZONE(int ndx) throws PsseModelException {return _base.getZONE(map(ndx));}
	@Override
	public float getP(int ndx) throws PsseModelException {return _base.getP(map(ndx));}
	@Override
	public void setP(int ndx, float mw) throws PsseModelException {_base.setP(map(ndx), mw);}
	@Override
	public float getQ(int ndx) throws PsseModelException {return _base.getQ(map(ndx));}
	@Override
	public void setQ(int ndx, float mvar) throws PsseModelException {_base.setQ(map(ndx), mvar);}
	@Override
	public float getIP(int ndx) throws PsseModelException {return _base.getIP(map(ndx));}
	@Override
	public float getIQ(int ndx) throws PsseModelException {return _base.getIQ(map(ndx));}
	@Override
	public float getYP(int ndx) throws PsseModelException {return _base.getYP(map(ndx));}
	@Override
	public float getYQ(int ndx) throws PsseModelException {return _base.getYQ(map(ndx));}
	@Override
	public int getOWNER(int ndx) throws PsseModelException {return _base.getOWNER(map(ndx));}
	@Override
	public float getPpu(int ndx) throws PsseModelException {return _base.getPpu(map(ndx));}
	@Override
	public void setPpu(int ndx, float p) throws PsseModelException {_base.setPpu(map(ndx), p);}
	@Override
	public float getQpu(int ndx) throws PsseModelException {return _base.getQpu(map(ndx));}
	@Override
	public void setQpu(int ndx, float q) throws PsseModelException {_base.setQpu(map(ndx), q);}
	@Override
	public float getPS(int ndx) throws PsseModelException {return _base.getPS(map(ndx));}
	@Override
	public void setPS(int ndx, float mw) throws PsseModelException {_base.setPS(map(ndx), mw);}
	@Override
	public float getQS(int ndx) throws PsseModelException {return _base.getQS(map(ndx));}
	@Override
	public void setQS(int ndx, float mvar) throws PsseModelException {_base.setQS(map(ndx), mvar);}
	@Override
	public float getPcold(int ndx) throws PsseModelException {return _base.getPcold(map(ndx));}
	@Override
	public float getQcold(int ndx) throws PsseModelException {return _base.getQcold(map(ndx));}
	@Override
	public void commit() throws PsseModelException {_base.commit();}
	@Override
	public String getObjectID(int ndx) throws PsseModelException {return _base.getObjectID(map(ndx));}
	@Override
	public String getFullName(int ndx) throws PsseModelException {return _base.getFullName(map(ndx));}
	@Override
	public String getDebugName(int ndx) throws PsseModelException {return _base.getDebugName(map(ndx));}
	@Override
	public int getRootIndex(int ndx) {return _base.getRootIndex(map(ndx));}
	@Override
	public int size() {return _ndxs.length;}

	@Override
	public void setInSvc(int ndx, boolean state) throws PsseModelException {_base.setInSvc(map(ndx), state);}
}
