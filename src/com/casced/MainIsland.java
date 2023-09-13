package com.casced;

import com.powerdata.openpa.psse.Island;
import com.powerdata.openpa.psse.IslandList;
import com.powerdata.openpa.psse.PsseModel;
import com.powerdata.openpa.psse.PsseModelException;

public class MainIsland {
	
	PsseModel _model;
	int _idxMainIsland;
	int _nbusMI; // number of buses in the main island.
	
	public MainIsland(PsseModel model) throws PsseModelException 
	{
		_model = model;
		initial();
	}

	void initial() throws PsseModelException
	{
		IslandList islands = _model.getIslands();
		int idxMainIsland = -1;
		int nbusMI = 0;
		for (Island island : islands) {
			if (island.isEnergized() == false) continue;
			int nbus = island.getBuses().size();
			if (nbusMI < nbus) {
				nbusMI = nbus;
				idxMainIsland = island.getIndex();
			}
		}
		_idxMainIsland = idxMainIsland;
		_nbusMI = nbusMI;
	}

	public Island getMainIsland() throws PsseModelException  
	{
		return _model.getIslands().get(_idxMainIsland);
	}
	
	public int getIdxMainIsland() {return _idxMainIsland;}
	
}
