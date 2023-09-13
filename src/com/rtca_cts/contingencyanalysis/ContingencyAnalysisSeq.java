package com.rtca_cts.contingencyanalysis;


import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import org.junit.Test;

import com.powerdata.openpa.psse.ACBranch;
import com.powerdata.openpa.psse.PsseModel;
import com.powerdata.openpa.psse.PsseModelException;
import com.powerdata.openpa.psse.powerflow.VoltageSource;
import com.rtca_cts.param.ParamIO;
import com.utilxl.array.AuxArrayXL;
import com.utilxl.iofiles.AuxFileXL;

/**
 * Sequential Contingency Analysis;
 * includes both T-1 and G-1;
 * Save violations information.
 * 
 * Initialized in Feb. 2014.
 * 
 * @author Xingpeng.Li (xplipower@gmail.com)
 * 
 */
public class ContingencyAnalysisSeq extends ContingencyAnalysis {
	
	float _VmReportDiv = 70.0f;       // report voltage violation infos based on different voltage levels: [0 - 70 kv) and [70, +inf).   

	// for Transmission contingency
	float _SdVioBrcTransm;                   // standard deviation of thermal violation
	float _SdVioLowVmTransm;                 // standard deviation of low voltage violation
	float _SdVioHighVmTransm;                // standard deviation of high voltage violation

	boolean _markBrcUseLowVol = true;           // if true, then, brc voltage level depends on the end-bus with lower voltage. 
	int _numTotalBrcVioLTReportVmTransm;
	int _numTotalBrcVioNotLTReportVmTransm;
	int[] _numBrcVioLTReportVmTransm;     // # of Brc violations Per Contingency, where Brc voltage level is Less Than _VmReportDiv
	int[] _numBrcVioNotLTReportVmTransm;     // # of Brc violations Per Contingency, where Brc voltage level is Not Less Than _VmReportDiv
	float[][] _BrcDiffVioLTReportVmTransm;          //  Brc violations, where Brc voltage level is Less Than _VmReportDiv;
	float[][] _BrcDiffVioNotLTReportVmTransm;          // Brc violations, where Brc voltage level is Not less than _VmReportDiv;
	int[][] _idxBrcVioLTReportVmTransm;          // index
	int[][] _idxBrcVioNotLTReportVmTransm;          // 

	int _numTotalVmLowVioLTReportVmTransm;     // # of low voltage violations overall Contingency, where bus voltage level is Less Than _VmReportDiv
	int _numTotalVmHighVioLTReportVmTransm;     // # of high voltage violations overall Contingency, where bus voltage level is Less Than _VmReportDiv
	int _numTotalVmLowVioNotLTReportVmTransm;     // # of low voltage violations overall Contingency, where bus voltage level is Not Less Than _VmReportDiv
	int _numTotalVmHighVioNotLTReportVmTransm;     // # of high voltage violations overall Contingency, where bus voltage level is Not Less Than _VmReportDiv
	int[] _numVmLowVioLTReportVmTransm;     // # of low voltage violations Per Contingency, where bus voltage level is Less Than _VmReportDiv
	int[] _numVmHighVioLTReportVmTransm;     // # of high voltage violations Per Contingency, where bus voltage level is Less Than _VmReportDiv
	int[] _numVmLowVioNotLTReportVmTransm;     // # of low voltage violations Per Contingency, where bus voltage level is Not Less Than _VmReportDiv
	int[] _numVmHighVioNotLTReportVmTransm;     // # of high voltage violations Per Contingency, where bus voltage level is Not Less Than _VmReportDiv
	float[][] _VmLowVioLTReportVmTransm;          // low-voltage, where bus voltage level is Less Than _VmReportDiv;
	float[][] _VmHighVioLTReportVmTransm;          // high-voltage, where bus voltage level is less than _VmReportDiv;
	float[][] _VmLowVioNotLReportVmTransm;          // low-voltage, where bus voltage level is Not Less Than _VmReportDiv;
	float[][] _VmHighVioNotLReportVmTransm;          // high-voltage, where bus voltage level is Not less than _VmReportDiv;
	int[][] _idxVmLowVioLTReportVmTransm;          // index
	int[][] _idxVmHighVioLTReportVmTransm;          // 
	int[][] _idxVmLowVioNotLReportVmTransm;          // 
	int[][] _idxVmHighVioNotLReportVmTransm;          //
	
	
	// for Generator contingency
	float _SdVioBrcGen;                   // standard deviation of thermal violation
	float _SdVioLowVmGen;                 // standard deviation of low voltage violation
	float _SdVioHighVmGen;                // standard deviation of high voltage violation
	
	int _numTotalBrcVioLTReportVmGen;
	int _numTotalBrcVioNotLTReportVmGen;
	int[] _numBrcVioLTReportVmGen;     // # of Brc violations Per Contingency, where Brc voltage level is Less Than _VmReportDiv
	int[] _numBrcVioNotLTReportVmGen;     // # of Brc violations Per Contingency, where Brc voltage level is Not Less Than _VmReportDiv
	float[][] _BrcDiffVioLTReportVmGen;          //  Brc violations, where Brc voltage level is Less Than _VmReportDiv;
	float[][] _BrcDiffVioNotLTReportVmGen;          // Brc violations, where Brc voltage level is Not less than _VmReportDiv;
	int[][] _idxBrcVioLTReportVmGen;          // index
	int[][] _idxBrcVioNotLTReportVmGen;          // 
		
	int _numTotalVmLowVioLTReportVmGen;     // # of low voltage violations overall Contingency, where bus voltage level is Less Than _VmReportDiv
	int _numTotalVmHighVioLTReportVmGen;     // # of high voltage violations overall Contingency, where bus voltage level is Less Than _VmReportDiv
	int _numTotalVmLowVioNotLTReportVmGen;     // # of low voltage violations overall Contingency, where bus voltage level is Not Less Than _VmReportDiv
	int _numTotalVmHighVioNotLTReportVmGen;     // # of high voltage violations overall Contingency, where bus voltage level is Not Less Than _VmReportDiv
	int[] _numVmLowVioLTReportVmGen;     // # of low voltage violations Per Contingency, where bus voltage level is Less Than _VmReportDiv
	int[] _numVmHighVioLTReportVmGen;     // # of high voltage violations Per Contingency, where bus voltage level is Less Than _VmReportDiv
	int[] _numVmLowVioNotLTReportVmGen;     // # of low voltage violations Per Contingency, where bus voltage level is Not Less Than _VmReportDiv
	int[] _numVmHighVioNotLTReportVmGen;     // # of high voltage violations Per Contingency, where bus voltage level is Not Less Than _VmReportDiv
	float[][] _VmLowVioLTReportVmGen;          // low-voltage, where bus voltage level is Less Than _VmReportDiv;
	float[][] _VmHighVioLTReportVmGen;          // high-voltage, where bus voltage level is less than _VmReportDiv;
	float[][] _VmLowVioNotLReportVmGen;          // low-voltage, where bus voltage level is Not Less Than _VmReportDiv;
	float[][] _VmHighVioNotLReportVmGen;          // high-voltage, where bus voltage level is Not less than _VmReportDiv;
	int[][] _idxVmLowVioLTReportVmGen;          // index
	int[][] _idxVmHighVioLTReportVmGen;          // 
	int[][] _idxVmLowVioNotLReportVmGen;          // 
	int[][] _idxVmHighVioNotLReportVmGen;          //
	
	
	public ContingencyAnalysisSeq(PsseModel model)
			throws PsseModelException, IOException
	{
		super(model);
		initialSeq();
	}

	public ContingencyAnalysisSeq(PsseModel model, VoltageSource vstart)
			throws PsseModelException, IOException
	{
		super(model, vstart);
		initialSeq();
	}

	void initialSeq() throws PsseModelException
	{
//		_path = ParamIO.getOutPath();
		AuxFileXL.createFolder(ParamIO.getOutPath());
		_path = ParamIO.getCAPath();
		AuxFileXL.createFolder(_path);
		_pathForGeneInfo = _path;
	}
	

	@Test
	@Override
	public void runTransmContingencyAnalysis() throws PsseModelException, IOException
	{
		assertTrue(checkOrigPf() == true); // this line is needed.
		_PfNotCTransm = new PfNotConverge(_nbr);
		_testAllTransm = new VioResultAll(_nbr);
		_numTestedNm1Transm = 0;
		
		System.out.println("Transmission contingency analysis starts running");
		boolean[] checkBrcConti = _model.getContingencyListBrcData().getIsBrcInContiList();

		// Start transmission contingency analysis loop
		for(int i=0; i<_nbr; i++)
		{
			boolean Tm1Check = checkBrcConti[i];
			if (Tm1Check == true)
			{
				boolean inSvc = _branches.isInSvc(i);
				if (inSvc == true) runSingleTransmConti(i);
			}
		}
		analyzeCaResultTransm();
		System.out.println("Transmission contingency analysis simulation is done");
		System.out.println("   Number of transmission contingencies simulated: "+_numTestedNm1Transm);
	}
		

	@Override
	public void runGenContingencyAnalysis() throws PsseModelException, IOException
	{
		assertTrue(checkOrigPf() == true); // this line is needed.
		assertTrue(_optionPfactor > 0);
		assertTrue(_optionPfactor <= 3);
		float[] MH = null;
		if (_optionPfactor == 3) {initGenDyr(); MH = _genDyr.getGenSH();}
		
		System.out.println("Generator contingency analysis starts running");
		boolean[] checkGenConti = _model.getContingencyListGenData().getIsGenInContiList();

		_PfNotCGen = new PfNotConverge(_nGen);
		_testAllGen = new VioResultAll(_nGen);
		_numTestedNm1Gen = 0;
		
		// Start generator contingency analysis loop 
		for(int i=0; i<_nGen; i++)
		{
			boolean Gm1Check = checkGenConti[i];			
			if (Gm1Check == false) continue;
			boolean inSvc = _gens.isInSvc(i);
			assertTrue(inSvc == true);
			if (inSvc == true) runSingleGenConti(i, MH);
		}
		analyzeCaResultGen();
		System.out.println("Generator contingency analysis simulation is done");
		System.out.println("   Number of generator contingencies simulated: "+_numTestedNm1Gen);
	}
	

	void analyzeCaResultTransm() throws PsseModelException
	{
		if (_MarkGenVarLimit)
		{
			_numContPVtoPQTransm = _testAllTransm.getNumContPVtoPQ();
			_idxContPVtoPQTransm = _testAllTransm.getIdxContPVtoPQ();
			_numPVtoPQEachContTransm = _testAllTransm.getNumPVtoPQEachCont();			
			_numOrigPVEachContTransm = _testAllTransm.getNumOrigPVEachCont();
		}
		
		_numNotConvTransm = _PfNotCTransm.getNumNotConv();
		_IdxNotConvTransm = _PfNotCTransm.getAllContinNotConv();

		_sumBrcVioExtPerContTransm = _testAllTransm.getSumBrcVioExt();
		_sumVmVioExtPerContTransm = _testAllTransm.getSumVmVioExt();

		_nVioVmAllTransm = _testAllTransm.getNumVioVm();      // the number of contingency that cause voltage violation, it is also the real size (the data needed) of sizeV, IdxContVioVm;
                                                              //  it is also the number of row of IdxV and VmVioDiff
                                                               // nVioVm != sizeV.length  -> Now, it seems fine.	
		_totalVmVioNumAllTransm = _testAllTransm.getTotalVmVioNum();
		_sizeVAllTransm = _testAllTransm.getSizeV();  
		_IdxContVioVmAllTransm = _testAllTransm.getIdxVioVm();
		
		_AllVmDiffTransm = _testAllTransm.getVmVioDiff();
		_AllVmTransm = _testAllTransm.getVmVio();
		_AllVaTransm = _testAllTransm.getVaVio();
		_IdxVAllTransm = _testAllTransm.getIdxV();
		
		_nVioBrcAllTransm = _testAllTransm.getNumVioBrc();
		_totalBrcVioNumAllTransm = _testAllTransm.getTotalBrcVioNum();   
		_sizeBrcAllTransm = _testAllTransm.getSizeBrc();
		_IdxContVioBrcAllTransm = _testAllTransm.getIdxVioBrc();
		
		_AllBrcDiffTransm = _testAllTransm.getBrcVioDiff();
		_IdxBrcAllTransm = _testAllTransm.getIdxBrc();
		
		_BrcVioPfrmAllTransm = _testAllTransm.getBrcVioPfrm();
		_BrcVioPtoAllTransm = _testAllTransm.getBrcVioPto();
		_BrcVioQfrmAllTransm = _testAllTransm.getBrcVioQfrm();
		_BrcVioQtoAllTransm = _testAllTransm.getBrcVioQto();

		_maxBrcSdiffPerContTransm = _testAllTransm.getMaxBrcDiffEachCont();
		_IdxMaxBrcSdiffPerContTransm = _testAllTransm.getIdxMaxBrcDiffEachCont();
		_maxBrcVioPerctPerContTransm = _testAllTransm.getMaxBrcDiffPerctEachCont();

		_nContVioVmAndBrcAllTransm = _testAllTransm.getNumContVio();
		_IdxContVioVmAndBrcAllTransm = _testAllTransm.getIdxContVio();
		_idxMapContToVmVioContTransm = _testAllTransm.getIdxMapContToVmVioCont();
		_idxMapContToBrcVioContTransm = _testAllTransm.getIdxMapContToBrcVioCont();
		
		analyzeVioInfoTransm();
		
		// separate _AllVmTransm[][]  and _IdxVAllTransm[][] by voltage level, and under-voltage/over-voltage
		_numVmLowVioLTReportVmTransm = new int[_nVioVmAllTransm];
		_numVmHighVioLTReportVmTransm = new int[_nVioVmAllTransm];
		_numVmLowVioNotLTReportVmTransm = new int[_nVioVmAllTransm];
		_numVmHighVioNotLTReportVmTransm = new int[_nVioVmAllTransm];
		_numTotalVmLowVioLTReportVmTransm = 0;
		_numTotalVmHighVioLTReportVmTransm = 0;
		_numTotalVmLowVioNotLTReportVmTransm = 0;
		_numTotalVmHighVioNotLTReportVmTransm = 0;
		for (int i=0; i<_nVioVmAllTransm; i++)
		{
			float[] VmPerCont = _AllVmTransm[i];
			_numVmLowVioLTReportVmTransm[i] = 0;
			_numVmHighVioLTReportVmTransm[i] = 0;
			_numVmLowVioNotLTReportVmTransm[i] = 0;
			_numVmHighVioNotLTReportVmTransm[i] = 0;
			for (int j=0; j<VmPerCont.length; j++)
			{
				int idx = _IdxVAllTransm[i][j];
				float vm = _AllVmTransm[i][j];
				if (_BaseKV[idx] < _VmReportDiv)
				{
					if (vm < _Vmin) _numVmLowVioLTReportVmTransm[i]++;
					else if (vm > _Vmax) _numVmHighVioLTReportVmTransm[i]++;
					else System.err.println("Something wrong here when separating the Voltage11 .... ");
				}
				else
				{
					if (vm < _Vmin) _numVmLowVioNotLTReportVmTransm[i]++;
					else if (vm > _Vmax) _numVmHighVioNotLTReportVmTransm[i]++;
					else System.err.println("Something wrong here when separating the Voltage12 .... ");
				}
			}
			_numTotalVmLowVioLTReportVmTransm += _numVmLowVioLTReportVmTransm[i];
			_numTotalVmHighVioLTReportVmTransm += _numVmHighVioLTReportVmTransm[i];
			_numTotalVmLowVioNotLTReportVmTransm += _numVmLowVioNotLTReportVmTransm[i];
			_numTotalVmHighVioNotLTReportVmTransm += _numVmHighVioNotLTReportVmTransm[i];
		}
		
		_VmLowVioLTReportVmTransm = new float[_nVioVmAllTransm][];
		_VmHighVioLTReportVmTransm = new float[_nVioVmAllTransm][];
		_VmLowVioNotLReportVmTransm = new float[_nVioVmAllTransm][];
		_VmHighVioNotLReportVmTransm = new float[_nVioVmAllTransm][];
		_idxVmLowVioLTReportVmTransm = new int[_nVioVmAllTransm][];
		_idxVmHighVioLTReportVmTransm = new int[_nVioVmAllTransm][];
		_idxVmLowVioNotLReportVmTransm = new int[_nVioVmAllTransm][];
		_idxVmHighVioNotLReportVmTransm = new int[_nVioVmAllTransm][];
		for (int i=0; i<_nVioVmAllTransm; i++)
		{
			float[] VmPerCont = _AllVmTransm[i];
			_VmLowVioLTReportVmTransm[i] = new float[_numVmLowVioLTReportVmTransm[i]];
			_VmHighVioLTReportVmTransm[i] = new float[_numVmHighVioLTReportVmTransm[i]];
			_VmLowVioNotLReportVmTransm[i] = new float[_numVmLowVioNotLTReportVmTransm[i]];
			_VmHighVioNotLReportVmTransm[i] = new float[_numVmHighVioNotLTReportVmTransm[i]];
			_idxVmLowVioLTReportVmTransm[i] = new int[_numVmLowVioLTReportVmTransm[i]];
			_idxVmHighVioLTReportVmTransm[i] = new int[_numVmHighVioLTReportVmTransm[i]];
			_idxVmLowVioNotLReportVmTransm[i] = new int[_numVmLowVioNotLTReportVmTransm[i]];
			_idxVmHighVioNotLReportVmTransm[i] = new int[_numVmHighVioNotLTReportVmTransm[i]];
			
			int idx1 = 0;
			int idx2 = 0;
			int idx3 = 0;
			int idx4 = 0;			
			
			for (int j=0; j<VmPerCont.length; j++)
			{
				int idx = _IdxVAllTransm[i][j];
				float vm = _AllVmTransm[i][j];
				if (_BaseKV[idx] < _VmReportDiv)
				{
					if (vm < _Vmin)
					{
						_VmLowVioLTReportVmTransm[i][idx1] = vm;
						_idxVmLowVioLTReportVmTransm[i][idx1] = idx;
						idx1++;
					}
					else if (vm > _Vmax)
					{
						_VmHighVioLTReportVmTransm[i][idx2] = vm;
						_idxVmHighVioLTReportVmTransm[i][idx2] = idx;
						idx2++;
					}
					else
					{
						System.err.println("Something wrong here when separating the Voltage21 .... ");
					}
						
				}
				else
				{
					if (vm < _Vmin)
					{
						_VmLowVioNotLReportVmTransm[i][idx3] = vm;
						_idxVmLowVioNotLReportVmTransm[i][idx3] = idx;
						idx3++;
					}
					else if (vm > _Vmax)
					{
						_VmHighVioNotLReportVmTransm[i][idx4] = vm;
						_idxVmHighVioNotLReportVmTransm[i][idx4] = idx;
						idx4++;
					}
					else
					{
						System.err.println("Something wrong here when separating the Voltage22 .... ");
					}
					
				}
			}
			int check = idx1 + idx2 + idx3 + idx4;
			assertTrue(check == VmPerCont.length);
		}
		_numVmLowVioLTReportVmTransm = AuxArrayXL.remZeroFrmArray(_numVmLowVioLTReportVmTransm);
		_numVmHighVioLTReportVmTransm = AuxArrayXL.remZeroFrmArray(_numVmHighVioLTReportVmTransm);
		_numVmLowVioNotLTReportVmTransm = AuxArrayXL.remZeroFrmArray(_numVmLowVioNotLTReportVmTransm);
		_numVmHighVioNotLTReportVmTransm = AuxArrayXL.remZeroFrmArray(_numVmHighVioNotLTReportVmTransm);

		_VmLowVioLTReportVmTransm = AuxArrayXL.shrinkTwoDimArray(_VmLowVioLTReportVmTransm);
		_VmHighVioLTReportVmTransm = AuxArrayXL.shrinkTwoDimArray(_VmHighVioLTReportVmTransm);
		_VmLowVioNotLReportVmTransm = AuxArrayXL.shrinkTwoDimArray(_VmLowVioNotLReportVmTransm);
		_VmHighVioNotLReportVmTransm = AuxArrayXL.shrinkTwoDimArray(_VmHighVioNotLReportVmTransm);
		_idxVmLowVioLTReportVmTransm = AuxArrayXL.shrinkTwoDimArray(_idxVmLowVioLTReportVmTransm);
		_idxVmHighVioLTReportVmTransm = AuxArrayXL.shrinkTwoDimArray(_idxVmHighVioLTReportVmTransm);
		_idxVmLowVioNotLReportVmTransm = AuxArrayXL.shrinkTwoDimArray(_idxVmLowVioNotLReportVmTransm);
		_idxVmHighVioNotLReportVmTransm = AuxArrayXL.shrinkTwoDimArray(_idxVmHighVioNotLReportVmTransm);
			
		/*
		if (_VmLowVioLTReportVmTransm != null)
		{
			assertTrue(_numVmLowVioLTReportVmTransm.length == _VmLowVioLTReportVmTransm.length);
			assertTrue(_numVmLowVioLTReportVmTransm.length == _idxVmLowVioLTReportVmTransm.length);
			for(int i=0; i<_numVmLowVioLTReportVmTransm.length; i++)
			{
				assertTrue(_numVmLowVioLTReportVmTransm[i] == _VmLowVioLTReportVmTransm[i].length);
				assertTrue(_numVmLowVioLTReportVmTransm[i] == _idxVmLowVioLTReportVmTransm[i].length);
			}
		}
		if (_VmHighVioLTReportVmTransm != null)
		{
			assertTrue(_numVmHighVioLTReportVmTransm.length == _VmHighVioLTReportVmTransm.length);
			assertTrue(_numVmHighVioLTReportVmTransm.length == _idxVmHighVioLTReportVmTransm.length);
			for(int i=0; i<_numVmHighVioLTReportVmTransm.length; i++)
			{
				assertTrue(_numVmHighVioLTReportVmTransm[i] == _VmHighVioLTReportVmTransm[i].length);
				assertTrue(_numVmHighVioLTReportVmTransm[i] == _idxVmHighVioLTReportVmTransm[i].length);
			}
		}
		if (_VmHighVioLTReportVmTransm != null)
		{
			assertTrue(_numVmLowVioNotLTReportVmTransm.length == _VmLowVioNotLReportVmTransm.length);
			assertTrue(_numVmLowVioNotLTReportVmTransm.length == _idxVmLowVioNotLReportVmTransm.length);
			for(int i=0; i<_numVmLowVioNotLTReportVmTransm.length; i++)
			{
				assertTrue(_numVmLowVioNotLTReportVmTransm[i] == _VmLowVioNotLReportVmTransm[i].length);
				assertTrue(_numVmLowVioNotLTReportVmTransm[i] == _idxVmLowVioNotLReportVmTransm[i].length);
			}
		}
		if (_VmHighVioNotLReportVmTransm != null)
		{
			assertTrue(_numVmHighVioNotLTReportVmTransm.length == _VmHighVioNotLReportVmTransm.length);
			assertTrue(_numVmHighVioNotLTReportVmTransm.length == _idxVmHighVioNotLReportVmTransm.length);
			for(int i=0; i<_numVmHighVioNotLTReportVmTransm.length; i++)
			{
				assertTrue(_numVmHighVioNotLTReportVmTransm[i] == _VmHighVioNotLReportVmTransm[i].length);
				assertTrue(_numVmHighVioNotLTReportVmTransm[i] == _idxVmHighVioNotLReportVmTransm[i].length);
			}	
		}*/
		
		
		
		// separate _AllBrcDiffTransm[][]  and _IdxBrcAllTransm[][] by voltage level, and under-voltage/over-voltage
		_numBrcVioLTReportVmTransm = new int[_nVioBrcAllTransm];
		_numBrcVioNotLTReportVmTransm = new int[_nVioBrcAllTransm];
		_numTotalBrcVioLTReportVmTransm = 0;
		_numTotalBrcVioNotLTReportVmTransm = 0;
		for (int i=0; i<_nVioBrcAllTransm; i++)
		{
			float[] brcDiffPerCont = _AllBrcDiffTransm[i];
			_numBrcVioLTReportVmTransm[i] = 0;
			_numBrcVioNotLTReportVmTransm[i] = 0;
			for (int j=0; j<brcDiffPerCont.length; j++)
			{
				int idx = _IdxBrcAllTransm[i][j];
				int idxFrmBus =  _branches.getFromBus(idx).getIndex();
				int idxToBus =  _branches.getToBus(idx).getIndex();				
				boolean markHighVmBrc = true;
				if (_BaseKV[idxFrmBus] < _VmReportDiv) markHighVmBrc = false;
				if (_BaseKV[idxToBus] < _VmReportDiv) markHighVmBrc = false;
				if (markHighVmBrc == false) _numBrcVioLTReportVmTransm[i]++; 
				else _numBrcVioNotLTReportVmTransm[i]++; 
			}
			_numTotalBrcVioLTReportVmTransm += _numBrcVioLTReportVmTransm[i];
			_numTotalBrcVioNotLTReportVmTransm += _numBrcVioNotLTReportVmTransm[i];
		}
		
		_BrcDiffVioLTReportVmTransm = new float[_nVioBrcAllTransm][];
		_BrcDiffVioNotLTReportVmTransm = new float[_nVioBrcAllTransm][];
		_idxBrcVioLTReportVmTransm = new int[_nVioBrcAllTransm][];
		_idxBrcVioNotLTReportVmTransm = new int[_nVioBrcAllTransm][];
		for (int i=0; i<_nVioBrcAllTransm; i++)
		{
			float[] brcDiffPerCont = _AllBrcDiffTransm[i];
			_BrcDiffVioLTReportVmTransm[i] = new float[_numBrcVioLTReportVmTransm[i]];
			_BrcDiffVioNotLTReportVmTransm[i] = new float[_numBrcVioNotLTReportVmTransm[i]];
			_idxBrcVioLTReportVmTransm[i] = new int[_numBrcVioLTReportVmTransm[i]];
			_idxBrcVioNotLTReportVmTransm[i] = new int[_numBrcVioNotLTReportVmTransm[i]];
			
			int idx1 = 0;
			int idx2 = 0;			
			for (int j=0; j<brcDiffPerCont.length; j++)
			{
				int idx = _IdxBrcAllTransm[i][j];
				float pfDiff = _AllBrcDiffTransm[i][j];
				int idxFrmBus =  _branches.getFromBus(idx).getIndex();
				int idxToBus =  _branches.getToBus(idx).getIndex();	
				
				boolean markHighVmBrc = true;
				if (_BaseKV[idxFrmBus] < _VmReportDiv) markHighVmBrc = false;
				if (_BaseKV[idxToBus] < _VmReportDiv) markHighVmBrc = false;
				if (markHighVmBrc == false)
				{
					_BrcDiffVioLTReportVmTransm[i][idx1] = pfDiff;
					_idxBrcVioLTReportVmTransm[i][idx1] = idx;
					idx1++;
				}
				else
				{
					_BrcDiffVioNotLTReportVmTransm[i][idx2] = pfDiff;
					_idxBrcVioNotLTReportVmTransm[i][idx2] = idx;
					idx2++;
				}
			}
			int check = idx1 + idx2;
			assertTrue(check == brcDiffPerCont.length);
		}
		_numBrcVioLTReportVmTransm = AuxArrayXL.remZeroFrmArray(_numBrcVioLTReportVmTransm);
		_numBrcVioNotLTReportVmTransm = AuxArrayXL.remZeroFrmArray(_numBrcVioNotLTReportVmTransm);
		_BrcDiffVioLTReportVmTransm = AuxArrayXL.shrinkTwoDimArray(_BrcDiffVioLTReportVmTransm);
		_BrcDiffVioNotLTReportVmTransm = AuxArrayXL.shrinkTwoDimArray(_BrcDiffVioNotLTReportVmTransm);
		_idxBrcVioLTReportVmTransm = AuxArrayXL.shrinkTwoDimArray(_idxBrcVioLTReportVmTransm);
		_idxBrcVioNotLTReportVmTransm = AuxArrayXL.shrinkTwoDimArray(_idxBrcVioNotLTReportVmTransm);	
		
		if (_BrcDiffVioLTReportVmTransm != null)
		{
			assertTrue(_numBrcVioLTReportVmTransm.length == _BrcDiffVioLTReportVmTransm.length);
			assertTrue(_numBrcVioLTReportVmTransm.length == _idxBrcVioLTReportVmTransm.length);
			for(int i=0; i<_numBrcVioLTReportVmTransm.length; i++)
			{
				assertTrue(_numBrcVioLTReportVmTransm[i] == _BrcDiffVioLTReportVmTransm[i].length);
				assertTrue(_numBrcVioLTReportVmTransm[i] == _idxBrcVioLTReportVmTransm[i].length);
			}
		}
		if (_numBrcVioNotLTReportVmTransm != null)
		{
			assertTrue(_numBrcVioNotLTReportVmTransm.length == _BrcDiffVioNotLTReportVmTransm.length);
			assertTrue(_numBrcVioNotLTReportVmTransm.length == _idxBrcVioNotLTReportVmTransm.length);
			for(int i=0; i<_numBrcVioNotLTReportVmTransm.length; i++)
			{
				assertTrue(_numBrcVioNotLTReportVmTransm[i] == _BrcDiffVioNotLTReportVmTransm[i].length);
				assertTrue(_numBrcVioNotLTReportVmTransm[i] == _idxBrcVioNotLTReportVmTransm[i].length);
			}
		}
	}
		
	void analyzeCaResultGen() throws PsseModelException
	{
		if (_MarkGenVarLimit)
		{
			_numContPVtoPQGen = _testAllGen.getNumContPVtoPQ();
			_idxContPVtoPQGen = _testAllGen.getIdxContPVtoPQ();
			_numPVtoPQEachContGen = _testAllGen.getNumPVtoPQEachCont();
			_numOrigPVEachContGen = _testAllGen.getNumOrigPVEachCont();
		}

		_numNotConvGen = _PfNotCGen.getNumNotConv();
		_IdxNotConvGen = _PfNotCGen.getAllContinNotConv();
		
		_sumBrcVioExtPerContGen = _testAllGen.getSumBrcVioExt();
		_sumVmVioExtPerContGen = _testAllGen.getSumVmVioExt();

		_nVioVmAllGen = _testAllGen.getNumVioVm();      // the number of contingency that cause voltage violation, it is also the real size (the data needed) of sizeV, IdxContVioVm;
                                                        //  it is also the number of row of IdxV and VmVioDiff
                                                        // nVioVm != sizeV.length  -> Now, it seems fine.
		_totalVmVioNumAllGen = _testAllGen.getTotalVmVioNum();
		_sizeVAllGen = _testAllGen.getSizeV();
		_IdxContVioVmAllGen = _testAllGen.getIdxVioVm();

		_AllVmDiffGen = _testAllGen.getVmVioDiff();
		_AllVmGen = _testAllGen.getVmVio();
		_AllVaGen = _testAllGen.getVaVio();
		_IdxVAllGen = _testAllGen.getIdxV();
		
		//
		_nVioBrcAllGen = _testAllGen.getNumVioBrc();
		_totalBrcVioNumAllGen = _testAllGen.getTotalBrcVioNum();
		_sizeBrcAllGen = _testAllGen.getSizeBrc(); 
		_IdxContVioBrcAllGen = _testAllGen.getIdxVioBrc();
		
		_AllBrcDiffGen = _testAllGen.getBrcVioDiff();
		_IdxBrcAllGen = _testAllGen.getIdxBrc();
				
		_BrcVioPfrmAllGen = _testAllGen.getBrcVioPfrm();
		_BrcVioPtoAllGen = _testAllGen.getBrcVioPto();
		_BrcVioQfrmAllGen = _testAllGen.getBrcVioQfrm();
		_BrcVioQtoAllGen = _testAllGen.getBrcVioQto();
		
		_maxBrcSdiffPerContGen = _testAllGen.getMaxBrcDiffEachCont();
		_IdxMaxBrcSdiffPerContGen = _testAllGen.getIdxMaxBrcDiffEachCont();
		_maxBrcVioPerctPerContGen = _testAllGen.getMaxBrcDiffPerctEachCont();

		_nContVioVmAndBrcAllGen = _testAllGen.getNumContVio();
		_IdxContVioVmAndBrcAllGen = _testAllGen.getIdxContVio();
		_idxMapContToVmVioContGen = _testAllGen.getIdxMapContToVmVioCont();
		_idxMapContToBrcVioContGen = _testAllGen.getIdxMapContToBrcVioCont();

		analyzeVioInfoGen();
		
		// separate _AllVmGen[][]  and _IdxVAllGen[][] by voltage level, and under-voltage/over-voltage
		_numVmLowVioLTReportVmGen = new int[_nVioVmAllGen];
		_numVmHighVioLTReportVmGen = new int[_nVioVmAllGen];
		_numVmLowVioNotLTReportVmGen = new int[_nVioVmAllGen];
		_numVmHighVioNotLTReportVmGen = new int[_nVioVmAllGen];
		_numTotalVmLowVioLTReportVmGen = 0;
		_numTotalVmHighVioLTReportVmGen = 0;
		_numTotalVmLowVioNotLTReportVmGen = 0;
		_numTotalVmHighVioNotLTReportVmGen = 0;
		for (int i=0; i<_nVioVmAllGen; i++)
		{
			float[] VmPerCont = _AllVmGen[i];
			_numVmLowVioLTReportVmGen[i] = 0;
			_numVmHighVioLTReportVmGen[i] = 0;
			_numVmLowVioNotLTReportVmGen[i] = 0;
			_numVmHighVioNotLTReportVmGen[i] = 0;
			for (int j=0; j<VmPerCont.length; j++)
			{
				int idx = _IdxVAllGen[i][j];
				float vm = _AllVmGen[i][j];
				if (_BaseKV[idx] < _VmReportDiv)
				{
					if (vm < _Vmin)	_numVmLowVioLTReportVmGen[i]++;
					else if (vm > _Vmax) _numVmHighVioLTReportVmGen[i]++;
					else System.err.println("Something wrong here when separating the Voltage11 .... ");
				}
				else
				{
					if (vm < _Vmin) _numVmLowVioNotLTReportVmGen[i]++;
					else if (vm > _Vmax) _numVmHighVioNotLTReportVmGen[i]++;
					else System.err.println("Something wrong here when separating the Voltage12 .... ");
				}
			}
			_numTotalVmLowVioLTReportVmGen += _numVmLowVioLTReportVmGen[i];
			_numTotalVmHighVioLTReportVmGen += _numVmHighVioLTReportVmGen[i];
			_numTotalVmLowVioNotLTReportVmGen += _numVmLowVioNotLTReportVmGen[i];
			_numTotalVmHighVioNotLTReportVmGen += _numVmHighVioNotLTReportVmGen[i];
		}
		
		_VmLowVioLTReportVmGen = new float[_nVioVmAllGen][];
		_VmHighVioLTReportVmGen = new float[_nVioVmAllGen][];
		_VmLowVioNotLReportVmGen = new float[_nVioVmAllGen][];
		_VmHighVioNotLReportVmGen = new float[_nVioVmAllGen][];
		_idxVmLowVioLTReportVmGen = new int[_nVioVmAllGen][];
		_idxVmHighVioLTReportVmGen = new int[_nVioVmAllGen][];
		_idxVmLowVioNotLReportVmGen = new int[_nVioVmAllGen][];
		_idxVmHighVioNotLReportVmGen = new int[_nVioVmAllGen][];
		for (int i=0; i<_nVioVmAllGen; i++)
		{
			float[] VmPerCont = _AllVmGen[i];
			_VmLowVioLTReportVmGen[i] = new float[_numVmLowVioLTReportVmGen[i]];
			_VmHighVioLTReportVmGen[i] = new float[_numVmHighVioLTReportVmGen[i]];
			_VmLowVioNotLReportVmGen[i] = new float[_numVmLowVioNotLTReportVmGen[i]];
			_VmHighVioNotLReportVmGen[i] = new float[_numVmHighVioNotLTReportVmGen[i]];
			_idxVmLowVioLTReportVmGen[i] = new int[_numVmLowVioLTReportVmGen[i]];
			_idxVmHighVioLTReportVmGen[i] = new int[_numVmHighVioLTReportVmGen[i]];
			_idxVmLowVioNotLReportVmGen[i] = new int[_numVmLowVioNotLTReportVmGen[i]];
			_idxVmHighVioNotLReportVmGen[i] = new int[_numVmHighVioNotLTReportVmGen[i]];
			
			int idx1 = 0;
			int idx2 = 0;
			int idx3 = 0;
			int idx4 = 0;			
			
			for (int j=0; j<VmPerCont.length; j++)
			{
				int idx = _IdxVAllGen[i][j];
				float vm = _AllVmGen[i][j];
				if (_BaseKV[idx] < _VmReportDiv)
				{
					if (vm < _Vmin)
					{
						_VmLowVioLTReportVmGen[i][idx1] = vm;
						_idxVmLowVioLTReportVmGen[i][idx1] = idx;
						idx1++;
					}
					else if (vm > _Vmax)
					{
						_VmHighVioLTReportVmGen[i][idx2] = vm;
						_idxVmHighVioLTReportVmGen[i][idx2] = idx;
						idx2++;
					}
					else System.err.println("Something wrong here when separating the Voltage21 .... ");
				}
				else
				{
					if (vm < _Vmin)
					{
						_VmLowVioNotLReportVmGen[i][idx3] = vm;
						_idxVmLowVioNotLReportVmGen[i][idx3] = idx;
						idx3++;
					}
					else if (vm > _Vmax)
					{
						_VmHighVioNotLReportVmGen[i][idx4] = vm;
						_idxVmHighVioNotLReportVmGen[i][idx4] = idx;
						idx4++;
					}
					else System.err.println("Something wrong here when separating the Voltage22 .... ");
				}
			}
			int check = idx1 + idx2 + idx3 + idx4;
			assertTrue(check == VmPerCont.length);
		}
		
		_numVmLowVioLTReportVmGen = AuxArrayXL.remZeroFrmArray(_numVmLowVioLTReportVmGen);
		_numVmHighVioLTReportVmGen = AuxArrayXL.remZeroFrmArray(_numVmHighVioLTReportVmGen);
		_numVmLowVioNotLTReportVmGen = AuxArrayXL.remZeroFrmArray(_numVmLowVioNotLTReportVmGen);
		_numVmHighVioNotLTReportVmGen = AuxArrayXL.remZeroFrmArray(_numVmHighVioNotLTReportVmGen);

		_VmLowVioLTReportVmGen = AuxArrayXL.shrinkTwoDimArray(_VmLowVioLTReportVmGen);
		_VmHighVioLTReportVmGen = AuxArrayXL.shrinkTwoDimArray(_VmHighVioLTReportVmGen);
		_VmLowVioNotLReportVmGen = AuxArrayXL.shrinkTwoDimArray(_VmLowVioNotLReportVmGen);
		_VmHighVioNotLReportVmGen = AuxArrayXL.shrinkTwoDimArray(_VmHighVioNotLReportVmGen);
		_idxVmLowVioLTReportVmGen = AuxArrayXL.shrinkTwoDimArray(_idxVmLowVioLTReportVmGen);
		_idxVmHighVioLTReportVmGen = AuxArrayXL.shrinkTwoDimArray(_idxVmHighVioLTReportVmGen);
		_idxVmLowVioNotLReportVmGen = AuxArrayXL.shrinkTwoDimArray(_idxVmLowVioNotLReportVmGen);
		_idxVmHighVioNotLReportVmGen = AuxArrayXL.shrinkTwoDimArray(_idxVmHighVioNotLReportVmGen);
		
		/*
		if(_VmLowVioLTReportVmGen != null) 
		{
			assertTrue(_numVmLowVioLTReportVmGen.length == _VmLowVioLTReportVmGen.length);
			assertTrue(_numVmLowVioLTReportVmGen.length == _idxVmLowVioLTReportVmGen.length);
			for(int i=0; i<_numVmLowVioLTReportVmGen.length; i++)
			{
				assertTrue(_numVmLowVioLTReportVmGen[i] == _VmLowVioLTReportVmGen[i].length);
				assertTrue(_numVmLowVioLTReportVmGen[i] == _idxVmLowVioLTReportVmGen[i].length);
			}
		}
		if(_VmHighVioLTReportVmGen != null) 
		{
			assertTrue(_numVmHighVioLTReportVmGen.length == _VmHighVioLTReportVmGen.length);
			assertTrue(_numVmHighVioLTReportVmGen.length == _idxVmHighVioLTReportVmGen.length);
			for(int i=0; i<_numVmHighVioLTReportVmGen.length; i++)
			{
				assertTrue(_numVmHighVioLTReportVmGen[i] == _VmHighVioLTReportVmGen[i].length);
				assertTrue(_numVmHighVioLTReportVmGen[i] == _idxVmHighVioLTReportVmGen[i].length);
			}
		}
		if(_VmLowVioNotLReportVmGen != null) 
		{
			assertTrue(_numVmLowVioNotLTReportVmGen.length == _VmLowVioNotLReportVmGen.length);
			assertTrue(_numVmLowVioNotLTReportVmGen.length == _idxVmLowVioNotLReportVmGen.length);
			for(int i=0; i<_numVmLowVioNotLTReportVmGen.length; i++)
			{
				assertTrue(_numVmLowVioNotLTReportVmGen[i] == _VmLowVioNotLReportVmGen[i].length);
				assertTrue(_numVmLowVioNotLTReportVmGen[i] == _idxVmLowVioNotLReportVmGen[i].length);
			}
		}
		if(_VmLowVioNotLReportVmGen != null) 
		{
			assertTrue(_numVmHighVioNotLTReportVmGen.length == _VmHighVioNotLReportVmGen.length);
			assertTrue(_numVmHighVioNotLTReportVmGen.length == _idxVmHighVioNotLReportVmGen.length);
			for(int i=0; i<_numVmHighVioNotLTReportVmGen.length; i++)
			{
				assertTrue(_numVmHighVioNotLTReportVmGen[i] == _VmHighVioNotLReportVmGen[i].length);
				assertTrue(_numVmHighVioNotLTReportVmGen[i] == _idxVmHighVioNotLReportVmGen[i].length);
			}
		}*/
		
		// separate _AllBrcDiffGen[][]  and _IdxBrcAllGen[][] by branch voltage level
		_numBrcVioLTReportVmGen = new int[_nVioBrcAllGen];
		_numBrcVioNotLTReportVmGen = new int[_nVioBrcAllGen];
		_numTotalBrcVioLTReportVmGen = 0;
		_numTotalBrcVioNotLTReportVmGen = 0;
		for (int i=0; i<_nVioBrcAllGen; i++)
		{
			float[] brcDiffPerCont = _AllBrcDiffGen[i];
			_numBrcVioLTReportVmGen[i] = 0;
			_numBrcVioNotLTReportVmGen[i] = 0;
			for (int j=0; j<brcDiffPerCont.length; j++)
			{
				int idx = _IdxBrcAllGen[i][j];
				int idxFrmBus =  _branches.getFromBus(idx).getIndex();
				int idxToBus =  _branches.getToBus(idx).getIndex();				
				boolean markHighVmBrc = true;
				if (_BaseKV[idxFrmBus] < _VmReportDiv) markHighVmBrc = false;
				if (_BaseKV[idxToBus] < _VmReportDiv) markHighVmBrc = false;
				if (markHighVmBrc == false) _numBrcVioLTReportVmGen[i]++; 
				else _numBrcVioNotLTReportVmGen[i]++; 
			}
			_numTotalBrcVioLTReportVmGen += _numBrcVioLTReportVmGen[i];
			_numTotalBrcVioNotLTReportVmGen += _numBrcVioNotLTReportVmGen[i];
		}
		
		_BrcDiffVioLTReportVmGen = new float[_nVioBrcAllGen][];
		_BrcDiffVioNotLTReportVmGen = new float[_nVioBrcAllGen][];
		_idxBrcVioLTReportVmGen = new int[_nVioBrcAllGen][];
		_idxBrcVioNotLTReportVmGen = new int[_nVioBrcAllGen][];
		for (int i=0; i<_nVioBrcAllGen; i++)
		{
			float[] brcDiffPerCont = _AllBrcDiffGen[i];
			_BrcDiffVioLTReportVmGen[i] = new float[_numBrcVioLTReportVmGen[i]];
			_BrcDiffVioNotLTReportVmGen[i] = new float[_numBrcVioNotLTReportVmGen[i]];
			_idxBrcVioLTReportVmGen[i] = new int[_numBrcVioLTReportVmGen[i]];
			_idxBrcVioNotLTReportVmGen[i] = new int[_numBrcVioNotLTReportVmGen[i]];
			
			int idx1 = 0;
			int idx2 = 0;			
			for (int j=0; j<brcDiffPerCont.length; j++)
			{
				int idx = _IdxBrcAllGen[i][j];
				float pfDiff = _AllBrcDiffGen[i][j];
				int idxFrmBus =  _branches.getFromBus(idx).getIndex();
				int idxToBus =  _branches.getToBus(idx).getIndex();	
				
				boolean markHighVmBrc = true;
				if (_BaseKV[idxFrmBus] < _VmReportDiv) markHighVmBrc = false;
				if (_BaseKV[idxToBus] < _VmReportDiv) markHighVmBrc = false;
				if (markHighVmBrc == false)
				{
					_BrcDiffVioLTReportVmGen[i][idx1] = pfDiff;
					_idxBrcVioLTReportVmGen[i][idx1] = idx;
					idx1++;
				}
				else
				{
					_BrcDiffVioNotLTReportVmGen[i][idx2] = pfDiff;
					_idxBrcVioNotLTReportVmGen[i][idx2] = idx;
					idx2++;
				}
			}
			int check = idx1 + idx2;
			assertTrue(check == brcDiffPerCont.length);
		}
		_numBrcVioLTReportVmGen = AuxArrayXL.remZeroFrmArray(_numBrcVioLTReportVmGen);
		_numBrcVioNotLTReportVmGen = AuxArrayXL.remZeroFrmArray(_numBrcVioNotLTReportVmGen);
		_BrcDiffVioLTReportVmGen = AuxArrayXL.shrinkTwoDimArray(_BrcDiffVioLTReportVmGen);
		_BrcDiffVioNotLTReportVmGen = AuxArrayXL.shrinkTwoDimArray(_BrcDiffVioNotLTReportVmGen);
		_idxBrcVioLTReportVmGen = AuxArrayXL.shrinkTwoDimArray(_idxBrcVioLTReportVmGen);
		_idxBrcVioNotLTReportVmGen = AuxArrayXL.shrinkTwoDimArray(_idxBrcVioNotLTReportVmGen);	
		
		if (_BrcDiffVioLTReportVmGen != null)
		{
			assertTrue(_numBrcVioLTReportVmGen.length == _BrcDiffVioLTReportVmGen.length);
			assertTrue(_numBrcVioLTReportVmGen.length == _idxBrcVioLTReportVmGen.length);
			for(int i=0; i<_numBrcVioLTReportVmGen.length; i++)
			{
				assertTrue(_numBrcVioLTReportVmGen[i] == _BrcDiffVioLTReportVmGen[i].length);
				assertTrue(_numBrcVioLTReportVmGen[i] == _idxBrcVioLTReportVmGen[i].length);
			}
		}
		if (_numBrcVioNotLTReportVmGen != null)
		{
			assertTrue(_numBrcVioNotLTReportVmGen.length == _BrcDiffVioNotLTReportVmGen.length);
			assertTrue(_numBrcVioNotLTReportVmGen.length == _idxBrcVioNotLTReportVmGen.length);
			for(int i=0; i<_numBrcVioNotLTReportVmGen.length; i++)
			{
				assertTrue(_numBrcVioNotLTReportVmGen[i] == _BrcDiffVioNotLTReportVmGen[i].length);
				assertTrue(_numBrcVioNotLTReportVmGen[i] == _idxBrcVioNotLTReportVmGen[i].length);
			}
		}
	}
	
	
	/** Output violation information for base case 
	 * @Override @throws IOException */
	public void outputVioInfoBaseCase() throws PsseModelException, IOException
	{
		//_pf.outputResults();
		String rootDr=getPath();
		if (_convPfBase == true)
		{
	        if (_MarkvioBase)
		    {
		    	if (_MarkvioVmBase)
		    	{
		    		try
		    		{
	          			File VmVioBase = new File(rootDr+"Vm_Violations_Base.txt");
	          			deleteFile(VmVioBase);
	          			OutputStream resultFile = new FileOutputStream(rootDr+"Vm_Violations_Base.txt", true);
	           		    PrintStream outFile = new PrintStream (resultFile);
	           		    outFile.println(" No., BusIndex, Vm, Vm_Violations_Base");
	           		    for (int nv=0; nv<_sizeVmVioBase; nv++)
	           		    {
	           		    	int idx = nv + 1;
	        				outFile.print(" "+idx);
	        				outFile.print(" "+(_getIdxVmVioBase[nv]+1));
	        				outFile.print(" "+_VmVioBase[nv]);
	        				outFile.print(" "+_VmVioDiffBase[nv]);
	        				outFile.println();
	           		    }
	              		outFile.close();
	               		resultFile.close();       		
	               		if (_showRunInfo == true) System.out.println("Output Vm Violation Data for base case successfully");
		    		}
		    		catch (IOException e) {
	    		    	System.out.println();
	    		    	System.out.println("Cannot write Vm Violation for base case info to file" + e);
	    		    	e.printStackTrace();
		    		}
		    	}
		    	else if (_showRunInfo == true) System.out.println("There is no voltage violations in the base case.");
		    	
		    	if (_MarkvioBrcBase)
		    	{
		    		try
		    		{
	          			File BrcVioBase = new File(rootDr+"Brc_Violations_Base.txt");
	          			deleteFile(BrcVioBase);
	           		    OutputStream resultFile = new FileOutputStream(rootDr+"Brc_Violations_Base.txt", true);
	           		    PrintStream outFile = new PrintStream (resultFile);
	           		    outFile.println(" No., BrcIndex, frmBus, toBus, Sfrm, Sto, _RateUsed, S_Vio=");
	           		    for (int nv=0; nv<_sizeBrcVioBase; nv++)
	           		    {
	           		    	int ndxBrc = _getIdxBrcVioBase[nv];
	           		    	ACBranch br = _branches.get(ndxBrc);
	           		    	assertTrue(br.isInSvc()==true);
	    					int fbus = br.getFromBus().getIndex() + 1;
	    					int tbus = br.getToBus().getIndex() + 1;
	           		    	int idx = nv + 1;
	        				outFile.print(" "+idx);
	        				outFile.print(" "+(ndxBrc+1));
	        				outFile.print(" "+fbus);
	        				outFile.print(" "+tbus);
	        				outFile.print(" "+_sfrmVioBase[nv]);
	        				outFile.print(" "+_stoVioBase[nv]);
	        				outFile.print(" "+_rateUsedBase[ndxBrc]);
	        				outFile.print(" "+_brcDiffVioBase[nv]);
	        				outFile.println();
	           		    }
	              		outFile.close();
	               		resultFile.close();       		
	               		if (_showRunInfo == true) System.out.println("Output Brc Violation Data for base case successfully");
		    		}
		    		catch (IOException e) {
	    		    	System.out.println();
	    		    	System.out.println("Cannot write Brc Violation info for base case to file" + e);
	    		    	e.printStackTrace();
		    		}
		    	}
		    	else
		    	{
		    		if (_showRunInfo == true) System.out.println("There is no branch thermal violations in the base case.");
		    	}
		    }
		    else
		    {
		    	if (_showRunInfo == true) System.out.println("There is no violations in the base case.");
		    }
		} else {
			if (_showRunInfo == true) System.out.println("The power flow cannot converge for the base case.");
  			File NotConvBase = new File(rootDr+"Not_Converge_Base.txt");
  			deleteFile(NotConvBase);
   		    OutputStream resultFile = new FileOutputStream(rootDr+"Not_Converge_Base.txt", true);
   		    PrintStream outFile = new PrintStream (resultFile);	    
   		    
   		    // For now, nothing is output to this file.
   		    
      		resultFile.close();
      		outFile.close();
		}
	}
	
	@Override
	public void outputGeneralInfoTmp(float[] times) throws PsseModelException 
	{
		String rootDr=getGeneInfoPath();
		if (_uri != null)
		{
			try
			{
	   		    OutputStream resultFile = new FileOutputStream(rootDr+"FilesChecked.txt", true);
	   		    PrintStream outFile = new PrintStream (resultFile);
	   		    outFile.println(_uri);
	   		    
	   			resultFile.close();
	   			outFile.close();
	   			if (_showRunInfo == true) {System.out.println(); System.out.println("Output filesChecked.txt to a file successfully.");}
	   		} catch (IOException e) {
	   			System.out.println();
	   			System.out.println("Fail to output FilesChecked Info to a file" + e);
	   			e.printStackTrace();
	   		}
		}
		
		// output the voltage violation information.
		try
		{
  			//File CaGeneralInfo = new File(rootDr+"CaGeneralInfo.txt");
   		    OutputStream resultFile = new FileOutputStream(rootDr+"CaGeneralInfo.txt", true);
   		    PrintStream outFile = new PrintStream (resultFile);
   		    			
			//float maxA = Math.max(_maxBrcVioPerctAllTransm, _maxBrcVioPerctAllGen) * 100;
			float maxB = Math.max(_maxBrcSdiffAllTransm, _maxBrcSdiffAllGen);
			int index = _idxMaxBrcSdiffAllGen;
			if (maxB == _maxBrcSdiffAllTransm) index = _idxMaxBrcSdiffAllTransm;
			maxB = maxB * 100;
			float maxA = 0;
			if (maxB > 0) maxA = 100 * maxB / getRateC()[index];
			float maxC;
			if (Math.abs(_maxVmdiffAllTransm) > Math.abs(_maxVmdiffAllGen)) maxC = _maxVmdiffAllTransm;
			else maxC = _maxVmdiffAllGen;
			if (maxC > 0) maxC = _Vmax + maxC;
			else maxC = _Vmin + maxC;
			float maxD = _Vmin + Math.min(_maxVmLowdiffAllTransm, _maxVmLowdiffAllGen);
			float maxE = _Vmax + Math.max(_maxVmHighdiffAllTransm, _maxVmHighdiffAllGen);
			
			float maxF = _sumVioBrcWorstContTransm;
			int idxWorstCont = _idxVioBrcWorstContTransm;
			idxWorstCont++;
			if (_sumVioBrcWorstContGen > maxF)
			{
				maxF = _sumVioBrcWorstContGen;
				idxWorstCont = _idxVioBrcWorstContGen * -1;
				idxWorstCont--;
			}
			maxF = maxF * 100;

			int[]aa = { (_numTestedNm1Transm + _numTestedNm1Gen),       (_numNotConvTransm + _numNotConvGen), 
				    (_numContPVtoPQTransm + _numContPVtoPQGen),     
				    (_nContVioVmAndBrcAllTransm + _nContVioVmAndBrcAllGen),				    
				    (_nVioBrcAllTransm + _nVioBrcAllGen),
				    (_nVioVmAllTransm + _nVioVmAllGen),             (_nVioVmLowAllTransm + _nVioVmLowAllGen),
				    (_nVioVmHighAllTransm + _nVioVmHighAllGen),     
				    (_totalBrcVioNumAllTransm + _totalBrcVioNumAllGen),
				    
				    (_numTotalBrcVioLTReportVmTransm + _numTotalBrcVioLTReportVmGen),
				    (_numTotalBrcVioNotLTReportVmTransm + _numTotalBrcVioNotLTReportVmGen),
				    
				    (_numVmVioAllTransm + _numVmVioAllGen),         (_numVmLowVioAllTransm + _numVmLowVioAllGen),
				    (_numVmHighVioAllTransm + _numVmHighVioAllGen), 
				    (_numTotalVmLowVioLTReportVmTransm + _numTotalVmLowVioLTReportVmGen),
				    (_numTotalVmLowVioNotLTReportVmTransm + _numTotalVmLowVioNotLTReportVmGen),
				    (_numTotalVmHighVioLTReportVmTransm + _numTotalVmHighVioLTReportVmGen),
				    (_numTotalVmHighVioNotLTReportVmTransm + _numTotalVmHighVioNotLTReportVmGen),				    
				     idxWorstCont  };			
			
			float[] tmp1 = AuxArrayXL.getSD(_AllBrcDiffTransm, _AllBrcDiffGen, 0);
			float[] tmp1L = AuxArrayXL.getSD(_BrcDiffVioLTReportVmTransm, _BrcDiffVioLTReportVmGen, 0);
			float[] tmp1H = AuxArrayXL.getSD(_BrcDiffVioNotLTReportVmTransm, _BrcDiffVioNotLTReportVmGen, 0);
			float[] tmp2 = AuxArrayXL.getSD(_VmLowVioLTReportVmTransm, _VmLowVioLTReportVmGen, _Vmin);
			float[] tmp3 = AuxArrayXL.getSD(_VmLowVioNotLReportVmTransm, _VmLowVioNotLReportVmGen, _Vmin);
			float[] tmp4 = AuxArrayXL.getSD(_VmHighVioLTReportVmTransm, _VmHighVioLTReportVmGen, _Vmax);
			float[] tmp5 = AuxArrayXL.getSD(_VmHighVioNotLReportVmTransm, _VmHighVioNotLReportVmGen, _Vmax);

			float asdf0L = AuxArrayXL.calculateAverageOfArray(_BrcDiffVioLTReportVmTransm, _BrcDiffVioLTReportVmGen);			
			float asdf0H = AuxArrayXL.calculateAverageOfArray(_BrcDiffVioNotLTReportVmTransm, _BrcDiffVioNotLTReportVmGen);			
			float asdf1 = AuxArrayXL.calculateAverageOfArray(_VmLowVioLTReportVmTransm, _VmLowVioLTReportVmGen);
			float asdf2 = AuxArrayXL.calculateAverageOfArray(_VmLowVioNotLReportVmTransm, _VmLowVioNotLReportVmGen);
			float asdf3 = AuxArrayXL.calculateAverageOfArray(_VmHighVioLTReportVmTransm, _VmHighVioLTReportVmGen);
			float asdf4 = AuxArrayXL.calculateAverageOfArray(_VmHighVioNotLReportVmTransm, _VmHighVioNotLReportVmGen);
			
   		    float[] tmp = { maxB, maxC, maxD, maxE, maxF, 
   		    		           tmp1[0]*100, tmp1L[0]*100, tmp1H[0]*100, tmp2[0], tmp3[0], tmp4[0], tmp5[0], 
   		    		           tmp1[1]*100, tmp1L[1]*100, tmp1H[1]*100, tmp2[1], tmp3[1], tmp4[1], tmp5[1], 
   		    		           tmp1[2]*100, tmp1L[2]*100, tmp1H[2]*100, tmp2[2], tmp3[2], tmp4[2], tmp5[2], 
   		    		           asdf0L*100, asdf0H*100, asdf1, asdf2, asdf3, asdf4,
   		    		           (_sumAllSdiffTransm + _sumAllSdiffGen)*100, (_sumAllVmVioTransm + _sumAllVmVioGen),
   		    		           (_sumAllVmLowVioTransm + _sumAllVmLowVioGen), (_sumAllVmHighVioTransm + _sumAllVmHighVioGen)  };
   		    
   		    int length1 = tmp.length;
   		    int lengthT = times.length;
   		    float[] bb = new float[length1 + lengthT];
   		    System.arraycopy(tmp, 0, bb, 0, length1);
   		    System.arraycopy(times, 0, bb, length1, lengthT);
   		    
   		    outputElemFrmArray(outFile, aa);
   		    outFile.print("  "+maxA+"%");
      		outputElemFrmArray(outFile, bb);
      		outFile.println();
      		
   			resultFile.close();
   			outFile.close();
   			if (_showRunInfo == true) System.out.println("Output CaGeneral Info to a file successfully.");
   		} catch (IOException e) {
   			System.out.println();
   			System.out.println("Fail to output CaGeneral Info to a file" + e);
   			e.printStackTrace();
   		}
		try
		{						
  			//File CaGeneralInfo_Gen = new File(rootDr+"CaGeneralInfo_Gen.txt");
   		    OutputStream resultFile = new FileOutputStream(rootDr+"CaGeneralInfo_Gen.txt", true);
   		    PrintStream outFile = new PrintStream (resultFile);
   		    
			//float maxA = _maxBrcVioPerctAllGen * 100;
			float maxB = _maxBrcSdiffAllGen * 100;
			float maxA = 100 * maxB / getRateC()[_idxMaxBrcSdiffAllGen];
			float maxC = _maxVmdiffAllGen;
			if (maxC > 0) maxC = _Vmax + maxC;
			else maxC = _Vmin + maxC;
			float maxD = _Vmin + _maxVmLowdiffAllGen;
			float maxE = _Vmax + _maxVmHighdiffAllGen;
			
			float maxF = _sumVioBrcWorstContGen;
			maxF = maxF * 100;
			int idxWorstCont = _idxVioBrcWorstContGen;
			idxWorstCont++;

			int[]aa = { (_numTestedNm1Gen),           (_numNotConvGen), 
				    (_numContPVtoPQGen),          
				    (_nContVioVmAndBrcAllGen),				    
				    ( _nVioBrcAllGen),
				    ( _nVioVmAllGen),             (_nVioVmLowAllGen),
				    (_nVioVmHighAllGen),          ( _totalBrcVioNumAllGen),
				    
				    (_numTotalBrcVioLTReportVmGen),
				    (_numTotalBrcVioNotLTReportVmGen),

				    ( _numVmVioAllGen),           (_numVmLowVioAllGen),
				    ( _numVmHighVioAllGen),       
				    (_numTotalVmLowVioLTReportVmGen),
				    (_numTotalVmLowVioNotLTReportVmGen),
				    (_numTotalVmHighVioLTReportVmGen),
				    (_numTotalVmHighVioNotLTReportVmGen),				    
				      idxWorstCont };	   	
			
			assertTrue((_numTotalVmLowVioLTReportVmGen+_numTotalVmLowVioNotLTReportVmGen)==_numVmLowVioAllGen);
			assertTrue((_numTotalVmHighVioLTReportVmGen+_numTotalVmHighVioNotLTReportVmGen)==_numVmHighVioAllGen);		
			
			float[] tmp1 = AuxArrayXL.getSD(_AllBrcDiffGen, 0);
			float[] tmp1L = AuxArrayXL.getSD(_BrcDiffVioLTReportVmGen, 0);
			float[] tmp1H = AuxArrayXL.getSD(_BrcDiffVioNotLTReportVmGen, 0);
			float[] tmp2 = AuxArrayXL.getSD(_VmLowVioLTReportVmGen, _Vmin);
			float[] tmp3 = AuxArrayXL.getSD(_VmLowVioNotLReportVmGen, _Vmin);
			float[] tmp4 = AuxArrayXL.getSD(_VmHighVioLTReportVmGen, _Vmax);
			float[] tmp5 = AuxArrayXL.getSD(_VmHighVioNotLReportVmGen, _Vmax);

			float asdf0L = AuxArrayXL.calculateAverageOfArray(_BrcDiffVioLTReportVmGen);			
			float asdf0H = AuxArrayXL.calculateAverageOfArray(_BrcDiffVioNotLTReportVmGen);			
			float asdf1 = AuxArrayXL.calculateAverageOfArray(_VmLowVioLTReportVmGen);
			float asdf2 = AuxArrayXL.calculateAverageOfArray(_VmLowVioNotLReportVmGen);
			float asdf3 = AuxArrayXL.calculateAverageOfArray(_VmHighVioLTReportVmGen);
			float asdf4 = AuxArrayXL.calculateAverageOfArray(_VmHighVioNotLReportVmGen);

   		    float[] tmp = { maxB, maxC, maxD, maxE, maxF, 
    		           tmp1[0]*100, tmp1L[0]*100, tmp1H[0]*100, tmp2[0], tmp3[0], tmp4[0], tmp5[0], 
    		           tmp1[1]*100, tmp1L[1]*100, tmp1H[1]*100, tmp2[1], tmp3[1], tmp4[1], tmp5[1], 
    		           tmp1[2]*100, tmp1L[2]*100, tmp1H[2]*100, tmp2[2], tmp3[2], tmp4[2], tmp5[2], 
    		           asdf0L*100, asdf0H*100, asdf1, asdf2, asdf3, asdf4,
   		    		          ( _sumAllSdiffGen * 100), ( _sumAllVmVioGen),
   		    		          ( _sumAllVmLowVioGen), ( _sumAllVmHighVioGen)  };
   		    
   		    /*int length1 = tmp.length;
   		    int lengthT = times.length;
   		    float[] bb = new float[length1 + lengthT];
   		    System.arraycopy(tmp, 0, bb, 0, length1);
   		    System.arraycopy(times, 0, bb, length1, lengthT);*/
   		    
   		    outputElemFrmArray(outFile, aa);
   		    outFile.print("  "+maxA+"%");
      		//outputElemFrmArray(outFile, bb);
      		outputElemFrmArray(outFile, tmp);
      		outFile.println();
      		
   			resultFile.close();
   			outFile.close();
   			if (_showRunInfo == true) System.out.println("Output CaGeneralInfo_Gen to a file successfully.");
   		} catch (IOException e) {
   			System.out.println();
   			System.out.println("Fail to output CaGeneralInfo_Gen to a file" + e);
   			e.printStackTrace();
   		}
		try
		{						
  			//File CaGeneralInfo_Transm = new File(rootDr+"CaGeneralInfo_Transm.txt");
   		    OutputStream resultFile = new FileOutputStream(rootDr+"CaGeneralInfo_Transm.txt", true);
   		    PrintStream outFile = new PrintStream (resultFile);

   		    //float maxA = _maxBrcVioPerctAllTransm * 100;
			float maxB = _maxBrcSdiffAllTransm * 100;
			float maxA = 100 * maxB / getRateC()[_idxMaxBrcSdiffAllTransm];
			float maxC = _maxVmdiffAllTransm;
			if (maxC > 0) maxC = _Vmax + maxC;
			else maxC = _Vmin + maxC;
			float maxD = _Vmin + _maxVmLowdiffAllTransm;
			float maxE = _Vmax + _maxVmHighdiffAllTransm;
			
			float maxF = _sumVioBrcWorstContTransm;
			maxF = maxF * 100;
			int idxWorstCont = _idxVioBrcWorstContTransm;
			idxWorstCont++;
			
			assertTrue((_numTotalVmLowVioLTReportVmTransm+_numTotalVmLowVioNotLTReportVmTransm)==_numVmLowVioAllTransm);
			assertTrue((_numTotalVmHighVioLTReportVmTransm+_numTotalVmHighVioNotLTReportVmTransm)==_numVmHighVioAllTransm);		
			
			int[]aa = {  (_numTestedNm1Transm ),           
					     (_numNotConvTransm), 
				         (_numContPVtoPQTransm ),          
				         (_nContVioVmAndBrcAllTransm),				         
				         (_nVioBrcAllTransm ),
				         (_nVioVmAllTransm ),              (_nVioVmLowAllTransm),
				         (_nVioVmHighAllTransm ),          (_totalBrcVioNumAllTransm ),
				         
						 (_numTotalBrcVioLTReportVmTransm),
						 (_numTotalBrcVioNotLTReportVmTransm),

				         (_numVmVioAllTransm ),            (_numVmLowVioAllTransm ),
				         (_numVmHighVioAllTransm ),        
						 (_numTotalVmLowVioLTReportVmTransm),
						 (_numTotalVmLowVioNotLTReportVmTransm),
						 (_numTotalVmHighVioLTReportVmTransm),
						 (_numTotalVmHighVioNotLTReportVmTransm),				    
				         idxWorstCont };
			
			float[] tmp1 = AuxArrayXL.getSD(_AllBrcDiffTransm, 0);
			float[] tmp1L = AuxArrayXL.getSD(_BrcDiffVioLTReportVmTransm, 0);
			float[] tmp1H = AuxArrayXL.getSD(_BrcDiffVioNotLTReportVmTransm, 0);
			float[] tmp2 = AuxArrayXL.getSD(_VmLowVioLTReportVmTransm, _Vmin);
			float[] tmp3 = AuxArrayXL.getSD(_VmLowVioNotLReportVmTransm, _Vmin);
			float[] tmp4 = AuxArrayXL.getSD(_VmHighVioLTReportVmTransm, _Vmax);
			float[] tmp5 = AuxArrayXL.getSD(_VmHighVioNotLReportVmTransm, _Vmax);
			
			float asdf0L = AuxArrayXL.calculateAverageOfArray(_BrcDiffVioLTReportVmTransm);			
			float asdf0H = AuxArrayXL.calculateAverageOfArray(_BrcDiffVioNotLTReportVmTransm);			
			float asdf1 = AuxArrayXL.calculateAverageOfArray(_VmLowVioLTReportVmTransm);
			float asdf2 = AuxArrayXL.calculateAverageOfArray(_VmLowVioNotLReportVmTransm);
			float asdf3 = AuxArrayXL.calculateAverageOfArray(_VmHighVioLTReportVmTransm);
			float asdf4 = AuxArrayXL.calculateAverageOfArray(_VmHighVioNotLReportVmTransm);
						
   		    float[] tmp = { maxB, maxC, maxD, maxE, maxF,
    		           tmp1[0]*100, tmp1L[0]*100, tmp1H[0]*100, tmp2[0], tmp3[0], tmp4[0], tmp5[0], 
    		           tmp1[1]*100, tmp1L[1]*100, tmp1H[1]*100, tmp2[1], tmp3[1], tmp4[1], tmp5[1], 
    		           tmp1[2]*100, tmp1L[2]*100, tmp1H[2]*100, tmp2[2], tmp3[2], tmp4[2], tmp5[2], 
    		           asdf0L*100, asdf0H*100, asdf1, asdf2, asdf3, asdf4,
	                            (_sumAllSdiffTransm * 100), (_sumAllVmVioTransm ),
   		    		            (_sumAllVmLowVioTransm ), (_sumAllVmHighVioTransm )  };
   		    
   		    /*int length1 = tmp.length;
   		    int lengthT = times.length;
   		    float[] bb = new float[length1 + lengthT];
   		    System.arraycopy(tmp, 0, bb, 0, length1);
   		    System.arraycopy(times, 0, bb, length1, lengthT);*/
   		    
   		    outputElemFrmArray(outFile, aa);
   		    outFile.print("  "+maxA+"%");
      		//outputElemFrmArray(outFile, bb);
      		outputElemFrmArray(outFile, tmp);
      		outFile.println();
      		
   			resultFile.close();
   			outFile.close();
   			if (_showRunInfo == true) System.out.println("Output CaGeneralInfo_Transm to a file successfully.");
   		} catch (IOException e) {
   			System.out.println();
   			System.out.println("Fail to output CaGeneralInfo_Transm to a file" + e);
   			e.printStackTrace();
   		}
	}
	
	
	public static void main(String[] args) throws Exception
	{
		long t_Start = System.nanoTime();
		String uri = null;
		String svstart = "Flat";
		float minxmag = 0.0001f;
		for(int i=0; i < args.length;)
		{
			String s = args[i++].toLowerCase();
			int ssx = 1;
			if (s.startsWith("--")) ++ssx;
			switch(s.substring(ssx))
			{
				case "uri":
					uri = args[i++];
					break;
				case "voltage":
					svstart = args[i++];
					break;
				case "minxmag":
					minxmag = Float.parseFloat(args[i++]);
					break;
			}
		}
		
		VoltageSource vstart = VoltageSource.fromConfig(svstart);
		if (uri == null)
		{
			System.err.format("Usage: -uri model_uri [-voltage flat|realtime] [ -minxmag smallest reactance magnitude (default to 0.001) ] [ -debug dir ] [ -results path_to_results]");
			System.exit(1);
		}

 	    PsseModel model = PsseModel.Open(uri);
 	    model.setMinXMag(minxmag);
 	    
		ParamIO.setCAFolder(ParamIO.getCAFolder()+"CaSeq_"+model.getFileName()+"/");
		AuxFileXL.createFolder(ParamIO.getCAPath());
		ContingencyAnalysis ca = new ContingencyAnalysisSeq(model, vstart);
		ca.setGeneInfoPath(ParamIO.getOutPath() + ParamIO.getCAFolder());
		ca.setURI(uri);
		ca.setShowRunInfo(true);

	    ca.runGenContingencyAnalysis();
		System.out.println("   Gen time : " + (System.nanoTime() - t_Start)/1e9f);		
	    ca.runTransmContingencyAnalysis();
		System.out.println("   Gen + Transm time : " + (System.nanoTime() - t_Start)/1e9f);		
		long t_EndCA = System.nanoTime();
		
		ca.outputVioInfoBaseCase();
		ca.outputVioInfoTransmCont();
		ca.outputVioInfoGenCont();

		long t_EndOutputCA = System.nanoTime();
		
		float[] times = new float[2];
		times[0] = (t_EndCA - t_Start)/1e9f;
		times[1] = (t_EndOutputCA - t_Start)/1e9f;		
		ca.outputGeneralInfoTmp(times);
		
		model.getContingencyListGenData().dumpContiList();
		model.getContingencyListBrcData().dumpContiList();

		SelectKeyContingencies selectKeyConti = new SelectKeyContingencies(ca);
		selectKeyConti.launch();		
		selectKeyConti.dumpKeyBrcContiList();
		selectKeyConti.dumpKeyGenContiList();

		System.out.println();
		System.out.println("   Total time : " + (System.nanoTime() - t_Start)/1e9f);		
	}
}
