package com.gitlab.srcmc.rctmod.api.data.save.collection;

import java.util.function.Function;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;

public class SavedStringChunkPosMap extends SavedMap<String, SavedStringChunkPosMap.ChunkPosDim, CompoundTag> {
    public static class ChunkPosDim {
        public final ChunkPos pos;
        public final ResourceLocation dim;

        private ChunkPosDim(ChunkPos pos, ResourceLocation dim) {
            this.pos = pos;
            this.dim = dim;
        }

        public static ChunkPosDim of(Entity entity) {
            return new ChunkPosDim(entity.chunkPosition(), entity.level().dimension().location());
        }
    }

    public static SavedStringChunkPosMap of(CompoundTag tag, Provider provider) {
        var map = new SavedStringChunkPosMap();
        map.load(tag);
        return map;
    }

    public SavedStringChunkPosMap() {
        super(
            k -> k, t -> t,
            v -> createTag(new int[]{v.pos.x, v.pos.z}, v.dim),
            t -> createChunkPosDim(t),
            ct -> ct::put,
            ct -> {
                Function<String, CompoundTag> get = k -> {
                    var t = ct.get(k); // support for legacy format
                    return (t instanceof IntArrayTag it) ? createTag(it.getAsIntArray(), null) : (CompoundTag)t;
                };

                return get;
            });
    }

    private static CompoundTag createTag(int[] pos, ResourceLocation dim) {
        var t = new CompoundTag();
        t.putIntArray("pos", pos);
        t.putString("dim", dim != null ? dim.toString() : "");
        return t;
    }

    private static ChunkPosDim createChunkPosDim(CompoundTag t) {
        var pos = t.getIntArray("pos");
        var dim = t.getString("dim");
        return new ChunkPosDim(new ChunkPos(pos[0], pos[1]), dim.isEmpty() ? null : ResourceLocation.parse(dim));
    }
}
