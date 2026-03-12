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
package com.gitlab.srcmc.rctmod.world.blocks.entities;

import com.gitlab.srcmc.rctmod.ModRegistries;
import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.gitlab.srcmc.rctmod.world.blocks.TrainerRepelRodBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class TrainerRepelRodBlockEntity extends BlockEntity {
    private boolean initialized, changed, canSpawn;

    public TrainerRepelRodBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(ModRegistries.BlockEntityTypes.TRAINER_REPEL_ROD.get(), blockPos, blockState);
        this.canSpawn = this.changed = !blockState.getValue(TrainerRepelRodBlock.POWERED);
    }
    
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public void setCanSpawn(boolean value) {
        if(this.canSpawn != value) {
            this.canSpawn = value;
            this.changed = !this.isRemoved();
        }
    }

    @Override
    public void setRemoved() {
        if(!this.level.isClientSide) {
            if(this.canSpawn) {
                RCTMod.getInstance().getTrainerSpawner().markChunks(this.level, this.getBlockPos(), false);
            }

            this.canSpawn = this.changed = false;
        }

        super.setRemoved();
    }

    public static void serverTick(Level level, BlockPos blockPos, BlockState blockState, TrainerRepelRodBlockEntity be) {
        if(be.changed) {
            be.initialized = be.initialized || be.canSpawn;

            if(be.initialized) {
                RCTMod.getInstance().getTrainerSpawner().markChunks(level, blockPos, be.canSpawn);
            }

            be.changed = false;
        }
    }

    public static void clientTick(Level level, BlockPos blockPos, BlockState blockState, TrainerRepelRodBlockEntity be) {
    }
}
