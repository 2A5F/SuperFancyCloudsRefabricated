package com.rimo.sfcr.util.gl;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.lwjgl.opengl.GL45C;

@Environment(EnvType.CLIENT)
public abstract class GlBuffer implements AutoCloseable {
    public int buffer;

    public GlBuffer() {
        buffer = GL45C.glGenBuffers();
        GlErr.check();
    }

    public static GlUniformBuffer createUniform() {
        return new GlUniformBuffer();
    }

    public static GlStorageBuffer createStorage() {
        return new GlStorageBuffer();
    }

    @Override
    public void close() {
        if (RenderSystem.isOnRenderThread()) {
            GL45C.glDeleteBuffers(buffer);
            GlErr.check();
        } else {
            RenderSystem.recordRenderCall(() -> {
                GL45C.glDeleteBuffers(buffer);
                GlErr.check();
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
