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



import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.powerdata.openpa.tools.StringParse;

/**
 * Track PSS/e fields for a given object type.  Allows a single record to be
 * specified over multiple lines.  Lines can contain a variable list of related
 * records.
 * 
 * @author chris@powerdata.com
 *
 */
public class PsseClass
{
	protected static final String parseDelim = ",";
	protected static final char quoteChar = '\'';
	
	protected String _classnm;
	protected ArrayList<PsseField[]> _lines = new ArrayList<>();

	
	public PsseClass(String classname)
	{
		_classnm = classname;
	}
	
	public void addLine(PsseField[] fields)
	{
		_lines.add(fields);
	}
	
	public String getClassName() {return _classnm;}
	public List<PsseField[]> getLines() {return _lines;}
	

	

	protected String readLine(LineNumberReader r) throws IOException {return r.readLine().trim();}
	
	public void processRecords(LineNumberReader rdr, PsseRecWriter wrtr,
			PsseClassSet cset) throws PsseProcException
	{
		List<PsseField[]> lines = getLines();
		int nfld = 0;
		for (PsseField[] line : lines)
			nfld += line.length;
		String[] rv = null;

		try
		{
			String l = readLine(rdr);
			while(isRecord(l))
			{
				rv = new String[nfld];
				int rvofs = 0;
				int iline = 0;
				while (l != null)
				{
					PsseField[] pl = lines.get(iline++);
					rvofs = loadTokens(rv, 0, pl, l, rvofs);
					l = hasLine(iline, rv) ? readLine(rdr) : null;
					Arrays.fill(rv, rvofs, rv.length, "");
				}
				wrtr.writeRecord(this, rv);
				l = readLine(rdr);
			}
		} catch (IOException ex)
		{
			throw new PsseProcException(ex);
		}
	}
	
	protected int loadTokens(String[] rec, int recstart, PsseField[] pl, String l, int rvofs)
	{
		rvofs += recstart;
		int endofs = rvofs + pl.length;
		StringParse sp = parseLine(l);
		while (sp.hasMoreTokens() && rvofs < endofs)
			rec[rvofs++] = sp.nextToken().trim();
		Arrays.fill(rec, rvofs, endofs, "");
		return endofs;
		
	}
	
	protected boolean hasLine(int lineno, String[] vals) {return lineno < _lines.size();}

	protected boolean isRecord(String l)
	{
		boolean rv = true;
		
		// look for comments, remove and re-trim;
		int ndx = l.indexOf('/');
		if (ndx != -1 && l.substring(0, ndx).trim().equals("0"))
			rv = false;
		return rv;
	}

	protected StringParse parseLine(String line)
	{
		StringParse sp = new StringParse(line, parseDelim);
		sp.setQuoteChar(quoteChar);
		return sp;
	}

	@Override
	public String toString() {return getClassName();}
	

}