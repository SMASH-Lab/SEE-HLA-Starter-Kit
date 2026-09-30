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

package org.see.skf.internal.runtime;

import org.see.skf.encoding.Coder;
import org.see.skf.internal.InternalObjectBuilderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class Trait {

    private static final Logger logger = LoggerFactory.getLogger(Trait.class);

    private Field field;

    private String annotatedName;

    private Coder<?> coder;

    private Method getter;

    private Method setter;

    private Method encode;

    private Method decode;

    private Trait() {}

    Trait(Builder builder) {
        this.field = builder.field;
        this.annotatedName = builder.annotatedName;
        this.getter = builder.getter;
        this.setter = builder.setter;
        this.coder = builder.coderData.coder();
        this.encode = builder.coderData.encodeMethod();
        this.decode = builder.coderData.decodeMethod();
    }

    public String getAssignedFieldName() {
        return this.field.getName();
    }

    public String getAnnotatedName() {
        return annotatedName;
    }

    byte[] encode(Object targetObject) {
        try {
            Object currentValue = this.getter.invoke(targetObject);
            if (currentValue == null) {
                logger.warn("The field <{}> has a NULL value and is likely to cause an exception during serialization.", field.getName());
            }

            return (byte[]) encode.invoke(coder, currentValue);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new SerializationException("Values could not be encoded for <" + getAssignedFieldName() + ">. Ensure that the field has been properly initialized.", e);
        }
    }

    Object[] decode(Object targetObject, byte[] data) {
        try {
            Object oldValue = this.getter.invoke(targetObject);

            // Your IDE may warn you below that the following line is incorrect. Casting the second argument to
            // java.lang.Object makes the warning go away. Be wise, and do not heed its words. All is as it should be.
            // Using byte[].class for the argument type is, in fact, the correct choice - decoding won't work
            // otherwise.
            Object newValue = this.decode.invoke(coder, data);
            this.setter.invoke(targetObject, newValue);

            return new Object[] { oldValue, newValue };
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new SerializationException("Values could not be decoded for the field <" + getAssignedFieldName() + ">.", e);
        }
    }

    static final class Builder {

        private Class<?> originClass;

        private Field field;

        private String annotatedName;

        private Method getter;

        private Method setter;

        private CoderManager.CoderReflectionData coderData;

        Builder sourceClass(Class<?> originClass) {
            this.originClass = originClass;
            return this;
        }

        Builder field(Field field) {
            this.field = field;
            return this;
        }

        Builder annotatedName(String name) {
            this.annotatedName = name;
            return this;
        }

        Builder coderData(CoderManager.CoderReflectionData coderData) {
            this.coderData = coderData;
            return this;
        }

        Trait build() {
            if (this.originClass == null || this.field == null || this.coderData == null) {
                throw new InternalObjectBuilderException("Missing one or more arguments required to instantiate an internal framework object representing an HLA class trait.");
            }

            Method[] accessors = retrieveAccessors(this.originClass, this.field);
            this.getter = accessors[0];
            this.setter = accessors[1];

            checkCoderToFieldTypeCompatibility(this.field, this.coderData);

            return new Trait(this);
        }

        private void checkCoderToFieldTypeCompatibility(Field field, CoderManager.CoderReflectionData coderData) {
            Class<?> fieldType = field.getType();
            Class<?> coderGenericType = coderData.genericType();

            if (!fieldType.equals(coderGenericType)) {
                String fieldName = field.getName();
                throw new AnnotationParseException("The field <" + fieldName + "> expects a coder of type <" + fieldType + "> but a coder that handles the type <" + coderGenericType + "> has been assigned to it instead.");
            }
        }

        private String capitalize(String word) {
            return word.substring(0, 1).toUpperCase() + word.substring(1);
        }

        private Method[] retrieveAccessors(Class<?> clazz, Field field) {
            String fieldName = field.getName();
            Class<?> fieldType = field.getType();

            String capitalizedFieldName = capitalize(fieldName);
            String getterName = "get" + capitalizedFieldName;
            String setterName = "set" + capitalizedFieldName;

            try {
                Method getterMethod = clazz.getDeclaredMethod(getterName);
                Method setterMethod = clazz.getDeclaredMethod(setterName, fieldType);

                return new Method[] { getterMethod, setterMethod };
            } catch (NoSuchMethodException e) {
                throw new AnnotationParseException("The field <" + fieldName + "> either lacks the mandatory " + getterName + "() and " + setterName + "() methods or these methods do not share the same type as the field: " + fieldType);
            }
        }
    }
}
