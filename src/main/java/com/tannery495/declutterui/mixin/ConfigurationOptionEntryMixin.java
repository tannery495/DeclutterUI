package com.tannery495.declutterui.mixin;

import com.tannery495.declutterui.Declutter;
import com.tannery495.declutterui.ListEditorStyle;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
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
        label.setMessage(withoutEllipsis(label.getMessage()));
        control.setMessage(withoutEllipsis(control.getMessage()));
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
        // List editors (chat phrases, chat tags): a wide text box and a remove button per row.
        String listKey = ListEditorStyle.listKey(screen);
        if (listKey != null) {
            int removeSize = 20;
            int boxRight = screen.width - margin - removeSize - 6;
            control.setWidth(Math.max(60, boxRight - margin));
            control.setPosition(margin, top);
            if (label instanceof ContainerEventHandler row) {
                // Order doesn't matter for a filter: hide the move arrows and the index, keep remove.
                // They're never rendered here, so park them off-screen and inactive to keep them unclickable.
                label.setWidth(removeSize);
                label.setPosition(boxRight + 6, top);
                AbstractWidget remove = null;
                for (GuiEventListener child : row.children()) {
                    if (!(child instanceof AbstractWidget widget)) continue;
                    if (ListEditorStyle.isRemoveButton(widget)) {
                        remove = widget;
                    } else {
                        widget.visible = false;
                        widget.active = false;
                        widget.setX(-10000);
                    }
                }
                control.render(graphics, mouseX, mouseY, partialTick);
                if (remove != null) remove.render(graphics, mouseX, mouseY, partialTick);
                ListEditorStyle.renderShortPhraseWarning(graphics, Minecraft.getInstance().font, listKey, control);
            } else {
                // The last row: an empty label and the add button.
                control.setMessage(ListEditorStyle.addLabel(listKey));
                control.render(graphics, mouseX, mouseY, partialTick);
            }
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

    private static Component withoutEllipsis(Component message) {
        String text = message.getString();
        String trimmed = text.endsWith("...") ? text.substring(0, text.length() - 3)
                : text.endsWith("\u2026") ? text.substring(0, text.length() - 1) : null;
        return trimmed == null ? message : Component.literal(trimmed.stripTrailing()).withStyle(message.getStyle());
    }
}
