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

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

import org.jgrapht.Graph;
import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedMultigraph;
import org.w3c.dom.Document;

import com.gitlab.srcmc.rctmod.ModCommon;
import com.gitlab.srcmc.rctmod.api.service.SeriesManager.SeriesGraph;
import com.gitlab.srcmc.rctmod.api.service.SeriesManager.TrainerNode;
import com.mxgraph.layout.mxCompactTreeLayout;
import com.mxgraph.util.mxCellRenderer;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.util.mxUtils;
import com.mxgraph.util.mxXmlUtils;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxStylesheet;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.LevelResource;

public class GraphUtils {
    private static final String GRAPH_OUTPUT_FMT_PATH = String.format("%s.%%s.graph.%%s", ModCommon.MOD_ID);
    private static final String GRAPH_OUTPUT_IMAGE_FORMAT = "PNG";
    private static final String GRAPH_OUTPUT_VECTOR_FORMAT = "SVG";
    private static final Color GRAPH_OUTPUT_BG_COLOR = Color.WHITE;
    private static final float GRAPH_OUTPUT_SCALE = 1;

    private static final String CELL_STYLE_DEFAULT = "RCT_CELL_DEFAULT";
    private static final String CELL_STYLE_OPTIONAL = "RCT_CELL_OPTIONAL";
    private static final String CELL_STYLE_REQUIRED = "RCT_CELL_REQUIRED";
    private static final String CELL_STYLE_DEFEATED = "RCT_CELL_DEFEATED";
    private static final String EDGE_STYLE_DEFAULT = "RCT_EDGE_DEFAULT";

    private static final Path EMPTY_PATH = Path.of("");

    public static URI generate(Player player, String seriesId, SeriesGraph graph) throws IOException {
        return generate(player, seriesId, graph, Format.SVG);
    }

    public static URI generate(Player player, String seriesId, SeriesGraph graph, Format format) throws IOException {
        if(format == Format.UML) {
            return URI.create(PlantUML.encode(player, seriesId, graph));
        }

        return saveToFile(player, toJGraph(graph), format).toUri();
    }

    public static Document generateSVG(SeriesGraph graph) throws IOException {
        return mxCellRenderer.createSvgDocument(toJXGraph(graph), null, GRAPH_OUTPUT_SCALE, GRAPH_OUTPUT_BG_COLOR, null);
    }

    public static BufferedImage generateImage(SeriesGraph graph) {
        return generateImage(graph, 1.0);
    }

    public static BufferedImage generateImage(SeriesGraph graph, double scale) {
        return generateImage(toJXGraph(graph), scale);
    }

    public static BufferedImage generateImage(mxGraph graph) {
        return generateImage(graph, 1.0);
    }

    public static BufferedImage generateImage(mxGraph graph, double scale) {
        return generateImage(graph, null, scale);
    }

    public static BufferedImage generateImage(mxGraph graph, double x, double y, double width, double height) {
        return generateImage(graph, x, y, width, height, 1.0);
    }

    public static BufferedImage generateImage(mxGraph graph, double x, double y, double width, double height, double scale) {
        return generateImage(graph, new mxRectangle(x, y, width, height), scale);
    }

    private static BufferedImage generateImage(mxGraph graph, mxRectangle clip, double scale) {
        return mxCellRenderer.createBufferedImage(graph, null, scale, GRAPH_OUTPUT_BG_COLOR, true, clip);
    }

    public static Graph<String, EmptyEdge> toJGraph(SeriesGraph graph) {
        var jGraph = new DirectedMultigraph<String, EmptyEdge>(EmptyEdge.class);
        var visited = new HashSet<TrainerNode>();
        var nodes = new HashMap<TrainerNode, Node>();
        var origins = new HashMap<TrainerNode, TrainerNode>();

        graph.stream().forEach(tNode -> {
            if(!visited.contains(tNode)) {
                var node = new Node(tNode, toStrNode(tNode, visited, origins));
                jGraph.addVertex(node.sNode);
                nodes.put(tNode, node);
            }
        });

        nodes.keySet().forEach(tNode -> {
            var orig = nodes.get(origins.getOrDefault(tNode, tNode));

            tNode.ancestors().forEach(anc -> {
                var node = nodes.get(origins.getOrDefault(anc, anc));
                jGraph.addEdge(node.sNode, orig.sNode);
            });
        });

        return jGraph;
    }

    public static JGraphXAdapter<String, EmptyEdge> toJXGraph(SeriesGraph graph) {
        return toJXGraph(toJGraph(graph));
    }

    public static JGraphXAdapter<String, EmptyEdge> toJXGraph(Graph<String, EmptyEdge> graph) {
        var jxGraph = new JGraphXAdapter<String, EmptyEdge>(graph);
        var layout = new mxCompactTreeLayout(jxGraph, true, true);
        var model = jxGraph.getModel();

        initStyles(jxGraph.getStylesheet());

        for(var cell : jxGraph.getChildCells(jxGraph.getDefaultParent())) {
            if(model.isVertex(cell)) {
                model.setStyle(cell, CELL_STYLE_DEFAULT);
            } else if(model.isEdge(cell)) {
                model.setStyle(cell, EDGE_STYLE_DEFAULT);
            }
        }

        layout.execute(jxGraph.getDefaultParent());
        return jxGraph;
    }

    private static Path saveToFile(Player player, Graph<String, EmptyEdge> graph, Format format) throws IOException {
        var jxGraph = toJXGraph(graph);
        var imgPath = EMPTY_PATH;
        var rootPath = player.level().isClientSide
            ? Minecraft.getInstance().gameDirectory.toPath()
            : player.getServer().getWorldPath(LevelResource.PLAYER_DATA_DIR);

        switch(format) {
            case SVG -> {
                imgPath = rootPath.resolve(String.format(GRAPH_OUTPUT_FMT_PATH, player.getStringUUID(), GRAPH_OUTPUT_VECTOR_FORMAT.toLowerCase()));
                var image = mxCellRenderer.createSvgDocument(jxGraph, null, GRAPH_OUTPUT_SCALE, GRAPH_OUTPUT_BG_COLOR, null);
                mxUtils.writeFile(mxXmlUtils.getXml(image), imgPath.toAbsolutePath().toString());
            }
            case PNG -> {
                imgPath = rootPath.resolve(String.format(GRAPH_OUTPUT_FMT_PATH, player.getStringUUID(), GRAPH_OUTPUT_IMAGE_FORMAT.toLowerCase()));
                var image = mxCellRenderer.createBufferedImage(jxGraph, null, GRAPH_OUTPUT_SCALE, GRAPH_OUTPUT_BG_COLOR, true, null);
                var imgFile = imgPath.toFile();
                ImageIO.write(image, GRAPH_OUTPUT_IMAGE_FORMAT, imgFile);
            }
            default -> {
                throw new IllegalArgumentException(String.format("Unsupported output format '%s' for '%s'", format.name(), rootPath.relativize(imgPath).toString()));
            }
        }

        return imgPath;
    }

    private static void initStyles(mxStylesheet mxss) {
        // DEFAULT
        var cellStyleDefault = new HashMap<String, Object>();
        cellStyleDefault.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_HEXAGON);
        cellStyleDefault.put(mxConstants.STYLE_OPACITY, 100);
        cellStyleDefault.put(mxConstants.STYLE_FILLCOLOR, mxUtils.getHexColorString(Color.CYAN));
        mxss.putCellStyle(CELL_STYLE_DEFAULT, cellStyleDefault);

        var edgeStyleDefault = new HashMap<String, Object>();
        edgeStyleDefault.put(mxConstants.STYLE_EDGE, mxConstants.EDGESTYLE_ORTHOGONAL);
        mxss.putCellStyle(EDGE_STYLE_DEFAULT, edgeStyleDefault);

        // OPTIONAL
        var cellStyleOptional = new HashMap<String, Object>();
        cellStyleOptional.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_HEXAGON);
        cellStyleOptional.put(mxConstants.STYLE_OPACITY, 80);
        cellStyleOptional.put(mxConstants.STYLE_FILLCOLOR, mxUtils.getHexColorString(Color.ORANGE));
        mxss.putCellStyle(CELL_STYLE_OPTIONAL, cellStyleOptional);

        // REQUIRED
        var cellStyleRequired = new HashMap<String, Object>();
        cellStyleRequired.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_HEXAGON);
        cellStyleRequired.put(mxConstants.STYLE_OPACITY, 100);
        cellStyleRequired.put(mxConstants.STYLE_FILLCOLOR, mxUtils.getHexColorString(Color.CYAN));
        mxss.putCellStyle(CELL_STYLE_REQUIRED, cellStyleRequired);

        // DEFEATED
        var cellStyleDefeated = new HashMap<String, Object>();
        cellStyleDefeated.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_HEXAGON);
        cellStyleDefeated.put(mxConstants.STYLE_OPACITY, 50);
        cellStyleDefeated.put(mxConstants.STYLE_FILLCOLOR, mxUtils.getHexColorString(Color.LIGHT_GRAY));
        mxss.putCellStyle(CELL_STYLE_DEFEATED, cellStyleDefeated);
    }

    private static String toStrNode(TrainerNode node, Set<TrainerNode> visited, Map<TrainerNode, TrainerNode> origins) {
        var sb = new StringBuilder();
        sb.append(node.id());

        node.siblings().forEach(sib -> {
            sb.append('\n').append(sib.id());
            visited.add(sib);
            origins.put(sib, node);
        });

        visited.add(node);
        return sb.toString();
    }

    public static enum Format {
        SVG, PNG, UML
    }

    static record Node(TrainerNode tNode, String sNode) {
    }

    static class EmptyEdge extends DefaultEdge {
        @Override
        public String toString() {
            return "";
        }
    }
}
