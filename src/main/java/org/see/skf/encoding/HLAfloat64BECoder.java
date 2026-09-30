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
import hla.rti1516_2025.encoding.HLAfloat64BE;

/**
 * Coder implementation for the HLAfloat64BE data type.
 *
 * @see HLAfloat64BE
 * @since 1.0
 */
public final class HLAfloat64BECoder implements Coder<Double> {

    private final HLAfloat64BE float64Type;

    public HLAfloat64BECoder(EncoderFactory encoderFactory) {
        this.float64Type = encoderFactory.createHLAfloat64BE();
    }

    @Override
    public Double decode(byte[] buffer) throws DecoderException {
        this.float64Type.decode(buffer);
        return this.float64Type.getValue();
    }

    @Override
    public byte[] encode(Double element) {
        this.float64Type.setValue(element);
        return this.float64Type.toByteArray();
    }
}
