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

import com.gitlab.srcmc.rctmod.world.entities.TrainerMob;

import net.minecraft.world.entity.ai.goal.MoveTowardsTargetGoal;

public class MoveCloseToTargetGoal extends MoveTowardsTargetGoal {
    private static final int DEFAULT_SOCIAL_DISTANCING = 36;
    private static final int PAUSE_MIN_TICKS = 0;
    private static final int PAUSE_MAX_TICKS = 200;

    private TrainerMob mob;
    private double minDistanceSquared;
    private double speedModifier;
    private float probability;
    private int pauseTicks;

    public MoveCloseToTargetGoal(TrainerMob pathfinderMob, double d, float f) {
        this(pathfinderMob, d, 0.5f, f, DEFAULT_SOCIAL_DISTANCING);
    }

    public MoveCloseToTargetGoal(TrainerMob pathfinderMob, double d, float p, float f) {
        this(pathfinderMob, d, p, f, DEFAULT_SOCIAL_DISTANCING);
    }

    public MoveCloseToTargetGoal(TrainerMob pathfinderMob, double d, float p, float f, float g) {
        super(pathfinderMob, d, f);
        this.mob = pathfinderMob;
        this.probability = p;
        this.minDistanceSquared = g*g;
        this.speedModifier = d;
    }

    @Override
    public boolean canUse() {
        var can = this.mob.getRandom().nextFloat() < this.probability
            && this.mob.isRequiredBy(mob.getTarget())
            && super.canUse() && !this.isNearbyTarget();

        if(can) {
            this.pauseTicks = this.mob.getRandom().nextInt(PAUSE_MIN_TICKS, PAUSE_MAX_TICKS);
            this.probability *= 0.75;
        }
        
        return can;
    }

    @Override
    public boolean canContinueToUse() {
        if(!this.mob.isInWater() && !this.mob.isInLava() && this.mob.getRandom().nextInt(600) == 0) {
            return false;
        }

        if(super.canContinueToUse()) {
            if(!this.isNearbyTarget()) {
                return true;
            }
        }

        return false;
    }

    private boolean isNearbyTarget() {
        var target = this.mob.getTarget();
        return target != null && target.distanceToSqr(this.mob) < this.minDistanceSquared;
    }
    
    @Override
    public void tick() {
        if(this.pauseTicks < 0) {
            this.mob.getNavigation().setSpeedModifier(this.mob.isInWater() || this.mob.isInLava() ? 1 : this.speedModifier);
            super.tick();
        } else {
            this.mob.getNavigation().setSpeedModifier(0);
            this.pauseTicks--;
        }
    }
}
