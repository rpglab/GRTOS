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

public class Shunt extends PsseBaseObject implements OneTermDev
{

	protected ShuntList	_list;

	public Shunt(int ndx, ShuntList list)
	{
		super(list,ndx);
		_list = list;
	}

	@Override
	public Bus getBus() throws PsseModelException {return _list.getBus(_ndx);}

	/** shunt nominal susceptance, p.u. at nominal bus kv */
	public float getGpu() throws PsseModelException {return _list.getGpu(_ndx);}
	/** shunt nominal susceptance, p.u. at nominal bus kv */
	public float getBpu() throws PsseModelException {return _list.getBpu(_ndx);}

	@Override
	public boolean isInSvc() throws PsseModelException {return _list.isInSvc(_ndx);}
	@Override
	public void setInSvc(boolean state) throws PsseModelException {_list.setInSvc(_ndx, state);}
	/** get connected bus */
	public String getI() throws PsseModelException {return _list.getI(_ndx);}
	/** shunt nominal B in MVAr at unity bus voltage */
	public float getB() throws PsseModelException {return _list.getB(_ndx);}
	/** shunt nominal G in MW at unity bus voltage */
	public float getG() throws PsseModelException {return _list.getG(_ndx);}

	@Override
	public float getP() throws PsseModelException {return _list.getP(_ndx);}
	@Override
	public float getQ() throws PsseModelException {return _list.getQ(_ndx);}
	@Override
	public void setP(float mw) throws PsseModelException {_list.setP(_ndx, mw);}
	@Override
	public void setQ(float mvar) throws PsseModelException {_list.setQ(_ndx, mvar);}
	@Override
	public float getPpu() throws PsseModelException {return _list.getPpu(_ndx);}
	@Override
	public void setPpu(float p) throws PsseModelException {_list.setPpu(_ndx, p);}
	@Override
	public float getQpu() throws PsseModelException {return _list.getQpu(_ndx);}
	@Override
	public void setQpu(float q) throws PsseModelException {_list.setQpu(_ndx, q);}
	
	/** get circuit ID of shunt */
	public String getID() throws PsseModelException {return _list.getID(_ndx);}
}
