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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GraphLink
{
	private static String[] wikipediaByNodes = {"by_nationality", "by_country"};
	
	private String name;
	private double score;
	private String type;

	private transient Map<String, String> relationsMap;
	
	private transient GraphLink next;
	
	public GraphLink(String name, double score, String type)
	{
		this.name = name;
		this.score = score;
		this.type = type;
		
		relationsMap = new HashMap<String, String>();
	}

	public GraphLink getNext()
	{
		return next;
	}

	public void setNext(GraphLink next)
	{
		this.next = next;
	}

	public String getPathAsString()
	{
		String out = "";
		
		GraphLink node = this;
		
		int counter = 0;
		do
		{
			out += node.name+"("+node.score+") -> ";
			counter++;
			if(counter > 10) break;
			node = node.next;
		}
		while(node != null);
		
		return out;
	}

	public void getLinksAsString( List<String[]> list, String root)
	{
		String[] link = new String[3];
		link[2] = ""+score;
		link[1] = name;
		
		if(next != null)
		{
			link[0] = next.name;
			list.add(link);
			next.getLinksAsString(list, root);
		}
		else if(root != null)
		{
			link[0] = root;
			list.add(link);
		}
	}

	public Map<String, String> getRelations()
	{
		return relationsMap;
	}
	
	public void setRelation(String relationType, String relationName)
	{
		relationsMap.put(relationType, relationName);
	}

	public boolean isPathOfSubcategories()
	{
		String nameNormalized = name.replace("Category:", "").toLowerCase();
		GraphLink pointer = next;
		while(pointer != null)
		{
			String pointerNormalized = pointer.name.replace("Category:", "").toLowerCase();
			if(!pointerNormalized.contains(nameNormalized))
			{
				return false;
			}
			
			pointer = pointer.next;
		}
		return true;
	}

	public void removeArtificialNodes()
	{
		boolean didCleanup = false;
		
		if(next != null)
		{
			for(String pattern : wikipediaByNodes)
			{
				if(next.name.endsWith(pattern))
				{
					next = next.next;
					removeArtificialNodes();
					didCleanup = true;
					break;
				}
			}
		}
		if(!didCleanup && next != null)
		{
			next.removeArtificialNodes();
		}
	}

	public double getTotalScore()
	{
		double score = 1;
		GraphLink current = this;
		
		while(current != null)
		{
			score *= current.score;
			current = current.next;
		}
		
		return score;
	}
	
	public List<GraphLink> toList()
	{
		List<GraphLink> list = new ArrayList<GraphLink>();
		GraphLink current = this;
		
		while(current != null)
		{
			list.add(current);
			current = current.next;
		}
		
		return list;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
