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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Object classes whose instances should generate events when any of its properties change are recommended to extend this
 * class. One or more dedicated <a href=https://docs.oracle.com/en/java/javase/11/docs/api/java.desktop/java/beans/PropertyChangeListener.html>PropertyChangeListener</a>
 * instances should be bound to remote entities discovered by the federate. That way, the federate will be immediately
 * notified when a property changes with the exact values provided for comparison.
 *
 * @since 2.0
 */
public abstract class PropertyChangeSubject {
    private final PropertyChangeSupport propertyChangeSupport;

    protected PropertyChangeSubject() {
        propertyChangeSupport = new PropertyChangeSupport(this);
    }

    /**
     * Register a property change listener that will monitor that is interested in changes to this object's properties.
     * @param listener A property change listener.
     */
    public void addPropertyListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Remove a previously registered property change listener. Nothing happens if the listener is null, was never added or
     * an exception is thrown.
     * @param listener The previously registered property change listener.
     */
    public void removePropertyListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    /**
     * Notifies all listeners that a property of this object has changed.
     * @param propertyName Name of the property.
     * @param oldValue The old value of the property.
     * @param newValue The new value of the property.
     */
    public void notifyListeners(String propertyName, Object oldValue, Object newValue) {
        propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }
}
