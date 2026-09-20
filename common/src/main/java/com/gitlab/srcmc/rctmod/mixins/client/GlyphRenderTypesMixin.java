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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.gitlab.srcmc.rctmod.ModCommon;
import com.gitlab.srcmc.rctmod.client.renderer.SpeechBubbleRenderTypes;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.GlyphRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

@Mixin(GlyphRenderTypes.class)
public class GlyphRenderTypesMixin {
    private static final Map<GlyphRenderTypes, RenderType> GLYPH_RENDER_TYPES = new HashMap<>();
    private static final Set<Object> ERROR_LOGGED = new HashSet<>();
    private GlyphRenderTypes self;

    // selects custom render type for glyphs (displayMode == null)
    @Inject(method = "select", at = @At("HEAD"), cancellable = true, remap = true)
    public void select(Font.DisplayMode displayMode, CallbackInfoReturnable<RenderType> cir) {
        if(displayMode == null) { // TODO: until a better solution comes up (e.g. custom enum value with mixins) -> method would throw NPE by default
            if(this.self == null) {
                this.self = (GlyphRenderTypes)(Object)this;
            }

            var renderType = GLYPH_RENDER_TYPES.get(this.self);

            if(renderType != null) {
                cir.setReturnValue(renderType);
            } else {
                if(!ERROR_LOGGED.contains(this.self)) {
                    ModCommon.LOG.error("No render type available for displayMode == null, falling back to textBackground");
                    ERROR_LOGGED.add(this.self);
                }

                cir.setReturnValue(RenderType.textBackground());
            }
        }
    }

    // registers custom render type for glyphs (displayMode == null)
    @Inject(method = "createForColorTexture", at = @At("RETURN"), cancellable = true, remap = true)
    private static void inject$createForColorTexture(ResourceLocation resourceLocation, CallbackInfoReturnable<GlyphRenderTypes> cir) {
        GLYPH_RENDER_TYPES.put(cir.getReturnValue(), SpeechBubbleRenderTypes.text(resourceLocation));
    }
}
