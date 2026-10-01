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
package com.gitlab.srcmc.rctmod.api.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import com.gitlab.srcmc.rctmod.ModCommon;
import com.gitlab.srcmc.rctmod.api.data.Optional;
import com.gitlab.srcmc.rctmod.api.data.pack.TrainerMobData;
import com.gitlab.srcmc.rctmod.api.data.pack.TrainerType;
import com.gitlab.srcmc.rctmod.api.utils.collections.StringList2d;
import com.gitlab.srcmc.rctmod.api.utils.collections.StringSet2d;
import com.gitlab.srcmc.rctmod.api.utils.collections.StringSetList;
import com.gitlab.srcmc.rctmod.api.utils.collections.StringSetListMap;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import net.minecraft.server.packs.resources.IoSupplier;

public final class JsonUtils<T> {
    public static final Gson GSON = ModCommon.RCT.gsonBuilder()
        .registerTypeAdapter(Optional.class, new Optional.TypeAdapter())
        .registerTypeAdapter(TrainerType.Color.class, new TrainerType.Color.Serializer())
        .registerTypeAdapter(TrainerType.Color.class, new TrainerType.Color.Deserializer())
        .registerTypeAdapter(StringSet2d.class, StringSet2d.createDeserializer())
        .registerTypeAdapter(StringList2d.class, StringList2d.createDeserializer())
        .registerTypeAdapter(StringSetList.class, StringSetList.createDeserializer())
        .registerTypeAdapter(StringSetListMap.class, StringSetListMap.createDeserializer())
        .registerTypeAdapter(TrainerMobData.class, new TrainerMobData.TrainerModDataDeserializer())
        .registerTypeAdapter(TrainerMobData.SeriesProperties.MapView.class, new TrainerMobData.MapViewDeserializer())
        .addSerializationExclusionStrategy(new AnnotationExclusionStrategy())
        .addDeserializationExclusionStrategy(new AnnotationExclusionStrategy())
        .setPrettyPrinting()
        .disableHtmlEscaping()
        .setLenient().create();

    public static <T> T loadFromOrThrow(String json, Class<T> type) throws JsonLoadException {
        try(var rd = new BufferedReader(new StringReader(json))) {
            return GSON.fromJson(rd, type);
        } catch(IOException | JsonParseException e) {
            throw new JsonLoadException(e);
        }
    }

    public static <T> T loadFromOrThrow(IoSupplier<InputStream> io, Class<T> type) throws JsonLoadException {
        try(var rd = new BufferedReader(new InputStreamReader(io.get(), StandardCharsets.UTF_8))) {
            return GSON.fromJson(rd, type);
        } catch(IOException | JsonParseException e) {
            throw new JsonLoadException(e);
        }
    }

    public static <T> T loadFromOrThrow(IoSupplier<InputStream> io, TypeToken<T> type) throws JsonLoadException {
        try(var rd = new BufferedReader(new InputStreamReader(io.get(), StandardCharsets.UTF_8))) {
            return GSON.fromJson(rd, type);
        } catch(IOException | JsonParseException e) {
            throw new JsonLoadException(e);
        }
    }

    public static <T> String toJson(T obj) {
        return GSON.toJson(obj);
    }
    
    private JsonUtils() {
    }

    public static class JsonLoadException extends Exception {
        public JsonLoadException() {
        }

        public JsonLoadException(Throwable cause) {
            super(cause);
        }

        public JsonLoadException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    // see: https://www.baeldung.com/gson-exclude-fields-serialization#3-with-a-custom-annotation
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public static @interface JsonExclude {}

    static class AnnotationExclusionStrategy implements ExclusionStrategy {
        @Override
        public boolean shouldSkipClass(Class<?> clazz) {
            return false;
        }

        @Override
        public boolean shouldSkipField(FieldAttributes field) {
            return field.getAnnotation(JsonExclude.class) != null || field.hasModifier(Modifier.TRANSIENT);
        }
    }
}
