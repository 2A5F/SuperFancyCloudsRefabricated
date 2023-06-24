package com.rimo.sfcr.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.random.Random;
import org.lwjgl.opengl.GL45C;

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

    public double scaleX;
    public double scaleY;
    public double scaleZ;

    public GLBuffer args;
    public GLBuffer params;

    public GpuSimplexNoise(Random random) {
        this(random, 1);
    }

    public GpuSimplexNoise(Random random, double scale) {
        this(random, scale, scale, scale);
    }

    public GpuSimplexNoise(Random random, double scaleX, double scaleY, double scaleZ) {
        RenderSystem.assertOnRenderThreadOrInit();
        this.originX = random.nextDouble() * 256.0;
        this.originY = random.nextDouble() * 256.0;
        this.originZ = random.nextDouble() * 256.0;

        this.scaleX = scaleX;
        this.scaleY = scaleY;
        this.scaleZ = scaleZ;

        this.args = GLBuffer.createUniform();
        this.args.bind();
        this.args.uploadImmutable(new double[]{originX, originY, originZ});
        this.args.unbind();

        this.params = GLBuffer.createUniform();
        this.uploadScale();
    }

    private void uploadScale() {
        this.params.bind();
        this.params.upload(new double[]{scaleX, scaleY, scaleZ}, GL45C.GL_DYNAMIC_DRAW);
        this.params.unbind();
    }

    public void setScale(double scale) {
        setScale(scale, scale, scale);
    }

    public void setScale(double scaleX, double scaleY, double scaleZ) {
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        this.scaleZ = scaleZ;
        this.uploadScale();
    }

    @Override
    public void close() {
        shader.close();
        args.close();
        params.close();
    }

    public void calc(GlTexture group_offset, GlTexture sample_result) {
        RenderSystem.assertOnRenderThreadOrInit();
        int group_x = MathUtils.IntCeilDiv(sample_result.width, 8);
        int group_y = MathUtils.IntCeilDiv(sample_result.height, 8);
        int group_z = MathUtils.IntCeilDiv(sample_result.depth, 8);
        if (group_offset.width != group_x || group_offset.height != group_y || group_offset.depth != group_z)
            throw new RuntimeException("group_offset size must be same to group size");
        GL45C.glUseProgram(shader.program);
        GLErr.check();
        GL45C.glBindBufferBase(GL45C.GL_UNIFORM_BUFFER, 0, this.args.buffer);
        GLErr.check();
        GL45C.glBindImageTexture(1, group_offset.texture, 0, true, 0, GL45C.GL_READ_ONLY, GL45C.GL_RGBA32I);
        GLErr.check();
        GL45C.glBindImageTexture(2, sample_result.texture, 0, true, 0, GL45C.GL_WRITE_ONLY, GL45C.GL_R32F);
        GLErr.check();
        GL45C.glBindBufferBase(GL45C.GL_UNIFORM_BUFFER, 3, this.params.buffer);
        GLErr.check();
        GL45C.glDispatchCompute(group_x, group_y, group_z);
        GLErr.check();
        GL45C.glMemoryBarrier(GL45C.GL_SHADER_IMAGE_ACCESS_BARRIER_BIT);
        GLErr.check();
    }
}
