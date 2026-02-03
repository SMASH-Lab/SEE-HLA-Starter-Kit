package org.see.skf.runtime;

import org.junit.jupiter.api.Test;
import org.see.skf.util.models.ModeTransitionRequest;
import org.see.skf.runtime.interactions.InteractionClassModelParser;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class InteractionClassModelParserTest {
    final InteractionClassModelParser parser = new InteractionClassModelParser(ModeTransitionRequest.class);

    @Test
    void testMetadata() {
        assertEquals("HLAinteractionRoot.ModeTransitionRequest", parser.getFomClassName());

        Field name = parser.getFieldForFomElement("execution_mode");
        assertNotNull(name);
        assertNotNull(parser.getFieldGetter(name));
        assertNotNull(parser.getFieldSetter(name));
        assertNotNull(parser.getFieldCoder(name));
        assertNotNull(CoderCollection.query(parser.getFieldCoder(name)));
    }
}
