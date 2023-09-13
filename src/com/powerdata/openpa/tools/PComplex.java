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



/**
 * Complex number in polar form
 * 
 * @author chris@powerdata.com
 *
 */

public class PComplex
{
	private float _r;
	private float _theta;
	
	public static final PComplex Zero = new PComplex(0,0);

	public PComplex(float r, float theta)
	{
		_r = r;
		_theta = theta;
	}

	public float r() {return _r;}
	public float theta() {return _theta;}
	
	/** multiply two complex numbers */
	public PComplex mult(PComplex v) {return new PComplex(_r*v.r(), _theta+v.theta());}
	/** multiply two complex numbers */
	public PComplex mult(float r, float theta) {return new PComplex(_r*r, _theta + theta);}
	/** multiply a complex number by a scalar */
	public PComplex mult(float scalar) {return new PComplex(_r * scalar, _theta);}

	/** multiply two complex numbers */
	public PComplex div(PComplex v) {return new PComplex(_r/v.r(), _theta-v.theta());}
	/** multiply two complex numbers */
	public PComplex div(float r, float theta) {return new PComplex(_r/r, _theta - theta);}
	/** multiply a complex number by a scalar */
	public PComplex div(float scalar) {return new PComplex(_r / scalar, _theta);}
	
	/** invert the complex number */
	public PComplex inv() {return new PComplex(1F/_r, -_theta);}
	/** return the absolute value of the complex number */
	public float abs() {return _r;}
	/** return the complex conjugate */
	public PComplex conjg() {return new PComplex(_r, -_theta);}
	
	/** convert to cartesian coordinates */
	public Complex cartesian()
	{
		return new Complex((float) (_r * Math.cos(_theta)),
				(float) (_r * Math.sin(_theta)));
	}
	
	@Override
	final public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append('(');
		sb.append(_r);
		sb.append(',');
		sb.append(_theta);
		sb.append(')');
		return sb.toString();
	}

	@Override
	public boolean equals(Object obj)
	{
		PComplex o = (PComplex) obj;
		return (_r == o._r && _theta == o._theta);
	}
	

}


