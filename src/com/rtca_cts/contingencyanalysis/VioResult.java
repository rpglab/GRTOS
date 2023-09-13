package com.rtca_cts.contingencyanalysis;


import com.powerdata.openpa.psse.ACBranchList;
import com.powerdata.openpa.psse.PsseModel;
import com.powerdata.openpa.psse.PsseModelException;
import com.rtca_cts.param.ParamVio;

/**
 * Record violations information for contingency that cause violation.
 * This routine can also be used to record violation for basic power flow.
 * 
 * Initialized in Feb. 2014.
 * 
 * @author Xingpeng.Li, xplipower@gmail.com 
 */
public class VioResult
{
	PsseModel _model;
	ACBranchList _branches;
	float[] _BaseKV;   // Voltage level, unit: KV.
	
	int _nbr;
	int _size;
	
	boolean _converged;
	boolean _Vio;  // violation exists if true.
	
	boolean _isAllBusMnt = true;
	boolean[] _isBusMnt = null;
	
	boolean _isAllBrcMnt = true;
	boolean[] _isBrcMnt = null;

    // For bus voltage violation info
	boolean _monitorVm = ParamVio.getIsMonitorVm(); // if false, then the code will not monitor voltage
	int _sizeV = 0;
	boolean _VioVoltage;   // voltag violation exists if true.
	int[] _idxV;    //  the index of bus which has voltage violation 
	float _VmMax;
	float _VmMin;
	float[]	_Vm, _Va, _VmDiff, _VmDiffAbs;  // _VmDiff denotes the difference between the N-1 practical voltages and its bounds; 
	float _VmDiffAbsMax; 
	float _sumVmDiffAbs;      // summation of voltage violations.
	int _vioVmDiffMaxInd;   // the index of bus which is with the maximum voltage difference
	
	// For branch thermal violation info
	boolean _VioBrcPf;   // flow violation exists if true.
	int _sizeBrc;  // number of branches that have thermal violation
	int[] _idxBrc;     // the indices of branches that have thermal violations
	
	// For _pfrmDire and _ptoDire, only store the branches that have violations, only one could be true while the other one must be false
	//boolean[] _pfrmDire;  // direction of the active power, if true, then, 'from' is the real power flow direction
	//boolean[] _ptoDire;   // direction of the active power, if true, then, 'to' is the real power flow direction
	float[] _RateA, _RateB, _RateC;
	float[] _RateUsed;
	float _RateUsedTol;  // if _RateUsed[i] is less than _RateUsedTol (per unit), the check of flow violation on branch i would be skipped.
	float[]	_sfrm, _sto, _sBrcDiff;
	float[]	_pfrm, _pto, _qfrm, _qto;
	float _sumBrcDiff;    // summation of all flow violations.
	float _sBrcDiffMax;
	int _sBrcDiffMaxInd; // index of branch that have the maximum violation 
	float _sBrcDiffMaxInPerc;   //  Maximum branch thermal violation in percentage.
	int _sBrcDiffMaxInPercInd; // index of branch that have the maximum violation in percentage


	public VioResult(PsseModel model, int nbr, boolean converge) throws PsseModelException 
	{
		_converged = converge;
		_nbr = nbr;
		_model = model;
		init();
	}
	
	void init() throws PsseModelException
	{
		_branches = _model.getBranches();
		_BaseKV = _model.getBuses().getBASKV();
		_VmMax = ParamVio.getVmMax();
		_VmMin = ParamVio.getVmMin();
		_RateUsedTol = ParamVio.getRateUsedTol();
	}

	public void launch(float[] Vm, float[] Va, float[] sfrm, float[] sto, float[] rating) throws PsseModelException
	{
		if (_converged)
		{
			if (_monitorVm == true) {
				setSizeV(Vm);
				setVioVoltage();
				if (_sizeV != 0) setVmVa(Vm, Va);
			}
			setSizeBrc(sfrm, sto, rating);
			setVioBrc();
			if (_sizeBrc != 0) setPf(sfrm, sto);
			setSizeVio();
		}
		else System.out.println("This Power flow does not converge");
	}

	public void launch(float[] Vm, float[] Va, float[] pfrm, float[] pto, float[] qfrm, float[] qto, float[] sfrm, float[] sto, float[] rating) throws PsseModelException
	{
		if (_converged)
		{
			if (_monitorVm == true) {
				setSizeV(Vm);
				setVioVoltage();
				if (_sizeV != 0) setVmVa(Vm, Va);
			}
			setSizeBrc(sfrm, sto, rating);
			setVioBrc();
			if (_sizeBrc != 0) { setPf(pfrm, pto, qfrm, qto, sfrm, sto);}
			setSizeVio();
		}
		else System.out.println("This Power flow does not converge");
	}

	private void setSizeV(float[] Vm)
	{
		for(int i = 0; i<Vm.length; i++)
		{
			boolean mark = true;
			if (_isAllBusMnt == false) mark = _isBusMnt[i];
			if (mark == false) continue;
			float Vmi = Vm[i];
			if ((Vmi < _VmMin) || (Vmi > _VmMax))
				_sizeV = _sizeV + 1;
		}
	}
	
	private void setVioVoltage() {_VioVoltage = (_sizeV == 0) ? false : true;}

	/** this method must be used after _sizeV is initialized **/
	private void setVmVa(float[] Vm, float[] Va)
	{
		_VmDiffAbsMax = 0;	
		_sumVmDiffAbs = 0;
		_Vm = new float[_sizeV];
		_Va = new float[_sizeV];
		_VmDiff = new float[_sizeV];
		_VmDiffAbs = new float[_sizeV];
		_idxV = new int[_sizeV];
		
		int j = 0;
		for(int i=0; i<Vm.length; i++)
		{
			boolean mark = true;
			if (_isAllBusMnt == false) mark = _isBusMnt[i];
			if (mark == false) continue;
			float Vmi = Vm[i];
			if(Vmi< _VmMin)
			{
				_idxV[j] = i;
				_Vm[j] = Vmi;
				_Va[j] = Va[i];
				_VmDiff[j] = Vmi - _VmMin;
				_VmDiffAbs[j] = _VmMin - Vmi;
				_sumVmDiffAbs +=  _VmDiffAbs[j];
				if(_VmDiffAbs[j] > Math.abs(_VmDiffAbsMax))
				{
					_VmDiffAbsMax = _VmDiff[j];
					_vioVmDiffMaxInd = j;
				}
				j++;
			}
			else if (Vmi > _VmMax)
			{
				_idxV[j] = i;
				_Vm[j] = Vmi;
				_Va[j] = Va[i];
				_VmDiff[j] = Vmi - _VmMax;
				_VmDiffAbs[j] = Vmi - _VmMax;
				_sumVmDiffAbs +=  _VmDiffAbs[j];
				if(_VmDiffAbs[j] > Math.abs(_VmDiffAbsMax))
				{
					_VmDiffAbsMax = _VmDiff[j];
					_vioVmDiffMaxInd = j;
				}
				j++;
			}
		}
	}
	
	// arguments are the power flow
	private void setSizeBrc(float[] sfrm, float[] sto, float[] rating) throws PsseModelException   
	{
		_RateUsed = rating.clone();
		for(int i=0; i<_nbr; i++)
		{
			boolean mark = true;
			if (_isAllBrcMnt == false) mark = _isBrcMnt[i];
			if (mark == false) continue;
			
			//TODO: to be optimized
			_RateUsed[i] = _RateUsed[i]/100;
			if (_RateUsed[i] < _RateUsedTol) continue;
			float a = sfrm[i];
			float b = sto[i];
			float c = Math.max(a, b);
			if (c > _RateUsed[i]) {_sizeBrc++;}
		}
	}
	
	private void setVioBrc() {_VioBrcPf = (_sizeBrc == 0) ? false : true;}

	private void setPf(float[] sfrm, float[] sto) throws PsseModelException
	{
		setPf(null, null, null, null, sfrm, sto);
	}
	private void setPf(float[] pfrm, float[] pto, float[] qfrm, float[] qto, float[] sfrm, float[] sto) throws PsseModelException
	{
		_idxBrc = new int[_sizeBrc];
		_sBrcDiff = new float[_sizeBrc];
		_sfrm = new float[_sizeBrc];
		_sto = new float[_sizeBrc];
		_pfrm = new float[_sizeBrc];
		_pto = new float[_sizeBrc];
		_qfrm = new float[_sizeBrc];
		_qto = new float[_sizeBrc];
		_sBrcDiffMax = 0;
		_sBrcDiffMaxInPerc = 0;
		_sumBrcDiff = 0;
		
		int j = 0;
		for(int i=0; i<_nbr; i++)
		{
			boolean mark = true;
			if (_isAllBrcMnt == false) mark = _isBrcMnt[i];
			if (mark == false) continue;
			if (_RateUsed[i] < _RateUsedTol) continue;
			float a = sfrm[i];
			float b = sto[i];
			float c = Math.max(a, b);
			if (c > _RateUsed[i])
			{
				_sBrcDiff[j] = c - _RateUsed[i];
				_sumBrcDiff += _sBrcDiff[j];
				if (_sBrcDiff[j] > _sBrcDiffMax)
				{
					_sBrcDiffMax = _sBrcDiff[j];
					_sBrcDiffMaxInd = j;
				}
				
				float aa = c / _RateUsed[i];
				if (aa > _sBrcDiffMaxInPerc)
				{
					_sBrcDiffMaxInPerc = aa;
					_sBrcDiffMaxInPercInd = j;					
				}
				
				_sfrm[j] = sfrm[i];
				_sto[j] = sto[i];
				if (pfrm != null)
				{
					_pfrm[j] = pfrm[i];
					_pto[j] = pto[i];
					_qfrm[j] = qfrm[i];
					_qto[j] = qto[i];
				}
				_idxBrc[j++] = i;
			}
		}
	}
	
	private void setSizeVio() {
		_size = _sizeV + _sizeBrc;
		_Vio = _VioVoltage || _VioBrcPf;
	}
		
	public int size(){ return _size;}  // number of violations including voltage violations and branch thermal limit 
    public boolean getViol() { return _Vio;}
	public boolean getConv() {return _converged;}

	// voltage violation
	public void setVmMax(float VmMax) { _VmMax = VmMax;}
	public void setVmMin(float VmMin) { _VmMin = VmMin;}
	public float[] getVm(){ return _Vm;}
	public float[] getVa(){ return _Va;}
	public boolean getVioVoltage() {return _VioVoltage;}
	public float getMaxVolDiff() {return _VmDiffAbsMax;}
	public int getMaxVioBusInd() {return _idxV[_vioVmDiffMaxInd];}
	public int[] getIdxV() { return _idxV;}
	public float[] getVmDiff() { return _VmDiff;}
	public float[] getVmDiffAbs() { return _VmDiffAbs;}
	public float getSumVmDiff() {return _sumVmDiffAbs;}
	public int sizeV() {return _sizeV;}
	
	// flow violation
	public boolean getVioBrc() 	{ return _VioBrcPf; }
	
	public float getMaxBrcVioDiff() {return _sBrcDiffMax;}
	public int getMaxVioBrcInd() {return _idxBrc[_sBrcDiffMaxInd];}
	public boolean getMaxVioBrc_FlowDirctn() {return (_pfrm[_sBrcDiffMaxInd] > 0) ? true : false;}
	public float getMaxBrcVioDiffPerct() {return _sBrcDiffMaxInPerc;}
	public int getMaxVioBrcPerctInd() {return _idxBrc[_sBrcDiffMaxInPercInd];}
	public boolean getMaxVioBrcPerct_FlowDirctn() {return (_pfrm[_sBrcDiffMaxInPercInd] > 0) ? true : false;}

	public int[] getIdxBrc() { return _idxBrc;}
	public int sizeBrc() {return _sizeBrc;}
	public float[] getBrcDiff() {return _sBrcDiff;}
	public float getSumBrcDiff() {return _sumBrcDiff;}
	public float[] getSfrm() {return _sfrm;}
	public float[] getSto() {return _sto;}
	public float[] getPfrm() {return _pfrm;}
	public float[] getPto() {return _pto;}
	public float[] getQfrm() {return _qfrm;}
	public float[] getQto() {return _qto;}
	public float[] getRateUsed() {return _RateUsed;}
	
	public float[] getRateUsedForVioBrc()
	{
		float[] rateUsedForVioBrc = new float[_sizeBrc];
		for(int i=0; i<_sizeBrc; i++)
			rateUsedForVioBrc[i] = _RateUsed[_idxBrc[i]];
		return rateUsedForVioBrc;
	}
	
	public void enableAllBrcMnt() {_isAllBrcMnt = true;}
	public void setIsBrcMnt(boolean[] isBrcMnt) {_isAllBrcMnt = false; _isBrcMnt = isBrcMnt;}

	public void enableAllBusMnt() {_isAllBusMnt = true;}
	public void setIsBusMnt(boolean[] isBusMnt) {_isAllBusMnt = false; _isBusMnt = isBusMnt;}
	
}
