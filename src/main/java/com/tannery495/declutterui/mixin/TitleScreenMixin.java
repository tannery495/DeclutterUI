package com.tannery495.declutterui.mixin;

import com.tannery495.declutterui.Config;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.CommonButtons;
import net.minecraft.client.gui.components.SplashRenderer;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
        method = "extractRenderState",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/components/SplashRenderer;extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;ILnet/minecraft/client/gui/Font;F)V"
        )
    )
    private void skipSplashRender(SplashRenderer splash, GuiGraphicsExtractor graphics, int screenWidth, Font font, float alpha) {
        if (!Config.HIDE_SPLASH_TEXT.get()) {
            splash.extractRenderState(graphics, screenWidth, font, alpha);
        }
    }

    @Inject(method = {"lambda$extractRenderState$0", "lambda$extractRenderState$1"}, at = @At("HEAD"), cancellable = true)
    private void hideVersionText(GuiGraphicsExtractor graphics, int alpha, Integer line, String text, CallbackInfo ci) {
        if (Config.HIDE_VERSION_TEXT.get()) {
            ci.cancel();
        }
    }
}
