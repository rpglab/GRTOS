package com.rtca_cts.ausData;

import com.powerdata.openpa.psse.PsseModel;
import com.powerdata.openpa.psse.PsseModelException;

/**
 * 
 * @author Xingpeng.Li (xplipower@gmail.com).
 * 
 * Note : 
 * 001: If another raw file is loaded, method refresh() must be called
 * 002: If rateA of one or multiple branches are changed, then clearRateA() is necessary to be called;
 * This goes the same for rateB and rateC; refresh() can be applied if rateA, rateB and rateC are all changed.
 */
public class ACBranchRates {
	
	PsseModel _model;
	
	float[] _rateA = null;
	float[] _rateB = null;
	float[] _rateC = null;
	
	int[] _frmBusIdx = null;
	int[] _toBusIdx = null;
	double[] _x = null;
		
	public ACBranchRates(PsseModel model) {_model = model;}

	public float[] getRateA() throws PsseModelException
	{
		if (_rateA != null) return _rateA;
		int nbr = _model.getBranches().size(); 
		_rateA = new float[nbr];
		int nline = _model.getLines().size();
		int nTransf = _model.getTransformers().size();		
        System.arraycopy(_model.getLines().getRATEA(), 0, _rateA, 0, nline);
        System.arraycopy(_model.getTransformers().getRATA1(), 0, _rateA, nline, nTransf);
        System.arraycopy(_model.getPhaseShifters().getRATA1(), 0, _rateA, (nline + nTransf), _model.getPhaseShifters().size());
        return _rateA;
	}
	
	public float[] getRateB() throws PsseModelException
	{
		if (_rateB != null) return _rateB;
		int nbr = _model.getBranches().size(); 
		_rateB = new float[nbr];
		int nline = _model.getLines().size();
		int nTransf = _model.getTransformers().size();		
        System.arraycopy(_model.getLines().getRATEB(), 0, _rateB, 0, nline);
        System.arraycopy(_model.getTransformers().getRATB1(), 0, _rateB, nline, nTransf);
        System.arraycopy(_model.getPhaseShifters().getRATB1(), 0, _rateB, (nline + nTransf), _model.getPhaseShifters().size());
        return _rateB;
	}
	
	public float[] getRateC() throws PsseModelException
	{
		if (_rateC != null) return _rateC;
		int nbr = _model.getBranches().size(); 
		_rateC = new float[nbr];
		int nline = _model.getLines().size();
		int nTransf = _model.getTransformers().size();
        System.arraycopy(_model.getLines().getRATEC(), 0, _rateC, 0, nline);
        System.arraycopy(_model.getTransformers().getRATC1(), 0, _rateC, nline, nTransf);
        System.arraycopy(_model.getPhaseShifters().getRATC1(), 0, _rateC, (nline + nTransf), _model.getPhaseShifters().size());
        return _rateC;
	}

	public int[] getFrmBusesIdx() throws PsseModelException
	{
		if (_frmBusIdx != null) return _frmBusIdx;
		fillBusesIdx();
        return _frmBusIdx;
	}
	
	public int[] getToBusesIdx() throws PsseModelException
	{
		if (_toBusIdx != null) return _toBusIdx;
		fillBusesIdx();
        return _toBusIdx;
	}
	
	private void fillBusesIdx() throws PsseModelException {
		int nbr = _model.getBranches().size(); 
		_frmBusIdx = new int[nbr];
		_toBusIdx = new int[nbr];
		for (int i=0; i<nbr; i++) {
			_frmBusIdx[i] = _model.getBranches().getFromBus(i).getIndex();
			_toBusIdx[i] = _model.getBranches().getToBus(i).getIndex();
		}
	}
	
	public double[] getBrcX() throws PsseModelException {
		if (_x != null) return _x;
		int nbr = _model.getBranches().size(); 
		_x = new double[nbr];
		for (int i=0; i<nbr; i++) {
			_x[i] = _model.getBranches().getX(i);
		}
        return _x;
	}

	public float getBrcX(int ndx) throws PsseModelException {
        return _model.getBranches().getX(ndx);
	}

	public void setX(int ndx, float x) throws PsseModelException 
	{
		int nline = _model.getLines().size();
		int nTransf = _model.getTransformers().size();		

		if (ndx < nline) _model.getLines().setX(ndx, x);
		else if ((ndx-=nline) < nTransf) _model.getTransformers().setX(ndx, x);
		else _model.getPhaseShifters().setX(ndx-nTransf, x);

//		if (ndx < _nlines) _lines.setX(ndx, x);
//		else if ((ndx-=_nlines) < _ntransformers) _transformers.setX(ndx, x);
//		else _phaseshifters.setX(ndx-_ntransformers, x);
		
		if (_x != null) _x[ndx] = x;
	}

	
	public void refresh()
	{
		 _rateA = null;
		 _rateB = null;
		 _rateC = null;
		 
		 _frmBusIdx = null;
		 _toBusIdx = null;
		 _x = null;
	}
	
	public void clearRateA() {_rateA = null;}
	public void clearRateB() {_rateB = null;}
	public void clearRateC() {_rateC = null;}

}
