package com.sced.auxData;

/**
 * Outage Transfer Distribution Factor (OTDF)
 * or, Post-contingency PTDF
 * 
 * Initialized in September 2016.
 * 
 * @author Xingpeng.Li (xplipower@gmail.com)
 *
 */
public class FormOTDFXL {
	
	FormPTDFXL _ptdf;
	FormLODFXL _lodf;

	public FormOTDFXL(FormPTDFXL ptdf, FormLODFXL lodf) {
		_ptdf = ptdf;
		_lodf = lodf;
		if (_ptdf.getSystemModel() != null) _ptdf.getSystemModel().getDiary().hotLine("FormOTDF class is initialized for calculating OTDF");
	}
	
	/** OTDF, given a bus @idxInjBus, when outage of line @param idxLineCtgcy occurs */
	public double[] getOTDFInjBus(int idxInjBus, int idxLineCtgcy) {
		return getOTDFInjBus(idxInjBus, idxLineCtgcy, _ptdf.getFrmBuses()[idxLineCtgcy], _ptdf.getToBuses()[idxLineCtgcy]);
	}
	
	/** OTDF, given a bus @idxInjBus, when outage of line @param idxLineCtgcy occurs */
	private double[] getOTDFInjBus(int idxInjBus, int idxLineCtgcy, int frmBusCtgcy, int toBusCtgcy) {
		double[] otdf = new double[_ptdf.sizeBrc()];
		double[] lodf = _lodf.getLODF(idxLineCtgcy, frmBusCtgcy, toBusCtgcy);
		for (int i=0; i<otdf.length; i++) {
			otdf[i] = _ptdf.getPTDF(i, idxInjBus) + _ptdf.getPTDF(idxLineCtgcy, idxInjBus)*lodf[i];
		}
		return otdf;
	}
	
	/** OTDF, given a bus @idxInjBus and a monitor line @idxLineMonitor, when outage of line @param idxLineCtgcy occurs */
	public double getOTDFInjBus(int idxLineMonitor, int idxInjBus, int idxLineCtgcy, int frmBusCtgcy, int toBusCtgcy) {
		double[] lodf = _lodf.getLODF(idxLineCtgcy, frmBusCtgcy, toBusCtgcy);
		double otdf = _ptdf.getPTDF(idxLineMonitor, idxInjBus) + _ptdf.getPTDF(idxLineCtgcy, idxInjBus)*lodf[idxLineMonitor];
		return otdf;
	}

	/** OTDF, given a monitor line @idxLineMonitor, when outage of line @param idxLineCtgcy occurs */
	public double[] getOTDFMonBrc(int idxLineMonitor, int idxLineCtgcy) {
		return getOTDFMonBrc(idxLineMonitor, idxLineCtgcy, _ptdf.getFrmBuses()[idxLineCtgcy], _ptdf.getToBuses()[idxLineCtgcy]);
	}

	/** OTDF, given a monitor line @idxLineMonitor, when outage of line @param idxLineCtgcy occurs */
	private double[] getOTDFMonBrc(int idxLineMonitor, int idxLineCtgcy, int frmBusCtgcy, int toBusCtgcy) {
		double[] lodf = _lodf.getLODF(idxLineCtgcy, frmBusCtgcy, toBusCtgcy);
		double[] otdf = new double[_ptdf.sizeBus()];
		for (int n=0; n<_ptdf.sizeBus(); n++)
			otdf[n] = _ptdf.getPTDF(idxLineMonitor, n) + _ptdf.getPTDF(idxLineCtgcy, n)*lodf[idxLineMonitor];
		return otdf;
	}
	

}
