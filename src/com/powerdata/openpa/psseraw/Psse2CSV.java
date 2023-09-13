package com.powerdata.openpa.psseraw;

/**
 * 

This class/code is from OpenPA version 1; the associated copyright is provided below:

Copyright (c) 2016, PowerData Corpration, Incremental Systems Corporation All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following 
conditions are met:

Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.

Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following 
disclaimer in the documentation and/or other materials provided with the distribution.

Neither the name of cmtest1 nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, 
BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO 
EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR 
TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
POSSIBILITY OF SUCH DAMAGE.

 *
 */


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.HashMap;
/**
 * Implementation of a PsseProcessor that generates CSV files.
 * 
 * @author chris@powerdata.com
 *
 */
public class Psse2CSV extends PsseProcessor
{
	PsseCSVWriter _wrtr;
	
	public Psse2CSV(Reader rawpsse, String specversion, File outdir) throws IOException,
			PsseProcException
	{
		super(rawpsse, specversion);
		_wrtr = new PsseCSVWriter(outdir);
	}

	@Override
	protected PsseRecWriter getWriter(String psseClassName) {return _wrtr;}

	public void cleanup() {_wrtr.cleanup();}

	public static void main(String[] args) throws Exception
	{
		File cwd = new File(System.getProperty("user.dir"));
		File outdir = cwd;
		String spsse = null;
		String sversion = null;

		int narg = args.length;
		for (int i = 0; i < narg;)
		{
			String a = args[i++];
			if (a.startsWith("-"))
			{
				int idx = (a.charAt(1) == '-') ? 2 : 1;
				switch (a.substring(idx))
				{
					case "d":
					case "dir":
					case "directory":
						outdir = new File(args[i++]);
						break;
					case "p":
					case "psse":
						spsse = args[i++];
						break;
					case "v":
					case "ver":
					case "version":
						sversion = args[i++];
						break;
					case "h":
					case "help":
						showHelp(false);
					default:
						System.out
								.println("parameter " + a + " not understood");
						showHelp(true);
				}
			}
		}

		File psse = resolveInputFile(cwd, spsse);
		if (psse == null)
		{
			System.err.println("Unable to locate pss/e file");
			showHelp(true);
		}

		Reader rpsse = new BufferedReader(new FileReader(psse));
		Psse2CSV p2c = new Psse2CSV(rpsse, sversion, outdir);
		
		PsseHeader hdr = p2c.getHeader();
		System.out.println("Loading File: "+psse);
		System.out.println("Change Code: "+hdr.getChangeCode());
		System.out.println("System Base MVA: "+hdr.getSystemBaseMVA());
		System.out.format("Case Time: %tc\n", hdr.getCaseTime());
		System.out.format("Import Time: %tc\n", hdr.getImportTime());
		System.out.println("Heading 1: "+hdr.getHeading1());
		System.out.println("Heading 2: "+hdr.getHeading2());
		String hver = hdr.getVersion();
		if (hver == null)
		{
			hver = String.format("%s - overridden", sversion);
		}
		System.out.println("Version: "+hver);
		
		p2c.process();
		rpsse.close();
		p2c.cleanup();
				
	}

	//TODO: Xingpeng has changed "protected" to "public"
	public static File resolveInputFile(File cwd, String spsse)
	{
		File psse = null;
		if (spsse != null)
		{
			psse = new File(spsse);
		}
		else
		{
			File[] flist = cwd.listFiles(new FilenameFilter()
			{
				@Override
				public boolean accept(File dir, String name)
				{
					return name.toLowerCase().endsWith(".raw");
				}
			});
			if (flist.length > 0)
				psse = flist[0];
		}
		return psse;
	}

	private static void showHelp(boolean err)
	{
		System.out
				.println("Psse2CSV --dir output_directory "+
						"--psse raw_psse_file [ --force-version use-version ] [ --help ]");
		System.exit(err ? 1 : 0);
	}
}

class PsseCSVWriter implements PsseRecWriter
{
	protected static final char _FldDelim = ',';
	protected static final char _QuoteChar = '\'';
	
	protected File _dir;
	protected HashMap<String,PrintWriter> _fmap = new HashMap<>();
	
	public PsseCSVWriter(File dir) { _dir = dir; }

	@Override
	public void writeRecord(PsseClass pclass, String[] record)
			throws PsseProcException
	{
		try
		{
			PrintWriter out = getWriter(pclass);
			int irec = 0;
			for (PsseField[] line : pclass.getLines())
			{
				for (PsseField fld : line)
				{
					if (irec > 0) out.print(_FldDelim); 
					boolean quote = fld.getType() == PsseFieldType.String;
					if (quote) out.print(_QuoteChar);
					out.print(record[irec++]);
					if (quote) out.print(_QuoteChar);
				}
			}
			out.println();
		} catch (IOException ioe)
		{
			throw new PsseProcException(ioe);
		}
	}

	@Override
	public void cleanup()
	{
		for (PrintWriter pw : _fmap.values())
			pw.close();

	}

	protected PrintWriter getWriter(PsseClass pc) throws IOException
	{
		String clname = pc.getClassName();
		PrintWriter pw = _fmap.get(clname);
		if (pw == null)
		{
			File f = new File(_dir, clname+".csv");
			pw = new PrintWriter(new BufferedWriter(new FileWriter(f)));
			int irec=0;
			for (PsseField[] line : pc.getLines())
			{
				for (PsseField fld : line)
				{
					if (irec++ > 0) pw.print(_FldDelim);
					pw.print(_QuoteChar);
					pw.print(fld.getName());
					pw.print(_QuoteChar);
				}
				
			}
			pw.println();
			_fmap.put(clname, pw);
		}
		return pw;
	}

}
