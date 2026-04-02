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

import org.joml.Quaternionf;

import com.gitlab.srcmc.rctmod.world.blocks.TrainerSpawnerBlock;
import com.gitlab.srcmc.rctmod.world.blocks.entities.TrainerSpawnerBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.world.item.ItemDisplayContext;

public class TrainerSpawnerBlockEntityRenderer implements BlockEntityRenderer<TrainerSpawnerBlockEntity> {
    private static final double PI2 = 2*org.joml.Math.PI;

    public TrainerSpawnerBlockEntityRenderer(Context context) {
    }

    @Override
    public void render(TrainerSpawnerBlockEntity be, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j) {
        double m = be.getRenderItems().size();
        int n = 0;

        var t = (be.getLevel().getGameTime() + f)/(TrainerSpawnerBlock.isBoosted(be.getBlockState()) ? 20.0 : 60.0);
        var d = (int)t;

        be.renderState.targetP = ((d%2) + (t - d)) * org.joml.Math.PI;
        be.renderState.p = wlerp(be.renderState.p, be.renderState.targetP, 0.05f, PI2);

        var rotationY = new Quaternionf().rotateLocalY((float)be.renderState.p);
        var translationY = 0.425 + org.joml.Math.sin(be.renderState.p/2) * 0.05;

        for(var item : be.getRenderItems()) {
            var x = m > 1 ? (0.5 + 0.15 * org.joml.Math.cos(be.renderState.p + PI2*n/m)) : 0.5;
            var z = m > 1 ? (0.5 + 0.15 * org.joml.Math.sin(be.renderState.p + PI2*n/m)) : 0.5;

            poseStack.pushPose();
            poseStack.translate(x, translationY, z);
            poseStack.scale(0.75f, 0.75f, 0.75f);
            poseStack.mulPose(rotationY);
            Minecraft.getInstance().getItemRenderer().renderStatic(item.getDefaultInstance(), ItemDisplayContext.GROUND, i, j, poseStack, multiBufferSource, be.getLevel(), 0);
            poseStack.popPose();
            n++;
        }
    }

    // neither 'from', 'to' or 'f' may be greater than max
    private static double wlerp(double from, double to, double f, double max) {
        var d = from > to ? (to + max - from) : (to - from);
        var t = from + d*f;
        return t > max ? t - max : t;
    }
}
