package com.rimo.sfcr.core.gpu;

import com.mojang.blaze3d.systems.RenderSystem;
import com.rimo.sfcr.SFCReMain;
import com.rimo.sfcr.config.SFCReConfig;
import com.rimo.sfcr.core.gpu.comp.GpuCloudMeshComp;
import com.rimo.sfcr.util.MathUtils;
import com.rimo.sfcr.core.gpu.comp.GpuSimplexNoiseComp;
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
    private final GpuSimplexNoiseComp simplexNoise;
    public final GpuCloudMeshComp cloudMesh;

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
        scaleXY = CONFIG.getNoiseScaleXY();
        scaleZ = CONFIG.getNoiseScaleZ();
        simplexNoise = new GpuSimplexNoiseComp(Random.create(seed), scaleXY, scaleXY, scaleZ);
        cloudMesh = new GpuCloudMeshComp(CONFIG.getNoiseLowerBound(), CONFIG.getNoiseUpperBound());
        loadConfig();
    }

    @Override
    public void close() {
        simplexNoise.close();
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
        var new_distance = MathUtils.ceil8(CONFIG.getCloudRenderDistance());
        var new_thickness = CONFIG.getCloudLayerThickness();
        if (new_distance != distance || new_thickness != thickness) {
            distance = new_distance;
            thickness = new_thickness;
            cloud_size_xy = distance * 2;
            cloud_size_z = thickness;
            reCreateTextures();
        }
    }

    private void reCreateTextures() {
        sample_result = GlTexture.create3D(cloud_size_xy, cloud_size_xy, cloud_size_z, GL45C.GL_R32F);
        sample_result.setLabel("sfcr.GpuCloudData.sample_result");
        needResampleNoise = true;
        cloudMesh.setNoiseData(sample_result);
    }

    private final IVec3 chunk = new IVec3(0, 0, 0);

    public void setChunk(int chunkX, int chunkZ) {
        if (chunk.eqXYZ(chunkX, 0, chunkZ)) return;
        chunk.setXYZ(chunkX, 0, chunkZ);
        simplexNoise.setOffset(chunkX * 16 - distance, -thickness / 2.0, chunkZ * 16 - distance);
        needResampleNoise = true;
    }

    private boolean needResampleNoise = false;

    public void calc() {
        if (needResampleNoise) {
            needResampleNoise = false;
            simplexNoise.calc(sample_result);
        }
        cloudMesh.calc();
    }
}
