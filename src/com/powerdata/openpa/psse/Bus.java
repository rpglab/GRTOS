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

import com.powerdata.openpa.tools.Complex;

public class Bus extends PsseBaseObject
{
	protected BusList _list;
	
	public Bus(int ndx, BusList list)
	{
		super(list,ndx);
		_list = list;
	}

	/* Groups */
	public SwitchList getSwitches() throws PsseModelException { return _list.getSwitches(_ndx); }
	public GenList getGenerators() throws PsseModelException { return _list.getGenerators(_ndx); }
	public LoadList getLoads() throws PsseModelException { return _list.getLoads(_ndx); }
	public LineList getLines() throws PsseModelException { return _list.getLines(_ndx); }
	public TransformerList getTransformers() throws PsseModelException { return _list.getTransformers(_ndx); }
	public ShuntList getShunts() throws PsseModelException { return _list.getShunts(_ndx); }
	public SvcList getSvcs() throws PsseModelException { return _list.getSvcs(_ndx); }
	public PhaseShifterList getPhaseShifters() throws PsseModelException {return _list.getPhaseShifters(_ndx);}

	/* Convenience methods */
	/** request that this node be islolated */
	public SwitchList isolate() throws PsseModelException { return _list.isolate(_ndx); }
	/** enumerated IDE code */
	public BusTypeCode getBusType() throws PsseModelException {return _list.getBusType(_ndx);}
	/** Area */
	public Area getAreaObject() throws PsseModelException {return _list.getAreaObject(_ndx);}
	/** Zone */
	public Zone getZoneObject() throws PsseModelException {return _list.getZoneObject(_ndx);}
	/** Owner */
	public Owner getOwnerObject() throws PsseModelException {return _list.getOwnerObject(_ndx);}
	@Deprecated
	/* CMM - we actually expclitily model the shunt in the ShuntList, so we should not need this call */
	/** get complex shunt admittance to ground */
	public Complex getShuntY() throws PsseModelException {return _list.getShuntY(_ndx);}
	/** test if the bus is energized */
	public boolean isEnergized() throws PsseModelException { return _list.isEnergized(_ndx); }
	/** get frequency source priority */
	public int getFrequencySourcePriority() throws PsseModelException {return _list.getFrequencySourcePriority(_ndx);}
	/** get the island number */
	public int getIsland() throws PsseModelException { return _list.getIsland(_ndx); }
	/** get a station number if it exists or zero if not */
	public int getStation() throws PsseModelException {return _list.getStation(_ndx); }

	/* Raw PSS/e methods */
	
	/** Bus number */
	public int getI() throws PsseModelException {return _list.getI(_ndx);}
	/** Alphanumeric identifier */
	public String getNAME() throws PsseModelException {return _list.getNAME(_ndx);}
	/** Bus base voltage */
	public float getBASKV() throws PsseModelException {return _list.getBASKV(_ndx);}
	/** Bus type code */
	public int getIDE() throws PsseModelException {return _list.getIDE(_ndx);}
	@Deprecated
	/* CMM we explicitely model the shunt, so this should not be needed */
	/** Active component of shunt admittance to ground in MW at unity voltage*/
	public float getGL() throws PsseModelException {return _list.getGL(_ndx);}
	@Deprecated
	/* CMM we explicitely model the shunt, so this should not be needed */
	/** Reactive component of shunt admittance to ground in MVAr at unity voltage*/
	public float getBL() throws PsseModelException {return _list.getBL(_ndx);}
	/** Area number */
	public int getAREA() throws PsseModelException {return _list.getAREA(_ndx);}
	/** Zone number */
	public int getZONE() throws PsseModelException {return _list.getZONE(_ndx);}
	/** Bus voltage magnitude p.u.*/
	public float getVMpu() throws PsseModelException {return _list.getVMpu(_ndx);}
	/** Set Bus voltage magnitude p.u.*/
	public void setVMpu(float vm) throws PsseModelException {_list.setVMpu(_ndx, vm);}
	/** Bus voltage phase angle in degrees */
	public float getVA() throws PsseModelException {return _list.getVA(_ndx);}
	/** Set Bus voltage phase angle in degrees */
	public void setVA(float va) throws PsseModelException {_list.setVA(_ndx, va);}
	/** Bus voltage phase angle in radians */
	public float getVArad() throws PsseModelException {return _list.getVArad(_ndx);}
	/** Set Bus voltage phase angle in radians */
	public void setVArad(float va) throws PsseModelException {_list.setVArad(_ndx, va);}
	/** Owner number */
	public int getOWNER() throws PsseModelException {return _list.getOWNER(_ndx);}

	public float getFrequency() throws PsseModelException {return _list.getFrequency(_ndx);}
	
	/** set IDE code */
	public void setIDE(int type) throws PsseModelException { _list.setIDE(_ndx, type);}

}
