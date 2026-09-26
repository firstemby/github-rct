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

import java.util.HashMap;
import java.util.Map;

/**
 * More lenient json parsing.
 * 
 * @see also {@link Collection2dDeserializer}
 */
public class StringSetListMap extends HashMap<String, StringSetList> {
    private static final StringSetList DEFAULT_LIST = new StringSetList();
    public static final String DEFAULT_KEY = "*";

    public StringSetListMap() {
    }

    public StringSetListMap(Map<String, StringSetList> source) {
        super(source);
    }

    public StringSetList getDefault() {
        return this.getOrDefault(DEFAULT_KEY, DEFAULT_LIST);
    }

    public static Collection2dMapDeserializer<StringSetList, Map<String, StringSetList>> createDeserializer() {
        return new Collection2dMapDeserializer<>(StringSetListMap::new, StringSetList.class);
    }
}
