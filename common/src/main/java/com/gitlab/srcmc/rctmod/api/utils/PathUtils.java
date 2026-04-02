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
package com.gitlab.srcmc.rctmod.api.utils;

public final class PathUtils {
    private PathUtils() {}

    /**
     * Retrieves the name component of a path, strips its extension and changes it to
     * lowercase. The path may only be separated by forward slash ('/') characters.
     * 
     * @param path Input path to retrieve the name component from.
     * @return Stripped lowercase name component.
     */
    public static String filename(String path) {
        var lastSep = path.lastIndexOf('/');
        var lastDot = path.lastIndexOf('.');

        return path.toLowerCase().substring(
            lastSep >= 0 ? lastSep + 1 : 0,
            (lastDot >= 0 && (lastSep < 0 || lastDot > lastSep)) ? lastDot : path.length());
    }

    /**
     * Strips the given path of its last component and changes it to lowercase. The
     * path may only be separated by forward slash ('/') characters. If the path
     * contains no separators an empty string is returned.
     * 
     * @param path Path to strip of its last component.
     * @return Stripped lowercase path.
     */
    public static String dirname(String path) {
        var lastSep = path.lastIndexOf('/');
        return lastSep >= 0 ? path.substring(0, lastSep) : "";
    }
}
