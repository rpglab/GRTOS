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

import com.powerdata.openpa.tools.PAMath;

public abstract class SwitchedShuntList extends PsseBaseList<SwitchedShunt>
{
	public static final SwitchedShuntList Empty = new SwitchedShuntList()
	{
		@Override
		public String getI(int ndx) throws PsseModelException {return null;}
		@Override
		public String getObjectID(int ndx) throws PsseModelException {return null;}
		@Override
		public int size() {return 0;}
	};
	
	protected SwitchedShuntList() {super();}
	public SwitchedShuntList(PsseModel model) {super(model);}

	/* Standard object retrieval */

	/** Get a SwitchedShunt by it's index. */
	@Override
	public SwitchedShunt get(int ndx) { return new SwitchedShunt(ndx,this); }
	/** Get a SwitchedShunt by it's ID. */
	@Override
	public SwitchedShunt get(String id) { return super.get(id); }

	/* convenience methods */
	
	/** Load bus */ 
	public Bus getBus(int ndx) throws PsseModelException {return _model.getBus(getI(ndx));}
	/** control mode */
	public SwShuntCtrlMode getCtrlMode(int ndx) throws PsseModelException 
	{
		return SwShuntCtrlMode.fromCode(getMODSW(ndx));
	}

	/** get controlled bus */
	public Bus getCtrlBus(int ndx) throws PsseModelException
	{
		return _model.getBus(getSWREM(ndx));
	}

	/** get case shunt susceptance */
	public float getCaseB(int ndx) throws PsseModelException {return PAMath.mvar2pu(getBINIT(ndx));}
	
	/* raw methods */

	/** bus number or name */
	public abstract String getI(int ndx)throws PsseModelException;
	/** Control mode */
	public int getMODSW(int ndx) throws PsseModelException {return 1;}
	/** controlled upper limit either voltage or reactive power p.u. */
	public float getVSWHI(int ndx) throws PsseModelException {return 1f;}
	/** controlled lower limit either voltage or reactive power p.u. */
	public float getVSWLO(int ndx) throws PsseModelException {return 1f;}
	/** controlled bus */
	public String getSWREM(int ndx) throws PsseModelException {return getI(ndx);}
	/** percent of total MVAr required to hold bus voltage contributed by this shunt */
	public float getRMPCT(int ndx) throws PsseModelException {return 100f;}
	/** Name of VSC dc line if bus is specified for control (MODSW = 4) */
	public String getRMIDNT(int ndx) throws PsseModelException {return "";}
	/** switched shunt admittance */
	public float getBINIT(int ndx) throws PsseModelException {return 0f;}
	
	public ShuntList getCapacitors(int ndx) throws PsseModelException {return ShuntList.Empty;}
	public ShuntList getReactors(int ndx) throws PsseModelException {return ShuntList.Empty;}
	public ShuntList getShunts(int ndx) throws PsseModelException {return ShuntList.Empty;}
	public float getP(int ndx) throws PsseModelException {return 0f;}
	public float getQ(int ndx) throws PsseModelException {return 0f;}
	public void setP(int ndx, float mw) throws PsseModelException {}
	public void setQ(int ndx, float mvar) throws PsseModelException {}
	public float getPpu(int ndx) throws PsseModelException {return PAMath.mw2pu(getP(ndx));}
	public void setPpu(int ndx, float p) throws PsseModelException {setP(ndx, PAMath.pu2mw(p));}
	public float getQpu(int ndx) throws PsseModelException {return PAMath.mvar2pu(getQ(ndx));}
	public void setQpu(int ndx, float q) throws PsseModelException {setQ(ndx, PAMath.mvar2pu(q));}
	public boolean isInSvc(int ndx) throws PsseModelException {return true;}
	public void setInSvc(int ndx, boolean state) throws PsseModelException {}
}
