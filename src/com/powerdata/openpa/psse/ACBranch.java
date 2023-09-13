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
 
public interface ACBranch extends TwoTermDev
{
	/** resistance p.u. on 100 MVA and bus base kv */
	public float getR() throws PsseModelException;
	/** reactance p.u. on 100MVA and bus base KV */
	public float getX() throws PsseModelException;
	/** Get complex impedance p.u. on 100 MVA and bus base kv */
	public Complex getZ() throws PsseModelException;
	/** Get complex admittance (1/Z) p.u. on 100 MVA and bus base kv */
	public Complex getY() throws PsseModelException;
	/** get from-side off-nominal tap ratio p.u. on 100MVA base and bus base KV */
	public float getFromTap() throws PsseModelException;
	/** get to-side off-nominal tap ratio p.u on 100MVA base and bus base KV */
	public float getToTap() throws PsseModelException;
	/** get transformer magnetizing conductance p.u. on 100MVA base */
	public float getGmag() throws PsseModelException;
	/** get transformer magnetizing susceptance p.u. on 100 MVA base */
	public float getBmag() throws PsseModelException;
	/** get from-side charging susceptance */
	public float getFromBchg() throws PsseModelException;
	/** get to-side charging susceptance */
	public float getToBchg() throws PsseModelException;
	/** get phase shift through branch (in RAD)*/
	public float getPhaseShift() throws PsseModelException;
	
	
	public String getCKT() throws PsseModelException;
	

}
