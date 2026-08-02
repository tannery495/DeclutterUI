package com.tannery495.declutterui.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.tannery495.declutterui.Config;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.DisplayRenderer;
import net.minecraft.world.entity.Display;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DisplayRenderer.TextDisplayRenderer.class)
public abstract class TextDisplayRendererMixin {
    @Inject(method = "renderInner", at = @At("HEAD"), cancellable = true)
    private void declutterui$hideTextDisplay(Display.TextDisplay entity,
            Display.TextDisplay.TextRenderState state, PoseStack poseStack,
            MultiBufferSource buffer, int light, float partialTick, CallbackInfo ci) {
        if (Config.HIDE_FLOATING_HOLOGRAM_TEXT.get()) ci.cancel();
    }
}
