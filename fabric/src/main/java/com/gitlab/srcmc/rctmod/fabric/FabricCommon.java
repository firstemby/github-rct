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
package com.gitlab.srcmc.rctmod.fabric;

import com.gitlab.srcmc.rctmod.ModCommon;
import com.gitlab.srcmc.rctmod.ModRegistries;
import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.gitlab.srcmc.rctmod.commands.arguments.TokenArgumentType;

import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeConfigRegistry;
import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeModConfigEvents;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.neoforged.fml.config.ModConfig;

public class FabricCommon implements ModInitializer {
    public FabricCommon() {
        ModRegistries.init(true);
        ModCommon.init();
    }

    @Override
    public void onInitialize() {
        ArgumentTypeRegistry.registerArgumentType(
            ModRegistries.location("token_argument_type"),
            TokenArgumentType.class,
            SingletonArgumentInfo.contextFree(TokenArgumentType::token));

        NeoForgeConfigRegistry.INSTANCE.register(ModCommon.MOD_ID, ModConfig.Type.SERVER, RCTMod.getInstance().getServerConfig().getSpec());
        NeoForgeModConfigEvents.loading(ModCommon.MOD_ID).register(FabricCommon::onConfigLoadingOrReloading);
        NeoForgeModConfigEvents.reloading(ModCommon.MOD_ID).register(FabricCommon::onConfigLoadingOrReloading);
    }

    static void onConfigLoadingOrReloading(ModConfig config) {
        switch (config.getType()) {
            case SERVER:
                RCTMod.getInstance().getServerConfig().reload();
                RCTMod.getInstance().getTrainerManager().setReloadRequired();
                break;
            case CLIENT:
                RCTMod.getInstance().getClientConfig().reload();
                break;
            default:
                break;
        }
    }
}
