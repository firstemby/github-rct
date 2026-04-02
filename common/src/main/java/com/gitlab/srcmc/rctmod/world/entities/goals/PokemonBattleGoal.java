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
package com.gitlab.srcmc.rctmod.world.entities.goals;

import java.util.EnumSet;

import com.gitlab.srcmc.rctmod.world.entities.TrainerMob;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;

public class PokemonBattleGoal extends LookAtPlayerGoal {
    private static final float LOOK_AT_SWITCH_CHANCE = 0.015f;

    private TrainerMob trainer;
    private boolean lookAtPlayer;

    public PokemonBattleGoal(TrainerMob trainer) {
        super(trainer, Mob.class, 8.0F, 1F, false);
        this.trainer = trainer;
        this.setFlags(EnumSet.of(Flag.LOOK, Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if(this.trainer.isInBattle()) {
            if(this.lookAtPlayer) {
                this.lookAt = this.mob.level().getNearestPlayer(this.lookAtContext, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
            } else if(this.lookAt == null || this.mob.distanceToSqr(this.lookAt) > this.lookDistance * this.lookDistance || this.mob.getRandom().nextFloat() < LOOK_AT_SWITCH_CHANCE) {
                this.lookAt = this.mob.level().getNearestEntity(
                    this.mob.level().getEntitiesOfClass(this.lookAtType, this.mob.getBoundingBox().inflate((double)this.lookDistance, 3.0, (double)this.lookDistance), (livingEntity) -> true),
                    this.lookAtContext, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
            }

            if(this.lookAt == null || this.mob.getRandom().nextFloat() < LOOK_AT_SWITCH_CHANCE) {
                this.lookAtPlayer = !this.lookAtPlayer;
            }

            return true;
        }

        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse();
    }

    @Override
    public void start() {
        this.trainer.getNavigation().stop();
    }

    @Override
    public void stop() {
        this.lookAt = null;
    }
    
    @Override
    public void tick() {
        if(this.lookAt != null && this.lookAt.isAlive()) {
            this.mob.getLookControl().setLookAt(this.lookAt.getX(), this.lookAt.getEyeY(), this.lookAt.getZ());
        }
    }
}
