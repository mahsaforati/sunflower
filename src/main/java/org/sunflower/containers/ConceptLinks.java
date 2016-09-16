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

package org.sunflower.containers;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Map.Entry;

public class ConceptLinks
{
	String name;
	int sourceCount;
	List<CategoryScore> categories;
	
	public ConceptLinks()
	{
		// TODO Auto-generated constructor stub
	}
	
	public ConceptLinks(String conceptStr, boolean doCleaning)
	{
		StringTokenizer tokenizer = new StringTokenizer(conceptStr);
		name = tokenizer.nextToken();
		sourceCount = new Integer(tokenizer.nextToken());
		
		categories = new ArrayList<CategoryScore>();
		while(tokenizer.hasMoreTokens())
		{
			CategoryScore category = new CategoryScore(tokenizer.nextToken());
			if(!doCleaning || isAccepted(category, name))
			{
				categories.add(category);
			}
		}
	}

	private boolean isAccepted(CategoryScore category, String conceptName)
	{
		String name = category.getName();
		
		//if(name.equals("Category:"+conceptName)) return false;
		
		if(name.matches(".*\\d+_births") || name.matches(".*\\d+_deaths"))
		{
			return false;
		}
		
		return true;
	}

	public void addCategory(CategoryScore cat)
	{
		if(categories == null)
		{
			categories = new ArrayList<CategoryScore>();
		}
		
		categories.add(cat);
	}
	
	public String getName()
	{
		return name;
	}
	
	public int getSourceCount()
	{
		return sourceCount;
	}
	
	public List<CategoryScore> getCategories()
	{
		return categories;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public void setSourceCount(int sourceCount)
	{
		this.sourceCount = sourceCount;
	}

	public void setCategories(List<CategoryScore> categories)
	{
		this.categories = categories;
	}

	public void println(PrintWriter outFile)
	{
		outFile.print(name+"\t"+sourceCount);
		
		for(CategoryScore entry : categories)
		{
			outFile.print("\t"+entry.getName()+":"+entry.getScore());
		}
		
		outFile.println("");
	}
}
