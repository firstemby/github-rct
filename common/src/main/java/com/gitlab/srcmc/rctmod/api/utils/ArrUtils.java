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
