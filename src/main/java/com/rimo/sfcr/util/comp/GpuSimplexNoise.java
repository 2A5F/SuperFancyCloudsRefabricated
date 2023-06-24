package com.rimo.sfcr.util.comp;

import com.mojang.blaze3d.systems.RenderSystem;
import com.rimo.sfcr.util.MathUtils;
import com.rimo.sfcr.util.gl.ComputeShaderProgram;
import com.rimo.sfcr.util.gl.GlBuffer;
import com.rimo.sfcr.util.gl.GlErr;
import com.rimo.sfcr.util.gl.GlTexture;
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

    private double originX;
    private double originY;
    private double originZ;

    private double scaleX;
    private double scaleY;
    private double scaleZ;

    private final GlBuffer args;
    private final GlBuffer params;

    public GpuSimplexNoise(Random random) {
        this(random, 1);
    }

    public GpuSimplexNoise(Random random, double scale) {
        this(random, scale, scale, scale);
    }

    public GpuSimplexNoise(Random random, double scaleX, double scaleY, double scaleZ) {
        RenderSystem.assertOnRenderThreadOrInit();

        reGenOrigin(random);

        this.scaleX = scaleX;
        this.scaleY = scaleY;
        this.scaleZ = scaleZ;

        args = GlBuffer.createUniform();
        uploadOrigin();

        params = GlBuffer.createUniform();
        uploadScale();
    }

    private void uploadOrigin() {
        args.bind();
        args.upload(new double[]{originX, originY, originZ}, GL45C.GL_DYNAMIC_DRAW);
        args.unbind();
    }

    private void uploadScale() {
        params.bind();
        params.upload(new double[]{scaleX, scaleY, scaleZ}, GL45C.GL_DYNAMIC_DRAW);
        params.unbind();
    }

    private void reGenOrigin(Random random) {
        originX = random.nextDouble() * 256.0;
        originY = random.nextDouble() * 256.0;
        originZ = random.nextDouble() * 256.0;
    }

    public void reRand(Random random) {
        RenderSystem.assertOnRenderThreadOrInit();
        reGenOrigin(random);
        uploadOrigin();
    }

    public void setScale(double scale) {
        setScale(scale, scale, scale);
    }

    public void setScale(double scaleX, double scaleY, double scaleZ) {
        if (this.scaleX == scaleX && this.scaleY == scaleY && this.scaleZ == scaleZ) return;
        RenderSystem.assertOnRenderThreadOrInit();
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        this.scaleZ = scaleZ;
        this.uploadScale();
    }

    @Override
    public void close() {
        args.close();
        params.close();
    }

    public void calc(GlTexture group_offset, GlTexture sample_result) {
        RenderSystem.assertOnRenderThreadOrInit();
        int group_x = MathUtils.ceilDiv(sample_result.width, 8);
        int group_y = MathUtils.ceilDiv(sample_result.height, 8);
        int group_z = MathUtils.ceilDiv(sample_result.depth, 8);
        if (group_offset.width != group_x || group_offset.height != group_y || group_offset.depth != group_z)
            throw new RuntimeException("group_offset size must be same to group size");
        GL45C.glUseProgram(shader.program);
        GlErr.check();
        GL45C.glBindBufferBase(GL45C.GL_UNIFORM_BUFFER, 0, this.args.buffer);
        GlErr.check();
        GL45C.glBindImageTexture(1, group_offset.texture, 0, true, 0, GL45C.GL_READ_ONLY, GL45C.GL_RGBA32I);
        GlErr.check();
        GL45C.glBindImageTexture(2, sample_result.texture, 0, true, 0, GL45C.GL_WRITE_ONLY, GL45C.GL_R32F);
        GlErr.check();
        GL45C.glBindBufferBase(GL45C.GL_UNIFORM_BUFFER, 3, this.params.buffer);
        GlErr.check();
        GL45C.glDispatchCompute(group_x, group_y, group_z);
        GlErr.check();
        GL45C.glMemoryBarrier(GL45C.GL_SHADER_IMAGE_ACCESS_BARRIER_BIT);
        GlErr.check();
    }
}
