package com.rtca_cts.contingencyanalysis;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import org.junit.Test;

import com.powerdata.openpa.psse.BusTypeCode;
import com.powerdata.openpa.psse.Gen;
import com.powerdata.openpa.psse.GenList;
import com.powerdata.openpa.psse.LoadList;
import com.powerdata.openpa.psse.PsseModel;
import com.powerdata.openpa.psse.PsseModelException;
import com.powerdata.openpa.psse.ShuntList;
import com.powerdata.openpa.psse.SvcList;
import com.utilxl.array.AuxArrayListXL;
import com.utilxl.array.AuxArrayXL;

/**
 * 
 * Initialized in Jun, 2014.
 * 
 * @author Xingpeng.Li (xplipower@gmail.com)
 * 
 */
public class GenVarLimit {
	
	int _option = 1;        // 1 : switch all the PV buses that Gen Var hit its limit to PQ buses.
	                    // 2 : switch at most "_numPVtoPQ" PV buses that Gen Var hit its limit to PQ buses based on the absolute violation.
                        // 3 : switch at most "_numPVtoPQ" PV buses that Gen Var hit its limit to PQ buses based on the percentage violation.
	int _numPVtoPQ = 10;        // used when _option = 2 or 3.
	
	PsseModel _model;
	GenList _gens;
	int _nGen;
	
	float[] _Qg;       // in MVar
	float[] _QLoad;    // in MVar
	float[] _Bshunt;   // in p.u.
	float[] _Bsvc;     // in p.u.
	
//	ElemGroupAtGenBuses _elemGroupGenBuses;
	HashMap<Integer,int[]> _idxGensAtPVBus = new HashMap<Integer,int[]>();  // key is bus index, value is the indices of Gens at that bus.
	HashMap<Integer,int[]> _idxLoadsAtPVBus = new HashMap<Integer,int[]>();  // key is bus index, value is the indices of Loads at that bus.
	HashMap<Integer,int[]> _idxShuntsAtPVBus = new HashMap<Integer,int[]>();  // key is bus index, value is the indices of Shunts at that bus.
	HashMap<Integer,int[]> _idxSVCsAtPVBus = new HashMap<Integer,int[]>();  // key is bus index, value is the indices of SVCs at that bus.
	HashMap<Integer,int[]> _idxBrcfrmAtPVBus = new HashMap<Integer,int[]>();  // key is bus index, value is the indices of branch whose frm bus is that bus.
	HashMap<Integer,int[]> _idxBrctoAtPVBus = new HashMap<Integer,int[]>();  // key is bus index, value is the indices of branch whose to bus that bus.

	int[] _OrigPVBuses;
	int[] _OrigPQBuses;
	
	//HashMap<Integer,float[]> _OrigPVBusesDel = new HashMap<Integer,float[]>();  // key is bus index, value is Qg of Gens at that bus.
	ArrayList<Integer> _OrigPVBusesDel = new ArrayList<Integer>();

	int[] _CurPVBuses;      // current PV buses
	int[] _CurPQBuses;
	
	//int[] _CurBusesPVtoPQ;
	ArrayList<Integer> _CurBusesPVtoPQ = new ArrayList<Integer>();
	ArrayList<Float> _CurBusesPVtoPVarVio = new ArrayList<Float>(); // save the Q violation: equals to (Qg - Qmax); or equals to (Qg - Qmin). 
	ArrayList<Float> _CurBusesPVtoPVarVioPctg = new ArrayList<Float>(); // save the Q violation: equals to (Qg - Qmax)/(Qmax - Qmin); or equals to (Qg - Qmin)/(Qmax - Qmin). 
	
	
	public GenVarLimit(PsseModel model) throws PsseModelException
	{
		_model = model;
		initial();
	}
	
	public GenVarLimit(PsseModel model, int option) throws PsseModelException
	{
		_model = model;
		_option = option;
		initial();
	}
	
	private void initial() throws PsseModelException
	{
		_gens = _model.getGenerators();
		_nGen = _gens.size();
		_Qg = _gens.getQ().clone();
		
		LoadList loadlist = _model.getLoads();
		ShuntList shuntlist = _model.getShunts();
		SvcList svcList = _model.getSvcs();
		_QLoad = loadlist.getQ();
		_Bshunt = shuntlist.getB();
		_Bsvc = svcList.getBINIT();

		_OrigPVBuses = _model.getBusNdxForType(BusTypeCode.Gen);
		_OrigPQBuses = _model.getBusNdxForType(BusTypeCode.Load);
		_CurPVBuses = _OrigPVBuses;
		_CurPQBuses = _OrigPQBuses;
		setElemGroupGenBuses(_model.getElemGroupGenBuses());
	}

	public void setElemGroupGenBuses(ElemGroupAtGenBuses elemGroupGenBuses)
	{
//		_elemGroupGenBuses = elemGroupGenBuses;
		_idxGensAtPVBus = elemGroupGenBuses.getIdxGensAtBus();
		_idxLoadsAtPVBus = elemGroupGenBuses.getIdxLoadsAtBus();
		_idxShuntsAtPVBus = elemGroupGenBuses.getIdxShuntsAtBus();
		_idxSVCsAtPVBus = elemGroupGenBuses.getIdxSVCsAtBus();
		_idxBrcfrmAtPVBus = elemGroupGenBuses.getIdxBrcfrmAtBus();
		_idxBrctoAtPVBus = elemGroupGenBuses.getIdxBrctoAtBus();
	}
	
	public boolean checkGenVarLimit(float[] Vm, float[] Qfrm, float[] Qto) throws PsseModelException
	{
		ArrayList<Integer> busesPVtoPQ = new ArrayList<Integer>();
		boolean var = true;
		for (int i=0; i<_CurPVBuses.length; i++)
		{
			int idxBus = _CurPVBuses[i];
			float sumQgmax = 0;
			float sumQgmin = 0;
			int[] idxGens = _idxGensAtPVBus.get(idxBus);
			int numGen = idxGens.length;
			for (int j=0; j<numGen; j++)
			{
				int b = idxGens[j];
				Gen gen = _gens.get(b);
				if (gen.isInSvc())
				{
					sumQgmax += gen.getQT();
					sumQgmin += gen.getQB();
				}
			}
			
			//ensureGenVarLimit()
			if ((Math.abs(sumQgmax) < 0.0001) && (Math.abs(sumQgmin) < 0.0001) ) continue;
			float sumQg = calcSumQg(Vm, Qfrm, Qto, idxBus)*100;			
			if (sumQg > sumQgmax)
			{
				var = false;
				busesPVtoPQ.add(_CurPVBuses[i]);
				float diffQ = sumQg - sumQgmax;
				_CurBusesPVtoPVarVio.add(diffQ);
				_CurBusesPVtoPVarVioPctg.add(diffQ/(sumQgmax - sumQgmin));
				
				for (int j=0; j<numGen; j++)
				{
					int b = idxGens[j];
					Gen gen = _gens.get(b);
					if (gen.isInSvc())
						gen.setQ(gen.getQT());
				}
			}
			else if (sumQg < sumQgmin)
			{
				var = false;
				busesPVtoPQ.add(_CurPVBuses[i]);
				float diffQ = sumQgmin - sumQg;
				_CurBusesPVtoPVarVio.add(diffQ);
				_CurBusesPVtoPVarVioPctg.add(diffQ/(sumQgmax - sumQgmin));

				for (int j=0; j<numGen; j++)
				{
					int b = idxGens[j];
					Gen gen = _gens.get(b);
					if (gen.isInSvc())
						gen.setQ(gen.getQB());
				}
			}
		}
		//_model.resetTP();
		choosePVtoPQ(busesPVtoPQ);
		switchPVtoPQ();
		return var;
	}
	
	public float calcSumQg(float[] Vm, float[] Qfrm, float[] Qto, int idxBus) throws PsseModelException
	{
		float vm = Vm[idxBus];
		float sumQg = 0;
		int[] idxLoads = _idxLoadsAtPVBus.get(idxBus);
		for(int i=0; i<idxLoads.length; i++)
		{
			int idx = idxLoads[i];
			if (_model.getLoads().get(idx).isInSvc())
				sumQg += _QLoad[idx]/100;
		}
		int[] idxShunts = _idxShuntsAtPVBus.get(idxBus);		
		for(int i=0; i<idxShunts.length; i++)
		{
			int idx = idxShunts[i];
			if (_model.getShunts().get(idx).isInSvc())
				sumQg -= _Bshunt[idx]/100 * vm * vm;
		}
		int[] idxSvcs = _idxSVCsAtPVBus.get(idxBus);		
		for(int i=0; i<idxSvcs.length; i++)
		{
			int idx = idxSvcs[i];
			if (_model.getSvcs().get(idx).isInSvc())
				sumQg -= _Bsvc[idx]/100 * vm * vm;
		}
		
		int[] idxBrcfrm = _idxBrcfrmAtPVBus.get(idxBus);		
		for(int i=0; i<idxBrcfrm.length; i++)
		{
			int idx = idxBrcfrm[i];
			if (_model.getBranches().get(idx).isInSvc())
				sumQg += Qfrm[idx];
		}
		int[] idxBrcto = _idxBrctoAtPVBus.get(idxBus);		
		for(int i=0; i<idxBrcto.length; i++)
		{
			int idx = idxBrcto[i];
			if (_model.getBranches().get(idx).isInSvc())
				sumQg += Qto[idx];
		}
		return sumQg;
	}
	
	public void choosePVtoPQ(ArrayList<Integer> origBusesPVtoPQ)
	{
		_CurBusesPVtoPQ.clear();
		ArrayList<Integer> busesPVtoPQ = processPVtoPQ(origBusesPVtoPQ);
		int sizeB = busesPVtoPQ.size();
		for(int i=0; i<sizeB; i++)
		{
			int ndx = busesPVtoPQ.get(i);
			_CurBusesPVtoPQ.add(ndx);
			_OrigPVBusesDel.add(ndx);
		}
	}
	
	ArrayList<Integer> processPVtoPQ(ArrayList<Integer> origBusesPVtoPQ)
	{
		ArrayList<Integer> busesPVtoPQ = new ArrayList<Integer>();
		if (_option == 1) busesPVtoPQ = origBusesPVtoPQ;
		else
		{
			if (origBusesPVtoPQ.size() <= _numPVtoPQ) busesPVtoPQ = origBusesPVtoPQ;
			else
			{
				int[] ndx = selectKeyPVBuses(origBusesPVtoPQ);
				
				int[] elem = AuxArrayListXL.getElemBasedOnIndex(origBusesPVtoPQ, ndx);
				for (int i=0; i<_numPVtoPQ; i++)
				{
					busesPVtoPQ.add(elem[i]);
				}
			}
		}
		return busesPVtoPQ;
	}
	
	/** switch at most "_numPVtoPQ" PV buses that Gen Var hit its limit to PQ buses. */
	int[] selectKeyPVBuses(ArrayList<Integer> origBusesPVtoPQ)
	{
		int[] ndx = new int[_numPVtoPQ];
		ArrayList<Float> QgVio = new ArrayList<Float>();
		if (_option == 2) QgVio = _CurBusesPVtoPVarVio;
		else if (_option == 3) QgVio = _CurBusesPVtoPVarVioPctg;
		else System.err.println("@Param _option is not chosen properly..");
		float[] maxElems = new float[_numPVtoPQ];
		Collections.sort(QgVio);
		int sizeQgVio = QgVio.size();
		for (int i=0; i<_numPVtoPQ; i++)
		{
			maxElems[i] = QgVio.get(sizeQgVio-i-1);
		}
//		for (int i=0; i<_numPVtoPQ; i++)
//		{
//			float maxQVio = Collections.max(QgVio);
//			int a = QgVio.indexOf(maxQVio);
//			assertTrue(a != -1);
//			maxElems[i] = maxQVio;
//			QgVio.remove(a);
//		}
		ndx = getIndices(QgVio, maxElems);
		_CurBusesPVtoPVarVio.clear();
		_CurBusesPVtoPVarVioPctg.clear();
		return ndx;
	}
	
	@Test
	int[] getIndices(ArrayList<Float> dataArray, float[] array)
	{
		boolean noDupElem = AuxArrayXL.checkNoDupElem(array);
		//TODO:  need to write another routine if it is false.
		assertTrue(noDupElem == true);
		int size = array.length;
		int[] ndx = new int[size];
		for (int i=0; i<size; i++)
		{
			ndx[i] = dataArray.indexOf(array[i]);
			assertTrue(ndx[i] != -1);
		}
		return ndx;
	}
			
	public void switchPVtoPQ() throws PsseModelException
	{
		Iterator<Integer> itr = _CurBusesPVtoPQ.iterator();
//		int tmp = -1;
		while(itr.hasNext())
		{
//			tmp++;
//			if (tmp >= 5) break;
			int idxBusPVtoPQ = itr.next();
			_CurPVBuses = delPVbus(_CurPVBuses, idxBusPVtoPQ);
			_CurPQBuses = addPQbus(_CurPQBuses, idxBusPVtoPQ);
		}
		_model.setBusNdxForTypeGen(_CurPVBuses);
		_model.setBusNdxForTypeLoad(_CurPQBuses);
	}
	
	private int[] delPVbus(int[] oldPV, int ndxDel) throws PsseModelException
	{
		int idx = -1;
		for (int i=0; i<oldPV.length; i++)
		{
			if (oldPV[i] == ndxDel)
			{
				idx = i;
				break;
			}
		}
		if (idx == -1) System.err.println("Something wrong when calculate the new PV buses in GenVarLimit.java.");
		
		int newPVlength = oldPV.length-1;
		int[] newPV = new int[newPVlength];
		System.arraycopy(oldPV, 0, newPV, 0, idx);
		int tmp = newPVlength - idx;
		int srcPos = idx + 1;
		System.arraycopy(oldPV, srcPos, newPV, idx, tmp);
		return newPV;
	}
	
	private int[] addPQbus(int[] oldPQ, int ndxBus) throws PsseModelException
	{
		int newPQlength = oldPQ.length + 1;
		int[] newPQ = new int[newPQlength];
		Arrays.fill(newPQ, -1);
		int ndxBusesAdded = newPQlength - 1;
		for (int i=0; i<oldPQ.length; i++)
		{
			if (ndxBus < oldPQ[i])
			{
				ndxBusesAdded = i;
				break;
			}
		}
		System.arraycopy(oldPQ, 0, newPQ, 0, ndxBusesAdded);
		newPQ[ndxBusesAdded] = ndxBus;
		int tmp = newPQlength - ndxBusesAdded - 1;
		int desPos = ndxBusesAdded + 1;
		System.arraycopy(oldPQ, ndxBusesAdded, newPQ, desPos, tmp);
		return newPQ;
	}
	
	/** Go back to original Bus type Setting, and original Pg, Qg */
	public void refresh() throws PsseModelException
	{
		_model.setBusNdxForTypeGen(_OrigPVBuses);
		_model.setBusNdxForTypeLoad(_OrigPQBuses);
		
		for (int idx=0; idx<_OrigPVBusesDel.size(); idx++)
		{
			int key = _OrigPVBusesDel.get(idx);
			int[] idxGens = _idxGensAtPVBus.get(key);
			for (int i=0; i<idxGens.length; i++)
			{
				int idxGen = idxGens[i];
				Gen gen = _gens.get(idxGen);
				if (gen.isInSvc())
				{
					float qg = _Qg[idxGen];
					gen.setQ(qg);
				}
			}
		}
	}
	
	public ArrayList<Integer> getOrigPVBusesDel() {return _OrigPVBusesDel;}

}
