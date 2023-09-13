package com.casced;

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
public class PfResults4ReDispatch {
	
	DiaryXL _diary;
	PsseModel _model;

	float _tol4BrcFlowMonitorBaseCase = ParamInput.getTol4BrcFlowMonitorBaseCase();          // tolerance in percent for determining monitor set for base case
	float _tol4BrcFlowWarningBaseCase = ParamInput.getTol4BrcFlowWarningBaseCase();           // tolerance in percent for determining whether to report them
	float _tol4BrcFlowVioBaseCase = ParamInput.getTol4BrcFlowVioBaseCase();              // tolerance in percent for determining potential violation

	boolean _conv;
	int _size;
	int[] _idxBrc;        // lines to be monitored
	BrcFlowMonitorType[] _flowMonType;
	float[] _pk;         // pk,frm in MW
	float[] _pkLimit;    // Line thermal limit in MW, sqrt(RateA^2 - max(Qfrm, Qto)^2)
	float[] _percentMVA;    // 
	float[] _ratingMVA;  // MVA rating

	InterfaceVioResults _interfaceResults;

	public PfResults4ReDispatch(PsseModel model)
	{
		_model = model;
		_diary = _model.getDiary();
	}
	
	public void setInterfaceVioResults(InterfaceVioResults interfaceResults) {_interfaceResults = interfaceResults;} 
	
	public float getTol4BrcFlowMonitorBaseCase() {return _tol4BrcFlowMonitorBaseCase;}
	public float getTol4BrcFlowWarningBaseCase() {return _tol4BrcFlowWarningBaseCase;}
	public float getTol4BrcFlowVioBaseCase() {return _tol4BrcFlowVioBaseCase;}

	public void retrieveBrcInfo4SCED(boolean conv) throws PsseModelException {
		_conv = conv;
		if (_conv == true) retrieveBrcInfo4SCED(_tol4BrcFlowMonitorBaseCase, _tol4BrcFlowWarningBaseCase, _tol4BrcFlowVioBaseCase);
	}

	private void retrieveBrcInfo4SCED(float tolBrcFlowMonitor, float tolBrcFlowWarning, float tolBrcFlowVio) throws PsseModelException {
		AnalyzePfVioResults analyzer = new AnalyzePfVioResults(_model);
		analyzer.analyzeLineFlowBaseCase(tolBrcFlowMonitor, tolBrcFlowWarning, tolBrcFlowVio);
		if (_interfaceResults.isInterfacesKey(-1) == false)
			if (analyzer.sizeVioBrc() == 0) return;
		_idxBrc = analyzer.getIdxBrc();
		_flowMonType = analyzer.getBrcFlowMonType();
		_pk = analyzer.getPk();
		_pkLimit = analyzer.getLimit();
		_percentMVA = analyzer.getPercent();
		_ratingMVA = analyzer.getRating();
		_size = _idxBrc.length;
	}
	
	public void cleanup() {
//		if (_size == 0) return;
//		_idxBrc = Arrays.copyOf(_idxBrc, _size);
//		_pk = Arrays.copyOf(_pk, _size);
//		_pkLimit = Arrays.copyOf(_pkLimit, _size);
//		_percent = Arrays.copyOf(_percent, _size);
	}
	
	public boolean isPfConverged() {return _conv;}

	public int size() {return _size;}
	public int[] getIdxBrc() {return _idxBrc;}
	public BrcFlowMonitorType[] getBrcFlowMonType() {return _flowMonType;}
	public float[] getPk() {return _pk;}
	public float[] getPkLimit() {return _pkLimit;}
	public float[] getPkPercent() {return _percentMVA;}
	public float[] getRating() {return _ratingMVA;}

	/** Get the maximum of individual violation in percent */
	public float getMaxIndivdlPctVio()
	{
		float maxVioPct = 0f;
		for (int i=0; i<_size; i++) {
			float vioPct = _percentMVA[i] - 1;
			if (maxVioPct < vioPct) maxVioPct = vioPct;
		}
		return maxVioPct;
	}
	
	/** Get the sum of MVA-flow-violation in p.u. */
	public float getSumAmtVio()
	{
		float sumVioAbs = 0f;
		for (int i=0; i<_size; i++) {
			if (_flowMonType[i] != BrcFlowMonitorType.Violation) continue;
			float vioAbs = (_percentMVA[i] - 1) * _ratingMVA[i];
			sumVioAbs += vioAbs;
		}
		return sumVioAbs;
	}
	
}
