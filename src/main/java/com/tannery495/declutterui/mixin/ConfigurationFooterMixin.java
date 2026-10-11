package com.tannery495.declutterui.mixin;

import com.tannery495.declutterui.Declutter;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.gui.ConfigurationScreen.ConfigurationSectionScreen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ConfigurationSectionScreen.class)
public abstract class ConfigurationFooterMixin extends OptionsSubScreen {
    protected ConfigurationFooterMixin(Screen parent, Options options, Component title) {
        super(parent, options, title);
    }
    @Shadow @Final protected ConfigurationSectionScreen.Context context;
    @Shadow @Final protected Button doneButton;
    @Shadow protected Button undoButton;
    @Shadow protected Button resetButton;

    @Inject(method = "addFooter", at = @At("HEAD"), cancellable = true)
    private void declutterui$compactCategoryFooter(CallbackInfo ci) {
        if (Declutter.MODID.equals(context.modId()) && undoButton == null && resetButton == null) {
            doneButton.setWidth(150);
            layout.addToFooter(doneButton);
            ci.cancel();
        }
    }

    @Inject(method = "addFooter", at = @At("TAIL"))
    private void declutterui$compactFooter(CallbackInfo ci) {
        if (!Declutter.MODID.equals(context.modId())) return;
        // Keep the footer comfortable while fitting smaller GUI sizes.
        int buttonWidth = Math.min(90, (width - 32) / 3);
        doneButton.setWidth(undoButton == null && resetButton == null ? 150 : buttonWidth);
        if (undoButton != null) undoButton.setWidth(buttonWidth);
        if (resetButton != null) resetButton.setWidth(buttonWidth);
    }
}
