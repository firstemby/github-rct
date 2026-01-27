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
