package com.sced.gurobi;

import java.util.HashMap;

import com.sced.model.BrcCtgcyListXL;
import com.sced.model.SensitivityFactorXL;
import com.sced.model.SystemModelXL;
import com.sced.model.data.BranchListXL;
import com.sced.model.data.BusGrpXL;
import com.sced.model.data.BusListXL;
import com.sced.model.data.GenListXL;
import com.sced.model.data.InterfaceListXL;
import com.sced.model.data.LoadListXL;
import com.sced.model.data.LoadListXL.LoadType;
import com.utilxl.log.DiaryXL;
import com.utilxl.log.LogTypeXL;

import gurobi.GRB;
import gurobi.GRBConstr;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;

/**
 * Initialized in Sep 2016.
 * 
 * @author Xingpeng Li (xplipower@gmail.com)
 *
 */
public class GRB_SCEDXL {
	
    DiaryXL _diary;
    SystemModelXL _sysModel;
    
    // Setting
    double _cutOffValue; // for ptdf matrix
    double _TimeED = 15; // timeframe for an economic dispatch period, unit is in minutes
    double _TimeSR = 10; // timeframe for spinning reserve, unit is in minutes
    double _priceSR = 699;   // the price for having 1 p.u. active power reserve
    
    boolean _isSCEDModeling4LMP = true;  // if true, the SCED model is adjusted for getting LMP; if false, system-wide (energy component) LMP is ignored.
    double _penaltyLoadShed = 5e5;  // penalty cost if shed 1 p.u. of active power load
    boolean _useLoadShedSV = true; // enable load shed slack variable.  
    
    double _penaltyPgLBSV = 5e7;  // penalty cost per unit for each non-zero _pgLBSlack[g]
    boolean _usePgLBSV;           // use slack variable for Pg,min if true. 
    
    //TODO: remove Pgc variable if _preventiveCtrl == true if it is necessary to improve the solution time
    boolean _preventiveCtrl = true;      // Force "Pgc == Pg" if true; there is no Pgc variable in terms of code implementation.
    boolean _enforceSameLoadShed = true;  // if true, then _pdShedded_c[c] = _pdShedded.
 
    double _tolForMonitor = 0.00; // per unit; thermal constraint will not be considered on lines of which the thermal capacity is <= _tolForMonitor

    boolean _autoCorrectInitSystBalance = false;  // if true, auto correct the system power mismatch; if false, just report the system power mismatch
	double _tolSystImb = 0.05;   // per unit; pgInit needs to be adjusted if system imbalance is >= toleranceImb.

	// Gurobi 
    GRBEnv    _env;
    GRBModel _grbModel;

    // Non-negative Slack variable
    GRBVar[] _pgLBSV; // pg lower limit slack variable

    // for base-case scenario
    GRBVar[] _pg;
    GRBVar[][] _pgi;
    GRBVar[] _reserve;
    GRBVar[] _theta;
    GRBVar[] _pk;
    GRBVar[] _pdShedded;
    GRBVar[] _pInterface; // total interface flow
    
    // for contingency scenario
    GRBVar[][] _pg_c;     // first dimension is # of contingencies
    GRBVar[][] _theta_c;
    GRBVar[][] _pk_c;
    GRBVar[][] _pdShedded_c;
    GRBVar[][] _pInterface_c;

    // Save some key results
    double _objValue;
    int _grbStatus;
    
    double[] _pg1;
    double[] _reserve1;
    double[] _pk1;
    double[] _theta1; 
    double[][] _pgi1;
    double[] _pdShedded1;
    double[] _pInterface1;
    double[] _pgLBSV1;
    
    double[][] _pdShedded_c1;
    double[][] _pk_c1;    // only save flow for monitored lines, same size with BrcCtgcyListXL._pkcInit unless BrcCtgcyListXL._monitorAllBrc == true

    double[][] _pInterface_c1;   // Rank: nCtgcy * nInterface
    double[][][] _pkInterfaceLine_c1;  // Rank: nCtgcy * nInterface * nLinesOfInterface
    
    /* Power balance constraint */
    GRBConstr _constSysPB;
    GRBConstr[] _constNodePB;
    
    /* Branch limit Constraints */
    GRBConstr[] _constPostvLimitPk;
    GRBConstr[] _constNegtvLimitPk;

    GRBConstr[][] _constPostvLimitPk_c;
    GRBConstr[][] _constNegtvLimitPk_c;
    
    /* Interface limit Constraints */
    GRBConstr[] _constPostvLimitInterface;
    GRBConstr[] _constNegtvLimitInterface;

    GRBConstr[][] _constPostvLimitInterface_c;
    GRBConstr[][] _constNegtvLimitInterface_c;
    
    /* Dual variables of branch limit constraints */
    boolean[] _isDualAvailableLimitPk1;
    double[] _dualVarPostvLimitPk1;
    double[] _dualVarNegtvLimitPk1;

    boolean[][] _isDualAvailableLimitPk_c1; // only save flow for monitored lines, same size with BrcCtgcyListXL._pkcInit unless BrcCtgcyListXL._monitorAllBrc == true
    double[][] _dualVarPostvLimitPk_c1;     // only save flow for monitored lines, same size with BrcCtgcyListXL._pkcInit unless BrcCtgcyListXL._monitorAllBrc == true
    double[][] _dualVarNegtvLimitPk_c1;     // only save flow for monitored lines, same size with BrcCtgcyListXL._pkcInit unless BrcCtgcyListXL._monitorAllBrc == true
   
    /* Dual variables of interface limit constraints */
    boolean[] _isDualAvailableLimitInterface1;
    double[] _dualVarPostvLimitInterface1;
    double[] _dualVarNegtvLimitInterface1;
    
    boolean[][] _isDualAvailableLimitInterface_c1;
    double[][] _dualVarPostvLimitInterface_c1;
    double[][] _dualVarNegtvLimitInterface_c1;
    
    /* dual variables of power balance equation. */
    double _sysLMP;      // for PTDF model
    double[] _nodalLMP;   // for B-theta model

    //TODO: may need to include an option of fixing generation with negative outputs
    // this parameter has NOT been implemented
    boolean _fixNonPositivePg; // it true, for online generators with negative outputs, then force pg=pgc=pg0.

	public GRB_SCEDXL (SystemModelXL sysModel, String log, DiaryXL diary) throws GRBException {
		_sysModel = sysModel;
		_env   = new GRBEnv(log);
	    _grbModel = new GRBModel(_env);
	    init(diary);
	}
	private void init(DiaryXL diary) {
		_diary = diary;
		_cutOffValue = _sysModel.getSenstvtFactor().getCutOffValue();
		_diary.hotLine(LogTypeXL.CheckPoint, "PTDF matrix cutoff value is initialized: "+_cutOffValue);
		_diary.hotLine(LogTypeXL.MileStone, "GRB_SCED class was initialized");
		reportInitCost();
		correctInitSystemBalance();
	}
	private void reportInitCost() {
		double totalVarCost = _sysModel.getGens().getInitSysTotalVarCost();
		String info = "The system initial total operation/variable cost is $" + totalVarCost;
		_diary.hotLine(info); System.out.println(info);
	}
	/** Do something if the total generation and total demand (loss may be already included as virtual load) are not balanced */
	private void correctInitSystemBalance()
	{
		GenListXL gens = _sysModel.getGens();
		LoadListXL loads = _sysModel.getLoads();
		
		double totalPgInit = gens.getTotalPgInit();
		double totalPdInit = loads.getTotalPLoadInit();
		double diff = Math.abs(totalPgInit - totalPdInit);
		if (diff < _tolSystImb) {
			String mess = "Initial system generation and demand (including virtual load if any) are balanced";
			_diary.hotLine(mess); System.out.println(mess);
			return;
		} else {
			String mess = "Initial system generation and demand are imbalanced by "+diff+" p.u.";
			_diary.hotLine(LogTypeXL.Warning, mess); System.out.println(mess);
			if (_autoCorrectInitSystBalance == false) {
				mess = "The initial power imbalance issue remains for SCED";
				_diary.hotLine(mess); System.out.println(mess);
				return;
			} else {
				mess = "The initial power imbalance issue is fixed via an automatic algorthm before running SCED";
				_diary.hotLine(mess); System.out.println(mess);
			}
			
			double[] factors = null;
			if (totalPgInit < totalPdInit) {
				factors = getPartcpFactor(gens.getPgmax());
				mess = "PgInit of all online generators with positive PgInit has increased with available-capacity based participation factors";
				_diary.hotLine(LogTypeXL.Warning, mess); System.out.println(mess);
			}
			else {
				factors = getPartcpFactor(gens.getPgmin());
				mess = "PgInit of all online generators with positive PgInit has decreased with available-down-margin based participation factors";
				_diary.hotLine(LogTypeXL.Warning, mess); System.out.println(mess);
			}
			double[] pgInit = gens.getPgInit();
			for (int g=0; g<gens.size(); g++)
				gens.setPgInit(g, pgInit[g] + diff*factors[g]);
		}
		
		/* double check */
		totalPgInit = gens.getTotalPgInit();
		diff = Math.abs(totalPgInit - totalPdInit);
		if (diff < _tolSystImb) {
			String mess = "The system generation and demand are balanced after adjustment"; 
			_diary.hotLine(mess); 
			System.out.println(mess);
		} else {
			_diary.hotLine("The system total generation is "+totalPgInit);
			_diary.hotLine("The system total demand is "+totalPdInit);
			String mess = "The system is STILL imbalanced after adjustment";
			_diary.hotLine(LogTypeXL.Error, mess);
			System.out.println(mess);
		}
		
		double totalVarCost = _sysModel.getGens().getInitSysTotalVarCost();
		String info = "After auto adjustment, the system initial total operation/variable cost is $" + totalVarCost;
		_diary.hotLine(info); System.out.println(info);
	}
	
	private double[] getPartcpFactor(double[] limit)
	{
		GenListXL gens = _sysModel.getGens();
		double[] factors = new double[gens.size()];
		double[] pgInit = gens.getPgInit();
		
		double totalMargin = 0;
		for (int g=0; g<gens.size(); g++)
		{
			if (gens.isInSvc(g) == false) continue;
			if (pgInit[g] <= 0) continue;
			factors[g] = limit[g] - pgInit[g];
			totalMargin += factors[g];
		}
		for (int g=0; g<gens.size(); g++)
			factors[g] = factors[g]/Math.abs(totalMargin);
		return factors;
	}
	
	public void setPgLBSV(boolean enableSV) {_usePgLBSV = enableSV;}
	public void setPreventiveCtrl(boolean preventiveCtrl) {_preventiveCtrl = preventiveCtrl;}
	public void setPTDFCutOffValue(double cutOffValue) {
		_cutOffValue = cutOffValue;
		_diary.hotLine(LogTypeXL.CheckPoint, "PTDF matrix cutoff value is reset to "+cutOffValue);
	}
	private boolean isAbsValueLTCutoffValue(double ptdfElem) {
		return (Math.abs(ptdfElem) < _cutOffValue) ? true : false;
	}
	
	
	/** This method must be called immediately after all variables have been defined */
	public void varUpdate() throws GRBException {_grbModel.update(); _diary.hotLine("Gurobi: integrate new variables");}
	
	public void declareVar() throws GRBException {
		if (_usePgLBSV == true) addVarPgLBSlack();
		declareBaseCaseVar();
		declareCtgcyCaseVar();
	}
	
	private void declareBaseCaseVar() throws GRBException {
		addVarPg();
		addVarReserve();
		if (_sysModel.getSenstvtFactor().isUsePTDFforSCED() == false) addVarTheta();
		addVarPk();
		addVarPgi();
		addVarPdShedded();
		if(_sysModel.getInterfaceList().size() > 0) addVarInterfaceFlow();
		_diary.hotLine("Base case variables for Gurobi are declared");
	}
	
	private void declareCtgcyCaseVar() throws GRBException {
		if(_preventiveCtrl == false) addVarPg_c();
		if (_sysModel.getSenstvtFactor().isUsePTDFforSCED() == false) addVarTheta_c();
		addVarPk_c();
		if (_enforceSameLoadShed == false) addVarPdShedded_c();
		if(_sysModel.getInterfaceList().size() > 0) addVarInterfaceFlow_c();
		_diary.hotLine("Contingency case variables for Gurobi are declared");
	}
	
	public void defineConstraints() throws GRBException {
		if (_useLoadShedSV == false) forceLoadShedBeZero();
		defineBaseCaseConstraints();
		defineCtgcyCaseConstraints();
	}

	
	private void defineBaseCaseConstraints() throws GRBException {
		addPowerBalance();
		fixOfflineAndNegativeLoad();
		calcBrcFlow();
		addBrcPfCap();
		addGenSegmentSum();
		addGenEnergyRampLimit();
		addGenSpinRampLimit();
		addGenCapLimit();
		addReserveReq();
		if(_sysModel.getInterfaceList().size() > 0) {
			addInterfaceLimit();
			addInterfaceFlowCalc();
		}
		_diary.hotLine("Base case constraints for Gurobi are defined");
	}
	
	private void defineCtgcyCaseConstraints() throws GRBException {
		addPowerBalance_BrcCtgcy();
		if (_enforceSameLoadShed == false) fixOfflineAndNegativeLoad_BrcCtgcy();
		calcBrcFlow_BrcCtgcy();
		addBrcPfCap_BrcCtgcy();
		if(_preventiveCtrl == false) {
			addGenCapLimit_BrcCtgcy();
			addGenFixed_BrcCtgcy();
			addSpinRamp_BrcCtgcy();
		}
		if(_sysModel.getInterfaceList().size() > 0) {
			addInterfaceLimit_BrcCtgcy();
			addInterfaceFlowCalc_BrcCtgcy();
		}
		addInactive_BrcCtgcy();
		//if (_preventiveCtrl == true) preventiveCtrl_BrcCtgcy();
		_diary.hotLine("Contingency case constraints for Gurobi are defined");
	}
	

////////////////------   Define variables   ------////////////
/////////// slack variable
	/** Add generator output slack variables */
	private void addVarPgLBSlack() throws GRBException {
		int nGens = _sysModel.getGens().size();
		_pgLBSV = new GRBVar[nGens];
	    for (int i = 0; i < nGens; i++) {
	    	_pgLBSV[i] = _grbModel.addVar(0, GRB.INFINITY, 0, GRB.CONTINUOUS, "");
	    }
	}

/////////// base-case
	/** Add generator output variables */
	private void addVarPg() throws GRBException {
		int nGens = _sysModel.getGens().size();
	    _pg = new GRBVar[nGens];
	    for (int i = 0; i < nGens; i++)
	    	_pg[i] = _grbModel.addVar(-GRB.INFINITY, GRB.INFINITY, 0, GRB.CONTINUOUS, "");
	}

	/** Add reserve variables */
	private void addVarReserve() throws GRBException {
		int nGens = _sysModel.getGens().size();
	    _reserve = new GRBVar[nGens];
	    for (int i = 0; i < nGens; i++)
	    	_reserve[i] = _grbModel.addVar(0, GRB.INFINITY, 0, GRB.CONTINUOUS, "");
	}

	/** Add voltage angle variables */
	private void addVarTheta() throws GRBException {
		int nBuses = _sysModel.getBuses().size();
	    _theta = new GRBVar[nBuses];
	    for (int i = 0; i < nBuses; i++)
	    	_theta[i] = _grbModel.addVar(-GRB.INFINITY, GRB.INFINITY, 0, GRB.CONTINUOUS, "");
		//_grbModel.addConstr(_theta[_sysModel.getBuses().getSlackBusIdx()], GRB.EQUAL, 0, "");  // set the angle reference
	}
	
	/** Add line flow variables */
	private void addVarPk() throws GRBException {
		//TODO: only define pk for lines that either is in monitor set and belong to an interface
		// seems no need to implement this feature at this point of time - 2017.02.21.
		int nlines = _sysModel.getBranches().size();
	    _pk = new GRBVar[nlines];
	    for (int i = 0; i < nlines; i++)
	    	_pk[i] = _grbModel.addVar(-GRB.INFINITY, GRB.INFINITY, 0, GRB.CONTINUOUS, "");
	}

	/** Add generator cost segment variables */
	private void addVarPgi() throws GRBException {
		int nGenCostCurve = _sysModel.getGens().sizeGenCost();
		double[][] segmentBreadth = _sysModel.getGens().getSegmentBreadth();
	    _pgi = new GRBVar[nGenCostCurve][];
	    for (int i = 0; i < nGenCostCurve; i++) {
	    	int tmp = segmentBreadth[i].length;
	    	_pgi[i] = new GRBVar[tmp];
	    	for (int j=0; j<tmp; j++)
		    	_pgi[i][j] = _grbModel.addVar(0, segmentBreadth[i][j], 0, GRB.CONTINUOUS, "");
	    }
	}
	
	/** Add shedded active power load variables */
	private void addVarPdShedded() throws GRBException {
		int nLoads = _sysModel.getLoads().size();
		_pdShedded = new GRBVar[nLoads];
		for (int i=0; i<nLoads; i++)
			_pdShedded[i] = _grbModel.addVar(0, GRB.INFINITY, 0, GRB.CONTINUOUS, "");
	}
	
	/** Add total interface flow variables */
	private void addVarInterfaceFlow() throws GRBException {
		int nInterface = _sysModel.getInterfaceList().size();
		_pInterface = new GRBVar[nInterface];
		for (int i=0; i<nInterface; i++)
			_pInterface[i] = _grbModel.addVar(-GRB.INFINITY, GRB.INFINITY, 0, GRB.CONTINUOUS, "");
	}
	
/////////// contingency-case
	private void addVarPg_c() throws GRBException {
		int nCtgcy = _sysModel.getKeyBrcList().size();
		int nGens = _sysModel.getGens().size();
	    _pg_c = new GRBVar[nCtgcy][nGens];
	    defVar(_pg_c);
	}

	private void addVarTheta_c() throws GRBException {
		int nCtgcy = _sysModel.getKeyBrcList().size();
		int nBuses = _sysModel.getBuses().size();
		_theta_c = new GRBVar[nCtgcy][nBuses];
	    defVar(_theta_c);
	}
	
//	/** Add line flow variables */
//	private void addVarPk_c() throws GRBException {
//		int nCtgcy = _sysModel.getKeyBrcList().size();
//		int nlines = _sysModel.getBranches().size();
//		_pk_c = new GRBVar[nCtgcy][nlines];
//		if (_sysModel.getSenstvtFactor().isUsePTDFforSCED() == false) defVar(_pk_c);
//		else {
//			for (int c=0; c<nCtgcy; c++) {
//				if (_sysModel.getKeyBrcList().isCtgcyActive(c) == false) continue;
//				boolean[] isBrcMonitor = _sysModel.getIsBrcFlowCalc4CtgcyCase(c);
//				for (int k=0; k<nlines; k++) {
//					if (isBrcMonitor[k] == false) continue;
//					_pk_c[c][k] = _grbModel.addVar(-GRB.INFINITY, GRB.INFINITY, 0, GRB.CONTINUOUS, "");
//				}
//			}
//		}
//	}
//	
	/** Add line flow variables */
	private void addVarPk_c() throws GRBException {
		int nCtgcy = _sysModel.getKeyBrcList().size();
		int nBrc = _sysModel.getBranches().size();
		if (_sysModel.getSenstvtFactor().isUsePTDFforSCED() == false) {_pk_c = new GRBVar[nCtgcy][nBrc]; defVar(_pk_c);}
		else if (_sysModel.getKeyBrcList().getIsMonitorAllBrc() == true) {_pk_c = new GRBVar[nCtgcy][nBrc];defVar(_pk_c);} 
		else {
			_pk_c = new GRBVar[nCtgcy][];
			for (int c=0; c<nCtgcy; c++) {
				int[] monitorSet = _sysModel.getKeyBrcList().getCtgcyCaseMonitorSet(c);
				int sizePkc = (monitorSet == null) ? 0 : monitorSet.length;
				if (_sysModel.getKeyBrcList().isCtgcyActive4Interface(c))
					sizePkc += _sysModel.getKeyBrcList().getMonInterfaceLines(c).length;
				_pk_c[c] = new GRBVar[sizePkc];
				for (int i=0; i<sizePkc; i++) {
					_pk_c[c][i] = _grbModel.addVar(-GRB.INFINITY, GRB.INFINITY, 0, GRB.CONTINUOUS, "");
				}
			}
		}
	}

	private void addVarPdShedded_c() throws GRBException {
		int nCtgcy = _sysModel.getKeyBrcList().size();
		int nLoads = _sysModel.getLoads().size();
		_pdShedded_c = new GRBVar[nCtgcy][nLoads];
		for (int i=0; i<nCtgcy; i++) {
			for (int j=0; j<nLoads; j++)
				_pdShedded_c[i][j] = _grbModel.addVar(0, GRB.INFINITY, 0, GRB.CONTINUOUS, "");
		}
	}
	
	private void addVarInterfaceFlow_c() throws GRBException {
		int nCtgcy = _sysModel.getKeyBrcList().size();
		int nInterface = _sysModel.getInterfaceList().size();
		_pInterface_c = new GRBVar[nCtgcy][nInterface];
	    defVar(_pInterface_c);
	}

////////////////------   Define objective function   ------////////////
	/** Minimize the total cost */
	public void addObj() throws GRBException {
		double[][] segmentPrice = _sysModel.getGens().getSegmentPrice();
		GRBLinExpr expr = new GRBLinExpr();
	    for (int i=0; i<_pgi.length; i++)
		    for (int j=0; j<_pgi[i].length; j++)
		    	expr.addTerm(segmentPrice[i][j], _pgi[i][j]);
	    int nGen = _reserve.length;
	    for (int g=0; g<nGen; g++)
	    	expr.addTerm(_priceSR, _reserve[g]);

	    int nLoads = _pdShedded.length;
	    for (int i=0; i<nLoads; i++)
	    	expr.addTerm(_penaltyLoadShed, _pdShedded[i]);
	    if (_enforceSameLoadShed == false) {
		    for (int c=0; c<_pdShedded_c.length; c++) {
		    	for (int d=0; d<nLoads; d++) {
			    	expr.addTerm(_penaltyLoadShed, _pdShedded_c[c][d]);
		    	}
		    }
	    }
	    
	    if (_usePgLBSV == true) {
	    	for (int g=0; g<nGen; g++)
	    		expr.addTerm(_penaltyPgLBSV, _pgLBSV[g]);
	    }
	    
	    _grbModel.setObjective(expr, GRB.MINIMIZE);
		_diary.hotLine("Objective function was added in the gurobi model");
	}
	
	
////////////////------   Define constraints   ------////////////
	/** Power balance */
	public void addPowerBalance() throws GRBException {
		if (_sysModel.getSenstvtFactor().isUsePTDFforSCED() == true) addSystemBalance();
		else addNodeBalance();
	}

	private void addNodeBalance() throws GRBException {
		initGRBConstr_NodePB();
		int nBuses = _sysModel.getBuses().size();
		BusGrpXL busGrp = _sysModel.getBusGrp();
		double[] busLoad = _sysModel.getLoads().getBusLoad();
		for (int i=0; i<nBuses; i++) {
		    GRBLinExpr expr = new GRBLinExpr();
			int[] brcIdx = busGrp.getBrcIndex(i);
			for (int j=0; j<brcIdx.length; j++) {
				if (i == _sysModel.getBranches().getFrmBusIdx(brcIdx[j])) expr.addTerm(-1.0, _pk[brcIdx[j]]);
				else if (i == _sysModel.getBranches().getToBusIdx(brcIdx[j])) expr.addTerm(1.0, _pk[brcIdx[j]]);
				else {
					String errInfo = "Bug found: bus "+i+ " and branch "+brcIdx[j] + " not connected but supposed to be ...";
					System.err.println(errInfo); _diary.hotLine(LogTypeXL.Error, errInfo);
				}
			}
			int[] genIdx = busGrp.getGenIndex(i);
			if (genIdx != null) {
				for (int j=0; j<genIdx.length; j++)
					expr.addTerm(1.0, _pg[genIdx[j]]);
			}
			int[] loadIdx = busGrp.getLoadIndex(i);
			if (loadIdx != null) {
				for (int j=0; j<loadIdx.length; j++)
					expr.addTerm(1.0, _pdShedded[loadIdx[j]]);
			}
			_constNodePB[i] = _grbModel.addConstr(expr, GRB.EQUAL, busLoad[i], "");
		}
		_diary.hotLine("Node power balance equations were added in the gurobi model");
	}

	private void addSystemBalance() throws GRBException {
	    GRBLinExpr expr = new GRBLinExpr();
		GenListXL gens = _sysModel.getGens();
		for (int g=0; g<gens.size(); g++)
			expr.addTerm(1.0, _pg[g]);
		LoadListXL loads = _sysModel.getLoads();
		for (int i=0; i<loads.size(); i++)
			expr.addTerm(1.0, _pdShedded[i]);
		double totalLoad = _sysModel.getLoads().getTotalPLoad();
		_constSysPB = _grbModel.addConstr(expr, GRB.EQUAL, totalLoad, "");
	}
		
	/** Fix offline and negative load */
	public void fixOfflineAndNegativeLoad() throws GRBException {
		LoadListXL loads = _sysModel.getLoads();
		for (int i=0; i<loads.size(); i++) {
			if (loads.isInSvc(i) == false) _grbModel.addConstr(_pdShedded[i], GRB.EQUAL, 0, "");
			else if (loads.getPLoad(i) <= 0) _grbModel.addConstr(_pdShedded[i], GRB.EQUAL, 0, "");
			else if (loads.getLoadType(i) == LoadType.Virtual) _grbModel.addConstr(_pdShedded[i], GRB.EQUAL, 0, "");
			else _grbModel.addConstr(_pdShedded[i], GRB.LESS_EQUAL, loads.getPLoad(i), "");
		}
	}

	/** Branch flow calculation */
	public void calcBrcFlow() throws GRBException {
		BranchListXL branches = _sysModel.getBranches();
		double[] x = branches.getX();
		double[] angles = branches.getAngle();
		int nBrc = branches.size();
		boolean[] isBrcMonitor = _sysModel.getIsBrcFlowCalc4BaseCase();
		for (int k=0; k<nBrc; k++) {
			if (branches.isInSvc(k) == false) _grbModel.addConstr(_pk[k], GRB.EQUAL, 0, "");
			else if (isBrcMonitor[k] == false) _grbModel.addConstr(_pk[k], GRB.EQUAL, 0, "");
			else {
				if (_sysModel.getSenstvtFactor().isUsePTDFforSCED() == true) calcBrcFlow_PTDF(k);
				else calcBrcFlow_B_Theta(k, x[k], branches, angles[k]);
			}
		}
	}

	private void calcBrcFlow_B_Theta(int k, double x, BranchListXL branches, double angle) throws GRBException {
	    GRBLinExpr expr = new GRBLinExpr();
	    expr.addTerm(x, _pk[k]);
	    int frmBus = branches.getFrmBusIdx(k);
	    int toBus = branches.getToBusIdx(k);
	    expr.addTerm(-1.0, _theta[frmBus]); 
	    expr.addTerm(1.0, _theta[toBus]);
		_grbModel.addConstr(expr, GRB.EQUAL, -angle, "");
	}

	private void calcBrcFlow_PTDF(int k) throws GRBException {
		SensitivityFactorXL senstvtFactor = _sysModel.getSenstvtFactor();
		BrcCtgcyListXL keyBrcCtgcy = _sysModel.getKeyBrcList();
		BusListXL buses = _sysModel.getBuses();
		BusGrpXL busGrp = _sysModel.getBusGrp();
		LoadListXL loads = _sysModel.getLoads();

		if (senstvtFactor.isUseSparsePTDF() == true) {
			//TODO: very useful only for large-scale system
			// seems no need to implement this feature.
			if (keyBrcCtgcy.isUsePkInit() == true) {

			} else {

			}
		} else {
		    GRBLinExpr expr = new GRBLinExpr();
		    expr.addTerm(1.0, _pk[k]);
			double[] ptdfDense = senstvtFactor.getDensePTDF(k);
			
			if (keyBrcCtgcy.isUsePkInit() == true) {
				double pkInit = _sysModel.getBranches().getPkInit(k);
				for (int n=0; n<buses.size(); n++) {
					if (isAbsValueLTCutoffValue(ptdfDense[n]) == true) continue;
					
					/* load impact on line flow */
					int[] idxLoad = busGrp.getLoadIndex(n);
					if (idxLoad != null) {
						for (int d=0; d<idxLoad.length; d++) {
							expr.addTerm(-ptdfDense[n], _pdShedded[idxLoad[d]]);
							double ploadDiff = loads.getPloadDiff(idxLoad[d]);
							pkInit -= ploadDiff*ptdfDense[n];
						}
					}
					
					/* gen impact on line flow */
					int[] idxGen = busGrp.getGenIndex(n);
					if (idxGen != null) {
						for (int g=0; g<idxGen.length; g++) {
							if (_sysModel.getGens().isInSvc(idxGen[g]) == false) continue;
							expr.addTerm(-ptdfDense[n], _pg[idxGen[g]]);
							double pgInit = _sysModel.getGens().getPgInit(idxGen[g]);
							pkInit -= pgInit*ptdfDense[n];
						}
					}
				}
				_grbModel.addConstr(expr, GRB.EQUAL, pkInit, "");
			} else {
				double pkFrmLoadInj = 0;
				for (int n=0; n<buses.size(); n++) {
					if (isAbsValueLTCutoffValue(ptdfDense[n]) == true) continue;
					
					/* load impact on line flow */
					int[] idxLoad = busGrp.getLoadIndex(n);
					if (idxLoad != null) {
						for (int d=0; d<idxLoad.length; d++)
						{
							int idx = idxLoad[d];
							expr.addTerm(-ptdfDense[n], _pdShedded[idx]);
							if (loads.isInSvc(idx) == true) pkFrmLoadInj -= ptdfDense[n]*loads.getPLoad(idx);
						}
					}
					
					/* gen impact on line flow */
					int[] idxGen = busGrp.getGenIndex(n);  
					if (idxGen != null) {
						for (int g=0; g<idxGen.length; g++)
							expr.addTerm(-ptdfDense[n], _pg[idxGen[g]]);
					}
				}
				_grbModel.addConstr(expr, GRB.EQUAL, pkFrmLoadInj, "");
			}
		}
	}
	
	/** Line thermal capacity limit */
	public void addBrcPfCap() throws GRBException {
		initGRBConstr_Pk();
		int numOffLine = 0;
		int numNoCap = 0;
		double[] limitA = _sysModel.getBranches().getBrcNormalRating();
		int nLinesMonitor = limitA.length;
		if (_sysModel.getMonitorSet().getIsMonitorAllBrc() == true) {
			for (int i=0; i<limitA.length; i++) {
				if (_sysModel.getBranches().isInSvc(i) == true) {
					if (limitA[i] <= _tolForMonitor) {
						_diary.hotLine(LogTypeXL.Warning, "The thermal constraint for branch "+(i+1)+" in the base case "
								+" is skipped due to its limit "+limitA[i]+" is <= the tolerance "+_tolForMonitor);
						numNoCap++; continue;
					}
					_constPostvLimitPk[i] = _grbModel.addConstr(_pk[i], GRB.LESS_EQUAL, limitA[i], "");
					_constNegtvLimitPk[i] = _grbModel.addConstr(_pk[i], GRB.GREATER_EQUAL, -limitA[i], "");
				} else numOffLine++;
			}
		} else {
			if(_sysModel.getMonitorSet().size() != 0) {
				int[] monitorSet = _sysModel.getMonitorSet().getMonitorBrcSet();
				double[] limit = _sysModel.getMonitorSet().getPkLimit();
				boolean[] isConstActive = _sysModel.getMonitorSet().getIsConstActive();
				nLinesMonitor = monitorSet.length;
				for (int i=0; i<nLinesMonitor; i++) {
					int idxBrc = monitorSet[i];
					if (isConstActive != null && isConstActive[i] == false) continue;
					if (_sysModel.getBranches().isInSvc(idxBrc) == true) {
						if (limit[i] <= _tolForMonitor) {
							_diary.hotLine(LogTypeXL.Warning, "The thermal constraint for branch "+(idxBrc+1)+" in the base case "
									+" is skipped due to its limit "+limit[i]+" is <= the tolerance "+_tolForMonitor);
							numNoCap++; continue;
						}
						_constPostvLimitPk[idxBrc] = _grbModel.addConstr(_pk[idxBrc], GRB.LESS_EQUAL, limit[i], "");
						_constNegtvLimitPk[idxBrc] = _grbModel.addConstr(_pk[idxBrc], GRB.GREATER_EQUAL, -limit[i], "");
					} else numOffLine++;
				}
			}
		}
		if (numOffLine != 0) _diary.hotLine(LogTypeXL.Warning, numOffLine+" lines out of "+nLinesMonitor+" are not in service for the base case.");
		if (numNoCap != 0) _diary.hotLine(LogTypeXL.Warning, numNoCap+" lines have very small rateA (<= "+_tolForMonitor+" p.u.), hence their thermal capacity constraints are ignored.");
		_diary.hotLine("Branch thermal limit constraints were added in the gurobi model");
	}
	
	/** Gen cost curve limit */
	public void addGenSegmentSum() throws GRBException {
		GenListXL gens = _sysModel.getGens();
		for (int g=0; g<gens.size(); g++) {
			boolean isCostCurve = gens.hasCostCurveFlag(g);
			int idxMapping = gens.getIdxMapToCostCurve(g);
			
			if (gens.isInSvc(g) == false) {
				_grbModel.addConstr(_pg[g], GRB.EQUAL, 0, "");
				/* The below if-else block is not needed but might speed up the solver searching process */
				if (isCostCurve == true) {
					int nSegment = _pgi[idxMapping].length;
					for (int j=0; j<nSegment; j++)
						_grbModel.addConstr(_pgi[idxMapping][j], GRB.EQUAL, 0, "");
				}
			} else if (isCostCurve == true) {
				GRBLinExpr expr = new GRBLinExpr();
				int nSegment = _pgi[idxMapping].length;
				for (int j=0; j<nSegment; j++)
				    expr.addTerm(1.0, _pgi[idxMapping][j]);
				expr.addTerm(-1.0, _pg[g]);
				_grbModel.addConstr(expr, GRB.EQUAL, 0, "");
				
				if (gens.getPgInit(g) <= 0) {
					String message = "Generator "+(g+1)+" is online and has cost-curve, but its initial output is " + gens.getPgInit(g) + " p.u., NOT positive";
					_diary.hotLine(LogTypeXL.Warning, message);
					// the below for-loop may be needed since pgi is forced to be non-negative
					for (int j=0; j<nSegment; j++)
						_grbModel.addConstr(_pgi[idxMapping][j], GRB.EQUAL, 0, "");
				}
			} else _grbModel.addConstr(_pg[g], GRB.EQUAL, gens.getPgInit(g), "");
		}
	}
	
	/** Gen energy ramping limit between periods */
	public void addGenEnergyRampLimit() throws GRBException {
		GenListXL gens = _sysModel.getGens();
		double[] pgInit = gens.getPgInit();
		double[] pgmax = gens.getPgmax();
		double[] pgmin = gens.getPgmin();
		for (int g=0; g<gens.size(); g++) {
			if (gens.isInSvc(g) == false) continue;
			
			double energyRamp = gens.getEnergyRamp(g) * _TimeED;
			double pgUpLimit = pgInit[g] + energyRamp;
			if (pgUpLimit > pgmax[g]) pgUpLimit = pgmax[g];     // Pg upper limit 
			double pgLowLimit = pgInit[g] - energyRamp;
			if (pgLowLimit < pgmin[g]) pgLowLimit = pgmin[g];   // Pg lower bound/limit
			
			_grbModel.addConstr(_pg[g], GRB.LESS_EQUAL, pgUpLimit, "");
			if (_usePgLBSV == false) _grbModel.addConstr(_pg[g], GRB.GREATER_EQUAL, pgLowLimit, "");
			else {
				GRBLinExpr expr = new GRBLinExpr();
				expr.addTerm(1.0, _pg[g]);
				expr.addTerm(1.0, _pgLBSV[g]);
				_grbModel.addConstr(expr, GRB.GREATER_EQUAL, pgLowLimit, "");
			}
		}
		_diary.hotLine("Generator Pg range limit constraints were added in the gurobi model");
	}

	/** Gen spin ramping limit for reserve */
	public void addGenSpinRampLimit() throws GRBException {
		GenListXL gens = _sysModel.getGens();
		//double[] pgmax = gens.getPgmax();
		for (int g=0; g<gens.size(); g++) {
			if (gens.isInSvc(g) == true) {
				double spinRamp = gens.getSpinRamp(g) * _TimeSR;
				//if (spinRamp > pgmax[g]) spinRamp = pgmax[g];     // Pg upper limit 
				_grbModel.addConstr(_reserve[g], GRB.LESS_EQUAL, spinRamp, "");
			} else _grbModel.addConstr(_reserve[g], GRB.EQUAL, 0, "");
		}
	}

	/** Gen capacity limit */
	public void addGenCapLimit() throws GRBException {
		GenListXL gens = _sysModel.getGens();
		double[] pgmax = gens.getPgmax();
		for (int g=0; g<gens.size(); g++) {
			if (gens.isInSvc(g) == true) {
				GRBLinExpr expr = new GRBLinExpr();
			    expr.addTerm(1.0, _pg[g]);
			    expr.addTerm(1.0, _reserve[g]);
			    _grbModel.addConstr(expr, GRB.LESS_EQUAL, pgmax[g], "");
			}
		}
	}
	
	/** Spinning reserve requirement for largest gen ctgcy */
	public void addReserveReq() throws GRBException {
		GenListXL gens = _sysModel.getGens();
		for (int g=0; g<gens.size(); g++) {
			if (gens.isInSvc(g) == true) {
				GRBLinExpr expr = new GRBLinExpr();
				for (int j=0; j<gens.size(); j++)
				    if (j != g) expr.addTerm(1.0, _reserve[j]);
			    expr.addTerm(-1.0, _pg[g]);
			    _grbModel.addConstr(expr, GRB.GREATER_EQUAL, 0, "");
			}
		}
	}

	/** Interface limit */
	public void addInterfaceLimit() throws GRBException {
		initGRBConstr_Interface();
		InterfaceListXL interfaces = _sysModel.getInterfaceList();
		double[] limit = interfaces.getTotalLimit();
		boolean[] isActive = interfaces.isInterfaceActive();
		for (int i=0; i<isActive.length; i++) {
			if (isActive[i] == true) {
				_constPostvLimitInterface[i] = _grbModel.addConstr(_pInterface[i], GRB.LESS_EQUAL, limit[i], "");
				_constNegtvLimitInterface[i] = _grbModel.addConstr(_pInterface[i], GRB.GREATER_EQUAL, -limit[i], "");
			}
		}
	}

	/** Interface flow calc */
	public void addInterfaceFlowCalc() throws GRBException {
		InterfaceListXL interfaces = _sysModel.getInterfaceList();
		boolean[] isActive = interfaces.isInterfaceActive();
		int[][] interfaceLines = interfaces.getInterfaceLines();
		boolean[][] interfaceLinesDirection = interfaces.getInterfaceLinesDirection();
		for (int i=0; i<isActive.length; i++) {
			if (isActive[i] == false) _grbModel.addConstr(_pInterface[i], GRB.EQUAL, 0, "");
			else {
				GRBLinExpr expr = new GRBLinExpr();
				for (int j=0; j<interfaceLines[i].length; j++) {
					int idxBrc = interfaceLines[i][j];
					if (interfaceLinesDirection[i][j] == true) expr.addTerm(1.0, _pk[idxBrc]);
					else expr.addTerm(-1.0, _pk[idxBrc]);
				}
				expr.addTerm(-1.0, _pInterface[i]);
				_grbModel.addConstr(expr, GRB.EQUAL, 0, "");
			}
		}
	}

	
/////////// contingency-related constraint
	/** Power balance equation UNDER CONTINGENCY */
	public void addPowerBalance_BrcCtgcy() throws GRBException {
		if (_sysModel.getSenstvtFactor().isUsePTDFforSCED() == true) addSystemBalance_BrcCtgcy();
		else addNodeBalance_BrcCtgcy();
	}

	private void addNodeBalance_BrcCtgcy() throws GRBException {
		int size = _sysModel.getKeyBrcList().size();
		int nBuses = _sysModel.getBuses().size();
		BusGrpXL busGrp = _sysModel.getBusGrp();
		double[] busLoad = _sysModel.getLoads().getBusLoad();
		for (int c=0; c<size; c++) {
			if (_sysModel.getKeyBrcList().isCtgcyActive(c) == false) continue;
			for (int i=0; i<nBuses; i++) {
			    GRBLinExpr expr = new GRBLinExpr();
				int[] brcIdx = busGrp.getBrcIndex(i);
				for (int j=0; j<brcIdx.length; j++) {
					if (i == _sysModel.getBranches().getFrmBusIdx(brcIdx[j])) expr.addTerm(-1.0, _pk_c[c][brcIdx[j]]);
					else if (i == _sysModel.getBranches().getToBusIdx(brcIdx[j])) expr.addTerm(1.0, _pk_c[c][brcIdx[j]]);
					else {
						String errInfo = "Bug found: bus "+i+ " and branch "+brcIdx[j] + " not connected but supposed to be ...";
						System.err.println(errInfo); _diary.hotLine(LogTypeXL.Error, errInfo);
					}
				}
				int[] genIdx = busGrp.getGenIndex(i);
				if (genIdx != null) {
					for (int j=0; j<genIdx.length; j++) {
						if(_preventiveCtrl == false) expr.addTerm(1.0, _pg_c[c][genIdx[j]]);
						else expr.addTerm(1.0, _pg[genIdx[j]]);
					}
						
				}
				int[] loadIdx = busGrp.getLoadIndex(i);
				if (loadIdx != null) {
					for (int j=0; j<loadIdx.length; j++) {
						if (_enforceSameLoadShed == false) expr.addTerm(1.0, _pdShedded_c[c][loadIdx[j]]);
						else expr.addTerm(1.0, _pdShedded[loadIdx[j]]);
					}
				}
				
				if (_isSCEDModeling4LMP == true) {
					for (int j=0; j<brcIdx.length; j++) {
						if (i == _sysModel.getBranches().getFrmBusIdx(brcIdx[j])) expr.addTerm(1.0, _pk[brcIdx[j]]);
						else if (i == _sysModel.getBranches().getToBusIdx(brcIdx[j])) expr.addTerm(-1.0, _pk[brcIdx[j]]);
					}
					if (genIdx != null) {
						for (int j=0; j<genIdx.length; j++)
							expr.addTerm(-1.0, _pg[genIdx[j]]);
					}
					if (loadIdx != null) {
						for (int j=0; j<loadIdx.length; j++)
							expr.addTerm(-1.0, _pdShedded[loadIdx[j]]);
					}
					_grbModel.addConstr(expr, GRB.EQUAL, 0, "");
				} else _grbModel.addConstr(expr, GRB.EQUAL, busLoad[i], "");
			}
		}
		_diary.hotLine("Node power balance equations UNDER contingency were added in the gurobi model");
	}

	private void addSystemBalance_BrcCtgcy() throws GRBException {
		int size = _sysModel.getKeyBrcList().size();
		for (int c=0; c<size; c++) {
			if (_sysModel.getKeyBrcList().isCtgcyActive(c) == false) continue;
		    GRBLinExpr expr = new GRBLinExpr();
			GenListXL gens = _sysModel.getGens();
			for (int g=0; g<gens.size(); g++) {
				if(_preventiveCtrl == false) expr.addTerm(1.0, _pg_c[c][g]);
				else expr.addTerm(1.0, _pg[g]);
			}
			LoadListXL loads = _sysModel.getLoads();
			for (int i=0; i<loads.size(); i++) {
				if (_enforceSameLoadShed == false) expr.addTerm(1.0, _pdShedded_c[c][i]);
				else expr.addTerm(1.0, _pdShedded[i]);
			}
			
			if (_isSCEDModeling4LMP == true) {
				for (int g=0; g<gens.size(); g++)
					expr.addTerm(-1.0, _pg[g]);
				for (int i=0; i<loads.size(); i++)
					expr.addTerm(-1.0, _pdShedded[i]);
				_grbModel.addConstr(expr, GRB.EQUAL, 0, "");
			} else {
				double totalLoad = _sysModel.getLoads().getTotalPLoad();
				_grbModel.addConstr(expr, GRB.EQUAL, totalLoad, "");
			}
		}
	}
		
	/** Fix offline and negative load UNDER CONTINGENCY */
	public void fixOfflineAndNegativeLoad_BrcCtgcy() throws GRBException {
		int size = _sysModel.getKeyBrcList().size();
		LoadListXL loads = _sysModel.getLoads();
		for (int c=0; c<size; c++) {
			if (_sysModel.getKeyBrcList().isCtgcyActive(c) == false) continue;
			for (int i=0; i<loads.size(); i++) {
				if (loads.isInSvc(i) == false) _grbModel.addConstr(_pdShedded_c[c][i], GRB.EQUAL, 0, "");
				else if (loads.getPLoad(i) <= 0) _grbModel.addConstr(_pdShedded_c[c][i], GRB.EQUAL, 0, "");
				else if (loads.getLoadType(i) == LoadType.Virtual) _grbModel.addConstr(_pdShedded_c[c][i], GRB.EQUAL, 0, "");
				else _grbModel.addConstr(_pdShedded_c[c][i], GRB.LESS_EQUAL, loads.getPLoad(i), "");
			}
		}
	}

	/** Branch flow calculation UNDER CONTINGENCY */
	public void calcBrcFlow_BrcCtgcy() throws GRBException {
		int size = _sysModel.getKeyBrcList().size();
		BranchListXL branches = _sysModel.getBranches();
		double[] x = branches.getX();
		double[] angles = branches.getAngle();
		
		boolean isAllBrcMonitor = false;
		if (_sysModel.getSenstvtFactor().isUsePTDFforSCED() == false) isAllBrcMonitor = true;
		else if (_sysModel.getKeyBrcList().getIsMonitorAllBrc() == true) isAllBrcMonitor = true;

		for (int c=0; c<size; c++) {
			if (_sysModel.getKeyBrcList().isCtgcyActive(c) == false) continue;
			
			int[] monitorSet = _sysModel.getKeyBrcList().getCtgcyCaseMonitorSet(c);
			int sizeMonitorSet = (monitorSet == null) ? 0 : monitorSet.length;
			for (int k=0; k<_pk_c[c].length; k++) {
				if (_pk_c[c][k] == null) continue;
				int idxBrc = k;
				int idxInterfaceLine = -1;
				if (isAllBrcMonitor == false) {
					if (_sysModel.getKeyBrcList().isCtgcyActive4Interface(c) == false) idxBrc = monitorSet[k];
					else {
						idxInterfaceLine = k - sizeMonitorSet;
						idxBrc= _sysModel.getKeyBrcList().getMonInterfaceLines(c)[idxInterfaceLine];
					}
				}
				if (branches.isInSvc(idxBrc) == false) _grbModel.addConstr(_pk_c[c][k], GRB.EQUAL, 0, "");
				else if (idxBrc == _sysModel.getKeyBrcList().getCtgcyBrcIdx(c)) _grbModel.addConstr(_pk_c[c][k], GRB.EQUAL, 0, "");
				else {
					if (_sysModel.getSenstvtFactor().isUsePTDFforSCED() == true) calcBrcFlow_BrcCtgcy_PTDF(c, k, idxBrc, idxInterfaceLine);
					else calcBrcFlow_BrcCtgcy_B_Theta(c, k, x[k], branches, angles[k]);
				}
			}
//			int[] idxMap2CtgcyList = getIdxMap2CtgcyList(c);
//			int[] idxMap2CtgcyInterfaceLines = getIdxMap2CtgcyInterfaceLines(c);
//			for (int k=0; k<nBrc; k++) {
//				if (_pk_c[c][k] == null) continue;
//				if (branches.isInSvc(k) == false) _grbModel.addConstr(_pk_c[c][k], GRB.EQUAL, 0, "");
//				else if (k == _sysModel.getKeyBrcList().getCtgcyBrcIdx(c)) _grbModel.addConstr(_pk_c[c][k], GRB.EQUAL, 0, "");
//				//else if (isBrcMonitor[k] == false) _grbModel.addConstr(_pk_c[c][k], GRB.EQUAL, 0, "");
//				else {
//					if (_sysModel.getSenstvtFactor().isUsePTDFforSCED() == true) calcBrcFlow_BrcCtgcy_PTDF(c, k, idxMap2CtgcyList, idxMap2CtgcyInterfaceLines);
//					else calcBrcFlow_BrcCtgcy_B_Theta(c, k, x[k], branches, angles[k]);
//				}
//			}
		}
	}
	
//	private int[] getIdxMap2CtgcyList(int c)
//	{
//		if (_sysModel.getSenstvtFactor().isUsePTDFforSCED() == false) return null;
//		if (_sysModel.getKeyBrcList().isUsePkInit() == false) return null;
//		if (_sysModel.getKeyBrcList().isUsePkcInit() == false) return null;
//		if (_sysModel.getKeyBrcList().isSimAllPotentialCtgcy() == true) return null;
//		return _sysModel.getKeyBrcList().getIdxMap2CtgcyList(c);
//	}
//	
//	private int[] getIdxMap2CtgcyInterfaceLines(int c)
//	{
//		if (_sysModel.getSenstvtFactor().isUsePTDFforSCED() == false) return null;
//		if (_sysModel.getKeyBrcList().isUsePkInit() == false) return null;
//		if (_sysModel.getKeyBrcList().isUsePkcInit() == false) return null;
//		if (_sysModel.getKeyBrcList().isSimAllPotentialCtgcy() == true) return null;
//		if (_sysModel.getInterfaceList().size() == 0) return null;
//		return _sysModel.getKeyBrcList().getIdxMap2CtgcyInterfaceLines(c);
//	}
	
	private void calcBrcFlow_BrcCtgcy_B_Theta(int c, int k, double x, BranchListXL branches, double angle) throws GRBException {
	    GRBLinExpr expr = new GRBLinExpr();
	    expr.addTerm(x, _pk_c[c][k]);
	    int frmBus = branches.getFrmBusIdx(k);
	    int toBus = branches.getToBusIdx(k);
	    expr.addTerm(-1.0, _theta_c[c][frmBus]); 
	    expr.addTerm(1.0, _theta_c[c][toBus]);
		_grbModel.addConstr(expr, GRB.EQUAL, -angle, "");
	}
	
	private void calcBrcFlow_BrcCtgcy_PTDF(int c, int k, int idxMonitorBrc, int idxInterfaceLine) throws GRBException {
		SensitivityFactorXL senstvtFactor = _sysModel.getSenstvtFactor();
		BrcCtgcyListXL keyBrcCtgcy = _sysModel.getKeyBrcList();
		BusListXL buses = _sysModel.getBuses();
		BusGrpXL busGrp = _sysModel.getBusGrp();
		LoadListXL loads = _sysModel.getLoads();

		if (senstvtFactor.isUseSparsePTDF() == true) {
			//TODO: seems no need to implement this feature.
			
		} else {
		    GRBLinExpr expr = new GRBLinExpr();
		    expr.addTerm(1.0, _pk_c[c][k]);
			int idxBrcCtgcy = keyBrcCtgcy.getCtgcyBrcIdx(c);
			double[] otdfDense = senstvtFactor.getOTDFMonBrc(idxBrcCtgcy, idxMonitorBrc);
			
		    if (keyBrcCtgcy.isUsePkInit() == true) {
		    	double pkcInit = 0;
		    	if (keyBrcCtgcy.isUsePkcInit() == true && keyBrcCtgcy.getIsMonitorAllBrc() == false 
		    			&& keyBrcCtgcy.isSimAllPotentialCtgcy() == false ) {
		    		if (idxInterfaceLine == -1) pkcInit = keyBrcCtgcy.getCtgcyPkcInit(c)[k];
		    		else pkcInit = keyBrcCtgcy.getPkcInitInterfaceLines(c)[idxInterfaceLine];
		    	} else {
					pkcInit = _sysModel.getBranches().getPkInit(idxMonitorBrc);
					double lodf = senstvtFactor.getLODF(idxBrcCtgcy, idxMonitorBrc);
					pkcInit += lodf*_sysModel.getBranches().getPkInit(idxBrcCtgcy);
		    	}
				
				for (int n=0; n<buses.size(); n++) {
					if (isAbsValueLTCutoffValue(otdfDense[n]) == true) continue;
					
					/* load impact on line flow */
					int[] idxLoad = busGrp.getLoadIndex(n);
					if (idxLoad != null) {
						for (int d=0; d<idxLoad.length; d++)
						{
							if (_enforceSameLoadShed == false) expr.addTerm(-otdfDense[n], _pdShedded_c[c][idxLoad[d]]);
							else expr.addTerm(-otdfDense[n], _pdShedded[idxLoad[d]]);
							double ploadDiff = loads.getPloadDiff(idxLoad[d]);
							pkcInit -= ploadDiff*otdfDense[n];
						}
					}
					
					/* gen impact on line flow */
					int[] idxGen = busGrp.getGenIndex(n);  
					if (idxGen != null) {
						for (int g=0; g<idxGen.length; g++) {
							if(_preventiveCtrl == false) expr.addTerm(-otdfDense[n], _pg_c[c][idxGen[g]]);
							else expr.addTerm(-otdfDense[n], _pg[idxGen[g]]);
							if (_sysModel.getGens().isInSvc(idxGen[g]) == false) continue;
							double pgInit = _sysModel.getGens().getPgInit(idxGen[g]);
							pkcInit -= pgInit*otdfDense[n];
						}
					}
				}
				_grbModel.addConstr(expr, GRB.EQUAL, pkcInit, "");
			} else {
				double pkcFrmLoadInj = 0;
				/* Load injection impact on line flow */
				for (int n=0; n<buses.size(); n++) {
					if (isAbsValueLTCutoffValue(otdfDense[n]) == true) continue;
					int[] idxLoad = busGrp.getLoadIndex(n);
					if (idxLoad == null) continue;
					for (int d=0; d<idxLoad.length; d++) {
						int idx = idxLoad[d];
						if (_enforceSameLoadShed == false) expr.addTerm(-otdfDense[n], _pdShedded_c[c][idx]);
						else expr.addTerm(-otdfDense[n], _pdShedded[idx]);
						if (loads.isInSvc(idx) == true) pkcFrmLoadInj -= otdfDense[n]*loads.getPLoad(idx);
					}
				}
				/* Generation injection impact on line flow */
				for (int n=0; n<buses.size(); n++) {
					if (isAbsValueLTCutoffValue(otdfDense[n]) == true) continue;
					int[] idxGen = busGrp.getGenIndex(n);
					if (idxGen == null) continue;
					for (int g=0; g<idxGen.length; g++) {
						if(_preventiveCtrl == false) expr.addTerm(-otdfDense[n], _pg_c[c][idxGen[g]]);
						else expr.addTerm(-otdfDense[n], _pg[idxGen[g]]);
					}
				}
				_grbModel.addConstr(expr, GRB.EQUAL, pkcFrmLoadInj, "");
			}
		}
	}
	
	/** Line thermal capacity limit UNDER CONTINGENCY */
	public void addBrcPfCap_BrcCtgcy() throws GRBException {
		initGRBConstr_Pkc_BrcCtgcy();
		BrcCtgcyListXL brcCtgcy = _sysModel.getKeyBrcList();
		int size = brcCtgcy.size();

		double[] limitC = _sysModel.getBranches().getBrcCtgcyRating();
		int nLinesMonitor = limitC.length;
		for (int c=0; c<size; c++) {
			if (brcCtgcy.isCtgcyActive(c) == false) continue;
			
			if (brcCtgcy.getIsMonitorAllBrc() == true) {
				for (int i=0; i<limitC.length; i++) {
					if (_pk_c[c][i] == null) continue;
					int idxCtgcyBrc = brcCtgcy.getCtgcyBrcIdx(c);
					if (i == idxCtgcyBrc) continue;
					if (_sysModel.getBranches().isInSvc(i) == true) {
						
						if (Double.isNaN(limitC[i])) {
							String mess = "In the contingency case "+(c+1)+" with contingency branch "+(idxCtgcyBrc+1)
									+", the thermal constraint for branch "+(i+1)+" is skipped due to its limit is "+limitC[i]+", which is NOT a number";
							_diary.hotLine(LogTypeXL.Error, mess); System.err.println(mess);
							continue;
						} else if (limitC[i] <= _tolForMonitor) {
							_diary.hotLine(LogTypeXL.Warning, "In the contingency case "+(c+1)+" with contingency branch "+(idxCtgcyBrc+1)
									+", the thermal constraint for branch "+(i+1)+" is skipped due to its limit "+limitC[i]+" is <= the tolerance "+_tolForMonitor);
							continue;
						}
						_constPostvLimitPk_c[c][i] = _grbModel.addConstr(_pk_c[c][i], GRB.LESS_EQUAL, limitC[i], "");
						_constNegtvLimitPk_c[c][i] = _grbModel.addConstr(_pk_c[c][i], GRB.GREATER_EQUAL, -limitC[i], "");
					}
				}
			} else {
				int[] monitorSet = brcCtgcy.getCtgcyCaseMonitorSet(c);
				double[] limits = brcCtgcy.getCtgcyPkcLimit(c);
				boolean[] isConstActive = brcCtgcy.getIsConstActive(c);
				if (monitorSet != null) {
					nLinesMonitor = monitorSet.length;
					for (int i=0; i<nLinesMonitor; i++) {
						int idxBrc = monitorSet[i];
						//if (_pk_c[c][i] == null) continue;
						int idxCtgcyBrc = brcCtgcy.getCtgcyBrcIdx(c);
						if (idxBrc == idxCtgcyBrc) continue;
						if (isConstActive != null && isConstActive[i] == false) continue;
						if (_sysModel.getBranches().isInSvc(idxBrc) == true) {
							double limit = limitC[idxBrc];
							if (brcCtgcy.isUsePkcInit() == true 
									&& brcCtgcy.isSimAllPotentialCtgcy() == false) limit = limits[i];

							if (Double.isNaN(limit)) {
								String mess = "In the contingency case "+(c+1)+" with contingency branch "+(idxCtgcyBrc+1)
										+", the thermal constraint for branch "+(idxBrc+1)+" is skipped due to its limit is "+limit+", which is NOT a number";
								_diary.hotLine(LogTypeXL.Error, mess); System.err.println(mess);
								continue;
							} else if (limit <= _tolForMonitor) {
								_diary.hotLine(LogTypeXL.Warning, "In the contingency case "+(c+1)+" with contingency branch "+(idxCtgcyBrc+1)
										+", the thermal constraint for branch "+(idxBrc+1)+" is skipped due to its limit "+limit+" is <= the tolerance "+_tolForMonitor);
								continue;
							}
							_constPostvLimitPk_c[c][i] = _grbModel.addConstr(_pk_c[c][i], GRB.LESS_EQUAL, limit, "");
							_constNegtvLimitPk_c[c][i] = _grbModel.addConstr(_pk_c[c][i], GRB.GREATER_EQUAL, -limit, "");
						}
					}
				}
			}
		}
		_diary.hotLine("Branch thermal limit constraints were added in the gurobi model");
	}
	
	/** Gen output limit UNDER CONTINGENCY */
	public void addGenCapLimit_BrcCtgcy() throws GRBException {
		GenListXL gens = _sysModel.getGens();
		double[] pgmax = gens.getPgmax();
		double[] pgmin = gens.getPgmin();
		int size = _sysModel.getKeyBrcList().size();
		for (int c=0; c<size; c++) {
			if (_sysModel.getKeyBrcList().isCtgcyActive(c) == false) continue;
			for (int g=0; g<gens.size(); g++) {
				if (gens.isInSvc(g) == false) {
					_grbModel.addConstr(_pg_c[c][g], GRB.EQUAL, 0, "");
				} else {
					_grbModel.addConstr(_pg_c[c][g], GRB.LESS_EQUAL, pgmax[g], "");
					if (_usePgLBSV == false) _grbModel.addConstr(_pg_c[c][g], GRB.GREATER_EQUAL, pgmin[g], "");
					else {
						GRBLinExpr expr = new GRBLinExpr();
						expr.addTerm(1.0, _pg_c[c][g]);
						expr.addTerm(1.0, _pgLBSV[g]);
						_grbModel.addConstr(expr, GRB.GREATER_EQUAL, pgmin[g], "");
					}
				}
			}
		}
	}

	/** Gen output limit UNDER CONTINGENCY */
	public void addGenFixed_BrcCtgcy() throws GRBException {
		GenListXL gens = _sysModel.getGens();
		int size = _sysModel.getKeyBrcList().size();
		for (int c=0; c<size; c++) {
			if (_sysModel.getKeyBrcList().isCtgcyActive(c) == false) continue;
			for (int g=0; g<gens.size(); g++) {
				if (gens.isInSvc(g) == true && gens.hasCostCurveFlag(g) == false) {
					_grbModel.addConstr(_pg_c[c][g], GRB.EQUAL, gens.getPgInit(g), "");
				}
			}
		}
	}

	/** Gen output limit UNDER CONTINGENCY */
	public void addSpinRamp_BrcCtgcy() throws GRBException {
		GenListXL gens = _sysModel.getGens();
		int size = _sysModel.getKeyBrcList().size();
		for (int c=0; c<size; c++) {
			if (_sysModel.getKeyBrcList().isCtgcyActive(c) == false) continue;
			for (int g=0; g<gens.size(); g++) {
				if (gens.isInSvc(g) == false) continue;
			    GRBLinExpr expr = new GRBLinExpr();
			    expr.addTerm(1, _pg_c[c][g]);
			    expr.addTerm(-1, _pg[g]);
			    double ramp = gens.getSpinRamp(g)*_TimeSR;
				_grbModel.addConstr(expr, GRB.LESS_EQUAL, ramp, "");
				_grbModel.addConstr(expr, GRB.GREATER_EQUAL, -ramp, "");
			}
		}
	}

	/** Interface limit UNDER CONTINGENCY */
	public void addInterfaceLimit_BrcCtgcy() throws GRBException {
		initGRBConstr_Interface_BrcCtgcy();
		InterfaceListXL interfaces = _sysModel.getInterfaceList();
		double[] limits = interfaces.getTotalLimit();
		double[][] emgcyLimits = interfaces.getInterfaceEmgcyLimits();
		int size = _sysModel.getKeyBrcList().size();
		for (int c=0; c<size; c++) {
			if (_sysModel.getKeyBrcList().isCtgcyActive(c) == false) continue;
			if (_sysModel.getKeyBrcList().isCtgcyActive4Interface(c) == false) continue;
			boolean[] isActive = _sysModel.getKeyBrcList().getIsInterfaceActiveCtgcyCase(c); /*interfaces.isInterfaceActive();*/
			int idxCtgcyBrc = _sysModel.getKeyBrcList().getCtgcyBrcIdx(c);
			int[] tmpIndices = interfaces.getItemIdxPerInterface(idxCtgcyBrc);
			for (int i=0; i<isActive.length; i++) {
				if (isActive[i] == true) {
					double limit = limits[i];
					if (tmpIndices[i] != -1) limit = emgcyLimits[i][tmpIndices[i]];
					_constPostvLimitInterface_c[c][i] = _grbModel.addConstr(_pInterface_c[c][i], GRB.LESS_EQUAL, limit, "");
					_constNegtvLimitInterface_c[c][i] = _grbModel.addConstr(_pInterface_c[c][i], GRB.GREATER_EQUAL, -limit, "");
				}
			}
		}
	}
	
	/** Interface flow calc UNDER CONTINGENCY */
	public void addInterfaceFlowCalc_BrcCtgcy() throws GRBException {
		InterfaceListXL interfaces = _sysModel.getInterfaceList();
		int[][] interfaceLines = interfaces.getInterfaceLines();
		boolean[][] interfaceLinesDirection = interfaces.getInterfaceLinesDirection();
		int size = _sysModel.getKeyBrcList().size();
		for (int c=0; c<size; c++) {
			if (_sysModel.getKeyBrcList().isCtgcyActive(c) == false) continue;
			if (_sysModel.getKeyBrcList().isCtgcyActive4Interface(c) == false) continue;
			int[] monitorSet = _sysModel.getKeyBrcList().getCtgcyCaseMonitorSet(c);
			int nMonitorSet = (monitorSet == null) ? 0 : monitorSet.length;
			HashMap<Integer, Integer> idxMap = getInterfaceLineIdxMap(c);
			boolean[] isActive = _sysModel.getKeyBrcList().getIsInterfaceActiveCtgcyCase(c);
			for (int i=0; i<isActive.length; i++) {
				if (isActive[i] == false) _grbModel.addConstr(_pInterface_c[c][i], GRB.EQUAL, 0, "");
				else {
					GRBLinExpr expr = new GRBLinExpr();
					for (int j=0; j<interfaceLines[i].length; j++) {
						int idxBrc = interfaceLines[i][j];
						int k = -1;
						if (idxMap.containsKey(idxBrc) == true) k = idxMap.get(idxBrc) + nMonitorSet;
						if (interfaceLinesDirection[i][j] == true) expr.addTerm(1.0, _pk_c[c][k]);
						else expr.addTerm(-1.0, _pk_c[c][k]);
					}
					expr.addTerm(-1.0, _pInterface_c[c][i]);
					_grbModel.addConstr(expr, GRB.EQUAL, 0, "");
				}
			}
		}
	}
	
	private HashMap<Integer, Integer> getInterfaceLineIdxMap(int c) {
		HashMap<Integer, Integer> idxMap = new HashMap<Integer, Integer>();
		int[] monInterfaceLines = _sysModel.getKeyBrcList().getMonInterfaceLines(c);
		int sizeMonInterfaceLines = (monInterfaceLines == null) ? 0 : monInterfaceLines.length;
		for (int k=0; k<sizeMonInterfaceLines; k++)
			idxMap.put(monInterfaceLines[k], k);
		return idxMap;
	}

	/** Inactive contingency - all related variables are zeros */
	public void addInactive_BrcCtgcy() throws GRBException {
		int size = _sysModel.getKeyBrcList().size();
		for (int c=0; c<size; c++) {
			if (_sysModel.getKeyBrcList().isCtgcyActive(c) == true) continue;
			
			if (_preventiveCtrl == false) {
				int sizeG = _sysModel.getGens().size();
				for (int g=0; g<sizeG; g++)
					_grbModel.addConstr(_pg_c[c][g], GRB.EQUAL, 0, "");
			}
			
			if (_sysModel.getSenstvtFactor().isUsePTDFforSCED() == false) {
				int sizeBus = _sysModel.getBuses().size();
				for (int n=0; n<sizeBus; n++)
					_grbModel.addConstr(_theta_c[c][n], GRB.EQUAL, 0, "");
			}

			if (_enforceSameLoadShed == false) {
				int sizeLoad = _sysModel.getLoads().size();
				for (int d=0; d<sizeLoad; d++)
					_grbModel.addConstr(_pdShedded_c[c][d], GRB.EQUAL, 0, "");
			}
			
			int sizeInterface = _sysModel.getInterfaceList().size();
			for (int i=0; i<sizeInterface; i++)
				_grbModel.addConstr(_pInterface_c[c][i], GRB.EQUAL, 0, "");
		}
	}

//	/** Preventive control mechanism */
//	private void preventiveCtrl_BrcCtgcy() throws GRBException {
//		int size = _sysModel.getKeyBrcList().size();
//		for (int c=0; c<size; c++) {
//			if (_sysModel.getKeyBrcList().isCtgcyActive(c) == false) continue;
//			
//			int sizeG = _sysModel.getGens().size();
//			for (int g=0; g<sizeG; g++) {
//				GRBLinExpr expr = new GRBLinExpr();
//				expr.addTerm(1.0, _pg_c[c][g]);
//				expr.addTerm(-1.0, _pg[g]);
//				_grbModel.addConstr(expr, GRB.EQUAL, 0, "");
//			}
//		}
//	}

	/** Disable load shed in the model */
	public void forceLoadShedBeZero() throws GRBException {
		int nload = _pdShedded.length;
		for (int i=0; i<nload; i++)
			_grbModel.addConstr(_pdShedded[i], GRB.EQUAL, 0, "");
		if (_enforceSameLoadShed == false) {
			int nctgcy = _pk_c.length;
			for (int c=0; c<nctgcy; c++)
				for (int i=0; i<nload; i++)
					_grbModel.addConstr(_pdShedded_c[c][i], GRB.EQUAL, 0, "");
		}
		_diary.hotLine("All load shed variables are forced to be zero in the model");
	}
	
//	/** Reserve requirement - 002 */
//	private void addReserveReq(double[] pgmax, double totalLoad) throws GRBException {
////		double prop = 0.06;  // proportional of total load as reserve requirement
//	    GRBLinExpr expr = new GRBLinExpr();
//		for (int i=0; i<_reserve.length; i++)
//		    expr.addTerm(1.0, _reserve[i]);
////		_model.addConstr(expr, GRB.GREATER_EQUAL, totalLoad*prop, "");
//		_model.addConstr(expr, GRB.GREATER_EQUAL, totalLoad*_propReserve, "");
//		_diary.hotLine("Reserve requirements were added in the gurobi model");
//		
//		for (int i=0; i<_reserve.length; i++) {
//			expr = new GRBLinExpr();
//			expr.addTerm(1.0, _reserve[i]); expr.addTerm(1.0, _pg[i]);
//			_model.addConstr(expr, GRB.LESS_EQUAL, pgmax[i], "");
//		}
//	}
			
	
	/** General way to define a one-dimension variable in Gurobi */
	private void defVar(GRBVar[][] vars) throws GRBException {
		for (int i=0; i<vars.length; i++)
			defVar(vars[i]);
	}

	/** General way to define a two-dimension variable in Gurobi */
	private void defVar(GRBVar[] vars) throws GRBException {
	    for (int i=0; i<vars.length; i++) {
	    	vars[i] = _grbModel.addVar(-GRB.INFINITY, GRB.INFINITY, 0, GRB.CONTINUOUS, "");
	    }
	}
	
	/** Initialize GRB Constraint for branch thermal limit in base-case */
	private void initGRBConstr_NodePB() {
		int nbuses = _sysModel.getBuses().size();
		_constNodePB = new GRBConstr[nbuses];
	}
	
	/** Initialize GRB Constraint for branch thermal limit in base-case */
	private void initGRBConstr_Pk() {
		int nlines = _sysModel.getBranches().size();
		_constPostvLimitPk = new GRBConstr[nlines];
		_constNegtvLimitPk = new GRBConstr[nlines];
	}
	
	/** Initialize GRB Constraint for branch thermal limit in contingency-case */
	private void initGRBConstr_Pkc_BrcCtgcy() {
		int nCtgcy = _sysModel.getKeyBrcList().size();
		int nlines = _sysModel.getBranches().size();
		if (_sysModel.getSenstvtFactor().isUsePTDFforSCED() == false) {_constPostvLimitPk_c = new GRBConstr[nCtgcy][nlines];_constNegtvLimitPk_c = new GRBConstr[nCtgcy][nlines];}
		else if (_sysModel.getKeyBrcList().getIsMonitorAllBrc() == true) {_constPostvLimitPk_c = new GRBConstr[nCtgcy][nlines];_constNegtvLimitPk_c = new GRBConstr[nCtgcy][nlines];}
		else {
			_constPostvLimitPk_c = new GRBConstr[nCtgcy][];
			_constNegtvLimitPk_c = new GRBConstr[nCtgcy][];
			for (int c=0; c<nCtgcy; c++) {
				int[] monitorSet = _sysModel.getKeyBrcList().getCtgcyCaseMonitorSet(c);
				int sizePkc = 0;
				if (monitorSet != null) sizePkc += monitorSet.length;
				for (int i=0; i<sizePkc; i++) {
					_constPostvLimitPk_c[c] = new GRBConstr[sizePkc];
					_constNegtvLimitPk_c[c] = new GRBConstr[sizePkc];
				}
			}
		}
	}
	
	/** Initialize GRB Constraint for interface limit in base-case */
	private void initGRBConstr_Interface() {
		int nInterface = _sysModel.getInterfaceList().size();
		_constPostvLimitInterface = new GRBConstr[nInterface];
		_constNegtvLimitInterface = new GRBConstr[nInterface];
	}
	
	/** Initialize GRB Constraint for interface limit in contingency-case */
	private void initGRBConstr_Interface_BrcCtgcy() {
		int nCtgcy = _sysModel.getKeyBrcList().size();
		int nInterface = _sysModel.getInterfaceList().size();
		_constPostvLimitInterface_c = new GRBConstr[nCtgcy][nInterface];
		_constNegtvLimitInterface_c = new GRBConstr[nCtgcy][nInterface];
	}

	
	/** Launch the gurobi solver */
	public void solve() throws GRBException {
		logSetting();
		_diary.hotLine(LogTypeXL.MileStone, "Gurobi starts solving");
		
		_grbModel.getEnv().set(GRB.DoubleParam.MIPGap, 0);
		_grbModel.getEnv().set(GRB.DoubleParam.TimeLimit, 60);
		_grbModel.getEnv().set(GRB.IntParam.Threads, 1);
		//_grbModel.getEnv().set(GRB.IntParam.Method, 1);
		_grbModel.optimize();
		_diary.hotLine(LogTypeXL.MileStone, "Gurobi finished solving");
		
		_grbStatus = _grbModel.get(GRB.IntAttr.Status);
		if (_grbStatus == GRB.OPTIMAL) {
			_objValue = _grbModel.get(GRB.DoubleAttr.ObjVal);
		    System.out.println("Obj: " + _objValue);
		} else System.err.println("Gurobi fails to solve the SCED problem...");
		saveResults();
		
	    // Dispose of model and environment
	    _grbModel.dispose(); //TODO: think about when to dispose Gurobi
	    _env.dispose();
		_diary.hotLine(LogTypeXL.MileStone, "Gurobi was disposed");
	}
	
	private void logSetting() {
		_diary.hotLine(LogTypeXL.CheckPoint, "PTDF matrix cutoff value is: "+_cutOffValue);
		_diary.hotLine(LogTypeXL.CheckPoint, "Time frame for one SCED period is: "+_TimeED+" minutes");
		_diary.hotLine(LogTypeXL.CheckPoint, "Time frame for spinning reserve is: "+_TimeSR+" minutes");
		_diary.hotLine(LogTypeXL.CheckPoint, "Price for 1p.u. spinning reserve is: $"+_priceSR);
		_diary.hotLine(LogTypeXL.CheckPoint, "Thermal constraints are NOT enforced for the branches "
				+ "that the associated limit is <= "+_tolForMonitor+" p.u.");
		
		if (_useLoadShedSV == false) _diary.hotLine(LogTypeXL.CheckPoint, "load shed feature is NOT modeled in SCED");
		else {
			_diary.hotLine(LogTypeXL.CheckPoint, "load shed feature is modeled in SCED");
			_diary.hotLine(LogTypeXL.CheckPoint, "Cost for 1p.u. load shed is: $"+_penaltyLoadShed);
		}
		
		if (_preventiveCtrl == true) _diary.hotLine(LogTypeXL.CheckPoint, "Preventive control is used for SCED, i.e. Pg==Pgc");
		else _diary.hotLine(LogTypeXL.CheckPoint, "Corrective control is used for SCED, which implies that Pgc could be different with Pg");
		
		if (_usePgLBSV == true) _diary.hotLine(LogTypeXL.CheckPoint, "Slack variables for relaxing Pgmin limit "
				+ "is modeld in SCED with a penalty cost of $"+_penaltyPgLBSV+" per unit");
	}

	private void saveResults() throws GRBException {
		saveBaseCaseResults();
		saveCtgcyCaseResults();
		
		if (_usePgLBSV == true) {
			_pgLBSV1 = new double[_pg.length];
			for (int g=0; g<_pg.length; g++)
				_pgLBSV1[g] = _pgLBSV[g].get(GRB.DoubleAttr.X);
		}
		
		saveDualResults();
	}
	
	

	private void saveBaseCaseResults() throws GRBException {
		saveBaseCaseGenResults();
		saveBaseCaseBrcResults();
		saveBaseCaseOtherResults();
	}
	
	private void saveBaseCaseGenResults() throws GRBException {
		_pg1 = new double[_pg.length];
		for (int i=0; i<_pg.length; i++) {
			_pg1[i] = _pg[i].get(GRB.DoubleAttr.X);
		}
		
		_reserve1 = new double[_reserve.length];
		for (int i=0; i<_reserve.length; i++) {
			_reserve1[i] = _reserve[i].get(GRB.DoubleAttr.X);
		}
		
		_pgi1 = new double[_pgi.length][];
		for (int i=0; i<_pgi.length; i++) {
			_pgi1[i] = new double[_pgi[i].length];
			for (int j=0; j<_pgi[i].length; j++)
				_pgi1[i][j] = _pgi[i][j].get(GRB.DoubleAttr.X);
		}

		double totalVarCost = _sysModel.getGens().getSysTotalVarCost(_pgi1);
		String info = "After SCED, the system total operation/variable cost is $" + totalVarCost;
		_diary.hotLine(info); System.out.println(info);
		
		double totalReserve = 0;
		for (int i=0; i<_reserve.length; i++)
			totalReserve += _reserve1[i] * _priceSR;
		info = "After SCED, the system total reserve cost is $" + totalReserve;
		_diary.hotLine(info); System.out.println(info+"\n");
	}
	
	private void saveBaseCaseBrcResults() throws GRBException {
		_pk1 = new double[_pk.length];
		double[] rating = _sysModel.getBranches().getBrcNormalRating();
		for (int i=0; i<_pk.length; i++) {
			_pk1[i] = _pk[i].get(GRB.DoubleAttr.X);
			if (rating[i] > _tolForMonitor) checkBrcLimit(i, _pk1[i], rating[i]);
		}
	}

	private void saveBaseCaseOtherResults() throws GRBException {
		if (_sysModel.getSenstvtFactor().isUsePTDFforSCED() == false) {
			_theta1 = new double[_theta.length];
			for (int i=0; i<_theta.length; i++) {
				_theta1[i] = _theta[i].get(GRB.DoubleAttr.X);
			}
		}
		
		_pdShedded1 = new double[_pdShedded.length];
		for (int i=0; i<_pdShedded.length; i++) {
			_pdShedded1[i] = _pdShedded[i].get(GRB.DoubleAttr.X);
		}

		int nInterface = _sysModel.getInterfaceList().size();
		_pInterface1 = new double[nInterface];
		for (int i=0; i<nInterface; i++) {
			_pInterface1[i] = _pInterface[i].get(GRB.DoubleAttr.X);
		}
	}
	
	private void saveCtgcyCaseResults() throws GRBException {
		int nCtgcy = _pk_c.length;
		if (nCtgcy == 0) return;

		boolean report = true;
		if (report == true) saveAndReportCtgcyCaseBrcResults(nCtgcy);
		else saveCtgcyCaseBrcResults(nCtgcy);
		
		if (_preventiveCtrl == false) saveCtgcyCaseGenResults(nCtgcy);
		saveCtgcyCaseOtherResults(nCtgcy);
	}
	
	private void saveCtgcyCaseGenResults(int nCtgcy) throws GRBException {
		double[][] pgc1 = new double[nCtgcy][];
		for (int c=0; c<nCtgcy; c++) {
			pgc1[c] =  new double[_pg_c[c].length];
			for (int i=0; i<_pg_c[c].length; i++)
				pgc1[c][i] = _pg_c[c][i].get(GRB.DoubleAttr.X);
		}
	}
	private void saveCtgcyCaseBrcResults(int nCtgcy) throws GRBException {
		if (_sysModel.getKeyBrcList().getIsMonitorAllBrc() == true)
		{
			int nBrc = _pk_c[0].length;
			_pk_c1 = new double[nCtgcy][nBrc];
			for (int c=0; c<nCtgcy; c++) 
				for (int i=0; i<nBrc; i++) {
					_pk_c1[c][i] = _pk_c[c][i].get(GRB.DoubleAttr.X);
				}
		} else {
			_pk_c1 = new double[nCtgcy][];
			for (int c=0; c<nCtgcy; c++) {
				int[] monitorSet = _sysModel.getKeyBrcList().getCtgcyCaseMonitorSet(c);
				int nMonitorSet = (monitorSet == null) ? 0 : monitorSet.length;
				_pk_c1[c] = new double[nMonitorSet];
				for (int i=0; i<nMonitorSet; i++) {
					_pk_c1[c][i] = _pk_c[c][i].get(GRB.DoubleAttr.X);
				}
			}
		}
	}
	private void saveAndReportCtgcyCaseBrcResults(int nCtgcy) throws GRBException {
		double[] rating = _sysModel.getBranches().getBrcCtgcyRating();
		if (_sysModel.getKeyBrcList().getIsMonitorAllBrc() == true)
		{
			int nBrc = rating.length;
			_pk_c1 = new double[nCtgcy][nBrc];
			for (int c=0; c<nCtgcy; c++) 
				for (int i=0; i<nBrc; i++) {
					_pk_c1[c][i] = _pk_c[c][i].get(GRB.DoubleAttr.X);
					if (rating[i] > _tolForMonitor) checkBrcLimit(c, i, _pk_c1[c][i], rating[i]);
				}
		} else {
			_pk_c1 = new double[nCtgcy][];
			for (int c=0; c<nCtgcy; c++) {
				int[] monitorSet = _sysModel.getKeyBrcList().getCtgcyCaseMonitorSet(c);
				int nMonitorSet = (monitorSet == null) ? 0 : monitorSet.length;
				_pk_c1[c] = new double[nMonitorSet];
				double[] limits = _sysModel.getKeyBrcList().getCtgcyPkcLimit(c);
				boolean[] isConstModeled = _sysModel.getKeyBrcList().getIsConstActive(c);
				for (int i=0; i<nMonitorSet; i++) {
					int idxBrc = monitorSet[i];
					_pk_c1[c][i] = _pk_c[c][i].get(GRB.DoubleAttr.X);
					if (isConstModeled != null && isConstModeled[i] == false) continue;
					if (idxBrc == _sysModel.getKeyBrcList().getCtgcyBrcIdx(c)) continue;
					else if (_sysModel.getBranches().isInSvc(idxBrc) == true) {
						double limit = rating[idxBrc];
						if (_sysModel.getKeyBrcList().isUsePkcInit() == true 
								&& _sysModel.getKeyBrcList().isSimAllPotentialCtgcy() == false) limit = limits[i];
						if (limit > _tolForMonitor) checkBrcLimit(c, idxBrc, _pk_c1[c][i], limit);
					}
				}
			}
		}
	}
	
	private void saveCtgcyCaseOtherResults(int nCtgcy) throws GRBException {
		if (_enforceSameLoadShed == false) {
			_pdShedded_c1 = new double[nCtgcy][];
			for (int c=0; c<nCtgcy; c++) {
				_pdShedded_c1[c] = new double[_pdShedded_c[c].length];
				for (int i=0; i<_pdShedded_c[c].length; i++)
					_pdShedded_c1[c][i] = _pdShedded_c[c][i].get(GRB.DoubleAttr.X);
			}
		} 
		
		int nInterface = _sysModel.getInterfaceList().size();
		if (nInterface == 0) return;
		int[] interfaceCtgcyBrcIdx = _sysModel.getKeyBrcList().getInterfaceCtgcyBrcIdx();
		if (interfaceCtgcyBrcIdx == null) return;
		int nActiveCtgcy4Interface = interfaceCtgcyBrcIdx.length;
		if (nActiveCtgcy4Interface == 0) return;

		_pInterface_c1 = new double[nCtgcy][];
	    _pkInterfaceLine_c1 = new double[nCtgcy][][];
		int[][] interfaceLines = _sysModel.getInterfaceList().getInterfaceLines();

		for (int c=0; c<nCtgcy; c++) {
			if (_sysModel.getKeyBrcList().isCtgcyActive(c) == false) continue;
			if (_sysModel.getKeyBrcList().isCtgcyActive4Interface(c) == false) continue;
			_pInterface_c1[c] = new double[nInterface];
			_pkInterfaceLine_c1[c] = new double[nInterface][];
			
			int[] monitorSet = _sysModel.getKeyBrcList().getCtgcyCaseMonitorSet(c);
			int nMonitorSet = (monitorSet == null) ? 0 : monitorSet.length;
			HashMap<Integer, Integer> idxMap = getInterfaceLineIdxMap(c);
			boolean[] isActive = _sysModel.getKeyBrcList().getIsInterfaceActiveCtgcyCase(c); /* similar to interfaces.isInterfaceActive() in base case */
			for (int i=0; i<isActive.length; i++) {
				if (isActive[i] == false) continue;
				_pInterface_c1[c][i] = _pInterface_c[c][i].get(GRB.DoubleAttr.X);
				_pkInterfaceLine_c1[c][i] = new double[interfaceLines[i].length];
				for (int j=0; j<interfaceLines[i].length; j++) {
					int idxBrc = interfaceLines[i][j];
					int k = -1;
					if (idxMap.containsKey(idxBrc) == true) k = idxMap.get(idxBrc) + nMonitorSet;
					_pkInterfaceLine_c1[c][i][j] = _pk_c[c][k].get(GRB.DoubleAttr.X);
				}
			}
		}
	}
	
	private void saveDualResults() throws GRBException {
		saveBaseCaseDualResults();
		saveCtgcyCaseDualResults();
	}
	
	private void saveBaseCaseDualResults() throws GRBException {
		if (_isSCEDModeling4LMP == true) {
			double baseMVA = _sysModel.getMVAbase();
			int idxSlackBus = _sysModel.getSenstvtFactor().getFormPTDF().getSlackBusIdx();
			if (_sysModel.getSenstvtFactor().isUsePTDFforSCED() == true) {
				_sysLMP = _constSysPB.get(GRB.DoubleAttr.Pi);
				System.out.println("PTDF model: the system-wide LMP (LMP of slack bus "+ (idxSlackBus+1) + ") is: "+_sysLMP/baseMVA + "$/MWh");
			} else {
				int nbuses = _sysModel.getBuses().size();
				_nodalLMP = new double[nbuses];
				for (int i=0; i<nbuses; i++)
					_nodalLMP[i] = _constNodePB[i].get(GRB.DoubleAttr.Pi);
				System.out.println("B-theta model: the LMP of slack bus " + (idxSlackBus+1) + " is: "+_nodalLMP[idxSlackBus]/baseMVA + "$/MWh");
			}
		}
		
		int nBrc = _sysModel.getBranches().size();
	    _dualVarPostvLimitPk1 = new double[nBrc];
	    _dualVarNegtvLimitPk1 = new double[nBrc];
	    _isDualAvailableLimitPk1 = new boolean[nBrc];
	    
	    for (int i=0; i<nBrc; i++)
	    {
	    	if (_constPostvLimitPk[i] == null) continue;
	    	_isDualAvailableLimitPk1[i] = true;
	    	_dualVarPostvLimitPk1[i] = _constPostvLimitPk[i].get(GRB.DoubleAttr.Pi);
	    	_dualVarNegtvLimitPk1[i] = _constNegtvLimitPk[i].get(GRB.DoubleAttr.Pi);
	    	
	    	if (_dualVarPostvLimitPk1[i] != 0) {
	    		String mess = "base-case flow postive limit, idxMonBrc: "+(i+1)+", dual: "+_dualVarPostvLimitPk1[i];
		    	_diary.hotLine(mess); System.out.println(mess);
	    	}
	    	if (_dualVarNegtvLimitPk1[i] != 0) {
	    		String mess = "base-case flow negative limit, idxMonBrc: "+(i+1)+", dual: "+_dualVarNegtvLimitPk1[i];
		    	_diary.hotLine(mess); System.out.println(mess);
	    	}
	    }
	    
	    /* Interface */
	    int nInterface = _sysModel.getInterfaceList().size();
	    if (nInterface == 0) return;
	    _isDualAvailableLimitInterface1 = new boolean[nInterface];
	    _dualVarPostvLimitInterface1 = new double[nInterface];
	    _dualVarNegtvLimitInterface1 = new double[nInterface];
	    
	    for (int i=0; i<nInterface; i++) {
	    	if (_constPostvLimitInterface[i] == null) continue;
	    	_isDualAvailableLimitInterface1[i] = true;
	    	_dualVarPostvLimitInterface1[i] = _constPostvLimitInterface[i].get(GRB.DoubleAttr.Pi);
	    	_dualVarNegtvLimitInterface1[i] = _constNegtvLimitInterface[i].get(GRB.DoubleAttr.Pi);
	   
	    	if (_dualVarPostvLimitInterface1[i] != 0) {
	    		String mess = "base-case interface flow postive limit, idxInterface: "+(i+1)+", dual: "+_dualVarPostvLimitInterface1[i];
		    	_diary.hotLine(mess); System.out.println(mess);
	    	}
	    	if (_dualVarNegtvLimitInterface1[i] != 0) {
	    		String mess = "base-case interface flow negative limit, idxInterface: "+(i+1)+", dual: "+_dualVarNegtvLimitInterface1[i];
		    	_diary.hotLine(mess); System.out.println(mess);
	    	}
	    }
	}

	private void saveCtgcyCaseDualResults() throws GRBException {
		int nCtgcy = _sysModel.getKeyBrcList().size();
		if (_sysModel.getKeyBrcList().getIsMonitorAllBrc() == true)
		{
			int nBrc = _sysModel.getBranches().size();
		    _isDualAvailableLimitPk_c1 = new boolean[nCtgcy][nBrc];
		    _dualVarPostvLimitPk_c1 = new double[nCtgcy][nBrc];
		    _dualVarNegtvLimitPk_c1 = new double[nCtgcy][nBrc];
			
			for (int c=0; c<nCtgcy; c++) {
	    		int idxCtgcyBrc = _sysModel.getKeyBrcList().getCtgcyBrcIdx(c);
				for (int i=0; i<nBrc; i++) {
			    	if (_constPostvLimitPk_c[c][i] == null) continue;
			    	_isDualAvailableLimitPk_c1[c][i] = true;
			    	_dualVarPostvLimitPk_c1[c][i] = _constPostvLimitPk_c[c][i].get(GRB.DoubleAttr.Pi);
			    	_dualVarNegtvLimitPk_c1[c][i] = _constNegtvLimitPk_c[c][i].get(GRB.DoubleAttr.Pi);
			    	
			    	if (_dualVarPostvLimitPk_c1[c][i] != 0) {
			    		String mess = "contingency-case flow positive limit, c: "+(c+1)+", idxCtgcyBrc: "+(idxCtgcyBrc+1)+", idxMonBrc: "+(i+1)+", dual: "+_dualVarPostvLimitPk_c1[c][i];
				    	_diary.hotLine(mess); System.out.println(mess);
			    	}
			    	if (_dualVarNegtvLimitPk_c1[c][i] != 0) {
			    		String mess = "contingency-case flow negative limit, c: "+(c+1)+", idxCtgcyBrc: "+(idxCtgcyBrc+1)+", idxMonBrc: "+(i+1)+", dual: "+_dualVarNegtvLimitPk_c1[c][i];
				    	_diary.hotLine(mess); System.out.println(mess);
			    	}
				}
			}
			
		} else {
			_isDualAvailableLimitPk_c1 = new boolean[nCtgcy][];
			_dualVarPostvLimitPk_c1 = new double[nCtgcy][];
			_dualVarNegtvLimitPk_c1 = new double[nCtgcy][];
			
			for (int c=0; c<nCtgcy; c++) {
	    		int idxCtgcyBrc = _sysModel.getKeyBrcList().getCtgcyBrcIdx(c);
				int[] monitorSet = _sysModel.getKeyBrcList().getCtgcyCaseMonitorSet(c);
				if (monitorSet == null) {
					_isDualAvailableLimitPk_c1[c] = new boolean[0]; 
					_dualVarPostvLimitPk_c1[c] = new double[0]; 
					_dualVarNegtvLimitPk_c1[c] = new double[0]; 
					continue;
				}
				
				int nMonitor = monitorSet.length;
				_isDualAvailableLimitPk_c1[c] = new boolean[nMonitor]; 
				_dualVarPostvLimitPk_c1[c] = new double[nMonitor]; 
				_dualVarNegtvLimitPk_c1[c] = new double[nMonitor]; 
				for (int i=0; i<nMonitor; i++) {
					int idxBrc = monitorSet[i];
			    	if (_constPostvLimitPk_c[c][i] == null) continue;
			    	_isDualAvailableLimitPk_c1[c][i] = true;
			    	_dualVarPostvLimitPk_c1[c][i] = _constPostvLimitPk_c[c][i].get(GRB.DoubleAttr.Pi);
			    	_dualVarNegtvLimitPk_c1[c][i] = _constNegtvLimitPk_c[c][i].get(GRB.DoubleAttr.Pi);
			    	
			    	if (_dualVarPostvLimitPk_c1[c][i] != 0) {
			    		String mess = "contingency-case flow positive limit, c: "+(c+1)+", idxCtgcyBrc: "+(idxCtgcyBrc+1)+", idxMonBrc: "+(idxBrc+1)+", dual: "+_dualVarPostvLimitPk_c1[c][i];
				    	_diary.hotLine(mess); System.out.println(mess);
			    	}
			    	if (_dualVarNegtvLimitPk_c1[c][i] != 0) {
			    		String mess = "contingency-case flow negative limit, c: "+(c+1)+", idxCtgcyBrc: "+(idxCtgcyBrc+1)+", idxMonBrc: "+(idxBrc+1)+", dual: "+_dualVarNegtvLimitPk_c1[c][i];
				    	_diary.hotLine(mess); System.out.println(mess);
			    	}
				}
			}
		}
		
		/* Interface */		
	    int nInterface = _sysModel.getInterfaceList().size();
	    if (nInterface == 0) return;
	    _isDualAvailableLimitInterface_c1 = new boolean[nCtgcy][nInterface];
	    _dualVarPostvLimitInterface_c1 = new double[nCtgcy][nInterface];
	    _dualVarNegtvLimitInterface_c1 = new double[nCtgcy][nInterface];
	    
		for (int c=0; c<nCtgcy; c++) {
			if (_sysModel.getKeyBrcList().isCtgcyActive(c) == false) continue;
			if (_sysModel.getKeyBrcList().isCtgcyActive4Interface(c) == false) continue;
    		int idxCtgcyBrc = _sysModel.getKeyBrcList().getCtgcyBrcIdx(c);

		    for (int i=0; i<nInterface; i++) {
		    	if (_constPostvLimitInterface_c[c][i] == null) continue;
		    	_isDualAvailableLimitInterface_c1[c][i] = true;
		    	_dualVarPostvLimitInterface_c1[c][i] = _constPostvLimitInterface_c[c][i].get(GRB.DoubleAttr.Pi);
		    	_dualVarNegtvLimitInterface_c1[c][i] = _constNegtvLimitInterface_c[c][i].get(GRB.DoubleAttr.Pi);

		    	if (_dualVarPostvLimitInterface_c1[c][i] != 0) {
		    		String mess = "contingency-case interface flow postive limit, c: "+(c+1)+", idxCtgcyBrc: "+(idxCtgcyBrc+1)+", idxInterface: "+(i+1)+", dual: "+_dualVarPostvLimitInterface_c1[c][i];
			    	_diary.hotLine(mess); System.out.println(mess);
		    	}
		    	if (_dualVarNegtvLimitInterface_c1[c][i] != 0) {
		    		String mess = "contingency-case interface flow negative limit, c: "+(c+1)+", idxCtgcyBrc: "+(idxCtgcyBrc+1)+", idxInterface: "+(i+1)+", dual: "+_dualVarNegtvLimitInterface_c1[c][i];
			    	_diary.hotLine(mess); System.out.println(mess);
		    	}
		    }
		}
	}

	private void checkBrcLimit(int idxBrc, double pk1, double rating) throws GRBException
	{
		if (pk1 == 0) return;
		double pkcAbs = Math.abs(pk1);
		if ((pkcAbs  - rating) > -0.001) {
    		String mess = "monBrcIdx: "+(idxBrc+1)+", pk: "+pk1+", rating: "+rating;
			_diary.hotLine(LogTypeXL.CheckPoint, mess);
			System.out.println(mess);
		}
	}

	private void checkBrcLimit(int c, int idxBrc, double pkc1, double rating) throws GRBException
	{
		if (pkc1 == 0) return;
		double pkcAbs = Math.abs(pkc1);
		if ((pkcAbs  - rating) > -0.001) {
    		int idxCtgcyBrc = _sysModel.getKeyBrcList().getCtgcyBrcIdx(c);
    		String mess = "c: "+(c+1)+", idxCtgcyBrc:"+(idxCtgcyBrc+1)+", monBrcIdx: "+(idxBrc+1)+", pkc: "+pkc1+", rating: "+rating;
			_diary.hotLine(LogTypeXL.CheckPoint, mess);
			System.out.println(mess);
		}
	}

	
	public double getObjValue() {return _objValue;}
	public int getSolverStatus() {return _grbStatus;}
	
	public double[] getPg() {return _pg1;}
	public double[] getReserve() {return _reserve1;}
	public double[] getPk() {return _pk1;}
	
	public double[] getTheta() {return _theta1;}
	public double[][] getPgi() {return _pgi1;}
	public double[] getPdShedded() {return _pdShedded1;}

	public double[][] getPdSheddedCtgcy() {return _pdShedded_c1;}
	public double[][] getPkc() {return _pk_c1;}


	/* Interface flow in base case */
	public double[] getPInterface() {return _pInterface1;}

	/* Interface flow in contingency case */
	public double[][] getPInterfaceCtgcy() {return _pInterface_c1;}
	public double[][][] getPkcInterfaceLineCtgcy() {return _pkInterfaceLine_c1;}


	/* Dual variables for branch limit constraints in base case */
	public boolean[] getIsDualAvailableLimitPk() {return _isDualAvailableLimitPk1;}
	public double[] getDualVarPostvLimitPk() {return _dualVarPostvLimitPk1;}
	public double[] getDualVarNegtvLimitPk() {return _dualVarNegtvLimitPk1;}
	
	/* Dual variables for branch limit constraints in contingency case */
	public boolean[][] getIsDualAvailableLimitPkc() {return _isDualAvailableLimitPk_c1;}
	public double[][] getDualVarPostvLimitPkc() {return _dualVarPostvLimitPk_c1;}
	public double[][] getDualVarNegtvLimitPkc() {return _dualVarNegtvLimitPk_c1;}

	/* Dual variables for interface limit constraints in base case */
	public boolean[] getIsDualAvailableLimitInterface() {return _isDualAvailableLimitInterface1;}
	public double[] getDualVarPostvLimitInterface() {return _dualVarPostvLimitInterface1;}
	public double[] getDualVarNegtvLimitInterface() {return _dualVarNegtvLimitInterface1;}
	
	/* Dual variables for interface limit constraints in contingency case */
	public boolean[][] getIsDualAvailableLimitInterfaceCtgcy() {return _isDualAvailableLimitInterface_c1;}
	public double[][] getDualVarPostvLimitInterfaceCtgcy() {return _dualVarPostvLimitInterface_c1;}
	public double[][] getDualVarNegtvLimitInterfaceCtgcy() {return _dualVarNegtvLimitInterface_c1;}

	public boolean getIsSCEDModeling4LMP() {return _isSCEDModeling4LMP;}
	/* Dual variables for LMP */
	public double getSysLMP() {return _sysLMP;}
	public double[] getNodalLMP() {return _nodalLMP;}
	
}
