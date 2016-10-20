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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.DateTime;

/**
 * 
 * @author SMASH-Lab University of Calabria
 * @version 0.1
 * 
 */
public class Configuration implements ConfigurationInterface {
	
	
	private static final String IP_PATTERN = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
									        "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
									        "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
									        "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

	private boolean isAsynchronousDelivery;
	private String crcHost;
	private int crcPort;
	private String federationName;
	private String federateName;
	private String federateType;
	private File fomDirectory;
	private boolean isRealtime;
	private boolean isTimeConstrained;
	private boolean isTimeRegulating;
	private DateTime simulationEphoc;
	
	protected Configuration() {}

	public boolean isAsynchronousDelivery() {
		return isAsynchronousDelivery;
	}


	public void setAsynchronousDelivery(boolean isAsynchronousDelivery) {
		this.isAsynchronousDelivery = isAsynchronousDelivery;
	}

	@Override
	public String getCrcHost() {
		return crcHost;
	}

	@Override
	public void setCrcHost(String crcHost) {
		if( crcHost == null || (!crcHost.equalsIgnoreCase("localhost") && !ipIsValid(crcHost)) )
			throw new IllegalArgumentException("Invalid IP Address");
		this.crcHost = crcHost;
	}

	@Override
	public int getCrcPort() {
		return crcPort;
	}

	@Override
	public void setCrcPort(int crcPort) {
		if(crcPort < 1024 || crcPort > 49151)
			throw new IllegalArgumentException("Invalid CRC Port, The CRC Port must be in the range [1024 - 49151]");
		this.crcPort = crcPort;
	}

	@Override
	public String getFederationName() {
		return federationName;
	}

	@Override
	public void setFederationName(String federationName) {
		this.federationName = federationName;
	}

	@Override
	public String getFederateName() {
		return federateName;
	}

	@Override
	public void setFederateName(String federateName) {
		this.federateName = federateName;
	}

	@Override
	public String getFederateType() {
		return federateType;
	}

	@Override
	public void setFederateType(String federateType) {
		this.federateType = federateType;
	}

	@Override
	public File getFomDirectory() {
		return fomDirectory;
	}

	@Override
	public void setFomDirectory(File fomDirectory) {
		if(fomDirectory == null || !fomDirectory.isDirectory())
			throw new IllegalArgumentException("The parameter must be a directory");
		this.fomDirectory = fomDirectory;
	}

	@Override
	public boolean isRealtime() {
		return isRealtime;
	}

	@Override
	public void setRealtime(boolean isRealtime) {
		this.isRealtime = isRealtime;
	}

	@Override
	public boolean isTimeConstrained() {
		return isTimeConstrained;
	}

	@Override
	public void setTimeConstrained(boolean isTimeConstrained) {
		this.isTimeConstrained = isTimeConstrained;
	}

	@Override
	public boolean isTimeRegulating() {
		return isTimeRegulating;
	}

	@Override
	public void setTimeRegulating(boolean isTimeRegulating) {
		this.isTimeRegulating = isTimeRegulating;
	}
	
	private boolean ipIsValid(String ip){          

	      Pattern pattern = Pattern.compile(IP_PATTERN);
	      Matcher matcher = pattern.matcher(ip);
	      return matcher.matches();             
	}

	@Override
	public DateTime getSimulationEphoc() {
		return simulationEphoc;
	}

	@Override
	public void setSimulationEphoc(DateTime simulationEphoc) {
		this.simulationEphoc = simulationEphoc;
	}
	
}
