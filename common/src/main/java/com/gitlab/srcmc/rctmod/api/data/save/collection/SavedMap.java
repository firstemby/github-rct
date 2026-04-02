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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

import com.gitlab.srcmc.rctmod.ModCommon;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;

public class SavedMap<K, V, U> extends SavedData implements Map<K, V> {
    private Map<K, V> map = new HashMap<>();
    private Function<K, String> keyToTag;
    private Function<String, K> tagToKey;
    private Function<V, U> valueToTag;
    private Function<U, V> tagToValue;
    private Function<CompoundTag, BiConsumer<String, U>> tagConsumer;
    private Function<CompoundTag, Function<String, U>> tagSupplier;
    private SavedMap<?, ?, ?> parent;

    public SavedMap(Function<K, String> keyToTag, Function<String, K> tagToKey, Function<V, U> valueToTag, Function<U, V> tagToValue, Function<CompoundTag, BiConsumer<String, U>> tagConsumer, Function<CompoundTag, Function<String, U>> tagSupplier) {
        this.keyToTag = keyToTag;
        this.tagToKey = tagToKey;
        this.valueToTag = valueToTag;
        this.tagToValue = tagToValue;
        this.tagConsumer = tagConsumer;
        this.tagSupplier = tagSupplier;
    }

    public void load(CompoundTag tag) {
        this.clear();
        tag.getAllKeys().forEach(key -> this.put(this.tagToKey.apply(key), this.tagToValue.apply(this.tagSupplier.apply(tag).apply(key))));
    }

    @Override
    public CompoundTag save(CompoundTag tag, Provider provider) {
        for(var entry : this.map.entrySet()) {
            this.tagConsumer.apply(tag).accept(this.keyToTag.apply(entry.getKey()), this.valueToTag.apply(entry.getValue()));
        }

        return tag;
    }

    @Override
    public void setDirty(boolean value) {
        if(value && !this.isDirty() && this.parent != null) {
            this.parent.setDirty();
        }

        super.setDirty(value);
    }

    @Override
    public int size() {
        return this.map.size();
    }

    @Override
    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return this.map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return this.map.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return this.map.get(key);
    }

    @Override
    public V put(K key, V value) {
        var prev = this.map.put(key, value);

        if((prev == null && value != null) || (prev != null && !prev.equals(value))) {
            setParent(value, this);
            this.setDirty();
        }

        return prev;
    }

    @Override
    public V remove(Object key) {
        var removed = this.map.remove(key);

        if(removed != null) {
            setParent(removed, null);
            this.setDirty();
        }

        return removed;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        m.forEach(this::put);
    }

    @Override
    public void clear() {
        if(!this.map.isEmpty()) {
            if(this.map.values().stream().findFirst().get() instanceof SavedMap<?, ?, ?>) {
                this.map.values().forEach(v -> setParent(v, null));
            }

            this.map.clear();
            this.setDirty();
        }
    }

    @Override
    public Set<K> keySet() {
        return this.map.keySet();
    }

    @Override
    public Collection<V> values() {
        return this.map.values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return this.map.entrySet();
    }

    private static <V> void setParent(V value, SavedMap<?, ?, ?> parent) {
        if(value instanceof SavedMap<?, ?, ?> child) {
            child.parent = parent;
        }
    }

    public static String filePath(String context) {
        return String.format("%s.%s.map", ModCommon.MOD_ID, context);
    }
}
