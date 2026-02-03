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

package org.see.skf.runtime.objects;

import hla.rti1516_2025.AttributeHandle;
import hla.rti1516_2025.AttributeHandleValueMap;
import org.see.skf.annotations.Attribute;
import org.see.skf.annotations.ObjectClass;
import org.see.skf.core.Coder;
import org.see.skf.runtime.CoderCollection;
import org.see.skf.core.PropertyChangeSubject;
import org.see.skf.runtime.AbstractModelParser;
import org.see.skf.runtime.ScopeLevel;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class ObjectClassModelParser extends AbstractModelParser {
    private Set<String> publishableAttributeNames;
    private Set<String> subscribableAttributeNames;

    public ObjectClassModelParser(Class<?> objectClass) {
        super(objectClass);
    }

    @Override
    protected void retrieveModelStructure() {
        ObjectClass objectClass = getFomClass().getAnnotation(ObjectClass.class);
        setFomClassName(objectClass.name());
        this.publishableAttributeNames = new HashSet<>();
        this.subscribableAttributeNames = new HashSet<>();

        Field[] classFields = getFomClass().getDeclaredFields();
        for (Field field : classFields) {
            if (field.isAnnotationPresent(Attribute.class)) {
                Attribute attribute = field.getAnnotation(Attribute.class);
                String attributeName = attribute.name();
                Class<? extends Coder<?>> coderClass = attribute.coder();
                ScopeLevel scopeLevel = attribute.scope();

                addField(attributeName, field, coderClass);
                setAttributeAccessLevel(attributeName, scopeLevel);
            }
        }

        logger.debug("Generated model class structure for the HLA object class <{}>.", objectClass.name());
    }

    @Override
    public Map<String, byte[]> encode(Object element) {
        Map<String, byte[]> encodingMap = new HashMap<>();

        try {
            for (String attributeName : publishableAttributeNames) {
                Field field = getFieldForFomElement(attributeName);
                Method getter = getFieldGetter(field);
                Object fieldValue = getter.invoke(element);

                if (fieldValue != null) {
                    Class<? extends Coder<?>> coderClass = getFieldCoder(field);
                    Coder<?> coder = CoderCollection.query(coderClass);

                    Method encode = coderClass.getMethod("encode", getter.getReturnType());
                    Object encodedValue = encode.invoke(coder, fieldValue);
                    encodingMap.put(attributeName, (byte[]) encodedValue);
                } else {
                    logger.error("Failed to encode data for the attribute \"{}\" of the HLA object class <{}> because the property getter method returned NULL.", field.getName(), getFomClassName());
                }
            }
        } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }

        return encodingMap;
    }

    public void decode(Object element, AttributeHandleValueMap attributeHandleToValue, Map<AttributeHandle, String> attributeHandleToName) {
        for (var entry : attributeHandleToValue.entrySet()) {
            try {
                String attributeName = attributeHandleToName.get(entry.getKey());

                Field field = getFieldForFomElement(attributeName);
                Class<? extends Coder<?>> coderClass = getFieldCoder(field);
                Coder<?> coder = CoderCollection.query(coderClass);

                Method decode = coderClass.getMethod("decode", byte[].class);
                Object encodedValue = entry.getValue();

                // IntelliJ will warn you here that the following line is incorrect. Changing the second argument to
                // Object.class makes the warning go away. Be wise, and do not heed its words. All is as it should be.
                // Using byte[].class for the parameter type is, in fact, the correct choice - decoding won't work
                // otherwise.
                Object newFieldValue = decode.invoke(coder, encodedValue);

                Method getter = getFieldGetter(field);
                Object oldFieldValue = getter.invoke(element);
                Method setter = getFieldSetter(field);

                setter.invoke(element, newFieldValue);

                PropertyChangeSubject prematureReference = (PropertyChangeSubject) element;

                // Dispatch updates to all registered property listeners that this field has been updated.
                prematureReference.notifyListeners(attributeName, oldFieldValue, newFieldValue);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new IllegalStateException("Unexpected problem encountered when trying to decode the latest values for an HLA object instance <" + element + "> of the type <" + getFomClassName() + ">" + e);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Mismatch in fields of object class detected. Ensure object fields are properly initialized and the getter and setter methods are of the correct type.");
            } catch (ClassCastException ignore) {
                // The object class does not implement UpdatableInstance. No property change updates will be dispatched.
            }
        }
    }

    private void setAttributeAccessLevel(String attributeName, ScopeLevel scopeLevel) {
        if (scopeLevel == ScopeLevel.PUBLISH_SUBSCRIBE) {
            publishableAttributeNames.add(attributeName);
            subscribableAttributeNames.add(attributeName);
        } else if (scopeLevel == ScopeLevel.PUBLISH) {
            publishableAttributeNames.add(attributeName);
        } else if (scopeLevel == ScopeLevel.SUBSCRIBE) {
            subscribableAttributeNames.add(attributeName);
        }
    }

    public Set<String> getPublishableAttributeNames() {
        return publishableAttributeNames;
    }

    public boolean isSubscribable() {
        return !subscribableAttributeNames.isEmpty();
    }

    public Set<String> getSubscribableAttributeNames() {
        return subscribableAttributeNames;
    }

    public int getPublishableAttributesCount() {
        return publishableAttributeNames.size();
    }

    public boolean isPublishable() {
        return !publishableAttributeNames.isEmpty();
    }

    public ScopeLevel getAttributeAccessLevel(String attributeName) {
        boolean isPublishable = publishableAttributeNames.contains(attributeName);
        boolean isSubscribable = subscribableAttributeNames.contains(attributeName);

        if (isPublishable && isSubscribable) {
            return ScopeLevel.PUBLISH_SUBSCRIBE;
        } else if (isPublishable) {
            return ScopeLevel.PUBLISH;
        }  else if (isSubscribable) {
            return ScopeLevel.SUBSCRIBE;
        } else {
            return ScopeLevel.NONE;
        }
    }
}
