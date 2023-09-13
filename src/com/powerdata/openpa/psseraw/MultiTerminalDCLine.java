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

/**
 * PsseClass that can parse and process raw PSS/e MultiTerminal DC Line records
 * 
 * @author chris@powerdata.com
 * 
 */
public class MultiTerminalDCLine extends PsseClass
{
	public MultiTerminalDCLine() {super("MultiTerminalDCLine");}

	@Override
	public void processRecords(LineNumberReader rdr, PsseRecWriter wrtr,
			PsseClassSet cset) throws PsseProcException
	{
		PsseField[] fields = getLines().get(0);
		String[] rec = new String[fields.length];
		try
		{
			String l = readLine(rdr);
			while (isRecord(l))
			{
				loadTokens(rec, 0, fields, l, 0);
				wrtr.writeRecord(this, rec);

				processInner(rdr, wrtr, cset.getMultiTermDC_ACConv(), rec[1], rec[0]);
				processInner(rdr, wrtr, cset.getMultiTermDCBus(), rec[2], rec[0]);
				processInner(rdr, wrtr, cset.getMultiTermDCLink(), rec[3], rec[0]);

				l = readLine(rdr);
			}
		} catch (IOException ioe) { throw new PsseProcException(ioe); }
	}

	protected void processInner(LineNumberReader rdr, PsseRecWriter wrtr,
			PsseClass inner, String scount, String dcline) throws IOException, PsseProcException
	{
		int count = Integer.parseInt(scount);
		PsseField[] fields = inner.getLines().get(0);
		for (int i=0; i < count; ++i)
		{
			String[] rec = new String[fields.length+1];
			loadTokens(rec, 1, fields, readLine(rdr), 0);
			rec[0] = dcline;
			wrtr.writeRecord(inner, rec);
		}
	}

	
}
