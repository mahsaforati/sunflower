/*
	Sunflower is a tool for extracting and representing category graph of words. The key idea is using different versions (languages) of Wikipedia to generate the category graph of the input.
	The project was developed by Marek Lipczak, Mahsa Forati and Arash Koushkestani.
	To view a demo and use web-services please visit: http://ws.cs.dal.ca:8080/sunflower/

	This software is released under Apache License 2.0. For academic use, please address the following paper:
	Tulip: lightweight entity recognition and disambiguation using wikipedia-based topic centroids

	Contributors = Marek Lipczak, Dr. Evangelos Milios, Mahsa Forati, Arash Koushkestani
	ORGANIZATION = Dalhousie University
	YEAR = 2016

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at
	http://www.apache.org/licenses/LICENSE-2.0
*/

package org.sunflower.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.zip.GZIPInputStream;

import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContext;


public class IOUtil
{

	public static BufferedReader openReadFile(String pathIn)
	{
		try
		{
			if(pathIn.endsWith(".bz2"))
			{
				return getBufferedReaderForBZ2File(pathIn);
			}
			if(pathIn.endsWith(".gz"))
			{
				return getBufferedReaderForGzFile(pathIn);
			}
			else
			{
				return openReadFileUTF8(pathIn);
			}
		}
		catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (UnsupportedEncodingException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (CompressorException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

	public static BufferedReader getBufferedReaderForBZ2File(String fileIn) throws CompressorException, IOException
	{
	    FileInputStream fin = new FileInputStream(fileIn);
	    BZip2CompressorInputStream input = new BZip2CompressorInputStream(fin, true);
	    BufferedReader br2 = new BufferedReader(new InputStreamReader(input, "UTF8"));

	    return br2;
	}
	
	public static BufferedReader getBufferedReaderForGzFile(String fileIn) throws CompressorException, IOException
	{
		InputStream fileStream = new FileInputStream(fileIn);
		InputStream gzipStream = new GZIPInputStream(fileStream);
		Reader decoder = new InputStreamReader(gzipStream, "UTF-8");
		BufferedReader buffered = new BufferedReader(decoder);
		
		return buffered;
	}
	
	public static BufferedReader openReadFileUTF8(String pathIn) throws MalformedURLException {
		try
		{
			BufferedReader inFile = new BufferedReader(new InputStreamReader(new FileInputStream(new File(pathIn)), "UTF8"));
			return inFile;
		}
		catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (UnsupportedEncodingException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}
	
	public static PrintWriter openWriteFile(String pathOut)
	{
		try
		{
			return new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File(pathOut)),"UTF-8"));
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static boolean makeFolderIfNeeded(String path)
	{
		path = path+"aaa";
		String[] pathSplit = path.split("/");
		path = "";
		for(int i = 0; i < pathSplit.length-1; i++)
		{
			path += pathSplit[i]+"/";
		}
		
		File folder = new File(path);
		
		if(folder.exists())
		{
			return false;
		}
		else
		{
			if(!folder.mkdirs())
			{
				System.out.println("[ERROR] problem creating folders");
			}
			return true;
		}
	}

	public static InputStream getInputStream(String pathIn)
	{
		try
		{
			if(pathIn.endsWith(".bz2"))
			{
				FileInputStream fin = new FileInputStream(pathIn);
				return new BZip2CompressorInputStream(fin, true);
			}
			if(pathIn.endsWith(".gz"))
			{
				InputStream fileStream = new FileInputStream(pathIn);
				return new GZIPInputStream(fileStream);
			}
			else
			{
				return new FileInputStream(pathIn);
			}
		}
		catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
