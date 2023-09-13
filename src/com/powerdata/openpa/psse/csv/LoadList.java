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

import com.powerdata.openpa.psse.PsseModel;
import com.powerdata.openpa.psse.BusList;
import com.powerdata.openpa.psse.PsseModelException;
import com.powerdata.openpa.tools.LoadArray;
import com.powerdata.openpa.tools.SimpleCSV;

public class LoadList extends com.powerdata.openpa.psse.LoadList
{
	PsseModel _eq;
	int _size;
	
	String[] _i, _id;
	
	int[] _status, _area, _zone, _owner;
	float[] _pl, _ql, _ip, _iq, _yp, _yq;
	
	BusList _buses;
	
	public LoadList() {super();}
	public LoadList(PsseModel eq, File dir) throws PsseModelException
	{
		super(eq);
		_eq = eq;
		_buses = eq.getBuses();
		try
		{
			_eq = eq;
			_buses = _eq.getBuses();
			SimpleCSV loads = new SimpleCSV(new File(dir, "Load.csv"));
			_size = loads.getRowCount();
			_i		= loads.get("I");
			_id		= LoadArray.String(loads,"ID",this,"getID");
			_pl		= LoadArray.Float(loads,"PL",this,"getP");
			_ql		= LoadArray.Float(loads,"QL",this,"getQ");
			_ip		= LoadArray.Float(loads,"IP",this,"getIP");
			_iq		= LoadArray.Float(loads,"IQ",this,"getIQ");
			_yp		= LoadArray.Float(loads,"YP",this,"getYP");
			_yq		= LoadArray.Float(loads,"YQ",this,"getYQ");
			_status = LoadArray.Int(loads, "STATUS", this, "getSTATUS");
			_area = LoadArray.Int(loads, "AREA", this, "getAREA");
			_zone = LoadArray.Int(loads, "ZONE", this, "getZONE");
			_owner = LoadArray.Int(loads, "OWNER", this, "getOWNER");
			reindex();

		}
		catch(IOException | SecurityException | ReflectiveOperationException e)
		{
			throw new PsseModelException(e);
		}
	}
	
	@Override
	public String getI(int ndx) throws PsseModelException {return _i[ndx];}
	@Override
	public String getObjectID(int ndx) throws PsseModelException {return "LD-"+_i[ndx]+":"+_id[ndx];}
	@Override
	public String getID(int ndx) throws PsseModelException {return _id[ndx];}
	@Override
	public int getSTATUS(int ndx) throws PsseModelException {return _status[ndx];}
	@Override
	public int getAREA(int ndx) throws PsseModelException {return _area[ndx];}
	@Override
	public int getZONE(int ndx) throws PsseModelException {return _zone[ndx];}
	@Override
	public float getP(int ndx) throws PsseModelException {return _pl[ndx];}
	@Override
	public float getQ(int ndx) throws PsseModelException {return _ql[ndx];}
	@Override
	public float getIP(int ndx) throws PsseModelException {return _ip[ndx];}
	@Override
	public float getIQ(int ndx) throws PsseModelException {return _iq[ndx];}
	@Override
	public float getYP(int ndx) throws PsseModelException {return _yp[ndx];}
	@Override
	public float getYQ(int ndx) throws PsseModelException {return _yq[ndx];}
	@Override
	public int getOWNER(int ndx) throws PsseModelException {return _owner[ndx];}
	@Override
	public int size() {return _size;}
	@Override
	public void setInSvc(int ndx, boolean state) throws PsseModelException
	{
		_status[ndx] = state ? 1 : 0;
	}
	
	// xingpeng, 06/06/2014
	@Override
	public float[] getQ() throws PsseModelException {return _ql;}

	//Added by Xingpeng.Li Jan.11 2015
	@Override
	public boolean[] getIsElemAtBus() throws PsseModelException {return _eq.getBusGroupElems().getIsLoadAtBus();}

}
