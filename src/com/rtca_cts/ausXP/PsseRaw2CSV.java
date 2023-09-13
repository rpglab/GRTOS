package com.rtca_cts.ausXP;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;

import com.powerdata.openpa.psseraw.Psse2CSV;
import com.utilxl.iofiles.AuxFileXL;
import com.utilxl.string.AuxStringXL;

/**
 * Convert Psse .raw file to .csv format outputs.
 * 
 * @author, Xingpeng.Li
 * 
 */
public class PsseRaw2CSV {
	
	/** 
	 * Convert Psse .raw file to .csv format outputs using Psse2CSV from openpa.
	 * 
	 * @param inputRawFile : path to raw file.
	 * @param outputCSVPath : path/folder where the .csv would be output.
	 * */
	public static void launch(String inputRawFile, String outputCSVPath) throws Exception
	{
		File cwd = new File(System.getProperty("user.dir"));
		File outdir = new File(outputCSVPath);
		String spsse = inputRawFile;
		String sversion = null;

		File psse = Psse2CSV.resolveInputFile(cwd, spsse);
		Reader rpsse = new BufferedReader(new FileReader(psse));
		Psse2CSV p2c = new Psse2CSV(rpsse, sversion, outdir);
		
		p2c.process();
		rpsse.close();
		p2c.cleanup();
	}
	
	
	public static void main(String args[]) throws Exception
	{
		String input = null;
		String output = null;
		for(int i=0; i < args.length;)
		{
			String s = args[i++].toLowerCase();
			int ssx = 1;
			if (s.startsWith("--")) ++ssx;
			switch(s.substring(ssx))
			{
				case "input":
					input = args[i++];
					break;
				case "output":
					output = args[i++];
					break;
			}
		}
		output = AuxStringXL.endWith(output, "/");
		output = output + "tmpFolder";
		AuxFileXL.createFolder(output);

		PsseRaw2CSV.launch(input, output);
		System.out.println("PsseRaw to CSV conversion is done here.");
	}
	
	
}
