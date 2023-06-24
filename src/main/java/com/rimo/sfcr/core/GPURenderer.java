package com.rimo.sfcr.core;

import com.rimo.sfcr.SFCReMain;
import com.rimo.sfcr.util.GLErr;
import com.rimo.sfcr.util.GlTexture;
import com.rimo.sfcr.util.GpuSimplexNoise;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.noise.SimplexNoiseSampler;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL45C;

import java.lang.reflect.Array;
import java.nio.FloatBuffer;
import java.util.Arrays;

public class GPURenderer implements AutoCloseable {

    GpuSimplexNoise simplexNoise;

    GlTexture group_offset;
    GlTexture sample_result;

    @Override
    public void close() {
        if (simplexNoise != null) simplexNoise.close();
        if (group_offset != null) group_offset.close();
        if (sample_result != null) sample_result.close();
        simplexNoise = null;
        group_offset = null;
        sample_result = null;
    }

    public long seed = SFCReMain.RUNTIME.seed;

    public void ensureInit() {
        GpuSimplexNoise.ensureInitStatic();
        if (simplexNoise == null || seed != SFCReMain.RUNTIME.seed) {
            seed = SFCReMain.RUNTIME.seed;
            if (simplexNoise != null) simplexNoise.close();
            simplexNoise = new GpuSimplexNoise(Random.create(seed), 0.25);
        }
        if (group_offset == null) {
            group_offset = GlTexture.create3D(1, 1, 1, GL45C.GL_RGBA32I);
        }
        if (sample_result == null) {
            sample_result = GlTexture.create3D(8, 8, 8, GL45C.GL_R32F);
        }
    }

    public FloatBuffer buf = BufferUtils.createFloatBuffer(8 * 8 * 8);

    public Boolean first = true;

    public void render(@Nullable ClientWorld world, MatrixStack matrices, Matrix4f projectionMatrix, float tickDelta, double cameraX, double cameraY, double cameraZ) {
        simplexNoise.calc(group_offset, sample_result);

        GL45C.glMemoryBarrier(GL45C.GL_CLIENT_MAPPED_BUFFER_BARRIER_BIT);
        GLErr.check();
        GL45C.glGetTextureSubImage(sample_result.texture, 0, 0, 0, 0, 8, 8, 8, GL45C.GL_RED, GL45C.GL_FLOAT, buf);
        GLErr.check();
        if (first) {
            first = false;
            var arr = new float[512];
            buf.get(0, arr);
            SFCReMain.LOGGER.info(Arrays.toString(arr));
        }
    }


}
