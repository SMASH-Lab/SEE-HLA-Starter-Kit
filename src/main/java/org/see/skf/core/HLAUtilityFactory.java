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

package org.see.skf.core;

import hla.rti1516_2025.RTIambassador;
import hla.rti1516_2025.RtiFactory;
import hla.rti1516_2025.RtiFactoryFactory;
import hla.rti1516_2025.encoding.EncoderFactory;
import hla.rti1516_2025.exceptions.FederateNotExecutionMember;
import hla.rti1516_2025.exceptions.NotConnected;
import hla.rti1516_2025.exceptions.RTIinternalError;
import hla.rti1516_2025.time.HLAinteger64TimeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thread-safe singleton object that encapsulates the RtiFactory, RTIAmbassador, EncoderFactory, and HLAinteger64TimeFactory
 * objects used by the framework.
 *
 * @since 2.0
 */
public enum HLAUtilityFactory {
    INSTANCE;

    private final RtiFactory rtiFactory;
    private RTIambassador rtiAmbassador;
    private EncoderFactory encoderFactory;
    private HLAinteger64TimeFactory timeFactory;

    HLAUtilityFactory() {
        Logger logger = LoggerFactory.getLogger(HLAUtilityFactory.class);

        try {
            rtiFactory = RtiFactoryFactory.getRtiFactory();
            rtiAmbassador = rtiFactory.getRtiAmbassador();
            String rtiName = rtiFactory.rtiName();
            String rtiVersion = rtiFactory.rtiVersion();
            logger.info("RTI Name: {}, Version: {}, Standard: {}", (rtiName != null) ? rtiName : "Unknown",
                    (rtiVersion !=  null) ? rtiVersion : "Unknown", rtiAmbassador.getHLAversion());
        } catch (Exception e) {
            logger.error("Failed to initialize one or more HLA utility objects required by the federate to function.");
            throw new IllegalStateException(e);
        }
    }

    public RTIambassador getRtiAmbassador() {
        if (rtiAmbassador == null) {
            try {
                rtiAmbassador = rtiFactory.getRtiAmbassador();
            } catch (RTIinternalError e) {
                throw new IllegalStateException("Failed to get RTI Ambassador instance.", e);
            }
        }
        return rtiAmbassador;
    }

    public EncoderFactory getEncoderFactory() {
        if (encoderFactory == null) {
            try {
                encoderFactory = rtiFactory.getEncoderFactory();
            } catch (RTIinternalError e) {
                throw new IllegalStateException("Failed to get EncoderFactory instance.", e);
            }
        }

        return encoderFactory;
    }

    public HLAinteger64TimeFactory getTimeFactory() {
        if (timeFactory == null) {
            try {
                timeFactory = (HLAinteger64TimeFactory) rtiAmbassador.getTimeFactory();
            } catch (FederateNotExecutionMember | NotConnected e) {
                throw new IllegalStateException("Failed to acquire HLAinteger64TimeFactory instance.", e);
            }
        }
        return timeFactory;
    }
}
