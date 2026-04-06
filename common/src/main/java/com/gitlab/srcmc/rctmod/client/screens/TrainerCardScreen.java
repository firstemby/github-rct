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

import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.gitlab.srcmc.rctmod.api.utils.LangKeys;
import com.gitlab.srcmc.rctmod.client.screens.widgets.PlayerInfoWidget;
import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class TrainerCardScreen extends Screen {
    private static final int CARD_W = 224;
    private static final int CARD_H = 128;

    private PlayerInfoWidget playerInfo;

    public TrainerCardScreen() {
        super(Component.translatable(LangKeys.GUI_TRAINER_CARD_TITLE));
        var mc = Minecraft.getInstance();
        this.font = mc.font;
    }

    @Override
    protected void init() {
        super.init();

        var window = Minecraft.getInstance().getWindow();
        this.width = window.getGuiScaledWidth();
        this.height = window.getGuiScaledHeight();
        
        var cfg = RCTMod.getInstance().getClientConfig();
        var pad = cfg.trainerCardPadding();
        var card_x = pad + (int)((this.width - (CARD_W + 2*pad))*cfg.trainerCardAlignmentX());
        var card_y = pad + (int)((this.height - (CARD_H + 2*pad))*cfg.trainerCardAlignmentY());

        this.playerInfo = new PlayerInfoWidget(card_x, card_y, CARD_W, CARD_H, this.font);
        this.addRenderableOnly(this.playerInfo);
        for(var r : this.playerInfo.getRenderableOnlies()) this.addRenderableOnly(r);
        for(var w : this.playerInfo.getRenderableWidgets()) this.addRenderableWidget(w);
    }

    @Override
    public boolean keyPressed(int key, int scancode, int mods) {
        var mc = Minecraft.getInstance();

        if(mc.options.keyInventory.matches(key, scancode)) {
            mc.setScreen(null);
            return true;
        }

        // TODO: test feature
        if(key == InputConstants.KEY_G) {
            mc.setScreen(new ProgressGraphScreen());
            return true;
        }

        return super.keyPressed(key, scancode, mods);
    }

    @Override
    public boolean mouseClicked(double d, double e, int i) {
        if(i == 1) {
            var mc = Minecraft.getInstance();
            mc.setScreen(null);
            return true;
        }

        return super.mouseClicked(d, e, i);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        this.playerInfo.tick();
    }
}
