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

package org.sunflower.db;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.sunflower.util.IOUtil;

public class RulesInMemoryReader extends RulesReader
{
	Set<String> rules;
	
	public RulesInMemoryReader()
	{
		rules = new HashSet<String>();
	}
	
	public RulesInMemoryReader(String pathIn)
	{
		rules = new HashSet<String>();
		BufferedReader inFile = IOUtil.openReadFile(pathIn);
		String line = null;
		
		try
		{
			int counter = 0;
			while((line = inFile.readLine()) != null)
			{
				String[] split = line.split("\t");
				String rule = split[0]+"\t"+split[1]+"\t"+split[2]+"\t"+split[3];
				rules.add(rule);
				
				if(counter >= 1000000) break;
				
				counter++;
				if(counter%1000000 == 0) System.out.println("^");
				else if(counter%100000 == 0) System.out.print("|");
				else if(counter%10000 == 0) System.out.print(".");
			}
			System.out.println(" <-- "+counter);
			
			inFile.close();
		}
		catch (IOException e) {e.printStackTrace();}
	}
	
	public boolean hasRule(String nameA, String nameB, String mentionA, String mentionB)
	{
		if(nameA.compareTo(nameB) < 0)
		{
			return rules.contains(nameA+"\t"+nameB+"\t"+mentionA+"\t"+mentionB);
		}
		else
		{
			return rules.contains(nameB+"\t"+nameA+"\t"+mentionB+"\t"+mentionA);
		}
	}
}
