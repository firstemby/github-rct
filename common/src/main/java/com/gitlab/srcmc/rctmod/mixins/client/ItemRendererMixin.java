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
package com.gitlab.srcmc.rctmod.mixins.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.gitlab.srcmc.rctmod.ModRegistries;
import com.gitlab.srcmc.rctmod.client.renderer.TargetArrowRenderer;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderStateShard.OutputStateShard;
import net.minecraft.client.renderer.RenderStateShard.ShaderStateShard;
import net.minecraft.client.renderer.RenderType.CompositeState;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

@Mixin(ItemRenderer.class)
public class ItemRendererMixin {
    private static final RenderType TRIANGLE_STRIP = RenderType.create(
        "trainer_card_arrow",
        DefaultVertexFormat.POSITION_COLOR,
        VertexFormat.Mode.TRIANGLE_STRIP,
        RenderType.SMALL_BUFFER_SIZE,
        CompositeState.builder()
            .setShaderState(ShaderStateShard.POSITION_COLOR_SHADER)
            .setTransparencyState(ShaderStateShard.TRANSLUCENT_TRANSPARENCY)
            .setOutputState(OutputStateShard.TRANSLUCENT_TARGET)
            .createCompositeState(false));

    @Inject(method = "render", at = @At("RETURN"), remap = true)
    public void onRender(ItemStack itemStack, ItemDisplayContext displayContext, boolean rotationReversed, PoseStack poseStack, MultiBufferSource bufferSource, int i1, int i2, BakedModel bakedModel, CallbackInfo ci) {
        if(displayContext.firstPerson() && itemStack.is(ModRegistries.Items.TRAINER_CARD.get())) {
            TargetArrowRenderer.getInstance().render(bufferSource.getBuffer(TRIANGLE_STRIP), poseStack, displayContext != ItemDisplayContext.FIRST_PERSON_RIGHT_HAND);
        }
    }
}
