package com.rtca_cts.ausData;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Test;

import com.powerdata.openpa.psse.ACBranch;
import com.powerdata.openpa.psse.ACBranchList;
import com.powerdata.openpa.psse.PsseModel;
import com.powerdata.openpa.psse.PsseModelException;
import com.powerdata.openpa.tools.LinkNet;
import com.utilxl.array.AuxArrayXL;
import com.utilxl.iofiles.OutputArrays;


/**
 * By utilizing LinkNet class from openpa
 * 
 * @author Xingpeng.Li (xplipower@gmail.com)
 *
 */
public class RadialBranches {
	
	LinkNet _net;
	PsseModel _model;
	int _nbr;
	int _nOrigIslands;
	boolean _showRunInfo = false;
	
	int[] _idxInServiceBrc;          // indices of all the in-service branches.
	ArrayList<Integer> _idxInServiceBrcHunter = null;  // indices of all the in-service branches.
	
	boolean[] _isBrcRadial = null;   // If true, then, it is radial and in-service. Note it is false for branches that are not in-service.
	int[] _ndxRadialBrc = null;      // indices of all the radial branches.

//	/** Any branches status changes, clearData() should be called. */
//	public void clearData() {_isBrcRadial = null; _ndxRadialBrc = null;}
//	public void eliminateBranch(int idxBrc) {_net.eliminateBranch(idxBrc, true);}
//	public void addBranchBack(int idxBrc) {_net.eliminateBranch(idxBrc, false);}

	/**   */
	public RadialBranches(PsseModel model) throws PsseModelException
	{
		_model = model;
		initial();
	}
	
	void initial() throws PsseModelException
	{
		_net = new LinkNet();
		ACBranchList branches = _model.getBranches();
		int nbus = _model.getBuses().size();
		_nbr = branches.size();
		_net.ensureCapacity(nbus-1, _nbr);

		_idxInServiceBrc = new int[_nbr];
		int ndx = 0;
		for(int i=0; i < _nbr; ++i)
		{
			ACBranch br = branches.get(i);
			if (br.isInSvc())
			{
				int fbus = br.getFromBus().getIndex();
				int tbus = br.getToBus().getIndex();
				_net.addBranch(fbus, tbus);
				_idxInServiceBrc[ndx++] = i;
			}
		}
		_idxInServiceBrc = Arrays.copyOf(_idxInServiceBrc, ndx);		
		_nOrigIslands = _net.findGroups().length;
		if (_showRunInfo == true) System.out.println("# of islands in original network: "+_nOrigIslands);
	}
	
	void calculateAllRadialBrc()
	{
		if (_nOrigIslands == 1)
		{
			int ndx = _idxInServiceBrc.length;
			_ndxRadialBrc = new int[ndx];
			_isBrcRadial = new boolean[_nbr];
			int num = 0;
			for (int i=0; i<ndx; i++)
			{
				_net.eliminateBranch(i, true);
				if (_net.findGroups().length != 1)
				{
					int idxBrc = _idxInServiceBrc[i];
					_ndxRadialBrc[num++] = idxBrc; 
					_isBrcRadial[idxBrc] = true;
				}
				_net.eliminateBranch(i, false);
			}
			_ndxRadialBrc = Arrays.copyOf(_ndxRadialBrc, num);
			if (_showRunInfo == true) System.out.println("# of radial branch: "+num);
		}
		else /*if (_showRunInfo == true)*/ System.err.println("The original network is not even connected.");		
	}
	
	/** If set true, then Pf shows detail iteration info */
	public void setShowRunInfo(boolean mark) { _showRunInfo = mark;}

	/** Get indices of all the radial branches. */
	public int[] getNdxRadialBrc()
	{
		if (_ndxRadialBrc == null) calculateAllRadialBrc();
		return _ndxRadialBrc;
	}
	
	public boolean[] getIsBrcRadial()
	{
		if (_isBrcRadial == null) calculateAllRadialBrc();
		return _isBrcRadial;
	}
	
	@Test
	public boolean isBrcRadialBrc(int idxBrc) throws PsseModelException
	{
		if (_isBrcRadial != null) return _isBrcRadial[idxBrc];
		if (_idxInServiceBrcHunter == null) _idxInServiceBrcHunter = AuxArrayXL.toArrayList(_idxInServiceBrc);
		int idxOfInterest = _idxInServiceBrcHunter.indexOf(idxBrc);
//		if (idxOfInterest == -1) System.err.println("Brc : "+idxBrc+" is not in-service; it should not be checked.");
//		System.out.println("idxBrc: "+idxBrc);
		if (idxOfInterest == -1) System.exit(0);
		assertTrue(idxOfInterest != -1);
		boolean isRadial = false;
		_net.eliminateBranch(idxOfInterest, true);
		if (_net.findGroups().length != 1) isRadial = true;
		_net.eliminateBranch(idxOfInterest, false);
		return isRadial;
	}
	
	
	/** added on Jul.7, 2019 */
	public static void main(String[] args) throws Exception {
		String uri = null;
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
			}
		}
		if (uri == null)
		{
			System.err.format("Usage: -uri model_uri [-voltage flat|realtime] [ -minxmag smallest reactance magnitude (default to 0.001) ] [ -debug dir ] [ -results path_to_results]");
			System.exit(1);
		}
		
		PsseModel model = PsseModel.Open(uri);
		int[] idxRadialLines = model.getRadialBrcData().getNdxRadialBrc().clone();
		OutputArrays.outputArray(AuxArrayXL.allElemsPlusANum(idxRadialLines, 1));
		System.out.println("Program ends here ...");
	}
	
	
}
