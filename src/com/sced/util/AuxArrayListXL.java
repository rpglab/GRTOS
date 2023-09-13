package com.sced.util;

import java.util.ArrayList;



/**
 * Auxiliary methods : for ArrayList
 * 
 * @author Xingpeng.Li (xplipower@gmail.com)
 *
 */
public class AuxArrayListXL {

	public AuxArrayListXL() {  }

	/** get elements based on the indices provided */
	public static int[] getElemBasedOnIndex(ArrayList<Integer> array, int[] ndx)
	{
		int size = ndx.length;
		int[] elem = new int[size];
		for (int i=0; i<size; i++)
		{
			int a = ndx[i];
			elem[i] = array.get(a);
		}
		return elem;
	}
	
	/** Add all elements to the dummy variable @dynArray */
	public static ArrayList<Integer> addElems(ArrayList<Integer> dynArray, int[] toBeAdded)
	{
		int size = toBeAdded.length;
		for (int i=0; i<size; i++)
			dynArray.add(toBeAdded[i]);
		return dynArray;
	}
	/** Add all elements to the dummy variable @dynArray */
	public static ArrayList<Integer> addElems(ArrayList<Integer> dynArray, ArrayList<Integer> toBeAdded)
	{
		int size = toBeAdded.size();
		for (int i=0; i<size; i++)
			dynArray.add(toBeAdded.get(i));
		return dynArray;
	}

	/** Elements which are not in the dummy variable @dynArray would be added to it. */
	public static ArrayList<Integer> addDiffElems(ArrayList<Integer> dynArray, int[] toBeAdded)
	{
		int size = toBeAdded.length;
		for (int i=0; i<size; i++)
			if (dynArray.contains(toBeAdded[i]) == false) dynArray.add(toBeAdded[i]);
		return dynArray;
	}

	/** Elements which are not in the dummy variable @dynArray would be added to it. */
	public static ArrayList<Integer> addDiffElems(ArrayList<Integer> dynArray, ArrayList<Integer> toBeAdded)
	{
		int size = toBeAdded.size();
		for (int i=0; i<size; i++)
		{
			if (dynArray.contains(toBeAdded.get(i)) == false) dynArray.add(toBeAdded.get(i));			
		}
		return dynArray;
	}
	
	/** Change an ArrayList<Integer> to an int[]. */
	public static int[] toIntArray(ArrayList<Integer> dynArray)
	{
		int size = dynArray.size();
		int[] array = new int[size];
		for (int i=0; i<size; i++)
			array[i] = dynArray.get(i);
		return array;
	}
	
	/** Change an ArrayList<Float> to an float[]. */
	public static float[] toFloatArray(ArrayList<Float> dynArray)
	{
		int size = dynArray.size();
		float[] array = new float[size];
		for (int i=0; i<size; i++)
			array[i] = dynArray.get(i);
		return array;
	}
	
	public static ArrayList<Integer> ShrinkSize(ArrayList<Integer> dynArray, int num)
	{
		int size = dynArray.size();
		for (int i=size; i>num; i--)
			dynArray.remove(i-1);
		return dynArray;
	}
	
	
	

}
