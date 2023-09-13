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

import com.powerdata.openpa.tools.BaseObject;

public interface OneTermDev extends BaseObject
{
	public Bus getBus() throws PsseModelException;
	/** get the current MW */
	public float getP() throws PsseModelException;
	/** get the current MVar */
	public float getQ() throws PsseModelException;
	/** set the current MW */
	public void setP(float mw) throws PsseModelException;
	/** set the current MVar */
	public void setQ(float mvar) throws PsseModelException;
	/** get active power p.u. on 100MVA base */
	public float getPpu() throws PsseModelException;
	/** set active power p.u. on 100MVA base */
	public void setPpu(float p) throws PsseModelException;
	/** get reactive power p.u. on 100MVA base */
	public float getQpu() throws PsseModelException;
	/** set reactive power p.u. on 100MVA base */
	public void setQpu(float q) throws PsseModelException;
	public boolean isInSvc() throws PsseModelException;
	public void setInSvc(boolean state) throws PsseModelException;
}
