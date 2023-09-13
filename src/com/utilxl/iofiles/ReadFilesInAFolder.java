package com.utilxl.iofiles;

import java.io.File;
import java.util.Arrays;

import com.utilxl.string.AuxStringXL;


/**
 * 
 * Initialized on Aug.2014
 * 
 * @author Xingpeng.Li (xplipower@gmail.com)
 *
 */
public class ReadFilesInAFolder {
	
	String _path;
	File[] _fileList = null;
	File[] _folderList;
	
	String[] _rawFileNames = null;

	public ReadFilesInAFolder(String path, boolean isAbsPath)
	{
		initial(path, isAbsPath);
	}
	
	/** 
	 * @param path : path, e.g. "C://MPJWorkspace//debug_20140214//dataToRead//".
	 *               or "M:\\MPJWorkspace\\debug_20140214\\dataToRead".
	 * @param absPath : absolute path if true; relative path if false.
	 */
	void initial(String path, boolean isAbsPath)
	{
		if (isAbsPath == true) _path = AuxStringXL.replaceElem(path, '\\', '/');
		else
		{
			String rootDr = (System.getProperty("user.dir"));
			rootDr = AuxStringXL.replaceElem(rootDr, '\\', '/');
			rootDr = AuxStringXL.endWith(rootDr, "/");
			if (path == null) _path = rootDr;
			else
			{
				rootDr = rootDr + path;
			    for (int i=0; i<rootDr.length(); i++)
			    {
			    	if (rootDr.charAt(i) == '\\')
			    		rootDr = rootDr.substring(0,i)+'/'+rootDr.substring(i+1);
			    }
			}
			_path = rootDr;
		}
		_path = AuxStringXL.endWith(_path, "/");
	}
	
	public void readFileNames()
	{
		File folder = new File(_path);
		File[] fileList = folder.listFiles();
		_fileList = new File[fileList.length];
		_folderList = new File[fileList.length];
		int idx = 0, idxFolders = 0; 
		for (int i=0; i<fileList.length; i++)
		{
			if (fileList[i].isFile()) {	_fileList[idx++] = fileList[i];}
			else if (fileList[i].isDirectory()) {_folderList[idxFolders++] = fileList[i];}
			else System.err.println("Some wrong happens when read file/folder names from a folder.");
		}
		File[] fileListTmp = new File[idx]; 
		System.arraycopy(_fileList, 0, fileListTmp, 0, idx);
		_fileList = fileListTmp;
		
		File[] folderListTmp = new File[idxFolders]; 
		System.arraycopy(_folderList, 0, folderListTmp, 0, idxFolders);
		_folderList = folderListTmp;
	}
	
	public boolean isRawFile(int i)
	{
		boolean isRawFile = false;
		if (_fileList[i].getName().endsWith(".raw")) isRawFile = true;
		return isRawFile;
	}
	
	public String getPath() {return _path;}

	public File[] getFiles() { return _fileList;}
	public File getFile(int i) { return _fileList[i];}
	public String getFileName(int i) { return _fileList[i].getName();}
	public int sizeFiles() {return (_fileList == null) ? 0 : _fileList.length;}
	public String[] getFileNames()
	{
		String[] fileNames = new String[_fileList.length];
		for (int i=0; i<_fileList.length; i++)
			fileNames[i] = getFileName(i);
		return fileNames;		
	}
	
	public String[] getAllRawFileNames()
	{
		if (_rawFileNames != null) return _rawFileNames;
		String[] rawFileNames = new String[_fileList.length];
		int  numRawFiles = 0;
		for (int i=0; i<_fileList.length; i++)
		{
			if (isRawFile(i) == true)
				rawFileNames[numRawFiles++] = getFileName(i);
		}
		_rawFileNames = Arrays.copyOf(rawFileNames, numRawFiles);
		return _rawFileNames;
	}
	
	public File[] getFolders() { return _folderList;}
	public File getFolder(int i) { return _folderList[i];}
	public String getFolderName(int i) {return _folderList[i].getName();}
	public int sizeFolders() {return (_folderList == null) ? 0 : _folderList.length;}
	public String[] getFolderNames()
	{
		String[] folderNames = new String[_folderList.length];
		for (int i=0; i<_folderList.length; i++)
			folderNames[i] = getFolderName(i);
		return folderNames;
	}
	
}

