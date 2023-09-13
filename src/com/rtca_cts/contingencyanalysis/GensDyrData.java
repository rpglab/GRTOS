package com.rtca_cts.contingencyanalysis;


import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Scanner;

import org.junit.Test;

/**
 * 
 * @author Xingpeng.Li (xplipower@gmail.com)
 * 
 */
public class GensDyrData {
	
	int _numGen;
	
	int[] _genIndex;         // starts from 0
	int[] _genBusIndex;      // starts from 0 if gen exists at bus 0. 
	int[] _genBusOrigIndex;  // Bus number of gen in the original dataset
	int[] _genId;            // gen ID at one same bus
	float[] _genMH;           // Parameter, H (inertia) - machine MVA based
	float[] _MachineMVA;      // Gen - Machine MVA
	float _SystemMVA = 100;         // System MVA
	float[] _genSH;           // Parameter, H (inertia) - system MVA based
	
	GensDyrData()
	{
		readData();
	}

	@Test
	private void readData()
	{
		String rootDr=(System.getProperty("user.dir"));
		try 
		{
			File dyrFile = new File(rootDr+"\\TVA_dyr.txt");
			InputStream dyrInput= new FileInputStream(dyrFile);
			Scanner dyrData = new Scanner(dyrInput);
			int idx = 0;
			int capacity = 10000;
			int[] genIndex = new int[capacity];
			int[] genBusIndex = new int[capacity];
			int[] genBusOrigIndex = new int[capacity];
			int[] genId = new int[capacity];
			float[] genMH = new float[capacity];
		    while (dyrData.hasNextLine())
		    {
		    	genIndex[idx] = dyrData.nextInt() - 1;
		    	genBusIndex[idx] = dyrData.nextInt() - 1;
		    	genBusOrigIndex[idx] = dyrData.nextInt();			
		    	genId[idx] = dyrData.nextInt();
		    	genMH[idx] = dyrData.nextFloat();
		    	idx++;
		    }
		    _numGen = idx;
		    assertTrue(idx < capacity);
		    _genIndex = Arrays.copyOf(genIndex, idx);
		    _genBusIndex = Arrays.copyOf(genBusIndex, idx);
			_genBusOrigIndex = Arrays.copyOf(genBusOrigIndex, idx);
			_genId = Arrays.copyOf(genId, idx);
			_genMH = Arrays.copyOf(genMH, idx);

			dyrData.close();
			dyrInput.close();
			System.out.println("Read Gen Dynamic data successfully.");
		} catch (IOException Err1) {
			System.out.println();
			System.out.println("Error loading Gen .dyr file!"+ Err1);
			System.out.println();
		}
	}
	
	public void setGenSystemMVA(float SystemMVA) { _SystemMVA = SystemMVA;}

	/** The H (inertia) will be automatically updated whenever invoke this method */
	public void setGenMachineMVA(float[] MachineMVA)
	{
		_MachineMVA = MachineMVA;
		assertTrue(MachineMVA.length == _genMH.length);
		assertTrue(MachineMVA.length == _numGen);
		if (_genSH == null) _genSH = new float[_numGen];
		for (int i=0; i<_genMH.length; i++)
		{
			_genSH[i] = _genMH[i] * MachineMVA[i] / _SystemMVA;
		}
	}
		
	public int getNumGens() {return _numGen;}
	public int[] getGenIndex() {return _genIndex;}
	public int[] getGenBusIndex() {return _genBusIndex;}
	public int[] getGenBusOrigIndex() {return _genBusOrigIndex;}
	public int[] getGenId() {return _genId;}
	public float[] getGenMachineMVA() {return _MachineMVA;}
	public float[] getGenMH() {return _genMH;}
	public float getGenSystemMVA() {return _SystemMVA;}
	public float[] getGenSH() {return _genSH;}
	
	public static void main(String[] args)
	{
		GensDyrData test = new GensDyrData();
		test.getNumGens();
	}
	
}
