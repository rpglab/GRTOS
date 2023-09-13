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


/* TODO:  Not fully implemented.  Shunts are broken out into separate lists */

public class SwitchedShunt extends PsseBaseObject implements OneTermDev
{
	protected SwitchedShuntList _list;
	
	public SwitchedShunt(int ndx, SwitchedShuntList list)
	{
		super(list,ndx);
		_list = list;
	}

	@Override
	public String getDebugName() throws PsseModelException {return "";}

	/* Convenience methods */
	/** Load bus (I) */ 
	public Bus getBus() throws PsseModelException {return _list.getBus(_ndx);}
	/** control mode */
	public SwShuntCtrlMode getCtrlMode() throws PsseModelException {return _list.getCtrlMode(_ndx);}
	/** get voltage limits for controlled bus TODO:  this does not reflect how these objects are modeled in PSS/e*/
	/** get controlled bus */
	public Bus getCtrlBus() throws PsseModelException {return _list.getCtrlBus(_ndx);}
	/** get case shunt susceptance */
	public float getCaseB() throws PsseModelException {return _list.getCaseB(_ndx);}
	
	/* Raw PSS/e methods */

	/** bus number or name */
	public String getI() throws PsseModelException {return _list.getI(_ndx);}
	/** Control mode */
	public int getMODSW() throws PsseModelException {return _list.getMODSW(_ndx);}
	/** controlled upper limit either voltage or reactive power p.u. */
	public float getVSWHI() throws PsseModelException {return _list.getVSWHI(_ndx);}
	/** controlled lower limit either voltage or reactive power p.u. */
	public float getVSWLO() throws PsseModelException {return _list.getVSWLO(_ndx);}
	/** controlled bus */
	public String getSWREM() throws PsseModelException {return _list.getSWREM(_ndx);}
	/** percent of total MVAr required to hold bus voltage contributed by this shunt */
	public float getRMPCT()  throws PsseModelException {return _list.getRMPCT(_ndx);}
	/** Name of VSC dc line if bus is specified for control (MODSW = 4) */
	public String getRMIDNT()  throws PsseModelException {return _list.getRMIDNT(_ndx);}
	/** switched shunt susceptance */
	public float getBINIT() throws PsseModelException {return _list.getBINIT(_ndx);}
	
	ShuntList getShunts() throws PsseModelException {return _list.getShunts(_ndx);}

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
	@Override
	public boolean isInSvc() throws PsseModelException {return _list.isInSvc(_ndx);}
	@Override
	public void setInSvc(boolean state) throws PsseModelException {_list.setInSvc(_ndx, state);}
}
