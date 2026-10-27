package com.tannery495.declutterui;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Removes the user's hidden rank tags (e.g. "OWNER", "[VIP]") from chat lines and name tags, keeping styles. */
public final class ChatTags {
    private ChatTags() {}

    /** Where the sender part of a chat line ends: "Name: msg", "Name » msg", "<Name> msg", ... */
    private static final String SEPARATORS = ":»>›|➤→";
    private static final String OPEN_BRACKETS = "[(<{";
    private static final String CLOSE_BRACKETS = "])>}";
    private static final String BOUNDARIES = "[](){}<>|:»›.,-*•·";
    private static final String SMALL_CAPS = "ᴀʙᴄᴅᴇꜰɢʜɪᴊᴋʟᴍɴᴏᴘǫʀꜱᴛᴜᴠᴡxʏᴢ";

    private record Segment(Style style, int start, String text) {}

    /**
     * Returns {@code message} without hidden tags, or {@code null} if nothing was removed.
     * With {@code senderOnly}, only text before the first separator is touched, so words in the
     * message body stay; lines without a separator (join messages, name tags) are checked whole.
     */
    @Nullable
    public static Component strip(Component message, boolean senderOnly) {
        List<? extends String> tags = Config.HIDDEN_CHAT_TAGS.get();
        if (tags.isEmpty()) return null;

        List<Segment> segments = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        message.visit((style, part) -> {
            segments.add(new Segment(style, builder.length(), part));
            builder.append(part);
            return Optional.empty();
        }, Style.EMPTY);
        String plain = builder.toString();
        String folded = fold(plain);
        int regionEnd = senderOnly ? senderEnd(plain) : plain.length();

        boolean[] removed = new boolean[plain.length()];
        boolean changed = false;
        for (String tag : tags) {
            String needle = fold(tag.strip());
            if (needle.isEmpty()) continue;
            for (int at = folded.indexOf(needle); at >= 0 && at + needle.length() <= regionEnd;
                 at = folded.indexOf(needle, at + 1)) {
                int start = at;
                int end = at + needle.length();
                if (!isBoundary(folded, start - 1) || !isBoundary(folded, end)) continue;
                // "VIP" listed, "[VIP]" shown: take the brackets too.
                if (start > 0 && end < plain.length()) {
                    int open = OPEN_BRACKETS.indexOf(plain.charAt(start - 1));
                    if (open >= 0 && open == CLOSE_BRACKETS.indexOf(plain.charAt(end))) {
                        start--;
                        end++;
                    }
                }
                // Take one neighbouring space so "OWNER Lover" becomes "Lover", not " Lover".
                if (end < plain.length() && plain.charAt(end) == ' ') {
                    end++;
                } else if (start > 0 && plain.charAt(start - 1) == ' ') {
                    start--;
                }
                for (int i = start; i < end; i++) removed[i] = true;
                changed = true;
            }
        }
        if (!changed) return null;

        MutableComponent result = Component.empty();
        for (Segment segment : segments) {
            StringBuilder kept = new StringBuilder();
            for (int i = 0; i < segment.text().length(); i++) {
                if (!removed[segment.start() + i]) kept.append(segment.text().charAt(i));
            }
            if (!kept.isEmpty()) result.append(Component.literal(kept.toString()).withStyle(segment.style()));
        }
        return result;
    }

    private static int senderEnd(String text) {
        for (int i = 0; i < text.length(); i++) {
            if (SEPARATORS.indexOf(text.charAt(i)) >= 0) return i;
        }
        return text.length();
    }

    private static boolean isBoundary(String text, int index) {
        if (index < 0 || index >= text.length()) return true;
        char c = text.charAt(index);
        return Character.isWhitespace(c) || BOUNDARIES.indexOf(c) >= 0;
    }

    /**
     * Lower-cases and maps small-caps and full-width letters to plain ones, one char for one char,
     * so "ᴏᴡɴᴇʀ" and "ＯＷＮＥＲ" match "OWNER" and indexes still line up with the original text.
     */
    static String fold(String text) {
        StringBuilder out = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            int smallCap = SMALL_CAPS.indexOf(c);
            if (smallCap >= 0) {
                c = (char) ('a' + smallCap);
            } else if (c >= '！' && c <= '～') {
                c = (char) (c - 0xFEE0);
            }
            out.append(Character.toLowerCase(c));
        }
        return out.toString();
    }
}
