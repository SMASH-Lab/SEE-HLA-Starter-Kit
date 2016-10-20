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
package skf.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import hla.rti1516e.RTIambassador;
import hla.rti1516e.RtiFactory;
import hla.rti1516e.RtiFactoryFactory;
import hla.rti1516e.exceptions.RTIinternalError;

public final class SEERTIAmbassador {
	
	private static final Logger logger = LogManager.getLogger(SEERTIAmbassador.class);
	
	private static RTIambassador rtiambassador = null;
	private static RtiFactory rtifactory = null;
	
	private SEERTIAmbassador() throws RTIinternalError {
		rtifactory = RtiFactoryFactory.getRtiFactory();
		rtiambassador = rtifactory.getRtiAmbassador();
		
		// Get information on the HLA/RTI
		logger.info("Vendor HLA/RTI: name:"+rtifactory.rtiName()+", rtiVersion: "+rtifactory.rtiVersion()+
					", hlaVersion: "+rtiambassador.getHLAversion());

	}
	
	public static synchronized RTIambassador getInstance() throws RTIinternalError {
		if(rtiambassador == null)
			new SEERTIAmbassador();
		return rtiambassador;
	}
	
	@Override
	protected Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException("Clone not supported!");
	}
	
}
