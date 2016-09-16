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

package org.sunflower.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.springframework.stereotype.Service;
import org.sunflower.containers.CategoryScore;
import org.sunflower.containers.CategoryScoreComparator;
import org.sunflower.containers.graph.GraphNameLink;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Service
public class RemoteProcessor
{
	public static final String CLUSTER_URL = "http://tomasz.cs.dal.ca:8080/GephiWeb/clustering.htm";
	
	private HttpClient httpclient;
	private Gson gson;
	
	public RemoteProcessor()
	{
		httpclient = new DefaultHttpClient();
		gson = new GsonBuilder().setPrettyPrinting().create();
		
		
	}

	public List<CategoryScore> getCategoryClusters(List<GraphNameLink> links)
	{
		List<CategoryScore> clusters;
		try
		{
			clusters = clusterLinks(links);
			return clusters;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return new ArrayList<CategoryScore>();
		}
		
	}

	private List<CategoryScore> clusterLinks(List<GraphNameLink> links) throws IOException
	{
		String jsonLinks = gson.toJson(links);
		
		HttpPost httpPost = new HttpPost(CLUSTER_URL);

		MultipartEntity reqEntity = new MultipartEntity();
		reqEntity.addPart("input", new StringBody(jsonLinks));
		reqEntity.addPart("res", new StringBody("1.0"));
		httpPost.setEntity(reqEntity);

		HttpResponse response = httpclient.execute(httpPost);
		
		if(response.getStatusLine().getStatusCode() != 200) System.out.println("RESPONSE STATUS: "+response.getStatusLine().getStatusCode());
		InputStream inputStream = response.getEntity().getContent();
		BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
		
		String line;
		String result = "";
		while((line = br.readLine()) != null)
		{
			result += line + "\n";
		}
		
		JsonObject resJson = new JsonParser().parse(result).getAsJsonObject();
		JsonArray elements = resJson.get("allClass").getAsJsonArray();
		Iterator<JsonElement> iter = elements.iterator();
		List<CategoryScore> clusters = new ArrayList<CategoryScore>(); 
		while(iter.hasNext())
		{
			JsonObject catJson = iter.next().getAsJsonObject();
			String name = catJson.get("categoryName").getAsString();
			Integer cluster = catJson.get("clusterId").getAsInt();
			
			clusters.add(new CategoryScore(name, cluster));
		}
		
		Collections.sort(clusters, new CategoryScoreComparator());
		
		return clusters;
	}
}
