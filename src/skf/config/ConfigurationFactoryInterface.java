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
import java.io.FileNotFoundException;
import java.io.IOException;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

public interface ConfigurationFactoryInterface {
		
	/**
	 * Imports the configuration parameters from a given file
	 * 
	 * @param file the configuration file as json
	 * @return a Configuration object rapresentation of the configuration file
	 * 
	 * @throws FileNotFoundException file not found
	 * @throws IOException io exception
	 * @throws JsonMappingException json exception during the attribute mapping process.
	 * @throws JsonParseException json exception during the attribute parsing process.
	 */
	public Configuration importConfiguration(File file) throws FileNotFoundException, JsonParseException, JsonMappingException, IOException;
	
	/**
	 * Exports the configuration parameters into a given directory
	 * 
	 * @param config AbstractConfiguration the configuration object
	 * @param outputFile output file
	 * @throws IOException io exception
	 */
	public void exportConfiguration(Configuration config, File outputFile) throws IOException;

	
	/**
	 * Creates an empty Configuration object
	 * 
	 * @return an empty Configuration object
	 */
	public Configuration createConfiguration();
	
}