package com.casced;

import java.util.Arrays;

import com.powerdata.openpa.psse.ACBranch;
import com.powerdata.openpa.psse.ACBranchList;
import com.powerdata.openpa.psse.PsseModel;
import com.powerdata.openpa.psse.PsseModelException;
import com.rtca_cts.ausData.PowerFlowResults;

/**
 * 
 * Initialized in March 2017.
 * 
 * @author Xingpeng Li (xplipower@gmail.com)
 *
 */
public class AnalyzePfVioResults {

	PsseModel _model;
	
	boolean _useMaxLineP = true; // if true _pk = sign(pfrm)*max(abs(pfrm), abs(pto)), if false, _pk = pfrm. 
	int _count;
	int[] _idxBrc;
	BrcFlowMonitorType[] _flowMonType;
	float[] _pk;       // in MW
	float[] _limit;    // in MW
	float[] _percentMVA;
	float[] _ratingMVA;  // MVA rating

	public AnalyzePfVioResults(PsseModel model)
	{
		_model = model;
	}

	/** for base case power flow simulation */
	void analyzeLineFlowBaseCase(float tolBrcFlowMonitor, float tolBrcFlowWarning, float tolBrcFlowVio) throws PsseModelException {
		analyzeLineFlow(-1, tolBrcFlowMonitor, tolBrcFlowWarning, tolBrcFlowVio, true);
	}
	/** for contingency case power flow simulation */
	void analyzeLineFlowCtgcyCase(int idxCtgcy, float tolBrcFlowMonitor, float tolBrcFlowWarning, float tolBrcFlowVio) throws PsseModelException {
		analyzeLineFlow(idxCtgcy, tolBrcFlowMonitor, tolBrcFlowWarning, tolBrcFlowVio, false);
	}

	/** Monitor line flow and save necessary results */
	void analyzeLineFlow(int idxCtgcy, float tolBrcFlowMonitor, float tolBrcFlowWarning, float tolBrcFlowVio, boolean useLTRating) throws PsseModelException {
		ACBranchList branches = _model.getBranches();
		int size = branches.size();
		int[] idxBrc = new int[size];
		float[] pk = new float[size];
		float[] limit = new float[size];
		float[] percent = new float[size];
		float[] ratingMVA = new float[size];
		BrcFlowMonitorType[] flowMonType = new BrcFlowMonitorType[size];
		
		float sbase = _model.getSBASE();
		float[] ratings;
		if (useLTRating == true) ratings = _model.getACBrcCapData().getRateA();
		else ratings = _model.getACBrcCapData().getRateC();
		PowerFlowResults pfResults = _model.getPowerFlowResults();
		
		_count = 0;
		for (int i=0; i<size; i++) {
			ACBranch branch = branches.get(i);
			if (branch.isInSvc() == false) continue;
			//TODO: temporary code
//			if (idxCtgcy != -1) {
//				int[] idxTmpNotMonitor = new int[] {491, 2733, 1487, 2689};
//				if (i == idxTmpNotMonitor[0]) continue;
//				if (i == idxTmpNotMonitor[1]) continue;
//				if (i == idxTmpNotMonitor[2]) continue;
//				if (i == idxTmpNotMonitor[3]) continue;
//			}
			
			float mva = sbase * Math.max(pfResults.getSfrom(i), pfResults.getSto(i));
			float rating = ratings[i];
			float rpct = mva / rating;
			if (rpct >= tolBrcFlowMonitor) {
				if (rpct >= tolBrcFlowWarning) {
					if (rpct >= tolBrcFlowVio) flowMonType[_count] = BrcFlowMonitorType.Violation;
					else flowMonType[_count] = BrcFlowMonitorType.Warning;
				} else flowMonType[_count] = BrcFlowMonitorType.Monitor;
				idxBrc[_count] = i;
				pk[_count] = sbase * getPkFrm(pfResults, i);
				percent[_count] = rpct;
				ratingMVA[_count] = rating;    // _model.getSBASE();
				limit[_count++] = sbase * pfResults.getPkLimit(i, rating/sbase);
			}
		}
		_idxBrc = Arrays.copyOf(idxBrc, _count);
		_flowMonType = Arrays.copyOf(flowMonType, _count);
		_pk = Arrays.copyOf(pk, _count);
		_limit = Arrays.copyOf(limit, _count);
		_percentMVA = Arrays.copyOf(percent, _count);
		_ratingMVA = Arrays.copyOf(ratingMVA, _count);
	}
	
	private float getPkFrm(PowerFlowResults pfResults, int k) {
		if (_useMaxLineP == false) return pfResults.getPfrom(k);
		else return pfResults.getMaxPkInit(k);
	}
	
	public int sizeVioBrc() {return _count;}
	public int[] getIdxBrc() {return _idxBrc;}
	public BrcFlowMonitorType[] getBrcFlowMonType() {return _flowMonType;}
	public float[] getPk() {return _pk;}
	public float[] getLimit() {return _limit;}
	public float[] getPercent() {return _percentMVA;}
	public float[] getRating() {return _ratingMVA;}
	
	
	
}
