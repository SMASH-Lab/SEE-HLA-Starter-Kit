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

package org.see.skf.annotations;

import org.see.skf.core.Coder;
import org.see.skf.runtime.ScopeLevel;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for designating a particular class field as the attribute of an HLA object class.
 *
 * @since 1.5
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Attribute {
    /**
     * The attribute name of the object class as defined in the FOM.
     *
     * @return Name of this attribute
     */
    String name();

    /**
     * A coder capable of handling data conversion for the type assigned to this attribute in the FOM.
     * @return A coder for this attribute's type
     */
    Class<? extends Coder<?>> coder();

    /**
     * The scope for this object class attribute: PUBLISH, SUBSCRIBE, PUBLISH_SUBSCRIBE, or
     * NONE. Defaults to PUBLISH_SUBSCRIBE if not specified.
     *
     * @return The scope level for this attribute
     */
    ScopeLevel scope() default ScopeLevel.PUBLISH_SUBSCRIBE;
}
