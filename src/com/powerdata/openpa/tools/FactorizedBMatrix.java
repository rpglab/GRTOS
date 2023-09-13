package com.powerdata.openpa.tools;

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

import java.io.PrintWriter;

import com.powerdata.openpa.psse.BusList;
import com.powerdata.openpa.psse.PsseModel;
import com.powerdata.openpa.psse.PsseModelException;
import com.utilxl.iofiles.OutputArrays;

/**
 * Keep a more efficient version of the factorized sparse matrix.  
 * 
 * @author chris@powerdata.com
 *
 */

public class FactorizedBMatrix
{
	float[] _bself, _bbrofs;
	int[] _pnode, _qnode, _brndx;
	/** bus eliminated */
	int[] _buselim;
	
	FactorizedBMatrix(float[] bself, float[] bbrofs, int[] pnode,
			int[] qnode, int[] brndx, int[] buselim)
	{
		_bself = bself;
		_bbrofs = bbrofs;
		_pnode = pnode;
		_qnode = qnode;
		_brndx = brndx;
		_buselim = buselim;
	}

	public void dump(PsseModel model, PrintWriter pw) throws PsseModelException
	{
		pw.println("\"brndx\",\"eord\",\"p\",\"pndx\",\"q\",\"qndx\",\"-bbranch/bself\",\"bself\"");
		BusList buses = model.getBuses();
		int iord = -1;
		int oldpn = -1;
		for (int i = 0; i < _pnode.length; ++i)
		{
			int pn = _pnode[i];
			int qn = _qnode[i];
			if (pn != oldpn)
			{
				oldpn = pn;
				++iord;
			}

			pw.format("%d,%d,\"%s\",%d,\"%s\",%d,%f,%f\n", _brndx[i], iord,
					buses.get(pn).getNAME(), pn, buses.get(qn).getNAME(), qn,
					_bbrofs[i], _bself[pn]);
		}
	}
	
	/** 
	 * Perform a forward reduction
	 * @param mm mismatch array
	 * @return Array (in bus order) results of forward reduction results 
	 */
	public float[] forwardReduction(float[] mm)
	{
		int nbr = _bbrofs.length;
		float[] rv = mm.clone();
		for (int i = 0; i < nbr; ++i)
		{
			rv[_qnode[i]] += _bbrofs[i] * rv[_pnode[i]];
		}
		return rv;
	}

	/**
	 * Perform backward substitution
	 * @param ds result of forward reduction
	 * @return Array (in bus order) of corrections
	 */
	public float[] backwardSubstitution(float[] ds)
	{
		int nbr = _bbrofs.length;
		float[] dx = new float[ds.length];
		for(int bus : _buselim)
		{
			dx[bus] = ds[bus] / _bself[bus];
		}
		for (int i = nbr - 1; i >= 0; --i)
		{
			dx[_pnode[i]] += _bbrofs[i] * dx[_qnode[i]];
		}
		return dx;
	}
	
	/**
	 * convenience method to solve the matrix by running both forward reduction
	 * and backward substitution
	 * 
	 * @param mm
	 *            Mismatch array
	 * @return Array (in bus order) of corrections
	 */
	public float[] solve(float[] mm)
	{
		return backwardSubstitution(forwardReduction(mm));
	}

	/**
	 * As a convenience, return buses that we determined get eliminated
	 * @return array of eliminated bus indexes
	 */
	public int[] getEliminatedBuses() {return _buselim;}
}
