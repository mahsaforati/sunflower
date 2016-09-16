package org.sunflower.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.sunflower.util.IOUtil;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ClusteringRequestTest
{
	public static void main(String[] args) throws IOException
	{	
		String longText = "";
		
		String pathIn = "/users/lipczak/tmp/clusteringExampleRequest.json";
		
		BufferedReader inFile = IOUtil.openReadFile(pathIn);
		
		String line = null;
		try
		{
			while((line = inFile.readLine()) != null)
			{
				longText += line+"\n";
			}
			
			inFile.close();
		}
		catch (IOException e)
		{
			System.err.println("Error for: "+line);
			e.printStackTrace();
		}
		
		String urlString = "http://129.173.212.192/GephiWeb/clustering.htm";
		
		String text = longText;
		
		HttpClient httpclient = new DefaultHttpClient();
		
		BufferedReader br = test(httpclient, urlString, text);
		
		String response = "";
		while((line = br.readLine()) != null)
		{
			response += line + "\n";
		}
		
		JsonObject resJson = new JsonParser().parse(response).getAsJsonObject();
		JsonArray elements = resJson.get("allClass").getAsJsonArray();
		Iterator<JsonElement> iter = elements.iterator();
		while(iter.hasNext())
		{
			JsonObject catJson = iter.next().getAsJsonObject();
			String name = catJson.get("categoryName").getAsString();
			Integer cluster = catJson.get("clusterId").getAsInt();
			
			System.out.println(name+"\t"+cluster);
		}
	}
	
	public static BufferedReader test(HttpClient httpclient, String url, String text) throws ClientProtocolException, IOException
	{
		HttpPost httpPost = new HttpPost(url);

		MultipartEntity reqEntity = new MultipartEntity();
		reqEntity.addPart("input", new StringBody(text));
		reqEntity.addPart("res", new StringBody("1.0"));
		httpPost.setEntity(reqEntity);

		HttpResponse response = httpclient.execute(httpPost);
		
		if(response.getStatusLine().getStatusCode() != 200) System.out.println("RESPONSE STATUS: "+response.getStatusLine().getStatusCode());
		InputStream inputStream = response.getEntity().getContent();
		BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
		
		return br;
	}
}
