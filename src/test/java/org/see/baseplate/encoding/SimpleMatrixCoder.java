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

import hla.rti1516_2025.encoding.DecoderException;
import hla.rti1516_2025.encoding.EncoderFactory;
import hla.rti1516_2025.encoding.HLAfixedArray;
import hla.rti1516_2025.encoding.HLAfloat64LE;
import org.ejml.simple.SimpleMatrix;
import org.see.skf.encoding.Coder;

public class SimpleMatrixCoder implements Coder<SimpleMatrix> {

    private final HLAfixedArray<HLAfloat64LE> coder;

    public SimpleMatrixCoder(EncoderFactory encoderFactory) {
        this.coder = encoderFactory.createHLAfixedArray(
                encoderFactory.createHLAfloat64LE(),
                encoderFactory.createHLAfloat64LE(),
                encoderFactory.createHLAfloat64LE(),
                encoderFactory.createHLAfloat64LE(),
                encoderFactory.createHLAfloat64LE(),
                encoderFactory.createHLAfloat64LE(),
                encoderFactory.createHLAfloat64LE(),
                encoderFactory.createHLAfloat64LE(),
                encoderFactory.createHLAfloat64LE()
        );
    }

    @Override
    public SimpleMatrix decode(byte[] bytes) throws DecoderException {
        this.coder.decode(bytes);

        SimpleMatrix matrix = new SimpleMatrix(3, 3);
        matrix.set(0, coder.get(0).getValue());
        matrix.set(1, coder.get(1).getValue());
        matrix.set(2, coder.get(2).getValue());
        matrix.set(3, coder.get(3).getValue());
        matrix.set(4, coder.get(4).getValue());
        matrix.set(5, coder.get(5).getValue());
        matrix.set(6, coder.get(6).getValue());
        matrix.set(7, coder.get(7).getValue());
        matrix.set(8, coder.get(8).getValue());

        return matrix;
    }

    @Override
    public byte[] encode(SimpleMatrix matrix) {
        coder.get(0).setValue(matrix.get(0));
        coder.get(1).setValue(matrix.get(1));
        coder.get(2).setValue(matrix.get(2));
        coder.get(3).setValue(matrix.get(3));
        coder.get(4).setValue(matrix.get(4));
        coder.get(5).setValue(matrix.get(5));
        coder.get(6).setValue(matrix.get(6));
        coder.get(7).setValue(matrix.get(7));
        coder.get(8).setValue(matrix.get(8));

        return coder.toByteArray();
    }
}
