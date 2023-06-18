package com.rimo.sfcr.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.lwjgl.opengl.GL44C;

import java.lang.ref.Cleaner;

@Environment(EnvType.CLIENT)
public class ComputeShaderProgram implements Cleaner.Cleanable {

    public int program;

    public ComputeShaderProgram(int program) {
        this.program = program;
    }

    @Override
    public void clean() {
        GL44C.glDeleteProgram(program);
    }

    public static ComputeShaderProgram load(String path) {
        var program = ShaderUtils.LoadComputeProgramRaw(path);
        if (program == -1) return null;
        return new ComputeShaderProgram(program); // todo
    }


}
