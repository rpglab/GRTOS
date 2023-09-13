package com.casced;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;

import com.powerdata.openpa.psse.ACBranchList;
import com.powerdata.openpa.psse.PsseModel;
import com.powerdata.openpa.psse.PsseModelException;
import com.rtca_cts.ausData.PowerFlowResults;
import com.sced.model.SystemModelXL;
import com.sced.model.param.ParamInput;
import com.utilxl.log.DiaryXL;
import com.utilxl.log.LogTypeXL;

/**
 * 
 * Initialized in March 2017.
 * 
 * @author Xingpeng Li (xplipower@gmail.com)
 *
 */
public class InterfaceVioResults {
	
	float _tol4BrcFlowMonitorBaseCase = ParamInput.getTol4BrcFlowMonitorBaseCase();          // tolerance in percent for determining monitor set for base case
	float _tol4BrcFlowWarningBaseCase = ParamInput.getTol4BrcFlowWarningBaseCase();           // tolerance in percent for determining whether to report them
	float _tol4BrcFlowVioBaseCase = ParamInput.getTol4BrcFlowVioBaseCase();              // tolerance in percent for determining potential violation

	float _tol4BrcFlowMonitorCtgcyCase = ParamInput.getTol4BrcFlowMonitorCtgcyCase();          // tolerance in percent for determining monitor set for contingency case
	float _tol4BrcFlowWarningCtgcyCase = ParamInput.getTol4BrcFlowWarningCtgcyCase();           // tolerance in percent for determining potential violation
	float _tol4BrcFlowVioCtgcyCase = ParamInput.getTol4BrcFlowVioCtgcyCase();              //  

	Results4ReDispatch _regResults;
	PsseModel _model;
	ACBranchList _branches;
	DiaryXL _diary;
	
	/* interface limit normal info */
	int _size;
	float[] _limitBaseCase;     // interface limit in the base case 
	int[][] _interfaceLines;  // line index
	float[][] _interfaceEmgcyLimits;  // interface total emergency limits if one line forming the interface is temperately under contingency.
	boolean[][] _interfaceLinesDirection; // if true, from-bus to to-bus; if false, to-bus to from-bus.

	/* interface limit base-case violation info */
	boolean[] _isInterfaceMonBaseCase;      // whether an interface would be monitored for base case
	float[][] _interfaceLineMWBaseCase;          // max(Pfrm, Pto)
	float[] _interfaceMWBaseCase;          // max(Pfrm, Pto)
	float[] _percentMWBaseCase;          // max(Pfrm, Pto) / limitMW
	BrcFlowMonitorType[] _flowMonTypeBaseCase;
	
	/* interface limit ctgcy-case violation info */
	int _numCtgcy;                   // # of critical contingencies, causing interface flow beyond threshold
	int[] _ctgcyList;        // critical contingency list
	boolean[][] _isInterfaceMonCtgcyCase;        // whether an interface would be monitored for contingency cases
	float[][][] _interfaceLineMWCtgcyCase;          // max(Pfrm, Pto)`
	float[][] _interfaceMWCtgcyCase;          // max(Pfrm, Pto)
	float[][] _percentMWCtgcyCase;          // max(Pfrm, Pto) / limitMW
	float[][] _limitCtgcyCase;          // limitMW
	BrcFlowMonitorType[][] _flowMonTypeCtgcyCase;
	
	public InterfaceVioResults(PsseModel model, Results4ReDispatch regResults, int numCtgcy) throws PsseModelException {
		_model = model;
		_branches = _model.getBranches();
		_regResults = regResults;
		_diary = _model.getDiary();
		init(); initVioVars(numCtgcy);
	}

	private void init() {
		/* Hard-coded as of now */
		_size = 0;
		if (_size == 0) return;
		
		_limitBaseCase = new float[_size];
		_interfaceLines = new int[_size][];
		_interfaceEmgcyLimits = new float[_size][];
		_interfaceLinesDirection = new boolean[_size][];

		//TODO: hard coded here
		_limitBaseCase[0] = 7500;
		//_interfaceLines[0] = new int[] {244,243,242,237};  // test 
		_interfaceLines[0] = new int[] {0,1,2,7};        // 
		_interfaceEmgcyLimits[0] = new float[] {4000, 4000, 2200, 600};
		_interfaceLinesDirection[0] = new boolean[] {true, true, true, true};
	}
	
	private void initVioVars(int numCtgcy)
	{
		_isInterfaceMonBaseCase = new boolean[_size];
		_interfaceLineMWBaseCase = new float[_size][];
		_interfaceMWBaseCase = new float[_size];
		_percentMWBaseCase = new float[_size];
		_flowMonTypeBaseCase = new BrcFlowMonitorType[_size];
		
		_numCtgcy = numCtgcy;
		_ctgcyList = new int[_numCtgcy];
		_isInterfaceMonCtgcyCase = new boolean[_numCtgcy][_size];
		_interfaceLineMWCtgcyCase = new float[_numCtgcy][_size][];
		_interfaceMWCtgcyCase = new float[_numCtgcy][_size];
		_percentMWCtgcyCase = new float[_numCtgcy][_size];
		_limitCtgcyCase = new float[_numCtgcy][_size];
		_flowMonTypeCtgcyCase = new BrcFlowMonitorType[_numCtgcy][_size];
		_numCtgcy = 0;
	}
	
	/** return true if interface total flow reaches beyond the monitor-limit  */
	public boolean isInterfacesKey(int idxCtgcyBrc) throws PsseModelException
	{
		if (_size == 0) return false;
		
		boolean mark = false;
		boolean[] isInterfaceMon = new boolean[_size];
		float[][] interfaceLineMWBaseCase = new float[_size][];
		float[] interfaceMW = new float[_size];
		float[] percentMW = new float[_size];
		float[] limitMW = new float[_size];
		BrcFlowMonitorType[] flowMonType = new BrcFlowMonitorType[_size];
		Arrays.fill(flowMonType, BrcFlowMonitorType.Normal);
		
		PowerFlowResults pfResults = _model.getPowerFlowResults();
		for (int i=0; i<_size; i++)
		{
			float totalFlow = 0;
			interfaceLineMWBaseCase[i] = new float[_interfaceLines[i].length];
			for (int k=0; k<_interfaceLines[i].length; k++)
			{
				int idxLine = _interfaceLines[i][k];
				if (idxLine == idxCtgcyBrc) continue;
				if (_branches.isInSvc(idxLine) == false) continue;
				
				float pfrom = pfResults.getPfrom(idxLine);
				float pto = pfResults.getPto(idxLine);
				if (Math.abs(pfrom) > Math.abs(pto)) interfaceLineMWBaseCase[i][k] = pfrom;
				else interfaceLineMWBaseCase[i][k] = -pto;

				if (_interfaceLinesDirection[i][k] == true) totalFlow += pfrom;
				else totalFlow += pto;
			}
			interfaceMW[i] = totalFlow;

			float limit = _limitBaseCase[i];
			if (idxCtgcyBrc >= 0) {
				int pos = getPos(idxCtgcyBrc, _interfaceLines[i]);
				if (pos >= 0) limit = _interfaceEmgcyLimits[i][pos];
			}
			limitMW[i] = limit;
			float ratio = _tol4BrcFlowMonitorBaseCase;
			if (idxCtgcyBrc >= 0) ratio = _tol4BrcFlowMonitorCtgcyCase;
			
			float percent = Math.abs(totalFlow/limit);
			percentMW[i] = percent;
			if (percent >= ratio) {
				isInterfaceMon[i] = true;
				mark = true;
				flowMonType[i] = BrcFlowMonitorType.Monitor;
				if (idxCtgcyBrc >= 0) {
					if (percent >= _tol4BrcFlowVioCtgcyCase) flowMonType[i] = BrcFlowMonitorType.Violation;
					else if (percent >= _tol4BrcFlowWarningCtgcyCase) flowMonType[i] = BrcFlowMonitorType.Warning;
				} else {
					if (percent >= _tol4BrcFlowVioBaseCase) flowMonType[i] = BrcFlowMonitorType.Violation;
					else if (percent >= _tol4BrcFlowWarningBaseCase) flowMonType[i] = BrcFlowMonitorType.Warning;
				}
			}
		}
		
		if (idxCtgcyBrc >= 0 && isCtgcyCritical(isInterfaceMon) == true) {
			_ctgcyList[_numCtgcy] = idxCtgcyBrc;
			_isInterfaceMonCtgcyCase[_numCtgcy] = isInterfaceMon;
			_interfaceLineMWCtgcyCase[_numCtgcy] = interfaceLineMWBaseCase;
			_interfaceMWCtgcyCase[_numCtgcy] = interfaceMW;
			_percentMWCtgcyCase[_numCtgcy] = percentMW;
			_limitCtgcyCase[_numCtgcy] = limitMW;
			_flowMonTypeCtgcyCase[_numCtgcy] = flowMonType;
			_numCtgcy++;
		} else {
			_isInterfaceMonBaseCase = isInterfaceMon;
			_interfaceLineMWBaseCase = interfaceLineMWBaseCase;
			_interfaceMWBaseCase = interfaceMW;
			_percentMWBaseCase = percentMW;
			_flowMonTypeBaseCase = flowMonType;
		}
		return mark;
	}
	
	private boolean isCtgcyCritical(boolean[] isInterfaceMon)
	{
		for (boolean flag : isInterfaceMon)
			if (flag == true) return true;
		return false;
	}

	private int getPos(int item, int[] array)
	{
		for (int i=0; i<array.length; i++)
			if (item == array[i]) return i;
		return -1;
	}

	public void cleanup() {
		_ctgcyList = Arrays.copyOf(_ctgcyList, _numCtgcy);
		_isInterfaceMonCtgcyCase = Arrays.copyOf(_isInterfaceMonCtgcyCase, _numCtgcy);
		_interfaceLineMWCtgcyCase = Arrays.copyOf(_interfaceLineMWCtgcyCase, _numCtgcy);
		_interfaceMWCtgcyCase = Arrays.copyOf(_interfaceMWCtgcyCase, _numCtgcy);
		_percentMWCtgcyCase = Arrays.copyOf(_percentMWCtgcyCase, _numCtgcy);
		_limitCtgcyCase = Arrays.copyOf(_limitCtgcyCase, _numCtgcy);
		_flowMonTypeCtgcyCase = Arrays.copyOf(_flowMonTypeCtgcyCase, _numCtgcy);
	}
	
	/* Interface data */ 
	public int getSizeInterface() {return _size;}
	public float[] getLimitBaseCase() {return _limitBaseCase;}
	public int[][] getInterfaceLines() {return _interfaceLines;}
	public float[][] getInterfaceEmgcyLimits() {return _interfaceEmgcyLimits;}
	public boolean[][] getInterfaceLinesDirection() {return _interfaceLinesDirection;}

	/* Interface base-case constraints */
	public boolean[] getIsInterfaceMonBaseCase() {return _isInterfaceMonBaseCase;}
	public float[][] getInterfaceLineMWBaseCase() {return _interfaceLineMWBaseCase;}
	public float[] getInterfaceMWBaseCase() {return _interfaceMWBaseCase;}
	public float[] getPercentMWBaseCase() {return _percentMWBaseCase;}
	public BrcFlowMonitorType[] getFlowMonTypeBaseCase() {return _flowMonTypeBaseCase;}
	
	/* Interface contingency-case constraints */
	public int getNumCtgcy() {return _numCtgcy;}
	public int[] getCtgcyList() {return _ctgcyList;}
	public boolean[][] getIsInterfaceMonCtgcyCase() {return _isInterfaceMonCtgcyCase;}
	public float[][][] getInterfaceLineMWCtgcyCase() {return _interfaceLineMWCtgcyCase;}
	public float[][] getInterfaceMWCtgcyCase() {return _interfaceMWCtgcyCase;}
	public float[][] getPercentMWCtgcyCase() {return _percentMWCtgcyCase;}
	public BrcFlowMonitorType[][] getFlowMonTypeCtgcyCase() {return _flowMonTypeCtgcyCase;}

	
	int[][] _monInterfaceLines;    // Note the difference between interface and interface lines (forming interfaces)
	float[][] _pkcInitInterfaceLines;    // Note the difference between interface and interface lines (forming interfaces)
	
	public int[][] getMonInterfaceLinesCtgcyCase() {
		if (_monInterfaceLines == null) calcInfoAllInterfaceLines();
		return _monInterfaceLines;
	}
	public float[][] getPkcInitInterfaceLinesCtgcyCase() {
		if (_pkcInitInterfaceLines == null) calcInfoAllInterfaceLines();
		return _pkcInitInterfaceLines;
	}
	
	private void calcInfoAllInterfaceLines() {
		_monInterfaceLines = new int[_numCtgcy][];
		_pkcInitInterfaceLines = new float[_numCtgcy][];
		int maxNumber = getMaxNumber(_interfaceLines) + 1;		
		for (int c=0; c<_numCtgcy; c++)
			calcInitInterfaceLines(c, maxNumber);
	}
	
	private void calcInitInterfaceLines(int c, int maxNumber)
	{
		boolean[] isActive = new boolean[maxNumber];
		float[] lineMW = new float[maxNumber];
		int count = 0;
		for (int i=0; i<_isInterfaceMonCtgcyCase[c].length; i++)
		{
			if (_isInterfaceMonCtgcyCase[c][i] == false) continue;
			for (int j=0; j<_interfaceLines[i].length; j++)
			{
				int idx = _interfaceLines[i][j];
				if (isActive[idx] == false) {
					isActive[idx] = true;
					lineMW[idx] = _interfaceLineMWCtgcyCase[c][i][j];
					count++;
				}
			}
		}
		
		int[] monLines = new int[count];
		float[] pkcInit = new float[count];
		count = 0;
		for (int i=0; i<maxNumber; i++)
		{
			if (isActive[i] == true) {
				monLines[count] = i;
				pkcInit[count++] = lineMW[i];
			}
		}
		_monInterfaceLines[c] = monLines;
		_pkcInitInterfaceLines[c] = pkcInit;
	}
	
	private int getMaxNumber(int[][] interfaceLines)
	{
		return getMaxNumber(getArrayList(interfaceLines));
	}
	private ArrayList<Integer> getArrayList(int[][] interfaceLines)
	{
		ArrayList<Integer> tmp = new ArrayList<Integer>();
		for (int i=0; i<interfaceLines.length; i++)
		{
			int size = interfaceLines[i].length;
			for (int j=0; j<size; j++)
				if (tmp.contains(interfaceLines[i][j]) == false) tmp.add(interfaceLines[i][j]);
		}
		return tmp;
	}
	private int getMaxNumber(ArrayList<Integer> array)
	{
		int maxNumber = -1;
		for (Integer a : array)
			if (a > maxNumber) maxNumber = a;
		return maxNumber;
	}
	
	
	public void dump(boolean isNm1Check) throws PsseModelException
	{
		String fileName = "ConstraintsFromCA2SCED_Interface.csv";
		if (isNm1Check == true) fileName = "Nm1CheckResults_Interface.csv";
		File pout = new File(fileName);
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(new BufferedWriter(new FileWriter(pout)));
		} catch (IOException e) {
			e.printStackTrace();
		}
		dump(pw, isNm1Check);
		pw.flush();
		pw.close();
		_diary.hotLine(LogTypeXL.Log, "File "+fileName+" has been created");
	}
	
	public void dump(PrintWriter pw, boolean isNm1Check) throws PsseModelException
	{
		if (isNm1Check == false) {
			pw.format("//---\n This file contains interface flow constraints\n");
			pw.format("They are obtained from base-case power flow study and contingency analysis study\n");
			pw.format("These contraints will be sent to DC power flow model based SCED simulation\n\n");
		} else pw.format("//---\n Results of base-case power flow check and N-1 feasibility check\n\n");

		pw.format("//--- base case\n _tol4BrcFlowMonitorBaseCase = %f\n", _tol4BrcFlowMonitorBaseCase);
		pw.format("_tol4BrcFlowWarningBaseCase = %f\n", _tol4BrcFlowWarningBaseCase);
		pw.format("_tol4BrcFlowVioBaseCase = %f\n\n", _tol4BrcFlowVioBaseCase);
		
		pw.format("//--- ctgcy case\n _tol4BrcFlowMonitorCtgcyCase = %f\n", _tol4BrcFlowMonitorCtgcyCase);
		pw.format("_tol4BrcFlowWarningCtgcyCase = %f\n", _tol4BrcFlowWarningCtgcyCase);
		pw.format("_tol4BrcFlowVioCtgcyCase = %f\n\n", _tol4BrcFlowVioCtgcyCase);

		pw.format("Note that if base-case power flow does not converge - contingency analysis run will be skipped \n");

		if (_regResults.isPfConverged() == true) pw.format("Fortunately base-case power flow does converge\n\n");
		else {pw.format("Unfortunately - base-case power flow does NOT converge\n\n"); return;}

		
		/* Main data */
		pw.println("idx,caseType,ctgcyBrcIdx,ctgcyBrcID,itemType,monitorBrcOrInterfaceIdx,monitorBrcID,"
				+ "monitorType,initFlowPfrmMW,limitMW,loadingCondition,percent,ratingMVA,vioMVA");
		
		/* Base case */
		int count = 0;
		for (int i=0; i<_size; i++)
		{
			if (_isInterfaceMonBaseCase[i] == false) continue;
			String brcFlowSt = SystemModelXL.getBrcFlowFlag(_percentMWBaseCase[i]);
			pw.format("%d,%s,%d,%s,%s,%d,%s,%s,%f,%f,%s,%f,%f,%f\n",
					(count+1),
					"baseCase",
					-1,
					"NA/Null",
					"Interface",
					(i+1),
					"NA/Null",
					_flowMonTypeBaseCase[i].toString(),
					_interfaceMWBaseCase[i],
					_limitBaseCase[i],
					brcFlowSt,
					_percentMWBaseCase[i],
					-1.0,
					-1.0);
			count++;
			
			for (int j=0; j<_interfaceLines[i].length; j++)
			{
				int idxMonBrc = _interfaceLines[i][j];
				double brcFlow = _interfaceLineMWBaseCase[i][j];
				
				pw.format("%d,%s,%d,%s,%s,%d,%s,%s,%f,%f,%s,%f,%f,%f\n",
						(count+1),
						"baseCase",
						-1,
						"NA/Null",
						"InterfaceLine",
						(idxMonBrc+1),
						_branches.getObjectID(idxMonBrc),
						"NA/Null",
						brcFlow,
						-1.0,
						"NA/Null",
						-1.0,
						-1.0,
						-1.0);
				count++;
			}
		}
		
		/* Contingency case */
		for (int c=0; c<_numCtgcy; c++)
		{
			int ctgcyBrc = _ctgcyList[c];
			String ctgcyID = _branches.getObjectID(ctgcyBrc);
			for (int i=0; i<_size; i++)
			{
				if (_isInterfaceMonCtgcyCase[c][i] == false) continue;
				String brcFlowSt = SystemModelXL.getBrcFlowFlag(_percentMWCtgcyCase[c][i]);
				pw.format("%d,%s,%d,%s,%s,%d,%s,%s,%f,%f,%s,%f,%f,%f\n",
						(count+1),
						"ctgcyCase",
						(ctgcyBrc+1),
						ctgcyID,
						"Interface",
						(i+1),
						"NA/Null",
						_flowMonTypeCtgcyCase[c][i].toString(),
						_interfaceMWCtgcyCase[c][i],
						_limitCtgcyCase[c][i],
						brcFlowSt,
						_percentMWCtgcyCase[c][i],
						-1.0,
						-1.0);
				count++;
				
				for (int j=0; j<_interfaceLines[i].length; j++)
				{
					int idxMonBrc = _interfaceLines[i][j];
					double brcFlow = _interfaceLineMWCtgcyCase[c][i][j];
					
					pw.format("%d,%s,%d,%s,%s,%d,%s,%s,%f,%f,%s,%f,%f,%f\n",
							(count+1),
							"ctgcyCase",
							(ctgcyBrc+1),
							ctgcyID,
							"InterfaceLine",
							(idxMonBrc+1),
							_branches.getObjectID(idxMonBrc),
							"NA/Null",
							brcFlow,
							-1.0,
							"NA/Null",
							-1.0,
							-1.0,
							-1.0);
					count++;
				}
			}
		}
	}

	
}
