package com.rimo.sfcr.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.random.Random;
import org.lwjgl.opengl.GL44C;

@Environment(EnvType.CLIENT)
public class GpuSimplexNoise implements AutoCloseable {

    private static ComputeShaderProgram shader;

    public static void ensureInitStatic() {
        RenderSystem.assertOnRenderThreadOrInit();
        if (shader != null) return;
        shader = ComputeShaderProgram.load("assets/sfcr/shaders/simplex_noise.comp");
    }

    public final double originX;
    public final double originY;
    public final double originZ;

    public GLBuffer permutation;

    public GpuSimplexNoise(Random random) {
        RenderSystem.assertOnRenderThreadOrInit();
        this.originX = random.nextDouble() * 256.0;
        this.originY = random.nextDouble() * 256.0;
        this.originZ = random.nextDouble() * 256.0;

        this.permutation = GLBuffer.createUniform();
        this.permutation.bind();

        int i;
        var permutation = new int[512];
        for (i = 0; i < 256; permutation[i] = i++) {
        }

        for (i = 0; i < 256; ++i) {
            int j = random.nextInt(256 - i);
            int k = permutation[i];
            permutation[i] = permutation[j + i];
            permutation[j + i] = k;
        }

        this.permutation.uploadImmutable(permutation);
        this.permutation.unbind();
    }

    @Override
    public void close() {
        shader.close();
        permutation.close();
    }

    public void calc(GlTexture group_offset, GlTexture sample_result) {
        RenderSystem.assertOnRenderThreadOrInit();
        int group_x = MathUtils.IntCeilDiv(sample_result.width, 8);
        int group_y = MathUtils.IntCeilDiv(sample_result.height, 8);
        int group_z = MathUtils.IntCeilDiv(sample_result.depth, 8);
        if (group_offset.width != group_x || group_offset.height != group_y || group_offset.depth != group_z)
            throw new RuntimeException("group_offset size must be same to group size");
        GL44C.glUseProgram(shader.program);
        GL44C.glBindImageTexture(1, group_offset.texture, 0, false, 0, GL44C.GL_READ_ONLY, GL44C.GL_RGBA32I);
        GL44C.glBindImageTexture(2, sample_result.texture, 0, false, 0, GL44C.GL_WRITE_ONLY, GL44C.GL_R32F);
        GL44C.glDispatchCompute(group_x, group_y, group_z);
        GLErr.Check();
    }
}
