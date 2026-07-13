package com.gitlab.srcmc.rctmod.fabric;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.StructureBlockEntity;

import com.gitlab.srcmc.rctmod.ModCommon;

public class RCTModGameTest {
	@GameTest(template = "rctmod:platform")
	public void platform(GameTestHelper context) {
		if(context.getBlockEntity(BlockPos.ZERO) instanceof StructureBlockEntity strutureBlock) {
			strutureBlock.placeStructure(context.getLevel());
		}

		var center = BlockPos.ZERO.above();
		context.assertBlockPresent(Blocks.GOLD_BLOCK, center);
		context.succeed();
	}
}
