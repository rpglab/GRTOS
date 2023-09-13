package com.rtca_cts.contingencyanalysis;

import com.powerdata.openpa.psse.Bus;
import com.powerdata.openpa.psse.BusTypeCode;
import com.powerdata.openpa.psse.Gen;
import com.powerdata.openpa.psse.PsseModel;
import com.powerdata.openpa.psse.PsseModelException;

/**
 * This class is only used for TVA test cases. 
 * 
 * @author Xingpeng.Li
 *
 */
public class FictitiousGen {
	
	PsseModel _model;
	int[] _idxGen;      // index of fictitious generators
	SetBusType _setBusType;
	
	public FictitiousGen(PsseModel model) throws PsseModelException
	{
		_model = model;
		_setBusType = new SetBusType(model);
		fixFictitiousGen();
	}
	
	public void fixFictitiousGen() throws PsseModelException
	{
		for(Gen g : _model.getGenerators())
		{
			if (g.isInSvc())
			{
				Bus b = g.getBus();
				int ndxBus = b.getIndex();
				//TODO : Only work for TVA system
				if (ndxBus < 1000)        
				{
					BusTypeCode btc = b.getBusType();
					if (btc == BusTypeCode.Gen) // || btc == BusTypeCode.Slack)
					{
						_setBusType.addPQBus(ndxBus);
						_setBusType.remPVBus(ndxBus);
					}
				}
			}		
		}
	}

}
