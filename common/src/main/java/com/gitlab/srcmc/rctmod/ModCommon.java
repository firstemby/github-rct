package com.gitlab.srcmc.rctmod;

import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gitlab.srcmc.rctapi.api.RCTApi;
import com.gitlab.srcmc.rctmod.server.ModServer;
import net.minecraft.world.entity.player.Player;

public class ModCommon {
    public static final String MOD_ID = "rctmod";
    public static final String MOD_NAME = "Radical Cobblemon Trainers";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);
    public static final RCTApi RCT = RCTApi.initInstance(MOD_ID);
    public static Supplier<Player> player;

    public static void init() {
        ModServer.registerEventHandlers();
    }

    // call on client to supply Minecraft.getInstance().player
    public static void initPlayer(Supplier<Player> player) {
        ModCommon.player = player;
    }

    public static Player localPlayer() {
        if(ModCommon.player == null) {
            throw new IllegalStateException("Local player not initialized, call ModCommon.initPlayer on the client side");
        }

        return ModCommon.player.get();
    }
}
