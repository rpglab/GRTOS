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

public abstract class AreaList extends PsseBaseList<Area>
{
	public static final AreaList Empty = new AreaList()
	{
		@Override
		public int getI(int ndx) throws PsseModelException {return 0;}
		@Override
		public String getObjectID(int ndx) throws PsseModelException {return null;}
		@Override
		public int size() {return 0;}
	};
	
	
	public AreaList() {super();}
	public AreaList(PsseModel model) {super(model);}

	/* Standard object retrieval */
	/** Get an AreaInterchange by it's index. */
	@Override
	public Area get(int ndx) { return new Area(ndx,this); }
	/** Get an AreaInterchange by it's ID. */
	@Override
	public Area get(String id) { return super.get(id); }
	
	/* Convenience methods */
	/** Area slack bus for area interchange control */ 
	public Bus getSlackBus(int ndx) throws PsseModelException
	{
		return _model.getBus(getISW(ndx));
	}
	/** Desired net interchange (PDES) leaving the area entered p.u. */
	public float getIntExport(int ndx) throws PsseModelException {return PAMath.mw2pu(getPDES(ndx));}
	/** Interchange tolerance bandwidth (PTOL) in p.u. */
	public float getIntTol(int ndx) throws PsseModelException {return PAMath.mw2pu(getPTOL(ndx));}
	
	/* Raw values */
	/** Area number */
	public abstract int getI(int ndx) throws PsseModelException;
	/** Area slack bus for area interchange control */
	public String getISW(int ndx) throws PsseModelException {return "0";}
	/** Desired net interchange leaving the area entered in MW */
	public String getARNAME(int ndx) throws PsseModelException {return "";}
	/** Interchange tolerance bandwidth entered in MW */
	public float getPDES(int ndx) throws PsseModelException {return 0F;}
	/** Alphanumeric identifier assigned to area */
	public float getPTOL(int ndx) throws PsseModelException {return 10F;}

}
