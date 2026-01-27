package com.gitlab.srcmc.rctmod.api.data.save.collection;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;

public class SavedStringIntegerMap extends SavedMap<String, Integer, Integer> {
    public static SavedStringIntegerMap of(CompoundTag tag, Provider provider) {
        var map = new SavedStringIntegerMap();
        map.load(tag);
        return map;
    }

    public SavedStringIntegerMap() {
        super(k -> k, t -> t, v -> v, t -> t, ct -> ct::putInt, ct -> ct::getInt);
    }
}
