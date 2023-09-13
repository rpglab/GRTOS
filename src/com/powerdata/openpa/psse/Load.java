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

public class Load extends PsseBaseObject implements OneTermDev
{
	protected LoadList _list;
	
	public Load(int ndx, LoadList list)
	{
		super(list,ndx);
		_list = list;
	}

	/** Load bus (I) */ 
	@Override
	public Bus getBus() throws PsseModelException {return _list.getBus(_ndx);}
	@Override
	/** get load in-service status (STATUS) as a boolean.  Returns true if in service */
	public boolean isInSvc() throws PsseModelException {return _list.isInSvc(_ndx);}
	@Override
	public void setInSvc(boolean state) throws PsseModelException {_list.setInSvc(_ndx, state);}
	/** get Area Interchange record */
	public Area getAreaObj() throws PsseModelException {return _list.getAreaObj(_ndx);}
	/** get Zone record */
	public Zone getZoneObj() throws PsseModelException {return _list.getZoneObj(_ndx);}
	/** return Owner */
	public Owner getOwnerObj() throws PsseModelException {return _list.getOwnerObj(_ndx);}
	

	
	/* raw PSS/e methods */

	/** bus number or name */
	public String getI() throws PsseModelException {return _list.getI(_ndx);}
	/** load identifies used to differentiate multiple loads at the same bus */
	public String getID() throws PsseModelException {return _list.getID(_ndx);}
	/** in-service status of load (1 for in-service, 0 for out-of-service) */
	public int getSTATUS() throws PsseModelException {return _list.getSTATUS(_ndx);}
	/** index of related area record.  Defaults to same area as bus I */
	public int getAREA() throws PsseModelException {return _list.getAREA(_ndx);}
	/** index of related zone record.  Defaults to same zone as bus I */
	public int getZONE() throws PsseModelException {return _list.getZONE(_ndx);}
	/** active power of constant MVA load in MW */
	@Override
	public float getP() throws PsseModelException {return _list.getP(_ndx);}
	/** reactive power of constant MVA load in MVAr */
	@Override
	public float getQ() throws PsseModelException {return _list.getQ(_ndx);}
	@Override
	public void setP(float mw) throws PsseModelException {_list.setP(_ndx, mw);}
	@Override
	public void setQ(float mvar) throws PsseModelException {_list.setQ(_ndx, mvar);}
	/** active power of constant current load MW at 1pu voltage */
	public float getIP() throws PsseModelException {return _list.getIP(_ndx);}
	/** reactive power of constant current load MVAr at 1pu voltage */
	public float getIQ() throws PsseModelException {return _list.getIQ(_ndx);}
	/** active power of constant admittance load MW at 1pu voltage*/
	public float getYP() throws PsseModelException {return _list.getYP(_ndx);}
	/** reactive power of constant admittance load MW at 1pu voltage*/
	public float getYQ() throws PsseModelException {return _list.getYQ(_ndx);}
	/** index of related OWNER record.  Defaults to same owner as bus I */
	public int getOWNER() throws PsseModelException {return _list.getOWNER(_ndx);}

	@Override
	public float getPpu() throws PsseModelException {return _list.getPpu(_ndx);}
	@Override
	public void setPpu(float p) throws PsseModelException {_list.setPpu(_ndx, p);}
	@Override
	public float getQpu() throws PsseModelException {return _list.getQpu(_ndx);}
	@Override
	public void setQpu(float q) throws PsseModelException {_list.setQpu(_ndx, q);}
	
	/** get the load MW "setpoint" */
	public float getPS() throws PsseModelException {return _list.getPS(_ndx);}
	/** set the load MW setpoint */
	public void setPS(float mw) throws PsseModelException {_list.setPS(_ndx, mw);}
	/** get the load MVAr setpoint */
	public float getQS() throws PsseModelException {return _list.getQS(_ndx);}
	/** set the load MVAr setpoint */
	public void setQS(float mvar) throws PsseModelException {_list.setQS(_ndx, mvar);} 
	
	/** get the cold load MW */
	public float getPcold()  throws PsseModelException {return _list.getPcold(_ndx);}
	/** get the cold load MVAr */
	public float getQcold() throws PsseModelException {return _list.getQcold(_ndx);}
}
