/*
 * This file is part of Radical Cobblemon Trainers.
 * Copyright (c) 2025, HDainester, All rights reserved.
 *
 * Radical Cobblemon Trainers is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Radical Cobblemon Trainers is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Radical Cobblemon Trainers. If not, see <http://www.gnu.org/licenses/lgpl>.
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
