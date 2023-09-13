package com.sced.model.data.base;

import com.sced.model.SystemModelXL;

public class TwoTermDevListXL extends BaseListXL {

	
	protected int[] _frmBusIdx;
	protected int[] _toBusIdx;
	protected String[] _id;
	
	public TwoTermDevListXL(SystemModelXL model) {super(model);}

	/** Get from-bus index */
	public int getFrmBusIdx(int i) {return _frmBusIdx[i];}
	/** Get to-bus index */
	public int getToBusIdx(int i) {return _toBusIdx[i];}
	/** Get circuit ID */
	public String getID(int i) {return _id[i];}
	
	/** Get from-bus index */
	public int[] getFrmBusIdx() {return _frmBusIdx;}
	/** Get to-bus index */
	public int[] getToBusIdx() {return _toBusIdx;}

}
