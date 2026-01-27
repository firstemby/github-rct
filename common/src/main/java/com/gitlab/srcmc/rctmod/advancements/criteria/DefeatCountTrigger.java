package com.gitlab.srcmc.rctmod.advancements.criteria;

import com.gitlab.srcmc.rctmod.world.entities.TrainerMob;
import com.mojang.serialization.Codec;

import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;

public class DefeatCountTrigger extends SimpleCriterionTrigger<DefeatCountTriggerInstance> {
    public void trigger(ServerPlayer player, TrainerMob mob) {
        this.trigger(player, triggerInstance -> triggerInstance.matches(player, mob));
    }

    @Override
    public Codec<DefeatCountTriggerInstance> codec() {
        return DefeatCountTriggerInstance.CODEC;
    }
}
