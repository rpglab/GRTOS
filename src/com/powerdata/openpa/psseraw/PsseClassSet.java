package com.powerdata.openpa.psseraw;

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



/**
 * Provide an abstract method to get a set of PsseClass objects appropriate to
 * the given version
 * 
 * @author chris@powerdata.com
 * 
 */
public abstract class PsseClassSet
{
	public static final int MaxConfigVerMajor = 33;
	
	public static PsseClassSet GetClassSetForVersion(String version) throws PsseProcException
	{
		final String msg = "Version %d not supported, attempting to read using version %d\n";
		int ix = version.indexOf('.');
		String svmaj = null;
		svmaj = (ix == -1) ? version : version.substring(0, ix);
		
		int vmaj = Integer.parseInt(svmaj);
		PsseClassSet rv = null;
		
		if (vmaj <= 29)
		{
			rv = new PsseClassSetVersion29();
		}
		else if (vmaj <= 30)
		{
			rv = new PsseClassSetVersion30();
		}
		else if (vmaj <= 32)
		{
			System.err.format(msg, vmaj, 30);
			rv = new PsseClassSetVersion30();
		}
		else
		{
			if (vmaj > MaxConfigVerMajor)
				System.err.format(msg, vmaj, 33);
			System.err.println("Version 33 support may not be fully implemented.");
			rv = new PsseClassSetVersion33();
		}
		
		return rv;
	}
	
	public abstract PsseClass[] getPsseClasses();
	public abstract int getVersionMajor();
	
	public abstract PsseClass getBus();
	public abstract PsseClass getLoad();
	public abstract PsseClass getGenerator();
	public abstract PsseClass getNontransformerBranch();
	public abstract PsseClass getTransformer();
	public abstract PsseClass getAreaInterchange();
	public abstract PsseClass getTwoTermDCLine();
	public abstract PsseClass getVSC_DCLine();
	public abstract PsseClass getSwitchedShunt();
	public abstract PsseClass getTxImpedanceCorrection();
	/** AC Converter Records - inner record for Multi Terminal DC Lines */
	public abstract PsseClass getMultiTermDC_ACConv();
	/** DC Bus Records - inner record for Multi Terminal DC Lines */
	public abstract PsseClass getMultiTermDCBus();
	/** DC Link Records - inner record for Multi Terminal DC Lines */
	public abstract PsseClass getMultiTermDCLink();
	public abstract PsseClass getMultiTermDCLine();
	public abstract PsseClass getMultiSectionLine();
	public abstract PsseClass getZone();
	public abstract PsseClass getInterAreaTransfer();
	public abstract PsseClass getOwner();
	public abstract PsseClass getFACTSDevice();
	public abstract PsseClass getFixedShunt();
}
