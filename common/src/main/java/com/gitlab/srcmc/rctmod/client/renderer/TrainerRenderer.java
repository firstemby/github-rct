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
