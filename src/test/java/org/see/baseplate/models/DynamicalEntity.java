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

package org.see.baseplate.models;

import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.ejml.simple.SimpleMatrix;
import org.see.baseplate.encoding.SimpleMatrixCoder;
import org.see.baseplate.encoding.Vector3DCoder;
import org.see.skf.core.annotations.Attribute;
import org.see.skf.core.annotations.ObjectClass;
import org.see.skf.encoding.HLAfloat64LECoder;

@ObjectClass(name = "HLAobjectRoot.PhysicalEntity.DynamicalEntity")
public class DynamicalEntity extends PhysicalEntity {

    @Attribute(name = "force", coder = Vector3DCoder.class)
    private Vector3D force;

    @Attribute(name = "torque", coder = Vector3DCoder.class)
    private Vector3D torque;

    @Attribute(name = "mass", coder = HLAfloat64LECoder.class)
    private Double mass;

    @Attribute(name = "mass_rate", coder = HLAfloat64LECoder.class)
    private Double massRate;

    @Attribute(name = "inertia", coder = SimpleMatrixCoder.class)
    private SimpleMatrix inertia;

    @Attribute(name = "inertia_rate", coder = SimpleMatrixCoder.class)
    private SimpleMatrix inertiaRate;

    public DynamicalEntity() {
        force = Vector3D.of(0, 0, 0);
        torque = Vector3D.of(0, 0, 0);
        mass = 0.0;
        massRate = 0.0;
        inertia = new SimpleMatrix(3, 3);
        inertiaRate = new SimpleMatrix(3, 3);
    }

    public Vector3D getForce() {
        return this.force;
    }

    public void setForce(Vector3D force) {
        this.force = force;
    }

    public Vector3D getTorque() {
        return this.torque;
    }

    public void setTorque(Vector3D torque) {
        this.torque = torque;
    }

    public Double getMass() {
        return this.mass;
    }

    public void setMass(Double mass) {
        this.mass = mass;
    }

    public Double getMassRate() {
        return this.massRate;
    }

    public void setMassRate(Double massRate) {
        this.massRate = massRate;
    }

    public SimpleMatrix getInertia() {
        return this.inertia;
    }

    public void setInertia(SimpleMatrix inertia) {
        this.inertia = inertia;
    }

    public SimpleMatrix getInertiaRate() {
        return this.inertiaRate;
    }

    public void setInertiaRate(SimpleMatrix inertiaRate) {
        this.inertiaRate = inertiaRate;
    }
}
