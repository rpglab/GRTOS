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

import java.util.List;

import com.powerdata.openpa.psse.Bus;
import com.powerdata.openpa.psse.BusList;
import com.powerdata.openpa.psse.Limits;
import com.powerdata.openpa.psse.PsseModel;
import com.powerdata.openpa.psse.PsseModelException;

public class SvcRawList extends com.powerdata.openpa.psse.SvcList
{
	PsseModel _eq;
	int _size;
	
	String[] _i, _swrem;
	float[] _rmpct, _binit, _minB, _maxB, _vsp;
	String[] _id;
	int[] _stat;
	
	public SvcRawList() {super();}

	public SvcRawList(PsseModel model, SwitchedShuntRawList raw,
			List<Integer> svcndx) throws PsseModelException
	{
		super(model);
		_eq = model;
		_size = svcndx.size();
		
		_i = new String[_size];
		_swrem = new String[_size];
		_rmpct = new float[_size];
		_binit = new float[_size];
		_minB = new float[_size];
		_maxB = new float[_size];
		_id = new String[_size];
		_vsp = new float[_size];
		_stat = new int[_size];

		BusList rawbus = model.getBuses();
		
		for (int i=0; i < _size; ++i)
		{
			int ndx = svcndx.get(i);
			Bus bus = rawbus.get(raw.getI(ndx));
			_i[i] = bus.getObjectID();
			String swrem = raw.getSWREM(ndx);
			_swrem[i] = (swrem.isEmpty() || swrem.equals("0")) ? _i[i] : swrem;
			_rmpct[i] = raw.getRMPCT(ndx);
			_binit[i] = raw.getBINIT(ndx);
			_vsp[i] = (raw.getVSWHI(ndx)+raw.getVSWLO(ndx))/2f;
			_stat[i] = raw.isInSvc(ndx)?1:0;
			
			int[] n = raw.getN(ndx);
			float[] b = raw.getB(ndx);
			for (int iblk=0; iblk < n.length; ++iblk)
			{
				float bblk = b[iblk];
				if (bblk < 0f)
				{
					_minB[i] += bblk * n[iblk];
				}
				else if (bblk > 0f)
				{
					_maxB[i] += bblk * n[iblk];
				}
			}
		}
	}

	@Override
	public String getObjectID(int ndx) throws PsseModelException {return _id[ndx];}
	@Override
	public int size() {return _size;}
	@Override
	public String getI(int ndx) throws PsseModelException {return _i[ndx];}
	@Override
	public String getSWREM(int ndx) throws PsseModelException {return _swrem[ndx];}

	@Override
	public float getRMPCT(int ndx) throws PsseModelException {return _rmpct[ndx];}
	@Override
	public float getBINIT(int ndx) throws PsseModelException {return _binit[ndx];}

	@Override
	public Limits getReactivePowerLimits(int ndx) throws PsseModelException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isInSvc(int ndx) throws PsseModelException {return _stat[ndx] == 1;}
	@Override
	public void setInSvc(int ndx, boolean state) throws PsseModelException 
	{
		_stat[ndx] = state ? 1 : 0;
	}
	
	@Override
	public float[] getBINIT() throws PsseModelException {return _binit;}


	//Added by Xingpeng.Li Jan.11 2015
	@Override
	public boolean[] getIsElemAtBus() throws PsseModelException {return _eq.getBusGroupElems().getIsSVCAtBus();}

}
