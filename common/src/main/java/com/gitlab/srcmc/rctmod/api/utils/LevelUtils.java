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
package com.gitlab.srcmc.rctmod.api.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.gitlab.srcmc.rctmod.api.data.pack.TrainerMobData;
import com.gitlab.srcmc.rctmod.api.data.sync.PlayerState;
import com.gitlab.srcmc.rctmod.api.service.SeriesManager;
import net.minecraft.world.entity.player.Player;

public class LevelUtils {
    private static final Map<UUID, Map.Entry<PlayerSeriesState, Integer>> PLAYER_LEVEL_CAP_CACHE = new HashMap<>(); // TODO: clear after server reload
    private static final Map<TrainerSeriesState, Integer> TRAINER_LEVEL_CACHE = new HashMap<>(); // TODO: clear after server reload

    public static int initialLevelCap(String seriesId) {
        var ilc = RCTMod.getInstance().getSeriesManager().getGraph(seriesId).getMetaData().initialLevelCap();
        return ilc.has() ? ilc.get() : RCTMod.getInstance().getServerConfig().initialLevelCap();
    }

    public static int relativeLevelCap(String seriesId, String trainerId) {
        if(trainerId != null) {
            var rlc = RCTMod.getInstance().getTrainerManager().getData(trainerId).getRelativeLevelCap(seriesId);

            if(rlc.has()) {
                return rlc.get();
            }
        }

        var rlc = RCTMod.getInstance().getSeriesManager().getGraph(seriesId).getMetaData().relativeLevelCap();
        return rlc.has() ? rlc.get() : RCTMod.getInstance().getServerConfig().relativeLevelCap();
    }

    static PlayerSeriesState prevState;

    public static int levelCap(Player player) {
        var sm = RCTMod.getInstance().getSeriesManager();
        var ps = PlayerState.get(player);

        if(ps.isLoading()) {
            return 0;
        }

        var sid = ps.getCurrentSeries();
        var plState = new PlayerSeriesState(sid, Set.copyOf(ps.getDefeatedTrainerIds()));

        return PLAYER_LEVEL_CAP_CACHE.compute(player.getUUID(), (key, entry) -> {
            if(entry != null && entry.getKey().equals(plState)) {                
                return entry;
            }

            var sg = sm.getGraph(sid).getNext(ps.getDefeatedTrainerIds());
            var lc = Math.max(initialLevelCap(sid), sg.stream()
                .map(tn -> trainerLevel(sid, tn.id()))
                .min(Integer::compare)
                .orElse(SeriesManager.EMPTY_SERIES_ID.equals(sid) ? 1 : LevelUtils.maxLevel()));

            return Map.entry(plState, lc);
        }).getValue();
    }

    public static int levelCap(String seriesId, String trainerId) {
        return Math.max(initialLevelCap(seriesId), trainerLevel(seriesId, trainerId));
    }

    public static int trainerLevel(String seriesId, String trainerId) {
        return trainerLevel(seriesId, trainerId, RCTMod.getInstance().getTrainerManager().getData(trainerId));
    }

    public static int trainerLevel(TrainerMobData tmd) {
        return tmd.getTrainerTeam().getTeam().stream().map(p -> p.getLevel()).max(Integer::compare).orElse(0);
    }

    private static int trainerLevel(String seriesId, String trainerId, TrainerMobData tmd) {
        var trState = new TrainerSeriesState(seriesId, trainerId);
        var level = TRAINER_LEVEL_CACHE.get(trState);

        if(level == null) {
            level = Math.max(
                Math.min(maxLevel(), Math.max(0, trainerLevel(tmd) + relativeLevelCap(seriesId, trainerId))),
                tmd.getRequiredDefeats(seriesId)
                    .stream().flatMap(Set::stream)
                    .map(tid -> trainerLevel(seriesId, tid)).max(Integer::compare).orElse(0));

            TRAINER_LEVEL_CACHE.put(trState, level);
        }
        
        return level;
    }

    public static int maxLevel() {
        return 100; // TODO: > 100 requires updated version of rctapi
    }

    private static record PlayerSeriesState(String seriesId, Set<String> defeatedTrainers) {
        @Override
        public boolean equals(Object o) {
            return (o instanceof PlayerSeriesState other)
                && Objects.equals(this.seriesId, other.seriesId)
                && (this.defeatedTrainers == other.defeatedTrainers
                || (this.defeatedTrainers != null && other.defeatedTrainers != null
                    && this.defeatedTrainers.size() == other.defeatedTrainers.size()
                    && this.defeatedTrainers.stream().allMatch(other.defeatedTrainers::contains)));
        }
    }

    private static record TrainerSeriesState(String seriesId, String trainerId) {
    }
}
