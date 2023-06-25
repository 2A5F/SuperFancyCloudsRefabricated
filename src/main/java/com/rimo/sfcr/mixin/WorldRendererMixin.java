package com.rimo.sfcr.mixin;

import com.rimo.sfcr.core.ForwardRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import org.joml.Matrix4f;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {

	@Shadow
	private @Nullable ClientWorld world;

	@Shadow
	@Final
	private MinecraftClient client;

	@Inject(method = "renderClouds", at = @At("HEAD"), cancellable = true)
	public void renderSFC(MatrixStack matrices, Matrix4f projectionMatrix, float tickDelta, double cameraX, double cameraY, double cameraZ, CallbackInfo ci) {
		ForwardRenderer.render(world, client.player, matrices, projectionMatrix, tickDelta, cameraX, cameraY, cameraZ, ci);
	}
}
