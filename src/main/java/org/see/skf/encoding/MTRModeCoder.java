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
import hla.rti1516_2025.encoding.HLAinteger16LE;
import org.see.skf.core.MTRMode;

/**
 * Coder implementation for the MTRMode data type.
 *
 * @see MTRMode
 * @since 1.0
 */
public final class MTRModeCoder implements Coder<MTRMode> {

    private final HLAinteger16LE executionModeType;

    public MTRModeCoder(EncoderFactory encoderFactory) {
        this.executionModeType = encoderFactory.createHLAinteger16LE();
    }

    @Override
    public MTRMode decode(byte[] buffer) throws DecoderException {
        this.executionModeType.decode(buffer);
        return MTRMode.query(this.executionModeType.getValue());
    }

    @Override
    public byte[] encode(MTRMode element) {
        this.executionModeType.setValue(element.getValue());
        return this.executionModeType.toByteArray();
    }
}
