package com.sced.auxData;

/** 
 * Transmission Switching Distribution Factor (TSDF)
 * 
 * Initialized in October 2018.
 * 
 * @author Xingpeng.Li (xplipower@gmail.com)
 * 
 */
public class FormTSDFXL {

	FormPTDFXL _ptdf;

	public FormTSDFXL(FormPTDFXL ptdf) {
		_ptdf = ptdf;
		if (_ptdf.getSystemModel() != null) _ptdf.getSystemModel().getDiary().hotLine("FormLODF class is initialized for calculating LODF");
	}
	
	/** TSDF for switching of line @param idxLineTS 
	 * @idxLineTS is the TS line,
	 * @idxLineMonitor is the line under monitor, probably with violation.
	 * */
	public double getTSDF(int idxLineTS, int idxLineMonitor) {
		if (idxLineMonitor == idxLineTS) return -1;
		double lodf = _ptdf.getPTDF(idxLineMonitor, _ptdf.getFrmBuses(idxLineTS)) - _ptdf.getPTDF(idxLineMonitor, _ptdf.getToBuses(idxLineTS));
		double lodfCtgcy = _ptdf.getPTDF(idxLineTS, _ptdf.getFrmBuses(idxLineTS)) - _ptdf.getPTDF(idxLineTS, _ptdf.getToBuses(idxLineTS));
		lodf = lodf / (1.0 - lodfCtgcy);
		return lodf;
	}

	/** TSDF for switching of line @param idxLineTS 
	 * @idxLineMonitor is the line under monitor, probably with violation.
	 * */
	public double[] getTSDF(int idxLineMonitor) {
		double[] tsdf = new double[_ptdf.sizeBrc()];
		double[] tsdfTS = new double[_ptdf.sizeBrc()];
		for (int i=0; i<_ptdf.sizeBrc(); i++) {
			int frmBus = _ptdf.getFrmBuses(i);
			int toBus = _ptdf.getToBuses(i);
			tsdf[i] = _ptdf.getPTDF(idxLineMonitor, frmBus) - _ptdf.getPTDF(idxLineMonitor, toBus);
			tsdfTS[i] = _ptdf.getPTDF(i, frmBus) - _ptdf.getPTDF(i, toBus);
		}
		for (int i=0; i<_ptdf.sizeBrc(); i++) {
			tsdf[i] = tsdf[i]/(1.0 - tsdfTS[i]);
		}
		tsdf[idxLineMonitor] = -1;
		//TODO to check
		return tsdf;
	}

	public double[][] getTSDF() {
		//TODO: to check
		int nbrc = _ptdf.sizeBrc();
		double[][] tsdfMatrix = new double[nbrc][nbrc];
		double[] tsdfTS = new double[nbrc];
		for (int idxTS=0; idxTS<nbrc; idxTS++)
			tsdfTS[idxTS] = _ptdf.getPTDF(idxTS, _ptdf.getFrmBuses(idxTS)) - _ptdf.getPTDF(idxTS, _ptdf.getToBuses(idxTS));
		for (int idxMon=0; idxMon<nbrc; idxMon++) {
			double[] tsdf = new double[nbrc];
			for (int idxTS=0; idxTS<nbrc; idxTS++) {
				int frmBus = _ptdf.getFrmBuses(idxTS);
				int toBus = _ptdf.getToBuses(idxTS);
				tsdf[idxTS] = _ptdf.getPTDF(idxMon, frmBus) - _ptdf.getPTDF(idxMon, toBus);
			}
			for (int idxTS=0; idxTS<_ptdf.sizeBrc(); idxTS++)
				tsdf[idxTS] = tsdf[idxTS]/(1.0 - tsdfTS[idxTS]);
			tsdf[idxMon] = -1;
			tsdfMatrix[idxMon] = tsdf;
		}
		return tsdfMatrix;
	}


}
