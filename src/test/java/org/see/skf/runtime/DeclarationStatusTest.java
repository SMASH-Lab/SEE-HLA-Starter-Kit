package org.see.skf.runtime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DeclarationStatusTest {
    DeclarationStatus declarationStatus;

    @BeforeEach
    void beforeAll() {
        declarationStatus = DeclarationStatus.UNDESIGNATED;
    }

    @Test
    void testPublishFlags() {
        declarationStatus = DeclarationStatus.setPublishFlag(declarationStatus);
        assertEquals(DeclarationStatus.PUBLISHED, declarationStatus);
        declarationStatus = DeclarationStatus.unsetPublishFlag(declarationStatus);
        assertEquals(DeclarationStatus.UNDESIGNATED, declarationStatus);
        declarationStatus = DeclarationStatus.setPublishFlag(declarationStatus);
        declarationStatus = DeclarationStatus.setSubscribeFlag(declarationStatus);
        assertEquals(DeclarationStatus.PUBLISHED_SUBSCRIBED, declarationStatus);
        declarationStatus = DeclarationStatus.unsetPublishFlag(declarationStatus);
        assertEquals(DeclarationStatus.SUBSCRIBED, declarationStatus);
        declarationStatus = DeclarationStatus.unsetSubscribeFlag(declarationStatus);
        assertEquals(DeclarationStatus.UNDESIGNATED, declarationStatus);
    }

    @Test
    void testSubscribeFlags() {
        declarationStatus = DeclarationStatus.setSubscribeFlag(declarationStatus);
        assertEquals(DeclarationStatus.SUBSCRIBED, declarationStatus);
        declarationStatus = DeclarationStatus.unsetSubscribeFlag(declarationStatus);
        assertEquals(DeclarationStatus.UNDESIGNATED, declarationStatus);
        declarationStatus = DeclarationStatus.setSubscribeFlag(declarationStatus);
        declarationStatus = DeclarationStatus.setPublishFlag(declarationStatus);
        assertEquals(DeclarationStatus.PUBLISHED_SUBSCRIBED, declarationStatus);
        declarationStatus = DeclarationStatus.unsetSubscribeFlag(declarationStatus);
        assertEquals(DeclarationStatus.PUBLISHED, declarationStatus);
        declarationStatus = DeclarationStatus.unsetPublishFlag(declarationStatus);
    }
}
