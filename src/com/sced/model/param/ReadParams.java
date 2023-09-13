package com.sced.model.param;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author Xingpeng Li, xplipower@gmail.com 
 *
 */
public class ReadParams {

	String _inputFile = "CpsConfg.ini";
	Map<String, String> _mapIni;
	
	public ReadParams() {}
	
	public void launch()
	{
		_mapIni = new HashMap<String, String>();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(_inputFile));
			String line = null;
			while ((line = br.readLine()) != null) {
				line = line.trim();
				if (line.length() == 0) continue;   // skip empty line
				if (line.startsWith("//") == true) continue; // skip comment line starting with "\\"
				if (line.startsWith("#") == true) continue; // skip comment line starting with "#"
				int idxRemove = line.indexOf("//");
				if (idxRemove != -1) line = line.substring(0, idxRemove);
				idxRemove = line.indexOf("#");
				if (idxRemove != -1) line = line.substring(0, idxRemove);
				
				int idx = line.indexOf("=");
				if (idx == -1) System.err.println("Could NOT parse line: " + line);
				else {
					String key = line.substring(0, idx).trim();
					String value = line.substring(idx+1).trim();
					_mapIni.put(key, value);
				}
			}
			br.close();
		} catch (IOException e) {
			System.out.println("Error: " + e);
			e.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		applyOuterParams();
		System.out.println("Finish loading parameters defined in " + _inputFile);
	}
	
	private void applyOuterParams()
	{
		for(Map.Entry<String, String> entry : _mapIni.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			loadParam(key, value);
		}
	}
	
	/** If more input parameters from MOD33Confg.ini file are needed,
	 * they need to be parsed in this method. */
	private void loadParam(String key, String value)
	{
		switch (key) 
		{
			/* Additional generator data */
			case "openPAGenCostFilePath":
				ParamInput.setOpenPAGenCostFilePath(value); 
				break;
			case "openPAGenRampFilePath":
				ParamInput.setOpenPAGenRampFilePath(value); 
				break;
			
			/* base case */
			case "tol4BrcFlowMonitorBaseCase":
				ParamInput.setTol4BrcFlowMonitorBaseCase(Float.parseFloat(value)); 
				break;
			case "tol4BrcFlowWarningBaseCase":
				ParamInput.setTol4BrcFlowWarningBaseCase(Float.parseFloat(value)); 
				break;
			case "tol4BrcFlowVioBaseCase":
				ParamInput.setTol4BrcFlowVioBaseCase(Float.parseFloat(value)); 
				break;

			
			/* contingency case */
			case "tol4BrcFlowMonitorCtgcyCase":
				ParamInput.setTol4BrcFlowMonitorCtgcyCase(Float.parseFloat(value)); 
				break;
			case "tol4BrcFlowWarningCtgcyCase":
				ParamInput.setTol4BrcFlowWarningCtgcyCase(Float.parseFloat(value)); 
				break;
			case "tol4BrcFlowVioCtgcyCase":
				ParamInput.setTol4BrcFlowVioCtgcyCase(Float.parseFloat(value)); 
				break;
		
			/* Model setting */
			case "useMainIsland":
				ParamInput.setUseMainIsland(parseLogicValue(key, value)); 
				break;
				
			/* Other setting */
			case "ratio4B":
				ParamInput.setRatio4B(Float.parseFloat(value)); 
				break;
			case "ratio4C":
				ParamInput.setRatio4C(Float.parseFloat(value)); 
				break;
			case "optionLoss":
				ParamInput.setOptionLoss(Integer.parseInt(value)); 
				break;

			/* Power flow setting */
			case "leastX":
				ParamInput.setLeastX(Float.parseFloat(value)); 
				break;
			case "decoupleImpedance":
				ParamInput.setDecoupleImpedance(parseLogicValue(key, value)); 
				break;
			case "setUnitRegOverride":
				ParamInput.setUnitRegOverride(parseLogicValue(key, value)); 
				break;
			
			/* SCED setting */
			case "simAllPotentialCtgcy":
				ParamInput.setSimAllPotentialCtgcy(parseLogicValue(key, value)); 
				break;
			case "monALLBrc4SimAllPotentialCtgcy":
				ParamInput.setMonALLBrc4SimAllPotentialCtgcy(parseLogicValue(key, value)); 
				break;
			case "monitorAllBrcBaseCase":
				ParamInput.setMonitorAllBrcBaseCase(parseLogicValue(key, value)); 
				break;
			case "monitorAllBrcCtgcyCase":
				ParamInput.setMonitorAllBrcCtgcyCasey(parseLogicValue(key, value)); 
				break;
			case "usePTDFforSCED":
				ParamInput.setUsePTDFforSCED(parseLogicValue(key, value)); 
				break;
			case "ptdfCutOffValue":
				ParamInput.setPTDFCutOffValue(Double.parseDouble(value)); 
				break;
			case "usePkInit":
				ParamInput.setUsePkInit(parseLogicValue(key, value)); 
				break;
			case "usePkcInit":
				ParamInput.setUsePkcInit(parseLogicValue(key, value)); 
				break;
			case "gurobiLogFileName":
				ParamInput.setGurobiLogFileName(value); 
				break;
			
			default :
				System.err.println("Parameter for " + key + " can not be identified: " + value);
//				System.exit(1);
				break;
		}
	}
	
	private boolean parseLogicValue(String key, String value)
	{
		String valueLow = value.toLowerCase();
		if (valueLow.equals("true")) return true;
		else if (valueLow.equals("false")) return false;
		else {
			System.err.println("Parameter for " + key + " can not be identified: " + value);
			return false;
		}
	}
	
	public String getConfigureFileName() {return _inputFile;}
	
}
