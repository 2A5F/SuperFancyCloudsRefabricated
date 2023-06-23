package com.rimo.sfcr.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.rimo.sfcr.SFCReMain;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.commons.io.IOUtils;
import org.lwjgl.opengl.GL44C;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Environment(EnvType.CLIENT)
public class ShaderUtils {
    public static int LoadShaderRaw(String path) {
        RenderSystem.assertOnRenderThreadOrInit();
        String code;
        try (var is = SFCReMain.class.getClassLoader().getResourceAsStream(path)) {
            code = IOUtils.toString(Objects.requireNonNull(is), StandardCharsets.UTF_8);
        } catch (IOException e) {
            SFCReMain.LOGGER.error("Could not load shader: <" + path + ">");
            return -1;
        }
        var shader = GL44C.glCreateShader(GL44C.GL_COMPUTE_SHADER);
        GL44C.glShaderSource(shader, code);
        GL44C.glCompileShader(shader);
        var compiled = GL44C.glGetShaderi(shader, GL44C.GL_COMPILE_STATUS);
        if (compiled == 0) {
            var log = GL44C.glGetShaderInfoLog(shader);
            SFCReMain.LOGGER.error("Could not compile shader <" + path + "> ; shader info: " + log);
            GL44C.glDeleteShader(shader);
            return -1;
        }
        return shader;
    }

    public static int LoadComputeProgramRaw(String path) {
        RenderSystem.assertOnRenderThreadOrInit();
        var shader = ShaderUtils.LoadShaderRaw(path);
        if (shader == -1) return -1;
        var program = GL44C.glCreateProgram();
        GL44C.glAttachShader(program, shader);
        GL44C.glLinkProgram(program);
        GL44C.glDeleteShader(shader);
        var linked = GL44C.glGetProgrami(program, GL44C.GL_LINK_STATUS);
        if (linked == 0) {
            var log = GL44C.glGetProgramInfoLog(program);
            SFCReMain.LOGGER.error("Could not link shader program <" + path + "> ; program info: " + log);
            GL44C.glDeleteProgram(program);
            return -1;
        }
        return program;
    }
}
