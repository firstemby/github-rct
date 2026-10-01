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

import java.util.List;
import java.util.Optional;
import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.gitlab.srcmc.rctmod.api.data.sync.PlayerState;
import com.gitlab.srcmc.rctmod.world.entities.TrainerMob;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger.SimpleInstance;

import net.minecraft.server.level.ServerPlayer;

public record DefeatCountTriggerInstance(List<String> trainerIds, String trainerType, int count) implements SimpleInstance {
    public boolean matches(ServerPlayer player, TrainerMob mob) {
        var tm = RCTMod.getInstance().getTrainerManager();
        var battleMem = tm.getBattleMemory(mob);
        var mobTr = tm.getData(mob);

        if(!this.trainerIds.isEmpty() && this.trainerIds.contains(mob.getTrainerId())) {
            return battleMem.getDefeatByCount(mob.getTrainerId(), player) >= this.count;
        }

        var seriesId = PlayerState.get(player).getCurrentSeries();

        if(!this.trainerType.isEmpty() && this.trainerType.equals(mobTr.getType(seriesId).id())) {
            var trainers = tm.getAllData().filter(e -> e.getValue().getType(seriesId).id().equals(this.trainerType));
            var level = player.getServer().overworld();

            if(this.count >= 0) {
                var it = trainers.iterator();
                var c = this.count;

                while(it.hasNext()) {
                    var e = it.next();
                    var mem = tm.getBattleMemory(level, e.getKey());

                    if(mem.getDefeatByCount(e.getKey(), player) > 0) {
                        if(--c <= 0) {
                            return true;
                        }
                    }
                }
            } else {
                return trainers // TODO: this might need optimization (potential many lookups after each battles for certain trainer types)
                    .allMatch(e -> tm.getBattleMemory(level, e.getKey())
                    .getDefeatByCount(e.getKey(), player) > 0);
            }

            return false;
        }

        return this.trainerIds.isEmpty() && this.trainerType.isEmpty()
            && battleMem.getDefeatByCount(mob.getTrainerId(), player) >= this.count;
    }

    @Override
    public Optional<ContextAwarePredicate> player() {
        return Optional.empty();
    }

    public static final Codec<DefeatCountTriggerInstance> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.list(Codec.STRING).optionalFieldOf("trainer_ids", List.of()).forGetter(DefeatCountTriggerInstance::trainerIds),
            Codec.STRING.optionalFieldOf("trainer_type", "").forGetter(DefeatCountTriggerInstance::trainerType),
            Codec.INT.optionalFieldOf("count", 1).forGetter(DefeatCountTriggerInstance::count)
        ).apply(instance, DefeatCountTriggerInstance::new));
}
