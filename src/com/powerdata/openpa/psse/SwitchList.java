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

public abstract class SwitchList extends PsseBaseList<Switch>
{
	public static final SwitchList Empty = new SwitchList()
	{
		@Override
		public Bus getFromBus(int ndx) throws PsseModelException {return null;}
		@Override
		public Bus getToBus(int ndx) throws PsseModelException {return null;}
		@Override
		public String getObjectID(int ndx) throws PsseModelException {return null;}
		@Override
		public int size() {return 0;}
	};
	protected SwitchList(){super();}
	public SwitchList(PsseModel model) {super(model);}

	/** Get a Switch by it's index. */
	@Override
	public Switch get(int ndx) { return new Switch(ndx,this); }
	/** Get an SwitchIn by it's ID. */
	@Override
	public Switch get(String id) { return super.get(id); }

	public abstract Bus getFromBus(int ndx) throws PsseModelException;
	public abstract Bus getToBus(int ndx) throws PsseModelException;
	@Deprecated // use getObjectName, this is redundant
	public String getName(int ndx) throws PsseModelException {return "";}
	public SwitchState getState(int ndx) throws PsseModelException {return SwitchState.Closed;}
	public void setState(int ndx, SwitchState state) throws PsseModelException {}
	public boolean canOperateUnderLoad(int ndx) throws PsseModelException {return true; }
	public String getI(int ndx) throws PsseModelException {return getFromBus(ndx).getObjectID();}
	public String getJ(int ndx) throws PsseModelException {return getToBus(ndx).getObjectID();}
	public boolean isInSvc(int ndx) throws PsseModelException {return true;}
	public void setInSvc(int ndx, boolean state) throws PsseModelException {}
}
