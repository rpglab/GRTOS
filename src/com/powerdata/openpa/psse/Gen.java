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

public class Gen extends PsseBaseObject implements OneTermDev
{
	public enum GenRegMode
	{
		Voltage, ReactivePower;
	}

	protected GenList _list;
	
	public Gen(int ndx, GenList list)
	{
		super(list,ndx);
		_list = list;
	}

	/** Generator bus (I) */ 
	@Override
	public Bus getBus() throws PsseModelException {return _list.getBus(_ndx);}
	/** remote regulated bus.  (IREG) Null if local */
	public Bus getRemoteRegBus() throws PsseModelException {return _list.getRemoteRegBus(_ndx);}
	/** get the Generator mode */
	public GenMode getMode() throws PsseModelException {return _list.getMode(_ndx); }
	/** get the Generator mode */
	public void setMode(GenMode mode) throws PsseModelException {_list.setMode(_ndx, mode); }
	@Override
	public boolean isInSvc() throws PsseModelException {return _list.isInSvc(_ndx);}
	public GenType getGenType() throws PsseModelException {return _list.getType(_ndx);}
	@Deprecated
	/** is unit in Automatic Voltage Regulation (AVR) */
	public boolean isInAvr() throws PsseModelException {return _list.isInAvr(_ndx);}
	
	/** get regulation Mode */
	public GenRegMode getRegMode() throws PsseModelException {return _list.getRegMode(_ndx);}
	/** set reguation model */
	public void setRegMode(GenRegMode mode) throws PsseModelException {_list.setRegMode(_ndx, mode);}


	/* Raw PSS/e methods */
	
	/** bus number or name */
	public String getI() throws PsseModelException {return _list.getI(_ndx);}
	/** Machine identifier */
	public String getID() throws PsseModelException {return _list.getID(_ndx);}
	@Override
	/** Generator active power output in MW */
	public float getP() throws PsseModelException {return _list.getP(_ndx);}
	@Override
	/** Generator reactive power output in MVAr */
	public float getQ() throws PsseModelException {return _list.getQ(_ndx);}
	@Override
	public void setP(float mw) throws PsseModelException {_list.setP(_ndx, mw);}
	@Override
	public void setQ(float mvar) throws PsseModelException {_list.setQ(_ndx, mvar);}

	/** Maximum generator reactive power output (MVAr) */
	public float getQT() throws PsseModelException {return _list.getQT(_ndx);}
	/** Maximum generator reactive power output (MVAr) */
	public void setQT(float mvar) throws PsseModelException {_list.setQT(_ndx, mvar);}
	/** Minimum generator reactive power output (MVAr) */
	public float getQB() throws PsseModelException {return _list.getQB(_ndx);}
	/** Minimum generator reactive power output (MVAr) */
	public void setQB(float mvar) throws PsseModelException {_list.setQB(_ndx, mvar);}
	/** Regulated voltage setpoint entered in p.u. */
	public float getVS() throws PsseModelException {return _list.getVS(_ndx);}
	/** Regulated voltage setpoint entered in p.u. */
	public void setVS(float vmpu) throws PsseModelException {_list.setVS(_ndx, vmpu);}
	/** remote regulated bus number or name.  Set to 0 if regulating local bus */
	public String getIREG() throws PsseModelException {return _list.getIREG(_ndx);}
	/** total MVA base of units represented in this machine */
	public float getMBASE() throws PsseModelException {return _list.getMBASE(_ndx);}
	/** machine resistance p.u. on MBASE base */
	public float getZR() throws PsseModelException {return _list.getZR(_ndx);}
	/** machine reactance p.u. on MBASE base */
	public float getZX() throws PsseModelException {return _list.getZX(_ndx);}
	/** Step-up transformer resistance entered in p.u. on MBASE base */
	public float getRT() throws PsseModelException {return _list.getRT(_ndx);}
	/** Step-up transformer reactance entered in p.u. on MBASE base */
	public float getXT() throws PsseModelException {return _list.getXT(_ndx);}
	/** Step-up transformer off-nominal turns ratio entered in p.u. */
	public float getGTAP() throws PsseModelException {return _list.getGTAP(_ndx);}
	/** Initial machine status (1 is in-service, 0 means out of service) */
	public int getSTAT() throws PsseModelException {return _list.getSTAT(_ndx);}
	/** Percent of the total Mvar required to hold the voltage at the bus controlled by this
	    bus "I" that are to be contributed by the generation at bus "I" */
	public float getRMPCT() throws PsseModelException {return _list.getRMPCT(_ndx);}
	/** max active power in MW */
	public float getPT() throws PsseModelException {return _list.getPT(_ndx);}
	/** max active power in MW */
	public void setPT(float mw) throws PsseModelException {_list.setPT(_ndx, mw);}
	/** min active power in MW */
	public float getPB() throws PsseModelException {return _list.getPB(_ndx);}
	/** min active power in MW */
	public void setPB(float mw) throws PsseModelException {_list.setPB(_ndx, mw);}
	
	/** return Ownership as a list */
	public OwnershipList getOwnership() throws PsseModelException {return _list.getOwnership(_ndx);}

	@Override
	public float getPpu() throws PsseModelException {return _list.getPpu(_ndx);}
	@Override
	public void setPpu(float p) throws PsseModelException {_list.setPpu(_ndx, p);}
	@Override
	public float getQpu() throws PsseModelException {return _list.getQpu(_ndx);}
	@Override
	public void setQpu(float q) throws PsseModelException {_list.setQpu(_ndx, q);}
	/** get MW setpoint */
	public float getPS() throws PsseModelException {return _list.getPS(_ndx);}
	/** set MW setpoint */
	public void setPS(float mw) throws PsseModelException {_list.setPS(_ndx, mw);}
	/** get MVAr set point (when regulating reactive power) */
	public float getQS() throws PsseModelException {return _list.getQS(_ndx);}
	/** set MVAr set point (when regulating reactive power) */
	public void setQS(float mvar) throws PsseModelException {_list.setQS(_ndx, mvar);}

	@Override
	public void setInSvc(boolean state) throws PsseModelException {_list.setInSvc(_ndx, state);}
}
