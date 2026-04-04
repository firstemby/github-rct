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

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;
import java.util.zip.Deflater;

import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.gitlab.srcmc.rctmod.api.service.SeriesManager.SeriesGraph;
import com.gitlab.srcmc.rctmod.api.service.SeriesManager.TrainerNode;

import net.minecraft.world.entity.player.Player;

public final class PlantUML {
    public static final String SERVER_URL = "https://www.plantuml.com/plantuml/svg/";
    private static final String BASE64_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz-_";
    private static final String SERIES_GRAPH_HEADER = ""
        + "@startuml\n"
        + "!theme lightgray\n"
        + "title \"%s\"\n"
        + "hide stereotypes\n"
        + "hide members\n"
        + "skinparam linetype ortho\n"
        + "skinparam object {\n"
        + "arrowColor #red\n"
        + "arrowThickness 2\n"
        + "backgroundColor #lightblue\n"
        + "backgroundColor<<optional>> #orange\n"
        + "backgroundColor<<defeated>> #lightgray\n"
        + "}\n"
        + "skinparam legend {\n"
        + "backgroundColor #white\n"
        + "fontColor #black\n"
        + "fontSize 11\n"
        + "}\n"
        + "legend top left\n"
        + "<b>Legend</b>\n"
        + "\n"
        + "<back:lightblue> Required </back> <back:orange> Optional </back> <back:lightgray> Defeated </back>\n"
        + "endlegend\n";


    private static class NodeGroup {
        public final Set<TrainerNode> set = new HashSet<>();
        public final TrainerNode origin;
        public final Player player;

        public NodeGroup(Player player, TrainerNode origin) {
            this.origin = origin;
            this.player = player;
        }

        public int maxRequiredLevelCap() {
            var tm = RCTMod.getInstance().getTrainerManager();
            return Stream.concat(Stream.of(this.origin), set.stream()).map(tn -> tm.getData(tn.id()).getRequiredLevelCap(player)).max(Integer::compare).orElse(LevelUtils.maxLevel());
        }

        public boolean isOptional() {
            return this.origin.isOptional() && this.set.stream().allMatch(nd -> nd.isOptional());
        }

        public Stream<TrainerNode> successors() {
            var s = this.origin.successors();

            for(var tn : set) {
                s = Stream.concat(s, tn.successors());
            }

            return s.distinct();
        }

        // public Stream<TrainerNode> ancestors() {
        //     var s = this.origin.ancestors();

        //     for(var tn : set) {
        //         s = Stream.concat(s, tn.ancestors());
        //     }

        //     return s.distinct();
        // }
    }

    public static String encode(Player player, String seriesId, SeriesGraph graph) {
        return encode(player, seriesId, graph, Set.of());
    }

    public static String encode(Player player, String seriesId, SeriesGraph graph, Set<String> defeats) {
        var groups = new HashMap<TrainerNode, NodeGroup>();
        var origins = new HashMap<TrainerNode, TrainerNode>();
        var directed = new HashMap<String, Set<String>>();
        var sb = new StringBuilder(String.format(PlantUML.SERIES_GRAPH_HEADER, graph.getMetaData().title()));

        graph.stream().forEach(tn -> {
            var sib = tn.siblings().filter(s -> groups.containsKey(origins.getOrDefault(s, s))).findFirst();

            if(sib.isPresent()) {
                groups.get(origins.get(sib.get())).set.add(tn);
            } else {
                groups.put(tn, new NodeGroup(player, tn));
                origins.put(tn, tn);
            }
        });

        groups.entrySet().forEach(e -> {
            var optional_defeated = new boolean[]{e.getKey().isOptional(), e.getKey().isDefeated(defeats)};
            var id = new StringBuilder(String.format("<b>%d</b>\\n%s", e.getValue().maxRequiredLevelCap(), e.getKey().id()));

            e.getValue().set.stream().sorted((a, b) -> a.id().compareTo(b.id())).forEach(tn -> {
                id.append("\\n").append(tn.id());
                optional_defeated[0] = optional_defeated[0] || tn.isOptional();
                optional_defeated[1] = optional_defeated[1] || tn.isDefeated(defeats);
            });

            sb.append(String.format("object \"%s\" as %s%s\n", id.toString(), e.getKey().id(), optional_defeated[1] ? "<<defeated>>" : optional_defeated[0] ? "<<optional>>" : ""));
        });

        groups.values().forEach(ng -> {
            var edges = directed.computeIfAbsent(ng.origin.id(), k -> new HashSet<>());
            var arr = ng.isOptional() ? "u" : "l";

            ng.successors().forEach(suc -> {
                var sgp = origins.get(suc);

                if(sgp != null && !edges.contains(sgp.id())) {
                    sb.append(String.format("%s -%s-> %s\n", ng.origin.id(), arr, sgp.id()));
                    edges.add(sgp.id());
                }
            });
        });

        sb.append("@enduml");
        return encode(sb.toString());
    }
    
    // see: https://plantuml.com/en/text-encoding (TODO: java Brotli?)
    private static String encode(String input) {
        var inputBytes = input.getBytes(StandardCharsets.UTF_8);
        var deflater = new Deflater(Deflater.FILTERED);

        deflater.setInput(inputBytes);
        deflater.finish();

        var outputStream = new ByteArrayOutputStream();
        var buffer = new byte[1024];

        while (!deflater.finished()) {
            outputStream.write(buffer, 0, deflater.deflate(buffer));
        }

        deflater.end();
        return "~1" + base64(outputStream.toByteArray());
    }

    private static String base64(byte[] data) {
        var encoded = new StringBuilder();
        int i = 0;
        
        while (i < data.length) {
            int b0 = data[i++] & 0xFF;

            if (i == data.length) {
                encoded.append(BASE64_CHARS.charAt(b0 >> 2));
                encoded.append(BASE64_CHARS.charAt((b0 & 0x03) << 4));
                encoded.append("==");
                break;
            }

            int b1 = data[i++] & 0xFF;

            if (i == data.length) {
                encoded.append(BASE64_CHARS.charAt(b0 >> 2));
                encoded.append(BASE64_CHARS.charAt((b0 & 0x03) << 4 | (b1 >> 4)));
                encoded.append(BASE64_CHARS.charAt((b1 & 0x0F) << 2));
                encoded.append("=");
                break;
            }

            int b2 = data[i++] & 0xFF;
            encoded.append(BASE64_CHARS.charAt(b0 >> 2));
            encoded.append(BASE64_CHARS.charAt((b0 & 0x03) << 4 | (b1 >> 4)));
            encoded.append(BASE64_CHARS.charAt((b1 & 0x0F) << 2 | (b2 >> 6)));
            encoded.append(BASE64_CHARS.charAt(b2 & 0x3F));
        }

        return encoded.toString();
    }

    private PlantUML() {
    }
}
