package com.rimo.sfcr.mixin;

import com.rimo.sfcr.core.ForwardRenderer;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "me.jellysquid.mods.sodium.client.render.immediate.CloudRenderer")
public class SodiumWorldRendererMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    public void renderSFC(@Nullable ClientWorld world, ClientPlayerEntity player, MatrixStack matrices, Matrix4f projectionMatrix, float ticks, float tickDelta, double cameraX, double cameraY, double cameraZ, CallbackInfo ci) {
        ForwardRenderer.render(world, player, matrices, projectionMatrix, tickDelta, cameraX, cameraY, cameraZ, ci);
    }
}
