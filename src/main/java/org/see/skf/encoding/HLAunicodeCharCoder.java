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

package org.see.skf.encoding;

import hla.rti1516_2025.encoding.DecoderException;
import hla.rti1516_2025.encoding.EncoderFactory;
import hla.rti1516_2025.encoding.HLAunicodeChar;

/**
 * Coder implementation for the HLAunicodeChar data type.
 *
 * @see HLAunicodeChar
 * @since 1.0
 */
public final class HLAunicodeCharCoder implements Coder<Short> {

    private final HLAunicodeChar char16Type;

    public HLAunicodeCharCoder(EncoderFactory encoderFactory) {
        this.char16Type = encoderFactory.createHLAunicodeChar();
    }

    @Override
    public Short decode(byte[] buffer) throws DecoderException {
        this.char16Type.decode(buffer);
        return this.char16Type.getValue();
    }

    @Override
    public byte[] encode(Short element) {
        this.char16Type.setValue(element);
        return this.char16Type.toByteArray();
    }
}
