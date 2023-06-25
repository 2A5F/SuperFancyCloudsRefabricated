package com.rimo.sfcr.core.gpu.comp;

import com.mojang.blaze3d.systems.RenderSystem;
import com.rimo.sfcr.util.MathUtils;
import com.rimo.sfcr.util.gl.*;
import org.lwjgl.opengl.GL45C;

public class GpuCloudMeshComp implements AutoCloseable {

    private static ComputeShaderProgram shader;

    public static void ensureInitStatic() {
        RenderSystem.assertOnRenderThreadOrInit();
        if (shader != null) return;
        shader = ComputeShaderProgram.load("assets/sfcr/shaders/cloud_mesh.comp");
    }

    // no ownership
    private GlTexture simplex_noise;

    public GlBuffer verts;
    public GlBuffer tris;
    private final GlBuffer quad_count_acc;
    private final GlBuffer arg_lower_upper_bound;
    private final FVec2 lower_upper_bound;
    private static final int[] zero_int = new int[]{0};

    public GpuCloudMeshComp(float lower_bound, float upper_bound) {
        quad_count_acc = GlBuffer.createStorage();
        quad_count_acc.bind();
        quad_count_acc.initGpuOnly(4, GL45C.GL_DYNAMIC_DRAW);
        quad_count_acc.unbind();
        arg_lower_upper_bound = GlBuffer.createUniform();
        lower_upper_bound = new FVec2(lower_bound, upper_bound);
        uploadLowerUpperBound();
    }

    @Override
    public void close() {
        if (verts != null) verts.close();
        if (tris != null) tris.close();
        quad_count_acc.close();
    }

    public void setNoiseData(GlTexture simplex_noise) {
        this.simplex_noise = simplex_noise;
        if (verts != null) verts.close();
        if (tris != null) tris.close();
        verts = GlBuffer.createStorage();
        tris = GlBuffer.createStorage();
        verts.bind();
        verts.initGpuOnly(simplex_noise.width * simplex_noise.height * simplex_noise.depth * 2 * 4 * 4 * 4, GL45C.GL_DYNAMIC_DRAW);
        verts.unbind();
        tris.bind();
        tris.initGpuOnly(simplex_noise.width * simplex_noise.height * simplex_noise.depth * 2 * 6 * 4, GL45C.GL_DYNAMIC_DRAW);
        tris.unbind();
    }

    private void uploadLowerUpperBound() {
        arg_lower_upper_bound.bind();
        arg_lower_upper_bound.upload(lower_upper_bound.xy, GL45C.GL_DYNAMIC_DRAW);
        arg_lower_upper_bound.unbind();
    }

    private void clearQuadCountAcc() {
        quad_count_acc.bind();
        GL45C.glClearBufferData(quad_count_acc.target, GL45C.GL_R32I, GL45C.GL_RED_INTEGER, GL45C.GL_INT, zero_int);
        GlErr.check();
        quad_count_acc.unbind();
    }

    public void calc() {
        RenderSystem.assertOnRenderThreadOrInit();
        clearQuadCountAcc();
        int group_x = MathUtils.ceilDiv(simplex_noise.width, 8);
        int group_y = MathUtils.ceilDiv(simplex_noise.height, 8);
        int group_z = MathUtils.ceilDiv(simplex_noise.depth, 8);
        GL45C.glUseProgram(shader.program);
        GlErr.check();
        GL45C.glBindImageTexture(0, simplex_noise.texture, 0, true, 0, GL45C.GL_READ_ONLY, GL45C.GL_R32F);
        GlErr.check();
        GL45C.glBindBufferBase(verts.target, 1, verts.buffer);
        GlErr.check();
        GL45C.glBindBufferBase(tris.target, 2, tris.buffer);
        GlErr.check();
        GL45C.glBindBufferBase(quad_count_acc.target, 3, quad_count_acc.buffer);
        GlErr.check();
        GL45C.glBindBufferBase(arg_lower_upper_bound.target, 4, arg_lower_upper_bound.buffer);
        GlErr.check();
        GL45C.glDispatchCompute(group_x, group_y, group_z);
        GlErr.check();
        GL45C.glMemoryBarrier(GL45C.GL_SHADER_IMAGE_ACCESS_BARRIER_BIT);
        GlErr.check();
    }
}
