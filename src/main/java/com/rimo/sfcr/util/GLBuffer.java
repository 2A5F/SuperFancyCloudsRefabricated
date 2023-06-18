package com.rimo.sfcr.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.lwjgl.opengl.GL44C;

import java.lang.ref.Cleaner;

@Environment(EnvType.CLIENT)
public abstract class GLBuffer implements Cleaner.Cleanable {
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
    public void clean() {
        GL44C.glDeleteBuffers(buffer);
        GLErr.Check();
    }

    public abstract void bind();

    public abstract void unbind();

    public abstract void uploadImmutable(int[] data);

    public abstract void upload(int[] data);
}
