package com.rimo.sfcr.core;

import com.rimo.sfcr.SFCReClient;
import com.rimo.sfcr.SFCReMain;
import com.rimo.sfcr.core.gpu.GpuRenderer;
import com.rimo.sfcr.util.comp.GpuGenNoiseGroupOffset;
import com.rimo.sfcr.util.comp.GpuSimplexNoise;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
public class ForwardRenderer {
    static GpuRenderer gpuRenderer;

    public static void render(@Nullable ClientWorld world, ClientPlayerEntity player, MatrixStack matrices, Matrix4f projectionMatrix, float tickDelta, double cameraX, double cameraY, double cameraZ, CallbackInfo ci) {
        if (world == null) return;
        if (SFCReMain.config.isEnableMod() && world.getDimension().hasSkyLight()) {
            if (SFCReMain.config.isEnableGPU()) {
                ensureInitGpu();
            }
            checkConfigChange();
            if (SFCReMain.config.isEnableGPU()) {
                gpuRenderer.render(world, player, matrices, projectionMatrix, tickDelta, cameraX, cameraY, cameraZ);
            }
            SFCReClient.RENDERER.render(world, matrices, projectionMatrix, tickDelta, cameraX, cameraY, cameraZ);
            ci.cancel();
        }
    }

    private static volatile boolean configChanged = false;

    public static void setConfigChanged() {
        configChanged = true;
    }

    private static void checkConfigChange() {
        if (!configChanged) return;
        configChanged = false;
        if (SFCReMain.config.isEnableGPU()) {
            gpuRenderer.onConfigChanged();
        }
    }

    private static boolean gpuInited = false;

    private static void ensureInitGpu() {
        if (gpuInited) return;
        gpuInited = true;
        GpuGenNoiseGroupOffset.ensureInitStatic();
        GpuSimplexNoise.ensureInitStatic();
        gpuRenderer = new GpuRenderer();
    }
}
