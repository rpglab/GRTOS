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
import java.io.Reader;

/**
 * Provide a starting point to process a pss/e file.  Subclass and provide appropriate writers for specific applications
 * 
 * @author chris@powerdata.com
 * 
 */
public abstract class PsseProcessor
{
	protected PsseHeader		_hdr;
	protected LineNumberReader	_rdr;
	protected String			_ver;
	protected PsseClassSet		_cset;

	public PsseProcessor(Reader rawpsse, String specversion)
			throws IOException, PsseProcException
	{
		_rdr = new LineNumberReader(rawpsse);
		_hdr = new PsseHeader(_rdr);
		String hver = _hdr.getVersion();
		_ver = (hver == null) ? specversion : hver;

		if (_ver == null)
		{
			throw new PsseProcException(
				"Unable to detect version from PSS/e file."
				+ "  Version must be manually specified");
		}
		
		_cset = PsseClassSet.GetClassSetForVersion(_ver);
	}

	public PsseHeader getHeader() {return _hdr;}
	
	public PsseClassSet getPsseClassSet() {return _cset;}
	
	public void process() throws PsseProcException, IOException
	{
		for (PsseClass pc : getPsseClassSet().getPsseClasses())
		{
			PsseRecWriter w = getWriter(pc.getClassName());
			if (!pc.getLines().isEmpty())
				pc.processRecords(_rdr, w, _cset);
		}
	}

	protected abstract PsseRecWriter getWriter(String psseClassName);

	
}
