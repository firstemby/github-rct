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

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.cobblemon.mod.common.CobblemonMemories;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.entity.pokemon.ai.sensors.DefendOwnerSensor;
import com.cobblemon.mod.common.util.BrainExtensionsKt;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.monster.Enemy;

@Mixin(DefendOwnerSensor.class)
public abstract class DefendOwnerSensorMixin extends Sensor<PokemonEntity> {
    /**
     * Fixes pokemon 'defending' against arbitrary entities targeting players
     * as this may not always indicate hostile intentions (e.g. Trainers)
     * 
     * The check for hostile mobs now additionally requires the mob to either
     * implement the {@link Enemy} interface or otherwise be recently hit by
     * the owner (see {@link isHostile(LivingEntity, LivingEntity)}).
     * 
     * Issue supposedly exists since Cobblemon 1.7.1.
     */
    @Inject(method = "setNearestAttacker", at = @At("HEAD"), cancellable = true, remap = true)
    private void inject$setNearestAttacker(PokemonEntity entity, NearestVisibleLivingEntities visibleMobs, LivingEntity owner, CallbackInfo ci) {
        var nearestAttacker = visibleMobs.findClosest(livingEntity -> {
            // Prevents Pokémons from attacking players as owned Pokémons are currently invulnerable
            if(livingEntity instanceof ServerPlayer) return false;

            if(livingEntity.getLastHurtByMob() == owner) return true;
            if(isHostile(livingEntity, owner)) return true; // instead of 'if((livingEntity instanceof Mob mob) && mob.getTarget() == owner) return true;'
            
            var mobAttackTarget = BrainExtensionsKt.getMemorySafely(livingEntity.getBrain(), MemoryModuleType.ATTACK_TARGET);
            if(mobAttackTarget.isPresent() && mobAttackTarget.get() == owner) return true;

            return false;
        }).map(mob -> (LivingEntity)mob).orElse(null);

        entity.getBrain().setMemory(CobblemonMemories.NEAREST_VISIBLE_ATTACKER, nearestAttacker);
        ci.cancel();
    }

    private static boolean isHostile(LivingEntity living, LivingEntity towards) {
        return (living instanceof Enemy || towards.getLastHurtByMob() == living) && (living instanceof Mob mob) && mob.getTarget() == towards;
    }
}
