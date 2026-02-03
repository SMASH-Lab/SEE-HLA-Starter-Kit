package org.see.skf.runtime;

import org.junit.jupiter.api.Test;
import org.see.skf.util.models.ExecutionConfiguration;
import org.see.skf.runtime.objects.ObjectClassModelParser;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ObjectClassModelParserTest {
    final ObjectClassModelParser parser = new ObjectClassModelParser(ExecutionConfiguration.class);

    @Test
    void testMetadata() {
        assertEquals("HLAobjectRoot.ExecutionConfiguration", parser.getFomClassName());
        assertNotNull(parser.getFieldForFomElement("root_frame_name"));
        assertNotNull(parser.getFieldForFomElement("least_common_time_step"));
        assertNotNull(parser.getFieldForFomElement("scenario_time_epoch"));

        Field rootFrameName = parser.getFieldForFomElement("root_frame_name");
        Field leastCommonTimeStep = parser.getFieldForFomElement("least_common_time_step");
        Field scenarioTimeEpoch = parser.getFieldForFomElement("scenario_time_epoch");
        assertNotNull(parser.getFieldGetter(rootFrameName));
        assertNotNull(parser.getFieldSetter(rootFrameName));
        assertNotNull(parser.getFieldGetter(leastCommonTimeStep));
        assertNotNull(parser.getFieldSetter(leastCommonTimeStep));
        assertNotNull(parser.getFieldGetter(scenarioTimeEpoch));
        assertNotNull(parser.getFieldSetter(scenarioTimeEpoch));

        assertNotNull(parser.getFieldCoder(rootFrameName));
        assertNotNull(CoderCollection.query(parser.getFieldCoder(rootFrameName)));
        assertNotNull(parser.getFieldCoder(leastCommonTimeStep));
        assertNotNull(CoderCollection.query(parser.getFieldCoder(leastCommonTimeStep)));
        assertNotNull(parser.getFieldCoder(scenarioTimeEpoch));
        assertNotNull(CoderCollection.query(parser.getFieldCoder(scenarioTimeEpoch)));

        assertEquals(ScopeLevel.SUBSCRIBE, parser.getAttributeAccessLevel("root_frame_name"));
        assertEquals(ScopeLevel.SUBSCRIBE, parser.getAttributeAccessLevel("least_common_time_step"));
        assertEquals(ScopeLevel.SUBSCRIBE, parser.getAttributeAccessLevel("scenario_time_epoch"));
    }

    @Test
    void testMethodGeneration() {
        assertEquals("getValue", parser.generateMethodName("get", "value"));
        assertEquals("setValue", parser.generateMethodName("set", "value"));
        assertEquals("getPositionVector", parser.generateMethodName("get", "positionVector"));
        assertEquals("setPositionVector", parser.generateMethodName("set", "positionVector"));
        assertEquals("getPhysicalInterface", parser.generateMethodName("get", "physicalInterface"));
        assertEquals("setPhysicalInterface", parser.generateMethodName("set", "physicalInterface"));
    }
}
