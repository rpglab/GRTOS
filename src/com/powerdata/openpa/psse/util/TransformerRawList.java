package com.powerdata.openpa.psse.util;

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

import com.powerdata.openpa.psse.Bus;
import com.powerdata.openpa.psse.OwnershipList;
import com.powerdata.openpa.psse.PsseBaseList;
import com.powerdata.openpa.psse.PsseModel;
import com.powerdata.openpa.psse.PsseModelException;
import com.powerdata.openpa.psse.TransformerCtrlMode;


public abstract class TransformerRawList extends PsseBaseList<TransformerRaw>
{
	public TransformerRawList(PsseModel model) throws PsseModelException
	{
		super(model);
	}
	
	/** Get a Bus by it's index. */
	@Override
	public TransformerRaw get(int ndx) { return new TransformerRaw(ndx,this); }
	/** Get a Bus by it's ID. */
	@Override
	public TransformerRaw get(String id) { return super.get(id); }

	
	public Bus getBusI(int ndx) throws PsseModelException {return _model.getBus(getI(ndx));}
	public Bus getBusJ(int ndx) throws PsseModelException {return _model.getBus(getJ(ndx));}
	public Bus getBusK(int ndx) throws PsseModelException {return _model.getBus(getK(ndx));}


	/* Line 1 */
	
	/** Get number or name of bus connected to first winding */
	public abstract String getI(int ndx) throws PsseModelException;
	/** Get number or name of bus connected to second winding */
	public abstract String getJ(int ndx) throws PsseModelException;
	/** If 3-winding, get number or name of bus connected to third winding */
	public String getK(int ndx) throws PsseModelException {return "0";}
	/** Get circuit ID (used to differentiate parallel branches) */
	public String getCKT(int ndx) throws PsseModelException {return "1";}
	/** Winding data I/O code */
	public int getCW(int ndx) throws PsseModelException {return 1;}
	/** Impedance data I/O code */
	public int getCZ(int ndx) throws PsseModelException {return 1;}
	/** Magnetizing admittance I/O code */
	public int getCM(int ndx) throws PsseModelException {return 1;}
	/** Magnetizing conductance */
	public float getMAG1(int ndx) throws PsseModelException {return 0f;}
	/** Magnetizing susceptance */
	public float getMAG2(int ndx) throws PsseModelException {return 0f;}
	/** Non-metered end code */
	public int getNMETR(int ndx) throws PsseModelException {return 2;}
	/** Transformer Name */
	public String getNAME(int ndx) throws PsseModelException {return "";}
	/** Initial transformer status */
	public int getSTAT(int ndx) throws PsseModelException {return 1;}
	/** return Ownership as a list */
	public OwnershipList getOwnership(int ndx) throws PsseModelException {return OwnershipList.Empty;}
	
	/* line 2 */
	
	/** resistance between first and second windings */
	public float getR1_2(int ndx) throws PsseModelException {return 0f;}
	/** reactance between first and second windings */
	public abstract float getX1_2(int ndx) throws PsseModelException;
	/** Winding 1 to 2 base MVA */
	public float getSBASE1_2(int ndx) throws PsseModelException {return _model.getSBASE();}
	/** resistance between second and third windings */
	public float getR2_3(int ndx) throws PsseModelException {return 0f;}
	/** reactance between second and third windings */
	public abstract float getX2_3(int ndx) throws PsseModelException;
	/** Winding 2 to 3 base MVA */
	public float getSBASE2_3(int ndx) throws PsseModelException {return _model.getSBASE();}
	/** resistance between third and first windings */
	public float getR3_1(int ndx) throws PsseModelException {return 0f;}
	/** reactance between third and first windings */
	public abstract float getX3_1(int ndx) throws PsseModelException;
	/** Winding 3 to 1 base MVA */
	public float getSBASE3_1(int ndx) throws PsseModelException {return _model.getSBASE();}
	/** Star node voltage magnitude */
	public float getVMSTAR(int ndx) throws PsseModelException {return 1f;}
	/** Star node voltage angle */
	public float getANSTAR(int ndx) throws PsseModelException {return 0f;}

	/* line 3 */
	
	/** Winding 1 off-nominal turns ratio */
	public float getWINDV1(int ndx) throws PsseModelException {return ((getCW(ndx)==2)?_model.getBus(getI(ndx)).getBASKV():1f);}
	/** Winding 1 nominal voltage */
	public float getNOMV1(int ndx) throws PsseModelException {return _model.getBus(getI(ndx)).getBASKV();}
	/** Winding 1 phase shift angle (degrees) */
	public float getANG1(int ndx) throws PsseModelException {return 0f;}
	/** Winding 1 first rating */
	public float getRATA1(int ndx) throws PsseModelException {return 0f;}
	/** Winding 1 second rating */
	public float getRATB1(int ndx) throws PsseModelException {return 0f;}
	/** Winding 1 third rating */
	public float getRATC1(int ndx) throws PsseModelException {return 0f;}
	/** Transformer control mode of winding 1 tap*/
	public int getCOD1(int ndx) throws PsseModelException {return 0;}
	/** Number or name of winding 1 voltage controlled bus */
	public String getCONT1(int ndx) throws PsseModelException {return "0";}
	/** Winding 1 tap maximum limit */
	public float getRMA1(int ndx) throws PsseModelException
	{
		int cod = Math.abs(getCOD1(ndx));
		int cw = getCW(ndx);
		if (cod == 3)
		{
			return 180f;
		}
		else if (cod < 3 && cw == 2)
		{
			return 1.1f * _model.getBus(getI(ndx)).getBASKV();
		}
		return 1.1f;
	}
	/** Winding 1 tap minimum limit */
	public float getRMI1(int ndx) throws PsseModelException
	{
		int cod = Math.abs(getCOD1(ndx));
		int cw = getCW(ndx);
		if (cod == 3)
		{
			return -180f;
		}
		else if (cod < 3 && cw == 2)
		{
			return 0.9f * _model.getBus(getI(ndx)).getBASKV();
		}
		return 0.9f;
	}
	/** Winding 1 band control maximum limit */
	public float getVMA1(int ndx) throws PsseModelException
	{
		int cod = Math.abs(getCOD1(ndx));
		if (cod == 2 || cod == 3)
			return 99999f;
		return 1.1f;
	}
	/** Winding 1 band control minimum limit */
	public float getVMI1(int ndx) throws PsseModelException
	{
		int cod = Math.abs(getCOD1(ndx));
		if (cod == 2 || cod == 3)
			return -99999f;
		return 0.9f;
	}
	/** Winding 1 tap available positions */
	public int getNTP1(int ndx) throws PsseModelException {return 33;}
	/** Winding 1 impedance correction table index */
	public int getTAB1(int ndx) throws PsseModelException {return 0;}
	/** Load drop compensation resistance */
	public float getCR1(int ndx) throws PsseModelException {return 0f;}
	/** Load drop compensation reactance */
	public float getCX1(int ndx) throws PsseModelException {return 0f;}
	
	/* line 4 */
	
	/** Winding 2 off-nominal turns ratio */
	public float getWINDV2(int ndx) throws PsseModelException {return ((getCW(ndx)==2)?_model.getBus(getJ(ndx)).getBASKV():1f);}
	/** Winding 2 nominal voltage */
	public float getNOMV2(int ndx) throws PsseModelException {return _model.getBus(getJ(ndx)).getBASKV();}
	/** Winding 2 phase shift angle (degrees) */
	public float getANG2(int ndx) throws PsseModelException {return 0f;}
	/** Winding 2 first rating */
	public float getRATA2(int ndx) throws PsseModelException {return 0f;}
	/** Winding 2 second rating */
	public float getRATB2(int ndx) throws PsseModelException {return 0f;}
	/** Winding 2 third rating */
	public float getRATC2(int ndx) throws PsseModelException {return 0f;}
	/** Transformer control mode of Winding 2 tap*/
	public int getCOD2(int ndx) throws PsseModelException {return 0;}
	/** Number or name of Winding 2 voltage controlled bus */
	public String getCONT2(int ndx) throws PsseModelException {return "0";}
	/** Winding 2 tap maximum limit */
	public float getRMA2(int ndx) throws PsseModelException
	{
		int cod = Math.abs(getCOD2(ndx));
		int cw = getCW(ndx);
		if (cod == 3)
		{
			return 180f;
		}
		else if (cod < 3 && cw == 2)
		{
			return 1.1f * _model.getBus(getJ(ndx)).getBASKV();
		}
		return 1.1f;
	}
	/** Winding 2 tap minimum limit */
	public float getRMI2(int ndx) throws PsseModelException
	{
		int cod = Math.abs(getCOD2(ndx));
		int cw = getCW(ndx);
		if (cod == 3)
		{
			return -180f;
		}
		else if (cod < 3 && cw == 2)
		{
			return 0.9f * _model.getBus(getJ(ndx)).getBASKV();
		}
		return 0.9f;
	}
	/** Winding 2 band control maximum limit */
	public float getVMA2(int ndx) throws PsseModelException
	{
		int cod = Math.abs(getCOD2(ndx));
		if (cod == 2 || cod == 3)
			return 99999f;
		return 1.1f;
	}
	/** Winding 2 band control minimum limit */
	public float getVMI2(int ndx) throws PsseModelException
	{
		int cod = Math.abs(getCOD2(ndx));
		if (cod == 2 || cod == 3)
			return -99999f;
		return 0.9f;
	}
	/** Winding 2 tap available positions */
	public int getNTP2(int ndx) throws PsseModelException {return 33;}
	/** Winding 2 impedance correction table index */
	public int getTAB2(int ndx) throws PsseModelException {return 0;}
	/** Load drop compensation resistance */
	public float getCR2(int ndx) throws PsseModelException {return 0f;}
	/** Load drop compensation reactance */
	public float getCX2(int ndx) throws PsseModelException {return 0f;}


	/* line 5 */
	
	/** Winding 3 off-nominal turns ratio */
	public float getWINDV3(int ndx) throws PsseModelException
	{
		String k = getK(ndx);
		return ((getCW(ndx) == 2 && !k.isEmpty() && !k.equals("0")) ? _model.getBus(getK(ndx)).getBASKV() : 1f);
	}

	/** Winding 3 nominal voltage */
	public float getNOMV3(int ndx) throws PsseModelException {return _model.getBus(getK(ndx)).getBASKV();}
	/** Winding 3 phase shift angle (degrees) */
	public float getANG3(int ndx) throws PsseModelException {return 0f;}
	/** Winding 3 first rating */
	public float getRATA3(int ndx) throws PsseModelException {return 0f;}
	/** Winding 3 second rating */
	public float getRATB3(int ndx) throws PsseModelException {return 0f;}
	/** Winding 3 third rating */
	public float getRATC3(int ndx) throws PsseModelException {return 0f;}
	/** Transformer control mode of Winding 3 tap*/
	public int getCOD3(int ndx) throws PsseModelException {return 0;}
	/** Number or name of Winding 3 voltage controlled bus */
	public String getCONT3(int ndx) throws PsseModelException {return "0";}
	/** Winding 3 tap maximum limit */
	public float getRMA3(int ndx) throws PsseModelException
	{
		int cod = Math.abs(getCOD3(ndx));
		int cw = getCW(ndx);
		String k = getK(ndx);
		if (cod == 3)
		{
			return 180f;
		}
		else if (cod < 3 && cw == 2 && !k.isEmpty() && !k.equals("0"))
		{
			return 1.1f * _model.getBus(getK(ndx)).getBASKV();
		}
		return 1.1f;
	}
	/** Winding 3 tap minimum limit */
	public float getRMI3(int ndx) throws PsseModelException
	{
		int cod = Math.abs(getCOD3(ndx));
		int cw = getCW(ndx);
		String k = getK(ndx);
		if (cod == 3)
		{
			return -180f;
		}
		else if (cod < 3 && cw == 2 && !k.isEmpty() && !k.equals("0"))
		{
			return 0.9f * _model.getBus(getK(ndx)).getBASKV();
		}
		return 0.9f;
	}
	/** Winding 3 band control maximum limit */
	public float getVMA3(int ndx) throws PsseModelException
	{
		int cod = Math.abs(getCOD3(ndx));
		if (cod == 2 || cod == 3)
			return 99999f;
		return 1.1f;
	}
	/** Winding 3 band control minimum limit */
	public float getVMI3(int ndx) throws PsseModelException
	{
		int cod = Math.abs(getCOD3(ndx));
		if (cod == 2 || cod == 3)
			return -99999f;
		return 0.9f;
	}
	/** Winding 3 tap available positions */
	public int getNTP3(int ndx) throws PsseModelException {return 33;}
	/** Winding 3 impedance correction table index */
	public int getTAB3(int ndx) throws PsseModelException {return 0;}
	/** Load drop compensation resistance */
	public float getCR3(int ndx) throws PsseModelException {return 0f;}
	/** Load drop compensation reactance */
	public float getCX3(int ndx) throws PsseModelException {return 0f;}

	public TransformerCtrlMode getCtrlMode1(int ndx) throws PsseModelException {return TransformerCtrlMode.fromCode(getCOD1(ndx));}
	public TransformerCtrlMode getCtrlMode2(int ndx) throws PsseModelException {return TransformerCtrlMode.fromCode(getCOD2(ndx));}
	public TransformerCtrlMode getCtrlMode3(int ndx) throws PsseModelException {return TransformerCtrlMode.fromCode(getCOD3(ndx));}
}
