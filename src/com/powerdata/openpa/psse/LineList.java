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
import com.powerdata.openpa.tools.PAMath;

public abstract class LineList extends PsseBaseList<Line>
{
	public static final LineList Empty = new LineList()
	{
		@Override
		public String getI(int ndx) throws PsseModelException {return null;}
		@Override
		public String getJ(int ndx) throws PsseModelException {return null;}
		@Override
		public float getX(int ndx) throws PsseModelException {return 0f;}
		@Override
		public String getObjectID(int ndx) throws PsseModelException {return null;}
		@Override
		public int size() {return 0;}
	};
	
	protected LineList() {super();}
	public LineList(PsseModel model) {super(model);}

	/* Standard object retrieval */

	/** Get a NontransformerBranch by it's index. */
	@Override
	public Line get(int ndx) { return new Line(ndx,this); }
	/** Get a NontransformerBranch by it's ID. */
	@Override
	public Line get(String id) { return super.get(id); }

	
	/** From-side bus */
	public Bus getFromBus(int ndx) throws PsseModelException {return _model.getBus(getI(ndx));}
	/** To-side bus */
	public Bus getToBus(int ndx)  throws PsseModelException {return _model.getBus(getJ(ndx));}
	/** Get "metered" end */
	public LineMeterEnd getMeteredEnd(int ndx) throws PsseModelException {return LineMeterEnd.Unknown;}
	/** get initial branch status (ST) as a boolean.  Returns true if in service */
	public boolean isInSvc(int ndx) throws PsseModelException {return getST(ndx) == 1;}
	public void setInSvc(int ndx, boolean state) throws PsseModelException {}
	/** get complex impedance */
	public Complex getZ(int ndx) throws PsseModelException
	{
		return PAMath.rebaseZ100(new Complex(getR(ndx), getX(ndx)),
				_model.getSBASE());
	}
	public Complex getY(int ndx) throws PsseModelException {return getZ(ndx).inv();}
	public float getFromBchg(int ndx) throws PsseModelException {return getB(ndx)/2f;}
	public float getToBchg(int ndx) throws PsseModelException {return getB(ndx)/2f;}
	@Override
	public String getObjectName(int ndx) throws PsseModelException
	{
		return getFromBus(ndx).getObjectName()+"-"+getToBus(ndx).getObjectName()+":"+getCKT(ndx);
	}

	/* raw PSS/e methods */
	/** From-side bus number or name */
	public abstract String getI(int ndx) throws PsseModelException;
	/** To-side bus number or name */
	public abstract String getJ(int ndx) throws PsseModelException;
	/** circuit identifier */
	public String getCKT(int ndx) throws PsseModelException {return "1";}
	/** Branch resistance entered in p.u. */
	public float getR(int ndx) throws PsseModelException {return 0f;}
	/** Branch reactance entered in p.u. */
	public abstract float getX(int ndx) throws PsseModelException;
	/** Branch charging susceptance entered in p.u. */
	public float getB(int ndx) throws PsseModelException {return 0f;}
	/** First loading rating entered in MVA */
	public float getRATEA(int ndx) throws PsseModelException {return 0f;}
	/** Second loading rating entered in MVA */
	public float getRATEB(int ndx) throws PsseModelException {return 0f;}
	/** Third loading rating entered in MVA */
	public float getRATEC(int ndx) throws PsseModelException {return 0f;}
	/** conductance of line shunt at bus "I" */ 
	public float getGI(int ndx) throws PsseModelException {return 0f;}
	/** susceptance of line shunt at bus "I" */ 
	public float getBI(int ndx) throws PsseModelException {return 0f;}
	/** conductance of line shunt at bus "J" */ 
	public float getGJ(int ndx) throws PsseModelException {return 0f;}
	/** susceptance of line shunt at bus "J" */ 
	public float getBJ(int ndx) throws PsseModelException {return 0f;}
	/** Initial branch status (1 is in-service, 0 means out of service) */
	public int getST(int ndx) throws PsseModelException {return 1;}
	/** Line length  entered in user-selected units */
	public float getLEN(int ndx) throws PsseModelException {return 0f;}
	/** return Ownership as a list */
	public OwnershipList getOwnership(int ndx) throws PsseModelException {return OwnershipList.Empty;} //TODO: implement

	/* realtime fields */

	public float getMVA(int ndx) throws PsseModelException { return 0.0f; }
	public float getMVAPercent(int ndx) throws PsseModelException { return 0.0f; }
	
	
	public float[] getRATEA() {	return null; }
	public float[] getRATEB() {	return null; }
	public float[] getRATEC() {	return null; }
	
	/** Set first loading rating in MVA */
	public void setRATEA(int ndx, float rateA) throws PsseModelException {}
	/** Set second loading rating entered in MVA */
	public void setRATEB(int ndx, float rateB) throws PsseModelException {}
	/** Set third loading rating entered in MVA */
	public void setRATEC(int ndx, float rateC) throws PsseModelException {}
	
	/** Set reactance value in per unit */
	public void setX(int ndx, float x) throws PsseModelException {}


}	
