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
