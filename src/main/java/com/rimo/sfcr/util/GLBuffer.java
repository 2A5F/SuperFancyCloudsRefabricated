package com.rimo.sfcr.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.lwjgl.opengl.GL45C;

@Environment(EnvType.CLIENT)
public abstract class GLBuffer implements AutoCloseable {
    public int buffer;

    public GLBuffer() {
        buffer = GL45C.glGenBuffers();
        GLErr.check();
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
            GL45C.glDeleteBuffers(buffer);
            GLErr.check();
        } else {
            RenderSystem.recordRenderCall(() -> {
                GL45C.glDeleteBuffers(buffer);
                GLErr.check();
            });
        }
    }

    public abstract void bind();

    public abstract void unbind();

    public abstract void uploadImmutable(int[] data);

    public abstract void uploadImmutable(double[] data);

    public abstract void upload(int[] data, int usage);

    public abstract void upload(double[] data, int usage);
}
