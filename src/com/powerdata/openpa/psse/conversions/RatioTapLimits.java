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

import com.powerdata.openpa.psse.Bus;
import com.powerdata.openpa.psse.PsseModelException;
import com.powerdata.openpa.psse.Limits;
import com.powerdata.openpa.psse.Transformer;

public abstract class RatioTapLimits
{
	/** CW  x 3 (windings) */
	static private final CwWndToFact[][] _ctf;
	static
	{
		CwWndToFact cw1w1 = new Cw1Wnd1ToFact();
		CwWndToFact cw1w2 = new Cw1Wnd2ToFact();
		CwWndToFact cw1w3 = new Cw1Wnd3ToFact();
		_ctf = new CwWndToFact[][] {
				{null, cw1w1, cw1w2, cw1w3}, 											// CW = 0(default to CW 1) for each of 3 windings
				{null, cw1w1, cw1w2, cw1w3}, 											// CW = 1 for each of 3 windings
				{null, new Cw2Wnd1ToFact(), new Cw2Wnd2ToFact(), new Cw2Wnd3ToFact()}, 	// CW = 2 for each of 3 windings
				{null, new Cw3Wnd1ToFact(), new Cw3Wnd2ToFact(), new Cw3Wnd3ToFact()}, 	// CW = 3 for each of 3 windings
		};
	}
	
	/** Call here to get the appropriate limits */
	/** Call here to get the appropriate limits */
	public static Limits getLimits(Transformer xf, int winding) throws PsseModelException
	{
		return _ctf[xf.getCW()][winding].getFactory(xf).getLimits(xf);
	}

	public abstract Limits getLimits(Transformer xf)
			throws PsseModelException;
	
}

interface CwWndToFact
{
	public RatioTapLimits getFactory(Transformer xf) throws PsseModelException;
}

/* Converters for Code CW 1 */

class Cw1Wnd1ToFact implements CwWndToFact
{
	private static final RatioTapLimits[] _Facts;
	static
	{
		_Facts = new RatioTapLimits[]
		{
			W1PassThru.Default,
			W1PassThru.Default,
			W1PassThru.Default,
			null, // should never happen
			W1PassThru.Default,
		};
	}
	@Override
	public RatioTapLimits getFactory(Transformer xf)
			throws PsseModelException
	{
		return _Facts[Math.abs(xf.getCOD1())];
	}
}

class Cw1Wnd2ToFact implements CwWndToFact
{
	private static final RatioTapLimits[] _Facts;
	static
	{
		_Facts = new RatioTapLimits[]
		{
			W2PassThru.Default,
			W2PassThru.Default,
			W2PassThru.Default,
			null, // should never happen
			W2PassThru.Default,
		};
	}

	@Override
	public RatioTapLimits getFactory(Transformer xf)
			throws PsseModelException
	{
		return _Facts[0];
	}
}

class Cw1Wnd3ToFact implements CwWndToFact
{
	private static final RatioTapLimits[] _Facts;
	static
	{
		_Facts = new RatioTapLimits[]
		{
			W3PassThru.Default,
			W3PassThru.Default,
			W3PassThru.Default,
			null, // should never happen
			W3PassThru.Default,
		};
	}

	@Override
	public RatioTapLimits getFactory(Transformer xf)
			throws PsseModelException
	{
		return _Facts[0];
	}
}

class W1PassThru extends RatioTapLimits
{
	static public final RatioTapLimits	Default	= new W1PassThru();

	@Override
	public Limits getLimits(Transformer xf)
			throws PsseModelException
	{
		return new Limits(xf.getRMI1(), xf.getRMA1());
	}
}

class W2PassThru extends RatioTapLimits
{
	static public final RatioTapLimits	Default	= new W2PassThru();

	@Override
	public Limits getLimits(Transformer xf)
			throws PsseModelException
	{
		return new Limits(xf.getRMI2(), xf.getRMA2());
	}
}

class W3PassThru extends RatioTapLimits
{
	static public final RatioTapLimits	Default	= new W3PassThru();

	@Override
	public Limits getLimits(Transformer xf)
			throws PsseModelException
	{
		return new Limits(0.9f, 1.1f);
	}
}


/* Converters for Code CW 2 */

class Cw2Wnd1ToFact implements CwWndToFact
{
	private static final RatioTapLimits[] _Facts;
	static
	{
		_Facts = new RatioTapLimits[]
		{
			W1PassThru.Default,
			Cw2W1.Default,
			Cw2W1.Default,
			null, // should never happen
			W1PassThru.Default,
		};
	}
	@Override
	public RatioTapLimits getFactory(Transformer xf)
			throws PsseModelException
	{
		return _Facts[Math.abs(xf.getCOD1())];
	}
}

class Cw2Wnd2ToFact implements CwWndToFact
{
	private static final RatioTapLimits[] _Facts;
	static
	{
		_Facts = new RatioTapLimits[]
		{
			W2PassThru.Default,
			Cw2W2.Default,
			Cw2W2.Default,
			null, // should never happen
			W2PassThru.Default,
		};
	}

	@Override
	public RatioTapLimits getFactory(Transformer xf)
			throws PsseModelException
	{
		return _Facts[0];
	}
}

class Cw2Wnd3ToFact implements CwWndToFact
{
	private static final RatioTapLimits[] _Facts;
	static
	{
		_Facts = new RatioTapLimits[]
		{
			W3PassThru.Default,
			Cw2W3.Default,
			Cw2W3.Default,
			null, // should never happen
			W3PassThru.Default,
		};
	}

	@Override
	public RatioTapLimits getFactory(Transformer xf)
			throws PsseModelException
	{
		return _Facts[0];
	}
}

class CwCvt
{
	static Limits cw2(float rmi, float rma, Bus bus) throws PsseModelException
	{
		float kv = bus.getBASKV();
		return new Limits(rmi/kv, rma/kv);
	}

	static Limits cw3(float rmi, float rma, float nomv, Bus bus) throws PsseModelException
	{
		float cwratio = nomv / bus.getBASKV();
		return new Limits(rmi/cwratio, rma/cwratio);
	}
}

class Cw2W1 extends RatioTapLimits
{
	static public final RatioTapLimits	Default	= new Cw2W1();

	@Override
	public Limits getLimits(Transformer xf)
			throws PsseModelException
	{
		
		return CwCvt.cw2(xf.getRMI1(), xf.getRMA1(), xf.getFromBus());
	}
}

class Cw2W2 extends RatioTapLimits
{
	static public final RatioTapLimits	Default	= new Cw2W2();

	@Override
	public Limits getLimits(Transformer xf)
			throws PsseModelException
	{
		return CwCvt.cw2(xf.getRMI2(), xf.getRMA2(), xf.getToBus());
	}
}

class Cw2W3 extends RatioTapLimits
{
	static public final RatioTapLimits	Default	= new Cw2W3();

	@Override
	public Limits getLimits(Transformer xf)
			throws PsseModelException
	{
		return new Limits(0.9f, 1.1f);
	}
}

/* Converters for Code CW 3 */

class Cw3
{
	
}

class Cw3Wnd1ToFact implements CwWndToFact
{
	private static final RatioTapLimits[] _Facts;
	static
	{
		_Facts = new RatioTapLimits[]
		{
			W1PassThru.Default,
			Cw3W1.Default,
			Cw3W1.Default,
			null, // should never happen
			W1PassThru.Default,
		};
	}
	@Override
	public RatioTapLimits getFactory(Transformer xf)
			throws PsseModelException
	{
		return _Facts[Math.abs(xf.getCOD1())];
	}
}

class Cw3Wnd2ToFact implements CwWndToFact
{
	private static final RatioTapLimits[] _Facts;
	static
	{
		_Facts = new RatioTapLimits[]
		{
			W2PassThru.Default,
			Cw3W2.Default,
			Cw3W2.Default,
			null, // should never happen
			W2PassThru.Default,
		};
	}

	@Override
	public RatioTapLimits getFactory(Transformer xf)
			throws PsseModelException
	{
		return _Facts[0];
	}
}

class Cw3Wnd3ToFact implements CwWndToFact
{
	private static final RatioTapLimits[] _Facts;
	static
	{
		_Facts = new RatioTapLimits[]
		{
			W3PassThru.Default,
			Cw3W3.Default,
			Cw3W3.Default,
			null, // should never happen
			W3PassThru.Default,
		};
	}

	@Override
	public RatioTapLimits getFactory(Transformer xf)
			throws PsseModelException
	{
		return _Facts[0];
	}
}

class Cw3W1 extends RatioTapLimits
{
	static public final RatioTapLimits	Default	= new Cw3W1();

	@Override
	public Limits getLimits(Transformer xf)
			throws PsseModelException
	{
		return CwCvt.cw3(xf.getRMI1(), xf.getRMA1(),
				xf.getNOMV1(), xf.getFromBus());
	}
}

class Cw3W2 extends RatioTapLimits
{
	static public final RatioTapLimits	Default	= new Cw3W2();

	@Override
	public Limits getLimits(Transformer xf)
			throws PsseModelException
	{
		return CwCvt.cw3(xf.getRMI2(), xf.getRMA2(),
				xf.getNOMV2(), xf.getToBus());
	}
}

class Cw3W3 extends RatioTapLimits
{
	static public final RatioTapLimits	Default	= new Cw3W3();

	@Override
	public Limits getLimits(Transformer xf)
			throws PsseModelException
	{
		return new Limits(0.9f, 1.1f);
	}
}

