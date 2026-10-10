package com.tannery495.declutterui.mixin;

import com.tannery495.declutterui.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class PlayerGlowMixin {
    @Inject(method = "shouldEntityAppearGlowing", at = @At("HEAD"), cancellable = true)
    private void hidePlayerGlow(Entity entity, CallbackInfoReturnable<Boolean> ci) {
        // Change only the local rendering decision, leaving entity and server state intact.
        if (entity instanceof Player && Config.HIDE_PLAYER_GLOW_OUTLINES.get()) {
            ci.setReturnValue(false);
        }
    }
}
