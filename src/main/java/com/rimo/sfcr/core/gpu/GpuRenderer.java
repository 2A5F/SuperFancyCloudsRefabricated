package com.rimo.sfcr.core.gpu;

import com.rimo.sfcr.SFCReMain;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class GpuRenderer implements AutoCloseable {

    GpuCloudData cloudData;

    public GpuRenderer() {
        cloudData = new GpuCloudData(SFCReMain.RUNTIME.seed);
    }

    @Override
    public void close() {
        cloudData.close();
    }

    public long seed = SFCReMain.RUNTIME.seed;

    public void render(@Nullable ClientWorld world, MatrixStack matrices, Matrix4f projectionMatrix, float tickDelta, double cameraX, double cameraY, double cameraZ) {
        if (seed != SFCReMain.RUNTIME.seed) {
            seed = SFCReMain.RUNTIME.seed;
            cloudData.setSeed(seed);
        }
        cloudData.calc();
    }


}
