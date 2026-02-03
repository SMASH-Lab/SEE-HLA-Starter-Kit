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

package org.see.skf.util.encoding;

import hla.rti1516_2025.encoding.DecoderException;
import hla.rti1516_2025.encoding.EncoderFactory;
import hla.rti1516_2025.encoding.HLAinteger64BE;
import org.see.skf.core.Coder;
import org.see.skf.core.HLAUtilityFactory;

/**
 * Coder for the HLAinteger64BE data type.
 * @since 1.5
 */
public class HLAinteger64BECoder implements Coder<Long> {
    private final HLAinteger64BE coder;

    public HLAinteger64BECoder() {
        EncoderFactory encoderFactory = HLAUtilityFactory.INSTANCE.getEncoderFactory();
        this.coder = encoderFactory.createHLAinteger64BE();
    }

    @Override
    public Long decode(byte[] buffer) throws DecoderException {
        coder.decode(buffer);
        return coder.getValue();
    }

    @Override
    public byte[] encode(Long element) {
        coder.setValue(element);
        return coder.toByteArray();
    }

    @Override
    public Class<Long> getAllowedType() {
        return Long.class;
    }
}
