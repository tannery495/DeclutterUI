package com.tannery495.declutterui.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.tannery495.declutterui.ChatTags;
import com.tannery495.declutterui.Config;
import com.tannery495.declutterui.DeclutterClient;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.client.renderer.entity.DisplayRenderer;
import net.minecraft.world.entity.Display;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DisplayRenderer.TextDisplayRenderer.class)
public abstract class TextDisplayRendererMixin {
    @Inject(method = "renderInner", at = @At("HEAD"), cancellable = true)
    private void declutterui$hideTextDisplay(Display.TextDisplay entity,
            Display.TextDisplay.TextRenderState state, PoseStack poseStack,
            MultiBufferSource buffer, int light, float partialTick, CallbackInfo ci) {
        if (Config.HIDE_FLOATING_HOLOGRAM_TEXT.get()
                || (Config.HIDE_PLAYER_NAME_TAGS.get() && DeclutterClient.isPlayerOrRidesPlayer(entity))) {
            ci.cancel();
        }
    }

    // Name-tag plugins can draw names as text displays riding the player; strip hidden tags there too.
    // Split per frame instead of using the cache, so toggling the option applies immediately.
    @Redirect(method = "renderInner(Lnet/minecraft/world/entity/Display$TextDisplay;Lnet/minecraft/world/entity/Display$TextDisplay$TextRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IF)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Display$TextDisplay;cacheDisplay(Lnet/minecraft/world/entity/Display$TextDisplay$LineSplitter;)Lnet/minecraft/world/entity/Display$TextDisplay$CachedInfo;"))
    private Display.TextDisplay.CachedInfo declutterui$stripTagsAboveHeads(Display.TextDisplay entity,
            Display.TextDisplay.LineSplitter splitter) {
        Display.TextDisplay.TextRenderState state = entity.textRenderState();
        if (state != null && Config.REMOVE_TAGS_ABOVE_HEADS.get() && DeclutterClient.isPlayerOrRidesPlayer(entity)) {
            Component stripped = ChatTags.strip(state.text(), false);
            if (stripped != null) return splitter.split(stripped, state.lineWidth());
        }
        return entity.cacheDisplay(splitter);
    }
}
