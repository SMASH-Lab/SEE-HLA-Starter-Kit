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

package org.see.skf.conf;

import java.io.File;

/**
 * Encapsulation of the set of parameters used to set up the federate with specific settings in the
 * federation execution.
 *
 * @since 2.0
 */
public interface FederateConfiguration {
    /**
     * The address of the machine hosting the Central Runtime Component (CRC).
     * It can be expressed as 'localhost', 'localhost:8989', or '192.168.1.62'.
     * When using Pitch Booster, the address should be of the format "MyCRCname@192.168.1.70:8688".
     * @return The address of the CRC.
     */
    String rtiAddress();

    /**
     * Name to be assumed by the federate.
     * @return Federate name.
     */
    String federateName();

    /**
     * THe type of the federate.
     * @return Federate type.
     */
    String federateType();

    /**
     * Name of the federation that the federate needs to join.
     * @return Federation name.
     */
    String federationName();

    /**
     * Role played by the federate according to the SpaceFOM standard classification.
     * @return EARLY if the federate is an early joiner; LATE if the federate is a late-joiner.
     */
    String federateRole();

    /**
     * An array of strings containing the path names or URLsof the FOM modules that are required by this federate to
     * function in the federation execution.
     * @return A file object representing the directory; null if no FOM modules are used.
     */
    String[] additionalFomModules();

    /**
     * Is asynchronous delivery turned on for this federate i.e., the RTI is allowed to deliver Receive Order
     * messages in the Granted State.
     * @return true if asynchronous delivery has been enabled for this federate.
     */
    boolean asynchronousDelivery();

    /**
     * Is the federate's actions constrained by HLA time management mechanisms.
     * @return true if the federate is time constrained.
     */
    boolean timeConstrained();

    /**
     * Is the federate time regulated i.e., able to send timestamped events.
     * @return true if the federate is time regulated.
     */
    boolean timeRegulating();

    /**
     * The lookahead value used in this federation i.e., the permitted time window into the future during which the
     * federate must take into account when sending messages.
     * @return the federation execution's look ahead value.
     */
    Long lookAhead();

    class Factory {
        private Factory() {}

        public static PropertyFileConfiguration create(File confFile) {
            return new PropertyFileConfiguration(confFile);
        }
    }
}
