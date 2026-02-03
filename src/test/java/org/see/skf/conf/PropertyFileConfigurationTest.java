package org.see.skf.conf;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class PropertyFileConfigurationTest {
    final FederateConfiguration conf1 = FederateConfiguration.Factory.create(new File("src/test/resources/test.conf"));
    final FederateConfiguration conf2 = FederateConfiguration.Factory.create(new File("src/test/resources/test2.conf"));

    @Test
    void testConf1() {
        assertEquals("localhost:8989", conf1.rtiAddress());
        assertEquals("SEE 2026", conf1.federationName());
        assertEquals("Brunel_Spaceport", conf1.federateName());
        assertEquals("Behavior", conf1.federateType());
        assertEquals("LATE", conf1.federateRole());
        assertEquals(1000000, conf1.lookAhead());
        assertFalse(conf1.asynchronousDelivery());
        assertTrue(conf1.timeRegulating());
        assertTrue(conf1.timeConstrained());
        assertEquals(0, conf1.additionalFomModules().length);
    }

    @Test
    void testConf2() {
        assertEquals("192.168.0.128:8688", conf2.rtiAddress());
        assertEquals("SEE 2026", conf2.federationName());
        assertEquals("RootReferenceFramePublisher", conf2.federateName());
        assertEquals("Environment", conf2.federateType());
        assertEquals("EARLY", conf2.federateRole());
        assertEquals(1000000, conf2.lookAhead());
        assertTrue(conf2.asynchronousDelivery());
        assertTrue(conf2.timeRegulating());
        assertTrue(conf2.timeConstrained());

        assertEquals(5, conf2.additionalFomModules().length);
    }
}
