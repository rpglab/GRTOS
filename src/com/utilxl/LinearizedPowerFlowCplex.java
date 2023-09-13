package com.utilxl;


import java.io.IOException;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloNumVarType;
import ilog.cplex.IloCplex;

import com.powerdata.openpa.psse.ACBranch;
import com.powerdata.openpa.psse.ACBranchList;
import com.powerdata.openpa.psse.BusTypeCode;
import com.powerdata.openpa.psse.GenList;
import com.powerdata.openpa.psse.LoadList;
import com.powerdata.openpa.psse.PsseModel;
import com.powerdata.openpa.psse.PsseModelException;
//import com.powerdata.openpa.psse.contingencyanalysis.ElemGroupAtGenBuses;
import com.powerdata.openpa.psse.powerflow.FastDecoupledPowerFlow;
import com.powerdata.openpa.psse.powerflow.VoltageSource;
import com.powerdata.openpa.psse.util.ImpedanceFilter;
//import com.powerdata.openpa.psse.util.MinZMagFilter;
import com.rtca_cts.ausData.BusGroupElems;
import com.utilxl.array.AuxArrayXL;
import com.utilxl.iofiles.OutputArrays;

public class LinearizedPowerFlowCplex {

	PsseModel _model;
	ACBranchList _brcs;
	ImpedanceFilter _zfilt;
	BusGroupElems _busGroupElems;
	boolean _MarkVarLimit;
	boolean _convACPF;
	boolean _cplexSolved;
	boolean _outputACPF;
	
	float _coefDC_P;     // for DC power flow model, Pij = _coefDC_P*theta_ij/xij;
	float[] _coefLAC_P;  // for linearized AC power flow model, Pij = _coefLAC_P[0]*diff_Vm*gij - _coefLAC_P[1]*theta_k*bij.
	float[] _coefLAC_Q;  // Qij = bpi - _coefLAC_Q[0]*Vi*bpi - _coefLAC_Q[1]*(Vi-Vj)*bij - _coefLAC_Q[2]*theta_k*gij.
	
	/** active power convergence tolerance */
	float _ptol = 0.0001f;
	/** reactive power convergence tolerance */
	float _qtol = 0.0001f;
	
	double[] _ptolAll;
	double[] _qtolAll;
	
	int _nbus;
	float[] _busPGen;
	float[] _busQGen;
	float[] _busPLoad;
	float[] _busQLoad;

	int[] _slackBus;
	int[] _pvBus;
	int[] _pqBus;
	
	float[] _vaRef;
	float[] _vmRef;
	float[] _tap_ij; // transformer tap; it is 1 for transmission line.
	
	double[] _vm;
	double[] _va;
	float[] _vm_ACPF; // results from AC power flow
	float[] _va_ACPF;

	int _nbrc;
	boolean[] _inSvc;
	int[] _frmBusIdx;
	int[] _toBusIdx;
	
	float[] _bpi;  // for branch it is B/2, for transformer it is Bmag.

	double[] _pij;
	double[] _pji;
	double[] _qij;
	double[] _qji;
	double[] _theta_ij;
	
	float[] _pij_ACPF;    // power flow results from OpenPA tool, note the definition of power flow direction
	float[] _qij_ACPF;    // power flow results from OpenPA tool, note the definition of power flow direction
	float[] _qji_ACPF;    // power flow results from OpenPA tool, note the definition of power flow direction
	float[] _theta_ij_ACPF;

	public LinearizedPowerFlowCplex(PsseModel model) throws PsseModelException, IOException
	{
		initial(model);
	}
	
	
	void initial(PsseModel model) throws PsseModelException, IOException
	{
		_model = model;
		initBrc(model);
	}
	void initBrc(PsseModel model) throws PsseModelException
	{
		_zfilt = model.getXFilter();
		_brcs = model.getBranches();
		_nbrc = _brcs.size();
		
		_inSvc = new boolean[_nbrc];
		_frmBusIdx = new int[_nbrc];
		_toBusIdx = new int[_nbrc];
		_tap_ij = new float[_nbrc];
		for (int i=0; i<_nbrc; i++)
		{
			ACBranch brc = _brcs.get(i);
			_inSvc[i] = brc.isInSvc();
			_frmBusIdx[i] = brc.getFromBus().getIndex();
			_toBusIdx[i] = brc.getToBus().getIndex();
			_tap_ij[i] = brc.getFromTap()/brc.getToTap();
		}
	}
	public void launchACPF() throws PsseModelException, IOException
	{
		String svstart = "Flat";
		VoltageSource vstart = VoltageSource.fromConfig(svstart);
		FastDecoupledPowerFlow pf = new FastDecoupledPowerFlow(_model);
//		if (_MarkVarLimit)
//		{
//			ElemGroupAtGenBuses elemGroupGenBuses = new ElemGroupAtGenBuses(_model);
//			pf.setElemGroupAtGenBuses(elemGroupGenBuses);
//		}
		pf.setMarkUsingVgSched(true);
		pf.setRealTimeStartForGenBus(true);
		pf.setShowRunInfo(false);
		long tmpT = System.nanoTime();
		_MarkVarLimit = false;
		pf.runPowerFlow(vstart, _MarkVarLimit, 1);
		System.out.println("AC power flow time is: " + (System.nanoTime() - tmpT)/1e9);
		_convACPF = pf.isPfConv();
		if (_convACPF == true)
		{
			_ptol = pf.getPtol()*0.99f;
			_qtol = pf.getQtol()*0.99f;
			_ptol = 0.0000f;
			_qtol = 0.0000f;

			if (_outputACPF == true) pf.outputResults();
			initBusType(_model);
			_nbus = _model.getBuses().size();
			_busGroupElems = _model.getBusGroupElems();
			_busGroupElems.launch();
			initPgAndPd(pf);
			_vm_ACPF = pf.getVM();
			_va_ACPF = pf.getVA();
			_pij_ACPF = pf.getPfrom();
			_qij_ACPF = pf.getQfrom();
			_qji_ACPF = pf.getQto();
		}
	}
	
	void initBusType(PsseModel model) throws PsseModelException
	{
		_pqBus = model.getBusNdxForType(BusTypeCode.Load);
		_pvBus = model.getBusNdxForType(BusTypeCode.Gen);
		_slackBus = model.getBusNdxForType(BusTypeCode.Slack);
//		OutputArrays.outputArray(new int[][]{_slackBus}, false, "tmpSlackBusArrayFile.txt", false, false);
	}
	
	void initPgAndPd(FastDecoupledPowerFlow pf) throws PsseModelException
	{
		_busPGen = new float[_nbus];
		_busQGen = new float[_nbus];
		GenList gens = _model.getGenerators();
		for(int i=0; i<_nbus; i++)
		{
			int[] idxGens = _busGroupElems.getGenIndex(i);
			if (idxGens != null)
			{
				for(int j=0; j<idxGens.length; j++)
				{
					int idxGen = idxGens[j];
					if(gens.isInSvc(idxGen))
					{
						_busPGen[i] += gens.getPpu(idxGen);					
						_busQGen[i] += gens.getQpu(idxGen);									
					}
				}
			}
		}

		_busPLoad = new float[_nbus];
		_busQLoad = new float[_nbus];
		LoadList loads = _model.getLoads();
		for(int i=0; i<_nbus; i++)
		{
			int[] idxLoads = _busGroupElems.getLoadIndex(i);
			if (idxLoads != null)
			{
				for(int j=0; j<idxLoads.length; j++)
				{
					int idxLoad = idxLoads[j];
					if(loads.isInSvc(idxLoad))
					{
						_busPLoad[i] += loads.getPpu(idxLoad);					
	     				_busQLoad[i] += loads.getQpu(idxLoad);									
					}
				}
			}
		}

		float[] vm = pf.getVM();
		for(int i=0; i<_nbus; i++)
		{
			int[] idxShunts = _busGroupElems.getShuntIndex(i);
			if (idxShunts != null)
			{
				float busShuntG = 0f;
				float busShuntB = 0f;
				for(int j=0; j<idxShunts.length; j++)
				{
					int idxShunt = idxShunts[j];
					if(_model.getShunts().isInSvc(idxShunt))
					{
					    busShuntG += _model.getShunts().getG(idxShunt)/100;
						busShuntB += _model.getShunts().getB(idxShunt)/100;
					}
				}
				_busPLoad[i] -= busShuntG*vm[i]*vm[i];					
 				_busQLoad[i] -= busShuntB*vm[i]*vm[i];									
			}
			
			int[] idxSvcs = _busGroupElems.getSVCIndex(i);
			if (idxSvcs != null)
			{
				float busSvcB = 0f;
				for(int j=0; j<idxSvcs.length; j++)
				{
					int idxSvc = idxSvcs[j];
					if(_model.getSvcs().isInSvc(idxSvc))
					{
						busSvcB += _model.getSvcs().get(idxSvc).getBINIT()/100f;
					}
				}
 				_busQLoad[i] -= busSvcB*vm[i]*vm[i];									
			}
		}
	}
	
	void consSetSlackBus(IloCplex dcPf, IloNumVar[] va) throws IloException, PsseModelException
	{
		for (int i=0; i<_slackBus.length; i++)
		{
			int idxSlack = _slackBus[i];
			dcPf.addEq(va[idxSlack], _va_ACPF[idxSlack]);
		}
	}
	
	void consSetSlackBus(IloCplex cplex, IloNumVar[] va, IloNumVar[] vm) throws IloException, PsseModelException
	{
		for (int i=0; i<_slackBus.length; i++)
		{
			int idxSlack = _slackBus[i];
			cplex.addEq(va[idxSlack], _va_ACPF[idxSlack]);
			cplex.addEq(vm[idxSlack], _vm_ACPF[idxSlack]);
		}
	}
	
	void consSetsPVBus(IloCplex cplex, IloNumVar[] vm) throws IloException, PsseModelException
	{
		for (int i=0; i<_pvBus.length; i++) {
			int idxPV = _pvBus[i];
			cplex.addEq(vm[idxPV], _vm_ACPF[idxPV]);
		}
	}
	
	/**  Meaningless ?? */
	void consSetTolSum(IloCplex cplex, IloNumVar[] tol) throws IloException
	{
		IloLinearNumExpr expr = cplex.linearNumExpr();  // an extension to linearNumExpr.
		for(int i=0; i<_nbus; i++)
		{
			expr.addTerm(1, tol[i]);  // addTerm method is expecting a coefficient and a variable not an Expr.
		}
		float ptolSum = 10*_ptol;
		if (ptolSum > 0.1) ptolSum = 0.1f;
		cplex.addLe(expr, ptolSum);
		cplex.addLe(cplex.negative(expr), ptolSum);
	}
	
	void cplexParamSetting(IloCplex cplex) throws IloException
	{
//		dcPf.setParam(IloCplex.DoubleParam.EpGap, 1);
		cplex.setParam(IloCplex.IntParam.Threads, 1);
//		Why do you want to change the number of threads? Per default CPLEX uses as many threads as you have CPUs. 
//		Note that changing the number of threads with the above parameter implicitly switches to opportunistic multi-threading. 
//		If you want to keep deterministic multi-threading then you must additionally do
		cplex.setParam(IloCplex.IntParam.ParallelMode, 1);
	}
	
	void cplexFailInfo(IloCplex cplex) throws IloException
	{
		System.err.println("Cplex failed to solve the problem...");
		cplex.exportModel("lpex1.lp");
		System.err.println("lpex1.lp has been exported...");
	}
	
	void checkDeltaPbrc() throws PsseModelException
	{
		float[] deltaP = new float[_nbrc];
		for(int i=0; i<_nbrc; i++)
			deltaP[i] = (float) (_pij[i] - _coefDC_P*(_va[_frmBusIdx[i]] - _va[_toBusIdx[i]] - _brcs.getPhaseShift(i))/_zfilt.getX(i));
		System.out.println("Max of absDeltaPij: "+AuxArrayXL.getAbsMaxElem(deltaP));
		System.out.println("Sum of absDeltaPij: "+AuxArrayXL.getSumAbsElems(deltaP));
	}

	void checkDeltaPQbrc() throws PsseModelException
	{
		float[] deltPij = new float[_nbrc];
		float[] deltQij = new float[_nbrc];
		float[] deltQji = new float[_nbrc];
		for(int i=0; i<_nbrc; i++) {
			float Bpi = _model.getBranches().getBmag(i) + _model.getBranches().getFromBchg(i);
			float b = _zfilt.getY(i).im();
			float g = _zfilt.getY(i).re();
			
			deltPij[i] = (float) (_pij[i] - _coefLAC_P[0]*(_vm[_frmBusIdx[i]]/_tap_ij[i] - _vm[_toBusIdx[i]])*g +
					_coefLAC_P[1]*(_va[_frmBusIdx[i]] - _va[_toBusIdx[i]] - _brcs.getPhaseShift(i))*b);
			deltQij[i] = (float) (_qij[i] + Bpi*(_coefLAC_Q[0]*_vm[_frmBusIdx[i]]/_tap_ij[i] - 1) + 
					_coefLAC_Q[1] * (_vm[_frmBusIdx[i]]/_tap_ij[i] - _vm[_toBusIdx[i]])*b +
					_coefLAC_Q[2] * (_va[_frmBusIdx[i]] - _va[_toBusIdx[i]] - _brcs.getPhaseShift(i))*g);
			deltQji[i] = (float) (_qji[i] + Bpi*(_coefLAC_Q[0]*_vm[_toBusIdx[i]] - 1) - 
					_coefLAC_Q[1] * (_vm[_frmBusIdx[i]]/_tap_ij[i] - _vm[_toBusIdx[i]])*b -
					_coefLAC_Q[2] * (_va[_frmBusIdx[i]] - _va[_toBusIdx[i]] - _brcs.getPhaseShift(i))*g);
		}
		System.out.println("\nMax of absDeltaPij: "+AuxArrayXL.getAbsMaxElem(deltPij));
		System.out.println("Sum of absDeltaPij: "+AuxArrayXL.getSumAbsElems(deltPij));
		System.out.println("\nMax of absDeltaQij: "+AuxArrayXL.getAbsMaxElem(deltQij));
		System.out.println("Sum of absDeltaQij: "+AuxArrayXL.getSumAbsElems(deltQij));
		System.out.println("\nMax of absDeltaQji: "+AuxArrayXL.getAbsMaxElem(deltQji));
		System.out.println("Sum of absDeltaQji: "+AuxArrayXL.getSumAbsElems(deltQji));
	}

	void checkPi() throws PsseModelException
	{
		float[] deltaP = new float[_nbus];
		for (int i=0; i<_nbus; i++)
		{
			if (AuxArrayXL.isElemInArray(i, _slackBus) == true) continue;
			int[] idxBrcs = _busGroupElems.getBrcIndex(i);
			deltaP[i] = _busPGen[i]-_busPLoad[i];
			for(int j=0; j<idxBrcs.length; j++)
			{
				int idxBrc = idxBrcs[j];
				if (_frmBusIdx[idxBrc] == i) deltaP[i] += (float) -_pij[idxBrc];
				else deltaP[i] += (float) _pij[idxBrc];
			}
		}
		System.out.println("\nMax of absDeltaPi: "+AuxArrayXL.getAbsMaxElem(deltaP));
		System.out.println("Sum of DeltaPi: "+AuxArrayXL.getSumElems(deltaP) + "\n");
	}
	
	void checkQi() throws PsseModelException
	{
		float[] deltaQ = new float[_nbus];
		for (int i=0; i<_nbus; i++)
		{
			if (AuxArrayXL.isElemInArray(i, _slackBus) == true) continue;
			if (AuxArrayXL.isElemInArray(i, _pvBus) == true) continue;
			int[] idxBrcs = _busGroupElems.getBrcIndex(i);
			deltaQ[i] = _busQGen[i]-_busQLoad[i];
			for (int j=0; j<idxBrcs.length; j++)
			{
				int idxBrc = idxBrcs[j];
				if (_frmBusIdx[idxBrc] == i) deltaQ[i] -= (float) _qij[idxBrc];
				else deltaQ[i] -= (float) _qji[idxBrc];
			}
		}
		System.out.println("\nMax of absDeltaQi: "+AuxArrayXL.getAbsMaxElem(deltaQ));
		System.out.println("Sum of DeltaQi: "+AuxArrayXL.getSumElems(deltaQ)+"\n");
	}
	
	void consSetAngleDiff(IloCplex dcPf, IloNumVar[] va, IloNumVar[] thetaK) throws IloException, PsseModelException
	{
		for(int i=0; i<_nbrc; i++)
		{
			IloNumExpr expr = dcPf.sum(dcPf.prod(1, va[_frmBusIdx[i]]),
					dcPf.prod(-1, va[_toBusIdx[i]]),
					dcPf.negative(thetaK[i]));
			expr = dcPf.sum(expr, -_brcs.getPhaseShift(i));
			dcPf.addEq(expr, 0);
		}
	}
	
	void setNodePBalanceConstr(IloCplex dcPf, IloNumVar[] pij, IloNumVar[] ptol) throws IloException, PsseModelException
	{
		for (int i=0; i<_nbus; i++)
		{
			if (AuxArrayXL.isElemInArray(i, _slackBus) == true) continue;
			IloNumExpr expr = dcPf.prod(1, ptol[i]);
			int[] idxBrcs = _busGroupElems.getBrcIndex(i);
			for(int j=0; j<idxBrcs.length; j++)
			{
				int idxBrc = idxBrcs[j];
				if (_frmBusIdx[idxBrc] == i) expr = dcPf.sum(expr, pij[idxBrc]);
				else expr = dcPf.sum(expr, dcPf.negative(pij[idxBrc]));
			}
			dcPf.addEq(expr, _busPGen[i]-_busPLoad[i]);
		}
	}
	
	void setNodeQBalanceConstr(IloCplex dcPf, IloNumVar[] qij, IloNumVar[] qji, IloNumVar[] qtol) throws IloException, PsseModelException
	{
		for (int i=0; i<_nbus; i++)
		{
			if (AuxArrayXL.isElemInArray(i, _slackBus) == true) continue;
			if (AuxArrayXL.isElemInArray(i, _pvBus) == true) continue;
			IloNumExpr expr = dcPf.prod(1, qtol[i]);
			int[] idxBrcs = _busGroupElems.getBrcIndex(i);
			for(int j=0; j<idxBrcs.length; j++)
			{
				int idxBrc = idxBrcs[j];
				if (_frmBusIdx[idxBrc] == i) expr = dcPf.sum(expr, qij[idxBrc]);
				else expr = dcPf.sum(expr, qji[idxBrc]);
			}
			dcPf.addEq(expr, _busQGen[i]-_busQLoad[i]);
		}
	}
	boolean solveDCPF() throws IloException, PsseModelException
	{
		return solveDCPF(1.0f);
	}

	public boolean solveDCPF(float coef) throws IloException, PsseModelException
	{
		_coefDC_P = coef;
		IloCplex dcPf = new IloCplex();

		IloNumVar[] va = dcPf.numVarArray(_nbus, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY, IloNumVarType.Float);
		IloNumVar[] pij = dcPf.numVarArray(_nbrc, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY, IloNumVarType.Float);
		IloNumVar[] thetaK = dcPf.numVarArray(_nbrc, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY, IloNumVarType.Float);
		IloNumVar[] ptol = dcPf.numVarArray(_nbus, -_ptol, _ptol, IloNumVarType.Float);
//		IloNumVar[] ptol = dcPf.numVarArray(_nbus, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY, IloNumVarType.Float);
//		dcPf.addLe(ptol[i], _ptol);
//		dcPf.addLe(dcPf.negative(ptol[i]), _ptol);
		
//		dcPf.addMinimize();
		cplexParamSetting(dcPf);
		consSetTolSum(dcPf, ptol);
		consSetSlackBus(dcPf, va);
		consSetAngleDiff(dcPf, va, thetaK);
		
		// line flow
		for (int i=0; i<_nbrc; i++)
		{
			if (_inSvc[i] == true)
			{
				float x = _zfilt.getX(i);
				IloNumExpr expr = dcPf.sum(dcPf.prod(_coefDC_P*1/x, thetaK[i]), 
						dcPf.negative(pij[i]));
				dcPf.addEq(expr, 0);
			}
		}
		setNodePBalanceConstr(dcPf, pij, ptol);
		
		long tmpT = System.nanoTime();
		_cplexSolved = dcPf.solve();
		System.out.println("DC power flow time is: " + (System.nanoTime() - tmpT)/1e9);

		if (_cplexSolved == true)
		{
			dcPf.output().println("Solution status = " + dcPf.getStatus());
			dcPf.output().println("Solution value = " + dcPf.getObjValue());
			_va = dcPf.getValues(va);
			_pij = dcPf.getValues(pij);
			_theta_ij = dcPf.getValues(thetaK);
		}
		else {cplexFailInfo(dcPf);}
		dcPf.end();
		checkDeltaPbrc();
		checkPi();
		return _cplexSolved;
	}
	
	public boolean solveDCPFwithVm() throws IloException, PsseModelException
	{
		return solveDCPFwithVm(new float[]{1,1}, new float[]{2, 1, 1});
	}

	public boolean solveDCPFwithVm(float[] coefLAC_P, float[] coefLAC_Q) throws IloException, PsseModelException
	{
		_coefLAC_P = coefLAC_P;
		_coefLAC_Q = coefLAC_Q;
		
		IloCplex dcPf = new IloCplex();
		IloNumVar[] vm = dcPf.numVarArray(_nbus, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY, IloNumVarType.Float);
		IloNumVar[] va = dcPf.numVarArray(_nbus, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY, IloNumVarType.Float);
		IloNumVar[] thetaK = dcPf.numVarArray(_nbrc, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY, IloNumVarType.Float);
		IloNumVar[] pij = dcPf.numVarArray(_nbrc, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY, IloNumVarType.Float);
		IloNumVar[] qij = dcPf.numVarArray(_nbrc, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY, IloNumVarType.Float);
		IloNumVar[] qji = dcPf.numVarArray(_nbrc, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY, IloNumVarType.Float);
		IloNumVar[] ptol = dcPf.numVarArray(_nbus, -_ptol, _ptol, IloNumVarType.Float);
		IloNumVar[] qtol = dcPf.numVarArray(_nbus, -_qtol, _qtol, IloNumVarType.Float);
		
//		dcPf.addMinimize();
		cplexParamSetting(dcPf);
		consSetTolSum(dcPf, ptol);
		consSetTolSum(dcPf, qtol);
		consSetSlackBus(dcPf, va, vm);
		consSetsPVBus(dcPf, vm);
		consSetAngleDiff(dcPf, va, thetaK);
		
		// line flow
		for (int i=0; i<_nbrc; i++)
		{
			if (_inSvc[i] == true)
			{
				int idxFrmBus = _frmBusIdx[i];
				int idxToBus = _toBusIdx[i];
				
//				float x = _zfilt.getX(i);
				float b = _zfilt.getY(i).im();
				float g = _zfilt.getY(i).re();
				float Bpi = _model.getBranches().getBmag(i) + _model.getBranches().getFromBchg(i);
				
				/* Active Power Flow equation */
				{
					IloNumExpr expr = dcPf.sum( dcPf.prod(_coefLAC_P[0]*g/_tap_ij[i], vm[idxFrmBus]),
							dcPf.prod(-g*_coefLAC_P[0], vm[idxToBus]),
							dcPf.prod(-b*_coefLAC_P[1], thetaK[i]),
							dcPf.negative(pij[i]));
					dcPf.addEq(expr, 0);
				}
				
				/* Qij Reactive Power Flow equation */
				{
					IloNumExpr expr = dcPf.sum(dcPf.prod(-b*_coefLAC_Q[1]/_tap_ij[i], vm[idxFrmBus]), 
							dcPf.prod(b*_coefLAC_Q[1], vm[idxToBus]), 
							dcPf.prod(-g*_coefLAC_Q[2], thetaK[i]),
							dcPf.prod(-_coefLAC_Q[0]*Bpi/_tap_ij[i], vm[idxFrmBus]), 
							dcPf.negative(qij[i]));
					dcPf.addEq(expr, -Bpi);
				}
				
				/* Qji Reactive Power Flow equation */
				{
					IloNumExpr expr = dcPf.sum(dcPf.prod(b*_coefLAC_Q[1]/_tap_ij[i], vm[idxFrmBus]), 
							dcPf.prod(-b*_coefLAC_Q[1], vm[idxToBus]), 
							dcPf.prod(g*_coefLAC_Q[2], thetaK[i]),
							dcPf.prod(-Bpi*_coefLAC_Q[0], vm[idxToBus]), 
							dcPf.negative(qji[i]));
					dcPf.addEq(expr, -Bpi);
				}
			}
		}
		setNodePBalanceConstr(dcPf, pij, ptol);
		setNodeQBalanceConstr(dcPf, qij, qji, qtol);

//		for (int i=0; i<_nbus; i++)
//		{
//			if (AuxArrayXL.isElemInArray(i, _slackBus) == true) continue;
//			IloNumExpr expr = dcPf.prod(1, ptol[i]);
//			int[] idxBrcs = _busGroupElems.getBrcIndex(i);
//			for(int j=0; j<idxBrcs.length; j++)
//			{
//				int idxBrc = idxBrcs[j];
//				if (_frmBusIdx[idxBrc] == i) expr = dcPf.sum(expr, pij[idxBrc]);
//				else expr = dcPf.sum(expr, dcPf.negative(pij[idxBrc]));
//			}
//			dcPf.addEq(expr, _busPGen[i]-_busPLoad[i]);
//		}
		
		long tmpT = System.nanoTime();
		_cplexSolved = dcPf.solve();
		System.out.println("DC power flow time is: " + (System.nanoTime() - tmpT)/1e9);

		if (_cplexSolved == true)
		{
			dcPf.output().println("Solution status = " + dcPf.getStatus()); 
			dcPf.output().println("Solution value = " + dcPf.getObjValue());
			_va = dcPf.getValues(va); 
			_vm = dcPf.getValues(vm);
			_pij = dcPf.getValues(pij); 
			_qij = dcPf.getValues(qij);
			_qji = dcPf.getValues(qji);
			_theta_ij = dcPf.getValues(thetaK);
		}
		else {cplexFailInfo(dcPf);}
		dcPf.end();
		checkDeltaPQbrc();
		checkPi();
		checkQi();
		return _cplexSolved;
	}
	
	
	public float[] getVm() {return AuxArrayXL.doubleTofloat(_vm);}
	double[] getVmDouble() {return _vm;}
	public float[] getVa() {return AuxArrayXL.doubleTofloat(_va);}
	double[] getVaDouble() {return _va;}

	public float[] getVm_ACPF() {return _vm_ACPF;}
	public float[] getVa_ACPF() {return _va_ACPF;}

	public float[] getPfrm() {return AuxArrayXL.doubleTofloat(_pij);}
	double[] getPfrmDouble() {return _pij;}
	public float[] get_Theta_ij() {return AuxArrayXL.doubleTofloat(_theta_ij);}
	
	public float getPfrm_ACPF(int idx) {return -_pij_ACPF[idx];}
	public float getQfrm_ACPF(int idx) {return -_qij_ACPF[idx];}
	public float getQto_ACPF(int idx) {return -_qji_ACPF[idx];}
	public float[] get_Theta_ij_ACPF() {
		if (_theta_ij_ACPF == null) {
			try {
				return calcTheta_ij_ACPF();
			} catch (PsseModelException e) {e.printStackTrace();}
		}
		return _theta_ij_ACPF;
	}

	public float[] getQfrm() {return AuxArrayXL.doubleTofloat(_qij);}
	double[] getQfrmDouble() {return _qij;}
	public float[] getQto() {return AuxArrayXL.doubleTofloat(_qji);}
	double[] getQtoDouble() {return _qji;}
	
	private float[] calcTheta_ij_ACPF() throws PsseModelException {
		_theta_ij_ACPF = new float[_pij_ACPF.length];
		for (int i=0; i<_pij_ACPF.length; i++) {
			int frmBusIdx = _brcs.getFromBus(i).getIndex();
			int toBusIdx = _brcs.getToBus(i).getIndex();
			_theta_ij_ACPF[i] = _va_ACPF[frmBusIdx] - _va_ACPF[toBusIdx] - _brcs.getPhaseShift(i);
		}
		return _theta_ij_ACPF;
	}

	
	public boolean getACPFconverged() {return _convACPF;}
	public boolean getCplexSolved() {return _cplexSolved;}
	
	public void setOutputACPFresults(boolean mark) {_outputACPF = mark;}

	
	public static void main(String[] args) throws Exception
	{
		long t_Start = System.nanoTime();
		String uri = null;
		for(int i=0; i < args.length;)
		{
			String s = args[i++].toLowerCase();
			int ssx = 1;
			if (s.startsWith("--")) ++ssx;
			switch(s.substring(ssx))
			{
				case "uri":
					uri = args[i++];
					break;
			}
		}
		
		if (uri == null)
		{
			System.err.format("Usage: -uri model_uri [-voltage flat|realtime] [ -minxmag smallest reactance magnitude (default to 0.001) ] [ -debug dir ] [ -results path_to_results]");
			System.exit(1);
		}
		
        float minxmag = 0.00001f;
		PsseModel model = PsseModel.Open(uri);
		model.setMinXMag(minxmag);
		System.out.println("   Time after read case file: " + (System.nanoTime() - t_Start)/1e9f);
		LinearizedPowerFlowCplex pfCplex = new LinearizedPowerFlowCplex(model);
		System.out.println("   Time after initialize a LinearizedPowerFlowCplex instant: " + (System.nanoTime() - t_Start)/1e9f + "\n");
		pfCplex.setOutputACPFresults(false);
		pfCplex.launchACPF();
		
//		float coeff = 1.0475734f;
//		float coeff = 1.0f;
		
//		float[] _coefLAC_P = new float[] {1.0f, 1.0f};  // traditional linearized AC model
//		float[] _coefLAC_Q = new float[] {2.0f, 1.0f, 1.0f};
		float[] _coefLAC_P = new float[] {0.980105f, 1.120457f}; // for ZZZ
		float[] _coefLAC_Q = new float[] {1.731001f, 1.013958f, 1.021680f};
//		float[] _coefLAC_P = new float[] {1.0586115f, 1.0522556f}; // for YYY
//		float[] _coefLAC_Q = new float[] {1.710832f, 1.030120f, 1.084884f};
		
//		if (pfCplex.getACPFconverged() == true) pfCplex.solveDCPF(coeff);
		if (pfCplex.getACPFconverged() == true) pfCplex.solveDCPFwithVm(_coefLAC_P, _coefLAC_Q);
		else System.err.println("The AC power flow does not converge.");
		
		if (pfCplex.getCplexSolved() == true)
		{
			float[] vm = pfCplex.getVm();
			float[] va = pfCplex.getVa();
			float[] pfrm = pfCplex.getPfrm();
			float[] qfrm = pfCplex.getQfrm();
			float[] qto = pfCplex.getQto();
			float[] theta_ij = pfCplex.get_Theta_ij();
			OutputArrays.outputArray(new float[][]{pfrm, qfrm, qto, theta_ij}, false, "tmpLineArrayFile.txt", false, false);
			OutputArrays.outputArray(new float[][]{vm, va}, false, "tmpBusArrayFile.txt", false, false);
		}
		
		System.out.println("\nTotal simulation time is: " + (System.nanoTime() - t_Start)/1e9);
		System.out.println("Simulation is done here!");
	}
}
