package com.gitlab.srcmc.rctmod.mixins.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.gitlab.srcmc.rctmod.ModRegistries;
import com.gitlab.srcmc.rctmod.client.renderer.TargetArrowRenderer;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderStateShard.OutputStateShard;
import net.minecraft.client.renderer.RenderStateShard.ShaderStateShard;
import net.minecraft.client.renderer.RenderType.CompositeState;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

@Mixin(ItemRenderer.class)
public class ItemRendererMixin {
    private static final RenderType TRIANGLE_STRIP = RenderType.create(
        "trainer_card_arrow",
        DefaultVertexFormat.POSITION_COLOR,
        VertexFormat.Mode.TRIANGLE_STRIP,
        RenderType.SMALL_BUFFER_SIZE,
        CompositeState.builder()
            .setShaderState(ShaderStateShard.POSITION_COLOR_SHADER)
            .setTransparencyState(ShaderStateShard.TRANSLUCENT_TRANSPARENCY)
            .setOutputState(OutputStateShard.TRANSLUCENT_TARGET)
            .createCompositeState(false));

    @Inject(method = "render", at = @At("RETURN"), remap = true)
    public void onRender(ItemStack itemStack, ItemDisplayContext displayContext, boolean rotationReversed, PoseStack poseStack, MultiBufferSource bufferSource, int i1, int i2, BakedModel bakedModel, CallbackInfo ci) {
        if(displayContext.firstPerson() && itemStack.is(ModRegistries.Items.TRAINER_CARD.get())) {
            TargetArrowRenderer.getInstance().render(bufferSource.getBuffer(TRIANGLE_STRIP), poseStack, displayContext != ItemDisplayContext.FIRST_PERSON_RIGHT_HAND);
        }
    }
}
