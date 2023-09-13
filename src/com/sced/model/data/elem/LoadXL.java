package com.sced.model.data.elem;

import com.sced.model.data.LoadListXL;
import com.sced.model.data.base.OneTermDevXL;

public class LoadXL implements OneTermDevXL {

	int _idx;
	LoadListXL _loads;
	
	public LoadXL (LoadListXL loads, int idx) {_loads = loads; _idx = idx;}

	public int getBusIdx() {return _loads.getBusIdx(_idx);}
	public boolean isInSvc() {return _loads.isInSvc(_idx);}

}
