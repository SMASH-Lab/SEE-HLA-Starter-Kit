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

import hla.rti1516_2025.exceptions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * A SRFOM-compliant late joiner implementation that automatically performs initialization, executive mode transitions, and
 * termination. It is highly recommended that SEE participants utilize this class as the starting point for building
 * federates.
 *
 * @since 2.0
 * @see org.see.skf.core.SKFederate
 */
public abstract class SEEAbstractFederate extends SKAbstractFederate {

    private static final Logger logger = LoggerFactory.getLogger(SEEAbstractFederate.class);
    private static final long OBJECT_DISCOVERY_WAITING_TIME = 32L;

    private final String[] requiredObjectInstanceNames;
    private final CountDownLatch latch;

    protected SEEAbstractFederate(File configurationFile, String... requiredObjectInstanceNames) {
        super(configurationFile);

        if (requiredObjectInstanceNames == null) {
            throw new IllegalArgumentException("Cannot accept the required object instance names as a NULL reference.");
        }

        this.requiredObjectInstanceNames = requiredObjectInstanceNames;
        this.latch = new CountDownLatch(this.requiredObjectInstanceNames.length);
    }

    @Override
    public final void configureAndStart() throws RTIexception {
        connectToRti();
        joinFederationExecution();
        declareSRFOMExecutiveClasses();

        createRequiredObjectInstanceListeners();

        // Publish/subscribe user specified object and interaction classes as well as await discovery of required objects.
        declareClasses();

        // Register object instances this federate will manage.
        declareObjectInstances();

        // Wait to discover all important objects needed by this federate.
        waitForRequiredObjects();
        setupTimeManagement();

        // Enter simulation executive loop.
        exec();
    }

    private void declareSRFOMExecutiveClasses() throws RTIexception {
        subscribeObjectClass(ExecutionConfiguration.class, "root_frame_name", "scenario_time_epoch", "current_execution_mode", "next_execution_mode", "next_mode_scenario_time", "next_mode_cte_time", "least_common_time_step");
        publishInteractionClass(ModeTransitionRequest.class);
    }

    private void createRequiredObjectInstanceListeners() {
        for (String requiredObjectName : this.requiredObjectInstanceNames) {
            addObjectInstanceListener(requiredObjectName, new ObjectInstanceListener() {
                @Override
                public void discovered(String producingFederateName) {
                    latch.countDown();
                }

                @Override
                public void initialized(Object instance) {
                    // Ignore.
                }

                @Override
                public void destroyed(String producingFederateName) {
                    // Ignore.
                }
            });
        }
    }

    private void waitForRequiredObjects() {
        if (this.requiredObjectInstanceNames.length > 0) {
            try {
                if (!this.latch.await(OBJECT_DISCOVERY_WAITING_TIME, TimeUnit.SECONDS)) {
                    logger.warn("Not all required object instances were discovered prior to starting federate execution.");
                } else {
                    logger.info("Discovered all required object instances.");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("Thread interrupted while waiting to discover required object instances.");
            }
        }
    }

    /**
     * Declare i.e., publish/subscribe all HLA object and interaction classes that this federate is interested in.
     * This method is called during the federate's initialization procedure.
     */
    protected abstract void declareClasses() throws RTIexception;

    /**
     * Register all object instances that this federate will manage during the federation execution. Choosing not to
     * register all of your required object instance here does not preclude registration elsewhere, however, as per the
     * SRFOM standard's guidance for late joiner initialization, it is advised to perform all registrations in this method.
     */
    protected abstract void declareObjectInstances() throws RTIexception;

}
