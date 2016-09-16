package org.sunflower.webservice;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.sunflower.containers.CategoryScore;
import org.sunflower.containers.graph.GraphLink;
import org.sunflower.containers.graph.GraphNameLink;
import org.sunflower.containers.graph.GraphPath;
import org.sunflower.containers.graph.VisGraph;
import org.sunflower.services.ConceptGraphProcessor;
import org.sunflower.services.RemoteProcessor;
import org.sunflower.services.RulesProcessor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Controller
public class ApiController
{
	public static int DEFAULT_WIDTH = 3;
	public static int DEFAULT_DEPTH = 4;
	
	@Autowired
	private ConceptGraphProcessor processor;
	@Autowired
	private RulesProcessor rulesProcessor;
	@Autowired
	private RemoteProcessor processorRemote;
	
	private Gson gson = new GsonBuilder().setPrettyPrinting().create();
	
	@RequestMapping(value = "concept/graph/{query}", method=RequestMethod.GET, produces = "application/json; charset=utf-8")//
	@ResponseBody
	public String getConceptGraph(@PathVariable("query") String query, @RequestParam("width") String widthStr,
			                      @RequestParam("depth") String depthStr, @RequestParam("noPruning") String noPruningStr,
			                      @RequestParam("fullLabels") String fullLabelsStr)
	{
		boolean fullLabels = false;
		
		if(fullLabelsStr != null)
		{
			fullLabels = new Boolean(fullLabelsStr);
		}
		
		Map<String, List<GraphLink>> paths = getPaths(query.toLowerCase(), depthStr, widthStr, noPruningStr, false);
		
		VisGraph visGraph = processor.getVisGraph(paths, query.toLowerCase(), !fullLabels);
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		//return "{\"json\" : \"json\"}";
		return gson.toJson(visGraph);
    }
	
	@RequestMapping(value = "concept/paths/{query}", method=RequestMethod.GET, produces = "application/json; charset=utf-8")//
	@ResponseBody
	public String getConceptPaths(@PathVariable("query") String query, @RequestParam("width") String widthStr,
			                      @RequestParam("depth") String depthStr, @RequestParam("noPruning") String noPruningStr)
	{
		Map<String, List<GraphLink>> paths = getPaths(query.toLowerCase(), depthStr, widthStr, noPruningStr, false);
		List<GraphPath> pathsList = processor.getPathsList(paths);
		
		return gson.toJson(pathsList);
	}
	
	@RequestMapping(value = "concept/profile", method=RequestMethod.GET, produces = "application/json; charset=utf-8")//
	@ResponseBody
	public String getConceptProfile(@RequestParam("id") String query, @RequestParam("width") String widthStr,
			                      @RequestParam("depth") String depthStr, @RequestParam("noPruning") String noPruningStr)
	{
		Map<String, List<GraphLink>> paths = getPaths(query, depthStr, widthStr, noPruningStr, false);
		List<CategoryScore> profile = processor.getConceptProfile(paths);
		
		return gson.toJson(profile);
	}
	
	@RequestMapping(value = "concept/typeprofile", method=RequestMethod.GET, produces = "application/json; charset=utf-8")//
	@ResponseBody
	public String getConceptTypeProfile(@RequestParam("id") String query)
	{
		List<CategoryScore> profile = processor.getConceptTypeProfile(query);
		return gson.toJson(profile);
	}
	
	@RequestMapping(value = "concept/categorycooccurrence", method=RequestMethod.GET, produces = "application/json; charset=utf-8")//
	@ResponseBody
	public String getCategoryCooccurrence(@RequestParam("id") String query, @RequestParam("type") String type)
	{
		List<GraphNameLink> links = null;
		if(type.equals("entityCooccurrence")) links = processor.getCategoryCooccurrence(query);
		if(type.equals("parentProfile")) links = processor.getCategoryParentProfileSimilarities(query);
		return gson.toJson(links);
	}
	
	@RequestMapping(value = "concept/categoryclusters", method=RequestMethod.GET, produces = "application/json; charset=utf-8")//
	@ResponseBody
	public String getCategoryClusters(@RequestParam("id") String query, @RequestParam("type") String type)
	{
		List<GraphNameLink> links = null;
		if(type.equals("entityCooccurrence")) links = processor.getCategoryCooccurrence(query);
		if(type.equals("parentProfile")) links = processor.getCategoryParentProfileSimilarities(query);
		List<CategoryScore> clusters = processorRemote.getCategoryClusters(links);
		return gson.toJson(clusters);
	}
	
	@RequestMapping(value = "concept/categorycoverage", method=RequestMethod.GET, produces = "application/json; charset=utf-8")//
	@ResponseBody
	public String getCategoryCoverage(@RequestParam("id") String query, @RequestParam("width") String widthStr,
			                      @RequestParam("depth") String depthStr, @RequestParam("noPruning") String noPruningStr)
	{
		Map<String, List<GraphLink>> paths = getPaths(query, depthStr, widthStr, noPruningStr, false);
		List<CategoryScore> profile = processor.getCategoryCoverage(paths);
		
		return gson.toJson(profile);
	}
	
	private Map<String, List<GraphLink>> getPaths(String concept, String depthStr, String widthStr, String noPruningStr, boolean makeRootLink)
	{
		boolean noPruning = false;
		int width = DEFAULT_WIDTH;
		int depth = DEFAULT_DEPTH;
		
		if(widthStr != null)
		{
			width = new Integer(widthStr);
		}
		
		if(depthStr != null)
		{
			depth = new Integer(depthStr);
		}
		
		if(noPruningStr != null)
		{
			noPruning = new Boolean(noPruningStr);
		}
		
		Map<String, List<GraphLink>> paths = processor.getPathsMapForConcept(concept, depth, width, false);
		processor.selectPaths(paths, concept);
		if(!noPruning) paths = processor.prunePaths(paths);
		return paths;
	}
	
	@RequestMapping(value = "concept/compareScore", method=RequestMethod.GET, produces = "application/json; charset=utf-8")//
	@ResponseBody
	public String getCompareScore(@RequestParam("concept1") String concept1, @RequestParam("concept2") String concept2,
								  @RequestParam("width") String widthStr, @RequestParam("depth") String depthStr,
								  @RequestParam("noPruning") String noPruningStr, @RequestParam("fullLabels") String fullLabelsStr)
	{
		boolean noPruning = false;
		boolean fullLabels = false;
		int width = DEFAULT_WIDTH;
		int depth = DEFAULT_DEPTH;
		
		if(widthStr != null)
		{
			width = new Integer(widthStr);
		}
		
		if(depthStr != null)
		{
			depth = new Integer(depthStr);
		}
		
		if(noPruningStr != null)
		{
			noPruning = new Boolean(noPruningStr);
		}
		
		if(fullLabelsStr != null)
		{
			fullLabels = new Boolean(fullLabelsStr);
		}
		
		Map<String, List<GraphLink>> paths1 = processor.getPathsMapForConcept(concept1.toLowerCase(), depth, width, false);
		processor.selectPaths(paths1, concept1.toLowerCase());
		if(!noPruning) paths1 = processor.prunePaths(paths1);
		Map<String, List<GraphLink>> paths2 = processor.getPathsMapForConcept(concept2.toLowerCase(), depth, width, false);
		processor.selectPaths(paths2, concept2.toLowerCase());
		if(!noPruning) paths2 = processor.prunePaths(paths2);
		
		double compareScore = processor.getCompareScore(paths1, paths2);
		
		return gson.toJson(""+compareScore);
    }

	@RequestMapping(value = "concept/viscompare", method=RequestMethod.GET, produces = "application/json; charset=utf-8")//
	@ResponseBody
	public String getConceptGraph(@RequestParam("concept1") String concept1, @RequestParam("concept2") String concept2,
								  @RequestParam("width") String widthStr, @RequestParam("depth") String depthStr,
								  @RequestParam("noPruning") String noPruningStr, @RequestParam("fullLabels") String fullLabelsStr)
	{
		boolean noPruning = false;
		boolean fullLabels = false;
		int width = DEFAULT_WIDTH;
		int depth = DEFAULT_DEPTH;
		
		if(widthStr != null)
		{
			width = new Integer(widthStr);
		}
		
		if(depthStr != null)
		{
			depth = new Integer(depthStr);
		}
		
		if(noPruningStr != null)
		{
			noPruning = new Boolean(noPruningStr);
		}
		
		if(fullLabelsStr != null)
		{
			fullLabels = new Boolean(fullLabelsStr);
		}
		
		Map<String, List<GraphLink>> paths1 = processor.getPathsMapForConcept(concept1, depth, width, true);
		processor.selectPaths(paths1, concept1);
		if(!noPruning) paths1 = processor.prunePaths(paths1);
		Map<String, List<GraphLink>> paths2 = processor.getPathsMapForConcept(concept2, depth, width, true);
		processor.selectPaths(paths2, concept2);
		if(!noPruning) paths2 = processor.prunePaths(paths2);
		Map<String, List<GraphLink>> paths = processor.combinePaths(paths1, paths2);
		
		//if(!noPruning) paths = processor.prunePaths(paths);
		
		List<String> rootNodes = new ArrayList<String>();
		rootNodes.add(concept1);
		rootNodes.add(concept2);
		VisGraph visGraph = processor.getVisGraph(paths, rootNodes, !fullLabels, false);
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		//return "{\"json\" : \"json\"}";
		return gson.toJson(visGraph);
    }
	
	@RequestMapping(value = "concept/autocomplete/{query}", method=RequestMethod.GET, produces = "application/json; charset=utf-8")//, produces = "application/json; charset=utf-8"
	@ResponseBody
	public String getAutocomplete(@PathVariable("query") String query)
	{
		query = query.replace(" ", "_");
		List<String> conceptNames = processor.getPartiallyMatchingConcepts(query);
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		return gson.toJson(conceptNames);
    }
	
	@RequestMapping(value = "rule/contains", method=RequestMethod.GET, produces = "application/json; charset=utf-8")//
	@ResponseBody
	public String getConceptGraph(@RequestParam("concept1") String concept1, @RequestParam("concept2") String concept2,
								  @RequestParam("surface1") String surface1, @RequestParam("surface2") String surface2)
	{
		return gson.toJson(rulesProcessor.contains(concept1, concept2, surface1, surface2));
	}
}
