package com.powerdata.openpa.psse.powerflow;

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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import com.powerdata.openpa.psse.ACBranchList;
import com.powerdata.openpa.psse.Bus;
import com.powerdata.openpa.psse.BusList;
import com.powerdata.openpa.psse.BusTypeCode;
import com.powerdata.openpa.psse.Island;
import com.powerdata.openpa.psse.IslandList;
import com.powerdata.openpa.psse.OneTermDev;
import com.powerdata.openpa.psse.PsseModel;
import com.powerdata.openpa.psse.PsseModelException;
import com.powerdata.openpa.psse.TwoTermDCLineList;
import com.powerdata.openpa.psse.TwoTermDev;
import com.powerdata.openpa.tools.LinkNet;
import com.powerdata.openpa.tools.PAMath;
/**
 * Report mismatches for each bus 
 * 
 * @author chris@powerdata.com
 *
 */
public class MismatchReport
{
	PsseModel							_model;
	LinkNet								_brnet	= new LinkNet();
	LinkNet								_otnet	= new LinkNet();
	int									_nbus;
	Object[]		_otdevorder;
	Object[]		_brorder;
	int									_ngen, _nload, _nshunt, _nsvc, _nbr;
	float[]								_shq, _svcq, _pfrm, _pto,
			_qfrm, _qto, _vm, _va, _pmm, _qmm;

	//	PrintWriter _out;
	@SuppressWarnings("unchecked")
	public MismatchReport(PsseModel model) throws PsseModelException
	{
		_model = model;
		_nbus = model.getBuses().size();
		
		_otdevorder = new Object[] {
				_model.getGenerators(), _model.getLoads(), _model.getShunts(),
				_model.getSvcs() };
		
		_ngen = _model.getGenerators().size();
		_nload = _model.getLoads().size();
		_nshunt = _model.getShunts().size();

		int otsize = 0;
		for(Object l : _otdevorder)
			otsize += ((List<? extends OneTermDev>) l).size();
		
//		TwoTermDevList ttdevs = model.getTwoTermDevs();
		TwoTermDCLineList t2dclines = model.getTwoTermDCLines();
		ACBranchList acbranch = model.getBranches();
		_nbr = acbranch.size()+t2dclines.size();
		_brnet.ensureCapacity(_nbus+1, _nbr);
		_otnet.ensureCapacity(_nbus+1, otsize);
		_brorder = new Object[] { acbranch, t2dclines };
		for(Object olist : _brorder)
		{
			List<? extends TwoTermDev> list = (List<? extends TwoTermDev>) olist; 
			for(TwoTermDev dev : list)
			{
				int fndx = dev.getFromBus().getIndex();
				int tndx = dev.getToBus().getIndex();
				_brnet.addBranch(fndx, tndx);
			}
		}
		
		for (Object otlist : _otdevorder)
		{
			for (OneTermDev otd : (List<? extends OneTermDev>)otlist)
			{
				int bndx = (otd.isInSvc()) ? otd.getBus().getIndex() : _nbus;
				_otnet.addBranch(bndx, _nbus);
			}
		}
		
		_pfrm = new float[_nbr];
		_qfrm = new float[_nbr];
		_pto = new float[_nbr];
		_qto = new float[_nbr];
	}
	
	public void report(PrintWriter out) throws IOException, PsseModelException
	{
		out.println("BusID,BusName,Energized,Island,Type,VA,VM,Pmm,Qmm,MaxMM,DevID,DevName,Pdev,Qdev");
		for (int i=0; i < _nbus; ++i)
		{
			_report(out, i, _brnet.findBranches(i), _otnet.findBranches(i));
		}
	}

	public void report(File odir, String iter) throws IOException, PsseModelException
	{
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(
				new File(odir, "mismatch" + iter + ".csv"))));
		report(out);
		out.close();
	}
	
	public void report(File out) throws IOException, PsseModelException
	{
		PrintWriter fout = new PrintWriter(new BufferedWriter(new FileWriter(out)));
		report(fout);
		fout.close();
	}

	void _report(PrintWriter out, int i, int[] branches, int[] otdevs) throws PsseModelException
	{
		BusList buses = _model.getBuses();
		Bus b = buses.get(i);
//		if (!b.isEnergized()) return;
		//TODO:  should island really ever be able to be -1?
		if (branches.length == 0 && otdevs.length == 0) return;
		float mmm = Math.max(Math.abs(_pmm[i]), Math.abs(_qmm[i]));
		IslandList islands = _model.getIslands();
		
		Island island = islands.get(b.getIsland());
		
		String btmp = String.format(
				"\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%f,%f,%f,%f,%f,", 
				b.getObjectID(), b.getObjectName(),
				island.isEnergized() ? "true" : "false", island.getIndex(), 
				b.getBusType(), PAMath.rad2deg(_va[i]), _vm[i], 
				PAMath.pu2mw(_pmm[i]), PAMath.pu2mvar(_qmm[i]), mmm);
		for(int ttdev : branches)
		{
			TwoTermDev ttd = null;
			int t = ttdev;
			for(Object blist : _brorder)
			{
				@SuppressWarnings("unchecked")
				List<? extends TwoTermDev> list = (List<? extends TwoTermDev>) blist;
				if (t < list.size()) {ttd = list.get(t); break;}
				t -= list.size();
			}
			
			int fbus = ttd.getFromBus().getIndex();
			float pp, qq;
			if (fbus == i)
			{
				pp = _pfrm[ttdev];
				qq = _qfrm[ttdev];
			}
			else
			{
				pp = _pto[ttdev];
				qq = _qto[ttdev];
			}
			out.print(btmp);
			out.format("\"%s\",\"%s\",%f,%f\n", ttd.getObjectID(),
					ttd.getObjectName(), PAMath.pu2mw(pp), PAMath.pu2mvar(qq));

		}
		
		for(int otdev : otdevs)
		{
			out.print(btmp);
			printDevice(out, otdev);
		}

	}

	void printDevice(PrintWriter out, int otdev) throws PsseModelException
	{
		OneTermDev od;
		float pval, qval;
		if (otdev < _ngen)
		{
			od = _model.getGenerators().get(otdev);
			Bus b = od.getBus();
			pval = (b.getBusType() == BusTypeCode.Slack) ? 0f : od.getPpu();
			qval = (b.getBusType() != BusTypeCode.Load) ? 0f : od.getQpu();
		}
		else if ((otdev -= _ngen) < _nload)
		{
			od = _model.getLoads().get(otdev);
			pval = -od.getPpu();
			qval = -od.getQpu();
		}
		else if ((otdev -= _nload) < _nshunt)
		{
			od = _model.getShunts().get(otdev);
			pval = 0;
			qval = _shq[otdev];
		}
		else
		{
			otdev -= _nshunt;
			od = _model.getSvcs().get(otdev);
			pval = 0;
			qval = _svcq[otdev];
		}

		out.format("\"%s\",\"%s\",%f,%f\n",
				od.getObjectID(), od.getObjectName(), PAMath.pu2mw(pval), PAMath.pu2mvar(qval)); 

	}

	public void setBranchFlows(float[] pfrm, float[] qfrm, float[] pto,
			float[] qto)
	{
		System.arraycopy(pfrm, 0, _pfrm, 0, pfrm.length);
		System.arraycopy(qfrm, 0, _qfrm, 0, qfrm.length);
		System.arraycopy(pto, 0, _pto, 0, pto.length);
		System.arraycopy(qto, 0, _qto, 0, qto.length);
	}

	public void setTwoTermDCLineFlows(float[] pfrm, float[] qfrm, float[] pto,
			float[] qto) throws PsseModelException
	{
		int nbr = _model.getBranches().size();
		System.arraycopy(pfrm, 0, _pfrm, nbr, pfrm.length);
		System.arraycopy(qfrm, 0, _qfrm, nbr, qfrm.length);
		System.arraycopy(pto, 0, _pto, nbr, pto.length);
		System.arraycopy(qto, 0, _qto, nbr, qto.length);
	}

	public void setShunts(float[] q)
	{
		_shq = q;
	}

	public void setSVCs(float[] q)
	{
		_svcq = q;
	}

	public void setVoltages(float[] va, float[] vm)
	{
		_vm = vm;
		_va = va;
	}
	
	public void setMismatches(float[] pmm, float[] qmm)
	{
		_pmm = pmm;
		_qmm = qmm;
	}

	/*public float[] getPfrom(){return _pfrm;}
	public float[] getPto(){return _pto;}
	public float[] getQfrom(){return _qfrm;} 
	public float[] getQto(){return _qto;} 
	public float[] getSfrom()
	{
		float[] sfrm = new float[_nbr];
		for(int i=0; i<_nbr; i++)
		{
			sfrm[i] = (float) Math.sqrt(Math.pow(_pfrm[i], 2) + Math.pow(_qfrm[i], 2));
		}
		return sfrm;
	}
	public float[] getSto()
	{
		float[] sto = new float[_nbr];
		for(int i=0; i<_nbr; i++)
		{
			sto[i] = (float) Math.sqrt(Math.pow(_pto[i], 2) + Math.pow(_qto[i], 2));
		}
		return sto;
	}*/
}