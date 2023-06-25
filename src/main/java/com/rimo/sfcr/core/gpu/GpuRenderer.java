package com.rimo.sfcr.core.gpu;

import com.rimo.sfcr.SFCReMain;
import com.rimo.sfcr.config.SFCReConfig;
import com.rimo.sfcr.core.RuntimeData;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class GpuRenderer implements AutoCloseable {

    private static final SFCReConfig CONFIG = SFCReMain.CONFIGHOLDER.getConfig();
    private final GpuCloudData cloudData;

    public GpuRenderer() {
        cloudData = new GpuCloudData(SFCReMain.RUNTIME.seed);
    }

    @Override
    public void close() {
        cloudData.close();
    }

    private long seed = SFCReMain.RUNTIME.seed;
    private double time;

    public void render(@Nullable ClientWorld world, ClientPlayerEntity player, MatrixStack matrices, Matrix4f projectionMatrix, float tickDelta, double cameraX, double cameraY, double cameraZ) {
        if (seed != SFCReMain.RUNTIME.seed) {
            seed = SFCReMain.RUNTIME.seed;
            cloudData.setSeed(seed);
        }
        if (time != SFCReMain.RUNTIME.time) {
            time = SFCReMain.RUNTIME.time;
            var dirArc = CONFIG.getCloudMoveDirection() * Math.PI / 180.0;
            var timeOffset = time * CONFIG.getCloudMoveSpeed();
            var dirX = Math.cos(dirArc);
            var dixZ = Math.sin(dirArc);
            var playerPos = player.getBlockPos();
            var chunkPos = new ChunkPos(playerPos.add((int) (dirX * timeOffset), 0, (int) (dixZ * timeOffset)));
            cloudData.setChunk(chunkPos.x, 0, chunkPos.z);
        }
        cloudData.calc();
    }

    public void onConfigChanged() {
        cloudData.onConfigChanged();
    }
}
