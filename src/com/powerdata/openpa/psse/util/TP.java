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

import java.util.ArrayList;
import java.util.Arrays;

import com.powerdata.openpa.psse.BusList;
import com.powerdata.openpa.psse.BusTypeCode;
import com.powerdata.openpa.psse.Gen;
import com.powerdata.openpa.psse.PsseModelException;
import com.powerdata.openpa.psse.PsseModel;
import com.powerdata.openpa.psse.TwoTermDev;
import com.powerdata.openpa.psse.TwoTermDevList;
import com.powerdata.openpa.tools.LinkNet;

@Deprecated
public class TP
{
	PsseModel _model;
	int _nbus;
	
	int[] _bus2island;
	boolean[] _energized;
	int[][] _groups;
	BusTypeCode[] _bustype;
	int[] _arefbyisland;
	int[] _loadbus, _genbus, _arefbus; 
	int[][] _pqbyisland, _pvbyisland;
	
	public TP(PsseModel model) throws PsseModelException
	{
		_model = model;
		BusList buses = model.getBuses();
		int nbus = buses.size();
		_nbus = nbus;
		LinkNet net = configureNetwork(model);
		_groups = net.findGroups();
		int nisland = _groups.length;
		
		_energized = new boolean[nisland];
		_arefbyisland = new int[nisland];
		Arrays.fill(_arefbyisland, -1);
		_bustype = new BusTypeCode[nbus];
		Arrays.fill(_bustype, BusTypeCode.Unknown);
		float[] maxgen = new float[nbus];

		_bus2island = new int[nbus];
		Arrays.fill(_bus2island, -1);
		for (int igrp=0; igrp < _groups.length; ++igrp)
		{
			for(int gbus : _groups[igrp])
			{
				_bus2island[gbus] = igrp;
			}
		}
		
		for(int i=0; i < nbus; ++i)
		{
			if (net.getConnectionCount(i)==0)
			{
				_bustype[i] = BusTypeCode.Isolated;
			}
		}

		int egycnt = 0;
		for(Gen g : model.getGenerators())
		{
			if (g.isInSvc())
			{
				int busndx = g.getBus().getIndex();
				int island = _bus2island[busndx];
				//TODO:  should islands be able to be -1?
				if (island != -1 && !_energized[island])
				{
					_energized[island] = true;
					++egycnt;
				}
				if ((g.getQT() - g.getQB()) > 1f
						&& _bustype[busndx] == BusTypeCode.Unknown)
				{
					_bustype[busndx] = BusTypeCode.Gen;
					maxgen[busndx] += g.getPT();
					if (_arefbyisland[island] == -1 || maxgen[busndx] > maxgen[_arefbyisland[island]])
							_arefbyisland[island] = busndx;
				}
			}
		}
//		_arefbyisland[0] = 43;
		
		int[] genbus = new int[nbus];
		int[] loadbus = new int[nbus];
		int ngen=0, nload=0, ihot=0;
		_arefbus = new int[egycnt];
		for (int i=0; i < nbus; ++i)
		{
			int island = _bus2island[i];
			
			if (island > -1 && _energized[island])
			{
				if (_bustype[i] == BusTypeCode.Gen)
				{
					if (_arefbyisland[island] != i)
					{
						genbus[ngen++] = i;
					}
					else
					{
						_arefbus[ihot++] = i;
						_bustype[i] = BusTypeCode.Slack;
					}
				}
				else
				{
					_bustype[i] = BusTypeCode.Load;
					loadbus[nload++] = i;
				}
			}
		}
		_genbus = Arrays.copyOf(genbus, ngen);
		_loadbus = Arrays.copyOf(loadbus, nload);
	}

	LinkNet configureNetwork(PsseModel model) throws PsseModelException
	{
		BusList buses = model.getBuses();
		TwoTermDevList branches = model.getTwoTermDevs();
		LinkNet rv = new LinkNet();
		int nbr = branches.size();
		rv.ensureCapacity(buses.getI(buses.size()-1), nbr);
		for(int i=0; i < nbr; ++i)
		{
			TwoTermDev b = branches.get(i);
			if (b.isInSvc())
			{
				rv.addBranch(b.getFromBus().getIndex(), b.getToBus().getIndex());
			}
		}

		return rv;
	}
	
	/**
	 * Return the island number of a node (connectivity node or topological node)
	 * @param node
	 * @return
	 */
	public int getIsland(int bus)
	{
		return _bus2island[bus];
	}
	
	/**
	 * Return the number of islands.
	 * @return
	 */
	public int getIslandCount() {return _groups.length;}

	/**
	 * Return the energization status of an island.
	 * @param island
	 * @return
	 * @throws PsseModelException
	 */
	public boolean isIslandEnergized(int island) throws PsseModelException
	{
		return _energized[island];
	}

	public int[] getIslandNodes(int island)
	{
		return _groups[island];
	}

	public BusTypeCode getBusType(int bus)
	{
		return _bustype[bus];
	}

	public int[] getBusNdxsForType(BusTypeCode bustype)
	{
		switch(bustype)
		{
			case Load: return _loadbus;
			case Gen: return _genbus;
			case Slack: return _arefbus;
			default: return new int[0];
		}
	}
	
	public int[] getBusNdxsForType(int islandndx, BusTypeCode bustype) throws PsseModelException
	{
		if (_energized[islandndx])
		{
			if (_pqbyisland == null)
			{
				analyzeIslandBustypes();
			}
			switch(bustype)
			{
				case Load: return _pqbyisland[islandndx];
				case Gen: return _pvbyisland[islandndx];
				case Slack: return new int[] {_arefbyisland[islandndx]};
				default: return new int[0];
			}
		}
		else
		{
			return new int[0];
		}
	}

	void analyzeIslandBustypes() throws PsseModelException
	{		
		int nisland = _groups.length;
		_pqbyisland = new int[nisland][];
		_pvbyisland = new int[nisland][];
		int[] nld = new int[nisland], ngen = new int[nisland];
		
		for(int ig=0; ig < nisland; ++ig)
		{
			int[] grp = _groups[ig];
			int ng = grp.length;
			_pqbyisland[ig] = new int[ng];
			_pvbyisland[ig] = new int[ng];
		}

		for(int gb : _genbus)
		{
			int ix = _bus2island[gb];
			_pvbyisland[ix][ngen[ix]++] = gb;
		}
		
		for(int lb : _loadbus)
		{
			int ix = _bus2island[lb];
			_pqbyisland[ix][nld[ix]++] = lb;
		}
		
		for(int ig=0; ig < nisland; ++ig)
		{
			_pvbyisland[ig] = Arrays.copyOf(_pvbyisland[ig], ngen[ig]);
			_pqbyisland[ig] = Arrays.copyOf(_pqbyisland[ig], nld[ig]);
		}
	}

	public int getAngleRefBusNdx(int ndx)
	{
		return _arefbyisland[ndx];
	}
	
	
	//Added by Xingpeng.Li, May.18, 2014.	
	public void setBusNdxsForTypeLoad(int[] LoadBuses)
	{
		_loadbus = LoadBuses;
		ArrayList<Integer> pqBuses = new ArrayList<Integer>();
		for (int i=0; i<LoadBuses.length; i++)
		{
			pqBuses.add(LoadBuses[i]);
		}
		for (int i=0; i<_bustype.length; i++)
		{
			if (pqBuses.contains(i)) _bustype[i] = BusTypeCode.Load;
		}
		_pqbyisland = null;
		_pvbyisland = null;
	}

	public void setBusNdxsForTypeGen(int[] GenBuses)
	{
		_genbus = GenBuses;
		ArrayList<Integer> pvBuses = new ArrayList<Integer>();
		for (int i=0; i<GenBuses.length; i++)
		{
			pvBuses.add(GenBuses[i]);
		}
		for (int i=0; i<_bustype.length; i++)
		{
			if (pvBuses.contains(i)) _bustype[i] = BusTypeCode.Gen;
		}
		_pqbyisland = null;
		_pvbyisland = null;
	}

	public void setBusNdxsForTypeSlack(int[] SlackBuses)
	{
		_arefbus = SlackBuses;
		ArrayList<Integer> slackBuses = new ArrayList<Integer>();
		for (int i=0; i<SlackBuses.length; i++)
		{
			slackBuses.add(SlackBuses[i]);
		}
		for (int i=0; i<_bustype.length; i++)
		{
			if (slackBuses.contains(i)) _bustype[i] = BusTypeCode.Slack;
		}
		_pqbyisland = null;
		_pvbyisland = null;
	}
}
