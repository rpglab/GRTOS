package com.rtca_cts.param;


/**
 * Parameters for RTCA with FACTS;
 * 
 * Initialized in December 8, 2018.
 * TBD: FACTS control not yet implemented.
 * 
 * @author Xingpeng.Li, xplipower@gmail.com 
 * 
 */
public class ParamFACTS {

	static boolean _enableFACTS = false;  // If FACTS is enabled, then TS is automatically disabled.
	                                      // majority of TS parameters will be applied here too, 
	                                      // except radial line part since FACTS does not change topology
	static float _XChangeRatio = 1.5f;

	
	// Set methods	
	public static void setStatusFACTS(boolean ratio) {_enableFACTS = ratio;}
	public static void setXChangeRatio(float ratio) {_XChangeRatio = ratio;}

	// Get methods
	public static boolean getStatusFACTS() {return _enableFACTS;}
	public static float getXChangeRatio() {return _XChangeRatio;}

	
}
