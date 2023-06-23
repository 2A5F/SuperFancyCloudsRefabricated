package com.rimo.sfcr.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.lwjgl.opengl.GL44C;

@Environment(EnvType.CLIENT)
public abstract class GLBuffer implements AutoCloseable {
    public int buffer;

    public GLBuffer() {
        buffer = GL44C.glGenBuffers();
        GLErr.Check();
    }

    public static UniformGLBuffer createUniform() {
        return new UniformGLBuffer();
    }

    public static StorageGLBuffer createStorage() {
        return new StorageGLBuffer();
    }

    @Override
    public void close() {
        if (RenderSystem.isOnRenderThread()) {
            GL44C.glDeleteBuffers(buffer);
            GLErr.Check();
        } else {
            RenderSystem.recordRenderCall(() -> {
                GL44C.glDeleteBuffers(buffer);
                GLErr.Check();
            });
        }
    }

    public abstract void bind();

    public abstract void unbind();

    public abstract void uploadImmutable(int[] data);

    public abstract void upload(int[] data);
}
