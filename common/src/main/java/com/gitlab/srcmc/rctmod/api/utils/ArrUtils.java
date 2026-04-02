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

import java.util.List;
import java.util.stream.IntStream;

public final class ArrUtils {
    public static List<byte[]> split(byte[] arr, int batchSize) {
        return IntStream.range(0, (arr.length + batchSize - 1) / batchSize)
            .mapToObj(i -> {
                int start = i * batchSize;
                int end = Math.min(start + batchSize, arr.length);
                var sarr = new byte[end - start];
                IntStream.range(start, end).forEach(j -> sarr[j - start] = arr[j]);
                return sarr;
            }).toList();
    }

    public static byte[] combine(List<byte[]> batches) {
        var size = batches.stream().map(b -> b.length).reduce(0, (b1, b2) -> b1 + b2);
        var arr = new byte[size];
        var it = batches.iterator();
        var i = 0;

        while(it.hasNext()) {
            var bs = it.next();
            
            for(var b : bs) {
                arr[i++] = b;
            }
        }

        return arr;
    }

    private ArrUtils() {
    }
}
