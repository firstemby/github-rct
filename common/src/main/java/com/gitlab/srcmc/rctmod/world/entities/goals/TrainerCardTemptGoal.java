package com.gitlab.srcmc.rctmod.world.entities.goals;

import com.gitlab.srcmc.rctmod.ModRegistries;
import com.gitlab.srcmc.rctmod.world.entities.TrainerMob;

import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;

public class TrainerCardTemptGoal extends TemptGoal {
    private static final double DEFAULT_MOVE_SPEED = 0.45;

    private TrainerMob trainer;

    public TrainerCardTemptGoal(TrainerMob trainer) {
        super(trainer, DEFAULT_MOVE_SPEED, item -> item.is(ModRegistries.Items.TRAINER_CARD.get()), false);
        this.trainer = trainer;
    }

    @Override
    public boolean canUse() {
        return super.canUse() && !this.lookedAtByTarget();
    }

    private boolean lookedAtByTarget() {
        if(this.player != null) {
            var from = this.player.getEyePosition();
            var to = from.add(this.player.getForward().scale(this.trainer.getForceBattleMaxDistance()));
            var hit = ProjectileUtil.getEntityHitResult(this.mob.level(), this.player, from, to, AABB.ofSize(from, 0.15, 0.15, 0.15).expandTowards(to.subtract(from)), e -> true);
            return hit != null && hit.getEntity() == this.mob;
        }

        return false;
    }
}
