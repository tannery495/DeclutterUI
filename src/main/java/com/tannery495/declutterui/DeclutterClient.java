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
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ScreenEvent;
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
    static void onScreenInit(ScreenEvent.Init.Post event) {
        Screen screen = event.getScreen();

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
            AbstractWidget hiddenAdjacentButton = null;
            if (Config.HIDE_PLAYER_REPORTING.get()) {
                hiddenAdjacentButton = hideWidgetByKey(screen, "menu.playerReporting");
            }
            if (Config.HIDE_OPEN_TO_LAN.get()) {
                hiddenAdjacentButton = hideWidgetByKey(screen, "menu.shareToLan");
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
                if (child instanceof AbstractWidget widget && widget != realmsWidget && widget.getY() > realmsY) {
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
