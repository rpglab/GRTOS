package com.rtca_cts.transmissionswitching;

import java.util.Arrays;

import com.utilxl.array.AuxArrayXL;

/**
 * 
 * @author Xingpeng.Li (xplipower@gmail.com)
 *
 */
public class DetermineBestSwitActions {
	
	TransmSwit _checkTS;

	float[] _sumBrcVioOfBestTSForBrcVio;
	float[] _sumVmVioOfBestTSForBrcVio;
	
			
	DetermineBestSwitActions(TransmSwit checkTS)
	{
		_checkTS = checkTS;
	}
	
	/** 
	 * Return the branch index of num best TS if # of beneficial TS is >= num.
	 * O.w, return all beneficial TS if # of beneficial TS is < num.
	 * */
	public int[] getBestTSforBrcVio(int num)
	{
//		int[] idxBestTS = new int[num];
//		Arrays.fill(idxBestTS, -1);
		
		int[] idxTS = _checkTS.getIdxTSCheck();
		float[][] sumVioTS = _checkTS.getSumVioTS(); // sumVioTS[i][0] is about BrcVio, while sumVioTS[i][1] is about VmVio.
		float[] sumBrcTS = AuxArrayXL.transposeArray(sumVioTS)[0];
		float sumContiBrcTS = _checkTS.getSumContiBrcVio();
		
		// Compare TSVio with contiVio
		int[] idxPos = AuxArrayXL.getIdxOfMinElems(sumBrcTS, num, 0f);
		int numTS = 0;
		for (int i=0; i<idxPos.length; i++)
		{
			if (sumBrcTS[idxPos[i]] >= sumContiBrcTS) break; 
			numTS++;
		}
		idxPos = Arrays.copyOf(idxPos, numTS);
		
		_sumBrcVioOfBestTSForBrcVio = AuxArrayXL.getElemsGivenIndex(sumBrcTS, idxPos);
		_sumVmVioOfBestTSForBrcVio = new float[numTS];
		for (int i=0; i<numTS; i++)
		{
			int idx = idxPos[i];
			_sumVmVioOfBestTSForBrcVio[i] = sumVioTS[idx][1];
		}
		
//		int[] idxTmp = AuxArrayXL.getElemsGivenIndex(idxTS, idxPos);
//		if (idxTmp.length == num) idxBestTS = idxTmp;
//		else
//		{
//			int[] additiveNums = new int[num - idxTmp.length];
//			Arrays.fill(additiveNums, -1);
//			System.arraycopy(idxTmp, 0, idxBestTS, 0, idxTmp.length);
//			System.arraycopy(additiveNums, 0, idxBestTS, idxTmp.length, additiveNums.length);
//		}
		return AuxArrayXL.getElemsGivenIndex(idxTS, idxPos);
	}
	
	public float[] getSumBrcVioOfBestTSForBrcVio() { return _sumBrcVioOfBestTSForBrcVio;}
	public float[] getSumVmVioOfBestTSForBrcVio() { return _sumVmVioOfBestTSForBrcVio;}
	

}
