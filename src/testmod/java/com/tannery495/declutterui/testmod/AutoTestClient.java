package com.tannery495.declutterui.testmod;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.serialization.Lifecycle;
import com.tannery495.declutterui.Config;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.components.toasts.TutorialToast;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.BackupConfirmScreen;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.GenericMessageScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.SafetyScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.worldselection.ConfirmExperimentalFeaturesScreen;
import net.minecraft.client.gui.screens.worldselection.WorldOpenFlows;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;
import com.tannery495.declutterui.ChatFilter;
import com.tannery495.declutterui.ChatTags;
import com.tannery495.declutterui.DeclutterClient;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Pig;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.lwjgl.glfw.GLFW;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.NeoForge;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BooleanSupplier;

/**
 * Dev-only automated check of every DeclutterUI option against the local test server.
 * For each option: trigger the element with the option off, screenshot, turn it on, trigger again,
 * screenshot, and record how many pixels changed. Results go to {@code autotest/} in the run directory.
 */
final class AutoTestClient {

    /** Pixels whose channels differ by more than this count as changed. */
    private static final int CHANNEL_TOLERANCE = 40;

    private record Step(int delayTicks, Runnable action) {}

    private static final Deque<Step> steps = new ArrayDeque<>();
    private static final List<String> results = new ArrayList<>();
    private static Path outputDir;
    private static int waitTicks;
    private static boolean started;
    private static int ticksWaitingForWorld;
    private static boolean leftServer;
    private static Style hoverStyle;
    private static NativeImage offImage;
    private static BooleanSupplier waitCondition;
    private static int waitConditionTicks;
    private static String waitDescription;
    private static String mode;
    private static int modeTicks;
    private static String lastScreenName;

    private static final String TEST_WORLD = "dtest_world";

    private AutoTestClient() {}

    static void init() {
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, event -> tick());
        NeoForge.EVENT_BUS.addListener(ScreenEvent.Render.Post.class, AutoTestClient::renderHover);
    }

    private static Minecraft mc() {
        return Minecraft.getInstance();
    }

    private static void tick() {
        Minecraft mc = mc();
        if (mode == null) {
            mode = readMode(mc);
        }
        if (mode.equals("onboarding")) {
            onboardingTick(mc);
            return;
        }
        if (!started) {
            if (mc.player == null || mc.level == null) {
                if (++ticksWaitingForWorld == 20 * 120) {
                    log("ERROR could not join the test server within 2 minutes; is runServer running?");
                    mc.stop();
                }
                return;
            }
            started = true;
            outputDir = mc.gameDirectory.toPath().resolve("autotest");
            try {
                Files.createDirectories(outputDir);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            mc.options.pauseOnLostFocus = false;
            if (mode.equals("nametags")) {
                planNameTagsOnly();
            } else if (mode.equals("settings")) {
                then(40, () -> {
                    log("Autotest started (settings only)");
                    leftServer = true;
                    mc.level.disconnect();
                    mc.disconnect();
                    mc.setScreen(new TitleScreen());
                });
                planDeepChecks();
                then(5, () -> {
                    writeResults();
                    log("Autotest finished");
                    mc.stop();
                });
            } else {
                planAll();
            }
        }
        if (mc.player == null && !leftServer) {
            log("ERROR disconnected from the test server; aborting");
            writeResults();
            mc.stop();
            return;
        }
        if (waitCondition != null) {
            if (waitCondition.getAsBoolean()) {
                waitCondition = null;
            } else if (--waitConditionTicks <= 0) {
                log("ERROR timed out waiting for " + waitDescription);
                waitCondition = null;
            } else {
                return;
            }
        }
        if (waitTicks > 0) {
            waitTicks--;
            return;
        }
        Step step = steps.poll();
        if (step == null) return;
        try {
            step.action().run();
        } catch (RuntimeException e) {
            log("ERROR in step: " + e);
        }
        Step next = steps.peek();
        if (next != null) waitTicks = next.delayTicks();
    }

    private static void then(int delayTicks, Runnable action) {
        steps.add(new Step(delayTicks, action));
    }

    /** Pauses the plan until {@code condition} holds (or the timeout passes, which is logged as an error). */
    private static void waitUntil(String description, BooleanSupplier condition, int timeoutTicks) {
        then(1, () -> {
            waitDescription = description;
            waitConditionTicks = timeoutTicks;
            waitCondition = condition;
        });
    }

    private static void command(String command) {
        var player = mc().player;
        if (player != null) player.connection.sendCommand(command);
    }

    /**
     * Queues one off/on comparison. {@code prepare} resets state before each trigger, {@code trigger}
     * makes the element appear, and the screenshot is taken {@code settleTicks} later.
     */
    private static void compare(String name, ModConfigSpec.BooleanValue option, Runnable prepare,
                                Runnable trigger, int settleTicks, Runnable cleanup) {
        boolean[] original = new boolean[1];
        then(2, () -> {
            original[0] = option.get();
            option.set(false);
            prepare.run();
        });
        then(5, trigger);
        then(settleTicks, () -> offImage = capture(name + "_off"));
        then(2, () -> {
            cleanup.run();
            option.set(true);
            prepare.run();
        });
        then(5, trigger);
        then(settleTicks, () -> {
            NativeImage onImage = capture(name + "_on");
            record(name, offImage, onImage);
            offImage.close();
            onImage.close();
            offImage = null;
        });
        then(2, () -> {
            cleanup.run();
            option.set(original[0]);
        });
    }

    private static void compare(String name, ModConfigSpec.BooleanValue option, Runnable trigger, int settleTicks) {
        compare(name, option, () -> {}, trigger, settleTicks, () -> {});
    }

    private static final Runnable NOTHING = () -> {};

    private static void planAll() {
        Minecraft mc = mc();
        then(40, () -> {
            log("Autotest started as " + mc.getUser().getName());
            if (mc.getCurrentServer() != null) {
                mc.getCurrentServer().setResourcePackStatus(ServerData.ServerPackStatus.ENABLED);
            }
            // The pack policy is decided at join; without this, pushed packs stay queued and never download.
            mc.getDownloadedPackSource().allowServerPacks();
            command("dtest clear");
            command("dtest reset");
            mc.player.setYRot(0);
            mc.player.setXRot(0);
        });

        // --- HUD elements sent by the server ---
        compare("titles", Config.HIDE_TITLES, () -> command("dtest clear"),
                () -> command("dtest title"), 25, NOTHING);
        compare("actionbar", Config.HIDE_ACTION_BAR_MESSAGES, () -> command("dtest actionbar"), 10);
        compare("bossbar", Config.HIDE_BOSS_BARS, () -> command("dtest clear"),
                () -> command("dtest bossbar"), 10, () -> command("dtest clear"));
        compare("sidebar", Config.HIDE_SCOREBOARD_SIDEBAR, () -> command("dtest clear"),
                () -> command("dtest sidebar"), 10, () -> command("dtest clear"));
        compare("tab_header_footer", Config.HIDE_TAB_HEADER_FOOTER, NOTHING, () -> {
            command("dtest tab");
            mc.options.keyPlayerList.setDown(true);
        }, 10, () -> {
            mc.options.keyPlayerList.setDown(false);
            command("dtest clear");
        });
        compare("glowing_outlines", Config.HIDE_GLOWING_OUTLINES, () -> command("dtest clear"),
                () -> command("dtest glow"), 30, () -> command("dtest clear"));
        compare("holograms", Config.HIDE_FLOATING_HOLOGRAM_TEXT, () -> command("dtest clear"),
                () -> command("dtest hologram"), 30, () -> command("dtest clear"));
        compare("chat_indicators", Config.HIDE_CHAT_INDICATORS, () -> mc.gui.getChat().clearMessages(false),
                () -> mc.player.connection.sendChat("Chat indicator test message"), 20, NOTHING);

        String name = mc.getUser().getName();
        Style entityHover = Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ENTITY,
                new HoverEvent.EntityTooltipInfo(EntityType.PLAYER, mc.player.getUUID(), Component.literal(name))));
        Style msgHover = Style.EMPTY
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to message " + name)))
                .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + name + " "));
        // Vanilla only shows entity hover tooltips with advanced tooltips (F3+H) enabled.
        compare("profile_popup_entity", Config.HIDE_PLAYER_PROFILE_POPUPS, NOTHING, () -> {
            mc.options.advancedItemTooltips = true;
            hoverStyle = entityHover;
            mc.setScreen(new ChatScreen(""));
        }, 5, () -> {
            mc.options.advancedItemTooltips = false;
            hoverStyle = null;
            mc.setScreen(null);
        });
        compare("profile_popup_msg", Config.HIDE_PLAYER_PROFILE_POPUPS, NOTHING, () -> {
            hoverStyle = msgHover;
            mc.setScreen(new ChatScreen(""));
        }, 5, () -> {
            hoverStyle = null;
            mc.setScreen(null);
        });

        // --- Below-name scores: view the player through an armor stand so their own name tag renders ---
        compare("below_name_scores", Config.HIDE_BELOW_NAME_SCORES, () -> {
            command("dtest clear");
            command("dtest reset");
            command("dtest belowname");
            command("dtest camera");
        }, () -> {
            ArmorStand stand = mc.level.getEntitiesOfClass(ArmorStand.class, new AABB(0, -61, 4, 1, -58, 5)).stream()
                    .findFirst().orElse(null);
            if (stand == null) {
                log("ERROR camera armor stand not found");
            } else {
                mc.setCameraEntity(stand);
            }
        }, 20, () -> {
            mc.setCameraEntity(mc.player);
            command("dtest clear");
        });

        planChatFilter();
        planChatTags();
        planNameTags();
        planScoreboardNumbers();

        // --- Popups (toasts) ---
        Runnable clearToasts = () -> mc.getToasts().clear();
        compare("advancement_toast", Config.SUPPRESS_ADVANCEMENT_TOASTS, clearToasts,
                () -> command("dtest advancement"), 30, clearToasts);
        compare("recipe_toast", Config.SUPPRESS_RECIPE_TOASTS, clearToasts,
                () -> command("dtest recipe"), 30, clearToasts);
        compare("unsecure_server_toast", Config.SUPPRESS_UNSAFE_SERVER_TOAST, clearToasts,
                () -> SystemToast.add(mc.getToasts(), SystemToast.SystemToastId.UNSECURE_SERVER_WARNING,
                        Component.translatable("multiplayer.unsecureserver.toast.title"),
                        Component.translatable("multiplayer.unsecureserver.toast")), 20, clearToasts);
        compare("narrator_toast", Config.SUPPRESS_NARRATOR_TOAST, clearToasts,
                () -> SystemToast.add(mc.getToasts(), SystemToast.SystemToastId.NARRATOR_TOGGLE,
                        Component.translatable("narrator.toast.enabled"), Component.literal("Narrates all")), 20, clearToasts);
        compare("world_backup_toast", Config.SUPPRESS_WORLD_BACKUP_TOAST, clearToasts,
                () -> SystemToast.add(mc.getToasts(), SystemToast.SystemToastId.WORLD_BACKUP,
                        Component.translatable("selectWorld.edit.backupCreated", "Test World"), null), 20, clearToasts);
        compare("tutorial_toast", Config.SUPPRESS_TUTORIAL_TOASTS, clearToasts,
                () -> mc.getToasts().addToast(new TutorialToast(TutorialToast.Icons.MOVEMENT_KEYS,
                        Component.translatable("tutorial.move.title", "WASD"),
                        Component.translatable("tutorial.move.description", "Space"), false)), 20, clearToasts);
        compare("resource_pack_error_toast", Config.SUPPRESS_RESOURCE_PACK_ERROR_TOASTS, clearToasts,
                () -> SystemToast.onPackCopyFailure(mc, "test-pack.zip"), 20, clearToasts);
        compare("resource_pack_download", Config.HIDE_RESOURCE_PACK_DOWNLOAD_PROGRESS, clearToasts,
                () -> command("dtest pack"), 30, clearToasts);
        // After the ~3 s download fails: the "pack(s) failed to download" message, with progress popups left on.
        boolean[] progressOption = new boolean[1];
        then(2, () -> {
            progressOption[0] = Config.HIDE_RESOURCE_PACK_DOWNLOAD_PROGRESS.get();
            Config.HIDE_RESOURCE_PACK_DOWNLOAD_PROGRESS.set(false);
        });
        compare("resource_pack_download_failed", Config.SUPPRESS_RESOURCE_PACK_ERROR_TOASTS, clearToasts,
                () -> command("dtest pack"), 100, clearToasts);
        then(2, () -> Config.HIDE_RESOURCE_PACK_DOWNLOAD_PROGRESS.set(progressOption[0]));

        // --- Hotbar and inventory ---
        then(2, () -> command("dtest items"));
        int[] slot = {0};
        compare("selected_item_name", Config.HIDE_SELECTED_ITEM_NAME, NOTHING, () -> {
            slot[0] = (slot[0] + 1) % 4;
            mc.player.getInventory().selected = slot[0];
        }, 8, NOTHING);
        // Creative mode swaps in the creative inventory, which has no recipe book.
        compare("recipe_book_button", Config.HIDE_RECIPE_BOOK, () -> command("dtest survival"),
                () -> mc.setScreen(new InventoryScreen(mc.player)), 5, () -> {
                    mc.setScreen(null);
                    command("dtest creative");
                });

        // --- Menus opened while in game ---
        compare("pause_feedback_buttons", Config.HIDE_FEEDBACK_BUTTONS, NOTHING,
                () -> mc.setScreen(new PauseScreen(true)), 5, () -> mc.setScreen(null));
        compare("pause_player_reporting", Config.HIDE_PLAYER_REPORTING, NOTHING,
                () -> mc.setScreen(new PauseScreen(true)), 5, () -> mc.setScreen(null));
        // Both pause-menu options on at once (multiplayer has no LAN button to hide).
        pauseCombination("pause_reporting_with_lan_hidden", Config.HIDE_PLAYER_REPORTING, Config.HIDE_OPEN_TO_LAN);
        // Servers that send links get a Feedback... submenu instead of the two feedback buttons.
        compare("pause_feedback_with_server_links", Config.HIDE_FEEDBACK_BUTTONS, () -> command("dtest links"),
                () -> mc.setScreen(new PauseScreen(true)), 5, () -> {
            if (Config.HIDE_FEEDBACK_BUTTONS.get() && mc.screen != null) {
                AbstractButton feedback = findDeepButton(mc.screen, key -> key.equals("menu.feedback"));
                AbstractButton links = findDeepButton(mc.screen, key -> key.equals("menu.server_links"));
                check("server links: Feedback... button is hidden", true, feedback == null || !feedback.visible);
                check("server links: Server Links... button stays", true, links != null && links.visible);
                AbstractButton options = findDeepButton(mc.screen, key -> key.equals("menu.options"));
                check("server links: no empty gap beside Server Links...", true, links != null && options != null
                        && links.getX() == options.getX() && links.getRight() == findDeepButton(mc.screen,
                        key -> key.equals("menu.returnToGame")).getRight());
            }
            mc.setScreen(null);
            command("dtest nolinks");
        });
        then(2, () -> mc.setScreen(new PauseScreen(true)));
        then(5, () -> {
            boolean reporting = Config.HIDE_PLAYER_REPORTING.get();
            Config.HIDE_PLAYER_REPORTING.set(true);
            mc.setScreen(new PauseScreen(true));
            check("pause menu: Mods beside Options before resize", true, modsBesideOptions(mc.screen));
            mc.screen.resize(mc, 320, 240);
            check("pause menu: Mods beside Options after a small resize", true, modsBesideOptions(mc.screen));
            mc.screen.resize(mc, mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight());
            check("pause menu: Mods beside Options after resizing back", true, modsBesideOptions(mc.screen));
            check("pause menu: Tab never focuses a hidden button", true, tabSkipsHiddenWidgets(mc.screen));
            Config.HIDE_PLAYER_REPORTING.set(reporting);
            mc.setScreen(null);
        });
        compare("options_online", Config.HIDE_ONLINE_OPTIONS, NOTHING,
                () -> mc.setScreen(new OptionsScreen(null, mc.options)), 5, () -> mc.setScreen(null));
        compare("options_credits", Config.HIDE_CREDITS, NOTHING,
                () -> mc.setScreen(new OptionsScreen(null, mc.options)), 5, () -> mc.setScreen(null));
        compare("options_telemetry", Config.DISABLE_TELEMETRY, NOTHING, () -> {
            mc.setScreen(new OptionsScreen(null, mc.options));
            log("  allowsTelemetry() with disableTelemetry=" + Config.DISABLE_TELEMETRY.get() + " -> " + mc.allowsTelemetry());
        }, 5, () -> mc.setScreen(null));

        // --- Out of game: leave the server and check menus ---
        then(5, () -> {
            command("dtest clear");
            ServerList servers = new ServerList(mc);
            servers.load();
            if (servers.get("localhost") == null) {
                servers.add(new ServerData("DeclutterUI Test Server", "localhost", ServerData.Type.OTHER), false);
                servers.save();
            }
        });
        then(10, () -> {
            leftServer = true;
            mc.level.disconnect();
            mc.disconnect();
            mc.setScreen(new TitleScreen());
        });
        compare("server_description", Config.HIDE_SERVER_DESCRIPTION, NOTHING,
                () -> mc.setScreen(new JoinMultiplayerScreen(new TitleScreen())), 60, NOTHING);
        compare("multiplayer_warning", Config.HIDE_MULTIPLAYER_WARNING, NOTHING,
                () -> mc.setScreen(new SafetyScreen(new TitleScreen())), 5, NOTHING);
        Runnable titleScreen = () -> mc.setScreen(new TitleScreen());
        compare("title_splash", Config.HIDE_SPLASH_TEXT, titleScreen, 10);
        compare("title_copyright", Config.HIDE_COPYRIGHT, titleScreen, 10);
        compare("title_version_text", Config.HIDE_VERSION_TEXT, titleScreen, 10);
        compare("title_realms", Config.HIDE_REALMS, titleScreen, 10);
        compare("title_accessibility_button", Config.HIDE_ACCESSIBILITY_BUTTON, titleScreen, 10);
        compare("title_language_button", Config.HIDE_LANGUAGE_BUTTON, titleScreen, 10);

        planDeepChecks();
        planSingleplayer();

        then(5, () -> {
            writeResults();
            log("Autotest finished");
            mc.stop();
        });
    }

    /** Options that only matter outside multiplayer: experimental warnings, the LAN button and the backup prompt. */
    private static void planSingleplayer() {
        Minecraft mc = mc();

        // Shown when ticking an experimental data pack while creating a world.
        compare("experimental_features_screen", Config.SKIP_EXPERIMENTAL_WARNING, NOTHING, () ->
                mc.setScreen(new ConfirmExperimentalFeaturesScreen(experimentalPacks(), accepted -> {
                    log("  experimental features screen answered: " + accepted);
                    mc.setScreen(new TitleScreen());
                })), 5, () -> mc.setScreen(new TitleScreen()));
        // Shown when pressing Create World with experimental settings.
        compare("experimental_world_creation", Config.SKIP_EXPERIMENTAL_WARNING, NOTHING, () ->
                WorldOpenFlows.confirmWorldCreation(mc, null, Lifecycle.experimental(), () -> {
                    log("  experimental world creation went ahead");
                    mc.setScreen(new TitleScreen());
                }, false), 5, () -> mc.setScreen(new TitleScreen()));

        then(5, () -> {
            log("Creating singleplayer test world");
            mc.createWorldOpenFlows().createFreshLevel(TEST_WORLD,
                    new LevelSettings(TEST_WORLD, GameType.CREATIVE, false, Difficulty.PEACEFUL, true,
                            new GameRules(), WorldDataConfiguration.DEFAULT),
                    WorldOptions.defaultWithRandomSeed(),
                    registries -> registries.registryOrThrow(Registries.WORLD_PRESET)
                            .getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),
                    new TitleScreen());
        });
        waitUntil("the singleplayer world to load", () -> mc.player != null && mc.screen == null, 20 * 60);
        compare("pause_open_to_lan", Config.HIDE_OPEN_TO_LAN, NOTHING,
                () -> mc.setScreen(new PauseScreen(true)), 5, () -> mc.setScreen(null));
        // Both pause-menu options on at once (singleplayer has no Player Reporting button to hide).
        pauseCombination("pause_lan_with_reporting_hidden", Config.HIDE_OPEN_TO_LAN, Config.HIDE_PLAYER_REPORTING);
        then(5, AutoTestClient::leaveSingleplayer);

        // A world saved by a newer version (a downgrade) makes vanilla 1.21.1 ask for a backup before opening it.
        compare("world_backup_prompt", Config.SKIP_WORLD_UPGRADE_BACKUP, AutoTestClient::markTestWorldNewer, () ->
                mc.createWorldOpenFlows().openWorld(TEST_WORLD, () -> mc.setScreen(new TitleScreen())), 100, () -> {
            if (mc.screen instanceof BackupConfirmScreen screen) {
                pressButton(screen, "gui.cancel");
            } else if (mc.level != null) {
                leaveSingleplayer();
            }
        });
        then(5, () -> {
            if (mc.level != null) leaveSingleplayer();
        });
    }

    /** Screenshots the reward messages with and without phrases, then checks each message kind directly. */
    private static void planChatFilter() {
        Minecraft mc = mc();
        List<String> phrases = List.of("Thanks for playing", "Rewards:");
        @SuppressWarnings("unchecked")
        List<String>[] originalPhrases = new List[1];
        boolean[] originalPlayerOption = new boolean[1];
        then(2, () -> {
            originalPhrases[0] = List.copyOf(Config.HIDDEN_CHAT_PHRASES.get());
            originalPlayerOption[0] = Config.HIDE_MATCHING_PLAYER_MESSAGES.get();
            Config.HIDDEN_CHAT_PHRASES.set(List.of());
            mc.gui.getChat().clearMessages(false);
            command("dtest reward");
        });
        then(20, () -> offImage = capture("chat_filter_off"));
        then(2, () -> {
            Config.HIDDEN_CHAT_PHRASES.set(phrases);
            mc.gui.getChat().clearMessages(false);
            command("dtest reward");
        });
        then(20, () -> {
            NativeImage onImage = capture("chat_filter_on");
            record("chat_filter", offImage, onImage);
            offImage.close();
            onImage.close();
            offImage = null;
        });
        then(2, () -> {
            UUID other = UUID.randomUUID();
            UUID self = mc.player.getUUID();
            String reward = "Hey! Thanks for playing at Megasurvival.";
            Config.HIDE_MATCHING_PLAYER_MESSAGES.set(false);
            check("server message with phrase is hidden", true, systemMessage(reward, false));
            check("phrase match ignores capitals", true, systemMessage("HEY! THANKS FOR PLAYING", false));
            check("server message without phrase stays", false, systemMessage("Ordinary server message", false));
            check("action bar with phrase is left to its own option", false, systemMessage(reward, true));
            check("other player's message stays when player option is off", false, playerMessage(other, reward));
            Config.HIDE_MATCHING_PLAYER_MESSAGES.set(true);
            check("other player's message is hidden when player option is on", true, playerMessage(other, reward));
            check("your own message always stays", false, playerMessage(self, reward));
            Config.HIDDEN_CHAT_PHRASES.set(List.of("", "   "));
            check("blank phrases hide nothing", false, systemMessage(reward, false));
            check("ChatFilter helper agrees on plain text", false, ChatFilter.containsHiddenPhrase(reward));
        });
        then(2, () -> {
            Config.HIDDEN_CHAT_PHRASES.set(originalPhrases[0]);
            Config.HIDE_MATCHING_PLAYER_MESSAGES.set(originalPlayerOption[0]);
            mc.gui.getChat().clearMessages(false);
        });
    }

    /** Layout, resize and keyboard checks on the title screen, plus a walk through DeclutterUI's own settings. */
    private static void planDeepChecks() {
        Minecraft mc = mc();
        boolean[] saved = new boolean[3];
        then(5, () -> {
            saved[0] = Config.HIDE_REALMS.get();
            saved[1] = Config.HIDE_COPYRIGHT.get();
            saved[2] = Config.HIDE_ACCESSIBILITY_BUTTON.get();
            Config.HIDE_REALMS.set(true);
            Config.HIDE_COPYRIGHT.set(false);
            Config.HIDE_ACCESSIBILITY_BUTTON.set(true);
            mc.setScreen(new TitleScreen());
        });
        then(10, () -> {
            capture("title_realms_hidden_copyright_shown").close();
            checkTitleLayout("title screen");
            mc.screen.resize(mc, 320, 240);
            checkTitleLayout("title screen after a small resize");
            mc.screen.resize(mc, mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight());
            checkTitleLayout("title screen after resizing back");
            check("title screen: Tab never focuses a hidden button", true, tabSkipsHiddenWidgets(mc.screen));
        });
        then(2, () -> {
            Config.HIDE_REALMS.set(saved[0]);
            Config.HIDE_COPYRIGHT.set(saved[1]);
            Config.HIDE_ACCESSIBILITY_BUTTON.set(saved[2]);
        });

        // DeclutterUI's settings open on the category list; open every category from there.
        then(5, () -> check("settings screen opens", true, openConfigScreen() != null));
        then(10, () -> {
            capture("settings_main").close();
            sectionScreen = mc.screen;
            categoryKeys.clear();
            for (AbstractWidget widget : allWidgets(mc.screen)) {
                String label = widget.getMessage().getString();
                if (widget instanceof AbstractButton && !label.equals("Done")) categoryKeys.add(label);
            }
            check("settings: all 7 categories listed (found " + categoryKeys.size() + ")", true, categoryKeys.size() == 7);
        });
        for (int i = 0; i < 7; i++) {
            int index = i;
            then(3, () -> {
                if (index >= categoryKeys.size()) return;
                mc.setScreen(sectionScreen);
                AbstractButton button = findButtonByLabel(mc.screen, categoryKeys.get(index));
                if (button != null) button.onPress();
            });
            then(8, () -> {
                if (index >= categoryKeys.size()) return;
                String name = categoryKeys.get(index).replace("...", "").replaceAll("[^A-Za-z]+", "_").toLowerCase();
                capture("settings_category_" + name).close();
                logButtons("category " + categoryKeys.get(index));
                check("settings: category '" + categoryKeys.get(index) + "' opens", true,
                        mc.screen != null && mc.screen != sectionScreen);
            });
        }
        // The new list option: its editor must open and show the phrases.
        then(3, () -> {
            Config.HIDDEN_CHAT_PHRASES.set(List.of("Thanks for playing", "Rewards:", "/"));
            mc.setScreen(sectionScreen);
            AbstractButton chat = findButtonByLabel(mc.screen, "Chat Filter");
            check("settings: Chat Filter category button found", true, chat != null);
            if (chat != null) chat.onPress();
        });
        then(8, () -> {
            capture("settings_chat_filter").close();
            List<String> labels = new ArrayList<>();
            for (AbstractWidget widget : allWidgets(mc.screen)) {
                if (!(widget instanceof AbstractButton) && !widget.getMessage().getString().isEmpty()
                        && !widget.getMessage().getString().contains(" > ")) {
                    labels.add(widget.getMessage().getString());
                }
            }
            check("settings: Chat Filter order is " + labels, true, labels.equals(List.of("Hidden Chat Phrases",
                    "Hidden Chat Tags", "Also Hide Matching Player Messages", "Also Remove Tags Above Heads",
                    "Hide Player Info in Chat")));
            boolean ellipsis = false;
            for (AbstractWidget widget : allWidgets(mc.screen)) {
                String text = widget.getMessage().getString();
                if (text.endsWith("...") || text.endsWith("\u2026")) ellipsis = true;
            }
            check("settings: no '...' on Chat Filter labels or buttons", true, !ellipsis);
            AbstractButton edit = findControlBesideLabel(mc.screen, "Hidden Chat Phrases");
            check("settings: Hidden Chat Phrases has an edit button", true, edit != null);
            if (edit != null) edit.onPress();
        });
        then(8, () -> {
            capture("settings_chat_phrases_list").close();
            logButtons("phrase list editor");
            boolean shows = false;
            for (AbstractWidget widget : allWidgets(mc.screen)) {
                if (widget.getMessage().getString().contains("Thanks for playing")) shows = true;
            }
            if (!shows) shows = screenHasEditBoxWith(mc.screen, "Thanks for playing");
            check("settings: phrase list editor shows the saved phrases", true, shows);
            int arrows = 0;
            int boxes = 0;
            AbstractButton firstRemove = null;
            String addLabel = null;
            for (AbstractWidget widget : allWidgets(mc.screen)) {
                String key = translationKey(widget);
                if (widget.visible && key != null && (key.endsWith("listelementup") || key.endsWith("listelementdown"))) arrows++;
                if (widget instanceof net.minecraft.client.gui.components.EditBox) boxes++;
                if (firstRemove == null && key != null && key.endsWith("listelementremove") && widget instanceof AbstractButton b) firstRemove = b;
                if (widget instanceof AbstractButton && widget.getMessage().getString().startsWith("+")) addLabel = widget.getMessage().getString();
            }
            check("list editor: no move arrows shown (found " + arrows + ")", true, arrows == 0);
            check("list editor: add button reads '+ Add phrase' (got " + addLabel + ")", true, "+ Add phrase".equals(addLabel));
            check("list editor: every row has a remove button", true, firstRemove != null);
            int before = boxes;
            if (firstRemove != null) firstRemove.onPress();
            int after = 0;
            for (AbstractWidget widget : allWidgets(mc.screen)) {
                if (widget instanceof net.minecraft.client.gui.components.EditBox) after++;
            }
            check("list editor: remove deletes a row (" + before + " -> " + after + ")", true, after == before - 1);
            Config.HIDDEN_CHAT_PHRASES.set(List.of());
            Config.HIDDEN_CHAT_TAGS.set(List.of("OWNER", "[VIP]", "A much longer tag to check the box width"));
            mc.setScreen(sectionScreen);
            AbstractButton chat = findButtonByLabel(mc.screen, "Chat Filter");
            if (chat != null) chat.onPress();
        });
        then(8, () -> {
            AbstractButton edit = findControlBesideLabel(mc.screen, "Hidden Chat Tags");
            check("settings: Hidden Chat Tags has an edit button", true, edit != null);
            if (edit != null) edit.onPress();
        });
        then(8, () -> {
            capture("settings_chat_tags_list").close();
            boolean wide = false;
            for (AbstractWidget widget : allWidgets(mc.screen)) {
                if (widget instanceof net.minecraft.client.gui.components.EditBox box && box.getWidth() >= mc.screen.width / 2) wide = true;
            }
            check("settings: list editor text boxes span at least half the screen", true, wide);
            Config.HIDDEN_CHAT_TAGS.set(List.of());
            mc.setScreen(new TitleScreen());
        });
    }

    private static Screen sectionScreen;
    private static final List<String> categoryKeys = new ArrayList<>();

    private static void checkTitleLayout(String label) {
        Screen screen = mc().screen;
        AbstractWidget copyright = findDeepWidget(screen, key -> key.equals("title.credits"));
        AbstractButton multiplayer = findDeepButton(screen, key -> key.equals("menu.multiplayer"));
        AbstractButton mods = findDeepButton(screen, key -> key.equals("fml.menu.mods"));
        check(label + ": copyright stays at the bottom edge", true,
                copyright != null && copyright.visible && copyright.getBottom() == screen.height);
        check(label + ": Mods moves up right under Multiplayer", true,
                multiplayer != null && mods != null && mods.getY() == multiplayer.getY() + 24);
    }

    /** Presses Tab repeatedly and fails if keyboard focus ever lands on an invisible or inactive widget. */
    private static boolean tabSkipsHiddenWidgets(Screen screen) {
        for (int i = 0; i < 25; i++) {
            screen.keyPressed(GLFW.GLFW_KEY_TAB, 0, 0);
            for (AbstractWidget widget : allWidgets(screen)) {
                if (widget.isFocused() && (!widget.visible || !widget.active)) {
                    log("  focused hidden widget: " + widget.getMessage().getString());
                    return false;
                }
            }
        }
        return true;
    }

    private static Screen openConfigScreen() {
        Minecraft mc = mc();
        return ModList.get().getModContainerById("declutterui")
                .flatMap(container -> container.getCustomExtension(IConfigScreenFactory.class)
                        .map(factory -> factory.createScreen(container, new TitleScreen())))
                .map(screen -> {
                    mc.setScreen(screen);
                    return screen;
                })
                .orElse(null);
    }

    private static List<AbstractWidget> allWidgets(ContainerEventHandler root) {
        List<AbstractWidget> found = new ArrayList<>();
        collectWidgets(root, found, 0);
        return found;
    }

    private static void collectWidgets(ContainerEventHandler parent, List<AbstractWidget> found, int depth) {
        if (depth > 6) return;
        for (GuiEventListener child : parent.children()) {
            if (child instanceof AbstractWidget widget) found.add(widget);
            if (child instanceof ContainerEventHandler container) collectWidgets(container, found, depth + 1);
        }
    }

    private static String translationKey(AbstractWidget widget) {
        return widget.getMessage().getContents() instanceof TranslatableContents tc ? tc.getKey() : null;
    }

    private static AbstractWidget findDeepWidget(Screen screen, java.util.function.Predicate<String> keyTest) {
        for (AbstractWidget widget : allWidgets(screen)) {
            String key = translationKey(widget);
            if (key != null && keyTest.test(key)) return widget;
        }
        return null;
    }

    private static AbstractButton findDeepButton(Screen screen, java.util.function.Predicate<String> keyTest) {
        for (AbstractWidget widget : allWidgets(screen)) {
            String key = translationKey(widget);
            if (widget instanceof AbstractButton button && key != null && keyTest.test(key)) return button;
        }
        return null;
    }

    private static AbstractButton firstNonFooterButton(Screen screen) {
        for (AbstractWidget widget : allWidgets(screen)) {
            String key = translationKey(widget);
            if (widget instanceof AbstractButton button && !"gui.done".equals(key) && !"gui.back".equals(key)) return button;
        }
        return null;
    }

    private static AbstractButton findButtonByLabel(Screen screen, String label) {
        for (AbstractWidget widget : allWidgets(screen)) {
            if (widget instanceof AbstractButton button && widget.getMessage().getString().equals(label)) return button;
        }
        return null;
    }

    private static boolean screenHasEditBoxWith(Screen screen, String text) {
        for (AbstractWidget widget : allWidgets(screen)) {
            if (widget instanceof net.minecraft.client.gui.components.EditBox box && box.getValue().contains(text)) return true;
        }
        return false;
    }

    /** Finds the button in the same settings row as the label showing {@code labelText}. */
    private static AbstractButton findControlBesideLabel(Screen screen, String labelText) {
        List<ContainerEventHandler> rows = new ArrayList<>();
        collectContainers(screen, rows, 0);
        for (ContainerEventHandler row : rows) {
            boolean hasLabel = false;
            AbstractButton control = null;
            for (GuiEventListener child : row.children()) {
                if (child instanceof AbstractWidget widget && widget.getMessage().getString().startsWith(labelText)) hasLabel = true;
                if (child instanceof AbstractButton button && !button.getMessage().getString().startsWith(labelText)) control = button;
            }
            if (hasLabel && control != null) return control;
        }
        return null;
    }

    private static void collectContainers(ContainerEventHandler parent, List<ContainerEventHandler> found, int depth) {
        if (depth > 6) return;
        for (GuiEventListener child : parent.children()) {
            if (child instanceof ContainerEventHandler container) {
                found.add(container);
                collectContainers(container, found, depth + 1);
            }
        }
    }

    private static void logButtons(String where) {
        StringBuilder line = new StringBuilder("  " + where + " (" + mc().screen.getClass().getSimpleName() + ") buttons:");
        for (AbstractWidget widget : allWidgets(mc().screen)) {
            if (widget instanceof AbstractButton) {
                line.append(" [").append(widget.getMessage().getString()).append("]");
            }
        }
        log(line.toString());
    }

    /** Compares {@code option} off/on while {@code alsoOn} stays on, and checks Mods moved up beside Options. */
    private static void pauseCombination(String name, ModConfigSpec.BooleanValue option, ModConfigSpec.BooleanValue alsoOn) {
        Minecraft mc = mc();
        boolean[] original = new boolean[1];
        then(2, () -> {
            original[0] = alsoOn.get();
            alsoOn.set(true);
        });
        compare(name, option, NOTHING, () -> mc.setScreen(new PauseScreen(true)), 5, () -> {
            if (option.get() && mc.screen != null) {
                check(name + ": Mods sits beside Options", true, modsBesideOptions(mc.screen));
            }
            mc.setScreen(null);
        });
        then(2, () -> alsoOn.set(original[0]));
    }

    private static boolean modsBesideOptions(Screen screen) {
        Button options = findButton(screen, "menu.options");
        Button mods = findButton(screen, "fml.menu.mods");
        return options != null && mods != null && mods.visible && mods.getY() == options.getY();
    }

    private static Button findButton(Screen screen, String translationKey) {
        for (GuiEventListener child : screen.children()) {
            if (child instanceof Button button && button.getMessage().getContents() instanceof TranslatableContents tc
                    && tc.getKey().equals(translationKey)) {
                return button;
            }
        }
        return null;
    }

    private static final List<String> TEST_TAGS = List.of("OWNER", "[2]", "[3]", "VIP", "\uE001");

    /** Rank formats from different servers, checked directly, plus a real chat screenshot. */
    private static void planChatTags() {
        Minecraft mc = mc();
        @SuppressWarnings("unchecked")
        List<String>[] original = new List[1];
        then(2, () -> {
            original[0] = List.copyOf(Config.HIDDEN_CHAT_TAGS.get());
            Config.HIDDEN_CHAT_TAGS.set(TEST_TAGS);
            tagCase("plain rank before name", Component.literal("OWNER Lover: welcome!"), true, "Lover: welcome!");
            tagCase("numbered bracket tags", Component.literal("[2] [3] .HundredBead2106: hi"), true, ".HundredBead2106: hi");
            tagCase("tag listed without brackets, shown with them, \u00bb separator",
                    Component.literal("[VIP] Steve \u00bb hello"), true, "Steve \u00bb hello");
            Component split = Component.literal("[").withStyle(ChatFormatting.WHITE)
                    .append(Component.literal("OWNER").withStyle(ChatFormatting.RED))
                    .append(Component.literal("]").withStyle(ChatFormatting.WHITE))
                    .append(Component.literal(" Lover").withStyle(ChatFormatting.AQUA))
                    .append(Component.literal(": hi"));
            tagCase("rank split into separately colored pieces", split, true, "Lover: hi");
            Component splitResult = ChatTags.strip(split, true);
            check("colors of the remaining name are kept", true, splitResult != null
                    && styleOf(splitResult, "Lover").getColor() != null
                    && styleOf(splitResult, "Lover").getColor().equals(Style.EMPTY.withColor(ChatFormatting.AQUA).getColor()));
            tagCase("small-caps lettering", Component.literal("\u1d0f\u1d21\u0274\u1d07\u0280 Lover: hi"), true, "Lover: hi");
            tagCase("full-width lettering", Component.literal("\uff2f\uff37\uff2e\uff25\uff32 Lover: hi"), true, "Lover: hi");
            tagCase("lower-case rank", Component.literal("owner Lover: hi"), true, "Lover: hi");
            tagCase("custom icon character (when listed)", Component.literal("\uE001 Lover: hi"), true, "Lover: hi");
            tagCase("rank word in the message body stays", Component.literal("Lover: the OWNER said hi"), true,
                    "Lover: the OWNER said hi");
            tagCase("tag at the end of the body stays", Component.literal("Lover: hi [VIP]"), true, "Lover: hi [VIP]");
            tagCase("names containing a tag stay whole", Component.literal("OWNERship_fan: hi"), true, "OWNERship_fan: hi");
            tagCase("vanilla <name> chat format", Component.translatable("chat.type.text",
                    Component.literal("OWNER Lover"), Component.literal("hi")), true, "<Lover> hi");
            tagCase("rank after the name", Component.literal("Lover [VIP]: hi"), true, "Lover: hi");
            tagCase("line without separator (join message)", Component.literal("[VIP] Lover joined the game"), true,
                    "Lover joined the game");
            tagCase("URL in a line is not treated as a name", Component.literal("Visit https://shop.example for VIP deals"),
                    true, "Visit https://shop.example for VIP deals");
            tagCase("name tag mode (whole text)", Component.literal("OWNER Dev"), false, "Dev");
            Component hovered = Component.literal("OWNER ").append(Component.literal("Lover").withStyle(Style.EMPTY
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("profile")))))
                    .append(Component.literal(": hi"));
            Component hoveredResult = ChatTags.strip(hovered, true);
            check("hover text on the name is kept", true,
                    hoveredResult != null && styleOf(hoveredResult, "Lover").getHoverEvent() != null);
            ClientChatReceivedEvent.System event = new ClientChatReceivedEvent.System(Component.literal("OWNER Lover: welcome!"), false);
            NeoForge.EVENT_BUS.post(event);
            check("chat event: tag removed from incoming message", true, event.getMessage().getString().equals("Lover: welcome!"));
            ClientChatReceivedEvent.System overlay = new ClientChatReceivedEvent.System(Component.literal("OWNER Lover: welcome!"), true);
            NeoForge.EVENT_BUS.post(overlay);
            check("chat event: action bar is left alone", true, overlay.getMessage().getString().equals("OWNER Lover: welcome!"));
            Config.HIDDEN_CHAT_TAGS.set(List.of("", "  "));
            tagCase("blank tags remove nothing", Component.literal("OWNER Lover: hi"), true, "OWNER Lover: hi");
            Config.HIDDEN_CHAT_TAGS.set(List.of());
            mc.gui.getChat().clearMessages(false);
            command("dtest rankchat");
        });
        then(20, () -> offImage = capture("chat_tags_off"));
        then(2, () -> {
            Config.HIDDEN_CHAT_TAGS.set(TEST_TAGS);
            mc.gui.getChat().clearMessages(false);
            command("dtest rankchat");
        });
        then(20, () -> {
            NativeImage onImage = capture("chat_tags_on");
            record("chat_tags", offImage, onImage);
            offImage.close();
            onImage.close();
            offImage = null;
        });
        then(2, () -> {
            Config.HIDDEN_CHAT_TAGS.set(original[0]);
            mc.gui.getChat().clearMessages(false);
        });
    }

    private static void tagCase(String description, Component input, boolean senderOnly, String expected) {
        Component output = ChatTags.strip(input, senderOnly);
        String got = output == null ? input.getString() : output.getString();
        String line = (got.equals(expected) ? "PASS " : "FAIL ") + "chat tags: " + description + " -> \"" + got + "\"";
        results.add(line);
        log(line);
    }

    private static Style styleOf(Component component, String text) {
        Style[] found = {Style.EMPTY};
        component.visit((style, part) -> {
            if (part.contains(text)) {
                found[0] = style;
                return Optional.of(true);
            }
            return Optional.empty();
        }, Style.EMPTY);
        return found[0];
    }

    /** Quick mode (autotest-mode.txt = "nametags"): only the name tag checks. */
    private static void planNameTagsOnly() {
        Minecraft mc = mc();
        then(40, () -> {
            log("Autotest started (name tags only)");
            command("dtest clear");
            command("dtest reset");
        });
        then(100, () -> log("  settled"));
        planNameTags();
        then(5, () -> {
            writeResults();
            log("Autotest finished");
            mc.stop();
        });
    }

    /** Names above heads, viewed from an armor stand facing the player so the player's own tags render. */
    private static void planNameTags() {
        Minecraft mc = mc();
        Runnable setup = () -> {
            command("dtest clear");
            command("dtest reset");
            command("dtest nametags");
            command("dtest camera");
        };
        Runnable lookFromStand = () -> {
            ArmorStand stand = mc.level.getEntitiesOfClass(ArmorStand.class, new AABB(0, -61, 4, 1, -58, 5)).stream()
                    .findFirst().orElse(null);
            if (stand == null) {
                log("ERROR camera armor stand not found");
            } else {
                mc.setCameraEntity(stand);
            }
        };
        Runnable restore = () -> {
            mc.setCameraEntity(mc.player);
            command("dtest clear");
        };
        compare("player_name_tags", Config.HIDE_PLAYER_NAME_TAGS, setup, lookFromStand, 20, () -> {
            if (Config.HIDE_PLAYER_NAME_TAGS.get()) {
                List<Entity> riders = mc.player.getPassengers();
                check("name tags: server mounted " + riders.size() + " name entities on the player", true, riders.size() == 2);
                for (Entity rider : riders) {
                    check("name tags: " + rider.getType().toShortString() + " riding the player counts as a player name",
                            true, DeclutterClient.isPlayerOrRidesPlayer(rider));
                }
                Pig pet = mc.level.getEntitiesOfClass(Pig.class, mc.player.getBoundingBox().inflate(5)).stream()
                        .findFirst().orElse(null);
                check("name tags: named pet is not treated as a player", true,
                        pet != null && !DeclutterClient.isPlayerOrRidesPlayer(pet));
            }
            restore.run();
        });
        @SuppressWarnings("unchecked")
        List<String>[] original = new List[1];
        then(2, () -> {
            original[0] = List.copyOf(Config.HIDDEN_CHAT_TAGS.get());
            Config.HIDDEN_CHAT_TAGS.set(TEST_TAGS);
        });
        compare("tags_above_heads", Config.REMOVE_TAGS_ABOVE_HEADS, setup, lookFromStand, 20, restore);
        then(2, () -> Config.HIDDEN_CHAT_TAGS.set(original[0]));
    }

    /** Normal numbers, numbers replaced by a symbol, and a server that already hides them. */
    private static void planScoreboardNumbers() {
        for (String variant : List.of("sidebar", "sidebarfixed", "sidebarblank")) {
            compare("scoreboard_numbers_" + variant, Config.HIDE_SCOREBOARD_NUMBERS, () -> command("dtest clear"),
                    () -> command("dtest " + variant), 10, () -> command("dtest clear"));
        }
    }

    private static boolean systemMessage(String text, boolean overlay) {
        return NeoForge.EVENT_BUS.post(new ClientChatReceivedEvent.System(Component.literal(text), overlay)).isCanceled();
    }

    private static boolean playerMessage(UUID sender, String text) {
        Minecraft mc = mc();
        ChatType.Bound bound = ChatType.bind(ChatType.CHAT, mc.level.registryAccess(), Component.literal("OtherPlayer"));
        return NeoForge.EVENT_BUS.post(new ClientChatReceivedEvent.Player(bound, Component.literal(text),
                PlayerChatMessage.unsigned(sender, text), sender)).isCanceled();
    }

    private static void check(String description, boolean expectedHidden, boolean hidden) {
        String line = (hidden == expectedHidden ? "PASS " : "FAIL ") + description
                + " (hidden=" + hidden + ")";
        results.add(line);
        log(line);
    }

    private static List<Pack> experimentalPacks() {
        PackRepository repository = ServerPacksSource.createVanillaTrustedRepository();
        repository.reload();
        return repository.getAvailablePacks().stream()
                .filter(pack -> !pack.getRequestedFeatures().isEmpty())
                .toList();
    }

    private static void leaveSingleplayer() {
        Minecraft mc = mc();
        if (mc.level != null) {
            mc.level.disconnect();
            mc.disconnect(new GenericMessageScreen(Component.translatable("menu.savingLevel")));
        }
        mc.setScreen(new TitleScreen());
    }

    /** Rewrites the test world's saved version to one newer than the game, like opening it in an older release. */
    private static void markTestWorldNewer() {
        Path levelDat = mc().getLevelSource().getBaseDir().resolve(TEST_WORLD).resolve("level.dat");
        try {
            CompoundTag root = NbtIo.readCompressed(levelDat, NbtAccounter.unlimitedHeap());
            CompoundTag data = root.getCompound("Data");
            int newer = SharedConstants.getCurrentVersion().getDataVersion().getVersion() + 1;
            CompoundTag version = data.getCompound("Version");
            version.putInt("Id", newer);
            version.putString("Name", "1.21.1-newer-test");
            data.put("Version", version);
            data.putInt("DataVersion", newer);
            NbtIo.writeCompressed(root, levelDat);
        } catch (IOException e) {
            log("ERROR editing level.dat: " + e);
        }
    }

    private static void pressButton(Screen screen, String translationKey) {
        for (GuiEventListener child : screen.children()) {
            if (child instanceof Button button && button.getMessage().getContents() instanceof TranslatableContents tc
                    && tc.getKey().equals(translationKey)) {
                button.onPress();
                return;
            }
        }
        log("ERROR no '" + translationKey + "' button on " + screen.getClass().getSimpleName());
    }

    private static String readMode(Minecraft mc) {
        Path file = mc.gameDirectory.toPath().resolve("autotest-mode.txt");
        try {
            return Files.exists(file) ? Files.readString(file).strip() : "server";
        } catch (IOException e) {
            return "server";
        }
    }

    /**
     * First-launch check: with onboardAccessibility set in options.txt, logs every screen the game shows
     * on its own, and whether the --quickPlayMultiplayer join still happens afterwards.
     */
    private static void onboardingTick(Minecraft mc) {
        modeTicks++;
        String screenName = mc.screen == null ? (mc.level == null ? "none" : "in-world") : mc.screen.getClass().getSimpleName();
        if (!screenName.equals(lastScreenName)) {
            log("  onboarding run (skipAccessibilityOnboarding=" + Config.SKIP_ACCESSIBILITY_ONBOARDING.get()
                    + ") screen -> " + screenName);
            lastScreenName = screenName;
        }
        if (modeTicks == 20 * 25) {
            outputDir = mc.gameDirectory.toPath().resolve("autotest");
            try {
                Files.createDirectories(outputDir);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            capture("onboarding_skip_" + Config.SKIP_ACCESSIBILITY_ONBOARDING.get()).close();
            log("  onboarding run result: joined server=" + (mc.player != null)
                    + ", options.onboardAccessibility=" + mc.options.onboardAccessibility);
            mc.stop();
        }
    }

    private static void renderHover(ScreenEvent.Render.Post event) {
        if (hoverStyle != null && event.getScreen() instanceof ChatScreen) {
            int x = event.getScreen().width / 2;
            int y = event.getScreen().height / 2;
            event.getGuiGraphics().renderComponentHoverEffect(mc().font, hoverStyle, x, y);
        }
    }

    private static NativeImage capture(String name) {
        Screen current = mc().screen;
        log("  " + name + ": screen=" + (current == null ? (mc().level == null ? "none" : "in-world")
                : current.getClass().getSimpleName()));
        NativeImage image = Screenshot.takeScreenshot(mc().getMainRenderTarget());
        try {
            image.writeToFile(outputDir.resolve(name + ".png"));
        } catch (IOException e) {
            log("ERROR saving " + name + ": " + e);
        }
        return image;
    }

    private static void record(String name, NativeImage off, NativeImage on) {
        int width = Math.min(off.getWidth(), on.getWidth());
        int height = Math.min(off.getHeight(), on.getHeight());
        long changed = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int a = off.getPixelRGBA(x, y);
                int b = on.getPixelRGBA(x, y);
                if (Math.abs((a & 0xFF) - (b & 0xFF)) > CHANNEL_TOLERANCE
                        || Math.abs((a >> 8 & 0xFF) - (b >> 8 & 0xFF)) > CHANNEL_TOLERANCE
                        || Math.abs((a >> 16 & 0xFF) - (b >> 16 & 0xFF)) > CHANNEL_TOLERANCE) {
                    changed++;
                }
            }
        }
        double percent = 100.0 * changed / ((long) width * height);
        String line = String.format("%-28s changedPixels=%8d (%.2f%%)", name, changed, percent);
        results.add(line);
        log(line);
    }

    private static void writeResults() {
        try {
            Files.write(outputDir.resolve("results.txt"), results);
        } catch (IOException e) {
            log("ERROR writing results: " + e);
        }
    }

    private static void log(String message) {
        System.out.println("[AUTOTEST] " + message);
    }
}
