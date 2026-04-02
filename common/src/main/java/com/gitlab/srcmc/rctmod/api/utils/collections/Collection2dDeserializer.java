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
package com.gitlab.srcmc.rctmod.api.utils.collections;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.function.Supplier;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

/**
 * Supports json parsing from:
 * - single value, e.g: '1' => [['1']]
 * - array of values, e.g: ['1', '2'] => [['1'], ['2']]
 * - array of arrays with values, e.g: [['1', '2'], '3'] => [['1', '2'], ['3']]
 */
public class Collection2dDeserializer<T, CInner extends Collection<T>, COuter extends Collection<CInner>> implements JsonDeserializer<COuter> {
    private static final Gson GSON = new Gson();
    
    private final TypeToken<T> TT = new TypeToken<>() {};
    private Supplier<CInner> ctorInner;
    private Supplier<COuter> ctorOuter;

    public Collection2dDeserializer(Supplier<CInner> ctorInner, Supplier<COuter> ctorOuter) {
        this.ctorInner = ctorInner;
        this.ctorOuter = ctorOuter;
    }

    private void add(CInner cInner, JsonElement json) {
        if(!json.isJsonNull()) {
            cInner.add(GSON.fromJson(json, this.TT));
        }
    }

    @Override
    public COuter deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        var cOuter = this.ctorOuter.get();

        if(json.isJsonArray()) {
            json.getAsJsonArray().forEach(e -> {
                var cInner = this.ctorInner.get();

                if(e.isJsonArray()) {
                    e.getAsJsonArray().forEach(e2 -> {
                        this.add(cInner, e2);
                    });
                } else {
                    this.add(cInner, e);
                }

                if(!cInner.isEmpty()) {
                    cOuter.add(cInner);
                }
            });
        } else {
            var cInner = this.ctorInner.get();
            this.add(cInner, json);

            if(!cInner.isEmpty()) {
                cOuter.add(cInner);
            }
        }

        return cOuter;
    }
}
