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
 * Power Applications conversions 
 * 
 * @author chris@powerdata.com
 *
 */
public class PAMath
{
	/* constants that get re-used */
	public static final float PI = (float) Math.PI;
	
	/** 3 over PI */
	public static final float THREEOVERPI = 3f / PI;
	
	/** 3 sqrt(2) over PI */
	public static final float THREESQRT2OVERPI = THREEOVERPI * ((float) Math.sqrt(2f));

	/** Constant to convert degrees and radians */
	public static final float D2R = PI/180f;

	
	/** convert degrees to radians */
	public static float deg2rad(float deg) {return deg*D2R;}
	/** convert radians to degrees */
	public static float rad2deg(float rad) {return rad/D2R;}

	/* methods to convert per-unit on 100MVA base */
	
	/** convert MW to per-unit */
	public static float mw2pu(float mw) {return mw/100F;}
	/** convert pu active power fo MW */
	public static float pu2mw(float pwr) {return pwr*100F;}
	/** convert MVAr to per-unit */
	public static float mvar2pu(float mvar) {return mvar/100F;}
	/** convert pu active power fo MVAr */
	public static float pu2mvar(float pwr) {return pwr*100F;}
	/** convert p.u. impedance to 100 MVA base */
	public static float rebaseZ100(float zval, float mvabase) {return (mvabase==100F)?zval:zval*100F/mvabase;}
	/** convert p.u. impedance to 100 MVA base */
	public static Complex rebaseZ100 (Complex zval, float mvabase)
	{
		float ratio = 100F/mvabase;
		return (mvabase==100F)?zval:new Complex(zval.re()*ratio, zval.im()*ratio);
	}

}
