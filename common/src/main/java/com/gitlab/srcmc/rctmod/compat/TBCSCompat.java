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
package com.gitlab.srcmc.rctmod.compat;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import com.gitlab.srcmc.rctapi.api.trainer.Trainer;
import com.gitlab.srcmc.rctmod.ModCommon;
import com.gitlab.srcmc.tbcs.commands.BattleEndCommand;
import com.gitlab.srcmc.tbcs.commands.TBCSCommands;
import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import dev.architectury.platform.Platform;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.LivingEntity;

public class TBCSCompat {
    private enum ModState {UNKNOWN, AVAILABLE, UNAVAILABLE};
    private static final String WIN_COMMANDS_CLASS = "com.gitlab.srcmc.tbcs.commands.BattleEndCommand$Map";
    private static final String TBCS_MOD_ID = "tbcs";
    private static final String TBCS_MIN_VERSION = "0.14.0-beta";
    private static ModState tbcsState = ModState.UNKNOWN;

    public static void registerWinCommands(Map<Integer, ?> map, MinecraftServer server, Map<Trainer, LivingEntity> trainerEntityMap, UUID battleId) {
        if(isTBCSAvailable() && map instanceof BattleEndCommand.Map commands) {
            TBCSCommands.getContext().registerWinCommands(ModCommon.RCT, server, trainerEntityMap, battleId, commands);
        }
    }
    
    private static boolean isTBCSAvailable() {
        if(TBCSCompat.tbcsState == ModState.UNKNOWN) {
            if(Platform.isModLoaded(TBCS_MOD_ID) && semgte(Platform.getMod(TBCS_MOD_ID).getVersion(), TBCS_MIN_VERSION)) {
                try {
                    Class.forName(WIN_COMMANDS_CLASS, false, TBCSCompat.class.getClassLoader());
                    TBCSCompat.tbcsState = ModState.AVAILABLE;
                    return true;
                } catch (ClassNotFoundException e) {
                    ModCommon.LOG.error(e.getMessage(), e);
                }
            }

            ModCommon.LOG.warn(String.format("%s mod (>= %s) not detected, trainer win commands will be ignored!", TBCS_MOD_ID, TBCS_MIN_VERSION));
            TBCSCompat.tbcsState = ModState.UNAVAILABLE;
        }

        return TBCSCompat.tbcsState == ModState.AVAILABLE;
    }

    private static boolean semgte(String ver1, String ver2) {
        // ignores any suffixes for now
        var parts1 = trimSuffix(ver1).split("\\.");
        var parts2 = trimSuffix(ver2).split("\\.");
        var n = Math.max(parts1.length, parts2.length);

        try {
            for(int i = 0; i < n; i++) {
                var p1 = parts1[i];
                var p2 = parts2[i];

                if(Integer.parseInt(p2) > Integer.parseInt(p1)) {
                    return false;
                }
            }
        } catch(NumberFormatException e) {
            ModCommon.LOG.error(e.getMessage(), e);
            return false;
        }

        return true;
    }

    private static String trimSuffix(String ver) {
        var s = ver.indexOf("-");

        if(s >= 0) {
            return ver.substring(0, s);
        }

        return ver;
    }

    public static class WinCommandsDeserializer implements JsonDeserializer<Map<Integer, ?>> {
        private static final Gson GSON = new Gson();

        private Function<JsonElement, Map<Integer, ?>> parse = json -> {
            this.updateParser();
            return this.parse.apply(json);
        };

        @Override
        public Map<Integer, ?> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return this.parse.apply(json);
        }

        private void updateParser() {
            if(isTBCSAvailable()) {
                this.parse = json -> BattleEndCommand.GSON.fromJson(json, BattleEndCommand.Map.class);
            } else {
                var type = TypeToken.getParameterized(Map.class, Integer.class, TypeToken.getArray(Object.class).getType()).getType();
                this.parse = json -> GSON.fromJson(json, type);
            }
        }
    }
}
