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
