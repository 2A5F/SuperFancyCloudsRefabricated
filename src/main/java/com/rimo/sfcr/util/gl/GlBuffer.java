package com.rimo.sfcr.util.gl;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.lwjgl.opengl.GL45C;

@Environment(EnvType.CLIENT)
public class GlBuffer implements AutoCloseable {
    public int buffer;
    public int target;

    public GlBuffer(int target) {
        this.target = target;
        buffer = GL45C.glGenBuffers();
        GlErr.check();
    }

    public GlBuffer(int target, int buffer) {
        this.target = target;
        this.buffer = buffer;
    }

    public static GlBuffer createUniform() {
        return new GlBuffer(GL45C.GL_UNIFORM_BUFFER);
    }

    public static GlBuffer createStorage() {
        return new GlBuffer(GL45C.GL_SHADER_STORAGE_BUFFER);
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

    public void bind() {
        RenderSystem.assertOnRenderThreadOrInit();
        GL45C.glBindBuffer(target, buffer);
        GlErr.check();
    }

    public void unbind() {
        RenderSystem.assertOnRenderThreadOrInit();
        GL45C.glBindBuffer(target, 0);
        GlErr.check();
    }

    public void uploadImmutable(int[] data) {
        RenderSystem.assertOnRenderThreadOrInit();
        GL45C.glBufferStorage(target, data, 0);
        GlErr.check();
    }

    public void uploadImmutable(float[] data) {
        RenderSystem.assertOnRenderThreadOrInit();
        GL45C.glBufferStorage(target, data, 0);
        GlErr.check();
    }

    public void uploadImmutable(double[] data) {
        RenderSystem.assertOnRenderThreadOrInit();
        GL45C.glBufferStorage(target, data, 0);
        GlErr.check();
    }

    public void upload(int[] data, int usage) {
        RenderSystem.assertOnRenderThreadOrInit();
        GL45C.glBufferData(target, data, usage);
        GlErr.check();
    }

    public void upload(float[] data, int usage) {
        RenderSystem.assertOnRenderThreadOrInit();
        GL45C.glBufferData(target, data, usage);
        GlErr.check();
    }

    public void upload(double[] data, int usage) {
        RenderSystem.assertOnRenderThreadOrInit();
        GL45C.glBufferData(target, data, usage);
        GlErr.check();
    }

    public void initGpuOnly(int size, int usage) {
        RenderSystem.assertOnRenderThreadOrInit();
        GL45C.glBufferData(target, size, usage);
        GlErr.check();
    }
}
