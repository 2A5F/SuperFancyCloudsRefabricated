package com.rimo.sfcr.util.gl;

import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.lwjgl.opengl.GL44C;
import org.lwjgl.opengl.GL45C;

@Environment(EnvType.CLIENT)
public class GlTexture implements AutoCloseable {
    public final int texture;
    public final int target;
    public final int format;
    public final int width;
    public final int height;
    public final int depth;

    @Override
    public void close() {
        if (RenderSystem.isOnRenderThread()) {
            TextureUtil.releaseTextureId(texture);
        } else {
            RenderSystem.recordRenderCall(() -> {
                TextureUtil.releaseTextureId(texture);
            });
        }
    }

    private GlTexture(int texture, int target, int format, int width, int height, int depth) {
        this.texture = texture;
        this.target = target;
        this.format = format;
        this.width = width;
        this.height = height;
        this.depth = depth;
    }

    public static GlTexture create1D(int length, int internalformat, int format, int type) {
        RenderSystem.assertOnRenderThreadOrInit();
        var tex = TextureUtil.generateTextureId();
        var target = GL45C.GL_TEXTURE_1D;
        GL45C.glBindTexture(target, tex);
        GlErr.check();
        GL45C.glTexImage1D(target, 0, internalformat, length, 0, format, type, 0);
        GlErr.check();
        GL45C.glBindTexture(target, 0);
        GlErr.check();
        return new GlTexture(tex, target, format, length, 1, 1);
    }

    public static GlTexture create3D(int width, int height, int depth, int format) {
        RenderSystem.assertOnRenderThreadOrInit();
        var tex = TextureUtil.generateTextureId();
        var target = GL45C.GL_TEXTURE_3D;
        GL45C.glBindTexture(target, tex);
        GlErr.check();
        GL45C.glTexStorage3D(target, 1, format, width, height, depth);
        GlErr.check();
        GL45C.glBindTexture(target, 0);
        GlErr.check();
        return new GlTexture(tex, target, format, width, height, depth);
    }

    public void upload1D(int format, int type, int[] data) {
        RenderSystem.assertOnRenderThreadOrInit();
        GL45C.glBindTexture(target, texture);
        GlErr.check();
        GL44C.glTexSubImage1D(target, 0, 0, width, format, type, data);
        GlErr.check();
        GL45C.glBindTexture(target, 0);
        GlErr.check();
    }
}
