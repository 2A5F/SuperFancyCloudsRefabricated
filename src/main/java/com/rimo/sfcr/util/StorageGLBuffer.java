package com.rimo.sfcr.util;

import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL44C;

public class StorageGLBuffer extends GLBuffer {
    @Override
    public void bind() {
        RenderSystem.assertOnRenderThreadOrInit();
        GL44C.glBindBuffer(GL44C.GL_SHADER_STORAGE_BUFFER, buffer);
        GLErr.Check();
    }

    @Override
    public void unbind() {
        RenderSystem.assertOnRenderThreadOrInit();
        GL44C.glBindBuffer(GL44C.GL_SHADER_STORAGE_BUFFER, 0);
        GLErr.Check();
    }

    @Override
    public void uploadImmutable(int[] data) {
        RenderSystem.assertOnRenderThreadOrInit();
        GL44C.glBufferStorage(GL44C.GL_SHADER_STORAGE_BUFFER,  data, 0);
        GLErr.Check();
    }

    @Override
    public void upload(int[] data) {
        RenderSystem.assertOnRenderThreadOrInit();
        GL44C.glBufferData(GL44C.GL_SHADER_STORAGE_BUFFER,  data, 0);
        GLErr.Check();
    }
}
