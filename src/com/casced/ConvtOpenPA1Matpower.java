package com.casced;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

import com.powerdata.openpa.psse.ACBranch;
import com.powerdata.openpa.psse.ACBranchList;
import com.powerdata.openpa.psse.BusList;
import com.powerdata.openpa.psse.GenList;
import com.powerdata.openpa.psse.LoadList;
import com.powerdata.openpa.psse.PsseModel;
import com.powerdata.openpa.psse.PsseModelException;
import com.powerdata.openpa.psse.SVC;
import com.powerdata.openpa.psse.SvcList;
import com.rtca_cts.ausData.BusGroupElems;
import com.sced.model.param.ParamInput;
import com.utilxl.log.DiaryXL;

/**
 * 
 * Initialized in March 2017.
 * 
 * @author Xingpeng Li (xplipower@gmail.com)
 *
 */
public class ConvtOpenPA1Matpower {

	PsseModel _model;
	boolean _isOutCSV;     // if true, then, csv files will be generated; otherwise, matpower format file will be created.

	public ConvtOpenPA1Matpower(PsseModel model) {_model = model;}
	
	public void setIsOutCSV(boolean flag) {_isOutCSV = flag;}
	
	private PrintWriter createPrintWriter(String fileName)
	{
		File pout = new File(fileName);
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(new BufferedWriter(new FileWriter(pout)));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return pw;
	}
	
	/* Only for the main Island */
	public void createMatpowerCase() throws PsseModelException
	{
		float sysBase = _model.getSBASE();
		BusList busPAData = _model.getBuses();
		int nbuses = busPAData.size();

		String caseName = "case" + nbuses;
		PrintWriter pw = createPrintWriter(caseName + ".m");

		if (_isOutCSV == false) {
			/* Title part */
			pw.println("function mpc = "+caseName);
			pw.println("%CASE"+nbuses+" Power flow data created from OpenPA format based case\n%");
			pw.println("%   Data format conversion is done by Xingpeng Li");
			pw.println("%   PhD student at Arizona State University\n%");
			pw.println("%   Date of convertion (yyyy-MM-dd HH:mm:ss): "+_model.getDiary().getCurrentTimeStamp());
			pw.println("%   Distributed with permission\n%");
			
			pw.println("%   MATPOWER Case Format : Version 2");
			pw.println("%\nmpc.version = '2';");
			
			pw.println("\n%%-----  Power Flow Data  -----%%");
			pw.println("%% system MVA base");
			pw.println("mpc.baseMVA = "+ sysBase + ";");
			
			/* Bus data */
			pw.println("\n%% bus data");
			pw.println("%	bus_i	type	Pd	Qd	Gs	Bs	area	Vm	Va	baseKV	zone	Vmax	Vmin	%busNumber	busName");
			pw.println("mpc.bus = [");
		} else {
			pw = createPrintWriter(caseName + "Bus.csv");
			pw.println("bus_i,type,Pd,Qd,Gs,Bs,area,Vm,Va,baseKV,zone,Vmax,Vmin,busNumber,busName");
		}
		
		int[] busNumber = new int[nbuses];
		for (int n=0; n<nbuses; n++)
			busNumber[n] = busPAData.getI(n);
		HashMap<Integer, Integer> numToIdx = new HashMap<Integer, Integer>();
		for (int i=0; i<nbuses; i++)
			numToIdx.put(busNumber[i], i);

		LoadList loadsPAData = _model.getLoads();
		int nloads = loadsPAData.size();
		float[] Pd = new float[nbuses];
		float[] Qd = new float[nbuses];
		for (int d=0; d<nloads; d++)
		{
			if (loadsPAData.isInSvc(d) == false) continue;
			int loadBusNumber = loadsPAData.getBus(d).getI();
			int busIdx = numToIdx.get(loadBusNumber);
			if (busIdx >= nbuses) {
				System.err.println("bus idx exceeds limit:"+busIdx);
				continue;
			}
			Pd[busIdx] += loadsPAData.getP(d);
			Qd[busIdx] += loadsPAData.getQ(d);
		}
				
		float[][] busShunts = getBusShunt();
		float[] busShuntsG = busShunts[0];
		float[] busShuntsB = busShunts[1];
		SvcList svclist = _model.getSvcs();
		for (int k = 0; k < svclist.size(); ++k)
		{
			SVC svc = svclist.get(k);
			if (svc.isInSvc()) {
				float bSvc = svc.getBINIT();
				int idxBus = svc.getBus().getIndex();
				busShuntsB[idxBus] += bSvc;
			}
		}

		//for (int n=nbuses-1; n>=0; n--) // reverse the order
		for (int n=0; n<nbuses; n++)
		{
			int type = busPAData.getIDE(n);
			int bus_i = n+1;
			float PdTmp = Pd[n];
			float QdTmp = Qd[n];
			float GSTmp = busShuntsG[n];
			float BSTmp = busShuntsB[n];
			int area = busPAData.getAREA(n);

			float vm = busPAData.getVMpu(n);
			float va = busPAData.getVA(n);
			float baseKV = busPAData.getBASKV(n);
			
			int zone = 1;
			float Vmax = 1.1f;
			float Vmin = 0.9f;
			String busName = busPAData.getFullName(n);
			
			
			if (_isOutCSV == false) {
				pw.print("	"+bus_i +"	"+type +"	"+PdTmp +"	"+QdTmp + "	"+GSTmp + "	"+BSTmp +"	"+area);
				pw.println("	"+vm +"	"+va +"	"+baseKV +"	"+zone + "	"+Vmax + "	"+Vmin + "	%"+busNumber[n]+"	"+busName);
			} else {
				pw.format("%d,%d,%f,%f,%f,%f,%d,%f,%f,%f,%d,%f,%f,%d,%s\n",
						bus_i,
						type,
						PdTmp,
						QdTmp,
						GSTmp,
						BSTmp,
						area,
						vm,
						va,
						baseKV,
						zone,
						Vmax,
						Vmin,
						busNumber[n],
						busName);
			}
		}
		if (_isOutCSV == false) pw.println("];");
		else {
			pw.flush();
			pw.close();
			System.out.println("Bus data has been dumped into a csv file");
		}
		
		/* Gen data */
		if (_isOutCSV == false) {
			pw.println("\n%% generator data");
			pw.println("%	bus	Pg	Qg	Qmax	Qmin	Vg	mBase	status	Pmax	Pmin	%genName");
			pw.println("mpc.gen = [");
		} else {
			pw = createPrintWriter(caseName + "Gen.csv");
			pw.println("bus,Pg,Qg,Qmax,Qmin,Vg,mBase,status,Pmax,Pmin,genName");
		}
	
		GenList gens = _model.getGenerators();
		int ngens = gens.size();
		//for (int g=ngens-1; g>=0; g--)  // reverse the order
		for (int g=0; g<ngens; g++)
		{
			int genBusNumber = gens.getBus(g).getI();
			int busIdx = numToIdx.get(genBusNumber);
			float pg = gens.getP(g);
			float qg = gens.getQ(g);
			float qgmax = gens.getQT(g);
			float qgmin = gens.getQB(g);
			float vg = gens.getVS(g);
			//vg = gens.getBus(g).getVMpu();
			
			int st = 1;
			if (gens.isInSvc(g) == false) st = 0;
			float pgmax = gens.getPT(g);
			float pgmin = gens.getPB(g);
			String genName = gens.getFullName(g);
			
			if (_isOutCSV == false) {
				pw.print("	"+(busIdx+1) +"	"+pg +"	"+qg +"	"+qgmax + "	"+qgmin + "	"+vg +"	"+sysBase);
				pw.println("	"+st +"	"+pgmax +"	"+pgmin + "	%"+genName);
			} else {
				pw.format("%d,%f,%f,%f,%f,%f,%f,%d,%f,%f,%s\n",
						(busIdx+1),
						pg,
						qg,
						qgmax,
						qgmin,
						vg,
						sysBase,
						st,
						pgmax,
						pgmin,
						genName);
			}
		}
		if (_isOutCSV == false) pw.println("];");
		else {
			pw.flush();
			pw.close();
			System.out.println("Generator data has been dumped into a csv file");
		}
		
		
		/* Branch data */
		if (_isOutCSV == false) {
			pw.println("\n%% branch data");
			pw.println("%	fbus	tbus	r	x	b	rateA	rateB	rateC	ratio	angle	status	angmin	angmax	%branchName");
			pw.println("mpc.branch = [");
		} else {
			pw = createPrintWriter(caseName + "Branch.csv");
			pw.println("fbus,tbus,r,x,b,rateA,rateB,rateC,ratio,angle,status,angmin,angmax,branchName");
		}
	
		float[] arrayRateA = _model.getACBrcCapData().getRateA();
		float[] arrayRateB = _model.getACBrcCapData().getRateB();
		float[] arrayRateC = _model.getACBrcCapData().getRateC();
		ACBranchList branches = _model.getBranches();
		for (int k=0; k<branches.size(); k++)
		{
			ACBranch branch = branches.get(k);
			int frmBusIdx = numToIdx.get(branch.getFromBus().getI());
			int toBusIdx = numToIdx.get(branch.getToBus().getI());
			float r = branch.getR();
			float x = branch.getX();
			float b = branch.getBmag() + branch.getFromBchg() + branch.getToBchg();
			float rateA = arrayRateA[k];
			float rateB = arrayRateB[k];
			float rateC = arrayRateC[k];
			float ratio = branch.getFromTap() / branch.getToTap();
			float angle = (float) (branch.getPhaseShift() * 180 / Math.PI);
			int st = 1;
			if (branch.isInSvc() == false) st = 0;
			float angmin = -360;
			float angmax = 360;
			String branchName = branch.getObjectID();
			
			if (_isOutCSV == false) {
				pw.print("	"+(frmBusIdx+1) +"	"+(toBusIdx+1) +"	"+r +"	"+x + "	"+b + "	"+rateA +"	"+rateB);
				pw.println("	"+rateC +"	"+ratio +"	"+angle +"	"+st +"	"+angmin +"	"+angmax + "	%"+branchName);
			} else {
				pw.format("%d,%d,%f,%f,%f,%f,%f,%f,%f,%f,%d,%f,%f,%s\n",
						(frmBusIdx+1),
						(toBusIdx+1),
						r,
						x,
						b,
						rateA,
						rateB,
						rateC,
						ratio,
						angle,
						st,
						angmin,
						angmax,
						branchName);
			}
		}
		if (_isOutCSV == false) pw.println("];");
		
		pw.flush();
		pw.close();
		if (_isOutCSV == false) System.out.println("Matpower format based case has been created");
		else System.out.println("Branch data has been dumped into a csv file");
	}
	
	public float[][] getBusShunt() throws PsseModelException
	{
		int nBus = _model.getBuses().size();
		float[] busShuntG = new float[nBus];
		float[] busShuntB = new float[nBus];
		BusGroupElems busGroupElems = _model.getBusGroupElems();
		for(int i=0; i<nBus; i++)
		{
			int[] idxShunts = busGroupElems.getShuntIndex(i);
			if (idxShunts == null) continue;
			for(int j=0; j<idxShunts.length; j++)
			{
				int idxShunt = idxShunts[j];
				if(_model.getShunts().isInSvc(idxShunt))
				{
				    busShuntG[i] += _model.getShunts().getG(idxShunt);
					busShuntB[i] += _model.getShunts().getB(idxShunt);
				}
			}			
		}
		return new float[][] {busShuntG, busShuntB};
	}
	
	
	public static void main(String[] args) throws PsseModelException
	{
		DiaryXL diary = new DiaryXL();
		diary.initial();

		String uri = null;
		File poutdir = new File(System.getProperty("user.dir"));
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
				case "outdir":
					poutdir = new File(args[i++]);
					break;
			}
		}
		if (uri == null)
		{
			System.err.format("Usage: -uri model_uri "
					+ "[ --outdir output_directory (deft to $CWD ]\n");
			System.exit(1);
		}
		final File outdir = poutdir;
		if (!outdir.exists()) {outdir.mkdirs();}
		
		PsseModel model = PsseModel.Open(uri);
		model.setMinXMag(ParamInput.getLeastX());
		model.setDiary(diary);

		ConvtOpenPA1Matpower converter = new ConvtOpenPA1Matpower(model);
		converter.setIsOutCSV(true);
		converter.createMatpowerCase();
		
		System.out.println("Simulation is done here");
	}
	
}
