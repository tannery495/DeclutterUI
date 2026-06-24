package com.tannery495.declutterui.mixin;

import com.tannery495.declutterui.Config;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.CommonButtons;
import net.minecraft.client.gui.components.SplashRenderer;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin {

    @Redirect(
        method = "init",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/components/CommonButtons;language(ILnet/minecraft/client/gui/components/Button$OnPress;Z)Lnet/minecraft/client/gui/components/SpriteIconButton;"
        )
    )
    private SpriteIconButton hideLanguageButton(
            int width,
            net.minecraft.client.gui.components.Button.OnPress onPress,
            boolean showLabel
    ) {
        SpriteIconButton button = CommonButtons.language(width, onPress, showLabel);
        if (Config.HIDE_LANGUAGE_BUTTON.get()) {
            button.visible = false;
            button.active = false;
        }
        return button;
    }

    @Redirect(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/components/SplashRenderer;render(Lnet/minecraft/client/gui/GuiGraphics;ILnet/minecraft/client/gui/Font;I)V"
        )
    )
    private void skipSplashRender(SplashRenderer splash, GuiGraphics guiGraphics, int screenWidth, Font font, int color) {
        if (!Config.HIDE_SPLASH_TEXT.get()) {
            splash.render(guiGraphics, screenWidth, font, color);
        }
    }
}
