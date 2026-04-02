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

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

public class RandomWaitGoal extends Goal {
    private int minWaitTime, maxWaitTime, waitTime, startTime;
    private float probability;
    private Mob mob;

    public RandomWaitGoal(Mob mob) {
        this(mob, 80, 240, 0.0025f);
    }

    public RandomWaitGoal(Mob mob, int minWaitTime, int maxWaitTime, float probability) {
        this.mob = mob;
        this.minWaitTime = minWaitTime;
        this.maxWaitTime = maxWaitTime;
        this.probability = probability;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public void start() {
        this.waitTime = this.mob.getRandom().nextInt(this.minWaitTime, this.maxWaitTime + 1);
        this.startTime = this.mob.tickCount;
        this.mob.getNavigation().stop();
        super.start();
    }

    @Override
    public void stop() {
        super.stop();
    }

    @Override
    public boolean canUse() {
        return this.probability > this.mob.getRandom().nextFloat();
    }

    @Override
    public boolean canContinueToUse() {
        return (this.mob.tickCount - this.startTime) < this.waitTime;
    }
}
