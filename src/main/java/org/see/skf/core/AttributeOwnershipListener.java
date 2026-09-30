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

import java.util.Set;

/**
 * The listener interface for receiving attribute ownership transfer events. The class that is interested in processing
 * an attribute ownership transfer callback implements this interface. The object created with that class is then registered
 * with an implementation of the SKFederate interface.
 *
 * @since 2.1
 */
public interface AttributeOwnershipListener {

    /**
     * Invoked when a requestAttributeOwnershipRelease callback is received by the federate.
     *
     * @param candidateAttributeNames Names of the attributes whose release is requested
     */
    void releaseRequested(Set<String> candidateAttributeNames);

    /**
     * Invoked when an attributeOwnershipAcquisitionNotification or attributeOwnershipUnavailable callback is received by the federate.
     *
     * @param outcome true if attributeOwnershipAcquisitionNotification was received (implying success) and false if attributeOwnershipUnavailable was received.
     * @param securedAttributeNames Names of the attributes concerned with this event
     */
    void secured(boolean outcome, Set<String> securedAttributeNames);

}
