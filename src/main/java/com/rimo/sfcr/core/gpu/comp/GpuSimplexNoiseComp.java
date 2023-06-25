package com.rimo.sfcr.core.gpu.comp;

import com.mojang.blaze3d.systems.RenderSystem;
import com.rimo.sfcr.util.MathUtils;
import com.rimo.sfcr.util.gl.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.random.Random;
import org.lwjgl.opengl.GL45C;

@Environment(EnvType.CLIENT)
public class GpuSimplexNoiseComp implements AutoCloseable {

    private static ComputeShaderProgram shader;

    public static void ensureInitStatic() {
        RenderSystem.assertOnRenderThreadOrInit();
        if (shader != null) return;
        shader = ComputeShaderProgram.load("assets/sfcr/shaders/simplex_noise.comp");
    }

    private final DVec3 origin;
    private final DVec3 scale;
    private final DVec3 offset;

    private final GlBuffer arg_origin;
    private final GlBuffer arg_scale;
    private final GlBuffer arg_offset;

    public GpuSimplexNoiseComp(Random random, double scaleX, double scaleY, double scaleZ) {
        RenderSystem.assertOnRenderThreadOrInit();

        origin = new DVec3(0, 0, 0);
        reGenOrigin(random);

        scale = new DVec3(scaleX, scaleY, scaleZ);
        offset = new DVec3(0, 0, 0);

        arg_origin = GlBuffer.createUniform();
        uploadOrigin();

        arg_scale = GlBuffer.createUniform();
        uploadScale();

        arg_offset = GlBuffer.createUniform();
        uploadOffset();
    }

    private void uploadOrigin() {
        arg_origin.bind();
        arg_origin.upload(origin.xyz, GL45C.GL_DYNAMIC_DRAW);
        arg_origin.unbind();
    }

    private void uploadScale() {
        arg_scale.bind();
        arg_scale.upload(scale.xyz, GL45C.GL_DYNAMIC_DRAW);
        arg_scale.unbind();
    }

    private void uploadOffset() {
        arg_offset.bind();
        arg_offset.upload(offset.xyz, GL45C.GL_DYNAMIC_DRAW);
        arg_offset.unbind();
    }

    private void reGenOrigin(Random random) {
        origin.setXYZ(random.nextDouble() * 256.0, random.nextDouble() * 256.0, random.nextDouble() * 256.0);
    }

    public void reRand(Random random) {
        RenderSystem.assertOnRenderThreadOrInit();
        reGenOrigin(random);
        uploadOrigin();
    }

    public void setScale(double scaleX, double scaleY, double scaleZ) {
        if (scale.eqXYZ(scaleX, scaleY, scaleZ)) return;
        RenderSystem.assertOnRenderThreadOrInit();
        scale.setXYZ(scaleX, scaleY, scaleZ);
        uploadScale();
    }

    public void setOffset(double offsetX, double offsetY, double offsetZ) {
        if (offset.eqXYZ(offsetX, offsetY, offsetZ)) return;
        RenderSystem.assertOnRenderThreadOrInit();
        offset.setXYZ(offsetX, offsetY, offsetZ);
        uploadOffset();
    }

    @Override
    public void close() {
        arg_origin.close();
        arg_scale.close();
        arg_offset.close();
    }

    public void calc(GlTexture sample_result) {
        RenderSystem.assertOnRenderThreadOrInit();
        int group_x = MathUtils.ceilDiv(sample_result.width, 8);
        int group_y = MathUtils.ceilDiv(sample_result.height, 8);
        int group_z = MathUtils.ceilDiv(sample_result.depth, 8);
        GL45C.glUseProgram(shader.program);
        GlErr.check();
        GL45C.glBindBufferBase(arg_origin.target, 0, arg_origin.buffer);
        GlErr.check();
        GL45C.glBindBufferBase(arg_scale.target, 1, arg_scale.buffer);
        GlErr.check();
        GL45C.glBindBufferBase(arg_offset.target, 2, arg_offset.buffer);
        GlErr.check();
        GL45C.glBindImageTexture(3, sample_result.texture, 0, true, 0, GL45C.GL_WRITE_ONLY, GL45C.GL_R32F);
        GlErr.check();
        GL45C.glDispatchCompute(group_x, group_y, group_z);
        GlErr.check();
        GL45C.glMemoryBarrier(GL45C.GL_SHADER_IMAGE_ACCESS_BARRIER_BIT);
        GlErr.check();
    }
}
