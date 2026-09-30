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
import org.see.baseplate.encoding.Vector3DCoder;
import org.see.skf.core.annotations.Attribute;
import org.see.skf.core.annotations.ObjectClass;
import org.see.skf.encoding.HLAunicodeStringCoder;

@ObjectClass(name = "HLAobjectRoot.PhysicalInterface")
public class PhysicalInterface {

    @Attribute(name = "name", coder = HLAunicodeStringCoder.class)
    private String name;

    @Attribute(name = "parent_name", coder = HLAunicodeStringCoder.class)
    private String parentName;

    @Attribute(name = "position", coder = Vector3DCoder.class)
    private Vector3D position;

    @Attribute(name = "attitude", coder = QuaternionCoder.class)
    private Quaternion attitude;

    public PhysicalInterface() {
        this.name = "";
        this.parentName = "";
        this.position = Vector3D.of(0, 0, 0);
        this.attitude = Quaternion.of(0, 0, 0, 0);
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParentName() {
        return this.parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public Vector3D getPosition() {
        return this.position;
    }

    public void setPosition(Vector3D position) {
        this.position = position;
    }

    public Quaternion getAttitude() {
        return this.attitude;
    }

    public void setAttitude(Quaternion attitude) {
        this.attitude = attitude;
    }
}
