package com.tannery495.declutterui.mixin;

import com.tannery495.declutterui.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiGraphics.class)
public abstract class PlayerProfilePopupMixin {
    @Inject(method = "renderComponentHoverEffect", at = @At("HEAD"), cancellable = true)
    private void hidePlayerProfilePopup(Font font, Style style, int x, int y, CallbackInfo ci) {
        if (!Config.HIDE_PLAYER_PROFILE_POPUPS.get()
                || !(Minecraft.getInstance().screen instanceof ChatScreen) || style == null) return;
        HoverEvent hover = style.getHoverEvent();
        if (hover == null) return;
        var entity = hover.getValue(HoverEvent.Action.SHOW_ENTITY);
        if (entity != null && entity.type == EntityType.PLAYER) {
            ci.cancel();
        } else if (hover.getAction() == HoverEvent.Action.SHOW_TEXT) {
            // Hide only the tooltip; preserve the server's name-click action.
            ClickEvent click = style.getClickEvent();
            if (click != null && (click.getAction() == ClickEvent.Action.SUGGEST_COMMAND
                    || click.getAction() == ClickEvent.Action.RUN_COMMAND)
                    && click.getValue().strip().matches("(?i)^/(?:[a-z0-9_.-]+:)?(?:msg|tell|w|whisper|message|pm)\\s+\\S+(?:\\s.*)?$")) {
                ci.cancel();
            }
        }
    }
}
