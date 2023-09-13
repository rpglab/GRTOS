package com.rtca_cts.param;

/**
 * 
 *  All static variables should be modified if needed at the very beginning of
 *  any application program! Otherwise, chaos may occur!!!
 * 
 * @author Xingpeng.Li (xplipower@gmail.com)
 *
 */
public class ParamFDPFlow {
	
	/** Active power convergence tolerance */
	static float _Ptol = 0.005f;
	/** Reactive power convergence tolerance */
	static float _Qtol = 0.005f;
	/** Maximum iterations before giving up */
	static int _Itermax = 20;
	
	/** Power flow algorithm option:
	 * 1: the original decoupled power flow algorithm of 1-st version OpenPA; (tap is not considered inside the algorithm)
	 * 2: the one introduced in W&W (Allen J. Wood) book.
	 *  */
	static int _PfAlgorithmOption = 2;
	
	/** Minimum branch reactance value */
	static float _minxmag = 0.0001f;
	/** Gen Var Limit. */
	static boolean _enableGenVarLimit = true;
	
	// Set methods
	public static void setPTol(float tol) {_Ptol = tol;}
	public static void setQTol(float tol) {_Qtol = tol;}
	public static void setItermax(int num) {_Itermax = num;}
	public static void setPfAlgorithm(int num) {_PfAlgorithmOption = num;}
	
	public static void setMinxmag(float minxmag) {_minxmag = minxmag;}
	public static void setGenVarLimit(boolean mark) {_enableGenVarLimit = mark;}
	
	
	// Get methods
	public static float getPTol() {return _Ptol;}
	public static float getQTol() {return _Qtol;}
	public static int getItermax() {return _Itermax;}
	public static int getPfAlgorithmOption() {return _PfAlgorithmOption;}

	public static float getMinxmag() {return _minxmag;}
	public static boolean getGenVarLimit() {return _enableGenVarLimit;}

}
