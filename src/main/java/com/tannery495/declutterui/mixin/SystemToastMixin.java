package com.tannery495.declutterui.mixin;

import com.tannery495.declutterui.Config;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SystemToast.class)
public abstract class SystemToastMixin {
    @Inject(method = "addOrUpdate", at = @At("HEAD"), cancellable = true)
    private static void declutterui$hideResourcePackDownloadToasts(
            ToastComponent toastManager, SystemToast.SystemToastId id,
            Component title, @Nullable Component message, CallbackInfo ci) {
        if (!(title.getContents() instanceof TranslatableContents contents)) return;
        if (Config.HIDE_RESOURCE_PACK_DOWNLOAD_PROGRESS.get() && contents.getKey().equals("download.pack.title")) {
            ci.cancel();
        } else if (Config.SUPPRESS_RESOURCE_PACK_ERROR_TOASTS.get() && contents.getKey().equals("download.pack.failed")) {
            // The failure reuses the progress toast; close it too so the last percentage doesn't linger.
            SystemToast.forceHide(toastManager, id);
            ci.cancel();
        }
    }
}
