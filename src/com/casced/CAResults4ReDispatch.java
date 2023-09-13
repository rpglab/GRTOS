package com.casced;

import java.util.Arrays;

import com.powerdata.openpa.psse.PsseModel;
import com.powerdata.openpa.psse.PsseModelException;
import com.sced.model.param.ParamInput;
import com.utilxl.log.DiaryXL;

/**
 * 
 * Initialized in March 2017.
 * 
 * @author Xingpeng Li (xplipower@gmail.com)
 *
 */
public class CAResults4ReDispatch {
	
	float _tol4BrcFlowMonitorCtgcyCase = ParamInput.getTol4BrcFlowMonitorCtgcyCase();          // tolerance in percent for determining monitor set for contingency case
	float _tol4BrcFlowWarningCtgcyCase = ParamInput.getTol4BrcFlowWarningCtgcyCase();           // tolerance in percent for determining potential violation
	float _tol4BrcFlowVioCtgcyCase = ParamInput.getTol4BrcFlowVioCtgcyCase();              //  

//	int[] _ctgcyList;            // index of contingency
//	boolean[] _isCtgcyMonitor;   // if true, one or multiple lines exist in the monitor set for the associated contingency
	
	PsseModel _model;
	DiaryXL _diary;

	// Non-converged contingency info
	int _sizeNconvCtgcy;
	int[] _nonConvCtgcy;
	
	// Critical contingency info
	int _size;                   // # of critical contingencies, NOT including non-converged contingency power flow cases
	int[] _criticalCtgcy;        // critical contingency list
	int[][] _idxBrcCtgcy;        // monitor line index for all critical contingencies
	BrcFlowMonitorType[][] _flowMonType;
	float[][] _pkc;              // Pk,c,from, in MW
	float[][] _pkcLimit;         // Line thermal limit in MW, sqrt(RateC^2 - max(Qfrm, Qto)^2)
	float[][] _percentMVA;          // 
	float[][] _ratingMVA;          // MVA rating
	
	InterfaceVioResults _interfaceResults;

	/** @param size: the number of contingencies simulated */
	public CAResults4ReDispatch(PsseModel model, int size) {_model = model; _size = size; initialize();}
	
	public void setInterfaceVioResults(InterfaceVioResults interfaceResults) {_interfaceResults = interfaceResults;} 
	
	public float getTol4BrcFlowMonitorCtgcyCase() {return _tol4BrcFlowMonitorCtgcyCase;}
	public float getTol4BrcFlowWarningCtgcyCase() {return _tol4BrcFlowWarningCtgcyCase;}
	public float getTol4BrcFlowVioCtgcyCase() {return _tol4BrcFlowVioCtgcyCase;}

	private void initialize() {
		_diary = _model.getDiary();
		
		_nonConvCtgcy = new int[_size];
		
		_criticalCtgcy = new int[_size];
		_idxBrcCtgcy = new int[_size][];
		_flowMonType = new BrcFlowMonitorType[_size][];
		_pkc = new float[_size][];
		_pkcLimit = new float[_size][];
		_percentMVA = new float[_size][];
		_ratingMVA = new float[_size][];
		_size = 0;
	}
	
	public void analyzeCAResults(int idxCtgcy, boolean conv) throws PsseModelException {
		if (conv == false) {_nonConvCtgcy[_sizeNconvCtgcy++] = idxCtgcy; return;}
		analyzeCAResults(idxCtgcy, _tol4BrcFlowMonitorCtgcyCase, _tol4BrcFlowWarningCtgcyCase, _tol4BrcFlowVioCtgcyCase);
	}

	public void analyzeCAResults(int idxCtgcy, float tolBrcFlowMonitor, float tolBrcFlowWarning, float tolBrcFlowVio) throws PsseModelException {
		AnalyzePfVioResults analyzer = new AnalyzePfVioResults(_model);
		analyzer.analyzeLineFlowCtgcyCase(idxCtgcy, tolBrcFlowMonitor, tolBrcFlowWarning, tolBrcFlowVio);
		if (_interfaceResults.isInterfacesKey(idxCtgcy) == false)
			if (analyzer.sizeVioBrc() == 0) return;
		_criticalCtgcy[_size] = idxCtgcy;
		_idxBrcCtgcy[_size] = analyzer.getIdxBrc();
		_flowMonType[_size] = analyzer.getBrcFlowMonType();
		_pkc[_size] = analyzer.getPk();
		_percentMVA[_size] = analyzer.getPercent();
		_ratingMVA[_size] = analyzer.getRating();
		_pkcLimit[_size++] = analyzer.getLimit();
	}
	
	public void cleanup() {
		_nonConvCtgcy = Arrays.copyOf(_nonConvCtgcy, _sizeNconvCtgcy);
		
		_criticalCtgcy = Arrays.copyOf(_criticalCtgcy, _size);
		_idxBrcCtgcy = Arrays.copyOf(_idxBrcCtgcy, _size);
		_flowMonType = Arrays.copyOf(_flowMonType, _size);
		_pkc = Arrays.copyOf(_pkc, _size);
		_pkcLimit = Arrays.copyOf(_pkcLimit, _size);	
		_percentMVA = Arrays.copyOf(_percentMVA, _size);	
		_ratingMVA = Arrays.copyOf(_ratingMVA, _size);	
	}
	
	public int sizeNconvCtgcy() {return _sizeNconvCtgcy;}
	public int[] getNonConvCtgcy() {return _nonConvCtgcy;}
	
	public int size() {return _size;}
	public int[] getCriticalCtgcy() {return _criticalCtgcy;}
	public int[][] getIdxBrcCtgcy() {return _idxBrcCtgcy;}
	public BrcFlowMonitorType[][] getBrcFlowMonType() {return _flowMonType;}
	
	public float[][] getPkc() {return _pkc;}
	public float[][] getPkcLimit() {return _pkcLimit;}
	public float[][] getPercent() {return _percentMVA;}
	public float[][] getRating() {return _ratingMVA;}
	
	
	/** Get the maximum of individual violation in percent */
	public float getMaxIndivdlPctVio()
	{
		float maxVioPct = 0f;
		for (int c=0; c<_size; c++) {
			int nvio = _flowMonType[c].length;
			for (int i=0; i<nvio; i++)
			{
				float vioPct = _percentMVA[c][i] - 1;
				if (maxVioPct < vioPct) maxVioPct = vioPct;
			}
		}
		return maxVioPct;
	}
	
	/** Get the sum of MVA flow violation in p.u. */
	public float getSumAmtVio()
	{
		float sumVioAbs = 0f;
		for (int c=0; c<_size; c++) {
			int nvio = _flowMonType[c].length;
			for (int i=0; i<nvio; i++)
			{
				if (_flowMonType[c][i] != BrcFlowMonitorType.Violation) continue;
				float vioAbs = (_percentMVA[c][i] - 1) * _ratingMVA[c][i];
				sumVioAbs += vioAbs;
			}
		}
		return sumVioAbs;
	}

}
