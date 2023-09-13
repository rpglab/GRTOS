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

import com.powerdata.openpa.tools.AbstractBaseObject;
import com.powerdata.openpa.tools.BaseList;
import com.powerdata.openpa.tools.Complex;

/** Representation of in-service branches */

public class ACBranchList extends BaseList<ACBranch>
{
	public static final ACBranchList Empty = new ACBranchList()
	{
		@Override
		public int size() {return 0;}
	};

	class ACBranchObj extends AbstractBaseObject implements ACBranch
	{
		public ACBranchObj(int ndx) {super(ACBranchList.this, ndx);}
		@Override
		public String getI() throws PsseModelException {return ACBranchList.this.getI(_ndx);}
		@Override
		public String getJ() throws PsseModelException {return ACBranchList.this.getJ(_ndx);}
		@Override
		public Bus getFromBus() throws PsseModelException {return ACBranchList.this.getFromBus(_ndx);}
		@Override
		public Bus getToBus() throws PsseModelException {return ACBranchList.this.getToBus(_ndx);}
		@Override
		public float getR() throws PsseModelException {return ACBranchList.this.getR(_ndx);}
		@Override
		public float getX() throws PsseModelException {return ACBranchList.this.getX(_ndx);}
		@Override
		public Complex getZ() throws PsseModelException {return ACBranchList.this.getZ(_ndx);}
		@Override
		public Complex getY() throws PsseModelException {return ACBranchList.this.getY(_ndx);}
		@Override
		public float getFromTap() throws PsseModelException {return ACBranchList.this.getFromTap(_ndx);}
		@Override
		public float getToTap() throws PsseModelException {return ACBranchList.this.getToTap(_ndx);}
		@Override
		public float getPhaseShift() throws PsseModelException {return ACBranchList.this.getPhaseShift(_ndx);}
		@Override
		public boolean isInSvc() throws PsseModelException {return ACBranchList.this.isInSvc(_ndx);}
		@Override
		public float getGmag() throws PsseModelException {return ACBranchList.this.getGmag(_ndx);}
		@Override
		public float getBmag() throws PsseModelException {return ACBranchList.this.getBmag(_ndx);}
		@Override
		public float getFromBchg() throws PsseModelException {return ACBranchList.this.getFromBchg(_ndx);}
		@Override
		public float getToBchg() throws PsseModelException  {return ACBranchList.this.getToBchg(_ndx);}
		@Override
		public void setInSvc(boolean state) throws PsseModelException {ACBranchList.this.setInSvc(_ndx, state);}
		@Override
		public String getCKT() throws PsseModelException { return ACBranchList.this.getCKT(_ndx);}
	}
	
	int _nlines;
	int _ntransformers;
	int _size;
	LineList _lines;
	TransformerList _transformers;
	PhaseShifterList _phaseshifters;
	
	ACBranchList() {super();}

	public void setInSvc(int ndx, boolean state) throws PsseModelException {findBranch(ndx).setInSvc(state);}
	public float getR(int ndx) throws PsseModelException {return findBranch(ndx).getR();}
	public float getX(int ndx) throws PsseModelException {return findBranch(ndx).getX();}

	public String getI(int ndx) throws PsseModelException
	{
		return findBranch(ndx).getI();
	}
	public String getJ(int ndx) throws PsseModelException
	{
		return findBranch(ndx).getJ();
	}

	public ACBranchList(LineList l, TransformerList xf, PhaseShifterList ps)
			throws PsseModelException
	{
		_lines = l;
		_transformers = xf;
		_phaseshifters = ps;
		_nlines = _lines.size();
		_ntransformers = _transformers.size();
		_size = _nlines + _ntransformers + ps.size();
	}

	/* Standard object retrieval */
	@Override
	public ACBranch get(int ndx) { return new ACBranchObj(ndx); }
	@Override
	public ACBranch get(String id) { return super.get(id); }

	public Complex getZ(int ndx) throws PsseModelException {return findBranch(ndx).getZ();}
	public Complex getY(int ndx) throws PsseModelException {return findBranch(ndx).getY();}
	public Bus getToBus(int ndx) throws PsseModelException {return findBranch(ndx).getToBus();}
	public Bus getFromBus(int ndx) throws PsseModelException {return findBranch(ndx).getFromBus();}
	public float getPhaseShift(int ndx) throws PsseModelException {return findBranch(ndx).getPhaseShift();}
	public float getToTap(int ndx) throws PsseModelException {return findBranch(ndx).getToTap();}
	public float getFromTap(int ndx) throws PsseModelException {return findBranch(ndx).getFromTap();}
	public float getGmag(int ndx) throws PsseModelException {return findBranch(ndx).getGmag();}
	public float getBmag(int ndx) throws PsseModelException {return findBranch(ndx).getBmag();}
	public float getFromBchg(int ndx) throws PsseModelException {return findBranch(ndx).getFromBchg();}
	public float getToBchg(int ndx) throws PsseModelException {return findBranch(ndx).getToBchg();}
	public boolean isInSvc(int ndx) throws PsseModelException {return findBranch(ndx).isInSvc();}

	ACBranch findBranch(int ndx)
	{
		if (ndx < _nlines)
		{
			return _lines.get(ndx);
		}
		else if ((ndx-=_nlines) < _ntransformers)
		{
			return _transformers.get(ndx);
		}
		else return _phaseshifters.get(ndx-_ntransformers);
	}

	@Override
	public String getObjectID(int ndx) throws PsseModelException
	{
		return findBranch(ndx).getObjectID();
	}

	@Override
	public String getObjectName(int ndx) throws PsseModelException
	{
		return findBranch(ndx).getObjectName();
	}

	@Override
	public int size() {return _size;}
	
	
	public float getRateA(int ndx) throws PsseModelException
	{
		float rateA;
		if (ndx < _nlines) rateA = _lines.getRATEA(ndx);
		else if ((ndx-=_nlines) < _ntransformers) rateA = _transformers.getRATA1(ndx);
		else rateA = _phaseshifters.getRATA1(ndx-_ntransformers);
		return rateA;
	}
	
	public float getRateB(int ndx) throws PsseModelException
	{
		float rateB;
		if (ndx < _nlines) rateB = _lines.getRATEB(ndx);
		else if ((ndx-=_nlines) < _ntransformers) rateB = _transformers.getRATB1(ndx);
		else rateB = _phaseshifters.getRATB1(ndx-_ntransformers);
		return rateB;
	}

	public float getRateC(int ndx) throws PsseModelException
	{
		float rateC;
		if (ndx < _nlines) rateC = _lines.getRATEC(ndx);
		else if ((ndx-=_nlines) < _ntransformers) rateC = _transformers.getRATC1(ndx);
		else rateC = _phaseshifters.getRATC1(ndx-_ntransformers);
		return rateC;
	}

	
	public void setRateA(int ndx, float rata1) throws PsseModelException
	{
		if (ndx < _nlines) _lines.setRATEA(ndx, rata1);
		else if ((ndx-=_nlines) < _ntransformers) _transformers.setRATA1(ndx, rata1);
		else _phaseshifters.setRATA1(ndx-_ntransformers, rata1);
	}

	public void setRateB(int ndx, float ratb1) throws PsseModelException
	{
		if (ndx < _nlines) _lines.setRATEB(ndx, ratb1);
		else if ((ndx-=_nlines) < _ntransformers) _transformers.setRATB1(ndx, ratb1);
		else _phaseshifters.setRATB1(ndx-_ntransformers, ratb1);
	}

	public void setRateC(int ndx, float ratc1) throws PsseModelException
	{
		if (ndx < _nlines) _lines.setRATEC(ndx, ratc1);
		else if ((ndx-=_nlines) < _ntransformers) _transformers.setRATC1(ndx, ratc1);
		else _phaseshifters.setRATC1(ndx-_ntransformers, ratc1);
	}

	/** circuit identifier */
	public String getCKT(int ndx) throws PsseModelException {return findBranch(ndx).getCKT();}
	
	public int getNumLines() { return _nlines;}
	public int getNumTransformers() {return _ntransformers;}
	public int getNumPhaseShifter() {return (_size - _nlines - _ntransformers);}


}

