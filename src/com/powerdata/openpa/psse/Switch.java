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

public class Switch extends PsseBaseObject implements ACBranch
{
	protected SwitchList _list;
	
	public Switch(int ndx, SwitchList list)
	{
		super(list,ndx);
		_list = list;
	}

	@Override
	public String getDebugName() throws PsseModelException {return "Switch "+getName();}

	@Override
	public Bus getFromBus() throws PsseModelException {return _list.getFromBus(_ndx);}
	@Override
	public Bus getToBus() throws PsseModelException {return _list.getToBus(_ndx);}
	@Deprecated // use getObjectName()
	public String getName() throws PsseModelException {return _list.getName(_ndx);}
	public SwitchState getState() throws PsseModelException {return _list.getState(_ndx);}
	public void setState(SwitchState state) throws PsseModelException { _list.setState(_ndx,state); }
	public boolean canOperateUnderLoad() throws PsseModelException {return _list.canOperateUnderLoad(_ndx); }

	@Override
	public String getI() throws PsseModelException {return _list.getI(_ndx);}
	@Override
	public String getJ() throws PsseModelException {return _list.getJ(_ndx);}

	@Override
	public float getR() throws PsseModelException {return 0;}
	@Override
	public float getX() throws PsseModelException {return 0;}
	@Override
	public Complex getZ() throws PsseModelException {return Complex.Zero;}
	@Override
	public Complex getY() throws PsseModelException {return Complex.Zero;}
	@Override
	public float getFromTap() throws PsseModelException {return 1;}
	@Override
	public float getToTap() throws PsseModelException {return 1;}
	@Override
	public float getGmag() throws PsseModelException {return 0;}
	@Override
	public float getBmag() throws PsseModelException {return 0;}
	@Override
	public float getFromBchg() throws PsseModelException {return 0;}
	@Override
	public float getToBchg() throws PsseModelException {return 0;}
	@Override
	public float getPhaseShift() throws PsseModelException {return 0;}
	@Override
	public boolean isInSvc() throws PsseModelException {return _list.isInSvc(_ndx);}
	@Override
	public void setInSvc(boolean state) throws PsseModelException {_list.setInSvc(_ndx, state);}

	@Override
	public String getCKT() throws PsseModelException { return null;}
}