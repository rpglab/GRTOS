package com.rtca_cts.contingencyanalysis;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;


/**
 * Save violations information for multiple contingencies that cause violation.
 * 
 * 
 * Initialized in 2014.
 * 
 * @author Xingpeng.Li, xplipower@gmail.com
 */
public class VioResultAll 
{
	// if enable Gen Var Limit
	int _numContPVtoPQ;      // number of contingencies that have PQ buses that are switched from original PV bus. 
	int[] _idxContPVtoPQ;    // index of contingency that have PQ buses that are switched from original PV bus. 
	int[] _numPVtoPQEachCont;        // number of PV buses that are switched to PQ buses for each contingency,
	int[] _numOrigPVEachCont;     // number of original PV buses.
	
	//int temp;
	int _nVio;          // number of contingencies that have violations
	float[] _sumVmVioExt;
	float[] _sumBrcVioExt;  // its length is : _nVio.
    int[] _size;      // number of violations per contingencies.
	int[] _idxCont;     //  indices of contingency that cause violation
    int[] _idxMapContToVmVioCont;  // the element corresponds to the index of _idxVioVm.
    int[] _idxMapContToBrcVioCont;
    
    int _nVioVm;      // the number of contingency that cause voltage violation, it is also the real size of sizeV, _idxVioVm;
                       //  it is also the number of row of IdxV and VmVioDiff
	int[] _idxVioVm;   // index of which branch is fault
	int[] _idxVioVmMapContExt; // the element corresponds to the index of _idxCont.
	int[][] _idxV;
	float[][] _VmVioDiff;  // _VioVmDiff denotes the difference between the N-1 practical voltages and its bounds 
	float[][] _VmVio;  // voltage magnitude, only for buses that cause voltage violation
	float[][] _VaVio;  // voltage angle, only for buses that cause voltage violation
	int[] _sizeV;     // number of bus voltage violations per contingencies.
	int[] _MaxVmDiffEachInd;     // index of bus that cause the maximum bus voltage violation
	float[] _MaxVmDiffAbsEach;  // the max abs voltage violation for each contingency
	float _VmDiffAbsMaxAll;  //
	int _VmDiffAbsMaxAllInd;    // virtual index of _VmDiffAbsMaxAll
	//boolean[] _VioVoltage;
	//float[] _VmDiffAbsMax;
	//float[][]	_Vm, _Va, _VmDiffAbs;  // _VmDiff denotes the difference between the N-1 buses voltages and its bounds;	
	//float _VmMax;
	//float _VmMin;
	
	int _nVioBrc;      // number of contingency that cause line thermal violation.
	int[] _idxVioBrc;   // index of which branch is fault	
	int[] _idxVioBrcMapContExt; // the element corresponds to the index of _idxCont.
	int[][] _idxBrc;     // the indices of branches that have thermal violation when a contingency occurs
	float[][] _VioBrcDiff;
	int[] _sizeBrc;     // number of branches that have thermal violation per contingencies.
	
	float[] _MaxBrcDiffEach;  // its length is : _nVioBrc.
	int[] _sMaxBrcDiffEachInd; //  index of branch that have the maximum violation 
	float _PfDiffMaxAll;
	int _PfDiffMaxAllInd;    // virtual index of _PfDiffMaxAll

	float[] _MaxBrcDiffPerctEach;
	int[] _sMaxBrcDiffPerctEachInd;
	float _PfDiffPerctMaxAll;
	int _PfDiffPerctMaxAllInd;

	float[][] _pfrm, _pto, _qfrm, _qto;
	
	
	public VioResultAll(int numCont)
	{
		_idxCont = new int[numCont];
		_size = new int[numCont];
        _sumVmVioExt = new float[numCont];
        _sumBrcVioExt = new float[numCont];
		_idxMapContToVmVioCont = new int[numCont];
		_idxMapContToBrcVioCont = new int[numCont];

		_idxContPVtoPQ = new int[numCont];
		_numPVtoPQEachCont = new int[numCont];
		_numOrigPVEachCont = new int[numCont];

		_idxVioVm = new int[numCont];
		_idxVioVmMapContExt = new int[numCont];
		_idxV = new int[numCont][];
		_VmVioDiff = new float[numCont][];
		_VmVio = new float[numCont][];
		_VaVio = new float[numCont][];
		_sizeV = new int[numCont];
		_MaxVmDiffEachInd = new int[numCont];
		_MaxVmDiffAbsEach = new float[numCont];

		_idxVioBrc = new int[numCont];
		_idxVioBrcMapContExt = new int[numCont];
		_idxBrc = new int[numCont][];        
		_VioBrcDiff = new float[numCont][];
        _sizeBrc = new int[numCont];

        _MaxBrcDiffEach = new float[numCont];
        _sMaxBrcDiffEachInd = new int[numCont];
        _MaxBrcDiffPerctEach = new float[numCont];
        _sMaxBrcDiffPerctEachInd = new int[numCont];

        _pfrm = new float[numCont][];
        _pto = new float[numCont][];
        _qfrm = new float[numCont][];
        _qto = new float[numCont][];

        Arrays.fill(_idxCont, -1); // remove this line when the program is finalized.
        Arrays.fill(_idxContPVtoPQ, -1); // remove this line when the program is finalized.
        Arrays.fill(_numPVtoPQEachCont, -1); // remove this line when the program is finalized.
        Arrays.fill(_idxMapContToVmVioCont, -1); // remove this line when the program is finalized.
        Arrays.fill(_idxMapContToBrcVioCont, -1); // remove this line when the program is finalized.
        
		Arrays.fill(_idxVioVm, -1);   // remove this line when the program is finalized.
		Arrays.fill(_idxVioVmMapContExt, -1); // remove this line when the program is finalized.
		Arrays.fill(_sizeV, -1); // remove this line when the program is finalized.
		Arrays.fill(_MaxVmDiffEachInd, -1);// remove this line when the program is finalized.
		
		Arrays.fill(_idxVioBrc, -1); // remove this line when the program is finalized
		Arrays.fill(_idxVioBrcMapContExt, -1); // remove this line when the program is finalized.
		Arrays.fill(_sizeBrc, -1);// remove this line when the program is finalized.
		Arrays.fill(_sMaxBrcDiffEachInd, -1);		// remove this line when the program is finalized.
	}

	
	public void updateVioData(int i, int sizeV, int sizeBrc, float sumBrcVio, float sumVmVio)
	{
		_sumBrcVioExt[_nVio] = sumBrcVio;
		_sumVmVioExt[_nVio] = sumVmVio;
		_size[_nVio] = sizeV + sizeBrc;
		_idxCont[_nVio++] = i;
	}
	
	/** Must be used after calling method updateVioData(). */
	public void updateVmVioData(int i, int[] idxV, float[] VmDiff, int sizeV, float VmDiffMaxI, int MaxVmDiffInd)
	{
		if (VmDiffMaxI != 0)
		{
			_idxMapContToVmVioCont[_nVio-1] = _nVioVm;
			_idxVioVm[_nVioVm] = i;
			_idxVioVmMapContExt[_nVioVm] = _nVio-1;
			_idxV[_nVioVm] = idxV;
			_VmVioDiff[_nVioVm] = VmDiff;
			_sizeV[_nVioVm] = sizeV;
			_MaxVmDiffAbsEach[_nVioVm] = VmDiffMaxI;  // VmDiffMaxI could be positive, also could be negative
			_MaxVmDiffEachInd[_nVioVm] = MaxVmDiffInd;
			if (Math.abs(VmDiffMaxI) > Math.abs(_VmDiffAbsMaxAll))
			{
				_VmDiffAbsMaxAll = VmDiffMaxI;
				_VmDiffAbsMaxAllInd = _nVioVm;
			}
			_nVioVm++;
		}
		else {System.err.println("Something wrong happens here");}
	}
	
	/** Must be used after calling method updateVioData(). */
	public void updateVmVioData(int i, int[] idxV, float[] VmDiff, int sizeV, float VmDiffMaxI, int MaxVmDiffInd, float[] Vm, float[] Va)
	{
		if (VmDiffMaxI != 0)
		{
			_VmVio[_nVioVm] = Vm;
			_VaVio[_nVioVm] = Va;
		}
		else {System.err.println("Something wrong happens here");}
		updateVmVioData(i, idxV, VmDiff, sizeV, VmDiffMaxI, MaxVmDiffInd); //This line has to be put in the end.
	}
	
	/** Must be used after calling method updateVioData(). */
	public void updateBrcVioData(int i, int[] idxBrc, float[] BrcDiff, int sizeBrc, float PfDiffMaxI, int MaxBrcDiffInd)
	{
		if (PfDiffMaxI != 0)   // PfDiffMaxI is the maximum violation of the i-th contingency.  
		{
			_idxMapContToBrcVioCont[_nVio-1] = _nVioBrc;
			_idxVioBrc[_nVioBrc] = i;
			_idxVioBrcMapContExt[_nVioBrc] = _nVio-1;
			_idxBrc[_nVioBrc] = idxBrc;
			_VioBrcDiff[_nVioBrc] = BrcDiff;
			_sizeBrc[_nVioBrc] = sizeBrc;
			_MaxBrcDiffEach[_nVioBrc] = PfDiffMaxI;
			_sMaxBrcDiffEachInd[_nVioBrc] = MaxBrcDiffInd;
			
			assertTrue(PfDiffMaxI > 0);
			//if (Math.abs(PfDiffMaxI) > _PfDiffMaxAll)
			if (PfDiffMaxI > _PfDiffMaxAll)
			{
				_PfDiffMaxAll = PfDiffMaxI;
				_PfDiffMaxAllInd = _nVioBrc;
			}
			_nVioBrc++;
		}
		else {System.err.println("Something wrong happens here in updateBrcVioData() in BrcCapVioListALL.java");}
	}
	
	/** Must be used after calling method updateVioData(). */
	public void updateBrcVioData(int i, int[] idxBrc, float[] BrcDiff, int sizeBrc, float PfDiffMaxI, int MaxBrcDiffInd,
			 float[] pfrm, float[] pto, float[] qfrm, float[] qto)
	{
		if (PfDiffMaxI != 0)   // PfDiffMaxI is the maximum violation of the i-th contingency.  
		{
			_pfrm[_nVioBrc] = pfrm;
			_pto[_nVioBrc] = pto;
			_qfrm[_nVioBrc] = qfrm;
			_qto[_nVioBrc] = qto;
		}
		else {System.err.println("Something wrong happens here in updateBrcVioData() in BrcCapVioListALL.java");}
		updateBrcVioData(i, idxBrc, BrcDiff, sizeBrc, PfDiffMaxI, MaxBrcDiffInd);           //This line has to be put in the end.
	}
	
	/** Attention: if called, this methods must be called before updateBrcVioData(). */ 
	public void updateBrcVioInPerctData(float PfDiffMaxInPerct, int PfDiffMaxInPerctInd)
	{
		_MaxBrcDiffPerctEach[_nVioBrc] = PfDiffMaxInPerct;
		_sMaxBrcDiffPerctEachInd[_nVioBrc] = PfDiffMaxInPerctInd;
		if (PfDiffMaxInPerct > _PfDiffPerctMaxAll)
		{
			_PfDiffPerctMaxAll = PfDiffMaxInPerct;
			_PfDiffPerctMaxAllInd = _nVioBrc;
		}
	}
	
	public void updateInfoPVtoPQ(int i, int numPVtoPQ, int numOrigOV)
	{
		_idxContPVtoPQ[_numContPVtoPQ] = i;
		_numPVtoPQEachCont[_numContPVtoPQ] = numPVtoPQ;
		_numOrigPVEachCont[_numContPVtoPQ] = numOrigOV;
		_numContPVtoPQ++;
	}
	
	//Get methods
	public int getNumContPVtoPQ() {return _numContPVtoPQ;}
	public int[] getIdxContPVtoPQ()
	{
		_idxContPVtoPQ = shrinkArraySize(_numContPVtoPQ, _idxContPVtoPQ);
		return _idxContPVtoPQ;
	}
	public int[] getNumPVtoPQEachCont()
	{
		_numPVtoPQEachCont = shrinkArraySize(_numContPVtoPQ, _numPVtoPQEachCont);
		return _numPVtoPQEachCont;
	}
	public int[] getNumOrigPVEachCont()
	{
		_numOrigPVEachCont = shrinkArraySize(_numContPVtoPQ, _numOrigPVEachCont);
		return _numOrigPVEachCont;
	}
	
	
	public int getNumContVio() {return _nVio;}
	public int[] getIdxContVio()
	{
		_idxCont = shrinkArraySize(_nVio, _idxCont);		
		return _idxCont;
	}
	public float[] getSumBrcVioExt()
	{
		_sumBrcVioExt = shrinkArraySize(_nVio, _sumBrcVioExt);
		return _sumBrcVioExt;
	}
	public float[] getSumVmVioExt() 
	{
		_sumVmVioExt = shrinkArraySize(_nVio, _sumVmVioExt);
		return _sumVmVioExt;
	}
	public int[] getIdxMapContToVmVioCont()
	{
		_idxMapContToVmVioCont = shrinkArraySize(_nVio, _idxMapContToVmVioCont);
		return _idxMapContToVmVioCont;
	}
	public int[] getIdxMapContToBrcVioCont()
	{
		_idxMapContToBrcVioCont = shrinkArraySize(_nVio, _idxMapContToBrcVioCont);
		return _idxMapContToBrcVioCont;
	}

	
	public int getNumVioVm() { return _nVioVm;}
    public int[] getIdxVioVm()
    {
    	_idxVioVm = shrinkArraySize(_nVioVm, _idxVioVm);
    	return _idxVioVm;
    }
    public int[] getIdxVioVmMapContExt()
    {
    	_idxVioVmMapContExt = shrinkArraySize(_nVioVm, _idxVioVmMapContExt);
    	return _idxVioVmMapContExt;
    }
	public int[] getSizeV()
	{
		_sizeV = shrinkArraySize(_nVioVm, _sizeV);
		return _sizeV;
	}
	public int[][] getIdxV() 
	{
		_idxV = shrinkArraySize(_nVioVm, _idxV);
		return _idxV;
	}
	public float[][] getVmVioDiff()
	{
		_VmVioDiff = shrinkArraySize(_nVioVm, _VmVioDiff);
		return _VmVioDiff;
	}
	public float[][] getVmVio() 
	{
		_VmVio = shrinkArraySize(_nVioVm, _VmVio);
		return _VmVio;
	}
	public float[][] getVaVio() 
	{
		_VaVio = shrinkArraySize(_nVioVm, _VaVio);
		return _VaVio;
	}
	public int getTotalVmVioNum()
	{
		int totalVmVioNum = 0;
		for(int i=0; i<_nVioVm; i++)
			totalVmVioNum += _sizeV[i];
		return totalVmVioNum;
	}
	
	
	public int getNumVioBrc() { return _nVioBrc;}
    public int[] getIdxVioBrc()
    {
    	_idxVioBrc = shrinkArraySize(_nVioBrc, _idxVioBrc);
    	return _idxVioBrc;
    }
    public int[] getIdxVioBrcMapContExt()
    {
    	_idxVioBrcMapContExt = shrinkArraySize(_nVioBrc, _idxVioBrcMapContExt);
    	return _idxVioBrcMapContExt;
    }
	public int[] getSizeBrc()
	{
		_sizeBrc = shrinkArraySize(_nVioBrc, _sizeBrc);
		return _sizeBrc;
	}
	public int[][] getIdxBrc() 
	{
		_idxBrc = shrinkArraySize(_nVioBrc, _idxBrc);
		return _idxBrc;
	}
	public float[][] getBrcVioDiff() 
	{
		_VioBrcDiff = shrinkArraySize(_nVioBrc, _VioBrcDiff);
		return _VioBrcDiff;
	}
	public float[][] getBrcVioPfrm() 
	{
		_pfrm = shrinkArraySize(_nVioBrc, _pfrm);
		return _pfrm; 
	}
	public float[][] getBrcVioPto() 
	{
		_pto = shrinkArraySize(_nVioBrc, _pto);
		return _pto;
	}
	public float[][] getBrcVioQfrm() 
	{
		_qfrm = shrinkArraySize(_nVioBrc, _qfrm);
		return _qfrm;
	}
	public float[][] getBrcVioQto() 
	{
		_qto = shrinkArraySize(_nVioBrc, _qto);
		return _qto;
	}
	public float[] getMaxBrcDiffEachCont()
	{
		_MaxBrcDiffEach = shrinkArraySize(_nVioBrc,_MaxBrcDiffEach);
		return _MaxBrcDiffEach;
	}
	public int[] getIdxMaxBrcDiffEachCont()
	{
		_sMaxBrcDiffEachInd = shrinkArraySize(_nVioBrc,_sMaxBrcDiffEachInd);
		return _sMaxBrcDiffEachInd;
	}

	public float[] getMaxBrcDiffPerctEachCont()
	{
		_MaxBrcDiffPerctEach = shrinkArraySize(_nVioBrc,_MaxBrcDiffPerctEach);
		return _MaxBrcDiffPerctEach;
	}
	public int[] getIdxMaxBrcDiffPerctEachCont()
	{
		_sMaxBrcDiffPerctEachInd = shrinkArraySize(_nVioBrc,_sMaxBrcDiffPerctEachInd);
		return _sMaxBrcDiffPerctEachInd;
	}

	
	public int getTotalBrcVioNum()
	{
		int totalBrcVioNum = 0;
		for(int i=0; i<_nVioBrc; i++)
			totalBrcVioNum += _sizeBrc[i];
		return totalBrcVioNum;
	}
	public int getTotalVioNum()
	{
		int totalVioNum = 0;
		for(int i=0; i<_nVioVm; i++)
			totalVioNum += _sizeV[i];
		for(int i=0; i<_nVioBrc; i++)
			totalVioNum += _sizeBrc[i];
		return totalVioNum;		
	}
	
	
	int[] shrinkArraySize(int size, int[] Array)
	{
		if (Array.length != size)
		{
			int[] newArray = Arrays.copyOf(Array, size);
			Array = newArray;
		}
		return Array;
	}
	float[] shrinkArraySize(int size, float[] Array)
	{
		if (Array.length != size)
		{
			float[] newArray = Arrays.copyOf(Array, size);
			Array = newArray;
		}
		return Array;
	}
	int[][] shrinkArraySize(int size, int[][] Array)
	{
		if (Array.length != size)
		{
			int[][] newArray = new int[size][];
	    	System.arraycopy(Array, 0, newArray, 0, size);
			Array = newArray;
		}
		return Array;
	}
	float[][] shrinkArraySize(int size, float[][] Array)
	{
		if (Array.length != size)
		{
			float[][] newArray = new float[size][];
	    	System.arraycopy(Array, 0, newArray, 0, size);
			Array = newArray;
		}
		return Array;
	}

	
}
