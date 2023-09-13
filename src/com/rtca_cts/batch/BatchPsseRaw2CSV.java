package com.rtca_cts.batch;

import com.rtca_cts.ausXP.PsseRaw2CSV;
import com.utilxl.iofiles.AuxFileXL;
import com.utilxl.iofiles.ReadFilesInAFolder;
import com.utilxl.string.AuxStringXL;

public class BatchPsseRaw2CSV {

	
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
		
		ReadFilesInAFolder readFiles = new ReadFilesInAFolder(input, true);
		readFiles.readFileNames();
		String[] fileNames = readFiles.getAllRawFileNames();
		
		String outputOrig = AuxStringXL.endWith(output, "/");
		String inputOrig = AuxStringXL.endWith(input, "/");
		for (int i=0; i<fileNames.length; i++)
		{
			output = outputOrig + fileNames[i] + "_CSV";
			AuxFileXL.createFolder(output);
			input = inputOrig + fileNames[i];
			PsseRaw2CSV.launch(input, output);
		}
		System.out.format(" %d PsseRaw files to CSV conversion is done here.", fileNames.length);
	}
	
}
