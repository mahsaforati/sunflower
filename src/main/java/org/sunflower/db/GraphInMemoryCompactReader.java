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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sunflower.containers.CategoryScore;
import org.sunflower.containers.ConceptLinks;
import org.sunflower.containers.TermFreq;
import org.sunflower.containers.TermFreqCompF;
import org.sunflower.util.IOUtil;

public class GraphInMemoryCompactReader extends GraphReader
{
	private Map<String, Integer> nameToIndex;
	private Map<Integer, String> indexToName;
	private int[][] indexes;
	private float[][] scores;
	
	public GraphInMemoryCompactReader()
	{
		nameToIndex = new HashMap<String, Integer>();
	}
	
	public GraphInMemoryCompactReader(String pathIn)
	{
		nameToIndex = new HashMap<String, Integer>();
		indexToName = new HashMap<Integer, String>();
		BufferedReader inFile = IOUtil.openReadFile(pathIn + ".index.dat");
		String line = null;
		
		try
		{
			int counter = 0;
			System.out.println(" Reading index "+pathIn);
			while((line = inFile.readLine()) != null)
			{
				String[] split = line.split("\t");
				Integer index = new Integer(split[0]);
				String name = split[1];
				nameToIndex.put(name, index);
				indexToName.put(index, name);
				
				counter++;
				if(counter%1000000 == 0) System.out.println("^");
				else if(counter%100000 == 0) System.out.print("|");
				else if(counter%10000 == 0) System.out.print(".");
			}
			System.out.println(" <-- "+counter);
			
			inFile.close();
			
			System.out.println(" Reading graph "+pathIn);
			inFile = IOUtil.openReadFile(pathIn + ".compact.dat");
			indexes = new int[indexToName.size()][];
			scores = new float[indexToName.size()][];
			counter = 0;
			while((line = inFile.readLine()) != null)
			{
				String[] split = line.split("\t");
				
				indexes[counter] = new int[split.length - 1];
				scores[counter] = new float[split.length - 1];
				for(int i = 0; i < indexes[counter].length; i++)
				{
					String[] split2 = split[i+1].split(":");
					indexes[counter][i] = new Integer(split2[0]);
					scores[counter][i] = new Float(split2[1]);
				}
				
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
	
	@Override
	public ConceptLinks getLinks(String concept)
	{
		Integer index = nameToIndex.get(concept);
		if(index == null) return null;
		
		ConceptLinks links = new ConceptLinks();
		links.setName(concept);
		links.setSourceCount(1);
		if(indexes[index].length > 0)
		{
			for(int i = 0; i < indexes[index].length; i++)
			{
				CategoryScore catScore = new CategoryScore(indexToName.get(indexes[index][i]), scores[index][i]);
				links.addCategory(catScore);
			}
		}
		else
		{
			links.setCategories(new ArrayList<CategoryScore>());
		}
				
		
		return links;
	}

	@Override
	public List<String> getPartiallyMatchingConcepts(String query)
	{
		query = query.toLowerCase();
		if(query.length() < 4) return new ArrayList<String>();
		
		List<TermFreq> matches = new ArrayList<TermFreq>();
		for(String key : nameToIndex.keySet())
		{
			if(key.toLowerCase().contains(query)) matches.add(new TermFreq(key, Math.log(nameToIndex.size() - nameToIndex.get(key))));
		}
		
		Collections.sort(matches, new TermFreqCompF());
		
		List<String> result = new ArrayList<String>();
		for(int i = 0; i < 20 && i < matches.size(); i++) result.add(matches.get(i).getTerm());
		
		return result;
	}

}
