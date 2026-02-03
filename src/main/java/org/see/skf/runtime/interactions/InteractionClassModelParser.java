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

package org.see.skf.runtime.interactions;

import hla.rti1516_2025.ParameterHandle;
import hla.rti1516_2025.ParameterHandleValueMap;
import org.see.skf.annotations.InteractionClass;
import org.see.skf.annotations.Parameter;
import org.see.skf.core.Coder;
import org.see.skf.runtime.CoderCollection;
import org.see.skf.runtime.AbstractModelParser;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class InteractionClassModelParser extends AbstractModelParser {
    private Set<String> parameterNames;

    public InteractionClassModelParser(Class<?> interactionClass) {
        super(interactionClass);
    }

    @Override
    protected void retrieveModelStructure() {
        InteractionClass interactionClass = getFomClass().getAnnotation(InteractionClass.class);
        setFomClassName(interactionClass.name());

        parameterNames = new HashSet<>();
        Field[] classFields = getFomClass().getDeclaredFields();
        for (Field field : classFields) {
            if (field.isAnnotationPresent(Parameter.class)) {
                Parameter parameter = field.getAnnotation(Parameter.class);
                String parameterName = parameter.name();
                Class<? extends Coder<?>> coderClass = parameter.coder();

                addField(parameterName, field, coderClass);
                parameterNames.add(parameterName);
            }
        }

        logger.debug("Generated model class structure for the HLA interaction class <{}>.", interactionClass.name());
    }

    @Override
    public Map<String, byte[]> encode(Object element) {
        Map<String, byte[]> encodingMap = new HashMap<>();

        try {
            for (String parameterName : parameterNames) {
                Field field = getFieldForFomElement(parameterName);
                Method getter = getFieldGetter(field);
                Object fieldValue = getter.invoke(element);

                if (fieldValue != null) {
                    var coderClass = getFieldCoder(field);
                    Coder<?> coder = CoderCollection.query(coderClass);

                    Method encode = coderClass.getMethod("encode", getter.getReturnType());
                    Object encodedValue = encode.invoke(coder, fieldValue);
                    encodingMap.put(parameterName, (byte[]) encodedValue);
                } else {
                    logger.error("Failed to encode data for the parameter \"{}\" of the HLA interaction class <{}> because the property getter method returned NULL.", field.getName(), getFomClassName());
                }
            }
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }

        return encodingMap;
    }

    public void decode(Object element, ParameterHandleValueMap parameterHandleToValue, Map<ParameterHandle, String> parameterHandleToName) {
            try {
                for (var entry : parameterHandleToValue.entrySet()) {
                    String parameterName = parameterHandleToName.get(entry.getKey());

                    Field field = getFieldForFomElement(parameterName);
                    var coderClass = getFieldCoder(field);
                    Coder<?> coder = CoderCollection.query(coderClass);

                    var decode = coderClass.getMethod("decode", byte[].class);
                    var encodedValue = entry.getValue();

                    // IntelliJ will warn you here that the following line is incorrect. Changing the second argument to
                    // Object.class makes the warning go away. Be wise, and do not heed its words. All is as it should be.
                    // Using byte[].class for the parameter type is, in fact, the correct choice - decoding won't work
                    // otherwise.
                    Object newFieldValue = decode.invoke(coder, encodedValue);
                    Method setter = getFieldSetter(field);

                    setter.invoke(element, newFieldValue);
                }
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                throw new IllegalStateException("Unexpected problem encountered when trying to decode the latest values for an HLA interaction <" + element + "> of the type <" + getFomClassName() + ">" + e);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Mismatch in fields of interaction class detected. Ensure object fields are properly initialized and the getter and setter methods are of the correct type.");
            }
    }

    public Set<String> getParameterNames() {
        return parameterNames;
    }

    public int getParameterCount() {
        return parameterNames.size();
    }
}
