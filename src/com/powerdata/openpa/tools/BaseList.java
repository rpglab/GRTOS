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

import java.util.AbstractList;
import java.util.HashMap;
import com.powerdata.openpa.psse.PsseModelException;

/**
 * Start of the object list hierarchy
 * 
 * @author chris@powerdata.com
 *
 * @param <T>
 */

public abstract class BaseList<T extends BaseObject> extends AbstractList<T> 
{
	protected HashMap<String,Integer> _idToNdx = new HashMap<String,Integer>();
	
	/** Get a unique identifier for the object */
	public abstract String getObjectID(int ndx) throws PsseModelException;
	public String getObjectName(int ndx) throws PsseModelException
	{
		return getObjectID(ndx);
	}
	public String getFullName(int ndx) throws PsseModelException
	{
		return getObjectName(ndx);
	}
	public String getDebugName(int ndx) throws PsseModelException
	{
		return getFullName(ndx);
	}
	
	public HashMap<String,Integer> idmap() {return _idToNdx;}
	
	/** Get an object by it's ID */
	public T get(String objectid)
	{
		Integer ndx = _idToNdx.get(objectid);
		return (ndx != null)?get(ndx):null;
	}
	/** Reindex the objectID to ndx mapping. 
	 * @throws PsseModelException */
	protected void reindex() throws PsseModelException
	{
		HashMap<String,Integer> idToNdx = new HashMap<String,Integer>();
		int count = this.size();
		for(int i=0; i<count; i++) idToNdx.put(getObjectID(i), i);
		_idToNdx = idToNdx;
	}
	public int getRootIndex(int ndx) {return ndx;}
}
