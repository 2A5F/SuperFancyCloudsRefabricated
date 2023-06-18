package com.rimo.sfcr.util;

import com.rimo.sfcr.SFCReMain;

import static org.lwjgl.opengl.ARBImaging.GL_TABLE_TOO_LARGE;
import static org.lwjgl.opengl.GL44C.*;
import static org.lwjgl.system.APIUtil.apiUnknownToken;

public class GLErr {
    public static void Check() {
        var err = glGetError();
        if (err == 0) return;
        SFCReMain.LOGGER.error("OpenGL Error: (" + err + ") " + getErrorString(err));
    }

    public static String getErrorString(int errorCode) {
        switch ( errorCode ) {
            case GL_NO_ERROR:
                return "No error";
            case GL_INVALID_ENUM:
                return "Enum argument out of range";
            case GL_INVALID_VALUE:
                return "Numeric argument out of range";
            case GL_INVALID_OPERATION:
                return "Operation illegal in current state";
            case GL_STACK_OVERFLOW:
                return "Command would cause a stack overflow";
            case GL_STACK_UNDERFLOW:
                return "Command would cause a stack underflow";
            case GL_OUT_OF_MEMORY:
                return "Not enough memory left to execute command";
            case GL_INVALID_FRAMEBUFFER_OPERATION:
                return "Framebuffer object is not complete";
            case GL_TABLE_TOO_LARGE:
                return "The specified table is too large";
            default:
                return apiUnknownToken(errorCode);
        }
    }
}
