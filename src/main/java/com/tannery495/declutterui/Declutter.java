package com.tannery495.declutterui;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

@Mod(Declutter.MODID)
public class Declutter {
    public static final String MODID = "declutterui";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Declutter(IEventBus modEventBus, ModContainer modContainer) {
        migrateFlatConfig();
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private static void migrateFlatConfig() {
        Path path = FMLPaths.CONFIGDIR.get().resolve("declutterui-common.toml");
        if (!Files.isRegularFile(path)) return;

        Map<String, String> categories = new LinkedHashMap<>();
        addCategory(categories, "titleScreen", "hideRealms", "hideAccessibilityButton", "hideLanguageButton",
                "hideSplashText", "hideCopyright", "hideVersionText");
        addCategory(categories, "menus", "hideCredits", "hideRecipeBook", "hideOnlineOptions");
        addCategory(categories, "pauseMenu", "hideFeedbackButtons", "hidePlayerReporting", "hideOpenToLan");
        addCategory(categories, "popups", "suppressAdvancementToasts", "suppressRecipeToasts",
                "suppressTutorialToasts", "suppressNarratorToast", "suppressUnsafeServerToast",
                "suppressWorldBackupToast", "suppressResourcePackErrorToasts");
        addCategory(categories, "warnings", "hideMultiplayerWarning", "skipExperimentalWarning",
                "skipAccessibilityOnboarding", "skipWorldUpgradeBackup");
        addCategory(categories, "hudPrivacy", "hideSelectedItemName", "hideChatIndicators", "disableTelemetry");

        try (CommentedFileConfig config = CommentedFileConfig.builder(path).sync().build()) {
            config.load();
            boolean changed = false;
            for (Map.Entry<String, String> entry : categories.entrySet()) {
                String oldKey = entry.getKey();
                String newKey = entry.getValue() + "." + oldKey;
                if (config.contains(oldKey) && !config.contains(newKey)) {
                    config.set(newKey, config.get(oldKey));
                    config.remove(oldKey);
                    changed = true;
                }
            }
            if (changed) {
                config.save();
                LOGGER.info("Grouped existing DeclutterUI settings into categories");
            }
        } catch (Exception exception) {
            LOGGER.warn("Could not migrate the DeclutterUI config into categories; defaults will be used where needed", exception);
        }
    }

    private static void addCategory(Map<String, String> categories, String category, String... keys) {
        for (String key : keys) {
            categories.put(key, category);
        }
    }
}
