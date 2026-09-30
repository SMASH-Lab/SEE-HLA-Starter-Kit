/*****************************************************************
 SEE Baseplate - A starter project template for the SEE HLA
 Starter Kit Framework.
 Copyright (c) 2026, Hridyanshu Aatreya - Modelling & Simulation
 Group (MSG) at Brunel University of London. All rights reserved.

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

package org.see.baseplate.encoding;

import hla.rti1516_2025.encoding.*;
import org.apache.commons.numbers.quaternion.Quaternion;
import org.see.skf.encoding.Coder;

public class QuaternionCoder implements Coder<Quaternion> {

    private final HLAfixedRecord coder;

    private final HLAfloat64LE scalar;
    private final HLAfixedArray<HLAfloat64LE> vector;

    public QuaternionCoder(EncoderFactory encoderFactory) {
        this.coder = encoderFactory.createHLAfixedRecord();

        this.scalar = encoderFactory.createHLAfloat64LE();
        this.vector = encoderFactory.createHLAfixedArray(encoderFactory.createHLAfloat64LE(), encoderFactory.createHLAfloat64LE(), encoderFactory.createHLAfloat64LE());
        this.coder.add(this.scalar);
        this.coder.add(this.vector);
    }

    @Override
    public Quaternion decode(byte[] bytes) throws DecoderException {
        this.coder.decode(bytes);
        return Quaternion.of(this.scalar.getValue(), this.vector.get(0).getValue(), this.vector.get(1).getValue(), this.vector.get(2).getValue());
    }

    @Override
    public byte[] encode(Quaternion quaternion) {
        this.scalar.setValue(quaternion.getW());
        this.vector.get(0).setValue(quaternion.getX());
        this.vector.get(1).setValue(quaternion.getY());
        this.vector.get(2).setValue(quaternion.getZ());

        return this.coder.toByteArray();
    }
}
