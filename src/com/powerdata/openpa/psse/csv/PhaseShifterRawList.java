package com.powerdata.openpa.psse.csv;

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

import com.powerdata.openpa.psse.PsseModelException;
import com.powerdata.openpa.tools.ComplexList;

public class PhaseShifterRawList extends com.powerdata.openpa.psse.PhaseShifterList
{
	int _size;
	PsseRawModel _eq;

	/* line 1 */
	private String[] _ckt, _name, _i, _j;
	private int[] _cw, _cm, _nmetr, _stat;
	private float[] _mag1, _mag2;
	
	/* line 2 */
	private ComplexList _z;
	private float[] _sbase;

	/* line 3 */
	private float[]		_windv1, _nomv1, _ang1, _rata1, _ratb1, _ratc1, _rma1,
			_rmi1, _vma1, _vmi1, _cr1, _cx1;
	private int[]		_cod1, _ntp1, _tab1;
	
	ComplexList _fs, _ts;
	
	public PhaseShifterRawList(PsseRawModel model, Transformer3RawList rlist,
			TransformerPrep prep) throws PsseModelException
	{
		super(model);
		_eq = model;

		_size = prep.size();
		
		int[] xfndx = prep.getXfRaw(), wndx = prep.getWndx();
		_i = prep.getBusI();
		_j = prep.getBusJ();
		_z = prep.getZ();
		
		_ckt = (String[]) Transformer3RawList.loadArray(rlist, xfndx, "CKT", String.class);
		_name = (String[]) Transformer3RawList.loadArray(rlist, xfndx, "NAME", String.class);
		_cw = (int[]) Transformer3RawList.loadArray(rlist, xfndx, "CW", int.class);
		_cm = (int[]) Transformer3RawList.loadArray(rlist, xfndx, "CM", int.class);
		_nmetr = Transformer3RawList.loadNmetr(rlist, xfndx, wndx);
		_stat = Transformer3RawList.loadStat(rlist, xfndx, wndx);
		float[][] tmag = Transformer3RawList.loadMag(rlist, xfndx, wndx);
		_mag1 = tmag[0];
		_mag2 = tmag[1];
		_sbase = Transformer3RawList.loadSbase(rlist, xfndx, wndx);

		_windv1 = (float[]) new WndLoader("WINDV").load(rlist, xfndx, wndx, float.class);
		_nomv1 = (float[]) new WndLoader("NOMV").load(rlist, xfndx, wndx, float.class);
		_ang1 = (float[]) new WndLoader("ANG").load(rlist, xfndx, wndx, float.class);
		_rata1 = (float[]) new WndLoader("RATA").load(rlist, xfndx, wndx, float.class);
		_ratb1 = (float[]) new WndLoader("RATB").load(rlist, xfndx, wndx, float.class);
		_ratc1 = (float[]) new WndLoader("RATC").load(rlist, xfndx, wndx, float.class);
		_cod1 = (int[]) new WndLoader("COD").load(rlist, xfndx, wndx, int.class);
		_rma1 = (float[]) new WndLoader("RMA").load(rlist, xfndx, wndx, float.class);
		_rmi1 = (float[]) new WndLoader("RMI").load(rlist, xfndx, wndx, float.class);
		_vma1 = (float[]) new WndLoader("VMA").load(rlist, xfndx, wndx, float.class);
		_vmi1 = (float[]) new WndLoader("VMI").load(rlist, xfndx, wndx, float.class);
		_ntp1 = (int[]) new WndLoader("NTP").load(rlist, xfndx, wndx, int.class);
		_tab1 = (int[]) new WndLoader("TAB").load(rlist, xfndx, wndx, int.class);
		_cr1 = (float[]) new WndLoader("CR").load(rlist, xfndx, wndx, float.class);
		_cx1 = (float[]) new WndLoader("CX").load(rlist, xfndx, wndx, float.class);
		
		_fs = new ComplexList(_size, true);
		_ts = new ComplexList(_size, true);
	}

	@Override
	public String getI(int ndx) throws PsseModelException {return _i[ndx];}
	@Override
	public String getJ(int ndx) throws PsseModelException {return _j[ndx];}

	@Override
	public String getCKT(int ndx) throws PsseModelException {return _ckt[ndx];}
	@Override
	public int getCW(int ndx) throws PsseModelException {return _cw[ndx];}
	@Override
	public int getCM(int ndx) throws PsseModelException {return _cm[ndx];}
	@Override
	public float getMAG1(int ndx) throws PsseModelException {return _mag1[ndx];}
	@Override
	public float getMAG2(int ndx) throws PsseModelException {return _mag2[ndx];}
	@Override
	public int getNMETR(int ndx) throws PsseModelException {return _nmetr[ndx];}
	@Override
	public String getNAME(int ndx) throws PsseModelException {return _name[ndx];}
	@Override
	public int getSTAT(int ndx) throws PsseModelException {return _stat[ndx];}

	@Override
	public void setInSvc(int ndx, boolean state)
			throws PsseModelException
	{
		_stat[ndx] = state ? 1 : 0;
		_eq.resetTP();
	}

	@Override
	public boolean isInSvc(int ndx) throws PsseModelException
	{
		return _stat[ndx] == 1;
	}

	@Override
	public float getR1_2(int ndx) throws PsseModelException {return _z.re(ndx);}
	@Override
	public float getX1_2(int ndx) throws PsseModelException {return _z.im(ndx);}
	@Override
	public float getSBASE1_2(int ndx) throws PsseModelException {return _sbase[ndx];}

	@Override
	public float getWINDV1(int ndx) throws PsseModelException {return _windv1[ndx];}
	@Override
	public float getNOMV1(int ndx) throws PsseModelException {return _nomv1[ndx];}
	@Override
	public float getANG1(int ndx) throws PsseModelException {return _ang1[ndx];}
	@Override
	public float getRATA1(int ndx) throws PsseModelException {return _rata1[ndx];}
	@Override
	public float getRATB1(int ndx) throws PsseModelException {return _ratb1[ndx];}
	@Override
	public float getRATC1(int ndx) throws PsseModelException {return _ratc1[ndx];}
	@Override
	public int getCOD1(int ndx) throws PsseModelException {return _cod1[ndx];}
	@Override
	public float getRMA1(int ndx) throws PsseModelException {return _rma1[ndx];}
	@Override
	public float getRMI1(int ndx) throws PsseModelException {return _rmi1[ndx];}
	@Override
	public float getVMA1(int ndx) throws PsseModelException {return _vma1[ndx];}
	@Override
	public float getVMI1(int ndx) throws PsseModelException {return _vmi1[ndx];}
	@Override
	public int getNTP1(int ndx) throws PsseModelException {return _ntp1[ndx];}
	@Override
	public int getTAB1(int ndx) throws PsseModelException {return _tab1[ndx];}
	@Override
	public float getCR1(int ndx) throws PsseModelException {return _cr1[ndx];}
	@Override
	public float getCX1(int ndx) throws PsseModelException {return _cx1[ndx];}
	
	
	@Override
	public String getObjectID(int ndx) throws PsseModelException
	{
		StringBuilder sb = new StringBuilder("PS-");
		sb.append(getI(ndx));
		sb.append('-');
		sb.append(getJ(ndx));
		sb.append('-');
		sb.append(getCKT(ndx));
		return sb.toString();
	}

	@Override
	public int size() {return _size;}

	
	@Override
	public float[] getRATA1() { return _rata1;}
	@Override
	public float[] getRATB1() { return _ratb1;}
	@Override
	public float[] getRATC1() { return _ratc1;}
	
	@Override
	public void setRATA1(int ndx, float rata1) throws PsseModelException {_rata1[ndx] = rata1;}
	@Override
	public void setRATB1(int ndx, float ratb1) throws PsseModelException {_ratb1[ndx] = ratb1;}
	@Override
	public void setRATC1(int ndx, float ratc1) throws PsseModelException {_ratc1[ndx] = ratc1;}
	

	@Override
	/** Set reactance value in per unit */
	public void setX(int ndx, float x) throws PsseModelException {}
	
}
