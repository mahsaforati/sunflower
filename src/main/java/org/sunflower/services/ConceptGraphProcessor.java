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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.sunflower.containers.CategoryScore;
import org.sunflower.containers.CategoryScoreComparator;
import org.sunflower.containers.ConceptLinks;
import org.sunflower.containers.graph.GraphLink;
import org.sunflower.containers.graph.GraphNameLink;
import org.sunflower.containers.graph.GraphNameLinkComparator;
import org.sunflower.containers.graph.GraphPath;
import org.sunflower.containers.graph.GraphPathComparator;
import org.sunflower.containers.graph.VisGraph;
import org.sunflower.db.GraphDbReader;
import org.sunflower.db.GraphInMemoryCompactReader;
import org.sunflower.db.GraphInMemoryReader;
import org.sunflower.db.GraphReader;
import org.sunflower.test.StartSunflowerServer;
import org.sunflower.util.ConfigContainer;
import org.sunflower.util.GlobalConstants;
import org.sunflower.util.SimCalc;

import com.mysql.jdbc.exceptions.jdbc4.MySQLNonTransientConnectionException;

@Service
public class ConceptGraphProcessor
{
	private GraphReader conceptGraph;
	private GraphReader categoryGraph;
	
	private GraphReader categoryToConceptMap;
	private GraphReader conceptLinkTypeGraph;
	
	private GraphReader categoryCooccurrenceGraph;
	
	public ConceptGraphProcessor()
	{
		connectDatabase();
	}
	
	public void connectDatabase()
	{
        ConfigContainer config = GlobalConstants.getConfig();
		if(config.getValue("runInMemory") != null && new Boolean(config.getValue("runInMemory")))
		{
			String pathConcepts = config.getValue("inMemoryConceptPath");
			String pathCategories = config.getValue("inMemoryCategoryPath");
			
			String pathLinkConcepts = config.getValue("inMemoryLinkConceptPath");
			String pathCatCooccurrence = config.getValue("inMemoryCatCooccurrencePath");
			
			if(pathConcepts != null && pathCategories != null)
			{
                conceptGraph = new GraphInMemoryReader(pathConcepts);
				categoryGraph = new GraphInMemoryReader(pathCategories);
			}
			else
			{
				conceptGraph = new GraphInMemoryReader();
				categoryGraph = new GraphInMemoryReader();
			}
			
			if(pathLinkConcepts != null)
			{
                conceptLinkTypeGraph = new GraphInMemoryReader(pathLinkConcepts);
            }
			else
			{
                conceptLinkTypeGraph = new GraphInMemoryReader();
			}
			
			if(pathCatCooccurrence != null)
			{
				categoryCooccurrenceGraph = new GraphInMemoryCompactReader(pathCatCooccurrence);
			}
			else
			{
				categoryCooccurrenceGraph = new GraphInMemoryReader();
			}
			
			
			
			categoryToConceptMap = new GraphInMemoryReader();
		}
		else
		{
			String user = config.getValue("username");// != null ? config.getValue("username") : "lipczak";
			String pass = config.getValue("password");// != null ? config.getValue("password") : "abc123!";
			connectDatabase(user, pass);
		}
	}
	
	public ConceptGraphProcessor(String user, String pass)
	{
		connectDatabase(user, pass);
	}
	
	public void connectDatabase(String user, String pass)
	{
		try
		{
            DriverManager.registerDriver(new com.mysql.jdbc.Driver ());
		    Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/sunflower?" + "user="+user+"&password="+pass+"&useUnicode=true&characterEncoding=utf-8");
		    conceptGraph = new GraphDbReader("ConceptJson", conn);
		    categoryGraph = new GraphDbReader("CategoryJson", conn);
		    
		    categoryToConceptMap = new GraphDbReader("CategoryToConceptJson", conn);
		    conceptLinkTypeGraph = new GraphDbReader("ConceptLinkTypeJson", conn);
		    System.out.println(" Server ready");
		}
		catch (SQLException ex)
		{
		    // handle any errors
		    System.out.println("SQLException: " + ex.getMessage());
		    System.out.println("SQLState: " + ex.getSQLState());
		    System.out.println("VendorError: " + ex.getErrorCode());
		    ex.printStackTrace();
		}
	}
	
	public List<CategoryScore> getConceptProfile(Map<String, List<GraphLink>> paths)
	{
		List<CategoryScore> profile = new ArrayList<CategoryScore>();
		
		for(Entry<String, List<GraphLink>> concept : paths.entrySet())
		{
			if(concept.getKey().contains("_by_")) continue;
			double score = 0;
			for(GraphLink path : concept.getValue())
			{
				score += path.getTotalScore();
			}
			profile.add(new CategoryScore(concept.getKey(), score));
		}
		
		Collections.sort(profile, new CategoryScoreComparator());
		return profile;
	}
	
	public Map<String, List<CategoryScore>> getDirectCategoriesProfiles(Map<String, List<GraphLink>> paths)
	{
		Map<String, List<CategoryScore>> profiles = new HashMap<String, List<CategoryScore>>();
		
		for(Entry<String, List<GraphLink>> concept : paths.entrySet())
		{
			if(concept.getKey().contains("_by_")) continue;
			double score = 0;
			for(GraphLink path : concept.getValue())
			{
				score += path.getTotalScore();
				while(path.getNext() != null) path = path.getNext();
				String categoryName = path.getName();
				List<CategoryScore> profile = profiles.get(categoryName);
				if(profile == null) profiles.put(categoryName, (profile = new ArrayList<CategoryScore>()));
				profile.add(new CategoryScore(concept.getKey(), score));
			}
			
		}
		//Collections.sort(profile, new CategoryScoreComparator());
		return profiles;
	}
	
	public List<GraphPath> getPathsList(Map<String, List<GraphLink>> pathsMap)
	{
		List<GraphPath> pathsList = new ArrayList<GraphPath>();
		for(Entry<String, List<GraphLink>> concept : pathsMap.entrySet())
		{
			for(GraphLink path : concept.getValue())
			{
				pathsList.add(new GraphPath(concept.getKey(), path.toList()));
			}
		}
		
		Collections.sort(pathsList, new GraphPathComparator());
		
		return pathsList;
	}
	
	public double getCompareScore(Map<String, List<GraphLink>> paths1, Map<String, List<GraphLink>> paths2) {
		return SimCalc.cosineSimilarity(fillValueMap(paths1), fillValueMap(paths2));
	}

	private Map<String, Double> fillValueMap(Map<String, List<GraphLink>> paths) {
		Map<String, Double> map = new HashMap<String, Double>();
		
		for(Entry<String, List<GraphLink>> concept : paths.entrySet())
		{
			if(concept.getKey().contains("_by_")) continue;
			double score = 0;
			for(GraphLink path : concept.getValue())
			{
				score += path.getTotalScore();
			}
			map.put(concept.getKey(), score);
		}
		
		return map;
	}
	
	public Map<String, List<GraphLink>> getPathsMapForConcept(String concept, int depth, int breath, boolean makeRootLink)
	{
		Map<String, List<GraphLink>> pathMap = new HashMap<String, List<GraphLink>>();
		
		ConceptLinks conceptLinks = null;
		try
		{
			conceptLinks = conceptGraph.getLinks(concept);
		} catch (SQLException e) {
			System.out.println(" Error for conceptGraph.getLinks(concept) in getPathsMapForConcept");
			if(MySQLNonTransientConnectionException.class.isInstance(e))
			{
				try
				{
					System.out.println(" restarting connection");
					connectDatabase();
					System.out.println(" reading links again");
					conceptLinks = conceptGraph.getLinks(concept);
					System.out.println(" conceptLinks rertieved "+conceptLinks);
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			}
			else
			{
				e.printStackTrace();
			}
		}
		
		if(conceptLinks != null)
		{
			GraphLink rootLink = null;
			if(makeRootLink)
			{
				rootLink = new GraphLink(concept, 1.0, "root");
			}
			
			fillPathMap(pathMap, conceptLinks, 1, rootLink, depth, breath);
		}
		
		return pathMap;
	}

	private void fillPathMap(Map<String, List<GraphLink>> pathMap, ConceptLinks links,
			                 int lvl, GraphLink head, int depth, int breath)
	{
		int counter = 0;
		for(CategoryScore link : links.getCategories())
		{
			if(counter == breath)
			{
				break;
			}
			GraphLink graphLink = new GraphLink(link.getName(), link.getScore()/(1.0*links.getSourceCount()), null);
			graphLink.setNext(head);
			List<GraphLink> pathsPerNode = pathMap.get(link.getName());
			if(pathsPerNode == null)
			{
				pathsPerNode = new ArrayList<GraphLink>();
				pathMap.put(link.getName(), pathsPerNode);
			}
			pathsPerNode.add(graphLink);
			ConceptLinks innerLinks = null;
			try
			{
				innerLinks = categoryGraph.getLinks(link.getName());
			} catch (SQLException e) {
				if(MySQLNonTransientConnectionException.class.isInstance(e))
				{
					try
					{
						connectDatabase();
						innerLinks = categoryGraph.getLinks(link.getName());
					} catch (SQLException e1) {
						e1.printStackTrace();
					}
				}
				else
				{
					e.printStackTrace();
				}
			}
			
			if(innerLinks != null && lvl < depth)
			{
				fillPathMap(pathMap, innerLinks, lvl+1, graphLink, depth, breath);
			}
			
			counter++;
		}
	}
	
	public ConceptLinks getLinksForConcept(String concept)
	{
		ConceptLinks links = null;
		try
		{
			links = conceptGraph.getLinks(concept);
		} catch (SQLException e) {
			if(MySQLNonTransientConnectionException.class.isInstance(e))
			{
				try
				{
					connectDatabase();
					links = conceptGraph.getLinks(concept);
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			}
			else
			{
				e.printStackTrace();
			}
		}
		
		return links;
	}
	
	public ConceptLinks getConceptsForCategory(String concept)
	{
		ConceptLinks links = null;
		try
		{
			links = categoryToConceptMap.getLinks(concept);
		} catch (SQLException e) {
			if(MySQLNonTransientConnectionException.class.isInstance(e))
			{
				try
				{
					connectDatabase();
					links = categoryToConceptMap.getLinks(concept);
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			}
			else
			{
				e.printStackTrace();
			}
		}
		
		return links;
	}

	public List<String> getPartiallyMatchingConcepts(String query)
	{
		return conceptGraph.getPartiallyMatchingConcepts(query);
	}
	
	public VisGraph getVisGraph(Map<String, List<GraphLink>> paths, String rootNode, boolean doNameCleaning)
	{
		List<String> rootNodes = new ArrayList<String>();
		rootNodes.add(rootNode);
		return getVisGraph(paths, rootNodes, doNameCleaning, true);
	}
	
	public VisGraph getVisGraph(Map<String, List<GraphLink>> paths, List<String> rootNodes, boolean doNameCleaning, boolean addRootLink)
	{
		VisGraph graph = new VisGraph();
		for(String rootNode : rootNodes)
		{
			graph.addNode(rootNode, doNameCleaning ? VisGraph.cleanName(rootNode) : rootNode, "start");
		}
		
		for(Entry<String, List<GraphLink>> entry : paths.entrySet())
		{
			String type = "intermediate";
			if(entry.getValue().get(0).getRelations().size() != 0)
			{
				type = "";
				for(String typeKey : entry.getValue().get(0).getRelations().keySet())
				{
					if(!type.equals(""))
					{
						type += " ";
					}
					type += typeKey;
				}
				
			}
			graph.addNode(entry.getKey(), doNameCleaning ? VisGraph.cleanName(entry.getKey()) : entry.getKey(), type);
		}
		
		for(Entry<String, List<GraphLink>> entry : paths.entrySet())
		{
			for(GraphLink link : entry.getValue())
			{
				List<String[]> linksStr = new ArrayList<String[]>();
				link.getLinksAsString(linksStr, addRootLink ? rootNodes.get(0):null);
				for(String[] linkStr : linksStr)
				{
					graph.addLink(linkStr[0], linkStr[1], new Double(linkStr[2]));
				}
			}
		}
		
		return graph;
	}

	public void selectPaths(Map<String, List<GraphLink>> paths, String concept)
	{
		ConceptLinks conceptLinks = null;
		try
		{
			conceptLinks = conceptGraph.getLinks(concept);
		} catch (SQLException e) {
			if(MySQLNonTransientConnectionException.class.isInstance(e))
			{
				try
				{
					connectDatabase();
					conceptLinks = conceptGraph.getLinks(concept);
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			}
			else
			{
				e.printStackTrace();
			}
		}
		ConceptLinks conceptTypes = null;
		try
		{
			conceptTypes = conceptLinkTypeGraph.getLinks(concept);
		} catch (SQLException e) {
			if(MySQLNonTransientConnectionException.class.isInstance(e))
			{
				try
				{
					connectDatabase();
					conceptTypes = conceptLinkTypeGraph.getLinks(concept);
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			}
			else
			{
				e.printStackTrace();
			}
		}
		
		selectBySubstringMatching(paths);
		selectByDirectCategories(paths, conceptLinks);
		selectByConceptLinkType(paths, conceptTypes);
	}

	private void selectBySubstringMatching(Map<String, List<GraphLink>> paths)
	{
		for(Entry<String, List<GraphLink>> entry : paths.entrySet())
		{
			for(GraphLink path : entry.getValue())
			{
				if(path.isPathOfSubcategories())
				{
					path.setRelation("pathOfSubcategories", entry.getKey());
				}
			}
		}
	}

	private void selectByDirectCategories(Map<String, List<GraphLink>> paths, ConceptLinks conceptLinks)
	{
		Map<String, CategoryScore> linkMap = new HashMap<String, CategoryScore>();
		if(conceptLinks == null) return;
		
		int counter = 0;
		for(CategoryScore typeLink : conceptLinks.getCategories())
		{
			linkMap.put(typeLink.getName(), typeLink);
			counter++;
			if(counter > 5)
			{
				break;
			}
		}
		
		for(Entry<String, List<GraphLink>> entry : paths.entrySet())
		{
			CategoryScore link = linkMap.get(entry.getKey());
			if(link != null)
			{
				setRelationToGraphLinks(entry.getValue(), "directCategory", link.getName());
			}
		}
	}

	private void selectByConceptLinkType(Map<String, List<GraphLink>> paths, ConceptLinks conceptTypes)
	{
		if(conceptTypes == null)
		{
			return;
		}
		
		Map<String, CategoryScore> typeMap = new HashMap<String, CategoryScore>();
		for(CategoryScore typeLink : conceptTypes.getCategories())
		{
			typeMap.put(typeLink.getName(), typeLink);
		}
		
		for(Entry<String, List<GraphLink>> entry : paths.entrySet())
		{
			ConceptLinks catConcepts = null;
			try
			{
				catConcepts = categoryToConceptMap.getLinks(entry.getKey());
			} catch (SQLException e) {
				if(MySQLNonTransientConnectionException.class.isInstance(e))
				{
					try
					{
						connectDatabase();
						catConcepts = categoryToConceptMap.getLinks(entry.getKey());
					} catch (SQLException e1) {
						e1.printStackTrace();
					}
				}
				else
				{
					e.printStackTrace();
				}
			}
			
			if(catConcepts != null)
			{
				for(CategoryScore catConcept : catConcepts.getCategories())
				{
					CategoryScore matchingLinkType = typeMap.get(catConcept.getName());
					if(matchingLinkType != null)
					{
						setRelationToGraphLinks(entry.getValue(), "linkType", matchingLinkType.getGeneralType());
					}
				}
			}
		}
	}

	private void setRelationToGraphLinks(List<GraphLink> links, String relationType, String relationName)
	{
		for(GraphLink link : links)
		{
			link.setRelation(relationType, relationName);
		}
	}

	public Map<String, List<GraphLink>> prunePaths(Map<String, List<GraphLink>> paths)
	{
		Map<String, List<GraphLink>> prunedMap = new HashMap<String, List<GraphLink>>();
		
		for(Entry<String, List<GraphLink>> entry : paths.entrySet())
		{
			List<GraphLink> prunedLinks = new ArrayList<GraphLink>();
			for(GraphLink link : entry.getValue())
			{
				if(link.getRelations().size() > 0)
				{
					prunedLinks.add(link);
				}
			}
			if(prunedLinks.size() > 0)
			{
				prunedMap.put(entry.getKey(), prunedLinks);
			}
		}
		
		removeArtificialNodes(paths);
		
		return prunedMap;
	}

	private void removeArtificialNodes(Map<String, List<GraphLink>> paths)
	{
		for(Entry<String, List<GraphLink>> entry : paths.entrySet())
		{
			for(GraphLink link : entry.getValue())
			{
				link.removeArtificialNodes();
			}
		}
	}

	public Map<String, List<GraphLink>> combinePaths(Map<String, List<GraphLink>> paths1,
			                                         Map<String, List<GraphLink>> paths2)
	{
		Set<String> intersection = new HashSet<String>(paths1.keySet());
		intersection.retainAll(paths2.keySet());
		
		Map<String, List<GraphLink>> combinedPaths = new HashMap<String, List<GraphLink>>();
		for(String commonNode : intersection)
		{
			List<GraphLink> combinedList = new ArrayList<GraphLink>(paths1.get(commonNode));
			combinedList.addAll(paths2.get(commonNode));
			combinedPaths.put(commonNode, combinedList);
		}
		
		return combinedPaths;
	}

	public List<CategoryScore> getConceptTypeProfile(String query)
	{
		try
		{
			ConceptLinks links = conceptLinkTypeGraph.getLinks(query);
			if(links == null) return null;
			return links.getCategories();
		}
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public List<GraphNameLink> getCategoryCooccurrence(String query)
	{
		try
		{
			Map<String, GraphNameLink> linkMap = new HashMap<String, GraphNameLink>();
			ConceptLinks categories = conceptGraph.getLinks(query);
			if(categories == null) return null;
			
			Set<String> categorySet = new HashSet<String>();
			for(CategoryScore cat : categories.getCategories()) categorySet.add(cat.getName());
			
			for(CategoryScore cat : categories.getCategories())
			{
				ConceptLinks cooccurrences = categoryCooccurrenceGraph.getLinks(cat.getName());
				if(cooccurrences == null) continue;
				for(CategoryScore cooc : cooccurrences.getCategories())
				{
					if(!categorySet.contains(cooc.getName())) continue;
					String key = cat.getName()+"\t"+cooc.getName();
					String keyRev = cooc.getName()+"\t"+cat.getName();
					
					if(!linkMap.containsKey(key) && !linkMap.containsKey(keyRev)) linkMap.put(key, new GraphNameLink(cat.getName(), cooc.getName(), cooc.getScore()));
				}
			}
			
			List<GraphNameLink> result = new ArrayList<GraphNameLink>();
			for(GraphNameLink l : linkMap.values()) result.add(l);
			Collections.sort(result, new GraphNameLinkComparator());
			return result;
		}
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public List<GraphNameLink> getCategoryParentProfileSimilarities(String concept)
	{
		Map<String, List<GraphLink>> paths = getPathsMapForConcept(concept, 4, 200, false);
		Map<String, List<CategoryScore>> profiles = getDirectCategoriesProfiles(paths);
		
		List<String> catNames = new ArrayList<String>();
		List<Map<String, Double>> mapProfiles = new ArrayList<Map<String,Double>>();
		
		for(Entry<String, List<CategoryScore>> profile : profiles.entrySet())
		{
			catNames.add(profile.getKey());
			Map<String, Double> profileMap = new HashMap<String, Double>();
			for(CategoryScore score : profile.getValue()) profileMap.put(score.getName(), 1.0);//score.getScore()
			mapProfiles.add(profileMap);
		}
		
		List<GraphNameLink> simLinks = new ArrayList<GraphNameLink>();
		
		for(int i = 0; i < catNames.size(); i++)
		{
			for(int j = i + 1; j < catNames.size(); j++)
			{
				double sim = SimCalc.cosineSimilarity(mapProfiles.get(i), mapProfiles.get(j));
				if(sim > 0) simLinks.add(new GraphNameLink(catNames.get(i), catNames.get(j), sim));
			}
		}
		
		Collections.sort(simLinks, new GraphNameLinkComparator());
		return simLinks;
	}
	
	public List<CategoryScore> getCategoryCoverage(Map<String, List<GraphLink>> paths) 
	{
		Map<String, Set<String>> coverageMap = new HashMap<String, Set<String>>();
		Map<String, Set<String>> fullCoverageMap = new HashMap<String, Set<String>>();
		List<CategoryScore> scores = new ArrayList<CategoryScore>();
		
		for(Entry<String, List<GraphLink>> e : paths.entrySet())
		{
			String name = e.getKey();
			Set<String> categories = new HashSet<String>();
			Set<String> allCategories = new HashSet<String>();
			for(GraphLink path : e.getValue())
			{
				while(path.getNext() != null)
				{
					path = path.getNext();
					allCategories.add(path.getName());
				}
				categories.add(path.getName());
			}
			coverageMap.put(name, categories);
			fullCoverageMap.put(name, allCategories);
		}
		
		for(Entry<String, Set<String>> e : coverageMap.entrySet())
		{
			String name = e.getKey();
			Set<String> categories = e.getValue();
			Set<String> allCategories = fullCoverageMap.get(name);
			
			if(categories.size() == 1) continue;
			
			boolean isCoveredBySubcategory = false;
			for(String c : allCategories)
			{
				if(!c.equals(name) && coverageMap.get(c) != null && coverageMap.get(c).size() > categories.size()*0.8)
				{
					isCoveredBySubcategory = true;
					break;
				}
			}
			
			if(isCoveredBySubcategory) continue;
			
			CategoryScore catScore = new CategoryScore(name, categories.size());
			String categoriesStr = "";
			for(String c : categories) categoriesStr += " "+c;
			catScore.setDetailedType(categoriesStr.trim());
			scores.add(catScore);
		}
		
		Collections.sort(scores, new CategoryScoreComparator());
		
		return scores;
	}
}
