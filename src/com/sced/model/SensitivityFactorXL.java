package com.sced.model;

import com.sced.auxData.FormLODFXL;
import com.sced.auxData.FormOTDFXL;
import com.sced.auxData.FormPTDFXL;
import com.sced.model.param.ParamInput;

/**
 * Initialized in October 2016.
 * 
 * @author Xingpeng Li (xplipower@gmail.com)
 *
 */
public class SensitivityFactorXL {

	SystemModelXL _model;

	public SensitivityFactorXL(SystemModelXL model) {_model = model;}
	
	boolean _usePTDFforSCED = ParamInput.getUsePTDFforSCED(); 
	double _cutOffValue = ParamInput.getPTDFCutOffValue();
	
	double[][] _ptdfDense;

	/* NOT yet been implemented */
	boolean _useSparsePTDF; // matters only when _usePTDFforSCED == true. 
	int[][] _busIdxSparsePTDF;    // bus index which is consistent with _ptdfSparse
	double[][] _ptdfSparse;  // _ptdfSparse[2][7] is the ptdf value corresponding to the BRANCH of index 2 and the BUS of index _busIdxSparsePTDF[2][7]

	FormPTDFXL _formPTDF;
	FormLODFXL _formLODF;
	FormOTDFXL _formOTDF;
	
	private void setFormPTDF() {_formPTDF = new FormPTDFXL(_model);}
	private void setFormLODF() {_formLODF = new FormLODFXL(getFormPTDF());}
	private void setFormOTDF() {_formOTDF = new FormOTDFXL(getFormPTDF(), getFormLODF());}
	
	public FormPTDFXL getFormPTDF() {
		if (_formPTDF == null) setFormPTDF();
		return _formPTDF;
	}
	
	public void calcDensePTDF() {
		if (_formPTDF == null) setFormPTDF();
		_ptdfDense = _formPTDF.getPTDF();
	}
	
	public FormLODFXL getFormLODF() {
		if (_formLODF == null) setFormLODF();
		return _formLODF;
	}
	
	public FormOTDFXL getFormOTDF() {
		if (_formOTDF == null) setFormOTDF();
		return _formOTDF;
	}
	
	/** LODF for outage of line @idxCtgcyLine on @idxBrcMon */
	public double getLODF(int idxBrcCtgcy, int idxBrcMon) {
		return getFormLODF().getLODF(idxBrcCtgcy, idxBrcMon);
	}

	/** LODF for outage of line @idxCtgcyLine */
	public double[] getLODF(int idxBrcCtgcy) {
		return getFormLODF().getLODF(idxBrcCtgcy);
	}

	/** Get OTDF or say 'PTDF for the post-contingency network'
	 * @idxBrcCtgcy is the contingency line
	 * @idxBrcMon is the monitor line
	 * @Return an array with size nb*1. 
	 * */
	public double[] getOTDFMonBrc(int idxBrcCtgcy, int idxBrcMon) {
		return getFormOTDF().getOTDFMonBrc(idxBrcMon, idxBrcCtgcy);
	}

	/** Get OTDF or say 'PTDF for the post-contingency network'
	 * @idxBrcCtgcy is the contingency line
	 * @idxBus is the monitor line
	 * @Return an array with size nbrc*1. 
	 * */
	public double[] getOTDFInjBus(int idxBrcCtgcy, int idxBus) {
		return getFormOTDF().getOTDFInjBus(idxBus, idxBrcCtgcy);
	}

	/* Set methods */
	public void setUsePTDFforSCED(boolean flag) {_usePTDFforSCED = flag;}
	public void setUseSparsePTDF(boolean flag) {_useSparsePTDF = flag;}
	public void setDensePTDF(double[][] ptdf) {_ptdfDense = ptdf;}
	public void setCutOffValue(double value) {_cutOffValue = value;}
	public void setBusIdxSparsePTDF(int[][] ptdfIdx) {_busIdxSparsePTDF = ptdfIdx;}
	public void setSparsePTDF(double[][] ptdf) {_ptdfSparse = ptdf;}
	
	
	/* Get methods */
	public boolean isUsePTDFforSCED() {return _usePTDFforSCED;}
	public boolean isUseSparsePTDF() {return _useSparsePTDF;}
	public double[] getDensePTDF(int idxBrc) {
		if (_ptdfDense == null) calcDensePTDF();
		return _ptdfDense[idxBrc];
	}
	public double getCutOffValue() {return _cutOffValue;}
	public int[] getBusIdxSparsePTDF(int idxBrc) {return _busIdxSparsePTDF[idxBrc];}
	public double[] getSparsePTDF(int idxBrc) {return _ptdfSparse[idxBrc];}
	
	
}
