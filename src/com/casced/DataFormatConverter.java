package com.casced;

import java.util.Arrays;

import com.powerdata.openpa.psse.ACBranch;
import com.powerdata.openpa.psse.ACBranchList;
import com.powerdata.openpa.psse.BusList;
import com.powerdata.openpa.psse.GenList;
import com.powerdata.openpa.psse.Island;
import com.powerdata.openpa.psse.LoadList;
import com.powerdata.openpa.psse.PsseModel;
import com.powerdata.openpa.psse.PsseModelException;
import com.rtca_cts.ausData.ACBranchRates;
import com.rtca_cts.ausData.PowerFlowResults;
import com.sced.model.BrcCtgcyListXL;
import com.sced.model.MonitorSetXL;
import com.sced.model.SystemModelXL;
import com.sced.model.data.BranchListXL;
import com.sced.model.data.BusGrpXL;
import com.sced.model.data.BusListXL;
import com.sced.model.data.GenListXL;
import com.sced.model.data.InterfaceListXL;
import com.sced.model.data.LoadListXL;
import com.sced.model.data.LoadListXL.LoadType;
import com.sced.model.param.ParamInput;
import com.utilxl.log.DiaryXL;
import com.utilxl.log.LogTypeXL;

/**
 * 
 * Initialized in January 2017.
 * 
 * @author Xingpeng Li (xplipower@gmail.com)
 *
 */
public class DataFormatConverter {

	PsseModel _model;
	boolean _useMainIsland;  // for parts that do not belong to the main island, ignored here.
	Island _mainIsland;
	
	int _optionLoss = ParamInput.getOptionLoss();

	public DataFormatConverter(PsseModel model) throws PsseModelException {_model = model; init();}

	public void init() throws PsseModelException 
	{
		if (_useMainIsland == true) {
			MainIsland misland = new MainIsland(_model);
			_mainIsland = _model.getIslands().get(misland.getIdxMainIsland());
		}
	}
	
	public void fillData(SystemModelXL scedModel) throws PsseModelException {			
		fillBusData(scedModel);
		fillGenData(scedModel);
		fillBranchData(scedModel);
		fillLoadData(scedModel);
	}
	
	private void fillBusData(SystemModelXL scedModel) throws PsseModelException {
		BusList busPAData = _model.getBuses();
		if (_useMainIsland == true) busPAData = _mainIsland.getBuses();
		int nbuses = busPAData.size();

		BusListXL buses = scedModel.getBuses();
		buses.setSize(nbuses);
		for (int i=0; i<nbuses; i++) {
			buses.setArea(i, busPAData.getAREA(i));
			buses.setBaseKV(i, busPAData.getBASKV(i));
			buses.setVa(i, busPAData.getVA(i)*Math.PI/180);
			buses.setVm(i, busPAData.getVMpu(i));
			buses.setBusNumber(i, busPAData.getRootIndex(i));
			if (i != busPAData.getRootIndex(i)) {
				System.exit(0);
			}
			buses.setBusID(i, busPAData.getObjectID(i));
			if (busPAData.getIDE(i) == 3) buses.setSlackBusIdx(i);
			//if (busPAData.getBusType(i) == BusTypeCode.Slack) buses.setSlackBusIdx(i);
		}
		buses.calcBusNumberMap();
		scedModel.getDiary().hotLine("Bus data was filled in the SystemModel from PAModel");
	}
	
	private void fillGenData(SystemModelXL scedModel) throws PsseModelException {
		double MVAbase = scedModel.getMVAbase();
		BusListXL buses = scedModel.getBuses();
		
		GenList gensPAData = _model.getGenerators();
		if (_useMainIsland == true) gensPAData = _mainIsland.getGenerators();
		int nGens =  gensPAData.size();
		
		GenListXL gens = scedModel.getGens();
		gens.setSize(nGens);
		for(int i=0; i<nGens; i++) {
			int genBusIdx = gensPAData.getBus(i).getIndex();
			gens.setBusIdx(i, buses.getIdx(genBusIdx));
			gens.setPgInit(i, gensPAData.getP(i)/MVAbase);
			gens.setPgmax(i, gensPAData.getPT(i)/MVAbase);
			gens.setPgmin(i, gensPAData.getPB(i)/MVAbase);
			if (gensPAData.isInSvc(i) == true) gens.setSvcSt(i, true);
			else gens.setSvcSt(i, false);
		}
		scedModel.getDiary().hotLine("Generator data was filled in the SystemModel from PAModel");	
	}
	
	private void fillLoadData(SystemModelXL scedModel) throws PsseModelException {
		DiaryXL diary = scedModel.getDiary();
		
		LoadList loadsPAData = _model.getLoads();
		if (_useMainIsland == true) loadsPAData = _mainIsland.getLoads();
		int nloads = loadsPAData.size();
		LoadListXL loads = scedModel.getLoads();
		loads.setSizeRealLoad(nloads);
		
		diary.hotLine("Option for modeling loss is: "+_optionLoss);
		if (_optionLoss == 0) {
			loads.setSize(nloads);
			fillRealLoadData(scedModel, loads, loadsPAData);
			diary.hotLine("Lossless model is used and, thus, loss is ignored");
		} else {
			diary.hotLine("Lossy model is used");
			BranchListXL branches = scedModel.getBranches();
			double[] losses = calcLoss(diary);
			
			if (_optionLoss == 1) {
				loads.setSize(nloads*2);
				fillRealLoadData(scedModel, loads, loadsPAData);
				
				double totalLoad = loads.getTotalPLoad();
				double pcnt = sum(losses)/totalLoad;
//				double totalPg = scedModel.getGens().getTotalPg();
//				double pcnt = totalPg/totalLoad - 1;
				for (int d=0; d<nloads; d++) {
					int idxLoad = d + nloads;
					loads.setBusIdx(idxLoad, loads.getBusIdx(d));
					if (loads.isInSvc(d) == true) {
						loads.setPloadInit(idxLoad, loads.getPLoad(d)*pcnt);
						loads.setSvcSt(idxLoad, true);
					} else loads.setSvcSt(idxLoad, false);
					loads.setLoadType(idxLoad, LoadType.Virtual);
				}
				diary.hotLine("The loss is added to all loads; a simple proportional-based method is applied");
				diary.hotLine("The proportional-based method is: PL,virtual = PL * ( totalPLoss/sum(PL) )");			
				diary.hotLine("It is equivalent to: PL,virtual = PL * ( (sum(Pg)/sum(PL)) - 1)");			
			} else if (_optionLoss == 2) {
				Object[] tmpObject = getLossDf4GenBuses(scedModel);
				int[] busIdx = (int[]) tmpObject[0];
				double[] pgPct = (double[]) tmpObject[1];
				int nVirload = pgPct.length;
				loads.setSize(nloads + nVirload);
				fillRealLoadData(scedModel, loads, loadsPAData);

				double totalLoss = sum(losses);
				for (int d=0; d<nVirload; d++)
				{
					int idxLoad = d + nloads;
					loads.setBusIdx(idxLoad, busIdx[d]);
					loads.setPloadInit(idxLoad, totalLoss*pgPct[d]);
					loads.setSvcSt(idxLoad, true);
					loads.setLoadType(idxLoad, LoadType.Virtual);
				}
				diary.hotLine("The loss is added to generator buses");
				diary.hotLine("The proportional-based method is: PL,virtual = totalPLoss * ( (PgMax-PgInit) / sum(PgMax-PgInit) )");			
			} else if (_optionLoss == 3 || _optionLoss == 4) {
				int nVirload = losses.length;
				loads.setSize(nloads + nVirload);
				fillRealLoadData(scedModel, loads, loadsPAData);

				PowerFlowResults pfresults = _model.getPowerFlowResults();
				for (int i=0; i<nVirload; i++) {
					int idxLoad = i + nloads;
					int loadBusIdx = -1;
					if (_optionLoss == 3) {
						loadBusIdx = branches.getToBusIdx(i);
						if (pfresults.getPto(i) > 0) loadBusIdx = branches.getFrmBusIdx(i);
					} else if (_optionLoss == 4) {
						loadBusIdx = branches.getFrmBusIdx(i);
						if (pfresults.getPfrom(i) < 0) loadBusIdx = branches.getToBusIdx(i);
					}
					loads.setBusIdx(idxLoad, loadBusIdx);
					loads.setPloadInit(idxLoad, losses[i]);
					loads.setSvcSt(idxLoad, true);
					loads.setLoadType(idxLoad, LoadType.Virtual);
				}
				if (_optionLoss == 3) diary.hotLine("The loss on each branch is entirely added to the actually receiving-bus as a fixed virtual load");
				if (_optionLoss == 4) diary.hotLine("The loss on each branch is entirely added to the actually sending-bus as a fixed virtual load");
			} else if (_optionLoss == 5) {
				int nVirload = losses.length;
				loads.setSize(nloads + 2*nVirload);
				fillRealLoadData(scedModel, loads, loadsPAData);

				for (int i=0; i<nVirload; i++) {
					int idxLoad = i*2 + nloads;
					
					int loadFrmBusIdx = branches.getFrmBusIdx(i);
					loads.setBusIdx(idxLoad, loadFrmBusIdx);
					loads.setPloadInit(idxLoad, losses[i]/2);
					loads.setSvcSt(idxLoad, true);
					loads.setLoadType(idxLoad, LoadType.Virtual);
					
					int loadToBusIdx = branches.getToBusIdx(i);
					loads.setBusIdx(idxLoad+1, loadToBusIdx);
					loads.setPloadInit(idxLoad+1, losses[i]/2);
					loads.setSvcSt(idxLoad+1, true);
					loads.setLoadType(idxLoad+1, LoadType.Virtual);
				}
				diary.hotLine("The loss on each branch is added to the frmBus (50%) and toBus (50%) evenly as fixed virtual loads");
			}
		}
		loads.setPloadPerPloadInit();
		diary.hotLine("Load data was filled in the SystemModel");
	}
	
	private void fillRealLoadData(SystemModelXL scedModel, LoadListXL loads, LoadList loadsPAData) throws PsseModelException
	{
		double MVAbase = scedModel.getMVAbase();
		BusListXL buses = scedModel.getBuses();
		int nloads = loadsPAData.size();
		for (int i=0; i<nloads; i++) {
			int loadBusNumber = loadsPAData.getBus(i).getIndex();
			loads.setBusIdx(i, buses.getIdx(loadBusNumber));
			loads.setPloadInit(i, loadsPAData.getP(i)/MVAbase);
			if (loadsPAData.isInSvc(i) == true) loads.setSvcSt(i, true);
			else loads.setSvcSt(i, false);
		}
		scedModel.getDiary().hotLine("Real load data was filled in the SystemModel");
	}
	
	private Object[] getLossDf4GenBuses(SystemModelXL scedModel)
	{
		BusGrpXL busGrp = scedModel.getBusGrp();
		GenListXL gens = scedModel.getGens();
		int[] busIdx = new int[gens.size()];
		double[] pgIncr = new double[gens.size()];
		
		int count = 0;
		for (int i=0; i<scedModel.getBuses().size(); i++)
		{
			int[] genIdx = busGrp.getGenIndex(i);
			if (genIdx == null) continue;
			double pgMaxSum = gens.getPgMaxSum(genIdx);
			double pgInitSum = gens.getPgInitSum(genIdx);
			
			busIdx[count] = i;
			pgIncr[count++] = pgMaxSum - pgInitSum;
		}
		busIdx = Arrays.copyOf(busIdx, count);
		pgIncr = Arrays.copyOf(pgIncr, count);
		
		double totalPgIncr = sum(pgIncr);
		for (int i=0; i<count; i++)
			pgIncr[i] /= totalPgIncr;
		return new Object[] {busIdx, pgIncr};
	}
		
	private void fillBranchData(SystemModelXL scedModel) throws PsseModelException {
		DiaryXL diary = scedModel.getDiary();
		float MVAbase = (float) scedModel.getMVAbase();
		BusListXL buses = scedModel.getBuses();
		
		/* Feed branch data into model */
		ACBranchList branchesPAData = _model.getBranches();
		int nbrcs = branchesPAData.size();

		ACBranchRates branchesRating = _model.getACBrcCapData();
		float[] rateA = branchesRating.getRateA();
		float[] rateB = branchesRating.getRateB();
		float[] rateC = branchesRating.getRateC();
		
		PowerFlowResults pfresults = _model.getPowerFlowResults();
		
		BranchListXL branches = scedModel.getBranches();
		branches.setSize(nbrcs);
		branches.initPkLimitA();
		branches.initPkLimitC();
		for (int i=0; i<nbrcs; i++) {
			ACBranch branch = branchesPAData.get(i);
			int frmBusNumber = branch.getFromBus().getIndex();
			branches.setFrmBusIdx(i, buses.getIdx(frmBusNumber));
			int toBusNumber = branch.getToBus().getIndex();
			branches.setToBusIdx(i, buses.getIdx(toBusNumber));
			branches.setID(i, branch.getObjectID());
			if (branch.isInSvc() == true) branches.setSvcSt(i, true);
			else branches.setSvcSt(i, false);
			
			branches.setR(i, branch.getR());
			branches.setX(i, branch.getX());
			branches.setRateA(i, rateA[i]/MVAbase);
			branches.setRateB(i, rateB[i]/MVAbase);
			branches.setRateC(i, rateC[i]/MVAbase);
			branches.setPkInit(i, pfresults.getMaxPkInit(i));
			branches.setAngle(i, branch.getPhaseShift());
			branches.setPkLimitA(i, pfresults.getPkLimit(i, rateA[i]/MVAbase));
			branches.setPkLimitC(i, pfresults.getPkLimit(i, rateC[i]/MVAbase));
		}
		diary.hotLine("Branch data was filled in the SystemModel");
	}	

	
	private double[] calcLoss(DiaryXL diary) throws PsseModelException
	{
		PowerFlowResults pfresults = _model.getPowerFlowResults();
		int nbrcs = _model.getBranches().size();
		double[] losses = new double[nbrcs];
		for (int i=0; i<nbrcs; i++) {
			losses[i] = pfresults.getPfrom(i) + pfresults.getPto(i);
			if (losses[i] < 0)
			{
				losses[i] = 0;
				diary.hotLine(LogTypeXL.Warning, "The loss on branch "+(i+1)+" was negative and changed to 0");
			}
		}
		_model.getDiary().hotLine("The loss for all branches was calculated");
		_model.getDiary().hotLine("Toal loss: "+sum(losses)+" p.u.");
		return losses;
	}
	
	private float sum(double[] losses) {
		float sum = 0;
		for (int i=0; i<losses.length; i++)
			sum += losses[i];
		return sum;
	}

	public void fillGenEcoData(SystemModelXL scedModel, GenEcoData genEcoData) throws PsseModelException {
		DiaryXL diary = scedModel.getDiary();
		double MVAbase = scedModel.getMVAbase();
		GenListXL gens = scedModel.getGens();

		gens.setSizeGenCost(genEcoData.sizeGenCost());
		gens.setSizeCostSegment(genEcoData.sizeSegment());

		int size = genEcoData.size();
		for (int i=0; i<size; i++) {
			gens.setEnergyRamp(i, genEcoData.getEnergyRamp(i)/MVAbase);
			gens.setSpinRamp(i, genEcoData.getSpinRamp(i)/MVAbase);
			gens.setCostCurveFlag(i, genEcoData.hasCostCurveData(i));
			
			int idxSegment = genEcoData.getMapToCostCurve(i);
			gens.setMapToCostCurve(i, idxSegment);
			if (idxSegment != -1) {
				double[] rawGenSegmentBreadth = genEcoData.getSegmentBreadth(idxSegment);
				double[] rawGenSegmentPrice = genEcoData.getSegmentPrice(idxSegment);
				int sizeGenSegment = rawGenSegmentBreadth.length;
				
				double[] genSegmentBreadth = new double[sizeGenSegment];
				double[] genSegmentPrice = new double[sizeGenSegment];
				for (int j=0; j<sizeGenSegment; j++) {
					genSegmentBreadth[j] = rawGenSegmentBreadth[j]/MVAbase;
					genSegmentPrice[j] = rawGenSegmentPrice[j]*MVAbase;
				}
				gens.setSegmentBreadth(idxSegment, genSegmentBreadth);
				gens.setSegmentPrice(idxSegment, genSegmentPrice);
			}
		}
		diary.hotLine("Generator cost curve data and ramping rate data was filled in the SystemModel");		
	}
	
	public void fillCtgcyData(SystemModelXL scedModel, Results4ReDispatch vioResult) throws PsseModelException {
		DiaryXL diary = scedModel.getDiary();
		double MVAbase = scedModel.getMVAbase();
		
		/* Base-case */ 
		MonitorSetXL monitorSet = scedModel.getMonitorSet();
		monitorSet.setMonitorBrcSet(null);
		//monitorSet.setMonitorBrcSet(vioResult.getPfIdxBrc());
		
		double[] pkInit = convFloat2Double(vioResult.getPfPk(), 1.0/MVAbase);
		double[] pkLimit = convFloat2Double(vioResult.getPfPkLimit(), 1.0/MVAbase);
		pkInit = null; pkLimit = null;
		monitorSet.setPkInit(pkInit);
		monitorSet.setPkLimit(pkLimit);
		monitorSet.setIsConstActive(getTrueBooleanArray(pkLimit));
		diary.hotLine("Base-case constraints data were filled in the SystemModel");
		
		/* Contingency-case */ 
		BrcCtgcyListXL brcCtgcies = scedModel.getKeyBrcList();
		int nCtgcy = vioResult.getCaCtgcySize();
		nCtgcy = 0;
		brcCtgcies.setSize(nCtgcy);
		
		int[] idxCtgcy = vioResult.getCaCriticalCtgcy();
		int[][] monitorBrc = vioResult.getCaIdxBrcCtgcy();
		brcCtgcies.setCtgcyBrcIdx(idxCtgcy);
		brcCtgcies.setCtgcyCaseMonitorSet(monitorBrc);
		for (int i=0; i<nCtgcy; i++)
			brcCtgcies.setIsCtgcyActive(i, true);
		
		double[][] pkcInit = convFloat2Double(vioResult.getCaPkc(), 1.0/MVAbase);
		double[][] pkcLimit = convFloat2Double(vioResult.getCaPkcLimit(), 1.0/MVAbase);
		pkcInit = null; pkcLimit = null;
		
		// tol4BrcFlowMonitor = 0.8 for both base-case and ctgcy-case
//		pkcLimit[11][1] = 13.58807;
//		pkcLimit[12][1] = 13.58807;
//		pkcLimit[11][1] = 13.54496;
//		pkcLimit[12][1] = 13.54496;
//		pkcLimit[11][1] = 13.32877;
//		pkcLimit[12][1] = 13.32877;
//		pkcLimit[11][1] = 13.3225;
//		pkcLimit[12][1] = 13.3225;
		
		// tol4BrcFlowMonitor = 0.5 for base-case, and = 0.9 ctgcy-case.
//		pkcLimit[5][0] = 13.58807;
//		pkcLimit[6][0] = 13.58807;
//		pkcLimit[5][0] = 13.54496;
//		pkcLimit[6][0] = 13.54496;
//		pkcLimit[5][0] = 13.32877;
//		pkcLimit[6][0] = 13.32877;
//		pkcLimit[5][0] = 13.3225;
//		pkcLimit[6][0] = 13.3225;
		
		// tol4BrcFlowMonitor = 1.0 for both base-case and ctgcy-case.
//		pkcLimit[0][0] = 13.58807;
//		pkcLimit[1][0] = 13.58807;
//		pkcLimit[0][0] = 13.54496;
//		pkcLimit[1][0] = 13.54496;
//		pkcLimit[0][0] = 13.32877;
//		pkcLimit[1][0] = 13.32877;
//		pkcLimit[0][0] = 13.3225;
//		pkcLimit[1][0] = 13.3225;
		
		
		// for Polish case - paper revision
		/* For Pseudo test */
//		pkcLimit[1520][1] = pkcLimit[1520][1] + 0.06;
//		pkcLimit[1520][2] = pkcLimit[1520][2] + 0.06;
//		pkcLimit[1520][3] = pkcLimit[1520][3] + 0.06;
//		pkcLimit[1520][4] = pkcLimit[1520][4] + 0.06;
//		pkcLimit[1807][1] = pkcLimit[1807][1] + 0.04;
		
		/* For CTS-1 */
//		pkcLimit[13][1] = pkcLimit[13][1] + 0.15;
//		pkcLimit[150][0] = pkcLimit[150][0] + 0.06;
//		pkcLimit[151][1] = pkcLimit[151][1] + 0.06;
//		pkcLimit[167][2] = pkcLimit[167][2] + 0.04;
//		pkcLimit[361][0] = pkcLimit[361][0] + 0.03;
//		pkcLimit[947][0] = pkcLimit[947][0] + 0.03;
//		pkcLimit[948][0] = pkcLimit[948][0] + 0.04;
//		
//		pkcLimit[1517][1] = pkcLimit[1517][1] + 0.11;
//		pkcLimit[1520][3] = pkcLimit[1520][3] + 0.14;
//		pkcLimit[1520][4] = pkcLimit[1520][4] + 0.13;
//		pkcLimit[1520][2] = pkcLimit[1520][2] + 0.07;
//		pkcLimit[1520][1] = pkcLimit[1520][1] + 0.08;
//		pkcLimit[1565][0] = pkcLimit[1565][0] + 0.06;
//		pkcLimit[1566][1] = pkcLimit[1566][1] + 0.06;
//		pkcLimit[1566][2] = pkcLimit[1566][2] + 0.01;
//		pkcLimit[1567][0] = pkcLimit[1567][0] + 0.06;
//		pkcLimit[1585][0] = pkcLimit[1585][0] + 0.04;
//		pkcLimit[1586][0] = pkcLimit[1586][0] + 0.03;
//		
//		pkcLimit[1607][0] = pkcLimit[1607][0] + 0.02;
//		pkcLimit[1640][0] = pkcLimit[1640][0] + 0.05;
//		pkcLimit[1650][3] = pkcLimit[1650][3] + 0.08;
//		pkcLimit[1650][4] = pkcLimit[1650][4] + 0.02;
//		pkcLimit[1678][0] = pkcLimit[1678][0] + 0.03;
//		pkcLimit[1705][0] = pkcLimit[1705][0] + 0.04;
//		pkcLimit[1729][1] = pkcLimit[1729][1] + 0.07;
//		pkcLimit[1729][2] = pkcLimit[1729][2] + 0.02;
//		pkcLimit[1807][1] = pkcLimit[1807][1] + 0.07;
//		pkcLimit[1807][2] = pkcLimit[1807][2] + 0.02;
//		pkcLimit[2179][2] = pkcLimit[2179][2] + 0.02;


		/* For CTS-3 */
//		pkcLimit[13][1] = pkcLimit[13][1] + 0.03;
//		pkcLimit[150][0] = pkcLimit[150][0] + 0.03;
//		pkcLimit[947][0] = pkcLimit[947][0] + 0.025;
//		pkcLimit[948][0] = pkcLimit[948][0] + 0.025;
//		
//		pkcLimit[1517][1] = pkcLimit[1517][1] + 0.045;
//		pkcLimit[1520][3] = pkcLimit[1520][3] + 0.045;
//		pkcLimit[1520][4] = pkcLimit[1520][4] + 0.045;
//		pkcLimit[1520][2] = pkcLimit[1520][2] + 0.04;
//		pkcLimit[1520][1] = pkcLimit[1520][1] + 0.04;
//		pkcLimit[1565][0] = pkcLimit[1565][0] + 0.025;
//		pkcLimit[1566][1] = pkcLimit[1566][1] + 0.025;
//		pkcLimit[1566][2] = pkcLimit[1566][2] + 0.005;
//		pkcLimit[1567][0] = pkcLimit[1567][0] + 0.025;
//		pkcLimit[1585][0] = pkcLimit[1585][0] + 0.02;
//		pkcLimit[1586][0] = pkcLimit[1586][0] + 0.02;
//		
//		pkcLimit[1607][0] = pkcLimit[1607][0] + 0.02;
//		pkcLimit[1640][0] = pkcLimit[1640][0] + 0.02;
//		pkcLimit[1650][3] = pkcLimit[1650][3] + 0.04;
//		pkcLimit[1650][4] = pkcLimit[1650][4] + 0.015;
//		pkcLimit[1678][0] = pkcLimit[1678][0] + 0.02;
//		pkcLimit[1705][0] = pkcLimit[1705][0] + 0.02;
//		pkcLimit[1729][1] = pkcLimit[1729][1] + 0.025;
//		pkcLimit[1729][2] = pkcLimit[1729][2] + 0.015;
//		pkcLimit[1807][1] = pkcLimit[1807][1] + 0.025;
//		pkcLimit[1807][2] = pkcLimit[1807][2] + 0.015;
//		pkcLimit[2179][2] = pkcLimit[2179][2] + 0.02;

		
		brcCtgcies.setPkcInit(pkcInit);
		brcCtgcies.setPkcLimit(pkcLimit);
		brcCtgcies.setIsConstActive(getTrueBooleanArray(pkcLimit));
		diary.hotLine("Contingency-case constraints data were filled in the SystemModel");
		
		/* Interface constraints */
		if (vioResult.getSizeInterface() == 0) return;
		brcCtgcies.setInterfaceCtgcyBrcIdx(vioResult.getInterfaceCtgcyList());
		brcCtgcies.setIsInterfaceActiveCtgcyCase(vioResult.getIsInterfaceMonCtgcyCase());
		brcCtgcies.setMonInterfaceLines(vioResult.getMonInterfaceLinesCtgcyCase());
		double[][] pkcInitInterfaceLines = convFloat2Double(vioResult.getPkcInitInterfaceLinesCtgcyCase(), 1.0/MVAbase);
		brcCtgcies.setPkcInitInterfaceLines(pkcInitInterfaceLines);
		
		InterfaceListXL interfaces = scedModel.getInterfaceList();
		interfaces.setSize(vioResult.getSizeInterface());
		interfaces.setInterfaceActiveFlag(vioResult.getIsInterfaceMonBaseCase());

		double[] limitBaseCase = convFloat2Double(vioResult.getLimitBaseCase(), 1.0/MVAbase);
		double[][] emgcyLimits = convFloat2Double(vioResult.getInterfaceEmgcyLimits(), 1.0/MVAbase);
		interfaces.setInterfaceLimit(limitBaseCase);
		interfaces.setInterfaceEmgcyLimits(emgcyLimits);
		interfaces.setInterfaceLines(vioResult.getInterfaceLines());
		interfaces.setInterfaceLinesDirection(vioResult.getInterfaceLinesDirection());
		diary.hotLine("Interface-based constraints data were filled in the SystemModel");
	}
	
	public void updateCtgcyData(SystemModelXL scedModel, Results4ReDispatch vioResult) throws PsseModelException
	{
		scedModel.clearMonitorSet();
		scedModel.clearKeyBrcList();
		scedModel.clearInterfaceList();
		fillCtgcyData(scedModel, vioResult);
	}
	
	private double[][] convFloat2Double(float[][] array, double factor) {
		if (array == null) return null;
		int size = array.length;
		double[][] newArray = new double[size][];
		for (int i=0; i<size; i++)
			newArray[i] = convFloat2Double(array[i], factor);
		return newArray;
	}

	private double[] convFloat2Double(float[] array, double factor) {
		if (array == null) return null;
		int size = array.length;
		double[] newArray = new double[size];
		for (int i=0; i<size; i++)
			newArray[i] = array[i]*factor;
		return newArray;
	}
	
	private boolean[] getTrueBooleanArray(double[] array) {
		int length = (array == null) ? 0 : array.length;
		return getTrueBooleanArray(length);
	}
	private boolean[] getTrueBooleanArray(int num) {
		boolean[] tmp = new boolean[num];
		Arrays.fill(tmp, true);
		return tmp;
	}
	private boolean[][] getTrueBooleanArray(double[][] array)
	{
		int rowCnt = 0;
		if (array != null) rowCnt = array.length;
		boolean[][] tmp = new boolean[rowCnt][];
		for (int i=0; i<rowCnt; i++)
			tmp[i] = getTrueBooleanArray(array[i].length);
		return tmp;
	}
	

	/** Reset Pg after SCED */
	public void resetPg(float[] newpg) throws PsseModelException
	{
		float baseMVA = _model.getSBASE();
		GenList gensPAData = _model.getGenerators();
		if (_useMainIsland == true) gensPAData = _mainIsland.getGenerators();
		int nGens =  gensPAData.size();
		for(int g=0; g<nGens; g++)
		{
			gensPAData.setPS(g, newpg[g]*baseMVA);
			gensPAData.setP(g, newpg[g]*baseMVA);
		}
		_model.getDiary().hotLine("Generators' Pg has been reset based on the result from SCED.");		
	}
	
	/** Reset Pg after SCED */
	public void resetPg(double[] newpg) throws PsseModelException
	{
		int ngen = newpg.length;
		float[] pg = new float[ngen];
		for (int g=0; g<ngen; g++)
			pg[g] = (float) newpg[g];
		resetPg(pg);
	}
	
	
	
	
}
