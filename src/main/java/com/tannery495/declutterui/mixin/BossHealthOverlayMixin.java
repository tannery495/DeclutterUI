package com.tannery495.declutterui.mixin;

import com.tannery495.declutterui.Config;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.BossHealthOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BossHealthOverlay.class)
public abstract class BossHealthOverlayMixin {

    @Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true)
    private void hideBossBars(GuiGraphicsExtractor graphics, CallbackInfo ci) {
        if (Config.HIDE_BOSS_BARS.get()) {
            ci.cancel();
        }
    }
}
