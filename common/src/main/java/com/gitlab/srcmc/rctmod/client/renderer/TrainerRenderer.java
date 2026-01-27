package com.gitlab.srcmc.rctmod.client.renderer;

import com.gitlab.srcmc.rctmod.client.ModClient;
import com.gitlab.srcmc.rctmod.world.entities.TrainerMob;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;

public class TrainerRenderer extends HumanoidMobRenderer<TrainerMob, PlayerModel<TrainerMob>> {
    private final ResourceLocation FALLBACK_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/player/wide/steve.png");

    public TrainerRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, new PlayerModel<>(pContext.bakeLayer(ModelLayers.PLAYER), false), 1f);
        
        this.addLayer(
            new HumanoidArmorLayer<>(this,
                new HumanoidModel<>(pContext.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                new HumanoidModel<>(pContext.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),
                pContext.getModelManager()
            ));
    }

    @Override
    public ResourceLocation getTextureLocation(TrainerMob mob) {
        return ModClient.RESOURCE_MANAGER.findResource(mob.getTrainerId(), "textures").orElse(FALLBACK_TEXTURE);
    }

    @Override
    public void render(TrainerMob pEntity, float pEntityYaw, float pPartialTicks, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
        super.render(pEntity, pEntityYaw, pPartialTicks, pPoseStack, pBuffer, pPackedLight);
    }
}
