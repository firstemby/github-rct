package com.gitlab.srcmc.rctmod.forge.client;

import com.gitlab.srcmc.rctmod.ModCommon;
import com.gitlab.srcmc.rctmod.ModRegistries;
import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.gitlab.srcmc.rctmod.client.ModClient;
import com.gitlab.srcmc.rctmod.client.renderer.TrainerSpawnerBlockEntityRenderer;

import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod(value = ModCommon.MOD_ID, dist = Dist.CLIENT)
public class NeoForgeClient {
    public NeoForgeClient(ModContainer container) {
        ModClient.init();
        ModClient.registerEventHandlers();
        container.registerConfig(ModConfig.Type.CLIENT, RCTMod.getInstance().getClientConfig().getSpec());
        LifecycleEvent.SETUP.register(this::onSetup);
    }
    
    void onSetup() {
        ModClient.setup();
        BlockEntityRendererRegistry.register(ModRegistries.BlockEntityTypes.TRAINER_SPAWNER.get(), TrainerSpawnerBlockEntityRenderer::new);
    }
}
