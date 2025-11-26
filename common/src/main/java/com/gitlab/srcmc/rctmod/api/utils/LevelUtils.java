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
package com.gitlab.srcmc.rctmod.api.utils;

import java.util.Set;

import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.gitlab.srcmc.rctmod.api.data.pack.TrainerMobData;
import com.gitlab.srcmc.rctmod.api.data.sync.PlayerState;
import com.gitlab.srcmc.rctmod.api.service.SeriesManager;

import net.minecraft.world.entity.player.Player;

public class LevelUtils {
    public static int initialLevelCap(String seriesId) {
        var ilc = RCTMod.getInstance().getSeriesManager().getGraph(seriesId).getMetaData().initialLevelCap();
        return ilc.has() ? ilc.get() : RCTMod.getInstance().getServerConfig().initialLevelCap();
    }

    public static int relativeLevelCap(String seriesId, String trainerId) {
        if(trainerId != null) {
            var rlc = RCTMod.getInstance().getTrainerManager().getData(trainerId).getRelativeLevelCap();

            if(rlc.has()) {
                return rlc.get();
            }
        }

        var rlc = RCTMod.getInstance().getSeriesManager().getGraph(seriesId).getMetaData().relativeLevelCap();
        return rlc.has() ? rlc.get() : RCTMod.getInstance().getServerConfig().relativeLevelCap();
    }

    public static int levelCap(Player player) {
        var sm = RCTMod.getInstance().getSeriesManager();
        var ps = PlayerState.get(player);
        var sid = ps.getCurrentSeries();
        var sg = sm.getGraph(sid).getNext(ps.getDefeatedTrainerIds());
        var lc = sg.stream()
            .map(tn -> trainerLevel(sid, tn.id()))
            .min(Integer::compare)
            .orElse(SeriesManager.EMPTY_SERIES_ID.equals(sid) ? 1 : LevelUtils.maxLevel());

        return Math.max(initialLevelCap(sid), lc);
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
        return Math.max(
            Math.min(maxLevel(), Math.max(0, trainerLevel(tmd) + relativeLevelCap(seriesId, trainerId))),
            tmd.getRequiredDefeats(seriesId)
                .stream().flatMap(Set::stream)
                .map(tid -> trainerLevel(seriesId, tid)).max(Integer::compare).orElse(0));
    }

    public static int maxLevel() {
        return 100; // TODO: > 100 requires updated version of rctapi
    }
}
