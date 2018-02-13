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

import java.util.Calendar;
import java.util.TimeZone;

import jodd.datetime.JDateTime;
import jodd.datetime.JulianDateStamp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import skf.utility.JulianDateType;
import skf.utility.TimeUnit;
import skf.utility.TimeUtility;
import hla.rti1516e.LogicalTime;
import hla.rti1516e.LogicalTimeFactory;
import hla.rti1516e.LogicalTimeInterval;
import hla.rti1516e.exceptions.FederateNotExecutionMember;
import hla.rti1516e.exceptions.IllegalTimeArithmetic;
import hla.rti1516e.exceptions.InvalidLogicalTimeInterval;
import hla.rti1516e.exceptions.NotConnected;
import hla.rti1516e.exceptions.RTIinternalError;
import hla.rti1516e.time.HLAinteger64Interval;
import hla.rti1516e.time.HLAinteger64Time;
import hla.rti1516e.time.HLAinteger64TimeFactory;

public class Time {

	private static final Logger logger = LogManager.getLogger(Time.class);

	private final JDateTime simulationScenarioTimeEphoc; // julianDate rapresentation of 'simulationScenarioTimeEphoc'

	private HLAinteger64Interval federationLookaheadInterval = null;

	private HLAinteger64Time federationTime = null;

	private HLAinteger64Time federateTime;
	private long executionCounter;

	/* Federate Join Time and date*/
	private long federateJoinHLALogicalTimeAsValue = -1L;
	private JDateTime federateJoinHLALogicalTimeAsJDateTime = null;

	private HLAinteger64TimeFactory timeFactory = null;


	protected Time(double simulationScenarioTimeEphoc) throws FederateNotExecutionMember, NotConnected, RTIinternalError {

		JulianDateStamp jdts = new JulianDateStamp();
		jdts.setTruncatedJulianDate(simulationScenarioTimeEphoc);
		this.simulationScenarioTimeEphoc = new JDateTime(jdts);
		this.simulationScenarioTimeEphoc.setTimeZone(TimeZone.getTimeZone("UTC"));

		this.timeFactory = (HLAinteger64TimeFactory) SEERTIAmbassador.getInstance().getTimeFactory();

	}

	@SuppressWarnings("rawtypes")
	protected void setFederationLogicalTime(LogicalTime federationTime) {
		this.federationTime = (HLAinteger64Time) federationTime;
	}

	@SuppressWarnings("rawtypes")
	protected LogicalTime getFederationLogicalTime() {
		return federationTime;
	}

	@SuppressWarnings("rawtypes")
	protected LogicalTimeInterval getFederationLookaheadInterval() {
		return federationLookaheadInterval;
	}

	protected void setFederationLookaheadInterval(HLAinteger64Interval federationLookaheadInterval) {
		this.federationLookaheadInterval = federationLookaheadInterval;
	}

	@SuppressWarnings("rawtypes")
	protected void setFederateLogicalTime(LogicalTime federateTime) {
		this.federateTime = (HLAinteger64Time) federateTime;
	}

	@SuppressWarnings("rawtypes")
	protected LogicalTime getFederateLogicalTime() {
		return this.federateTime;
	}

	protected void initializeFederationLogicalTime() {
		this.federationTime = timeFactory.makeInitial();
	}

	protected void initializeFederationLookaheadInterval(long lookahead) {
		this.federationLookaheadInterval = timeFactory.makeInterval(lookahead);

	}

	@SuppressWarnings("rawtypes")
	protected LogicalTime nextTimeStep() throws IllegalTimeArithmetic, InvalidLogicalTimeInterval {
		this.federationTime = this.federationTime.add(federationLookaheadInterval);
		return federationTime;
	}

	public long getFederateTimeCycle() {
		return executionCounter;
	}

	protected void setFederateTimeCycle(long executionCounter) {
		this.executionCounter = executionCounter;
	}

	@SuppressWarnings("rawtypes")
	public LogicalTimeFactory getTimeFactory() {
		return timeFactory;

	}

	public long getFederateJoinHLALogicalTimeValue(){
		if(this.federateJoinHLALogicalTimeAsValue <= 0)
			buildFederateJoinTime();

		return federateJoinHLALogicalTimeAsValue;
	}
	
	private void buildFederateJoinTime(){
		//JdateTime
		this.federateJoinHLALogicalTimeAsJDateTime = this.simulationScenarioTimeEphoc.clone();
		int milliseconds = (int) TimeUtility.convert(federationTime.getValue() - getFederateTimeCycle(), 
				TimeUnit.MICROSECONDS, TimeUnit.MILLISECONDS);
		this.federateJoinHLALogicalTimeAsJDateTime = federateJoinHLALogicalTimeAsJDateTime.addMillisecond(milliseconds);
		
		//HLALogicalTime
		federateJoinHLALogicalTimeAsValue = federationTime.getValue() - getFederateTimeCycle();
	}

	public Calendar getFederateJoinTimeAsCalendar(){

		if(this.federateJoinHLALogicalTimeAsValue <= 0)
			buildFederateJoinTime();
		
		Calendar cal = federateJoinHLALogicalTimeAsJDateTime.convertToCalendar();
		cal.setTimeZone(TimeZone.getTimeZone("UTC"));

		return cal;
	}
	
	public double getFederateJoinTimeAsJulianDate(JulianDateType juliantype) {

		if(this.federateJoinHLALogicalTimeAsValue <= 0)
			buildFederateJoinTime();
		
		return getJulianValue(federateJoinHLALogicalTimeAsJDateTime, juliantype);
		
	}
	
	public long getSimulationScenarioTime(){
		return getFederateJoinHLALogicalTimeValue() + getFederateTimeCycle();
	}
	
	public long getFederationTimeCycle(){
		return federationTime.getValue();
	}
	

	public double getFederationTimeAsJulianDate(JulianDateType juliantype) {
		
		JDateTime clone = this.simulationScenarioTimeEphoc.clone();
		int milliseconds = (int) TimeUtility.convert(federationTime.getValue(), 
				TimeUnit.MICROSECONDS, TimeUnit.MILLISECONDS);

		JDateTime jdt = clone.addMillisecond(milliseconds);
		return getJulianValue(jdt, juliantype);
	}
	
	
	private double getJulianValue(JDateTime jdt, JulianDateType juliantype){
		
		switch (juliantype) {
		case DATE:
			return jdt.getJulianDate().doubleValue();
		case MODIFIED:
			return jdt.getJulianDate().getModifiedJulianDate().doubleValue();
		case REDUCED:
			return jdt.getJulianDate().getReducedJulianDate().doubleValue();
		case TRUNCATED:
			return jdt.getJulianDate().getTruncatedJulianDate().doubleValue();
		default:
			logger.error("Illegal JulianDateType: "+juliantype);
			throw new IllegalArgumentException("Illegal JulianDateType: "+juliantype);
		}
		
	}

	public Calendar getFederationTimeAsCalendar() {

		JDateTime clone = this.simulationScenarioTimeEphoc.clone();

		int milliseconds = (int) TimeUtility.convert(federationTime.getValue(), 
				TimeUnit.MICROSECONDS, TimeUnit.MILLISECONDS);

		Calendar cal = clone.addMillisecond(milliseconds).convertToCalendar();
		cal.setTimeZone(TimeZone.getTimeZone("UTC"));

		return cal;
	}

}
