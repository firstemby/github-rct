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
package com.gitlab.srcmc.rctmod.api.data;

import java.util.Collections;
import java.util.List;

import com.gitlab.srcmc.rctmod.ModRegistries;
import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.gitlab.srcmc.rctmod.world.entities.TrainerMob;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class TrainerBattle {
    private List<Player> initiatorSidePlayers;
    private List<TrainerMob> initiatorSideMobs;
    private List<Player> trainerSidePlayers;
    private List<TrainerMob> trainerSideMobs;

    public TrainerBattle(Player initiator, TrainerMob opponent) {
        this(new Player[]{initiator}, new TrainerMob[]{}, new Player[]{}, new TrainerMob[]{opponent});
    }

    public TrainerBattle(Player[] initiatorSidePlayers, TrainerMob[] initiatorSideMobs, Player[] trainerSidePlayers, TrainerMob[] trainerSideMobs) {
        if(initiatorSidePlayers.length == 0) {
            throw new UnsupportedOperationException("battle must have atleast 1 initiator player");
        }

        if(trainerSidePlayers.length == 0 && trainerSideMobs.length == 0) {
            throw new UnsupportedOperationException("battle must have atleast 1 trainer mob or player opponent");
        }

        this.initiatorSidePlayers = List.of(initiatorSidePlayers);
        this.initiatorSideMobs = List.of(initiatorSideMobs);
        this.trainerSidePlayers = List.of(trainerSidePlayers);
        this.trainerSideMobs = List.of(trainerSideMobs);
    }

    public Player getInitiator() {
        return this.initiatorSidePlayers.get(0);
    }

    public List<Player> getInitiatorSidePlayers() {
        return Collections.unmodifiableList(this.initiatorSidePlayers);
    }

    public List<TrainerMob> getInitiatorSideMobs() {
        return Collections.unmodifiableList(this.initiatorSideMobs);
    }

    public List<Player> getTrainerSidePlayers() {
        return Collections.unmodifiableList(this.trainerSidePlayers);
    }

    public List<TrainerMob> getTrainerSideMobs() {
        return Collections.unmodifiableList(this.trainerSideMobs);
    }

    public void distributeRewards(boolean initiatorWins) {
        var winnerPlayers = initiatorWins ? initiatorSidePlayers : trainerSidePlayers;
        var looserMobs = initiatorWins ? trainerSideMobs : initiatorSideMobs;
        var tm = RCTMod.getInstance().getTrainerManager();

        for(var player : winnerPlayers) {
            for(var mob : looserMobs) {
                var tpd = tm.getData(player);
                var btm = tm.getBattleMemory(mob);
                tpd.addProgressDefeat(mob.getTrainerId());
                btm.addDefeatedBy(mob.getTrainerId(), player);
                ModRegistries.CriteriaTriggers.DEFEAT_COUNT.get().trigger((ServerPlayer)player, mob);
            }
        }

        for(var mob : looserMobs) {
            mob.finishBattle(this, true);
        }

        for(var mob : initiatorWins ? initiatorSideMobs : trainerSideMobs) {
            mob.finishBattle(this, false);
        }
    }
}
