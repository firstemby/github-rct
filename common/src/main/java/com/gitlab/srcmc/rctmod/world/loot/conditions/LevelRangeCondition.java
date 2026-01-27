package com.gitlab.srcmc.rctmod.world.loot.conditions;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import com.gitlab.srcmc.rctmod.ModRegistries;
import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.gitlab.srcmc.rctmod.world.entities.TrainerMob;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

public record LevelRangeCondition(IntRange range) implements LootItemCondition {
    private static Supplier<LootItemConditionType> TYPE = ModRegistries.LootItemConditions.LEVEL_RANGE;

    public static final MapCodec<LevelRangeCondition> CODEC = RecordCodecBuilder.mapCodec(
        instance -> instance.group(
            IntRange.CODEC.fieldOf("range").forGetter(LevelRangeCondition::range)
        ).apply(instance, LevelRangeCondition::new)
    );

    public LootItemConditionType getType() {
        return TYPE.get();
    }

    public Set<LootContextParam<?>> getReferencedContextParams() {
        return Collections.unmodifiableSet(new HashSet<>(this.range.getReferencedContextParams()));
    }

    public boolean test(LootContext lootContext) {
        if(lootContext.getParamOrNull(LootContextParams.THIS_ENTITY) instanceof TrainerMob mob) {
            var teamLevel = RCTMod.getInstance().getTrainerManager()
                .getData(mob).getTrainerTeam().getTeam().stream()
                .map(p -> p.getLevel()).max(Integer::compare).orElse(0);

            return this.range.test(lootContext, teamLevel);
        }

        return false;
    }
}
