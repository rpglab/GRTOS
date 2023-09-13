package com.rtca_cts.param;

public class ParamGenReDisptch {
	
	static boolean _checkArea = false;    // if true, then, contingency list won't include branches that not belonging to the areas of interest.
	static int[] _areaList = null;        // areas of interest in terms of generating contingency list.

	// Set methods
	public static void setCheckArea(boolean mark) {_checkArea = mark;}
	public static void setAreaList(int[] area) {_areaList = area;}

	
	// Get methods
	public static boolean getCheckArea() {return _checkArea;}
	public static int[] getAreaList() {return _areaList;}

	
}
