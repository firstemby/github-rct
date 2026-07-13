package com.gitlab.srcmc.rctmod;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RCTModTest {
    /**
     * Tests initialization of systems.
     */
    @Test
    void testSystemsInit() {
        Assertions.assertInstanceOf(ModCommon.class, new ModCommon());
    }
}