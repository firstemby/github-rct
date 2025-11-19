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

import com.gitlab.srcmc.rctmod.api.RCTMod;

public class LevelUtils {
    public static int initialLevelCap(String seriesId) {
        var ilc = RCTMod.getInstance().getSeriesManager().getGraph(seriesId).getMetaData().initialLevelCap();
        return ilc.has() ? ilc.value() : RCTMod.getInstance().getServerConfig().initialLevelCap();
    }

    public static int relativeLevelCap(String seriesId) {
        return relativeLevelCap(seriesId, null);
    }

    public static int relativeLevelCap(String seriesId, String trainerId) {
        if(trainerId != null) {
            var rlc = RCTMod.getInstance().getTrainerManager().getData(trainerId).getRelativeLevelCap();

            if(rlc.has()) {
                return rlc.value();
            }
        }

        var rlc = RCTMod.getInstance().getSeriesManager().getGraph(seriesId).getMetaData().relativeLevelCap();
        return rlc.has() ? rlc.value() : RCTMod.getInstance().getServerConfig().relativeLevelCap();
    }

    public static int maxLevel() {
        return 100;
    }
}
