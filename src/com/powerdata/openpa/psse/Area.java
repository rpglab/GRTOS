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

public class Area extends PsseBaseObject
{
	protected AreaList _list;
	
	public Area(int ndx, AreaList list)
	{
		super(list,ndx);
		_list = list;
	}

	@Override
	public String getDebugName() throws PsseModelException {return getARNAME();}

	/* convenience methods */
	
	/** Area slack bus for area interchange control */ 
	public Bus getSlackBus() throws PsseModelException {return _list.getSlackBus(_ndx);}
	/** Desired net interchange (PDES) leaving the area entered p.u. */
	public float getIntExport()  throws PsseModelException {return _list.getIntExport(_ndx);}
	/** Interchange tolerance bandwidth (PTOL) in p.u. */
	public float getIntTol() throws PsseModelException {return _list.getIntTol(_ndx);}

	/* raw PSS/e methods */
	
	/** Area number */
	public int getI() throws PsseModelException {return _list.getI(_ndx);}
	/** Area slack bus for area interchange control */
	public String getISW() throws PsseModelException {return _list.getISW(_ndx);}
	/** Desired net interchange leaving the area entered in MW */
	public float getPDES() throws PsseModelException {return _list.getPDES(_ndx);}
	/** Interchange tolerance bandwidth entered in MW */
	public float getPTOL() throws PsseModelException {return _list.getPTOL(_ndx);}
	/** Alphanumeric identifier assigned to area */
	public String getARNAME() throws PsseModelException {return _list.getARNAME(_ndx);}

}
