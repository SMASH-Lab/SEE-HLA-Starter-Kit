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
package skf.model.parser;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import skf.coder.Coder;

@SuppressWarnings("rawtypes")
public abstract class AbstractObjectModelParser {

	private static final Logger logger = LogManager.getLogger(AbstractObjectModelParser.class);

	protected String classHandleName = null;
	protected Map<String, Coder> mapFieldCoder = null;
	protected Field[] fields = null;
	
	
	protected Map<String, byte[]> encoderMap = null; 


	public AbstractObjectModelParser () {
		this.mapFieldCoder = new HashMap<String, Coder>();
		this.encoderMap = new HashMap<String, byte[]>();
	}

	protected abstract void retrieveClassModelStructure();


	protected void matchingObjectCoderIsValid(Field f, Coder coderTmp) {
		if(!coderTmp.getAllowedType().equals(f.getType())){
			logger.error("The Coder: "+coderTmp.getAllowedType()+" is not valid for the Object: "+f.getType());
			throw new RuntimeException("The Coder: "+coderTmp.getAllowedType()+" is not valid for the Object: "+f.getType());
		}
	}

	public String getClassHandleName() {
		return this.classHandleName;
	}
	
	public Map<String, Coder> getMapFieldCoder() {
		return this.mapFieldCoder;
	}

}
