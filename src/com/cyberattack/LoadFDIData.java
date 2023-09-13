package com.cyberattack;

import java.io.IOException;

import com.sced.util.AuxStringXL;

public class LoadFDIData {

	String _path2Case;
	String _loadFileName = "bus.csv";
	String _genFileName = "generator.csv";
	String _branchFileName = "branch.csv";
	
	CyberAttackDataCenter _dataCenter;

	public LoadFDIData(String path2Case) {_path2Case = path2Case;}
	
	public void setLoadFileName(String loadFileName) {_loadFileName = loadFileName;}
	public void setGenFileName(String genFileName) {_genFileName = genFileName;}
	public void setBrcFileName(String brcFileName) {_branchFileName = brcFileName;}
	
	
	public void loadData() throws IOException
	{
		_dataCenter = new CyberAttackDataCenter();
		
		String path2Load = AuxStringXL.endWith(_path2Case, "/") +  _loadFileName; 
		_dataCenter.getCyberAttackLoad().readLoadData(path2Load);

		String path2Gen = AuxStringXL.endWith(_path2Case, "/") +  _genFileName; 
		_dataCenter.getCyberAttackGen().readGenData(path2Gen);

		String path2Brc = AuxStringXL.endWith(_path2Case, "/") +  _branchFileName; 
		_dataCenter.getCyberAttackBranch().readBranchData(path2Brc);
	}
	
	public CyberAttackDataCenter getFDIDataModel() {return _dataCenter;}
	
	
}
