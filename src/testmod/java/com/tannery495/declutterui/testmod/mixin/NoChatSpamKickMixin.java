package com.tannery495.declutterui.testmod.mixin;

import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** The autotest sends commands faster than the vanilla chat spam limit allows; never kick for it on the test server. */
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class NoChatSpamKickMixin {
    @Inject(method = "detectRateSpam", at = @At("HEAD"), cancellable = true)
    private void declutteruitest$skipSpamCheck(CallbackInfo ci) {
        ci.cancel();
    }
}
