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

package org.see.skf.conf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

/**
 * Implementation of the FederateConfiguration to use the Property file format.
 *
 * @since 2.0
 */
public final class PropertyFileConfiguration implements FederateConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(PropertyFileConfiguration.class);

    private static final String RTI_ADDRESS = "rtiAddress";
    private static final String FEDERATION_NAME = "federationName";
    private static final String FEDERATE_NAME = "federateName";
    private static final String FEDERATE_TYPE = "federateType";
    private static final String FEDERATE_ROLE = "federateRole";
    private static final String LOOK_AHEAD = "lookahead";
    private static final String FOM_DIRECTORY = "fomDirectory";
    private static final String ASYNC_DELIVERY = "asynchronousDelivery";
    private static final String TIME_REGULATING = "timeRegulating";
    private static final String TIME_CONSTRAINED = "timeConstrained";

    // Mandatory properties
    private final String rtiAddress;
    private final String federationName;
    private final String federateName;
    private final String federateType;
    private final String federateRole;
    private final Long lookAhead;
    private final boolean asynchronousDelivery;
    private final boolean timeConstrained;
    private final boolean timeRegulating;

    // Optional properties
    private final String[] additionalFomModules;

    public PropertyFileConfiguration(File confFile) {
        Properties properties = new Properties();

        try (FileInputStream inputStream = new FileInputStream(confFile)) {
            properties.load(inputStream);
        } catch (IOException e) {
            logger.error("Error while trying to read configuration file.");
            throw new IllegalStateException(e);
        }

        rtiAddress = properties.getProperty(RTI_ADDRESS);
        federationName = properties.getProperty(FEDERATION_NAME);
        federateName = properties.getProperty(FEDERATE_NAME);
        federateType = properties.getProperty(FEDERATE_TYPE);
        federateRole = properties.getProperty(FEDERATE_ROLE);
        asynchronousDelivery = Boolean.parseBoolean(properties.getProperty(ASYNC_DELIVERY));
        timeConstrained = Boolean.parseBoolean(properties.getProperty(TIME_CONSTRAINED));
        timeRegulating = Boolean.parseBoolean(properties.getProperty(TIME_REGULATING));

        String fomDirectory = properties.getProperty(FOM_DIRECTORY);
        additionalFomModules = loadFomModulePaths(fomDirectory);

        try {
            lookAhead = Long.parseLong(properties.getProperty(LOOK_AHEAD));
        }  catch (NumberFormatException conversionFailure) {
            logger.error("Only integer values are accepted for the <lookahead> parameter");
            throw new NumberFormatException(conversionFailure.getMessage());
        }

        validateProperties();
    }

    private String[] loadFomModulePaths(String fomDirectory) {
        if (fomDirectory != null) {
            File directory = new File(fomDirectory.replace("\"", ""));

            if (directory.exists() && directory.isDirectory()) {
                File[] fomModules = directory.listFiles();

                if (fomModules != null) {
                    ArrayList<String> pathList = new ArrayList<>();
                    for (File fomModule : fomModules) {
                        pathList.add(fomModule.getAbsolutePath());
                    }

                    return pathList.toArray(new String[0]);
                }
            }
        }

        return new String[0];
    }

    private void validateProperties() {
        if (rtiAddress == null) {
            throw new IllegalStateException("The <rtiAddress> property is missing.");
        }

        if (federationName == null) {
            throw new IllegalStateException("The <federationName> property is missing.");
        }

        if (federateName == null) {
            throw new IllegalStateException("The <federateName> property is missing.");
        }

        if (federateType == null) {
            throw new IllegalStateException("The <federateType> property is missing.");
        }

        if (federateRole == null) {
            throw new IllegalStateException("The <federateRole> property is missing.");
        }

        if (lookAhead == null) {
            throw new IllegalStateException("The <lookahead> property is missing.");
        }
    }

    @Override
    public String rtiAddress() {
        return rtiAddress;
    }

    @Override
    public String federateName() {
        return federateName;
    }

    @Override
    public String federateType() {
        return federateType;
    }

    @Override
    public String federationName() {
        return federationName;
    }

    @Override
    public String federateRole() {
        return federateRole;
    }

    @Override
    public String[] additionalFomModules() {
        return additionalFomModules;
    }

    @Override
    public boolean asynchronousDelivery() {
        return asynchronousDelivery;
    }

    @Override
    public boolean timeConstrained() {
        return timeConstrained;
    }

    @Override
    public boolean timeRegulating() {
        return timeRegulating;
    }

    @Override
    public Long lookAhead() {
        return lookAhead;
    }
}
