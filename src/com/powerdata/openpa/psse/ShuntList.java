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

public abstract class ShuntList extends PsseBaseList<Shunt>
{
	public static final ShuntList Empty = new ShuntList()
	{
		@Override
		public String getI(int ndx) throws PsseModelException {return null;}
		@Override
		public String getObjectID(int ndx) throws PsseModelException {return null;}
		@Override
		public int size() {return 0;}
	};
	
	protected ShuntList(){super();}

	public ShuntList(PsseModel model) throws PsseModelException
	{
		super(model);
	}

	/** Get a Switch by it's index. */
	@Override
	public Shunt get(int ndx) { return new Shunt(ndx,this); }
	/** Get an SwitchIn by it's ID. */
	@Override
	public Shunt get(String id) { return super.get(id); }
	
	/* convenience methods */
	
	public Bus getBus(int ndx) throws PsseModelException {return _model.getBus(getI(ndx));}
	
	public float getBpu(int ndx) throws PsseModelException
	{
		return isInSvc(ndx) ? getB(ndx)/100f : 0;
	}
	public float getGpu(int ndx) throws PsseModelException
	{
		return isInSvc(ndx) ? getG(ndx)/100f : 0;
	}

	/** get connected bus */
	public abstract String getI(int ndx) throws PsseModelException;
	/** shunt nominal B in MVAr at unity bus voltage */
	public float getB(int ndx) throws PsseModelException {return 0f;}
	/** shunt nominal G, MW at unity voltage */
	public float getG(int ndx) throws PsseModelException {return 0f;}

	public boolean isInSvc(int ndx) throws PsseModelException {return true;}
	public void setInSvc(int ndx, boolean state) throws PsseModelException {}

	public float getP(int ndx) throws PsseModelException {return 0f;}
	public float getQ(int ndx) throws PsseModelException {return 0f;}
	public void setP(int ndx, float mw) throws PsseModelException {}
	public void setQ(int ndx, float mvar) throws PsseModelException {}
	public float getPpu(int ndx) throws PsseModelException {return 0f;}
	public void setPpu(int ndx, float p) throws PsseModelException {setP(ndx, p*100f);}
	public float getQpu(int ndx) throws PsseModelException {return 0f;}
	public void setQpu(int ndx, float q) throws PsseModelException {setQ(ndx, q*100f);}

	public String getID(int ndx) throws PsseModelException {return "";}
	
	
	public float[] getB() throws PsseModelException {return null;}

	//Added by Xingpeng.Li Jan.11 2015
	public boolean[] getIsElemAtBus() throws PsseModelException {return null;}

}
