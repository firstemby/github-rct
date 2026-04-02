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
package com.gitlab.srcmc.rctmod.advancements.criteria;

import com.gitlab.srcmc.rctmod.world.entities.TrainerMob;
import com.mojang.serialization.Codec;

import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;

public class DefeatCountTrigger extends SimpleCriterionTrigger<DefeatCountTriggerInstance> {
    public void trigger(ServerPlayer player, TrainerMob mob) {
        this.trigger(player, triggerInstance -> triggerInstance.matches(player, mob));
    }

    @Override
    public Codec<DefeatCountTriggerInstance> codec() {
        return DefeatCountTriggerInstance.CODEC;
    }
}
