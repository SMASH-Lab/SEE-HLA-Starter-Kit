/*****************************************************************
 SEE HLA Starter Kit Framework -  A Java framework for developing
 SRFOM-compliant HLA Federates in the Simulation Exploration
 Experience (SEE) program.

 Copyright (c) 2014, 2026 SMASH Lab - University of Calabria
 (Italy), Hridyanshu Aatreya - Modelling & Simulation Group (MSG)
 at Brunel University of London (UK). All rights reserved.

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

package org.see.skf.internal;

import hla.rti1516_2025.FederateHandle;
import hla.rti1516_2025.RTIambassador;
import hla.rti1516_2025.exceptions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public final class FederateMapping {

    private static final Logger logger = LoggerFactory.getLogger(FederateMapping.class);

    private final RTIambassador rtiAmbassador;

    private final Map<FederateHandle, String> handleToName;

    public FederateMapping() {
        rtiAmbassador = HLAUtilityFactory.INSTANCE.getRtiAmbassador();
        this.handleToName = new HashMap<>();
    }

    public String get(FederateHandle handle) {
        this.handleToName.computeIfAbsent(handle, h -> {
            try {
                return this.rtiAmbassador.getFederateName(handle);
            } catch (InvalidFederateHandle | FederateHandleNotKnown | FederateNotExecutionMember | NotConnected |
                     RTIinternalError e) {
                logger.error("Name of the federate using the handle <{}> could not be retrieved from the RTI.", handle, e);
                return null;
            }
        });

        return this.handleToName.get(handle);
    }
}
