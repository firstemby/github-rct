package com.gitlab.srcmc.rctmod.api.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public interface IModConfig {
    static final String SEPARATOR = "━━━━━━━━━━";

    /**
     * Reload is invoked after the config was reloaded and all config values have been parsed.
     */
    default void reload() {}
    ModConfigSpec getSpec();
}
