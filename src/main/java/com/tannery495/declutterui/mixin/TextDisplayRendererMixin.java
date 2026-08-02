package com.tannery495.declutterui.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.tannery495.declutterui.Config;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.DisplayRenderer;
import net.minecraft.client.renderer.entity.state.TextDisplayEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DisplayRenderer.TextDisplayRenderer.class)
public abstract class TextDisplayRendererMixin {
    @Inject(method = "submitInner", at = @At("HEAD"), cancellable = true)
    private void declutterui$hideTextDisplay(TextDisplayEntityRenderState state,
            PoseStack poseStack, SubmitNodeCollector collector, int light,
            float interpolationProgress, CallbackInfo ci) {
        if (Config.HIDE_FLOATING_HOLOGRAM_TEXT.get()) ci.cancel();
    }
}
