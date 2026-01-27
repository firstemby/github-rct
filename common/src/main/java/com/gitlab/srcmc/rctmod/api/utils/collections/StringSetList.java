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
import java.util.HashSet;
import java.util.Set;

/**
 * More lenient json parsing.
 * 
 * @see also {@link Collection2dDeserializer}
 */
public class StringSetList extends ArrayList<Set<String>> {
    public StringSetList() {
    }

    public StringSetList(Collection<? extends Set<String>> source) {
        super(source);
    }

    public static Collection2dDeserializer<String, Set<String>, StringSetList> createDeserializer() {
        return new Collection2dDeserializer<>(HashSet<String>::new, StringSetList::new);
    }
}
