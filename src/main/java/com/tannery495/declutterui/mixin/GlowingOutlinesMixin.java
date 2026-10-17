package com.tannery495.declutterui.mixin;

import com.tannery495.declutterui.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class GlowingOutlinesMixin {
    @Inject(method = "shouldEntityAppearGlowing", at = @At("HEAD"), cancellable = true)
    private void declutterui$hideGlowingOutlines(Entity entity, CallbackInfoReturnable<Boolean> ci) {
        // Only suppress local outlines; keep the entity and its server state unchanged.
        if (Config.HIDE_GLOWING_OUTLINES.get()) {
            ci.setReturnValue(false);
        }
    }
}
