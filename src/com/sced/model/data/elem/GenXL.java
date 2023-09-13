package com.sced.model.data.elem;

import com.sced.model.data.GenListXL;
import com.sced.model.data.base.OneTermDevXL;

public class GenXL implements OneTermDevXL {

	int _idx;
	GenListXL _gens;
	
	public GenXL (GenListXL gens, int idx) {_gens = gens; _idx = idx;}
	
	public int getBusIdx() {return _gens.getBusIdx(_idx);}
	public boolean isInSvc() {return _gens.isInSvc(_idx);}

}
