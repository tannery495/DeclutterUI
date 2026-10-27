package com.tannery495.declutterui.mixin;

import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(OptionsSubScreen.class)
public interface OptionsSubScreenInvoker {
    @Invoker("repositionElements")
    void declutterui$repositionElements();
}
