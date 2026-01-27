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
