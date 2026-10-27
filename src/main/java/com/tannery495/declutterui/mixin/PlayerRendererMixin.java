package com.tannery495.declutterui.mixin;

import com.tannery495.declutterui.Config;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin {

    // With no below-name objective, vanilla draws just the name tag at its normal height.
    @Redirect(method = "renderNameTag", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/scores/Scoreboard;getDisplayObjective(Lnet/minecraft/world/scores/DisplaySlot;)Lnet/minecraft/world/scores/Objective;"))
    private Objective declutterui$hideBelowNameScore(Scoreboard scoreboard, DisplaySlot slot) {
        return Config.HIDE_BELOW_NAME_SCORES.get() ? null : scoreboard.getDisplayObjective(slot);
    }
}
