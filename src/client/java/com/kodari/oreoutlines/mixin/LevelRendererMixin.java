package com.kodari.oreoutlines.mixin;

import com.kodari.oreoutlines.OreOutlinesClient;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.gizmos.Gizmos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
    @Shadow public abstract Gizmos.TemporaryCollection collectPerFrameRenderThreadGizmos();

    @Inject(method = "submitFeatures", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;finalizeGizmoCollection()V"))
    private void oreOutlines$collect(CallbackInfo ci) {
        try (Gizmos.TemporaryCollection ignored = collectPerFrameRenderThreadGizmos()) {
            OreOutlinesClient.draw();
        }
    }
}