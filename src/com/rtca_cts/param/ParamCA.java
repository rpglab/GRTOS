package com.rtca_cts.param;


/**
 *  All static variables should be modified if needed at the very beginning of
 *  any application program! Otherwise, chaos may occur!!!
 * 
 * @author Xingpeng.Li (xplipower@gmail.com)
 *
 */
public class ParamCA {
	
	//Just for creating contingency list. See ParamVio.java for CA Monitor Setting.
	static boolean _excludeRadialBranch = true;
	static boolean _exculdeOneBusIslandBrc = false;

	static boolean _CheckBrcVmLevel = false;
	static float _BrcHighVmLevel = 70.001f;    // Bus voltage level below this value will not be recorded if _CheckBusVmLevel is true
                                               // Note that this value won't matter if array _isHighVoltBus[] in VioResult.java is not null;
	static boolean _isBrcVmAbsHigh = false;    // if true, then, for contingency list routine, only branch with two-end buses 
	                                           // being of high voltage is considered as high-voltage level branch.
	
	static boolean _checkArea = false;    // if true, then, contingency list won't include branches that not belonging to the areas of interest.
	static boolean _isTwoEndBusesInArea = true; // if true, only branch whose two-end buses are both in this area will be considered as BrcInArea.
	static int[] _areaForContiList = null;        // areas of interest in terms of generating contingency list.
	
	/** @param _optionPfac
	 * 0 : No generation re-dispatch,
	 * 1 : Participation factors are based on generators' capacity,
	 * 2 : Participation factors are based on generators' available reserve, (10-minute ramping is not considered for now)
	 * 3 : Participation factors are based on generators' inertia (H).
	 */
	static int _optionPfactor = 2;

	
	// Set methods
	public static void setExcludeRadialBranch(boolean mark) {_excludeRadialBranch = mark;}
	public static void setExculdeOneBusIslandBrc(boolean mark) {_exculdeOneBusIslandBrc = mark;}

	public static void setCheckBrcVmLevel(boolean mark) {_CheckBrcVmLevel = mark;}
	public static void setBrcHighVmLevel(float a) {_BrcHighVmLevel = a;}
	public static void setIsBrcVmAbsHigh(boolean mark) {_isBrcVmAbsHigh = mark;}

	public static void setCheckArea(boolean mark) {_checkArea = mark;}
	public static void setTwoEndBusesInArea(boolean mark) {_isTwoEndBusesInArea = mark;}
	public static void setAreaForContiList(int[] area) {_areaForContiList = area;}
	
	public static void setPfactorForGenConti(int a) {_optionPfactor = a;}
	
	// Get methods
	public static boolean getExcludeRadialBranch() {return _excludeRadialBranch;}
	public static boolean getExculdeOneBusIslandBrc() {return _exculdeOneBusIslandBrc;}

	public static boolean getCheckBrcVmLevel() {return _CheckBrcVmLevel;}
	public static float getBrcHighVmLevel() {return _BrcHighVmLevel;}
	public static boolean getIsBrcVmAbsHigh() {return _isBrcVmAbsHigh;}

	public static boolean getCheckArea() {return _checkArea;}
	public static boolean getTwoEndBusesInArea() {return _isTwoEndBusesInArea;}
	public static int[] getAreaForContiList() {return _areaForContiList;}

	public static int getPfactorForGenConti() {return _optionPfactor;}

}




