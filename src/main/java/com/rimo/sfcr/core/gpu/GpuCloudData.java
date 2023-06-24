package com.rimo.sfcr.core.gpu;

import com.rimo.sfcr.util.comp.GpuGenNoiseGroupOffset;
import com.rimo.sfcr.util.comp.GpuSimplexNoise;
import com.rimo.sfcr.util.gl.GlTexture;
import net.minecraft.util.math.random.Random;
import org.lwjgl.opengl.GL45C;

public class GpuCloudData implements AutoCloseable {
    private long seed;
    private final GpuGenNoiseGroupOffset genNoiseGroupOffset;
    private final GpuSimplexNoise simplexNoise;

    private GlTexture group_offset;
    private GlTexture sample_result;

    public GpuCloudData(long seed) {
        this.seed = seed;
        genNoiseGroupOffset = new GpuGenNoiseGroupOffset();
        simplexNoise = new GpuSimplexNoise(Random.create(seed), 0.1);
        group_offset = GlTexture.create3D(1, 1, 1, GL45C.GL_RGBA32I);
        sample_result = GlTexture.create3D(8, 8, 8, GL45C.GL_R32F);
    }

    @Override
    public void close() {
        genNoiseGroupOffset.close();
        simplexNoise.close();
        group_offset.close();
        sample_result.close();
    }

    public long getSeed() {
        return seed;
    }

    public void setSeed(long seed) {
        boolean changed = seed != this.seed;
        this.seed = seed;
        if (changed) onSeedChange();
    }

    private void onSeedChange() {
        simplexNoise.reRand(Random.create(seed));
    }

    public void calc() {
        simplexNoise.calc(group_offset, sample_result);
    }
}
