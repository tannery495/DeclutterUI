package com.tannery495.declutterui;

import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;

import java.util.Locale;

/** Drops incoming chat messages that contain one of the user's hidden phrases. */
public final class ChatFilter {
    private ChatFilter() {}

    public static boolean shouldHide(ClientChatReceivedEvent event) {
        // Action bar text arrives as an overlay system message; it has its own option.
        if (event instanceof ClientChatReceivedEvent.System system && system.isOverlay()) return false;
        if (event instanceof ClientChatReceivedEvent.Player) {
            if (!Config.HIDE_MATCHING_PLAYER_MESSAGES.get()) return false;
            var player = Minecraft.getInstance().player;
            if (player != null && player.getUUID().equals(event.getSender())) return false;
        }
        return containsHiddenPhrase(event.getMessage().getString());
    }

    /** Case-insensitive substring match against the plain text, so colors and formatting don't matter. */
    public static boolean containsHiddenPhrase(String text) {
        String lower = text.toLowerCase(Locale.ROOT);
        for (String phrase : Config.HIDDEN_CHAT_PHRASES.get()) {
            String needle = phrase.strip().toLowerCase(Locale.ROOT);
            if (!needle.isEmpty() && lower.contains(needle)) return true;
        }
        return false;
    }
}
