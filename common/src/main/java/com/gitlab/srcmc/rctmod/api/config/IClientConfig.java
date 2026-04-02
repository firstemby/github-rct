/*
 * This file is part of Radical Cobblemon Trainers.
 * Copyright (c) 2026, HDainester, All rights reserved.
 * 
 * Radical Cobblemon Trainers can be redistributed and/or modified under the terms of the
 * Mod Community License (MCOML).
 * 
 * In short, you may share or modify the source code as you wish, as long as the
 * work stays under this license, and if you have modified the code, you must:
 * 
 *   (a) state your changes if you don't use an SVC like Git
 *   (b) list the original authors (as listed in the copyright at the top of
 *       this license)
 * 
 * When sharing binaries, you must adhere to a few more rules. Either:
 * 
 *   (a) your derivative work differs sufficiently from the original or
 *   (b) you have received written permission from all Project Owners
 * 
 * Radical Cobblemon Trainers is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the MCOML for more details.
 * 
 * You should have received a copy of the MCOML along with Radical Cobblemon Trainers.
 * If not, see <https://gitlab.com/srcmc/mcoml/-/blob/main/v1/license.txt>.
 */
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
