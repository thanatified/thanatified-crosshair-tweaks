package com.crosshairtweaks.mixin;

import com.crosshairtweaks.CrosshairTweaksClient;
import com.crosshairtweaks.config.CrosshairConfig;
import com.crosshairtweaks.render.CrosshairRenderer;
import com.crosshairtweaks.render.EnvironmentalBlend;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

	// If vanilla decided to call this method at all, it already checked
	// perspective/debug-hud/F1 etc, so we can just take over completely.
	@Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
	private void crosshairtweaks$replaceCrosshair(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
		ci.cancel();

		CrosshairConfig config = CrosshairTweaksClient.CONFIG;
		MinecraftClient client = MinecraftClient.getInstance();

		int centerX = context.getScaledWindowWidth() / 2;
		int centerY = context.getScaledWindowHeight() / 2;

		boolean targetingPlayer = config.playerEnabled && isLookingAtPlayer(client);

		var shape = targetingPlayer ? config.playerShape : config.shape;
		int size = targetingPlayer ? config.playerSize : config.size;
		int thickness = targetingPlayer ? config.playerThickness : config.thickness;
		int gap = config.gap;
		int baseColor = targetingPlayer ? config.playerColor : config.color;

		// Player-target crosshair intentionally skips the environmental
		// blend recolor - you want that one to stay a clear, consistent
		// "you're aiming at a person" signal, not shift with the background.
		int finalColor = targetingPlayer ? baseColor : EnvironmentalBlend.computeColor(config, baseColor);

		CrosshairRenderer.draw(context, centerX, centerY, shape, size, thickness, gap, finalColor,
				config.outline, config.outlineThickness, config.outlineColor);
	}

	private static boolean isLookingAtPlayer(MinecraftClient client) {
		HitResult target = client.crosshairTarget;
		if (!(target instanceof EntityHitResult entityHitResult)) {
			return false;
		}
		Entity entity = entityHitResult.getEntity();
		return entity instanceof PlayerEntity;
	}
}
