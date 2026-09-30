/*****************************************************************
 SEE HLA Starter Kit Framework -  A Java framework for developing
 SRFOM-compliant HLA Federates in the Simulation Exploration
 Experience (SEE) program.

 Copyright (c) 2014, 2026 SMASH Lab - University of Calabria
 (Italy), Hridyanshu Aatreya - Modelling & Simulation Group (MSG)
 at Brunel University of London (UK). All rights reserved.

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
 * The listener interface for receiving remote object instance events. The class that is interested in processing
 * remote object instance callbacks implements this interface. The object created with that class is then registered
 * with an implementation of the SKFederate interface.
 *
 * @since 2.1
 */
public interface ObjectInstanceListener {

    /**
     * Invoked when a discoverObjectInstance callback is received by the federate.
     *
     * @param producingFederateName Name of the federate that owns this object instance
     */
    void discovered(String producingFederateName);

    /**
     * Invoked when the discovered remote object instance's values have become available. Values of the object instance
     * can then be queried.
     *
     * @param instance The object that represents this object instance
     */
    void initialized(Object instance);

    /**
     * Invoked when the removeObjectInstance callback is received. This implies that the remote object instance has been
     * deleted. It is recommended to avoid using any previous references held to the deleted object instance as the values
     * will be stale.
     *
     * @param producingFederateName Name of the federate that deleted this object instance
     */
    void destroyed(String producingFederateName);

}
