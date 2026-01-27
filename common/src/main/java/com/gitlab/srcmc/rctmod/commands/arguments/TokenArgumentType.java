package com.gitlab.srcmc.rctmod.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

public class TokenArgumentType implements ArgumentType<String> {
    public static TokenArgumentType token() {
        return new TokenArgumentType();
    }

    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
        final int start = reader.getCursor();

        while(reader.canRead() && isAllowed(reader.peek())) {
            reader.skip();
        }

        return reader.getString().substring(start, reader.getCursor());
    }

    public static boolean isAllowed(char c) {
        return StringReader.isAllowedInUnquotedString(c) || c == '/';
    }
}
