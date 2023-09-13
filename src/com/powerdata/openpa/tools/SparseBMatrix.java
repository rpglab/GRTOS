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

import java.util.Arrays;

import com.powerdata.openpa.tools.SparseMatrixFactorizer.EliminatedBus;

/**
 * Manage a sparse B matrix.  
 * 
 * TODO:  allow the original bself and bbranch values to be changed, so rhat 
 * factorization can be re-run without creating a new object (and thus requiring elimination 
 * to be re-run)
 * 
 * @author chris@powerdata.com
 *
 */
public class SparseBMatrix
{
	SparseMatrixFactorizer _factorizer;
	float[] _bbranch, _bself;
	int[] _pnode, _qnode, _elimbrndx;
	int[] _buselim;
	
	/**
	 * Create a new Sparse Susceptance Matrix
	 * @param onet LinkNet sparse representation of network
	 * @param saveBusNdx Set of bus indexes to be retained during elimination
	 * @param bbranch Array of Off-diagonal elements in the same order as branches added to LinkNet
	 * @param bself Array of Diagonal elements for each bus
	 */
	public SparseBMatrix(LinkNet onet, int[] saveBusNdx, float[] bbranch, float[] bself)
	{
		LinkNet net = onet.clone();
		_factorizer = new SparseMatrixFactorizer(net, saveBusNdx);
		_bbranch = bbranch;
		_bself = bself;
		int nfactbr = (int) (_factorizer.getFactorizedBranchCount() * 1.25f);
		_pnode = new int[nfactbr];
		_qnode = new int[nfactbr];
		_elimbrndx = new int[nfactbr];
		int nused=0;
		for(int ielim=0, jbr=0; ielim < _factorizer.size(); ++ielim)
		{
			EliminatedBus ebus = _factorizer.get(ielim);
			int[] cnodes = ebus.getRemainingNodes();
			int[] elimbr = ebus.getElimBranches();
			int ebusndx = ebus.getElimBusNdx();
			for(int i=0; i < cnodes.length; ++i, ++jbr)
			{
				++nused;
				_pnode[jbr] = ebusndx;
				_qnode[jbr] = cnodes[i];
				_elimbrndx[jbr] = elimbr[i];
			}
		}
		/*
		 * TODO: CMM (11/2013)- review if this is really necessary, I think I
		 * fixed the issue that originally made this necessary
		 */
		if (nused < nfactbr)
		{
			_pnode = Arrays.copyOf(_pnode, nused);
			_qnode = Arrays.copyOf(_qnode, nused);
			_elimbrndx = Arrays.copyOf(_elimbrndx, nused);
		}
		
		/*
		 * track the nodes we plan on eliminating
		 */
		int nbus = _bself.length;
		_buselim = new int[nbus - saveBusNdx.length];
		boolean[] elim = new boolean[nbus];
		Arrays.fill(elim, true);
		for(int bx : saveBusNdx) elim[bx] = false;
		for(int i=0, j=0; i < nbus; ++i)
		{
			if (elim[i]) _buselim[j++] = i;
		}
	}
	
	/** factorize the B matrix */
	public FactorizedBMatrix factorize()
	{
		float[] bself = _bself.clone();
		int nfbr = _factorizer.getFactorizedBranchCount();
		float[] bbranch = Arrays.copyOf(_bbranch, nfbr);
		int nelim = _factorizer.size();
		for(int ielim=0; ielim < nelim; ++ielim)
		{
			EliminatedBus ebus = _factorizer.get(ielim);
			int[] cnodes = ebus.getRemainingNodes();
			int[] cbr = ebus.getElimBranches();
			int[] tbr = ebus.getRemainingBranches();
			int itbr=0;
			for (int i=0; i < cnodes.length; ++i)
			{
				float bbrdiag = bbranch[cbr[i]];
				float bprep = -bbrdiag / bself[ebus.getElimBusNdx()];
				bself[cnodes[i]] += bprep * bbrdiag;
				for (int j=i+1; j < cnodes.length; ++j, ++itbr)
				{
//					int bbrndx = tbr[itbr];
//					if (bbrndx != -1)
//						bbranch[bbrndx] += bprep * bbranch[cbr[j]];
					bbranch[tbr[itbr]] += bprep * bbranch[cbr[j]];
				}
			}
		}

		/* convert bbranch to eliminated order */
		int nwl = _pnode.length;
		float[] elimbbr = new float[nwl];
		for(int i=0; i < nwl; ++i)
		{
			elimbbr[i] = -bbranch[_elimbrndx[i]] / bself[_pnode[i]];
		}
		return new FactorizedBMatrix(bself, elimbbr, _pnode, _qnode, _elimbrndx, _buselim);
	}

}
