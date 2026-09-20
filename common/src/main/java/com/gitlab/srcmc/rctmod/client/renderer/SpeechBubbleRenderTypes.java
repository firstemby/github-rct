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

import java.util.HashMap;
import java.util.Map;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class SpeechBubbleRenderTypes {
    private static final int RENDER_BUF_SIZE = 1536; // same as RenderType.textSeeThrough
    private static final Map<ResourceLocation, RenderType> CACHE = new HashMap<>();

    public static RenderType background(ResourceLocation texture) {
        return CACHE.computeIfAbsent(texture, rl -> RenderType.create(
            "speech_bubble_background",
            DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
            VertexFormat.Mode.QUADS, RENDER_BUF_SIZE, false, true,
            RenderType.CompositeState.builder()
                .setShaderState(RenderStateShard.RENDERTYPE_TEXT_SEE_THROUGH_SHADER)
                .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                .setLightmapState(RenderStateShard.LIGHTMAP)
                .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
                .setOutputState(RenderStateShard.OUTLINE_TARGET) // pretty much on top of everything
                .createCompositeState(false)
        ));
    }

    public static RenderType text(ResourceLocation texture) {
        return CACHE.computeIfAbsent(texture, rl -> RenderType.create(
            "speech_bubble_text",
            DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
            VertexFormat.Mode.QUADS, RENDER_BUF_SIZE, false, true,
            RenderType.CompositeState.builder()
                .setShaderState(RenderStateShard.RENDERTYPE_TEXT_SEE_THROUGH_SHADER)
                .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                .setLightmapState(RenderStateShard.LIGHTMAP)
                .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
                .setOutputState(RenderStateShard.OUTLINE_TARGET)
                .createCompositeState(false)
        ));
    }
}
