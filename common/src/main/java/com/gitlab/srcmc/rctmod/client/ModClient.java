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
package com.gitlab.srcmc.rctmod.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.spongepowered.include.com.google.common.io.Files;

import com.gitlab.srcmc.rctmod.ModCommon;
import com.gitlab.srcmc.rctmod.ModRegistries;
import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.gitlab.srcmc.rctmod.api.data.pack.DataPackManager;
import com.gitlab.srcmc.rctmod.api.data.sync.PlayerState;
import com.gitlab.srcmc.rctmod.api.utils.ArrUtils;
import com.gitlab.srcmc.rctmod.api.utils.LangKeys;
import com.gitlab.srcmc.rctmod.client.renderer.TargetArrowRenderer;
import com.gitlab.srcmc.rctmod.client.renderer.TrainerAssociationRenderer;
import com.gitlab.srcmc.rctmod.client.renderer.TrainerRenderer;
import com.gitlab.srcmc.rctmod.client.screens.IScreenManager;
import com.gitlab.srcmc.rctmod.client.screens.ScreenManager;
import com.gitlab.srcmc.rctmod.network.BatchedPayload;
import com.gitlab.srcmc.rctmod.network.BatchedPayloads;
import com.gitlab.srcmc.rctmod.network.TrainerTargetPayload;
import com.gitlab.srcmc.rctmod.network.BatchedPayload.Payload;
import com.gitlab.srcmc.rctmod.network.utils.FileData;

import dev.architectury.event.events.client.ClientPlayerEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.NetworkManager.PacketContext;
import dev.architectury.networking.NetworkManager.Side;
import dev.architectury.registry.ReloadListenerRegistry;
import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ModClient {
    public static final DataPackManager RESOURCE_MANAGER = new DataPackManager(PackType.CLIENT_RESOURCES);
    public static final IScreenManager SCREENS = new ScreenManager();

    public static void registerEventHandlers() {
        ClientTickEvent.CLIENT_LEVEL_POST.register(ModClient::onClientLevelTick);
        ClientPlayerEvent.CLIENT_PLAYER_JOIN.register(ModClient::onClientPlayerJoin);
    }

    public static void init() {
        var mc = Minecraft.getInstance();

        if(mc != null) {
            ModCommon.initPlayer(() -> mc.player);
            ReloadListenerRegistry.register(PackType.CLIENT_RESOURCES, RESOURCE_MANAGER);
        } else {
            ModCommon.LOG.warn("Local player could not be initialized. Minecraft instance is null.");
        }
        
        EntityRendererRegistry.register(ModRegistries.Entities.TRAINER, TrainerRenderer::new);
        EntityRendererRegistry.register(ModRegistries.Entities.TRAINER_ASSOCIATION, TrainerAssociationRenderer::new);
    }

    public static void setup() {
        NetworkManager.registerReceiver(Side.S2C, BatchedPayloads.PLAYER_STATE.TYPE, BatchedPayloads.PLAYER_STATE.CODEC, ModClient::receivePlayerState);
        NetworkManager.registerReceiver(Side.S2C, BatchedPayloads.TRAINER_MANAGER.TYPE, BatchedPayloads.TRAINER_MANAGER.CODEC, ModClient::receiveTrainerManager);
        NetworkManager.registerReceiver(Side.S2C, BatchedPayloads.FILE_DOWNLOAD.TYPE, BatchedPayloads.FILE_DOWNLOAD.CODEC, ModClient::receiveFileDownload);
        NetworkManager.registerReceiver(Side.S2C, TrainerTargetPayload.TYPE, TrainerTargetPayload.CODEC, ModClient::receiveTrainerTarget);
    }

    // ClientTickEvent

    static void onClientLevelTick(Level level) {
        var tm = RCTMod.getInstance().getTrainerManager();

        if(!ModClient.trainerManagerUpdates.isEmpty()) {
            tm.fromPayloads(ModClient.trainerManagerUpdates.poll());
        }

        if(!tm.isLoading() && !ModClient.playerStateUpdates.isEmpty()) {
            PlayerState.get(ModCommon.localPlayer()).deserializeUpdate(ModClient.playerStateUpdates.poll());
        }

        if(!ModClient.fileDownloadUpdates.isEmpty()) {
            saveFile(BatchedPayload.<FileData>from(ModClient.fileDownloadUpdates.poll()));
        }

        TargetArrowRenderer.getInstance().tick();
    }

    static void saveFile(FileData fileData) {
        var mc = Minecraft.getInstance();

        try {
            var filePath = mc.gameDirectory.toPath().resolve(fileData.filename());
            filePath.getParent().toFile().mkdirs();

            var file = filePath.toFile();
            Files.write(fileData.data(), file);

            mc.player.sendSystemMessage(Component.translatable(LangKeys.COMMANDS_FEEDBACK_FILE_SAVED)
                .append(" ")
                .append(Component.literal(file.getAbsolutePath()).setStyle(Style.EMPTY
                    .applyFormats(ChatFormatting.UNDERLINE, ChatFormatting.GREEN)
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable(LangKeys.COMMANDS_FEEDBACK_CLICK_FILE)))
                    .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, file.getAbsolutePath())))));
        } catch (IOException e) {
            ModCommon.LOG.error(e.getMessage(), e);
            mc.player.sendSystemMessage(Component.translatable(LangKeys.COMMANDS_ERRORS_FILE_SAVE).append(": " + e.getMessage()).withStyle(ChatFormatting.RED));
        }
    }

    // ClientPlayerEvent

    static void onClientPlayerJoin(LocalPlayer player) {
        PLAYER_STATE_PAYLOADS.clear();
        TRAINER_MANAGER_PAYLOADS.clear();
        FILE_DOWNLOAD_PAYLOADS.clear();
        RCTMod.getInstance().getTrainerManager().setLoading(true);
        PlayerState.initFor(player);
    }

    // NetworkManager

    private static final Queue<byte[]> playerStateUpdates = new ConcurrentLinkedDeque<>();
    private static final Queue<Payload[]> trainerManagerUpdates = new ConcurrentLinkedDeque<>();
    private static final Queue<Payload[]> fileDownloadUpdates = new ConcurrentLinkedDeque<>();

    static List<byte[]> PLAYER_STATE_PAYLOADS = new ArrayList<>();
    static List<Payload> TRAINER_MANAGER_PAYLOADS = new ArrayList<>();
    static List<Payload> FILE_DOWNLOAD_PAYLOADS = new ArrayList<>();
    
    static void receivePlayerState(BatchedPayload.Payload pl, PacketContext context) {
        PlayerState.get(ModCommon.localPlayer()).setLoading();
        PLAYER_STATE_PAYLOADS.add(pl.bytes());

        if(pl.remainingBatches() == 0) {
            if(!ModClient.playerStateUpdates.offer(ArrUtils.combine(PLAYER_STATE_PAYLOADS))) {
                ModCommon.LOG.error("Failed to store player state updates");
            }

            PLAYER_STATE_PAYLOADS.clear();
        }
    }

    static void receiveTrainerManager(BatchedPayload.Payload pl, PacketContext context) {
        RCTMod.getInstance().getTrainerManager().setLoading(true);
        TRAINER_MANAGER_PAYLOADS.add(pl);

        if(pl.remainingBatches() == 0) {
            if(!ModClient.trainerManagerUpdates.offer(TRAINER_MANAGER_PAYLOADS.toArray(new Payload[TRAINER_MANAGER_PAYLOADS.size()]))) {
                ModCommon.LOG.error("Failed to store trainer manager updates");
            }

            TRAINER_MANAGER_PAYLOADS.clear();
        }
    }
    static void receiveFileDownload(BatchedPayload.Payload pl, PacketContext context) {
        FILE_DOWNLOAD_PAYLOADS.add(pl);

        if(pl.remainingBatches() == 0) {
            if(!ModClient.fileDownloadUpdates.offer(FILE_DOWNLOAD_PAYLOADS.toArray(new Payload[FILE_DOWNLOAD_PAYLOADS.size()]))) {
                ModCommon.LOG.error("Failed to store file download updates");
            }

            FILE_DOWNLOAD_PAYLOADS.clear();
        }
    }

    static void receiveTrainerTarget(TrainerTargetPayload tt, PacketContext context) {
        TargetArrowRenderer.getInstance().setTarget(context.getPlayer(), new Vec3(tt.targetX(), tt.targetY(), tt.targetZ()), tt.otherDim());
    }
}
