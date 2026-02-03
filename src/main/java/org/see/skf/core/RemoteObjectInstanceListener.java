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

package org.see.skf.core;

/**
 * Base interface for all remote object instance listeners, which receive events whenever a remote object instance has
 * been discovered, and initialized for use by the federate, or if it was deleted and becomes inaccessible.
 */
public interface RemoteObjectInstanceListener {
    /**
     * The federate notifies the listener when <b>any</b> remote object instance has been created.
     * @param name Name of the object instance.
     * @param objectInstanceElement Representation of the object instance.
     */
    void instanceAdded(String name, Object objectInstanceElement);

    /**
     * The federate notifies the listener when a remote object instance has been destroyed.
     * @param name Name of the object instance.
     */
    void instanceRemoved(String name);
}
