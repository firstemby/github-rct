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

import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.gitlab.srcmc.rctmod.api.service.TrainerManager;
import com.gitlab.srcmc.rctmod.world.entities.TrainerMob;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;

public class ForceIntoBattleGoal extends Goal {
    private static final int TIMEOUT_TICKS = 60;
    private static final float CANCEL_PROBABILITY = 0.005f;
    private static final double BB_HEIGHT = 3;

    private int tickOfLastAttempt, tickOfLookAt;
    private TrainerManager tm;
    private TrainerMob mob;

    private final Class<? extends LivingEntity> lookAtType;
    private Player lookAt;

    public ForceIntoBattleGoal(TrainerMob mob, Class<? extends LivingEntity> clazz) {
        this.setFlags(EnumSet.of(Flag.LOOK, Flag.MOVE));
        this.tm = RCTMod.getInstance().getTrainerManager();
        this.mob = mob;
        this.lookAtType = clazz;
    }

    @Override
    public void start() {
        this.tickOfLookAt = this.mob.tickCount;
        this.mob.getNavigation().stop();
    }

    @Override
    public void stop() {
        this.lookAt = null;
    }

    @Override
    public boolean canUse() {
        if(this.mob.getRandom().nextFloat() < this.mob.getForceBattleChance()
        && this.mob.tickCount - this.tickOfLastAttempt > TIMEOUT_TICKS) {
            var lookAtContext = this.initContext();
            Entity lookAt = null;

            if(lookAtContext != null) {
                if(this.mob.getTarget() != null) {
                    lookAt = this.mob.getTarget();
                }

                if(this.lookAtType == Player.class) {
                    lookAt = this.mob.level().getNearestPlayer(lookAtContext, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
                } else {
                    double d = this.mob.getForceBattleMaxDistance();

                    lookAt = this.mob.level().getNearestEntity(
                        this.mob.level().getEntitiesOfClass(this.lookAtType, this.mob.getBoundingBox().inflate(d, BB_HEIGHT, d), le -> true),
                        lookAtContext, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
                }

                if(lookAt instanceof Player player) {
                    this.lookAt = player;
                    return hasNoMenuOpen(player) && this.isInLevelRange(player) && this.mob.canBattleAgainst(player);
                }
            }
        }

        return false;
    }

    @Override
    public boolean canContinueToUse() {
        double d = this.mob.getForceBattleMaxDistance();

        if(this.lookAt != null && hasNoMenuOpen(this.lookAt)
        && this.lookAt.isAlive() && !this.mob.isInBattle() && this.mob.getCooldown() == 0
        && this.mob.distanceToSqr(this.lookAt) < d * d) {
            return true;
        }

        this.stop();
        return false;
    }

    @Override
    public void tick() {
        if(this.lookAt != null && this.lookAt.isAlive()) {
            this.mob.getLookControl().setLookAt(this.lookAt.getX(), this.mob.getEyeY(), this.lookAt.getZ());
        }

        // TODO: option to check if player holds a trainer card (cancel otherwise)
        if(this.lookAt instanceof Player) {
            // test if target is looking at this mob
            var from = this.lookAt.getEyePosition();
            var to = from.add(this.lookAt.getForward().scale(this.mob.getForceBattleMaxDistance()));
            var hit = ProjectileUtil.getEntityHitResult(this.mob.level(), this.lookAt, from, to, AABB.ofSize(from, 0.15, 0.15, 0.15).expandTowards(to.subtract(from)), e -> true);

            if(hit == null || hit.getEntity() != this.mob) {
                if(this.mob.getRandom().nextFloat() < CANCEL_PROBABILITY) {
                    this.tickOfLastAttempt = this.mob.tickCount;
                    this.stop();
                    return;
                } else {
                    this.tickOfLookAt = this.mob.tickCount;
                }
            }
        }

        if((this.mob.tickCount - this.tickOfLookAt) > this.mob.getForceBattleLookTicks()) {
            this.tryStartBattle();
            this.stop();
        }
    }

    private void tryStartBattle() {
        if(this.lookAt instanceof Player player && this.isInLevelRange(player)) {
            this.mob.startBattleWith(player);
        }

        this.tickOfLastAttempt = this.mob.tickCount;
    }

    private TargetingConditions initContext() {
        return this.lookAtType == Player.class
            ? TargetingConditions.forNonCombat()
                .range((double)this.mob.getForceBattleMaxDistance())
                .selector(le -> EntitySelector.notRiding(mob).test(le))
            : TargetingConditions.forNonCombat()
                .range((double)this.mob.getForceBattleMaxDistance());
    }

    private boolean isInLevelRange(Player player) {
        return Math.abs(this.tm.getPlayerLevel(player) - this.tm.getData(this.mob).getTrainerTeam()
            .getTeam().stream().map(p -> p.getLevel())
            .max(Integer::compare).orElse(0)) <= this.mob.getForceBattleMaxLevelDiff();
    }

    private static boolean hasNoMenuOpen(Player player) {
        return !player.hasContainerOpen(); // TODO: builtin way to check for open (inventory) screen on server side?
    }
}
