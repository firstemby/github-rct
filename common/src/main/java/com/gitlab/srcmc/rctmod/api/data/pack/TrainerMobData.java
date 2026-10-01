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
import java.io.Serializable;
import java.lang.reflect.Type;
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
import com.gitlab.srcmc.rctmod.api.data.pack.TrainerMobData.SeriesProperties.MapView;
import com.gitlab.srcmc.rctmod.api.data.sync.PlayerState;
import com.gitlab.srcmc.rctmod.api.service.SeriesManager.TrainerNode;
import com.gitlab.srcmc.rctmod.api.utils.LevelUtils;
import com.gitlab.srcmc.rctmod.api.utils.JsonUtils.JsonExclude;
import com.gitlab.srcmc.rctmod.api.utils.collections.StringSetList;
import com.gitlab.srcmc.rctmod.compat.TBCSCompat.WinCommandsDeserializer;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.reflect.TypeToken;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class TrainerMobData implements IDataPackObject, Serializable {
    private static final long serialVersionUID = 0L;
    
    public static class SeriesProperties implements Serializable {
        private static final long serialVersionUID = 0L;

        public static class MapView extends HashMap<String, Object> {}

        private String type = "normal";
        private StringSetList requiredDefeats = new StringSetList();
        private StringSetList requiredSeries = new StringSetList();
        // private Set<String> substitutes = new HashSet<>();
        private boolean optional;

        private int maxTrainerWins = 3;
        private int maxTrainerDefeats = 1;
        private int battleCooldownTicks = 240;
        private Optional<Integer> relativeLevelCap = Optional.empty();

        private float spawnWeightFactor = 1F; // >= 0
        private Set<String> biomeTagBlacklist = new HashSet<>();
        private Set<String> biomeTagWhitelist = new HashSet<>();

        @JsonAdapter(WinCommandsDeserializer.class)
        private Map<Integer, ?> winCommands;

        private transient TrainerType typeCache;

        public SeriesProperties() {
        }

        public SeriesProperties(SeriesProperties template) {
            this(template, Map.of());
        }

        public SeriesProperties(SeriesProperties template, Map<String, Object> fields) {
            this.clone(template);
            this.clone(fields);
        }

        public TrainerType getType() {
            if(this.typeCache == null) {
                this.typeCache = TrainerType.valueOf(this.type.toLowerCase().trim());
            }

            return this.typeCache;
        }

        public void clone(SeriesProperties template) {
            this.type = template.type;
            this.requiredDefeats = template.requiredDefeats;
            this.requiredSeries = template.requiredSeries;
            // this.substitutes = template.substitutes;
            this.optional = template.optional;
            this.maxTrainerWins = template.maxTrainerWins;
            this.maxTrainerDefeats = template.maxTrainerDefeats;
            this.battleCooldownTicks = template.battleCooldownTicks;
            this.relativeLevelCap = template.relativeLevelCap;
            this.spawnWeightFactor = template.spawnWeightFactor;
            this.biomeTagBlacklist = template.biomeTagBlacklist;
            this.biomeTagWhitelist = template.biomeTagWhitelist;
            this.winCommands = template.winCommands;
        }

        public void clone(Map<String, Object> fields) {
            for(var key : fields.keySet()) {
                if("type".equals(key)) {
                    var value = fields.get(key);

                    if(value instanceof String value2) {
                        this.type = value2;
                    } else {
                        ModCommon.LOG.warn(String.format("Wrong value type in trainer mob at '%s': Expected %s, got %s", key, String.class.getSimpleName(), value == null ? "null" : value.getClass().getSimpleName()));
                    }
                } else if("requiredDefeats".equals(key)) {
                    var value = fields.get(key);

                    if(value instanceof StringSetList value2) {
                        this.requiredDefeats = value2;
                    } else {
                        ModCommon.LOG.warn(String.format("Wrong value type in trainer mob at '%s': Expected %s, got %s", key, StringSetList.class.getSimpleName(), value == null ? "null" : value.getClass().getSimpleName()));
                    }
                } else if("requiredSeries".equals(key)) {
                    var value = fields.get(key);

                    if(value instanceof StringSetList value2) {
                        this.requiredSeries = value2;
                    } else {
                        ModCommon.LOG.warn(String.format("Wrong value type in trainer mob at '%s': Expected %s, got %s", key, StringSetList.class.getSimpleName(), value == null ? "null" : value.getClass().getSimpleName()));
                    }
                // } else if("substitutes".equals(key)) {
                //     var value = fields.get(key);

                //     if(value instanceof Set value2) {
                //         this.substitutes = copySet(value2, String.class);
                //     } else {
                //         ModCommon.LOG.warn(String.format("Wrong value type in trainer mob at '%s': Expected %s, got %s", key, Set.class.getSimpleName(), value == null ? "null" : value.getClass().getSimpleName()));
                //     }
                } else if("optional".equals(key)) {
                    var value = fields.get(key);

                    if(value instanceof Boolean value2) {
                        this.optional = value2;
                    } else {
                        ModCommon.LOG.warn(String.format("Wrong value type in trainer mob at '%s': Expected %s, got %s", key, Boolean.class.getSimpleName(), value == null ? "null" : value.getClass().getSimpleName()));
                    }
                } else if("maxTrainerWins".equals(key)) {
                    var value = fields.get(key);

                    if(value instanceof Integer value2) {
                        this.maxTrainerWins = value2;
                    } else {
                        ModCommon.LOG.warn(String.format("Wrong value type in trainer mob at '%s': Expected %s, got %s", key, Integer.class.getSimpleName(), value == null ? "null" : value.getClass().getSimpleName()));
                    }
                } else if("maxTrainerDefeats".equals(key)) {
                    var value = fields.get(key);

                    if(value instanceof Integer value2) {
                        this.maxTrainerDefeats = value2;
                    } else {
                        ModCommon.LOG.warn(String.format("Wrong value type in trainer mob at '%s': Expected %s, got %s", key, Integer.class.getSimpleName(), value == null ? "null" : value.getClass().getSimpleName()));
                    }
                } else if("battleCooldownTicks".equals(key)) {
                    var value = fields.get(key);

                    if(value instanceof Integer value2) {
                        this.battleCooldownTicks = value2;
                    } else {
                        ModCommon.LOG.warn(String.format("Wrong value type in trainer mob at '%s': Expected %s, got %s", key, Integer.class.getSimpleName(), value == null ? "null" : value.getClass().getSimpleName()));
                    }
                } else if("relativeLevelCap".equals(key)) {
                    var value = fields.get(key);

                    if(value instanceof Optional value2 && value2.has() && value2.get() instanceof Integer value3) {
                        this.relativeLevelCap = Optional.of(value3);
                    } else {
                        ModCommon.LOG.warn(String.format("Wrong value type in trainer mob at '%s': Expected %s, got %s", key, Integer.class.getSimpleName(), value == null ? "null" : value.getClass().getSimpleName()));
                    }
                } else if("spawnWeightFactor".equals(key)) {
                    var value = fields.get(key);

                    if(value instanceof Float value2) {
                        this.spawnWeightFactor = value2;
                    } else {
                        ModCommon.LOG.warn(String.format("Wrong value type in trainer mob at '%s': Expected %s, got %s", key, Float.class.getSimpleName(), value == null ? "null" : value.getClass().getSimpleName()));
                    }
                } else if("biomeTagBlacklist".equals(key)) {
                    var value = fields.get(key);

                    if(value instanceof Set value2) {
                        this.biomeTagBlacklist = copySet(value2, String.class);
                    } else {
                        ModCommon.LOG.warn(String.format("Wrong value type in trainer mob at '%s': Expected %s, got %s", key, Set.class.getSimpleName(), value == null ? "null" : value.getClass().getSimpleName()));
                    }
                } else if("biomeTagWhitelist".equals(key)) {
                    var value = fields.get(key);

                    if(value instanceof Set value2) {
                        this.biomeTagWhitelist = copySet(value2, String.class);
                    } else {
                        ModCommon.LOG.warn(String.format("Wrong value type in trainer mob at '%s': Expected %s, got %s", key, Set.class.getSimpleName(), value == null ? "null" : value.getClass().getSimpleName()));
                    }
                } else if("winCommands".equals(key)) {
                    var value = fields.get(key);

                    if(value instanceof Map value2) {
                        this.winCommands = copyMap(value2, Integer.class, Object.class);
                    } else {
                        ModCommon.LOG.warn(String.format("Wrong value type in trainer mob at '%s': Expected %s, got %s", key, Set.class.getSimpleName(), value == null ? "null" : value.getClass().getSimpleName()));
                    }
                } else {
                    ModCommon.LOG.warn(String.format("Unknown property for trainer mob: '%s'", key));
                }
            }
        }

        @SuppressWarnings("unused")
        private void defaultReadObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
            ois.defaultReadObject();
            this.typeCache = null;
        }

        static <T> Set<T> copySet(Set<?> source, Class<T> valueType) {
            var copy = new HashSet<T>();
            var it = source.iterator();

            while(it.hasNext()) {
                var value = it.next();

                if(value == null || valueType.isInstance(value)) {
                    copy.add(valueType.cast(value));
                } else {
                    ModCommon.LOG.warn("Wrong value type in set: Expected %s, got %s", valueType.getSimpleName(), value == null ? "null" : value.getClass().getSimpleName());
                }
            }

            return Collections.unmodifiableSet(copy);
        }

        static <T, V> Map<T, V> copyMap(Map<?, ?> source, Class<T> keyType, Class<V> valueType) {
            var copy = new HashMap<T, V>();
            var sameKeyType = false;

            if(!source.isEmpty()) {
                var someKey = source.keySet().iterator().next();
                sameKeyType = keyType.isInstance(someKey);

                if(!sameKeyType) {
                    ModCommon.LOG.warn("Wrong value type in map: Expected %s, got %s", valueType.getSimpleName(), someKey != null ? someKey.getClass().getSimpleName() : "null");
                }
            }

            if(sameKeyType) {
                source.keySet().forEach(key -> {
                    var value = source.get(key);

                    if(value == null || valueType.isInstance(value)) {
                        copy.put(keyType.cast(key), valueType.cast(value));
                    } else {
                        ModCommon.LOG.warn("Wrong value type in map: Expected %s, got %s", valueType.getSimpleName(), value == null ? "null" : value.getClass().getSimpleName());
                    }
                });
            }

            return Collections.unmodifiableMap(copy);
        }
    }

    static class GlobalProperties extends SeriesProperties {
        private static final long serialVersionUID = 0L;

        private String signatureItem = "";
        private Optional<Boolean> forceBattleOnSight = Optional.empty();
        private Optional<Float> forceBattleMaxDistance = Optional.empty();
        private Optional<Integer> forceBattleLookTicks = Optional.empty();
        private Optional<Integer> forceBattleMaxLevelDiff = Optional.empty();
    }

    // global
    private Map<String, SeriesProperties> series = Map.of();
    private GlobalProperties properties = new GlobalProperties();

    // data pack and registry
    @JsonExclude private Map<String, Text[]> dialog = new HashMap<>();
    @JsonExclude private TrainerTeam trainerTeam;
    @JsonExclude private RLWrapper textureResource;
    @JsonExclude private RLWrapper lootTableResource;
    @JsonExclude private String trainerId = "";

    public TrainerMobData() {
        this.textureResource = new RLWrapper(ResourceLocation.fromNamespaceAndPath(ModCommon.MOD_ID, "textures/" + DataPackManager.PATH_DEFAULT + ".png"));
        this.lootTableResource = new RLWrapper(ResourceLocation.fromNamespaceAndPath(ModCommon.MOD_ID, DataPackManager.PATH_DEFAULT));
        this.trainerTeam = new TrainerTeam();
    }

    public void updateSeriesProperties(String seriesId, SeriesMetaData seriesMeta) {
        var metaProperties = seriesMeta.progression().get(this.trainerId);

        if(metaProperties != null) {
            var seriesProperties = this.series.get(seriesId);
            var series = new HashMap<String, SeriesProperties>(this.series);

            if(seriesProperties == null) {
                series.put(seriesId, new SeriesProperties(this.properties, metaProperties));
            } else {
                series.put(seriesId, new SeriesProperties(seriesProperties, metaProperties));
            }

            this.series = Collections.unmodifiableMap(series);
        }
    }

    protected SeriesProperties properties(String seriesId) {
        var properties = this.series.get(seriesId);
        return properties != null ? properties : this.properties;
    }

    public TrainerType getType(String seriesId) {
        return this.properties(seriesId).getType();
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

    public Optional<Integer> getRelativeLevelCap(String seriesId) {
        var relativeLevelCap = this.properties(seriesId).relativeLevelCap;

        if(relativeLevelCap.has()) {
            var v = relativeLevelCap.get();

            if(v < IServerConfig.MIN_RELATIVE_LEVEL_CAP) {
                ModCommon.LOG.warn(String.format("invalid relativeLevelCap for '%s': expected range is [%d, %d] got %d", this.trainerId, IServerConfig.MIN_RELATIVE_LEVEL_CAP, IServerConfig.MAX_RELATIVE_LEVEL_CAP, v));
                relativeLevelCap = Optional.of(IServerConfig.MIN_RELATIVE_LEVEL_CAP);
            } else if(v > IServerConfig.MAX_RELATIVE_LEVEL_CAP) {
                ModCommon.LOG.warn(String.format("invalid relativeLevelCap for '%s': expected range is [%d, %d] got %d", this.trainerId, IServerConfig.MIN_RELATIVE_LEVEL_CAP, IServerConfig.MAX_RELATIVE_LEVEL_CAP, v));
                relativeLevelCap = Optional.of(IServerConfig.MAX_RELATIVE_LEVEL_CAP);
            }
        }

        return relativeLevelCap;
    }

    public boolean isOfSeries(String seriesId) {
        return this.series.isEmpty() || this.series.containsKey(seriesId)
            || RCTMod.getInstance().getSeriesManager().getGraph(seriesId).getMetaData().progression().keySet().contains(this.trainerId);
    }

    public Stream<String> getSeries() {
        var sm = RCTMod.getInstance().getSeriesManager();

        return Stream.concat(
            sm.getSeriesIds().stream().filter(sid -> sm.getGraph(sid).getMetaData().progression().containsKey(this.trainerId)),
            this.series.keySet().stream()).distinct();
    }

    public List<Set<String>> getRequiredDefeats(String seriesId) {
        return this.properties(seriesId).requiredDefeats;
    }

    public List<Set<String>> getRequiredSeries(String seriesId) {
        return this.properties(seriesId).requiredSeries;
    }

    public Stream<String> getMissingSeriesRequirements(String seriesId, Map<String, Integer> completedSeries) {
        return this.getMissingSeriesRequirements(seriesId, completedSeries, false);
    }

    public Stream<String> getMissingSeriesRequirements(String seriesId, Map<String, Integer> completedSeries, boolean includeAlternatives) {
        return this.properties(seriesId).requiredSeries.stream().mapMulti((set, cons) -> {
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

    // public Set<String> getSubstitutes(String seriesId) {
    //     return this.properties(seriesId).substitutes;
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

    public boolean isOptional(String seriesId) {
        return this.properties(seriesId).optional;
    }

    public int getMaxTrainerWins(String seriesId) {
        return this.properties(seriesId).maxTrainerWins;
    }

    public int getMaxTrainerDefeats(String seriesId) {
        return this.properties(seriesId).maxTrainerDefeats;
    }

    public int getBattleCooldownTicks(String seriesId) {
        return this.properties(seriesId).battleCooldownTicks;
    }

    public float getSpawnWeightFactor(String seriesId) {
        return this.properties(seriesId).spawnWeightFactor;
    }

    public Set<String> getBiomeTagBlacklist(String seriesId) {
        return this.properties(seriesId).biomeTagBlacklist;
    }

    public Set<String> getBiomeTagWhitelist(String seriesId) {
        return this.properties(seriesId).biomeTagWhitelist;
    }

    public Optional<Boolean> getForceBattleOnSight() {
        return this.properties.forceBattleOnSight;
    }

    public Optional<Float> getForceBattleMaxDistance() {
        if(this.properties.forceBattleMaxDistance.has()) {
            var v = this.properties.forceBattleMaxDistance.get();

            if(v < IServerConfig.MIN_FORCE_BATTLE_MAX_DISTANCE) {
                ModCommon.LOG.warn(String.format("invalid forceBattleMaxDistance for '%s': expected range is [%.2f, %.2f] got %.2f", this.trainerId, IServerConfig.MIN_FORCE_BATTLE_MAX_DISTANCE, IServerConfig.MAX_FORCE_BATTLE_MAX_DISTANCE, v));
                this.properties.forceBattleMaxDistance = Optional.of(IServerConfig.MIN_FORCE_BATTLE_MAX_DISTANCE);
            } else if(v > IServerConfig.MAX_FORCE_BATTLE_MAX_DISTANCE) {
                ModCommon.LOG.warn(String.format("invalid forceBattleMaxDistance for '%s': expected range is [%.2f, %.2f] got %.2f", this.trainerId, IServerConfig.MIN_FORCE_BATTLE_MAX_DISTANCE, IServerConfig.MAX_FORCE_BATTLE_MAX_DISTANCE, v));
                this.properties.forceBattleMaxDistance = Optional.of(IServerConfig.MAX_FORCE_BATTLE_MAX_DISTANCE);
            }
        }

        return this.properties.forceBattleMaxDistance;
    }

    public Optional<Integer> getForceBattleLookTicks() {
        if(this.properties.forceBattleLookTicks.has()) {
            var v = this.properties.forceBattleLookTicks.get();

            if(v < IServerConfig.MIN_FORCE_BATTLE_LOOK_TICKS) {
                ModCommon.LOG.warn(String.format("invalid forceBattleLookTicks for '%s': expected range is [%d, inf) got %d", this.trainerId, IServerConfig.MIN_FORCE_BATTLE_LOOK_TICKS, v));
                this.properties.forceBattleLookTicks = Optional.of(IServerConfig.MIN_FORCE_BATTLE_LOOK_TICKS);
            }
        }

        return this.properties.forceBattleLookTicks;
    }

    public Optional<Integer> getForceBattleMaxLevelDiff() {
        if(this.properties.forceBattleMaxLevelDiff.has()) {
            var v = this.properties.forceBattleMaxLevelDiff.get();

            if(v < IServerConfig.MIN_FORCE_BATTLE_MAX_LEVEL_DIFF) {
                ModCommon.LOG.warn(String.format("invalid forceBattleMaxLevelDiff for '%s': expected range is [%d, inf) got %d", this.trainerId, IServerConfig.MIN_FORCE_BATTLE_MAX_LEVEL_DIFF, v));
                this.properties.forceBattleMaxLevelDiff = Optional.of(IServerConfig.MIN_FORCE_BATTLE_MAX_LEVEL_DIFF);
            }
        }

        return this.properties.forceBattleMaxLevelDiff;
    }

    public Map<String, Text[]> getDialog() {
        return this.dialog;
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
        return this.properties.signatureItem;
    }

    public Map<Integer, ?> getWinCommands(String seriesId) {
        return this.properties(seriesId).winCommands;
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

    public static class MapViewDeserializer implements JsonDeserializer<MapView> {
        @Override
        public MapView deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return deserialize(json, context);
        }

        public static MapView deserialize(JsonElement json, JsonDeserializationContext context) throws JsonParseException {
            var map = new MapView();

            if(json.isJsonObject()) {
                var jobj = json.getAsJsonObject();

                jobj.entrySet().forEach(e -> {
                    var key = e.getKey();
                    var value = e.getValue();
                    var parsed = switch(key) {
                        case "requiredSeries" -> context.deserialize(value, StringSetList.class);
                        case "requiredDefeats" -> context.deserialize(value, StringSetList.class);
                        default -> context.deserialize(value, Object.class);
                    };

                    map.put(key, parsed);
                });
            }

            return map;
        }
    }

    public static class TrainerModDataDeserializer implements JsonDeserializer<TrainerMobData> {
        @Override
        public TrainerMobData deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            var tmd = new TrainerMobData();

            if(json.isJsonObject()) {
                var jobj = json.getAsJsonObject();
                tmd.properties = context.deserialize(json, GlobalProperties.class); // ignores "series"
                tmd.series = this.deserializeSeriesMap(tmd.properties, jobj.get("series"), context);
            }

            return tmd;
        }

        public Map<String, SeriesProperties> deserializeSeriesMap(GlobalProperties global, JsonElement json, JsonDeserializationContext context) throws JsonParseException {
            var map = new HashMap<String, SeriesProperties>();

            if(json.isJsonArray()) {
                json.getAsJsonArray().forEach(e -> {
                    var key = e.getAsString();
                    map.put(key, new SeriesProperties(global));
                });
            } else if(json.isJsonObject()) {
                var jobj = json.getAsJsonObject();
                jobj.keySet().forEach(key -> map.put(key, new SeriesProperties(global, MapViewDeserializer.deserialize(jobj.get(key), context))));
            }

            return Collections.unmodifiableMap(map);
        }
    }
}
