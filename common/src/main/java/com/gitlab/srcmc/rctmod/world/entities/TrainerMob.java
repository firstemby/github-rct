/*
 * This file is part of Radical Cobblemon Trainers.
 * Copyright (c) 2025, HDainester, All rights reserved.
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
package com.gitlab.srcmc.rctmod.world.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import com.gitlab.srcmc.rctapi.api.trainer.TrainerNPC;
import com.gitlab.srcmc.rctapi.api.util.Text;
import com.gitlab.srcmc.rctmod.ModCommon;
import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.gitlab.srcmc.rctmod.api.data.TrainerBattle;
import com.gitlab.srcmc.rctmod.api.data.pack.SeriesMetaData;
import com.gitlab.srcmc.rctmod.api.data.sync.PlayerState;
import com.gitlab.srcmc.rctmod.api.utils.ChatUtils;
import com.gitlab.srcmc.rctmod.api.utils.LangKeys;
import com.gitlab.srcmc.rctmod.world.blocks.entities.TrainerSpawnerBlockEntity;
import com.gitlab.srcmc.rctmod.world.entities.goals.ForceIntoBattleGoal;
import com.gitlab.srcmc.rctmod.world.entities.goals.ForcedStrollAwayGoal;
import com.gitlab.srcmc.rctmod.world.entities.goals.LookAtPlayerAndWaitGoal;
import com.gitlab.srcmc.rctmod.world.entities.goals.MoveCloseToTargetGoal;
import com.gitlab.srcmc.rctmod.world.entities.goals.MoveToHomePosGoal;
import com.gitlab.srcmc.rctmod.world.entities.goals.PokemonBattleGoal;
import com.gitlab.srcmc.rctmod.world.entities.goals.RandomStrollAwayGoal;
import com.gitlab.srcmc.rctmod.world.entities.goals.RandomStrollThroughVillageGoal;
import com.gitlab.srcmc.rctmod.world.entities.goals.RandomWaitGoal;
import com.gitlab.srcmc.rctmod.world.entities.goals.TrainerCardTemptGoal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Evoker;
import net.minecraft.world.entity.monster.Illusioner;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.entity.monster.Zoglin;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.Npc;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class TrainerMob extends PathfinderMob implements Npc {
    private static final EntityDataAccessor<String> DATA_TRAINER_ID = SynchedEntityData.defineId(TrainerMob.class, EntityDataSerializers.STRING);
    private static final EntityType<TrainerMob> TYPE = EntityType.Builder
        .of(TrainerMob::new, MobCategory.MISC)
        .canSpawnFarFromPlayer()
        .sized(0.6F, 1.95F).build("trainer");

    static final int TICKS_TO_DESPAWN = 600;
    static final int TICKS_TO_DETACH = 600;
    static final int DESPAWN_TICK_SCALE = 20;
    static final int DESPAWN_DISTANCE = 128;
    static final int MAX_PLAYER_TRACKING_RANGE = 128;
    static final int TARGET_UPDATE_INTERVAL = 120;
    static final int BASE_BATTLE_COOLDOWN_TICKS = 400;

    static final int AFK_CHECK_INTERVAL_TICKS = 2400;
    static final int AFK_CHECK_MAX_COUNT = 6;
    static final int AFK_PLAYER_MAX_COUNT = 3;

    static final int UNSEEN_CHECK_INTERVAL_TICKS = 40;
    static final int LINE_OF_SIGHT_CHECK_DISTANCE = 128;

    private Map<UUID, int[]> winsAndDefeats = new HashMap<>();
    private int cooldown;
    private int extraCooldown;
    private int detachTicks;
    private Player opponent;
    private UUID originPlayer;
    private boolean persistent;
    private int despawnTicks;
    private boolean done;
    private BlockPos homePos;
    private TrainerSpawnerBlockEntity homeSpawner;
    private List<PlayerTransform> nearestTransforms = new ArrayList<>();
    private int nearestAfkCheckCount;
    private int lastTimeSeen;

    protected TrainerMob(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.setCustomName(Component.literal("Trainer"));
    }

    public static EntityType<TrainerMob> getEntityType() {
        return TYPE;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
            .add(Attributes.MOVEMENT_SPEED, 0.5)
            .add(Attributes.FOLLOW_RANGE, 48.0)
            .add(Attributes.MAX_HEALTH, 30);
    }

    public boolean canBattleAgainst(Entity e) {
        if(e instanceof Player player) {
            var rct = RCTMod.getInstance();
            var tm = rct.getTrainerManager();
            var tpd = tm.getData(player);
            var tmd = tm.getData(this);
            var bm = tm.getBattleMemory(this);

            return this.getCooldown() == 0
                && !this.isInBattle()
                && !rct.isInBattle(player)
                && tm.getActivePokemon(player) > 0
                // && tpd.getLevelCap() >= tmd.getRequiredLevelCap(player) // deprecated
                && tm.getPlayerLevel(player) <= tpd.getLevelCap()
                && tmd.getMissingRequirements(tm.getData(player).getDefeatedTrainerIds()).filter(tid -> tm.getData(tid).isOfSeries(tpd.getCurrentSeries())).findFirst().isEmpty()
                && (tmd.isOfSeries(tpd.getCurrentSeries()) || bm.getDefeatByCount(this.getTrainerId(), player) > 0)
                && this.couldBattleAgainst(e);
        }

        return false;
    }

    public boolean couldBattleAgainst(Entity e) {
        if(e instanceof Player player) {
            return this.isPersistenceRequired() || (!this.wasDefeatedBy(player.getUUID()) && !this.wasVictoriousAgainst(player.getUUID()));
        }

        return false;
    }

    public void startBattleWith(Player player) {
        var realPlayer = player.level().getPlayerByUUID(player.getUUID());

        if(player.equals(realPlayer)) {
            if(this.canBattleAgainst(player)) {
                if(RCTMod.getInstance().makeBattle(this, player)) {
                    this.setOpponent(player);
                    RCTMod.getInstance().getTrainerManager().addBattle(player, this);
                    ChatUtils.reply(this, player, "on_battle_start");
                }
            } else {
                this.replyTo(player);
            }
        } else {
            ModCommon.LOG.warn(String.format("attempt to start battle against '%s' with proxy '%s' for '%s'", this.getDisplayName().getString(), player.getDisplayName().getString(), realPlayer != null ? realPlayer.getDisplayName().getString() : player.getStringUUID()));
        }
    }

    public int getForceBattleLookTicks() {
        var tmd = RCTMod.getInstance().getTrainerManager().getData(this);

        return tmd.getForceBattleLookTicks().has()
            ? tmd.getForceBattleLookTicks().get()
            : RCTMod.getInstance().getServerConfig().forceBattleLookTicks();
    }

    public float getForceBattleChance() {
        var tmd = RCTMod.getInstance().getTrainerManager().getData(this);
        
        return tmd.getForceBattleOnSight().has()
            ? (tmd.getForceBattleOnSight().get() ? 1f : 0f)
            : (RCTMod.getInstance().getServerConfig().forceBattleOnSight() ? 1f : 0f);
    }

    public float getForceBattleMaxDistance() {
        var tmd = RCTMod.getInstance().getTrainerManager().getData(this);

        return tmd.getForceBattleMaxDistance().has()
            ? tmd.getForceBattleMaxDistance().get()
            : RCTMod.getInstance().getServerConfig().forceBattleMaxDistance();
    }

    public int getForceBattleMaxLevelDiff() {
        var tmd = RCTMod.getInstance().getTrainerManager().getData(this);

        return tmd.getForceBattleMaxLevelDiff().has()
            ? tmd.getForceBattleMaxLevelDiff().get()
            : RCTMod.getInstance().getServerConfig().forceBattleMaxLevelDiff();
    }

    protected void replyTo(Player player) {
        var tm = RCTMod.getInstance().getTrainerManager();
        var tmd = tm.getData(this);
        var tpd = tm.getData(player);
        var msr = tmd.getMissingRequirements(tpd.getDefeatedTrainerIds()).filter(tid -> tm.getData(tid).isOfSeries(tpd.getCurrentSeries())).findFirst();
        var bm = tm.getBattleMemory(this);

        if(this.isInBattle()) {
            ChatUtils.reply(this, player, "trainer_busy");
        } else if(!this.isPersistenceRequired() && this.wasDefeatedBy(player.getUUID())) {
            ChatUtils.reply(this, player, "trainer_lost");
        } else if(!this.isPersistenceRequired() && this.wasVictoriousAgainst(player.getUUID())) {
            ChatUtils.reply(this, player, "trainer_won");
        } else if(this.getCooldown() > 0) {
            ChatUtils.reply(this, player, "on_cooldown");
        } else if(RCTMod.getInstance().isInBattle(player)) {
            ChatUtils.reply(this, player, "player_busy");
        } else if((!tmd.isOfSeries(tpd.getCurrentSeries()) && bm.getDefeatByCount(this.getTrainerId(), player) == 0)) {
            ChatUtils.reply(this, player, "wrong_series");
        } else if(msr.isPresent()) {
            ChatUtils.reply(this, player, "missing_required_trainer_"  + tm.getData(msr.get()).getType().id(), "missing_required_trainer");
        // } else if(tpd.getLevelCap() < tmd.getRequiredLevelCap(player)) { // deprecated
        //     ChatUtils.reply(this, player, "low_level_cap");
        } else if(tm.getPlayerLevel(player) > tpd.getLevelCap()) {
            ChatUtils.reply(this, player, "over_level_cap");
        } else if(tm.getActivePokemon(player) == 0) {
            ChatUtils.reply(this, player, "missing_pokemon");
        } else {
            ChatUtils.reply(this, player, "unknown_reason");
        }
    }

    protected void setOpponent(Player player) {
        this.opponent = player;
    }

    public Player getOpponent() {
        return this.opponent;
    }

    public boolean isInBattle() {
        return this.opponent != null;
    }

    private void addWins(Player player, int wins) {
        this.winsAndDefeats.compute(player.getUUID(), (k, v) -> {
            if(v == null) {
                return new int[]{wins, 0};
            }

            v[0] += wins;
            return v;
        });
    }

    private void addDefeats(Player player, int defeats) {
        this.winsAndDefeats.compute(player.getUUID(), (k, v) -> {
            if(v == null) {
                return new int[]{0, defeats};
            }

            v[1] += defeats;
            return v;
        });
    }

    public boolean wasDefeatedBy(UUID opponentUUID) {
        var wd = this.winsAndDefeats.get(opponentUUID);
        var md =  RCTMod.getInstance().getTrainerManager().getData(this).getMaxTrainerDefeats();
        return md >= 0 && wd != null && wd[1] >= md;
    }

    public boolean wasVictoriousAgainst(UUID opponentUUID) {
        var wd = this.winsAndDefeats.get(opponentUUID);
        var mw = RCTMod.getInstance().getTrainerManager().getData(this).getMaxTrainerWins();
        return mw >= 0 && wd != null && wd[0] >= mw;
    }

    public boolean wasDefeated() {
        var maxDefeats = RCTMod.getInstance().getTrainerManager().getData(this).getMaxTrainerDefeats();

        if(maxDefeats > 0) {
            for(var wd : this.winsAndDefeats.values()) {
                if(wd[1] >= maxDefeats) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean wasVictorious() {
        var maxWins = RCTMod.getInstance().getTrainerManager().getData(this).getMaxTrainerWins();

        if(maxWins > 0) {
            for(var wd : this.winsAndDefeats.values()) {
                if(wd[0] >= maxWins) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean wasExhausted() {
        return !this.isPersistenceRequired() && (this.wasDefeated() || this.wasVictorious());
    }

    private void udpateCustomName() {
        var tmd = RCTMod.getInstance().getTrainerManager().getData(this);
        this.setCustomName(tmd.getTrainerTeam().getName().getComponent());
    }

    @Override
    public Component getDisplayName() {
        var tmd = RCTMod.getInstance().getTrainerManager().getData(this);
        var suffix = new StringBuilder();
        var cmp = this.getCustomName().copy();
        var level = this.level();
        var showSymbols = false;
        var showColors = true;
        var showItalic = false;

        if(level.isClientSide) {
            var cfg = RCTMod.getInstance().getClientConfig();
            var ps = PlayerState.get(ModCommon.localPlayer());
            showSymbols = cfg.showTrainerTypeSymbols();
            showColors = cfg.showTrainerTypeColors();

            // TODO: PlayerState only keeps track of defeated trainers from the current series
            // of a player, hence it would be necessary to fetch the defeat counts of trainers
            // from different series (which would introduce some significant overhead).
            showItalic = tmd.isOfSeries(ps.getCurrentSeries()) && ps.getTrainerDefeatCount(this.getTrainerId()) == 0;
        }

        if(showSymbols) {
            var sym = tmd.getType().symbol();
            
            if(sym.length() > 0) {
                suffix.append(' ').append(sym);
            }
        }

        if(showColors) {
            cmp.setStyle(cmp.getStyle().withColor(tmd.getType().color()));
        }

        if(showItalic) {
            cmp.setStyle(cmp.getStyle().withItalic(true));
        }

        return cmp.append(suffix.toString());
    }

    public void setTrainerId(String trainerId) {
        var level = this.level();

        if(!level.isClientSide) {
            var currentId = this.getTrainerId();

            if(!Objects.equals(currentId, trainerId)) {
                RCTMod.getInstance().getTrainerSpawner().notifyChangeTrainerId(this, trainerId);
                this.entityData.set(DATA_TRAINER_ID, trainerId);
                RCTMod.getInstance().getTrainerManager().runAfterLoaded(() -> this.updateTrainerNPC(trainerId));
            }
        }
    }

    private void updateTrainerNPC(String trainerId) {
        try {
            var trainer = ModCommon.RCT.getTrainerRegistry().getById(trainerId, TrainerNPC.class);

            if(trainer != null) {
                trainer.setEntity(this);
                this.udpateCustomName();
            } else {
                var text = Text.translatable(LangKeys.COMMANDS_ERRORS_INVALID_TRAINER_ID);
                ModCommon.LOG.warn(text.getComponent(trainerId, this.getStringUUID(), this.blockPosition().toShortString(), this.level().dimension().location().toString()).getString());
                ChatUtils.broadcastError(this.getServer(), text, 2, trainerId, this.getStringUUID(), this.blockPosition().toShortString(), this.level().dimension().location().toString());
            }
        } catch(IllegalArgumentException e) {
            ModCommon.LOG.error(String.format("Invalid trainer id '%s' (%s)", trainerId, this.getStringUUID()), e);
        }
    }

    public String getTrainerId() {
        return this.entityData.get(DATA_TRAINER_ID);
    }

    public void setHomePos(BlockPos blockPos) {
        var level = this.level();

        if(!level.isClientSide) {
            var currentHome = this.getHomePos();

            if(!Objects.equals(currentHome, blockPos)) {
                this.homePos = blockPos;
            }
        }
    }

    public void setHomeSpawner(TrainerSpawnerBlockEntity be) {
        this.homeSpawner = be;
        this.setHomePos(be != null ? be.getBlockPos().above() : null);
    }

    public BlockPos getHomePos() {
        return this.homePos;
    }

    public TrainerSpawnerBlockEntity getHomeSpawner() {
        return this.homeSpawner;
    }

    public void finishBattle(TrainerBattle battle, boolean defeated) {
        var level = this.level();

        if(!level.isClientSide && this.isInBattle()) {
            var mobTr = RCTMod.getInstance().getTrainerManager().getData(this);
            var opponent = this.getOpponent();

            this.cooldown = mobTr.getBattleCooldownTicks() + this.extraCooldown;
            this.extraCooldown += this.cooldown;
            this.setOpponent(null);
            this.setTarget(null);

            if(defeated) {
                if(battle.getInitiatorSideMobs().contains(this)) {
                    battle.getTrainerSidePlayers().forEach(player -> {
                        ChatUtils.reply(this, player, "on_battle_lost");
                    });
                } else {
                    var tm = RCTMod.getInstance().getTrainerManager();
                    var initiatorSidePlayer = battle.getInitiatorSidePlayers();
                    Player minWinsPlayer = null;

                    for(var player : initiatorSidePlayer) {
                        ChatUtils.reply(this, player, "on_battle_lost");
                        minWinsPlayer = minWinsPlayer == null || tm.getBattleMemory(this).getDefeatByCount(this.getTrainerId(), player) < tm.getBattleMemory(this).getDefeatByCount(this.getTrainerId(), minWinsPlayer) ? player : minWinsPlayer;
                    }

                    if(minWinsPlayer != null) {
                        this.dropBattleLoot(mobTr.getLootTableResource(), minWinsPlayer);
                    }
                }

                if(opponent != null) {
                    this.addDefeats(opponent, 1);
                }
            } else {
                if(battle.getInitiatorSideMobs().contains(this)) {
                    battle.getTrainerSidePlayers().forEach(player -> {
                        ChatUtils.reply(this, player, "on_battle_won");
                    });
                } else {
                    battle.getInitiatorSidePlayers().forEach(player -> {
                        ChatUtils.reply(this, player, "on_battle_won");
                    });
                }

                this.addWins(opponent, 1);
            }

            if(!this.done && (this.wasDefeatedBy(opponent.getUUID()) || this.wasVictoriousAgainst(opponent.getUUID()))) {
                this.detachTicks = TICKS_TO_DETACH;
                this.done = true;
            }
        }
    }

    public void cancelBattle() {
        if(this.opponent != null) {
            RCTMod.getInstance().getTrainerManager().removeBattle(this.opponent.getUUID());
            RCTMod.getInstance().stopBattle(this);
            this.setOpponent(null);
        }
    }

    protected void dropBattleLoot(ResourceLocation lootTableResource, Player player) {
        var level = this.level();
        var lootTable = level.getServer().reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE, lootTableResource));
        var builder = (new LootParams.Builder((ServerLevel)level))
            .withParameter(LootContextParams.THIS_ENTITY, this)
            .withParameter(LootContextParams.ORIGIN, this.position())
            .withParameter(LootContextParams.DAMAGE_SOURCE, level.damageSources().generic())
            .withParameter(LootContextParams.LAST_DAMAGE_PLAYER, player)
            .withLuck(RCTMod.getInstance()
                .getTrainerManager().getData(player)
                .getBonusLuck((int)(player.getLuck() * SeriesMetaData.MAX_DIFFICULTY))); // vanilla player luck stat will also boost trainer loot

        lootTable.getRandomItems(
            builder.create(LootContextParamSets.ENTITY),
            this.getLootTableSeed(), this::spawnAtLocation);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_TRAINER_ID, "");
    }

    @Override
    public void tick() {
        super.tick();
        var level = this.level();

        if(!level.isClientSide) {
            if(this.cooldown > 0) {
                this.cooldown--;
            } else if(this.extraCooldown > 0) {
                if(!this.isInBattle() || (this.tickCount % 3) == 0) {
                    this.extraCooldown--;
                }
            }

            if(this.done && this.originPlayer != null) {
                if(this.detachTicks > 0) {
                    this.detachTicks--;
                } else {
                    this.setOriginPlayer(null);
                }
            }

            if(this.isInBattle()) {
                var opponent = this.getOpponent();

                if(opponent != null && !opponent.isAlive()) {
                    this.addWins(opponent, 1);
                    this.cancelBattle();
                }
            } else {
                this.updateTarget();
            }

            if(!this.isRemoved()) {
                RCTMod.getInstance().getTrainerSpawner().register(this);
            }

            if(!this.isRemoved()) {
                this.checkDespawnIfUnseen();
            }

            if(!this.isRemoved()) {
                this.checkDespawnNearAfkPlayers();
            }
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
        var level = this.level();

        if(!level.isClientSide) {
            var tm = RCTMod.getInstance().getTrainerManager();

            if(!tm.isReloadRequired() && !tm.isLoading() && PlayerState.get(player) != null) {
                this.startBattleWith(player);
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void remove(RemovalReason reason) {
        if(RCTMod.getInstance().getServerConfig().logSpawning()) {
            ModCommon.LOG.info(String.format("Removed trainer '%s' (%s): %s", this.getTrainerId(), this.getStringUUID(), reason.toString()));
        }
        
        RCTMod.getInstance().getTrainerSpawner().unregister(this);
        this.cancelBattle();
        super.remove(reason);
    }

    private void updateTarget() {
        if(this.tickCount % TARGET_UPDATE_INTERVAL == 0) {
            var tm = RCTMod.getInstance().getTrainerManager();
            var tmd = tm.getData(this);

            this.setTarget(this.level()
                .getNearbyPlayers(
                    TargetingConditions
                        .forNonCombat()
                        .ignoreLineOfSight()
                        .selector(p -> PlayerState.get((Player)p).getLevelCap() >= tmd.getRequiredLevelCap((Player)p)
                            && !this.wasDefeatedBy(p.getUUID())
                            && !this.wasVictoriousAgainst(p.getUUID())),
                    this, this.getBoundingBox().inflate(MAX_PLAYER_TRACKING_RANGE))
                .stream()
                    .min((p1, p2) -> Integer.compare(Math.abs(tm.getPlayerLevel(p1) - tmd.getRequiredLevelCap((Player)p1)), Math.abs(tm.getPlayerLevel(p2) - tmd.getRequiredLevelCap((Player)p2))))
                    .orElse(null));
        }
    }

    public void setPersistent(boolean persistent) {
        this.setPersistent(persistent, false);
    }

    public void setPersistent(boolean persistent, boolean silent) {
        if(this.persistent != persistent) {
            if(!silent) {
                RCTMod.getInstance().getTrainerSpawner().notifyChangePersistence(this, persistent);
            }

            this.persistent = persistent;
        }
    }

    @Override
    public boolean isPersistenceRequired() {
        return this.persistent;
    }

    @Override
    public boolean shouldBeSaved() {
        return this.isPersistenceRequired();
    }

    @Override
    public boolean isAlwaysTicking() {
        return true;
    }

    @Override
    public boolean removeWhenFarAway(double d) {
        return false;
    }

    @Override
    public boolean saveAsPassenger(CompoundTag tag) {
        return this.isPersistenceRequired() && super.saveAsPassenger(tag);
    }

    @Override
    public boolean canChangeDimensions(Level level1, Level level2) {
        return false;
    }

    public boolean shouldDespawn() {
        if(++this.despawnTicks % DESPAWN_TICK_SCALE == 0) {
            if(!this.isInBattle() && this.level().getNearestPlayer(this, Math.max(DESPAWN_DISTANCE, RCTMod.getInstance().getServerConfig().maxHorizontalDistanceToPlayers())) == null) {
                return this.despawnTicks >= TICKS_TO_DESPAWN;
            }

            this.despawnTicks = 0;
        }

        return false;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.WANDERING_TRADER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WANDERING_TRADER_DEATH;
    }

    public int getCooldown() {
        return this.cooldown;
    }

    public void setOriginPlayer(UUID originPlayer) {
        if((this.originPlayer == null && originPlayer != null) || (this.originPlayer != null && !this.originPlayer.equals(originPlayer))) {
            RCTMod.getInstance().getTrainerSpawner().notifyChangeOriginPlayer(this, originPlayer);
            this.originPlayer = originPlayer;
        }
    }

    public UUID getOriginPlayer() {
        return this.originPlayer;
    }
    
    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putInt("Cooldown", this.getCooldown());
        compoundTag.putString("TrainerId", this.getTrainerId());
        compoundTag.putBoolean("Persistent", this.isPersistenceRequired());
        compoundTag.putBoolean("InBattle", this.isInBattle());

        if(this.homePos != null) {
            compoundTag.putLong("HomePos", this.homePos.asLong());
        }

        if(this.originPlayer != null) {
            compoundTag.putUUID("OriginPlayer", this.originPlayer);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);

        if(compoundTag.contains("Cooldown")) {
            this.cooldown = compoundTag.getInt("Cooldown");
        }

        if(compoundTag.contains("TrainerId")) {
            this.setTrainerId(compoundTag.getString("TrainerId"));
        }

        if(compoundTag.contains("HomePos")) {
            this.setHomePos(BlockPos.of(compoundTag.getLong("HomePos")));
        }

        if(compoundTag.contains("OriginPlayer")) {
            this.setOriginPlayer(compoundTag.getUUID("OriginPlayer"));
        }

        if(compoundTag.contains("Persistent")) {
            this.setPersistent(compoundTag.getBoolean("Persistent"));
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        var level = this.level();

        if(!level.isClientSide && this.isAlive()) {
            if(this.random.nextInt(400) == 0 && this.deathTime == 0) {
               this.heal(1.0F);
            }
        }
    }

    @Override
    protected void registerGoals() {
        var maxTrackingDistance = 2*RCTMod.getInstance().getServerConfig().maxHorizontalDistanceToPlayers();
        var i = 0;

        this.getNavigation().setCanFloat(true);
        this.goalSelector.addGoal(i, new FloatGoal(this));
        this.goalSelector.addGoal(i++, new PokemonBattleGoal(this));
        this.goalSelector.addGoal(i, new AvoidEntityGoal<Zombie>(this, Zombie.class, 8.0F, 0.5, 0.5));
        this.goalSelector.addGoal(i, new AvoidEntityGoal<Evoker>(this, Evoker.class, 12.0F, 0.5, 0.5));
        this.goalSelector.addGoal(i, new AvoidEntityGoal<Vindicator>(this, Vindicator.class, 8.0F, 0.5, 0.5));
        this.goalSelector.addGoal(i, new AvoidEntityGoal<Vex>(this, Vex.class, 8.0F, 0.5, 0.5));
        this.goalSelector.addGoal(i, new AvoidEntityGoal<Pillager>(this, Pillager.class, 15.0F, 0.5, 0.5));
        this.goalSelector.addGoal(i, new AvoidEntityGoal<Illusioner>(this, Illusioner.class, 12.0F, 0.5, 0.5));
        this.goalSelector.addGoal(i, new AvoidEntityGoal<Zoglin>(this, Zoglin.class, 10.0F, 0.5, 0.5));
        this.goalSelector.addGoal(i++, new PanicGoal(this, 0.5));
        this.goalSelector.addGoal(i++, new TrainerCardTemptGoal(this));
        this.goalSelector.addGoal(i++, new ForceIntoBattleGoal(this, Player.class));
        this.goalSelector.addGoal(i, new LookAtPlayerAndWaitGoal(this, LivingEntity.class, 2.0F, 0.04F, 160, 600));
        this.goalSelector.addGoal(i, new LookAtPlayerAndWaitGoal(this, LivingEntity.class, 4.0F, 0.004F, 80, 300));
        this.goalSelector.addGoal(i++, new LookAtPlayerGoal(this, LivingEntity.class, 8.0F));
        this.goalSelector.addGoal(i++, new MoveToHomePosGoal(this));
        this.goalSelector.addGoal(i++, new ForcedStrollAwayGoal(this, 0.35));
        this.goalSelector.addGoal(i++, new RandomWaitGoal(this));
        this.goalSelector.addGoal(i++, new MoveCloseToTargetGoal(this, 0.35, maxTrackingDistance));
        this.goalSelector.addGoal(i++, new RandomStrollThroughVillageGoal(this, 0.35f, p -> this.wasExhausted() ? p * 0.25f : p * 0.75f));
        this.goalSelector.addGoal(i++, new RandomStrollAwayGoal(this, 0.35));
        this.goalSelector.addGoal(i++, new WaterAvoidingRandomStrollGoal(this, 0.35));
    }

    public boolean isRequiredBy(LivingEntity entity) {
        if(entity instanceof Player player) {
            var tm = RCTMod.getInstance().getTrainerManager();
            var tmd = tm.getData(this);
            var tpd = tm.getData(player);

            return !tpd.getDefeatedTrainerIds().contains(this.getTrainerId())
                && tmd.getMissingRequirements(tpd.getDefeatedTrainerIds()).filter(tid -> tm.getData(tid).isOfSeries(tpd.getCurrentSeries())).findFirst().isEmpty();
        }

        return false;
    }

    private void checkDespawnNearAfkPlayers() {
        if(!this.isPersistenceRequired() && this.tickCount % AFK_CHECK_INTERVAL_TICKS == 0) {
            if(this.nearestAfkCheckCount >= AFK_CHECK_MAX_COUNT) {
                this.discard();
                return;
            }

            var level = this.level();
            List<PlayerTransform> nextTransforms = level.getNearbyPlayers(TargetingConditions.forNonCombat(), this, this.getHitbox().inflate(RCTMod.getInstance().getServerConfig().maxHorizontalDistanceToPlayers()))
                .stream().limit(AFK_PLAYER_MAX_COUNT)
                .sorted((p1, p2) -> Integer.compare(p1.getId(), p2.getId()))
                .map(PlayerTransform::new).toList();

            if(this.nearestTransforms.size() > 0 && nextTransforms.size() == this.nearestTransforms.size() && nextTransforms.containsAll(this.nearestTransforms)) {
                this.nearestAfkCheckCount++;
            } else {
                this.nearestTransforms = nextTransforms;
                this.nearestAfkCheckCount = 0;
            }
        }
    }

    // TODO: This is kinda a proof of concept (might miss players having this trainer in line-of-sight
    // -> the greater UNSEEN_CHECK_INTERVAL_TICKS the higher the chance to miss).
    private void checkDespawnIfUnseen() {
        var maxUnseenTicks = RCTMod.getInstance().getServerConfig().despawnTicksIfUnseen();

        if(maxUnseenTicks > 0 && !this.isPersistenceRequired() && this.tickCount % UNSEEN_CHECK_INTERVAL_TICKS == 0)  {
            if(this.level().players().stream().anyMatch(p -> this.hasLineOfSight(p))) {
                this.lastTimeSeen = this.tickCount;
            } else if((this.tickCount - this.lastTimeSeen) > maxUnseenTicks) {
                this.discard();
            }
        }
    }

    // Slightly optimized (no sqrt).
    @Override
    public boolean hasLineOfSight(Entity entity) {
        if(entity.level() != this.level()) {
            return false;
        }

        var vec3 = new Vec3(this.getX(), this.getEyeY(), this.getZ());
        var vec32 = new Vec3(entity.getX(), entity.getEyeY(), entity.getZ());

        if(vec32.distanceToSqr(vec3) > 16384) { // 128 * 128
            return false;
        }

        return this.level().clip(new ClipContext(vec3, vec32, Block.COLLIDER, Fluid.NONE, this)).getType() == HitResult.Type.MISS;
    }

    private class PlayerTransform {
        public final float xRot, yRot;
        public final double x, y, z;

        public PlayerTransform(Player player) {
            if(player != null) {
                this.x = player.position().x;
                this.y = player.position().y;
                this.z = player.position().z;
                this.xRot = player.getXRot();
                this.yRot = player.getYRot();
            } else {
                this.x = 0;
                this.y = 0;
                this.z = 0;
                this.xRot = 0;
                this.yRot = 0;
            }
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.x, this.y, this.z, this.xRot, this.yRot);
        }

        @Override
        public boolean equals(Object o) {
            if(o instanceof PlayerTransform t) {
                return this.x == t.x && this.y == t.y && this.z == t.z && this.xRot == t.xRot && this.yRot == t.yRot;
            }

            return false;
        }
    }
}
