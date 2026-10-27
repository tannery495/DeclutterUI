package com.tannery495.declutterui.mixin;

import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ConfigurationScreen.ConfigurationListScreen.class)
public interface ConfigurationListScreenAccessor {
    @Accessor("key")
    String declutterui$getKey();
}
