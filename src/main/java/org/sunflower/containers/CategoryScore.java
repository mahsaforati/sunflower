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

public class CategoryScore
{
	private String name;
	private double score;
	private String detailedType;
	private String generalType;
	
	public CategoryScore(String catStr)
	{
		int breakPoint = catStr.lastIndexOf(":");
		name = catStr.substring(0, breakPoint);
		score = new Double(catStr.substring(breakPoint+1));
	}
	
	public CategoryScore(String name, double score)
	{
		this.name = name;
		this.score = score;
	}

	public CategoryScore()
	{
		// TODO Auto-generated constructor stub
	}

	public String getName()
	{
		return name;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	public double getScore()
	{
		return score;
	}
	
	public void setScore(double score)
	{
		this.score = score;
	}

	public String getDetailedType() {
		return detailedType;
	}

	public void setDetailedType(String detailedType) {
		this.detailedType = detailedType;
	}

	public String getGeneralType() {
		return generalType;
	}

	public void setGeneralType(String generalType) {
		this.generalType = generalType;
	}
}
