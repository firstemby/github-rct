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
package com.gitlab.srcmc.rctmod.client.screens.widgets.text;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.network.chat.Component;

public class AutoScaledStringWidget extends StringWidget {
    private float scale = 1f, autoScale = 1f;
    private float halign, valign = 0.5f;
    private boolean clip, fit;

    public AutoScaledStringWidget(Component component, Font font) {
        super(component, font);
    }

    public AutoScaledStringWidget(int i, int j, Component component, Font font) {
        super(i, j, component, font);
    }

    public AutoScaledStringWidget(int i, int j, int k, int l, Component component, Font font) {
        super(i, j, k, l, component, font);
        this.setMessage(component);
    }

    @Override
    public void setMessage(Component component) {
        var w = this.getWidth();
        var h = this.getHeight();
        var cw = this.getFont().width(component);
        var ch = this.getFont().lineHeight;
        this.autoScale = Math.min(cw > w ? w/(float)cw : 1f, ch > h ? h/(float)ch : 1f);
        super.setMessage(component);
    }

    public boolean getClip() {
        return this.clip;
    }

    public boolean getFit() {
        return this.fit;
    }

    public float getScale() {
        return this.scale;
    }

    public float getHAlign() {
        return this.halign;
    }

    public float setVAlign() {
        return this.valign;
    }

    public void setClip(boolean clip) {
        this.clip = clip;
    }

    public void setFit(boolean fit) {
        this.fit = fit;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public void setHAlign(float halign) {
        this.halign = halign;
    }

    public void setVAlign(float valign) {
        this.valign = valign;
    }

    public AutoScaledStringWidget clipped(boolean clip) {
        this.setClip(clip);
        return this;
    }

    public AutoScaledStringWidget fitting(boolean fit) {
        this.setFit(fit);
        return this;
    }

    public AutoScaledStringWidget scaled(float scale) {
        this.setScale(scale);
        return this;
    }

    public AutoScaledStringWidget alignCenter() {
        this.halign = 0.5f;
        this.valign = 0.5f;
        return this;
    }

    public AutoScaledStringWidget alignLeft() {
        this.halign = 0f;
        return this;
    }

    public AutoScaledStringWidget alignRight() {
        this.halign = 1f;
        return this;
    }

    public AutoScaledStringWidget alignTop() {
        this.valign = 0f;
        return this;
    }

    public AutoScaledStringWidget alignBottom() {
        this.valign = 1f;
        return this;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
        var clip = this.clip;
        var scale = this.fit ? this.scale*this.autoScale : this.scale;
        var x = this.getX();
        var y = this.getY();
        var w = this.getWidth();
        var h = this.getHeight();

        if(clip) {
            guiGraphics.enableScissor(x, y, x + w, y + h);
        }

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x + (w - this.getFont().width(this.getMessage())*scale)*halign, y + (h - this.getFont().lineHeight*scale)*valign, 0);
        guiGraphics.pose().scale(scale, scale, 1f);
        guiGraphics.pose().translate(-x, -y, 0);
        guiGraphics.drawString(this.getFont(), this.getMessage(), x, y, this.getColor());
        guiGraphics.pose().popPose();

        if(clip) {
            guiGraphics.disableScissor();
        }
    }
}
