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
