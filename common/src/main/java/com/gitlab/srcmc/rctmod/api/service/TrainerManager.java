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
package com.gitlab.srcmc.rctmod.api.service;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Stream;

import com.cobblemon.mod.common.Cobblemon;
import com.gitlab.srcmc.rctapi.api.errors.RCTException;
import com.gitlab.srcmc.rctapi.api.trainer.Trainer;
import com.gitlab.srcmc.rctapi.api.trainer.TrainerNPC;
import com.gitlab.srcmc.rctmod.ModCommon;
import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.gitlab.srcmc.rctmod.api.data.TrainerBattle;
import com.gitlab.srcmc.rctmod.api.data.pack.DataPackManager;
import com.gitlab.srcmc.rctmod.api.data.pack.TrainerMobData;
import com.gitlab.srcmc.rctmod.api.data.pack.TrainerType;
import com.gitlab.srcmc.rctmod.api.data.save.TrainerBattleMemory;
import com.gitlab.srcmc.rctmod.api.data.save.TrainerPlayerData;
import com.gitlab.srcmc.rctmod.api.utils.PathUtils;
import com.gitlab.srcmc.rctmod.commands.utils.SuggestionUtils;
import com.gitlab.srcmc.rctmod.network.BatchedPayload;
import com.gitlab.srcmc.rctmod.network.BatchedPayloads;
import com.gitlab.srcmc.rctmod.server.ModServer;
import com.gitlab.srcmc.rctmod.world.entities.TrainerMob;
import com.google.gson.JsonElement;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.SavedData.Factory;

public class TrainerManager extends DataPackManager {
    private Map<String, TrainerMobData> trainerMobs = new HashMap<>();
    private SeriesManager seriesManager;

    private Queue<Thread> payloadUpdateThreads = new ConcurrentLinkedDeque<>();
    private Map<UUID, String> uuidToTrainerId = new HashMap<>();
    private Set<String> playerTrainerIds = new HashSet<>();
    private Set<Player> receivedUpdates = new HashSet<>();
    private Map<UUID, TrainerBattle> trainerBattles = new HashMap<>();
    private Queue<Runnable> onLoadedHandlers = new LinkedList<>();
    private MinecraftServer server;
    private ResourceManager resourceManager;
    private boolean loading, isReloadedAsDatapack;
    private boolean reloadRequired;

    record Payload(Map<String, TrainerMobData> trainerMobs, SeriesManager seriesManager) implements Serializable {
        private static final long serialVersionUID = 0;
    }

    public TrainerManager() {
        super(PackType.SERVER_DATA);
        this.seriesManager = new SeriesManager();
    }

    public void runAfterLoaded(Runnable f) {
        if(!this.isReloadRequired() || !this.isLoading()) {
            f.run();
        } else {
            this.onLoadedHandlers.offer(f);
        }
    }

    public void setLoading(boolean value) {
        this.loading = !this.isReloadedAsDatapack && value;

        if(this.server != null && !this.loading && !this.reloadRequired) {
            this.server.execute(() -> {
                while(!this.onLoadedHandlers.isEmpty()) {
                    this.onLoadedHandlers.poll().run();
                }
            });
        }
    }

    public void setIsReloadedAsDatapack(boolean value) {
        this.isReloadedAsDatapack = value;
    }

    public boolean isLoading() {
        return this.loading;
    }

    public void fromPayloads(BatchedPayload.Payload[] pls) {
        var t = new Thread(() -> {
            TrainerType.clear();
            Payload pl = BatchedPayload.<Payload>from(pls);
            this.trainerMobs = pl.trainerMobs();
            this.seriesManager.copyFrom(pl.seriesManager());
            this.payloadUpdateThreads.poll(); // self

            if(!this.payloadUpdateThreads.isEmpty()) {
                this.payloadUpdateThreads.peek().start();
            } else {
                this.setLoading(false);
            }
        });

        t.setPriority(Thread.NORM_PRIORITY / 2);
        this.payloadUpdateThreads.add(t);

        if(this.payloadUpdateThreads.size() == 1) {
            t.start();
        }
    }

    public BatchedPayload.Payload[] toPayloads() {
        return BatchedPayloads.TRAINER_MANAGER.payloads(new Payload(
            this.trainerMobs,
            this.seriesManager));
    }

    public SeriesManager getSeriesManager() {
        return this.seriesManager;
    }

    public void setServer(MinecraftServer server) {
        if(server != null) {
            var bm = ModCommon.RCT.getBattleManager();
            
            bm.getStates()
                .stream().toList() // copy to avoid concurrent modification
                .forEach(bs -> bm.end(bs.getBattle().getBattleId(), true));

            ModCommon.RCT.getTrainerRegistry().init(server);
        }

        this.server = server;
    }

    public boolean isReloadRequired() {
        return this.reloadRequired && this.resourceManager != null;
    }

    public void setReloadRequired() {
        this.reloadRequired = true;
    }

    private void registerTrainer(String trainerId, TrainerMobData tmd, Map<String, Trainer> oldTrainers) {
        if(this.server != null && this.server.isSameThread()) {
            var reg = ModCommon.RCT.getTrainerRegistry();

            try {
                var newTrainer = reg.registerNPC(trainerId, tmd.getTrainerTeam());
                var oldTrainer = oldTrainers.get(trainerId);

                if(oldTrainer != null) {
                    newTrainer.setEntity(oldTrainer.getEntity());
                }
            } catch(RCTException errors) {
                ModCommon.LOG.warn("Model validation failure for '" + trainerId + "'" + errors.getErrors().stream().map(e -> e.message).reduce("", (a, b) -> a + "\n  " + b));
            } catch(Exception e) {
                // this.trainerMobs.remove(trainerId); // not that it really matters at this point
                ModCommon.LOG.error("Failed to register trainer '" + trainerId + "'", e);
            }
        }
    }

    public String registerPlayer(Player player) {
        var trainerId = this.uuidToTrainerId.get(player.getUUID());

        if(trainerId == null) {
            int attempt = 0;
            var name = player.getName().getString().replace(' ', '_');
            trainerId = name;

            while(this.playerTrainerIds.contains(trainerId)) {
                trainerId = String.format("%s_%d", name, ++attempt);
            }

            this.uuidToTrainerId.put(player.getUUID(), trainerId);
            this.playerTrainerIds.add(trainerId);
        }

        return trainerId;
    }

    public String unregisterPlayer(Player player) {
        var trainerId = this.uuidToTrainerId.remove(player.getUUID());

        if(trainerId != null) {
            this.playerTrainerIds.remove(trainerId);
        }

        return trainerId;
    }

    public String getTrainerId(Player player) {
        if(!this.uuidToTrainerId.containsKey(player.getUUID())) {
            this.registerPlayer(player);
        }

        return this.uuidToTrainerId.get(player.getUUID());
    }

    public void addBattle(Player initiator, TrainerMob opponent) {
        this.trainerBattles.put(initiator.getUUID(), new TrainerBattle(initiator, opponent));
    }

    public void addBattle(Player[] initiatorSidePlayers, TrainerMob[] initiatorSideMobs, Player[] trainerSidePlayers, TrainerMob[] trainerSideMobs) {
        var battle = new TrainerBattle(initiatorSidePlayers, initiatorSideMobs, trainerSidePlayers, trainerSideMobs);
        this.trainerBattles.put(battle.getInitiator().getUUID(), battle);
    }

    public Optional<TrainerBattle> getBattle(UUID initiatorId) {
        return this.trainerBattles.containsKey(initiatorId)
            ? Optional.of(this.trainerBattles.get(initiatorId))
            : Optional.empty();
    }

    public boolean removeBattle(UUID initiatorId) {
        return this.trainerBattles.remove(initiatorId) != null;
    }

    public TrainerMobData getData(TrainerMob mob) {
        return this.getData(mob.getTrainerId());
    }

    public TrainerMobData getData(String trainerId) {
        if(!this.trainerMobs.containsKey(trainerId)) {
            // currently disabled -> spams log -> alternative in TrainerMob
            // if(!trainerId.isEmpty()) {
            //     ModCommon.LOG.error(String.format("Invalid trainer id '%s'", trainerId));
            // }

            return new TrainerMobData();
        }

        return this.trainerMobs.get(trainerId);
    }

    public boolean isValidId(String trainerId) {
        return this.trainerMobs.containsKey(trainerId);
    }

    public int getPlayerLevel(Player player) {
        int maxLevel = 0;

        if(player instanceof ServerPlayer serverPlayer) {
            for(var pk : Cobblemon.INSTANCE.getStorage().getParty(serverPlayer)) {
                maxLevel = Math.max(maxLevel, pk.getLevel());
            }
        }

        return maxLevel;
    }

    public int getActivePokemon(Player player) {
        int count = 0;

        if(player instanceof ServerPlayer serverPlayer) {
            for(var pk : Cobblemon.INSTANCE.getStorage().getParty(serverPlayer)) {
                if(!pk.isFainted()) {
                    count++;
                }
            }
        }

        return count;
    }

    public TrainerPlayerData getData(Player player) {
        var builder = new TrainerPlayerData.Builder(player);
        
        return player.getServer().overworld().getDataStorage().computeIfAbsent(
            new Factory<>(builder::create, builder::of, DataFixTypes.LEVEL),
            TrainerPlayerData.filePath(player)).forPlayer(player);
    }

    public Stream<Map.Entry<String, TrainerMobData>> getAllData(String... series) {
        return series.length > 0
            ? this.trainerMobs.entrySet().stream().filter(e -> Stream.of(series).anyMatch(s -> e.getValue().isOfSeries(s)))
            : this.trainerMobs.entrySet().stream();
    }

    public TrainerBattleMemory getBattleMemory(TrainerMob mob) {
        return getBattleMemory(mob.getServer().overworld(), mob.getTrainerId());
    }

    public TrainerBattleMemory getBattleMemory(ServerLevel level, String trainerId) {
        var dds = level.getServer().overworld().getDataStorage();
        var ver = TrainerBattleMemory.getVersion(dds);

        if(ver.isOutdated()) {
            TrainerBattleMemory.migrate(this.server, RCTMod.getInstance().getTrainerManager()); // just to be save
        }

        return dds.computeIfAbsent(
            new Factory<>(TrainerBattleMemory::new, TrainerBattleMemory::of, DataFixTypes.LEVEL),
            TrainerBattleMemory.filePath(trainerId, ver));
    }

    public boolean updateRequired(Player player) {
        return this.receivedUpdates.add(player);
    }

    public boolean requiresUpdate(Player player) {
        return this.receivedUpdates.remove(player);
    }

    public void loadTrainers() {
        this.forceReload(this.resourceManager);

        if(TrainerBattleMemory.getVersion(this.server.overworld().getDataStorage()).isOutdated()) {
            TrainerBattleMemory.migrate(this.server, this);
        }

        SuggestionUtils.initSuggestions();
    }

    protected void forceReload(ResourceManager resourceManager) {
        this.reloadRequired = false;
        this.reload(resourceManager);

        var oldTrainers = new HashMap<String, Trainer>();
        var newTrainerMobs = new HashMap<String, TrainerMobData>();
        var reg = ModCommon.RCT.getTrainerRegistry();

        reg.getIds().stream()
            .map(tid -> Map.<String, Trainer>entry(tid, reg.getById(tid)))
            .filter(entry -> entry.getValue() instanceof TrainerNPC)
            .forEach(entry -> oldTrainers.put(entry.getKey(), entry.getValue()));

        reg.clearNPCs();

        this.listTrainerTeams((rl, io) -> {
            var trainerId = PathUtils.filename(rl.getPath());

            this.loadResource(trainerId, "mobs", tmd -> {
                newTrainerMobs.put(trainerId, tmd);
                this.registerTrainer(trainerId, tmd, oldTrainers);
            }, TrainerMobData.class);
        });

        this.trainerMobs = newTrainerMobs;
        this.seriesManager.onLoad(this);

        newTrainerMobs.forEach((trainerId, tmd) -> {
            this.seriesManager.getSeriesIds().forEach(seriesId -> {
                var sg = this.seriesManager.getGraph(seriesId);

                tmd.getRequiredSeries(seriesId).forEach(set -> set.forEach(sid -> {
                    if(sg == this.seriesManager.UNKNOWN_SERIES) {
                        ModCommon.LOG.warn(String.format("Trainer '%s' has requiredSeries with unknown series id '%s'", trainerId, sid));
                    }
                }));
            });
        });

        this.receivedUpdates = new HashSet<>();
        this.close();
        ModServer.syncTrainerManger(this.server.getPlayerList().getPlayers());
        this.setLoading(false);
        ModCommon.LOG.info(String.format("Registered %d trainers", this.trainerMobs.size()));
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        this.resourceManager = resourceManager;
        this.setReloadRequired();
    }
}
