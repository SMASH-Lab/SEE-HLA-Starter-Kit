/*****************************************************************
SEE HLA Starter Kit -  A Java framework to develop HLA Federates
in the context of the SEE (Simulation Exploration Experience) 
project.
Copyright (c) 2014, SMASH Lab - University of Calabria (Italy), 
All rights reserved.

GNU Lesser General Public License (GNU LGPL).

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 3.0 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library. 
If not, see http://http://www.gnu.org/licenses/
*****************************************************************/
package skf.config;

import java.io.File;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig.Feature;


/**
 * 
 * @author SMASH-Lab University of Calabria
 * @version 0.1
 * 
 */
public class ConfigurationFactory implements ConfigurationFactoryInterface {

	Logger logger = LogManager.getLogger(ConfigurationFactory.class);

	private ObjectMapper mapper = null;

	@Override
	public Configuration importConfiguration(File file) throws JsonParseException, JsonMappingException, IOException {
		logger.info("Importing the SFK configuration file");
		return getObjectMapper().readValue(file, Configuration.class);
		
	}


	@Override
	public void exportConfiguration(Configuration config, File outputFile) throws JsonGenerationException, JsonMappingException, IOException {
		logger.info("Exporting the SFK configuration file");
		getObjectMapper().writerWithDefaultPrettyPrinter().writeValue(outputFile, config);

	}

	@Override
	public Configuration createConfiguration() {
		return new Configuration();
	}
	
	private ObjectMapper getObjectMapper() {
		if(mapper == null){
			 mapper = new ObjectMapper();
			 mapper.configure(Feature.SORT_PROPERTIES_ALPHABETICALLY , true);
			 mapper.configure(Feature.WRITE_DATES_AS_TIMESTAMPS, false);
		}
		return mapper;
	}
}
