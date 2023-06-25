package com.rimo.sfcr.core.gpu;

import com.mojang.blaze3d.systems.RenderSystem;
import com.rimo.sfcr.SFCReMain;
import com.rimo.sfcr.config.SFCReConfig;
import com.rimo.sfcr.util.MathUtils;
import com.rimo.sfcr.util.comp.GpuGenNoiseGroupOffset;
import com.rimo.sfcr.util.comp.GpuSimplexNoise;
import com.rimo.sfcr.util.gl.GlTexture;
import com.rimo.sfcr.util.gl.IVec3;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.random.Random;
import org.lwjgl.opengl.GL45C;

@Environment(EnvType.CLIENT)
public class GpuCloudData implements AutoCloseable {
    private static final SFCReConfig CONFIG = SFCReMain.CONFIGHOLDER.getConfig();

    private long seed;
    private final GpuGenNoiseGroupOffset genNoiseGroupOffset;
    private final GpuSimplexNoise simplexNoise;

    private GlTexture group_offset;
    private GlTexture sample_result;

    public double scaleXY;
    public double scaleZ;

    private int distance;
    private int thickness;

    private int cloud_size_xy;
    private int cloud_size_z;

    public GpuCloudData(long seed) {
        RenderSystem.assertOnRenderThreadOrInit();
        this.seed = seed;
        genNoiseGroupOffset = new GpuGenNoiseGroupOffset();
        scaleXY = CONFIG.getNoiseScaleXY();
        scaleZ = CONFIG.getNoiseScaleZ();
        simplexNoise = new GpuSimplexNoise(Random.create(seed), scaleXY, scaleXY, scaleZ);
        loadConfig();
    }

    @Override
    public void close() {
        genNoiseGroupOffset.close();
        simplexNoise.close();
        group_offset.close();
        sample_result.close();
    }

    public void setSeed(long seed) {
        boolean changed = seed != this.seed;
        this.seed = seed;
        if (changed) onSeedChange();
    }

    private void onSeedChange() {
        simplexNoise.reRand(Random.create(seed));
    }

    public void onConfigChanged() {
        loadConfig();
    }

    private void loadConfig() {
        RenderSystem.assertOnRenderThreadOrInit();
        scaleXY = CONFIG.getNoiseScaleXY();
        scaleZ = CONFIG.getNoiseScaleZ();
        simplexNoise.setScale(scaleXY, scaleXY, scaleZ);
        var new_distance = CONFIG.getCloudRenderDistance();
        var new_thickness = CONFIG.getCloudLayerThickness();
        if (new_distance != distance || new_thickness != thickness) {
            distance = CONFIG.getCloudRenderDistance();
            thickness = CONFIG.getCloudLayerThickness();
            cloud_size_xy = distance * 2 + 1;
            cloud_size_z = thickness;
            reCreateTextures();
        }
    }

    private void reCreateTextures() {
        var group_offset_xy = MathUtils.ceilDiv(cloud_size_xy, 8);
        var group_offset_z = MathUtils.ceilDiv(cloud_size_z, 8);
        group_offset = GlTexture.create3D(group_offset_xy, group_offset_xy, group_offset_z, GL45C.GL_RGBA32I);
        sample_result = GlTexture.create3D(cloud_size_xy, cloud_size_xy, cloud_size_z, GL45C.GL_R32F);
        group_offset.setLabel("sfcr.GpuCloudData.group_offset");
        sample_result.setLabel("sfcr.GpuCloudData.sample_result");
        needRegenNoiseGroupOffset = true;
        needResampleNoise = true;
    }

    private final IVec3 chunk = new IVec3(0, 0, 0);

    public void setChunk(int chunkX, int chunkY, int chunkZ) {
        if (chunk.eqXYZ(chunkX, chunkY, chunkZ)) return;
        chunk.setXYZ(chunkX, chunkY, chunkZ);
        simplexNoise.setOffset(chunkX * 16, chunkY * 16, chunkZ * 16);
        needResampleNoise = true;
    }

    private boolean needRegenNoiseGroupOffset = false;
    private boolean needResampleNoise = false;

    public void calc() {
        if (needRegenNoiseGroupOffset) {
            needRegenNoiseGroupOffset = false;
            genNoiseGroupOffset.calc(group_offset);
        }
        if (needResampleNoise) {
            needResampleNoise = false;
            simplexNoise.calc(group_offset, sample_result);
        }
    }
}
