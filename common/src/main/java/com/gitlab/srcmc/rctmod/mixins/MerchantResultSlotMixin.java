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
package com.gitlab.srcmc.rctmod.mixins;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.gitlab.srcmc.rctapi.api.util.Text;
import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.gitlab.srcmc.rctmod.api.service.SeriesManager;
import com.gitlab.srcmc.rctmod.api.utils.ChatUtils;
import com.gitlab.srcmc.rctmod.api.utils.LangKeys;
import com.gitlab.srcmc.rctmod.world.entities.TrainerAssociation;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantContainer;
import net.minecraft.world.inventory.MerchantResultSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;

@Mixin(MerchantResultSlot.class)
public class MerchantResultSlotMixin {
    @Shadow @Final private Merchant merchant;
    @Shadow @Final private MerchantContainer slots;

    @Inject(method = "onTake", at = @At("RETURN"), remap = true)
    public void inject_onTake(Player player, ItemStack itemStack, CallbackInfo ci) {
        if(this.merchant instanceof TrainerAssociation ta) {
            offerSeriesSwitch(ta, player, itemStack);
        }
    }

    @Inject(method = "onQuickCraft", at = @At("RETURN"), remap = true)
    protected void inject_onQuickCraft(ItemStack itemStack, int i, CallbackInfo ci) {
        if(this.merchant instanceof TrainerAssociation ta) {
            offerSeriesSwitch(ta, ta.getTradingPlayer(), itemStack);
        }
    }

    private static void offerSeriesSwitch(TrainerAssociation ta, Player player, ItemStack itemStack) {
        if(!itemStack.isEmpty()) {
            var offer = ta.takeOffer();

            if(offer != null) {
                if(!replaceDummy(player, itemStack, offer.getValue())) {
                    itemStack.remove(DataComponents.CUSTOM_NAME);
                    itemStack.remove(DataComponents.LORE);
                    itemStack.applyComponents(offer.getValue().getComponents());
                }

                var tpd = RCTMod.getInstance().getTrainerManager().getData(player);
                var seriesPaused = SeriesManager.FREEROAM_SERIES_ID.equals(offer.getKey());
                var seriesContinued = SeriesManager.FREEROAM_SERIES_ID.equals(tpd.getCurrentSeries()) && offer.getKey().equals(tpd.getPreviousSeries());
                
                ChatUtils.sendTitle(player,
                    Text.translatable(seriesPaused ? LangKeys.GUI_TITLE_SERIES_PAUSED : (seriesContinued ? LangKeys.GUI_TITLE_SERIES_CONTINUED : LangKeys.GUI_TITLE_SERIES_STARTED)),
                    RCTMod.getInstance().getSeriesManager().getGraph(seriesPaused ? tpd.getCurrentSeries() : offer.getKey()).getMetaData().title());
                    
                tpd.setCurrentSeries(offer.getKey(), seriesPaused || seriesContinued);
                ta.updateOffersFor(player);
            }
        }
    }

    private static boolean replaceDummy(Player player, ItemStack dummy, ItemStack replacement) {
        var inv = player.getInventory();
        var i = inv.findSlotMatchingItem(dummy);

        if(i >= 0) {
            inv.setItem(i, replacement);
            return true;
        }

        return false;
    }
}
