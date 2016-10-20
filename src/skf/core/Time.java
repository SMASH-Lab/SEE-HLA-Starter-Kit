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


import jodd.datetime.JDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import skf.utility.JulianDateType;
import skf.utility.TimeUnit;
import skf.utility.TimeUtility;
import hla.rti1516e.exceptions.FederateNotExecutionMember;
import hla.rti1516e.exceptions.IllegalTimeArithmetic;
import hla.rti1516e.exceptions.NotConnected;
import hla.rti1516e.exceptions.RTIinternalError;
import hla.rti1516e.time.HLAinteger64Interval;
import hla.rti1516e.time.HLAinteger64Time;
import hla.rti1516e.time.HLAinteger64TimeFactory;

public class Time {
	
	private static final Logger logger = LogManager.getLogger(Time.class);

	private final long LOOKAHEAD_USEC = 1000000;

	private final DateTime simulationEphoc;

	private HLAinteger64Interval lookaheadInterval = null;
	private HLAinteger64Time logical_time = null;
	private long federateTime;

	private HLAinteger64TimeFactory time_factory = null;
	private long executionCounter;

	private JDateTime julianDate = null;


	protected Time(DateTime simulationEphoc) throws FederateNotExecutionMember, NotConnected, RTIinternalError {

		this.simulationEphoc = new DateTime(simulationEphoc);
		this.time_factory = (HLAinteger64TimeFactory) SEERTIAmbassador.getInstance().getTimeFactory();

	}

	protected void setLogicalTime(HLAinteger64Time time) {
		this.logical_time = time;
	}

	protected HLAinteger64Time getLogicalTime() {
		return logical_time;
	}

	protected HLAinteger64Interval getLookaheadInterval() {
		return lookaheadInterval;
	}

	protected void setLookaheadInterval(HLAinteger64Interval hlAinteger64Interval) {
		this.lookaheadInterval = hlAinteger64Interval;
	}

	protected void setFederateTime(long federateTime) {
		this.federateTime = federateTime;
	}

	protected long getFederateTime() {
		return this.federateTime;
	}

	protected void initializeLogicalTime() {
		this.logical_time = time_factory.makeInitial();
	}

	protected void initializeLookaheadInterval() {
		this.lookaheadInterval = time_factory.makeInterval(LOOKAHEAD_USEC);

	}

	protected HLAinteger64Time nextTimeStep() throws IllegalTimeArithmetic {
		this.logical_time = this.logical_time.add(lookaheadInterval);
		return logical_time;
	}

	public long getFederationExecutionTimeCycle() {
		return logical_time.getValue();
	}

	public long getFederateExecutionTimeCycle() {
		return executionCounter;
	}

	protected void setFederateExecutionTimeCycle(long executionCounter) {
		this.executionCounter = executionCounter;
	}

	public DateTime getFederationExecutionTime() {
		return simulationEphoc.plusMillis((int) Math.round((TimeUtility.convert(this.logical_time.getValue(), TimeUnit.MICROSECONDS, TimeUnit.MILLISECONDS))));
	}

	public double getFederationExecutionTimeInJulianDate(JulianDateType juliantype) {

		if(julianDate == null)
			julianDate = new JDateTime();

		DateTime currTime = simulationEphoc.plusMillis((int) Math.round((TimeUtility.convert(this.logical_time.getValue(), TimeUnit.MICROSECONDS, TimeUnit.MILLISECONDS))));
		julianDate.set(currTime.getYear(), currTime.getMonthOfYear(), currTime.getDayOfMonth(), currTime.getHourOfDay(), currTime.getMinuteOfHour(), currTime.getSecondOfMinute(), currTime.getMillisOfSecond());

		switch (juliantype) {
			case DATE:
				return julianDate.getJulianDateDouble();
			case MODIFIED:
				return julianDate.getJulianDate().getModifiedJulianDate().doubleValue();
			case REDUCED:
				return julianDate.getJulianDate().getReducedJulianDate().doubleValue();
			case TRUNCATED:
				return julianDate.getJulianDate().getTruncatedJulianDate().doubleValue();
			default:
				logger.error("Illegal JulianDateType: "+juliantype);
				throw new IllegalArgumentException("Illegal JulianDateType: "+juliantype);
		}
	}
	
	

}
