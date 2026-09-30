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
import hla.rti1516_2025.encoding.HLAinteger64BE;

/**
 * Coder implementation for the HLAinteger64BE data type.
 *
 * @see HLAinteger64BE
 * @since 1.0
 */
public final class HLAinteger64BECoder implements Coder<Long> {

    private final HLAinteger64BE int64Type;

    public HLAinteger64BECoder(EncoderFactory encoderFactory) {
        this.int64Type = encoderFactory.createHLAinteger64BE();
    }

    @Override
    public byte[] encode(Long data) {
        this.int64Type.setValue(data);
        return this.int64Type.toByteArray();
    }

    @Override
    public Long decode(byte[] data) throws DecoderException {
        this.int64Type.decode(data);
        return this.int64Type.getValue();
    }
}
