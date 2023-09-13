package com.rtca_cts.ausData;

import java.util.ArrayList;

import com.powerdata.openpa.psse.ACBranchList;
import com.powerdata.openpa.psse.BusList;
import com.powerdata.openpa.psse.GenList;
import com.powerdata.openpa.psse.PsseModel;
import com.powerdata.openpa.psse.PsseModelException;
import com.utilxl.array.AuxArrayListXL;
import com.utilxl.array.AuxArrayXL;

/**
 * 
 * @author Xingpeng.Li (xplipower@gmail.com)
 *
 */
public class AreaData {

	PsseModel _model;
	int[] _areaList;
	ArrayList<Integer> _areas;        // Areas which are concerned.
	int _numGenInArea = -1;
	int _numBusInArea = -1;
	int _numBrcInArea = -1;         // If any bus that is in one of those areas, the branch is considered as an element in the areas.
	int _numBrcTwoEndsInArea = -1;

	boolean[] _isGenInArea = null;
	boolean[] _isBusInArea = null;
	boolean[] _isBrcInArea = null;   // If any bus that is in one of those areas, the branch is considered as an element in the areas.
	boolean[] _isBrcTwoEndsInArea = null;   // branch is considered as an element in the areas only when both end-buses have to be one of those areas.
	
	 
	public AreaData(PsseModel model, int[] areas)
	{
		_model = model;
		_areaList = areas;
		_areas = AuxArrayXL.toArrayList(areas);
	}
	
	public AreaData(PsseModel model, ArrayList<Integer> areas)
	{
		_model = model;
		_areas = areas;
	}
	
	public boolean[] getIsGenInArea() throws PsseModelException
	{
		if (_isGenInArea == null) genToArea();
		return _isGenInArea;
	}
	
	public boolean[] getIsBusInArea() throws PsseModelException
	{
		if (_isBusInArea == null) busToArea();
		return _isBusInArea;
	}
	
	public boolean[] getIsBranchInArea() throws PsseModelException
	{
		if (_isBrcInArea == null) brcToArea();
		return _isBrcInArea;
	}

	public boolean[] getIsBrcTwoEndsInArea() throws PsseModelException
	{
		if (_isBrcTwoEndsInArea == null) brcTwoEndsToArea();
		return _isBrcTwoEndsInArea;
	}

	public int getNumBusInArea() throws PsseModelException 
	{
		if (_isBusInArea == null) busToArea();
		return _numBusInArea;
	}

	public int getNumBrcInArea() throws PsseModelException 
	{
		if (_isBrcInArea == null) brcToArea();
		return _numBusInArea;
	}

	public int getNumBrcTwoEndsInArea() throws PsseModelException 
	{
		if (_isBrcTwoEndsInArea == null) brcTwoEndsToArea();
		return _numBusInArea;
	}

	void genToArea() throws PsseModelException
	{
		if (_isBusInArea == null) busToArea();
		_numGenInArea = 0;
		GenList gens = _model.getGenerators();
		_isGenInArea = new boolean[gens.size()];
		for (int i=0; i<gens.size(); i++)
		{
			int idxBus = gens.getBus(i).getIndex();
			if (_isBusInArea[idxBus] == true) {_isGenInArea[i] = true; _numGenInArea++;}
//			else _isGenInArea[i] = false;
		}
	}
	
	void busToArea() throws PsseModelException
	{
		_numBusInArea = 0;
		BusList buses = _model.getBuses();
		_isBusInArea = new boolean[buses.size()];
		for (int i=0; i<buses.size(); i++)
		{
			int idxArea = buses.getAREA(i);
			if (_areas.contains(idxArea) == true) { _isBusInArea[i] = true; _numBusInArea++;}
//			else _isBusInArea[i] = false;
		}
	}
	
	void brcToArea() throws PsseModelException
	{
		_numBrcInArea = 0;
		ACBranchList branches = _model.getBranches();
		_isBrcInArea = new boolean[branches.size()];
		for (int i=0; i<branches.size(); i++)
		{
			int idxAreaOfFrmBus = branches.getFromBus(i).getAREA();
			if (_areas.contains(idxAreaOfFrmBus) == true) { _isBrcInArea[i] = true; _numBrcInArea++; continue;}
			int idxAreaOfToBus = branches.getToBus(i).getAREA();
			if (_areas.contains(idxAreaOfToBus) == true) {_isBrcInArea[i] = true; _numBrcInArea++;}
			else _isBrcInArea[i] = false;
		}
	}
	
	void brcTwoEndsToArea() throws PsseModelException
	{
		_numBrcTwoEndsInArea = 0;
		ACBranchList branches = _model.getBranches();
		_isBrcTwoEndsInArea = new boolean[branches.size()];
		for (int i=0; i<branches.size(); i++)
		{
			_isBrcTwoEndsInArea[i] = true;
			int idxAreaOfFrmBus = branches.getFromBus(i).getAREA();
			if (_areas.contains(idxAreaOfFrmBus) == false) { _isBrcTwoEndsInArea[i] = false; continue;}
			int idxAreaOfToBus = branches.getToBus(i).getAREA();
			if (_areas.contains(idxAreaOfToBus) == false) { _isBrcTwoEndsInArea[i] = false; continue;}
			 _numBrcTwoEndsInArea++;
		}
	}
	
	public int[] getAreaList() 
	{
		if (_areaList == null) _areaList = AuxArrayListXL.toIntArray(_areas);
		return _areaList;
	}
	
	public void setIsBusInArea(boolean[] tmp) throws PsseModelException {_isBusInArea = tmp;}
	public void setAreaList(int[] areas)
	{
		setAreaList(AuxArrayXL.toArrayList(areas));
	}
	public void setAreaList(ArrayList<Integer> areas)
	{
		_areas = areas;
		resetData();
	}
	
	void resetData()
	{
		_numGenInArea = -1;
		_numBusInArea = -1;
		_numBrcInArea = -1;         // If any bus that is in one of those areas, the branch is considered as an element in the areas.
		_numBrcTwoEndsInArea = -1;

		_isGenInArea = null;
		_isBusInArea = null;
		_isBrcInArea = null;   // If any bus that is in one of those areas, the branch is considered as an element in the areas.
		_isBrcTwoEndsInArea = null;   // branch is considered as an element in the areas only when both end-buses have to be one of those areas.
	}
	
}
