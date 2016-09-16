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

import org.springframework.stereotype.Service;
import org.sunflower.db.RulesInMemoryReader;
import org.sunflower.db.RulesReader;
import org.sunflower.util.ConfigContainer;
import org.sunflower.util.GlobalConstants;

@Service
public class RulesProcessor
{
	RulesReader rulesContainer;
	
	public RulesProcessor()
	{
		connectDatabase();
	}
	
	public void connectDatabase()
	{
		ConfigContainer config = GlobalConstants.getConfig();
		if(config.getValue("runInMemory") != null && new Boolean(config.getValue("runInMemory")))
		{
			String pathRules = config.getValue("inMemoryRulesPath");
			
			if(pathRules != null)
			{
				rulesContainer = new RulesInMemoryReader(pathRules);
			}
			else
			{
				rulesContainer = new RulesInMemoryReader();
			}
		}
		else
		{
			//not implemented yet
		}
	}
	
	public boolean contains(String concept1, String concept2, String surface1, String surface2)
	{
		return rulesContainer.hasRule(concept1, concept2, surface1, surface2);
	}
}
