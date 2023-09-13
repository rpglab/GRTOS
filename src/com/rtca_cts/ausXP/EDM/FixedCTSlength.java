package com.rtca_cts.ausXP.EDM;

import java.util.HashMap;

import com.utilxl.iofiles.ReadMultiCollumIntData;

/**
 * Initial input # of CTS solutions are fixed
 * Invalid CTS (with index -1 are removed).
 * 
 * @author xingpeng
 *
 */
public class FixedCTSlength {
	
	HashMap<Integer, int[]> _mapCtgcyToTSList;
	
	int _numRow;
	int _numCol;
	String _path;
	boolean _isAbsPath;
	
	public FixedCTSlength(int numRow, int numCol, String path, boolean isAbsPath) {
		_numRow = numRow;
		_numCol = numCol;
		_path = path;
		_isAbsPath = isAbsPath;
	}

	public HashMap<Integer, int[]> getMap() {
		ReadMultiCollumIntData TSinfo = new ReadMultiCollumIntData(_numRow, _numCol, _path, false);
		int[][] rawInfos = TSinfo.getArrayByRow();
		
		_mapCtgcyToTSList = new HashMap<Integer, int[]>();
		for (int i=0; i<_numRow; i++) {
			int sizeTS = _numCol - 1;
			int[] TSListTmp = new int[sizeTS];
			int count = 0;
			for (int j=0; j<sizeTS; j++)
			{
				if (rawInfos[i][j+1] < 0) continue;
				TSListTmp[j] = rawInfos[i][j+1] - 1;
				count++;
			}
			int[] TSList = new int[count];
			System.arraycopy(TSListTmp, 0, TSList, 0, count);
			_mapCtgcyToTSList.put(rawInfos[i][0] - 1, TSList);
		}
		
		return _mapCtgcyToTSList;
	}
}
