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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

import org.sunflower.containers.ConceptLinks;

public class GraphDbReader extends GraphReader
{
	private String table;
	private Connection conn = null;
	private Gson gson;
	
	public GraphDbReader(String table, Connection conn)
	{
		this.table = table;
		this.conn = conn;
		gson = new Gson();
	}
	
	public ConceptLinks getLinks(String concept) throws SQLException
	{	
		Statement stmt = null;
		ResultSet rs = null;
		
		try
		{
		    stmt = conn.createStatement();
		    rs = stmt.executeQuery("SELECT json FROM "+table+" WHERE name='"+concept+"'");
		    if(!rs.next())
		    {
		    	return null;
		    }
		    String jsonStr = rs.getString(1);
		    return gson.fromJson(jsonStr, ConceptLinks.class);
		}
		finally
		{
		    release(rs, stmt);
		}
	}

	public List<String> getPartiallyMatchingConcepts(String query)
	{	
		Statement stmt = null;
		ResultSet rs = null;
		query = query.toLowerCase();
		
		try
		{
		    stmt = conn.createStatement();
		    rs = stmt.executeQuery("SELECT name FROM "+table+" WHERE (LOWER(name) LIKE '"+query+"%' OR LOWER(name) LIKE '%\\_"+query+"%') ORDER BY id");
		    if(!rs.next())
		    {
		    	return new ArrayList<String>();
		    }
		    List<String> matchingConcepts = new ArrayList<String>();
		    
		    for(int i = 0; i < 100; i++)
		    {
		    	matchingConcepts.add(rs.getString(1).replace("_", " "));
		    	
		    	if(!rs.next()) break;
		    }
		    
		    return matchingConcepts;
		}
		catch (SQLException ex)
		{
		    // handle any errors
		    System.out.println("SQLException: " + ex.getMessage());
		    System.out.println("SQLState: " + ex.getSQLState());
		    System.out.println("VendorError: " + ex.getErrorCode());
		    ex.printStackTrace();
		}
		finally
		{
		    release(rs, stmt);
		}
		
		return new ArrayList<String>();
	}
	
	private void release(ResultSet rs, Statement stmt)
	{
		if (rs != null) {
	        try {
	            rs.close();
	        } catch (SQLException sqlEx) { } // ignore

	        rs = null;
	    }

	    if (stmt != null) {
	        try {
	            stmt.close();
	        } catch (SQLException sqlEx) { } // ignore

	        stmt = null;
	    }
	}
}
