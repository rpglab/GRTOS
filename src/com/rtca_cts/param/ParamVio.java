package com.rtca_cts.param;


/**
 * 
 * All static variables should be modified if needed at the very beginning of
 *  any application program! Otherwise, chaos may occur!!!
 * 
 * @author Xingpeng.Li (xplipower@gmail.com)
 *
 */
public class ParamVio {
	
	// A contingency is critical If either Vm violation or Brc violation is critical.
	// For each type of violations, it would be considered as critical if either sumVio > sumTol or any elemVio > elemTol is satisfied.
	// If sumTol is the only concern, then, elemTol should be set to a very large value, e.g. 999f.
	static float _TolSumBrcVio = 0.05f;    // tolerance for summation of flow violations (in per unit).
	                                       // this value must be set as normal value for TS to be implemented !!!!
	static float _TolElemBrcVio = 0.05f;   // tolerance for individual flow violation (in per unit).
	
	static float _TolSumVmVio = 0.005f;    // tolerance for summation of voltage violations (in per unit).
                                           // this value must be set as normal value for TS to be implemented !!!!
	static float _TolElemVmVio = 0.005f;   // tolerance for individual voltage violation (in per unit).
//	static float _TolElemVmVio = 9990.000f;  
	
	
	//For Monitor: monitor all buses and/or branches.
	static boolean _monitorVm = false;     // voltage violation is not monitored at all if it is set to false, 
	                                       // it has a higher priority than _isAllBusMnt	
	static boolean _isAllBusMnt = false;   // monitor voltage violations on all buses. Its priority is higher than _checkArea and _CheckBusVmLevel.
	static boolean _isAllBrcMnt = false;   // monitor flow violations on all branches. 

	public static void setIsMonitorVm(boolean monitorVm) {_monitorVm = monitorVm;}
	public static void setIsAllBusMnt(boolean isAllMnt) {_isAllBusMnt = isAllMnt;}
	public static void setIsAllBrcMnt(boolean isAllMnt) {_isAllBrcMnt = isAllMnt;}
	
	public static boolean getIsMonitorVm() {return _monitorVm;}
	public static boolean getIsAllBusMnt() {return _isAllBusMnt;}
	public static boolean getIsAllBrcMnt() {return _isAllBrcMnt;}
	
	
	//For Monitor: Area info - if a branch is not in the areas of interest, then, violation on this branch will not be monitored.
	static boolean _checkArea = false;
	static int[] _areaMntList = null;        // areas of interest in terms of recording violations.
	static boolean _isTwoEndBusesInArea = true; // if true, only branch whose both end buses are within _areaMntList belongs to _areaMntList.

	public static void setCheckArea(boolean mark) {_checkArea = mark;}
	public static void setAreaMntList(int[] area) {_areaMntList = area;}
	public static void setTwoEndBusesInArea(boolean mark) {_isTwoEndBusesInArea = mark;}

	public static boolean getCheckArea() {return _checkArea;}
	public static int[] getAreaMntList() {return _areaMntList;}
	public static boolean getTwoEndBusesInArea() {return _isTwoEndBusesInArea;}
	
	
    //For Monitor: for bus voltage limit settings
	static float _VmMax = 1.1f;
	static float _VmMin = 0.9f;
	
	static boolean _CheckBusVmLevel = false;
	static float _BusHighVmLevel = 70.001f;      // for VmVio, bus voltage level below this value will not be recorded if _CheckBusVmLevel is true
	                                        // Note that this value won't matter if array _isHighVoltBus[] in VioResult.java is not null;

	
    //For Monitor: for brc thermal limit settings
	static boolean _CheckBrcVmLevel = false;
	static float _BrcHighVmLevel = 70.001f;      // for BrcVio, only branches whose voltage level (either Frm bus or To bus) is greater than this value 
	                                        // will be recorded if checkBrcVmLevel is true.
	                                        // Note that this value won't matter if array _isHighVoltBrc[] in VioResult.java is not null;
	static boolean _isBrcVmAbsHigh = false; // If false, then, branches whose either end bus is greater than
 	                                             //  _BrcHighVmLevel would be considered as high-voltage.
	                                           // If true, then, only branches whose both end buses are greater than
                                                 //  _BrcHighVmLevelForVioMonitor would be considered as high-voltage.
	static float _RateUsedTol = 0.001f;   // if _RateUsed[i] is less than _RateUsedTol (per unit), the check of flow violation on branch i would be skipped.

	
	// Set methods
	public static void setTolMVA(float sumTol, float elemTol) {setTolSumMVA(sumTol); setTolElemMVA(elemTol);}
	public static void setTolSumMVA(float a) {_TolSumBrcVio = a;}
	public static void setTolElemMVA(float a) {_TolElemBrcVio = a;}
	
	public static void setTolVm(float sumTol, float elemTol) {setTolSumVm(sumTol); setTolElemVm(elemTol);}
	public static void setTolSumVm(float a) {_TolSumVmVio = a;}
	public static void setTolElemVm(float a) {_TolElemVmVio = a;}
	
	public static void setCheckBusVmLevel(boolean mark) {_CheckBusVmLevel = mark;}
	public static void setBusHighVmLevel(float a) {_BusHighVmLevel = a;}
	public static void setVmMax(float a) {_VmMax = a;}
	public static void setVmMin(float a) {_VmMin = a;}
	
	public static void setCheckBrcVmLevel(boolean mark) {_CheckBrcVmLevel = mark;}
	public static void setBrcHighVmLevel(float a) {_BrcHighVmLevel = a;}
	public static void setIsBrcVmAbsHigh(boolean mark) {_isBrcVmAbsHigh = mark;}
	public static void setRateUsedTol(float a) {_RateUsedTol = a;}

	// Get methods
	public static float getTolSumMVA() {return _TolSumBrcVio;}
	public static float getTolElemMVA() {return _TolElemBrcVio;}
	
	public static float getTolSumVm() {return _TolSumVmVio;}
	public static float getTolElemVm() {return _TolElemVmVio;}

	public static boolean getCheckBusVmLevel() {return _CheckBusVmLevel;}
	public static float getBusHighVmLevel() {return _BusHighVmLevel;} 
	public static float getVmMax() {return _VmMax;} 
	public static float getVmMin() {return _VmMin;}
	
	public static boolean getCheckBrcVmLevel() {return _CheckBrcVmLevel;}
	public static float getBrcHighVmLevel() {return _BrcHighVmLevel;} 
	public static boolean getIsBrcVmAbsHigh() {return _isBrcVmAbsHigh;}
	public static float getRateUsedTol() {return _RateUsedTol;}
	
	
	
////////////Below For CA ONLY ///////////////////
	static boolean _markTolVio = true;         // if true: then, tolerance is applied, and record the contingencies
                                                 // that the corresponding flow violation or voltage violation is over _TolSumBrcVio or _TolSumVmVio.
	public static void setMarkTolVio(boolean mark) {_markTolVio = mark;}
	public static boolean getMarkTolVio() {return _markTolVio;}
////////////Above For CA ONLY ///////////////////




////////////Below For TS ONLY ///////////////////
static boolean _checkNewVioOccured = true;   // if true, then, check the existence of new violation after switching 
static boolean _checkContiVioWorse = true;   // if true, then, check whether contingency violation gets worse after switching.

public static void setCheckNewVioOccured(boolean a) {_checkNewVioOccured = a;}
public static void setCheckContiVioWorse(boolean a) {_checkContiVioWorse = a;}

public static boolean getCheckNewVioOccured() {return _checkNewVioOccured;}
public static boolean getCheckContiVioWorse() {return _checkContiVioWorse;}

// The situation is marked as getting worse if the diff between elem-based Vio of post-Switch and elem-based Vio of post-Contingency 
// is greater than one of the following setting values.
// or: new violation due to Switching comes up only if elem-based viol is greater than the corresponding following setting values.
static float _BrcVioElemTol = 0.02f;   // tolerance for branch-based flow violation
static float _VmVioElemTol = 0.005f;   // tolerance for bus-based voltage violation

public static void setBrcVioElemTol(float a) {_BrcVioElemTol = a;}
public static void setVmVioElemTol(float a) {_VmVioElemTol = a;}

public static float getBrcVioElemTol() {return _BrcVioElemTol;}
public static float getVmVioElemTol() {return _VmVioElemTol;}
////////////Above For TS ONLY ///////////////////



}

