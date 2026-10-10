package com.tannery495.declutterui.mixin;

import com.tannery495.declutterui.Config;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlayerTabOverlay.class)
public abstract class PlayerTabOverlayMixin {
    @Shadow private Component header;
    @Shadow private Component footer;

    // Mask reads rather than erasing server data, so toggling the option restores it immediately.
    @Redirect(method = "render", at = @At(value = "FIELD",
            target = "Lnet/minecraft/client/gui/components/PlayerTabOverlay;header:Lnet/minecraft/network/chat/Component;"))
    private Component hideHeader(PlayerTabOverlay overlay) {
        return Config.HIDE_TAB_HEADER_FOOTER.get() ? null : header;
    }

    @Redirect(method = "render", at = @At(value = "FIELD",
            target = "Lnet/minecraft/client/gui/components/PlayerTabOverlay;footer:Lnet/minecraft/network/chat/Component;"))
    private Component hideFooter(PlayerTabOverlay overlay) {
        return Config.HIDE_TAB_HEADER_FOOTER.get() ? null : footer;
    }

}
