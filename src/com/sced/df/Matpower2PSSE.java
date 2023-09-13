package com.sced.df;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import com.powerdata.openpa.tools.SimpleCSV;
import com.utilxl.log.DiaryXL;

/**
 * Initialized in March 2017.
 * 
 * Convert Matpower format based files into PSS/E ver30 raw file
 * The input are three .csv files for bus, branch, and generator.
 * 
 * @author Xingpeng Li (xplipower@gmail.com)
 *
 */
public class Matpower2PSSE {

	DiaryXL _diary;
	String _busFilePath;
	String _genFilePath;
	String _brcFilePath;
	
	public Matpower2PSSE(DiaryXL diary, String busFilePath, String genFilePath, String brcFilePath)
	{
		_diary = diary;
		_busFilePath = busFilePath;
		_genFilePath = genFilePath;
		_brcFilePath = brcFilePath;
	}
	
	private PrintWriter createPrintWriter(String fileName)
	{
		File pout = new File(fileName);
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(new BufferedWriter(new FileWriter(pout)));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return pw;
	}
	
	public void launch() throws IOException
	{
		String blank12SingleQuote = "'            '";
		
		SimpleCSV busCSV = new SimpleCSV(_busFilePath);
		int[] busIdx = busCSV.getInts("bus_i");
		int nbuses = busIdx.length;
		String caseName = "case" + nbuses+ "busPsse30";
		PrintWriter pw = createPrintWriter(caseName + ".raw");

		pw.println("0, 100.0/RAWD VER 30 EXPORT BY Xingpeng.Li (xplipower@gmail.com)");
		pw.println("Date of convertion (yyyy-MM-dd HH:mm:ss): "+ _diary.getCurrentTimeStamp());
		pw.println(nbuses+" Buses Test Case");
		
		/* Bus data */
		int[] busType = busCSV.getInts("type");
		float[] Gs = busCSV.getFloats("Gs");
		float[] Bs = busCSV.getFloats("Bs");
		float[] vm = busCSV.getFloats("Vm");
		float[] va = busCSV.getFloats("Va");
		float[] baseKV = busCSV.getFloats("baseKV");
		int[] area = busCSV.getInts("area");
		int[] zone = busCSV.getInts("zone");
		for (int n=0; n<nbuses; n++)
		{
			String busName = "Bus_" + busIdx[n];
			if (busName.length() < 12) busName = String.format("%1$-" + 12 + "s", busName);
			pw.print("	"+busIdx[n] +",	'"+busName +"',	"+baseKV[n] +",	"+busType[n] + ",	"+Gs[n] + ",	"+Bs[n]);
			pw.println(",	"+area[n] +",	"+zone[n] +",	"+vm[n] +",	"+va[n] + ",	"+1);
		}
		pw.println("0 / END OF BUS DATA, BEGIN LOAD DATA");

		/* Load data */
		float[] Pd = busCSV.getFloats("Pd");
		float[] Qd = busCSV.getFloats("Qd");
		int nloads = Pd.length;
		for (int i=0; i<nloads; i++)
		{
			if ((Pd[i] == 0) && (Qd[i] == 0)) continue;
			pw.print("	"+busIdx[i] +",	'1',	1,	"+area[i]+",	" +zone[i]);
			pw.println(",	"+Pd[i] +",	"+Qd[i] +",	0.0,	0.0,	0.0,	0.0,	1");
		}
		pw.println("0 / END OF LOAD DATA, BEGIN GENERATOR DATA");

		/* Generator data */
		SimpleCSV genCSV = new SimpleCSV(_genFilePath);
		int[] bus = genCSV.getInts("bus");
		float[] Pg = genCSV.getFloats("Pg");
		float[] Qg = genCSV.getFloats("Qg");
		float[] Qgmax = genCSV.getFloats("Qmax");
		float[] Qgmin = genCSV.getFloats("Qmin");
		float[] Vg = genCSV.getFloats("Vg");
		float[] mBase = genCSV.getFloats("mBase");
		int[] stGen = genCSV.getInts("status");
		float[] Pgmax = genCSV.getFloats("Pmax");
		float[] Pgmin = genCSV.getFloats("Pmin");
		int ngens = bus.length;
		
		HashMap<Integer, Integer> genID = new HashMap<Integer, Integer>();
		for (int g=0; g<ngens; g++)
		{
			int id = 1;
			if (genID.containsKey(bus[g])) {
				id = genID.get(bus[g]) + 1;
				genID.replace(bus[g], id);
			} else genID.put(bus[g], id);
			pw.print("	"+bus[g] +",	'"+id+ "',	"+Pg[g]+ ",	"+Qg[g]+ ",	"+Qgmax[g]+ ",	"+Qgmin[g]+ ",	"+Vg[g]);
			pw.println(",	0,	"+mBase[g] +",	0.0,	1.0,	0.0,	0.0,	1.0,	" +stGen[g]+ ",	100.0,	"+Pgmax[g]+",	"+Pgmin[g]);
		}
		pw.println("0 / END OF GENERATOR DATA, BEGIN BRANCH DATA");
		
		/* Line data */
		SimpleCSV brcCSV = new SimpleCSV(_brcFilePath);
		int[] fbus = brcCSV.getInts("fbus");
		int[] tbus = brcCSV.getInts("tbus");
		float[] r = brcCSV.getFloats("r");
		float[] x = brcCSV.getFloats("x");
		float[] b = brcCSV.getFloats("b");
		float[] rateA = brcCSV.getFloats("rateA");
		float[] rateB = brcCSV.getFloats("rateB");
		float[] rateC = brcCSV.getFloats("rateC");
		int[] stBrc = brcCSV.getInts("status");

		int nBrc = fbus.length;
		int numXfm = 0;
		int[] idxXfm = new int[nBrc];
		HashMap<Integer, ArrayList<Integer>> mapLine = new HashMap<Integer, ArrayList<Integer>>();
		for (int i=0; i<nBrc; i++)
		{
			if (isXfm(baseKV, fbus[i], tbus[i]) == true) {
				idxXfm[numXfm++] = i;
				continue;
			}
			int id = getBrcID(mapLine, fbus[i], tbus[i]);
			pw.print("	"+fbus[i] +",	"+tbus[i]+ ",	"+id + ",	"+r[i]+ ",	"+x[i]+ ",	"+b[i]);
			pw.println(",	"+rateA[i]+ ",	"+rateB[i]+ ",	"+rateC[i]+",	0.0,	0.0,	0.0,	0.0,	"+stBrc[i] +",	0.0");
		}
		idxXfm = Arrays.copyOf(idxXfm, numXfm);
		pw.println("0 / END OF BRANCH DATA, BEGIN TRANSFORMER DATA");
		
		/* Xfm data */
		float[] ratio = brcCSV.getFloats("ratio");
		float[] angle = brcCSV.getFloats("angle");
		HashMap<Integer, ArrayList<Integer>> mapXfm = new HashMap<Integer, ArrayList<Integer>>();
		for (int i=0; i<numXfm; i++)
		{
			int idx = idxXfm[i];
			int id = getBrcID(mapXfm, fbus[idx], tbus[idx]);
			float ratioAdj = ratio[idx];
			if (Math.abs(ratioAdj) < 0.1f) ratioAdj = 1.0f;
			
			pw.println("	"+fbus[idx] +",	"+tbus[idx]+ ",	0,	'"+id + "',	1,	1,	1,	0.0,	"+ b[idx]+ ",	2,	"+ blank12SingleQuote+ ",	" +stBrc[idx]);
			pw.println(r[idx]+ ",	"+x[idx]+ ",	100.0");
			pw.println(ratioAdj+",	0.00, "+angle[idx]+",	"+rateA[idx]+ ",	"+rateB[idx]+ ",	"+rateC[idx] +",	0,	0,	1.100,	0.900,	1.1,	0.9,	33,	0,	0.0,	0.0");
			pw.println("1.000,	0.000");
		}
		pw.println("0 / END OF TRANSFORMER DATA, BEGIN AREA DATA");
		
		pw.println("    1,	8, 0.0,	10.0,"+blank12SingleQuote);    //TODO: this is hard code, may change it per need.
		pw.println("    2,	21, 0.0,	10.0,"+blank12SingleQuote);
		pw.println("0 / END OF AREA DATA, BEGIN TWO-TERMINAL DC DATA");
		pw.println("0 / END OF TWO-TERMINAL DC DATA, BEGIN VSC DC LINE DATA");
		pw.println("0 / END OF VSC DC LINE DATA, BEGIN SWITCHED SHUNT DATA");
		pw.println("0 / END SWITCHED SHUNT DATA, BEGIN IMPEDANCE CORRECTION TABLE DATA");
		pw.println("0 / END OF IMPEDANCE CORRECTION TABLE DATA, BEGIN MULTI-TERMINAL DC DATA");
		pw.println("0 / END OF MULTI-TERMINAL DC DATA, BEGIN MULTI-SECTION LINE DATA");
		pw.println("0 / END OF MULTI-SECTION LINE DATA, BEGIN ZONE DATA");
		pw.println("    1,'Pseudo_Zone '");  //TODO: this is hard code, may change it per need.
		pw.println("0 / END OF ZONE DATA, BEGIN INTER-AREA TRANSFER DATA");
		pw.println("0 / END OF INTER-AREA TRANSFER DATA, BEGIN OWNER DATA");
		pw.println("0 / END OF OWNER DATA, BEGIN FACTS DEVICE DATA");
		pw.println("0 / END OF FACTS DATA");
		pw.println("0 /");
		pw.print("\n");
		pw.close();
		
		System.out.println("Create raw file successfully.");
	}
	
	private boolean isXfm(float[] baseKV, int fbus, int tbus)
	{
		if (Math.abs(baseKV[fbus-1]-baseKV[tbus-1]) > 1.0f) return true;
		else return false;
	}
	
	private int countItem(int item, ArrayList<Integer> arrayTmp)
	{
		int count = 0;
		for (Integer temp : arrayTmp)
			if (temp == item) count++;
		return count;
	}
	
	private int getBrcID(HashMap<Integer, ArrayList<Integer>> mapLine, int fbus, int tbus)
	{
		int id = 1;
		if (mapLine.containsKey(fbus)) {
			ArrayList<Integer> arrayTmp = mapLine.get(fbus);
			int count = countItem(tbus, arrayTmp);
			if (count != 0) id = count + 1;
			arrayTmp.add(tbus);
		} else {
			ArrayList<Integer> arrayTmp = new ArrayList<Integer>();
			arrayTmp.add(tbus);
			mapLine.put(fbus, arrayTmp);
		}
		return id;
	}
	
	
	static public void main(String args[]) throws IOException
	{
		DiaryXL diary = new DiaryXL();
		diary.initial();
		
//		String busFilePath = "case179Bus.csv";
//		String genFilePath = "case179Gen.csv";
//		String brcFilePath = "case179Branch.csv";

		String busNumber = "2383";
		String busFilePath = "case" + busNumber + "Bus.csv";
		String genFilePath = "case" + busNumber + "Gen.csv";
		String brcFilePath = "case" + busNumber + "Branch.csv";
		
		Matpower2PSSE converter = new Matpower2PSSE(diary, busFilePath, genFilePath, brcFilePath);
		converter.launch();
		
		System.out.println("Simulation is done here");
	}
	
}
