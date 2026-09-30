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

import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.numbers.quaternion.Quaternion;
import org.see.skf.encoding.Coder;

public class SpaceTimeCoordinateStateCoder implements Coder<SpaceTimeCoordinateState> {
    private final HLAfixedRecord coder;

    // Translation state component.
    private final HLAfixedRecord translationalState;
    private final HLAfixedArray<HLAfloat64LE> position;
    private final HLAfixedArray<HLAfloat64LE> velocity;

    // Rotational state component.
    private final HLAfixedRecord rotationalState;
    private final HLAfixedArray<HLAfloat64LE> angularVelocity;
    private final HLAfixedRecord attitudeQuaternion;
    private final HLAfloat64LE scalar;
    private final HLAfixedArray<HLAfloat64LE> vector;

    // Simulated time component.
    private final HLAfloat64LE time;

    public SpaceTimeCoordinateStateCoder(EncoderFactory encoderFactory) {

        this.coder = encoderFactory.createHLAfixedRecord();

        this.translationalState = encoderFactory.createHLAfixedRecord();
        this.position = encoderFactory.createHLAfixedArray(encoderFactory.createHLAfloat64LE(), encoderFactory.createHLAfloat64LE(), encoderFactory.createHLAfloat64LE());
        this.velocity = encoderFactory.createHLAfixedArray(encoderFactory.createHLAfloat64LE(), encoderFactory.createHLAfloat64LE(), encoderFactory.createHLAfloat64LE());
        this.translationalState.add(this.position);
        this.translationalState.add(this.velocity);

        this.rotationalState = encoderFactory.createHLAfixedRecord();
        this.angularVelocity = encoderFactory.createHLAfixedArray(encoderFactory.createHLAfloat64LE(), encoderFactory.createHLAfloat64LE(), encoderFactory.createHLAfloat64LE());
        this.attitudeQuaternion = encoderFactory.createHLAfixedRecord();
        this.scalar = encoderFactory.createHLAfloat64LE();
        this.vector = encoderFactory.createHLAfixedArray(encoderFactory.createHLAfloat64LE(), encoderFactory.createHLAfloat64LE(), encoderFactory.createHLAfloat64LE());
        this.attitudeQuaternion.add(this.scalar);
        this.attitudeQuaternion.add(this.vector);
        this.rotationalState.add(this.attitudeQuaternion);
        this.rotationalState.add(this.angularVelocity);

        this.time = encoderFactory.createHLAfloat64LE();

        this.coder.add(this.translationalState);
        this.coder.add(this.rotationalState);
        this.coder.add(time);
    }

    @Override
    public SpaceTimeCoordinateState decode(byte[] bytes) throws DecoderException {
        this.coder.decode(bytes);

        Vector3D decodedPosition = Vector3D.of(this.position.get(0).getValue(), this.position.get(1).getValue(), this.position.get(2).getValue());
        Vector3D decodedVelocity = Vector3D.of(this.velocity.get(0).getValue(), this.velocity.get(1).getValue(), this.velocity.get(2).getValue());

        double decodedScalar = this.scalar.getValue();
        Vector3D decodedVector = Vector3D.of(this.vector.get(0).getValue(), this.vector.get(1).getValue(), this.vector.get(2).getValue());
        Vector3D decodedAngularVelocity = Vector3D.of(this.angularVelocity.get(0).getValue(), this.angularVelocity.get(1).getValue(), this.angularVelocity.get(2).getValue());
        Quaternion decodedAttitudeQuaternion = Quaternion.of(decodedScalar, decodedVector.getX(), decodedVector.getY(), decodedVector.getZ());

        double decodedTime = this.time.getValue();

        SpaceTimeCoordinateState state = new SpaceTimeCoordinateState();
        state.setPosition(decodedPosition);
        state.setVelocity(decodedVelocity);
        state.setAttitudeQuaternion(decodedAttitudeQuaternion);
        state.setAngularVelocity(decodedAngularVelocity);
        state.setTime(decodedTime);

        return state;
    }

    @Override
    public byte[] encode(SpaceTimeCoordinateState state) {
        this.position.get(0).setValue(state.getPosition().getX());
        this.position.get(1).setValue(state.getPosition().getY());
        this.position.get(2).setValue(state.getPosition().getZ());

        this.velocity.get(0).setValue(state.getVelocity().getX());
        this.velocity.get(1).setValue(state.getVelocity().getY());
        this.velocity.get(2).setValue(state.getVelocity().getZ());

        this.scalar.setValue(state.getAttitudeQuaternion().getW());
        this.vector.get(0).setValue(state.getAttitudeQuaternion().getX());
        this.vector.get(1).setValue(state.getAttitudeQuaternion().getY());
        this.vector.get(2).setValue(state.getAttitudeQuaternion().getZ());

        this.angularVelocity.get(0).setValue(state.getAngularVelocity().getX());
        this.angularVelocity.get(1).setValue(state.getAngularVelocity().getY());
        this.angularVelocity.get(2).setValue(state.getAngularVelocity().getZ());

        this.time.setValue(state.getTime());

        return this.coder.toByteArray();
    }
}
