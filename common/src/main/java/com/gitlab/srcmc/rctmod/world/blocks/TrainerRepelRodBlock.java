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
package com.gitlab.srcmc.rctmod.world.blocks;

import org.jetbrains.annotations.Nullable;

import com.gitlab.srcmc.rctmod.ModRegistries;
import com.gitlab.srcmc.rctmod.world.blocks.entities.TrainerRepelRodBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TrainerRepelRodBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
    public static final MapCodec<TrainerRepelRodBlock> CODEC = TrainerRepelRodBlock.simpleCodec(TrainerRepelRodBlock::new);
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    public TrainerRepelRodBlock() {
        this(BlockBehaviour.Properties.ofFullCopy(Blocks.LIGHTNING_ROD));
        this.registerDefaultState(this.stateDefinition.any()
            .setValue(FACING, Direction.UP)
            .setValue(WATERLOGGED, false)
            .setValue(POWERED, false));
    }

    public TrainerRepelRodBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new TrainerRepelRodBlockEntity(blockPos, blockState);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        var level = blockPlaceContext.getLevel();

        return this.defaultBlockState()
            .setValue(FACING, blockPlaceContext.getClickedFace())
            .setValue(WATERLOGGED, level.getFluidState(blockPlaceContext.getClickedPos()).getType() == Fluids.WATER)
            .setValue(POWERED, level.hasNeighborSignal(blockPlaceContext.getClickedPos()));
    }

    @Override
    protected BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        if(blockState.getValue(WATERLOGGED)) {
            levelAccessor.scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelAccessor));
        }

        return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
    }

    @Override
    protected RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }

    @Override
    protected FluidState getFluidState(BlockState blockState) {
        return blockState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(blockState);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(new Property[]{FACING, WATERLOGGED, POWERED});
    }

    @Override
    protected void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl) {
        if(!level.isClientSide) {
            this.updatePowered(level, blockState, blockPos);
        }
        
        super.neighborChanged(blockState, level, blockPos, block, blockPos2, bl);
    }

    @Override
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, RandomSource randomSource) {
        if(blockState.getValue(POWERED)) {
            return;
        }

        var facing = blockState.getValue(FACING);
        double x, y, z;

        switch (facing) {
            case UP:
                x = 0.5;
                y = 1.2;
                z = 0.5;
                break;
            case DOWN:
                x = 0.5;
                y = -0.2;
                z = 0.5;
                break;
            case NORTH:
                x = 0.5;
                y = 0.5;
                z = -0.2;
                break;
            case EAST:
                x = 1.2;
                y = 0.5;
                z = 0.5;
                break;
            case SOUTH:
                x = 0.5;
                y = 0.5;
                z = 1.2;
                break;
            case WEST:
                x = -0.2;
                y = 0.5;
                z = 0.5;
                break;
            default:
                x = y = z = 0.5;
        }

        var d = blockPos.getX() + x + (randomSource.nextDouble() - 0.5) * 0.2;
        var e = blockPos.getY() + y + (randomSource.nextDouble() - 0.5) * 0.2;
        var f = blockPos.getZ() + z + (randomSource.nextDouble() - 0.5) * 0.2;
        level.addParticle(DustParticleOptions.REDSTONE, d, e, f, 0.0, 0.0, 0.0);
    }

    @Override
    public MapCodec<? extends BaseEntityBlock> codec() {
        return TrainerRepelRodBlock.CODEC;
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        return TrainerRepelRodBlock.createTickerHelper(blockEntityType, ModRegistries.BlockEntityTypes.TRAINER_REPEL_ROD.get(), level.isClientSide ? TrainerRepelRodBlockEntity::clientTick : TrainerRepelRodBlockEntity::serverTick);
    }

    private void updatePowered(Level level, BlockState blockState, BlockPos blockPos) {
        var powered = level.hasNeighborSignal(blockPos);

        if(blockState.getValue(POWERED) != powered) {
            level.setBlock(blockPos, blockState.setValue(POWERED, powered), Block.UPDATE_CLIENTS);
            level.getBlockEntity(blockPos, ModRegistries.BlockEntityTypes.TRAINER_REPEL_ROD.get()).ifPresent(be -> be.setCanSpawn(!powered));
        }
    }

    // RodBlock
    protected static final float AABB_MIN = 6.0F;
    protected static final float AABB_MAX = 10.0F;
    protected static final VoxelShape Y_AXIS_AABB = Block.box((double)6.0F, (double)0.0F, (double)6.0F, (double)10.0F, (double)16.0F, (double)10.0F);
    protected static final VoxelShape Z_AXIS_AABB = Block.box((double)6.0F, (double)6.0F, (double)0.0F, (double)10.0F, (double)10.0F, (double)16.0F);
    protected static final VoxelShape X_AXIS_AABB = Block.box((double)0.0F, (double)6.0F, (double)6.0F, (double)16.0F, (double)10.0F, (double)10.0F);

    @Override
    protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        switch(Direction.Axis.VALUES[blockState.getValue(FACING).getAxis().ordinal()]) {
            case X:
            default:
                return X_AXIS_AABB;
            case Z:
                return Z_AXIS_AABB;
            case Y:
                return Y_AXIS_AABB;
        }
    }

    @Override
    protected BlockState rotate(BlockState blockState, Rotation rotation) {
        return blockState.setValue(FACING, rotation.rotate((Direction)blockState.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState blockState, Mirror mirror) {
        return blockState.setValue(FACING, mirror.mirror((Direction)blockState.getValue(FACING)));
    }

    @Override
    protected boolean isPathfindable(BlockState blockState, PathComputationType pathComputationType) {
        return false;
    }
}
