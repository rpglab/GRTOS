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

import com.powerdata.openpa.psse.util.BusSubList;


public abstract class IslandList extends PsseBaseList<Island>
{
	public static final IslandList Empty = new IslandList()
	{
		@Override
		public String getObjectID(int ndx) throws PsseModelException {return null;}
		@Override
		public int size() {return 0;}
	};
	
	protected IslandList() {super();}
	public IslandList(PsseModel model) {super(model);}

	/** Get a Transformer by it's index. */
	@Override
	public Island get(int ndx)
	{
		return (ndx == -1) ? Island.DeEnergizedIsland : new Island(ndx, this);
	}

	/** Get a Transformer by it's ID. */
	@Override
	public Island get(String id) { return super.get(id); }

	public BusList getBuses(int ndx) {return BusList.Empty;}
	public GenList getGenerators(int ndx) {return GenList.Empty;}
	public LoadList getLoads(int ndx) {return LoadList.Empty;}
	public LineList getLines(int ndx) {return LineList.Empty;}
	public TransformerList getTransformers(int ndx)
	{
		return TransformerList.Empty;
	}
	public PhaseShifterList getPhaseShifters(int ndx)
	{
		return PhaseShifterList.Empty;
	}
	public ShuntList getShunts(int ndx) throws PsseModelException {return ShuntList.Empty;}
	public SvcList getSvcs(int ndx) throws PsseModelException {return SvcList.Empty;}
	public SwitchList getSwitches(int ndx) {return SwitchList.Empty;}

	public BusList getBusesForType(int ndx, BusTypeCode bustype) throws PsseModelException
	{
		return new BusSubList(_model.getBuses(), getBusNdxsForType(ndx, bustype));
	}
	public int[] getBusNdxsForType(int ndx, BusTypeCode bustype) throws PsseModelException {return new int[0];}
	public boolean isEnergized(int ndx) throws PsseModelException {return false;}
	public int getAngleRefBusNdx(int ndx) throws PsseModelException {return -1;}
}
