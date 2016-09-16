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

package org.sunflower.containers.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class VisGraph
{
	private transient Map<String, Integer> nodeMap;
	private transient Set<String> linkSet;
	private List<VisNode> nodes;
	private List<VisLink> links;
	
	public VisGraph()
	{
		nodeMap = new HashMap<String, Integer>();
		nodes = new ArrayList<VisNode>();
		linkSet = new HashSet<String>();
		links = new ArrayList<VisLink>();
	}
	
	public int addNode(String id, String name, String type)
	{
		VisNode node = new VisNode(name);
		node.setGroup(type);
		int index = nodes.size();
		nodes.add(node);
		nodeMap.put(id, index);
		return index;
	}
	
	public void addLink(String src, String dst, double strength)
	{
		Integer srcIndex = nodeMap.get(src);
		if(srcIndex == null)
		{
			srcIndex = addNode(src, cleanName(src), "intermediate");
		}
		Integer dstIndex = nodeMap.get(dst);
		if(dstIndex == null)
		{
			dstIndex = addNode(dst, cleanName(dst), "intermediate");
		}
		
		String linkHash = ""+srcIndex+"+"+dstIndex;
		if(!linkSet.contains(linkHash))
		{
			links.add(new VisLink(srcIndex, dstIndex, strength));
			linkSet.add(linkHash);
		}
	}
	
	public List<VisNode> getNodes()
	{
		return nodes;
	}
	
	public List<VisLink> getLinks()
	{
		return links;
	}
	public void setNodes(List<VisNode> nodes)
	{
		this.nodes = nodes;
	}
	
	public void setLinks(List<VisLink> links)
	{
		this.links = links;
	}
	
	public static String cleanName(String name)
	{
		name = name.replace("_", " ");
		if(name.startsWith("Category:"))
		{
			name = "~"+name.substring("Category:".length());
		}
		
		if(name.length() > 20)
		{
			name = name.substring(0, 8)+"..."+name.substring(name.length()-8, name.length());
		}
		
		return name;
	}
}
