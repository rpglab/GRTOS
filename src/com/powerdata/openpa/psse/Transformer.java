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

/**
 * View all transformers as 2-winding.
 * 
 * @author chris@powerdata.com
 *
 */
public class Transformer extends PsseBaseObject implements ACBranch
{
	protected TransformerList _list;
	
	public Transformer(int ndx, TransformerList list)
	{
		super(list,ndx);
		_list = list;
	}

	/* Convenience methods */

	/** Winding 1 bus */ 
	public Bus getFromBus() throws PsseModelException {return _list.getFromBus(_ndx);}
	/** Winding 2 bus */
	public Bus getToBus() throws PsseModelException {return _list.getToBus(_ndx);}
	@Override
	public float getR() throws PsseModelException {return _list.getR(_ndx);}
	@Override
	public float getX() throws PsseModelException {return _list.getX(_ndx);}
	@Override
	public Complex getZ() throws PsseModelException {return _list.getZ(_ndx);}
	@Override
	public Complex getY() throws PsseModelException {return _list.getY(_ndx);}
	@Override
	public float getFromTap() throws PsseModelException {return _list.getFromTap(_ndx);}
	@Override
	public float getToTap() throws PsseModelException {return _list.getToTap(_ndx);}
	@Override
	public float getPhaseShift() throws PsseModelException {return _list.getPhaseShift(_ndx);}
	@Override
	public boolean isInSvc() throws PsseModelException {return _list.isInSvc(_ndx);}
	@Override
	public void setInSvc(boolean state) throws PsseModelException {_list.setInSvc(_ndx, state);}
	/** get the transformer control mode */
	public TransformerCtrlMode getCtrlMode() throws PsseModelException {return _list.getCtrlMode(_ndx);}
	public boolean getRegStat() throws PsseModelException {return _list.getRegStat(_ndx);}
	public void setRegStat(boolean stat) throws PsseModelException {_list.setRegStat(_ndx, stat);}

	
	
	/* RAW methods */
	/** Winding 1 bus number or name */ 
	public String getI() throws PsseModelException {return _list.getI(_ndx);}
	/** Winding 2 bus number or name */ 
	public String getJ() throws PsseModelException {return _list.getJ(_ndx);}
	/** circuit identifier */ 
	public String getCKT() throws PsseModelException {return _list.getCKT(_ndx);}
	/** Winding data I/O code */ 
	public int getCW() throws PsseModelException {return _list.getCW(_ndx);}
	/** Impedance data I/O code */
	public int getCZ() throws PsseModelException {return _list.getCZ(_ndx);}
	/** Magnetizing admittance I/O code */
	public int getCM() throws PsseModelException {return _list.getCM(_ndx);}
	/** Magnetizing conductance */
	public float getMAG1() throws PsseModelException {return _list.getMAG1(_ndx);}
	/** Magnetizing susceptance */
	public float getMAG2() throws PsseModelException {return _list.getMAG2(_ndx);}
	/** Nonmetered end code */
	public int getNMETR() throws PsseModelException {return _list.getNMETR(_ndx);}
	/** Name */
	public String getNAME() throws PsseModelException {return _list.getNAME(_ndx);}
	/** Initial Transformer status */
	public int getSTAT() throws PsseModelException {return _list.getSTAT(_ndx);}
	/** Measured resistance between winding 1 and winding 2 busses */
	public float getR1_2() throws PsseModelException {return _list.getR1_2(_ndx);}
	/** Measured reactance between winding 1 and winding 2 busses */
	public float getX1_2() throws PsseModelException {return _list.getX1_2(_ndx);}
	/** get winding 1-2 base MVA */
	public float getSBASE1_2() throws PsseModelException {return _list.getSBASE1_2(_ndx);}
	/** winding 1 off-nominal turns ratio */
	public float getWINDV1() throws PsseModelException {return _list.getWINDV1(_ndx);}
	/** nominal winding 1 voltage in kV */
	public float getNOMV1() throws PsseModelException {return _list.getNOMV1(_ndx);}
	/** winding 1 phase shift (DEG) */
	public float getANG1() throws PsseModelException {return _list.getANG1(_ndx);}
	/** winding 1 rating A in MVA */
	public float getRATA1() throws PsseModelException {return _list.getRATA1(_ndx);}
	/** winding 1 rating B in MVA */
	public float getRATB1() throws PsseModelException {return _list.getRATB1(_ndx);}
	/** winding 1 rating C in MVA */
	public float getRATC1() throws PsseModelException {return _list.getRATC1(_ndx);}
	/** Transformer control mode */
	public int getCOD1() throws PsseModelException {return _list.getCOD1(_ndx);}
	/** controlled bus */
	public String getCONT1() throws PsseModelException {return _list.getCONT1(_ndx);}
	/** RMA upper limit*/
	public float getRMA1() throws PsseModelException {return _list.getRMA1(_ndx);}
	/** RMI lower limit */
	public float getRMI1() throws PsseModelException {return _list.getRMI1(_ndx);}
	/** VMA upper limit */
	public float getVMA1() throws PsseModelException {return _list.getVMA1(_ndx);}
	/** VMI lower limit */
	public float getVMI1() throws PsseModelException {return _list.getVMI1(_ndx);}
	/** number of taps positions available */
	public int getNTP1() throws PsseModelException {return _list.getNTP1(_ndx);}
	/** transformer impedance correction table */
	public int getTAB1() throws PsseModelException {return _list.getTAB1(_ndx);}
	/** load drop compensation resistance in pu on system base */
	public float getCR1() throws PsseModelException {return _list.getCR1(_ndx);}
	/** load drop compensation reactance in pu on system base */
	public float getCX1() throws PsseModelException {return _list.getCX1(_ndx);}
	/** return Ownership as a list */
	public OwnershipList getOwnership() throws PsseModelException {return _list.getOwnership(_ndx);}

	/** winding 2 off-nominal turns ratio */
	public float getWINDV2() throws PsseModelException {return _list.getWINDV2(_ndx);}
	/** nominal winding 2 voltage in kV */
	public float getNOMV2() throws PsseModelException {return _list.getNOMV2(_ndx);}
	/** RMA upper limit*/
	public float getRMA2() throws PsseModelException {return _list.getRMA2(_ndx);}
	/** RMI lower limit */
	public float getRMI2() throws PsseModelException {return _list.getRMI2(_ndx);}
	/** number of taps positions available */
	public int getNTP2() throws PsseModelException {return _list.getNTP2(_ndx);}

	@Override
	public float getGmag() throws PsseModelException {return _list.getGmag(_ndx);}
	@Override
	public float getBmag() throws PsseModelException {return _list.getBmag(_ndx);}
	@Override
	public float getFromBchg() throws PsseModelException {return 0f;}
	@Override
	public float getToBchg() throws PsseModelException  {return 0f;}
}
