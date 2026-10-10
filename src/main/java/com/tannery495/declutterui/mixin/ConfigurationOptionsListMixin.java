package com.tannery495.declutterui.mixin;

import com.tannery495.declutterui.Declutter;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(OptionsList.class)
public abstract class ConfigurationOptionsListMixin {
    @Shadow @Final private OptionsSubScreen screen;

    @Inject(method = "getRowWidth", at = @At("HEAD"), cancellable = true)
    private void declutterui$expandSettingsRows(CallbackInfoReturnable<Integer> cir) {
        if (screen instanceof ConfigurationSectionScreenAccessor section
                && Declutter.MODID.equals(section.declutterui$getContext().modId())) {
            cir.setReturnValue(screen.width - 2 * Math.max(16, screen.width / 12));
        }
    }
}
