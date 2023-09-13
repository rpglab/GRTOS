package com.casced;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import com.powerdata.openpa.psse.PsseModel;
import com.powerdata.openpa.psse.PsseModelException;
import com.powerdata.openpa.psse.powerflow.VoltageSource;
import com.rtca_cts.param.ParamTS;
import com.rtca_cts.transmissionswitching.CorrectiveTransmSwit;
import com.sced.model.BrcCtgcyListXL;
import com.sced.model.param.ReadParams;
import com.utilxl.array.AuxArrayXL;
import com.utilxl.log.DiaryXL;
import com.utilxl.log.LogTypeXL;


/**
 * To be improved: currently, it cannot handle >=2 violations for one critical ctgcy.
 * 
 * @author xingpeng
 *
 */
public class SCEDwCTS_CA extends SCEDwCA {

	
	HashMap<Integer, dataStructure> _preSCED_CTS;
	
	public SCEDwCTS_CA(DiaryXL diary, PsseModel modelPA) {
		super(diary, modelPA);
	}

	public void updateEmergencyLimit() throws PsseModelException, IOException
	{
		runCTSPreSCED();
		
	}

	public void runCTSPreSCED() throws PsseModelException, IOException
	{
		BrcCtgcyListXL brcCtgcy = _scedModel.getKeyBrcList();
		int size = brcCtgcy.size();
		int[] tmpCriticalCtgcyListPreSCED = new int[size];
		int count = 0;
		for (int c=0; c<size; c++) {
			if (brcCtgcy.isCtgcyActive(c) == false) continue;
			tmpCriticalCtgcyListPreSCED[count++] = brcCtgcy.getCtgcyBrcIdx(c);
		}
		int[] criticalCtgcyListPreSCED = new int[count];
		System.arraycopy(tmpCriticalCtgcyListPreSCED, 0, criticalCtgcyListPreSCED, 0, count);
		
		ParamTS.setTSOption(1);
		CorrectiveTransmSwit engineCTS = new CorrectiveTransmSwit(_modelPA);
		engineCTS.setOutputAllTS(true, engineCTS.getPathAllTS() + _modelPA.getFileName() + "/");     
		engineCTS.setContToCheckTransm(criticalCtgcyListPreSCED);
		long t_Start = System.nanoTime();
		engineCTS.initial(t_Start, _modelPA.getURI(), VoltageSource.LastSolved, _vmBasePf, _vaBasePf);
		
		_preSCED_CTS = new HashMap<Integer, dataStructure>();
		int numTS = ParamTS.getReportBestNumTS();
		for (int i=0; i<count; i++)
		{
			engineCTS.initPreTS_Setting();
			float[][] sumBrcVioTS = null;
			//float[][] sumBrcVioTS = engineCTS.runTransmSwitTransm(i);
			int[] idxBestTS = AuxArrayXL.getIdxOfMaxElems(sumBrcVioTS[1], numTS);
			float[] vioAbsReduction = new float[numTS];
			float[] improvePct = new float[numTS];
			for (int j=0; j<numTS; j++)
			{
				int idx = idxBestTS[j];
				vioAbsReduction[j] = sumBrcVioTS[0][idx];
				improvePct[j] = sumBrcVioTS[1][idx];
			}
			dataStructure tmp = new dataStructure();
			tmp.setIdxBestTS(idxBestTS);
			tmp.setVioAbsReduction(vioAbsReduction);
			tmp.setImprovePct(improvePct);
			_preSCED_CTS.put(criticalCtgcyListPreSCED[i], tmp);
		}
		float timeSol = (System.nanoTime() - t_Start)/1e9f;
		engineCTS.analyzeData();
		engineCTS.outputResults(timeSol);
	}
	
	public class dataStructure {
		int[] _idxBestTS;
		float[] _vioAbsReduction;
		float[] _improvePct;
		
		void setIdxBestTS(int[] idxBestTS) {_idxBestTS = idxBestTS;}
		void setVioAbsReduction(float[] vioAbsReduction) {_vioAbsReduction = vioAbsReduction;}
		void setImprovePct(float[] improvePct) {_improvePct = improvePct;}
		
	}
	
	
	public static void main(String[] args) throws Exception
	{
		/** Log file creation */
		DiaryXL diary = new DiaryXL();
		diary.initial();
		
		/** Load parameters in configure file */
		ReadParams loadParam = new ReadParams();
		loadParam.launch();
		diary.hotLine("Configure file '"+loadParam.getConfigureFileName()+"' has been loaded");
		
		/** Parse program arguments */
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
		model.setDiary(diary);
		diary.hotLine(LogTypeXL.MileStone, "OpenPA model raw data is loaded");
		
		/** SCED with CA simulation */
		SCEDwCA scedCA = new SCEDwCTS_CA(diary, model);
		scedCA.runInitPfCA(vstart);
		scedCA.prepare4SCED();
		scedCA.runSCEDwCA();
		//scedCA._scedModel.getSenstvtFactor().getFormPTDF().dump();

		/** Program ends here */
		diary.done();
		System.out.println("Program ends here.");
	}
	
	
}
