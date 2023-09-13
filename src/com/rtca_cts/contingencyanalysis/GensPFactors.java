package com.rtca_cts.contingencyanalysis;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

import com.powerdata.openpa.psse.Bus;
import com.powerdata.openpa.psse.BusTypeCode;
import com.powerdata.openpa.psse.Gen;
import com.powerdata.openpa.psse.GenList;
import com.powerdata.openpa.psse.Island;
import com.powerdata.openpa.psse.PsseModel;
import com.powerdata.openpa.psse.PsseModelException;
import com.rtca_cts.param.ParamGenReDisptch;

/**
 * 
 * Initialized in April, 2014.
 * 
 * @author Xingpeng.Li (xplipower@gmail.com)
 * 
 */
public class GensPFactors {
	
	PsseModel _model;
	Island _island;
	GenList _gens;
	int _nGens;
	int _numGensOn;
	int _ndxBusGenPmax;   // bus index of the generator that has the largest (active power) capacity; it is used only if _markSlacktoPV is true.

	boolean _checkArea = ParamGenReDisptch.getCheckArea();
	
	int _ndxGenCont;    // index of Generator that is contingency.
	int _ndxBusGenCont; // index of bus where the generator, which is contingency, locates.
	boolean _markSlack;   // true if the bus where the generator that has contingency locates is slack bus.
	                      // if false, then, the following three variables should be false.
	boolean _markStillSlack;  // true if the bus where the generator that has contingency locates is still a slack bus.
	boolean _markSlacktoPV;  // true if the bus where the generator that has contingency locates becomes a PV bus.
	boolean _markSlacktoPQ;  // true if the bus where the generator that has contingency locates becomes a PQ bus.
	boolean _markPV;    // true if the bus where the generator that has contingency locates is PV bus.
	                      // if false, then, the following two variables should be false.
	boolean _markStillPV;   // true if the bus where the generator that has contingency locates is still a PV bus.
	boolean _markPVtoPQ;    // true if the bus where the generator that has contingency locates becomes a PQ bus.
	
	int[] _OrigSlackBuses;
	int[] _OrigPVBuses;
	int[] _OrigPQBuses;
	
	boolean _isQgAdj = false; // if false, generation redispatch does not involve reactive power.
	float[] _PgOrig;  // original active power 
	float[] _QgOrig;  // original reactive power
	
	float _PgenLoss;
	float _QgenLoss;
	
	float[] _PfactorsP;
	float[] _PfactorsQ;
	
	public GensPFactors(PsseModel model, int ndxGenCont) throws PsseModelException
	{
		_model = model;
		_markSlack = false;
		_markPV = false;
		//checkIsland();
		_gens = _model.getGenerators();
		_ndxGenCont = ndxGenCont;
		_ndxBusGenCont = _gens.get(ndxGenCont).getBus().getIndex();
		
		_nGens = _gens.size();
		_PfactorsP = new float[_nGens];
		_PfactorsQ = new float[_nGens];
		
		_PgOrig = _gens.getP().clone();
		_QgOrig = _gens.getQ().clone();
		
		_PgenLoss = _gens.get(ndxGenCont).getP();
		_QgenLoss = _gens.get(ndxGenCont).getQ();
		checkGenBusType();
	}
	
	boolean[] getIsGenInArea() throws PsseModelException
	{
		return _model.getAreaDataGenReDisptch().getIsGenInArea();
	}
	
	void checkGenBusType() throws PsseModelException
	{
		int[] pv = _model.getBusNdxForType(BusTypeCode.Gen);
		int[] slack = _model.getBusNdxForType(BusTypeCode.Slack);
		//int[] pq = _model.getBusNdxForType(BusTypeCode.Load);
		assert(slack.length == 1);
		_markSlack = false;
		_markPV = false;
		for (int i=0; i<slack.length; i++)
		{
			if (_ndxBusGenCont == slack[i])
			{
				_markSlack = true;
				_OrigSlackBuses= slack;
				break;
			}
		}
		if (_markSlack)
		{
			_markStillSlack = false;
			_markSlacktoPV = false;
			_markSlacktoPQ = false;
			float Pmax = -1;
			int ndxBusGenPmax = -1;
			boolean markSlacktoPV = false;
			for (Gen g : _gens)
			{
				if (g.isInSvc())
				{
					Bus bus = g.getBus();
					BusTypeCode btc = bus.getBusType();
					if (btc == BusTypeCode.Gen || btc == BusTypeCode.Slack)
					{
						int idx = bus.getIndex();
						float PGmax = g.getPT();
						if (Pmax < PGmax)
						{
							Pmax = PGmax;
							ndxBusGenPmax = idx;
							_ndxBusGenPmax = ndxBusGenPmax;
						}
						if (markSlacktoPV == false)
						{
							if (idx == _ndxBusGenCont)
							{
								markSlacktoPV = true;
							}
						}
					}
					else if (bus.getBusType() != BusTypeCode.Load) 
					{
						System.out.println("Unknown Bus type for Gen.");
						System.out.println("index of Gen: "+g.getIndex());
					}
				}
			}
			if (ndxBusGenPmax == _ndxBusGenCont) _markStillSlack = true;
			else
			{
				if (markSlacktoPV == true)
				{
					_markSlacktoPV = true;
					_OrigPVBuses = _model.getBusNdxForType(BusTypeCode.Gen);
				}
				if (_markSlacktoPV == false)
				{
					_markSlacktoPQ = true;
					_OrigPVBuses = _model.getBusNdxForType(BusTypeCode.Gen);
					_OrigPQBuses = _model.getBusNdxForType(BusTypeCode.Load);
				}
			}
		}
		else
		{
			_markStillPV = false;
			_markPVtoPQ = false;
			for (int i=0; i<pv.length; i++)
			{
				if (_ndxBusGenCont == pv[i])
				{
					_markPV = true;
				}
			}
			if (_markPV)
			{
				for (Gen g : _gens)
				{
					if (g.isInSvc())
					{
						Bus bus = g.getBus();
						BusTypeCode btc = bus.getBusType();
						if (btc == BusTypeCode.Gen)
						{
							if (bus.getIndex() == _ndxBusGenCont)
							{
								_markStillPV = true;
								break;
							}
						}
					}
				}
				if (_markStillPV == false)
				{
					_OrigPVBuses = pv;
					_OrigPQBuses = _model.getBusNdxForType(BusTypeCode.Load);
				}
			}
		}
	}
	
	/** Create generators participation factors based on capacity */
	void createPfactorsCapa() throws PsseModelException
	{
		float sumPmax = 0f;
		float sumQmax = 0f;
		_numGensOn = 0;
		boolean[] isGenInArea = null;
		if (_checkArea == true) isGenInArea = getIsGenInArea();
		for (int i=0; i<_nGens; i++)
		{
			if (_checkArea == true && isGenInArea[i] == false) continue; 
			Gen gen = _gens.get(i);
			if (gen.isInSvc())
			{
				_PfactorsP[i] = gen.getPT();
				_PfactorsQ[i] = gen.getQT();
				sumPmax = sumPmax + _PfactorsP[i];
				sumQmax = sumQmax + _PfactorsQ[i];
				_numGensOn += 1;
			}
		}
		for (int i=0; i<_nGens; i++)
		{
			_PfactorsP[i] = _PfactorsP[i] / sumPmax;
			_PfactorsQ[i] = _PfactorsQ[i] / sumQmax;
		}
	}
	
	/** Create generators participation factors based on available reserve */
	public void createPfactorsRes() throws PsseModelException
	{
		float sumPmax = 0f;
		float sumQmax = 0f;
		_numGensOn = 0;
		boolean[] isGenInArea = null;
		if (_checkArea == true) isGenInArea = getIsGenInArea();
		for (int i=0; i<_nGens; i++)
		{
			if (_checkArea == true && isGenInArea[i] == false) continue; 
			Gen gen = _gens.get(i);
			if (gen.isInSvc())
			{
//				Bus bus = gen.getBus();
//				BusTypeCode btc = bus.getBusType();
				//TODO: why bus-type is considered when re-dispatch Pg.   May be No. 
				//TODO: should Qg be re-dispatched? 
//				if (btc == BusTypeCode.Gen) // || btc == BusTypeCode.Slack)
				{
					_PfactorsP[i] = gen.getPT() - gen.getP();
					float qg = gen.getQ();
					if (qg > 0)	_PfactorsQ[i] = gen.getQT() - qg;
					else _PfactorsQ[i] = qg - gen.getQT();
					sumPmax = sumPmax + _PfactorsP[i];
					sumQmax = sumQmax + _PfactorsQ[i];
					_numGensOn += 1;
				}
			}
		}
		for (int i=0; i<_nGens; i++)
		{
			_PfactorsP[i] = _PfactorsP[i] / sumPmax;
			_PfactorsQ[i] = _PfactorsQ[i] / sumQmax;
		}		
	}
	
	/** Create generators participation factors based on generators' inertia (Param H) */
	void createPfactorsH(float[] inertiaH) throws PsseModelException
	{
		float sumPmax = 0f;
		float sumQmax = 0f;
		_numGensOn = 0;
		for (int i=0; i<_nGens; i++)
		{
			//TODO: For inertia based re-dispatch, should area info be considered?   May be No.
//			if (_checkArea == true && _isGenInArea[i] == false) continue; 
			Gen gen = _gens.get(i);
			if (gen.isInSvc())
			{
				_PfactorsP[i] = inertiaH[i];
				_PfactorsQ[i] = inertiaH[i];
				sumPmax = sumPmax + _PfactorsP[i];
				sumQmax = sumQmax + _PfactorsQ[i];
				_numGensOn += 1;
			}
		}
		for (int i=0; i<_nGens; i++)
		{
			_PfactorsP[i] = _PfactorsP[i] / sumPmax;
			_PfactorsQ[i] = _PfactorsQ[i] / sumQmax;
		}
	}
	
	public void launch() throws PsseModelException
	{
		setNewBusType();
		redispatch();
		checkPfactors();
	}
	
	private void setNewBusType() throws PsseModelException
	{
		if (_markSlack)
		{
			if (_markStillSlack == false)
			{
				if (_markSlacktoPV == true)	SlacktoPVbus();
				else if (_markSlacktoPQ == true) SlacktoPQbus();
				else
				{
					System.out.println(" Method checkGenBusType() does not work properly.");
				}
			}
		}
		else if (_markPV)
		{
			if (_markStillPV == false) PVtoPQbus();
		}
	}
	
	private void SlacktoPVbus() throws PsseModelException
	{
		assert(_markSlacktoPV);
		setNewSlackbus(_ndxBusGenPmax);
		int[] newPV = delPVbus(_ndxBusGenPmax);
		addPVbus(newPV, _ndxBusGenCont);
	}
	
	private void SlacktoPQbus() throws PsseModelException
	{
		assert(_markSlacktoPQ);
		setNewSlackbus(_ndxBusGenPmax);
		delPVbus(_ndxBusGenPmax);
		addPQbus(_ndxBusGenCont);
	}
	
	private void PVtoPQbus() throws PsseModelException
	{
		delPVbus(_ndxBusGenCont);
		addPQbus(_ndxBusGenCont);
	}
	
	private void setNewSlackbus(int slack) throws PsseModelException
	{
		int[] newSlack = {slack};
		assert(slack != _ndxBusGenCont);
		_model.setBusNdxForTypeSlack(newSlack);
	}
	
	/** param: ndxDel is the index of bus that is moved from PV bus list */
	@Test
	private int[] delPVbus(int ndxDel) throws PsseModelException
	{
		int idx = -1;
		for (int i=0; i<_OrigPVBuses.length; i++)
		{
			if (_OrigPVBuses[i] == ndxDel)
			{
				idx = i;
				break;
			}
		}
		if (idx == -1)
		{
			System.out.println("Something wrong when calculate the new PV buses when _markStilltoPV is true.");
		}
		assertTrue(idx != -1);
		int newPVlength = _OrigPVBuses.length-1;
		int[] newPV = new int[newPVlength];
		System.arraycopy(_OrigPVBuses, 0, newPV, 0, idx);
		int tmp = newPVlength - idx;
		int srcPos = idx + 1;
		System.arraycopy(_OrigPVBuses, srcPos, newPV, idx, tmp);
		_model.setBusNdxForTypeGen(newPV);
		return newPV;
	}
	
	public int[] delPVbus(int[] OrigPVBuses,int ndxDel) throws PsseModelException
	{
		int idx = -1;
		for (int i=0; i<OrigPVBuses.length; i++)
		{
			if (OrigPVBuses[i] == ndxDel)
			{
				idx = i;
				break;
			}
		}
		if (idx == -1)
		{
			System.out.println("Something wrong when calculate the new PV buses when _markStilltoPV is true.");
		}
		assertTrue(idx != -1);
		int newPVlength = OrigPVBuses.length-1;
		int[] newPV = new int[newPVlength];
		System.arraycopy(OrigPVBuses, 0, newPV, 0, idx);
		int tmp = newPVlength - idx;
		int srcPos = idx + 1;
		System.arraycopy(OrigPVBuses, srcPos, newPV, idx, tmp);
		_model.setBusNdxForTypeGen(newPV);
		return newPV;
	}

	
	private void addPVbus(int[] PVBuses, int ndxBus) throws PsseModelException
	{
		int newPVlength = PVBuses.length + 1;
		int[] newPV = new int[newPVlength];
		Arrays.fill(newPV, -1);
		int ndxGencontAtPVBuses = newPVlength - 1;
		for (int i=0; i<PVBuses.length; i++)
		{
			if (ndxBus < PVBuses[i])
			{
				ndxGencontAtPVBuses = i;
				break;
			}
		}
		System.arraycopy(PVBuses, 0, newPV, 0, ndxGencontAtPVBuses);
		newPV[ndxGencontAtPVBuses] = ndxBus;
		int tmp = newPVlength - ndxGencontAtPVBuses - 1;
		int desPos = ndxGencontAtPVBuses + 1;
		System.arraycopy(PVBuses, ndxGencontAtPVBuses, newPV, desPos, tmp);
	}
	
	public int[] addPQbus(int[] OrigPQBuses, int ndxBus) throws PsseModelException
	{
		int newPQlength = OrigPQBuses.length + 1;
		int[] newPQ = new int[newPQlength];
		Arrays.fill(newPQ, -1);
		int ndxGencontAtPQBuses = newPQlength - 1;
		for (int i=0; i<OrigPQBuses.length; i++)
		{
			if (ndxBus < OrigPQBuses[i])
			{
				ndxGencontAtPQBuses = i;
				break;
			}
		}
		System.arraycopy(OrigPQBuses, 0, newPQ, 0, ndxGencontAtPQBuses);
		newPQ[ndxGencontAtPQBuses] = ndxBus;
		int tmp = newPQlength - ndxGencontAtPQBuses - 1;
		int desPos = ndxGencontAtPQBuses + 1;
		System.arraycopy(OrigPQBuses, ndxGencontAtPQBuses, newPQ, desPos, tmp);
		return newPQ;
	}
	
	private void addPQbus(int ndxBus) throws PsseModelException
	{
		int newPQlength = _OrigPQBuses.length + 1;
		int[] newPQ = new int[newPQlength];
		Arrays.fill(newPQ, -1);
		int ndxGencontAtPQBuses = newPQlength - 1;
		for (int i=0; i<_OrigPQBuses.length; i++)
		{
			if (ndxBus < _OrigPQBuses[i])
			{
				ndxGencontAtPQBuses = i;
				break;
			}
		}
		System.arraycopy(_OrigPQBuses, 0, newPQ, 0, ndxGencontAtPQBuses);
		newPQ[ndxGencontAtPQBuses] = ndxBus;
		int tmp = newPQlength - ndxGencontAtPQBuses - 1;
		int desPos = ndxGencontAtPQBuses + 1;
		System.arraycopy(_OrigPQBuses, ndxGencontAtPQBuses, newPQ, desPos, tmp);
		_model.setBusNdxForTypeLoad(newPQ);
	}

	
	private void redispatch() throws PsseModelException
	{
		boolean[] isGenInArea = null;
		if (_checkArea == true) isGenInArea = getIsGenInArea();
		for (int i=0; i<_nGens; i++)
		{
			if (_checkArea == true && isGenInArea[i] == false) continue; 
			Gen gen = _gens.get(i);
			if (gen.isInSvc())
			{
//				_PgOrig[i] = gen.getP();
				float redisP = _PgOrig[i] + _PfactorsP[i]*_PgenLoss;
				gen.setP(redisP);

				if (_isQgAdj == true)
				{
//					_QgOrig[i] = gen.getQ();
					float redisQ = _QgOrig[i] + _PfactorsQ[i]*_QgenLoss;
					gen.setQ(redisQ);
				}
			}
		}
	}
	
	/** method refresh() must be invoked immediately after each generation contingency analysis.
	 * it should be invoked before the generator that has a contingency is back in-servie.
	 * @throws PsseModelException */
	public void refresh() throws PsseModelException
	{
//		boolean[] isGenInArea = null;
//		if (_checkArea == true) isGenInArea = getIsGenInArea();
//		for (int i=0; i<_nGens; i++)
//		{
//			if (_checkArea == true && isGenInArea[i] == false) continue; 
//			Gen gen = _gens.get(i);
//			if (gen.isInSvc())
//			{
//				gen.setP(_PgOrig[i]);
//				gen.setQ(_QgOrig[i]);
//			}
//		}
		
		_gens.setP(_PgOrig);
		_gens.setQ(_QgOrig);
		
		if (_markSlack)
		{
			if (_markStillSlack == false)
			{
				_model.setBusNdxForTypeSlack(_OrigSlackBuses);
				_model.setBusNdxForTypeGen(_OrigPVBuses);
				if (_markSlacktoPQ)
				{
					_model.setBusNdxForTypeLoad(_OrigPQBuses);
				}
			}
		}
		else if(_markPV)
		{
			if (_markStillPV == false)
			{
				_model.setBusNdxForTypeGen(_OrigPVBuses);
				_model.setBusNdxForTypeLoad(_OrigPQBuses);
			}
		}
	}
	
	boolean checkPfactors()
	{
		boolean isCorrect = true;
		float sumP = 0;
		float sumQ = 0;
		for (int i=0; i<_PfactorsP.length; i++)
		{
			sumP += _PfactorsP[i];
			sumQ += _PfactorsQ[i];
		}
		if (Math.abs(sumP - 1) >= 0.001) isCorrect = false;
		if (Math.abs(sumQ - 1) >= 0.001) isCorrect = false;
		if (!isCorrect)
		{
			System.err.println("Something wrong when calculate the Pfactors.");
			System.err.println("sumP: "+sumP);
			System.err.println("sumQ: "+sumQ);
		}
		return isCorrect;
	}
	
	public float[] getPfactorsP() {return _PfactorsP;}
	public float[] getPfactorsQ() {return _PfactorsQ;}
	
	public float[] getPgOrig() {return _PgOrig;}
	public float[] getQgOrig() {return _QgOrig;}
	
	public void setCheckArea(boolean mark) {_checkArea = mark;}
//	public void setIsGenInArea(boolean[] marks) {_isGenInArea = marks;}
	

}
