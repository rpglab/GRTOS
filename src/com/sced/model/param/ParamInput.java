package com.sced.model.param;

/**
 * 
 * @author Xingpeng Li, xplipower@gmail.com 
 *
 */
public class ParamInput {
	
	//static int _inputSystemDataFormat = 1;
	
	static String _openPAGenCostFilePath = "input/sced_ver01/cascadia_2016/genCost.txt";
	static String _openPAGenRampFilePath = "input/sced_ver01/cascadia_2016/genRamp.txt";
	
	static int _optionLoss = 0;   // 0: lossless model. note that ignoring loss may compromise the solution.
	                              //     The program may NOT converge if this option (lossless model) is selected if usePTDFforSCED == true and usePkInit == true, unless totalPgInit=totalPd.
    							  //     The solution may NOT be right if lossless model is used along with setting usePTDFforSCED == true and usePkInit == true.
	                              // 1: simple proportionally added to each load based PL amount. PL,new = PL,old*( sum(Pg)/sum(PL) ).
	 							  // 2: loss added to generator buses.
    							  // 3: loss on each branch is entirely added to the actually receiving-bus as a fixed virtual load.
								  // 4: loss on each branch is entirely added to the actually sending-bus as a fixed virtual load.
    							  // 5: Lossy model is used and, the loss on each branch is added to the frmBus (50%) and toBus (50%) evenly as fixed virtual loads.

	static float _tol4BrcFlowMonitorBaseCase = 0.8f;          // tolerance in percent for determining monitor set for base case
	static float _tol4BrcFlowWarningBaseCase = 0.9f;           // tolerance in percent for determining potential violation
	static float _tol4BrcFlowVioBaseCase = 1.0f;              // typically, this parameter should be exactly 1.

	static float _tol4BrcFlowMonitorCtgcyCase = 0.8f;         // tolerance in percent for determining monitor set for contingency case
	static float _tol4BrcFlowWarningCtgcyCase = 0.9f;           // tolerance in percent for determining potential violation
	static float _tol4BrcFlowVioCtgcyCase = 1.0f;              // typically, this parameter should be exactly 1.

	static float _ratio4B = 1.05f;      // If rateB is not available, _ratio4B * rateA is used for rateB
	static float _ratio4C = 1.05f;      // If rateC is not available, _ratio4C * rateA is used for rateC
	
	/* Model setting */
	static boolean _useMainIsland = false;   // it needs to be true if the original model is not a fully connected network. This parameter is for OpenPA version 2 only.
	
	/* Settings for Power flow */
	static float _leastX = 0.0001f;
	static boolean _decoupleImpedance = true;
	static boolean _unitRegOverride = false;

	/* Settings for SCED */
	static boolean _simAllPotentialCtgcy = false; // if true, overrides all other settings; if false; the very below setting doesn't matter and it will follow other setting.
	static boolean _monALLBrc4SimAllPotentialCtgcy = true; // if true monitor all lines; it typically should be true
	static boolean _monitorAllBrcBaseCase = false;          // for base case, monitor all lines flow if true; otherwise, monitor a subset of lines.
	static boolean _monitorAllBrcCtgcyCase = false;         // for each CRITICAL contingency, monitor all lines flow if true; otherwise, monitor a subset of lines.
	static boolean _usePTDFforSCED = true;  // if false, B-theta model is then used, and note that 'usePkcInit' may still affect the SCED results.
	static boolean _usePkInit = true;       // matters only when usePTDFforSCED == true
	static boolean _usePkcInit = true;      // for branch flow calculation under contingency, it matters only when usePTDFforSCED == true, usePkInit == true, and BrcCtgcyListXL._monitorAllBrc == false
			                                // for branch flow emergency limit under contingency, it matters only when BrcCtgcyListXL._monitorAllBrc == false.
	static double _ptdfCutOffValue = 0.0001;

	//static boolean _usePkcLimit = false;       // typically _usePkcLimit and _usePkcInit should be the same
	static String _gurobiLogFileName = "gurobi_SCED.log";  // log file name for solver gurobi


	/*-----------------------------  Get methods  -----------------------------*/
//	public static int getInputSystemDataFormat() {return _inputSystemDataFormat;}
	public static String getOpenPAGenCostFilePath() {return _openPAGenCostFilePath;}
	public static String getOpenPAGenRampFilePath() {return _openPAGenRampFilePath;}
	
	/* For loss distribution */
	public static int getOptionLoss() {return _optionLoss;}
	/* For base case */
	public static float getTol4BrcFlowMonitorBaseCase() {return _tol4BrcFlowMonitorBaseCase;}
	public static float getTol4BrcFlowWarningBaseCase() {return _tol4BrcFlowWarningBaseCase;}
	public static float getTol4BrcFlowVioBaseCase() {return _tol4BrcFlowVioBaseCase;}
	/* For contingency case */
	public static float getTol4BrcFlowMonitorCtgcyCase() {return _tol4BrcFlowMonitorCtgcyCase;}
	public static float getTol4BrcFlowWarningCtgcyCase() {return _tol4BrcFlowWarningCtgcyCase;}
	public static float getTol4BrcFlowVioCtgcyCase() {return _tol4BrcFlowVioCtgcyCase;}
	/* Branch rating */
	public static float getRatio4B() {return _ratio4B;}
	public static float getRatio4C() {return _ratio4C;}
	/* Model setting */
	public static boolean getUseMainIsland() {return _useMainIsland;}
	/* Power flow setting */
	public static float getLeastX() {return _leastX;}
	public static boolean getDecoupleImpedance() {return _decoupleImpedance;}
	public static boolean getUnitRegOverride() {return _unitRegOverride;}
	/* SCED setting */
	public static boolean getSimAllPotentialCtgcy() {return _simAllPotentialCtgcy;}
	public static boolean getMonALLBrc4SimAllPotentialCtgcy() {return _monALLBrc4SimAllPotentialCtgcy;}
	public static boolean getMonitorAllBrcBaseCase() {return _monitorAllBrcBaseCase;}
	public static boolean getMonitorAllBrcCtgcyCasey() {return _monitorAllBrcCtgcyCase;}
	public static boolean getUsePTDFforSCED() {return _usePTDFforSCED;}
	public static double getPTDFCutOffValue() {return _ptdfCutOffValue;}
	public static boolean getUsePkInit() {return _usePkInit;}
	public static boolean getUsePkcInit() {return _usePkcInit;}
	public static String getGurobiLogFileName() {return _gurobiLogFileName;}
	

	/*-----------------------------  Set methods  -----------------------------*/
	//public static void setInputSystemDataFormat(int ii) {_inputSystemDataFormat = ii;}
	public static void setOpenPAGenCostFilePath(String str) {_openPAGenCostFilePath = str;}
	public static void setOpenPAGenRampFilePath(String str) {_openPAGenRampFilePath = str;}

	/** For loss distribution */
	public static void setOptionLoss(int optionLoss) {_optionLoss = optionLoss;}
	/* For base case */
	public static void setTol4BrcFlowMonitorBaseCase(float tol) {_tol4BrcFlowMonitorBaseCase = tol;}
	public static void setTol4BrcFlowWarningBaseCase(float tol) {_tol4BrcFlowWarningBaseCase = tol;}
	public static void setTol4BrcFlowVioBaseCase(float tol) {_tol4BrcFlowVioBaseCase = tol;}
	/* For contingency case */
	public static void setTol4BrcFlowMonitorCtgcyCase(float tol) {_tol4BrcFlowMonitorCtgcyCase = tol;}
	public static void setTol4BrcFlowWarningCtgcyCase(float tol) {_tol4BrcFlowWarningCtgcyCase = tol;}
	public static void setTol4BrcFlowVioCtgcyCase(float tol) {_tol4BrcFlowVioCtgcyCase = tol;}
	/* Branch rating */
	public static void setRatio4B(float ratio) {_ratio4B = ratio;}
	public static void setRatio4C(float ratio) {_ratio4C = ratio;}
	/* Model setting */
	public static void setUseMainIsland(boolean flag) {_useMainIsland = flag;}
	/* Power flow setting */
	public static void setLeastX(float leastX) {_leastX = leastX;}
	public static void setDecoupleImpedance(boolean st) {_decoupleImpedance = st;}
	public static void setUnitRegOverride(boolean st) {_unitRegOverride = st;}
	/* SCED setting */
	public static void setSimAllPotentialCtgcy(boolean st) {_simAllPotentialCtgcy = st;}
	public static void setMonALLBrc4SimAllPotentialCtgcy(boolean st) {_monALLBrc4SimAllPotentialCtgcy = st;}
	public static void setMonitorAllBrcBaseCase(boolean st) {_monitorAllBrcBaseCase = st;}
	public static void setMonitorAllBrcCtgcyCasey(boolean st) {_monitorAllBrcCtgcyCase = st;}
	public static void setUsePTDFforSCED(boolean st) {_usePTDFforSCED = st;}
	public static void setPTDFCutOffValue(double cutoffValue) {_ptdfCutOffValue = cutoffValue;}
	public static void setUsePkInit(boolean st) {_usePkInit = st;}
	public static void setUsePkcInit(boolean st) {_usePkcInit = st;}
	public static void setGurobiLogFileName(String name) {_gurobiLogFileName = name;}

	
}
