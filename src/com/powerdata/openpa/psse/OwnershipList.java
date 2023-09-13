package com.powerdata.openpa.psse;

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

public abstract class OwnershipList extends PsseBaseList<Ownership>
{
	protected OwnedEquip _eq;
	
	public static final OwnershipList Empty = new OwnershipList()
	{
		@Override
		public String getObjectID(int ndx) throws PsseModelException {return null;}
		@Override
		public int size() {return 0;}
	};
	protected OwnershipList() {super();}
	public OwnershipList(PsseModel model, OwnedEquip eq)
	{
		super(model);
		_eq = eq;
	}

	/* Standard object retrieval */

	/** Get an Ownership by it's index. */
	@Override
	public Ownership get(int ndx) { return new Ownership(ndx,this); }
	/** Get an Ownership by it's ID. */
	@Override
	public Ownership get(String id) { return super.get(id); }

	/* convenience methods */
	public Owner getOwner(int ndx) throws PsseModelException {return _model.getOwners().get(getO(ndx));}

	/* raw PSS/e methods */
	public int getO(int ndx) throws PsseModelException {return _eq.getBus().getOWNER();}
	public float getF(int ndx) throws PsseModelException {return 100f;}

	@Override
	public int size() {return 1;}
}
