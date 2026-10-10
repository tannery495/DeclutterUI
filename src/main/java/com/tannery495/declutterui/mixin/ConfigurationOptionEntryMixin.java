package com.tannery495.declutterui.mixin;

import com.tannery495.declutterui.Declutter;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.client.gui.components.OptionsList$Entry")
public abstract class ConfigurationOptionEntryMixin {
    @Shadow @Final private List<AbstractWidget> children;
    @Shadow @Final private Screen screen;

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void declutterui$layoutSettingsRow(GuiGraphics graphics, int index, int top, int left,
            int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick, CallbackInfo ci) {
        if (!(screen instanceof ConfigurationSectionScreenAccessor section)
                || !Declutter.MODID.equals(section.declutterui$getContext().modId()) || children.size() != 2) {
            return;
        }

        int margin = Math.max(16, screen.width / 12);
        AbstractWidget label = children.get(0);
        AbstractWidget control = children.get(1);
        if (section.declutterui$getContext().keylist().isEmpty()) {
            int buttonWidth = Math.min(240, screen.width - 2 * margin);
            control.setMessage(label.getMessage());
            control.setWidth(buttonWidth);
            control.setPosition((screen.width - buttonWidth) / 2, top);
            label.visible = false;
            label.active = false;
            control.render(graphics, mouseX, mouseY, partialTick);
            ci.cancel();
            return;
        }
        int controlWidth = Math.min(100, (screen.width - 2 * margin) / 3);
        int controlX = screen.width - margin - controlWidth;
        label.setWidth(Math.max(1, controlX - margin - 12));
        label.setPosition(margin, top);
        control.setWidth(controlWidth);
        control.setPosition(controlX, top);
        label.render(graphics, mouseX, mouseY, partialTick);
        control.render(graphics, mouseX, mouseY, partialTick);
        ci.cancel();
    }
}
