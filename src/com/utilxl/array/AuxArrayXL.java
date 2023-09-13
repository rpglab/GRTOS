package com.utilxl.array;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.rtca_cts.ausData.RadialBranches;

/**
 * Auxiliary methods : for array
 * 
 * Initialized in Jun. 2014
 * 
 * @author Xingpeng.Li (xplipower@gmail.com)
 *
 */
public class AuxArrayXL {

	public AuxArrayXL() {  }
	
	/** Check an array is normal or not */
	public static boolean isNormalArray(int[] array)
	{
		if (array == null) return false;
		if (array.length == 0) return false;
		return true;
	}
	
	/** Check an array is normal or not */
	public static boolean isNormalArray(float[] array)
	{
		if (array == null) return false;
		if (array.length == 0) return false;
		return true;
	}
	
	/** true if elem is contained in array */
	public static boolean isElemBelongsToArray(int[] array, int elem)
	{
		boolean con = false;
		int size = array.length;
		for(int i=0; i<size; i++)
		{
			if (elem == array[i]) { con = true; break;}
		}
		return con;
	}
	
	/** remove zero element items from an int-type array */
	public static int[] remZeroFrmArray(int[] OrigArray)
	{
		int num = 0;
		int OrigSize = OrigArray.length;
		int[] idx = new int[OrigSize];
		for(int i=0; i<OrigSize; i++)
		{
			if (OrigArray[i] == 0) continue;
			idx[num++] = i;
		}
		if (num == 0) return null;
		int[] newArray = new int[num];
		for(int i=0; i<num; i++)
		{
			newArray[i] = OrigArray[idx[i]];
		}
		return newArray;
	}
	
	
	/** remove null element and [] element from a 2-dim array */
	public static float[][] shrinkTwoDimArray(float[][] OrigArray)
	{
		int num = 0;
		int OrigSize = OrigArray.length;
		int[] idx = new int[OrigSize];
		for(int i=0; i<OrigSize; i++)
		{
			if (OrigArray[i] == null) continue;
			if (OrigArray[i].length == 0) continue;
			idx[num++] = i;
		}
		if (num == 0) return null;
		float[][] newArray = new float[num][];
		for (int i=0; i<num; i++)
		{
			newArray[i] = OrigArray[idx[i]];
		}
		return newArray;
	}
	
	/** remove null element and [] element from a 2-dim array */
	public static int[][] shrinkTwoDimArray(int[][] OrigArray)
	{
		int num = 0;
		int OrigSize = OrigArray.length;
		int[] idx = new int[OrigSize];
		for(int i=0; i<OrigSize; i++)
		{
			if (OrigArray[i] == null) continue;
			if (OrigArray[i].length == 0) continue;
			idx[num++] = i;
		}
		if (num == 0) return null;
		int[][] newArray = new int[num][];
		for (int i=0; i<num; i++)
		{
			newArray[i] = OrigArray[idx[i]];
		}
		return newArray;
	}
	
	/** calculate the average of an array  */
	public static float calculateAverageOfArray(float[] array)
	{
		if (array == null) return 0f;
		int size = array.length;
		if (size == 0) return 0f;
		float average = 0;
		for (int i=0; i<size; i++)
		{
			average+= array[i];
		}
		average = average / size;
		return average;
	}

	/** calculate the average of an array  
	 *  zero elements are ignored,
	 *  e.g., if array size is 10, and there are 4 elements are zeros
	 *  then, the average returned is : sum / (10 - 4). 
	 *  Return zero if it is a null array.
	 */
	public static float calculateAverageOfArrayIgnoreZero(float[] array)
	{
		if (array == null) return 0f;
		int size = array.length;
		if (size == 0) return 0f;
		float average = 0;
		int num = 0;
		for (int i=0; i<size; i++)
		{
			if (array[i] != 0)
			{
				num++;
				average+= array[i];
			}
		}
		if (num != 0) average = average / num;
		return average;
	}
	

	/** calculate the average of two array  */
	public static float calculateAverageOfArray(float[] array1, float[] array2)
	{
		float[] array = connectTwoArray(array1, array2);
		return calculateAverageOfArray(array);
	}
	
	/**  first calculate averages aa[i] for each arrays[i], then calculate and return average of aa.  */
	public static float calculateAverageOfArray(float[][] arrays)
	{
		if (arrays == null) return 0f;
		float average = calculateAverageOfArray(getAverageArrayofArrays(arrays));
		return average;
	}
	
	public static float calculateAverageOfArray(float[][] arrays1, float[][] arrays2)
	{
		float[] array1 = null;
		float[] array2 = null;
		
		if (arrays1 != null) array1 = getAverageArrayofArrays(arrays1);
		if (arrays2 != null) array2 = getAverageArrayofArrays(arrays2);

		float[] array = connectTwoArray(array1, array2);
		return calculateAverageOfArray(array);
	}
	
	/**  calculate averages aa[i] for each arrays[i], return the average array aa. */
	public static float[] getAverageArrayofArrays(float[][] arrays)
	{
		if (arrays == null) return null;
		int size = arrays.length;
		assertTrue(size != 0); 
		float[] array = new float[size];
		for(int i=0; i<size; i++)
		{
			array[i] = calculateAverageOfArray(arrays[i]);
		}
		return array;
	}
	
	
	public static int[] connectTwoArray(int[] a, int[] b)
	{
		if (a == null) return b;
		int length1 = a.length;
		if (length1 == 0) return b; 
		
		if (b == null) return a;
		int length2 = b.length;
		if (length2 == 0) return a;
		
		int[] c = new int[length1 + length2];
		System.arraycopy(a, 0, c, 0, length1);
		System.arraycopy(b, 0, c, length1, length2);
		return c;
	}

	public static float[] connectTwoArray(float[] a, float[] b)
	{
		if (a == null) return b;
		int length1 = a.length;
		if (length1 == 0) return b; 
		
		if (b == null) return a;
		int length2 = b.length;
		if (length2 == 0) return a;
		
		float[] c = new float[length1 + length2];
		System.arraycopy(a, 0, c, 0, length1);
		System.arraycopy(b, 0, c, length1, length2);
		return c;
	}
	
	/** Change a 2-dim arrays to a 1-dim array. */
	public static int[] connectMultiArrays(int[][] arrays)
	{
		int size = arrays.length;
		int[] array = arrays[0];
		for (int i=1; i<size; i++)
			array = connectTwoArray(array, arrays[i]);
		return array;
	}
	
	/** calculate standard deviation 
	 * the returned values: 
	 * float[0]: Standard deviation,
	 * float[1]: Average value;
	 * float[2]: Elements in array which is the most far away from Param x.
	 * float[3]: number of elements in array.  --- Not correct, since the existence of 0.0
	 * */
	public static float[] getSD(float[] array, float x)
	{
		float[] backData = {0f, 0f, 0f, 0f};
		if (array == null) return backData;
		int size = array.length;
		if (size == 0) return backData;
		float sd = 0;;
		float average = 0;
		for (int i=0; i<size; i++)
		{
			average += array[i];
		}
		average = average/size;
		
		for (int i=0; i<size;i++)
		{
		    sd = (float) (sd + Math.pow(array[i] - average, 2));
		}
		//if (size != 1) sd = sd/(size - 1);
		//else sd = 0;
		sd = sd/size;
		sd = (float) Math.sqrt(sd);
		backData[0] = sd;
		backData[1] = average;
		
		float maxDev = 0;
		for (int i=0; i<array.length; i++)
		{
			float diff = array[i] - x;
			if (Math.abs(diff) > Math.abs(maxDev) )
				maxDev = diff;
		}
		backData[2] = maxDev + x;
		//backData[3] = size;
		return backData;
	}
	
	/** calculate standard deviation */
	public static float[] getSD(float[] array_1, float[] array_2, float x)
	{
		float[] array = connectTwoArray(array_1, array_2);
		float[] sd = getSD(array, x);
		return sd;
	}
	
	/** calculate standard deviation */
	public static float[] getSD(float[][] array2, float x)
	{
		float[] array = getOneDimArray(array2);
		float[] sd = getSD(array, x);
		return sd;
	}
	
	/** calculate standard deviation */
	public static float[] getSD(float[][] array_1, float[][] array_2, float x)
	{
		float[] array1 = null;
		if (array_1 != null) array1 = getOneDimArray(array_1.clone());
		float[] array2 = null;
		if (array_2 != null) array2 = getOneDimArray(array_2.clone());
		float[] sd = getSD(array1, array2, x);
		return sd;
	}
	
	public static float[] getOneDimArray(float[][] array_2)
	{
		if (array_2 == null) return null;
		float[][] array2 = array_2.clone(); 
		int size = 0;
		for(int i=0; i<array2.length; i++)
		{
			size += array2[i].length;
		}
		float[] array = new float[size];
		int aa = 0;
		for(int i=0; i<array2.length; i++)
		{
			if (array2[i].length != 0)
			{
				System.arraycopy(array2[i], 0, array, aa, array2[i].length);
				aa += array2[i].length;
			}
		}
		return array;
	}
	
	/** 
	 * @return
	 * 	[0]: min, 
	 *  [1]: max, 
	 *  [2]: average.
	 */
	public static float[] getMinMaxAvg(float[] array)
	{
		assertTrue(array != null);
		int size = array.length;
		assertTrue(size > 0);
		float min = array[0];
		float max = array[0];
		float avg = array[0];		
		for(int i=1; i<size; i++)
		{
			if (array[i] < min) min = array[i];
			if (array[i] > max) max = array[i];
			avg += array[i];
		}
		avg = avg / size;		
		return new float[] {min, max, avg};
	}
	
	/** 
	 * @return
	 * 	[0]: min, 
	 *  [1]: max, 
	 *  [2]: average.
	 */
	public static float[] getMinMaxAvg(float[] array1, float[] array2)
	{
		float[] array = connectTwoArray(array1, array2);
		return getMinMaxAvg(array);
	}

	/** 
	 * return values are -1 if it is null array or [] array.
	 * @return
	 * 	[0]: position of min element, 
	 *  [1]: position of max element, 
	 */
	public static int[] getIdxMinMax(float[] array)
	{
		if(array == null) return new int[] {-1, -1};
		int size = array.length;
		if(size == 0) return new int[] {-1, -1};
		float min = array[0];
		float max = array[0];
		int minPos = 0;
		int maxPos = 0;	
		for(int i=1; i<size; i++)
		{
			if (array[i] < min) {min = array[i]; minPos = i;}
			else if (array[i] > max) {max = array[i]; maxPos = i;}
		}		
		return new int[] {minPos, maxPos};
	}
	
	/** 
	 * Note that zero elements are ignored.
	 * return values are -1 if all elements are zeros or it is null array or [] array.
	 * @return
	 * 	[0]: position of min element, 
	 *  [1]: position of max element, 
	 */
	public static int[] getIdxMinMaxIgnoreZero(float[] array)
	{
		if(array == null) return new int[] {-1, -1};
		int size = array.length;
		if(size == 0) return new int[] {-1, -1};
		int startPos = 0;
		float min = 0;
		float max = 0;
		int minPos = -1;
		int maxPos = -1;
		for(int i=0; i<size; i++)
		{
			if (array[i] != 0)
			{
				startPos = i;
				min = array[0];	max = array[0];
				minPos = i; maxPos = i;
			}
		}
		for(int i=startPos; i<size; i++)
		{
			if (array[i] != 0)
			{
				if (array[i] < min) {min = array[i]; minPos = i;}
				else if (array[i] > max) {max = array[i]; maxPos = i;}	
			}
		}
		return new int[] {minPos, maxPos};
	}
	
	/** 
	 * @return
	 * 	true : there are no duplicate elements;
	 * 	false : there are duplicate elements.
	 */
	public static boolean checkNoDupElem(float[] array)
	{
		boolean noDupElem = true;
		int size = array.length;
		List<Float> f = new ArrayList<Float>();
		for (float elem : array) 
			f.add(elem);
		for (int i=0; i<size; i++)
		{
			int a = f.indexOf(array[i]);
			int b = f.lastIndexOf(array[i]);
			if (a != b) { noDupElem = false; break;}
		}		
		return noDupElem;
	}
	
	/** Slow. Do not use this method unless necessary. */ 
	public static ArrayList<Integer> toArrayList(int[] array)
	{
		ArrayList<Integer> arrayRet = new ArrayList<Integer>();
		if (array == null) return arrayRet;
		for(int i=0; i<array.length; i++)
			arrayRet.add(array[i]);
		return arrayRet;
	}
	
	/** Slow. Do not use this method unless necessary. */ 
	public static ArrayList<Float> toArrayList(float[] array)
	{
		ArrayList<Float> arrayRet = new ArrayList<Float>();
		for(int i=0; i<array.length; i++)
			arrayRet.add(array[i]);
		return arrayRet;
	}
	
	/** Change an int-type number to an array which has only one element. */
	public static int[] toArray(int var)
	{
		int[] array = {var}; 
		return array;
	}
	
	/** Change a float-type number to an array which has only one element. */
	public static float[] toArray(float var)
	{
		float[] array = {var}; 
		return array;
	}
	
	/** All elems in array are added by @num .
	 * NOTE THAT: the original array passed in will also change! */
	public static int[] allElemsPlusANum(int[] array, int num)
	{
		int size = array.length;
		for (int i=0; i<size; i++)
			array[i] += num;
		return array;
	}
	
	/** All elems in array are added by @num .
	 * NOTE THAT: the original array passed in will also change! */
	public static float[] allElemsPlusANum(float[] array, float num)
	{
		int size = array.length;
		for (int i=0; i<size; i++)
			array[i] += num;
		return array;
	}
	
	//TODO: multiple same elements
	public static void findElemPos(int[] array, int elem)
	{
		
	}
	
	/** return the indices of all the elems in @array that equals to @mark, 
	 * return null if not found. */
	public static int[] findIndex(boolean[] array, boolean mark)
	{
		int num = 0;
		int size = array.length;
		int[] idx = new int[size];
		for (int i=0; i<size; i++)
			if (array[i] == mark) idx[num++] = i;
		if (num == 0) return null;
		return Arrays.copyOf(idx, num);
	}
	
	/** return the indices of all the elems in @array that equals to @number, 
	 * return null if not found. */
	public static int[] findIndex(int[] array, int number)
	{
		int num = 0;
		int size = array.length;
		int[] idx = new int[size];
		for (int i=0; i<size; i++)
			if (array[i] == number) idx[num++] = i;
		if (num == 0) return null;
		return Arrays.copyOf(idx, num);
	}
	
	/** return the indices of all the elems in @array that equals to @number, 
	 * return null if not found. */
	public static int[] findIndex(float[] array, float number)
	{
		int num = 0;
		int size = array.length;
		int[] idx = new int[size];
		for (int i=0; i<size; i++)
			if (array[i] == number) idx[num++] = i;
		if (num == 0) return null;
		return Arrays.copyOf(idx, num);
	}
	
	/** Check whether an elem exists in an array or not. */
	public static boolean isElemInArray(int[] array, int elem)
	{
		if (array == null) return false;
		int size = array.length;
		for (int i=0; i<size; i++)
		{
			if (array[i] == elem) return true;
		}
		return false;
	}
	
	/** Make a new array which has all the elems in old-array, 
	 * this new array does not have any same elems.  */
	public static ArrayList<Integer> makeElemsUnique(int[] array)
	{
		ArrayList<Integer> arrayList = new ArrayList<Integer>();
		int size = array.length;
		for (int i=0; i<size; i++)
		{
			if (arrayList.contains(array[i]) == false) arrayList.add(array[i]);
		}
		return arrayList;
	}
	
	/** Return an int[] array whose elems are 0, 1, 2, ... , (size-1). */
	public static int[] getSeqIndex(int size)
	{
		int[] array = new int[size];
		for (int i=0; i<size; i++)
			array[i] = i;
		return array;
	}
	
	/** Return an array with all same elements. 
	 * @num : array length.
	 * @elem : value of each element. */
	public static int[] getArrayWithSameElems(int num, int elem)
	{
		int[] array = new int[num];
		Arrays.fill(array, elem);
		return array;
	}
	
	/** Return an array with all same elements.
	 * @num : array length.
	 * @elem : value of each element. */
	public static float[] getArrayWithSameElems(int num, float elem)
	{
		float[] array = new float[num];
		Arrays.fill(array, elem);
		return array;
	}
	
	/** Return the index where has the maximum/largest number. 
	 * If there are multiple same maximum numbers, then,
	 * return the smallest index.
	 */
	public static int getIdxMaxNum(float[] numbers)
	{
		int idx = -1;
		if (numbers == null) return idx;
		if (numbers.length == 0) return idx;
		int size = numbers.length;
		float max = numbers[0];
		idx = 0;
		for (int i=1; i<size; i++)
			if (numbers[i] > max) {idx = i; max = numbers[i];}
		return idx;
	}

	/** Return the index where has the maximum number.
	 * If there are multiple same maximum numbers, then,
	 * return the smallest index.
	 */
	public static int getIdxMaxNum(int[] numbers)
	{
		int idx = -1;
		if (numbers == null) return idx;
		if (numbers.length == 0) return idx;
		int size = numbers.length;
		int max = numbers[0];
		idx = 0;
		for (int i=1; i<size; i++)
			if (numbers[i] > max) {idx = i; max = numbers[i];}
		return idx;
	}

	/** Return the index where has the maximum number;
	 * If there are multiple same maximum numbers, then,
	 * compare the corresponding number in numbers2nd[],
	 * and return the one which is bigger. 
	 */
	public static int getIdxMaxNum(int[] numbers, int[] numbers2nd)
	{
		int idx = -1;
		if (numbers == null) return idx;
		if (numbers.length == 0) return idx;
		int size = numbers.length;
		int max = numbers[0];
		int max2nd = numbers2nd[0];
		idx = 0;
		for (int i=1; i<size; i++)
		{
			if (numbers[i] > max)
			{
				idx = i;
				max = numbers[i];
				max2nd = numbers2nd[i];
			}
			else if (numbers[i] == max)
			{
				if (numbers2nd[i] > max2nd)
				{
					idx = i;
					max = numbers[i];
					max2nd = numbers2nd[i];
				}
			}
		}
		return idx;
	}

	/** Return the index where has the maximum number;
	 * If there are multiple same maximum numbers, then,
	 * compare the corresponding number in numbers2nd[],
	 * and return the one which is bigger. 
	 */
	public static int getIdxMaxNum(float[] numbers, float[] numbers2nd)
	{
		int idx = -1;
		if (numbers == null) return idx;
		if (numbers.length == 0) return idx;
		int size = numbers.length;
		float max = numbers[0];
		float max2nd = numbers2nd[0];
		idx = 0;
		for (int i=1; i<size; i++)
		{
			if (numbers[i] > max)
			{
				idx = i;
				max = numbers[i];
				max2nd = numbers2nd[i];
			}
			else if (numbers[i] == max)
			{
				if (numbers2nd[i] > max2nd) 
				{
					idx = i;
					max = numbers[i];
					max2nd = numbers2nd[i];
				}
			}
		}
		return idx;
	}

	/** return the quotients array
	 * e.g. 
	 *  elem 0 := array1[0]/array2[0];
	 *  elem 1 := array1[1]/array2[1];
	 *  and so on ...
	 * */
	public static float[] arrayIndiviDividen(float[] array1, float[] array2)
	{
		int size = array1.length;
		float[] quotients = new float[size];
		for (int i=0; i<size; i++)
			quotients[i] = array1[i]/array2[i];
		return quotients;
	}
		
	/** get the summation of all elements. */
	public static float getSumElems(float[] array)
	{
		float elem = array[0];
		int size = array.length;
		for (int i=1; i<size; i++)
			elem += array[i];
		return elem;
	}

	/** get the summation of all elements. */
	public static int getSumElems(int[] array)
	{
		int elem = array[0];
		int size = array.length;
		for (int i=1; i<size; i++)
			elem += array[i];
		return elem;
	}

	/** Find the maximum elements. */
	public static float getMaxElem(float[] array)
	{
		float elem = array[0];
		int size = array.length;
		for (int i=1; i<size; i++)
			if (array[i] > elem) elem = array[i];
		return elem;
	}
	
	/** Find the maximum elements. */
	public static int getMaxElem(int[] array)
	{
		int elem = array[0];
		int size = array.length;
		for (int i=1; i<size; i++)
			if (array[i] > elem) elem = array[i];
		return elem;
	}
	
	/** get the summation of all elements. */
	public static double getSumAbsElems(double[] array)
	{
		double elem = Math.abs(array[0]);
		int size = array.length;
		for (int i=1; i<size; i++)
			elem += Math.abs(array[i]);
		return elem;
	}

	/** get the summation of all elements. */
	public static float getSumAbsElems(float[] array)
	{
		float elem = Math.abs(array[0]);
		int size = array.length;
		for (int i=1; i<size; i++)
			elem += Math.abs(array[i]);
		return elem;
	}

	/** get the summation of all elements. */
	public static int getSumAbsElems(int[] array)
	{
		int elem = Math.abs(array[0]);
		int size = array.length;
		for (int i=1; i<size; i++)
			elem += Math.abs(array[i]);
		return elem;
	}

	/** Find the maximum elements. */
	public static double getAbsMaxElem(double[] array)
	{
		double elem = array[0];
		int size = array.length;
		for (int i=1; i<size; i++)
			if (Math.abs(array[i]) > Math.abs(elem)) elem = array[i];
		return elem;
	}

	/** Find the maximum elements. */
	public static float getAbsMaxElem(float[] array)
	{
		float elem = array[0];
		int size = array.length;
		for (int i=1; i<size; i++)
			if (Math.abs(array[i]) > Math.abs(elem)) elem = array[i];
		return elem;
	}
	
	/** Find the maximum elements. */
	public static int getAbsMaxElem(int[] array)
	{
		int elem = array[0];
		int size = array.length;
		for (int i=1; i<size; i++)
			if (Math.abs(array[i]) > Math.abs(elem)) elem = array[i];
		return elem;
	}
	


	/** Find the first num maximum elements. */
	public static int[] getMaxElems(int[] array, int num)
	{
		int[] elems = new int[num];
		ArrayList<Integer> arrayTmp = AuxArrayXL.toArrayList(array);
		Collections.sort(arrayTmp); // Sort the arrayTmp
		for (int i=0; i<num; i++)
			elems[i] = arrayTmp.get(arrayTmp.size() - 1 - i);
		return elems;
	}

	/** Find the first num maximum elements. */
	public static float[] getMaxElems(float[] array, int num)
	{
		float[] elems = new float[num];
		ArrayList<Float> arrayTmp = AuxArrayXL.toArrayList(array);
		Collections.sort(arrayTmp); // Sort the arrayTmp
		for (int i=0; i<num; i++)
			elems[i] = arrayTmp.get(arrayTmp.size() - 1 - i);
		return elems;
	}
	
	/** Find the first num minimum elements. */
	public static int[] getMinElems(int[] array, int num)
	{
		int[] elems = new int[num];
		ArrayList<Integer> arrayTmp = AuxArrayXL.toArrayList(array);
		Collections.sort(arrayTmp); // Sort the arrayTmp
		elems = AuxArrayListXL.toIntArray(arrayTmp);
		return elems;
	}

	/** Find the first num minimum elements. */
	public static float[] getMinElems(float[] array, int num)
	{
		float[] elems = new float[num];
		ArrayList<Float> arrayTmp = AuxArrayXL.toArrayList(array);
		Collections.sort(arrayTmp); // Sort the arrayTmp
		elems = AuxArrayListXL.toFloatArray(arrayTmp);
		return elems;
	}

	/** Find the index of the first num maximum elements. */
	public static int[] getIdxOfMaxElems(int[] array, int num)
	{
		if (array == null) return null;
		if (array.length < num) num = array.length;
		int[] max = new int[num];
	    int[] maxIndex = new int[num];
	    Arrays.fill(max, Integer.MIN_VALUE);
	    Arrays.fill(maxIndex, -1);

	    top: for (int i = 0; i < array.length; i++)
	    {
	        for (int j = 0; j < num; j++)
	        {
	            if (array[i] > max[j])
	            {
	                for (int k = num-1; k>j; k--)
	                {
	                    maxIndex[k] = maxIndex[k-1]; 
	                    max[k] = max[k-1];
	                }
	                maxIndex[j] = i; 
	                max[j] = array[i];
	                continue top;
	            }
	        }
	    }
	    return maxIndex;
	}
	
	/** Find the index of the first num maximum elements. */
	public static int[] getIdxOfMaxElems(float[] array, int num)
	{
	    float[] max = new float[num];
	    int[] maxIndex = new int[num];
	    Arrays.fill(max, Float.NEGATIVE_INFINITY);
	    Arrays.fill(maxIndex, -1);

	    top: for (int i = 0; i < array.length; i++)
	    {
	        for (int j = 0; j < num; j++)
	        {
	            if (array[i] > max[j])
	            {
	                for (int k = num-1; k>j; k--)
	                {
	                    maxIndex[k] = maxIndex[k-1]; 
	                    max[k] = max[k-1];
	                }
	                maxIndex[j] = i; 
	                max[j] = array[i];
	                continue top;
	            }
	        }
	    }
	    return maxIndex;
	}
	
	/** Find the index of the first num maximum elements. */
	public static int[] getIdxOfMaxElems(double[] array, int num)
	{
		double[] max = new double[num];
	    int[] maxIndex = new int[num];
	    Arrays.fill(max, Double.NEGATIVE_INFINITY);
	    Arrays.fill(maxIndex, -1);

	    top: for (int i = 0; i < array.length; i++)
	    {
	        for (int j = 0; j < num; j++)
	        {
	            if (array[i] > max[j])
	            {
	                for (int k = num-1; k>j; k--)
	                {
	                    maxIndex[k] = maxIndex[k-1]; 
	                    max[k] = max[k-1];
	                }
	                maxIndex[j] = i; 
	                max[j] = array[i];
	                continue top;
	            }
	        }
	    }
	    return maxIndex;
	}
	
	/**
	 * Return the index where has the maximum number;
	 * If there are multiple same maximum numbers, then,
	 * compare the corresponding number in numbers2nd[],
	 * and return the one which is bigger. 
	 */
	public static int[] getIdxOfMaxElems(float[] array, float[] array2, int num)
	{
	    float[] max = new float[num];
	    int[] maxIndex = new int[num];
	    Arrays.fill(max, Float.NEGATIVE_INFINITY);
	    Arrays.fill(maxIndex, -1);

	    top: for (int i = 0; i < array.length; i++)
	    {
	        for (int j = 0; j < num; j++)
	        {
	            if (array[i] > max[j])
	            {
	                for (int k = num-1; k>j; k--)
	                {
	                    maxIndex[k] = maxIndex[k-1]; 
	                    max[k] = max[k-1];
	                }
	                maxIndex[j] = i; 
	                max[j] = array[i];
	                continue top;
	            } else if (array[i] == max[j] && array2[i] > array2[maxIndex[j]]) {
	                for (int k = num-1; k>j; k--)
	                {
	                    maxIndex[k] = maxIndex[k-1]; 
	                    max[k] = max[k-1];
	                }
	                maxIndex[j] = i; 
	                max[j] = array[i];
	                continue top;
	            }
	        }
	    }
	    return maxIndex;
	}
	
	/** Find the index of the first num minimum elements. */
	public static int[] getIdxOfMinElems(int[] array, int num)
	{
		int tol = Integer.MIN_VALUE;
		return getIdxOfMinElems(array, num, tol);
	}
	
	/** Find the index of the first num minimum elements, which are > tol. */
	public static int[] getIdxOfMinElems(int[] array, int num, int tol)
	{
		if (array == null) return null;
		if (array.length < num) num = array.length;
		int[] min = new int[num];
	    int[] minIndex = new int[num];
	    Arrays.fill(min, Integer.MAX_VALUE);
	    Arrays.fill(minIndex, -1);

	    top: for (int i = 0; i < array.length; i++)
	    {
        	if (array[i] < tol) continue top;
	        for (int j = 0; j < num; j++)
	        {
	            if (array[i] < min[j])
	            {
	                for (int k = num-1; k>j; k--)
	                {
	                	minIndex[k] = minIndex[k-1]; 
	                	min[k] = min[k-1];
	                }
	                minIndex[j] = i; 
	                min[j] = array[i];
	                continue top;
	            }
	        }
	    }
	    return minIndex;
	}
	
	/** Find the index of the first num minimum elements. */
	public static int[] getIdxOfMinElems(float[] array, int num)
	{
		float tol = Float.NEGATIVE_INFINITY;
		return getIdxOfMinElems(array, num, tol);
	}
	
	/** Find the index of the first num minimum elements, which are > tol. */
	public static int[] getIdxOfMinElems(float[] array, int num, float tol)
	{
		if (array == null) return null;
		if (array.length < num) num = array.length;
		float[] min = new float[num];
	    int[] minIndex = new int[num];
	    Arrays.fill(min, Float.POSITIVE_INFINITY);
	    Arrays.fill(minIndex, -1);

	    top: for (int i = 0; i < array.length; i++)
	    {
	        for (int j = 0; j < num; j++)
	        {
	        	if (array[i] < tol) continue top;
	            if (array[i] < min[j])
	            {
	                for (int k = num-1; k>j; k--)
	                {
	                	minIndex[k] = minIndex[k-1]; 
	                	min[k] = min[k-1];
	                }
	                minIndex[j] = i;
	                min[j] = array[i];
	                continue top;
	            }
	        }
	    }
	    return minIndex;
	}

	/** Find the index of the first num minimum elements. */
	public static int[] getIdxOfMinElems(double[] array, int num)
	{
		double tol = Double.NEGATIVE_INFINITY;
		return getIdxOfMinElems(array, num, tol);
	}
	
	/** Find the index of the first num minimum elements, which are > tol. */
	public static int[] getIdxOfMinElems(double[] array, int num, double tol)
	{
		if (array == null) return null;
		if (array.length < num) num = array.length;
		double[] min = new double[num];
	    int[] minIndex = new int[num];
	    Arrays.fill(min, Float.POSITIVE_INFINITY);
	    Arrays.fill(minIndex, -1);

	    top: for (int i = 0; i < array.length; i++)
	    {
	        for (int j = 0; j < num; j++)
	        {
	        	if (array[i] < tol) continue top;
	            if (array[i] < min[j])
	            {
	                for (int k = num-1; k>j; k--)
	                {
	                	minIndex[k] = minIndex[k-1]; 
	                	min[k] = min[k-1];
	                }
	                minIndex[j] = i;
	                min[j] = array[i];
	                continue top;
	            }
	        }
	    }
	    return minIndex;
	}


	/** Return the max length of all arrays[i], where i=0, 1, ... , arrays.length-1. */
	public static int getMaxLength(int[][] arrays)
	{
		int maxLength = 0;
		if (arrays == null) return maxLength;
		int size = arrays.length;
		for (int i=0; i<size; i++)
		{
			if (arrays[i] == null) continue;
			if (arrays[i].length > maxLength) maxLength = arrays[i].length;
		}
		return maxLength;
	}
	
	/** Return the max length of all arrays[i], where i=0, 1, ... , arrays.length-1. */
	public static int getMaxLength(float[][] arrays)
	{
		int maxLength = 0;
		if (arrays == null) return maxLength;
		int size = arrays.length;
		for (int i=0; i<size; i++)
		{
			if (arrays[i] == null) continue;
			if (arrays[i].length > maxLength) maxLength = arrays[i].length;
		}
		return maxLength;
	}
	
	/** Return the max length of all arrays[i], where i=0, 1, ... , arrays.length-1. */
	public static int[] getArraysLength(int[][] arrays)
	{
		if (arrays == null) return null;
		int size = arrays.length;
		int[] arrayLength = new int[size];
		for (int i=0; i<size; i++)
		{
			if (arrays[i] == null) {arrayLength[i] = 0; continue;}
			arrayLength[i] = arrays[i].length;
		}
		return arrayLength;
	}
	
	/** Return the max length of all arrays[i], where i=0, 1, ... , arrays.length-1. */
	public static int[] getArraysLength(float[][] arrays)
	{
		if (arrays == null) return null;
		int size = arrays.length;
		int[] arrayLength = new int[size];
		for (int i=0; i<size; i++)
		{
			if (arrays[i] == null) {arrayLength[i] = 0; continue;}
			arrayLength[i] = arrays[i].length;
		}
		return arrayLength;
	}
	
	/** Return index of rows whose first element is same with indicator number. 
	 * numbers arrays[i][0] are to be checked: 
	 *    e.g.,  arrays[0][0], arrays[1][0], arrays[2][0] ...
	 * */
	public static int[] getIdxOfRowWithSameMark(int[][] arrays, int indicatorNum)
	{
		int size = arrays.length;
		int[] idxs = new int[size];
		int num = 0;
		for (int i=0; i<size; i++)
			if (arrays[i] != null && arrays[i][0] == indicatorNum) idxs[num++] = i;
		return Arrays.copyOf(idxs, num);
	}
	
	/** Return index of rows whose first element is same with indicator number. */
	public static int[] getIdxOfRowWithSameMark(float[][] arrays, float indicatorNum)
	{
		int size = arrays.length;
		int[] idxs = new int[size];
		int num = 0;
		for (int i=0; i<size; i++)
			if (arrays[i] != null && arrays[i][0] == indicatorNum) idxs[num++] = i;
		return Arrays.copyOf(idxs, num);
	}
	
	/** Input array must be with same length.
	 * e.g. the following input is not correct:
	 *      array[0] = [ 2, 3, 4];
	 *      array[1] = [ 6, 7];
	 */
	public static int[][] transposeArray(int[][] array)
	{
		int nRow = array.length;
		int nCol = array[0].length;
		int[][] arrayT = new int[nCol][nRow];
		for (int i=0; i<nRow; i++)
			for (int j=0; j<nCol; j++)
				arrayT[j][i] = array[i][j];
		return arrayT;
	}
	
	/** Input array must be with same length.
	 * e.g. array[0] = [ 2, 3, 4];
	 *      array[1] = [ 6, 7];
	 * Then, this input array is wrong.
	 *   */
	public static float[][] transposeArray(float[][] array)
	{
		int nRow = array.length;
		if (nRow == 0) return array;
		int nCol = array[0].length;
		float[][] arrayT = new float[nCol][nRow];
		for (int i=0; i<nRow; i++)
			for (int j=0; j<nCol; j++)
				arrayT[j][i] = array[i][j];
		return arrayT;
	}
	
	/** Input array must be with same length.
	 * e.g. array[0] = [ 2, 3, 4];
	 *      array[1] = [ 6, 7];
	 * Then, this input array is wrong.
	 *   */
	public static double[][] transposeArray(double[][] array)
	{
		int nRow = array.length;
		if (nRow == 0) return array;
		int nCol = array[0].length;
		double[][] arrayT = new double[nCol][nRow];
		for (int i=0; i<nRow; i++)
			for (int j=0; j<nCol; j++)
				arrayT[j][i] = array[i][j];
		return arrayT;
	}
	
	public static int[] getElemsGivenIndex(int[] array, int[] indices)
	{
		int size = indices.length;
		int[] newArray = new int[size];
		int num = 0;
		for (int i=0; i<size; i++)
		{
			int index = indices[i];
			/*if (index != -1)*/ newArray[num++] = array[index];
		}
		return Arrays.copyOf(newArray, num);
	}
	
	public static float[] getElemsGivenIndex(float[] array, int[] indices)
	{
		int size = indices.length;
		float[] newArray = new float[size];
		int num = 0;
		for (int i=0; i<size; i++)
		{
			int index = indices[i];
			/*if (index > 0)*/ newArray[num++] = array[index];
		}
		return Arrays.copyOf(newArray, num);
	}
	
	/** Format change: from double[] to float[].  */
	public static float[] doubleTofloat(double[] array)
	{
		int size = array.length;
		float[] arrayNew = new float[size];
		for (int i=0; i<size; i++)
		{
			arrayNew[i] = (float) array[i];
		}
		return arrayNew;
	}
	
	/** true if @elem is one of the elememts of @array  */
	public static boolean isElemInArray(int elem, int[] array)
	{
		boolean mark = false;
		int size = array.length;
		for(int i=0; i<size; i++)
		{
			if (elem == array[i])
				{mark = true; break;}
		}
		return mark;
	}

	/** Return an integer array. */
	public static int[] toIntArray(float[] array)
	{
		int size = array.length;
		int[] intArray = new int[size];
		for (int i=0; i<size; i++) {
			intArray[i] = (int) array[i];
		}
		return intArray;
	}
	
	/** Return true if two arrays share the same value */
	public static boolean isTwoArraySame(double[] array1, double[] array2)
	{
		for (int i=0; i<array1.length; i++)
			if (array1[i] != array2[i]) {
				if (Double.isNaN(array1[i]) && Double.isNaN(array2[i])) continue;
				return false;
			}
		return true;
	}

	/** Return true if two arrays share the same value */
	public static boolean isTwoArraySame(double[][] array1, double[][] array2)
	{
		//int idx = 0;
		for (int i=0; i<array1.length; i++)
			{if (isTwoArraySame(array1[i], array2[i])==false) return false;
			//if(Double.isNaN(array1[i][0])) {System.out.println(i);idx++;}
			}
		//System.out.println(idx);
		return true;
	}

	/** Each arrays[i] share the same length */
	public static int[] mergeToOneDim(int[][] arrays)
	{
		int numArray = arrays.length;
		if (numArray == 1) return arrays[0];
		int numElem = arrays[0].length;
		int[] newArray = new int[numArray*numElem];
		int idx = 0;
		for (int i=0; i<arrays.length; i++)
			for (int j=0; j<arrays[i].length; j++)
				newArray[idx++] = arrays[i][j];
		return newArray;
	}
	
	/** remove duplicate element */
	public static int[] removeDupElems(int[] array)
	{
		int[] newArray = new int[array.length];
		int num = 0;
		newArray[num++] = array[0];
		for (int i=1; i<array.length; i++) {
			boolean isDup = false;
			for (int j=0; j<i; j++) {
				if (array[j] == array[i]) {isDup = true; break;}
			}
			if (isDup == false) newArray[num++] = array[i];
		}
		newArray = Arrays.copyOf(newArray, num);
		return newArray;
	}
	
	
}
