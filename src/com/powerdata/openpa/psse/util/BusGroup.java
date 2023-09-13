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

import com.powerdata.openpa.psse.BusList;
import com.powerdata.openpa.psse.GenList;
import com.powerdata.openpa.psse.LineList;
import com.powerdata.openpa.psse.LoadList;
import com.powerdata.openpa.psse.PhaseShifterList;
import com.powerdata.openpa.psse.PsseLists;
import com.powerdata.openpa.psse.PsseModelException;
import com.powerdata.openpa.psse.ShuntList;
import com.powerdata.openpa.psse.SvcList;
import com.powerdata.openpa.psse.SwitchList;
import com.powerdata.openpa.psse.SwitchedShuntList;
import com.powerdata.openpa.psse.TransformerList;
import com.powerdata.openpa.psse.TwoTermDCLineList;
import com.powerdata.openpa.tools.AbstractBaseObject;

public class BusGroup extends AbstractBaseObject implements PsseLists
{
	BusGroupList _bglist;
	
	public BusGroup(BusGroupList list, int ndx)
	{
		super(list, ndx);
		_bglist = list;
	}

	@Override
	public BusList getBuses() throws PsseModelException {return _bglist.getBuses(_ndx);}
	@Override
	public GenList getGenerators() throws PsseModelException {return _bglist.getGenerators(_ndx);}
	@Override
	public LoadList getLoads() throws PsseModelException {return _bglist.getLoads(_ndx);}
	@Override
	public LineList getLines() throws PsseModelException {return _bglist.getLines(_ndx);}

	@Override
	public TransformerList getTransformers() throws PsseModelException
	{
		return _bglist.getTransformers(_ndx);
	}

	@Override
	public PhaseShifterList getPhaseShifters() throws PsseModelException
	{
		return _bglist.getPhaseShifters(_ndx);
	}

	@Override
	public SwitchList getSwitches() throws PsseModelException {return _bglist.getSwitches(_ndx);}
	@Override
	public ShuntList getShunts() throws PsseModelException {return _bglist.getShunts(_ndx);}
	@Override
	public SvcList getSvcs() throws PsseModelException {return _bglist.getSvcs(_ndx);}
	@Override
	public SwitchedShuntList getSwitchedShunts() throws PsseModelException 
	{
		return _bglist.getSwitchedShunts(_ndx);
	}

	@Override
	public TwoTermDCLineList getTwoTermDCLines() throws PsseModelException
	{
		return _bglist.getTwoTermDCLines(_ndx);
	}

}
