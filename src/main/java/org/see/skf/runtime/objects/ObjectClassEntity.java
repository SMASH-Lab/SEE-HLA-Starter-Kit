/*****************************************************************
 SEE HLA Starter Kit Framework -  A Java library that supports
 the development of HLA Federates in the Simulation Exploration
 Experience (SEE) program.

 Copyright (c) 2014, 2026 SMASH Lab - University of Calabria
 (Italy), Hridyanshu Aatreya - Modelling & Simulation Group (MSG)
 at Brunel University of London. All rights reserved.

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

package org.see.skf.runtime.objects;

import hla.rti1516_2025.ObjectInstanceHandle;
import org.see.skf.runtime.AbstractClassElement;

public final class ObjectClassEntity extends AbstractClassElement {
    private final String name;
    private final ObjectInstanceHandle handle;
    private final ObjectClassModel model;

    public ObjectClassEntity(String instanceName, ObjectInstanceHandle handle, ObjectClassModel model, Object element) {
        super(element);

        this.name = instanceName;
        this.handle = handle;
        this.model = model;
    }

    public String getName() {
        return name;
    }

    public ObjectClassModel getModel() {
        return model;
    }

    public ObjectInstanceHandle getHandle() {
        return handle;
    }
}
