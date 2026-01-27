/*
 * This file is part of Radical Cobblemon Trainers.
 * Copyright (c) 2026, HDainester, All rights reserved.
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
