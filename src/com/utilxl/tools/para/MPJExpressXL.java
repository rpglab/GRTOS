package com.utilxl.tools.para;

import mpi.MPI;

/**
 * Auxiliary methods : for MPJExpress
 * 
 * Initialized in Sep. 2014
 * 
 * @author Xingpeng.Li (xplipower@gmail.com)
 *
 */
public class MPJExpressXL {
	
	int _nproc;
	int _rank;

	public MPJExpressXL(int nproc, int rank)
	{
		_nproc = nproc;
		_rank = rank;
	}
	
	/** 
	 * @return
	 * int[0] : nStart;
	 * int[1] : nEnd;
	 * int[2] : nIter;
	 * */
	public int[] assignWorkPara(int numTasks)
	{
		int nAverage = numTasks/_nproc;
		int nleft = numTasks - _nproc*nAverage;
		int nIter = nAverage;
		int	nStart = nAverage*_rank;
		if (_rank < nleft)
		{
			nStart += _rank;
			nIter += 1;
		}
		else nStart += nleft;
		int nEnd = nStart + nIter - 1;
		return new int[] {nStart, nEnd, nIter};		
	}

	/** Sum one integer number from all threads, saved in thread 0. */ 
	public int ThreadCommSumOneNumber(int number)
	{
		if (_nproc == 1) return number;
		else
		{
			int sum = number;
			int tag = 1;
		    if(_rank == 0)
		    {
		    	for(int i=1; i<_nproc; i++)
		    	{
		    		int[] tempDat = {-1};
		    		MPI.COMM_WORLD.Recv(tempDat, 0, 1, MPI.INT, i, tag);
		    		sum += tempDat[0];
		    	}
		    }
		    else
		    {
		    	int[] tempDat = {number};
		    	MPI.COMM_WORLD.Send(tempDat, 0, 1, MPI.INT, 0, tag);     	
		    }
			return sum;
		}
	}
	
	/** Sum one integer number from all threads, saved in thread 0. */ 
	public float ThreadCommSumOneNumber(float number)
	{
		if (_nproc == 1) return number;
		else
		{
			float sum = number;
			int tag = 1;
		    if(_rank == 0)
		    {
		    	for(int i=1; i<_nproc; i++)
		    	{
		    		float[] tempDat = {-1};
		    		MPI.COMM_WORLD.Recv(tempDat, 0, 1, MPI.FLOAT, i, tag);
		    		sum += tempDat[0];
		    	}
		    }
		    else
		    {
		    	float[] tempDat = {number};
		    	MPI.COMM_WORLD.Send(tempDat, 0, 1, MPI.FLOAT, 0, tag);     	
		    }
			return sum;
		}
	}
	
	
	/** Comm an integer value from each thread as an array, saved in thread 0. */ 
	public int[] ThreadCommOneNumber(int number)
	{
		int[] array = new int[_nproc];
		if (_nproc == 1) array[0] = number;
		
		int tag = 1;
	    if(_rank == 0)
	    {
	    	array[0] = number;
	    	for(int i=1; i<_nproc; i++)
	    	{
	    		int[] tempDat = {-1};
	    		MPI.COMM_WORLD.Recv(tempDat, 0, 1, MPI.INT, i, tag);
	    		array[i] = tempDat[0];
	    	}
	    }
	    else {MPI.COMM_WORLD.Send(new int[] {number}, 0, 1, MPI.INT, 0, tag);}     	
		return array;
	}

	/** Comm a float value from each thread as an array, saved in thread 0. */ 
	public float[] ThreadCommOneNumber(float number)
	{
		float[] array = new float[_nproc];
		if (_nproc == 1) array[0] = number;
		
		int tag = 1;
	    if(_rank == 0)
	    {
	    	array[0] = number;
	    	for(int i=1; i<_nproc; i++)
	    	{
	    		float[] tempDat = {-1};
	    		MPI.COMM_WORLD.Recv(tempDat, 0, 1, MPI.FLOAT, i, tag);
	    		array[i] = tempDat[0];
	    	}
	    }
	    else {MPI.COMM_WORLD.Send(new float[] {number}, 0, 1, MPI.FLOAT, 0, tag);}     	
		return array;
	}


	/** put several arrays from different threads as a single array, saved in thread 0. 
	 * array does not have to be with the same length. */ 
	public float[] ThreadCommOneArray(float[] array)
	{
		if (_nproc == 1) return array;
		if (array == null) array = new float[0];

		int tag = 1;
		float[][] arrays = new float[_nproc][];
		arrays[0] = array;
		int[] arraySize = new int[_nproc];
		float[] newArray = null;
		int sizeNewArray = array.length;
		arraySize[0] = sizeNewArray;
	    if(_rank == 0)
	    {
	    	for(int i=1; i<_nproc; i++)
	    	{
		    	int[] tempDat = {arraySize[0]};
		    	MPI.COMM_WORLD.Recv(tempDat, 0, 1, MPI.INT, i, tag);
		    	arraySize[i] = tempDat[0];
		    	sizeNewArray += tempDat[0];
	    	}
	    }
	    else
	    {
	    	int[] tempDat = {arraySize[0]};
	    	MPI.COMM_WORLD.Send(tempDat, 0, 1, MPI.INT, 0, tag);     		    	
	    }
	    
	    if(_rank == 0)
	    {
	    	newArray = new float[sizeNewArray];
	    	System.arraycopy(array, 0, newArray, 0, array.length);
	    	int desPo = array.length;
	    	for(int i=1; i<_nproc; i++)
	    	{
	    		arrays[i] = new float[arraySize[i]];
		    	MPI.COMM_WORLD.Recv(arrays[i], 0, arraySize[i], MPI.FLOAT, i, tag);     		    	
		    	System.arraycopy(arrays[i], 0, newArray, desPo, arrays[i].length);
		    	desPo += arrays[i].length;
	    	}
	    }
	    else {MPI.COMM_WORLD.Send(array, 0, array.length, MPI.FLOAT, 0, tag);}
	    
		return newArray;
	}

	/** put several arrays from different threads as a single array, saved in thread 0. 
	 * array does not have to be with the same length. */ 
	public int[] ThreadCommOneArray(int[] array)
	{
		if (_nproc == 1) return array;
		if (array == null) array = new int[0];
		
		int tag = 1;
		int[][] arrays = new int[_nproc][];
		arrays[0] = array;
		int[] arraySize = new int[_nproc];
		int[] newArray = null;
		int sizeNewArray = array.length;
		arraySize[0] = sizeNewArray;
	    if (_rank == 0)
	    {
	    	for(int i=1; i<_nproc; i++)
	    	{
		    	int[] tempDat = {arraySize[0]};
		    	MPI.COMM_WORLD.Recv(tempDat, 0, 1, MPI.INT, i, tag);
		    	arraySize[i] = tempDat[0];
		    	sizeNewArray += tempDat[0];
	    	}
	    }
	    else
	    {
	    	int[] tempDat = {arraySize[0]};
	    	MPI.COMM_WORLD.Send(tempDat, 0, 1, MPI.INT, 0, tag);     		    	
	    }
	    
	    if(_rank == 0)
	    {
	    	newArray = new int[sizeNewArray];
	    	System.arraycopy(array, 0, newArray, 0, array.length);
	    	int desPo = array.length;
	    	for(int i=1; i<_nproc; i++)
	    	{
	    		arrays[i] = new int[arraySize[i]];
		    	MPI.COMM_WORLD.Recv(arrays[i], 0, arraySize[i], MPI.INT, i, tag);     		    	
		    	System.arraycopy(arrays[i], 0, newArray, desPo, arrays[i].length);
		    	desPo += arrays[i].length;
	    	}
	    }
	    else {MPI.COMM_WORLD.Send(array, 0, array.length, MPI.INT, 0, tag);}
	    
		return newArray;
	}
	
	/** Put several two-dim arrays from different threads as a whole array, saved in thread 0. 
	 * Note that each single array from the two-dim array does not have to be with the same length.
	 * e.g. arrays = [ 2, 4, 7; 
	 *                 5, 6 ]
	 *      arrayElemLength = [3, 2];
	 *  */
	public int[][] ThreadCommTwoArray(int[][] arrays)
	{
		if (_nproc == 1) return arrays;
		int [] arrayElemNum = null;
		//TODO:
		return ThreadCommTwoArray(arrays, arrayElemNum);
	}

	
	/** Put several two-dim arrays from different threads as a whole array, saved in thread 0. 
	 * Note that each single array from the two-dim array does not have to be with the same length.
	 * e.g. arrays = [ 2, 4, 7; 
	 *                 5, 6 ]
	 *      arrayElemLength = [3, 2];
	 *  */
	public int[][] ThreadCommTwoArray(int[][] arrays, int[] arrayElemNum)
	{
		if (_nproc == 1) return arrays;
		int length = 0;
		if (arrays != null) length = arrays.length;
		int[] lengthArray = ThreadCommOneNumber(length);
		arrayElemNum = ThreadCommOneArray(arrayElemNum);
		
		int lengthTotal = length;
		for (int i=1; i<_nproc; i++)
			lengthTotal += lengthArray[i];
		int[][] newArrays = new int[lengthTotal][];
		
		int tag = 1;
	    if (_rank == 0)
	    {
	    	for (int k=0; k<length; k++)
	    		newArrays[k] = arrays[k];
	    	
	    	for (int i=1; i<_nproc; i++)
		    {
	    		for (int p=0; p<lengthArray[i]; p++)
	    		{
	    			int[] tmpData = new int[arrayElemNum[length]];
	    			MPI.COMM_WORLD.Recv(tmpData, 0, arrayElemNum[length], MPI.INT, i, tag);
	    			newArrays[length] = tmpData;
		    		length++;
	    		}
		    }
	    }
	    else
	    {
	    	for (int p=0; p<arrays.length; p++)
	    		MPI.COMM_WORLD.Send(arrays[p], 0, arrays[p].length, MPI.INT, 0, tag);
	    }
	    return newArrays;
	}

	/** Put several two-dim arrays from different threads as a whole array, saved in thread 0. 
	 * Note that each single array from the two-dim array does not have to be with the same length.
	 * e.g. arrays = [ 2, 4, 7; 
	 *                 5, 6 ]
	 *      arrayElemLength = [3, 2];
	 *  */
	public float[][] ThreadCommTwoArray(float[][] arrays)
	{
		if (_nproc == 1) return arrays;
		int [] arrayElemNum = null;
		//TODO:
		return ThreadCommTwoArray(arrays, arrayElemNum);
	}

	
	/** Put several two-dim arrays from different threads as a whole array, saved in thread 0. 
	 * Note that each single array from the two-dim array does not have to be with the same length.
	 * e.g. arrays = [ 2, 4, 7; 
	 *                 5, 6 ]
	 *      arrayElemLength = [3, 2];
	 *  */
	public float[][] ThreadCommTwoArray(float[][] arrays, int[] arrayElemNum)
	{
		if (_nproc == 1) return arrays;
		int length = 0;
		if (arrays != null) length = arrays.length;
		int[] lengthArray = ThreadCommOneNumber(length);
		arrayElemNum = ThreadCommOneArray(arrayElemNum);
		
		int lengthTotal = length;
		for (int i=1; i<_nproc; i++)
			lengthTotal += lengthArray[i];
		float[][] newArrays = new float[lengthTotal][];
		
		int tag = 1;
	    if (_rank == 0)
	    {
	    	for (int k=0; k<length; k++)
	    		newArrays[k] = arrays[k];
	    	
	    	for (int i=1; i<_nproc; i++)
		    {
	    		for (int p=0; p<lengthArray[i]; p++)
	    		{
	    			float[] tmpData = new float[arrayElemNum[length]];
	    			MPI.COMM_WORLD.Recv(tmpData, 0, arrayElemNum[length], MPI.FLOAT, i, tag);
	    			newArrays[length] = tmpData;
		    		length++;
	    		}
		    }
	    }
	    else
	    {
	    	for (int p=0; p<arrays.length; p++)
	    		MPI.COMM_WORLD.Send(arrays[p], 0, arrays[p].length, MPI.FLOAT, 0, tag);
	    }
	    return newArrays;
	}


	/** Put several two-dim arrays from different threads as a whole array, saved in thread 0. 
	 * Note that each single array from the two-dim array are with the same length. */
	public int[][] ThreadCommTwoDimArray(int[][] arrays)
	{
		if (_nproc == 1) return arrays;
		int lengthArray;
		if (arrays.length == 0) lengthArray = 0;
		else lengthArray = arrays[0].length;
		int[] sizeAll = ThreadCommOneNumber(arrays.length);
		int size = 0;
		for (int i=0; i<_nproc; i++)
		{
			size += sizeAll[i];
		}
		int[][] arraysNew = new int[size][lengthArray];
		
		int tag = 1;
	    if(_rank == 0)
	    {
	    	System.arraycopy(arrays, 0, arraysNew, 0, arrays.length);
	    	int row = arrays.length;
	    	for(int i=1; i<_nproc; i++)
	    	{
		    	for(int ij=0; ij<sizeAll[i]; ij++)
			    	MPI.COMM_WORLD.Recv(arraysNew[row++], 0, lengthArray, MPI.INT, i, tag);     	
	    	}
	    }
	    else
	    {
	    	for(int i=0; i<arrays.length; i++)
		    	MPI.COMM_WORLD.Send(arrays[i], 0, lengthArray, MPI.INT, 0, tag);     		    	
	    }
		return arraysNew;
	}

	/** Put several two-dim arrays from different threads as a whole array, saved in thread 0. 
	 * Note that each single array from the two-dim array are with the same length.
	 * */ 
	public float[][] ThreadCommTwoDimArray(float[][] arrays)
	{
		if (_nproc == 1) return arrays;
		int lengthArray;
		if (arrays.length == 0) lengthArray = 0;
		else lengthArray = arrays[0].length;
		int[] sizeAll = ThreadCommOneNumber(arrays.length);
		int size = 0;
		for (int i=0; i<_nproc; i++)
		{
			size += sizeAll[i];
		}
    	float[][] arraysNew = new float[size][lengthArray];
		
		int tag = 1;
	    if(_rank == 0)
	    {
	    	System.arraycopy(arrays, 0, arraysNew, 0, arrays.length);
	    	int row = arrays.length;
	    	for(int i=1; i<_nproc; i++)
	    	{
		    	for(int ij=0; ij<sizeAll[i]; ij++)
			    	MPI.COMM_WORLD.Recv(arraysNew[row++], 0, lengthArray, MPI.FLOAT, i, tag);     	
	    	}
	    }
	    else
	    {
	    	for(int i=0; i<arrays.length; i++)
		    	MPI.COMM_WORLD.Send(arrays[i], 0, lengthArray, MPI.FLOAT, 0, tag);     		    	
	    }
		return arraysNew;
	}

	/** array is spreaded from thread 0 to all other threads. */
	public int[] spreadArray(int[] array)
	{
		if (_nproc == 1) return array;
		else
		{
			int number = 0;
			if (array != null) number = array.length;

			int tag = 1;
		    if(_rank == 0)
		    {
		    	for(int i=1; i<_nproc; i++)
		    	{
		    		int[] tempDat = {number};
		    		MPI.COMM_WORLD.Send(tempDat, 0, 1, MPI.INT, i, tag);
		    	}
		    }
		    else
		    {
		    	int[] tempDat = {-1};
		    	MPI.COMM_WORLD.Recv(tempDat, 0, 1, MPI.INT, 0, tag);	
		    	number = tempDat[0];
		    }

//		    if (number == 0) return null;
			int[] arrayNew = new int[number];
		    if(_rank == 0)
		    {
		    	arrayNew = array;
		    	for(int i=1; i<_nproc; i++)
		    		MPI.COMM_WORLD.Send(arrayNew, 0, number, MPI.INT, i, tag);
		    }
		    else MPI.COMM_WORLD.Recv(arrayNew, 0, number, MPI.INT, 0, tag);	
		    
		    return arrayNew;
		}
	}
	

}
