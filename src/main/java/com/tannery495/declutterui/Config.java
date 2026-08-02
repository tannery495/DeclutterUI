package com.tannery495.declutterui;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue HIDE_REALMS;
    public static final ModConfigSpec.BooleanValue HIDE_ACCESSIBILITY_BUTTON;
    public static final ModConfigSpec.BooleanValue HIDE_LANGUAGE_BUTTON;
    public static final ModConfigSpec.BooleanValue HIDE_SPLASH_TEXT;
    public static final ModConfigSpec.BooleanValue HIDE_COPYRIGHT;
    public static final ModConfigSpec.BooleanValue HIDE_VERSION_TEXT;

    public static final ModConfigSpec.BooleanValue HIDE_CREDITS;
    public static final ModConfigSpec.BooleanValue HIDE_RECIPE_BOOK;
    public static final ModConfigSpec.BooleanValue HIDE_ONLINE_OPTIONS;

    public static final ModConfigSpec.BooleanValue HIDE_FEEDBACK_BUTTONS;
    public static final ModConfigSpec.BooleanValue HIDE_PLAYER_REPORTING;
    public static final ModConfigSpec.BooleanValue HIDE_OPEN_TO_LAN;

    public static final ModConfigSpec.BooleanValue SUPPRESS_ADVANCEMENT_TOASTS;
    public static final ModConfigSpec.BooleanValue SUPPRESS_RECIPE_TOASTS;
    public static final ModConfigSpec.BooleanValue SUPPRESS_TUTORIAL_TOASTS;
    public static final ModConfigSpec.BooleanValue SUPPRESS_NARRATOR_TOAST;
    public static final ModConfigSpec.BooleanValue SUPPRESS_UNSAFE_SERVER_TOAST;
    public static final ModConfigSpec.BooleanValue SUPPRESS_WORLD_BACKUP_TOAST;
    public static final ModConfigSpec.BooleanValue SUPPRESS_RESOURCE_PACK_ERROR_TOASTS;

    public static final ModConfigSpec.BooleanValue HIDE_MULTIPLAYER_WARNING;
    public static final ModConfigSpec.BooleanValue SKIP_EXPERIMENTAL_WARNING;
    public static final ModConfigSpec.BooleanValue SKIP_ACCESSIBILITY_ONBOARDING;
    public static final ModConfigSpec.BooleanValue SKIP_WORLD_UPGRADE_BACKUP;

    public static final ModConfigSpec.BooleanValue HIDE_SELECTED_ITEM_NAME;
    public static final ModConfigSpec.BooleanValue HIDE_CHAT_INDICATORS;
    public static final ModConfigSpec.BooleanValue HIDE_BOSS_BARS;
    public static final ModConfigSpec.BooleanValue HIDE_SCOREBOARD_SIDEBAR;
    public static final ModConfigSpec.BooleanValue HIDE_ACTION_BAR_MESSAGES;
    public static final ModConfigSpec.BooleanValue HIDE_FLOATING_HOLOGRAM_TEXT;
    public static final ModConfigSpec.BooleanValue DISABLE_TELEMETRY;

    static final ModConfigSpec SPEC;

    static {
        BUILDER.translation("declutterui.configuration.category.titleScreen").push("titleScreen");
        HIDE_REALMS = bool("declutterui.configuration.hideRealms",
                "Hide the Realms button from the main menu and any Realms options from settings screens",
                "hideRealms", true);
        HIDE_ACCESSIBILITY_BUTTON = bool("declutterui.configuration.hideAccessibilityButton",
                "Hide the accessibility shortcut button on the title screen",
                "hideAccessibilityButton", true);
        HIDE_LANGUAGE_BUTTON = bool("declutterui.configuration.hideLanguageButton",
                "Hide the language shortcut button on the title screen",
                "hideLanguageButton", true);
        HIDE_SPLASH_TEXT = bool("declutterui.configuration.hideSplashText",
                "Hide the yellow rotating splash text on the title screen",
                "hideSplashText", true);
        HIDE_COPYRIGHT = bool("declutterui.configuration.hideCopyright",
                "Hide the copyright notice on the title screen",
                "hideCopyright", true);
        HIDE_VERSION_TEXT = bool("declutterui.configuration.hideVersionText",
                "Hide the version and modded text in the bottom-left corner of the title screen",
                "hideVersionText", false);
        BUILDER.pop();

        BUILDER.translation("declutterui.configuration.category.menus").push("menus");
        HIDE_CREDITS = bool("declutterui.configuration.hideCredits",
                "Hide the Credits & Attribution button from the Options screen",
                "hideCredits", true);
        HIDE_RECIPE_BOOK = bool("declutterui.configuration.hideRecipeBook",
                "Hide the recipe book button from inventory and crafting screens",
                "hideRecipeBook", true);
        HIDE_ONLINE_OPTIONS = bool("declutterui.configuration.hideOnlineOptions",
                "Hide the Online Options button from the Options screen",
                "hideOnlineOptions", true);
        BUILDER.pop();

        BUILDER.translation("declutterui.configuration.category.pauseMenu").push("pauseMenu");
        HIDE_FEEDBACK_BUTTONS = bool("declutterui.configuration.hideFeedbackButtons",
                "Hide the Give Feedback and Report Bugs buttons from the Game Menu",
                "hideFeedbackButtons", true);
        HIDE_PLAYER_REPORTING = bool("declutterui.configuration.hidePlayerReporting",
                "Hide the Player Reporting button from the Game Menu",
                "hidePlayerReporting", true);
        HIDE_OPEN_TO_LAN = bool("declutterui.configuration.hideOpenToLan",
                "Hide the Open to LAN button from the Game Menu",
                "hideOpenToLan", false);
        BUILDER.pop();

        BUILDER.translation("declutterui.configuration.category.popups").push("popups");
        SUPPRESS_ADVANCEMENT_TOASTS = bool("declutterui.configuration.suppressAdvancementToasts",
                "Suppress advancement unlock toast notifications",
                "suppressAdvancementToasts", true);
        SUPPRESS_RECIPE_TOASTS = bool("declutterui.configuration.suppressRecipeToasts",
                "Suppress recipe unlock toast notifications",
                "suppressRecipeToasts", true);
        SUPPRESS_TUTORIAL_TOASTS = bool("declutterui.configuration.suppressTutorialToasts",
                "Suppress tutorial hint toast notifications",
                "suppressTutorialToasts", true);
        SUPPRESS_NARRATOR_TOAST = bool("declutterui.configuration.suppressNarratorToast",
                "Suppress the narrator on/off toast notification",
                "suppressNarratorToast", true);
        SUPPRESS_UNSAFE_SERVER_TOAST = bool("declutterui.configuration.suppressUnsafeServerToast",
                "Suppress the unsecure server warning toast",
                "suppressUnsafeServerToast", true);
        SUPPRESS_WORLD_BACKUP_TOAST = bool("declutterui.configuration.suppressWorldBackupToast",
                "Suppress the popup shown after a world backup completes",
                "suppressWorldBackupToast", true);
        SUPPRESS_RESOURCE_PACK_ERROR_TOASTS = bool("declutterui.configuration.suppressResourcePackErrorToasts",
                "Suppress resource pack load, copy, and file import failure popups",
                "suppressResourcePackErrorToasts", false);
        BUILDER.pop();

        BUILDER.translation("declutterui.configuration.category.warnings").push("warnings");
        HIDE_MULTIPLAYER_WARNING = bool("declutterui.configuration.hideMultiplayerWarning",
                "Skip the Caution: Third-Party Online Play warning screen",
                "hideMultiplayerWarning", true);
        SKIP_EXPERIMENTAL_WARNING = bool("declutterui.configuration.skipExperimentalWarning",
                "Skip the experimental features confirmation screen",
                "skipExperimentalWarning", true);
        SKIP_ACCESSIBILITY_ONBOARDING = bool("declutterui.configuration.skipAccessibilityOnboarding",
                "Skip the accessibility setup screen that appears on first launch",
                "skipAccessibilityOnboarding", true);
        SKIP_WORLD_UPGRADE_BACKUP = bool("declutterui.configuration.skipWorldUpgradeBackup",
                "Skip the world upgrade backup prompt (off by default for safety)",
                "skipWorldUpgradeBackup", false);
        BUILDER.pop();

        BUILDER.translation("declutterui.configuration.category.hudPrivacy").push("hudPrivacy");
        HIDE_SELECTED_ITEM_NAME = bool("declutterui.configuration.hideSelectedItemName",
                "Hide item names shown above the hotbar when changing the selected item",
                "hideSelectedItemName", false);
        HIDE_CHAT_INDICATORS = bool("declutterui.configuration.hideChatIndicators",
                "Hide the colored indicator bars shown next to chat messages",
                "hideChatIndicators", true);
        HIDE_BOSS_BARS = bool("declutterui.configuration.hideBossBars",
                "Hide boss names and health bars displayed at the top of the screen",
                "hideBossBars", false);
        HIDE_SCOREBOARD_SIDEBAR = bool("declutterui.configuration.hideScoreboardSidebar",
                "Hide the scoreboard sidebar displayed on the right side of the screen",
                "hideScoreboardSidebar", false);
        HIDE_ACTION_BAR_MESSAGES = bool("declutterui.configuration.hideActionBarMessages",
                "Hide action bar messages displayed above the hotbar",
                "hideActionBarMessages", false);
        HIDE_FLOATING_HOLOGRAM_TEXT = bool("declutterui.configuration.hideFloatingHologramText",
                "Hide text displays and invisible armor stand nameplates commonly used for server holograms",
                "hideFloatingHologramText", false);
        DISABLE_TELEMETRY = bool("declutterui.configuration.disableTelemetry",
                "Disable all telemetry data collection",
                "disableTelemetry", true);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    private static ModConfigSpec.BooleanValue bool(String translation, String comment, String key, boolean defaultValue) {
        return BUILDER.translation(translation).comment(comment).define(key, defaultValue);
    }
}
