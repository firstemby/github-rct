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
