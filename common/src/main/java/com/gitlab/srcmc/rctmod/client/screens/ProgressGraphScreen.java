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
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.NativeImage;
import com.mxgraph.view.mxGraph;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;

import java.awt.image.BufferedImage;

public class ProgressGraphScreen extends Screen {
    private static final float MIN_ZOOM = 0.25f;
    private static final float MAX_ZOOM = 10f;
    private static final float ZOOM_STEP = 0.1f;
    private static final int PADDING = 8;
    // private static final int UPDATE_DELAY = 20; // deprecated

    private static final int KEY_RESET = InputConstants.KEY_R;
    private static final int KEY_ZOOM_IN = InputConstants.KEY_PAGEUP;
    private static final int KEY_ZOOM_OUT = InputConstants.KEY_PAGEDOWN;
    // private static final int KEY_PAN = InputConstants.MOUSE_BUTTON_LEFT; // no (localized) name?

    private final Minecraft mc;
    private final mxGraph graph;

    private DynamicTexture graphTexture;
    private ResourceLocation graphTexturePath;
    private Thread graphThread;

    private float zoom = 1.0f;
    private int offsetX = 0;
    private int offsetY = 0;
    private int graphWidth;
    private int graphHeight;
    private boolean dragging = false;
    private int dragStartX, dragStartY;

    private StringWidget widgetMessage;
    private StringWidget widgetOffsetX;
    private StringWidget widgetOffsetY;
    private StringWidget widgetZoom;
    private StringWidget widgetKeyReset;
    private StringWidget widgetKeyZoomIn;
    private StringWidget widgetKeyZoomOut;
    private StringWidget widgetKeyPan;

    public ProgressGraphScreen() {
        super(Component.literal("Progression Graph"));
        this.mc = Minecraft.getInstance();
        var rct = RCTMod.getInstance();
        var ps = PlayerState.get(this.mc.player);
        var sg = rct.getSeriesManager().getGraph(ps.getCurrentSeries()).getRemaining(true);
        this.graph = GraphUtils.toJXGraph(sg);
    }

    @Override
    protected void init() {
        super.init();
        this.initMessage();
        this.initInfoBar();
        this.updateInfoBar();
        this.updateImage();
    }

    private void initMessage() {
        this.widgetMessage = new StringWidget(Component.literal("Loading..."), this.font).alignCenter(); // TODO: lang
        this.widgetMessage.setWidth(this.width);
        this.widgetMessage.setHeight(this.height);
        this.addRenderableOnly(this.widgetMessage);
    }

    private void updateImage() {
        var zoom = this.zoom;
        var offX = this.offsetX;
        var offY = this.offsetY;
        var prevThread = this.graphThread;

        this.graphThread = new Thread(() -> {
            try {
                if(prevThread != null) {
                    // prevThread.interrupt();
                    prevThread.join();
                }

                // prevents rapid image (re)generation after fast subsequent updates (next thread interrupts -> looks clumpsy -> alternative idea: max thread count and interrupt oldest thread if exceeded thread)
                // Thread.sleep(UPDATE_DELAY);

                var guiScale = this.mc.getWindow().getGuiScale();
                var buffered = GraphUtils.generateImage(this.graph, -offX * guiScale, -offY * guiScale, this.width * guiScale, this.height * guiScale, zoom);
                var image = toNativeImage(buffered);
                
                if(!Thread.currentThread().isInterrupted()) {
                    this.mc.execute(() -> {
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
                    });
                }
            } catch(InterruptedException _e) {
                // no op
            } catch(Exception e) {
                ModCommon.LOG.error(e.getMessage(), e);

                this.mc.execute(() -> {
                    this.widgetMessage.setMessage(Component.literal("Failed to render graph: " + e.getMessage())); // TODO: lang
                    this.widgetMessage.visible = true;
                });
            }
        });
        
        this.graphThread.setPriority(Thread.NORM_PRIORITY / 2);
        this.graphThread.start();
    }

    private void initInfoBar() {
        this.removeWidget(this.widgetOffsetX);
        this.removeWidget(this.widgetOffsetY);
        this.removeWidget(this.widgetZoom);
        this.removeWidget(this.widgetKeyReset);
        this.removeWidget(this.widgetKeyZoomIn);
        this.removeWidget(this.widgetKeyZoomOut);
        this.removeWidget(this.widgetKeyPan);

        this.widgetOffsetX = new StringWidget(0, 0, this.width, this.font.lineHeight, Component.empty(), this.font).alignLeft();
        this.widgetOffsetY = new StringWidget(0, 0, this.width, this.font.lineHeight, Component.empty(), this.font).alignLeft();
        this.widgetZoom = new StringWidget(0, 0, this.width, this.font.lineHeight, Component.empty(), this.font).alignLeft();
        this.widgetKeyReset = new StringWidget(0, 0, this.width, this.font.lineHeight, Component.empty(), this.font).alignLeft();
        this.widgetKeyZoomIn = new StringWidget(0, 0, this.width, this.font.lineHeight, Component.empty(), this.font).alignLeft();
        this.widgetKeyZoomOut = new StringWidget(0, 0, this.width, this.font.lineHeight, Component.empty(), this.font).alignLeft();
        this.widgetKeyPan = new StringWidget(0, 0, this.width, this.font.lineHeight, Component.empty(), this.font).alignLeft();

        this.addRenderableOnly(this.widgetOffsetX);
        this.addRenderableOnly(this.widgetOffsetY);
        this.addRenderableOnly(this.widgetZoom);
        this.addRenderableOnly(this.widgetKeyReset);
        this.addRenderableOnly(this.widgetKeyZoomIn);
        this.addRenderableOnly(this.widgetKeyZoomOut);
        this.addRenderableOnly(this.widgetKeyPan);

        // static messages (top left)
        this.widgetKeyReset.setMessage(Component.literal(String.format("<%s>: Reset View", InputConstants.getKey(KEY_RESET, 0).getDisplayName().getString())));
        this.widgetKeyZoomIn.setMessage(Component.literal(String.format("<%s / Scroll Up>: Zoom In", InputConstants.getKey(KEY_ZOOM_IN, 0).getDisplayName().getString())));
        this.widgetKeyZoomOut.setMessage(Component.literal(String.format("<%s / Scroll Down>: Zoom Out", InputConstants.getKey(KEY_ZOOM_OUT, 0).getDisplayName().getString())));
        this.widgetKeyPan.setMessage(Component.literal(String.format("<%s + Move>: Pan View", "Click")));

        this.widgetKeyReset.setColor(0xBB676767);
        this.widgetKeyZoomIn.setColor(0xBB676767);
        this.widgetKeyZoomOut.setColor(0xBB676767);
        this.widgetKeyPan.setColor(0xBB676767);

        var x = PADDING;
        var y = PADDING;

        this.widgetKeyReset.setPosition(x, y);
        this.widgetKeyZoomIn.setPosition(x, y + 1 * this.font.lineHeight);
        this.widgetKeyZoomOut.setPosition(x, y + 2 * this.font.lineHeight);
        this.widgetKeyPan.setPosition(x, y + 3 * this.font.lineHeight);
    }

    private void updateInfoBar() {
        // dynamic messages (top right)
        this.widgetZoom.setMessage(Component.literal(String.format("Zoom: %d%%", (int)(this.zoom * 100))));
        this.widgetOffsetX.setMessage(Component.literal(String.format("X: %d", this.offsetX)));
        this.widgetOffsetY.setMessage(Component.literal(String.format("Y: %d", this.offsetY)));

        this.widgetZoom.setColor(0xBB676767);
        this.widgetOffsetX.setColor(0xBB676767);
        this.widgetOffsetY.setColor(0xBB676767);

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
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        super.render(guiGraphics, mouseX, mouseY, delta);

        if(this.graphTexturePath != null) {
            guiGraphics.blit(this.graphTexturePath, 0, 0, this.width, this.height, 0, 0, this.graphWidth, this.graphHeight, this.graphWidth, this.graphHeight);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        var oldZoom = this.zoom;
        this.zoom = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, this.zoom + (float)scrollY * ZOOM_STEP));
        this.offsetX = (int)(mouseX - (mouseX - this.offsetX) * ((oldZoom != 0 ? this.zoom / oldZoom : 1.0)));
        this.offsetY = (int)(mouseY - (mouseY - this.offsetY) * ((oldZoom != 0 ? this.zoom / oldZoom : 1.0)));
        this.updateInfoBar();
        this.updateImage();
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if(keyCode == KEY_ZOOM_IN) {
            this.zoom = Math.min(MAX_ZOOM, this.zoom + ZOOM_STEP);
            this.updateInfoBar();
            this.updateImage();
            return true;
        }

        if(keyCode == KEY_ZOOM_OUT) {
            this.zoom = Math.max(MIN_ZOOM, this.zoom - ZOOM_STEP);
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
        
        for(int x, y = 0; y < buffered.getHeight(); y++) {
            for(x = 0; x < buffered.getWidth(); x++) {
                nativeImage.setPixelRGBA(x, y, buffered.getRGB(x, y));
            }
        }
        
        return nativeImage;
    }
}
