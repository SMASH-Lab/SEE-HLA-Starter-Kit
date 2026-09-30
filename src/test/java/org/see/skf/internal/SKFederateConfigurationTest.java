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

package org.see.skf.internal;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.see.skf.core.SKFederate;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

class SKFederateConfigurationTest {

    private static final String TEST_CONFIG_RESOURCE_PATH = "src" + File.separator + "test" + File.separator + "resources" + File.separator + "confs" + File.separator;

    private static final File validConf =  new File(TEST_CONFIG_RESOURCE_PATH + "valid.conf");
    private static final File invalidConf1 = new File(TEST_CONFIG_RESOURCE_PATH + "invalid1.conf");
    private static final File invalidConf2 = new File(TEST_CONFIG_RESOURCE_PATH + "invalid2.conf");
    private static final File invalidConf3 = new File(TEST_CONFIG_RESOURCE_PATH + "invalid3.conf");
    private static final File invalidConf4 = new File(TEST_CONFIG_RESOURCE_PATH + "invalid4.conf");
    private static final File invalidConf5 = new File(TEST_CONFIG_RESOURCE_PATH + "invalid5.conf");
    private static final File invalidConf6 = new File(TEST_CONFIG_RESOURCE_PATH + "invalid6.conf");
    private static final File invalidConf7 = new File(TEST_CONFIG_RESOURCE_PATH + "invalid7.conf");

    @Test
    void testValidConfigProperties() {
        SKFederateConfiguration config = new FederatePropertyConfiguration(validConf);

        assertEquals("localhost:8989", config.rtiAddress());
        assertEquals("SEE 2027", config.federationName());
        assertEquals("Spaceport", config.federateName());
        assertEquals("Behavior", config.federateType());
        Assertions.assertEquals(SKFederate.Role.LATE, config.federateRole());
        assertEquals(1000000, config.lookahead());
        assertEquals(8, config.maxThreads());
        assertTrue(areFomModulesValid(config.additionalFomModules()));
    }

    private boolean areFomModulesValid(String[] moduleFilePaths) {
        for (String filePath : moduleFilePaths) {
            File file = new File(filePath);
            if (!file.exists()) {
                return false;
            }
        }

        return true;
    }

    @Test
    void testInvalidConfigProperties() {
        assertThrows(InvalidFederateConfigurationException.class, () -> new FederatePropertyConfiguration(invalidConf1));
        assertThrows(InvalidFederateConfigurationException.class, () -> new FederatePropertyConfiguration(invalidConf2));
        assertThrows(InvalidFederateConfigurationException.class, () -> new FederatePropertyConfiguration(invalidConf3));
        assertThrows(InvalidFederateConfigurationException.class, () -> new FederatePropertyConfiguration(invalidConf4));
        assertThrows(InvalidFederateConfigurationException.class, () -> new FederatePropertyConfiguration(invalidConf5));
        assertThrows(InvalidFederateConfigurationException.class, () -> new FederatePropertyConfiguration(invalidConf6));
        assertThrows(InvalidFederateConfigurationException.class, () -> new FederatePropertyConfiguration(invalidConf7));
    }
}