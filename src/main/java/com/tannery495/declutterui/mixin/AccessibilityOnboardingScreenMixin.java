package com.tannery495.declutterui.mixin;

import com.tannery495.declutterui.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.Function;

@Mixin(Minecraft.class)
public class AccessibilityOnboardingScreenMixin {

    @Shadow @Final public Options options;

    // Stop the onboarding screen from being queued at startup, so the rest of the startup chain
    // (ban notices, --quickPlay joins) still runs instead of being replaced by the title screen.
    @Inject(method = "addInitialScreens", at = @At("HEAD"))
    private void skipAccessibilityOnboarding(List<Function<Runnable, Screen>> screens, CallbackInfo ci) {
        if (Config.SKIP_ACCESSIBILITY_ONBOARDING.get() && options.onboardAccessibility) {
            options.onboardAccessibility = false;
            options.save();
        }
    }
}
