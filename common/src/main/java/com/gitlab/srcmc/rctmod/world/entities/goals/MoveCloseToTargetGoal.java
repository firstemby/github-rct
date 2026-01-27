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
