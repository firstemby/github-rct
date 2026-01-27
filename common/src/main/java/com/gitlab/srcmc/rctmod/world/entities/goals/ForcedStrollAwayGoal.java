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

import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.gitlab.srcmc.rctmod.api.service.TrainerSpawner;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;

public class ForcedStrollAwayGoal extends RandomStrollGoal {
    private static final float STROLL_CHANCE = 0.0015f;
    private static final int MAX_TARGET_FAILS = 16;
    private static final int MAX_CHUNK_RADIUS = TrainerSpawner.CHUNK_REPEL_RADIUS * 2;
    private static final int MAX_KEEP_DIRECTION_TICKS = 1200;
    private static final int MIN_KEEP_DIRECTION_TICKS = 300;

    private int[] direction;
    private float probability;
    private int failCount;
    private int tickOfDirection;
    private int keepDirectionTicks;

    public ForcedStrollAwayGoal(PathfinderMob pathfinderMob, double d) {
        this(pathfinderMob, d, 0.15f);
    }

    public ForcedStrollAwayGoal(PathfinderMob pathfinderMob, double d, float p) {
        super(pathfinderMob, d);
        this.probability = p;
    }

    @Override
    public boolean canUse() {
        if(this.mob.hasControllingPassenger()
        || this.mob.isPersistenceRequired()
        || this.mob.getRandom().nextFloat() > this.probability) {
            return false;
        }

        if(!this.isInMarkedChunk()) {
            if(this.direction == null || this.mob.tickCount - this.tickOfDirection > this.keepDirectionTicks) {
                return false;
            }
        }

        if((this.direction == null || this.failCount > MAX_TARGET_FAILS)) {
            if(!this.findUnmarkedChunk(this.mob.chunkPosition())) {
                return false;
            }
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

    private boolean isInMarkedChunk() {
        return RCTMod.getInstance().getTrainerSpawner().isMarkedAt(this.mob.level(), this.mob.chunkPosition());
    }

    private boolean findUnmarkedChunk(ChunkPos from) {
        this.direction = null;

        var spawner = RCTMod.getInstance().getTrainerSpawner();
        var r = this.mob.getRandom().nextInt(MAX_CHUNK_RADIUS);
        var dx = new int[]{-1,  0,  1, -1, 0, 1, -1, 1};
        var dz = new int[]{-1, -1, -1,  1, 1, 1,  0, 0};

        for(int j, i = 1; i <= MAX_CHUNK_RADIUS; i++) {
            for(j = 0; j < dx.length; j++) {
                var k = ((j + r) % MAX_CHUNK_RADIUS); // random offset
                var pos = new ChunkPos(from.x + dx[k] * i, from.z + dz[k] * i);

                if(!spawner.isMarkedAt(this.mob.level(), pos)) {
                    this.direction = new int[]{(pos.x - from.x) * 128, (pos.z - from.z) * 128};
                    this.tickOfDirection = this.mob.tickCount;
                    this.keepDirectionTicks = this.mob.getRandom().nextInt(MIN_KEEP_DIRECTION_TICKS, MAX_KEEP_DIRECTION_TICKS + 1);
                    return true;
                }
            }
        }

        return false;
    }
}
