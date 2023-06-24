package com.rimo.sfcr.core;

import com.rimo.sfcr.SFCReClient;
import com.rimo.sfcr.SFCReMain;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
public class ForwardRenderer {
    static GPURenderer gpuRenderer = new GPURenderer();

    public static void render(@Nullable ClientWorld world, MatrixStack matrices, Matrix4f projectionMatrix, float tickDelta, double cameraX, double cameraY, double cameraZ, CallbackInfo ci) {
        if (world == null) return;
        if (SFCReMain.config.isEnableMod() && world.getDimension().hasSkyLight()) {
            if (SFCReMain.config.isEnableGPU()) {
                gpuRenderer.ensureInit();
                gpuRenderer.render(world, matrices, projectionMatrix, tickDelta, cameraX, cameraY, cameraZ);
            }
            SFCReClient.RENDERER.render(world, matrices, projectionMatrix, tickDelta, cameraX, cameraY, cameraZ);
            ci.cancel();
        }
    }

}
