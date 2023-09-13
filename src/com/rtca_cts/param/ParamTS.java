package com.rtca_cts.param;

/**
 *  All static variables should be modified if needed at the very beginning of
 *  any application program! Otherwise, chaos may occur!!!
 * 
 *  Routine for creating switching list.
 * 
 * @author Xingpeng.Li (xplipower@gmail.com)
 *
 */
public class ParamTS {
	
	/** 
	 * 1 : lines close to contingency element as TS List.
	 * 2 : lines close to violation elements as TS List.
	 * 3 : fixed TS List from data mining method as TS List.
	 * 4 : complete enumeration.
	 * 5 : different TS List for different Ctgcy - Enhanced Data Mining Method. -- Input data files need to be processed first before they can be used.
	 * 6 : different TS List for different Ctgcy - Enhanced Data Mining (version 2) Method. -- Input data files can be directly used without data processing.
	 * 7 : LODF based candidate TS List (for flow violation only).
	 * 8 : LODF*CtgcyBrcPfrm based candidate TS List (for flow violation only).
	 */
	static int _TSOption = 7;
	// numBrcVioConcern and numVmVioConcern can work only if _TSOption == 2;
	static int _numBrcVioConcern = 1;    // only consider the first _numBrcVioConcern worst violations when selecting switching actions for flow vio.
	static boolean _isNumBrcVioAbsConcern = true;  // get the first _numBrcVioConcern worst violations based on absolute flow MW violations rather than percentage values.
	static int _numVmVioConcern = 1;     // only consider the first _numVmVioConcern worst violations when selecting switching actions for Vm vio.
	
	static int _numTS = 20;  // expected length of TS List, used when _TSOption = 1 or 2.
	static int _reportBestNumTS = 5; // report the best N switching actions for each contingency.
	
	static boolean _excludeRadialBranch = true;

	static boolean _CheckBrcVmLevel = false;
	static float _BrcHighVmLevel = 70.001f;    // Bus voltage level below this value will not be recorded if _CheckBusVmLevel is true
                                               // Note that this value won't matter if array _isHighVoltBus[] in VioResult.java is not null;
	static boolean _isBrcVmAbsHigh = false;    // if true, then, for contingency list routine, only branch with two-end buses 
	                                           // being of high voltage is considered as high-voltage level branch.
	
	static boolean _checkArea = false;    // if true, then, contingency list won't include branches that not belonging to the areas of interest.
	static boolean _isTwoEndBusesInArea = true; // if true, only branch whose two-end buses are both in this area will be considered as BrcInArea.
	static int[] _areaList = null;
	
	// Set methods
	public static void setTSOption(int option) {_TSOption = option;}
	public static void setNumBrcVioConcern(int numBrc) {_numBrcVioConcern = numBrc;}
	public static void setIsNumBrcVioAbsConcern(boolean markAbsBrcVio) {_isNumBrcVioAbsConcern = markAbsBrcVio;}
	public static void setNumVmVioConcern(int numVm) {_numVmVioConcern = numVm;}
	
	public static void setNumTS(int num) {_numTS = num;}
	public static void setReportBestNumTS(int num) {_reportBestNumTS = num;}
	
	public static void setExcludeRadialBranch(boolean mark) {_excludeRadialBranch = mark;}

	public static void setCheckBrcVmLevel(boolean mark) {_CheckBrcVmLevel = mark;}
	public static void setBrcHighVmLevel(float a) {_BrcHighVmLevel = a;}
	public static void setIsBrcVmAbsHigh(boolean mark) {_isBrcVmAbsHigh = mark;}

	public static void setCheckArea(boolean mark) {_checkArea = mark;}
	public static void setTwoEndBusesInArea(boolean mark) {_isTwoEndBusesInArea = mark;}
	public static void setAreaForContiList(int[] area) {_areaList = area;}
	
	// Get methods
	public static int getTSOption() {return _TSOption;}
	public static int getNumBrcVioConcern() {return _numBrcVioConcern;}
	public static boolean getIsNumBrcVioAbsConcern() {return _isNumBrcVioAbsConcern;}
	public static int getNumVmVioConcern() {return _numVmVioConcern;}

	public static int getNumTS() {return _numTS;}
	public static int getReportBestNumTS() {return _reportBestNumTS;}
	
	public static boolean getExcludeRadialBranch() {return _excludeRadialBranch;}
	
	public static boolean getCheckBrcVmLevel() {return _CheckBrcVmLevel;}
	public static float getBrcHighVmLevel() {return _BrcHighVmLevel;}
	public static boolean getIsBrcVmAbsHigh() {return _isBrcVmAbsHigh;}

	public static boolean getCheckArea() {return _checkArea;}
	public static boolean getTwoEndBusesInArea() {return _isTwoEndBusesInArea;}
	public static int[] getAreaList() {return _areaList;}

	
}
