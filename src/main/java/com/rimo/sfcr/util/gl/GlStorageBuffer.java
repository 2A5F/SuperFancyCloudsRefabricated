package com.rimo.sfcr.util.gl;

import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL45C;

public class GlStorageBuffer extends GlBuffer {
    @Override
    public void bind() {
        RenderSystem.assertOnRenderThreadOrInit();
        GL45C.glBindBuffer(GL45C.GL_SHADER_STORAGE_BUFFER, buffer);
        GlErr.check();
    }

    @Override
    public void unbind() {
        RenderSystem.assertOnRenderThreadOrInit();
        GL45C.glBindBuffer(GL45C.GL_SHADER_STORAGE_BUFFER, 0);
        GlErr.check();
    }

    @Override
    public void uploadImmutable(int[] data) {
        RenderSystem.assertOnRenderThreadOrInit();
        GL45C.glBufferStorage(GL45C.GL_SHADER_STORAGE_BUFFER,  data, 0);
        GlErr.check();
    }

    @Override
    public void uploadImmutable(double[] data) {
        RenderSystem.assertOnRenderThreadOrInit();
        GL45C.glBufferStorage(GL45C.GL_SHADER_STORAGE_BUFFER,  data, 0);
        GlErr.check();
    }

    @Override
    public void upload(int[] data, int usage) {
        RenderSystem.assertOnRenderThreadOrInit();
        GL45C.glBufferData(GL45C.GL_SHADER_STORAGE_BUFFER,  data, usage);
        GlErr.check();
    }

    @Override
    public void upload(double[] data, int usage) {
        RenderSystem.assertOnRenderThreadOrInit();
        GL45C.glBufferData(GL45C.GL_SHADER_STORAGE_BUFFER,  data, usage);
        GlErr.check();
    }
}
