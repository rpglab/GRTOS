package com.sced.input;

import java.io.File;
import java.io.IOException;

import com.sced.input.dev.BranchesXL;
import com.sced.input.dev.BusesXL;
import com.sced.input.dev.BranchContingenciesXL;
import com.sced.input.dev.GeneratorsXL;
import com.sced.input.dev.InterfacesXL;
import com.sced.input.dev.LoadsXL;
import com.sced.util.ReadFilesInAFolderXL;
import com.utilxl.log.DiaryXL;
import com.utilxl.log.LogTypeXL;

/**
 * 
 * User defined input data format.
 * 
 * @author Xingpeng Li, xplipower@gmail.com 
 *
 */
public class ReadModelDataXL {

	DiaryXL _diary;
	String _folderPath;
	boolean _heading;  // true if first line is heading
	
	BusesXL _buses;
	BranchesXL _branches;
	GeneratorsXL _gens;
	LoadsXL _loads;
	
	BranchContingenciesXL _contingencies;
	InterfacesXL _interfaces;
	
	public ReadModelDataXL(String folderPath, DiaryXL diary) {
		_diary = diary;
		_folderPath = folderPath;
		initial();
		_diary.hotLine(LogTypeXL.MileStone, "Initialized an instance for class ReadModelData ...");
	}
	
	private void initial() {
		_buses = new BusesXL(this);
		_branches = new BranchesXL(this);
		_gens = new GeneratorsXL(this);
		_loads = new LoadsXL(this);

		_contingencies = new BranchContingenciesXL(this);
		_interfaces = new InterfacesXL(this);
	}

	public void readData() {
//		ReadFilesInAFolder readFiles = new ReadFilesInAFolder("C:\\Users\\xingpeng\\data\\TVASystem\\raw", true);
		ReadFilesInAFolderXL readFiles = new ReadFilesInAFolderXL(_folderPath, false);
		readFiles.readFileNames();
		File[] files = readFiles.getFiles();
		String[] names = readFiles.getFileNamesLowerCase();
		
		/* A list of expected file names (extension is dropped) */
		String busFileName = "bus.txt";
		String branchFileName = "branch.txt";
		String contingencyFileName = "contingency.txt";
		String contingencylineFileName = "contingencyline.txt";
		String genFileName = "gen.txt";
		String gencostFileName = "gencost.txt";
		String interfaceFileName = "interface.txt";
		String interfacelineFileName = "interfaceline.txt";
		String loadFileName = "load.txt";
		
		try {
			int idxBusFile = findFile(names, busFileName);
			int idxGenFile = findFile(names, genFileName);
			int idxContingencyFile = findFile(names, contingencyFileName);
			int idxInterfaceFile = findFile(names, interfaceFileName);
			_buses.readData(files[idxBusFile]);
			_gens.readData(files[idxGenFile]);
			_contingencies.readData(files[idxContingencyFile]);
			if (idxInterfaceFile != -1) _interfaces.readData(files[idxInterfaceFile]);
			
			for (int i=0; i<files.length; i++) {
				String name = names[i];
//				int idxDOT = name.lastIndexOf(".");
//				name = name.substring(0, idxDOT);
				if (i != idxBusFile && i != idxGenFile && i != idxContingencyFile && i != idxInterfaceFile) {
					if (name.equals(branchFileName)) _branches.readData(files[i]);
					else if (name.equals(loadFileName)) _loads.readData(files[i]);
					else if (name.equals(gencostFileName)) _gens.readDataGensCost(files[i]);
					else if (name.equals(contingencylineFileName)) _contingencies.readDataCtgcyElem(files[i]);
					else if (name.equals(interfacelineFileName)) _interfaces.readDataInterfaceElem(files[i]);
					else _diary.hotLine(LogTypeXL.Warning, "The program does not read file "+name+", either not needed or file name issue ...");
				}
			}
			_diary.hotLine(LogTypeXL.MileStone, "Finish reading all input data ... ");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private int findFile(String[] names, String fileName)
	{
		for (int i=0; i<names.length; i++) {
			if (names[i].equals(fileName)) return i;
		}
		return -1;
	}
	
	public void setIsHasHeading(boolean h) {_heading = h;}
	public boolean hasHeading() {return _heading;}

	public DiaryXL getDiary() {return _diary;}
	public BusesXL getBuses() {return _buses;}
	public BranchesXL getBrances() {return _branches;}
	public GeneratorsXL getGenerators() {return _gens;}
	public LoadsXL getLoads() {return _loads;}
	public BranchContingenciesXL getBranchContingencies() {return _contingencies;}
	public InterfacesXL getInterfaces() {return _interfaces;}
	
	public static void main(String[] agrc)
	{
		DiaryXL diary = new DiaryXL();
		diary.initial();

		ReadModelDataXL read = new ReadModelDataXL("input/sced_ver01/small_case_sced_data/w_title", diary);
		read.setIsHasHeading(true);
		read.readData();
		
		/* Program ends here */
		diary.done();
		System.out.println("Program ends here.");
	}
	
	
}
