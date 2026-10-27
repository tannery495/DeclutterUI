package com.tannery495.declutterui.testmod;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket;
import net.minecraft.network.protocol.common.ClientboundServerLinksPacket;
import net.minecraft.server.ServerLinks;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.network.protocol.game.ClientboundTabListPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Dev-only helper: {@code /dtest <feature>} makes the server send each HUD element
 * DeclutterUI can hide, so the client options can be checked against a real server.
 */
@Mod("declutteruitest")
public class DeclutterTestMod {

    public DeclutterTestMod() {
        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, event -> register(event.getDispatcher()));
        if (FMLEnvironment.dist.isClient() && Boolean.getBoolean("declutterui.autotest")) {
            AutoTestClient.init();
        }
    }

    private interface Action {
        void run(ServerPlayer player, CommandSourceStack source);
    }

    private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("dtest")
                .executes(ctx -> help(ctx.getSource()))
                .then(feature("title", DeclutterTestMod::title))
                .then(feature("actionbar", DeclutterTestMod::actionBar))
                .then(feature("bossbar", DeclutterTestMod::bossBar))
                .then(feature("sidebar", DeclutterTestMod::sidebar))
                .then(feature("tab", DeclutterTestMod::tabList))
                .then(feature("glow", DeclutterTestMod::glow))
                .then(feature("hologram", DeclutterTestMod::hologram))
                .then(feature("chat", DeclutterTestMod::chat))
                .then(feature("toasts", DeclutterTestMod::toasts))
                .then(feature("advancement", DeclutterTestMod::advancementToast))
                .then(feature("recipe", DeclutterTestMod::recipeToast))
                .then(feature("reset", DeclutterTestMod::reset))
                .then(feature("belowname", DeclutterTestMod::belowName))
                .then(feature("camera", DeclutterTestMod::camera))
                .then(feature("reward", DeclutterTestMod::rewardMessages))
                .then(feature("links", DeclutterTestMod::serverLinks))
                .then(feature("nametags", DeclutterTestMod::nameTagScenario))
                .then(feature("rankchat", DeclutterTestMod::rankChat))
                .then(feature("sidebarfixed", (player, source) -> {
                    sidebar(player, source);
                    run(player, "scoreboard objectives modify dtest numberformat fixed {\"text\":\"\u2605\",\"color\":\"gold\"}");
                }))
                .then(feature("sidebarblank", (player, source) -> {
                    sidebar(player, source);
                    run(player, "scoreboard objectives modify dtest numberformat blank");
                }))
                .then(feature("nolinks", (player, source) ->
                        player.connection.send(new ClientboundServerLinksPacket(List.of()))))
                .then(feature("survival", (player, source) -> run(player, "gamemode survival @s")))
                .then(feature("creative", (player, source) -> run(player, "gamemode creative @s")))
                .then(feature("items", DeclutterTestMod::items))
                .then(feature("pack", DeclutterTestMod::resourcePack))
                .then(feature("all", DeclutterTestMod::all))
                .then(feature("clear", DeclutterTestMod::clear)));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> feature(String name, Action action) {
        return Commands.literal(name).executes(ctx -> execute(ctx, action));
    }

    private static int execute(CommandContext<CommandSourceStack> ctx, Action action) throws CommandSyntaxException {
        action.run(ctx.getSource().getPlayerOrException(), ctx.getSource());
        return 1;
    }

    private static int help(CommandSourceStack source) {
        source.sendSystemMessage(Component.literal(
                "/dtest <title|actionbar|bossbar|sidebar|tab|glow|hologram|chat|toasts|advancement|recipe|items|pack|all|clear|reset>"));
        return 1;
    }

    /** Runs a vanilla command as the player with full permissions, so no op is needed. */
    private static void run(ServerPlayer player, String command) {
        CommandSourceStack source = player.createCommandSourceStack().withPermission(4).withSuppressedOutput();
        player.server.getCommands().performPrefixedCommand(source, command);
    }

    private static void title(ServerPlayer player, CommandSourceStack source) {
        run(player, "title @s times 10 100 20");
        run(player, "title @s subtitle {\"text\":\"A playtime reward has been unlocked!\"}");
        run(player, "title @s title {\"text\":\"Reward Unlocked!\",\"color\":\"green\",\"bold\":true}");
    }

    private static void actionBar(ServerPlayer player, CommandSourceStack source) {
        run(player, "title @s actionbar {\"text\":\"Action bar message from the server\",\"color\":\"gold\"}");
    }

    private static void bossBar(ServerPlayer player, CommandSourceStack source) {
        run(player, "bossbar add dtest:bar {\"text\":\"Test Boss Bar\",\"color\":\"light_purple\"}");
        run(player, "bossbar set dtest:bar color purple");
        run(player, "bossbar set dtest:bar value 60");
        run(player, "bossbar set dtest:bar players @a");
    }

    private static void sidebar(ServerPlayer player, CommandSourceStack source) {
        run(player, "scoreboard objectives add dtest dummy {\"text\":\"Test Sidebar\",\"color\":\"yellow\"}");
        run(player, "scoreboard players set Coins dtest 1250");
        run(player, "scoreboard players set Kills dtest 42");
        run(player, "scoreboard players set Rank:_VIP dtest 13");
        run(player, "scoreboard objectives setdisplay sidebar dtest");
    }

    private static void tabList(ServerPlayer player, CommandSourceStack source) {
        player.connection.send(new ClientboundTabListPacket(
                Component.literal("Welcome to the Test Server!\nHeader line two"),
                Component.literal("Footer: visit example.invalid")));
    }

    private static void glow(ServerPlayer player, CommandSourceStack source) {
        run(player, "summon minecraft:pig ^ ^ ^4 {Glowing:1b,NoAI:1b,Tags:[\"dtest\"]}");
        run(player, "summon minecraft:cow ^2 ^ ^4 {Glowing:1b,NoAI:1b,Tags:[\"dtest\"]}");
    }

    private static void hologram(ServerPlayer player, CommandSourceStack source) {
        run(player, "summon minecraft:text_display ^-2 ^2 ^5 {billboard:\"center\","
                + "text:'{\"text\":\"Text display hologram\",\"color\":\"aqua\"}',Tags:[\"dtest\"]}");
        run(player, "summon minecraft:armor_stand ^ ^1 ^6 {Invisible:1b,Marker:1b,NoGravity:1b,CustomNameVisible:1b,"
                + "CustomName:'{\"text\":\"Armor stand hologram\",\"color\":\"red\"}',Tags:[\"dtest\"]}");
    }

    private static void chat(ServerPlayer player, CommandSourceStack source) {
        String name = player.getGameProfile().getName();
        // Unsigned player chat on an offline server shows the gray "not secure" indicator bar.
        run(player, "say Unsigned chat message - look for the indicator bar on the left");
        // Open chat and hover the names: a show_entity profile popup, then a /msg show_text tooltip.
        run(player, "tellraw @s [{\"text\":\"Hover this name: \"},{\"selector\":\"@s\"}]");
        run(player, "tellraw @s [{\"text\":\"Hover this name too: \"},{\"text\":\"" + name + "\",\"color\":\"aqua\","
                + "\"hoverEvent\":{\"action\":\"show_text\",\"contents\":\"Click to message " + name + "\"},"
                + "\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\"/msg " + name + " \"}}]");
    }

    private static void toasts(ServerPlayer player, CommandSourceStack source) {
        advancementToast(player, source);
        recipeToast(player, source);
    }

    private static void advancementToast(ServerPlayer player, CommandSourceStack source) {
        run(player, "advancement revoke @s only minecraft:story/mine_stone");
        run(player, "advancement grant @s only minecraft:story/mine_stone");
    }

    private static void recipeToast(ServerPlayer player, CommandSourceStack source) {
        run(player, "recipe take @s minecraft:diamond_pickaxe");
        run(player, "recipe give @s minecraft:diamond_pickaxe");
    }

    private static void belowName(ServerPlayer player, CommandSourceStack source) {
        run(player, "scoreboard objectives add dtest_hp health {\"text\":\"\u2764\",\"color\":\"red\"}");
        run(player, "scoreboard objectives setdisplay below_name dtest_hp");
    }

    /** An invisible armor stand 4 blocks in front of the reset spot, facing back at the player. */
    private static void camera(ServerPlayer player, CommandSourceStack source) {
        run(player, "summon minecraft:armor_stand 0.5 -60 4.5 {Invisible:1b,NoGravity:1b,Rotation:[180f,0f],Tags:[\"dtest\",\"dtest_camera\"]}");
    }

    /** The Megasurvival-style reward announcement from the user's screenshot, plus an ordinary server message. */
    private static void rewardMessages(ServerPlayer player, CommandSourceStack source) {
        run(player, "tellraw @s [{\"text\":\"Hey! \",\"color\":\"green\"},{\"text\":\"Thanks for playing at \",\"color\":\"white\"},"
                + "{\"text\":\"Megasurvival\",\"color\":\"aqua\"},{\"text\":\". A new reward has been unlocked. To view, click on this message. :)\",\"color\":\"white\"}]");
        run(player, "tellraw @s [{\"text\":\"Rewards:\",\"color\":\"aqua\"},{\"text\":\"\\n- Haste I & Speed I\",\"color\":\"white\"}]");
        run(player, "tellraw @s {\"text\":\"Ordinary server message that should stay\",\"color\":\"yellow\"}");
    }

    /** Server links (1.21+) change the pause menu: Feedback... and Server Links... replace the feedback buttons. */
    private static void serverLinks(ServerPlayer player, CommandSourceStack source) {
        ServerLinks links = new ServerLinks(List.of(
                ServerLinks.Entry.knownType(ServerLinks.KnownLinkType.WEBSITE, URI.create("https://example.invalid")),
                ServerLinks.Entry.knownType(ServerLinks.KnownLinkType.COMMUNITY, URI.create("https://example.invalid/discord"))));
        player.connection.send(new ClientboundServerLinksPacket(links.untrust()));
    }

    /**
     * Every way a server commonly puts a name above a player: an OWNER team prefix on the real name tag,
     * a TAB-plugin-style invisible armor stand riding the player, a text display riding the player,
     * a below-name score, plus a named pet that must stay visible.
     */
    private static void nameTagScenario(ServerPlayer player, CommandSourceStack source) {
        run(player, "team add dtest_owner");
        run(player, "team modify dtest_owner prefix {\"text\":\"OWNER \",\"color\":\"red\"}");
        run(player, "team join dtest_owner @s");
        belowName(player, source);
        run(player, "summon minecraft:armor_stand 0.5 -60 0.5 {Invisible:1b,Marker:1b,CustomNameVisible:1b,"
                + "CustomName:'{\"text\":\"[VIP] TAB Name\",\"color\":\"gold\"}',Tags:[\"dtest\",\"dtest_rider\"]}");
        mountOnPlayer(player, "dtest_rider");
        run(player, "summon minecraft:text_display 0.5 -60 0.5 {billboard:\"center\",translation:[0f,0.9f,0f],"
                + "text:'{\"text\":\"[VIP] Text Display Name\",\"color\":\"aqua\"}',Tags:[\"dtest\",\"dtest_rider2\"]}");
        mountOnPlayer(player, "dtest_rider2");
        run(player, "summon minecraft:pig 2.5 -60 1.5 {NoAI:1b,CustomNameVisible:1b,Rotation:[180f,0f],"
                + "CustomName:'{\"text\":\"Rex the pet\"}',Tags:[\"dtest\"]}");
    }

    /**
     * Vanilla /ride refuses to mount entities on players; plugins do it in code, so do the same here.
     * Commands run from inside /dtest are queued until it returns, so mount once the summons have run.
     */
    private static void mountOnPlayer(ServerPlayer player, String tag) {
        player.server.execute(() -> mountNow(player, tag));
    }

    private static void mountNow(ServerPlayer player, String tag) {
        for (Entity entity : player.serverLevel().getAllEntities()) {
            if (entity.getTags().contains(tag)) {
                boolean mounted = entity.startRiding(player, true);
                System.out.println("[DTEST] mount " + tag + " (" + entity.getType().toShortString() + ") on player: "
                        + mounted + ", passengers now " + player.getPassengers().size());
            }
        }
        // Vanilla only tells *other* players what rides you. The test views its own player through a
        // camera, so send the same update to it to see what other players see.
        player.connection.send(new ClientboundSetPassengersPacket(player));
    }

    /** Chat lines in the styles different servers use for ranks. */
    private static void rankChat(ServerPlayer player, CommandSourceStack source) {
        run(player, "tellraw @s [{\"text\":\"OWNER \",\"color\":\"red\",\"bold\":true},{\"text\":\"Lover\",\"color\":\"white\"},{\"text\":\": welcome!\",\"color\":\"gray\"}]");
        run(player, "tellraw @s [{\"text\":\"[2] [3] \",\"color\":\"dark_gray\"},{\"text\":\".HundredBead2106\",\"color\":\"white\"},{\"text\":\": hi does anybody know how to buy spawners\"}]");
        run(player, "tellraw @s [{\"text\":\"[\",\"color\":\"white\"},{\"text\":\"VIP\",\"color\":\"green\"},{\"text\":\"] Steve\",\"color\":\"white\"},{\"text\":\" \u00bb the OWNER said hi\",\"color\":\"gray\"}]");
        run(player, "tellraw @s {\"text\":\"\u1d0f\u1d21\u0274\u1d07\u0280 Lover joined the game\",\"color\":\"yellow\"}");
    }

    /** Puts the player in a fixed, well-lit spot so test screenshots are comparable. */
    private static void reset(ServerPlayer player, CommandSourceStack source) {
        run(player, "gamerule doDaylightCycle false");
        run(player, "gamerule doWeatherCycle false");
        run(player, "time set noon");
        run(player, "weather clear");
        run(player, "gamemode creative @s");
        run(player, "clear @s");
        run(player, "tp @s 0.5 -60 0.5 0 0");
    }

    private static void items(ServerPlayer player, CommandSourceStack source) {
        run(player, "item replace entity @s hotbar.0 with minecraft:diamond_sword");
        run(player, "item replace entity @s hotbar.1 with minecraft:golden_apple");
        run(player, "item replace entity @s hotbar.2 with minecraft:crafting_table");
        run(player, "item replace entity @s hotbar.3 with minecraft:furnace");
        source.sendSystemMessage(Component.literal(
                "Scroll the hotbar to see item names; open the inventory for the recipe book button."));
    }

    private static void resourcePack(ServerPlayer player, CommandSourceStack source) {
        // Served slowly so the download progress toast stays up for a few seconds; the hash can never match,
        // so the download then fails ("pack(s) failed to download") and no pack is applied.
        player.connection.send(new ClientboundResourcePackPushPacket(UUID.randomUUID(),
                SlowPackServer.url(), "0".repeat(40), false,
                Optional.of(Component.literal("DeclutterUI test pack (the download fails on purpose)"))));
    }

    private static void all(ServerPlayer player, CommandSourceStack source) {
        for (Action action : List.<Action>of(DeclutterTestMod::title, DeclutterTestMod::actionBar,
                DeclutterTestMod::bossBar, DeclutterTestMod::sidebar, DeclutterTestMod::tabList,
                DeclutterTestMod::glow, DeclutterTestMod::hologram, DeclutterTestMod::chat,
                DeclutterTestMod::toasts, DeclutterTestMod::items)) {
            action.run(player, source);
        }
    }

    private static void clear(ServerPlayer player, CommandSourceStack source) {
        run(player, "title @s clear");
        run(player, "bossbar remove dtest:bar");
        run(player, "scoreboard objectives remove dtest");
        run(player, "scoreboard objectives remove dtest_hp");
        run(player, "team remove dtest_owner");
        run(player, "kill @e[tag=dtest]");
        player.connection.send(new ClientboundTabListPacket(Component.empty(), Component.empty()));
    }
}
