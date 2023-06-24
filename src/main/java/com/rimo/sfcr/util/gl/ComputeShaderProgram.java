package com.rimo.sfcr.util.gl;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.lwjgl.opengl.GL45C;

@Environment(EnvType.CLIENT)
public class ComputeShaderProgram implements AutoCloseable {

    public int program;

    public ComputeShaderProgram(int program) {
        this.program = program;
    }

    @Override
    public void close() {
        if (RenderSystem.isOnRenderThread()) {
            GL45C.glDeleteProgram(program);
        } else {
            RenderSystem.recordRenderCall(() -> {
                GL45C.glDeleteProgram(program);
            });
        }
    }

    public static ComputeShaderProgram load(String path) {
        RenderSystem.assertOnRenderThreadOrInit();
        var program = ShaderUtils.LoadComputeProgramRaw(path);
        if (program == -1) return null;
        return new ComputeShaderProgram(program);
    }
}
