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

import java.util.List;

import com.powerdata.openpa.psse.ACBranch;
import com.powerdata.openpa.psse.PsseModelException;
import com.powerdata.openpa.tools.Complex;

/**
 * Force Reactance a minimum distance away from zero.

 * @author chris@powerdata.com
 *
 */
public class MinZMagFilter extends ImpedanceFilter
{
	protected float[] _x, _g, _b;
	
	/**
	 * Create a new minimum impedance filter.
	 * @param branches List of branches on which to manipulate reactance
	 * @param minxmag smallest value of X, such that |X| >= minX
	 * @throws PsseModelException 
	 */
	public MinZMagFilter(List<? extends ACBranch> branches, float minxmag) throws PsseModelException
	{
		super(branches);
		int nbr = branches.size();
		_x = new float[nbr];
		_g = new float[nbr];
		_b = new float[nbr];
		for(int i=0; i < nbr; ++i)
		{
			ACBranch branch = branches.get(i);
			float r = branch.getR();
			float x = branch.getX();
			if (Math.abs(x) < minxmag)
			{
				x = Math.copySign(minxmag, x);
			}
			Complex y = new Complex(r, x).inv();
			_x[i] = x;
			_g[i] = y.re();
			_b[i] = y.im();
		}
	}

	@Override
	public float getX(int ndx) throws PsseModelException {return _x[ndx];}
	@Override
	public Complex getZ(int ndx) throws PsseModelException {return new Complex(getR(ndx), _x[ndx]);}
	@Override
	public Complex getY(int ndx) throws PsseModelException {return new Complex(_g[ndx], _b[ndx]);}
}
