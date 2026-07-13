package com.gitlab.srcmc.rctmod;

import org.junit.jupiter.api.BeforeAll;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;

public abstract class BootstrappedTest {
    /**
     * Initializes minecraft constants, registries and similar.
     */
    @BeforeAll
    void init() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }
}
