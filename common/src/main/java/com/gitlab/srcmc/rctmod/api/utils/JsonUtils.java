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
import java.nio.charset.StandardCharsets;
import com.gitlab.srcmc.rctmod.ModCommon;
import com.gitlab.srcmc.rctmod.api.data.Optional;
import com.gitlab.srcmc.rctmod.api.data.pack.TrainerType;
import com.gitlab.srcmc.rctmod.api.utils.collections.StringList2d;
import com.gitlab.srcmc.rctmod.api.utils.collections.StringSet2d;
import com.gitlab.srcmc.rctmod.api.utils.collections.StringSetList;
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
    public static @interface Exclude {}

    static class AnnotationExclusionStrategy implements ExclusionStrategy {
        @Override
        public boolean shouldSkipClass(Class<?> clazz) {
            return false;
        }
    
        @Override
        public boolean shouldSkipField(FieldAttributes field) {
            return field.getAnnotation(Exclude.class) != null;
        }
    }
}
