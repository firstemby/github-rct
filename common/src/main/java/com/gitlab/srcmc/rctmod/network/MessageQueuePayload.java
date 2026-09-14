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
package com.gitlab.srcmc.rctmod.network;

import java.util.ArrayList;
import java.util.Collection;
import com.gitlab.srcmc.rctmod.ModCommon;
import com.gitlab.srcmc.rctmod.world.entities.TrainerMob.Message;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record MessageQueuePayload(int entityId, Collection<Message> messages) implements CustomPacketPayload {
    public static final Type<MessageQueuePayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ModCommon.MOD_ID, "target_trainer"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MessageQueuePayload> CODEC = StreamCodec.of(
        (b, v) -> {
            b.writeInt(v.entityId);
            b.writeInt(v.messages.size());

            for(var message : v.messages) {
                b.writeUtf(message.langKey());
                b.writeLong(message.age());
            }
        },
        b -> {
            var entityId = b.readInt();
            var count = b.readInt();
            var messages = new ArrayList<Message>(count);

            for(int i = 0; i < count; i++) {
                messages.add(new Message(b.readUtf(), b.readLong()));
            }

            return new MessageQueuePayload(entityId, messages);
        }
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return MessageQueuePayload.TYPE;
    }
}
