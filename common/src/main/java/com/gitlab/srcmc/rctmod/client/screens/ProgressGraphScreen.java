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
package com.gitlab.srcmc.rctmod.client.screens;

import com.gitlab.srcmc.rctmod.ModCommon;
import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.gitlab.srcmc.rctmod.api.data.sync.PlayerState;
import com.gitlab.srcmc.rctmod.api.utils.GraphUtils;
import com.gitlab.srcmc.rctmod.api.utils.LangKeys;
import com.gitlab.srcmc.rctmod.network.ClientActionPayload;
import com.gitlab.srcmc.rctmod.network.ClientActionPayload.Action;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mxgraph.view.mxGraph;

import dev.architectury.networking.NetworkManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import java.awt.image.BufferedImage;

public class ProgressGraphScreen extends Screen {
    private static final float MIN_ZOOM = 0.1f;
    private static final float MAX_ZOOM = 100f;
    private static final float ZOOM_STEP = 0.05f;
    private static final int PADDING = 8;
    private static final int MAX_GRAPH_THREADS = 4;

    private static final int KEY_RESET = InputConstants.KEY_R;
    private static final int KEY_TOGGLE_IDS = InputConstants.KEY_T;
    private static final int KEY_ZOOM_IN = InputConstants.KEY_PAGEUP;
    private static final int KEY_ZOOM_OUT = InputConstants.KEY_PAGEDOWN;

    private final Minecraft mc;
    private final TrainerCardScreen trainerCardScreen;
    private mxGraph graph;

    private DynamicTexture graphTexture;
    private ResourceLocation graphTexturePath;
    private Thread graphThread, graphInitThread;
    private int currentThreads;
    private int maxThreads = MAX_GRAPH_THREADS;
    private boolean closed;

    private float zoom = 1.0f;
    private int offsetX = 0;
    private int offsetY = 0;
    private int graphWidth;
    private int graphHeight;
    private boolean dragging = false;
    private int dragStartX, dragStartY;
    private boolean showTrainerIds;
    private boolean wasMaxZoom;

    private StringWidget widgetMessage;
    private StringWidget widgetSeriesState;
    private StringWidget widgetOffsetX;
    private StringWidget widgetOffsetY;
    private StringWidget widgetZoom;
    private StringWidget widgetKeyReset;
    private StringWidget widgetKeyZoomIn;
    private StringWidget widgetKeyZoomOut;
    private StringWidget widgetKeyPan;
    private StringWidget widgetKeyToggleIds;
    private StringWidget widgetKeyReturn;

    public ProgressGraphScreen(TrainerCardScreen trainerCardScreen) {
        super(Component.literal("Progression Graph")); // TODO: lang (currently not displayed anywhere)
        this.trainerCardScreen = trainerCardScreen;
        this.mc = Minecraft.getInstance();
    }

    @Override
    protected void init() {
        super.init();
        this.initImage();
        this.initMessage();
        this.initInfoBar();
        this.updateInfoBar();
        this.initGraph();
    }

    private void initGraph() {
        var rct = RCTMod.getInstance();
        var ps = PlayerState.get(this.mc.player);
        var sg = rct.getSeriesManager().getGraph(ps.getCurrentSeries()).getRemaining(true);
        var seriesId = ps.getCurrentSeries();
        var defeatedTrainers = ps.getDefeatedTrainerIds();
        var prevThread = this.graphInitThread;

        this.graphInitThread = new Thread(() -> {
            try {
                if(prevThread != null) {
                    prevThread.interrupt();
                    prevThread.join();
                }

                this.graph = GraphUtils.toJXGraph(ps, sg, seriesId, defeatedTrainers, this.showTrainerIds);

                if(!Thread.currentThread().isInterrupted()) {
                    this.mc.execute(this::updateImage);
                }
            } catch(InterruptedException e) {
                if(prevThread != null) {
                    prevThread.interrupt();

                    try {
                        prevThread.join();
                    } catch(InterruptedException _e) {
                        // no op
                    }
                }
            }
        });

        this.graphInitThread.start();
    }

    private void initMessage() {
        this.widgetMessage = new StringWidget(Component.translatable(LangKeys.GUI_TRAINER_CARD_LOADING).append("..."), this.font).alignCenter();
        this.widgetMessage.setWidth(this.width);
        this.widgetMessage.setHeight(this.height);
        this.addRenderableOnly(this.widgetMessage);
    }

    private void initImage() {
        if(this.graphTexture != null) {
            this.graphTexture.close();
            this.graphTexture = null;
        }

        if(this.graphThread != null) {
            this.graphThread.interrupt();
            this.graphThread = null;
        }

        if(this.graphInitThread != null) {
            this.graphInitThread.interrupt();
            this.graphInitThread = null;
        }

        this.graphTexturePath = null;
    }

    private void updateImage() {
        var graph = this.graph;

        if(graph == null || this.currentThreads >= this.maxThreads) {
            return;
        }

        var prevThread = this.graphThread;

        this.graphThread = new Thread(() -> {
            try {
                if(prevThread != null) {
                    prevThread.join();
                }

                var zoom = this.zoom;
                var offX = this.offsetX;
                var offY = this.offsetY;
                var guiScale = this.mc.getWindow().getGuiScale();
                var buffered = GraphUtils.generateImage(graph, -offX * guiScale, -offY * guiScale, this.width * guiScale, this.height * guiScale, zoom);
                var image = toNativeImage(buffered);
                
                if(!Thread.currentThread().isInterrupted()) {
                    this.mc.execute(() -> {
                        if(!this.closed) {
                            this.widgetMessage.visible = false;
                            this.graphWidth = buffered.getWidth();
                            this.graphHeight = buffered.getHeight();

                            if(this.graphTexture == null) {
                                this.graphTexturePath = new ResourceLocation(ModCommon.MOD_ID, "dynamic/progress_graph");
                                this.graphTexture = new DynamicTexture(image);
                                this.mc.getTextureManager().register(this.graphTexturePath, this.graphTexture);
                            } else {
                                this.graphTexture.setPixels(image);
                                this.graphTexture.upload();
                            }
                        }
                    });
                }
            } catch(InterruptedException _e) {
                if(prevThread != null) {
                    prevThread.interrupt();

                    try {
                        prevThread.join();
                    } catch (InterruptedException __e) {
                        // no op
                    }
                }
            } catch(Exception e) {
                ModCommon.LOG.error(e.getMessage(), e);

                this.mc.execute(() -> {
                    this.widgetMessage.setMessage(Component.literal("Failed to render graph: " + e.getMessage())); // TODO: lang
                    this.widgetMessage.visible = true;
                });
            }

            this.currentThreads--;
        });

        if(++this.currentThreads >= this.maxThreads) {
            this.maxThreads = Math.max(1, this.maxThreads / 2);
        } else if(this.maxThreads < MAX_GRAPH_THREADS) {
            this.maxThreads++;
        }

        this.graphThread.start();
    }

    private void initInfoBar() {
        this.removeWidget(this.widgetSeriesState);
        this.removeWidget(this.widgetOffsetX);
        this.removeWidget(this.widgetOffsetY);
        this.removeWidget(this.widgetZoom);
        this.removeWidget(this.widgetKeyReset);
        this.removeWidget(this.widgetKeyZoomIn);
        this.removeWidget(this.widgetKeyZoomOut);
        this.removeWidget(this.widgetKeyPan);
        this.removeWidget(this.widgetKeyToggleIds);
        this.removeWidget(this.widgetKeyReturn);

        this.widgetSeriesState = new StringWidget(0, 0, this.width, this.font.lineHeight, Component.empty(), this.font).alignLeft();
        this.widgetOffsetX = new StringWidget(0, 0, this.width, this.font.lineHeight, Component.empty(), this.font).alignLeft();
        this.widgetOffsetY = new StringWidget(0, 0, this.width, this.font.lineHeight, Component.empty(), this.font).alignLeft();
        this.widgetZoom = new StringWidget(0, 0, this.width, this.font.lineHeight, Component.empty(), this.font).alignLeft();
        this.widgetKeyReset = new StringWidget(0, 0, this.width, this.font.lineHeight, Component.empty(), this.font).alignLeft();
        this.widgetKeyZoomIn = new StringWidget(0, 0, this.width, this.font.lineHeight, Component.empty(), this.font).alignLeft();
        this.widgetKeyZoomOut = new StringWidget(0, 0, this.width, this.font.lineHeight, Component.empty(), this.font).alignLeft();
        this.widgetKeyPan = new StringWidget(0, 0, this.width, this.font.lineHeight, Component.empty(), this.font).alignLeft();
        this.widgetKeyToggleIds = new StringWidget(0, 0, this.width, this.font.lineHeight, Component.empty(), this.font).alignLeft();
        this.widgetKeyReturn = new StringWidget(0, 0, this.width, this.font.lineHeight, Component.empty(), this.font).alignLeft();

        this.addRenderableOnly(this.widgetSeriesState);
        this.addRenderableOnly(this.widgetOffsetX);
        this.addRenderableOnly(this.widgetOffsetY);
        this.addRenderableOnly(this.widgetZoom);
        this.addRenderableOnly(this.widgetKeyReset);
        this.addRenderableOnly(this.widgetKeyZoomIn);
        this.addRenderableOnly(this.widgetKeyZoomOut);
        this.addRenderableOnly(this.widgetKeyPan);
        this.addRenderableOnly(this.widgetKeyToggleIds);
        this.addRenderableOnly(this.widgetKeyReturn);

        // static messages (bottom left)
        this.widgetKeyReset.setMessage(Component.literal("<").append(styleKeyName(InputConstants.getKey(KEY_RESET, 0).getDisplayName().copy())).append(">: ").append(Component.translatable(LangKeys.GUI_DESCRIPTION_PROGRESS_GRAPH_RESET_VIEW)));
        this.widgetKeyReturn.setMessage(Component.literal("<").append(styleKeyName(this.mc.options.keyInventory.getTranslatedKeyMessage().copy())).append(">: ").append(Component.translatable(LangKeys.GUI_DESCRIPTION_PROGRESS_GRAPH_RETURN)));
        this.widgetKeyPan.setMessage(Component.literal("<").append(Component.translatable(LangKeys.GUI_TRAINER_CARD_HOLD)).append(" ").append(styleKeyName(Component.translatable(LangKeys.GUI_MOUSE_LEFT))).append(">: ").append(Component.translatable(LangKeys.GUI_DESCRIPTION_PROGRESS_GRAPH_PAN_VIEW)));
        this.widgetKeyToggleIds.setMessage(Component.literal("<").append(styleKeyName(InputConstants.getKey(KEY_TOGGLE_IDS, 0).getDisplayName().copy())).append(">: ").append(Component.translatable(LangKeys.GUI_DESCRIPTION_PROGRESS_GRAPH_TOGGLE_IDS)));
        this.widgetKeyZoomIn.setMessage(Component.literal("<").append(styleKeyName(InputConstants.getKey(KEY_ZOOM_IN, 0).getDisplayName().copy())).append("/").append(styleKeyName(Component.translatable(LangKeys.GUI_MOUSE_WHEEL))).append(">: ").append(Component.translatable(LangKeys.GUI_DESCRIPTION_PROGRESS_GRAPH_ZOOM_IN)));
        this.widgetKeyZoomOut.setMessage(Component.literal("<").append(styleKeyName(InputConstants.getKey(KEY_ZOOM_OUT, 0).getDisplayName().copy())).append("/").append(styleKeyName(Component.translatable(LangKeys.GUI_MOUSE_WHEEL))).append(">: ").append(Component.translatable(LangKeys.GUI_DESCRIPTION_PROGRESS_GRAPH_ZOOM_OUT)));

        var x = PADDING;
        var y = this.height - 6 * this.font.lineHeight - PADDING;

        this.widgetKeyReset.setPosition(x, y);
        this.widgetKeyReturn.setPosition(x, y + 1 * this.font.lineHeight);
        this.widgetKeyToggleIds.setPosition(x, y + 2 * this.font.lineHeight);
        this.widgetKeyPan.setPosition(x, y + 3 * this.font.lineHeight);
        this.widgetKeyZoomIn.setPosition(x, y + 4 * this.font.lineHeight);
        this.widgetKeyZoomOut.setPosition(x, y + 5 * this.font.lineHeight);
    }

    private static Component styleKeyName(MutableComponent name) {
        return name.withStyle(ChatFormatting.AQUA);
    }

    private void updateInfoBar() {
        var ps = PlayerState.get(this.mc.player);
        var sg = RCTMod.getInstance().getSeriesManager().getGraph(ps.getCurrentSeries());
        var required = sg.getRemaining(ps.getDefeatedTrainerIds(), false, false, true).size();

        // dynamic messages (top left)
        this.widgetSeriesState.setMessage(Component.empty().append(RCTMod.getInstance().getSeriesManager().getGraph(ps.getCurrentSeries()).getMetaData().title().getComponent().withStyle(ChatFormatting.ITALIC)).append(": ").append(required > 0
            ? Component.translatable(LangKeys.GUI_TRAINER_CARD_MISSING_TRAINERS).append(" ").append(String.valueOf(required))
            : Component.translatable(LangKeys.GUI_TRAINER_CARD_COMPLETED)));

        this.widgetSeriesState.setPosition(PADDING, PADDING);

        // dynamic messages (top right)
        this.widgetZoom.setMessage(Component.translatable(LangKeys.GUI_TRAINER_CARD_ZOOM).append(": ").append(String.valueOf((int)(this.zoom * 100))).append("%"));
        this.widgetOffsetX.setMessage(Component.translatable(LangKeys.GUI_TRAINER_CARD_X).append(": ").append(String.valueOf(-this.offsetX)));
        this.widgetOffsetY.setMessage(Component.translatable(LangKeys.GUI_TRAINER_CARD_Y).append(": ").append(String.valueOf(-this.offsetY)));

        var maxWidth = Math.max(this.font.width(this.widgetZoom.getMessage()), Math.max(this.font.width(this.widgetOffsetX.getMessage()), this.font.width(this.widgetOffsetY.getMessage())));
        var x = this.width - maxWidth - PADDING;
        var y = PADDING;

        this.widgetZoom.setPosition(x, y);
        this.widgetOffsetX.setPosition(x, y + 1 * this.font.lineHeight);
        this.widgetOffsetY.setPosition(x, y + 2 * this.font.lineHeight);

        this.widgetZoom.setWidth(maxWidth + PADDING);
        this.widgetOffsetX.setWidth(maxWidth + PADDING);
        this.widgetOffsetY.setWidth(maxWidth + PADDING);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        super.renderBackground(guiGraphics, mouseX, mouseY, delta);

        if(this.graphTexturePath != null) {
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            guiGraphics.blit(this.graphTexturePath, 0, 0, this.width, this.height, 0, 0, this.graphWidth, this.graphHeight, this.graphWidth, this.graphHeight);
            RenderSystem.disableBlend();
            RenderSystem.defaultBlendFunc();
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        var oldZoom = this.updateZoom((float)scrollY * ZOOM_STEP);
        this.offsetX = (int)(mouseX - (mouseX - this.offsetX) * ((oldZoom != 0 ? this.zoom / oldZoom : 1.0)));
        this.offsetY = (int)(mouseY - (mouseY - this.offsetY) * ((oldZoom != 0 ? this.zoom / oldZoom : 1.0)));
        this.updateInfoBar();
        this.updateImage();
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if(keyCode == KEY_ZOOM_IN) {
            this.updateZoom(ZOOM_STEP);
            this.updateInfoBar();
            this.updateImage();
            return true;
        }

        if(keyCode == KEY_ZOOM_OUT) {
            this.updateZoom(-ZOOM_STEP);
            this.updateInfoBar();
            this.updateImage();
            return true;
        }

        if(keyCode == KEY_RESET) {
            this.zoom = 1.0f;
            this.offsetX = 0;
            this.offsetY = 0;
            this.updateInfoBar();
            this.updateImage();
            return true;
        }

        if(keyCode == KEY_TOGGLE_IDS) {
            this.showTrainerIds = !this.showTrainerIds;
            this.initGraph();
            return true;
        }

        if(this.mc.options.keyInventory.matches(keyCode, scanCode)) {
            this.mc.setScreen(this.trainerCardScreen);
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(button == 0) {
            this.dragging = true;
            this.dragStartX = (int)mouseX - this.offsetX;
            this.dragStartY = (int)mouseY - this.offsetY;
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if(button == 0) {
            this.dragging = false;
            return true;
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if(this.updateOffset(mouseX, mouseY)) {
            return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    private float updateZoom(float add) {
        var oldZoom = this.zoom;
        this.zoom = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, this.zoom + add));

        if(!this.wasMaxZoom && this.zoom >= MAX_ZOOM) {
            NetworkManager.sendToServer(new ClientActionPayload(Action.ZOOM_GRAPH_FULL));
            this.wasMaxZoom = true;
        }

        return oldZoom;
    }

    private boolean updateOffset(double mouseX, double mouseY) {
        if(this.dragging) {
            this.offsetX = (int)mouseX - this.dragStartX;
            this.offsetY = (int)mouseY - this.dragStartY;
            this.updateInfoBar();
            this.updateImage();
            return true;
        }

        return false;
    }

    @Override
    public void onClose() {
        this.closed = true;

        if(this.graphThread != null) {
            this.graphThread.interrupt();
        }

        if(this.graphTexture != null) {
            this.graphTexture.close();
        }

        super.onClose();
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    public static NativeImage toNativeImage(BufferedImage buffered) {
        var nativeImage = new NativeImage(buffered.getWidth(), buffered.getHeight(), false);
        var pixels = buffered.getRGB(0, 0, buffered.getWidth(), buffered.getHeight(), null, 0, buffered.getWidth());

        for(int i = 0; i < pixels.length; i++) {
            var argb = pixels[i];
            var abgr = (argb & 0xFF00FF00) | ((argb & 0x00FF0000) >> 16) | ((argb & 0x000000FF) << 16);
            nativeImage.setPixelRGBA(i % buffered.getWidth(), i / buffered.getWidth(), abgr);
        }
        
        return nativeImage;
    }
}
