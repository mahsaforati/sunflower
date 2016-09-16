package org.sunflower.test;

import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.sunflower.containers.graph.GraphLink;
import org.sunflower.containers.graph.VisGraph;
import org.sunflower.services.ConceptGraphProcessor;
import org.sunflower.util.ConfigContainer;
import org.sunflower.util.IOUtil;

public class StartSunflowerServer
{
	//test change
	public static void main(String[] args)
	{
		if(args.length == 0)
		{
			String[] hardCoded = {"TASK=runCategoryGraphServer", "FILE_CONF=sunflower.conf"};
			
			
			args = hardCoded;
		}

        ConfigContainer config = new ConfigContainer(args);
		String task = config.getValue("TASK");
		if(config.getValue("PATH_CONF") != null)
		{
			config = new ConfigContainer(config.getValue("PATH_CONF"), config);
		}
		else if(config.getValue("FILE_CONF") != null)
		{
			//config = new ConfigContainer(StartSunflowerServer.class.getResource(config.getValue("FILE_CONF")).getPath(), config);
		}
		
		if(task.equals("runCategoryGraphServer"))
		{
			
			String username = config.getValue("username") != null ? config.getValue("username") : "lipczak";
			String password = config.getValue("password") != null ? config.getValue("password") : "Aaaa111!";
			String pathOut = "/users/lipczak/data/sunflower/json/";
			ConceptGraphProcessor processor = new ConceptGraphProcessor(username, password);
			
			//
			// TEST
			//
			
			String concept = "Royal_Canadian_Mint";//"Risk_management";//"Support_vector_machine";//"Star_Wars";//"Zamość";//"Mobile_phone";//"Microsoft";//"IPhone";//"Segway_PT";
			
			
			pathOut += concept+".json";
			
			Map<String, List<GraphLink>> paths = processor.getPathsMapForConcept(concept, 4, 3, false);
			processor.selectPaths(paths, concept);
			paths = processor.prunePaths(paths);
			
			VisGraph visGraph = processor.getVisGraph(paths, concept, true);
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			String json = gson.toJson(visGraph);
			PrintWriter outFile = IOUtil.openWriteFile(pathOut);
			outFile.println(json);
			outFile.close();
			
			System.out.println("concept links for "+concept+" "+processor.getLinksForConcept(concept));
			for(Entry<String, List<GraphLink>> pathEntry : paths.entrySet())
			{
				String category = pathEntry.getKey();
				
				System.out.println(category);
				System.out.println("concepts "+processor.getConceptsForCategory(category));
				for(GraphLink path : pathEntry.getValue())
				{
					System.out.println("\t"+path.getPathAsString());
				}
			}
		}
	}
}
