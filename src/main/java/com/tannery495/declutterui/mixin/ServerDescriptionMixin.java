package com.tannery495.declutterui.mixin;

import com.tannery495.declutterui.Config;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "net.minecraft.client.gui.screens.multiplayer.ServerSelectionList$OnlineServerEntry")
public abstract class ServerDescriptionMixin {
    @Shadow @Final private ServerData serverData;

    @Redirect(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/Font;split(Lnet/minecraft/network/chat/FormattedText;I)Ljava/util/List;"))
    private List<FormattedCharSequence> declutterui$hideDescription(Font font, FormattedText text, int width) {
        if (Config.HIDE_SERVER_DESCRIPTION.get()
                && serverData.state() != ServerData.State.UNREACHABLE) {
            return List.of();
        }
        return font.split(text, width);
    }
}
