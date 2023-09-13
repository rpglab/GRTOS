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

public abstract class LoadList extends PsseBaseList<Load>
{
	public static final LoadList Empty = new LoadList()
	{
		@Override
		public String getI(int ndx) throws PsseModelException {return null;}
		@Override
		public String getObjectID(int ndx) throws PsseModelException {return null;}
		@Override
		public int size() {return 0;}
	};
	
	protected LoadList() {super();}
	public LoadList(PsseModel model) {super(model);}

	/* Standard object retrieval */

	/** Get a Load by it's index. */
	@Override
	public Load get(int ndx) { return new Load(ndx,this); }
	/** Get a Load by it's ID. */
	@Override
	public Load get(String id) { return super.get(id); }

	/* convenience methods */
	
	/** Load bus (I) */ 
	public Bus getBus(int ndx) throws PsseModelException {return _model.getBus(getI(ndx));}
	/** get load in-service status (STATUS) as a boolean.  Returns true if in service */
	public boolean isInSvc(int ndx) throws PsseModelException {return getSTATUS(ndx) == 1;}
	public void setInSvc(int ndx, boolean state) throws PsseModelException {}
	/** get Area Interchange record */
	public Area getAreaObj(int ndx) throws PsseModelException
	{
		return _model.getAreas().get(String.valueOf(getAREA(ndx)));
	}
	/** get Zone record */
	public Zone getZoneObj(int ndx) throws PsseModelException
	{
		return _model.getZones().get(String.valueOf(getZONE(ndx)));
	}
	/** return Owner */
	public Owner getOwnerObj(int ndx) throws PsseModelException
	{
		return _model.getOwners().get(String.valueOf(getOWNER(ndx)));
	}
	@Override
	public String getObjectName(int ndx) throws PsseModelException
	{
		return getBus(ndx).getNAME()+":"+getID(ndx);
	}

	/* raw methods */

	/** bus number or name */
	public abstract String getI(int ndx) throws PsseModelException;
	/** load identifies used to differentiate multiple loads at the same bus */
	public String getID(int ndx) throws PsseModelException {return "1";}
	/** in-service status of load (1 for in-service, 0 for out-of-service) */
	public int getSTATUS(int ndx) throws PsseModelException {return 1;}
	/** index of related area record.  Defaults to same area as bus I */
	public int getAREA(int ndx) throws PsseModelException {return getBus(ndx).getAREA();}
	/** index of related zone record.  Defaults to same zone as bus I */
	public int getZONE(int ndx) throws PsseModelException {return getBus(ndx).getZONE();}
	/** active power of constant MVA load in MW */
	public float getP(int ndx) throws PsseModelException {return 0f;}
	public void setP(int ndx, float mw) throws PsseModelException {}
	/** reactive power of constant MVA load in MVAr */
	public float getQ(int ndx) throws PsseModelException {return 0f;}
	public void setQ(int ndx, float mvar) throws PsseModelException {}
	/** active power of constant current load MW at 1pu voltage */
	public float getIP(int ndx) throws PsseModelException {return 0f;}
	/** reactive power of constant current load MVAr at 1pu voltage */
	public float getIQ(int ndx) throws PsseModelException {return 0f;}
	/** active power of constant admittance load MW at 1pu voltage*/
	public float getYP(int ndx) throws PsseModelException {return 0f;}
	/** reactive power of constant admittance load MW at 1pu voltage*/
	public float getYQ(int ndx) throws PsseModelException {return 0f;}
	/** index of related OWNER record.  Defaults to same owner as bus I */
	public int getOWNER(int ndx) throws PsseModelException {return getBus(ndx).getOWNER();}
	
	public float getPpu(int ndx) throws PsseModelException {return PAMath.mw2pu(getP(ndx));}
	public void setPpu(int ndx, float p) throws PsseModelException {}
	public float getQpu(int ndx) throws PsseModelException {return PAMath.mvar2pu(getQ(ndx));}
	public void setQpu(int ndx, float q) throws PsseModelException {}
	public float getPS(int ndx) throws PsseModelException {return getP(ndx);}
	public void setPS(int ndx, float mw) throws PsseModelException {}
	public float getQS(int ndx) throws PsseModelException {return getQ(ndx);}
	public void setQS(int ndx, float mvar) throws PsseModelException {}
	public float getPcold(int ndx) throws PsseModelException {return 0f;}
	public float getQcold(int ndx) throws PsseModelException {return 0f;}
	
	// xingpeng, 06/06/2014
	public float[] getQ() throws PsseModelException {return null;}
	
	//Added by Xingpeng.Li Jan.11 2015
	public boolean[] getIsElemAtBus() throws PsseModelException {return null;}

	
}
