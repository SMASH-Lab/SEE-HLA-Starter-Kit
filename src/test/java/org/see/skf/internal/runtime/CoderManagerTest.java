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

package org.see.skf.internal.runtime;

import org.junit.jupiter.api.Test;
import org.see.skf.encoding.Coder;
import org.see.skf.encoding.HLAbooleanCoder;
import org.see.skf.encoding.HLAunicodeStringCoder;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class CoderManagerTest {
    private final CoderManager coderManager;

    public CoderManagerTest() {
        this.coderManager = new CoderManager();
    }

    @Test
    void testDefaultCoders() {
        assertThrows(NullPointerException.class, () -> coderManager.get(null));
        assertNotNull(coderManager.get(HLAunicodeStringCoder.class));
        assertNotNull(coderManager.get(HLAbooleanCoder.class));
    }

    @Test
    void testInheritance() {
        assertNotNull(coderManager.get(HLAunicodeStringCoderL1.class));
        assertNotNull(coderManager.get(HLAunicodeStringCoderL2.class));
        assertNotNull(coderManager.get(HLAunicodeStringCoderL3.class));
        assertNotNull(coderManager.get(HLAbooleanCoderL1.class));
        assertNotNull(coderManager.get(HLAbooleanCoderL2.class));
    }

    @Test
    void testSerializationMethodInvocations() {
        CoderManager.CoderReflectionData data = this.coderManager.get(HLAunicodeStringCoder.class);
        Coder<?> coder = data.coder();
        Method encodeMethod = data.encodeMethod();
        Method decodeMethod = data.decodeMethod();

        assertDoesNotThrow(() -> {
            Object encodedValue = encodeMethod.invoke(coder, "Hello, World!");
            assertNotNull(encodedValue);
            Object decodedValue = decodeMethod.invoke(coder, encodedValue);
            assertNotNull(decodedValue);
        });
    }

    @Test
    void testFailCases() {
        assertThrows(Exception.class, () -> coderManager.get(InvalidCoder.class));
    }
}