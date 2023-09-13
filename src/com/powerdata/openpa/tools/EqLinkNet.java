package com.powerdata.openpa.tools;

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

import java.util.Arrays;
import java.util.HashSet;
import com.powerdata.openpa.psse.Bus;
import com.powerdata.openpa.psse.OneTermDev;
import com.powerdata.openpa.psse.PsseModel;
import com.powerdata.openpa.psse.PsseModelException;
import com.powerdata.openpa.psse.TwoTermDev;

public class EqLinkNet
{
	PsseModel _mdl;
	LinkNet _linknet = new LinkNet();
	long _branches[] = new long[1];
	int _islands[][] = null;
	public EqLinkNet(PsseModel mdl)
	{
		_mdl = mdl;
	}
	public int getBranchCount() { return _linknet.getBranchCount(); }
	public int getMaxBusNdx() { return _linknet.getMaxBusNdx(); }
	public int getConnectionCount(int busNdx) { return _linknet.getConnectionCount(busNdx); }
	public int add(TwoTermDev dev) throws PsseModelException
	{
		int br = _linknet.addBranch(dev.getFromBus().getIndex(), dev.getToBus().getIndex());
		if (br >= _branches.length) _branches = Arrays.copyOf(_branches, _branches.length * 2);
		_branches[br] = EqType.GetID(dev);
		return br;
	}
	public int add(OneTermDev dev) throws PsseModelException
	{
		int br = _linknet.addBranch(dev.getBus().getIndex(), dev.getBus().getIndex());
		if (br >= _branches.length) _branches = Arrays.copyOf(_branches, _branches.length * 2);
		_branches[br] = EqType.GetID(dev);
		return br;		
	}
	public BaseObject getBranch(int br) throws PsseModelException
	{
		BaseObject eq = EqType.getObject(_mdl, _branches[br]);
		return eq;
	}
	public BaseObject findBranch(Bus from, Bus to) throws PsseModelException
	{
		int br = _linknet.findBranch(from.getIndex(), to.getIndex());
		if (br >= 0) return getBranch(br);
		return null;
	}
	public BaseObject[] findBranches(Bus bus) throws PsseModelException
	{
		int br[] = _linknet.findBranches(bus.getIndex());
		BaseObject eq[] = new BaseObject[br.length];
		for(int i=0; i<br.length; i++) eq[i] = EqType.getObject(_mdl, _branches[br[i]]);
		return eq;
	}
	public Bus[] findBuses(Bus bus) throws PsseModelException
	{
		int buses[] = _linknet.findBuses(bus.getIndex());
		Bus eq[] = new Bus[buses.length];
		for(int i=0; i<buses.length; i++) eq[i] = _mdl.getBuses().get(buses[i]);
		return eq;
	}
	public Bus[] getAllBuses() throws PsseModelException
	{
		int buses[] = _linknet.getAllBuses();
		Bus eq[] = new Bus[buses.length];
		for(int i=0; i<buses.length; i++) eq[i] = _mdl.getBuses().get(buses[i]);
		return eq;		
	}
	public int[][] getIslands()
	{
		return _linknet.findGroups();
	}
	public int getIslandCount()
	{
		if (_islands == null) _islands = _linknet.findGroups();
		return _islands.length;
	}
	public Bus[] getIslandBuses(int island) throws PsseModelException
	{
		if (_islands == null) _islands = _linknet.findGroups();
		int buses[] = _islands[island];
		Bus eq[] = new Bus[buses.length];
		for(int i=0; i<buses.length; i++) eq[i] = _mdl.getBuses().get(buses[i]);
		return eq;				
	}
	public BaseObject[] getIslandEquipment(int island) throws PsseModelException
	{
		if (_islands == null) _islands = _linknet.findGroups();
		HashSet<Long> br = new HashSet<>();
		// get a single list of all branches
		for(int bus : _islands[island])
		{
			for(int b : _linknet.findBranches(bus))
			{
				br.add(_branches[b]);
			}
		}
		BaseObject eq[] = new BaseObject[br.size()];
		Long ids[] = br.toArray(new Long[0]);
		for(int i=0; i<eq.length; i++) eq[i] = EqType.getObject(_mdl, ids[i]);
		return eq;				
	}
}
