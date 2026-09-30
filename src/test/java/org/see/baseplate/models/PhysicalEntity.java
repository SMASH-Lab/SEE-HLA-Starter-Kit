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
import org.apache.commons.numbers.quaternion.Quaternion;
import org.see.baseplate.encoding.QuaternionCoder;
import org.see.baseplate.encoding.SpaceTimeCoordinateState;
import org.see.baseplate.encoding.SpaceTimeCoordinateStateCoder;
import org.see.baseplate.encoding.Vector3DCoder;
import org.see.skf.core.annotations.Attribute;
import org.see.skf.core.annotations.ObjectClass;
import org.see.skf.encoding.HLAunicodeStringCoder;

@ObjectClass(name = "HLAobjectRoot.PhysicalEntity")
public class PhysicalEntity {

    @Attribute(name = "name", coder = HLAunicodeStringCoder.class)
    private String name;

    @Attribute(name = "type", coder = HLAunicodeStringCoder.class)
    private String type;

    @Attribute(name = "status", coder = HLAunicodeStringCoder.class)
    private String status;

    @Attribute(name = "parent_reference_frame", coder = HLAunicodeStringCoder.class)
    private String parentReferenceFrame;

    @Attribute(name = "state", coder = SpaceTimeCoordinateStateCoder.class)
    private SpaceTimeCoordinateState state;

    @Attribute(name = "acceleration", coder = Vector3DCoder.class)
    private Vector3D acceleration;

    @Attribute(name = "rotational_acceleration", coder = Vector3DCoder.class)
    private Vector3D rotationalAcceleration;

    @Attribute(name = "center_of_mass", coder = Vector3DCoder.class)
    private Vector3D centerOfMass;

    @Attribute(name = "body_wrt_structural", coder = QuaternionCoder.class)
    private Quaternion bodyWrtStructural;

    public PhysicalEntity() {
        this.name = "";
        this.type = "";
        this.status = "";
        this.parentReferenceFrame = "";
        this.state = new SpaceTimeCoordinateState();
        this.acceleration = Vector3D.of(0, 0, 0);
        this.rotationalAcceleration = Vector3D.of(0, 0, 0);
        this.centerOfMass = Vector3D.of(0, 0, 0);
        this.bodyWrtStructural = Quaternion.of(0, 0, 0, 0);
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getParentReferenceFrame() {
        return this.parentReferenceFrame;
    }

    public void setParentReferenceFrame(String parentReferenceFrame) {
        this.parentReferenceFrame = parentReferenceFrame;
    }

    public SpaceTimeCoordinateState getState() {
        return this.state;
    }

    public void setState(SpaceTimeCoordinateState state) {
        this.state = state;
    }

    public Vector3D getAcceleration() {
        return this.acceleration;
    }

    public void setAcceleration(Vector3D acceleration) {
        this.acceleration = acceleration;
    }

    public Vector3D getRotationalAcceleration() {
        return this.rotationalAcceleration;
    }

    public void setRotationalAcceleration(Vector3D rotationalAcceleration) {
        this.rotationalAcceleration = rotationalAcceleration;
    }

    public Vector3D getCenterOfMass() {
        return this.centerOfMass;
    }

    public void setCenterOfMass(Vector3D centerOfMass) {
        this.centerOfMass = centerOfMass;
    }

    public Quaternion getBodyWrtStructural() {
        return this.bodyWrtStructural;
    }

    public void setBodyWrtStructural(Quaternion bodyWrtStructural) {
        this.bodyWrtStructural = bodyWrtStructural;
    }
}
