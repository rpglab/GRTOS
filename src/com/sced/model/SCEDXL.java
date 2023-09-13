package com.sced.model;

import gurobi.GRBException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import com.powerdata.openpa.psse.PsseModel;
import com.powerdata.openpa.psse.PsseModelException;
import com.rtca_cts.ausData.ACBranchRates;
import com.rtca_cts.ausData.PowerFlowResults;
import com.sced.gurobi.GRB_SCEDXL;
import com.sced.input.ReadModelDataXL;
import com.sced.model.data.BranchListXL;
import com.sced.model.data.BusListXL;
import com.sced.model.data.GenListXL;
import com.sced.model.data.InterfaceListXL;
import com.sced.model.data.LoadListXL;
import com.sced.model.data.LoadListXL.LoadType;
import com.utilxl.log.DiaryXL;
import com.utilxl.log.LogTypeXL;

/**
 * Results for SCED.
 * 
 * @author Xingpeng Li, xplipower@gmail.com 
 *
 */
public class SCEDXL {
	
	DiaryXL _diary;
	SystemModelXL _sysModel;
	
	double _objValue;
	int _grbStatus;
	
	double[][] _pgi;
	double[] _pg;
	double[] _reserve;

	double[] _theta;
	
	double[] _pk;
	//boolean[] _monitor;

	double _totalPdShedded;
	double[] _pdShedded;
	
	boolean _isLoadShed;
	ArrayList<Integer> _ctgcyIdxwLoadShed;   // -1 denotes base case, 0 or positive number denotes contingency idx
	ArrayList<Integer> _loadShedIdx;        // index of shedded load
	ArrayList<Double> _loadShedAmt;     // the amount of shedded load

    double[][] _pkc;    // only save flow for monitored lines, same size with BrcCtgcyListXL._pkcInit unless BrcCtgcyListXL._monitorAllBrc == true
	
    // Dual variables
    boolean[] _isDualAvailableLimitPk;
    double[] _dualVarPostvLimitPk;
    double[] _dualVarNegtvLimitPk;

    boolean[][] _isDualAvailableLimitPkc; // only save flow for monitored lines, same size with BrcCtgcyListXL._pkcInit unless BrcCtgcyListXL._monitorAllBrc == true
    double[][] _dualVarPostvLimitPkc;     // only save flow for monitored lines, same size with BrcCtgcyListXL._pkcInit unless BrcCtgcyListXL._monitorAllBrc == true
    double[][] _dualVarNegtvLimitPkc;     // only save flow for monitored lines, same size with BrcCtgcyListXL._pkcInit unless BrcCtgcyListXL._monitorAllBrc == true
 
    /* Interface */
    double[] _pInterface;       // base case interface, rank: nInterface
    double[][] _pInterfaceCtgcy;   // Rank: nCtgcy * nInterface
    double[][][] _pkcInterfaceLineCtgcy;  // Rank: nCtgcy * nInterface * nLinesOfInterface

    /* Dual variables of interface limit constraints */
    boolean[] _isDualAvailableLimitPInterface; // Rank: nInterface
    double[] _dualVarPostvLimitPInterface;   // Rank: nInterface
    double[] _dualVarNegtvLimitPInterface;   // Rank: nInterface
    
    boolean[][] _isDualAvailableLimitPInterfaceCtgcy; // Rank: nCtgcy * nInterface
    double[][] _dualVarPostvLimitPInterfaceCtgcy;   // Rank: nCtgcy * nInterface
    double[][] _dualVarNegtvLimitPInterfaceCtgcy;   // Rank: nCtgcy * nInterface

    /* Nodal LMP */
    boolean _isSCEDModeling4LMP;
	double _sysLMP;
	double[] _nodalLMP;
	double[] _singleBrcCongestionNodalLMP;
	double[] _interfaceCongestionNodalLMP;
	double[] _pgCost;

	public SCEDXL(DiaryXL diary, SystemModelXL sysModel) {_diary = diary; _sysModel = sysModel;}
	
	public void saveResults(GRB_SCEDXL grbSolver) {
		String infoStr = "Start saving results";
		_diary.hotLine(LogTypeXL.CheckPoint, infoStr); System.out.println("\n"+infoStr);
		
		_grbStatus = grbSolver.getSolverStatus();
		_objValue = grbSolver.getObjValue();
		
		_pgi = grbSolver.getPgi();
		_pg = grbSolver.getPg();
		_reserve = grbSolver.getReserve();
		_pk = grbSolver.getPk();
		//_theta = grbSolver.getTheta());
		_pdShedded = grbSolver.getPdShedded();
		_pkc = grbSolver.getPkc();
		
		/* Interface */
		_pInterface = grbSolver.getPInterface();
		_pInterfaceCtgcy = grbSolver.getPInterfaceCtgcy();
		_pkcInterfaceLineCtgcy = grbSolver.getPkcInterfaceLineCtgcy();

		/* Dual variable of branch limit constraints */
	    _isDualAvailableLimitPk = grbSolver.getIsDualAvailableLimitPk();
	    _dualVarPostvLimitPk = grbSolver.getDualVarPostvLimitPk();
	    _dualVarNegtvLimitPk = grbSolver.getDualVarNegtvLimitPk();

	    _isDualAvailableLimitPkc = grbSolver.getIsDualAvailableLimitPkc(); 
	    _dualVarPostvLimitPkc = grbSolver.getDualVarPostvLimitPkc();     
	    _dualVarNegtvLimitPkc = grbSolver.getDualVarNegtvLimitPkc();   

	    
		/* Dual variable of interface limit constraints */
	    _isDualAvailableLimitPInterface = grbSolver.getIsDualAvailableLimitInterface();
	    _dualVarPostvLimitPInterface = grbSolver.getDualVarPostvLimitInterface();
	    _dualVarNegtvLimitPInterface = grbSolver.getDualVarNegtvLimitInterface();
	    
	    _isDualAvailableLimitPInterfaceCtgcy = grbSolver.getIsDualAvailableLimitInterfaceCtgcy();
	    _dualVarPostvLimitPInterfaceCtgcy = grbSolver.getDualVarPostvLimitInterfaceCtgcy();
	    _dualVarNegtvLimitPInterfaceCtgcy = grbSolver.getDualVarNegtvLimitInterfaceCtgcy();

	    calcNodeLMP(grbSolver);
		dataCheck(grbSolver);
		infoStr = "Complete saving results and data checking";
		_diary.hotLine(LogTypeXL.CheckPoint, infoStr); System.out.println("\n"+infoStr);
	}
	
	private void calcNodeLMP(GRB_SCEDXL grbSolver) 
	{
		//_isSCEDModeling4LMP = true;
		String infoStr = "Start calculating LMP";
		_diary.hotLine(LogTypeXL.CheckPoint, infoStr); System.out.println(infoStr);

	    _isSCEDModeling4LMP = grbSolver.getIsSCEDModeling4LMP();
	    if (_isSCEDModeling4LMP == false) return;
	    
	    calcSingleBrcCongestionNodaLMP(grbSolver);
	    calcInterfaceCongestionNodaLMP(grbSolver);
	    
	    if (_sysModel.getSenstvtFactor().isUsePTDFforSCED() == true) {
	    	_sysLMP = grbSolver.getSysLMP();
			int nbuses = _sysModel.getBuses().size();
	    	_nodalLMP = new double[nbuses];
	    	for (int n=0; n<nbuses; n++)
	    		_nodalLMP[n] = _sysLMP + _singleBrcCongestionNodalLMP[n] + ((_interfaceCongestionNodalLMP != null) ? _interfaceCongestionNodalLMP[n] : 0);
	    } else _nodalLMP = grbSolver.getNodalLMP();
	    
	    infoStr = "Finish calculating LMP";
		_diary.hotLine(LogTypeXL.CheckPoint, infoStr); System.out.println("\n"+infoStr);
	}
	
	private void calcSingleBrcCongestionNodaLMP(GRB_SCEDXL grbSolver) 
	{
    	SensitivityFactorXL senstvtFactor = _sysModel.getSenstvtFactor();
    	double[][] ptdf = senstvtFactor.getFormPTDF().getPTDF();
		int nbuses = _sysModel.getBuses().size();
    	int nbrcs = _sysModel.getBranches().size();
		int nCtgcy = _sysModel.getKeyBrcList().size();

		/* single branch limit */ 
		_singleBrcCongestionNodalLMP = new double[nbuses];
	    for (int n=0; n<nbuses; n++) 
	    {
			/* base-case branch limit */
			for (int k=0; k<nbrcs; k++) {
				if (_isDualAvailableLimitPk[k] == false) continue;
				_singleBrcCongestionNodalLMP[n] += ptdf[k][n]*(_dualVarPostvLimitPk[k] + _dualVarNegtvLimitPk[k]);
			}
			
			/* contingency-case branch limit */
			if (_sysModel.getKeyBrcList().getIsMonitorAllBrc() == true) {
				for (int c=0; c<nCtgcy; c++) 
				{
		    		int idxCtgcyBrc = _sysModel.getKeyBrcList().getCtgcyBrcIdx(c);
		    		double[] otdf = senstvtFactor.getOTDFInjBus(idxCtgcyBrc, n);
					for (int k=0; k<nbrcs; k++) 
					{
						if (_isDualAvailableLimitPkc[c][k] == false) continue;
						_singleBrcCongestionNodalLMP[n] += otdf[k]*(_dualVarPostvLimitPkc[c][k] + _dualVarNegtvLimitPkc[c][k]);
					}
				}
			} else {
				for (int c=0; c<nCtgcy; c++) 
				{
					int[] monitorSet = _sysModel.getKeyBrcList().getCtgcyCaseMonitorSet(c);
					if (monitorSet == null) continue;
		    		int idxCtgcyBrc = _sysModel.getKeyBrcList().getCtgcyBrcIdx(c);
		    		double[] otdf = senstvtFactor.getOTDFInjBus(idxCtgcyBrc, n);
					int nMonitor = monitorSet.length;
					for (int i=0; i<nMonitor; i++) 
					{
						int idxBrc = monitorSet[i];
						if (_isDualAvailableLimitPkc[c][i] == false) continue;
						_singleBrcCongestionNodalLMP[n] += otdf[idxBrc]*(_dualVarPostvLimitPkc[c][i] + _dualVarNegtvLimitPkc[c][i]);
					}
				}
			}
		}
	}
	
	private void calcInterfaceCongestionNodaLMP(GRB_SCEDXL grbSolver) 
	{
    	SensitivityFactorXL senstvtFactor = _sysModel.getSenstvtFactor();
    	double[][] ptdf = senstvtFactor.getFormPTDF().getPTDF();
		int nbuses = _sysModel.getBuses().size();
		int nCtgcy = _sysModel.getKeyBrcList().size();

	    /* interface limit */
	    int nInterface = _sysModel.getInterfaceList().size();
	    if (nInterface == 0) return;
	    
		_interfaceCongestionNodalLMP = new double[nbuses];
		int[][] interfaceLines = _sysModel.getInterfaceList().getInterfaceLines();
		boolean[][] interfaceLinesDirection = _sysModel.getInterfaceList().getInterfaceLinesDirection();
	    for (int n=0; n<nbuses; n++)
	    {
	    	/* base case interface limit */
		    for (int i=0; i<nInterface; i++) 
		    {
		    	if (_isDualAvailableLimitPInterface[i] == false) continue;
		    	for (int j=0; j<interfaceLines[i].length; j++)
		    	{
					int idxBrc = interfaceLines[i][j];
					if (interfaceLinesDirection[i][j] == true) _interfaceCongestionNodalLMP[n] += ptdf[idxBrc][n]*(_dualVarPostvLimitPInterface[i] - _dualVarNegtvLimitPInterface[i]);
					else _interfaceCongestionNodalLMP[n] -= ptdf[idxBrc][n]*(_dualVarPostvLimitPInterface[i] - _dualVarNegtvLimitPInterface[i]);
		    	}
		    }

	    	/* contingency case interface limit */
			for (int c=0; c<nCtgcy; c++)
			{
				if (_sysModel.getKeyBrcList().isCtgcyActive(c) == false) continue;
				if (_sysModel.getKeyBrcList().isCtgcyActive4Interface(c) == false) continue;
	    		int idxCtgcyBrc = _sysModel.getKeyBrcList().getCtgcyBrcIdx(c);
	    		double[] otdf = senstvtFactor.getOTDFInjBus(idxCtgcyBrc, n);
	    		
			    for (int i=0; i<nInterface; i++) {
		    		if (_isDualAvailableLimitPInterfaceCtgcy[c][i] == false) continue;
			    	for (int j=0; j<interfaceLines[i].length; j++)
			    	{
						int idxBrc = interfaceLines[i][j];
						if (interfaceLinesDirection[i][j] == true) _interfaceCongestionNodalLMP[n] += otdf[idxBrc]*(_dualVarPostvLimitPInterfaceCtgcy[c][i] - _dualVarNegtvLimitPInterfaceCtgcy[c][i]);
						else _interfaceCongestionNodalLMP[n] -= otdf[idxBrc]*(_dualVarPostvLimitPInterfaceCtgcy[c][i] - _dualVarNegtvLimitPInterfaceCtgcy[c][i]);
			    	}
			    }
			}
	    }
	}

	private void dataCheck(GRB_SCEDXL grbSolver) {
		String infoStatus = "The status of solver is "+_grbStatus+", note that 2 denotes OPTIMAL";
		String inforObj = "The objective value for SCED is $"+_objValue;
		_diary.hotLine(LogTypeXL.CheckPoint, infoStatus); System.out.println("\n"+infoStatus);
		_diary.hotLine(LogTypeXL.CheckPoint, inforObj); System.out.println(inforObj);

		_totalPdShedded = sum(_pdShedded);
		String infoLdShed = "Total shedded load for base case: "+_totalPdShedded+" p.u.";
		_diary.hotLine(LogTypeXL.CheckPoint, infoLdShed);
		if (_totalPdShedded > 0) {
			System.err.println(infoLdShed);
			_isLoadShed = true;
			for (int i=0; i<_pdShedded.length; i++)
				if(_pdShedded[i] > 0) addItem(-1, i, _pdShedded[i]);
		}
		
		int countCtgcy = 0;
		double[][] pdSheddedCtgcy = grbSolver.getPdSheddedCtgcy();
		int numCtgcyShed = 0;
		if (pdSheddedCtgcy != null) numCtgcyShed = pdSheddedCtgcy.length;
		for (int c=0; c<numCtgcyShed; c++)
		{
			double[] tmpShed = pdSheddedCtgcy[c];
			double sumTmpShed = sum(tmpShed);
			if (sumTmpShed > 0) 
			{
				countCtgcy++;
				_isLoadShed = true;
				_diary.hotLine(LogTypeXL.CheckPoint, "Total shedded load for contingency "+(c+1)+" : "+sumTmpShed+" p.u.");
				for (int j=0; j<tmpShed.length; j++)
					if(tmpShed[j] > 0) {
						addItem(c, j, tmpShed[j]);
						//_diary.hotLine(LogTypeXL.Error, "Load "+(j+1)+" shed load "+tmpShed[j]+" p.u. for contingency "+(c+1));
					}
			}
		}
		String infoLdShedCtgcy = "# of contingencies that cause load shedding: "+countCtgcy;
		_diary.hotLine(LogTypeXL.CheckPoint, infoLdShedCtgcy);
		if (countCtgcy > 0) System.err.println(infoLdShedCtgcy);
		System.out.println();
	}
	
	private double sum(double[] array) {
		double total = 0;
		for (int i=0; i<array.length; i++)
			total += array[i];
		return total;
	}
	
	private void addItem(int ctgcyIdx, int shedLoadIdx, double amount)
	{
		if (_ctgcyIdxwLoadShed == null) initLoadShedVars();
		_ctgcyIdxwLoadShed.add(ctgcyIdx);
		_loadShedIdx.add(shedLoadIdx);
		_loadShedAmt.add(amount);
	}
	
	private void initLoadShedVars() {
		_ctgcyIdxwLoadShed = new ArrayList<Integer>();
		_loadShedIdx = new ArrayList<Integer>();
		_loadShedAmt = new ArrayList<Double>();
	}
	
	public double getObjValue() {return _objValue;}
	public int getSolverStatus() {return _grbStatus;}
	
	public double[] getPg() {return _pg;}
	public double[] getPk() {return _pk;}
	public double[] getReserve() {return _reserve;}
	public double[] getTheta() {return _theta;}
	
	/** Dump SCED results */
	public void dumpSCEDResults(PsseModel modelPA) throws PsseModelException
	{
		_diary.hotLine("SCED results start to dump to the files");
		dumpSCEDResults(false);
		dumpBrc(modelPA);
		_diary.hotLine("All SCED results are dumpped to the files");
	}
	
	private void dumpSCEDResults(boolean isDumpBrc) {
		dumpLoadShed();
		dumpCostCurve();
		dumpGen();
		if (isDumpBrc == true) dumpBrc();
		dumpBrcConstraints();
		dumpInterface();
		dumpNodalLMP();
	}

	/** For both base-case and contingency-case */
	public void dumpLoadShed()
	{
		if (_isLoadShed == false) return;
		String fileName = "SCEDResult_LoadShed.csv";
		File pout = new File(fileName);
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(new BufferedWriter(new FileWriter(pout)));
		} catch (IOException e) {
			e.printStackTrace();
		}
		dumpLoadShed(pw);
		pw.flush();
		pw.close();
		_diary.hotLine("SCED loadShed results are dumpped to the file "+fileName);
	}
	
	/** For both base-case and contingency-case */
	public void dumpLoadShed(PrintWriter pw)
	{
		double baseMVA = _sysModel.getMVAbase();
		LoadListXL loads = _sysModel.getLoads();
		BranchListXL branches = _sysModel.getBranches();
		pw.println("idx,ctgcyIdx,ctgcyBrcID,loadIdx,loadBusIdx,loadBusID,status,pLoadShed,pLoad,loadType");
		for (int i=0; i<_ctgcyIdxwLoadShed.size(); i++)
		{
			int idxCtgcy = _ctgcyIdxwLoadShed.get(i);
			String ctgcyBrcID = "baseCase";
			if (idxCtgcy >= 0) ctgcyBrcID = branches.getID(_sysModel.getKeyBrcList().getCtgcyBrcIdx(idxCtgcy));
			int idxLoad = _loadShedIdx.get(i);
			int idxBus = loads.getBusIdx(idxLoad);
			
			int st = 0;
			if (loads.isInSvc(idxLoad) == true) st = 1;
			pw.format("%d,%d,%s,%d,%d,%s,%d,%f,%f,%s\n",
					(i+1),
					(idxCtgcy+1),
					ctgcyBrcID,
					(idxLoad+1),
					(idxBus+1),
					_sysModel.getBuses().getBusID(idxBus),
					st,
					_loadShedAmt.get(i)*baseMVA,
					loads.getPLoad(idxLoad)*baseMVA,
					loads.getLoadType(idxLoad).toString());
		}
	}
	
	
	public void dumpGen()
	{
		String fileName = "SCEDResult_Gen.csv";
		File pout = new File(fileName);
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(new BufferedWriter(new FileWriter(pout)));
		} catch (IOException e) {
			e.printStackTrace();
		}
		dumpGen(pw);
		pw.flush();
		pw.close();
		_diary.hotLine("SCED gen results are dumpped to the file "+fileName);
	}
	
	public void dumpGen(PrintWriter pw)
	{
		pw.println("Generators' outputs are for base case");
		double baseMVA = _sysModel.getMVAbase();
		GenListXL gens = _sysModel.getGens();
		pw.print("genIdx,genBusIdx,genBusID,status,reserve,pgSCED,pgInit,diffPg,absDiffPg,pgmax,pgmin,energyRamp,spinRamp,costCurveFlag");
		if (_nodalLMP != null) pw.print(",LMP,revenue");
		if (_pgCost != null) pw.println(",cost");
		else pw.println("cost");
		for (int i=0; i<gens.size(); i++)
		{
			int st = 0;
			if (gens.isInSvc(i) == true) st = 1;
			int hasCostCurve = 1;
			if (gens.hasCostCurveFlag(i) == false) hasCostCurve = 0;
			int busIdx = gens.getBusIdx(i);
			double diffPg = _pg[i] - gens.getPgInit(i);
			pw.format("%d,%d,%s,%d,%f,%f,%f,%f,%f,%f,%f,%f,%f,%d",
					(i+1),
					(busIdx+1),
					_sysModel.getBuses().getBusID(busIdx),
					st,
					_reserve[i]*baseMVA,
					_pg[i]*baseMVA,
					gens.getPgInit(i)*baseMVA,
					diffPg*baseMVA,
					Math.abs(diffPg)*baseMVA,
					gens.getPgmax(i)*baseMVA,
					gens.getPgmin(i)*baseMVA,
					gens.getEnergyRamp(i)*baseMVA,
					gens.getSpinRamp(i)*baseMVA,
					hasCostCurve);
			if (_nodalLMP != null) pw.format(",%f,%f", _nodalLMP[busIdx]/baseMVA, _nodalLMP[busIdx]*_pg[i]);
			if (_pgCost != null) pw.format(",%f\n",_pgCost[i]);
			else pw.format("\n");
		}
	}
	
	
	public void dumpCostCurve()
	{
		String fileName = "SCEDResult_GenCostCurve.csv";
		File pout = new File(fileName);
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(new BufferedWriter(new FileWriter(pout)));
		} catch (IOException e) {
			e.printStackTrace();
		}
		dumpCostCurve(pw);
		pw.flush();
		pw.close();
		_diary.hotLine("SCED generator cost curve (Pgi) results is dumpped to the file "+fileName);
	}
	
	public void dumpCostCurve(PrintWriter pw)
	{
		int ngens = _sysModel.getGens().size();
		_pgCost = new double[ngens];
		double[][] segmentBreadth = _sysModel.getGens().getSegmentBreadth();
		double[][] segmentPrice = _sysModel.getGens().getSegmentPrice();
		pw.println("idx,genIdx,segmentIdx,segmentBreadth,pgi,segLeftCap,segmentPrice");
		double baseMVA = _sysModel.getMVAbase();
		int count = 0;
		for (int g=0; g<ngens; g++)
		{
			if (_sysModel.getGens().hasCostCurveFlag(g) == false) continue;
			int idxMap = _sysModel.getGens().getIdxMapToCostCurve(g);
			for (int i=0; i<segmentBreadth[idxMap].length; i++)
			{
				_pgCost[g] += _pgi[idxMap][i]*segmentPrice[idxMap][i];
				count++;
				pw.format("%d,%d,%d,%f,%f,%f,%f\n",
						count,
						(g+1),
						(i+1),
						segmentBreadth[idxMap][i]*baseMVA,
						_pgi[idxMap][i]*baseMVA,
						(segmentBreadth[idxMap][i] - _pgi[idxMap][i])*baseMVA,
						segmentPrice[idxMap][i]/baseMVA);
			}
		}
	}
	
	public void dumpBrc() {dumpBrc(null, null);}
	public void dumpBrc(PsseModel model) throws PsseModelException {
		PowerFlowResults pfResults = model.getPowerFlowResults();
		ACBranchRates ratings = model.getACBrcCapData();
		float[] rateA = ratings.getRateA();
		double baseMVA = _sysModel.getMVAbase();
		
		int size = model.getBranches().size();
		double[] pfrm = new double[size];
		double[] limitA = new double[size];
		for (int i=0; i<size; i++)
		{
			float rating = (float) (rateA[i]/baseMVA);
			pfrm[i] = pfResults.getMaxPkInit(i);
			limitA[i] = pfResults.getPkLimit(i, rating);
		}
		dumpBrc(pfrm, limitA);
	}
	public void dumpBrc(double[] pkPostSCEDCA, double[] pkLimitPostSCEDCA)
	{
		String fileName = "SCEDResult_Brc.csv";
		File pout = new File(fileName);
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(new BufferedWriter(new FileWriter(pout)));
		} catch (IOException e) {
			e.printStackTrace();
		}
		dumpBrc(pw, pkPostSCEDCA, pkLimitPostSCEDCA);
		pw.flush();
		pw.close();
		_diary.hotLine("SCED branch results are dumpped to the file "+fileName);
	}
	
	public void dumpBrc(PrintWriter pw, double[] pkPostSCEDCA, double[] pkLimitPostSCEDCA)
	{
		double baseMVA = _sysModel.getMVAbase();
		pw.println("Branch flows are for base case");
		pw.println("The unit for dual variables is  $/MW  --- NOT $/p.u.");
		BranchListXL branches = _sysModel.getBranches();
		String isBrcRadial = "Unknown";
		boolean[] brcRadialFlag = branches.getIsBrcRadial();
		pw.print("brcIdx,fromBusIdx,toBusIdx,branchID,status,isBrcRadial,pkInit,pkLimitA,pkSCED,flowCalcFlagSCED,dualVarFlag,dualVarPostvLimit,dualVarNegtvLimit");
		if (pkPostSCEDCA != null) pw.println(",pkInitNm1Check,pkLimitANm1Check");
		else pw.println();
		
		boolean[] isBrcFlowCalc = _sysModel.getIsBrcFlowCalc4BaseCase();
		for (int i=0; i<branches.size(); i++)
		{
			int st = 0;
			if (branches.isInSvc(i) == true) st = 1;
			if (brcRadialFlag != null) {
				if (brcRadialFlag[i] == true) isBrcRadial = "Yes";
				else isBrcRadial = "No";
			}
			int isFlowCalcFlag = 0;
			if (isBrcFlowCalc[i] == true) isFlowCalcFlag = 1;
			double[] normalRating = branches.getBrcNormalRating();
			
			String dualVarFlag = "NA";
			double dualVarPostv = 0;
			double dualVarNegtv = 0;
			if (_isDualAvailableLimitPk[i] == true) {
				dualVarFlag = "Available";
				dualVarPostv = _dualVarPostvLimitPk[i];
				dualVarNegtv = _dualVarNegtvLimitPk[i];
			}
			
			pw.format("%d,%d,%d,%s,%d,%s,%f,%f,%f,%d,%s,%f,%f",
					(i+1),
					(branches.getFrmBusIdx(i)+1),
					(branches.getToBusIdx(i)+1),
					branches.getID(i),
					st,
					isBrcRadial,
					branches.getPkInit(i)*baseMVA,
					normalRating[i]*baseMVA,
					_pk[i]*baseMVA,
					isFlowCalcFlag,
					dualVarFlag,
					dualVarPostv/baseMVA,
					dualVarNegtv/baseMVA);
			if (pkPostSCEDCA != null) pw.format(",%f,%f\n", pkPostSCEDCA[i]*baseMVA, pkLimitPostSCEDCA[i]*baseMVA);
			else pw.format("\n");
		}
	}

	
	public void dumpBrcConstraints()
	{
		String fileName = "SCEDResult_BrcConstraints.csv";
		File pout = new File(fileName);
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(new BufferedWriter(new FileWriter(pout)));
		} catch (IOException e) {
			e.printStackTrace();
		}
		dumpBrcConstraints(pw);
		pw.flush();
		pw.close();
		_diary.hotLine("SCED branch constraints results are dumpped to the file "+fileName);
	}
	
	public void dumpBrcConstraints(PrintWriter pw)
	{
		double baseMVA = _sysModel.getMVAbase();
		pw.println("This file ONLY reports the results for the LAST iteration SCED");
		pw.println("The unit for dual variables is $/MW --- NOT $/p.u.");
		pw.println("idx,caseType,ctgcyBrcIdx,ctgcyBrcID,monBrcIdx,monBrcID,isConstModeled,brcFlowSCED,brcRatingSCED,percent,condition,dualVarFlag,dualVarPostv,dualVarNegtvLimit");
		BranchListXL branches = _sysModel.getBranches();
		
		/* Base case constraints */
		int count = 0;
		MonitorSetXL monSet = _sysModel.getMonitorSet();
		if (monSet.getIsMonitorAllBrc() == true) {
			double[] rating = branches.getBrcNormalRating();
			count = branches.size();
			for (int i=0; i<count; i++) {
				double percent = Math.abs(_pk[i]/rating[i]);
				String brcFlowSt = SystemModelXL.getBrcFlowFlag(percent);
				
				String dualVarFlag = "NA";
				double dualVarPostv = 0;
				double dualVarNegtv = 0;
				if (_isDualAvailableLimitPk[i] == true) {
					dualVarFlag = "Available";
					dualVarPostv = _dualVarPostvLimitPk[i];
					dualVarNegtv = _dualVarNegtvLimitPk[i];
				}

				pw.format("%d,%s,%d,%s,%d,%s,%s,%f,%f,%f,%s,%s,%f,%f\n",
						(i+1),
						("baseCase"),
						-1,
						"NA/Null",
						(i+1),
						branches.getID(i),
						"Yes",
						_pk[i]*baseMVA,
						rating[i]*baseMVA,
						percent,
						brcFlowSt,
						dualVarFlag,
						dualVarPostv/baseMVA,
						dualVarNegtv/baseMVA);
			}
		} else {
			count = monSet.size();
			int[] brcSet = monSet.getMonitorBrcSet();
			double[] rating = monSet.getPkLimit();
			boolean[] isConstModel = monSet.getIsConstActive();
			for (int i=0; i<count; i++) {
				int idxBrc = brcSet[i];
				double percent = Math.abs(_pk[idxBrc]/rating[i]);
				String brcFlowSt = SystemModelXL.getBrcFlowFlag(percent);
				
				String dualVarFlag = "NA";
				double dualVarPostv = 0;
				double dualVarNegtv = 0;
				if (_isDualAvailableLimitPk[idxBrc] == true) {
					dualVarFlag = "Available";
					dualVarPostv = _dualVarPostvLimitPk[idxBrc];
					dualVarNegtv = _dualVarNegtvLimitPk[idxBrc];
				}
				String isModeled = "Yes";
				if (isConstModel != null && isConstModel[i] == false) isModeled = "No";

				pw.format("%d,%s,%d,%s,%d,%s,%s,%f,%f,%f,%s,%s,%f,%f\n",
						(i+1),
						("baseCase"),
						-1,
						"NA/Null",
						(idxBrc+1),
						branches.getID(idxBrc),
						isModeled,
						_pk[idxBrc]*baseMVA,
						rating[i]*baseMVA,
						percent,
						brcFlowSt,
						dualVarFlag,
						dualVarPostv/baseMVA,
						dualVarNegtv/baseMVA);
			}
		}
		
		/* Contingency case constraints */
		BrcCtgcyListXL brcCtgcyList = _sysModel.getKeyBrcList();
		double[] rating = branches.getBrcCtgcyRating();
		int nCtgcy = brcCtgcyList.size();
		if (brcCtgcyList.getIsMonitorAllBrc() == true) {
			int nBrc = rating.length;
			for (int c=0; c<nCtgcy; c++)
			{
				int idxCtgcyBrc = brcCtgcyList.getCtgcyBrcIdx(c);
				for (int i=0; i<nBrc; i++) {
					double percent = Math.abs(_pkc[c][i]/rating[i]);
					String brcFlowSt = SystemModelXL.getBrcFlowFlag(percent);

					String dualVarFlag = "NA";
					double dualVarPostv = 0;
					double dualVarNegtv = 0;
					if (_isDualAvailableLimitPkc[c][i] == true) {
						dualVarFlag = "Available";
						dualVarPostv = _dualVarPostvLimitPkc[c][i];
						dualVarNegtv = _dualVarNegtvLimitPkc[c][i];
					}

					pw.format("%d,%s,%d,%s,%d,%s,%s,%f,%f,%f,%s,%s,%f,%f\n",
							count,
							"ctgcyCase",
							(idxCtgcyBrc+1),
							branches.getID(idxCtgcyBrc),
							(i+1),
							branches.getID(i),
							"Yes",
							_pkc[c][i]*baseMVA,
							rating[i]*baseMVA,
							percent,
							brcFlowSt,
							dualVarFlag,
							dualVarPostv/baseMVA,
							dualVarNegtv/baseMVA);
				}
			}
		} else {
			for (int c=0; c<nCtgcy; c++)
			{
				int idxCtgcyBrc = brcCtgcyList.getCtgcyBrcIdx(c);
				int[] idxBrc = brcCtgcyList.getCtgcyCaseMonitorSet(c);
				boolean[] isConstModel = brcCtgcyList.getIsConstActive(c);
				int size = idxBrc.length;
				
				for (int j=0; j<size; j++)
				{
					count++;
					int idxMonBrc = idxBrc[j];
					double ratingTmp = rating[idxMonBrc];
					if (brcCtgcyList.isUsePkcInit() == true) ratingTmp = brcCtgcyList.getCtgcyPkcLimit(c)[j];

					double percent = Math.abs(_pkc[c][j]/ratingTmp);
					String brcFlowSt = SystemModelXL.getBrcFlowFlag(percent);

					String dualVarFlag = "NA";
					double dualVarPostv = 0;
					double dualVarNegtv = 0;
					if (_isDualAvailableLimitPkc[c][j] == true) {
						dualVarFlag = "Available";
						dualVarPostv = _dualVarPostvLimitPkc[c][j];
						dualVarNegtv = _dualVarNegtvLimitPkc[c][j];
					}
					String isModeled = "Yes";
					if (isConstModel != null && isConstModel[j] == false) isModeled = "No";

					pw.format("%d,%s,%d,%s,%d,%s,%s,%f,%f,%f,%s,%s,%f,%f\n",
							count,
							"ctgcyCase",
							(idxCtgcyBrc+1),
							branches.getID(idxCtgcyBrc),
							(idxMonBrc+1),
							branches.getID(idxMonBrc),
							isModeled,
							_pkc[c][j]*baseMVA,
							ratingTmp*baseMVA,
							percent,
							brcFlowSt,
							dualVarFlag,
							dualVarPostv/baseMVA,
							dualVarNegtv/baseMVA);
				}
			}
		}
	}
	
	
	/** For both base-case and contingency-case */
	public void dumpInterface()
	{
		if (_sysModel.getInterfaceList().size() == 0) return;
		int[] interfaceCtgcyBrcIdx = _sysModel.getKeyBrcList().getInterfaceCtgcyBrcIdx();
		if (interfaceCtgcyBrcIdx == null) return;
		if (interfaceCtgcyBrcIdx.length == 0) return;
		
		String fileName = "SCEDResult_Interface.csv";
		File pout = new File(fileName);
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(new BufferedWriter(new FileWriter(pout)));
		} catch (IOException e) {
			e.printStackTrace();
		}
		dumpInterface(pw);
		pw.flush();
		pw.close();
		_diary.hotLine("SCED interface results are dumpped to the file "+fileName);
	}
	
	/** For both base-case and contingency-case */
	public void dumpInterface(PrintWriter pw)
	{
		double baseMVA = _sysModel.getMVAbase();
		//BranchListXL branches = _sysModel.getBranches();
		BrcCtgcyListXL brcCtgcyList = _sysModel.getKeyBrcList();
		
		InterfaceListXL interfaces = _sysModel.getInterfaceList();
		int[][] interfaceLines = interfaces.getInterfaceLines();
		double[] limits = interfaces.getTotalLimit();
		double[][] emgcyLimits = interfaces.getInterfaceEmgcyLimits();
		int nCtgcy = _pInterfaceCtgcy.length;
	
		pw.println("idx,caseType,ctgcyIdx,itemType,monitorBrcOrInterfaceIdx,pfrmMW,limitMW,loadingCondition,percent,dualVarFlag,dualPostvLimit,dualNegtvLimit");
		int count = 0;
		
		/* Base case */
		boolean[] isActiveBaseCase = interfaces.isInterfaceActive();
		for (int i=0; i<isActiveBaseCase.length; i++) {
			if (isActiveBaseCase[i] == false) continue;
			count++;
			
			double limit = limits[i];
			double percent = Math.abs(_pInterface[i]/limit);
			String brcFlowSt = SystemModelXL.getBrcFlowFlag(percent);

			String dualVarFlag = "NA";
			double dualVarPostv = 0;
			double dualVarNegtv = 0;
			if (_isDualAvailableLimitPInterface[i] == true) {
				dualVarFlag = "Available";
				dualVarPostv = _dualVarPostvLimitPInterface[i];
				dualVarNegtv = _dualVarNegtvLimitPInterface[i];
			}

			pw.format("%d,%s,%d,%s,%d,%f,%f,%s,%f,%s,%f,%f\n",
					count,
					"baseCase",
					-1,
					"Interface",
					(i+1),
					_pInterface[i]*baseMVA,
					limit*baseMVA,
					brcFlowSt,
					percent,
					dualVarFlag,
					dualVarPostv/baseMVA,
					dualVarNegtv/baseMVA);
			
			for (int j=0; j<interfaceLines[i].length; j++) {
				count++;
				int idxBrc = interfaceLines[i][j];
				pw.format("%d,%s,%d,%s,%d,%f,%f,%s,%f,%s,%f,%f\n",
						count,
						"baseCase",
						-1,
						"InterfaceLine",
						(idxBrc+1),
						_pk[idxBrc]*baseMVA,
						-1.0,
						"NA/Null",
						-1.0,
						"NA/Null",
						-1.0,
						-1.0);
			}
		}

		/* contingency cases */
		for (int c=0; c<nCtgcy; c++) {
			if (brcCtgcyList.isCtgcyActive(c) == false) continue;
			if (brcCtgcyList.isCtgcyActive4Interface(c) == false) continue;
			int idxCtgcyBrc = brcCtgcyList.getCtgcyBrcIdx(c);

			boolean[] isActive = brcCtgcyList.getIsInterfaceActiveCtgcyCase(c);
			int[] tmpIndices = interfaces.getItemIdxPerInterface(idxCtgcyBrc);
			for (int i=0; i<isActive.length; i++) {
				if (isActive[i] == false) continue;
				count++;
				
				double limit = limits[i];
				if (tmpIndices[i] != -1) limit = emgcyLimits[i][tmpIndices[i]];
				double percent = Math.abs(_pInterfaceCtgcy[c][i]/limit);
				String brcFlowSt = SystemModelXL.getBrcFlowFlag(percent);

				String dualVarFlag = "NA";
				double dualVarPostv = 0;
				double dualVarNegtv = 0;
				if (_isDualAvailableLimitPInterfaceCtgcy[c][i] == true) {
					dualVarFlag = "Available";
					dualVarPostv = _dualVarPostvLimitPInterfaceCtgcy[c][i];
					dualVarNegtv = _dualVarNegtvLimitPInterfaceCtgcy[c][i];
				}

				pw.format("%d,%s,%d,%s,%d,%f,%f,%s,%f,%s,%f,%f\n",
						count,
						"ctgcyCase",
						(idxCtgcyBrc+1),
						"Interface",
						(i+1),
						_pInterfaceCtgcy[c][i]*baseMVA,
						limit*baseMVA,
						brcFlowSt,
						percent,
						dualVarFlag,
						dualVarPostv/baseMVA,
						dualVarNegtv/baseMVA);
				
				for (int j=0; j<interfaceLines[i].length; j++) {
					count++;
					int idxBrc = interfaceLines[i][j];
					pw.format("%d,%s,%d,%s,%d,%f,%f,%s,%f,%s,%f,%f\n",
							count,
							"ctgcyCase",
							(idxCtgcyBrc+1),
							"InterfaceLine",
							(idxBrc+1),
							_pkcInterfaceLineCtgcy[c][i][j]*baseMVA,
							-1.0,
							"NA/Null",
							-1.0,
							"NA/Null",
							-1.0,
							-1.0);
				}
			}
		}
	}

	private void dumpNodalLMP()
	{
		String fileName = "SCEDResult_Bus.csv";
		File pout = new File(fileName);
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(new BufferedWriter(new FileWriter(pout)));
		} catch (IOException e) {
			e.printStackTrace();
		}
		dumpNodalLMP(pw);
		pw.flush();
		pw.close();
		_diary.hotLine("SCED bus results are dumpped to the file "+fileName);
	}

	private void dumpNodalLMP(PrintWriter pw)
	{
		double baseMVA = _sysModel.getMVAbase();
		BusListXL buses = _sysModel.getBuses();
		int nbuses = buses.size();
		
		double[] realBusLoad = new double[nbuses];
		double[] virtualBusLoad = new double[nbuses];
		LoadListXL loads = _sysModel.getLoads();
		int nloads = loads.size();
		double totalPd = 0;
		for (int d=0; d<nloads; d++)
		{
			if (loads.isInSvc(d) == false) continue;
			int idxBus = loads.getBusIdx(d);
			if (loads.getLoadType(d) == LoadType.Real) {realBusLoad[idxBus] += loads.getPLoad(d); totalPd += loads.getPLoad(d);}
			else virtualBusLoad[idxBus] += loads.getPLoad(d);
		}
		
		double[] pgMaxBus = new double[nbuses];
		double[] pgMinBus = new double[nbuses];
		double[] pgInitBus = new double[nbuses];
		double[] pgBus = new double[nbuses];
		GenListXL gens = _sysModel.getGens();
		int ngens = gens.size();
		double totalPg = 0;
		for (int g=0; g<ngens; g++) 
		{
			if (gens.isInSvc(g) == false) continue;
			int idxBus = gens.getBusIdx(g);
			pgMaxBus[idxBus] += gens.getPgmax(g);
			pgMinBus[idxBus] += gens.getPgmin(g);
			pgInitBus[idxBus] += gens.getPgInit(g);
			pgBus[idxBus] += _pg[g];
			totalPg += _pg[g];
		}
		double ratio = totalPg/totalPd;
		
		if (_isSCEDModeling4LMP == true) {
			int idxSlackBus = _sysModel.getSenstvtFactor().getFormPTDF().getSlackBusIdx();
			if (_sysModel.getSenstvtFactor().isUsePTDFforSCED() == true) {
				pw.println("PTDF based SCED model is used");
				pw.println("The system-wide LMP (LMP of slack bus "+ (idxSlackBus+1) + ") is: " + _sysLMP/baseMVA + "$/MWh");
			} else {
				pw.println("B-theta based SCED model is used");
				pw.println("The LMP of slack bus " + (idxSlackBus+1) + " is: "+_nodalLMP[idxSlackBus]/baseMVA + "$/MWh");
			}
		}
		pw.print("busIdx,busNumber,busType,realLoad,virtualLoad,totalLoad,pgSCED,pgInit,pgMax,pgMin");
		if (_isSCEDModeling4LMP == true) pw.println(",nodalLMP,congestionNodalLMP,singleBrcCongestionLMP,interfaceCongestionLMP,genRevenue,realLoadPayment,loadPayment,RatioTotalLoadPayment");
		else pw.println();

		
		for (int n=0; n<nbuses; n++) {
			String busType = "Connection";
			if (pgBus[n] != 0 || pgInitBus[n] != 0) {
				busType = "Gen";
				if (realBusLoad[n] != 0) busType = "GenLoad";
			} else if (realBusLoad[n] != 0) busType = "Load";
			double totalBusLoad = realBusLoad[n] + virtualBusLoad[n];
			
			pw.format("%d,%d,%s,%f,%f,%f,%f,%f,%f,%f",
					(n+1),
					buses.getBusNumber(n),
					busType,
					realBusLoad[n]*baseMVA,
					virtualBusLoad[n]*baseMVA,
					totalBusLoad*baseMVA,
					pgBus[n]*baseMVA,
					pgInitBus[n]*baseMVA,
					pgMaxBus[n]*baseMVA,
					pgMinBus[n]*baseMVA);
			if (_isSCEDModeling4LMP == false) pw.format("\n");
			else {
				double interfaceCongestionNodalLMP = (_interfaceCongestionNodalLMP != null) ? _interfaceCongestionNodalLMP[n] : 0;
				pw.format(",%f,%f,%f,%f,%f,%f,%f,%f\n",
						_nodalLMP[n]/baseMVA,
						(_singleBrcCongestionNodalLMP[n] + interfaceCongestionNodalLMP)/baseMVA,
						_singleBrcCongestionNodalLMP[n]/baseMVA,
						interfaceCongestionNodalLMP/baseMVA,
						pgBus[n]*_nodalLMP[n],
						realBusLoad[n]*_nodalLMP[n],
						totalBusLoad*_nodalLMP[n],
						ratio*realBusLoad[n]*_nodalLMP[n]);
			}
		}
	}

	public static void main(String[] agrc)
	{
		DiaryXL diary = new DiaryXL();
		diary.initial();

		ReadModelDataXL read = new ReadModelDataXL("input/sced_ver01/small_case_sced_data/w_title", diary);
		//ReadModelData read = new ReadModelData("input/sced_ver01/pjm_sced_data/w_title", diary);
		read.setIsHasHeading(true);
		read.readData();
		
		SystemModelXL model = new SystemModelXL(diary);
		model.fillData(read);
		//model.getSenstvtFactor().setUsePTDFforSCED(true);
		//model.getKeyBrcList().setUsePkInit(true);

		/* For test */
//		model.getMonitorSet().setIsMonitorAllBrc(false);
//		model.getMonitorSet().setMonitorBrcSet(new int[]{17});
//		int[][] monitorSetCtgcy = new int[2][2];
//		for (int i=0; i<monitorSetCtgcy.length; i++) {
//			for (int j=0; j<monitorSetCtgcy[i].length; j++) {
//				monitorSetCtgcy[i][j] = j+1;
//			}
//		}
//		model.getKeyBrcList().setCtgcyCaseMonitorSet(monitorSetCtgcy);
//		model.getKeyBrcList().setIsMonitorAllBrc(false);

		String log = "Gurobi_SCED.log";
		SCEDXL sced = new SCEDXL(diary, model);
		try {
			GRB_SCEDXL grbSolver = new GRB_SCEDXL(model, log, diary);
			grbSolver.setPgLBSV(false); // test
			grbSolver.setPreventiveCtrl(false);
			
			grbSolver.declareVar();
			grbSolver.varUpdate();
			
			grbSolver.defineConstraints();
			
			grbSolver.addObj();
			
			grbSolver.solve();
			sced.saveResults(grbSolver);
		} catch (GRBException e) {
			String errInfo = "Error occured when calling Gurobi solver...";
			System.err.println(errInfo);
			diary.hotLine(LogTypeXL.Error, errInfo);
			e.printStackTrace();
		}
		/* Program ends here */
		diary.done();
		System.out.println("Program ends here.");
	}


}
