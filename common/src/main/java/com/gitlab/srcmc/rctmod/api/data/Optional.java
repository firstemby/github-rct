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
