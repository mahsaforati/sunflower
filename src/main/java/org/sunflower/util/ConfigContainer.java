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

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

public class ConfigContainer
{
	HashMap<String, String> conf;
	
	public ConfigContainer(String pathIn, ConfigContainer config)
	{
        conf = new HashMap<String, String>();
		BufferedReader inFile = IOUtil.openReadFile(pathIn);
		
		if(inFile != null)
		{
			String line;
			try
			{
				while((line = inFile.readLine()) != null)
				{
					if(line.startsWith("#")) continue;
					String[] split = argValSplit(line);
					if(split == null)
					{
						System.out.println("[WARN] Skipping unparsable element: "+line);
					}
					conf.put(split[0], split[1]);
				}
				
				inFile.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			System.err.println("Error while reading the config file = "+pathIn);
		}
		
		if(config != null)
		{
			String overwriteOnInit = config.getValue("overwriteOnInit");
			if(overwriteOnInit != null && overwriteOnInit.equalsIgnoreCase("true"))
			{
				for(Entry<String, String> entry : config.conf.entrySet())
				{
					conf.put(entry.getKey(), entry.getValue());
				}
			}
		}
	}
	
	public ConfigContainer(String[] args)
	{
		conf = new HashMap<String, String>();

        for(String arg : args)
		{
            String[] split = argValSplit(arg);
            if(split == null)
			{
				System.out.println("[WARN] Skipping unparsable element: "+arg);
			}
			conf.put(split[0], split[1]);
		}
	}
	
	private String[] argValSplit(String line)
	{
		int splitValue = line.indexOf('=');
		if(splitValue < 0) return null;
		
		String arg = line.substring(0, splitValue).trim();
		String val = line.substring(splitValue+1, line.length()).trim();
		
		if(arg.startsWith("-"))  arg = arg.substring(1, arg.length());
		if(val.startsWith("\"") && val.endsWith("\""))  val = val.substring(1, val.length()-1);
		
		String[] res = new String[2];
		res[0] = arg;
		res[1] = val;
		
		return res;
	}
	
	public String getValue(String arg)
	{
		return conf.get(arg);
	}

	public void setValue(String arg, String val)
	{
		conf.put(arg, val);
	}
}
