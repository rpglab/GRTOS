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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.util.HashMap;

import com.powerdata.openpa.psse.util.BusSubList;
import com.powerdata.openpa.psse.util.ImpedanceFilter;
import com.powerdata.openpa.psse.util.LogSev;
import com.powerdata.openpa.psse.util.PsseModelLog;
import com.powerdata.openpa.psse.util.TP;
import com.powerdata.openpa.tools.AbstractBaseObject;
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
import com.rtca_cts.transmissionswitching.NBestTS_report;
import com.rtca_cts.transmissionswitching.TransmSwitList;
import com.utilxl.log.DiaryXL;
/**
 * 
 * @author marck@powerdata.com
 *
 */
public class PsseModel implements PsseLists
{
	/** static translations of scheme to input class */
	static HashMap<String,String> _SchemeToInputClass = new HashMap<String,String>();
	/** seed the class translations with some defaults */
	static
	{
		SetSchemeInputClass("pssecsv", "com.powerdata.openpa.psse.csv.PsseRawModel");
		SetSchemeInputClass("psseraw", "com.powerdata.openpa.psse.csv.PsseRawModel");
		SetSchemeInputClass("pd2cim", "com.powerdata.pa.psse.pd2cim.PsseModel");
	}
	/**
	 * Set a scheme to input class name translation.
	 * @param scheme
	 * @param pkg
	 */
	public static void SetSchemeInputClass(String scheme, String pkg)
	{
		_SchemeToInputClass.put(scheme, pkg);
	}
	/**
	 * Create a new input class using a uri.  The scheme needs to have been
	 * mapped in the scheme to input class translations.
	 * @param uri
	 * @return
	 * @throws PsseModelException
	 */
	public static PsseModel Open(String uri, int rank) throws PsseModelException
	{
		if (rank == 0) System.out.println("uri: "+uri);
		String[] tok = uri.split(":", 2);
		String clsnm = _SchemeToInputClass.get(tok[0]);
		if (clsnm == null) throw new PsseModelException("Scheme not defined for Input: "+tok[0]);
		
		try
		{
			Class<?> cls = Class.forName(clsnm);
			Constructor<?> con = cls.getConstructor(new Class[] {String.class});
			PsseModel rv = (PsseModel) con.newInstance(new Object[]{tok[1]});
			rv.setURI(uri);
			rv.getBusTypeManagerData(); //added on Jan.28th, 2015.
			return rv;
		}
		catch (Exception e)
		{
			throw new PsseModelException("Scheme "+tok[0]+" "+e, e);
		}
	}
	
	/**
	 * Create a new input class using a uri.  The scheme needs to have been
	 * mapped in the scheme to input class translations.
	 * @param uri
	 * @return
	 * @throws PsseModelException
	 */
	public static PsseModel Open(String uri) throws PsseModelException
	{
		return Open(uri, 0);
	}
	
	protected PsseModelLog _log = new PsseModelLog()
	{
		@Override
		public void log(LogSev severity, AbstractBaseObject obj, String msg) throws PsseModelException
		{
			String objclass = obj.getClass().getSimpleName();
			String objnm = obj.getDebugName();
			String objid = obj.getObjectID();
			((severity == LogSev.Error) ? System.err : System.out)
				.format("%s %s[%s] %s\n", objclass, objnm, objid, msg);
		}
	};
	
	String _uri;
	String _fileName;
	
	public String getFileName()
	{
		if (_fileName == null) calcFileName();
		return _fileName;
	}
	
	String calcFileName()
	{
		int startIdx = _uri.lastIndexOf("/") + 1;
		int endIdx = _uri.indexOf("&");
		_fileName = _uri.substring(startIdx, endIdx);
		startIdx = _fileName.lastIndexOf("=") + 1;
		_fileName = _fileName.substring(startIdx, _fileName.length());
		return _fileName;
	}
	
	public PsseModel() {} 
	public PsseModel(PsseModelLog log) {_log = log;} 
	
	public void log(LogSev severity, AbstractBaseObject obj, String msg) throws PsseModelException
	{
		_log.log(severity, obj, msg);
	}
	public long refresh() throws PsseModelException { return 0; }
	
	/** get system base MVA */
	public float getSBASE() {return 100f;}
	/** get psse version */
	public int getPsseVersion() {return 30;}

	/** find a Bus by ID */ 
	public Bus getBus(String id) throws PsseModelException {return getBuses().get(id);}
	
	/* Model-specific lists */
	public ImpCorrTblList getImpCorrTables() throws PsseModelException {return ImpCorrTblList.Empty;}
	public AreaList getAreas() throws PsseModelException {return AreaList.Empty;}
	public OwnerList getOwners() throws PsseModelException {return OwnerList.Empty;}
	public ZoneList getZones() throws PsseModelException {return ZoneList.Empty;}
	public IslandList getIslands() throws PsseModelException {return IslandList.Empty;}

	/* equipment group lists */
	@Override
	public BusList getBuses() throws PsseModelException {return BusList.Empty;}
	@Override
	public GenList getGenerators() throws PsseModelException {return GenList.Empty;}
	@Override
	public LoadList getLoads() throws PsseModelException {return LoadList.Empty;}
	@Override
	public LineList getLines() throws PsseModelException {return LineList.Empty;}
	@Override
	public TransformerList getTransformers() throws PsseModelException
	{
		return TransformerList.Empty;
	}
	@Override
	public PhaseShifterList getPhaseShifters() throws PsseModelException
	{
		return PhaseShifterList.Empty;
	}
	@Override
	public SwitchList getSwitches() throws PsseModelException {return SwitchList.Empty;}
	@Override
	public ShuntList getShunts() throws PsseModelException {return ShuntList.Empty;}
	@Override
	public SvcList getSvcs() throws PsseModelException {return SvcList.Empty;}
	@Override
	public SwitchedShuntList getSwitchedShunts() throws PsseModelException {return SwitchedShuntList.Empty;}
	@Override
	public TwoTermDCLineList getTwoTermDCLines() throws PsseModelException {return TwoTermDCLineList.Empty;}
	
	
	/** for convience, get a list of all ac branches */
	public ACBranchList getBranches() throws PsseModelException
	{
		if (_branches == null) _branches = new ACBranchList(getLines(), getTransformers(), getPhaseShifters());
		return _branches;
	}

// Comment on Jan.25, 2015.
//	/** for convience, get a list of all ac branches */
//	public ACBranchList getBranches() throws PsseModelException
//	{
//		return new ACBranchList(getLines(), getTransformers(), getPhaseShifters());
//	}
	public OneTermDevList getOneTermDevs() throws PsseModelException
	{
		return new OneTermDevList(getLoads(), getGenerators(), getShunts(), getSvcs());
	}

	public TwoTermDevList getTwoTermDevs() throws PsseModelException
	{
		return new TwoTermDevList(new Object[] { getLines(), getTransformers(),
				getPhaseShifters(), getSwitches(), getTwoTermDCLines() });
	}

	public String getURI() {return _uri;}
	public void setURI(String uri) {_uri = uri;}
	
	public int[] getBusNdxForType(BusTypeCode bustype) throws PsseModelException {return new int[0];}

	public BusList getBusesForType(BusTypeCode bustype)
			throws PsseModelException
	{
		return new BusSubList(getBuses(), getBusNdxForType(bustype));
	}
	
	
	//Added by Xingpeng.Li, May.18, 2014.	
	public void setBusNdxForTypeLoad(int[] LoadBuses) throws PsseModelException {}
	public void setBusNdxForTypeGen(int[] GenBuses) throws PsseModelException {}
	public void setBusNdxForTypeSlack(int[] SlackBuses) throws PsseModelException {}
	public void resetTP() {}

	//Added by Xingpeng.Li, Jan.7, 2015.	
	public void setBusGroupElems(BusGroupElems tmp) {}
	public BusGroupElems getBusGroupElems() throws PsseModelException {return null;}
	public void setElemGroupGenBuses(ElemGroupAtGenBuses tmp) {}
	public ElemGroupAtGenBuses getElemGroupGenBuses() throws PsseModelException {return null;}

	//Added by Xingpeng.Li, Jan.19, 2015.	
	public void setMinXMag(float x) {}
	public float getMinXMag() {return 0f;}
	public ImpedanceFilter getXFilter() throws PsseModelException {return null;}
	public void clearXFilter() throws PsseModelException {}

	//Added by Xingpeng.Li, Jan.23, 2015.	
	public void clearElemsMnt() {}
	public void setElemsMnt(ElemsMonitorVio elemsMnt) {}
	public ElemsMonitorVio getElemMnt() throws PsseModelException {return null;}

	//Added by Xingpeng.Li, Jan.24, 2015.	
	public void clearACBrcCap() {}
	public void setACBrcCap(ACBranchRates acBrcRates) {}
	public ACBranchRates getACBrcCapData() throws PsseModelException {return null;}

	public void clearNearbyElems() {}
	public void setNearbyElems(NearbyElems nearbyElems) {}
	/** Currently, _nearbyElems is for generating TS_List only; Warning: for other applications,
	 * call clearNearbyElems() to reset _nearbyElems. Refer to TransmSwitList.java. BE CAREFUL. */
	public NearbyElems getNearbyElemsData() throws PsseModelException {return null;}

	//Added by Xingpeng.Li, Jan.25, 2015.
	ACBranchList _branches = null;
	public TransmSwitList getTransmSwitListData() throws PsseModelException {return null;}
	
	//Added by Xingpeng.Li, Jan.26, 2015.
	public ContingencyListBrc getContingencyListBrcData() throws PsseModelException {return null;}
	public ContingencyListGen getContingencyListGenData() throws PsseModelException {return null;}

	//Added by Xingpeng.Li, Jan.26, 2015.
	public void setElemMapBus(ElemMapBus tmp) {}
	public ElemMapBus getElemMapBus() throws PsseModelException {return null;}

	//Added by Xingpeng.Li, Jan.28, 2015.
	public BusTypeManager getBusTypeManagerData() throws PsseModelException {return null;}

	//Added by Xingpeng.Li, Jan.29, 2015.
	public void setAreaDataGenReDisptch(AreaData areaData) throws PsseModelException { }
	public AreaData getAreaDataGenReDisptch() throws PsseModelException {return null;}

	/** Be very careful: _radialBrc is valid only for the original system topology. */
	public RadialBranches getRadialBrcData() throws PsseModelException {return null;}
	
    public OneBusIsland getOneBusIslandData() throws PsseModelException {return null;}
	public NBestTS_report getNBestTSReport() throws PsseModelException {return null;}
	public NBestTS_report getNBestTSReport_NoTitle() throws PsseModelException {return null;}
    
	//Added by Xingpeng.Li, Mar.23, 2017.
	public PowerFlowResults getPowerFlowResults() throws PsseModelException {return null;}
	public void clearPowerFlowResults() { }
	
	public void setDiary(DiaryXL diary) { }
	public DiaryXL getDiary() {return null;}
	
	// Write some data to a txt file - Sep.28th, 2015.
	public void writeData() throws PsseModelException {
		String fileName = "C:\\Users\\xingpeng\\Desktop\\" + getFileName();
		writeBusDATA(fileName);
		writeBrcDATA(fileName);
		writeGenDATA(fileName);
	}
	
	void writeBusDATA(String fileName) throws PsseModelException 
	{
		try
		{
			String brcFileName = fileName + "_bus" + ".txt";
		    OutputStream resultFile = new FileOutputStream(brcFileName, false);
		    PrintStream outFile = new PrintStream (resultFile);
		    
		    BusList buses = getBuses();
		    BusGroupElems busgrp = getBusGroupElems();
			outFile.println(" busIndex, type, Pload_MW, Qload_MW, Gs_MW, Bs_MW, area, Vm, Va_degree, baseKV, zone");
			for (int i=0; i<buses.size(); i++) {
				Bus bus = buses.get(i);
				int busType = 1;
				if (bus.getBusType().equals(BusTypeCode.Gen)) busType = 2;
				else if (bus.getBusType().equals(BusTypeCode.Slack)) busType = 3;
				outFile.print(" "+i + " "+busType);
				
				float Pload = busgrp.getTotalPLoad(i)*100;
				float Qload = busgrp.getTotalQLoad(i)*100;
				outFile.print(" "+Pload + " "+Qload);
				
				float GShunt = busgrp.getTotalGShunt(i)*100;
				float BShunt = busgrp.getTotalBShunt(i)*100;
				outFile.print(" "+GShunt + " "+BShunt);
				
				outFile.print(" "+bus.getAREA() + " "+bus.getVMpu() + " " + bus.getVA());
				outFile.println(" " + bus.getBASKV() + " " + bus.getZONE());
			}
		    outFile.close();
		    System.out.println("Write bus data successfully");
	    } catch (FileNotFoundException e) {
	    	System.out.println();
	    	System.out.println("Cannot write bus data to file" + e);
	    	e.printStackTrace();
	    }
	}
	
	void writeBrcDATA(String fileName) throws PsseModelException 
	{
		try
		{
			String brcFileName = fileName + "_branch" + ".txt";
		    OutputStream resultFile = new FileOutputStream(brcFileName, false);
		    PrintStream outFile = new PrintStream (resultFile);
		    
		    ACBranchList branches = getBranches();
			outFile.println(" index, frmBusIndex, toBusIndex, brcID, status, r, x, 1/2*bmag, RateA_mw, RateB_mw, RateC_mw, tap, angle_radian");
			for (int i=0; i<branches.size(); i++) {
				outFile.print(" "+i);
				outFile.print(" "+branches.getFromBus(i).getIndex() + " "+branches.getToBus(i).getIndex() + " "+branches.getCKT(i));
				int st = (branches.isInSvc(i) == true) ? 1 : 0;
				outFile.print(" "+st +" "+branches.getR(i) +" "+branches.getX(i)+ " "+(branches.getBmag(i)+branches.getFromBchg(i)) );
				outFile.print(" "+branches.getRateA(i) + " "+branches.getRateB(i)  + " "+branches.getRateC(i) );
				outFile.println(" "+branches.getFromTap(i)/branches.getToTap(i) + " " +branches.getPhaseShift(i));
			}
		    outFile.close();
		    System.out.println("Write branch data successfully");
	    } catch (FileNotFoundException e) {
	    	System.out.println();
	    	System.out.println("Cannot write branch data to file" + e);
	    	e.printStackTrace();
	    }
	}
	
	void writeGenDATA(String fileName) throws PsseModelException 
	{
		try
		{
			String brcFileName = fileName + "_gen" + ".txt";
		    OutputStream resultFile = new FileOutputStream(brcFileName, false);
		    PrintStream outFile = new PrintStream (resultFile);
		    
		    GenList gens = getGenerators();
			outFile.println(" index, busIndex, Pg_mw, Qg_mw, Qmax_mw, Qmin_mw, Vg, MachBase_mw, st, Pmax_mw, Pmin_mw");
			for (int i=0; i<gens.size(); i++) {
				Gen gen = gens.get(i);
				outFile.print(" "+i + " "+gen.getBus().getIndex());
				outFile.print(" "+gen.getP() + " "+gen.getQ());
				outFile.print(" "+gen.getQT() + " "+gen.getQB() + " " + gen.getVS() + " " + gen.getMBASE());
				
				int isInSvc = 1;
				if (gen.isInSvc() == false) isInSvc = 0;
				outFile.print(" " + isInSvc);
				outFile.println(" " + gen.getPT() + " " + gen.getPB());
			}
		    outFile.close();
		    System.out.println("Write gen data successfully");
	    } catch (FileNotFoundException e) {
	    	System.out.println();
	    	System.out.println("Cannot write gen data to file" + e);
	    	e.printStackTrace();
	    }
	}
	
	
}	

