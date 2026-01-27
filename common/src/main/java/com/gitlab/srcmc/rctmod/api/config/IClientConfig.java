package com.gitlab.srcmc.rctmod.api.config;

public interface IClientConfig extends IModConfig {
    /**
     * Determines if symbols for trainer types are shown next to trainer names.
     * 
     * range [true or false]
     * default false
     */
    default boolean showTrainerTypeSymbols() { return false; }

    /**
     * Determines if trainer names are colored based of their type.
     * 
     * range [true or false]
     * default true
     */
    default boolean showTrainerTypeColors() { return true; }

    /**
     * Padding of the trainer card gui.
     * 
     * default 8
     */
    default int trainerCardPadding() { return 8; }

    /**
     * Horizontal alignment of the trainer card gui, i.e. 0=left, 0.5=center, 1=right.
     * 
     * range [0, 1]
     * default 0
     */
    default float trainerCardAlignmentX() { return 0; }

    /**
     * Vertical alignment of the trainer card gui, i.e. 0=top, 0.5=center, 1=bottom.
     * 
     * range [0, 1]
     * default 0
     */
    default float trainerCardAlignmentY() { return 0; }
}
