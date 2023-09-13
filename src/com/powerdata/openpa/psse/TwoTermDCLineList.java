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

import com.powerdata.openpa.psse.TwoTermDCLine.CtrlMode;
import com.powerdata.openpa.tools.PAMath;

public abstract class TwoTermDCLineList extends PsseBaseList<TwoTermDCLine>
{
	public static final TwoTermDCLineList Empty = new TwoTermDCLineList()
	{
		@Override
		public float getRDC(int ndx) throws PsseModelException {return 0f;}
		@Override
		public float getSETVL(int ndx) throws PsseModelException {return 0f;}
		@Override
		public float getVSCHD(int ndx) throws PsseModelException {return 0f;}
		@Override
		public String getIPR(int ndx) throws PsseModelException {return "";}
		@Override
		public int getNBR(int ndx) throws PsseModelException {return 0;}
		@Override
		public float getALFMX(int ndx) throws PsseModelException {return 0f;}
		@Override
		public float getALFMN(int ndx) throws PsseModelException {return 0f;}
		@Override
		public float getXCR(int ndx) throws PsseModelException {return 0f;}
		@Override
		public String getIPI(int ndx) throws PsseModelException {return "";}
		@Override
		public int getNBI(int ndx) throws PsseModelException  {return 0;}
		@Override
		public float getGAMMX(int ndx) throws PsseModelException {return 0f;}
		@Override
		public float getGAMMN(int ndx) throws PsseModelException {return 0f;}
		@Override
		public float getXCI(int ndx) throws PsseModelException {return 0f;}
		@Override
		public int getDCLineNum(int ndx) throws PsseModelException {return 0;}
		@Override
		public int size() {return 0;}
		@Override
		public String getObjectID(int ndx) throws PsseModelException {return null;}
		@Override
		public float getEBASR(int ndx) throws PsseModelException {return 0f;}
		@Override
		public float getEBASI(int ndx) throws PsseModelException {return 0f;}
	};

	protected TwoTermDCLineList() {super();}
	public TwoTermDCLineList(PsseModel model) {super(model);}
	
	
	/** Get a Two Terminal DC Line by it's index. */
	@Override
	public TwoTermDCLine get(int index) {return new TwoTermDCLine(this, index);}
	/** Get a Two Terminal DC Line by it's ID. */
	@Override
	public TwoTermDCLine get(String id) {return super.get(id);}

	public String getI(int ndx) throws PsseModelException {return getIPR(ndx);}
	public String getJ(int ndx) throws PsseModelException {return getIPI(ndx);}
	public Bus getFromBus(int ndx) throws PsseModelException {return _model.getBus(getIPR(ndx));}
	public Bus getToBus(int ndx) throws PsseModelException {return _model.getBus(getIPI(ndx));}
	public int getMDC(int ndx) throws PsseModelException {return 0;}
	public void setMDC(int ndx, int mdc) throws PsseModelException {}
	public CtrlMode getCtrlMode(int ndx) throws PsseModelException {return CtrlMode.fromCode(getMDC(ndx));}
	public void setCtrlMode(int ndx, CtrlMode cmode) throws PsseModelException {}
	public abstract float getRDC(int ndx) throws PsseModelException;
	public abstract float getSETVL(int ndx) throws PsseModelException;
	public void setSETVL(int ndx, float svl) throws PsseModelException {}
	public abstract float getVSCHD(int ndx) throws PsseModelException;
	public void setVSCHD(int ndx, float vdc) throws PsseModelException {}
	public float getVCMOD(int ndx) throws PsseModelException {return 0f;}
	public float getRCOMP(int ndx) throws PsseModelException {return 0f;}
	public float getDELTI(int ndx) throws PsseModelException {return 0f;}
	public float getDCVMIN(int ndx) throws PsseModelException {return 0f;}

	public abstract String getIPR(int ndx) throws PsseModelException;
	public abstract int getNBR(int ndx) throws PsseModelException;;
	public abstract float getALFMX(int ndx) throws PsseModelException;
	public abstract float getALFMN(int ndx) throws PsseModelException;
	public float getRCR(int ndx) throws PsseModelException {return 0f;}
	public abstract float getXCR(int ndx) throws PsseModelException;
	public String getICR(int ndx) throws PsseModelException {return "0";}
	public String getIFR(int ndx) throws PsseModelException {return "0";} 
	public String getITR(int ndx) throws PsseModelException {return "0";}
	public String getIDR(int ndx) throws PsseModelException {return "1";}
	public Transformer getTransformerR(int ndx) throws PsseModelException
	{
		// TODO Implement more reasonable default
		return null;
	}

	public float getXCAPR(int ndx) throws PsseModelException {return 0f;}
	public abstract String getIPI(int ndx) throws PsseModelException;
	public abstract int getNBI(int ndx) throws PsseModelException;
	public abstract float getGAMMX(int ndx) throws PsseModelException;
	public abstract float getGAMMN(int ndx) throws PsseModelException;
	public float getRCI(int ndx) throws PsseModelException {return 0f;}
	public abstract float getXCI(int ndx) throws PsseModelException;
	public String getICI(int ndx) throws PsseModelException {return "0";}
	public String getIFI(int ndx) throws PsseModelException {return "0";}
	public String getITI(int ndx) throws PsseModelException {return "0";}
	public Transformer getTransformerI(int ndx)
	{
		// TODO Implement more reasonable default
		return null;
	}

	public String getIDI(int ndx) throws PsseModelException {return "1";}
	public float getXCAPI(int ndx) throws PsseModelException {return 0f;}
	public abstract int getDCLineNum(int ndx) throws PsseModelException;

	public int getCCCITMX(int ndx) throws PsseModelException {return 20;}
	public float getCCCACC(int ndx) throws PsseModelException {return 1f;}
	public float getTRR(int ndx) throws PsseModelException {return 1f;}
	public float getTAPR(int ndx) throws PsseModelException {return 1f;}
	public float getTMXR(int ndx) throws PsseModelException {return 1.5f;}
	public float getTMNR(int ndx) throws PsseModelException {return 0.51f;}
	public float getSTPR(int ndx) throws PsseModelException {return .00625f;}
	public float getTRI(int ndx) throws PsseModelException {return 1f;}
	public float getTAPI(int ndx) throws PsseModelException {return 1f;}
	public float getTMXI(int ndx) throws PsseModelException {return 1.5f;}
	public float getTMNI(int ndx) throws PsseModelException {return 0.51f;}
	public float getSTPI(int ndx) throws PsseModelException {return .00625f;}
	public abstract float getEBASR(int ndx) throws PsseModelException;
	public abstract float getEBASI(int ndx) throws PsseModelException;
	public float getALFMXrad(int ndx) throws PsseModelException {return PAMath.deg2rad(getALFMX(ndx));}
	public float getALFMNrad(int ndx) throws PsseModelException {return PAMath.deg2rad(getALFMN(ndx));}
	public float getGAMMXrad(int ndx) throws PsseModelException {return PAMath.deg2rad(getGAMMX(ndx));}
	public float getGAMMNrad(int ndx) throws PsseModelException {return PAMath.deg2rad(getGAMMN(ndx));}
	public boolean isInSvc(int ndx) throws PsseModelException {return getMDC(ndx) != 0;}
	public void setInSvc(int ndx, boolean state) throws PsseModelException {}
}
