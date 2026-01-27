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
