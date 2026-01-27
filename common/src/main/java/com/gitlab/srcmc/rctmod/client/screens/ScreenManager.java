package com.gitlab.srcmc.rctmod.client.screens;

import net.minecraft.client.Minecraft;

public class ScreenManager implements IScreenManager {
    @Override
    public void openTrainerCardScreen() {
        Minecraft.getInstance().setScreen(new TrainerCardScreen());
    }
}
