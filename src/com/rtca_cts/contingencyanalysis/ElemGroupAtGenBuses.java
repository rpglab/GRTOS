package com.rtca_cts.contingencyanalysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.powerdata.openpa.psse.ACBranch;
import com.powerdata.openpa.psse.ACBranchList;
import com.powerdata.openpa.psse.BusTypeCode;
import com.powerdata.openpa.psse.GenList;
import com.powerdata.openpa.psse.OneTermDev;
import com.powerdata.openpa.psse.PsseModel;
import com.powerdata.openpa.psse.PsseModelException;

/**
 * 
 * @author Xingpeng.Li (xplipower@gmail.com)
 * 
 */
public class ElemGroupAtGenBuses {
	
	PsseModel _model;
	GenList _gens;
//	float[] _OrigQg;
	int _nGen;
	
	boolean _checkOnLineElemsOnly = false;  // if true, then only check elements which are online.
	
	int _numPVBuses;
	ArrayList<Integer> _PVBuses;   // Actually, it is the set of Gen Buses.
	ArrayList<Float> _PVBusesSumQgMax;
	ArrayList<Float> _PVBusesSumQgMin;
	
	HashMap<Integer,int[]> _idxGensAtPVBus = new HashMap<Integer,int[]>();  // key is bus index, value is the index of Gen at that bus.
	HashMap<Integer,int[]> _idxLoadsAtPVBus = new HashMap<Integer,int[]>();  // key is bus index, value is the index of Load at that bus.
	HashMap<Integer,int[]> _idxShuntsAtPVBus = new HashMap<Integer,int[]>();  // key is bus index, value is the index of Shunt at that bus.
	HashMap<Integer,int[]> _idxSVCsAtPVBus = new HashMap<Integer,int[]>();  // key is bus index, value is the index of SVC at that bus.
	HashMap<Integer,int[]> _idxBrcfrmAtPVBus = new HashMap<Integer,int[]>();  // key is bus index, value is the index of branch whose frm bus is that bus.
	HashMap<Integer,int[]> _idxBrctoAtPVBus = new HashMap<Integer,int[]>();  // key is bus index, value is the index of branch whose to bus that bus.
	
	public ElemGroupAtGenBuses(PsseModel model) throws PsseModelException
	{
		_model = model;
		_gens = _model.getGenerators();
		_nGen = _gens.size();
		initial();
		launch();
	}
	
	void launch() throws PsseModelException
	{
		setGensAtPVBus();
		setLoadsAtPVBus();
		setShuntsAtPVBus();
		setSVCsAtPVBus();
		setBrcAtPVBus();
	}
	
	void initial() throws PsseModelException
	{
		//TODO: use all generators buses? -- maybe not needed.
		int[] OrigPVBus = _model.getBusNdxForType(BusTypeCode.Gen);
		int[] slack = _model.getBusNdxForType(BusTypeCode.Slack);
		int[] OrigPVBuses = new int[OrigPVBus.length + slack.length];
		System.arraycopy(OrigPVBus, 0, OrigPVBuses, 0, OrigPVBus.length);
		System.arraycopy(slack, 0, OrigPVBuses, OrigPVBus.length, slack.length);
		
		_numPVBuses = OrigPVBuses.length;
		_PVBuses = new ArrayList<Integer>();
		for (int i=0; i<OrigPVBuses.length; i++)
			_PVBuses.add(OrigPVBuses[i]);
	}
	
	void setGensAtPVBus() throws PsseModelException {setElemsAtPVBus(_model.getGenerators(), _idxGensAtPVBus);}
	void setLoadsAtPVBus() throws PsseModelException {setElemsAtPVBus(_model.getLoads(), _idxLoadsAtPVBus);}
	void setShuntsAtPVBus() throws PsseModelException {setElemsAtPVBus(_model.getShunts(), _idxShuntsAtPVBus);}
	void setSVCsAtPVBus() throws PsseModelException {setElemsAtPVBus(_model.getSvcs(), _idxSVCsAtPVBus);}
	
	private void setElemsAtPVBus(List<? extends OneTermDev> list, HashMap<Integer,int[]> idxElemsAtPVBus) throws PsseModelException
	{
		int numElem = list.size();
		int[] numElemAtPVBuses = new int[_numPVBuses];
		int[][] idxElemAtPVBus = new int[_numPVBuses][numElem];

		for(int i=0; i<numElem; i++)
		{
			OneTermDev elem = list.get(i);
			boolean check = true;
			if (_checkOnLineElemsOnly == true) check = elem.isInSvc();
			if (check == true)
			{
	     		int idx	= elem.getBus().getIndex();
				if(_PVBuses.contains(idx) == true)
				{
					int idxBusElem = _PVBuses.indexOf(idx);
					idxElemAtPVBus[idxBusElem][numElemAtPVBuses[idxBusElem]] = i;
					numElemAtPVBuses[idxBusElem]++;
				}
			}
		}
		for(int i=0; i<_numPVBuses; i++)
		{
			int[] a = Arrays.copyOf(idxElemAtPVBus[i], numElemAtPVBuses[i]);
			idxElemsAtPVBus.put(_PVBuses.get(i), a);
		}
	}

		
	private void setBrcAtPVBus() throws PsseModelException
	{
		ACBranchList branches = _model.getBranches();
		int nbr = branches.size();
		
		int[] numBrcAtPVBusesfrm = new int[_numPVBuses];
		int[][] idxBrcAtPVBusfrm = new int[_numPVBuses][nbr];
		
		int[] numBrcAtPVBusesto = new int[_numPVBuses];
		int[][] idxBrcAtPVBusto = new int[_numPVBuses][nbr];

		for(int i=0; i<nbr; i++)
		{
			ACBranch brc = branches.get(i);
			boolean check = true;
			if (_checkOnLineElemsOnly == true) check = brc.isInSvc();
			if (check == true)
			{
	     		int frm	= brc.getFromBus().getIndex();
				if(_PVBuses.contains(frm) == true)
				{
					int idxBusBrcfrm = _PVBuses.indexOf(frm);
					idxBrcAtPVBusfrm[idxBusBrcfrm][numBrcAtPVBusesfrm[idxBusBrcfrm]] = i;
					numBrcAtPVBusesfrm[idxBusBrcfrm]++;
				}
	     		int to	= brc.getToBus().getIndex();
				if(_PVBuses.contains(to) == true)
				{
					int idxBusBrcto = _PVBuses.indexOf(to);
					idxBrcAtPVBusto[idxBusBrcto][numBrcAtPVBusesto[idxBusBrcto]] = i;
					numBrcAtPVBusesto[idxBusBrcto]++;
				}
			}
		}
		for(int i=0; i<_numPVBuses; i++)
		{
			int[] a = Arrays.copyOf(idxBrcAtPVBusfrm[i], numBrcAtPVBusesfrm[i]);
			_idxBrcfrmAtPVBus.put(_PVBuses.get(i), a);
		}
		for(int i=0; i<_numPVBuses; i++)
		{
			int[] a = Arrays.copyOf(idxBrcAtPVBusto[i], numBrcAtPVBusesto[i]);
			_idxBrctoAtPVBus.put(_PVBuses.get(i), a);
		}
	}
	
	public HashMap<Integer,int[]> getIdxGensAtBus() {return _idxGensAtPVBus;}
	public HashMap<Integer,int[]> getIdxLoadsAtBus() {return _idxLoadsAtPVBus;}
	public HashMap<Integer,int[]> getIdxShuntsAtBus() {return _idxShuntsAtPVBus;}
	public HashMap<Integer,int[]> getIdxSVCsAtBus() {return _idxSVCsAtPVBus;}
	public HashMap<Integer,int[]> getIdxBrcfrmAtBus() {return _idxBrcfrmAtPVBus;}
	public HashMap<Integer,int[]> getIdxBrctoAtBus() {return _idxBrctoAtPVBus;}
	
}
