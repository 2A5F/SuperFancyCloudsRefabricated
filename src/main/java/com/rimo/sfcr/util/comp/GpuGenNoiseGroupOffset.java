package com.rimo.sfcr.util.comp;

import com.mojang.blaze3d.systems.RenderSystem;
import com.rimo.sfcr.util.MathUtils;
import com.rimo.sfcr.util.gl.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.lwjgl.opengl.GL45C;

@Environment(EnvType.CLIENT)
public class GpuGenNoiseGroupOffset implements AutoCloseable {
    private static ComputeShaderProgram shader;

    public static void ensureInitStatic() {
        RenderSystem.assertOnRenderThreadOrInit();
        if (shader != null) return;
        shader = ComputeShaderProgram.load("assets/sfcr/shaders/gen_noise_group_offset.comp");
    }

    private final IVec3 origin;
    private final GlBuffer arg_origin;

    public GpuGenNoiseGroupOffset() {
        this(0, 0, 0);
    }

    public GpuGenNoiseGroupOffset(int originX, int originY, int originZ) {
        origin = new IVec3(originX, originY, originZ);
        arg_origin = GlBuffer.createUniform();
        uploadOrigin();
    }

    private void uploadOrigin() {
        arg_origin.bind();
        arg_origin.upload(origin.xyz, GL45C.GL_DYNAMIC_DRAW);
        arg_origin.unbind();
    }

    public void setOrigin(int originX, int originY, int originZ) {
        if (origin.eqXYZ(originX, originY, originZ)) return;
        origin.setXYZ(originX, originY, originZ);
        this.uploadOrigin();
    }

    @Override
    public void close() {
        arg_origin.close();
    }

    public void calc(GlTexture group_offset) {
        RenderSystem.assertOnRenderThreadOrInit();
        int group_x = MathUtils.ceilDiv(MathUtils.ceil8(group_offset.width), 8);
        int group_y = MathUtils.ceilDiv(MathUtils.ceil8(group_offset.height), 8);
        int group_z = MathUtils.ceilDiv(MathUtils.ceil8(group_offset.depth), 8);
        GL45C.glUseProgram(shader.program);
        GlErr.check();
        GL45C.glBindBufferBase(GL45C.GL_UNIFORM_BUFFER, 0, arg_origin.buffer);
        GlErr.check();
        GL45C.glBindImageTexture(1, group_offset.texture, 0, true, 0, GL45C.GL_WRITE_ONLY, GL45C.GL_RGBA32I);
        GlErr.check();
        GL45C.glDispatchCompute(group_x, group_y, group_z);
        GlErr.check();
        GL45C.glMemoryBarrier(GL45C.GL_SHADER_IMAGE_ACCESS_BARRIER_BIT);
        GlErr.check();
    }
}
