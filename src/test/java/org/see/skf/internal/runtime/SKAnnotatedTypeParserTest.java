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

import org.junit.jupiter.api.Test;
import org.see.baseplate.models.DynamicalEntity;
import org.see.baseplate.models.PhysicalEntity;
import org.see.baseplate.models.ReferenceFrame;
import org.see.skf.core.annotations.Attribute;
import org.see.skf.core.annotations.InteractionClass;
import org.see.skf.core.annotations.ObjectClass;
import org.see.skf.core.annotations.Parameter;
import org.see.skf.encoding.HLAbooleanCoder;
import org.see.skf.encoding.HLAunicodeStringCoder;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SKAnnotatedTypeParserTest {

    private final SKAnnotatedTypeParser parser;

    private final Set<String> referenceFrameAttributes;
    private final Set<String> physicalEntityAttributes;
    private final Set<String> dynamicalEntityAttributes;

    private final Set<String> annotatedObjectClassAttributes;
    private final Set<String> annotatedInteractionClassAttributes;

    public SKAnnotatedTypeParserTest() {
        CoderManager coderManager = new CoderManager();
        this.parser = new SKAnnotatedTypeParser(coderManager);

        this.referenceFrameAttributes = new HashSet<>() {{
            add("name");
            add("parent_name");
        }};

        this.physicalEntityAttributes = new HashSet<>() {{
            add("name");
            add("type");
            add("status");
            add("parent_reference_frame");
            add("acceleration");
            add("rotational_acceleration");
            add("center_of_mass");
        }};

        this.dynamicalEntityAttributes = new HashSet<>(){{
            add("force");
            add("mass");
            add("mass_rate");
        }};
        this.dynamicalEntityAttributes.addAll(physicalEntityAttributes);

        this.annotatedObjectClassAttributes = new HashSet<>(){{
            add("attribute1");
            add("attribute2");
            add("attribute3");
            add("attribute4");
        }};

        this.annotatedInteractionClassAttributes = new HashSet<>(){{
            add("parameter1");
            add("parameter2");
            add("parameter3");
            add("parameter4");
        }};
    }

    @Test
    void testConstructorExceptions() {
        assertThrows(Exception.class, () -> parser.parseObjectInstanceProxy(UnannotatedObject.class));
        assertThrows(Exception.class, () -> parser.parseObjectInstanceProxy(MultiannotatedObject.class));

        assertDoesNotThrow(() -> parser.parseObjectInstanceProxy(PhysicalEntity.class));
        assertDoesNotThrow(() -> parser.parseObjectInstanceProxy(DynamicalEntity.class));
        assertDoesNotThrow(() -> parser.parseObjectInstanceProxy(ReferenceFrame.class));
    }

    @Test
    void basicParseTest() {
        SKAnnotatedTypeParser.Metadata physicalEntityStructure = parser.parseObjectInstanceProxy(PhysicalEntity.class);
        SKAnnotatedTypeParser.Metadata dynamicalEntityStructure = parser.parseObjectInstanceProxy(DynamicalEntity.class);
        SKAnnotatedTypeParser.Metadata referenceFrameStructure = parser.parseObjectInstanceProxy(ReferenceFrame.class);

        assertEquals("HLAobjectRoot.PhysicalEntity", physicalEntityStructure.getFomClassName());
        assertEquals("HLAobjectRoot.PhysicalEntity.DynamicalEntity", dynamicalEntityStructure.getFomClassName());
        assertEquals("HLAobjectRoot.ReferenceFrame", referenceFrameStructure.getFomClassName());

        assertTrue(areAllTraitsPresent(physicalEntityAttributes, physicalEntityStructure.getTraits()));
        assertTrue(areAllTraitsPresent(dynamicalEntityAttributes, dynamicalEntityStructure.getTraits()));
        assertTrue(areAllTraitsPresent(referenceFrameAttributes, referenceFrameStructure.getTraits()));
    }

    private boolean areAllTraitsPresent(Set<String> expectedTraits, Set<Trait> actualTraits) {
        for (String traitName : expectedTraits) {
            if (!hasTraitWithName(traitName, actualTraits)) {
                return false;
            }
        }

        return true;
    }

    private boolean hasTraitWithName(String name, Set<Trait> traits) {
        boolean found = false;

        for (Trait t : traits) {
            if (t.getAnnotatedName().equals(name)) {
                found = true;
                break;
            }
        }

        return found;
    }

    @Test
    void testInheritance() {
        SKAnnotatedTypeParser.Metadata annotatedObjectClassL3Structure = parser.parseObjectInstanceProxy(AnnotatedObjectClassL3.class);
        assertEquals("HLAobjectRoot.AnnotatedObjectClassL1", annotatedObjectClassL3Structure.getFomClassName());
        assertTrue(areAllTraitsPresent(annotatedObjectClassAttributes, annotatedObjectClassL3Structure.getTraits()));

        SKAnnotatedTypeParser.Metadata annotatedInteractionClassL3Structure = parser.parseInteractionProxy(AnnotatedInteractionClassL3.class);
        assertEquals("HLAinteractionRoot.AnnotatedInteractionClassL3", annotatedInteractionClassL3Structure.getFomClassName());
        assertTrue(areAllTraitsPresent(annotatedInteractionClassAttributes, annotatedInteractionClassL3Structure.getTraits()));
    }

    @Test
    void testCoderFieldTypeMismatch() {
        assertDoesNotThrow(() -> parser.parseObjectInstanceProxy(PhysicalEntity.class));
        assertThrows(Exception.class, () -> parser.parseObjectInstanceProxy(FieldCoderMismatchObjectClass.class));
        assertThrows(Exception.class, () -> parser.parseObjectInstanceProxy(FieldCoderMismatchInteractionClass.class));
    }
}

class UnannotatedObject { }

@ObjectClass(name = "HLAobjectRoot.PhysicalEntity")
@InteractionClass(name = "HLAinteractionRoot.ModeTransitionRequest")
class MultiannotatedObject { }

@ObjectClass(name = "HLAobjectRoot.AnnotatedObjectClassL1")
class AnnotatedObjectClassL1 extends UnannotatedObject {
    @Attribute(name = "attribute1", coder = HLAunicodeStringCoder.class)
    private String attribute1;

    @Attribute(name = "attribute2", coder = HLAunicodeStringCoder.class)
    private String attribute2;

    public AnnotatedObjectClassL1() {
        this.attribute1 = "";
        this.attribute2 = "";
    }

    public String getAttribute1() {
        return attribute1;
    }

    public void setAttribute1(String attribute1) {
        this.attribute1 = attribute1;
    }

    public String getAttribute2() {
        return attribute2;
    }

    public void setAttribute2(String attribute2) {
        this.attribute2 = attribute2;
    }
}

class AnnotatedObjectClassL2 extends AnnotatedObjectClassL1 {
    private String unannotatedAttribute1;

    private String unannotatedAttribute2;

    public AnnotatedObjectClassL2() {
        this.unannotatedAttribute1 = "";
        this.unannotatedAttribute2 = "";
    }

    public String getUnannotatedAttribute1() {
        return unannotatedAttribute1;
    }

    public void setUnannotatedAttribute1(String unannotatedAttribute1) {
        this.unannotatedAttribute1 = unannotatedAttribute1;
    }

    public String getUnannotatedAttribute2() {
        return unannotatedAttribute2;
    }

    public void setUnannotatedAttribute2(String unannotatedAttribute2) {
        this.unannotatedAttribute2 = unannotatedAttribute2;
    }
}

class AnnotatedObjectClassL3 extends AnnotatedObjectClassL2 {
    @Attribute(name = "attribute3", coder = HLAunicodeStringCoder.class)
    private String attribute3;

    @Attribute(name = "attribute4", coder = HLAunicodeStringCoder.class)
    private String attribute4;

    public AnnotatedObjectClassL3() {
        this.attribute3 = "";
        this.attribute4 = "";
    }

    public String getAttribute3() {
        return attribute3;
    }

    public void setAttribute3(String attribute3) {
        this.attribute3 = attribute3;
    }

    public String getAttribute4() {
        return attribute4;
    }

    public void setAttribute4(String attribute4) {
        this.attribute4 = attribute4;
    }
}

@InteractionClass(name = "HLAinteractionRoot.AnnotatedInteractionClassL1")
class AnnotatedInteractionClassL1 {
    @Parameter(name = "parameter1", coder = HLAbooleanCoder.class)
    private Boolean parameter1;

    @Parameter(name = "parameter2", coder = HLAunicodeStringCoder.class)
    private String parameter2;

    public AnnotatedInteractionClassL1() {
        this.parameter1 = false;
        this.parameter2 = "";
    }

    public Boolean getParameter1() {
        return parameter1;
    }

    public void setParameter1(Boolean parameter1) {
        this.parameter1 = parameter1;
    }

    public String getParameter2() {
        return parameter2;
    }

    public void setParameter2(String parameter2) {
        this.parameter2 = parameter2;
    }
}

class AnnotatedInteractionClassL2 extends AnnotatedInteractionClassL1 {
    private final int unannotatedParameter1;
    private final float unannotatedParameter2;

    public AnnotatedInteractionClassL2() {
        this.unannotatedParameter1 = 0;
        this.unannotatedParameter2 = 0;
    }

    public int getUnannotatedParameter1() {
        return unannotatedParameter1;
    }

    public float getUnannotatedParameter2() {
        return unannotatedParameter2;
    }
}

@InteractionClass(name = "HLAinteractionRoot.AnnotatedInteractionClassL3")
class AnnotatedInteractionClassL3 extends AnnotatedInteractionClassL2 {
    @Parameter(name = "parameter3", coder = HLAunicodeStringCoder.class)
    private String parameter3;

    @Parameter(name = "parameter4", coder = HLAunicodeStringCoder.class)
    private String parameter4;

    public AnnotatedInteractionClassL3() {
        this.parameter3 = "";
        this.parameter4 = "";
    }

    public String getParameter3() {
        return parameter3;
    }

    public void setParameter3(String parameter3) {
        this.parameter3 = parameter3;
    }

    public String getParameter4() {
        return parameter4;
    }

    public void setParameter4(String parameter4) {
        this.parameter4 = parameter4;
    }
}

@ObjectClass(name = "HLAobjectRoot.PhysicalEntity")
class FieldCoderMismatchObjectClass {

    @Attribute(name = "name", coder = HLAbooleanCoder.class)
    private String name;

    public FieldCoderMismatchObjectClass() {
        this.name = "";
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

@InteractionClass(name = "HLAinteractionRoot.FederateMessage")
class FieldCoderMismatchInteractionClass {

    @Attribute(name = "name", coder = HLAbooleanCoder.class)
    private String targetFederate;

    public FieldCoderMismatchInteractionClass() {
        this.targetFederate = "";
    }

    public String getTargetFederate() {
        return this.targetFederate;
    }

    public void setTargetFederate(String name) {
        this.targetFederate = name;
    }
}