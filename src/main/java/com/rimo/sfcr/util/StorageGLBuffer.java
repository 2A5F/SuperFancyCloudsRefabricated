package com.rimo.sfcr.util;

import org.lwjgl.opengl.GL44C;

public class StorageGLBuffer extends GLBuffer {
    @Override
    public void bind() {
        GL44C.glBindBuffer(GL44C.GL_SHADER_STORAGE_BUFFER, buffer);
        GLErr.Check();
    }

    @Override
    public void unbind() {
        GL44C.glBindBuffer(GL44C.GL_SHADER_STORAGE_BUFFER, 0);
        GLErr.Check();
    }

    @Override
    public void uploadImmutable(int[] data) {
        GL44C.glBufferStorage(GL44C.GL_SHADER_STORAGE_BUFFER,  data, 0);
        GLErr.Check();
    }

    @Override
    public void upload(int[] data) {
        GL44C.glBufferData(GL44C.GL_SHADER_STORAGE_BUFFER,  data, 0);
        GLErr.Check();
    }
}
