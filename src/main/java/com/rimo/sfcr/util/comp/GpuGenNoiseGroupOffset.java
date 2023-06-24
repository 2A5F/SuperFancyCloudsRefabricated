package com.rimo.sfcr.util.comp;

import com.mojang.blaze3d.systems.RenderSystem;
import com.rimo.sfcr.util.MathUtils;
import com.rimo.sfcr.util.gl.ComputeShaderProgram;
import com.rimo.sfcr.util.gl.GlBuffer;
import com.rimo.sfcr.util.gl.GlErr;
import com.rimo.sfcr.util.gl.GlTexture;
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

    public GlBuffer args;

    public int originX;
    public int originY;
    public int originZ;

    public GpuGenNoiseGroupOffset() {
        this(0, 0, 0);
    }

    public GpuGenNoiseGroupOffset(int originX, int originY, int originZ) {
        this.originX = originX;
        this.originY = originY;
        this.originZ = originZ;

        this.args = GlBuffer.createUniform();
        uploadOrigin();
    }

    private void uploadOrigin() {
        this.args.bind();
        this.args.upload(new int[]{originX, originY, originZ}, GL45C.GL_DYNAMIC_DRAW);
        this.args.unbind();
    }

    public void setOrigin(int originX, int originY, int originZ) {
        if (this.originX == originX && this.originY == originY && this.originZ == originZ) return;
        this.originX = originX;
        this.originY = originY;
        this.originZ = originZ;
        this.uploadOrigin();
    }

    @Override
    public void close() {
        args.close();
    }

    public void calc(GlTexture group_offset) {
        RenderSystem.assertOnRenderThreadOrInit();
        int group_x = MathUtils.ceil8(group_offset.width);
        int group_y = MathUtils.ceil8(group_offset.height);
        int group_z = MathUtils.ceil8(group_offset.depth);
        GL45C.glUseProgram(shader.program);
        GlErr.check();
        GL45C.glBindBufferBase(GL45C.GL_UNIFORM_BUFFER, 0, this.args.buffer);
        GlErr.check();
        GL45C.glBindImageTexture(1, group_offset.texture, 0, true, 0, GL45C.GL_WRITE_ONLY, GL45C.GL_RGBA32I);
        GlErr.check();
        GL45C.glDispatchCompute(group_x, group_y, group_z);
        GlErr.check();
        GL45C.glMemoryBarrier(GL45C.GL_SHADER_IMAGE_ACCESS_BARRIER_BIT);
        GlErr.check();
    }
}
