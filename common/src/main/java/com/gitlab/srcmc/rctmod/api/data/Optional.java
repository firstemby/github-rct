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
package com.gitlab.srcmc.rctmod.api.data;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.function.Supplier;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class Optional<T> implements Supplier<T>, Serializable {
    private static final long serialVersionUID = 0;

    private final T value;

    private Optional(T value) {
        this.value = value;
    }

    public boolean has() {
        return this.value != null;
    }

    public T get() {
        return this.value;
    }

    @Override
    public String toString() {
        return String.format("%s[%s]", this.getClass().getName(), String.valueOf(this.value));
    }

    public static <T> Optional<T> of(T value) {
        return new Optional<T>(value);
    }

    public static <T> Optional<T> empty() {
        return new Optional<T>(null);
    }

    public static class TypeAdapter implements JsonSerializer<Optional<?>>, JsonDeserializer<Optional<?>> {
        @Override
        public Optional<?> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return Optional.of(context.deserialize(json, ((ParameterizedType)typeOfT).getActualTypeArguments()[0]));
        }

        @Override
        public JsonElement serialize(Optional<?> src, Type typeOfSrc, JsonSerializationContext context) {
            return context.serialize(src.get());
        }
    }
}
