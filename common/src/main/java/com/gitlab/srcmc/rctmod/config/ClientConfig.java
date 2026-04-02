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
package com.gitlab.srcmc.rctmod.config;

import com.gitlab.srcmc.rctmod.api.config.IClientConfig;

import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.ConfigValue;

public class ClientConfig implements IClientConfig {
    // trainers
    private ConfigValue<Boolean> showTrainerTypeSymbolsValue;
    private ConfigValue<Boolean> showTrainerTypeColorsValue;

    private boolean showTrainerTypeSymbolsCached;
    private boolean showTrainerTypeColorsCached;

    // trainer card
    private ConfigValue<Integer> trainerCardPaddingValue;
    private ConfigValue<Double> trainerCardAlignmentXValue;
    private ConfigValue<Double> trainerCardAlignmentYValue;

    private int trainerCardPaddingCached;
    private float trainerCardAlignmentXCached;
    private float trainerCardAlignmentYCached;

    private ModConfigSpec spec;

    public ClientConfig() {
        var builder = new ModConfigSpec.Builder();
        builder.push("Trainers");

        this.showTrainerTypeSymbolsValue = builder
            .comment(SEPARATOR, "Determines if symbols for trainer types are shown next to trainer names.")
            .define("showTrainerTypeSymbols", IClientConfig.super.showTrainerTypeSymbols());

        this.showTrainerTypeColorsValue = builder
            .comment(SEPARATOR, "Determines if trainer names are colored based of their type.")
            .define("showTrainerTypeColors", IClientConfig.super.showTrainerTypeColors());

        builder.pop();
        builder.push("Trainer Card");

        this.trainerCardPaddingValue = builder
            .comment(SEPARATOR, "Padding of the trainer card gui.")
            .defineInRange("trainerCardPadding", IClientConfig.super.trainerCardPadding(), 0, Integer.MAX_VALUE - 1);

        this.trainerCardAlignmentXValue = builder
            .comment(SEPARATOR, "Horizontal alignment of the trainer card gui, i.e. 0=left, 0.5=center, 1=right.")
            .defineInRange("trainerCardAlignmentX", IClientConfig.super.trainerCardAlignmentX(), 0, 1);

        this.trainerCardAlignmentYValue = builder
            .comment(SEPARATOR, "Vertical alignment of the trainer card gui, i.e. 0=top, 0.5=center, 1=bottom.")
            .defineInRange("trainerCardAlignmentY", IClientConfig.super.trainerCardAlignmentY(), 0, 1);

        this.spec = builder.build();
    }

    @Override
    public void reload() {
        this.updateCache();
    }

    private void updateCache() {
        this.showTrainerTypeSymbolsCached = this.showTrainerTypeSymbolsValue.get();
        this.showTrainerTypeColorsCached = this.showTrainerTypeColorsValue.get();
        this.trainerCardPaddingCached = this.trainerCardPaddingValue.get();
        this.trainerCardAlignmentXCached = this.trainerCardAlignmentXValue.get().floatValue();
        this.trainerCardAlignmentYCached = this.trainerCardAlignmentYValue.get().floatValue();
    }

    @Override
    public ModConfigSpec getSpec() {
        return this.spec;
    }

    @Override
    public boolean showTrainerTypeSymbols() {
        return this.showTrainerTypeSymbolsCached;
    }

    @Override
    public boolean showTrainerTypeColors() {
        return this.showTrainerTypeColorsCached;
    }

    @Override
    public int trainerCardPadding() {
        return this.trainerCardPaddingCached;
    }

    @Override
    public float trainerCardAlignmentX() {
        return this.trainerCardAlignmentXCached;
    }

    @Override
    public float trainerCardAlignmentY() {
        return this.trainerCardAlignmentYCached;
    }
}
