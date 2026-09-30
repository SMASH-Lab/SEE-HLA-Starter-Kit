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

import org.see.skf.core.annotations.InteractionClass;
import org.see.skf.core.annotations.Parameter;
import org.see.skf.encoding.MTRModeCoder;

/**
 * The ModeTransitionRequest (MTR) interaction is used by participating federates, that are not the Master Federate, to
 * request a federation execution mode transition. An MTR can be sent at anytime during the initialization or execution
 * but only certain MTR requests are valid at certain times. An instance of this class can be used to request a federation-wide mode transition.
 *
 * @since 1.5
 */
@InteractionClass(name = "HLAinteractionRoot.ModeTransitionRequest")
public final class ModeTransitionRequest {

    @Parameter(name = "execution_mode", coder = MTRModeCoder.class)
    private MTRMode executionMode;

    public ModeTransitionRequest() {
        /* Zero-arg constructor as required by the framework and the JavaBeans standard. */
        this.executionMode = MTRMode.MTR_UNDESIGNATED;
    }

    public MTRMode getExecutionMode() {
        return this.executionMode;
    }

    public void setExecutionMode(MTRMode executionMode) {
        this.executionMode = executionMode;
    }
}
