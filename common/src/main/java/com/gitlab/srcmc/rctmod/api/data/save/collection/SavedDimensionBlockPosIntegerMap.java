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
package com.gitlab.srcmc.rctmod.api.data.save.collection;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public class SavedDimensionBlockPosIntegerMap extends SavedMap<ResourceLocation, SavedBlockPosIntegerMap, CompoundTag> {
    public static SavedDimensionBlockPosIntegerMap of(CompoundTag tag, Provider provider) {
        var map = new SavedDimensionBlockPosIntegerMap();
        map.load(tag);
        return map;
    }

    public SavedDimensionBlockPosIntegerMap() {
        super(k -> k.toString(), ResourceLocation::parse,
            v -> v.save(new CompoundTag(), null),
            t -> { var m = new SavedBlockPosIntegerMap(); m.load(t); return m; },
            t -> t::put, t -> t::getCompound);
    }
}
