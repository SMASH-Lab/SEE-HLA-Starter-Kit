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

import org.see.baseplate.encoding.SpaceTimeCoordinateState;
import org.see.baseplate.encoding.SpaceTimeCoordinateStateCoder;
import org.see.skf.core.annotations.Attribute;
import org.see.skf.core.annotations.ObjectClass;
import org.see.skf.encoding.HLAunicodeStringCoder;

@ObjectClass(name = "HLAobjectRoot.ReferenceFrame")
public class ReferenceFrame {

    @Attribute(name = "name", coder = HLAunicodeStringCoder.class)
    private String name;

    @Attribute(name = "parent_name", coder = HLAunicodeStringCoder.class)
    private String parentName;

    @Attribute(name = "state", coder = SpaceTimeCoordinateStateCoder.class)
    private SpaceTimeCoordinateState state;

    public ReferenceFrame() {
        this.name = "";
        this.parentName = "";
        this.state = new SpaceTimeCoordinateState();
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

    public SpaceTimeCoordinateState getState() {
        return this.state;
    }

    public void setState(SpaceTimeCoordinateState state) {
        this.state = state;
    }
}
