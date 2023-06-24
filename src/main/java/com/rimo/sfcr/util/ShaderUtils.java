package com.rimo.sfcr.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.rimo.sfcr.SFCReMain;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.commons.io.IOUtils;
import org.lwjgl.opengl.GL45C;

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
        var shader = GL45C.glCreateShader(GL45C.GL_COMPUTE_SHADER);
        GL45C.glShaderSource(shader, code);
        GL45C.glCompileShader(shader);
        var compiled = GL45C.glGetShaderi(shader, GL45C.GL_COMPILE_STATUS);
        if (compiled == 0) {
            var log = GL45C.glGetShaderInfoLog(shader);
            SFCReMain.LOGGER.error("Could not compile shader <" + path + "> ; shader info: " + log);
            GL45C.glDeleteShader(shader);
            return -1;
        }
        return shader;
    }

    public static int LoadComputeProgramRaw(String path) {
        RenderSystem.assertOnRenderThreadOrInit();
        var shader = ShaderUtils.LoadShaderRaw(path);
        if (shader == -1) return -1;
        var program = GL45C.glCreateProgram();
        GL45C.glAttachShader(program, shader);
        GL45C.glLinkProgram(program);
        GL45C.glDeleteShader(shader);
        var linked = GL45C.glGetProgrami(program, GL45C.GL_LINK_STATUS);
        if (linked == 0) {
            var log = GL45C.glGetProgramInfoLog(program);
            SFCReMain.LOGGER.error("Could not link shader program <" + path + "> ; program info: " + log);
            GL45C.glDeleteProgram(program);
            return -1;
        }
        return program;
    }
}
