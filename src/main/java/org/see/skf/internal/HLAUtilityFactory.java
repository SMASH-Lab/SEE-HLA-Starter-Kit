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

import hla.rti1516_2025.RTIambassador;
import hla.rti1516_2025.RtiFactory;
import hla.rti1516_2025.RtiFactoryFactory;
import hla.rti1516_2025.encoding.EncoderFactory;
import hla.rti1516_2025.exceptions.RTIinternalError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum HLAUtilityFactory {
    INSTANCE;

    private static final String FRAMEWORK_VERSION = "2.1.0";

    private final RTIambassador rtiAmbassador;
    private final EncoderFactory encoderFactory;

    HLAUtilityFactory() {
        try {
            RtiFactory rtiFactory = RtiFactoryFactory.getRtiFactory();
            Logger logger = LoggerFactory.getLogger(HLAUtilityFactory.class);
            rtiAmbassador = rtiFactory.getRtiAmbassador();
            encoderFactory = rtiFactory.getEncoderFactory();

            String rtiName = rtiFactory.rtiName();
            String rtiVersion = rtiFactory.rtiVersion();
            String hlaStandard = rtiAmbassador.getHLAversion();
            String jreVersion = System.getProperty("java.version");
            logger.info("SEE HLA Starter Kit Version {}. RTI: {} {}. HLA Standard: {}. JRE: {}", FRAMEWORK_VERSION, rtiName, rtiVersion, hlaStandard, jreVersion);
        } catch (RTIinternalError e) {
            throw new RuntimeException("Failed to initialize one or more HLA utility objects for use by the federate.", e);
        }
    }

    public RTIambassador getRtiAmbassador() {
        return rtiAmbassador;
    }

    public EncoderFactory getEncoderFactory() {
        return encoderFactory;
    }
}
