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

import com.gitlab.srcmc.rctmod.world.entities.TrainerMob;
import com.gitlab.srcmc.rctmod.world.entities.TrainerMob.Message;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

public class SpeechBubbleRenderHelper {
    private static final double HEIGHT_OFFSET = 0.55;
    private static final int FULL_BRIGHT_LIGHT = 240;
    private static final float TEXT_SCALE = 0.02f; // nametags are scaled to 0.025f
    private static final boolean TEXT_DROP_SHADOW = false;
    private static final int TEXT_FOREGROUND = 0xFF444444; // TODO
    private static final int TEXT_BACKGROUND = 0x00000000;
    private static final float BACKGROUND_Z = -0.01f;
    private static final Font.DisplayMode TEXT_DISPLAY_MODE = Font.DisplayMode.SEE_THROUGH;

    public static void render(TrainerMob entity, Message message, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        if(message != null) {
            var mc = Minecraft.getInstance();
            var bubble = message.bubble(entity, mc.font);
            var alpha = bubble.alpha();

            if(alpha > 0f) {
                poseStack.pushPose();
                poseStack.translate(0, entity.getBbHeight() + HEIGHT_OFFSET, 0);
                poseStack.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());
                poseStack.scale(TEXT_SCALE, -TEXT_SCALE, TEXT_SCALE);

                renderBackground(RenderType.text(bubble.texture), bubble.left, bubble.top, bubble.width, bubble.height, poseStack, buffer, alpha);
                var textY = bubble.yOff + bubble.top + bubble.vPadding;

                for(var line : bubble.lines()) {
                    var lineWidth = mc.font.width(line);
                    var textX = bubble.xOff - lineWidth / 2f; // centered

                    if(lineWidth > 0 && lineWidth > bubble.maxTextWidth)  {
                        // this is for edge case where intermediate states of the text may produce lines
                        // with widths greater than the width of the full text (adjusted by scaling down).
                        poseStack.pushPose();
                        poseStack.translate(0, textY, 0);
                        poseStack.scale(bubble.maxTextWidth / lineWidth, 1f, 1f);
                        mc.font.drawInBatch(line, -lineWidth / 2f, 0, scaleAlpha(TEXT_FOREGROUND, alpha), TEXT_DROP_SHADOW, poseStack.last().pose(), buffer, TEXT_DISPLAY_MODE, scaleAlpha(TEXT_BACKGROUND, alpha), packedLight);
                        poseStack.popPose();
                    } else {
                        mc.font.drawInBatch(line, textX, textY, scaleAlpha(TEXT_FOREGROUND, alpha), TEXT_DROP_SHADOW, poseStack.last().pose(), buffer, TEXT_DISPLAY_MODE, scaleAlpha(TEXT_BACKGROUND, alpha), packedLight);
                    }

                    textY += mc.font.lineHeight;
                }

                poseStack.popPose();
            }
        }
    }

    private static void renderBackground(RenderType renderType, float left, float top, float width, float height, PoseStack poseStack, MultiBufferSource buffer, float alpha) {
        var right = left + width;
        var bottom = top + height;
        var a = (int)Math.ceil(alpha * 255);
        var consumer = buffer.getBuffer(renderType);
        var matrix = poseStack.last().pose();

        consumer.addVertex(matrix, left, top, BACKGROUND_Z)
            .setColor(255, 255, 255, a)
            .setUv(0f, 0f)
            .setUv2(FULL_BRIGHT_LIGHT, FULL_BRIGHT_LIGHT);

        consumer.addVertex(matrix, left, bottom, BACKGROUND_Z)
            .setColor(255, 255, 255, a)
            .setUv(0f, 1f)
            .setUv2(FULL_BRIGHT_LIGHT, FULL_BRIGHT_LIGHT);

        consumer.addVertex(matrix, right, bottom, BACKGROUND_Z)
            .setColor(255, 255, 255, a)
            .setUv(1f, 1f)
            .setUv2(FULL_BRIGHT_LIGHT, FULL_BRIGHT_LIGHT);

        consumer.addVertex(matrix, right, top, BACKGROUND_Z)
            .setColor(255, 255, 255, a)
            .setUv(1f, 0f)
            .setUv2(FULL_BRIGHT_LIGHT, FULL_BRIGHT_LIGHT);
    }

    private static int scaleAlpha(int c, float s) {
        return ((int)Math.max(4, ((0xFF000000 & c) >>> 24) * s) << 24) | (c & 0x00FFFFFF); // clamped to 4 -> font mask ignores lowest 2 bits -> treats color as RGB
    }
}
