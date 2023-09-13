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

import com.powerdata.openpa.psse.BusTypeCode;
import com.powerdata.openpa.psse.PsseModelException;
import com.powerdata.openpa.psse.util.TP;
import com.powerdata.openpa.psse.PsseModel;

public class IslandList extends com.powerdata.openpa.psse.IslandList
{
	PsseRawModel _model;
	public IslandList(PsseRawModel eq) throws PsseModelException
	{
		super(eq);
		_model = eq;
	}
	
	@Override
	public String getObjectID(int ndx) throws PsseModelException
	{
		return String.valueOf(ndx);
	}

	@Override
	public int size()
	{
		int rv = 0;
		try
		{
			rv = _model.tp().getIslandCount();
		}
		catch(PsseModelException ex)
		{
			System.err.println(ex);
		}
		return rv;
	}

	@Override
	public int[] getBusNdxsForType(int ndx, BusTypeCode bustype) throws PsseModelException
	{
		return _model.tp().getBusNdxsForType(ndx, bustype);
	}

	@Override
	public boolean isEnergized(int ndx) throws PsseModelException
	{
		return _model.tp().isIslandEnergized(ndx);
	}

	@Override
	public int getAngleRefBusNdx(int ndx) throws PsseModelException
	{
		return _model.tp().getAngleRefBusNdx(ndx);
	}
}
