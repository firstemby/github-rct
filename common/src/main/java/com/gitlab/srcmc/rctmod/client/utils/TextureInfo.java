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
package com.gitlab.srcmc.rctmod.client.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.gitlab.srcmc.rctmod.ModCommon;
import com.mojang.blaze3d.platform.NativeImage;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

public record TextureInfo(int width, int height) {
    private static final Map<ResourceLocation, TextureInfo> CACHE = new HashMap<>();
    private static final TextureInfo EMPTY = new TextureInfo(0, 0);
    
    public static TextureInfo get(ResourceLocation location) {
        var info = CACHE.get(location);

        if(info != null) {
            return info;
        }
        
        try {
            var mc = Minecraft.getInstance();
            var optional = mc.getResourceManager().getResource(location);
            info = EMPTY;

            if(optional.isPresent()) {
                var resource = optional.get();

                try (var input = resource.open();
                    var image = NativeImage.read(input)) {
                    info = new TextureInfo(image.getWidth(), image.getHeight());
                    CACHE.put(location, info);
                }
            }
        } catch (IOException e) {
            ModCommon.LOG.error("Failed to read texture dimensions: {}", location, e);
        }
        
        return info;
    }
}
