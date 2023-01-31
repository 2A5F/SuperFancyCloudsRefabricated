package com.rimo.sfcr;

import org.joml.Matrix4f;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.noise.SimplexNoiseSampler;
import net.minecraft.util.math.ColorHelper;

public class SFCReRenderer {

	private final Identifier whiteTexture = new Identifier("sfcr", "white.png");
	
	private final SFCReConfig config = AutoConfig.getConfigHolder(SFCReConfig.class).getConfig();
	private int cloudRenderDistance = config.cloudRenderDistance;
	private int cloudLayerThickness = config.cloudLayerThickness;
	
	private static final boolean hasCloudsHeightModifier = FabricLoader.getInstance().isModLoaded("sodiumextra")||FabricLoader.getInstance().isModLoaded("raisedclouds");

	public SimplexNoiseSampler cloudNoise = new SimplexNoiseSampler(net.minecraft.util.math.random.Random.create());

	public VertexBuffer cloudBuffer;

	public boolean[][][] _cloudData = new boolean[cloudRenderDistance][cloudLayerThickness][cloudRenderDistance];

	public Thread dataProcessThread;
	public boolean isProcessingData = false;

	public int moveTimer = 40;
	public double partialOffset = 0;
	public double partialOffsetSecondary = 0;
	public int cloudRenderDistanceOffset = (cloudRenderDistance - 96) / 2 * 16;	//Idk why "*16" but it work fine.

	public double time;

	public int fullOffset = 0;

	public double xScroll;
	public double zScroll;

	public BufferBuilder.BuiltBuffer cb;

	public void init() {
		cloudNoise = new SimplexNoiseSampler(net.minecraft.util.math.random.Random.create());
		isProcessingData = false;
	}

	@SuppressWarnings("resource")
	public void tick() {

		if (MinecraftClient.getInstance().player == null)
			return;
		
		if (!config.enableMod)
			return;

		//If already processing, don't start up again.
		if (isProcessingData)
			return;

		var player = MinecraftClient.getInstance().player;

		var xScroll = MathHelper.floor(player.getX() / 16) * 16;
		var zScroll = MathHelper.floor(player.getZ() / 16) * 16;

		int timeOffset = (int) (Math.floor(time / 6) * 6);

		if (timeOffset != moveTimer || xScroll != this.xScroll || zScroll != this.zScroll) {
			moveTimer = timeOffset;
			isProcessingData = true;

			dataProcessThread = new Thread(() -> collectCloudData(xScroll, zScroll));
			dataProcessThread.start();
		}
	}

	public void render(ClientWorld world, MatrixStack matrices, Matrix4f projectionMatrix, float tickDelta, double cameraX, double cameraY, double cameraZ) {
		
		float f = world.getDimensionEffects().getCloudsHeight();
		if (!hasCloudsHeightModifier)
			f = config.cloudHeight;
		
		if (!Float.isNaN(f)) {
			//Setup render system
			RenderSystem.disableCull();
			RenderSystem.enableBlend();
			RenderSystem.enableDepthTest();
			RenderSystem.blendFuncSeparate(
					GlStateManager.SrcFactor.SRC_ALPHA,
					GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA,
					GlStateManager.SrcFactor.ONE,
					GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA
			);
			RenderSystem.depthMask(true);

			Vec3d cloudColor = world.getCloudsColor(tickDelta);

			synchronized (this) {
				//Fix up partial offset...
				partialOffset += MinecraftClient.getInstance().getLastFrameDuration() * 0.25f * 0.25f;
				partialOffsetSecondary += MinecraftClient.getInstance().getLastFrameDuration() * 0.25f * 0.25f;

				time += MinecraftClient.getInstance().getLastFrameDuration() / 20.0f;

				var cb = cloudBuffer;

				if (cb == null && this.cb != null && !isProcessingData) {
					cloudBuffer = new VertexBuffer();
					cloudBuffer.bind();
					cloudBuffer.upload(this.cb);
					cb = cloudBuffer;
					VertexBuffer.unbind();
				}

				if (cb != null) {
					//Setup shader
					RenderSystem.setShader(GameRenderer::getPositionTexColorNormalProgram);
					RenderSystem.setShaderTexture(0, whiteTexture);
					if (config.enableFog) {
						BackgroundRenderer.setFogBlack();
						RenderSystem.setShaderFogStart(RenderSystem.getShaderFogStart() * config.fogDistance);
						RenderSystem.setShaderFogEnd(RenderSystem.getShaderFogEnd() * 2 * config.fogDistance);
					} else {
						BackgroundRenderer.clearFog();
					}

					RenderSystem.setShaderColor((float) cloudColor.x, (float) cloudColor.y, (float) cloudColor.z, 1);

					matrices.push();
					matrices.translate(-cameraX, -cameraY, -cameraZ);
					matrices.translate(xScroll - cloudRenderDistanceOffset, f - 15, zScroll + partialOffset - cloudRenderDistanceOffset);
					cb.bind();

					for (int s = 0; s < 2; ++s) {
						if (s == 0) {
							RenderSystem.colorMask(false, false, false, false);
						} else {
							RenderSystem.colorMask(true, true, true, true);
						}

						ShaderProgram shaderProgram = RenderSystem.getShader();
						cb.draw(matrices.peek().getPositionMatrix(), projectionMatrix, shaderProgram);
					}

					VertexBuffer.unbind();
					matrices.pop();

					//Restore render system
					RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
					RenderSystem.enableCull();
					RenderSystem.disableBlend();
				}
			}
		}
	}

	public void clean() {

		try {
			if (dataProcessThread != null)
				dataProcessThread.join();
		} catch (Exception e) {
			//Ignore...
		}
	}

	public BufferBuilder builder = new BufferBuilder(2097152);
	public FloatArrayList vertexList = new FloatArrayList();
	public ByteArrayList normalList = new ByteArrayList();

	private final float[][] normals = {
			{1, 0, 0},
			{-1, 0, 0},
			{0, 1, 0},
			{0, -1, 0},
			{0, 0, 1},
			{0, 0, -1},
	};

	private final int[] colors = {
			ColorHelper.Argb.getArgb((int) (255 * 0.8f), (int) (255 * 0.95f), (int) (255 * 0.9f), (int) (255 * 0.9f)),
			ColorHelper.Argb.getArgb((int) (255 * 0.8f), (int) (255 * 0.75f), (int) (255 * 0.75f), (int) (255 * 0.75f)),
			ColorHelper.Argb.getArgb((int) (255 * 0.8f), 255, 255, 255),
			ColorHelper.Argb.getArgb((int) (255 * 0.8f), (int) (255 * 0.6f), (int) (255 * 0.6f), (int) (255 * 0.6f)),
			ColorHelper.Argb.getArgb((int) (255 * 0.8f), (int) (255 * 0.92f), (int) (255 * 0.85f), (int) (255 * 0.85f)),
			ColorHelper.Argb.getArgb((int) (255 * 0.8f), (int) (255 * 0.8f), (int) (255 * 0.8f), (int) (255 * 0.8f)),
	};

	double remappedValue(double noise) {
		return (Math.pow(Math.sin(Math.toRadians(((noise * 180) + 302) * 1.15)), 0.28) + noise - 0.5f) * 2;
	}

	private void collectCloudData(double scrollX, double scrollZ) {
		//Updating RenderDistance and Thickness if they changed.
		if (cloudRenderDistance != config.cloudRenderDistance || cloudLayerThickness != config.cloudLayerThickness) {
			cloudRenderDistance = config.cloudRenderDistance;
			cloudLayerThickness = config.cloudLayerThickness;
			_cloudData = new boolean[cloudRenderDistance][cloudLayerThickness][cloudRenderDistance];
			//Fix offset causing by cloudRenderDistance changed.
			cloudRenderDistanceOffset = (cloudRenderDistance - 96) / 2 * 16;
		}

		try {
			double startX = scrollX / 16;
			double startZ = scrollZ / 16;

			double timeOffset = Math.floor(time / 6) * 6;

			synchronized (this) {
				while (partialOffsetSecondary >= 16) {
					partialOffsetSecondary -= 16;
					fullOffset++;
				}
			}
			
			float baseFreq = 0.05f;
			float baseTimeFactor = 0.01f;

			float l1Freq = 0.09f;
			float l1TimeFactor = 0.02f;

			float l2Freq = 0.001f;
			float l2TimeFactor = 0.1f;

			for (int cx = 0; cx < cloudRenderDistance; cx++) {
				for (int cy = 0; cy < cloudLayerThickness; cy++) {
					for (int cz = 0; cz < cloudRenderDistance; cz++) {
						double cloudVal = cloudNoise.sample(
								(startX + cx + (timeOffset * baseTimeFactor)) * baseFreq,
								(cy - (timeOffset * baseTimeFactor * 2)) * baseFreq,
								(startZ + cz - fullOffset) * baseFreq
						);
						double cloudVal1 = cloudNoise.sample(
								(startX + cx + (timeOffset * l1TimeFactor)) * l1Freq,
								(cy - (timeOffset * l1TimeFactor)) * l1Freq,
								(startZ + cz - fullOffset) * l1Freq
						);
						double cloudVal2 = cloudNoise.sample(
								(startX + cx + (timeOffset * l2TimeFactor)) * l2Freq,
								0,
								(startZ + cz - fullOffset) * l2Freq
						);

						//Smooth floor function...
						cloudVal2 *= 3;
						cloudVal2 = (cloudVal2 - (Math.sin(Math.PI * 2 * cloudVal2) / (Math.PI * 2))) / 2.0f;

						cloudVal = ((cloudVal + (cloudVal1 * 0.8f)) / 1.8f) * cloudVal2;

						cloudVal = cloudVal * remappedValue(1 - ((double) (cy + 1) / 32));

						_cloudData[cx][cy][cz] = cloudVal > (0.5f);
					}
				}
			}

			var tmp = rebuildCloudMesh();

			synchronized (this) {
				cb = tmp;
				cloudBuffer = null;

				this.xScroll = scrollX;
				this.zScroll = scrollZ;

				while (partialOffset >= 16) {
					partialOffset -= 16;
				}
			}
		} catch (Exception e) {
			// -- Ignore...
		}

		isProcessingData = false;
	}

	public void addVertex(float x, float y, float z) {
		vertexList.add(x - 48);
		vertexList.add(y);
		vertexList.add(z - 48);
	}

	private BufferBuilder.BuiltBuffer rebuildCloudMesh() {

		vertexList.clear();
		normalList.clear();

		for (int cx = 0; cx < cloudRenderDistance; cx++) {
			for (int cy = 0; cy < cloudLayerThickness; cy++) {
				for (int cz = 0; cz < cloudRenderDistance; cz++) {
					if (!_cloudData[cx][cy][cz])
						continue;

					//Right
					if (cx == cloudRenderDistance - 1 || !_cloudData[cx + 1][cy][cz]) {
						addVertex(cx + 1, cy, cz);
						addVertex(cx + 1, cy, cz + 1);
						addVertex(cx + 1, cy + 1, cz + 1);
						addVertex(cx + 1, cy + 1, cz);

						normalList.add((byte) 0);
					}

					//Left....
					if (cx == 0 || !_cloudData[cx - 1][cy][cz]) {
						addVertex(cx, cy, cz);
						addVertex(cx, cy, cz + 1);
						addVertex(cx, cy + 1, cz + 1);
						addVertex(cx, cy + 1, cz);

						normalList.add((byte) 1);
					}

					//Up....
					if (cy == cloudLayerThickness - 1 || !_cloudData[cx][cy + 1][cz]) {
						addVertex(cx, cy + 1, cz);
						addVertex(cx + 1, cy + 1, cz);
						addVertex(cx + 1, cy + 1, cz + 1);
						addVertex(cx, cy + 1, cz + 1);

						normalList.add((byte) 2);
					}

					//Down
					if (cy == 0 || !_cloudData[cx][cy - 1][cz]) {
						addVertex(cx, cy, cz);
						addVertex(cx + 1, cy, cz);
						addVertex(cx + 1, cy, cz + 1);
						addVertex(cx, cy, cz + 1);

						normalList.add((byte) 3);
					}


					//Forward....
					if (cz == cloudRenderDistance - 1 || !_cloudData[cx][cy][cz + 1]) {
						addVertex(cx, cy, cz + 1);
						addVertex(cx + 1, cy, cz + 1);
						addVertex(cx + 1, cy + 1, cz + 1);
						addVertex(cx, cy + 1, cz + 1);

						normalList.add((byte) 4);
					}

					//Backward
					if (cz == 0 || !_cloudData[cx][cy][cz - 1]) {
						addVertex(cx, cy, cz);
						addVertex(cx + 1, cy, cz);
						addVertex(cx + 1, cy + 1, cz);
						addVertex(cx, cy + 1, cz);

						normalList.add((byte) 5);
					}
				}
			}
		}

		builder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR_NORMAL);

		try {
			int vertCount = vertexList.size() / 3;

			for (int i = 0; i < vertCount; i++) {
				int origin = i * 3;
				var x = vertexList.getFloat(origin) * 16;
				var y = vertexList.getFloat(origin + 1) * 8;
				var z = vertexList.getFloat(origin + 2) * 16;

				int normIndex = normalList.getByte(i / 4);
				var norm = normals[normIndex];
				var nx = norm[0];
				var ny = norm[1];
				var nz = norm[2];

				builder.vertex(x, y, z).texture(0.5f, 0.5f).color(colors[normIndex]).normal(nx, ny, nz).next();
			}
		} catch (Exception e) {
			// -- Ignore...
			SFCReMod.LOGGER.error(e.toString());
		}

		return builder.end();
	}
	
	//Push the config to Mixin.
	public int getFogDistance() {
		if (config.enableFog) {
			return config.fogDistance;
		}
		return config.getMaxFogDistance();
	}
	
	public boolean getModEnabled() {
		return config.enableMod;
	}
	
}
