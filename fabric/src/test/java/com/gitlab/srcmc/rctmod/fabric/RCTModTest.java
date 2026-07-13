package com.gitlab.srcmc.rctmod.fabric;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.gitlab.srcmc.rctmod.ModCommon;

public class RCTModTest {
    /**
     * Tests initialization of systems.
     */
    @Test
    void testSystemsInit() {
        Assertions.assertInstanceOf(ModCommon.class, new ModCommon());
    }
}