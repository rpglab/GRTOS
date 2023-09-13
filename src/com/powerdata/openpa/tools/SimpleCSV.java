package com.powerdata.openpa.tools;

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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Utility to easily read and write CSV files.
 * 
 * This requires that the first row in the file is the "header" and not
 * data.
 * 
 * A CSV model can be created by using the default constructor, then 
 * setting a header using setHeader().  After that call addRow() for
 * each new row.
 * 
 * @author marck
 */
public class SimpleCSV
{
	/** Array of column names */
	String _colNames[] = null;
	/** Array of columns */
	ArrayList<ArrayList<String>> _cols = new ArrayList<ArrayList<String>>();
	/** Array of columns by name */
	HashMap<String,ArrayList<String>> _colsByName = new HashMap<String,ArrayList<String>>();
	/** Number of rows */
	int _rowCount = 0;

	public SimpleCSV(){}
	public SimpleCSV(InputStream in) throws IOException
	{
		load(in);
	}
	public SimpleCSV(String filename) throws IOException
	{
		load(filename);
	}
	public SimpleCSV(File file) throws IOException
	{
		load(file);
	}
	public String[] getColumnNames() { return _colNames; }
	public int getRowCount() { return _rowCount; }
	public int getColCount() { return (_colNames != null)?_colNames.length:0; }
	//
	//	Get values based on column offset
	//
	public String get(int col, int row)
	{
		ArrayList<String> vals = _cols.get(col);
		return (vals == null)?"":vals.get(row);
	}
	public byte getByte(int col, int row) { return Byte.parseByte(get(col,row)); }
	public short getShort(int col, int row) { return Short.parseShort(get(col,row)); }
	public int getInt(int col, int row) { return Integer.parseInt(get(col,row)); }
	public long getLong(int col, int row) { return Long.parseLong(get(col,row)); }
	public float getFloat(int col, int row) { return Float.parseFloat(get(col,row)); }
	public double getDouble(int col, int row) { return Double.parseDouble(get(col,row)); }
	//
	//	Get values based on column name
	//
	public String get(String col, int row)
	{
		ArrayList<String> vals = _colsByName.get(col);
		return (vals == null)?"":vals.get(row);
	}
	public byte getByte(String col, int row) { return Byte.parseByte(get(col,row)); }
	public short getShort(String col, int row) { return Short.parseShort(get(col,row)); }
	public int getInt(String col, int row) { return Integer.parseInt(get(col,row)); }
	public long getLong(String col, int row) { return Long.parseLong(get(col,row)); }
	public float getFloat(String col, int row) { return Float.parseFloat(get(col,row)); }
	public double getDouble(String col, int row) { return Double.parseDouble(get(col,row)); }
	//
	//	Get entire columns
	//
	public String[] get(int col)
	{
		ArrayList<String> vals = _cols.get(col);
		return (vals == null)?null:vals.toArray(new String[0]);
	}
	public String[] get(String col)
	{
		ArrayList<String> vals = _colsByName.get(col);
		return (vals == null)?null:vals.toArray(new String[0]);
	}
	public boolean hasCol(String col) { return (_colsByName.get(col) != null); }
	public float[] getFloats(int col) { return getFloats(get(col)); }
	public float[] getFloats(String col) { return getFloats(get(col)); }
	public float[] getFloats(String svals[])
	{
		float fvals[] = null;
		if (svals != null)
		{
			fvals = new float[_rowCount];
			for(int i=0; i<_rowCount; i++)
			{
				fvals[i] = Float.parseFloat(svals[i]);
			}
		}
		return fvals;
	}
	public double[] getDoubles(int col) { return getDoubles(get(col)); }
	public double[] getDoubles(String col) { return getDoubles(get(col)); }
	public double[] getDoubles(String svals[])
	{
		double fvals[] = null;
		if (svals != null)
		{
			fvals = new double[_rowCount];
			for(int i=0; i<_rowCount; i++)
			{
				fvals[i] = Double.parseDouble(svals[i]);
			}
		}
		return fvals;
	}
	public int[] getInts(int col) { return getInts(get(col)); }
	public int[] getInts(String col) { return getInts(get(col)); }
	public int[] getInts(String svals[])
	{
		int fvals[] = null;
		if (svals != null)
		{
			fvals = new int[_rowCount];
			for(int i=0; i<_rowCount; i++)
			{
				fvals[i] = Integer.parseInt(svals[i]);
			}
		}
		return fvals;
	}
	//
	//	Set values based on column offset
	//
	public void set(int col, int row, String val) { _cols.get(col).set(row, val); }
	public void set(int col, int row, byte val) { set(col, row, String.valueOf(val)); }
	public void set(int col, int row, short val) { set(col, row, String.valueOf(val)); }
	public void set(int col, int row, int val) { set(col, row, String.valueOf(val)); }
	public void set(int col, int row, long val) { set(col, row, String.valueOf(val)); }
	public void set(int col, int row, float val) { set(col, row, String.valueOf(val)); }
	public void set(int col, int row, double val) { set(col, row, String.valueOf(val)); }
	//
	//	Set values based on the column name
	//
	public String set(String col, int row, String val) { return _colsByName.get(col).set(row,val); }
	public void set(String col, int row, byte val) { set(col, row, String.valueOf(val)); }
	public void set(String col, int row, short val) { set(col, row, String.valueOf(val)); }
	public void set(String col, int row, int val) { set(col, row, String.valueOf(val)); }
	public void set(String col, int row, long val) { set(col, row, String.valueOf(val)); }
	public void set(String col, int row, float val) { set(col, row, String.valueOf(val)); }
	public void set(String col, int row, double val) { set(col, row, String.valueOf(val)); }
	
	public void setHeader(String header[])
	{
		_cols.clear();
		_colsByName.clear();
		_rowCount = 0;
		_colNames = header;
		for(int i=0; i<_colNames.length; i++)
		{
			ArrayList<String> col = new ArrayList<String>();
			_cols.add(col);
			if (!_colsByName.containsKey(_colNames[i]))
			{
				_colsByName.put(_colNames[i], col);
			}
			else if (_colNames[i].length() > 0)
			{
				//System.out.println("WARNING: Duplicate column name ignored: "+_colNames[i]);
			}
		}
	}
	public int addRow() throws Exception
	{
		if (_colNames == null) throw new Exception("ERROR in addRow(): CSV Header not set.");
		for(ArrayList<String> col : _cols) { col.add(""); }
		++_rowCount;
		return _rowCount;
	}
	public int deleteRow(int row) throws Exception
	{
		if (_colNames == null) throw new Exception("ERROR in deleteRow(): CSV Header not set.");
		if (row >= _rowCount) throw new Exception("ERROR in deleteRow(): Row out of range.");
		for(ArrayList<String> col : _cols) { col.remove(row); }
		--_rowCount;
		return _rowCount;		
	}
	public void load(String filename) throws IOException
	{
		load(new File(filename));
	}
	public void load(File file) throws IOException
	{
		if (!file.exists()) return;
		FileInputStream in = new FileInputStream(file);
		load(in);
		in.close();
	}

	public void load(InputStream in) throws IOException
	{
		BufferedReader r = new BufferedReader(new InputStreamReader(in));
		_colNames = null;
		// assume a header
		String line = r.readLine();
		setHeader(new StringParse(line,",").setQuoteChar('\'').getTokens());
		int ccnt = _cols.size();
		// load the data
		while((line = r.readLine()) != null)
		{
			if (line.startsWith("#")) continue;
			String vals[] = new StringParse(line,",").setQuoteChar('\'').getTokens();
			for(int i=0; i<ccnt; i++)
			{
				_cols.get(i).add((i<vals.length)?vals[i].trim():"");
			}
			++_rowCount;
		}
	}
	static public boolean IsNumber(String s)
	{
		int len = s.length();
		if (len == 0) return false;
		for(int i=0; i<len; i++)
		{
			char c = s.charAt(i);
			switch(c)
			{
				case '+': if (len == 1) return false;
				case '-': if (len == 1) return false;
				case '.': if (len == 1) return false;
				case 'e': if (len == 1) return false;
				case 'E': if (len == 1) return false;
				case '0':
				case '1':
				case '2':
				case '3':
				case '4':
				case '5':
				case '6':
				case '7':
				case '8':
				case '9':
					break;
				default: return false;
			}
		}
		return true;
	}
	static public String Escape(String s)
	{
		// see if there is work to do
		if(s.indexOf('"') != -1 || s.indexOf('\\') != -1)
		{
			// replace with special characters
			s = s.replaceAll("'", "\001");
			s = s.replaceAll("\\", "\002");
			// now replace with escaped versions
			s = s.replaceAll("\001", "\\'");
			s = s.replaceAll("\002", "\\\\");
		}
		// if this is not a number then quote it
		if (!IsNumber(s))
		{
			s = "'"+s+"'";
		}
		return s;
	}
	public void save(String filename) throws IOException
	{
		OutputStream out = new FileOutputStream(filename);
		save(out);
		out.close();
	}
	public void save(OutputStream out)
	{
		PrintWriter w = new PrintWriter(new OutputStreamWriter(out));
		save(w);
	}
	public void save(PrintWriter w)
	{
		// write out the header
		for(int i=0; i<_colNames.length; i++)
		{
			if (i>0) w.print(",");
			w.print(_colNames[i]);
		}
		w.println();
		for(int r=0; r<_rowCount; r++)
		{
			for(int c=0; c<_colNames.length; c++)
			{
				if (c>0) w.print(",");
				w.print(Escape(get(c,r)));
			}
			w.println();
		}
		w.flush();
	}
	static public void main(String args[])
	{
		try
		{
			System.out.println(System.getProperty("user.dir"));
			SimpleCSV csv = new SimpleCSV("testdata/db/Branches.csv");
			csv.save(System.out);
		}
		catch(Exception e)
		{
			System.out.println("ERROR: "+e);
		}
	}
}
