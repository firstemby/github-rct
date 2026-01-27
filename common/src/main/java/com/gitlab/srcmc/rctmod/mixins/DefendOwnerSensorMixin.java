package com.gitlab.srcmc.rctmod.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.cobblemon.mod.common.CobblemonMemories;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.entity.pokemon.ai.sensors.DefendOwnerSensor;
import com.cobblemon.mod.common.util.BrainExtensionsKt;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.monster.Enemy;

@Mixin(DefendOwnerSensor.class)
public abstract class DefendOwnerSensorMixin extends Sensor<PokemonEntity> {
    /**
     * Fixes pokemon 'defending' against arbitrary entities targeting players
     * as this may not always indicate hostile intentions (e.g. Trainers)
     * 
     * The check for hostile mobs now additionally requires the mob to either
     * implement the {@link Enemy} interface or otherwise be recently hit by
     * the owner (see {@link isHostile(LivingEntity, LivingEntity)}).
     * 
     * Issue supposedly exists since Cobblemon 1.7.1.
     */
    @Inject(method = "setNearestAttacker", at = @At("HEAD"), cancellable = true, remap = true)
    private void inject$setNearestAttacker(PokemonEntity entity, NearestVisibleLivingEntities visibleMobs, LivingEntity owner, CallbackInfo ci) {
        var nearestAttacker = visibleMobs.findClosest(livingEntity -> {
            // Prevents Pokémons from attacking players as owned Pokémons are currently invulnerable
            if(livingEntity instanceof ServerPlayer) return false;

            if(livingEntity.getLastHurtByMob() == owner) return true;
            if(isHostile(livingEntity, owner)) return true; // instead of 'if((livingEntity instanceof Mob mob) && mob.getTarget() == owner) return true;'
            
            var mobAttackTarget = BrainExtensionsKt.getMemorySafely(livingEntity.getBrain(), MemoryModuleType.ATTACK_TARGET);
            if(mobAttackTarget.isPresent() && mobAttackTarget.get() == owner) return true;

            return false;
        }).map(mob -> (LivingEntity)mob).orElse(null);

        entity.getBrain().setMemory(CobblemonMemories.NEAREST_VISIBLE_ATTACKER, nearestAttacker);
        ci.cancel();
    }

    private static boolean isHostile(LivingEntity living, LivingEntity towards) {
        return (living instanceof Enemy || towards.getLastHurtByMob() == living) && (living instanceof Mob mob) && mob.getTarget() == towards;
    }
}
