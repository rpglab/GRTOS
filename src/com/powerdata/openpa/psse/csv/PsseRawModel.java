package com.powerdata.openpa.psse.csv;

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

import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;

import com.powerdata.openpa.psse.BusList;
import com.powerdata.openpa.psse.BusTypeCode;
import com.powerdata.openpa.psse.LineList;
import com.powerdata.openpa.psse.PsseModelException;
import com.powerdata.openpa.psse.conversions.XfrZToolFactory;
import com.powerdata.openpa.psse.conversions.XfrZTools;
import com.powerdata.openpa.psse.util.ImpedanceFilter;
import com.powerdata.openpa.psse.util.MinZMagFilter;
import com.powerdata.openpa.psse.util.TP;
import com.powerdata.openpa.psse.util.TransformerRaw;
import com.powerdata.openpa.psseraw.Psse2CSV;
import com.powerdata.openpa.psseraw.PsseProcException;
import com.powerdata.openpa.tools.QueryString;
import com.powerdata.openpa.tools.StarNetwork;
import com.rtca_cts.ausData.ACBranchRates;
import com.rtca_cts.ausData.AreaData;
import com.rtca_cts.ausData.BusGroupElems;
import com.rtca_cts.ausData.ElemMapBus;
import com.rtca_cts.ausData.NearbyElems;
import com.rtca_cts.ausData.OneBusIsland;
import com.rtca_cts.ausData.PowerFlowResults;
import com.rtca_cts.ausData.RadialBranches;
import com.rtca_cts.ausXP.BusTypeManager;
import com.rtca_cts.contingencyanalysis.ContingencyListBrc;
import com.rtca_cts.contingencyanalysis.ContingencyListGen;
import com.rtca_cts.contingencyanalysis.ElemGroupAtGenBuses;
import com.rtca_cts.contingencyanalysis.ElemsMonitorVio;
import com.rtca_cts.param.ParamFDPFlow;
import com.rtca_cts.param.ParamGenReDisptch;
import com.rtca_cts.transmissionswitching.NBestTS_report;
import com.rtca_cts.transmissionswitching.TransmSwitList;
import com.utilxl.log.DiaryXL;

public class PsseRawModel extends com.powerdata.openpa.psse.PsseModel
{
	/** root of the directory where the csv files are stored */
	File _dir;
	BusListRaw				_buses;
	LineListRaw			_branchList;
	TransformerRawList		_xfrList;
	PhaseShifterRawList	_psList;
	ShuntRawList			_shList;
	SvcRawList				_svcList;
	TwoTermDCLineList		_dcline;
	LoadList			_loads;
	GenList				_generatorList;
	private TP			_tp = null;
	IslandList			_islands = null;
	
	public PsseRawModel(String parms) throws PsseModelException
	{
		QueryString q = new QueryString(parms);
		if (!q.containsKey("path") && !q.containsKey("file"))
		{
			throw new PsseModelException("com.powerdata.openpa.psse.csv.PsseInputModel Missing path= or file= in uri.");
		}
		if (q.containsKey("file"))
		{
			File raw = new File(q.get("file")[0]);
			File tmpdir = new File(System.getProperty("java.io.tmpdir"));
			String sname = raw.getName();
			_dir = new File(tmpdir, sname.substring(0, sname.length()-4));
			try
			{
				if (_dir.exists())
				{
					File[] flist = _dir.listFiles(new FilenameFilter()
					{
						@Override
						public boolean accept(File arg0, String arg1)
						{
							return arg1.toLowerCase().endsWith(".csv");
						}
					});
					for(File f : flist) f.delete();
				}
				_dir.mkdirs();
				Reader rpsse = new FileReader(raw);
				Psse2CSV p2c = new Psse2CSV(rpsse, null, _dir);
				p2c.process();
				rpsse.close();
				p2c.cleanup();
			} catch (IOException | PsseProcException e)
			{
				throw new PsseModelException(e);
			}
		}
		else
		{
			_dir = new File(q.get("path")[0]);
		}
		analyzeRawTransformers();
		analyzeRawShunts();
		_tp = new TP(this);
	}
	
	
	public File getDir() { return _dir; }
	@Override
	public BusListRaw getBuses() throws PsseModelException
	{
		if (_buses == null) _buses = new BusListRaw(this);
		return _buses;
	}
	
	@Override
	public TwoTermDCLineList getTwoTermDCLines() throws PsseModelException
	{
		if (_dcline == null) _dcline = new TwoTermDCLineList(this);
		return _dcline;
	}

	@Override
	public LineList getLines() throws PsseModelException
	{
		if (_branchList == null) _branchList = new LineListRaw(this);
		return _branchList;
	}
	@Override
	public TransformerRawList getTransformers() throws PsseModelException
	{
		return _xfrList;
	}
	@Override
	public PhaseShifterRawList getPhaseShifters() throws PsseModelException
	{
		return _psList;
	}
	
	@Override
	public ShuntRawList getShunts() throws PsseModelException
	{
		return _shList;
	}
	@Override
	public SvcRawList getSvcs() throws PsseModelException
	{
		return _svcList;
	}
	
	/** convert 3-winding to 2-winding and detect phase shifters */
	protected void analyzeRawTransformers() throws PsseModelException
	{
		BusList rbus = getBuses();

		XfrZToolFactory ztf = XfrZToolFactory.Open(getPsseVersion());
		
		Transformer3RawList rlist = new Transformer3RawList(this, getDir());
		final TransformerPrep psprep = new TransformerPrep(),
				xfprep = new TransformerPrep();
		
		class ResolveXfrPrep
		{
			TransformerPrep get(int mode)
			{
				return (Math.abs(mode) == 3) ? psprep : xfprep;
			}
		}

		ResolveXfrPrep rp = new ResolveXfrPrep();
		ArrayList<Integer> ndx3w = new ArrayList<>();
		
		for (TransformerRaw xf : rlist)
		{
			String k = xf.getK();
			String bus1 = rbus.get(xf.getI()).getObjectID();
			String bus2 = rbus.get(xf.getJ()).getObjectID();
			XfrZTools zt = ztf.get(xf.getCZ());
			
			if (k.equals("0"))
			{
				rp.get(xf.getCOD1()).prep(xf, 1, bus1, bus2, zt.convert2W(xf));
			}
			else
			{
				String bus3 = rbus.get(xf.getK()).getObjectID();
				ndx3w.add(xf.getIndex());
				String newstar = "TXSTAR-"+xf.getObjectID();
				StarNetwork z = zt.convert3W(xf).star();
				rp.get(xf.getCOD1()).prep(xf, 1, bus1, newstar, z.getZ1());
				rp.get(xf.getCOD2()).prep(xf, 2, bus2, newstar, z.getZ2());
				rp.get(xf.getCOD3()).prep(xf, 3, bus3, newstar, z.getZ3());
			}
		}
		_buses.addStarNodes(rlist, ndx3w);
		_xfrList = new TransformerRawList(this, rlist, xfprep);
		_psList = new PhaseShifterRawList(this, rlist, psprep);
	}

	protected void analyzeRawShunts() throws PsseModelException
	{
		SwitchedShuntRawList rsh = new SwitchedShuntRawList(this);
		
		ArrayList<Integer> shndx = new ArrayList<>();
		ArrayList<Integer> svcndx = new ArrayList<>();
		
		for (int i=0; i < rsh.size(); ++i)
		{
			((testForSvc(rsh.getMODSW(i), rsh.getBINIT(i), rsh.getN(i), rsh.getB(i)))?svcndx:shndx).add(i);
		}
		
		_shList = new ShuntRawList(this, rsh, shndx);
		_svcList = new SvcRawList(this, rsh, svcndx);
	}
	
	
	boolean testForSvc(int modsw, float binit, int[] n, float[] b) throws PsseModelException
	{
		if (modsw == 2) return true;
		if (modsw == 0 && n[0] == 1)
		{
			if (n[1] == 0 && 0f < binit && binit < b[0]) return true;
			if (n[2] == 0 && b[0] < binit && binit < b[1]) return true;
		}
		return false;
	}


	@Override
	public LoadList getLoads() throws PsseModelException
	{
		if (_loads == null) _loads = new LoadList(this, getDir());
		return _loads;
	}

	@Override
	public GenList getGenerators() throws PsseModelException
	{
		if (_generatorList == null) _generatorList = new GenList(this, getDir());
		return _generatorList;
	}
	public TP tp() throws PsseModelException
	{
		if (_tp == null) _tp = new TP(this);
		return _tp;
//		return new TP(this);
	}

	@Override
	public void resetTP() {_tp = null;_islands = null;}
	
//	@Deprecated
//	public int getIslandCount() throws PsseModelException
//	{
//		return _tp.getIslandCount();
//	}
//
//	@Deprecated
//	public boolean isNodeEnergized(int node) throws PsseModelException
//	{
//		int island = _tp.getIsland(node);
//		return (island == -1) ? false : _tp.isIslandEnergized(island);
//	}
//
//	@Deprecated
//	public int getIsland(int node) throws PsseModelException
//	{
//		return _tp.getIsland(node);
//	}
//
//	@Deprecated
//	public BusList getBusesForIsland(int island) throws PsseModelException
//	{
//		return new com.powerdata.openpa.psse.util.BusSubList(_buses,
//				_tp.getIslandNodes(island));
//	}
//
//	@Deprecated
//	public BusTypeCode getBusType(int node) throws PsseModelException
//	{
//		return _tp.getBusType(node);
//	}
//
	@Override
	public IslandList getIslands() throws PsseModelException
	{
		if (_islands == null)
			_islands = new IslandList(this);
		return _islands;
//		return new IslandList(this);
	}

	@Override
	public int[] getBusNdxForType(BusTypeCode bustype)
			throws PsseModelException
	{
		return tp().getBusNdxsForType(bustype);
	}
	
	
	//Added by Xingpeng.Li, May.18, 2014.	
	@Override
	public void setBusNdxForTypeLoad(int[] LoadBuses) throws PsseModelException 
	{
		tp().setBusNdxsForTypeLoad(LoadBuses);
	}
	@Override
	public void setBusNdxForTypeGen(int[] GenBuses) throws PsseModelException 
	{
		tp().setBusNdxsForTypeGen(GenBuses);
	}
	@Override
	public void setBusNdxForTypeSlack(int[] SlackBuses) throws PsseModelException
	{
		tp().setBusNdxsForTypeSlack(SlackBuses);
	}
	
	
	
	//Added by Xingpeng.Li, Jan.24, 2015.	
	ACBranchRates _ACBrcCap = null;
	@Override
	public void clearACBrcCap() {_ACBrcCap = null;}
	@Override
	public void setACBrcCap(ACBranchRates acBrcRates) {_ACBrcCap = acBrcRates;}
	@Override
	public ACBranchRates getACBrcCapData() throws PsseModelException
	{
	    if (_ACBrcCap == null) _ACBrcCap = new ACBranchRates(this);
	    return _ACBrcCap;
	}
	
	//Added by Xingpeng.Li, Jan.19, 2015.  - Information about Impedance Filter. 
	float _minxmag = ParamFDPFlow.getMinxmag();
	ImpedanceFilter _zfilt = null;
	@Override
	public void setMinXMag(float x) {_minxmag = x; _zfilt = null;}
	@Override
	public float getMinXMag() {return _minxmag;}
	@Override
	public void clearXFilter() throws PsseModelException {_zfilt = null;}
	@Override
	public ImpedanceFilter getXFilter() throws PsseModelException
	{
		if (_zfilt == null) _zfilt = new MinZMagFilter(this.getBranches(), _minxmag);
		return _zfilt;
	}
	
	//Added by Xingpeng.Li, Jan.7, 2015.	
	ElemGroupAtGenBuses _elemGroupGenBuses = null;
	@Override
	public void setElemGroupGenBuses(ElemGroupAtGenBuses tmp) {_elemGroupGenBuses = tmp;}
	@Override
	public ElemGroupAtGenBuses getElemGroupGenBuses() throws PsseModelException
	{
		if (_elemGroupGenBuses == null) _elemGroupGenBuses = new ElemGroupAtGenBuses(this);
		return _elemGroupGenBuses;
	}

	//Added by Xingpeng.Li, Jan.7, 2015.	
	BusGroupElems _busGroupElems = null;
	@Override
	public void setBusGroupElems(BusGroupElems tmp) {_busGroupElems = tmp;}
	@Override
	public BusGroupElems getBusGroupElems() throws PsseModelException
	{
		if (_busGroupElems == null) _busGroupElems = new BusGroupElems(this);
		return _busGroupElems;
	}
	
	//Added by Xingpeng.Li, Jan.26, 2015.
	ElemMapBus _elemMapBus = null;
	@Override
	public void setElemMapBus(ElemMapBus tmp) {_elemMapBus = tmp;}
	@Override
	public ElemMapBus getElemMapBus() throws PsseModelException
	{
		if (_elemMapBus == null) _elemMapBus = new ElemMapBus(this);
		return _elemMapBus;
	}

	//Added by Xingpeng.Li, Jan.24, 2015.	
	NearbyElems _nearbyElems = null;
	@Override
	public void clearNearbyElems() {_nearbyElems = null;}
	@Override
	public void setNearbyElems(NearbyElems nearbyElems) {_nearbyElems = nearbyElems;}
	@Override
	public NearbyElems getNearbyElemsData() throws PsseModelException
	{
	    if (_nearbyElems == null) _nearbyElems = new NearbyElems(this);
	    return _nearbyElems;
	}
	

	//Added by Xingpeng.Li, Jan.23, 2015.  - Elements to be monitored in terms of violations record. 
	ElemsMonitorVio _elemsMnt = null;
	@Override
	public void clearElemsMnt() {_elemsMnt = null;}
	@Override
	public void setElemsMnt(ElemsMonitorVio elemsMnt) {_elemsMnt = elemsMnt;}
	@Override
	public ElemsMonitorVio getElemMnt() throws PsseModelException
	{
		if (_elemsMnt == null) _elemsMnt = new ElemsMonitorVio(this);
		return _elemsMnt;
	}

	//Added by Xingpeng.Li, Jan.26, 2015.	
	ContingencyListBrc _BrcContiList = null;
	@Override
	public ContingencyListBrc getContingencyListBrcData() throws PsseModelException
	{
		if (_BrcContiList == null) _BrcContiList = new ContingencyListBrc(this);
		return _BrcContiList;
	}

	ContingencyListGen _GenContiList = null;
	@Override
	public ContingencyListGen getContingencyListGenData() throws PsseModelException
	{
		if (_GenContiList == null) _GenContiList = new ContingencyListGen(this);
		return _GenContiList;
	}

	//Added by Xingpeng.Li, Jan.24, 2015.	
	TransmSwitList _TSList = null;
	@Override
	public TransmSwitList getTransmSwitListData() throws PsseModelException
	{
		if (_TSList == null) _TSList = new TransmSwitList(this);
		return _TSList;
	}
	
	//Added by Xingpeng.Li, Jan.28, 2015.
	BusTypeManager _busTypeManager = null;
	@Override
	public BusTypeManager getBusTypeManagerData() throws PsseModelException
	{
		if (_busTypeManager == null) _busTypeManager = new BusTypeManager(this);
		return _busTypeManager;
	}
	
	//AreaData for generator re-dispatch
	AreaData _areaDataGenReDisptch = null;
	int[] _areaList = ParamGenReDisptch.getAreaList();
	@Override
	public void setAreaDataGenReDisptch(AreaData areaData) throws PsseModelException {_areaDataGenReDisptch = areaData;}
	@Override
	public AreaData getAreaDataGenReDisptch() throws PsseModelException
	{
		if (_areaDataGenReDisptch == null) _areaDataGenReDisptch = new AreaData(this, _areaList);
		return _areaDataGenReDisptch;
	}

	//Added by Xingpeng.Li, Feb.10, 2015.
	NBestTS_report _reportBestTS;
	@Override
	public NBestTS_report getNBestTSReport() throws PsseModelException
	{
		if (_reportBestTS == null) _reportBestTS = new NBestTS_report(this);
		return _reportBestTS;
	}

	NBestTS_report _reportBestTSNoTitle;
	@Override
	public NBestTS_report getNBestTSReport_NoTitle() throws PsseModelException
	{
		if (_reportBestTSNoTitle == null) _reportBestTSNoTitle = new NBestTS_report(this);
		return _reportBestTSNoTitle;
	}

	RadialBranches _radialBrc = null;
	@Override
	public RadialBranches getRadialBrcData() throws PsseModelException
	{
		if (_radialBrc == null) _radialBrc = new RadialBranches(this);
		return _radialBrc;
	}

	//Added by Xingpeng.Li, Feb.13, 2015.
	OneBusIsland _oneBusIsland = null;
	@Override
	public OneBusIsland getOneBusIslandData() throws PsseModelException
	{
		if (_oneBusIsland == null) _oneBusIsland = new OneBusIsland(this);
		return _oneBusIsland;
	}
	

	//Added by Xingpeng.Li, Mar.23, 2017.
	PowerFlowResults _pfResults = null;
	@Override
	public PowerFlowResults getPowerFlowResults() throws PsseModelException
	{
		if (_pfResults == null) _pfResults = new PowerFlowResults(this);
		return _pfResults;
	}
	@Override
	public void clearPowerFlowResults() {_pfResults = null;}

	//Added by Xingpeng.Li, Mar.23, 2017.
	DiaryXL _diary;
	@Override
	public void setDiary(DiaryXL diary) {_diary = diary;}
	@Override
	public DiaryXL getDiary() {return _diary;}

}
