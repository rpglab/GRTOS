package com.rtca_cts.ausData;

import com.powerdata.openpa.psse.PsseModel;
import com.powerdata.openpa.psse.PsseModelException;
import com.powerdata.openpa.psse.powerflow.FastDecoupledPowerFlow;
import com.utilxl.log.LogTypeXL;

public class PowerFlowResults {
	
	PsseModel _model;
	float _sbase;
	
	float[] _pfrom;   // in p.u.
	float[] _pto;
	float[] _qfrom;
	float[] _qto;
	
	float[] _sfrom;
	float[] _sto;
	
	public PowerFlowResults(PsseModel model) throws PsseModelException
	{
		_model = model;
		_sbase = _model.getSBASE();
	}
	
	public void setResults(FastDecoupledPowerFlow pfEngine) {
		_pfrom = pfEngine.getPfrom();
		_pto = pfEngine.getPto();
		_qfrom = pfEngine.getQfrom();
		_qto = pfEngine.getQto();
		_sfrom = pfEngine.getSfrom();
		_sto = pfEngine.getSto();
	}
	
	public float getPfrom(int k) {return _pfrom[k];}
	public float getPto(int k) {return _pto[k];}
	public float getQfrom(int k) {return _qfrom[k];}
	public float getQto(int k) {return _qto[k];}
	public float getSfrom(int k) {return _sfrom[k];}
	public float getSto(int k) {return _sto[k];}

	public float[] getPfrom() {return _pfrom;}
	public float[] getPto() {return _pto;}
	public float[] getQfrom() {return _qfrom;}
	public float[] getQto() {return _qto;}
	public float[] getSfrom() {return _sfrom;}
	public float[] getSto() {return _sto;}

	
	/** Return pfrm or -pto, which the associate absolute value is bigger */
	public float getMaxPkInit(int k) {
		if (Math.abs(_pfrom[k]) > Math.abs(_pto[k])) return _pfrom[k];
		else return -_pto[k];
	}

	
//	/** Get MW limit by fixing reactive power 
//	 * @rating should be in p.u. */
//	public float[] getPkLimit(float[] rating)
//	{
//		int size = rating.length;
//		float[] limitP = new float[size];
//		for (int i=0; i<size; i++)
//			limitP[i] = getPkLimit(i, rating[i]);
//		return limitP;
//	}
	
	/** Get MW limit by fixing reactive power
	 * @rating should be in p.u.  */
	public float getPkLimit(int k, float rating) {
		String mess = " MVar exceeds the rating "+ (rating*_sbase) +" MVA";
		if (Math.abs(_qfrom[k]) > rating) _model.getDiary().hotLine(LogTypeXL.Error, "From-side "+"Reactive power "+ (_qfrom[k]*_sbase) +mess);
		if (Math.abs(_qto[k]) > rating) _model.getDiary().hotLine(LogTypeXL.Error, "To-side "+"Reactive power "+ (_qto[k]*_sbase) +mess);
		float flowPfromLimit = (float) Math.sqrt(Math.pow(rating,2) - Math.pow(_qfrom[k],2));
		float flowPtoLimit = (float) Math.sqrt(Math.pow(rating,2) - Math.pow(_qto[k],2));
		float flowLimit = Math.min(flowPfromLimit, flowPtoLimit);
		return flowLimit;
	}
	
	
}
