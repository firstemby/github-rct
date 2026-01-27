package com.gitlab.srcmc.rctmod.network;

public final class BatchedPayloads {
    public static final BatchedPayload PLAYER_STATE = BatchedPayload.create("player_state");
    public static final BatchedPayload TRAINER_MANAGER = BatchedPayload.create("trainer_manager");

    private BatchedPayloads() {
    }
}
