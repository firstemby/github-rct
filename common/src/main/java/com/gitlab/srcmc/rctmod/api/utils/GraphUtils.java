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
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import javax.imageio.ImageIO;
import javax.swing.SwingConstants;

import org.apache.commons.lang3.stream.Streams;
import org.jgrapht.Graph;
import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.graph.DirectedMultigraph;
import org.w3c.dom.Document;

import com.gitlab.srcmc.rctmod.ModCommon;
import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.gitlab.srcmc.rctmod.api.data.sync.PlayerState;
import com.gitlab.srcmc.rctmod.api.service.SeriesManager.SeriesGraph;
import com.gitlab.srcmc.rctmod.api.service.SeriesManager.TrainerNode;
import com.mxgraph.canvas.mxGraphics2DCanvas;
import com.mxgraph.canvas.mxICanvas;
import com.mxgraph.canvas.mxImageCanvas;
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.util.mxCellRenderer;
import com.mxgraph.util.mxCellRenderer.CanvasFactory;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxHtmlColor;
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
    private static final Color GRAPH_OUTPUT_BG_COLOR = new Color(35, 0, 0, 100);
    private static final float GRAPH_OUTPUT_SCALE = 1;
    private static final String HIDDEN_TRAINER_NAME = "?????";
    private static final boolean ANTI_ALIAS = true;

    private static final String CELL_STYLE_DEFAULT = "RCT_CELL_DEFAULT";
    private static final String CELL_STYLE_OPTIONAL = "RCT_CELL_OPTIONAL";
    private static final String CELL_STYLE_REQUIRED = "RCT_CELL_REQUIRED";
    private static final String CELL_STYLE_DEFEATED = "RCT_CELL_DEFEATED";
    private static final String EDGE_STYLE_DEFAULT = "RCT_EDGE_DEFAULT";
    private static final String EDGE_STYLE_OPTIONAL = "RCT_EDGE_OPTIONAL";
    private static final String EDGE_STYLE_REQUIRED = "RCT_EDGE_REQUIRED";
    private static final String EDGE_STYLE_DEFEATED = "RCT_EDGE_DEFATED";

    private static final Path EMPTY_PATH = Path.of("");
    private static final Set<String> EMPTY_SET = Set.of();

    public static URI generate(Player player, SeriesGraph graph, boolean displayIds) throws IOException {
        return generate(player, graph, Format.SVG, displayIds);
    }

    public static URI generate(Player player, SeriesGraph graph, Format format, boolean displayIds) throws IOException {
        var ps = PlayerState.get(player);
        var seriesId = ps.getCurrentSeries();

        if(format == Format.UML) {
            return URI.create(PlantUML.encode(player, seriesId, graph));
        }

        return saveToFile(player, toJGraph(ps, graph, seriesId, displayIds), format).toUri();
    }

    public static Document generateSVG(PlayerState playerState, SeriesGraph graph, String seriesId, boolean displayIds) throws IOException {
        return mxCellRenderer.createSvgDocument(toJXGraph(playerState, graph, seriesId, displayIds), null, GRAPH_OUTPUT_SCALE, GRAPH_OUTPUT_BG_COLOR, null);
    }

    public static BufferedImage generateImage(PlayerState playerState, SeriesGraph graph, String seriesId, boolean displayIds) {
        return generateImage(playerState, graph, seriesId, 1.0, displayIds);
    }

    public static BufferedImage generateImage(PlayerState playerState, SeriesGraph graph, String seriesId, double scale, boolean displayIds) {
        return generateImage(toJXGraph(playerState, graph, seriesId, displayIds), scale);
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
        // reimplements mxCellRenderer.createBufferedImage with BufferedImage type ARGB (regardless of the background)
        var canvas = (mxImageCanvas)mxCellRenderer.drawCells(graph, null, scale, clip, new CanvasFactory() {
                public mxICanvas createCanvas(int width, int height) {
                    return new AlphaImageCanvas(new mxGraphics2DCanvas(), width, height, GRAPH_OUTPUT_BG_COLOR, ANTI_ALIAS);
                }
            }
		);

        return (canvas != null) ? canvas.destroy() : null;
    }

    public static Graph<Node, Edge> toJGraph(PlayerState playerState, SeriesGraph graph, String seriesId, boolean dispalyIds) {
        var jGraph = new DirectedMultigraph<Node, Edge>(Edge.class);
        var nodeMap = new HashMap<TrainerNode, Node>();
        var closed = new HashSet<Node>();

        graph.stream().forEach(trainerNode -> {
            if(!nodeMap.containsKey(trainerNode)) {
                jGraph.addVertex(initNode(playerState, seriesId, trainerNode, nodeMap, dispalyIds));
            }
        });

        nodeMap.entrySet().forEach(entry -> {
            var trainerNode = entry.getKey();
            var node = entry.getValue();

            if(!closed.contains(node)) {
                closed.add(node);
                var ancClosed = new HashSet<Node>();

                trainerNode.ancestors().forEach(anc -> {
                    var ancNode = nodeMap.get(anc);

                    if(!ancClosed.contains(ancNode)) {
                        ancClosed.add(ancNode);
                        jGraph.addEdge(node, ancNode);
                    }
                });
            }
        });

        return jGraph;
    }

    private static Node initNode(PlayerState playerState, String seriesId, TrainerNode trainerNode, Map<TrainerNode, Node> nodeMap, boolean displayIds) {
        var node = new Node(playerState, seriesId, Stream.concat(Stream.of(trainerNode), trainerNode.siblings()).toArray(n -> new TrainerNode[n]), displayIds);
        trainerNode.siblings().forEach(sib -> nodeMap.put(sib, node));
        nodeMap.put(trainerNode, node);
        return node;
    }

    public static JGraphXAdapter<Node, Edge> toJXGraph(PlayerState playerState, SeriesGraph graph, String seriesId, boolean displayIds) {
        return toJXGraph(playerState, graph, seriesId, EMPTY_SET, displayIds);
    }

    public static JGraphXAdapter<Node, Edge> toJXGraph(PlayerState playerState, SeriesGraph graph, String seriesId, Set<String> defeatedTrainers, boolean displayIds) {
        return toJXGraph(toJGraph(playerState, graph, seriesId, displayIds), seriesId, defeatedTrainers);
    }

    public static JGraphXAdapter<Node, Edge> toJXGraph(Graph<Node, Edge> graph, String seriesId) {
        return toJXGraph(graph, seriesId, EMPTY_SET);
    }

    public static JGraphXAdapter<Node, Edge> toJXGraph(Graph<Node, Edge> graph, String seriesId, Set<String> defeatedTrainers) {
        var jxGraph = new JGraphXAdapter<Node, Edge>(graph);
        var layout = new mxHierarchicalLayout(jxGraph, SwingConstants.WEST);
        var model = jxGraph.getModel();

        initStyles(jxGraph.getStylesheet());

        for(var cell : jxGraph.getChildCells(jxGraph.getDefaultParent())) {
            if(model.isVertex(cell)) {
                model.setStyle(cell, (model.getValue(cell) instanceof Node node)
                    ? (node.trainers[0].isDefeated(defeatedTrainers)
                        ? CELL_STYLE_DEFEATED
                        : (node.trainers[0].isOptional() ? CELL_STYLE_OPTIONAL : CELL_STYLE_REQUIRED))
                    : CELL_STYLE_DEFAULT);
            } else if(model.isEdge(cell)) {
                var targetCell = model.getTerminal(cell, false);

                model.setStyle(cell, (model.getValue(targetCell) instanceof Node node)
                    ? (node.trainers[0].isDefeated(defeatedTrainers)
                        ? EDGE_STYLE_DEFEATED
                        : (node.trainers[0].isOptional() ? EDGE_STYLE_OPTIONAL : EDGE_STYLE_REQUIRED))
                    : EDGE_STYLE_DEFAULT);
            }
        }
        
        layout.execute(jxGraph.getDefaultParent());
        return jxGraph;
    }

    private static Path saveToFile(Player player, Graph<Node, Edge> graph, Format format) throws IOException {
        var seriesId = PlayerState.get(player).getCurrentSeries();
        var jxGraph = toJXGraph(graph, seriesId);
        var imgPath = EMPTY_PATH;
        var rootPath = player.level().isClientSide
            ? Minecraft.getInstance().gameDirectory.toPath()
            : player.getServer().getWorldPath(LevelResource.PLAYER_DATA_DIR);

        switch(format) {
            case SVG -> {
                imgPath = rootPath.resolve(String.format(GRAPH_OUTPUT_FMT_PATH, player.getStringUUID(), GRAPH_OUTPUT_VECTOR_FORMAT.toLowerCase()));
                var image = mxCellRenderer.createSvgDocument(jxGraph, null, GRAPH_OUTPUT_SCALE, GRAPH_OUTPUT_BG_COLOR, null); // background color is not supported!
                mxUtils.writeFile(mxXmlUtils.getXml(image), imgPath.toAbsolutePath().toString());
            }
            case PNG -> {
                imgPath = rootPath.resolve(String.format(GRAPH_OUTPUT_FMT_PATH, player.getStringUUID(), GRAPH_OUTPUT_IMAGE_FORMAT.toLowerCase()));
                var image = generateImage(jxGraph, jxGraph.getBoundingBox(jxGraph.getDefaultParent()), GRAPH_OUTPUT_SCALE);
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
        // DEFAULT (cell)
        var cellStyleDefault = new HashMap<String, Object>();
        cellStyleDefault.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_HEXAGON);
        cellStyleDefault.put(mxConstants.STYLE_PERIMETER, mxConstants.PERIMETER_HEXAGON);
        cellStyleDefault.put(mxConstants.STYLE_OPACITY, 100);
        cellStyleDefault.put(mxConstants.STYLE_STROKE_OPACITY, 100);
        cellStyleDefault.put(mxConstants.STYLE_STROKECOLOR, "#4E598C");
        cellStyleDefault.put(mxConstants.STYLE_FILLCOLOR, "#92D5E6");
        cellStyleDefault.put(mxConstants.STYLE_STROKEWIDTH, 2);
        cellStyleDefault.put(mxConstants.STYLE_FONTSIZE, 7); // no overlap in hexagon (approx.)
        cellStyleDefault.put(mxConstants.STYLE_FONTCOLOR, mxHtmlColor.hexString(Color.DARK_GRAY));
        mxss.putCellStyle(CELL_STYLE_DEFAULT, cellStyleDefault);

        // DEFAULT (edge)
        var edgeStyleDefault = new HashMap<String, Object>();
        edgeStyleDefault.put(mxConstants.STYLE_OPACITY, 100);
        edgeStyleDefault.put(mxConstants.STYLE_STROKE_OPACITY, 100);
        edgeStyleDefault.put(mxConstants.STYLE_STROKECOLOR, "#92D5E6");
        mxss.putCellStyle(EDGE_STYLE_DEFAULT, edgeStyleDefault);

        // OPTIONAL (cell)
        var cellStyleOptional = new HashMap<String, Object>(cellStyleDefault);
        cellStyleOptional.put(mxConstants.STYLE_OPACITY, 80);
        cellStyleOptional.put(mxConstants.STYLE_STROKE_OPACITY, 80);
        cellStyleOptional.put(mxConstants.STYLE_STROKECOLOR, "#FCAF58");
        cellStyleOptional.put(mxConstants.STYLE_FILLCOLOR, "#F9C784");
        mxss.putCellStyle(CELL_STYLE_OPTIONAL, cellStyleOptional);

        // OPTIONAL (edge)
        var edgeStyleOptional = new HashMap<String, Object>(edgeStyleDefault);
        edgeStyleOptional.put(mxConstants.STYLE_DASHED, true);
        edgeStyleOptional.put(mxConstants.STYLE_OPACITY, 80);
        edgeStyleOptional.put(mxConstants.STYLE_STROKE_OPACITY, 80);
        edgeStyleOptional.put(mxConstants.STYLE_STROKECOLOR, "#F9C784");
        mxss.putCellStyle(EDGE_STYLE_OPTIONAL, edgeStyleOptional);

        // REQUIRED (cell)
        var cellStyleRequired = new HashMap<String, Object>(cellStyleDefault);
        mxss.putCellStyle(CELL_STYLE_REQUIRED, cellStyleRequired);

        // REQUIRED (edge)
        var edgeStyleRequired = new HashMap<String, Object>(edgeStyleDefault);
        mxss.putCellStyle(EDGE_STYLE_REQUIRED, edgeStyleRequired);

        // DEFEATED (cell)
        var cellStyleDefeated = new HashMap<String, Object>(cellStyleDefault);
        cellStyleDefeated.put(mxConstants.STYLE_OPACITY, 50);
        cellStyleDefeated.put(mxConstants.STYLE_STROKE_OPACITY, 50);
        cellStyleDefeated.put(mxConstants.STYLE_STROKECOLOR, mxHtmlColor.hexString(Color.DARK_GRAY));
        cellStyleDefeated.put(mxConstants.STYLE_FILLCOLOR, mxHtmlColor.hexString(Color.LIGHT_GRAY));
        mxss.putCellStyle(CELL_STYLE_DEFEATED, cellStyleDefeated);

        // DEFEATED (edge)
        var edgeStyleDefeated = new HashMap<String, Object>(edgeStyleDefault);
        edgeStyleDefeated.put(mxConstants.STYLE_OPACITY, 50);
        edgeStyleDefeated.put(mxConstants.STYLE_STROKE_OPACITY, 50);
        edgeStyleDefeated.put(mxConstants.STYLE_STROKECOLOR, mxHtmlColor.hexString(Color.LIGHT_GRAY));
        mxss.putCellStyle(EDGE_STYLE_DEFEATED, edgeStyleDefeated);
    }

    public static enum Format {
        SVG, PNG, UML
    }

    // An image canvas that will write to a BufferedImage in ARGB mode.
    static class AlphaImageCanvas extends mxImageCanvas {
        public AlphaImageCanvas(mxGraphics2DCanvas canvas, int width, int height, Color background, boolean antiAlias) {
            this(canvas, width, height, background, antiAlias, true);
        }

        public AlphaImageCanvas(mxGraphics2DCanvas canvas, int width, int height, Color background, boolean antiAlias, boolean textAntiAlias) {
            super(canvas, 0, 0, null, antiAlias, textAntiAlias); // super creates an 'empty' image first but it is overwritten by the image below (type ARGB)
            this.image = mxUtils.createBufferedImage(width, height, background, BufferedImage.TYPE_INT_ARGB);

            if(this.image != null) {
                Graphics2D g = this.image.createGraphics();
                mxUtils.setAntiAlias(g, antiAlias, textAntiAlias);
                canvas.setGraphics(g);
            }
        }
    }

    static class Node {
        public final TrainerNode[] trainers;
        public final String name;

        public Node(PlayerState playerState, String seriesId, TrainerNode[] trainers) {
            this(playerState, seriesId, trainers, false);
        }

        public Node(PlayerState playerState, String seriesId, TrainerNode[] trainers, boolean displayIds) {
            var rct = RCTMod.getInstance();
            var hide = rct.getSeriesManager().getGraph(seriesId).getMetaData().hideTrainerIdentities().get();
            var tm = rct.getTrainerManager();
            var sb = new StringBuilder();

            // level cap
            sb.append(Stream.concat(Stream.of(LevelUtils.initialLevelCap(seriesId)), Streams.of(trainers).map(tn -> LevelUtils.trainerLevel(seriesId, tn.id()))).max(Integer::compare).orElse(0)).append('\n');

            // trainer ids/names
            if(displayIds) {
                if(hide && isHidden(playerState, trainers)) {
                    sb.append(HIDDEN_TRAINER_NAME).append('\n');
                } else {
                    for(var trainer : trainers) {
                        sb.append(trainer.id()).append('\n');
                    }
                }
            } else {
                if(hide && isHidden(playerState, trainers)) {
                    sb.append(HIDDEN_TRAINER_NAME).append('\n');
                } else {
                    var names = new HashSet<String>();

                    for(var trainer : trainers) {
                        names.add(tm.getData(trainer.id()).getTrainerTeam().getName().getComponent().getString());
                    }

                    for(var name : names) {
                        sb.append(name).append('\n');
                    }
                }
            }

            this.name = sb.toString().trim();
            this.trainers = trainers;
        }

        @Override
        public String toString() {
            return this.name;
        }

        static boolean isHidden(PlayerState playerState, TrainerNode[] trainers) {
            return Stream.of(trainers).noneMatch(tn -> playerState.getDefeatedTrainerIds().contains(tn.id())
                || playerState.getTrainerDefeatCount(tn.id()) > 0
                || playerState.isKeyTrainer(tn.id())
                || tn.successors().allMatch(succ -> Stream.concat(Stream.of(succ), succ.siblings())
                    // .filter(succ2 -> !succ2.isOptional()) // TODO: not sure
                    .map(sib -> sib.id()).anyMatch(playerState.getDefeatedTrainerIds()::contains)));
        }
    }

    static class Edge {
        @Override
        public String toString() {
            return "";
        }
    }
}
