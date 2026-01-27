package com.gitlab.srcmc.rctmod.api.utils.collections;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * More lenient json parsing.
 * 
 * @see also {@link Collection2dDeserializer}
 */
public class StringSet2d extends HashSet<Set<String>> {
    public StringSet2d() {
    }

    public StringSet2d(Collection<? extends Set<String>> source) {
        super(source);
    }

    public static Collection2dDeserializer<String, Set<String>, StringSet2d> createDeserializer() {
        return new Collection2dDeserializer<>(HashSet<String>::new, StringSet2d::new);
    }
}
