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
package com.gitlab.srcmc.rctmod.api;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.cobblemon.mod.common.battles.BattleRegistry;
import com.gitlab.srcmc.rctapi.api.battle.BattleFormatProvider;
import com.gitlab.srcmc.rctapi.api.trainer.TrainerNPC;
import com.gitlab.srcmc.rctapi.api.trainer.TrainerPlayer;
import com.gitlab.srcmc.rctmod.ModCommon;
import com.gitlab.srcmc.rctmod.api.config.IClientConfig;
import com.gitlab.srcmc.rctmod.api.config.IServerConfig;
import com.gitlab.srcmc.rctmod.api.data.sync.PlayerState;
import com.gitlab.srcmc.rctmod.api.service.SeriesManager;
import com.gitlab.srcmc.rctmod.api.service.TrainerManager;
import com.gitlab.srcmc.rctmod.api.service.TrainerSpawner;
import com.gitlab.srcmc.rctmod.compat.TBCSCompat;
import com.gitlab.srcmc.rctmod.config.ClientConfig;
import com.gitlab.srcmc.rctmod.config.ServerConfig;
import com.gitlab.srcmc.rctmod.world.entities.TrainerMob;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public final class RCTMod {
    private static Supplier<RCTMod> instanceSupplier = () -> {
        var defaultInstance = new RCTMod(
            new TrainerManager(), new TrainerSpawner(),
            new ClientConfig(), new ServerConfig());

        RCTMod.instanceSupplier = () -> defaultInstance;
        return defaultInstance;
    };

    private TrainerManager trainerManager;
    private TrainerSpawner trainerSpawner;
    private IClientConfig clientConfig;
    private IServerConfig serverConfig;

    private RCTMod(
        TrainerManager trainerManager,
        TrainerSpawner trainerSpawner,
        IClientConfig clientConfig,
        IServerConfig serverConfig)
    {
        this.trainerManager = trainerManager;
        this.trainerSpawner = trainerSpawner;
        this.clientConfig = clientConfig;
        this.serverConfig = serverConfig;
    }

    public TrainerManager getTrainerManager() {
        return this.trainerManager;
    }

    public SeriesManager getSeriesManager() {
        return this.trainerManager.getSeriesManager();
    }

    public TrainerSpawner getTrainerSpawner() {
        return this.trainerSpawner;
    }

    public IClientConfig getClientConfig() {
        return this.clientConfig;
    }

    public IServerConfig getServerConfig() {
        return this.serverConfig;
    }

    public boolean makeBattle(TrainerMob mob, Player player) {
        var reg = ModCommon.RCT.getTrainerRegistry();

        try {
            var trPlayer = reg.getById(RCTMod.getInstance().getTrainerManager().getTrainerId(player), TrainerPlayer.class);
            var trNPC = reg.getById(mob.getTrainerId(), TrainerNPC.class);

            if(trPlayer == null) {
                ModCommon.LOG.warn("Failed to start battle: No trainer registered for player '" + player.getDisplayName().getString() + "'");
            } else if(trNPC == null) {
                ModCommon.LOG.warn("Failed to start battle: No trainer registered for mob '" + mob.getDisplayName().getString() + "'");
            } else {
                var trMob = RCTMod.getInstance().getTrainerManager().getData(mob);
                var team = trMob.getTrainerTeam();
                trNPC.setEntity(mob); // safety measure to prevent a battle against entities in unloaded chunks
                var battleId = ModCommon.RCT.getBattleManager().startBattle(List.of(trPlayer), List.of(trNPC), (BattleFormatProvider)team.getBattleFormat(), team.getBattleRules());

                if(battleId != null) {
                    TBCSCompat.registerWinCommands(trMob.getWinCommands(PlayerState.get(player).getCurrentSeries()), player.getServer(), Map.of(trPlayer, player, trNPC, mob), battleId);
                    return true;
                }

                return false;
            }
        } catch(IllegalArgumentException e) {
            ModCommon.LOG.error("Failed to start battle", e);
        }

        return false;
    }

    public void stopBattle(TrainerMob mob) {
        var opp = (ServerPlayer)mob.getOpponent();

        if(opp != null) {
            var battle = BattleRegistry.getBattleByParticipatingPlayer(opp);

            if(battle == null) {
                ModCommon.LOG.warn(String.format("Player '%s' is not in a battle", opp.getDisplayName().getString()));
            } else {
                battle.stop();
            }
        }
    }

    public boolean isInBattle(Player player) {
        return BattleRegistry.getBattleByParticipatingPlayerId(player.getUUID()) != null;
    }

    public static RCTMod getInstance() {
        return RCTMod.instanceSupplier.get();
    }
}
