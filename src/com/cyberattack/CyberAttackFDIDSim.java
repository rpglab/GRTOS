package com.cyberattack;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

import com.cyberattack.data.CyberAttackLoad;
import com.utilxl.array.AuxArrayXL;

/**
 * Initialized in May, 2017.
 * 
 * Function: Power System Cyber Attack Detection.
 * - False Data Injection Detection (FDID) simulator.
 * 
 * @author Xingpeng Li (xplipower@gmail.com)
 *
 */
public class CyberAttackFDIDSim {

	/*------------------  System data  ------------------*/	
//	PAModel _paModel;
//	SystemModelXL _scedModel;
	CyberAttackDataCenter _cyberModel;
	String _fileName;
	
	double _ptdfTolValue;
	double _loadChangeTol = 0.05;
	
	int _expPower = 1;
	double _baseMVA;
	
	/* base case */
	int _threasholdNumLoad = 5;
	
	int[] _numCrtclLoad;
	DetectionType[] _flag1TopBase;
	double[] _ratio1TopBase;
	DetectionType[] _flag2TopBase;
	double[] _ratio2TopBase;

	DetectionType[] _flag1Base;
	DetectionType[] _flag2Base;
	DetectionType[] _flag3Base;
	DetectionType[] _flag4Base;
	
	double[] _ratio1Base;
	double[] _ratio2Base;
	double[] _ratio3Base;
	double[] _ratio4Base;
	
	DetectionType[] _flagS10Base;
	DetectionType[] _flag11Base;
	double[] _attackIndex12_1Base;
	double[] _attackIndex12_2Base;
	DetectionType[] _flag12Base;
	DetectionType[] _flag13Base;
	DetectionType[] _flag14Base;
	DetectionType[] _flag15Base;
	
	double[] _ratio11Base;
	double[] _ratio12Base;
	double[] _ratio13Base;
	double[] _ratio14Base;
	double[] _ratio15Base;

	
	public CyberAttackFDIDSim(CyberAttackDataCenter cyberModel) 
	{
		_cyberModel = cyberModel;
		init();
	}
	
	private void init()
	{
		_ptdfTolValue = _cyberModel.getPTDFTolValue();
		_baseMVA = _cyberModel.getMVAbase();
	}
	
	public enum DetectionType
	{
		Normal, Monitor, Warning, Danger, Unknown;
		public static DetectionType fromConfig(String cfg)
		{
			switch(cfg.toLowerCase())
			{
				case "normal": return Normal;			
				case "monitor": return Monitor;			
				case "warning": return Warning;			
				case "severe": return Danger;
				case "unknown": return Unknown;
				default: return Unknown;
			}
		}
	}
	
	public void detectBaseCase()
	{
		int[] monitorSet = _cyberModel.getCyberAttackMonitorSet().getMonitorSetPreRT();
		if (monitorSet == null) return;
		int nBrc = monitorSet.length;
		if (nBrc == 0) return;
		
		_numCrtclLoad = new int[nBrc];
		_flag1TopBase = new DetectionType[nBrc];
		_ratio1TopBase = new double[nBrc];
		_flag2TopBase = new DetectionType[nBrc];
		_ratio2TopBase = new double[nBrc];
		
		_flag1Base = new DetectionType[nBrc];
		_flag2Base = new DetectionType[nBrc];
		_flag3Base = new DetectionType[nBrc];
		_flag4Base = new DetectionType[nBrc];
		
		_flagS10Base = new DetectionType[nBrc];
		_flag11Base = new DetectionType[nBrc];
		_attackIndex12_1Base = new double[nBrc];
		_attackIndex12_2Base = new double[nBrc];
		_flag12Base = new DetectionType[nBrc];
		_flag13Base = new DetectionType[nBrc];
		_flag14Base = new DetectionType[nBrc];
		_flag15Base = new DetectionType[nBrc];
		
		_ratio1Base = new double[nBrc];
		_ratio2Base = new double[nBrc];
		_ratio3Base = new double[nBrc];
		_ratio4Base = new double[nBrc];
		
		_ratio11Base = new double[nBrc];
		_ratio12Base = new double[nBrc];
		_ratio13Base = new double[nBrc];
		_ratio14Base = new double[nBrc];
		_ratio15Base = new double[nBrc];

		double[] pkPreRT = _cyberModel.getCyberAttackBranch().getPkPreRT();
		double[] pkRT_ISO = _cyberModel.getCyberAttackBranch().getPkRT_ISO();
		double[] pkPostRT_SCED = _cyberModel.getCyberAttackBranch().getPkSCED();
		double[] rateA = _cyberModel.getCyberAttackBranch().getRateA();

		double[] pdPreRT = _cyberModel.getCyberAttackLoad().getPdPreRT(); // Pd-
		double[] pdRT_ISO = _cyberModel.getCyberAttackLoad().getPdRT_ISO(); // Pd0,ISO
		double[] deltaPd = calcDeltaPd(pdRT_ISO, pdPreRT);

		CyberAttackLoad loads = _cyberModel.getCyberAttackLoad();
		for (int i=0; i<monitorSet.length; i++) {
			int k = monitorSet[i];
			double[] ptdf = _cyberModel.getDensePTDF(k);
			int[] indicator4loads = calcIndicators4Load(pkPreRT[k], deltaPd, pdPreRT, ptdf);

			int nload = pdPreRT.length;
			int NunCriticalLoads = 0;
			double total_PTDF = 0;
			double total_deltaLoad = 0;
			boolean[] criticalLoads = new boolean[nload];
			double[] ptdf4Loads = new double[nload];
			
			for (int d=0; d<nload; d++)
			{
				int busIdx = loads.getLoadBusIdx(d);
				if (isAbsValueLTCutoffValue(ptdf[busIdx]) == true) continue;
				NunCriticalLoads++;
				total_PTDF += Math.abs(ptdf[busIdx]);
				total_deltaLoad += Math.abs(deltaPd[d]);
				criticalLoads[d] = true;
				ptdf4Loads[d] = ptdf[busIdx];
			}
			
			double[] IF_PTDF = calcIF(criticalLoads, ptdf4Loads, total_PTDF);
			double[] IF_deltaLoad = calcIF(criticalLoads, deltaPd, total_deltaLoad);
			double[] IF_all = calcIF_all(criticalLoads, ptdf4Loads, deltaPd);
			
			detectBaseCaseC1Flags(k, NunCriticalLoads, pkPreRT[k], criticalLoads, indicator4loads, IF_PTDF, IF_deltaLoad, IF_all);
			detectBaseCaseC2Flags(k, pkPreRT[k], pkRT_ISO[k], pkPostRT_SCED[k], rateA[k]);
			detectBaseCaseCompFlag(k);
		}
	}
	
	/** Malicious load change pattern detection  */
	private void detectBaseCaseC1Flags(int k, int NunCriticalLoads, double pkPreRT, boolean[] criticalLoads, 
			int[] indicator4loads, double[] IF_PTDF, double[] IF_deltaLoad, double[] IF_all)
	{
		double ratio1 = 0, ratio2 = 0, ratio3 = 0, ratio4 = 0;
		int nloads = criticalLoads.length;
		double signPkPreRT = Math.signum(pkPreRT);
		for (int d=0; d<nloads; d++) {
			if (criticalLoads[d] == false) continue;
			ratio1 += indicator4loads[d];
			ratio2 += indicator4loads[d]*IF_PTDF[d];
			ratio3 += indicator4loads[d]*IF_deltaLoad[d];
			ratio4 += indicator4loads[d]*IF_all[d];
		}
		
		_numCrtclLoad[k] = NunCriticalLoads;
		
		ratio1 = ratio1*signPkPreRT/NunCriticalLoads;
		_ratio1Base[k] = ratio1;
		_flag1Base[k] = calcDetectionType4LoadFlags(ratio1);
		
		ratio2 = ratio2*signPkPreRT;
		_ratio2Base[k] = ratio2;
		_flag2Base[k] = calcDetectionType4LoadFlags(ratio2);
		
		ratio3 = ratio3*signPkPreRT;
		_ratio3Base[k] = ratio3;
		_flag3Base[k] = calcDetectionType4LoadFlags(ratio3);
		
		ratio4 = ratio4*signPkPreRT;
		_ratio4Base[k] = ratio4;
		_flag4Base[k] = calcDetectionType4LoadFlags(ratio4);
	}
	
	/** Overload risk detection  */
	private void detectBaseCaseC2Flags(int k, double pkPreRT, double pkRT_ISO, double pkPostRT_SCED, double rateA)
	{
		double ratio11 = 0, ratio12 = 0, ratio13 = 0, ratio14 = 0, ratio15 = 0;
		double signPkPreRT = Math.signum(pkPreRT);
		double pkPostRTE = pkPostRT_SCED - pkRT_ISO + pkPreRT;
		double pctPostRT_SCED = pkPostRT_SCED/rateA;

		ratio11 = signPkPreRT*(pkPreRT - pkRT_ISO + pkPreRT)/rateA;
		ratio12 = signPkPreRT*pkPostRTE/rateA;
		_attackIndex12_1Base[k] = Math.pow(ratio12, _expPower);
		double wfactor = weightingFactor(rateA);
		_attackIndex12_2Base[k] = _attackIndex12_1Base[k] * wfactor;

		ratio13 = -signPkPreRT*(pkRT_ISO - pkPreRT)/rateA;
		ratio14 = -signPkPreRT*(pkPostRT_SCED - pkRT_ISO)/rateA;
		ratio15 = ratio13 + ratio14;
		
		_ratio11Base[k] = ratio11;
		_ratio12Base[k] = ratio12;
		_ratio13Base[k] = ratio13;
		_ratio14Base[k] = ratio14;
		_ratio15Base[k] = ratio15;
		
		_flag11Base[k] = determineBaseCaseC2Flag11(ratio11);
		_flag12Base[k] = determineBaseCaseC2Flag12(ratio12);
		_flag13Base[k] = determineBaseCaseC2Flag13(ratio13, pctPostRT_SCED);
		_flag14Base[k] = determineBaseCaseC2Flag14(ratio14, pctPostRT_SCED);
		_flag15Base[k] = determineBaseCaseC2Flag15(ratio15, pctPostRT_SCED);
	}


	private DetectionType determineBaseCaseC2Flag11(double ratio)
	{
		return determineBaseCaseC2Flags_1(ratio);
	}

	private DetectionType determineBaseCaseC2Flag12(double ratio)
	{
		return determineBaseCaseC2Flags_1(ratio);
	}
	
	private DetectionType determineBaseCaseC2Flag13(double ratio, double pct)
	{
		return determineBaseCaseC2Flags_2(ratio, pct);
	}
	
	private DetectionType determineBaseCaseC2Flag14(double ratio, double pct)
	{
		return determineBaseCaseC2Flags_2(ratio, pct);
	}
	
	private DetectionType determineBaseCaseC2Flag15(double ratio, double pct)
	{
		if (ratio >= 0.2 && (pct >= (1-ratio/2) || pct >=0.85)) return DetectionType.Danger;
		else if (ratio >= 0.15 && (pct >= (1-ratio/2) || pct >=0.9)) return DetectionType.Warning;
		else if (ratio >= 0.1 && (pct >= (1-ratio/2) || pct >=0.9)) return DetectionType.Monitor;
		else return DetectionType.Normal;
	}
	
	private DetectionType determineBaseCaseC2Flags_1(double ratio)
	{
		if (ratio >= 1.15) return DetectionType.Danger;
		else if (ratio >= 1.10) return DetectionType.Warning;
		else if (ratio >= 1.05) return DetectionType.Monitor;
		else return DetectionType.Normal;
	}
	
	private DetectionType determineBaseCaseC2Flags_2(double ratio, double pct)
	{
		if (ratio >= 0.15 && pct >= (1-ratio)) return DetectionType.Danger;
		else if (ratio >= 0.1 && pct >= 0.85) return DetectionType.Warning;
		else if (ratio > 0.05 && pct >= 0.90) return DetectionType.Monitor;
		else return DetectionType.Normal;
	}
	
	public DetectionType getDetectionType(int value)
	{
		if (value == 0) return DetectionType.Normal;
		else if (value == 1) return DetectionType.Monitor;
		else if (value == 2) return DetectionType.Warning;
		else if (value == 3) return DetectionType.Danger;
		return null;
	}
	
	public int getDetectionTypeValue(DetectionType type)
	{
		switch (type) {
			case Normal: return 0;
			case Monitor: return 1;
			case Warning: return 2;
			case Danger: return 3;
			default:
				return (int) Double.NaN;
		}
	}
	
	private void detectBaseCaseCompFlag(int k)
	{
		_flagS10Base[k] = getWorseFlag(_flag11Base[k], _flag12Base[k]);
		if (_flagS10Base[k].equals(determineBaseCaseC2Flag11(Math.max(_ratio11Base[k], _ratio12Base[k]))) == false)
		{
			System.err.println("Something is wrong, branch " + k + " , alert level does not match");
		}
		_flag1TopBase[k] = combine2OrderedFlags(k, _flag4Base[k], _flagS10Base[k]);
		//_ratio1TopBase[k] = _ratio4Base[k] * (_ratio11Base[k] + _ratio12Base[k])/2;
		_ratio1TopBase[k] = _ratio4Base[k] * Math.max(_ratio11Base[k], _ratio12Base[k]);
		
		_flag2TopBase[k] = combine2OrderedFlags(k, _flag4Base[k], _flag11Base[k]);
		_ratio2TopBase[k] = _ratio4Base[k] * _ratio11Base[k];
	}

	private DetectionType combine2OrderedFlags(int k, DetectionType flag1, DetectionType flag2)
	{
		if (_numCrtclLoad[k] < _threasholdNumLoad) return flag2;
		return combine2Flags(flag1, flag2);
	}
	
	private DetectionType combine2Flags(DetectionType flag1, DetectionType flag2)
	{
		double tmp = getDetectionTypeValue(flag1) + getDetectionTypeValue(flag2);
		int num = (int) Math.ceil(tmp/2);
		return getDetectionType(num);
	}
	
	private DetectionType getWorseFlag(DetectionType flag1, DetectionType flag2)
	{
		if (flag1.equals(DetectionType.Danger)) return DetectionType.Danger;
		if (flag2.equals(DetectionType.Danger)) return DetectionType.Danger;
		
		if (flag1.equals(DetectionType.Warning)) return DetectionType.Warning;
		if (flag2.equals(DetectionType.Warning)) return DetectionType.Warning;
		
		if (flag1.equals(DetectionType.Monitor)) return DetectionType.Monitor;
		if (flag2.equals(DetectionType.Monitor)) return DetectionType.Monitor;
		
		return DetectionType.Normal;
	}
	
	private double[] calcDeltaPd(double[] pdRT_ISO, double[] pdPreRT) {
		int nloads = pdRT_ISO.length;
		double[] deltaPd = new double[nloads];
		for (int d=0; d<nloads; d++)
			deltaPd[d] = pdRT_ISO[d] - pdPreRT[d];
		return deltaPd;
	}
	
	private boolean isAbsValueLTCutoffValue(double ptdfElem) {
		return (Math.abs(ptdfElem) < _ptdfTolValue) ? true : false;
	}
	
	private double weightingFactor(double limitAk)
	{
		double ratio = limitAk/_baseMVA;
		if (ratio > 5) return 2;
		else return (1 + ratio/5);
	}

	private int[] calcIndicators4Load(double pkPreRT, double[] deltaPd, double[] pdPreRT, double[] ptdf) {
		double tol = _loadChangeTol;
		CyberAttackLoad loads = _cyberModel.getCyberAttackLoad();
		int nloads = deltaPd.length;
		int[] indicators = new int[nloads];
		for (int d=0; d<nloads; d++) {
			double ratio = deltaPd[d]/pdPreRT[d];
			int busIdx = loads.getLoadBusIdx(d);
			if (ratio <= -tol) indicators[d] = (int) -Math.signum(ptdf[busIdx]);
			else if (ratio < tol) indicators[d] = 0;
			else indicators[d] = (int) Math.signum(ptdf[busIdx]);
		}
		return indicators;
	}
	
	private double[] calcIF(boolean[] criticalElem, double[] array, double sumAbs) {
		int size = array.length;
		double[] ImpactFactor = new double[size];
		for (int i=0; i<size; i++) {
			if (criticalElem[i] == false) continue;
			ImpactFactor[i] = Math.abs(array[i])/sumAbs;
		}
		return ImpactFactor;
	}
	
	private double[] calcIF_all(boolean[] criticalElem, double[] array1, double[] array2) {
		int size = array1.length;
		double[] newArray = new double[size];
		double sumAbs = 0;
		for (int i=0; i<size; i++) {
			if (criticalElem[i] == false) continue;
			newArray[i] = array1[i] * array2[i];
			sumAbs += Math.abs(newArray[i]);
		}
		return calcIF(criticalElem, newArray, sumAbs);
	}
	
	/* determine FDI detection flag 1 - flag 4 */
	private DetectionType calcDetectionType4LoadFlags(double ratio) {
		if (ratio >= 0.5) return DetectionType.Danger;
		else if (ratio >= 0.35) return DetectionType.Warning;
		else if (ratio >= 0.2) return DetectionType.Monitor;
		else return DetectionType.Normal;
	}
	
	/** not yet implemented */
	public void detectCtgcyCase()
	{
		int[] idxCtgcyPreRT = _cyberModel.getCyberAttackMonitorSet().getIdxCtgcyPreRT();
		int[][] monitorSets = _cyberModel.getCyberAttackMonitorSet().getMonitorCtgcySetPreRT();
		for (int c=0; c<idxCtgcyPreRT.length; c++)
		{
			int idxCtgcy = idxCtgcyPreRT[c];
			// Get OTDF
			
			int[] monitorSet = monitorSets[c];
			for (int i=0; i<monitorSet.length; i++) {
				int k = monitorSet[i];
				detectCtgcyCaseFlag1Branch(idxCtgcy, k);
				detectCtgcyCaseFlag2Branch(idxCtgcy, k);
				detectCtgcyCaseFlag3Branch(idxCtgcy, k);
			}
		}
		
	}
	private void detectCtgcyCaseFlag1Branch(int ctgcyBrc, int monBrc) {}
	private void detectCtgcyCaseFlag2Branch(int ctgcyBrc, int monBrc) {}
	private void detectCtgcyCaseFlag3Branch(int ctgcyBrc, int monBrc) {}

	public double[] calcAvgTopRatio()
	{
		int threashold = _threasholdNumLoad;
		int size = _ratio1Base.length;
		double[] a = new double[size];
		int cnt = 0;
		for (int i=0; i<size; i++)
		{
			if (_numCrtclLoad[i] < threashold) continue;
			a[cnt++] = _ratio1Base[i];
		}
		
		double[] a2 = new double[cnt];
		System.arraycopy(a, 0, a2, 0, cnt);
		Arrays.sort(a2);
		
		double b1 = calcAvgTopElem(a2, 5);
		double b2 = calcAvgTopElem(a2, 8);
		double b3 = calcAvgTopElem(a2, 10);
		double b4 = calcAvgTopElem(a2, 12);
		double b5 = calcAvgTopElem(a2, 15);
		double b6 = calcAvgTopElem(a2, 20);
		return new double[] {b1, b2, b3, b4, b5, b6};
	}
	
	private double calcAvgTopElem(double[] sortedArray, int num)
	{
		double avg = 0;
		int size = sortedArray.length;
		for (int i=0; i<num; i++)
			avg += sortedArray[size-i-1];
		avg = avg / num;
		return avg;
	}
	
	public int[] getIdxMaxElemInCAI(int num) {
		return AuxArrayXL.getIdxOfMaxElems(_ratio1TopBase, num);
	}
	public double[] getCAI() {return _ratio1TopBase;}
	public int[] getIdxALC(DetectionType type) {
		//DetectionType.Danger
		//return _flag1TopBase;
		int size = _flag1TopBase.length;
		int[] a = new int[size];
		int cnt = 0;
		for (int i=0; i<size; i++)
		{
			if (_flag1TopBase[i].equals(type) == false) continue;
			a[cnt++] = i;
		}
		int[] a2 = new int[cnt];
		System.arraycopy(a, 0, a2, 0, cnt);
		return a2;
	}
	 
	public void dump(String fileName)
	{
		int idx = fileName.lastIndexOf("/");
		if (idx < 0) _fileName = fileName;
		else _fileName = "FDIDResults_" + fileName.substring(idx+1, fileName.length()) + ".csv";
		File pout = new File(_fileName);
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(new BufferedWriter(new FileWriter(pout)));
		} catch (IOException e) {
			e.printStackTrace();
		}
		dump(pw);
		pw.flush();
		pw.close();
	}

	public void dump()
	{
		String fileName = "FDIDResults.csv";
		dump(fileName);
	}
	
	public void dump(PrintWriter pw)
	{
		/*double[] b = calcAvgTopRatio();
		System.out.println("Average of top 5 elements in ratio1: "+b[0]);
		System.out.println("Average of top 8 elements in ratio1: "+b[1]);
		System.out.println("Average of top 10 elements in ratio1: "+b[2]);
		System.out.println("Average of top 12 elements in ratio1: "+b[3]);
		System.out.println("Average of top 15 elements in ratio1: "+b[4]);
		System.out.println("Average of top 20 elements in ratio1: "+b[5]);*/

		double[] pkPreRT = _cyberModel.getCyberAttackBranch().getPkPreRT();
		double[] pkRT_ISO = _cyberModel.getCyberAttackBranch().getPkRT_ISO();
		double[] pkPostRT_SCED = _cyberModel.getCyberAttackBranch().getPkSCED();
		double[] rateA = _cyberModel.getCyberAttackBranch().getRateA();
		int[] monitorSet = _cyberModel.getCyberAttackMonitorSet().getMonitorSetPreRT();
		int nbrc = monitorSet.length;
		pw.println("idx,brcIdx,flag1Top,ratio1Top,flag2Top,ratio2Top,numCrtclLoad,flag1,flag2,flag3,flag4,flagS,flag11,flag12,flag13,flag14,flag15,ratio1,abs_R1,ratio2,ratio3,ratio4,abs_R4,ratio11,ratio12,ratioS,atkIdx1,atkIdx2,ratio13,ratio14,ratio15,pkPreRT,pkRT,pkSCED,rating");
		for (int i=0; i<nbrc; i++)
		{
			int k = monitorSet[i];
			pw.format("%d,%d,%s,%f,%s,%f,%d,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f\n",
					(i+1),
					(k+1),
					_flag1TopBase[i],
					_ratio1TopBase[i],
					_flag2TopBase[i],
					_ratio2TopBase[i],
					_numCrtclLoad[i],
					_flag1Base[i],
					_flag2Base[i],
					_flag3Base[i],
					_flag4Base[i],
					_flagS10Base[i],
					_flag11Base[i],
					_flag12Base[i],
					_flag13Base[i],
					_flag14Base[i],
					_flag15Base[i],
					_ratio1Base[i],
					Math.abs(_ratio1Base[i]),
					_ratio2Base[i],
					_ratio3Base[i],
					_ratio4Base[i],
					Math.abs(_ratio4Base[i]),
					_ratio11Base[i],
					_ratio12Base[i],
					Math.max(_ratio11Base[i], _ratio12Base[i]),
					_attackIndex12_1Base[i],
					_attackIndex12_2Base[i],
					_ratio13Base[i],
					_ratio14Base[i],
					_ratio15Base[i],
					pkPreRT[k],
					pkRT_ISO[k],
					pkPostRT_SCED[k],
					rateA[k]);
		}
	}
	
	public static void main(String[] args) throws IOException
	{
		//String path2Case = "input/FDI_model/case118_20171024_w_noise/Ln_118/L_0.2_N_0.1";
		//String path2Case = "input/FDI_model/case118_20170911_only_w_noise/N_2_7";
		String path2Case = "input/FDI_model/polish_v1/Ln_24";
		//String path2Case = "input/FDI_model/case118_20171024_const/Ln_111/L_0.1_N_0.1";
		LoadFDIData loading = new LoadFDIData(path2Case);
		loading.loadData();
		
		CyberAttackDataCenter fdiModel = loading.getFDIDataModel();
		fdiModel.getCyberAttackMonitorSet().enableMonitorAllBrc();
		
		CyberAttackFDIDSim fdidSim = new CyberAttackFDIDSim(fdiModel);
		fdidSim.detectBaseCase();
		fdidSim.dump(path2Case);
		
		System.out.println("Simulation is done here");
	}


}
