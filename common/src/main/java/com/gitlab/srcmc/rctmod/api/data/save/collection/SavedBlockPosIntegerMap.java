package com.gitlab.srcmc.rctmod.api.data.save.collection;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;

public class SavedBlockPosIntegerMap extends SavedMap<BlockPos, Integer, Integer> {
    public static SavedBlockPosIntegerMap of(CompoundTag tag, Provider provider) {
        var map = new SavedBlockPosIntegerMap();
        map.load(tag);
        return map;
    }

    public SavedBlockPosIntegerMap() {
        super(k -> k.getX() + "," + k.getY() + "," + k.getZ(),
            t -> { var v = t.split(","); return new BlockPos(Integer.parseInt(v[0]), Integer.parseInt(v[1]), Integer.parseInt(v[2])); },
            v -> v, t -> t,
            ct -> ct::putInt, ct -> ct::getInt);
    }
}
