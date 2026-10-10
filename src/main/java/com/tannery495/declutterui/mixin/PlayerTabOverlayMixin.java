package com.tannery495.declutterui.mixin;

import com.tannery495.declutterui.Config;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.level.GameType;
import net.minecraft.world.scores.PlayerTeam;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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

    @Inject(method = "getNameForDisplay", at = @At("HEAD"), cancellable = true)
    private void formatName(PlayerInfo info, CallbackInfoReturnable<Component> ci) {
        boolean hideRanks = Config.HIDE_TAB_RANK_TAGS.get();
        boolean accountNames = Config.SHOW_TAB_ACCOUNT_NAMES.get();
        if (!hideRanks && !accountNames) return;
        PlayerTeam team = info.getTeam();
        MutableComponent name;
        if (accountNames || info.getTabListDisplayName() == null) {
            name = Component.literal(info.getProfile().getName());
            if (!hideRanks) name = PlayerTeam.formatNameForTeam(team, name);
            else if (team != null) name.withStyle(team.getColor());
        } else {
            Component display = info.getTabListDisplayName();
            String text = display.getString();
            String prefix = team == null ? "" : team.getPlayerPrefix().getString();
            String suffix = team == null ? "" : team.getPlayerSuffix().getString();
            int start = !prefix.isEmpty() && text.startsWith(prefix) ? prefix.length() : 0;
            int end = !suffix.isEmpty() && text.endsWith(suffix) ? text.length() - suffix.length() : text.length();
            name = end > start ? declutterui$slice(display, start, end) : display.copy();
        }
        if (info.getGameMode() == GameType.SPECTATOR) name.withStyle(ChatFormatting.ITALIC);
        ci.setReturnValue(name);
    }

    @Unique
    private static MutableComponent declutterui$slice(Component original, int start, int end) {
        MutableComponent result = Component.empty();
        int[] offset = {0};
        original.visit((style, text) -> {
            int from = Math.max(0, start - offset[0]);
            int to = Math.min(text.length(), end - offset[0]);
            if (from < to) result.append(Component.literal(text.substring(from, to)).setStyle(style));
            offset[0] += text.length();
            return Optional.<Void>empty();
        }, Style.EMPTY);
        return result;
    }
}
