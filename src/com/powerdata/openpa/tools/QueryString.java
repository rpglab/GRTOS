package com.powerdata.openpa.tools;

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

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Utiltiy to parse query strings.
 * 
 * A query string has the form: field=value&field2=value ...
 * 
 * The field names can be repeated.  Sometimes the order of the fields is
 * important, other times only the values matter.  This utility allows
 * different approaches depending on which of these is important.
 * 
 * The one kludge is that in most cases there will be exactly one value
 * per field, however, since more are possible it is not enough to simply
 * return a String value for a field name, as a result the caller is
 * forced to check the return value for null and check the count.
 * 
 * @author marck
 *
 */
public class QueryString
{
	String _query[][];
	HashMap<String,String[]> _fields = new HashMap<String,String[]>();
	public QueryString(String query)
	{
		this(query, false);
	}
	public QueryString(String query, boolean keepQuotes)
	{
		// split based on &
		String pairs[] = new StringParse(query,"&", keepQuotes).getTokens();
		// split based on = and index them
		_query = new String[pairs.length][];
		HashMap<String,ArrayList<String>> fields = new HashMap<String,ArrayList<String>>();
		for(int i=0; i<pairs.length; i++)
		{
			_query[i] = new StringParse(pairs[i],"=").getTokens();
			ArrayList<String> vals = fields.get(_query[i][0]);
			if (vals == null)
			{
				vals = new ArrayList<String>();
				fields.put(_query[i][0], vals);
			}
			vals.add(_query[i][1]);
		}
		// convert the ArrayList to String[]
		for(String k : fields.keySet())
		{
			_fields.put(k, fields.get(k).toArray(new String[0]));
		}
	}
	public int count() { return _query.length; }
	public String[] get(int i) { return _query[i]; }
	public String getField(int i) { return _query[i][0]; }
	public String getVal(int i) { return _query[i][1]; }
	public String[] get(String field) { return _fields.get(field); }
	public boolean containsKey(String field) { return _fields.containsKey(field); }
	public int getFieldCount() { return _fields.size(); }
	public HashMap<String,String[]> getMap() { return _fields; }
	public String[][] getQuery() { return _query; }
}
