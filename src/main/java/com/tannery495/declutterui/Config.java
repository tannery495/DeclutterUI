package com.tannery495.declutterui;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue HIDE_REALMS = BUILDER
            .comment("Hide the Realms button from the main menu and any Realms options from settings screens")
            .define("hideRealms", true);

    public static final ModConfigSpec.BooleanValue DISABLE_TELEMETRY = BUILDER
            .comment("Disable all telemetry data collection")
            .define("disableTelemetry", true);

    public static final ModConfigSpec.BooleanValue HIDE_CREDITS = BUILDER
            .comment("Hide the Credits & Attribution button from the Options screen")
            .define("hideCredits", true);

    public static final ModConfigSpec.BooleanValue HIDE_MULTIPLAYER_WARNING = BUILDER
            .comment("Skip the 'Caution: Third-Party Online Play' warning screen when clicking Multiplayer")
            .define("hideMultiplayerWarning", true);

    public static final ModConfigSpec.BooleanValue SKIP_EXPERIMENTAL_WARNING = BUILDER
            .comment("Skip the experimental features confirmation screen when creating a world with experimental settings")
            .define("skipExperimentalWarning", true);

    public static final ModConfigSpec.BooleanValue HIDE_RECIPE_BOOK = BUILDER
            .comment("Hide the recipe book button from the inventory, crafting table, and furnace screens")
            .define("hideRecipeBook", true);

    public static final ModConfigSpec.BooleanValue HIDE_FEEDBACK_BUTTONS = BUILDER
            .comment("Hide the 'Give Feedback' and 'Report Bugs' buttons from the Game Menu")
            .define("hideFeedbackButtons", true);

    public static final ModConfigSpec.BooleanValue SUPPRESS_ADVANCEMENT_TOASTS = BUILDER
            .comment("Suppress advancement unlock toast notifications")
            .define("suppressAdvancementToasts", true);

    public static final ModConfigSpec.BooleanValue SUPPRESS_RECIPE_TOASTS = BUILDER
            .comment("Suppress recipe unlock toast notifications")
            .define("suppressRecipeToasts", true);

    public static final ModConfigSpec.BooleanValue SUPPRESS_TUTORIAL_TOASTS = BUILDER
            .comment("Suppress tutorial hint toast notifications")
            .define("suppressTutorialToasts", true);

    public static final ModConfigSpec.BooleanValue SUPPRESS_NARRATOR_TOAST = BUILDER
            .comment("Suppress the narrator on/off toast notification")
            .define("suppressNarratorToast", true);

    public static final ModConfigSpec.BooleanValue SUPPRESS_UNSAFE_SERVER_TOAST = BUILDER
            .comment("Suppress the 'This server is not running a secure chat mode' toast when joining servers")
            .define("suppressUnsafeServerToast", true);

    public static final ModConfigSpec.BooleanValue SUPPRESS_WORLD_BACKUP_TOAST = BUILDER
            .comment("Suppress the popup shown after a world backup completes")
            .define("suppressWorldBackupToast", true);

    public static final ModConfigSpec.BooleanValue HIDE_ACCESSIBILITY_BUTTON = BUILDER
            .comment("Hide the accessibility shortcut button on the title screen")
            .define("hideAccessibilityButton", true);

    public static final ModConfigSpec.BooleanValue HIDE_PLAYER_REPORTING = BUILDER
            .comment("Hide the 'Player Reporting' button from the Game Menu (multiplayer only)")
            .define("hidePlayerReporting", true);

    public static final ModConfigSpec.BooleanValue HIDE_OPEN_TO_LAN = BUILDER
            .comment("Hide the 'Open to LAN' button from the Game Menu (singleplayer only)")
            .define("hideOpenToLan", false);

    public static final ModConfigSpec.BooleanValue HIDE_CHAT_INDICATORS = BUILDER
            .comment("Hide the colored indicator bars shown next to chat messages")
            .define("hideChatIndicators", true);

    public static final ModConfigSpec.BooleanValue HIDE_ONLINE_OPTIONS = BUILDER
            .comment("Hide the 'Online Options' button from the Options screen")
            .define("hideOnlineOptions", true);

    public static final ModConfigSpec.BooleanValue HIDE_SPLASH_TEXT = BUILDER
            .comment("Hide the yellow rotating splash text on the title screen")
            .define("hideSplashText", true);

    public static final ModConfigSpec.BooleanValue HIDE_COPYRIGHT = BUILDER
            .comment("Hide the 'Copyright Mojang AB. Do not distribute!' text on the title screen")
            .define("hideCopyright", true);

    public static final ModConfigSpec.BooleanValue SKIP_ACCESSIBILITY_ONBOARDING = BUILDER
            .comment("Skip the accessibility/narrator onboarding screen that appears on first launch")
            .define("skipAccessibilityOnboarding", true);

    public static final ModConfigSpec.BooleanValue SKIP_WORLD_UPGRADE_BACKUP = BUILDER
            .comment("Skip the 'Create a backup before upgrading this world?' screen (off by default for safety)")
            .define("skipWorldUpgradeBackup", false);

    static final ModConfigSpec SPEC = BUILDER.build();
}
