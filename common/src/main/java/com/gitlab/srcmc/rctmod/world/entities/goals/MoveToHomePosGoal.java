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

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.level.LevelReader;

public class MoveToHomePosGoal extends MoveToBlockGoal {
    private TrainerMob trainerMob;
    private boolean reachedTarget;
    private boolean hasPath;
    private int searchRange;
    private int ticks;

    public MoveToHomePosGoal(TrainerMob trainerMob) {
        this(trainerMob, 0.35, 64);
    }

    public MoveToHomePosGoal(TrainerMob trainerMob, double speedModifier, int searchRange) {
        super(trainerMob, speedModifier, searchRange);
        this.trainerMob = trainerMob;
        this.searchRange = searchRange;
    }

    @Override
    public void start() {
        this.reachedTarget = false;
        this.ticks = 0;
    }

    @Override
    public double acceptedDistance() {
        return 0.1;
    }

    @Override
    public boolean canUse() {
        // TODO: check if mob still has HomePos
        return this.findNearestBlock() && this.isValidTarget(this.trainerMob.level(), this.blockPos);
    }

    @Override
    public boolean canContinueToUse() {
        return !this.reachedTarget && this.canUse();
    }

    @Override
    protected boolean findNearestBlock() {
        if(this.trainerMob.getHomePos() != null) {
            this.blockPos = this.trainerMob.getHomePos().below();

            if(this.blockPos.closerToCenterThan(this.trainerMob.position().add(0, -0.5, 0), this.searchRange)) {
                return true;
            }
        }

        return false;
    }

    @Override
    protected boolean isValidTarget(LevelReader levelReader, BlockPos blockPos) {
        return !levelReader.getBlockState(blockPos).isAir()
            && levelReader.getBlockState(blockPos.above()).isAir()
            && levelReader.getBlockState(blockPos.above().above()).isAir();
    }

    protected void navigate() {
        if(this.ticks % 40 == 0) {
            this.hasPath = this.mob.getNavigation().moveTo((double)this.blockPos.getX() + 0.5, (double)this.blockPos.getY(), (double)this.blockPos.getZ() + 0.5, 0, this.speedModifier);
        }
    }

    protected void centerOnTarget() {
        var t = this.blockPos.getCenter();
        this.trainerMob.getMoveControl().setWantedPosition(t.x, t.y, t.z, this.speedModifier);
    }

    @Override
    public void tick() {
        if(this.blockPos.closerToCenterThan(this.mob.position().add(0, -0.5, 0),  this.acceptedDistance())) {
            this.reachedTarget = true;
        } else if(!this.hasPath && this.blockPos.closerToCenterThan(this.mob.position().add(0, -0.5, 0), 2)) {
            this.centerOnTarget();
        } else {
            this.navigate();

            if(++this.ticks > 1200) {
                this.ticks = 0;
            }
        }
    }
}
