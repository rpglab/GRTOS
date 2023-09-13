package com.casced;

import com.sced.gurobi.GRB_SCEDXL;

public class UpdateSCEDConstraintsGRB {

	GRB_SCEDXL _grbSolver;
	
	public UpdateSCEDConstraintsGRB(GRB_SCEDXL grbSolver) {_grbSolver = grbSolver;}
	
	public void launchUpdate(Results4ReDispatch origResults, Results4ReDispatch newResults) {
		//TODO: add constraint into GRB SCED model
	}
	
}
