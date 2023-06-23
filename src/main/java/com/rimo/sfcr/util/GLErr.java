package com.rimo.sfcr.util;

import com.rimo.sfcr.SFCReMain;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import static org.lwjgl.opengl.ARBImaging.GL_TABLE_TOO_LARGE;
import static org.lwjgl.opengl.GL44C.*;
import static org.lwjgl.system.APIUtil.apiUnknownToken;

@Environment(EnvType.CLIENT)
public class GLErr {
    public static void Check() {
        var err = glGetError();
        if (err == 0) return;
        SFCReMain.LOGGER.error("OpenGL Error: (" + err + ") " + getErrorString(err));
    }

    public static String getErrorString(int errorCode) {
        return switch (errorCode) {
            case GL_NO_ERROR -> "No error";
            case GL_INVALID_ENUM -> "Enum argument out of range";
            case GL_INVALID_VALUE -> "Numeric argument out of range";
            case GL_INVALID_OPERATION -> "Operation illegal in current state";
            case GL_STACK_OVERFLOW -> "Command would cause a stack overflow";
            case GL_STACK_UNDERFLOW -> "Command would cause a stack underflow";
            case GL_OUT_OF_MEMORY -> "Not enough memory left to execute command";
            case GL_INVALID_FRAMEBUFFER_OPERATION -> "Framebuffer object is not complete";
            case GL_TABLE_TOO_LARGE -> "The specified table is too large";
            default -> apiUnknownToken(errorCode);
        };
    }
}
