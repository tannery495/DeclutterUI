package com.tannery495.declutterui;

import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.toasts.AdvancementToast;
import net.minecraft.client.gui.components.toasts.RecipeToast;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.components.toasts.TutorialToast;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import com.tannery495.declutterui.mixin.OptionsSubScreenInvoker;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.client.event.ToastAddEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = Declutter.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = Declutter.MODID, value = Dist.CLIENT)
public class DeclutterClient {
    public DeclutterClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onChatReceived(ClientChatReceivedEvent event) {
        if (ChatFilter.shouldHide(event)) {
            event.setCanceled(true);
            return;
        }
        // Action bar text arrives as an overlay system message; tags are only removed from chat lines.
        if (event instanceof ClientChatReceivedEvent.System system && system.isOverlay()) return;
        Component stripped = ChatTags.strip(event.getMessage(), true);
        if (stripped != null) {
            event.setMessage(stripped);
        }
    }

    @SubscribeEvent
    static void onToastAdd(ToastAddEvent event) {
        var toast = event.getToast();
        if (Config.SUPPRESS_ADVANCEMENT_TOASTS.get() && toast instanceof AdvancementToast) {
            event.setCanceled(true);
        } else if (Config.SUPPRESS_RECIPE_TOASTS.get() && toast instanceof RecipeToast) {
            event.setCanceled(true);
        } else if (Config.SUPPRESS_TUTORIAL_TOASTS.get() && toast instanceof TutorialToast) {
            event.setCanceled(true);
        } else if (Config.SUPPRESS_NARRATOR_TOAST.get()
                && toast instanceof SystemToast sys
                && sys.getToken() == SystemToast.SystemToastId.NARRATOR_TOGGLE) {
            event.setCanceled(true);
        } else if (Config.SUPPRESS_UNSAFE_SERVER_TOAST.get()
                && toast instanceof SystemToast sys
                && sys.getToken() == SystemToast.SystemToastId.UNSECURE_SERVER_WARNING) {
            event.setCanceled(true);
        } else if (Config.SUPPRESS_WORLD_BACKUP_TOAST.get()
                && toast instanceof SystemToast sys
                && sys.getToken() == SystemToast.SystemToastId.WORLD_BACKUP) {
            event.setCanceled(true);
        } else if (Config.SUPPRESS_RESOURCE_PACK_ERROR_TOASTS.get()
                && toast instanceof SystemToast sys
                && (sys.getToken() == SystemToast.SystemToastId.PACK_LOAD_FAILURE
                || sys.getToken() == SystemToast.SystemToastId.PACK_COPY_FAILURE
                || sys.getToken() == SystemToast.SystemToastId.FILE_DROP_FAILURE)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    static void onScreenRender(ScreenEvent.Render.Post event) {
        String listKey = ListEditorStyle.listKey(event.getScreen());
        if (listKey != null) {
            ListEditorStyle.renderHelp(event.getGuiGraphics(), Minecraft.getInstance().font, event.getScreen(), listKey);
        }
    }

    @SubscribeEvent
    static void onScreenInit(ScreenEvent.Init.Post event) {
        Screen screen = event.getScreen();

        // DeclutterUI's list editors get a taller header so one line of help fits under the title.
        if (ListEditorStyle.listKey(screen) != null && screen instanceof OptionsSubScreen options) {
            options.layout.setHeaderHeight(ListEditorStyle.HEADER_HEIGHT);
            ((OptionsSubScreenInvoker) screen).declutterui$repositionElements();
        }

        if (Config.HIDE_RECIPE_BOOK.get() && screen instanceof RecipeUpdateListener) {
            for (GuiEventListener child : screen.children()) {
                if (child instanceof ImageButton btn) {
                    btn.visible = false;
                    btn.active = false;
                }
            }
        }

        if (screen instanceof TitleScreen) {
            if (Config.HIDE_ACCESSIBILITY_BUTTON.get()) {
                hideWidgetByKey(screen, "options.accessibility");
            }
            if (Config.HIDE_COPYRIGHT.get()) {
                hideWidgetByKey(screen, "title.credits");
            }
        }

        if (screen instanceof net.minecraft.client.gui.screens.PauseScreen) {
            // Servers that send links get a Feedback... submenu button in place of the two feedback buttons.
            if (Config.HIDE_FEEDBACK_BUTTONS.get()) {
                AbstractWidget feedback = hideWidgetByKey(screen, "menu.feedback");
                AbstractWidget serverLinks = feedback == null ? null : findWidgetByKey(screen, "menu.server_links");
                if (serverLinks != null) {
                    int right = serverLinks.getRight();
                    serverLinks.setX(feedback.getX());
                    serverLinks.setWidth(right - feedback.getX());
                }
            }

            AbstractWidget hiddenAdjacentButton = null;
            if (Config.HIDE_PLAYER_REPORTING.get()) {
                hiddenAdjacentButton = hideWidgetByKey(screen, "menu.playerReporting");
            }
            if (Config.HIDE_OPEN_TO_LAN.get()) {
                // Multiplayer has no LAN button; keep the Player Reporting slot in that case.
                AbstractWidget lanButton = hideWidgetByKey(screen, "menu.shareToLan");
                if (lanButton != null) hiddenAdjacentButton = lanButton;
            }
            if (hiddenAdjacentButton != null) {
                moveModsBesideOptions(screen, hiddenAdjacentButton);
            }
        }

        if (Config.HIDE_ONLINE_OPTIONS.get() && screen instanceof OptionsScreen) {
            hideWidgetByKey(screen, "options.online");
        }

        if (!Config.HIDE_REALMS.get()) return;

        AbstractWidget realmsWidget = null;
        for (GuiEventListener child : screen.children()) {
            if (child instanceof AbstractWidget widget && isRealmsWidget(widget)) {
                realmsWidget = widget;
                break;
            }
        }

        if (realmsWidget != null) {
            int realmsY = realmsWidget.getY();
            int shift = realmsWidget.getHeight() + 4;
            realmsWidget.visible = false;
            realmsWidget.active = false;
            for (GuiEventListener child : screen.children()) {
                // Leave bottom-anchored text (the copyright line) where it is.
                if (child instanceof AbstractWidget widget && widget != realmsWidget && widget.getY() > realmsY
                        && widget.getBottom() < screen.height) {
                    widget.setY(widget.getY() - shift);
                }
            }
        }

        for (GuiEventListener child : screen.children()) {
            if (child instanceof AbstractSelectionList<?> list) {
                for (GuiEventListener entry : list.children()) {
                    if (entry instanceof ContainerEventHandler container) {
                        for (GuiEventListener nested : container.children()) {
                            if (nested instanceof AbstractWidget widget && isRealmsWidget(widget)) {
                                widget.visible = false;
                                widget.active = false;
                            }
                        }
                    }
                }
            }
        }
    }

    private static AbstractWidget hideWidgetByKey(Screen screen, String key) {
        for (GuiEventListener child : screen.children()) {
            if (child instanceof AbstractWidget widget && hasTranslationKey(widget.getMessage(), key)) {
                widget.visible = false;
                widget.active = false;
                return widget;
            }
        }
        return null;
    }

    @SubscribeEvent
    static void onRenderNameTag(RenderNameTagEvent event) {
        // Covers players, player-like NPCs, and name-tag entities that servers mount on players.
        if (Config.HIDE_PLAYER_NAME_TAGS.get() && isPlayerOrRidesPlayer(event.getEntity())) {
            event.setCanRender(TriState.FALSE);
            return;
        }
        if (Config.REMOVE_TAGS_ABOVE_HEADS.get()) {
            Component stripped = ChatTags.strip(event.getContent(), false);
            if (stripped != null) {
                event.setContent(stripped);
            }
        }
        if (Config.HIDE_FLOATING_HOLOGRAM_TEXT.get()
                && event.getEntity() instanceof ArmorStand armorStand
                && armorStand.isInvisible()
                && armorStand.hasCustomName()) {
            event.setCanRender(TriState.FALSE);
        }
    }

    public static boolean isPlayerOrRidesPlayer(Entity entity) {
        return entity instanceof Player || (entity.isPassenger() && entity.getRootVehicle() instanceof Player);
    }

    private static void moveModsBesideOptions(Screen screen, AbstractWidget hiddenNeighbor) {
        AbstractWidget modsButton = findWidgetByKey(screen, "fml.menu.mods");
        if (modsButton == null) return;

        int vacatedRowY = modsButton.getY();
        int rowShift = modsButton.getHeight() + 4;
        modsButton.setX(hiddenNeighbor.getX());
        modsButton.setY(hiddenNeighbor.getY());
        modsButton.setWidth(hiddenNeighbor.getWidth());

        for (GuiEventListener child : screen.children()) {
            if (child instanceof AbstractWidget widget && widget != modsButton && widget.getY() > vacatedRowY) {
                widget.setY(widget.getY() - rowShift);
            }
        }
    }

    private static AbstractWidget findWidgetByKey(Screen screen, String key) {
        for (GuiEventListener child : screen.children()) {
            if (child instanceof AbstractWidget widget && hasTranslationKey(widget.getMessage(), key)) {
                return widget;
            }
        }
        return null;
    }

    private static boolean hasTranslationKey(Component component, String key) {
        return component.getContents() instanceof TranslatableContents tc && tc.getKey().equals(key);
    }

    private static boolean isRealmsWidget(AbstractWidget widget) {
        return containsRealmsKey(widget.getMessage());
    }

    private static boolean containsRealmsKey(Component component) {
        if (component.getContents() instanceof TranslatableContents tc) {
            String key = tc.getKey();
            if (key.equals("menu.online") || key.startsWith("options.realms")) return true;
            for (Object arg : tc.getArgs()) {
                if (arg instanceof Component c && containsRealmsKey(c)) return true;
            }
        }
        for (Component sibling : component.getSiblings()) {
            if (containsRealmsKey(sibling)) return true;
        }
        return false;
    }
}
