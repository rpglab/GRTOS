package com.rtca_cts.ausXP.EDM;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import com.powerdata.openpa.psse.PsseModelException;
import com.utilxl.array.AuxArrayXL;
import com.utilxl.iofiles.ReadMultiCollumFloatData;

/**
 * Initialized in Mar. 2016.
 * Data preparation for EDM simulation.
 * 
 * @author Xingpeng.Li (xplipower@gmail.com)
 *
 */
public class PreDataProcessing {

	HashMap<Integer, int[]> _mapCtgcyToTSList;
//	String _ctgcyType = "Branch";
	String _endMark = "-6";
	
//	public PreDataProcessing(String ctgcyType) {_ctgcyType = ctgcyType;}
	
//	int[] _hours; // hours of concern
	
	ArrayList<Integer> _idxCtgcy = new ArrayList<Integer>(); //  index of Contingency which has at least a beneficial CTS solution (beyond _toc)
	ArrayList<Integer> _numHours = new ArrayList<Integer>(); //  # of hours that contains the same contingency
	float _toc; // 10% as the tolerance to filter out minor CTS solutions.
	ArrayList<Integer> _numHoursCTS = new ArrayList<Integer>(); // # of hours that contains the same contingency and at least a CTS solution can provide _toc improvement
	ArrayList<ArrayList<Integer>> _idxCTS = new ArrayList<ArrayList<Integer>>(); // CTS solution that provides improvement more than _toc
	ArrayList<ArrayList<Integer>> _idxHour = new ArrayList<ArrayList<Integer>>();
	ArrayList<ArrayList<Float>> _improvement = new ArrayList<ArrayList<Float>>();
	
	ArrayList<Integer> _idxCtgcy_NoImp = new ArrayList<Integer>(); // index of Contingency which does have a beneficial CTS solution (beyond _toc) 
	ArrayList<Integer> _numHours_NoImp = new ArrayList<Integer>(); //  # of hours that contains the same contingency

	int[] _idxCtgcy_raw;
	int[] _idxHours_raw;
	int[] _idxCTS_raw;
	float[] _impPctg_raw;
	float[] _origFlowVio;
	float[] _origVmVio;
	
	public void process() {
		int size = _idxCtgcy_raw.length;
		for (int i=0; i<size; i++) {
			if (_impPctg_raw[i] <= _toc) continue;
			int idxTarget = -1;
			if (_idxCtgcy.contains(_idxCtgcy_raw[i]) == false) {
				_idxCtgcy.add(_idxCtgcy_raw[i]);
				idxTarget = _idxCtgcy.size() - 1;
//				_numHours.add(1);
				_numHoursCTS.add(1);
				
				ArrayList<Integer> idxCTS_tmp = new ArrayList<>(Arrays.asList(_idxCTS_raw[i]));
				ArrayList<Integer> idxHour_tmp = new ArrayList<>(Arrays.asList(_idxHours_raw[i]));
				ArrayList<Float> reduction_tmp = new ArrayList<>(Arrays.asList(_impPctg_raw[i]));
				_idxCTS.add(idxCTS_tmp);
				_idxHour.add(idxHour_tmp);
				_improvement.add(reduction_tmp);
			} else {
				idxTarget = _idxCtgcy.indexOf(_idxCtgcy_raw[i]);
//				_numHours.set(idxTarget, (_numHours.get(idxTarget)+1));
				_numHoursCTS.set(idxTarget, (_numHoursCTS.get(idxTarget)+1));
				
				if (_idxCTS.get(idxTarget).contains(_idxCTS_raw[i])) continue;
				_idxCTS.get(idxTarget).add(_idxCTS_raw[i]);
				_idxHour.get(idxTarget).add(_idxHours_raw[i]);
				_improvement.get(idxTarget).add(_impPctg_raw[i]);
			}
		}
		
		for (int i=0; i<_idxCtgcy.size(); i++) {
			_numHours.add(0);
		}
		for (int i=0; i<size; i++) {
			int idx = _idxCtgcy.indexOf(_idxCtgcy_raw[i]);
			if (idx == -1) {
				int idx_NoImp = _idxCtgcy_NoImp.indexOf(_idxCtgcy_raw[i]);
				if (idx_NoImp == -1) {
					_idxCtgcy_NoImp.add(_idxCtgcy_raw[i]);
					_numHours_NoImp.add(1);
				} else {
					_numHours_NoImp.set(idx_NoImp, _numHours_NoImp.get(idx_NoImp)+1);
				}
			} else _numHours.set(idx, _numHours.get(idx)+1);
		}
	}
	
	public void dump(String ctgcyType) {
		String outFileName = "EDM_" + ctgcyType + ".txt";
		try {
			OutputStream resultFile = new FileOutputStream(outFileName, false);
		    PrintStream outFile = new PrintStream (resultFile);
		    outFile.print(" ctgcyType ctgcy_Idx" + " numOfHrsForCtgcy" + " numOfHrsWCTSImp" + " numOfCTS");
		    outFile.println(" CTS_Idx" + " CTS_Hr" + " CTS_Imp" + " (endMark is:'" + _endMark + "')");
		    
		    for (int i=0; i<_idxCtgcy_NoImp.size(); i++) {
			    outFile.print(" " + ctgcyType);
			    outFile.print(" " + _idxCtgcy_NoImp.get(i) + " " + _numHours_NoImp.get(i));
			    outFile.println(" " + 0 + " " + _endMark);
			}
		    for (int i=0; i<_idxCtgcy.size(); i++) {
			    outFile.print(" " + ctgcyType);
			    outFile.print(" " + _idxCtgcy.get(i) + " " + _numHours.get(i));
			    outFile.print(" " + _numHoursCTS.get(i) + " " + _idxCTS.get(i).size());
			    for (int j=0; j<_idxCTS.get(i).size(); j++) {
			    	outFile.print(" " + _idxCTS.get(i).get(j));
			    	outFile.print(" " + _idxHour.get(i).get(j));
			    	outFile.print(" " + _improvement.get(i).get(j));
			    }
			    outFile.println(" " + _endMark);
		    }
		    outFile.close();
			resultFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public HashMap<Integer, int[]> getMap() {
		if (_mapCtgcyToTSList != null) return _mapCtgcyToTSList;
		_mapCtgcyToTSList = new HashMap<Integer, int[]>();
		int size = _idxCtgcy.size();
		for (int i=0; i<size; i++) {
			ArrayList<Integer> TSList = _idxCTS.get(i);
			int sizeTS = TSList.size();
			int[] TSListTmp = new int[sizeTS];
			for (int j=0; j<sizeTS; j++)
				TSListTmp[j] = TSList.get(j)-1;
			_mapCtgcyToTSList.put(_idxCtgcy.get(i)-1, TSListTmp);
		}
		return _mapCtgcyToTSList;
	}
	
//	public void setCtgcyType(String str) {_ctgcyType = str;}
	public void setToc(float toc) {_toc = toc;}
	public void setIdxCtgcyRaw(int[] array) {_idxCtgcy_raw = array;}
	public void setIdxHourRaw(int[] array) {_idxHours_raw = array;}
	public void setIdxCTSRaw(int[] array) {_idxCTS_raw = array;}
	public void setImpPctg_raw(float[] array) {_impPctg_raw = array;}
	public void setOrigFlowVioRaw(float[] array) {_origFlowVio = array;}
	public void setOrigVmVioRaw(float[] array) {_origVmVio = array;}
	
	public void preprocess(float[][] rawInfos) {
		setIdxCtgcyRaw(AuxArrayXL.toIntArray(rawInfos[0]));
		setIdxHourRaw(AuxArrayXL.toIntArray(rawInfos[1]));
		setIdxCTSRaw(AuxArrayXL.toIntArray(rawInfos[2]));
		setImpPctg_raw(rawInfos[3]);
		setOrigFlowVioRaw(rawInfos[4]);
		setOrigVmVioRaw(rawInfos[5]);
	}
	public void server(int numRow, int numCol, String path, boolean isAbsPath, float toc) {
		ReadMultiCollumFloatData TSinfo = new ReadMultiCollumFloatData(numRow, numCol, path, false);
		float[][] rawInfos = TSinfo.getArrayByCol();
		setToc(toc);
		preprocess(rawInfos);
		process();
//		long t_Start = System.nanoTime();
		getMap();
//		System.out.println("\nTotal MAP simulation time is: " + (System.nanoTime() - t_Start)/1e9);
	}
	
	
	public static void main(String[] args) throws PsseModelException, IOException
	{
		long t_Start = System.nanoTime();
		
//		ReadMultiCollumFloatData BrcTSinfo = new ReadMultiCollumFloatData(883, 6, "dataToRead\\EDM_Brc.txt", false);
//		float[][] rawInfos = BrcTSinfo.getArrayByCol();
//		ReadMultiCollumFloatData GenTSinfo = new ReadMultiCollumFloatData(1983, 6, "dataToRead\\EDM_Gen.txt", false);
//		float[][] rawInfos = GenTSinfo.getArrayByCol();

//		int[] hourPeriods = new int[] {
//		1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 
//		13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24,
//		25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36,
//		37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48,
//};

		PreDataProcessing proc = new PreDataProcessing();
//		proc.setToc(0.1f);
//		proc.setIdxCtgcyRaw(AuxArrayXL.toIntArray(rawInfos[0]));
//		proc.setIdxHourRaw(AuxArrayXL.toIntArray(rawInfos[1]));
//		proc.setIdxCTSRaw(AuxArrayXL.toIntArray(rawInfos[2]));
//		proc.setImpPctg_raw(rawInfos[3]);
//		proc.setOrigFlowVioRaw(rawInfos[4]);
//		proc.setOrigVmVioRaw(rawInfos[5]);
//		proc.process();
//		proc.dump("Branch");
//		proc.dump("Gen");
//		int[] a = proc.getMap().get(6);
		
		float toc = 0.10f;
		proc.server(1983, 6, "dataToRead\\EDM_Gen.txt", false, toc);
		proc.dump("Gen");
//		proc.server(883, 6, "dataToRead\\EDM_Brc.txt", false, toc);
//		proc.dump("Branch");

//		int[] a = proc.getMap().get(-1);
		
		System.out.println("\nTotal simulation time is: " + (System.nanoTime() - t_Start)/1e9);
		System.out.println("Simulation is done here!");
	}
	
	
}
