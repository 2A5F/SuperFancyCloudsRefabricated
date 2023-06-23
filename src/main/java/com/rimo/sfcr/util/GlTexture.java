package com.rimo.sfcr.util;

import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.lwjgl.opengl.GL44C;

@Environment(EnvType.CLIENT)
public class GlTexture implements AutoCloseable {
    public final int texture;
    public final int target;
    public final int width;
    public final int height;
    public final int depth;
    private boolean bilinear;
    private boolean mipmap;

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

    private GlTexture(int texture, int target, int width, int height, int depth, boolean bilinear, boolean mipmap) {
        this.texture = texture;
        this.target = target;
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.bilinear = bilinear;
        this.mipmap = mipmap;
    }

    public static GlTexture create3D(int width, int height, int depth, int format) {
        return create3D(width, height, depth, format, false, false);
    }

    public static GlTexture create3D(int width, int height, int depth, int format, boolean bilinear, boolean mipmap) {
        RenderSystem.assertOnRenderThreadOrInit();
        var tex = TextureUtil.generateTextureId();
        var target = GL44C.GL_TEXTURE_3D;
        GL44C.glBindTexture(target, tex);
        setFilterInternal(target, bilinear, mipmap);
        GL44C.glTexStorage3D(target, 1, format, width, height, depth);
        return new GlTexture(tex, target, width, height, depth, bilinear, mipmap);
    }

    public void bind() {
        RenderSystem.assertOnRenderThreadOrInit();
        GL44C.glBindTexture(target, texture);
    }

    public void setFilter(boolean bilinear, boolean mipmap) {
        RenderSystem.assertOnRenderThreadOrInit();
        this.bilinear = bilinear;
        this.mipmap = mipmap;
        this.bind();
        setFilterInternal(target, bilinear, mipmap);
    }

    private static void setFilterInternal(int target, boolean bilinear, boolean mipmap) {
        int min, mag;
        if (bilinear) {
            min = mipmap ? GL44C.GL_LINEAR_MIPMAP_LINEAR : GL44C.GL_LINEAR;
            mag = GL44C.GL_LINEAR;
        } else {
            min = mipmap ? GL44C.GL_NEAREST_MIPMAP_LINEAR : GL44C.GL_NEAREST;
            mag = GL44C.GL_NEAREST;
        }

        GL44C.glTexParameteri(target, GL44C.GL_TEXTURE_MIN_FILTER, min);
        GLErr.Check();
        GL44C.glTexParameteri(target, GL44C.GL_TEXTURE_MAG_FILTER, mag);
        GLErr.Check();
    }

    public boolean isBilinear() {
        return bilinear;
    }

    public boolean isMipmap() {
        return mipmap;
    }

}
