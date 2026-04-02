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
package com.gitlab.srcmc.rctmod.client.renderer;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import com.gitlab.srcmc.rctmod.world.items.TrainerCard;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class TargetArrowRenderer {
    private static final float[] PYRAMID_STRIP = {
         0, -1, -1,
         0,  1,  0,
         1, -1,  0,
         0, -1,  1,
        -1, -1,  0,
         0,  1,  0,
         0, -1, -1,
         1, -1,  0,
        -1, -1,  0,
    };

    private static final float[] PYRAMID_COLORS = {
        0.0f, 0.0f, 0.0f,
        1.0f, 0.0f, 0.0f, // top
        0.0f, 0.0f, 0.0f,
        0.0f, 0.0f, 0.0f,
        0.0f, 0.0f, 0.0f,
        1.0f, 0.0f, 0.0f, // top
        0.0f, 0.0f, 0.0f,
        0.0f, 0.0f, 0.0f,
        0.0f, 0.0f, 0.0f,
    };

    private static final float PYRAMID_ALPHA = 0.75f;
    private static final Vector3f PYRAMID_UP = new Vector3f(0, 1, 0);
    private static final int TICKS_TO_ACTIVATE = 40;

    public static double[] TX = {-0.25, -0.15}, TY = {0.15, 0.25}, TZ = {0, 0};
    public static float SX = 0.015f, SY = 0.045f, SZ = 0.03f;

    private Vector3f direction = new Vector3f();
    private Vector3f[] partialDirection = {new Vector3f(), new Vector3f()}; // left, right
    private Quaternionf[] rotation = {new Quaternionf(), new Quaternionf()};
    private Quaternionf[] localY = {new Quaternionf(), new Quaternionf()};

    private int activationTicks, ticks;
    private Entity source;
    private Vec3 target;
    private boolean otherDim;
    private int activeTicks;

    private static final TargetArrowRenderer INSTANCE = new TargetArrowRenderer();

    public static TargetArrowRenderer getInstance() {
        return INSTANCE;
    }

    public void tick() {
        if(this.hasTarget()) {
            this.updateDirection();

            if(this.activationTicks < TICKS_TO_ACTIVATE) {
                this.activationTicks++;
            }

            if(--this.activeTicks < 0) {
                this.setTarget(null, null, false);
            }
        } else {
            if(this.activationTicks > 0) {
                this.activationTicks--;
            }
        }

        if(++this.ticks < 0) {
            this.ticks = 0;
        }
    }

    public void render(VertexConsumer buffer, PoseStack poseStack, boolean lefthand) {
        if(this.activationTicks > 0) {
            var mc = Minecraft.getInstance();
            var cam = mc.gameRenderer.getMainCamera();
            
            if(cam.isInitialized()) {
                var partialTick = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false);
                var t = Math.min(this.activationTicks + partialTick, TICKS_TO_ACTIVATE)/TICKS_TO_ACTIVATE;
                var ti = lefthand ? 0 : 1;

                this.partialDirection[ti].lerp(this.direction, Math.min(0.05f, 0.2f*partialTick));
                PYRAMID_UP.rotationTo(this.partialDirection[ti], this.rotation[ti]);

                poseStack.pushPose();
                poseStack.translate(TX[ti], TY[ti], TZ[ti]);
                poseStack.mulPose(cam.rotation().difference(this.rotation[ti], this.rotation[ti]));
                poseStack.mulPose(localY[ti].identity().rotateLocalY((float)Math.PI*(this.ticks + partialTick)/TICKS_TO_ACTIVATE));
                poseStack.scale(SX * t, (float)(SY + Math.sin((this.ticks + partialTick)/10)*0.01) * t, SZ * t);
                poseStack.mulPose(localY[ti].identity().rotateLocalY((float)(Math.PI/4)));
                var m = poseStack.last().pose();

                for(int i = 0; i < PYRAMID_STRIP.length; i += 3) {
                    buffer
                        .addVertex(m, PYRAMID_STRIP[i], PYRAMID_STRIP[i + 1], PYRAMID_STRIP[i + 2])
                        .setColor(PYRAMID_COLORS[i], PYRAMID_COLORS[i + 1], PYRAMID_COLORS[i + 2], PYRAMID_ALPHA * t);
                }

                poseStack.popPose();
            }
        }
    }
    
    public void setTarget(Entity source, Vec3 target, boolean otherDim) {
        this.source = source;
        this.target = target;
        this.otherDim = otherDim;
        this.activeTicks = 4*TrainerCard.SYNC_INTERVAL_TICKS;
    }

    public boolean hasTarget() {
        return this.source != null && this.target != null;
    }

    private void updateDirection() {
        if(!this.otherDim) {
            this.direction.lerp(this.target.subtract(this.source.position()).toVector3f(), 0.2f);
        } else {
            var t = Math.PI * this.ticks / 20.0;
            this.direction.lerp(new Vector3f((float)Math.sin(t), (float)Math.cos(t), 0), 0.05f);
        }
    }
}
