package com.sced.model;

import java.util.Arrays;

import com.sced.input.ReadModelDataXL;
import com.sced.input.dev.BranchContingenciesXL;
import com.sced.input.dev.BranchesXL;
import com.sced.input.dev.BusesXL;
import com.sced.input.dev.GeneratorsXL;
import com.sced.input.dev.InterfacesXL;
import com.sced.input.dev.LoadsXL;
import com.sced.model.data.BusGrpXL;
import com.sced.model.data.BusListXL;
import com.sced.model.data.GenListXL;
import com.sced.model.data.BranchListXL;
import com.sced.model.data.InterfaceListXL;
import com.sced.model.data.LoadListXL;
import com.utilxl.log.DiaryXL;
import com.utilxl.log.LogTypeXL;

/**
 * Input Data for SCED
 * 
 * Initialized in March 2017
 * 
 * @author Xingpeng Li, xplipower@gmail.com 
 * 
 */
public class SystemModelXL {

	DiaryXL _diary;
	double _MVAbase = 100;
	
	BusGrpXL _busGrp;
	BusListXL _buses;
	GenListXL _gens;
	LoadListXL _loads;
	
	BranchListXL _branches;
//	XfmList _xfms;
//	PhaseShifterList _phaseshifters;
//	ACBranchList _acbranches;
	
	BrcCtgcyListXL _brcCtgcies; // contain all network constraints
	InterfaceListXL _interfaces;
	MonitorSetXL _monitorSet;
	SensitivityFactorXL _senstvtFactor;
	
	public double getMVAbase() {return _MVAbase;}
	public BusListXL getBuses() {return _buses;}
	public GenListXL getGens() {return _gens;}
	public LoadListXL getLoads() {return _loads;}
	public BranchListXL getBranches() {return _branches;}
	public BusGrpXL getBusGrp() {
		if (_busGrp == null) _busGrp = new BusGrpXL(this);
		return _busGrp;
	}
	public BrcCtgcyListXL getKeyBrcList() {return _brcCtgcies;}
	public InterfaceListXL getInterfaceList() {return _interfaces;}
	public MonitorSetXL getMonitorSet() {return _monitorSet;}
	public SensitivityFactorXL getSenstvtFactor() {return _senstvtFactor;}
	public void clearMonitorSet() {
		_monitorSet = new MonitorSetXL(this);
		_diary.hotLine(LogTypeXL.MileStone, "All data in MonitorSet is removed");
	}
	public void clearKeyBrcList() {
		_brcCtgcies = new BrcCtgcyListXL(this);
		_diary.hotLine(LogTypeXL.MileStone, "All data in KeyBranchList removed");
	}
	public void clearInterfaceList() {
		_interfaces = new InterfaceListXL(this);
		_diary.hotLine(LogTypeXL.MileStone, "All data in InterfaceList is removed");
	}

	public SystemModelXL(DiaryXL diary) {_diary = diary; initial();}
	private void initial()
	{
		_buses = new BusListXL(this);
		_gens = new GenListXL(this);
		_loads = new LoadListXL(this);
 		_branches = new BranchListXL(this);
		
		_brcCtgcies = new BrcCtgcyListXL(this);
		_interfaces = new InterfaceListXL(this);
		_monitorSet = new MonitorSetXL(this);
		_senstvtFactor = new SensitivityFactorXL(this);
		
		_diary.hotLine(LogTypeXL.MileStone, "Class SystemModel was completely initialized");
	}
	
	public DiaryXL getDiary() {return _diary;}
	
	/**  All data should be in per unit. */
	public void fillData(ReadModelDataXL inputModel)
	{
		/** Feed bus data into model */
		BusesXL busRawData = inputModel.getBuses();
		int nbuses = busRawData.size();
 
		_buses.setBusNumMapIdx(busRawData.getMapMatrix());
		_buses.setSize(nbuses);
		for (int i=0; i<nbuses; i++) {
			_buses.setArea(i, busRawData.getArea(i));
			_buses.setBaseKV(i, busRawData.getBaseKV(i));
			_buses.setBusNumber(i, busRawData.getBusNum(i));
			_buses.setVa(i, busRawData.getVa(i));
//			_buses.setVm(i, busRawData.getVm(i));
		}
		_diary.hotLine("Bus data was filled in the SystemModel");
		
		/** Feed in generator data into model */
		GeneratorsXL gens = inputModel.getGenerators();
		int nGens =  gens.size();
		_gens.setSize(nGens);
		_gens.setSizeGenCost(gens.sizeGenCost());
		_gens.setSizeCostSegment(gens.sizeSegment());
		for(int i=0; i<nGens; i++) {
			_gens.setBusIdx(i, gens.getBusIdx(i));
			_gens.setPgInit(i, gens.getPgInit(i)/_MVAbase);
			_gens.setPgmax(i, gens.getPgmax(i)/_MVAbase);
			_gens.setPgmin(i, gens.getPgmin(i)/_MVAbase);
			if (gens.getStat(i) == 1) _gens.setSvcSt(i, true);
			else _gens.setSvcSt(i, false);
			
			_gens.setEnergyRamp(i, gens.getEnergyRamp(i)/_MVAbase);
			_gens.setSpinRamp(i, gens.getSpinRamp(i)/_MVAbase);
			_gens.setCostCurveFlag(i, gens.hasCostCurveData(i));
			
			int idxSegment = gens.getMapToCostCurve(i);
			_gens.setMapToCostCurve(i, idxSegment);
			if (idxSegment != -1) {
				double[] rawGenSegmentBreadth = gens.getSegmentBreadth(idxSegment);
				double[] rawGenSegmentPrice = gens.getSegmentPrice(idxSegment);
				int sizeGenSegment = rawGenSegmentBreadth.length;
				
				double[] genSegmentBreadth = new double[sizeGenSegment];
				double[] genSegmentPrice = new double[sizeGenSegment];
				for (int j=0; j<sizeGenSegment; j++) {
					genSegmentBreadth[j] = rawGenSegmentBreadth[j]/_MVAbase;
					genSegmentPrice[j] = rawGenSegmentPrice[j]*_MVAbase;
				}
				_gens.setSegmentBreadth(idxSegment, genSegmentBreadth);
				_gens.setSegmentPrice(idxSegment, genSegmentPrice);
			}
		}
		_diary.hotLine("Generator data was filled in the SystemModel");
		
//		/** Feed generator detailed data into model */
//		GensCost genInfo = inputModel.getGeninfo();
//		if (nGens != genInfo.size()) {System.err.println("Gens size does not match .."); System.exit(0);}
//		for(int i=0; i<nGens; i++) {
//			if (gens.getBusIdx(i) != genInfo.getBusIdx(i)) {System.err.println("Gen bus index inconsistency on Gen: " + i); System.exit(0);}
////			_gens.setPgmax(i, genInfo.getPgmax(i)/_MVAbase);
////			_gens.setPgmin(i, genInfo.getPgmin(i)/_MVAbase);
//			_gens.setRamp(i, genInfo.getRampDisp(i)/_MVAbase);
//			_gens.setRampSU(i, genInfo.getRampSU(i)/_MVAbase);
//			_gens.setRampSD(i, genInfo.getRampSD(i)/_MVAbase);
//			_gens.setMinup(i, genInfo.getMinUP(i));
//			_gens.setMindown(i, genInfo.getMinDW(i));
//			_gens.setCostSU(i, genInfo.getCostSU(i));
//			_gens.setCostNL(i, genInfo.getCostNL(i));
//			_gens.setCostOp(i, genInfo.getCostOp(i)*_MVAbase);
//		}
//		_diary.hotLine("Generator detailed data was filled in the SystemModel");
		
		/** Feed load data into model*/
		LoadsXL loads = inputModel.getLoads();
		int nloads = loads.size();
		_loads.setSize(nloads);
		for (int i=0; i<nloads; i++) {
			_loads.setBusIdx(i, loads.getBusIdx(i));
			_loads.setPload(i, loads.getPload(i)/_MVAbase);
			if (loads.getStat(i) == 1) _loads.setSvcSt(i, true);
			else _loads.setSvcSt(i, false);
		}
		_diary.hotLine("Load data was filled in the SystemModel");
		
		/** Feed branch data into model */
		BranchesXL branches = inputModel.getBrances();
		int nbrcs = branches.size();
		_branches.setSize(nbrcs);
		for (int i=0; i<nbrcs; i++) {
			_branches.setFrmBusIdx(i, branches.getFrmBusIdx(i));
			_branches.setToBusIdx(i, branches.getToBusIdx(i));
			_branches.setID(i, branches.getCKT(i));
			if (branches.getStat(i) == 1) _branches.setSvcSt(i, true);
			else _branches.setSvcSt(i, false);
			_branches.setR(i, branches.getR(i));
			_branches.setX(i, branches.getX(i));
			_branches.setRateA(i, branches.getRateA(i)/_MVAbase);
			_branches.setRateB(i, branches.getRateB(i)/_MVAbase);
			_branches.setRateC(i, branches.getRateC(i)/_MVAbase);
			_branches.setPkInit(i, branches.getPkInit(i)/_MVAbase);
			_branches.setAngle(i, branches.getAngle(i));
		}
		_diary.hotLine("Branch data was filled in the SystemModel");
		
//		/** Feed transformer data into model */
//		int sizeXfms = branches.sizeXfm();
//		_xfms.setSize(sizeXfms);
//		int[] idxXfms = branches.getIdxXfm();
//		for (int i=0; i<sizeXfms; i++) {
//			int idxXfm = idxXfms[i];
//			_xfms.setFrmBusIdx(i, branches.getFrmBusIdx(idxXfm));
//			_xfms.setToBusIdx(i, branches.getToBusIdx(idxXfm));
//			_xfms.setID(i, branches.getCKT(idxXfm));
//			if (branches.getStat(idxXfm) == 1) _xfms.setSvcSt(i, true);
//			else _xfms.setSvcSt(i, false);
//			_xfms.setR(i, branches.getR(idxXfm));
//			_xfms.setX(i, branches.getX(idxXfm));
//			_xfms.setRateA(i, branches.getRateA(idxXfm)/_MVAbase);
//			_xfms.setRateB(i, branches.getRateB(idxXfm)/_MVAbase);
//			_xfms.setRateC(i, branches.getRateC(idxXfm)/_MVAbase);
//			_xfms.setTap(i, branches.getTap(idxXfm));
//		}
//		_diary.hotLine("Transformer data was filled in the SystemModel");

//		/** Feed phase shifter data into model */
//		int sizePSs = branches.sizePhaseShifter();
//		_phaseshifters.setSize(sizePSs);
//		int[] idxPSs = branches.getIdxPhaseShifter();
//		for (int i=0; i<sizePSs; i++) {
//			int idxPS = idxPSs[i];
//			_phaseshifters.setFrmBusIdx(i, branches.getFrmBusIdx(idxPS));
//			_phaseshifters.setToBusIdx(i, branches.getToBusIdx(idxPS));
//			_phaseshifters.setID(i, branches.getCKT(idxPS));
//			if (branches.getStat(idxPS) == 1) _phaseshifters.setSvcSt(i, true);
//			else _phaseshifters.setSvcSt(i, false);
//			_phaseshifters.setR(i, branches.getR(idxPS));
//			_phaseshifters.setX(i, branches.getX(idxPS));
//			_phaseshifters.setRateA(i, branches.getRateA(idxPS)/_MVAbase);
//			_phaseshifters.setRateB(i, branches.getRateB(idxPS)/_MVAbase);
//			_phaseshifters.setRateC(i, branches.getRateC(idxPS)/_MVAbase);
//			_phaseshifters.setTap(i, branches.getTap(idxPS));
//			_phaseshifters.setAngle(i, branches.getAngle(idxPS));
//		}
//		_diary.hotLine("Phase shifter data was filled in the SystemModel");

		/** Feed interface data into model*/
		InterfacesXL interfaces = inputModel.getInterfaces();
		int nInterface = interfaces.size();
		_interfaces.setSize(nInterface);
		for (int i=0; i<nInterface; i++) {
			if (interfaces.getIsEnabled(i) == 1) _interfaces.setInterfaceActiveFlag(i, true);
			else _interfaces.setInterfaceActiveFlag(i, false);
			_interfaces.setInterfaceLimit(i, interfaces.getInterfaceLimit(i)/_MVAbase);
			_interfaces.setInterfaceLines(i, interfaces.getInterfaceLineIdx(i).clone());
			_interfaces.setInterfaceLinesDirection(i, interfaces.getInterfaceLineDirection(i).clone());
		}
		_diary.hotLine("Interface data was filled in the SystemModel");
		
		/** Feed contingency data into model*/
		BranchContingenciesXL brcCtgcyList = inputModel.getBranchContingencies();
		int nBrcCtgcy = brcCtgcyList.size();
		_brcCtgcies.setSize(nBrcCtgcy);
		for (int i=0; i<nBrcCtgcy; i++) {
			_brcCtgcies.setIsCtgcyActive(i, brcCtgcyList.isActive(i));
			//TODO: just a notice, that only the first contingency element is taken per contingency.
			_brcCtgcies.setCtgcyBrcIdx(i, brcCtgcyList.getCtgcyBrcIdx(i)[0]); 
		}
		_diary.hotLine("Contingency data was filled in the SystemModel");
		
		_diary.hotLine(LogTypeXL.MileStone, "SystemModel data input was completed");
		checkData();
//		_acbranches = new ACBranchList(this);
	}
	
	/** Check input model data and fix them if needed */
	public void checkData() {
		_buses.dataCheck();
		_gens.dataCheck();
		_loads.dataCheck();
		_branches.checkData();
		_monitorSet.dataCheck();
		_brcCtgcies.dataCheck();
		_interfaces.dataCheck();
	}
	
	/** Write all data to files */
	public void dump()
	{
		_buses.dump();
		_gens.dump();
		_gens.dumpCostCurve();
		_loads.dump();
		_branches.dump();
		_interfaces.dump();
		_brcCtgcies.dump();
		_monitorSet.dump();
	}
	
	
	boolean[] _isBrcFlowCalc4BaseCase;  // need to know/calculate this line flow for base case
	public boolean[] getIsBrcFlowCalc4BaseCase()
	{
		if (_isBrcFlowCalc4BaseCase == null) calcIsBrcFlowCalc4BaseCase();
		return _isBrcFlowCalc4BaseCase;
	}
	private void calcIsBrcFlowCalc4BaseCase()
	{
		int nBrc = getBranches().size();
		boolean[] isBrcMonitor = new boolean[nBrc];
		if (getSenstvtFactor().isUsePTDFforSCED() == false) Arrays.fill(isBrcMonitor, true); // B-theta model
		else if (getMonitorSet().getIsMonitorAllBrc() == true) Arrays.fill(isBrcMonitor, true);
		else {
			int[] monitorSet = getMonitorSet().getMonitorBrcSet();
			boolean[] isConstActive = getMonitorSet().getIsConstActive();
			for (int i=0; i<getMonitorSet().size(); i++) {
				if (isConstActive != null && isConstActive[i] == false) continue;
				int idxBrc = monitorSet[i];
				isBrcMonitor[idxBrc] = true;
			}
			
			InterfaceListXL interfaces = getInterfaceList();
			boolean[] isActive = interfaces.isInterfaceActive();
			int[][] interfaceLines = interfaces.getInterfaceLines();
			for (int i=0; i<interfaces.size(); i++) {
				if (isActive[i] == false) continue;
				for (int j=0; j<interfaceLines[i].length; j++)
					isBrcMonitor[interfaceLines[i][j]] = true;
			}
		}
		_isBrcFlowCalc4BaseCase = isBrcMonitor;
	}
	
	/** @param c: index of the contingency monitor set - BrcCtgcyListXL._monitorSetCtgcyCase */
	public boolean[] getIsBrcFlowCalc4CtgcyCase(int c)
	{
		int nBrc = getBranches().size();
		boolean[] isBrcMonitor = new boolean[nBrc];
		if (getSenstvtFactor().isUsePTDFforSCED() == false) Arrays.fill(isBrcMonitor, true);
		else if (getKeyBrcList().getIsMonitorAllBrc() == true) Arrays.fill(isBrcMonitor, true);
		else {
			int[] monitorSet = getKeyBrcList().getCtgcyCaseMonitorSet(c);
			boolean[] isConstActive = getKeyBrcList().getIsConstActive(c);
			int sizeMonitorSet = 0;
			if (monitorSet != null) sizeMonitorSet = monitorSet.length;
			for (int i=0; i<sizeMonitorSet; i++) {
				if (isConstActive != null && isConstActive[i] == false) continue;
				int idxBrc = monitorSet[i];
				isBrcMonitor[idxBrc] = true;
			}
			
			InterfaceListXL interfaces = getInterfaceList();
			int size = interfaces.size();
			if (size > 0 && getKeyBrcList().isCtgcyActive4Interface(c) == true) {
				boolean[] isActive = getKeyBrcList().getIsInterfaceActiveCtgcyCase(c);
				int[][] interfaceLines = interfaces.getInterfaceLines();
				for (int i=0; i<interfaces.size(); i++) {
					if (isActive[i] == false) continue;
					for (int j=0; j<interfaceLines[i].length; j++)
						isBrcMonitor[interfaceLines[i][j]] = true;
				}
			}
		}
		//isBrcMonitor[getKeyBrcList().getCtgcyBrcIdx(c)] = false; // do not need to calculate the contingency lines' flow
		return isBrcMonitor;
	}

	/** This is a static method */
	static public String getBrcFlowFlag(double percent)
	{
		String brcFlowSt = "Normal";
		if (Math.abs(percent - 1) < 0.0001) brcFlowSt = "Congested";
		else if (percent > 1) brcFlowSt = "Overloaded";
		return brcFlowSt;
	}


	
}
