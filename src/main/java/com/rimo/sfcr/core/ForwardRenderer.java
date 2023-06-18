package com.rimo.sfcr.core;

import com.rimo.sfcr.SFCReClient;
import com.rimo.sfcr.SFCReMain;
import com.rimo.sfcr.util.GpuSimplexNoise;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
public class ForwardRenderer {
    static boolean inited = false;
    static long seed = SFCReMain.RUNTIME.seed;
    static GpuSimplexNoise simplexNoise;

    public static void render(@Nullable ClientWorld world, MatrixStack matrices, Matrix4f projectionMatrix, float tickDelta, double cameraX, double cameraY, double cameraZ, CallbackInfo ci) {
        if (world == null) return;
        MakeInit();
        if (SFCReMain.config.isEnableMod() && world.getDimension().hasSkyLight()) {
            MakeGpuSimplexNoise();
            SFCReClient.RENDERER.render(world, matrices, projectionMatrix, tickDelta, cameraX, cameraY, cameraZ);
            ci.cancel();
        }
    }

    private static void MakeInit() {
        if (inited) return;
        inited = true;
        GpuSimplexNoise.Init();
    }

    private static void MakeGpuSimplexNoise() {
        if (simplexNoise == null || seed != SFCReMain.RUNTIME.seed) {
            seed = SFCReMain.RUNTIME.seed;
            simplexNoise = new GpuSimplexNoise(Random.create(seed));
        }
    }
}
