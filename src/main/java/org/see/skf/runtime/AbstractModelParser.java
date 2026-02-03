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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class AbstractModelParser {
    protected static final Logger logger = LoggerFactory.getLogger(AbstractModelParser.class);
    private static final String REGEX = "(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])";

    // Effectively final - is resolved in the subclasses.
    private String fomClassName;

    private final Class<?> fomClass;
    private final Set<Field> fields;
    private final Map<String, Field> fomElementNameToField;
    private final Map<Field, String> fieldToFomElementName;
    private final Map<Field, Class<? extends Coder<?>>> fieldToCoder;
    private final Map<Field, Method> fieldToGetter;
    private final Map<Field, Method> fieldToSetter;

    protected AbstractModelParser(Class<?> fomClass) {
        this.fomClass = fomClass;
        this.fields = new HashSet<>();
        this.fomElementNameToField = new HashMap<>();
        this.fieldToFomElementName = new HashMap<>();
        this.fieldToCoder = new HashMap<>();
        this.fieldToGetter = new HashMap<>();
        this.fieldToSetter = new HashMap<>();

        retrieveModelStructure();
    }

    protected abstract void retrieveModelStructure();

    public abstract Map<String, byte[]> encode(Object element);

    private void setFieldAccessors(Field field) {
        var getterName = generateMethodName("get", field.getName());
        var setterName = generateMethodName("set", field.getName());

        Method getter;
        try {
            getter = fomClass.getMethod(getterName);
            fieldToGetter.put(field, getter);
        } catch (NoSuchMethodException e) {
            String fieldName = field.getName();
            throw new IllegalArgumentException("Failed to locate a suitable getter for the field \"" + fieldName + "\" of the HLA object class <" + fomClassName + ">.");
        }

        try {
            Class<?> getterReturnType = getter.getReturnType();
            Method setter = fomClass.getMethod(setterName, getterReturnType);
            fieldToSetter.put(field, setter);
        } catch (NoSuchMethodException e) {
            String fieldName = field.getName();
            throw new IllegalArgumentException("Failed to locate a suitable setter for the field \"" + fieldName + "\" of the HLA object class <" + fomClassName + ">.");
        }
    }

    public String generateMethodName(String prefix, String fieldName) {
        String[] fieldNameSplit = fieldName.split(REGEX);

        for (int i = 0; i < fieldNameSplit.length; i++) {
            String capitalized = capitalize(fieldNameSplit[i]);
            fieldNameSplit[i] = capitalized;
        }

        StringBuilder result = new StringBuilder(prefix);

        for (String word : fieldNameSplit) {
            result.append(word);
        }

        return result.toString();
    }

    public void addField(String fomName, Field field, Class<? extends Coder<?>> coder) {
        // Fields have 3 KEY features: FOM name (attribute/parameter), and a Coder.
        // Access level can vary because objects have them at the attribute level, whereas interactions have them at
        // the class level. This functionality is implemented by the subclasses respectively.
        this.fields.add(field);
        this.fomElementNameToField.put(fomName, field);
        this.fieldToFomElementName.put(field, fomName);

        setFieldAccessors(field);
        this.fieldToCoder.put(field, coder);
    }

    private String capitalize(String word) {
        return word.substring(0, 1).toUpperCase() + word.substring(1);
    }

    public String getFomClassName() {
        return fomClassName;
    }

    public void setFomClassName(String fomClassName) {
        this.fomClassName = fomClassName;
    }

    public Class<?> getFomClass() {
        return fomClass;
    }

    public Set<Field> getAllFields() {
        return fields;
    }

    public Field getFieldForFomElement(String fomName) {
        return fomElementNameToField.get(fomName);
    }

    public String getFomElementNameForField(Field field) {
        return fieldToFomElementName.get(field);
    }

    public Method getFieldGetter(Field field) {
        return fieldToGetter.get(field);
    }

    public Method getFieldSetter(Field field) {
        return fieldToSetter.get(field);
    }

    public Class<? extends Coder<?>> getFieldCoder(Field field) {
        return fieldToCoder.get(field);
    }
}
