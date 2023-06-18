package com.rimo.sfcr.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.random.Random;
import org.lwjgl.opengl.GL44;
import org.lwjgl.opengl.GLUtil;

@Environment(EnvType.CLIENT)
public class GpuSimplexNoise {

    private static ComputeShaderProgram shader;

    public static void Init() {
        shader = ComputeShaderProgram.load("assets/sfcr/shaders/simplex_noise.comp");
    }

    public final double originX;
    public final double originY;
    public final double originZ;

    public GLBuffer permutation;

    public GpuSimplexNoise(Random random) {
        this.originX = random.nextDouble() * 256.0;
        this.originY = random.nextDouble() * 256.0;
        this.originZ = random.nextDouble() * 256.0;

        this.permutation = GLBuffer.createUniform();
        this.permutation.bind();

        int i;
        var permutation = new int[512];
        for (i = 0; i < 256; permutation[i] = i++) {
        }

        for (i = 0; i < 256; ++i) {
            int j = random.nextInt(256 - i);
            int k = permutation[i];
            permutation[i] = permutation[j + i];
            permutation[j + i] = k;
        }

        this.permutation.uploadImmutable(permutation);
        this.permutation.unbind();
    }
}
