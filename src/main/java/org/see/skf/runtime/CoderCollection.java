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

package org.see.skf.runtime;

import org.see.skf.core.Coder;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ConcurrentHashMap;

public final class CoderCollection {
    private static final ConcurrentHashMap<Class<? extends Coder<?>>, Coder<?>> coderMap = new ConcurrentHashMap<>();

    private CoderCollection() {}

    public static void add(Class<? extends Coder<?>> coderClass, Coder<?> coderInstance) {
        coderMap.put(coderClass, coderInstance);
    }

    public static Coder<?> query(Class<? extends Coder<?>> coderClass) {
        Coder<?> coder = coderMap.get(coderClass);

        // Cache previously undiscovered coders until this method can guarantee the retrieval of every coder ever
        // used by the simulation in its lifetime.
        if (coder == null) {
            coder = createCoderInstance(coderClass);
        }

        return coder;
    }

    private static Coder<?> createCoderInstance(Class<? extends Coder<?>> coderClass) {
        try {
            Coder<?> coder = coderClass.getDeclaredConstructor().newInstance();
            coderMap.put(coderClass, coder);
            return coder;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
