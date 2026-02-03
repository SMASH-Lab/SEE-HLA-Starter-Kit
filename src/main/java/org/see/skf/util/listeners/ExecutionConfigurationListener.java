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

package org.see.skf.util.listeners;

import org.see.skf.core.RemoteObjectInstanceListener;
import org.see.skf.core.SEEAbstractFederate;
import org.see.skf.util.models.ExecutionConfiguration;

import java.beans.PropertyChangeListener;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Listener for the remote ExCO object instance. It attaches a property listener to watch for changes in execution mode.
 * @since 2.0
 */
public class ExecutionConfigurationListener implements RemoteObjectInstanceListener {
    private final SEEAbstractFederate federate;
    private final AtomicBoolean discoveryWaitFlag;

    public ExecutionConfigurationListener(SEEAbstractFederate federate, AtomicBoolean discoveryWaitFlag) {
        this.federate = federate;
        this.discoveryWaitFlag = discoveryWaitFlag;
    }

    @Override
    public void instanceAdded(String name, Object objectInstanceElement) {
        ExecutionConfiguration exCO = (ExecutionConfiguration) federate.queryRemoteObjectInstance("ExCO");

        if (name.equals("ExCO") && exCO != null) {
            // This property listener will watch for execution mode changes and instruct the federate accordingly.
            PropertyChangeListener executionModeChangeListener = evt -> {
                if (evt.getSource().equals(exCO)) {
                    ExecutionConfiguration.ExecutionMode currentMode = exCO.getCurrentExecutionMode();
                    ExecutionConfiguration.ExecutionMode nextMode = exCO.getNextExecutionMode();

                    if (currentMode == ExecutionConfiguration.ExecutionMode.EXEC_MODE_RUNNING && nextMode == ExecutionConfiguration.ExecutionMode.EXEC_MODE_FREEZE) {
                        federate.freezeExecution();
                    } else if (currentMode == ExecutionConfiguration.ExecutionMode.EXEC_MODE_FREEZE && nextMode == ExecutionConfiguration.ExecutionMode.EXEC_MODE_RUNNING) {
                        federate.resumeExecution();
                    } else if ((currentMode == ExecutionConfiguration.ExecutionMode.EXEC_MODE_FREEZE || currentMode == ExecutionConfiguration.ExecutionMode.EXEC_MODE_RUNNING) && nextMode == ExecutionConfiguration.ExecutionMode.EXEC_MODE_SHUTDOWN) {
                        federate.shutdownExecution();
                    }
                }
            };

            exCO.addPropertyListener(executionModeChangeListener);
            discoveryWaitFlag.set(true);
        }
    }

    @Override
    public void instanceRemoved(String name) {
        if (name.equals("ExCO")) {
            // Until we determine *how* to receive attribute updates for ExCO prior to shut down, the ExCO instance
            // being deleted is the only way to determine if the federation has gone into termination mode.
            federate.shutdownExecution();
        }
    }
}
