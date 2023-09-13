package com.rtca_cts.ausData;

import com.powerdata.openpa.psse.Gen;
import com.powerdata.openpa.psse.GenList;
import com.powerdata.openpa.psse.PsseModel;
import com.powerdata.openpa.psse.PsseModelException;

public class CheckModelData {
	
	PsseModel _model;
	BusGroupElems _busGroupElems;
	
	float _PgMaxLimit = 5000; // in MW. 
	
	
	public CheckModelData(PsseModel model) throws PsseModelException
	{
		_model = model;
		_busGroupElems = _model.getBusGroupElems();
	}

	public void launchDataCheck() throws PsseModelException
	{
		checkGenData();
	}
	
	public void checkPgMaxData() throws PsseModelException
	{
		GenList gens = _model.getGenerators();
		for (Gen g : gens)
		{
			float PgMax = g.getPT();
			if (PgMax > _PgMaxLimit) g.setPT(1.1f*g.getP());
		}
	}

	
	public void checkGenData() throws PsseModelException
	{
		//TODO: Only PgMax is checked.
		GenList gens = _model.getGenerators();
//		float rateUsedTol = ParamVio.getRateUsedTol();
		float[] rateUsed = _model.getACBrcCapData().getRateA();
		for (Gen g : gens)
		{
			float PgMax = g.getPT();
			if (PgMax > _PgMaxLimit)
			{
				int idxBus = g.getBus().getIndex();
				int[] idxBrcs = _busGroupElems.getBrcIndex(idxBus);
				float sumRate = 0;
				for (int idxBrc : idxBrcs)
					sumRate =+ rateUsed[idxBrc];
				if (sumRate > _PgMaxLimit) sumRate = _PgMaxLimit;
				if (sumRate < g.getP()) g.setPT(sumRate);
				// the following trick is necessary in case branch capacity is not realistic.
				float limitP = g.getP()*10;
				if (sumRate > limitP) g.setPT(limitP);
			}
		}
	}
	
	
}
