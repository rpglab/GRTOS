package com.sced.model.data.base;

import com.sced.model.SystemModelXL;

public class BaseListXL {

	protected SystemModelXL _model;
	protected int _size;
	protected boolean[] _st;

	public BaseListXL(SystemModelXL model) {_model = model;}
	
	/** Get list size */
	public int size() {return _size;}
	/** Is a device in service */
	public boolean isInSvc(int i) {return _st[i];}
	
	public boolean[] getIsInSvc() {return _st;}
	
	public int sizeInSvc() {
		int num = 0;
		for (int i=0; i<_size; i++) {
			if (_st[i] == true) num++;
		}
		return num;
	}
}
