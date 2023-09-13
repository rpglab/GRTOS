package com.sced.model.data.base;

import com.sced.model.SystemModelXL;

public class OneTermDevList extends BaseListXL {
	
	protected int[] _busIdx;
	
	public OneTermDevList(SystemModelXL model) {super(model);}

	public void setBusIdx(int idx, int busIdx) {_busIdx[idx] = busIdx;}
	public int getBusIdx(int i) {return _busIdx[i];}
	
//	public OneTermDev get(int i) {return this.get(i);}
	
}
