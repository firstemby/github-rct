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
package com.gitlab.srcmc.rctmod.api.data.pack;

import java.io.Serializable;
import java.util.List;

import com.gitlab.srcmc.rctapi.api.util.Text;
import com.gitlab.srcmc.rctmod.api.data.Optional;

public record SeriesMetaData(Text title, Text description, int difficulty, Optional<Integer> initialLevelCap, Optional<Integer> relativeLevelCap, List<List<String>> requiredSeries) implements Comparable<SeriesMetaData>, Serializable {
    private static final long serialVersionUID = 0L;
    public static final int MIN_DIFFICULTY = 1;
    public static final int MAX_DIFFICULTY = 10;

    public SeriesMetaData(Text title) {
        this(title, Text.empty(), MIN_DIFFICULTY + (MAX_DIFFICULTY - MIN_DIFFICULTY) / 2);
    }

    public SeriesMetaData(Text title, Text description) {
        this(title, description, MIN_DIFFICULTY + (MAX_DIFFICULTY - MIN_DIFFICULTY) / 2);
    }

    public SeriesMetaData(Text title, Text description, int difficulty) {
        this(title, description, difficulty, Optional.empty());
    }

    public SeriesMetaData(Text title, Text description, int difficulty, Optional<Integer> initialLevelCap) {
        this(title, description, difficulty, initialLevelCap, Optional.empty());
    }

    public SeriesMetaData(Text title, Text description, int difficulty, Optional<Integer> initialLevelCap, Optional<Integer> relativeLevelCap) {
        this(title, description, difficulty, initialLevelCap, relativeLevelCap, List.of());
    }

    public SeriesMetaData(Text title, Text description, int difficulty, Optional<Integer> initialLevelCap, Optional<Integer> relativeLevelCap, List<List<String>> requiredSeries) {
        this.title = title == null ? Text.empty() : title;
        this.description = description == null ? Text.empty() : description;
        this.difficulty = Math.max(MIN_DIFFICULTY, Math.min(MAX_DIFFICULTY, difficulty));
        this.initialLevelCap = initialLevelCap == null ? Optional.empty() : initialLevelCap;
        this.relativeLevelCap = relativeLevelCap == null ? Optional.empty() : relativeLevelCap;
        this.requiredSeries = requiredSeries;
    }

    @Override
    public int compareTo(SeriesMetaData other) {
        var c = Integer.compare(this.difficulty, other.difficulty);
        return c == 0 ? this.title.compareTo(other.title) : c;
    }

    public Optional<Integer> initialLevelCap() {
        if(this.initialLevelCap.has()) {
            var v = this.initialLevelCap.get();

            if(v < 0) { // TODO: constants for config ranges
                // TODO: This might spam the log! Come up with a caching strategy (switch to class?)
                // ModCommon.LOG.warn(String.format("invalid initialLevelCap for '%s': expected range is [1, 100] got %d", this.title, v));
                return Optional.of(1);
            } else if(v > 100) {
                // ModCommon.LOG.warn(String.format("invalid initialLevelCap for '%s': expected range is [1, 100] got %d", this.title, v));
                return Optional.of(100);
            }
        }
        
        return this.initialLevelCap;
    }
}
