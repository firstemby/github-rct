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
package com.gitlab.srcmc.rctmod.forge;

import com.gitlab.srcmc.rctmod.ModCommon;
import com.gitlab.srcmc.rctmod.ModRegistries;
import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.gitlab.srcmc.rctmod.api.data.RCTModDataProviders;
import com.gitlab.srcmc.rctmod.commands.arguments.TokenArgumentType;

import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataProvider.Factory;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@Mod(ModCommon.MOD_ID)
public class NeoForgeCommon {
    public NeoForgeCommon(ModContainer container) {
        ModRegistries.init();
        ModCommon.init();
        container.registerConfig(ModConfig.Type.SERVER, RCTMod.getInstance().getServerConfig().getSpec());
        container.getEventBus().addListener(NeoForgeCommon::onCommonSetup);
        container.getEventBus().addListener(NeoForgeCommon::onConfigLoading);
        container.getEventBus().addListener(NeoForgeCommon::onConfigReloading);
        container.getEventBus().addListener(NeoForgeCommon::onGatherData);
    }

    static void onCommonSetup(FMLCommonSetupEvent event) {
        ArgumentTypeInfos.registerByClass(TokenArgumentType.class, ModRegistries.ArgumentTypes.TOKEN_ARGUMENT_TYPE.get());
    }

    static void onConfigLoading(ModConfigEvent.Loading event) {
        onConfigLoadingOrReloading(event.getConfig());
    }

    static void onConfigReloading(ModConfigEvent.Reloading event) {
        onConfigLoadingOrReloading(event.getConfig());
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

    // TOOD: datagen task (./gradlew runData) gets stuck in IDLE after completions making it impossible to
    // use in automated setups (see https://discord.com/channels/792699517631594506/1480000109893652481)
    static void onGatherData(GatherDataEvent event) {
        for(var dp : RCTModDataProviders.values()) {
            event.createDatapackRegistryObjects(dp.registryBuilder());
            event.getGenerator().addProvider(true, (Factory<DataProvider>)dp.provider()::withOutput);
        }
    }
}
