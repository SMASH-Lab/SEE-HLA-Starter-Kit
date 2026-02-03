/*****************************************************************
 SEE HLA Starter Kit Framework -  A Java library that supports
 the development of HLA Federates in the Simulation Exploration
 Experience (SEE) program.

 Copyright (c) 2014, 2026 SMASH Lab - University of Calabria
 (Italy), Hridyanshu Aatreya - Modelling & Simulation Group (MSG)
 at Brunel University of London. All rights reserved.

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

package org.see.skf.time;

import hla.rti1516_2025.exceptions.IllegalTimeArithmetic;
import hla.rti1516_2025.time.*;
import org.see.skf.core.HLAUtilityFactory;

public final class Time {
    private double sstEpoch;

    private HLAinteger64Interval lookAhead;
    private HLAinteger64Time federationTime;
    private HLAinteger64Time federateTime;

    private long timeCyclesExecuted;

    public HLAinteger64Time nextTimeStep() throws IllegalTimeArithmetic {
        federationTime = federationTime.add(lookAhead);
        return federationTime;
    }

    public HLAinteger64Time getFederationLogicalTime() {
        return federationTime;
    }

    public void setFederationLogicalTime(LogicalTime<?, ?> newTime) {
        federationTime = (HLAinteger64Time) newTime;
    }

    public HLAinteger64Time getFederateLogicalTime() {
        return federateTime;
    }

    public void setFederateLogicalTime(LogicalTime<?, ?> federateTime) {
        this.federateTime = (HLAinteger64Time) federateTime;
    }

    public LogicalTimeInterval<HLAinteger64Interval> getLookAheadAsLogicalTime() {
        return lookAhead;
    }

    public long getLookAhead() {
        return lookAhead.getValue();
    }

    public void setLookAhead(long lookAheadInterval) {
        HLAinteger64TimeFactory timeFactory = HLAUtilityFactory.INSTANCE.getTimeFactory();
        lookAhead = timeFactory.makeInterval(lookAheadInterval);
    }

    public double getSstEpoch() {
        return sstEpoch;
    }

    public void setSstEpoch(double sstEpoch) {
        this.sstEpoch = sstEpoch;
    }

    public long getTimeCyclesExecuted() {
        return timeCyclesExecuted;
    }

    public void setTimeCyclesExecuted(long value) {
        timeCyclesExecuted = value;
    }
}
