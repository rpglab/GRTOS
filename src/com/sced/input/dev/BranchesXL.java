package com.sced.input.dev;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import com.sced.input.ReadModelDataXL;
import com.sced.util.AuxMethodXL;

/**
 * 
 * @author Xingpeng Li, xplipower@gmail.com 
 *
 */
public class BranchesXL {

	ReadModelDataXL _model;
	BusesXL _buses;
	int _size;

	int[] _idxLine;
	int[] _idxXfm;
	int[] _idxPhaseShifter;
	
	// base value from input file
	int[] _frmBusIdx;    // index of from bus 
	int[] _toBusIdx;     // index of to bus 
	
//	int[] _frmBusNum;    // frm bus number before reindex
//	int[] _toBusNum;     // to bus number before reindex
	
	String[] _ckt;       // circuit ID
	int[] _type;         // 1 for line, 2 for xfm, 3 for phase shifter
	int[] _stat;         // branch status: 1 for in service, 0 for out of service 

	double[] _r;
	double[] _x;
	double[] _bpi;
	
	double[] _tap;
	double[] _angle;     // phase shifter angle, 0 for non-phase shifter branch 
	
	double[] _rateA;     // in MVA
	double[] _rateB;
	double[] _rateC;
	
	double[] _pkInit;    // used for DC power flow model
	
	double[] _pfrm;
	double[] _pto;
	double[] _qfrm;
	double[] _qto;
	
	public BranchesXL(ReadModelDataXL model) {_model = model; _buses = _model.getBuses();}
	
	public int size() {return _size;}
	public int getFrmBusIdx(int i) {return _frmBusIdx[i];}
	public int getToBusIdx(int i) {return _toBusIdx[i];}
	public String getCKT(int i) {return _ckt[i];}
	public int getBrcType(int i) {return _type[i];}
	public int getStat(int i) {return _stat[i];}
	
	public double getR(int i) {return _r[i];}
	public double getX(int i) {return _x[i];}
	public double getBpi(int i) {return _bpi[i];}
	public double getTap(int i) {return _tap[i];}
	public double getAngle(int i) {return _angle[i];}
	
	public double getRateA(int i) {return _rateA[i];}
	public double getRateB(int i) {return _rateB[i];}
	public double getRateC(int i) {return _rateC[i];}
	
	public double getPkInit(int i) {return _pkInit[i];}

	public double getPfrm(int i) {return _pfrm[i];}
	public double getPto(int i) {return _pto[i];}
	public double getQfrm(int i) {return _qfrm[i];}
	public double getQto(int i) {return _qto[i];}
	
	public int sizeLine() {analyzeLineType(); return _idxLine.length;}
	public int sizeXfm() {analyzeLineType(); return (_idxXfm == null) ? 0 : _idxXfm.length;}
	public int sizePhaseShifter() {analyzeLineType(); return (_idxPhaseShifter == null) ? 0 : _idxPhaseShifter.length;}
	
	public int[] getIdxLine() {analyzeLineType(); return _idxLine;}
	public int[] getIdxXfm() {analyzeLineType(); return _idxXfm;}
	public int[] getIdxPhaseShifter() {analyzeLineType(); return _idxPhaseShifter;}
	
	private void analyzeLineType() {
		if (_idxLine != null) return;
		ArrayList<Integer> idxLine = new ArrayList<Integer>();
		ArrayList<Integer> idxXfm = new ArrayList<Integer>();
		ArrayList<Integer> idxPhaseShifter = new ArrayList<Integer>();
		if (_type != null) {
			for(int i=0; i<_size; i++) {
				if (_type[i] == 1) idxLine.add(i);
				else if (_type[i] == 2) idxXfm.add(i);
				else if (_type[i] == 3) idxPhaseShifter.add(i);
			}
		} else {
			if (_tap == null) {
				if (_angle == null) {
					for (int i=0; i<_size; i++)
						idxLine.add(i);
				} else {
					for (int i=0; i<_size; i++) {
						if (_angle[i] != 0.0) idxPhaseShifter.add(i);
						else idxLine.add(i);
					}
				}
			} else {
				if (_angle == null) {
					for (int i=0; i<_size; i++) {
						if (_tap[i] != 0.0) idxXfm.add(i);
						else idxLine.add(i);
					}
				} else {
					for (int i=0; i<_size; i++) {
						if (_angle[i] != 0.0) idxPhaseShifter.add(i);
						if (_tap[i] != 0.0) idxXfm.add(i);
						else idxLine.add(i);
					}
				}
			}
		}
		_idxLine = AuxMethodXL.convtArrayListToInt(idxLine);
		_idxXfm = AuxMethodXL.convtArrayListToInt(idxXfm);
		_idxPhaseShifter = AuxMethodXL.convtArrayListToInt(idxPhaseShifter);
	}
	
	public void readData(File file) throws IOException
	{
		ArrayList<Integer> frmBusIdx = new ArrayList<Integer>();   // index of associate from bus
		ArrayList<Integer> toBusIdx = new ArrayList<Integer>();    // index of associate to bus

//		ArrayList<Integer> frmBusNum = new ArrayList<Integer>();
//		ArrayList<Integer> toBusNum = new ArrayList<Integer>();
		ArrayList<String> ckt = new ArrayList<String>();
//		ArrayList<Integer> type = new ArrayList<Integer>();
		ArrayList<Integer> stat = new ArrayList<Integer>();

		ArrayList<Double> r = new ArrayList<Double>();
		ArrayList<Double> x = new ArrayList<Double>();
//		ArrayList<Double> bpi = new ArrayList<Double>();
		
		ArrayList<Double> rateA = new ArrayList<Double>();
		ArrayList<Double> rateB = new ArrayList<Double>();
		ArrayList<Double> rateC = new ArrayList<Double>();
//		ArrayList<Double> tap = new ArrayList<Double>();
		ArrayList<Double> angle = new ArrayList<Double>();

		ArrayList<Double> pkInit = new ArrayList<Double>();

//		ArrayList<Double> pfrm = new ArrayList<Double>();
//		ArrayList<Double> pto = new ArrayList<Double>();
//		ArrayList<Double> qfrm = new ArrayList<Double>();
//		ArrayList<Double> qto = new ArrayList<Double>();

		_size = 0;
		System.out.println("Reading branches data ...");
		_model.getDiary().hotLine("Start reading branches data ...");
		Scanner reader = new Scanner(file);
		if (_model.hasHeading() == true) reader.nextLine();
		while (reader.hasNextLine() && reader.hasNext()) {
			_size++;
					
			int frmBusNumber = reader.nextInt();
			int toBusNumber = reader.nextInt();
			frmBusIdx.add(_buses.getIdx(frmBusNumber));
			toBusIdx.add(_buses.getIdx(toBusNumber));
//			frmBusNum.add(frmBusNumber);
//			toBusNum.add(toBusNumber);
			
			ckt.add(reader.next());
//			type.add(reader.nextInt());
			stat.add(reader.nextInt());
			
			r.add(reader.nextDouble());
			x.add(reader.nextDouble());
//			bpi.add(reader.nextDouble());
//			tap.add(reader.nextDouble());
			angle.add(reader.nextDouble());

			pkInit.add(reader.nextDouble());

			rateA.add(reader.nextDouble());
			rateB.add(reader.nextDouble());
			rateC.add(reader.nextDouble());

//			pfrm.add(reader.nextDouble());
//			pto.add(reader.nextDouble());
//			qfrm.add(reader.nextDouble());
//			qto.add(reader.nextDouble());
		}
		reader.close();
		
		/* save data into member variables */
		_frmBusIdx = AuxMethodXL.convtArrayListToInt(frmBusIdx);
		_toBusIdx = AuxMethodXL.convtArrayListToInt(toBusIdx);
//		_frmBusNum = AuxMethod.convtArrayListToInt(frmBusNum);
//		_toBusNum = AuxMethod.convtArrayListToInt(toBusNum);
		
		_ckt = AuxMethodXL.convtArrayListToStr(ckt);
//		_type = AuxMethod.convtArrayListToInt(type);
		_stat = AuxMethodXL.convtArrayListToInt(stat);
		
		_r = AuxMethodXL.convtArrayListToDouble(r);
		_x = AuxMethodXL.convtArrayListToDouble(x);
//		_bpi = AuxMethod.convtArrayListToDouble(bpi);
//		_tap = AuxMethod.convtArrayListToDouble(tap);
		_angle = AuxMethodXL.convtArrayListToDouble(angle);

		_rateA = AuxMethodXL.convtArrayListToDouble(rateA);
		_rateB = AuxMethodXL.convtArrayListToDouble(rateB);
		_rateC = AuxMethodXL.convtArrayListToDouble(rateC);

		_pkInit = AuxMethodXL.convtArrayListToDouble(pkInit);
		
//		_pfrm = AuxMethod.convtArrayListToDouble(pfrm);
//		_pto = AuxMethod.convtArrayListToDouble(pto);
//		_qfrm = AuxMethod.convtArrayListToDouble(qfrm);
//		_qto = AuxMethod.convtArrayListToDouble(qto);
		
		System.out.println("   Finish reading branch data");
		_model.getDiary().hotLine("Finish reading branches data ...");
	}


}
