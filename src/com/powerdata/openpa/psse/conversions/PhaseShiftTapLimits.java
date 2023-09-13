package com.powerdata.openpa.psse.conversions;

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

import com.powerdata.openpa.psse.Limits;
import com.powerdata.openpa.psse.PhaseShifter;
import com.powerdata.openpa.psse.PsseModelException;

public abstract class PhaseShiftTapLimits
{
	private static final WndToTapLim[]	_TapLim	= new WndToTapLim[] { null,
			new Wnd1TapLim(), new Wnd2TapLim(), new Wnd3TapLim() };

	/** Call here to get the appropriate limits */
	public static Limits getLimits(PhaseShifter xf, int winding)
			throws PsseModelException
	{
		return _TapLim[winding].getLimits(xf);
	}

}

interface WndToTapLim
{
	public Limits getLimits(PhaseShifter xf) throws PsseModelException;
}

class Wnd1TapLim implements WndToTapLim
{
	@Override
	public Limits getLimits(PhaseShifter xf) throws PsseModelException
	{
		return new Limits(xf.getRMI1(), xf.getRMA1());
	}
}

class Wnd2TapLim implements WndToTapLim
{
	@Override
	public Limits getLimits(PhaseShifter xf)
			throws PsseModelException
	{
		return new Limits(-180f, 180f);
	}
}

class Wnd3TapLim implements WndToTapLim
{
	@Override
	public Limits getLimits(PhaseShifter xf) throws PsseModelException
	{
		return new Limits(180f, 180);
	}
}
