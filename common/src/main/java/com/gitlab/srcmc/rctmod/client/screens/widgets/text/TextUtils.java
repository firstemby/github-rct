package com.gitlab.srcmc.rctmod.client.screens.widgets.text;

public final class TextUtils {
    public static String trim(String s, int maxLength) {
        if(s.length() > maxLength) {
            return s.substring(0, maxLength - 3) + "...";
        }

        return s;
    }

    private TextUtils() {}
}
