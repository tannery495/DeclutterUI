package com.tannery495.declutterui.mixin;

import net.neoforged.neoforge.client.gui.ConfigurationScreen.ConfigurationSectionScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ConfigurationSectionScreen.class)
public interface ConfigurationSectionScreenAccessor {
    @Accessor("context")
    ConfigurationSectionScreen.Context declutterui$getContext();
}
