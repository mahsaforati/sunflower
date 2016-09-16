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

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.sunflower.containers.ConceptLinks;
import org.sunflower.containers.TermFreq;
import org.sunflower.containers.TermFreqCompF;
import org.sunflower.util.IOUtil;

public class GraphInMemoryReader extends GraphReader
{
	private Map<String, ConceptLinks> links;
	
	public GraphInMemoryReader()
	{
		links = new HashMap<String, ConceptLinks>();
	}
	
	public GraphInMemoryReader(String pathIn)
	{
		links = new HashMap<String, ConceptLinks>();
		BufferedReader inFile = IOUtil.openReadFile(pathIn);
		String line = null;
		
		try
		{
            // TODO ************************
            int counter = 0;
			while((line = inFile.readLine()) != null)
			{
				ConceptLinks concept = new ConceptLinks(line, true);
				links.put(concept.getName(), concept);
				
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
		return links.get(concept);
	}

	@Override
	public List<String> getPartiallyMatchingConcepts(String query)
	{
		query = query.toLowerCase();
		if(query.length() < 3) return new ArrayList<String>();
		
		List<TermFreq> matches = new ArrayList<TermFreq>();
		for(String key : links.keySet())
		{
			if(key.toLowerCase().contains(query)) matches.add(new TermFreq(key, links.get(key).getSourceCount()));
		}
		
		Collections.sort(matches, new TermFreqCompF());
		
		List<String> result = new ArrayList<String>();
		for(int i = 0; i < 20 && i < matches.size(); i++) result.add(matches.get(i).getTerm());
		
		return result;
	}

}
