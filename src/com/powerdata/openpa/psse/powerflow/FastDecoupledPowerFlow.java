package com.powerdata.openpa.psse.powerflow;

/**
 * 

This class/code is from OpenPA version 1; the associated copyright is provided below:

Copyright (c) 2016, PowerData Corpration, Incremental Systems Corporation All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following 
conditions are met:

Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.

Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following 
disclaimer in the documentation and/or other materials provided with the distribution.

Neither the name of cmtest1 nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, 
BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO 
EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR 
TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
POSSIBILITY OF SUCH DAMAGE.

 *
 */

import static org.junit.Assert.assertTrue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Stack;

import org.junit.Test;

import com.powerdata.openpa.psse.ACBranch;
import com.powerdata.openpa.psse.ACBranchList;
import com.powerdata.openpa.psse.Bus;
import com.powerdata.openpa.psse.BusList;
import com.powerdata.openpa.psse.BusTypeCode;
import com.powerdata.openpa.psse.Gen;
import com.powerdata.openpa.psse.GenList;
import com.powerdata.openpa.psse.Island;
import com.powerdata.openpa.psse.IslandList;
import com.powerdata.openpa.psse.Load;
import com.powerdata.openpa.psse.LoadList;
import com.powerdata.openpa.psse.PsseModel;
import com.powerdata.openpa.psse.PsseModelException;
import com.powerdata.openpa.psse.SVC;
import com.powerdata.openpa.psse.Shunt;
import com.powerdata.openpa.psse.ShuntList;
import com.powerdata.openpa.psse.SvcList;
import com.powerdata.openpa.psse.Transformer;
import com.powerdata.openpa.psse.TransformerCtrlMode;
import com.powerdata.openpa.psse.TransformerList;
import com.powerdata.openpa.psse.util.ImpedanceFilter;
import com.powerdata.openpa.tools.Complex;
import com.powerdata.openpa.tools.FactorizedBMatrix;
import com.powerdata.openpa.tools.LinkNet;
import com.powerdata.openpa.tools.PAMath;
import com.powerdata.openpa.tools.SparseBMatrix;
import com.rtca_cts.ausData.BusGroupElems;
import com.rtca_cts.contingencyanalysis.GenVarLimit;
import com.rtca_cts.contingencyanalysis.VioResult;
import com.rtca_cts.param.ParamFDPFlow;
import com.rtca_cts.param.ParamIO;
import com.rtca_cts.param.ParamVio;
import com.utilxl.iofiles.AuxFileXL;


/**
 * Utility to run a Fast-Decoupled AC Power Flow
 * 
 * @author chris@powerdata.com
 *
 */
public class FastDecoupledPowerFlow
{
	/** active power convergence tolerance */
	float _ptol = ParamFDPFlow.getPTol();
	/** reactive power convergence tolerance */
	float _qtol = ParamFDPFlow.getQTol();
	/** maximum iterations before giving up */
	int _itermax = ParamFDPFlow.getItermax();
	int _PfAlgorithmOption = ParamFDPFlow.getPfAlgorithmOption();
	PsseModel _model;
	FactorizedBMatrix _bp, _bpp;
	SparseBMatrix _prepbpp;
	int _nhotislands;
	int[] _hotislands;
	int _nislands;
	//int[] _islands;

	float[] _vm, _va;
	int[] _pq;
	int[] _pv;
	int[] _slack;
	int[] _pqOrig;
	int[] _pvOrig;
	int[] _slackOrig;
	boolean _adjusttaps = false;
	int[] _adjustablexfr = null;
	float _qtoltap = .05f;
	File _dbgdir;
	ImpedanceFilter _zfilt;
	
	int _nbus;    // number of buses.
	int _nbr;     // number of branches.
    int _ngen;     // number of generators.
    int _nLoad;     // number of loads.
	int _nsh;        // number of shunts.
	int _nsvc;       // number of svcs.
 	
	BusList _buses;
	ACBranchList _branches;
    GenList _gens;
	LoadList _loads;
	ShuntList _shunts;
	SvcList _svclist;

	PowerFlowConvergenceList _pflist;
	boolean _conv;
	VioResult _testA;
	VioResult _testC;  // using rateC for branch flow limit
	boolean _useRateCForBrcVio;
	
	public FastDecoupledPowerFlow(PsseModel model)
			throws PsseModelException, IOException
	{
		_model = model;
		setupHotIslands();
		_zfilt = _model.getXFilter();
		buildMatrices();
		initial();
	}

	void initial()
	{
//		_path = ParamIO.getOutPath();
		AuxFileXL.createFolder(ParamIO.getOutPath());
		_path = ParamIO.getACPFPath();
		AuxFileXL.createFolder(_path);
	}
	
	public void setDebugDir(File dbgdir) throws IOException, PsseModelException
	{
		_dbgdir = dbgdir;
		if (_dbgdir.exists())
		{
			Stack<File> subdir = new Stack<>();
			subdir.push(dbgdir);
			for(File df : subdir)
			{
				File[] dirlist = df.listFiles();

				for (File d : dirlist)
				{
					if (d.isDirectory())
						subdir.push(d);
					else
						d.delete();
				}
				df.delete();
			}
		}
		_dbgdir.mkdirs();
		dumpMatrices(_dbgdir);
	}
	
	/** Set active power convergence tolerance (p.u. on 100MVA base) */
	public void setPtol(float ptol) {_ptol = ptol;}
	/** Get active power convergence tolerance (p.u. on 100MVA base) */
	public float getPtol() {return _ptol;}
	/** Set reactive power convergence tolerance (p.u. on 100MVA base) */
	public void setQtol(float qtol) {_qtol = qtol;}
	/** Get reactive power convergence tolerance (p.u. on 100MVA base) */
	public float getQtol() {return _qtol;}
	/** Get current impedance filter */
	public ImpedanceFilter getImpedanceFilter() {return _zfilt;}
	
	/** Set maximum iterations before giving up */
	public void setMaxIter(int itermax) {_itermax = itermax;} 
	/** Get maximum iterations before giving up */
	public int getMaxIter() {return _itermax;}
	/** If set true, then Pf shows detail iteration info */
	public void setShowRunInfo(boolean mark) { _showRunInfo = mark;}

	HashMap<Integer,int[]> _GensAtBus;
//	ElemGroupAtGenBuses _elemGroupGenBuses;
//	BusGroupElems _busGroupElems = null;
	ArrayList<Integer> _OrigPVBusesDel = new ArrayList<Integer>();
	LinkNet _net;
	float[] _bselfbp;
	float[] _bbranchbp;
	float[] _bselfbpp;
	float[] _bbranchbpp;
	
	String _path;
	boolean _MarkUsingVgSched = true;    // if true, then, use generator scheduled set point to be the Vm of PV Buses.
	boolean _useToolIndexforOutput = false; // output all the bus/brc/... data based on the re-index by this tool, but plus 1.
	boolean _showRunInfo = true;       // output some detail iteration information if true.
	int _nRound;   // # of rounds to get the Pf satisfy the GenVarLimit; it would be 1 if GenVarLimit is ignored.
	boolean _isRealTimeStartForGenBus = false;   // if _MarkUsingVgSched == true, then, this option doesn't matter. 
	boolean _isLastSolvedStartForGenBus = false;   // if _MarkUsingVgSched == true, then, this option doesn't matter. 
	
	
	/** check GenVarLimit if the second param is true */
	public PowerFlowConvergenceList runPowerFlow(VoltageSource vsrc, boolean markGenVarLimit)
			throws PsseModelException, IOException
	{
		return runPowerFlow(vsrc, markGenVarLimit, 1, 10);
	}
	
	/** check GenVarLimit if the second param is true */
	public PowerFlowConvergenceList runPowerFlow(VoltageSource vsrc, boolean markGenVarLimit, int option)
			throws PsseModelException, IOException
	{
		return runPowerFlow(vsrc, markGenVarLimit, option, 10);
	}
	
	/** check GenVarLimit if the second param is true */
	public PowerFlowConvergenceList runPowerFlow(VoltageSource vsrc, boolean markGenVarLimit, int option, int maxIter) 
			throws PsseModelException, IOException
	{
		if (markGenVarLimit == false) return runPowerFlow(vsrc);
		else
		{
			PowerFlowConvergenceList capslist = runPowerFlow(vsrc);
            if (_conv)
            {
            	vsrc = VoltageSource.LastSolved;
        		GenVarLimit genVarL = new GenVarLimit(_model, option);
        		boolean reachMaxIter = false;
        		for (int k=0; k<maxIter; k++)
        		{
        			boolean var = genVarL.checkGenVarLimit(_vm, _qfrm, _qto);
        			if (var == false)
        			{
        				_slack = _model.getBusNdxForType(BusTypeCode.Slack);
        				_pv = _model.getBusNdxForType(BusTypeCode.Gen);
        				_pq = _model.getBusNdxForType(BusTypeCode.Load);
        				
        				_bp = null; _bpp = null;
        				_prepbpp = null;
        				setupPfBMaxtrix(_pv, _slack);        				
        				capslist = runPowerFlow(vsrc);
                        if (_conv == false) break;
                        if (k == (maxIter - 1)) reachMaxIter = true;
        			} else break;
        		}
        		if (reachMaxIter == true && _conv == true)
        		{
                    System.err.println("Gen Var Limit is still violated after " + maxIter + " rounds...");
                    _conv = false;
        		}
        		_OrigPVBusesDel = genVarL.getOrigPVBusesDel();
        		genVarL.refresh();
            }
            if (_showRunInfo == true)
            {
            	if(_conv == true) System.out.println("Power flow converges after "+_nRound+" rounds");
            	else System.out.println("Power flow does not converge after "+_nRound+" rounds");
            }
			return capslist;
		}
	}
	
	public ArrayList<Integer> getOrigPVBusesDel() {return _OrigPVBusesDel;}
	public int getNumOrigPVtoPQBuses() {return _OrigPVBusesDel.size();}
	public int getNumOrigPVBuses() {return _pvOrig.length;}
	public void setMarkUsingVgSched(boolean aa) {_MarkUsingVgSched = aa;}
	public void useToolIndexForOutput(boolean mark) {_useToolIndexforOutput = mark;}
	public void setRealTimeStartForGenBus(boolean mark) {_isRealTimeStartForGenBus = mark;}
	public void setLastSolvedStartForGenBus(boolean mark) {_isLastSolvedStartForGenBus = mark;}

	//TODO: to check, to be removed
	/** Switch back to the original bus type setting. */
//	public void refreshBusType() throws PsseModelException
//	{
//		_model.setBusNdxForTypeSlack(_slackOrig);
//		_model.setBusNdxForTypeGen(_pvOrig);
//		_model.setBusNdxForTypeLoad(_pqOrig);
//	}
	
	/** run the power flow */
	public PowerFlowConvergenceList runPowerFlow(VoltageSource vsrc)
			throws PsseModelException, IOException
	{
		_model.clearPowerFlowResults();
		boolean debug = _dbgdir != null;
		MismatchReport mmr = new MismatchReport(_model);
		PowerCalculator pcalc = new PowerCalculator(_model, _zfilt);
		if (debug)
		{
			pcalc.setDebugEnabled(mmr);
		}
		BusList buses = _model.getBuses();
		int nbus = buses.size();
		
		if (_adjusttaps) findAdjustableTransformers();

		float[] vm, va;

		switch(vsrc)
		{
			case RealTime:
				float[][] rtv = pcalc.getRTVoltages();
				va = rtv[0];
				vm = rtv[1];
				break;

			case LastSolved:
				va = _va;
				vm = _vm;
				break;
				
			case Flat:
			default:
				va = new float[nbus];
				vm = flatMag(_model.getBusNdxForType(BusTypeCode.Load));
				break;
		}

		// set Vm for PV buses;
		if (_MarkUsingVgSched == true)
		{
			// Use Vg schedule value
			for(Gen g : _model.getGenerators())
			{
				if (g.isInSvc())
				{
					Bus b = g.getBus();
					BusTypeCode btc = b.getBusType();
					if (btc == BusTypeCode.Gen || btc == BusTypeCode.Slack)
					{
						//TODO:  resolve multiple setpoints if found
						int bndx = b.getIndex();
						vm[bndx] = g.getVS();
					}
				}
			}
		} else if (_isRealTimeStartForGenBus == true) {
			float[] vmbus = pcalc.getRTVoltages()[1];
			for(Gen g : _model.getGenerators())
			{
				if (g.isInSvc())
				{
					Bus b = g.getBus();
					BusTypeCode btc = b.getBusType();
					if (btc == BusTypeCode.Gen || btc == BusTypeCode.Slack)
					{
						int bndx = b.getIndex();
						vm[bndx] = vmbus[bndx];
					}
				}
			}
		} else if (_isLastSolvedStartForGenBus == true) {
			for(Gen g : _model.getGenerators())
			{
				if (g.isInSvc())
				{
					Bus b = g.getBus();
					BusTypeCode btc = b.getBusType();
					if (btc == BusTypeCode.Gen || btc == BusTypeCode.Slack)
					{
						int bndx = b.getIndex();
						vm[bndx] = _vm[bndx];
					}
				}
			}
		}

		boolean nconv=true;
		float[][] mm = pcalc.calculateMismatches(va, vm);
		if (debug) mmr.report(_dbgdir, "000");
		
		PowerFlowConvergenceList prlist = new PowerFlowConvergenceList(
				_hotislands.length);

		for(int iiter=0; iiter < _itermax && nconv; ++iiter)
		{
			/* 
			 * put this section in its own block to allow the results of solve to get cleaned up sooner 
			 */
			{
				float[] pmm = mm[0];
				int[] ebus = _bp.getEliminatedBuses();
				for(int bx : ebus) pmm[bx] /= vm[bx];
				float[] dp = _bp.solve(pmm);
//				for(int i=0; i < nbus; ++i) va[i] += dp[i];
				for(int bx : ebus) va[bx] += dp[bx];
			}
			
			mm = pcalc.calculateMismatches(va, vm);
			if (debug) mmr.report(_dbgdir, String.format("%02d-va", iiter));
			if (_showRunInfo == true) System.out.format("Iteration %d angle: ", iiter);
			nconv = notConverged(mm[0], mm[1], _ptol, _qtol, prlist, iiter);

			if (nconv)
			{
				float[] qmm = mm[1];
				int[] ebus = _bpp.getEliminatedBuses();
				for(int bx : ebus) qmm[bx] /= vm[bx];
				float[] dq = _bpp.solve(qmm);

				for(int bx : ebus) vm[bx] += dq[bx];
				mm = pcalc.calculateMismatches(va, vm);
				if (debug) mmr.report(_dbgdir, String.format("%02d-vm", iiter));
				if (_showRunInfo == true) System.out.format("Iteration %d voltage: ", iiter);
				nconv = notConverged(mm[0], mm[1], _ptol, _qtol, prlist, iiter);
			}
		}
		_nRound++;
		_conv = (nconv == false);
		_vm = vm;
		_va = va;
		ACBranchList branches = _model.getBranches();
		int nbr = branches.size();
		float[][] results = pcalc.calcACBranchFlows(branches, _va, _vm, _zfilt);
//		_pfrm = results[0];
//		_qfrm = results[1];
//		_pto = results[2];
//		_qto = results[3];
		_pfrm = changeSign(results[0]);
		_qfrm = changeSign(results[1]);
		_pto = changeSign(results[2]);
		_qto = changeSign(results[3]);
		_sfrm = new float[nbr];
		_sto = new float[nbr];
		for(int i=0; i<nbr; i++)
		{
			_sfrm[i] = (float) Math.sqrt(Math.pow(_pfrm[i], 2) + Math.pow(_qfrm[i], 2));
			_sto[i] = (float) Math.sqrt(Math.pow(_pto[i], 2) + Math.pow(_qto[i], 2));
		}
		
		_model.getPowerFlowResults().setResults(this);
		_pflist = prlist;
		return prlist;
	}
	
	private float[] changeSign(float[] array)
	{
		int size = array.length;
		float[] newArray = new float[size];
		for (int i=0; i<size; i++)
			newArray[i] = -array[i];
		return newArray;
	}
	
	void findAdjustableTransformers() throws PsseModelException
	{
		TransformerList transformers = _model.getTransformers();
		int[] tndx = new int[transformers.size()];
		int nadj=0;
		for(Transformer t : transformers)
		{
			if (t.getRegStat() && t.getCtrlMode() == TransformerCtrlMode.Voltage)
			{
				tndx[nadj++] = t.getIndex();
			}
		}
		_adjustablexfr = Arrays.copyOf(tndx, nadj);
	}

	float[] flatMag(int[] qbus) throws PsseModelException
	{
		float[] vm = new float[_model.getBuses().size()];
		for(int b : qbus) vm[b] = 1f;
		return vm;
	}

	boolean notConverged(float[] pmm, float[] qmm, float ptol, float qtol,
			PowerFlowConvergenceList res, int iter) throws PsseModelException
	{
		boolean nc = false;
		IslandList islands = _model.getIslands();
		for (int i=0; i < res.size(); ++i)
		{
			PowerFlowConvergence pr = res.get(i);
			boolean tnc = notConverged(pmm, qmm, islands.get(_hotislands[i]), ptol, qtol, pr); 
			nc |= tnc;
			if (!tnc) pr.setIterationCount(iter+1);
		}
		return nc;
	}

	boolean notConverged(float[] pmm, float[] qmm, Island island, float ptol,
			float qtol, PowerFlowConvergence r) throws PsseModelException
	{
		int[] pq = island.getBusNdxsForType(BusTypeCode.Load);
		int[] pv = island.getBusNdxsForType(BusTypeCode.Gen);
		int worstp = findWorst(pmm, new int[][] { pq, pv });
		int worstq = findWorst(qmm, new int[][] {pq});
		boolean conv = false;
		if (worstq != -1)
		{
			if (worstp != -1)
			{
				if (_showRunInfo == true) 
				{
					BusList buses = _model.getBuses();
					Bus pworst = buses.get(worstp);
					Bus qworst = buses.get(worstq);
					System.out.format("pmm %s [%s] %f, qmm %s [%s] %f\n", 
							pworst.getObjectID(), pworst.getNAME(), pmm[worstp],
							qworst.getObjectID(), qworst.getNAME(), qmm[worstq]);
				}
				conv = (Math.abs(pmm[worstp]) < ptol) && (Math.abs(qmm[worstq]) < qtol);
				r.setWorstPbus(worstp);
				r.setWorstPmm(pmm[worstp]);
				r.setWorstQbus(worstq);
				r.setWorstQmm(qmm[worstq]);				
			}
			else
			{
				System.out.println("The program should not run to this line");
			}
		}
		else
		{
			if (worstp == -1)
			{
				conv = true;   // if program runs to this line, then, this island has only one bus.
				System.out.println("This island has only one bus");
			}
			else
			{
				conv = (Math.abs(pmm[worstp]) < ptol);
				r.setWorstPbus(worstp);
				r.setWorstPmm(pmm[worstp]);
				r.setWorstQbus(-1);
				r.setWorstQmm(-1);				
			}
		}

		r.setConverged(conv);
		return !conv;
	}

	int findWorst(float[] mm, int[][] lists)
	{
		float wval = 0f;
		int wb = -1;
		for(int i=0; wb == -1 && i < lists.length; ++i)
		{
			for (int b : lists[i])
			{
				wb = b;
				break;
			}
		}
		for (int[] list : lists)
		{
			for (int b : list)
			{
				float am = Math.abs(mm[b]);
				if (am > wval)
				{
					wval = am;
					wb = b;
				}
			}
		}
		return wb;
	}

	void setupHotIslands() throws PsseModelException
	{
		IslandList islands = _model.getIslands();
		int nhot = 0;
		int nIsland = islands.size();
		_nislands = nIsland;
		int[] hotisl = new int[nIsland];
		for(int i=0; i < islands.size(); ++i)
		{
			Island island = islands.get(i);
			if (island.isEnergized())
				hotisl[nhot++] = i; 
		}

		_hotislands = Arrays.copyOf(hotisl, nhot);
		_nhotislands = _hotislands.length;
	}
	
	public int getNumofHotIslands() {return _nhotislands;}
	public int getNumofIslands() {return _nislands;}
	public float[] getVA() {return _va;}
	public float[] getVM() {return _vm;}
	public void setVA(float[] va) {_va = va;}
	public void setVM(float[] vm) {_vm = vm;}

	void buildMatrices() throws PsseModelException, IOException
	{		
		LinkNet net = new LinkNet();
		ACBranchList branches = _model.getBranches();
		_branches = branches;
		int nbus = _model.getBuses().size(), nbranch = branches.size();
		net.ensureCapacity(nbus-1, nbranch);
		float[] bselfbp = new float[nbus];
		float[] bbranchbp = new float[nbranch];
		float[] bselfbpp = new float[nbus];
		float[] bbranchbpp = new float[nbranch];
		
	    _gens = _model.getGenerators();
	    _ngen = _gens.size();
	    _buses = _model.getBuses();
		_shunts = _model.getShunts();
		_nsh = _shunts.size();
		_loads = _model.getLoads();
		_nLoad = _loads.size();
		_svclist = _model.getSvcs();
		_nsvc = _svclist.size();

		for(Shunt shunt : _shunts)
		{
			if (shunt.isInSvc())
				bselfbpp[shunt.getBus().getIndex()] -= shunt.getBpu();
		}

		int nbr = branches.size();
		_nbr = nbr;
		_nbus = nbus;
		for(int i=0; i < nbr; ++i)
		{
			ACBranch br = branches.get(i);
			if (br.isInSvc())
			{
				int fbus = br.getFromBus().getIndex();
				int tbus = br.getToBus().getIndex();
				int brx = net.findBranch(fbus, tbus);
				if (brx == -1)
				{
					brx = net.addBranch(fbus, tbus);
				}
				Complex z = _zfilt.getZ(i);
				
				float bbp = 1 / z.im();

				bbranchbp[brx] -= bbp;
				bselfbp[fbus] += bbp;
				bselfbp[tbus] += bbp;
				float bbpp = -_zfilt.getY(i).im();
				if (_PfAlgorithmOption == 1)
				{
					bbranchbpp[brx] -= bbpp;
					bselfbpp[fbus] += (bbpp - br.getFromBchg() - br.getBmag());
					bselfbpp[tbus] += (bbpp - br.getToBchg() - br.getBmag());
				}
				else if (_PfAlgorithmOption == 2)
				{
					bbranchbpp[brx] -= bbpp/br.getFromTap()/br.getToTap();
					bselfbpp[fbus] += (bbpp/br.getFromTap()/br.getFromTap() - br.getFromBchg() - br.getBmag());
					bselfbpp[tbus] += (bbpp/br.getToTap()/br.getToTap() - br.getToBchg() - br.getBmag());
				}
			}
		}

		int[] pq = _model.getBusNdxForType(BusTypeCode.Load);
		int[] pv = _model.getBusNdxForType(BusTypeCode.Gen);
		int[] slack = _model.getBusNdxForType(BusTypeCode.Slack);
//		OutputArrays.outputArray(pv);
		_pqOrig = pq;
		_pvOrig = pv;
		_slackOrig = slack;
		
		_pq = pq;
		_pv = pv;
		_slack = slack;
		
		_net = net;
		_bselfbp = bselfbp;
		_bbranchbp = bbranchbp;
		_bselfbpp = bselfbpp;
		_bbranchbpp = bbranchbpp;

		setupPfBMaxtrix(pv, slack);
	}
	
	void setupPfBMaxtrix(int[] pv, int[] slack)
	{
		int[] bppbus = Arrays.copyOf(pv, pv.length+slack.length);
		System.arraycopy(slack, 0, bppbus, pv.length, slack.length);
		SparseBMatrix prepbp = new SparseBMatrix(_net, slack, _bbranchbp, _bselfbp);
		_prepbpp = new SparseBMatrix(_net, bppbus, _bbranchbpp, _bselfbpp);      				
		_bp = prepbp.factorize();
		_bpp = _prepbpp.factorize();
	}

	float[] _pfrm, _pto, _qfrm, _qto, _sfrm, _sto;	
	public float[] getPfrom() {return _pfrm;}
	public float[] getPto() {return _pto;}
	public float[] getQfrom() {return _qfrm;}
	public float[] getQto() {return _qto;}
	public float[] getSfrom() {return _sfrm;}
	public float[] getSto() {return _sto;}
	public int getNumBrc() {return _nbr;}
	public int getNumBuses() {return _nbus;}
	public int[] getSlackBuses() {return _slack;}
	public int[] getPvBuses() {return _pv;}
	public int[] getPqBuses() {return _pq;}
	public boolean isPfConv() {return _conv;}
	
	public void updateModelVoltage() throws PsseModelException {
		_model.getBuses().setVMpu(_vm);
		_model.getBuses().setVArad(_va);
	}
	
	public float[] getRateA() throws PsseModelException
	{
		return _model.getACBrcCapData().getRateA();
	}
	
	public float[] getRateB() throws PsseModelException
	{
		return _model.getACBrcCapData().getRateB();
	}
	
	public float[] getRateC() throws PsseModelException
	{
		return _model.getACBrcCapData().getRateC();
	}
	
	public VioResult analyzeVioInfo(float[] rates) throws PsseModelException
	{
		VioResult test = new VioResult(_model, _nbr, true);
        if (ParamVio.getIsAllBusMnt() == false) test.setIsBusMnt(_model.getElemMnt().getIsBusMnt());
        if (ParamVio.getIsAllBrcMnt() == false) test.setIsBrcMnt(_model.getElemMnt().getIsBrcMnt());
        test.setVmMax(ParamVio.getVmMax()); test.setVmMin(ParamVio.getVmMin());
        test.launch(_vm, _va, 
        		_pfrm, _pto, _qfrm, _qto, _sfrm, _sto, rates);
        return test;
	}
	
	public void clearVioRateA() {_testA = null;}
	public void clearVioRateC() {_testC = null;}
	
	public VioResult getVioRateA() throws PsseModelException 
	{
		if (_testA == null) _testA = analyzeVioInfo(getRateA());
		return _testA;
	}

	public VioResult getVioRateC() throws PsseModelException 
	{
		if (_testC == null) _testC = analyzeVioInfo(getRateC());
		return _testC;
	}

	public void outputDelPVtoScreen() throws PsseModelException, IOException
	{
		System.out.println();
	    System.out.println("number of slack buses: "+_slack.length);
	    for (int mm=0; mm<_slack.length; mm++)
	    {
	    	int aa = _model.getBuses().get(_slack[mm]).getI();
	    	System.out.println("slack buses: "+aa);
	    }
		if (_OrigPVBusesDel != null)
		{
		    System.out.println("number of OrigPVBusesDel buses: "+_OrigPVBusesDel.size());
			for (int mm=0; mm<_OrigPVBusesDel.size(); mm++)
			{
		    	int aa = _model.getBuses().get(_OrigPVBusesDel.get(mm)).getI();
		    	System.out.println("OrigPVBusesDel buses: "+aa);
			}
		}
	    System.out.println("number of pv buses when the power flow converges: "+_pv.length);
	    for (int mm=0; mm<_pv.length; mm++)
	    {
	    	int aa = _model.getBuses().get(_pv[mm]).getI();
	    	System.out.println("PV buses: "+aa);
	    }
	}
	
	public String getPath() { return _path;}
	public void setPath(String path) { AuxFileXL.createFolder(path); _path = path;}
	
	public void setUseRateCForBrcVio(boolean mark) { _useRateCForBrcVio = mark;}
	
	public void outputResults() throws PsseModelException, IOException
	{
		outputVioInfo();
		outputPgQg();
		outputPgQgAll();
		outputShunt();
		outputBusShunt();
		outputSVC();
		outputLoad();
		outputBusLoads();
		outputVmVa();
		outputPowerFlow();
	}
	
	// output violation Info for power flow
	public void outputVioInfo() throws PsseModelException, IOException
	{
		assertTrue(_conv == true);
		String rootDr=getPath();
		VioResult testVio = null;
		if (_useRateCForBrcVio == true)
		{
			if (_testC == null) _testC = analyzeVioInfo(getRateC());
			testVio = _testC;
		}
		else
		{
			if (_testA == null) {_testA = analyzeVioInfo(getRateA());}
			testVio = _testA;
		}
			
       if (testVio.getViol())
       {
    	   if (testVio.getVioVoltage())
    	   {
    			try
    			{		    
          			File VmVio = new File(rootDr+"Vm_Violations.txt");
           			if (VmVio.exists() && !VmVio.isDirectory())
           			{
           				System.out.println(); 
           				if (VmVio.delete()) System.out.println("Original "+VmVio.getName() + " is deleted.");
           			    else System.out.println("Delete operation is failed.");
           			}
           		    OutputStream resultFile = new FileOutputStream(rootDr+"Vm_Violations.txt", true);
           		    PrintStream outFile = new PrintStream (resultFile);
           		    outFile.println(" No., BusIndex, Vm, Vm_Violations, busVmLevel");
           		    int sizeVmVio = testVio.sizeV();
           		    int[] getIdxV = testVio.getIdxV();
           		    float[] Vm = testVio.getVm();
           		    float[] VmVioDiff = testVio.getVmDiff();
           		    for (int nv=0; nv<sizeVmVio; nv++)
           		    {
           		    	int idxBus = getIdxV[nv];
           		    	int idx = nv + 1;
        				outFile.print(" "+idx);
           		    	if(_useToolIndexforOutput == true) outFile.print(" "+(idxBus+1));
           		    	else outFile.print(" "+_buses.getI(idxBus));
        				outFile.print(" "+Vm[nv]);
        				outFile.print(" "+VmVioDiff[nv]);
        				outFile.print(" "+_buses.getBASKV(idxBus));
        				outFile.println();	           		    		
           		    }
              		outFile.close();
               		resultFile.close();       		
         		    System.out.println("Output Vm Violation Data  successfully");
    			}
    			catch (FileNotFoundException e) {
    		    	System.out.println();
    		    	System.out.println("Cannot write Vm Violation info to file" + e);
    		    	e.printStackTrace();
    			}
    	   }
    	   if (testVio.getVioBrc())
    	   {
    			try
    			{
          			File BrcVio = new File(rootDr+"Brc_Violations.txt");
           			if (BrcVio.exists() && !BrcVio.isDirectory())
           			{
           				System.out.println(); 
           				if (BrcVio.delete()) System.out.println("Original "+BrcVio.getName() + " is deleted.");
           			    else System.out.println("Delete operation is failed.");
           			}
           		    OutputStream resultFile = new FileOutputStream(rootDr+"Brc_Violations.txt", true);
           		    PrintStream outFile = new PrintStream (resultFile);
           		    outFile.println(" No., BrcIndex, frmBus, toBus, Sfrm, Sto, _RateUsed, S_Vio, frmBusVmLevel, toBusVmLevel");
           		    int sizeBrcVio = testVio.sizeBrc();
           		    int[] getIdxBrc = testVio.getIdxBrc();
           		    float[] sfrm = testVio.getSfrm();
           		    float[] sto = testVio.getSto();
           		    float[] rateUsed = testVio.getRateUsed();
           		    float[] brcDiff = testVio.getBrcDiff();
           		    for (int nv=0; nv<sizeBrcVio; nv++)
           		    {
           		    	int ndxBrc = getIdxBrc[nv];
           		    	ACBranch br = _branches.get(ndxBrc);
           		    	assertTrue(br.isInSvc()==true);
    					int fbus = br.getFromBus().getIndex();
    					int tbus = br.getToBus().getIndex();
           		    	int idx = nv + 1;
        				outFile.print(" "+idx);
        				outFile.print(" "+(ndxBrc+1));
        				if(_useToolIndexforOutput == true) 
        				{
               		    	outFile.print(" "+(fbus+1));
               		    	outFile.print(" "+(tbus+1));
        				}
        				else
        				{
        					outFile.print(" "+_buses.getI(fbus));        					
        					outFile.print(" "+_buses.getI(tbus));        					
        				}
        				outFile.print(" "+sfrm[nv]);
        				outFile.print(" "+sto[nv]);
        				outFile.print(" "+rateUsed[ndxBrc]);
        				outFile.print(" "+brcDiff[nv]);
        				outFile.print(" "+_buses.getBASKV(fbus));
        				outFile.print(" "+_buses.getBASKV(tbus));
        				outFile.println();		           		    				           		    	
           		    }
              		outFile.close();
               		resultFile.close();       		
         		    System.out.println("Output Brc Violation Data  successfully");
    			}
    			catch (FileNotFoundException e) {
    		    	System.out.println();
    		    	System.out.println("Cannot write Brc Violation info to file" + e);
    		    	e.printStackTrace();
    			}
    	   }
       }

	}
	
	public void outputPowerFlow() throws PsseModelException
	{
		assertTrue(_conv == true);
		String rootDr=getPath();
		try
		{
			File genH = new File(rootDr+"PowerFlowData.txt");
			if (genH.exists() && !genH.isDirectory())
			{
				System.out.println(); 
				if (genH.delete()) System.out.println("Original "+genH.getName() + " is deleted.");
			    else System.out.println("Delete operation is failed.");
			}
		    OutputStream resultFile = new FileOutputStream(rootDr+"PowerFlowData.txt", true);
		    PrintStream outFile = new PrintStream (resultFile);
		    outFile.println(" LineIndex, fbus, tbus, pfrm, pto, qfrm, qto, rateA, rateB, rateC, r, x, b_frm, b_to, tapI, tapJ, phaseShiftInRad,");
		    float[] rateA = getRateA();
		    float[] rateB = getRateB();
		    float[] rateC = getRateC();
			for(int i=0; i<_nbr; i++)
			{
				ACBranch br = _branches.get(i);
				if (br.isInSvc())
				{
					int fbus = br.getFromBus().getIndex();
					int tbus = br.getToBus().getIndex();
					outFile.print(" "+(i+1));
    				if(_useToolIndexforOutput == true) 
    				{
           		    	fbus++; outFile.print(" "+fbus);
           		    	tbus++; outFile.print(" "+tbus);
    				}
    				else
    				{
    					outFile.print(" "+_buses.getI(fbus));        					
    					outFile.print(" "+_buses.getI(tbus));        					
    				}
					outFile.print(" "+ _pfrm[i]*100);
					outFile.print(" "+ _pto[i]*100);
					outFile.print(" "+ _qfrm[i]*100);
					outFile.print(" "+ _qto[i]*100);
					outFile.print(" "+rateA[i]);
					outFile.print(" "+rateB[i]);
					outFile.print(" "+rateC[i]);
					
					outFile.print(" "+br.getR());
					outFile.print(" "+br.getX());
					outFile.print(" "+(br.getBmag()+br.getFromBchg()));
					outFile.print(" "+(br.getBmag()+br.getToBchg()));
					outFile.print(" "+br.getFromTap());
					outFile.print(" "+br.getToTap());
					outFile.print(" "+br.getPhaseShift());
					outFile.println();
				}
			}
		    outFile.close();
		    System.out.println("Output PowerFlow Data  successfully");
	    } catch (FileNotFoundException e) {
	    	System.out.println();
	    	System.out.println("Cannot write PowerFlow output file" + e);
	    	e.printStackTrace();
	    }
	}
		
	public void outputVmVa() throws PsseModelException
	{
		assertTrue(_conv == true);
		String rootDr=getPath();
		try
		{
			File genH = new File(rootDr+"VmVa.txt");
			if (genH.exists() && !genH.isDirectory())
			{
				System.out.println(); 
				if (genH.delete()) System.out.println("Original "+genH.getName() + " is deleted.");
			    else System.out.println("Delete operation is failed.");
			}
		    OutputStream resultFile = new FileOutputStream(rootDr+"VmVa.txt", true);
		    PrintStream outFile = new PrintStream (resultFile);
		    outFile.println(" BusIndex Va Vm busType");
		    float[] Va = getVA();
		    float[] Vm = getVM();
			for(int i=0; i<_nbus; i++)
			{
				Bus bus2 = _buses.get(i);
				int idx = bus2.getIndex();
				assert(idx == i);
				if(_useToolIndexforOutput == true) {idx++; outFile.print(" "+idx);}
				else outFile.print(" "+_buses.getI(idx));
				outFile.print(" "+Va[i]);
				outFile.print(" "+Vm[i]);
				int bustype = 1;
				if (bus2.getBusType() == BusTypeCode.Gen) bustype = 2;
				else if (bus2.getBusType() == BusTypeCode.Slack) bustype = 3;
				outFile.print(" "+bustype);
				outFile.println();
			}
		    outFile.close();
		    System.out.println("Output Bus Voltage Data  successfully");
	    } catch (FileNotFoundException e) {
	    	System.out.println();
	    	System.out.println("Cannot write Voltage output file" + e);
	    	e.printStackTrace();
	    }
	}
	
	public void outputPgQg() throws PsseModelException
	{
		assertTrue(_conv == true);
		BusGroupElems busGroupElems = _model.getBusGroupElems();
		String rootDr=getPath();
		try
		{
			File genH = new File(rootDr+"PgQg.txt");
			if (genH.exists() && !genH.isDirectory())
			{
				System.out.println(); 
				if (genH.delete()) System.out.println("Original "+genH.getName() + " is deleted.");
			    else System.out.println("Delete operation is failed.");
			}
		    OutputStream resultFile = new FileOutputStream(rootDr+"PgQg.txt", true);
		    PrintStream outFile = new PrintStream (resultFile);
		    outFile.println(" GenIndex GenBusIndex PgMW QgMVA");
			for(int i=0; i<_ngen; i++)
			{
				Gen gen = _gens.get(i);
				if (gen.isInSvc())
				{
					Bus bus = gen.getBus();
					BusTypeCode busType = bus.getBusType();
					boolean markSlackBus = (busType == BusTypeCode.Slack);
					boolean markPVBus = (busType == BusTypeCode.Gen);
					float pMWInit = gen.getPpu();
					float qMVAInit = gen.getQpu();
					int idx = bus.getIndex();

					if (markSlackBus == true || markPVBus == true)
					{
						float Ppu = 0;
						float Qpu = 0;
						
						for(int k=0; k < _nbr; ++k)
						{
							boolean inSvc = _branches.isInSvc(k);
							if (inSvc == true)
							{
								ACBranch branch = _branches.get(k);
								int fbndx = branch.getFromBus().getIndex();
								int tbndx = branch.getToBus().getIndex();
								if (idx == fbndx)
								{
									Ppu = Ppu - _pfrm[k];
									Qpu = Qpu - _qfrm[k];
								}
								else if (idx == tbndx)
								{
									Ppu = Ppu - _pto[k];
									Qpu = Qpu - _qto[k];
								}
							}
						}
						
						for(Load l : _loads)
						{
							if (l.isInSvc())
							{
								int bndx = l.getBus().getIndex();
								if (idx == bndx)
								{
									Ppu = Ppu + l.getPpu();
									Qpu = Qpu + l.getQpu();
								}
							}
						}
						
						for (int k = 0; k < _nsh; ++k)
						{
							Shunt shunt = _shunts.get(k);
							if (shunt.isInSvc())
							{
								int bndx = shunt.getBus().getIndex();
								if (idx == bndx)
								{
									float bvm = _vm[bndx];
									float q = _shunts.get(k).getBpu() * bvm * bvm;
									Qpu = Qpu - q;
								}
							}
						}
									
						for (int k = 0; k < _nsvc; ++k)
						{
							SVC svc = _svclist.get(k);
							if (svc.isInSvc())
							{
								int bndx = svc.getBus().getIndex();
								if (idx == k)
								{
									float bvm = _vm[bndx];
									float q = svc.getBINIT() / 100f * bvm * bvm;
									Qpu = Qpu - q;
								}	
							}
						}
						
						int[] genIdxBus = busGroupElems.getGenIndex(idx);
						float sumPCap = 0.0f;
						float sumQCap = 0.0f;
						
						for (int kk=0; kk<genIdxBus.length; kk++)
						{
							Gen g = _gens.get(genIdxBus[kk]);
							if (g.isInSvc() == true)
							{
								sumPCap += (g.getPT() - g.getPB());
								sumQCap += (g.getQT() - g.getQB());
							}
						}
						float pPF = 0.0f;
						float qPF = 0.0f;
						if (sumPCap > 0.0001f) pPF = (gen.getPT() - gen.getPB()) / sumPCap;
						else pPF = 1.0f;
						if (sumQCap > 0.0001f) qPF = (gen.getQT() - gen.getQB()) / sumQCap;
						else qPF = 1.0f;

						if (markSlackBus == true) pMWInit = Ppu * pPF;
						if (markSlackBus == true ||  markPVBus == true) qMVAInit = Qpu * qPF;				
						pMWInit = pMWInit*100;
						qMVAInit = qMVAInit*100;
												
						outFile.print(" "+(i+1));
						if(_useToolIndexforOutput == true) outFile.print(" "+(idx+1));
						else outFile.print(" "+_buses.getI(idx));
						outFile.print(" "+pMWInit);
						outFile.print(" "+qMVAInit);
						outFile.println();
					}
					else
					{
						pMWInit = pMWInit*100;
						qMVAInit = qMVAInit*100;
						outFile.print(" "+(i+1));
						if(_useToolIndexforOutput == true) outFile.print(" "+(idx+1));
						else outFile.print(" "+_buses.getI(idx));
						outFile.print(" "+pMWInit);
						outFile.print(" "+qMVAInit);
						outFile.println();
					}
				}
			}
		    outFile.close();
		    System.out.println("Output Gen outputs Data  successfully");
	    } catch (FileNotFoundException e) {
	    	System.out.println();
	    	System.out.println("Cannot write Gen output file" + e);
	    	e.printStackTrace();
	    }
	}

	public void outputPgQgAll() throws PsseModelException
	{
		assertTrue(_conv == true);
		BusGroupElems busGroupElems = _model.getBusGroupElems();
		String rootDr=getPath();
		try
		{
			File genH = new File(rootDr+"PgQg_All.txt");
			if (genH.exists() && !genH.isDirectory())
			{
				System.out.println(); 
				if (genH.delete()) System.out.println("Original "+genH.getName() + " is deleted.");
			    else System.out.println("Delete operation is failed.");
			}
		    OutputStream resultFile = new FileOutputStream(rootDr+"PgQg_All.txt", true);
		    PrintStream outFile = new PrintStream (resultFile);
		    outFile.println(" GenIndex GenBusIndex Status PgMW  QgMVA  Vm  Va");
			for(int i=0; i<_ngen; i++)
			{
				Gen gen = _gens.get(i);
				if (gen.isInSvc())
				{
					Bus bus = gen.getBus();
					BusTypeCode busType = bus.getBusType();
					boolean markSlackBus = (busType == BusTypeCode.Slack);
					boolean markPVBus = (busType == BusTypeCode.Gen);
					float pMWInit = gen.getPpu();
					float qMVAInit = gen.getQpu();
					int idx = bus.getIndex();

					if (markSlackBus == true || markPVBus == true)
					{
						float Ppu = 0;
						float Qpu = 0;
						
						for(int k=0; k < _nbr; ++k)
						{
							boolean inSvc = _branches.isInSvc(k);
							if (inSvc == true)
							{
								ACBranch branch = _branches.get(k);
								int fbndx = branch.getFromBus().getIndex();
								int tbndx = branch.getToBus().getIndex();
								if (idx == fbndx)
								{
									Ppu = Ppu - _pfrm[k];
									Qpu = Qpu - _qfrm[k];
								}
								else if (idx == tbndx)
								{
									Ppu = Ppu - _pto[k];
									Qpu = Qpu - _qto[k];
								}
							}
						}
						
						for(Load l : _loads)
						{
							if (l.isInSvc())
							{
								int bndx = l.getBus().getIndex();
								if (idx == bndx)
								{
									Ppu = Ppu + l.getPpu();
									Qpu = Qpu + l.getQpu();
								}
							}
						}
						
						for (int k = 0; k < _nsh; ++k)
						{
							Shunt shunt = _shunts.get(k);
							if (shunt.isInSvc())
							{
								int bndx = shunt.getBus().getIndex();
								if (idx == bndx)
								{
									float bvm = _vm[bndx];
									float q = _shunts.get(k).getBpu() * bvm * bvm;
									Qpu = Qpu - q;
								}
							}
						}
									
						for (int k = 0; k < _nsvc; ++k)
						{
							SVC svc = _svclist.get(k);
							if (svc.isInSvc())
							{
								int bndx = svc.getBus().getIndex();
								if (idx == k)
								{
									float bvm = _vm[bndx];
									float q = svc.getBINIT() / 100f * bvm * bvm;
									Qpu = Qpu - q;
								}	
							}
						}
						
						int[] genIdxBus = busGroupElems.getGenIndex(idx);
						float sumPCap = 0.0f;
						float sumQCap = 0.0f;
													
						for (int kk=0; kk<genIdxBus.length; kk++)
						{
							Gen g = _gens.get(genIdxBus[kk]);
							if (g.isInSvc() == true)
							{
								sumPCap += (g.getPT() - g.getPB());
								sumQCap += (g.getQT() - g.getQB());
							}
						}
						float pPF = 0.0f;
						float qPF = 0.0f;
						if (sumPCap > 0.0001f) pPF = (gen.getPT() - gen.getPB()) / sumPCap;
						else pPF = 1.0f;
						if (sumQCap > 0.0001f) qPF = (gen.getQT() - gen.getQB()) / sumQCap;
						else qPF = 1.0f;

						if (markSlackBus == true) pMWInit = Ppu * pPF;
						if (markSlackBus == true ||  markPVBus == true) qMVAInit = Qpu * qPF;				
						pMWInit = pMWInit*100;
						qMVAInit = qMVAInit*100;
												
						outFile.print(" "+(i+1));
						if(_useToolIndexforOutput == true) outFile.print(" "+(idx+1));
						else outFile.print(" "+_buses.getI(idx));
						outFile.print(" "+1);     // Status, on-line
						outFile.print(" "+pMWInit);
						outFile.print(" "+qMVAInit);
						outFile.print(" "+_vm[idx]);
						outFile.print(" "+_va[idx]);
						outFile.println();
					}
					else
					{
						pMWInit = pMWInit*100;
						qMVAInit = qMVAInit*100;
						outFile.print(" "+(i+1));
						if(_useToolIndexforOutput == true) outFile.print(" "+(idx+1));
						else outFile.print(" "+_buses.getI(idx));
						outFile.print(" "+1);     // Status, on-line
						outFile.print(" "+pMWInit);
						outFile.print(" "+qMVAInit);
						outFile.print(" "+_vm[idx]);
						outFile.print(" "+_va[idx]);
						outFile.println();
					}
				}
				else
				{
					Bus bus = gen.getBus();
					int idx = bus.getIndex();
					outFile.print(" "+(i+1));
					if(_useToolIndexforOutput == true) outFile.print(" "+(idx+1));
					else outFile.print(" "+_buses.getI(idx));
					outFile.print(" "+0);     // Status, off-line
					outFile.print(" "+0);
					outFile.print(" "+0);
					outFile.print(" "+_vm[idx]);
					outFile.print(" "+_va[idx]);
					outFile.println();
				}
			}
		    outFile.close();
		    System.out.println("Output Gen_All outputs Data  successfully");
	    } catch (FileNotFoundException e) {
	    	System.out.println();
	    	System.out.println("Cannot write Gen All output file" + e);
	    	e.printStackTrace();
	    }
	}

	public void outputShunt() throws PsseModelException
	{
		assertTrue(_conv == true);
		String rootDr=getPath();
		try
		{		    
			File genH = new File(rootDr+"Shunt.txt");
			if (genH.exists() && !genH.isDirectory())
			{
				System.out.println(); 
				if (genH.delete()) System.out.println("Original "+genH.getName() + " is deleted.");
			    else System.out.println("Delete operation is failed.");
			}
			OutputStream resultFile = new FileOutputStream(rootDr+"Shunt.txt",true);
			PrintStream outFile = new PrintStream(resultFile);
			outFile.println(" ShuntBusIndex  QMvar");
			for (int i = 0; i < _nsh; ++i)
			{
				Shunt shunt = _shunts.get(i);
				if (shunt.isInSvc())
				{
					int bndx = shunt.getBus().getIndex();
					float bvm = _vm[bndx];
					float qshunt = shunt.getBpu() * bvm * bvm * 100f;
					if(_useToolIndexforOutput == true) outFile.print(" "+(bndx+1));
					else outFile.print(" "+_buses.getI(bndx));
					outFile.println(" "+qshunt);
				}
			}
		    outFile.close();
		    System.out.println("Output Shunt outputs Data  successfully");
		} catch (FileNotFoundException e) {
	    	System.out.println();
	    	System.out.println("Cannot write Shunt output file" + e);
	    	e.printStackTrace();
		}
	}
	
	public void outputBusShunt() throws PsseModelException
	{
		float[][] busShunts = getBusShunt();
		float[] busShuntsG = busShunts[0];
		float[] busShuntsB = busShunts[1];
		String rootDr=getPath();
		try
		{
			File genH = new File(rootDr+"BusShunt.txt");
			if (genH.exists() && !genH.isDirectory())
			{
				System.out.println(); 
				if (genH.delete()) System.out.println("Original "+genH.getName() + " is deleted.");
			    else System.out.println("Delete operation is failed.");
			}
		    OutputStream resultFile = new FileOutputStream(rootDr+"BusShunt.txt", true);
		    PrintStream outFile = new PrintStream (resultFile);
		    outFile.println(" BusIndex, MW injected at V = 1.0 p.u, MVAr injected at V = 1.0 p.u.");
			for(int i=0; i<_nbus; i++)
			{
				//outFile.print(" "+(i+1));
				if(_useToolIndexforOutput == true) outFile.print(" "+(i+1));
				else outFile.print(" "+_buses.getI(i));
				outFile.print(" "+busShuntsG[i]);
				outFile.print(" "+busShuntsB[i]);
				outFile.println();
			}
		    outFile.close();
		    System.out.println("Output BusShunt Data successfully");
	    } catch (FileNotFoundException e) {
	    	System.out.println();
	    	System.out.println("Cannot write BusLoad output file" + e);
	    	e.printStackTrace();
	    }
	}
	
	public void outputSVC() throws PsseModelException
	{
		assertTrue(_conv == true);
		String rootDr=getPath();
		try
		{		    
			File genH = new File(rootDr+"SVC.txt");
			if (genH.exists() && !genH.isDirectory())
			{
				System.out.println(); 
				if (genH.delete()) System.out.println("Original "+genH.getName() + " is deleted.");
			    else System.out.println("Delete operation is failed.");
			}
			OutputStream resultFile = new FileOutputStream(rootDr+"SVC.txt",true);
			PrintStream outFile = new PrintStream(resultFile);
			outFile.println(" SVCBusIndex, QMvar");
			for (int k = 0; k < _nsvc; ++k)
			{
				SVC svc = _svclist.get(k);
				if (svc.isInSvc())
				{
					int bndx = svc.getBus().getIndex();
					float bvm = _vm[bndx];
					float q = svc.getBINIT() / 100f * bvm * bvm;
					if(_useToolIndexforOutput == true) outFile.print(" "+(bndx+1));
					else outFile.print(" "+_buses.getI(bndx));
					outFile.println(" "+q*100);					
				}
			}
		    outFile.close();
		    System.out.println("Output SVC outputs Data  successfully");
		} catch (FileNotFoundException e) {
	    	System.out.println();
	    	System.out.println("Cannot write SVC output file" + e);
	    	e.printStackTrace();
		}
	}
		
	public void outputLoad() throws PsseModelException, IOException
	{
		String rootDr=getPath();
		try
		{		    
			File genH = new File(rootDr+"Load.txt");
			if (genH.exists() && !genH.isDirectory())
			{
				System.out.println(); 
				if (genH.delete()) System.out.println("Original "+genH.getName() + " is deleted.");
			    else System.out.println("Delete operation is failed.");
			}
		    OutputStream resultFile = new FileOutputStream(rootDr+"Load.txt", true);
		    PrintStream outFile = new PrintStream (resultFile);
		    outFile.println(" LoadBusIndex, Pload, Qload");
			for(int i=0; i<_nLoad; i++)
			{
				Load load = _loads.get(i);
				if (load.isInSvc())
				{
					Bus bus = load.getBus();
					int idx = bus.getIndex();
					float pMW = load.getPpu()*100;
					float qMVA = load.getQpu()*100;
					if(_useToolIndexforOutput == true) outFile.print(" "+(idx+1));
					else outFile.print(" "+_buses.getI(idx));
					outFile.print(" "+pMW);
					outFile.print(" "+qMVA);
					outFile.println();
				}
			}
		    outFile.close();
		    System.out.println("Output Load outputs Data  successfully");
	    } catch (FileNotFoundException e) {
	    	System.out.println();
	    	System.out.println("Cannot write Load output file" + e);
	    	e.printStackTrace();
	    }
	}
	
	public void outputBusLoads() throws PsseModelException
	{
		float[][] busLoads = getBusLoad();
		float[] busPLoads = busLoads[0];
		float[] busQLoads = busLoads[1];
		String rootDr=getPath();
		try
		{
			File genH = new File(rootDr+"BusLoad.txt");
			if (genH.exists() && !genH.isDirectory())
			{
				System.out.println(); 
				if (genH.delete()) System.out.println("Original "+genH.getName() + " is deleted.");
			    else System.out.println("Delete operation is failed.");
			}
		    OutputStream resultFile = new FileOutputStream(rootDr+"BusLoad.txt", true);
		    PrintStream outFile = new PrintStream (resultFile);
		    outFile.println(" LoadBusIndex, Pload, Qload");
			for(int i=0; i<_nbus; i++)
			{
				if(_useToolIndexforOutput == true) outFile.print(" "+(i+1));
				else outFile.print(" "+_buses.getI(i));
				outFile.print(" "+busPLoads[i]);
				outFile.print(" "+busQLoads[i]);
				outFile.println();
			}
		    outFile.close();
		    System.out.println("Output BusLoad Data successfully");
	    } catch (FileNotFoundException e) {
	    	System.out.println();
	    	System.out.println("Cannot write BusLoad output file" + e);
	    	e.printStackTrace();
	    }
	}
	
	/** return[0] : bus P Load;
	 *  return[1] : bus Q Load.
	 *   */
	public float[][] getBusLoad() throws PsseModelException
	{
		int nBus = _model.getBuses().size();
		float[] busPLoad = new float[nBus];
		float[] busQLoad = new float[nBus];
		BusGroupElems busGroupElems = _model.getBusGroupElems();
		for(int i=0; i<nBus; i++)
		{
			int[] idxLoads = busGroupElems.getLoadIndex(i);
			if (idxLoads != null)
			{
				for(int j=0; j<idxLoads.length; j++)
				{
					int idxLoad = idxLoads[j];
					if(_model.getLoads().isInSvc(idxLoad))
					{
						busPLoad[i] += _model.getLoads().getP(idxLoad);					
	     				busQLoad[i] += _model.getLoads().getQ(idxLoad);									
					}
				}
			}
		}
		return new float[][] {busPLoad, busQLoad};
	}
	
	/** return[0] : bus Shunt Gs;
	 *  return[1] : bus Shunt Bs.
	 *   */
	public float[][] getBusShunt() throws PsseModelException
	{
		int nBus = _model.getBuses().size();
		float[] busShuntG = new float[nBus];
		float[] busShuntB = new float[nBus];
		BusGroupElems busGroupElems = _model.getBusGroupElems();
		for(int i=0; i<nBus; i++)
		{
			int[] idxShunts = busGroupElems.getShuntIndex(i);
			if (idxShunts != null)
			{
				for(int j=0; j<idxShunts.length; j++)
				{
					int idxShunt = idxShunts[j];
					if(_model.getShunts().isInSvc(idxShunt))
					{
					    busShuntG[i] += _model.getShunts().getG(idxShunt);
						busShuntB[i] += _model.getShunts().getB(idxShunt);
					}
				}
			}
		}
		return new float[][] {busShuntG, busShuntB};
	}
	
	

	@Test
	public static void main(String[] args) throws Exception
	{
		long t_Start = System.nanoTime();
		String uri = null;
		String svstart = "Flat";
		File results = null;
		float minxmag = 0.0001f;
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
				case "voltage":
					svstart = args[i++];
					break;
				case "results":
					results = new File(args[i++]);
					break;
				case "minxmag":
					minxmag = Float.parseFloat(args[i++]);
					break;
			}
		}
		
		VoltageSource vstart = VoltageSource.fromConfig(svstart);

		if (uri == null)
		{
			System.err.format("Usage: -uri model_uri [-voltage flat|realtime] [ -minxmag smallest reactance magnitude (default to 0.001) ] [ -debug dir ] [ -results path_to_results]");
			System.exit(1);
		}
		
		System.out.println("   Time before read case file: " + (System.nanoTime() - t_Start)/1e9f);
		PsseModel model = PsseModel.Open(uri);
		model.getBusTypeManagerData();
		System.out.println("   Time after read case file: " + (System.nanoTime() - t_Start)/1e9f);
//		CheckModelData checkData = new CheckModelData(model);
//		checkData.launchDataCheck();
//		model.writeData();
				
		model.setMinXMag(minxmag);
		FastDecoupledPowerFlow pf = new FastDecoupledPowerFlow(model);
		pf.setShowRunInfo(true);

		boolean MarkVarLimit = ParamFDPFlow.getGenVarLimit();
		pf.setMarkUsingVgSched(true);
		PowerFlowConvergenceList pslist = pf.runPowerFlow(vstart, MarkVarLimit, 1);	
		System.out.println("   Solution time after power flow converges: " + (System.nanoTime() - t_Start)/1e9f);
		pf.updateModelVoltage();
		//if (pf.isPfConv() == true) pf.outputResults();
		if (pf.getVioRateC().getViol() == true)
		{
        	float sumVmVio = pf.getVioRateC().getSumVmDiff();
        	float sumBrcVio = pf.getVioRateC().getSumBrcDiff();
        	System.out.println("PF sumVmVio: "+sumVmVio);
        	System.out.println("PF sumBrcVio: "+sumBrcVio);
		}
    	System.out.println("# of SlackBuses: "+pf.getSlackBuses().length);
    	System.out.println("# of PvBuses: "+pf.getPvBuses().length);
    	System.out.println("# of PqBuses: "+pf.getPqBuses().length);

		System.out.println("Island Converged Iterations WorstPBus   Pmm   WorstQBus   Qmm");
		IslandList islands = model.getIslands();
		BusList buses = model.getBuses();
		for(PowerFlowConvergence psol : pslist)
		{
			Island i = islands.get(psol.getIslandNdx());
			System.out.format("  %s     %5s       %2d     %9s %7.2f %9s %7.2f\n",
				i.getObjectName(),
				String.valueOf(psol.getConverged()),
				psol.getIterationCount(),
				buses.get(psol.getWorstPbus()).getObjectName(),
				PAMath.pu2mw(psol.getWorstPmm()),
				buses.get(psol.getWorstQbus()).getObjectName(),
				PAMath.pu2mvar(psol.getWorstQmm()));
		}
		if (results != null)
		{
			MismatchReport mmr = new MismatchReport(model);
			PowerCalculator pc = new PowerCalculator(model);
			pc.setDebugEnabled(mmr);
			pc.calculateMismatches(pf.getVA(), pf.getVM());
			mmr.report(results);
		}
		System.out.println("   Total time : " + (System.nanoTime() - t_Start)/1e9f);
	}

	public void dumpMatrices(File tdir) throws IOException, PsseModelException
	{
		dumpMatrix(_bp, tdir, "factbp.csv");
		dumpMatrix(_bpp, tdir, "factbpp.csv");
	}
	
	void dumpMatrix(FactorizedBMatrix b, File tdir, String nm)
			throws IOException, PsseModelException
	{
		PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(
				new File(tdir, nm))));
		b.dump(_model, pw);
		pw.close();
	}

	void adjustTransformerTaps(float[] vm, float[] va)
	{
		
	}
}
