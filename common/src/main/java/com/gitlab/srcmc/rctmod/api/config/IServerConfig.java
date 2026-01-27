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
package com.gitlab.srcmc.rctmod.api.config;

import java.util.List;

import com.gitlab.srcmc.rctmod.api.service.SeriesManager;

public interface IServerConfig extends IModConfig {
    /**
     * Reload is invoked after the config was reloaded and all config values have been parsed.
     */
    default void reload() {}

    /**
     * A global factor that determines if a spawn attempt for a trainer is made.
     * 
     * range [0, 1]
     * default 0.85
     */
    default double globalSpawnChance() { return 0.85; }

    /**
     * The chance for a trainer to spawn will shrink towards this value based of how
     * many trainers are already spawned in for a player. For example if a player has 0
     * trainers spawned for them the chance will be as configured by globalSpawnChance,
     * if a player has barely filled up their spawn cap (maxTrainersPerPlayer), i.e.
     * only one more free spot is left, the chance for the last trainer will be as
     * configured by globalSpawnChanceMinimum. Set to any value equal to or above
     * globalSpawnChance to disable (e.g. 1.0).
     * 
     * range [0, 1]
     * default: 0.1
     */
    default double globalSpawnChanceMinimum() { return 0.1; }

    /**
     * The interval in ticks at which a spawn attempt is made per player.
     * 
     * range [1, inf)
     * default 180
     */
    default int spawnIntervalTicks() { return 180; }

    /**
     * The spawn interval ticks will grow towards this value based of how many trainers
     * are already spawned in for a player. For example if a player has 0 trainers
     * spawned for them the spawn interval ticks will be as configured by
     * spawnIntervalTicks, if a player has barely filled up their spawn cap
     * (maxTrainersPerPlayer), i.e. only one more free spot is left, the spawn interval
     * for the last trainer will be as configured by spawnIntervalTicksMaximum. Set to
     * any value equal to or below spawnIntervalTicks to disable (e.g. 0).
     * 
     * range [0, inf)
     * default: 2400
     */
    default int spawnIntervalTicksMaximum() { return 2400; }

    /**
     * (Non persistent) trainers will despawn if they have no line of sight to any player
     * for the configured amount of ticks. Set to a negative value to disable this feature
     * (trainers may only despawn if they chunk becomes unloaded in that case).
     * 
     * range (-inf, inf)
     * default: 6000
     */
    default int despawnTicksIfUnseen() { return 6000; }

    /**
     * If enabled trainers will only spawn naturally around players that have a trainer
     * card in their inventory (does not affect trainer spawners).
     * 
     * default: false
     */
    default boolean spawningRequiresTrainerCard() { return false; }

    /**
     * If enabled a single trainer association npc may spawn naturally nearby players
     * that carry a trainer card and have either not started a series or completed
     * their current series. One may also spawn nearby any player in proximity to a
     * village (at least 3 occupied beds and a village center). These can spawn
     * everywhere but will respect the 'dimensionBlacklist' and 'dimensionWhitelist'
     * settings.
     * 
     * default: true
     */
    default boolean spawnTrainerAssociation() { return true; }

    /**
     * The min horizontal distance a trainer can spawn from players.
     * 
     * range [1, inf)
     * default: 25
     */
    default int minHorizontalDistanceToPlayers() { return 25; }

    /**
     * The max horizontal distance a trainer can spawn from players.
     * 
     * range [1, inf)
     * default: 70
     */
    default int maxHorizontalDistanceToPlayers() { return 70; }

    /**
     * The max vertical distance a trainer can spawn from players.
     * 
     * range [1, inf)
     * default: 30
     */
    default int maxVerticalDistanceToPlayers() { return 30; }

    /**
     * By default, this mod prevents the spawning of a trainer if another entity with
     * the same identity already exists anywhere in the world (in any dimension).
     * However, if this is set to a positive value, the mod will check for the existence
     * of a trainer with the same identity within the specified radius instead.
     * 
     * range (-inf, inf)
     * default: 500
     */
    default int uniqueTrainerRadius() { return 500; }

    /**
     * Spawn cap of trainers per player.
     * 
     * range [0, inf)
     * default: 12
     */
    default int maxTrainersPerPlayer() { return 12; }

    /**
     * Total trainer spawn cap. This value may be increased for servers with higher
     * expected player numbers (> 4), for example (|players| + 1)*maxTrainersPerPlayer.
     * 
     * range [0, inf)
     * default: 60
     */
    default int maxTrainersTotal() { return 60; }

    /**
     * The maximum level difference between the strongest pokemon in the team of a
     * player (at or below that players level cap) and the strongest pokemon in the
     * team of a trainer to allow that trainer to spawn for the player.
     * 
     * This value will scale linearly with a players level relative to 100 but will
     * not go below 4 (unless explicitely set to a lower value). An exception to this
     * are key trainers, who may always spawn with a level difference up to the
     * configured value.
     * 
     * For example with player Level = 20 and maxLevelDiff = 25:
     * result = max(4, (20/100) * 25) = max(4, 5) = 5
     * 
     * The spawn weight decreases with a higher level difference.
     * 
     * range [0, 100]
     * default: 25
     */
    default int maxLevelDiff() { return 25; }

    /**
     * A comma separated list of dimensions (e.g. ["multiworld:spawn" , "minecraft:the_end"]).
     * In these dimensions trainers will never spawn.
     *
     * default: []
     */
    default List<? extends String> dimensionBlacklist() { return List.of(); }

    /**
     * A comma separated list of dimensions (e.g. ["multiworld:spawn" , "minecraft:the_end"]).
     * Trainers may only spawn in these dimensions (unless the list is empty).
     *
     * default: []
     */
    default List<? extends String> dimensionWhitelist() { return List.of(); }

    /**
     * A comma separated list of biome tags (e.g. ["is_overworld", "is_forest"]). A
     * biome may not have any of the given tags attached to it, for a trainer to spawn
     * in that biome. Trainers may also have additional tags defined by a data pack.
     * 
     * default: []
     */
    default List<? extends String> biomeTagBlacklist() { return List.of(); }

    /**
     * A comma separated list of biome tags (e.g. ["is_overworld", "is_forest"]). A
     * biome must have atleast one of the given tags attached to it, for a trainer to
     * spawn in that biome (unless the list is empty). Trainers may also have
     * additional tags defined by a data pack.
     * 
     * default: []
     */
    default List<? extends String> biomeTagWhitelist() { return List.of(); }

    /**
     * Determines if trainers may force players into a battle if they look at each
     * other for a certain amount if time.
     * 
     * default: true
     */
    default boolean forceBattleOnSight() { return true; }

    /**
     * Max distance in blocks at which trainers may start a battle on sight.
     * 
     * range [0, 32]
     * default: 6
     */
    default float forceBattleMaxDistance() { return 8f; }

    /**
     * Number of ticks a trainer and player need to stare at each other to initiate a
     * battle. Setting this to a very large number effectively disables this features
     * but trainers will behave slightly different.
     * 
     * range [0, inf)
     * default: 60
     */
    default int forceBattleLookTicks() { return 60; }

    /**
     * Maximum level difference between the strongest pokemon of a trainer and a player
     * to allow the trainer to force a battle.
     * 
     * range [0, inf)
     * default: 16
     */
    default int forceBattleMaxLevelDiff() { return 16; }

    /**
     * Initial level cap of players. Pokemon will not gain any experience if at or
     * above the level cap. This value will be capped to not go below the level cap
     * for the first trainer of a series (you may use 'relativeLevelCap' for that).
     * 
     * range [1, 100]
     * default 15
     */
    default int initialLevelCap() { return 15; }

    /**
     * The level cap of a player is based of the strongest pokemon from the party of their next
     * required trainer in a series. The relativeLevelCap is added to the resulting value. For
     * instance if the next trainer for a player has a Pikachu at level 50, that players level
     * cap will be 50 + relativeLevelCap (can be negative).
     * 
     * default: 0
     */
    default int relativeLevelCap() { return 0; }

    /**
     * If enabled the level cap of a players will not prevent their pokemon from
     * gaining experience and leveling up. Trainers will still refuse to battle players
     * that carry pokemon above their level cap!
     * 
     * default: false
     */
    default boolean allowOverLeveling() { return false; }

    /**
     * The initial series players are placed in when entering a world for the first
     * time. Apart from any series id this value may also be set to one of the special
     * series ids "empty" (i.e. no series) or "freeroam".
     * 
     * Note that when setting the initial series to "freeroam" it is usually a good
     * idea to also disable 'freeroamRequiresCompletedSeries'.
     * 
     * default: "empty"
     */
    default String initialSeries() { return SeriesManager.EMPTY_SERIES_ID; }

    /**
     * The freeroam series will grant a level cap of 100 and allows players to pause
     * the progression of their current series. If this option is enabled, players must
     * have completed any other series first to gain access to the trade at the trainer
     * association.
     * 
     * default: true
     */
    default boolean freeroamRequiresCompletedSeries() { return true; }

    /**
     * If enabled additional information are printed to the log whenever a trainer
     * spawns or despawns.
     * 
     * default false
     */
    default boolean logSpawning() { return false; }

    ///////////////
    // CONSTANTS //
    ///////////////

    public static final int MIN_RELATIVE_LEVEL_CAP = -100;
    public static final int MAX_RELATIVE_LEVEL_CAP = 100;
    public static final float MIN_FORCE_BATTLE_MAX_DISTANCE = 0f;
    public static final float MAX_FORCE_BATTLE_MAX_DISTANCE = 32f;
    public static final int MIN_FORCE_BATTLE_LOOK_TICKS = 0;
    public static final int MIN_FORCE_BATTLE_MAX_LEVEL_DIFF = 0;
}
