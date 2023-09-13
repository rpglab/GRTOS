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



/**
 * Class and field definitions for PSS/e version 30
 * 
 * @author chris
 *
 */
public class PsseClassSetVersion30 extends PsseClassSetVersion29
{
	private static final int		VersionMajor	= 30;
	public static final PsseClass	Transformer30	= new PsseTransformerClass();
	public static final PsseClass	SwitchedShunt30	= new PsseClass("SwitchedShunt");
	public static final PsseClass	FACTSDevice30	= new PsseClass("FACTSDevice");

	static
	{
		Transformer30.addLine(new PsseField[] {
			new PsseField("I", PsseFieldType.String),
			new PsseField("J", PsseFieldType.String),
			new PsseField("K", PsseFieldType.String),
			new PsseField("CKT", PsseFieldType.String),
			new PsseField("CW", PsseFieldType.Integer),
			new PsseField("CZ", PsseFieldType.Integer),
			new PsseField("CM", PsseFieldType.Integer),
			new PsseField("MAG1", PsseFieldType.Float),
			new PsseField("MAG2", PsseFieldType.Float),
			new PsseField("NMETR", PsseFieldType.Integer),
			new PsseField("NAME", PsseFieldType.String),
			new PsseField("STAT", PsseFieldType.Integer),
			new PsseField("O1", PsseFieldType.Integer),
			new PsseField("F1", PsseFieldType.Float),
			new PsseField("O2", PsseFieldType.Integer),
			new PsseField("F2", PsseFieldType.Float),
			new PsseField("O3", PsseFieldType.Integer),
			new PsseField("F3", PsseFieldType.Float),
			new PsseField("O4", PsseFieldType.Integer),
			new PsseField("F4", PsseFieldType.Float) });

		Transformer30.addLine(new PsseField[] {
			new PsseField("R1-2", PsseFieldType.Float),
			new PsseField("X1-2", PsseFieldType.Float),
			new PsseField("SBASE1-2", PsseFieldType.Float),
			new PsseField("R2-3", PsseFieldType.Float),
			new PsseField("X2-3", PsseFieldType.Float),
			new PsseField("SBASE2-3", PsseFieldType.Float),
			new PsseField("R3-1", PsseFieldType.Float),
			new PsseField("X3-1", PsseFieldType.Float),
			new PsseField("SBASE3-1", PsseFieldType.Float),
			new PsseField("VMSTAR", PsseFieldType.Float),
			new PsseField("ANSTAR", PsseFieldType.Float) });
		
		Transformer30.addLine(new PsseField[] {
			new PsseField("WINDV1", PsseFieldType.Float),
			new PsseField("NOMV1", PsseFieldType.Float),
			new PsseField("ANG1", PsseFieldType.Float),
			new PsseField("RATA1", PsseFieldType.Float),
			new PsseField("RATB1", PsseFieldType.Float),
			new PsseField("RATC1", PsseFieldType.Float),
			new PsseField("COD1", PsseFieldType.Integer),
			new PsseField("CONT1", PsseFieldType.String),
			new PsseField("RMA1", PsseFieldType.Float),
			new PsseField("RMI1", PsseFieldType.Float),
			new PsseField("VMA1", PsseFieldType.Float),
			new PsseField("VMI1", PsseFieldType.Float),
			new PsseField("NTP1", PsseFieldType.Integer),
			new PsseField("TAB1", PsseFieldType.Integer),
			new PsseField("CR1", PsseFieldType.Float),
			new PsseField("CX1", PsseFieldType.Float) });
		
		Transformer30.addLine(new PsseField[] {
			new PsseField("WINDV2", PsseFieldType.Float),
			new PsseField("NOMV2", PsseFieldType.Float),
			new PsseField("ANG2", PsseFieldType.Float),
			new PsseField("RATA2", PsseFieldType.Float),
			new PsseField("RATB2", PsseFieldType.Float),
			new PsseField("RATC2", PsseFieldType.Float),
			new PsseField("COD2", PsseFieldType.Integer),
			new PsseField("CONT2", PsseFieldType.String),
			new PsseField("RMA2", PsseFieldType.Float),
			new PsseField("RMI2", PsseFieldType.Float),
			new PsseField("VMA2", PsseFieldType.Float),
			new PsseField("VMI2", PsseFieldType.Float),
			new PsseField("NTP2", PsseFieldType.Integer),
			new PsseField("TAB2", PsseFieldType.Integer),
			new PsseField("CR2", PsseFieldType.Float),
			new PsseField("CX2", PsseFieldType.Float) });
		
		Transformer30.addLine(new PsseField[] {
			new PsseField("WINDV3", PsseFieldType.Float),
			new PsseField("NOMV3", PsseFieldType.Float),
			new PsseField("ANG3", PsseFieldType.Float),
			new PsseField("RATA3", PsseFieldType.Float),
			new PsseField("RATB3", PsseFieldType.Float),
			new PsseField("RATC3", PsseFieldType.Float),
			new PsseField("COD3", PsseFieldType.Integer),
			new PsseField("CONT3", PsseFieldType.String),
			new PsseField("RMA3", PsseFieldType.Float),
			new PsseField("RMI3", PsseFieldType.Float),
			new PsseField("VMA3", PsseFieldType.Float),
			new PsseField("VMI3", PsseFieldType.Float),
			new PsseField("NTP3", PsseFieldType.Integer),
			new PsseField("TAB3", PsseFieldType.Integer),
			new PsseField("CR3", PsseFieldType.Float),
			new PsseField("CX3", PsseFieldType.Float) });

		SwitchedShunt30.addLine(new PsseField[] {
			new PsseField("I", PsseFieldType.String),
			new PsseField("MODSW", PsseFieldType.Integer),
			new PsseField("VSWHI", PsseFieldType.Float),
			new PsseField("VSWLO", PsseFieldType.Float),
			new PsseField("SWREM", PsseFieldType.String),
			new PsseField("RMPCT", PsseFieldType.Float),
			new PsseField("RMIDNT", PsseFieldType.String),
			new PsseField("BINIT", PsseFieldType.Float),
			new PsseField("N1", PsseFieldType.Integer),
			new PsseField("B1", PsseFieldType.Float),
			new PsseField("N2", PsseFieldType.Integer),
			new PsseField("B2", PsseFieldType.Float),
			new PsseField("N3", PsseFieldType.Integer),
			new PsseField("B3", PsseFieldType.Float),
			new PsseField("N4", PsseFieldType.Integer),
			new PsseField("B4", PsseFieldType.Float),
			new PsseField("N5", PsseFieldType.Integer),
			new PsseField("B5", PsseFieldType.Float),
			new PsseField("N6", PsseFieldType.Integer),
			new PsseField("B6", PsseFieldType.Float),
			new PsseField("N7", PsseFieldType.Integer),
			new PsseField("B7", PsseFieldType.Float),
			new PsseField("N8", PsseFieldType.Integer),
			new PsseField("B8", PsseFieldType.Float) });

		FACTSDevice30.addLine(new PsseField[] {
			new PsseField("N", PsseFieldType.Integer),
			new PsseField("I", PsseFieldType.String),
			new PsseField("J", PsseFieldType.String),
			new PsseField("MODE", PsseFieldType.Integer),
			new PsseField("PDES", PsseFieldType.Float),
			new PsseField("QDES", PsseFieldType.Float),
			new PsseField("VSET", PsseFieldType.Float),
			new PsseField("SHMX", PsseFieldType.Float),
			new PsseField("TRMX", PsseFieldType.Float),
			new PsseField("VTMN", PsseFieldType.Float),
			new PsseField("VTMX", PsseFieldType.Float),
			new PsseField("VSMX", PsseFieldType.Float),
			new PsseField("IMX", PsseFieldType.Float),
			new PsseField("LINX", PsseFieldType.Float),
			new PsseField("RMPCT", PsseFieldType.Float),
			new PsseField("OWNER", PsseFieldType.Integer),
			new PsseField("SET1", PsseFieldType.Float),
			new PsseField("SET2", PsseFieldType.Float),
			new PsseField("VSREF", PsseFieldType.Integer)

		});
	}

	@Override
	public PsseClass[] getPsseClasses()
	{
		PsseClass[] rv = super.getPsseClasses();
		rv[4] = Transformer30;
		rv[8] = SwitchedShunt30;
		rv[15] = FACTSDevice30;
		return rv;
	}

	@Override
	public int getVersionMajor()
	{
		return VersionMajor;
	}

	@Override
	public PsseClass getTransformer() {return Transformer30;}
	@Override
	public PsseClass getSwitchedShunt() {return SwitchedShunt30;}
	@Override
	public PsseClass getFACTSDevice() {return FACTSDevice30;}
	
}
