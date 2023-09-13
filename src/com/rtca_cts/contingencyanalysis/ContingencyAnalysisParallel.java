package com.rtca_cts.contingencyanalysis;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;

import mpi.MPI;

import com.powerdata.openpa.psse.ACBranch;
import com.powerdata.openpa.psse.PsseModel;
import com.powerdata.openpa.psse.PsseModelException;
import com.powerdata.openpa.psse.powerflow.VoltageSource;
import com.rtca_cts.param.ParamIO;
import com.utilxl.iofiles.AuxFileXL;
import com.utilxl.tools.para.MPJExpressXL;

/**
 * N-1 Contingency Analysis;
 * Parallel Computing version using MPJ Express;
 * It has passed tests on Windows 7 (64bits). 
 * 
 * Initialized in Mar. 2014.
 * 
 * @author Xingpeng.Li (xplipower@gmail.com)
 * 
 */
public class ContingencyAnalysisParallel extends ContingencyAnalysis
{
	MPJExpressXL _paraMPJ;
	
	// divide branches for parallel
	int _nStartTransm;
	int _nEndTransm;
	int _nIterTransm;
	
	// divide Gens for parallel
	int _nStartGen;
	int _nEndGen;
	int _nIterGen;

	
	public ContingencyAnalysisParallel(PsseModel model)
			throws PsseModelException, IOException
	{
		super(model);
		_nproc = MPI.COMM_WORLD.Size();
		_rank = MPI.COMM_WORLD.Rank();
		initialPara();
	}
	
	public ContingencyAnalysisParallel(PsseModel model, int nproc, int rank)
			throws PsseModelException, IOException
	{
		super(model);
		_nproc = nproc;
		_rank = rank;
		initialPara();
	}
	
	public ContingencyAnalysisParallel(PsseModel model, VoltageSource vstart, int nproc, int rank)
			throws PsseModelException, IOException
	{
		super(model, vstart);
		_nproc = nproc;
		_rank = rank;
		initialPara();
	}
	
	public void initialPara() throws PsseModelException
	{
		if(_rank == 0) AuxFileXL.createFolder(ParamIO.getOutPath());
		_path = ParamIO.getCAPath();
		if(_rank == 0) AuxFileXL.createFolder(_path);
		_pathForGeneInfo = _path;
//		_pathForGeneInfo = ParamIO.getCAGeneInfoPath();
//		if(_rank == 0) AuxFileXL.createFolder(_pathForGeneInfo);

//		if(_rank == 0) AuxFileXL.createFolder(ParamIO.getCAParaPath());
//		_path = ParamIO.getCAParaPath() + "_nproc_"+ _nproc+"/";
//		if(_rank == 0) AuxFileXL.createFolder(_path);
		
//		_pathForGeneInfo = System.getProperty("user.dir")+"/filesOfOutput_rank_"+String.valueOf(_rank)+"/";
//		AuxFileXL.createFolder(_pathForGeneInfo);

		_paraMPJ = new MPJExpressXL(_nproc, _rank);
		int[] tmp = _paraMPJ.assignWorkPara(_nbr);
		_nStartTransm = tmp[0];
		_nEndTransm = tmp[1];
		_nIterTransm = tmp[2];
		
		tmp = _paraMPJ.assignWorkPara(_nGen);
		_nStartGen = tmp[0];
		_nEndGen = tmp[1];
		_nIterGen = tmp[2];		
	}
	
	
	@Override
	public void runTransmContingencyAnalysis() throws PsseModelException, IOException
	{
		assertTrue(checkOrigPf() == true); // this line is needed.

        int[] idxContiBrc = null;
        {
        	if (_rank == 0) System.out.println("Transmission contingency analysis starts running");
			boolean[] checkBrcConti = _model.getContingencyListBrcData().getIsBrcInContiList();

        	int num = 0;
        	int[] tmpArray = new int[_nbr];
        	for (int i=0; i<_nbr; i++)
        		if (checkBrcConti[i] == true) tmpArray[num++] = i;
        	
        	idxContiBrc = Arrays.copyOf(tmpArray, num);
    		int[] tmp = _paraMPJ.assignWorkPara(num);
    		_nStartTransm = tmp[0];
    		_nEndTransm = tmp[1];
    		_nIterTransm = tmp[2];
    		if (_showRunInfo == true) System.out.println("num: "+num+", nIter: "+_nIterTransm+", _nStartTransm: "+_nStartTransm+", _nEndTransm: "+_nEndTransm+", rank: "+_rank);
        }

		_PfNotCTransm = new PfNotConverge(_nIterTransm);
		_testAllTransm = new VioResultAll(_nIterTransm);
		for(int idummy=_nStartTransm; idummy<=_nEndTransm; idummy++)
		{
			int i = idxContiBrc[idummy];
			boolean Tm1Check = true;
			if (Tm1Check == true)
			{
				boolean inSvc = _branches.isInSvc(i);
				if (inSvc == true) runSingleTransmConti(i);
			}
		}
		if (_showRunInfo == true) System.out.println("_rank: "+_rank+" is done with Transmission Contingency Analysis. # of Contingencies checked: "+_numTestedNm1Transm);
		String element = "Brc";
		ThreadComm(_testAllTransm, _PfNotCTransm, element, _numTestedNm1Transm);
		if (_rank == 0) analyzeVioInfoTransm();
    	if (_rank == 0) System.out.println("Transmission contingency analysis simulation is done");
	}
	
	@Override
	public void runGenContingencyAnalysis() throws PsseModelException, IOException
	{
		assertTrue(checkOrigPf() == true); // this line is needed.

		if ((_optionPfactor < 0) || (_optionPfactor > 3)) System.err.println("Gen Contingency option is not chosen properly..");
		float[] MH = null;
		if (_optionPfactor == 3) {initGenDyr(); MH = _genDyr.getGenSH();}
		
        int[] idxContiGen = null;
        {
        	if (_rank == 0) System.out.println("Generator contingency analysis starts running");
			boolean[] checkGenConti = _model.getContingencyListGenData().getIsGenInContiList();

			int num = 0;
        	int[] tmpArray = new int[_nGen];
        	for (int i=0; i<_nGen; i++)
        		if (checkGenConti[i] == true) tmpArray[num++] = i;

        	idxContiGen = Arrays.copyOf(tmpArray, num);
    		int[] tmp = _paraMPJ.assignWorkPara(num);
    		_nStartGen = tmp[0];
    		_nEndGen = tmp[1];
    		_nIterGen = tmp[2];
    		if (_showRunInfo == true) System.out.println("num: "+num+", nIter: "+_nIterGen+", _nStartGen: "+_nStartGen+", _nEndGen: "+_nEndGen+", rank: "+_rank);
        }
        
		_PfNotCGen = new PfNotConverge(_nIterGen);
		_testAllGen = new VioResultAll(_nIterGen);
		for (int idummy=_nStartGen; idummy<=_nEndGen; idummy++)
		{
			int i = idxContiGen[idummy];
			boolean Gm1Check = true;
			if (Gm1Check == true)
			{
				boolean inSvc = _gens.isInSvc(i);
				assertTrue(inSvc == true);
				if (inSvc == true) runSingleGenConti(i, MH);
			}
		}
		if (_showRunInfo == true) System.out.println("_rank: "+_rank+" is done with Generator Contingency Analysis. # of Contingencies checked: "+_numTestedNm1Gen);
		String element = "Gens";
		ThreadComm(_testAllGen, _PfNotCGen, element, _numTestedNm1Gen);
		if (_rank == 0) analyzeVioInfoGen();
		if (_rank == 0) System.out.println("Generator contingency analysis simulation is done");
	}
	

	public void ThreadComm(VioResultAll testAll, PfNotConverge PfNotC, String element, int numElemTestedNm1)
	{
		ThreadCommNumElemTestedNm1(element, numElemTestedNm1);
		if (_MarkGenVarLimit) ThreadCommInfoPVtoPQ(testAll, element);
		ThreadComm(testAll, PfNotC, element);
	}
	
	public void ThreadCommNumElemTestedNm1(String element, int numElemTestedNm1)
	{
		int MarkElement = checkElemMark(element);
		if (MarkElement == 1) _numTestedNm1Transm = _paraMPJ.ThreadCommSumOneNumber(numElemTestedNm1);
		else if (MarkElement == 2) _numTestedNm1Gen = _paraMPJ.ThreadCommSumOneNumber(numElemTestedNm1);
		else System.err.println("Something wrong happened when assign data (NotConv) to field variables.");
	}
	
	void ThreadCommInfoPVtoPQ(VioResultAll testAll, String element)
	{
		int MarkElement = checkElemMark(element);
		if (MarkElement == 1)
		{
			_numContPVtoPQTransm = _paraMPJ.ThreadCommSumOneNumber(testAll.getNumContPVtoPQ());
			_idxContPVtoPQTransm = _paraMPJ.ThreadCommOneArray(testAll.getIdxContPVtoPQ());
			_numPVtoPQEachContTransm = _paraMPJ.ThreadCommOneArray(testAll.getNumPVtoPQEachCont());
			_numOrigPVEachContTransm = _paraMPJ.ThreadCommOneArray(testAll.getNumOrigPVEachCont());
		}
		else if (MarkElement == 2)
		{
			_numContPVtoPQGen = _paraMPJ.ThreadCommSumOneNumber(testAll.getNumContPVtoPQ());
			_idxContPVtoPQGen = _paraMPJ.ThreadCommOneArray(testAll.getIdxContPVtoPQ());
			_numPVtoPQEachContGen = _paraMPJ.ThreadCommOneArray(testAll.getNumPVtoPQEachCont());
			_numOrigPVEachContGen = _paraMPJ.ThreadCommOneArray(testAll.getNumOrigPVEachCont());
		}
		else System.err.println("Something wrong happened when assign data (NotConv) to field variables.");
	}
		
	public void ThreadComm(VioResultAll testAll, PfNotConverge PfNotC, String element)
	{
		ThreadCommNotConv(PfNotC, element);
		ThreadCommVmAndBrc(testAll, element);
		ThreadCommVm(testAll, element);
		ThreadCommBrc(testAll, element);
	}
	
	int checkElemMark(String element)
	{
		int MarkElement = -1;
		String s = element.toLowerCase();
		switch(s)
		{
			case "brc":
			case "branch":
			case "branches":
				MarkElement = 1;
				break;
			case "gen":
			case "gens":
			case "generator":
			case "generators":
				MarkElement = 2;
				break;
		}
		assert(MarkElement != -1);
		return MarkElement;
	}
	
	// all data are passed to the thread whose rank is 0.
	public void ThreadCommNotConv(PfNotConverge PfNotC, String element)
	{
		int MarkElement = checkElemMark(element);
		int numNotConv = PfNotC.getNumNotConv();      // the number of Gens contingencies that the power flow can not converge
		int[] IdxNotConv = PfNotC.getAllContinNotConv();    // store the index of contingency Gens, which the power flow program can not converge under those contingencies 
		assertTrue(IdxNotConv.length == numNotConv);
		if (MarkElement == 1)
		{
			_numNotConvTransm = _paraMPJ.ThreadCommSumOneNumber(numNotConv);
			_IdxNotConvTransm = _paraMPJ.ThreadCommOneArray(IdxNotConv);
		}
		else if (MarkElement == 2)
		{
			_numNotConvGen = _paraMPJ.ThreadCommSumOneNumber(numNotConv);
			_IdxNotConvGen = _paraMPJ.ThreadCommOneArray(IdxNotConv);
		}
	}
	
	
	public void ThreadCommVmAndBrc(VioResultAll testAll, String element)
	{
		int MarkElement = checkElemMark(element);
		if (MarkElement == 1)
		{
			_nContVioVmAndBrcAllTransm = _paraMPJ.ThreadCommSumOneNumber(testAll.getNumContVio());
			_IdxContVioVmAndBrcAllTransm = _paraMPJ.ThreadCommOneArray(testAll.getIdxContVio());
			_idxMapContToVmVioContTransm = ThreadCommPartiIndexArray(testAll.getIdxMapContToVmVioCont());
			_idxMapContToBrcVioContTransm = ThreadCommPartiIndexArray(testAll.getIdxMapContToBrcVioCont());
			_sumBrcVioExtPerContTransm = _paraMPJ.ThreadCommOneArray(testAll.getSumBrcVioExt());
			_sumVmVioExtPerContTransm = _paraMPJ.ThreadCommOneArray(testAll.getSumVmVioExt());
		}
		else if (MarkElement == 2)
		{
			_nContVioVmAndBrcAllGen = _paraMPJ.ThreadCommSumOneNumber(testAll.getNumContVio());
			_IdxContVioVmAndBrcAllGen = _paraMPJ.ThreadCommOneArray(testAll.getIdxContVio());
			_idxMapContToVmVioContGen = ThreadCommPartiIndexArray(testAll.getIdxMapContToVmVioCont());
			_idxMapContToBrcVioContGen = ThreadCommPartiIndexArray(testAll.getIdxMapContToBrcVioCont());
			_sumBrcVioExtPerContGen = _paraMPJ.ThreadCommOneArray(testAll.getSumBrcVioExt());
			_sumVmVioExtPerContGen = _paraMPJ.ThreadCommOneArray(testAll.getSumVmVioExt());
		}
	}
	
	/** Specific index array comm between threads. */
	private int[] ThreadCommPartiIndexArray(int[] idxArray)
	{
		int[] newIdxArray = _paraMPJ.ThreadCommOneArray(idxArray);
		if (_rank == 0)
		{
			int num = 0;
			for (int i=0; i<newIdxArray.length; i++)
				if (newIdxArray[i] != -1) newIdxArray[i] = num++;
		}
		return newIdxArray;
	}
	
	public void ThreadCommVm(VioResultAll testAll, String element)
	{
		int[] sizeV = testAll.getSizeV();          // sizeV[i] denotes how many buses have voltage violation issue in the i-th contingency that recorded
		int MarkElement = checkElemMark(element);
		if (MarkElement == 1)
		{
			_nVioVmAllTransm = _paraMPJ.ThreadCommSumOneNumber(testAll.getNumVioVm());
			_sizeVAllTransm = _paraMPJ.ThreadCommOneArray(sizeV);
            _totalVmVioNumAllTransm = _paraMPJ.ThreadCommSumOneNumber(testAll.getTotalVmVioNum());
            _IdxContVioVmAllTransm = _paraMPJ.ThreadCommOneArray(testAll.getIdxVioVm());
            _AllVmDiffTransm = _paraMPJ.ThreadCommTwoArray(testAll.getVmVioDiff(), sizeV);
            _AllVmTransm = _paraMPJ.ThreadCommTwoArray(testAll.getVmVio(), sizeV);
            _AllVaTransm = _paraMPJ.ThreadCommTwoArray(testAll.getVaVio(), sizeV);
            _IdxVAllTransm = _paraMPJ.ThreadCommTwoArray(testAll.getIdxV(), sizeV);
		}
		else if (MarkElement == 2)
		{
			_nVioVmAllGen = _paraMPJ.ThreadCommSumOneNumber(testAll.getNumVioVm());
			_sizeVAllGen = _paraMPJ.ThreadCommOneArray(sizeV);
            _totalVmVioNumAllGen = _paraMPJ.ThreadCommSumOneNumber(testAll.getTotalVmVioNum());
            _IdxContVioVmAllGen = _paraMPJ.ThreadCommOneArray(testAll.getIdxVioVm());
            _AllVmDiffGen = _paraMPJ.ThreadCommTwoArray(testAll.getVmVioDiff(), sizeV);
            _AllVmGen = _paraMPJ.ThreadCommTwoArray(testAll.getVmVio(), sizeV);
            _AllVaGen = _paraMPJ.ThreadCommTwoArray(testAll.getVaVio(), sizeV);
            _IdxVAllGen = _paraMPJ.ThreadCommTwoArray(testAll.getIdxV(), sizeV);
		}
	}

	public void ThreadCommBrc(VioResultAll testAll, String element)
	{
		int[] sizeBrc = testAll.getSizeBrc();        
		int MarkElement = checkElemMark(element);
		if (MarkElement == 1)
		{
			_nVioBrcAllTransm = _paraMPJ.ThreadCommSumOneNumber(testAll.getNumVioBrc());
			_sizeBrcAllTransm = _paraMPJ.ThreadCommOneArray(sizeBrc);
			_IdxContVioBrcAllTransm = _paraMPJ.ThreadCommOneArray(testAll.getIdxVioBrc());
			_totalBrcVioNumAllTransm = _paraMPJ.ThreadCommSumOneNumber(testAll.getTotalBrcVioNum());
			
			_AllBrcDiffTransm = _paraMPJ.ThreadCommTwoArray(testAll.getBrcVioDiff(), sizeBrc);
			_IdxBrcAllTransm = _paraMPJ.ThreadCommTwoArray(testAll.getIdxBrc(), sizeBrc);
			_BrcVioPfrmAllTransm = _paraMPJ.ThreadCommTwoArray(testAll.getBrcVioPfrm(), sizeBrc);
			_BrcVioPtoAllTransm = _paraMPJ.ThreadCommTwoArray(testAll.getBrcVioPto(), sizeBrc);
			_BrcVioQfrmAllTransm = _paraMPJ.ThreadCommTwoArray(testAll.getBrcVioQfrm(), sizeBrc);
			_BrcVioQtoAllTransm = _paraMPJ.ThreadCommTwoArray(testAll.getBrcVioQto(), sizeBrc);
			
			_maxBrcSdiffPerContTransm = _paraMPJ.ThreadCommOneArray(testAll.getMaxBrcDiffEachCont());
			_maxBrcVioPerctPerContTransm = _paraMPJ.ThreadCommOneArray(testAll.getMaxBrcDiffPerctEachCont());
    		_IdxMaxBrcSdiffPerContTransm = _paraMPJ.ThreadCommOneArray(testAll.getIdxMaxBrcDiffEachCont());
		}
		else if (MarkElement == 2)
		{
			_nVioBrcAllGen = _paraMPJ.ThreadCommSumOneNumber(testAll.getNumVioBrc());
			_sizeBrcAllGen = _paraMPJ.ThreadCommOneArray(sizeBrc);
			_IdxContVioBrcAllGen = _paraMPJ.ThreadCommOneArray(testAll.getIdxVioBrc());
			_totalBrcVioNumAllGen = _paraMPJ.ThreadCommSumOneNumber(testAll.getTotalBrcVioNum());
			
			_AllBrcDiffGen = _paraMPJ.ThreadCommTwoArray(testAll.getBrcVioDiff(), sizeBrc);
			_IdxBrcAllGen = _paraMPJ.ThreadCommTwoArray(testAll.getIdxBrc(), sizeBrc);
			_BrcVioPfrmAllGen = _paraMPJ.ThreadCommTwoArray(testAll.getBrcVioPfrm(), sizeBrc);
			_BrcVioPtoAllGen = _paraMPJ.ThreadCommTwoArray(testAll.getBrcVioPto(), sizeBrc);
			_BrcVioQfrmAllGen = _paraMPJ.ThreadCommTwoArray(testAll.getBrcVioQfrm(), sizeBrc);
			_BrcVioQtoAllGen = _paraMPJ.ThreadCommTwoArray(testAll.getBrcVioQto(), sizeBrc);
			
			_maxBrcSdiffPerContGen = _paraMPJ.ThreadCommOneArray(testAll.getMaxBrcDiffEachCont());
			_maxBrcVioPerctPerContGen = _paraMPJ.ThreadCommOneArray(testAll.getMaxBrcDiffPerctEachCont());
    		_IdxMaxBrcSdiffPerContGen = _paraMPJ.ThreadCommOneArray(testAll.getIdxMaxBrcDiffEachCont());
		}
	}
		
	/** Output violation information for base case 
	 * @Override @throws IOException */
	public void outputVioInfoBaseCase() throws PsseModelException, IOException
	{
		//_pf.outputResults();
		String rootDr = getPath();
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
	           		    PrintStream outFile = new PrintStream (resultFile, true);
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
		    		} catch (IOException e) {
	    		    	System.out.println();
	    		    	System.out.println("Cannot write Vm Violation for base case info to file" + e);
	    		    	e.printStackTrace();
		    		}
		    	} else System.out.println("There is no voltage violations in the base case.");
		    	
		    	if (_MarkvioBrcBase)
		    	{
		    		try
		    		{
	          			File BrcVioBase = new File(rootDr+"Brc_Violations_Base.txt");
	          			deleteFile(BrcVioBase);
	           		    OutputStream resultFile = new FileOutputStream(rootDr+"Brc_Violations_Base.txt", true);
	           		    PrintStream outFile = new PrintStream (resultFile, true);
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
		    	} else {
		    		if (_showRunInfo == true) System.out.println("There is no branch thermal violations in the base case.");
		    	}
		    } else {
		    	if (_showRunInfo == true) System.out.println("There is no violations in the base case.");
		    }
		} else {
			System.out.println("The power flow cannot converge for the base case.");
  			File NotConvBase = new File(rootDr+"Not_Converge_Base.txt");
  			deleteFile(NotConvBase);
   		    OutputStream resultFile = new FileOutputStream(rootDr+"Not_Converge_Base.txt", true);
   		    PrintStream outFile = new PrintStream (resultFile, true);	
   		    
   		    // For now, nothing is output to this file.
   		    
      		resultFile.close();
      		outFile.close();
		}
	}
		
	
	public void outputGeneralInfoTmp(float[] times)
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
	   			System.out.println("Fail to output CaGeneral Info to a file" + e);
	   			e.printStackTrace();
	   		}
		}

		// output the voltage violation information.
		try
		{
  			//File CaGeneralInfo = new File(rootDr+"CaGeneralInfo.txt");
   		    OutputStream resultFile = new FileOutputStream(rootDr+"CaGeneralInfo.txt", true);
   		    PrintStream outFile = new PrintStream (resultFile);
   		    
			int[]aa = { _nproc,
					    (_numTestedNm1Transm + _numTestedNm1Gen),       (_numNotConvTransm + _numNotConvGen), 
					    (_numContPVtoPQTransm + _numContPVtoPQGen),
					    (_nContVioVmAndBrcAllTransm + _nContVioVmAndBrcAllGen),
					    (_nVioBrcAllTransm + _nVioBrcAllGen),
					    (_nVioVmAllTransm + _nVioVmAllGen),             (_nVioVmLowAllTransm + _nVioVmLowAllGen),
					    (_nVioVmHighAllTransm + _nVioVmHighAllGen),     (_totalBrcVioNumAllTransm + _totalBrcVioNumAllGen),
					    (_numVmVioAllTransm + _numVmVioAllGen),         (_numVmLowVioAllTransm + _numVmLowVioAllGen),
					    (_numVmHighVioAllTransm + _numVmHighVioAllGen)  };
			
			float maxA = Math.max(_maxBrcVioPerctAllTransm, _maxBrcVioPerctAllGen);
			if (maxA > 0) maxA = (maxA - 1) * 100;
			float maxB = Math.max(_maxBrcSdiffAllTransm, _maxBrcSdiffAllGen)*100;
			
//			float maxB = Math.max(_maxBrcSdiffAllTransm, _maxBrcSdiffAllGen);
//			int index = _idxMaxBrcSdiffAllGen;
//			if (maxB == _maxBrcSdiffAllTransm) index = _idxMaxBrcSdiffAllTransm;
//			maxB = maxB * 100;
//			float maxA = 0;
//			if (maxB > 0) maxA = 100 * maxB / _RateC[index];

			float maxC;
			if (Math.abs(_maxVmdiffAllTransm) > Math.abs(_maxVmdiffAllGen)) maxC = _maxVmdiffAllTransm;
			else maxC = _maxVmdiffAllGen;
			if (maxC > 0) maxC = _Vmax + maxC;
			else maxC = _Vmin + maxC;
			float maxD = _Vmin + Math.min(_maxVmLowdiffAllTransm, _maxVmLowdiffAllGen);
			float maxE = _Vmax + Math.max(_maxVmHighdiffAllTransm, _maxVmHighdiffAllGen);
			
   		    float[] tmp = { maxB, maxC, maxD, maxE,
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
   			if (_showRunInfo == true)
   			{
   	   			System.out.println();
   	   			System.out.println("Output CaGeneral Info to a file successfully.");   				
   			}
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
   		    
			int[]aa = { _nproc,
					    (_numTestedNm1Gen),       (_numNotConvGen), 
					    (_numContPVtoPQGen),     
					    (_nContVioVmAndBrcAllGen),
					    ( _nVioBrcAllGen),
					    ( _nVioVmAllGen),             (_nVioVmLowAllGen),
					    (_nVioVmHighAllGen),     ( _totalBrcVioNumAllGen),
					    ( _numVmVioAllGen),         (_numVmLowVioAllGen),
					    ( _numVmHighVioAllGen)  };
			
			float maxA = _maxBrcVioPerctAllGen;
			if (maxA > 0) maxA = (maxA - 1) *100;
			float maxB = _maxBrcSdiffAllGen * 100;
			float maxC = _maxVmdiffAllGen;
			if (maxC > 0) maxC = _Vmax + maxC;
			else maxC = _Vmin + maxC;
			float maxD = _Vmin + _maxVmLowdiffAllGen;
			float maxE = _Vmax + _maxVmHighdiffAllGen;
			
   		    float[] tmp = { maxB, maxC, maxD, maxE,
   		    		          ( _sumAllSdiffGen * 100), ( _sumAllVmVioGen),
   		    		          ( _sumAllVmLowVioGen), ( _sumAllVmHighVioGen)  };
   		    
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
   			if (_showRunInfo == true)
   			{
   	   			System.out.println();
   	   			System.out.println("Output CaGeneralInfo_Gen to a file successfully.");
   			}
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
   		    
			int[]aa = { _nproc,
					    (_numTestedNm1Transm ),       (_numNotConvTransm), 
					    (_numContPVtoPQTransm ),
					    (_nContVioVmAndBrcAllTransm),
					    (_nVioBrcAllTransm ),
					    (_nVioVmAllTransm ),             (_nVioVmLowAllTransm),
					    (_nVioVmHighAllTransm ),     (_totalBrcVioNumAllTransm),
					    (_numVmVioAllTransm ),         (_numVmLowVioAllTransm ),
					    (_numVmHighVioAllTransm )  };
			
			float maxA = _maxBrcVioPerctAllTransm;
			if (maxA > 0) maxA = (maxA - 1) *100;
			float maxB = _maxBrcSdiffAllTransm * 100;
			float maxC = _maxVmdiffAllTransm;
			if (maxC > 0) maxC = _Vmax + maxC;
			else maxC = _Vmin + maxC;
			float maxD = _Vmin + _maxVmLowdiffAllTransm;
			float maxE = _Vmax + _maxVmHighdiffAllTransm;
			
   		    float[] tmp = { maxB, maxC, maxD, maxE,
   		    		           (_sumAllSdiffTransm * 100), (_sumAllVmVioTransm),
   		    		           (_sumAllVmLowVioTransm ), (_sumAllVmHighVioTransm)  };
   		    
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
   			if (_showRunInfo == true)
   			{
   	   			System.out.println();
   	   			System.out.println("Output CaGeneralInfo_Transm to a file successfully.");
   			}
   		} catch (IOException e) {
   			System.out.println();
   			System.out.println("Fail to output CaGeneralInfo_Transm to a file" + e);
   			e.printStackTrace();
   		}
	}
	
	

	
	public static void main(String[] args) throws Exception
	{   
		System.out.println("   Inputs: " + args[0] + " "+ args[1] + " ");
		MPI.Init(args);
		long t_Start = System.nanoTime();

	    if (MPI.Initialized() == false)
	    	System.out.println("  Error Happens when starting MPI program.");
	    int nproc = MPI.COMM_WORLD.Size();
        int rank = MPI.COMM_WORLD.Rank();
               
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
		if (rank == 0) AuxFileXL.createFolder(ParamIO.getCAPath());
		ParamIO.setCAFolder(ParamIO.getCAFolder()+"CaPara_"+model.getFileName()+"/");
		if (rank == 0) AuxFileXL.createFolder(ParamIO.getCAPath());
		//MPI.COMM_WORLD.Barrier();  // may not be needed as the CA takes quite a while before creating files in the just created folder.
		
		ContingencyAnalysis ca = new ContingencyAnalysisParallel(model, vstart, nproc, rank);
		ca.setGeneInfoPath(ParamIO.getOutPath() + ParamIO.getCAFolder());
		ca.setURI(uri);
		ca.setShowRunInfo(true);
		
	    ca.runGenContingencyAnalysis();
	    ca.runTransmContingencyAnalysis();

		if(rank == 0)
		{
			long t_EndCA = System.nanoTime();
			System.out.println("   Total time : " + (t_EndCA - t_Start)/1e9f);
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
			System.out.println("   Total time including file_output : " + (System.nanoTime() - t_Start)/1e9f);
		}

	    MPI.Finalize();
	}
}
