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
package com.gitlab.srcmc.rctmod.world.loot.conditions;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import com.gitlab.srcmc.rctmod.ModRegistries;
import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.gitlab.srcmc.rctmod.world.entities.TrainerMob;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

public record LevelRangeCondition(IntRange range) implements LootItemCondition {
    private static Supplier<LootItemConditionType> TYPE = ModRegistries.LootItemConditions.LEVEL_RANGE;

    public static final MapCodec<LevelRangeCondition> CODEC = RecordCodecBuilder.mapCodec(
        instance -> instance.group(
            IntRange.CODEC.fieldOf("range").forGetter(LevelRangeCondition::range)
        ).apply(instance, LevelRangeCondition::new)
    );

    public LootItemConditionType getType() {
        return TYPE.get();
    }

    public Set<LootContextParam<?>> getReferencedContextParams() {
        return Collections.unmodifiableSet(new HashSet<>(this.range.getReferencedContextParams()));
    }

    public boolean test(LootContext lootContext) {
        if(lootContext.getParamOrNull(LootContextParams.THIS_ENTITY) instanceof TrainerMob mob) {
            var teamLevel = RCTMod.getInstance().getTrainerManager()
                .getData(mob).getTrainerTeam().getTeam().stream()
                .map(p -> p.getLevel()).max(Integer::compare).orElse(0);

            return this.range.test(lootContext, teamLevel);
        }

        return false;
    }
}
