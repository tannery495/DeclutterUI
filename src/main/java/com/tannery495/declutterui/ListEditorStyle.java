package com.tannery495.declutterui;

import com.tannery495.declutterui.mixin.ConfigurationListScreenAccessor;
import com.tannery495.declutterui.mixin.ConfigurationSectionScreenAccessor;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import org.jetbrains.annotations.Nullable;

/** Simpler list editors for DeclutterUI's phrase and tag lists: a text box and a remove button per row. */
public final class ListEditorStyle {
    private ListEditorStyle() {}

    /** Header tall enough for the title plus one line of help. */
    public static final int HEADER_HEIGHT = 48;
    private static final int HELP_COLOR = 0xFFA0A0A0;
    private static final int WARNING_COLOR = 0xFFE0A030;

    /** The config key of a DeclutterUI list editor (e.g. "hiddenChatPhrases"), or null for any other screen. */
    @Nullable
    public static String listKey(@Nullable Screen screen) {
        if (screen instanceof ConfigurationScreen.ConfigurationListScreen<?>
                && screen instanceof ConfigurationSectionScreenAccessor section
                && Declutter.MODID.equals(section.declutterui$getContext().modId())) {
            return ((ConfigurationListScreenAccessor) screen).declutterui$getKey();
        }
        return null;
    }

    public static Component addLabel(String key) {
        return Component.translatable("declutterui.configuration." + key + ".add");
    }

    public static boolean isRemoveButton(AbstractWidget widget) {
        return widget.getMessage().getContents() instanceof TranslatableContents tc
                && tc.getKey().equals("neoforge.configuration.uitext.listelementremove");
    }

    public static void renderHelp(GuiGraphics graphics, Font font, Screen screen, String key) {
        Component help = Component.translatable("declutterui.configuration." + key + ".help");
        graphics.drawCenteredString(font, help, screen.width / 2, 32, HELP_COLOR);
    }

    /** Phrases match anywhere in a message, so one or two characters (like "/") hide far more than expected. */
    public static void renderShortPhraseWarning(GuiGraphics graphics, Font font, String key, AbstractWidget control) {
        if (!key.equals("hiddenChatPhrases") || !(control instanceof EditBox box)) return;
        int length = box.getValue().strip().length();
        if (length == 0 || length > 2) return;
        Component warning = Component.translatable("declutterui.configuration.hiddenChatPhrases.tooShort");
        int x = box.getRight() - font.width(warning) - 6;
        graphics.drawString(font, warning, x, box.getY() + (box.getHeight() - 8) / 2, WARNING_COLOR);
    }
}
