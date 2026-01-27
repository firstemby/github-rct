package com.gitlab.srcmc.rctmod.forge;

import com.gitlab.srcmc.rctmod.ModCommon;
import com.gitlab.srcmc.rctmod.ModRegistries;
import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.gitlab.srcmc.rctmod.commands.arguments.TokenArgumentType;

import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod(ModCommon.MOD_ID)
public class NeoForgeCommon {
    public NeoForgeCommon(ModContainer container) {
        ModRegistries.init();
        ModCommon.init();
        container.registerConfig(ModConfig.Type.SERVER, RCTMod.getInstance().getServerConfig().getSpec());
        container.getEventBus().addListener(NeoForgeCommon::onCommonSetup);
        container.getEventBus().addListener(NeoForgeCommon::onConfigLoading);
        container.getEventBus().addListener(NeoForgeCommon::onConfigReloading);
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
}
