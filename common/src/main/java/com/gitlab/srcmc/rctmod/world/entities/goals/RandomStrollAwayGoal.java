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

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;

public class RandomStrollAwayGoal extends RandomStrollGoal {
    private static final float STROLL_CHANCE = 0.0015f;
    private static final int MAX_TARGET_FAILS = 16;

    private int[] direction;
    private float probability;
    private int failCount;

    public RandomStrollAwayGoal(PathfinderMob pathfinderMob, double d) {
        this(pathfinderMob, d, 0.15f);
    }

    public RandomStrollAwayGoal(PathfinderMob pathfinderMob, double d, float p) {
        super(pathfinderMob, d);
        this.probability = p;
        this.updateDirection();
    }

    @Override
    public boolean canUse() {
        if(this.mob.hasControllingPassenger()
        || this.mob.isPersistenceRequired()
        || this.mob.getRandom().nextFloat() > this.probability) {
            return false;
        }

        if(this.failCount > MAX_TARGET_FAILS) {
            this.updateDirection();
        }

        var pos = this.getPosition();

        if(pos == null) {
            this.failCount++;
            return false;
        }

        this.wantedX = pos.x;
        this.wantedY = pos.y;
        this.wantedZ = pos.z;
        this.failCount = 0;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if(!this.mob.isInWater() && !this.mob.isInLava() && this.mob.getRandom().nextInt(600) == 0) {
            return false;
        }

        return super.canContinueToUse();
    }

    @Override
    protected Vec3 getPosition() {
        var r = this.mob.getRandom().nextFloat();

        if(this.mob.isInWaterOrBubble() || this.mob.isInLava()) {
            Vec3 vec3 = LandRandomPos.getPosTowards(this.mob, 15, 7, this.mob.getPosition(1F).add((int)(r*this.direction[0]), 0, (int)(r*this.direction[1])));
            return vec3 == null ? super.getPosition() : vec3;
        } else {
            return this.mob.getRandom().nextFloat() > STROLL_CHANCE
                ? LandRandomPos.getPosTowards(this.mob, 10, 7, this.mob.getPosition(1F).add((int)(r*this.direction[0]), 0, (int)(r*this.direction[1])))
                : super.getPosition();
        }
    }
    
    @Override
    public void tick() {
        this.mob.getNavigation().setSpeedModifier(this.mob.isInWater() || this.mob.isInLava() ? 1 : this.speedModifier);
        super.tick();
    }

    private void updateDirection() {
        switch(this.mob.getRandom().nextInt(8)) {
            case 0:
                this.direction = new int[]{1000, 0}; // north
                break;
            case 1:
                this.direction = new int[]{1000, 1000}; // north east
                break;
            case 2:
                this.direction = new int[]{0, 1000}; // east
                break;
            case 3:
                this.direction = new int[]{-1000, 1000}; // south east
                break;
            case 4:
                this.direction = new int[]{-1000, 0}; // south
                break;
            case 5:
                this.direction = new int[]{-1000, -1000}; // south west
                break;
            case 6:
                this.direction = new int[]{0, -1000}; // west
                break;
            case 7:
                this.direction = new int[]{1000, -1000}; // north west
                break;
        }
    }
}
