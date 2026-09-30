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
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.see.skf.encoding.Coder;

public class Vector3DCoder implements Coder<Vector3D> {

    private final HLAfixedArray<HLAfloat64LE> coder;

    public Vector3DCoder(EncoderFactory encoderFactory) {
        coder = encoderFactory.createHLAfixedArray(
                encoderFactory.createHLAfloat64LE(),
                encoderFactory.createHLAfloat64LE(),
                encoderFactory.createHLAfloat64LE()
        );
    }

    @Override
    public Vector3D decode(byte[] bytes) throws DecoderException {
        coder.decode(bytes);
        return Vector3D.of(coder.get(0).getValue(), coder.get(1).getValue(), coder.get(2).getValue());
    }

    @Override
    public byte[] encode(Vector3D vector) {
        coder.get(0).setValue(vector.getX());
        coder.get(1).setValue(vector.getY());
        coder.get(2).setValue(vector.getZ());

        return coder.toByteArray();
    }
}
