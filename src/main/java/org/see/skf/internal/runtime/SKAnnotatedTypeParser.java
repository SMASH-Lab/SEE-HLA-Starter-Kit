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

import org.see.skf.core.annotations.Attribute;
import org.see.skf.core.annotations.InteractionClass;
import org.see.skf.core.annotations.ObjectClass;
import org.see.skf.core.annotations.Parameter;
import org.see.skf.encoding.Coder;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

public final class SKAnnotatedTypeParser {

    private final CoderManager coderManager;

    public SKAnnotatedTypeParser(CoderManager coderManager) {
        this.coderManager = coderManager;
    }

    public String getInstanceProxyFomName(Class<?> clazz) {
        isNull(clazz);

        ObjectClass fomNameAnnotation = clazz.getAnnotation(ObjectClass.class);
        if (fomNameAnnotation == null) {
            throw new AnnotationParseException("No @ObjectClass annotation attached for <" + clazz.getName() + ">.");
        }

        return fomNameAnnotation.name();
    }

    public String getInteractionProxyFomName(Class<?> clazz) {
        isNull(clazz);

        InteractionClass fomNameAnnotation = clazz.getAnnotation(InteractionClass.class);
        if (fomNameAnnotation == null) {
            throw new AnnotationParseException("No @InteractionClass annotation attached for <" + clazz.getName() + ">.");
        }

        return fomNameAnnotation.name();
    }

    public Metadata parseObjectInstanceProxy(Class<?> proxyClass) {
        isNull(proxyClass);
        return buildObjectInstanceStructure(proxyClass);
    }

    public Metadata parseInteractionProxy(Class<?> proxyClass) {
        isNull(proxyClass);
        return buildInteractionStructure(proxyClass);
    }

    private void isNull(Object proxy) {
        if (proxy == null) {
            throw new NullPointerException("NULL reference provided for parsing.");
        }
    }

    private void illegalMultiAnnotationCheck(Class<?> clazz) {
        ObjectClass a1 = clazz.getAnnotation(ObjectClass.class);
        InteractionClass a2 = clazz.getAnnotation(InteractionClass.class);

        if (a1 != null && a2 != null) {
           throw new AnnotationParseException(clazz + " uses both @ObjectClass and @InteractionClass annotations which is disallowed.");
        }
    }

    private Metadata buildObjectInstanceStructure(Class<?> clazz) {
        ObjectClass fomNameAnnotation = clazz.getAnnotation(ObjectClass.class);
        if (fomNameAnnotation == null) {
            throw new AnnotationParseException("No @ObjectClass annotation attached for <" + clazz.getName() + ">.");
        }

        Metadata metadata = new Metadata(clazz, fomNameAnnotation.name());
        while (clazz != Object.class && clazz.isAnnotationPresent(ObjectClass.class)) {
            illegalMultiAnnotationCheck(clazz);

            for (Field field : clazz.getDeclaredFields()) {
                Attribute attribute = field.getAnnotation(Attribute.class);

                if (attribute != null) {
                    String attributeName = attribute.name();
                    Class<? extends Coder<?>> coderClass = attribute.coder();
                    CoderManager.CoderReflectionData coderData = this.coderManager.get(coderClass);

                    Trait t = new Trait.Builder()
                            .sourceClass(clazz)
                            .field(field)
                            .annotatedName(attributeName)
                            .coderData(coderData)
                            .build();

                    metadata.add(t, clazz);
                }
            }

            clazz = clazz.getSuperclass();
        }

        return metadata;
    }

    private Metadata buildInteractionStructure(Class<?> clazz) {
        InteractionClass fomNameAnnotation = clazz.getAnnotation(InteractionClass.class);
        if (fomNameAnnotation == null) {
            throw new AnnotationParseException("No @InteractionClass annotation attached for <" + clazz.getName() + ">.");
        }

        Metadata metadata = new Metadata(clazz, fomNameAnnotation.name());
        while (clazz != Object.class && clazz.isAnnotationPresent(InteractionClass.class)) {
            illegalMultiAnnotationCheck(clazz);

            for (Field field : clazz.getDeclaredFields()) {
                Parameter parameter = field.getAnnotation(Parameter.class);

                if (parameter != null) {
                    String parameterName = parameter.name();
                    Class<? extends Coder<?>> coderClass = parameter.coder();
                    CoderManager.CoderReflectionData coderData = this.coderManager.get(coderClass);

                    Trait t = new Trait.Builder()
                            .sourceClass(clazz)
                            .field(field)
                            .annotatedName(parameterName)
                            .coderData(coderData)
                            .build();

                    metadata.add(t, clazz);
                }
            }

            clazz = clazz.getSuperclass();
        }

        return metadata;
    }

    public static final class Metadata {

        private final Class<?> proxyClass;

        private final String fomClassName;

        private final Set<Trait> traits;

        private Metadata(Class<?> proxyClass, String fomClassName) {
            this.proxyClass = proxyClass;
            this.fomClassName = fomClassName;
            this.traits = new HashSet<>();
        }

        private void add(Trait trait, Class<?> forClass) {
            String name = trait.getAssignedFieldName();

            if (traitExists(name)) {
                throw new AnnotationParseException("The member <" + name + "> is duplicated in the hierarchy of the class <" + forClass.getName() + ">.");
            }

            this.traits.add(trait);
        }

        private boolean traitExists(String name) {
            return this.traits.stream().anyMatch(trait -> trait.getAssignedFieldName().equals(name));
        }

        public Class<?> getProxyClass(){
            return this.proxyClass;
        }

        public String getFomClassName() {
            return this.fomClassName;
        }

        public Set<Trait> getTraits() {
            return this.traits;
        }
    }
}
