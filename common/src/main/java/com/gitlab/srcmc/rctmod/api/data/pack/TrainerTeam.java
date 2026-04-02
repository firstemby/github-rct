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

import java.util.List;
import java.util.UUID;

import com.cobblemon.mod.common.api.battles.model.ai.BattleAI;
import com.gitlab.srcmc.rctapi.api.ai.RCTBattleAI;
import com.gitlab.srcmc.rctapi.api.battle.BattleFormat;
import com.gitlab.srcmc.rctapi.api.battle.BattleRules;
import com.gitlab.srcmc.rctapi.api.models.BagItemModel;
import com.gitlab.srcmc.rctapi.api.models.PokemonModel;
import com.gitlab.srcmc.rctapi.api.models.TrainerModel;
import com.gitlab.srcmc.rctapi.api.util.JTO;

public class TrainerTeam extends TrainerModel {
    private static final long serialVersionUID = 0L;

    private String identity;
    private BattleFormat battleFormat;
    private BattleRules battleRules;

    public TrainerTeam() {
        this(null, "Trainer", BattleFormat.GEN_9_SINGLES, new BattleRules(), JTO.of(RCTBattleAI::new), List.of(), List.of());
    }

    public TrainerTeam(String identity, String name, BattleFormat battleFormat, BattleRules battleRules, JTO<BattleAI> ai, List<BagItemModel> bag, List<PokemonModel> team) {
        super(name, ai, bag, team);
        this.identity = identity;
        this.battleFormat = battleFormat;
        this.battleRules = battleRules;
    }

    public String getIdentity() {
        if(this.identity == null) {
            this.identity = this.getName().getLiteral() != null
                ? this.getName().getLiteral()
                : (this.getName().getTranslatable() != null
                    ? this.getName().getTranslatable()
                    : UUID.randomUUID().toString());
        }

        return this.identity;
    }

    public BattleFormat getBattleFormat() {
        return this.battleFormat;
    }

    public BattleRules getBattleRules() {
        return this.battleRules;
    }
}
