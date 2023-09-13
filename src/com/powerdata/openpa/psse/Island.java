package com.powerdata.openpa.psse;

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


public class Island extends PsseBaseObject
{
	public static final Island	DeEnergizedIsland	= new Island(-1, IslandList.Empty)
	{
		@Override
		public BusList getBuses() throws PsseModelException {return BusList.Empty;}
		@Override
		public GenList getGenerators() throws PsseModelException {return GenList.Empty;}
		@Override
		public LoadList getLoads() throws PsseModelException {return LoadList.Empty;}
		@Override
		public LineList getLines() throws PsseModelException {return LineList.Empty;}
		@Override
		public TransformerList getTransformers() throws PsseModelException {return TransformerList.Empty;}
		@Override
		public PhaseShifterList getPhaseShifters() throws PsseModelException {return PhaseShifterList.Empty;}
		@Override
		public ShuntList getShunts() throws PsseModelException {return ShuntList.Empty;}
		@Override
		public SvcList getSvcs() throws PsseModelException {return SvcList.Empty;}
		@Override
		public SwitchList getSwitches() throws PsseModelException {return SwitchList.Empty;}
		@Override
		public BusList getBusesForType(BusTypeCode bustype) throws PsseModelException {return BusList.Empty;}
		@Override
		public int[] getBusNdxsForType(BusTypeCode bustype) throws PsseModelException {return new int[0];}

														@Override
														public boolean isEnergized()
																throws PsseModelException
														{
															return false;
														}

		@Override
		public int getAngleRefBusNdx() throws PsseModelException {return -1;}
	};
	protected IslandList _list;
	
	public Island(int ndx, IslandList list)
	{
		super(list,ndx);
		_list = list;
	}

	public BusList getBuses() throws PsseModelException {return _list.getBuses(_ndx);}
	public GenList getGenerators() throws PsseModelException {return _list.getGenerators(_ndx);}
	public LoadList getLoads() throws PsseModelException {return _list.getLoads(_ndx);}
	public LineList getLines() throws PsseModelException {return _list.getLines(_ndx);}
	public TransformerList getTransformers() throws PsseModelException {return _list.getTransformers(_ndx);}
	public PhaseShifterList getPhaseShifters() throws PsseModelException {return _list.getPhaseShifters(_ndx);}
	public ShuntList getShunts() throws PsseModelException { return _list.getShunts(_ndx); }
	public SvcList getSvcs() throws PsseModelException { return _list.getSvcs(_ndx); }
	public SwitchList getSwitches() throws PsseModelException {return _list.getSwitches(_ndx);}
	
	public BusList getBusesForType(BusTypeCode bustype) throws PsseModelException
	{
		return _list.getBusesForType(_ndx, bustype);
	}
	
	public int[] getBusNdxsForType(BusTypeCode bustype) throws PsseModelException
	{
		return _list.getBusNdxsForType(_ndx, bustype);
	}

	public boolean isEnergized() throws PsseModelException {return _list.isEnergized(_ndx);}

	public int getAngleRefBusNdx() throws PsseModelException {return _list.getAngleRefBusNdx(_ndx);}
}
