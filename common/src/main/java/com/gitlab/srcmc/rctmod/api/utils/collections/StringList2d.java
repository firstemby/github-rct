/*
 * This file is part of Radical Cobblemon Trainers.
 * Copyright (c) 2026, HDainester, All rights reserved.
 *
 * Radical Cobblemon Trainers is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Radical Cobblemon Trainers is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Radical Cobblemon Trainers. If not, see <http://www.gnu.org/licenses/lgpl>.
 */
package com.gitlab.srcmc.rctmod.api.utils.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * More lenient json parsing.
 * 
 * @see also {@link Collection2dDeserializer}
 */
public class StringList2d extends ArrayList<List<String>> {
    public StringList2d() {
    }

    public StringList2d(Collection<? extends List<String>> source) {
        super(source);
    }

    public static Collection2dDeserializer<String, List<String>, StringList2d> createDeserializer() {
        return new Collection2dDeserializer<>(ArrayList<String>::new, StringList2d::new);
    }
}
