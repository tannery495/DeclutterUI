package com.tannery495.declutterui.mixin;

import com.tannery495.declutterui.Config;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.numbers.NumberFormat;
import net.minecraft.world.scores.PlayerScoreEntry;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiMixin {

    @Inject(method = "maybeRenderSelectedItemName", at = @At("HEAD"), cancellable = true)
    private void hideSelectedItemName(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (Config.HIDE_SELECTED_ITEM_NAME.get()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderScoreboardSidebar", at = @At("HEAD"), cancellable = true)
    private void hideScoreboardSidebar(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (Config.HIDE_SCOREBOARD_SIDEBAR.get()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderOverlayMessage", at = @At("HEAD"), cancellable = true)
    private void hideActionBarMessages(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (Config.HIDE_ACTION_BAR_MESSAGES.get()) {
            ci.cancel();
        }
    }

    // An empty score has zero width, so the sidebar also shrinks to fit the remaining text.
    @Redirect(method = "lambda$displayScoreboardSidebar$12", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/scores/PlayerScoreEntry;formatValue(Lnet/minecraft/network/chat/numbers/NumberFormat;)Lnet/minecraft/network/chat/MutableComponent;"))
    private net.minecraft.network.chat.MutableComponent hideScoreboardNumbers(PlayerScoreEntry entry, NumberFormat format) {
        return Config.HIDE_SCOREBOARD_NUMBERS.get() ? Component.empty() : entry.formatValue(format);
    }

    @Inject(method = "renderTitle", at = @At("HEAD"), cancellable = true)
    private void hideTitles(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (Config.HIDE_TITLES.get()) {
            ci.cancel();
        }
    }
}
