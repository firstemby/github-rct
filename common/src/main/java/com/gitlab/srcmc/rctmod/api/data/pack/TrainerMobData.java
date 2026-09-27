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
package com.gitlab.srcmc.rctmod.api.data.pack;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.gitlab.srcmc.rctapi.api.util.Text;
import com.gitlab.srcmc.rctmod.ModCommon;
import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.gitlab.srcmc.rctmod.api.config.IServerConfig;
import com.gitlab.srcmc.rctmod.api.data.Optional;
import com.gitlab.srcmc.rctmod.api.data.sync.PlayerState;
import com.gitlab.srcmc.rctmod.api.service.SeriesManager.TrainerNode;
import com.gitlab.srcmc.rctmod.api.utils.LevelUtils;
import com.gitlab.srcmc.rctmod.api.utils.JsonUtils.JsonExclude;
import com.gitlab.srcmc.rctmod.api.utils.collections.StringSetList;
import com.gitlab.srcmc.rctmod.api.utils.collections.StringSetListMap;
import com.gitlab.srcmc.rctmod.compat.TBCSCompat.WinCommandsDeserializer;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.reflect.TypeToken;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class TrainerMobData implements IDataPackObject, Serializable {
    private static final long serialVersionUID = 0L;

    private String type = "normal";
    private String signatureItem = "";
    private StringSetListMap requiredDefeats = new StringSetListMap();
    private StringSetList requiredSeries = new StringSetList();
    private Set<String> series = new HashSet<>();
    // private Set<String> substitutes = new HashSet<>();
    private boolean optional;

    private int maxTrainerWins = 3;
    private int maxTrainerDefeats = 1;
    private int battleCooldownTicks = 240;
    private Optional<Integer> relativeLevelCap = Optional.empty();
    
    private float spawnWeightFactor = 1F; // >= 0
    private Set<String> biomeTagBlacklist = new HashSet<>();
    private Set<String> biomeTagWhitelist = new HashSet<>();
    
    private Optional<Boolean> forceBattleOnSight = Optional.empty();
    private Optional<Float> forceBattleMaxDistance = Optional.empty();
    private Optional<Integer> forceBattleLookTicks = Optional.empty();
    private Optional<Integer> forceBattleMaxLevelDiff = Optional.empty();

    @JsonAdapter(WinCommandsDeserializer.class)
    private Map<Integer, ?> winCommands;

    @JsonExclude private Map<String, Text[]> dialog = new HashMap<>();
    @JsonExclude private TrainerTeam trainerTeam;
    @JsonExclude private RLWrapper textureResource;
    @JsonExclude private RLWrapper lootTableResource;
    @JsonExclude private String trainerId = "";

    private transient Supplier<TrainerType> lazyType;

    public TrainerMobData() {
        this.textureResource = new RLWrapper(ResourceLocation.fromNamespaceAndPath(ModCommon.MOD_ID, "textures/" + DataPackManager.PATH_DEFAULT + ".png"));
        this.lootTableResource = new RLWrapper(ResourceLocation.fromNamespaceAndPath(ModCommon.MOD_ID, DataPackManager.PATH_DEFAULT));
        this.trainerTeam = new TrainerTeam();
        this.init();
    }

    public TrainerMobData(TrainerMobData origin) {
        this.type = origin.type;
        this.signatureItem = origin.signatureItem;
        this.winCommands = origin.winCommands != null ? Map.copyOf(origin.winCommands) : null;
        this.relativeLevelCap = origin.relativeLevelCap;
        this.requiredDefeats = origin.requiredDefeats != null ? new StringSetListMap(origin.requiredDefeats) : null;
        this.requiredSeries = origin.requiredSeries != null ? new StringSetList(origin.requiredSeries) : null;
        this.series = origin.series != null ? Set.copyOf(origin.series) : null;
        // this.substitutes = origin.substitutes != null ? Set.copyOf(origin.substitutes) : null;
        this.optional = origin.optional;
        this.maxTrainerWins = origin.maxTrainerWins;
        this.maxTrainerDefeats = origin.maxTrainerDefeats;
        this.battleCooldownTicks = origin.battleCooldownTicks;
        this.spawnWeightFactor = origin.spawnWeightFactor;
        this.biomeTagBlacklist = origin.biomeTagBlacklist != null ? Set.copyOf(origin.biomeTagBlacklist) : null;
        this.biomeTagWhitelist = origin.biomeTagWhitelist != null ? Set.copyOf(origin.biomeTagWhitelist) : null;
        this.forceBattleOnSight = origin.forceBattleOnSight;
        this.forceBattleMaxDistance = origin.forceBattleMaxDistance;
        this.forceBattleLookTicks = origin.forceBattleLookTicks;
        this.forceBattleMaxLevelDiff = origin.forceBattleMaxLevelDiff;
        this.dialog = origin.dialog != null ? Map.copyOf(origin.dialog) : null;
        this.textureResource = origin.textureResource;
        this.lootTableResource = origin.lootTableResource;
        this.trainerTeam = origin.trainerTeam; // no need for deep copy since immutable
        this.trainerId = origin.trainerId;
        this.init();
    }

    private void init() {
        this.lazyType = () -> {
            var type = TrainerType.valueOf(this.type.toLowerCase());
            this.lazyType = () -> type;
            return type;
        };
    }

    private void writeObject(ObjectOutputStream oos) throws IOException {
        // Since the type of win commands depends on a different library it
        // is unsafe to include these during serialization (transient will
        // prevent GSON from parsing the property, hence this workaround).
        var winCommands = this.winCommands;
        this.winCommands = null;
        oos.defaultWriteObject();
        this.winCommands = winCommands;
        oos.writeObject(this.getType());
    }

    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        var type = TrainerType.registerOrGet(this.type, (TrainerType)ois.readObject());
        this.lazyType = () -> type;
    }

    public TrainerType getType() {
        return this.lazyType.get();
    }

    public int getRewardLevelCap(Player player) {
        var ps = PlayerState.get(player);
        var sid = ps.getCurrentSeries();
        var sm = RCTMod.getInstance().getSeriesManager();
        var rlc = this.getRequiredLevelCap(player);

        return sm.getGraph(sid)
            .getNext(Set.of(this.trainerId))
            .stream().map(TrainerNode::id)
            .map(tid -> LevelUtils.levelCap(sid, tid))
            .filter(lc -> lc > rlc)
            .min(Integer::compare)
            .orElse(rlc);
    }

    public int getRequiredLevelCap(Player player) {
        return LevelUtils.levelCap(PlayerState.get(player).getCurrentSeries(), this.trainerId);
    }

    public Optional<Integer> getRelativeLevelCap() {
        if(this.relativeLevelCap.has()) {
            var v = this.relativeLevelCap.get();

            if(v < IServerConfig.MIN_RELATIVE_LEVEL_CAP) {
                ModCommon.LOG.warn(String.format("invalid relativeLevelCap for '%s': expected range is [%d, %d] got %d", this.trainerId, IServerConfig.MIN_RELATIVE_LEVEL_CAP, IServerConfig.MAX_RELATIVE_LEVEL_CAP, v));
                this.relativeLevelCap = Optional.of(IServerConfig.MIN_RELATIVE_LEVEL_CAP);
            } else if(v > IServerConfig.MAX_RELATIVE_LEVEL_CAP) {
                ModCommon.LOG.warn(String.format("invalid relativeLevelCap for '%s': expected range is [%d, %d] got %d", this.trainerId, IServerConfig.MIN_RELATIVE_LEVEL_CAP, IServerConfig.MAX_RELATIVE_LEVEL_CAP, v));
                this.relativeLevelCap = Optional.of(IServerConfig.MAX_RELATIVE_LEVEL_CAP);
            }
        }

        return this.relativeLevelCap;
    }

    public boolean isOfSeries(String seriesId) {
        return this.series.isEmpty() || this.series.contains(seriesId)
            || RCTMod.getInstance().getSeriesManager().getGraph(seriesId).getMetaData().progression().keySet().contains(this.trainerId);
    }

    public Stream<String> getSeries() {
        var sm = RCTMod.getInstance().getSeriesManager();

        return Stream.concat(
            sm.getSeriesIds().stream().filter(sid -> sm.getGraph(sid).getMetaData().progression().containsKey(this.trainerId)),
            this.series.stream()).distinct();
    }

    public List<Set<String>> getRequiredDefeats(String seriesId) {
        var list = this.requiredDefeats.get(seriesId);

        if(list == null) {
            var node = RCTMod.getInstance().getSeriesManager().getGraph(seriesId).getMetaData().progression().get(this.trainerId);

            if(node != null) {
                list = node.requiredDefeats();
            }
        }

        return list != null ? list : this.requiredDefeats.getDefault();
    }

    public List<Set<String>> getRequiredSeries() {
        return Collections.unmodifiableList(this.requiredSeries);
    }

    public Stream<String> getMissingSeriesRequirements(Map<String, Integer> completedSeries) {
        return this.getMissingSeriesRequirements(completedSeries, false);
    }

    public Stream<String> getMissingSeriesRequirements(Map<String, Integer> completedSeries, boolean includeAlternatives) {
        return this.requiredSeries.stream().mapMulti((set, cons) -> {
            var satisfied = set.stream().anyMatch(sid -> completedSeries.getOrDefault(sid, 0) >= 1);
            if(!satisfied && !set.isEmpty()) {
                if(includeAlternatives) {
                    set.forEach(cons);
                } else {
                    cons.accept(set.stream().findFirst().get());
                }
            }
        });
    }

    // public Set<String> getSubstitutes() {
    //     return Collections.unmodifiableSet(this.substitutes);
    // }

    public Stream<String> getMissingRequirements(String seriesId, Set<String> defeatedTrainerIds) {
        return this.getMissingRequirements(seriesId, defeatedTrainerIds, false);
    }

    public Stream<String> getMissingRequirements(String seriesId, Set<String> defeatedTrainerIds, boolean includeAlternatives) {
        return this.getRequiredDefeats(seriesId).stream().mapMulti((set, cons) -> {
            if(includeAlternatives) {
                if(set.stream().noneMatch(defeatedTrainerIds::contains)) {
                    set.stream().forEach(cons);
                }
            } else if(!set.isEmpty() && set.stream().noneMatch(defeatedTrainerIds::contains)) {
                cons.accept(set.stream().findFirst().get());
            }
        });
    }

    public boolean isOptional() {
        return this.optional;
    }

    public int getMaxTrainerWins() {
        return this.maxTrainerWins;
    }

    public int getMaxTrainerDefeats() {
        return this.maxTrainerDefeats;
    }

    public int getBattleCooldownTicks() {
        return this.battleCooldownTicks;
    }

    public float getSpawnWeightFactor() {
        return this.spawnWeightFactor;
    }

    public Set<String> getBiomeTagBlacklist() {
        return Collections.unmodifiableSet(this.biomeTagBlacklist);
    }

    public Set<String> getBiomeTagWhitelist() {
        return Collections.unmodifiableSet(this.biomeTagWhitelist);
    }

    public Optional<Boolean> getForceBattleOnSight() {
        return this.forceBattleOnSight;
    }

    public Optional<Float> getForceBattleMaxDistance() {
        if(this.forceBattleMaxDistance.has()) {
            var v = this.forceBattleMaxDistance.get();

            if(v < IServerConfig.MIN_FORCE_BATTLE_MAX_DISTANCE) {
                ModCommon.LOG.warn(String.format("invalid forceBattleMaxDistance for '%s': expected range is [%.2f, %.2f] got %.2f", this.trainerId, IServerConfig.MIN_FORCE_BATTLE_MAX_DISTANCE, IServerConfig.MAX_FORCE_BATTLE_MAX_DISTANCE, v));
                this.forceBattleMaxDistance = Optional.of(IServerConfig.MIN_FORCE_BATTLE_MAX_DISTANCE);
            } else if(v > 32) {
                ModCommon.LOG.warn(String.format("invalid forceBattleMaxDistance for '%s': expected range is [%.2f, %.2f] got %.2f", this.trainerId, IServerConfig.MIN_FORCE_BATTLE_MAX_DISTANCE, IServerConfig.MAX_FORCE_BATTLE_MAX_DISTANCE, v));
                this.forceBattleMaxDistance = Optional.of(IServerConfig.MAX_FORCE_BATTLE_MAX_DISTANCE);
            }
        }

        return this.forceBattleMaxDistance;
    }

    public Optional<Integer> getForceBattleLookTicks() {
        if(this.forceBattleLookTicks.has()) {
            var v = this.forceBattleLookTicks.get();

            if(v < IServerConfig.MIN_FORCE_BATTLE_LOOK_TICKS) {
                ModCommon.LOG.warn(String.format("invalid forceBattleLookTicks for '%s': expected range is [%d, inf) got %d", this.trainerId, IServerConfig.MIN_FORCE_BATTLE_LOOK_TICKS, v));
                this.forceBattleLookTicks = Optional.of(IServerConfig.MIN_FORCE_BATTLE_LOOK_TICKS);
            }
        }

        return this.forceBattleLookTicks;
    }

    public Optional<Integer> getForceBattleMaxLevelDiff() {
        if(this.forceBattleMaxLevelDiff.has()) {
            var v = this.forceBattleMaxLevelDiff.get();

            if(v < IServerConfig.MIN_FORCE_BATTLE_MAX_LEVEL_DIFF) {
                ModCommon.LOG.warn(String.format("invalid forceBattleMaxLevelDiff for '%s': expected range is [%d, inf) got %d", this.trainerId, IServerConfig.MIN_FORCE_BATTLE_MAX_LEVEL_DIFF, v));
                this.forceBattleMaxLevelDiff = Optional.of(IServerConfig.MIN_FORCE_BATTLE_MAX_LEVEL_DIFF);
            }
        }

        return this.forceBattleMaxLevelDiff;
    }

    public Map<String, Text[]> getDialog() {
        return Collections.unmodifiableMap(this.dialog);
    }

    public ResourceLocation getTextureResource() {
        return this.textureResource.resourceLocation();
    }

    public ResourceLocation getLootTableResource() {
        return this.lootTableResource.resourceLocation();
    }

    public TrainerTeam getTrainerTeam() {
        return this.trainerTeam;
    }

    public String getSignatureItem() {
        return this.signatureItem;
    }

    public Map<Integer, ?> getWinCommands() {
        return this.winCommands;
    }

    @Override
    public void onLoad(DataPackManager dpm, String trainerId, String context) {
        var lootTableResource = dpm.findResource(trainerId, "loot_table");
        var textureResource = dpm.findResource(trainerId, "textures");

        if(textureResource.isPresent()) {
            this.textureResource = new RLWrapper(textureResource.get());
        }

        if(lootTableResource.isPresent()) {
            // the loot table is loaded by net.minecraft.world.level.storage.loot.LootDataManager
            // which resolves a 'shorthand' resource location automatically.
            this.lootTableResource = new RLWrapper(ResourceLocation.fromNamespaceAndPath(ModCommon.MOD_ID, lootTableResource.get().getPath()
                .replace("loot_table/", "")
                .replace(".json", "")));
        }

        dpm.loadResource(trainerId, "dialogs",
            dialog -> this.dialog = dialog,
            new TypeToken<Map<String, Text[]>>() {});

        if(this.dialog == null) {
            this.dialog = new HashMap<>();
        }

        var trainerTeam = dpm.loadTrainerTeam(trainerId);

        if(trainerTeam.isPresent()) {
            this.trainerTeam = trainerTeam.get();
        }

        this.trainerId = trainerId;
    }

    class RLWrapper implements Serializable {
        private static final long serialVersionUID = 0L;
        
        private transient Supplier<ResourceLocation> rlSup;
        private transient ResourceLocation rl;
        private String namespace, path;

        public RLWrapper() {
            this(null);
        }

        public RLWrapper(ResourceLocation rl) {
            if(rl != null) {
                this.namespace = rl.getNamespace();
                this.path = rl.getPath();
            }

            this.rl = rl;
            this.init();
        }

        private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
            ois.defaultReadObject();
            this.init();
        }

        public ResourceLocation resourceLocation() {
            return this.rlSup.get();
        }

        private void init() {
            this.rlSup = () -> {
                if(this.rl == null) {
                    this.rl = ResourceLocation.fromNamespaceAndPath(this.namespace, this.path);
                }
                
                this.rlSup = () -> this.rl;
                return this.rl;
            };
        }
    }
}
